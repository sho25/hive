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
name|fs
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|Options
operator|.
name|Rename
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
name|util
operator|.
name|Progressable
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
name|Shell
import|;
end_import

begin_comment
comment|/****************************************************************  * A FileSystem that can serve a given scheme/authority using some  * other file system. In that sense, it serves as a proxy for the  * real/underlying file system  *****************************************************************/
end_comment

begin_class
specifier|public
class|class
name|ProxyFileSystem
extends|extends
name|FilterFileSystem
block|{
specifier|protected
name|String
name|myScheme
decl_stmt|;
specifier|protected
name|String
name|myAuthority
decl_stmt|;
specifier|protected
name|URI
name|myUri
decl_stmt|;
specifier|protected
name|String
name|realScheme
decl_stmt|;
specifier|protected
name|String
name|realAuthority
decl_stmt|;
specifier|protected
name|URI
name|realUri
decl_stmt|;
specifier|protected
name|Path
name|swizzleParamPath
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|String
name|pathUriString
init|=
name|p
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|URI
name|newPathUri
init|=
name|URI
operator|.
name|create
argument_list|(
name|pathUriString
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|realScheme
argument_list|,
name|realAuthority
argument_list|,
name|newPathUri
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|Path
name|swizzleReturnPath
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|String
name|pathUriString
init|=
name|p
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|URI
name|newPathUri
init|=
name|URI
operator|.
name|create
argument_list|(
name|pathUriString
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|myScheme
argument_list|,
name|myAuthority
argument_list|,
name|newPathUri
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
name|FileStatus
name|swizzleFileStatus
parameter_list|(
name|FileStatus
name|orig
parameter_list|,
name|boolean
name|isParam
parameter_list|)
block|{
name|FileStatus
name|ret
init|=
operator|new
name|FileStatus
argument_list|(
name|orig
operator|.
name|getLen
argument_list|()
argument_list|,
name|orig
operator|.
name|isDir
argument_list|()
argument_list|,
name|orig
operator|.
name|getReplication
argument_list|()
argument_list|,
name|orig
operator|.
name|getBlockSize
argument_list|()
argument_list|,
name|orig
operator|.
name|getModificationTime
argument_list|()
argument_list|,
name|orig
operator|.
name|getAccessTime
argument_list|()
argument_list|,
name|orig
operator|.
name|getPermission
argument_list|()
argument_list|,
name|orig
operator|.
name|getOwner
argument_list|()
argument_list|,
name|orig
operator|.
name|getGroup
argument_list|()
argument_list|,
name|isParam
condition|?
name|swizzleParamPath
argument_list|(
name|orig
operator|.
name|getPath
argument_list|()
argument_list|)
else|:
name|swizzleReturnPath
argument_list|(
name|orig
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
name|ProxyFileSystem
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported constructor"
argument_list|)
throw|;
block|}
specifier|public
name|ProxyFileSystem
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported constructor"
argument_list|)
throw|;
block|}
comment|/**    *    * @param p    * @return    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|Path
name|resolvePath
parameter_list|(
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Return the fully-qualified path of path f resolving the path
comment|// through any symlinks or mount point
name|checkPath
argument_list|(
name|p
argument_list|)
expr_stmt|;
return|return
name|getFileStatus
argument_list|(
name|p
argument_list|)
operator|.
name|getPath
argument_list|()
return|;
block|}
comment|/**    * Create a proxy file system for fs.    *    * @param fs FileSystem to create proxy for    * @param myUri URI to use as proxy. Only the scheme and authority from    *              this are used right now    */
specifier|public
name|ProxyFileSystem
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|URI
name|myUri
parameter_list|)
block|{
name|super
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|URI
name|realUri
init|=
name|fs
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|this
operator|.
name|realScheme
operator|=
name|realUri
operator|.
name|getScheme
argument_list|()
expr_stmt|;
name|this
operator|.
name|realAuthority
operator|=
name|realUri
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
name|this
operator|.
name|realUri
operator|=
name|realUri
expr_stmt|;
name|this
operator|.
name|myScheme
operator|=
name|myUri
operator|.
name|getScheme
argument_list|()
expr_stmt|;
name|this
operator|.
name|myAuthority
operator|=
name|myUri
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
name|this
operator|.
name|myUri
operator|=
name|myUri
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|URI
name|name
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|URI
name|realUri
init|=
operator|new
name|URI
argument_list|(
name|realScheme
argument_list|,
name|realAuthority
argument_list|,
name|name
operator|.
name|getPath
argument_list|()
argument_list|,
name|name
operator|.
name|getQuery
argument_list|()
argument_list|,
name|name
operator|.
name|getFragment
argument_list|()
argument_list|)
decl_stmt|;
name|super
operator|.
name|initialize
argument_list|(
name|realUri
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|URI
name|getUri
parameter_list|()
block|{
return|return
name|myUri
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|makeQualified
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
name|swizzleReturnPath
argument_list|(
name|super
operator|.
name|makeQualified
argument_list|(
name|swizzleParamPath
argument_list|(
name|path
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|checkPath
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
block|{
name|super
operator|.
name|checkPath
argument_list|(
name|swizzleParamPath
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|BlockLocation
index|[]
name|getFileBlockLocations
parameter_list|(
name|FileStatus
name|file
parameter_list|,
name|long
name|start
parameter_list|,
name|long
name|len
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|getFileBlockLocations
argument_list|(
name|swizzleFileStatus
argument_list|(
name|file
argument_list|,
literal|true
argument_list|)
argument_list|,
name|start
argument_list|,
name|len
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataInputStream
name|open
parameter_list|(
name|Path
name|f
parameter_list|,
name|int
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|open
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|,
name|bufferSize
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataOutputStream
name|append
parameter_list|(
name|Path
name|f
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|append
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|,
name|bufferSize
argument_list|,
name|progress
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataOutputStream
name|create
parameter_list|(
name|Path
name|f
parameter_list|,
name|FsPermission
name|permission
parameter_list|,
name|boolean
name|overwrite
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|short
name|replication
parameter_list|,
name|long
name|blockSize
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|create
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|,
name|permission
argument_list|,
name|overwrite
argument_list|,
name|bufferSize
argument_list|,
name|replication
argument_list|,
name|blockSize
argument_list|,
name|progress
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|setReplication
parameter_list|(
name|Path
name|src
parameter_list|,
name|short
name|replication
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|setReplication
argument_list|(
name|swizzleParamPath
argument_list|(
name|src
argument_list|)
argument_list|,
name|replication
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|rename
parameter_list|(
name|Path
name|src
parameter_list|,
name|Path
name|dst
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|dest
init|=
name|swizzleParamPath
argument_list|(
name|dst
argument_list|)
decl_stmt|;
comment|// Make sure for existing destination we return false as per FileSystem api contract
return|return
name|super
operator|.
name|isFile
argument_list|(
name|dest
argument_list|)
condition|?
literal|false
else|:
name|super
operator|.
name|rename
argument_list|(
name|swizzleParamPath
argument_list|(
name|src
argument_list|)
argument_list|,
name|dest
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rename
parameter_list|(
name|Path
name|src
parameter_list|,
name|Path
name|dst
parameter_list|,
name|Rename
modifier|...
name|options
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|rename
argument_list|(
name|swizzleParamPath
argument_list|(
name|src
argument_list|)
argument_list|,
name|swizzleParamPath
argument_list|(
name|dst
argument_list|)
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|delete
parameter_list|(
name|Path
name|f
parameter_list|,
name|boolean
name|recursive
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|delete
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|,
name|recursive
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|deleteOnExit
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|deleteOnExit
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileStatus
index|[]
name|listStatus
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|orig
init|=
name|super
operator|.
name|listStatus
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|ret
init|=
operator|new
name|FileStatus
index|[
name|orig
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|orig
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ret
index|[
name|i
index|]
operator|=
name|swizzleFileStatus
argument_list|(
name|orig
index|[
name|i
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getHomeDirectory
parameter_list|()
block|{
return|return
name|swizzleReturnPath
argument_list|(
name|super
operator|.
name|getHomeDirectory
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setWorkingDirectory
parameter_list|(
name|Path
name|newDir
parameter_list|)
block|{
name|super
operator|.
name|setWorkingDirectory
argument_list|(
name|swizzleParamPath
argument_list|(
name|newDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getWorkingDirectory
parameter_list|()
block|{
return|return
name|swizzleReturnPath
argument_list|(
name|super
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|mkdirs
parameter_list|(
name|Path
name|f
parameter_list|,
name|FsPermission
name|permission
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|mkdirs
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|,
name|permission
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyFromLocalFile
parameter_list|(
name|boolean
name|delSrc
parameter_list|,
name|Path
name|src
parameter_list|,
name|Path
name|dst
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|copyFromLocalFile
argument_list|(
name|delSrc
argument_list|,
name|swizzleParamPath
argument_list|(
name|src
argument_list|)
argument_list|,
name|swizzleParamPath
argument_list|(
name|dst
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyFromLocalFile
parameter_list|(
name|boolean
name|delSrc
parameter_list|,
name|boolean
name|overwrite
parameter_list|,
name|Path
index|[]
name|srcs
parameter_list|,
name|Path
name|dst
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|copyFromLocalFile
argument_list|(
name|delSrc
argument_list|,
name|overwrite
argument_list|,
name|srcs
argument_list|,
name|swizzleParamPath
argument_list|(
name|dst
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyFromLocalFile
parameter_list|(
name|boolean
name|delSrc
parameter_list|,
name|boolean
name|overwrite
parameter_list|,
name|Path
name|src
parameter_list|,
name|Path
name|dst
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|copyFromLocalFile
argument_list|(
name|delSrc
argument_list|,
name|overwrite
argument_list|,
name|src
argument_list|,
name|swizzleParamPath
argument_list|(
name|dst
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyToLocalFile
parameter_list|(
name|boolean
name|delSrc
parameter_list|,
name|Path
name|src
parameter_list|,
name|Path
name|dst
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|copyToLocalFile
argument_list|(
name|delSrc
argument_list|,
name|swizzleParamPath
argument_list|(
name|src
argument_list|)
argument_list|,
name|dst
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|startLocalOutput
parameter_list|(
name|Path
name|fsOutputFile
parameter_list|,
name|Path
name|tmpLocalFile
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|startLocalOutput
argument_list|(
name|swizzleParamPath
argument_list|(
name|fsOutputFile
argument_list|)
argument_list|,
name|tmpLocalFile
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|completeLocalOutput
parameter_list|(
name|Path
name|fsOutputFile
parameter_list|,
name|Path
name|tmpLocalFile
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|completeLocalOutput
argument_list|(
name|swizzleParamPath
argument_list|(
name|fsOutputFile
argument_list|)
argument_list|,
name|tmpLocalFile
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ContentSummary
name|getContentSummary
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|getContentSummary
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileStatus
name|getFileLinkStatus
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|swizzleFileStatus
argument_list|(
name|super
operator|.
name|getFileLinkStatus
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileStatus
name|getFileStatus
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|swizzleFileStatus
argument_list|(
name|super
operator|.
name|getFileStatus
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileChecksum
name|getFileChecksum
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|getFileChecksum
argument_list|(
name|swizzleParamPath
argument_list|(
name|f
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOwner
parameter_list|(
name|Path
name|p
parameter_list|,
name|String
name|username
parameter_list|,
name|String
name|groupname
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|setOwner
argument_list|(
name|swizzleParamPath
argument_list|(
name|p
argument_list|)
argument_list|,
name|username
argument_list|,
name|groupname
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTimes
parameter_list|(
name|Path
name|p
parameter_list|,
name|long
name|mtime
parameter_list|,
name|long
name|atime
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|setTimes
argument_list|(
name|swizzleParamPath
argument_list|(
name|p
argument_list|)
argument_list|,
name|mtime
argument_list|,
name|atime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setPermission
parameter_list|(
name|Path
name|p
parameter_list|,
name|FsPermission
name|permission
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|setPermission
argument_list|(
name|swizzleParamPath
argument_list|(
name|p
argument_list|)
argument_list|,
name|permission
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

