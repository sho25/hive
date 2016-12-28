begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Map
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
name|conf
operator|.
name|HiveConf
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
name|conf
operator|.
name|HiveConf
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
name|api
operator|.
name|Database
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
name|api
operator|.
name|Partition
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
name|Table
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
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|static
name|Warehouse
name|wh
decl_stmt|;
specifier|private
name|String
name|user
decl_stmt|;
specifier|private
name|String
name|group
decl_stmt|;
specifier|public
specifier|static
name|ReplChangeManager
name|getInstance
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Warehouse
name|wh
parameter_list|)
throws|throws
name|IOException
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
argument_list|,
name|wh
argument_list|)
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
name|ReplChangeManager
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Warehouse
name|wh
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|inited
condition|)
block|{
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
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
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPLCMDIR
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
name|ReplChangeManager
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|ReplChangeManager
operator|.
name|wh
operator|=
name|wh
expr_stmt|;
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
name|user
operator|=
name|usergroupInfo
operator|.
name|getShortUserName
argument_list|()
expr_stmt|;
name|group
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
comment|/***    * Recycle a managed table, move table files to cmroot    * @param db    * @param table    * @return    * @throws IOException    * @throws MetaException    */
specifier|public
name|int
name|recycle
parameter_list|(
name|Database
name|db
parameter_list|,
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
throws|,
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
name|Path
name|tablePath
init|=
name|wh
operator|.
name|getTablePath
argument_list|(
name|db
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|tablePath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|failCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|tablePath
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|recycle
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
name|failCount
operator|++
expr_stmt|;
block|}
block|}
return|return
name|failCount
return|;
block|}
comment|/***    * Recycle a partition of a managed table, move partition files to cmroot    * @param db    * @param table    * @param part    * @return    * @throws IOException    * @throws MetaException    */
specifier|public
name|int
name|recycle
parameter_list|(
name|Database
name|db
parameter_list|,
name|Table
name|table
parameter_list|,
name|Partition
name|part
parameter_list|)
throws|throws
name|IOException
throws|,
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pm
init|=
name|Warehouse
operator|.
name|makeSpecFromValues
argument_list|(
name|table
operator|.
name|getPartitionKeys
argument_list|()
argument_list|,
name|part
operator|.
name|getValues
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|partPath
init|=
name|wh
operator|.
name|getPartitionPath
argument_list|(
name|db
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|pm
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|partPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|failCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|partPath
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|recycle
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
name|failCount
operator|++
expr_stmt|;
block|}
block|}
return|return
name|failCount
return|;
block|}
comment|/***    * Recycle a single file (of a partition, or table if nonpartitioned),    *   move files to cmroot. Note the table must be managed table    * @param path    * @return    * @throws IOException    * @throws MetaException    */
specifier|public
name|boolean
name|recycle
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
throws|,
name|MetaException
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return
literal|true
return|;
block|}
name|Path
name|cmPath
init|=
name|getCMPath
argument_list|(
name|path
argument_list|,
name|conf
argument_list|,
name|getCksumString
argument_list|(
name|path
argument_list|,
name|conf
argument_list|)
argument_list|)
decl_stmt|;
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
literal|"Moving "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" to "
operator|+
name|cmPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|succ
init|=
name|fs
operator|.
name|rename
argument_list|(
name|path
argument_list|,
name|cmPath
argument_list|)
decl_stmt|;
comment|// Ignore if a file with same content already exist in cmroot
comment|// We might want to setXAttr for the new location in the future
if|if
condition|(
operator|!
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
literal|"A file with the same content of "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" already exists, ignore"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
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
name|cmPath
argument_list|,
name|now
argument_list|,
name|now
argument_list|)
expr_stmt|;
comment|// set the file owner to hive (or the id metastore run as)
name|fs
operator|.
name|setOwner
argument_list|(
name|cmPath
argument_list|,
name|user
argument_list|,
name|group
argument_list|)
expr_stmt|;
comment|// tag the original file name so we know where the file comes from
name|fs
operator|.
name|setXAttr
argument_list|(
name|cmPath
argument_list|,
literal|"user.original-loc"
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
return|return
name|succ
return|;
block|}
comment|// Get checksum of a file
specifier|static
specifier|public
name|String
name|getCksumString
parameter_list|(
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: fs checksum only available on hdfs, need to
comment|//       find a solution for other fs (eg, local fs, s3, etc)
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
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
name|String
name|checksumString
init|=
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
decl_stmt|;
return|return
name|checksumString
return|;
block|}
comment|/***    * Convert a path of file inside a partition or table (if non-partitioned)    *   to a deterministic location of cmroot. So user can retrieve the file back    *   with the original location plus signature.    * @param path original path inside partition or table    * @param conf    * @param signature unique signature of the file, can be retrieved by {@link getSignature}    * @return    * @throws IOException    * @throws MetaException    */
specifier|static
specifier|public
name|Path
name|getCMPath
parameter_list|(
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|signature
parameter_list|)
throws|throws
name|IOException
throws|,
name|MetaException
block|{
name|String
name|newFileName
init|=
name|signature
operator|+
name|path
operator|.
name|getName
argument_list|()
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
name|Path
name|cmPath
init|=
operator|new
name|Path
argument_list|(
name|cmroot
argument_list|,
name|newFileName
argument_list|)
decl_stmt|;
return|return
name|cmPath
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
name|processDir
argument_list|(
name|cmroot
argument_list|,
name|now
argument_list|)
expr_stmt|;
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
specifier|private
name|boolean
name|processDir
parameter_list|(
name|Path
name|folder
parameter_list|,
name|long
name|now
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|files
init|=
name|folder
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|listStatus
argument_list|(
name|folder
argument_list|)
decl_stmt|;
name|boolean
name|empty
init|=
literal|true
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|file
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
if|if
condition|(
name|processDir
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
name|now
argument_list|)
condition|)
block|{
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
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
expr_stmt|;
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
name|empty
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
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
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
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
expr_stmt|;
block|}
else|else
block|{
name|empty
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
return|return
name|empty
return|;
block|}
block|}
comment|// Schedule CMClearer thread. Will be invoked by metastore
specifier|public
specifier|static
name|void
name|scheduleCMClearer
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
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
name|hiveConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPLCMDIR
operator|.
name|varname
argument_list|)
argument_list|,
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|hiveConf
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
name|hiveConf
argument_list|)
argument_list|,
literal|0
argument_list|,
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|hiveConf
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

