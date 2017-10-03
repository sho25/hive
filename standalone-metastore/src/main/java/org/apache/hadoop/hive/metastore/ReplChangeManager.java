begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|concurrent
operator|.
name|BasicThreadFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileChecksum
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|PathFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Trash
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|permission
operator|.
name|FsPermission
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hdfs
operator|.
name|DFSConfigKeys
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|MetaException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
operator|.
name|ConfVars
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|utils
operator|.
name|FileUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|ReplChangeManager
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReplChangeManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|private
name|ReplChangeManager
name|instance
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|inited
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|enabled
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|Path
name|cmroot
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|String
name|msUser
decl_stmt|;
specifier|private
name|String
name|msGroup
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ORIG_LOC_TAG
init|=
literal|"user.original-loc"
decl_stmt|;
specifier|static
specifier|final
name|String
name|REMAIN_IN_TRASH_TAG
init|=
literal|"user.remain-in-trash"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|URI_FRAGMENT_SEPARATOR
init|=
literal|"#"
decl_stmt|;
specifier|public
enum|enum
name|RecycleType
block|{
name|MOVE
block|,
name|COPY
block|}
specifier|public
specifier|static
class|class
name|FileInfo
block|{
name|FileSystem
name|srcFs
decl_stmt|;
name|Path
name|sourcePath
decl_stmt|;
name|Path
name|cmPath
decl_stmt|;
name|String
name|checkSum
decl_stmt|;
name|boolean
name|useSourcePath
decl_stmt|;
specifier|public
name|FileInfo
parameter_list|(
name|FileSystem
name|srcFs
parameter_list|,
name|Path
name|sourcePath
parameter_list|)
block|{
name|this
operator|.
name|srcFs
operator|=
name|srcFs
expr_stmt|;
name|this
operator|.
name|sourcePath
operator|=
name|sourcePath
expr_stmt|;
name|this
operator|.
name|cmPath
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|checkSum
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|useSourcePath
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|FileInfo
parameter_list|(
name|FileSystem
name|srcFs
parameter_list|,
name|Path
name|sourcePath
parameter_list|,
name|Path
name|cmPath
parameter_list|,
name|String
name|checkSum
parameter_list|,
name|boolean
name|useSourcePath
parameter_list|)
block|{
name|this
operator|.
name|srcFs
operator|=
name|srcFs
expr_stmt|;
name|this
operator|.
name|sourcePath
operator|=
name|sourcePath
expr_stmt|;
name|this
operator|.
name|cmPath
operator|=
name|cmPath
expr_stmt|;
name|this
operator|.
name|checkSum
operator|=
name|checkSum
expr_stmt|;
name|this
operator|.
name|useSourcePath
operator|=
name|useSourcePath
expr_stmt|;
block|}
specifier|public
name|FileSystem
name|getSrcFs
parameter_list|()
block|{
return|return
name|srcFs
return|;
block|}
specifier|public
name|Path
name|getSourcePath
parameter_list|()
block|{
return|return
name|sourcePath
return|;
block|}
specifier|public
name|Path
name|getCmPath
parameter_list|()
block|{
return|return
name|cmPath
return|;
block|}
specifier|public
name|String
name|getCheckSum
parameter_list|()
block|{
return|return
name|checkSum
return|;
block|}
specifier|public
name|boolean
name|isUseSourcePath
parameter_list|()
block|{
return|return
name|useSourcePath
return|;
block|}
specifier|public
name|void
name|setIsUseSourcePath
parameter_list|(
name|boolean
name|useSourcePath
parameter_list|)
block|{
name|this
operator|.
name|useSourcePath
operator|=
name|useSourcePath
expr_stmt|;
block|}
specifier|public
name|Path
name|getEffectivePath
parameter_list|()
block|{
if|if
condition|(
name|useSourcePath
condition|)
block|{
return|return
name|sourcePath
return|;
block|}
else|else
block|{
return|return
name|cmPath
return|;
block|}
block|}
block|}
specifier|public
specifier|static
name|ReplChangeManager
name|getInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
name|instance
operator|=
operator|new
name|ReplChangeManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
specifier|private
name|ReplChangeManager
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
try|try
block|{
if|if
condition|(
operator|!
name|inited
condition|)
block|{
if|if
condition|(
name|MetastoreConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|REPLCMENABLED
argument_list|)
condition|)
block|{
name|ReplChangeManager
operator|.
name|enabled
operator|=
literal|true
expr_stmt|;
name|ReplChangeManager
operator|.
name|cmroot
operator|=
operator|new
name|Path
argument_list|(
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|REPLCMDIR
argument_list|)
argument_list|)
expr_stmt|;
name|ReplChangeManager
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|fs
operator|=
name|cmroot
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Create cmroot with permission 700 if not exist
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|cmroot
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|cmroot
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|cmroot
argument_list|,
operator|new
name|FsPermission
argument_list|(
literal|"700"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|UserGroupInformation
name|usergroupInfo
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
decl_stmt|;
name|msUser
operator|=
name|usergroupInfo
operator|.
name|getShortUserName
argument_list|()
expr_stmt|;
name|msGroup
operator|=
name|usergroupInfo
operator|.
name|getPrimaryGroupName
argument_list|()
expr_stmt|;
block|}
name|inited
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|// Filter files starts with ".". Note Hadoop consider files starts with
comment|// "." or "_" as hidden file. However, we need to replicate files starts
comment|// with "_". We find at least 2 use cases:
comment|// 1. For har files, _index and _masterindex is required files
comment|// 2. _success file is required for Oozie to indicate availability of data source
specifier|private
specifier|static
specifier|final
name|PathFilter
name|hiddenFileFilter
init|=
operator|new
name|PathFilter
argument_list|()
block|{
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
return|return
operator|!
name|p
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|/***    * Move a path into cmroot. If the path is a directory (of a partition, or table if nonpartitioned),    *   recursively move files inside directory to cmroot. Note the table must be managed table    * @param path a single file or directory    * @param type if the files to be copied or moved to cmpath.    *             Copy is costly but preserve the source file    * @param ifPurge if the file should skip Trash when move/delete source file.    *                This is referred only if type is MOVE.    * @return int    * @throws MetaException    */
name|int
name|recycle
parameter_list|(
name|Path
name|path
parameter_list|,
name|RecycleType
name|type
parameter_list|,
name|boolean
name|ifPurge
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return
literal|0
return|;
block|}
try|try
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|isDirectory
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|path
argument_list|,
name|hiddenFileFilter
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|count
operator|+=
name|recycle
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
name|type
argument_list|,
name|ifPurge
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|String
name|fileCheckSum
init|=
name|checksumFor
argument_list|(
name|path
argument_list|,
name|fs
argument_list|)
decl_stmt|;
name|Path
name|cmPath
init|=
name|getCMPath
argument_list|(
name|conf
argument_list|,
name|path
operator|.
name|getName
argument_list|()
argument_list|,
name|fileCheckSum
argument_list|)
decl_stmt|;
comment|// set timestamp before moving to cmroot, so we can
comment|// avoid race condition CM remove the file before setting
comment|// timestamp
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|fs
operator|.
name|setTimes
argument_list|(
name|path
argument_list|,
name|now
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|cmPath
argument_list|)
operator|&&
name|fileCheckSum
operator|.
name|equalsIgnoreCase
argument_list|(
name|checksumFor
argument_list|(
name|cmPath
argument_list|,
name|fs
argument_list|)
argument_list|)
condition|)
block|{
comment|// If already a file with same checksum exists in cmPath, just ignore the copy/move
comment|// Also, mark the operation is unsuccessful to notify that file with same name already
comment|// exist which will ensure the timestamp of cmPath is updated to avoid clean-up by
comment|// CM cleaner.
name|success
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|MOVE
case|:
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Moving {} to {}"
argument_list|,
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|cmPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Rename fails if the file with same name already exist.
name|success
operator|=
name|fs
operator|.
name|rename
argument_list|(
name|path
argument_list|,
name|cmPath
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|COPY
case|:
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Copying {} to {}"
argument_list|,
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|cmPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// It is possible to have a file with same checksum in cmPath but the content is
comment|// partially copied or corrupted. In this case, just overwrite the existing file with
comment|// new one.
name|success
operator|=
name|FileUtils
operator|.
name|copy
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|fs
argument_list|,
name|cmPath
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
name|conf
argument_list|)
expr_stmt|;
break|break;
block|}
default|default:
comment|// Operation fails as invalid input
break|break;
block|}
block|}
comment|// Ignore if a file with same content already exist in cmroot
comment|// We might want to setXAttr for the new location in the future
if|if
condition|(
name|success
condition|)
block|{
comment|// set the file owner to hive (or the id metastore run as)
name|fs
operator|.
name|setOwner
argument_list|(
name|cmPath
argument_list|,
name|msUser
argument_list|,
name|msGroup
argument_list|)
expr_stmt|;
comment|// tag the original file name so we know where the file comes from
comment|// Note we currently only track the last known trace as
comment|// xattr has limited capacity. We shall revisit and store all original
comment|// locations if orig-loc becomes important
try|try
block|{
name|fs
operator|.
name|setXAttr
argument_list|(
name|cmPath
argument_list|,
name|ORIG_LOC_TAG
argument_list|,
name|path
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error setting xattr for {}"
argument_list|,
name|path
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"A file with the same content of {} already exists, ignore"
argument_list|,
name|path
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Need to extend the tenancy if we saw a newer file with the same content
name|fs
operator|.
name|setTimes
argument_list|(
name|cmPath
argument_list|,
name|now
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Tag if we want to remain in trash after deletion.
comment|// If multiple files share the same content, then
comment|// any file claim remain in trash would be granted
if|if
condition|(
operator|(
name|type
operator|==
name|RecycleType
operator|.
name|MOVE
operator|)
operator|&&
operator|!
name|ifPurge
condition|)
block|{
try|try
block|{
name|fs
operator|.
name|setXAttr
argument_list|(
name|cmPath
argument_list|,
name|REMAIN_IN_TRASH_TAG
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error setting xattr for {}"
argument_list|,
name|cmPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|count
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|// Get checksum of a file
specifier|static
specifier|public
name|String
name|checksumFor
parameter_list|(
name|Path
name|path
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: fs checksum only available on hdfs, need to
comment|//       find a solution for other fs (eg, local fs, s3, etc)
name|String
name|checksumString
init|=
literal|null
decl_stmt|;
name|FileChecksum
name|checksum
init|=
name|fs
operator|.
name|getFileChecksum
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|checksum
operator|!=
literal|null
condition|)
block|{
name|checksumString
operator|=
name|StringUtils
operator|.
name|byteToHexString
argument_list|(
name|checksum
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|checksum
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|checksumString
return|;
block|}
specifier|static
specifier|public
name|void
name|setCmRoot
parameter_list|(
name|Path
name|cmRoot
parameter_list|)
block|{
name|ReplChangeManager
operator|.
name|cmroot
operator|=
name|cmRoot
expr_stmt|;
block|}
comment|/***    * Convert a path of file inside a partition or table (if non-partitioned)    *   to a deterministic location of cmroot. So user can retrieve the file back    *   with the original location plus checksum.    * @param conf    * @param name original filename    * @param checkSum checksum of the file, can be retrieved by {@link #checksumFor(Path, FileSystem)}    * @return Path    */
specifier|static
name|Path
name|getCMPath
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|checkSum
parameter_list|)
throws|throws
name|IOException
throws|,
name|MetaException
block|{
name|String
name|newFileName
init|=
name|name
operator|+
literal|"_"
operator|+
name|checkSum
decl_stmt|;
name|int
name|maxLength
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_NAMENODE_MAX_COMPONENT_LENGTH_KEY
argument_list|,
name|DFSConfigKeys
operator|.
name|DFS_NAMENODE_MAX_COMPONENT_LENGTH_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|newFileName
operator|.
name|length
argument_list|()
operator|>
name|maxLength
condition|)
block|{
name|newFileName
operator|=
name|newFileName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|maxLength
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Path
argument_list|(
name|cmroot
argument_list|,
name|newFileName
argument_list|)
return|;
block|}
comment|/***    * Get original file specified by src and chksumString. If the file exists and checksum    * matches, return the file; otherwise, use chksumString to retrieve it from cmroot    * @param src Original file location    * @param checksumString Checksum of the original file    * @param conf    * @return Corresponding FileInfo object    */
specifier|public
specifier|static
name|FileInfo
name|getFileInfo
parameter_list|(
name|Path
name|src
parameter_list|,
name|String
name|checksumString
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
try|try
block|{
name|FileSystem
name|srcFs
init|=
name|src
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|checksumString
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|FileInfo
argument_list|(
name|srcFs
argument_list|,
name|src
argument_list|)
return|;
block|}
name|Path
name|cmPath
init|=
name|getCMPath
argument_list|(
name|conf
argument_list|,
name|src
operator|.
name|getName
argument_list|()
argument_list|,
name|checksumString
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|srcFs
operator|.
name|exists
argument_list|(
name|src
argument_list|)
condition|)
block|{
return|return
operator|new
name|FileInfo
argument_list|(
name|srcFs
argument_list|,
name|src
argument_list|,
name|cmPath
argument_list|,
name|checksumString
argument_list|,
literal|false
argument_list|)
return|;
block|}
name|String
name|currentChecksumString
decl_stmt|;
try|try
block|{
name|currentChecksumString
operator|=
name|checksumFor
argument_list|(
name|src
argument_list|,
name|srcFs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// If the file is missing or getting modified, then refer CM path
return|return
operator|new
name|FileInfo
argument_list|(
name|srcFs
argument_list|,
name|src
argument_list|,
name|cmPath
argument_list|,
name|checksumString
argument_list|,
literal|false
argument_list|)
return|;
block|}
if|if
condition|(
operator|(
name|currentChecksumString
operator|==
literal|null
operator|)
operator|||
name|checksumString
operator|.
name|equals
argument_list|(
name|currentChecksumString
argument_list|)
condition|)
block|{
return|return
operator|new
name|FileInfo
argument_list|(
name|srcFs
argument_list|,
name|src
argument_list|,
name|cmPath
argument_list|,
name|checksumString
argument_list|,
literal|true
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|FileInfo
argument_list|(
name|srcFs
argument_list|,
name|src
argument_list|,
name|cmPath
argument_list|,
name|checksumString
argument_list|,
literal|false
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/***    * Concatenate filename and checksum with "#"    * @param fileUriStr Filename string    * @param fileChecksum Checksum string    * @return Concatenated Uri string    */
comment|// TODO: this needs to be enhanced once change management based filesystem is implemented
comment|// Currently using fileuri#checksum as the format
specifier|static
specifier|public
name|String
name|encodeFileUri
parameter_list|(
name|String
name|fileUriStr
parameter_list|,
name|String
name|fileChecksum
parameter_list|)
block|{
if|if
condition|(
name|fileChecksum
operator|!=
literal|null
condition|)
block|{
return|return
name|fileUriStr
operator|+
name|URI_FRAGMENT_SEPARATOR
operator|+
name|fileChecksum
return|;
block|}
else|else
block|{
return|return
name|fileUriStr
return|;
block|}
block|}
comment|/***    * Split uri with fragment into file uri and checksum    * @param fileURIStr uri with fragment    * @return array of file name and checksum    */
specifier|static
specifier|public
name|String
index|[]
name|getFileWithChksumFromURI
parameter_list|(
name|String
name|fileURIStr
parameter_list|)
block|{
name|String
index|[]
name|uriAndFragment
init|=
name|fileURIStr
operator|.
name|split
argument_list|(
name|URI_FRAGMENT_SEPARATOR
argument_list|)
decl_stmt|;
name|String
index|[]
name|result
init|=
operator|new
name|String
index|[
literal|2
index|]
decl_stmt|;
name|result
index|[
literal|0
index|]
operator|=
name|uriAndFragment
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
name|uriAndFragment
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|result
index|[
literal|1
index|]
operator|=
name|uriAndFragment
index|[
literal|1
index|]
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isCMFileUri
parameter_list|(
name|Path
name|fromPath
parameter_list|,
name|FileSystem
name|srcFs
parameter_list|)
block|{
name|String
index|[]
name|result
init|=
name|getFileWithChksumFromURI
argument_list|(
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|result
index|[
literal|1
index|]
operator|!=
literal|null
return|;
block|}
comment|/**    * Thread to clear old files of cmroot recursively    */
specifier|static
class|class
name|CMClearer
implements|implements
name|Runnable
block|{
specifier|private
name|Path
name|cmroot
decl_stmt|;
specifier|private
name|long
name|secRetain
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
name|CMClearer
parameter_list|(
name|String
name|cmrootString
parameter_list|,
name|long
name|secRetain
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|cmroot
operator|=
operator|new
name|Path
argument_list|(
name|cmrootString
argument_list|)
expr_stmt|;
name|this
operator|.
name|secRetain
operator|=
name|secRetain
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"CMClearer started"
argument_list|)
expr_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|cmroot
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|cmroot
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|long
name|modifiedTime
init|=
name|file
operator|.
name|getModificationTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|now
operator|-
name|modifiedTime
operator|>
name|secRetain
operator|*
literal|1000
condition|)
block|{
try|try
block|{
if|if
condition|(
name|fs
operator|.
name|getXAttrs
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|containsKey
argument_list|(
name|REMAIN_IN_TRASH_TAG
argument_list|)
condition|)
block|{
name|boolean
name|succ
init|=
name|Trash
operator|.
name|moveToAppropriateTrash
argument_list|(
name|fs
argument_list|,
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|succ
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Move "
operator|+
name|file
operator|.
name|toString
argument_list|()
operator|+
literal|" to trash"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Fail to move "
operator|+
name|file
operator|.
name|toString
argument_list|()
operator|+
literal|" to trash"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|boolean
name|succ
init|=
name|fs
operator|.
name|delete
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|succ
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Remove "
operator|+
name|file
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Fail to remove "
operator|+
name|file
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error getting xattr for "
operator|+
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception when clearing cmroot:"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Schedule CMClearer thread. Will be invoked by metastore
specifier|static
name|void
name|scheduleCMClearer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|MetastoreConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|REPLCMENABLED
argument_list|)
condition|)
block|{
name|ScheduledExecutorService
name|executor
init|=
name|Executors
operator|.
name|newSingleThreadScheduledExecutor
argument_list|(
operator|new
name|BasicThreadFactory
operator|.
name|Builder
argument_list|()
operator|.
name|namingPattern
argument_list|(
literal|"cmclearer-%d"
argument_list|)
operator|.
name|daemon
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|executor
operator|.
name|scheduleAtFixedRate
argument_list|(
operator|new
name|CMClearer
argument_list|(
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|REPLCMDIR
argument_list|)
argument_list|,
name|MetastoreConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|REPLCMRETIAN
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|conf
argument_list|)
argument_list|,
literal|0
argument_list|,
name|MetastoreConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|REPLCMINTERVAL
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

