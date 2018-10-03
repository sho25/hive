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
name|ql
operator|.
name|io
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|server
operator|.
name|namenode
operator|.
name|NameNode
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|LocatedFileStatus
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
name|RemoteIterator
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
name|DistributedFileSystem
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
name|shims
operator|.
name|HadoopShims
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
name|shims
operator|.
name|ShimLoader
import|;
end_import

begin_class
specifier|public
class|class
name|HdfsUtils
block|{
specifier|private
specifier|static
specifier|final
name|HadoopShims
name|SHIMS
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
decl_stmt|;
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
name|HdfsUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Object
name|getFileId
parameter_list|(
name|FileSystem
name|fileSystem
parameter_list|,
name|Path
name|path
parameter_list|,
name|boolean
name|allowSynthetic
parameter_list|,
name|boolean
name|checkDefaultFs
parameter_list|,
name|boolean
name|forceSyntheticIds
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|forceSyntheticIds
operator|==
literal|false
operator|&&
name|fileSystem
operator|instanceof
name|DistributedFileSystem
condition|)
block|{
name|DistributedFileSystem
name|dfs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|fileSystem
decl_stmt|;
if|if
condition|(
operator|(
operator|!
name|checkDefaultFs
operator|)
operator|||
name|isDefaultFs
argument_list|(
name|dfs
argument_list|)
condition|)
block|{
name|Object
name|result
init|=
name|SHIMS
operator|.
name|getFileId
argument_list|(
name|dfs
argument_list|,
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
return|return
name|result
return|;
block|}
block|}
if|if
condition|(
operator|!
name|allowSynthetic
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot get unique file ID from "
operator|+
name|fileSystem
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"; returning null"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|FileStatus
name|fs
init|=
name|fileSystem
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
decl_stmt|;
return|return
operator|new
name|SyntheticFileId
argument_list|(
name|path
argument_list|,
name|fs
operator|.
name|getLen
argument_list|()
argument_list|,
name|fs
operator|.
name|getModificationTime
argument_list|()
argument_list|)
return|;
block|}
comment|// This is not actually used for production.
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|long
name|createTestFileId
parameter_list|(
name|String
name|pathStr
parameter_list|,
name|FileStatus
name|fs
parameter_list|,
name|boolean
name|doLog
parameter_list|,
name|String
name|fsName
parameter_list|)
block|{
name|int
name|nameHash
init|=
name|pathStr
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|long
name|fileSize
init|=
name|fs
operator|.
name|getLen
argument_list|()
decl_stmt|,
name|modTime
init|=
name|fs
operator|.
name|getModificationTime
argument_list|()
decl_stmt|;
name|int
name|fileSizeHash
init|=
call|(
name|int
call|)
argument_list|(
name|fileSize
operator|^
operator|(
name|fileSize
operator|>>>
literal|32
operator|)
argument_list|)
decl_stmt|,
name|modTimeHash
init|=
call|(
name|int
call|)
argument_list|(
name|modTime
operator|^
operator|(
name|modTime
operator|>>>
literal|32
operator|)
argument_list|)
decl_stmt|,
name|combinedHash
init|=
name|modTimeHash
operator|^
name|fileSizeHash
decl_stmt|;
name|long
name|id
init|=
operator|(
operator|(
name|nameHash
operator|&
literal|0xffffffffL
operator|)
operator|<<
literal|32
operator|)
operator||
operator|(
name|combinedHash
operator|&
literal|0xffffffffL
operator|)
decl_stmt|;
if|if
condition|(
name|doLog
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot get unique file ID from "
operator|+
name|fsName
operator|+
literal|"; using "
operator|+
name|id
operator|+
literal|" ("
operator|+
name|pathStr
operator|+
literal|","
operator|+
name|nameHash
operator|+
literal|","
operator|+
name|fileSize
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
return|return
name|id
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|FileStatus
argument_list|>
name|listLocatedStatus
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|PathFilter
name|filter
parameter_list|)
throws|throws
name|IOException
block|{
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
name|itr
init|=
name|fs
operator|.
name|listLocatedStatus
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|FileStatus
name|stat
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
operator|||
name|filter
operator|.
name|accept
argument_list|(
name|stat
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|stat
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|// TODO: this relies on HDFS not changing the format; we assume if we could get inode ID, this
comment|//       is still going to work. Otherwise, file IDs can be turned off. Later, we should use
comment|//       as public utility method in HDFS to obtain the inode-based path.
specifier|private
specifier|static
name|String
name|HDFS_ID_PATH_PREFIX
init|=
literal|"/.reserved/.inodes/"
decl_stmt|;
specifier|public
specifier|static
name|Path
name|getFileIdPath
parameter_list|(
name|FileSystem
name|fileSystem
parameter_list|,
name|Path
name|path
parameter_list|,
name|long
name|fileId
parameter_list|)
block|{
return|return
operator|(
operator|(
name|fileSystem
operator|instanceof
name|DistributedFileSystem
operator|)
operator|)
condition|?
operator|new
name|Path
argument_list|(
name|HDFS_ID_PATH_PREFIX
operator|+
name|fileId
argument_list|)
else|:
name|path
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isDefaultFs
parameter_list|(
name|DistributedFileSystem
name|fs
parameter_list|)
block|{
name|URI
name|uri
init|=
name|fs
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|String
name|scheme
init|=
name|uri
operator|.
name|getScheme
argument_list|()
decl_stmt|;
if|if
condition|(
name|scheme
operator|==
literal|null
condition|)
return|return
literal|true
return|;
comment|// Assume that relative URI resolves to default FS.
name|URI
name|defaultUri
init|=
name|FileSystem
operator|.
name|getDefaultUri
argument_list|(
name|fs
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|defaultUri
operator|.
name|getScheme
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|scheme
argument_list|)
condition|)
return|return
literal|false
return|;
comment|// Mismatch.
name|String
name|defaultAuthority
init|=
name|defaultUri
operator|.
name|getAuthority
argument_list|()
decl_stmt|,
name|authority
init|=
name|uri
operator|.
name|getAuthority
argument_list|()
decl_stmt|;
if|if
condition|(
name|authority
operator|==
literal|null
condition|)
return|return
literal|true
return|;
comment|// Schemes match, no authority - assume default.
if|if
condition|(
name|defaultAuthority
operator|==
literal|null
condition|)
return|return
literal|false
return|;
comment|// TODO: What does this even mean?
if|if
condition|(
operator|!
name|defaultUri
operator|.
name|getHost
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|uri
operator|.
name|getHost
argument_list|()
argument_list|)
condition|)
return|return
literal|false
return|;
comment|// Mismatch.
name|int
name|defaultPort
init|=
name|defaultUri
operator|.
name|getPort
argument_list|()
decl_stmt|,
name|port
init|=
name|uri
operator|.
name|getPort
argument_list|()
decl_stmt|;
if|if
condition|(
name|port
operator|==
operator|-
literal|1
condition|)
return|return
literal|true
return|;
comment|// No port, assume default.
comment|// Note - this makes assumptions that are DFS-specific; DFS::getDefaultPort is not visible.
return|return
operator|(
name|defaultPort
operator|==
operator|-
literal|1
operator|)
condition|?
operator|(
name|port
operator|==
name|NameNode
operator|.
name|DEFAULT_PORT
operator|)
else|:
operator|(
name|port
operator|==
name|defaultPort
operator|)
return|;
block|}
block|}
end_class

end_unit

