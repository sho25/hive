begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStreamWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
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
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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

begin_comment
comment|/**  *  HDFS implementation of templeton storage.  *  *  This implementation assumes that all keys in key/value pairs are  *  chosen such that they don't have any newlines in them.  *  */
end_comment

begin_class
specifier|public
class|class
name|HDFSStorage
implements|implements
name|TempletonStorage
block|{
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
specifier|public
name|String
name|storage_root
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JOB_PATH
init|=
literal|"/jobs"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JOB_TRACKINGPATH
init|=
literal|"/created"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OVERHEAD_PATH
init|=
literal|"/overhead"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HDFSStorage
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|void
name|startCleanup
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
try|try
block|{
name|HDFSCleanup
operator|.
name|startInstance
argument_list|(
name|config
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cleanup instance didn't start."
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|saveField
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|key
parameter_list|,
name|String
name|val
parameter_list|)
throws|throws
name|NotFoundException
block|{
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|PrintWriter
name|out
init|=
literal|null
decl_stmt|;
comment|//todo: FileSystem#setPermission() - should this make sure to set 777 on jobs/ ?
name|Path
name|keyfile
init|=
operator|new
name|Path
argument_list|(
name|getPath
argument_list|(
name|type
argument_list|)
operator|+
literal|"/"
operator|+
name|id
operator|+
literal|"/"
operator|+
name|key
argument_list|)
decl_stmt|;
try|try
block|{
comment|// This will replace the old value if there is one
comment|// Overwrite the existing file
name|out
operator|=
operator|new
name|PrintWriter
argument_list|(
operator|new
name|OutputStreamWriter
argument_list|(
name|fs
operator|.
name|create
argument_list|(
name|keyfile
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|errMsg
init|=
literal|"Couldn't write to "
operator|+
name|keyfile
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errMsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NotFoundException
argument_list|(
name|errMsg
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|close
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getField
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|key
parameter_list|)
block|{
name|BufferedReader
name|in
init|=
literal|null
decl_stmt|;
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|getPath
argument_list|(
name|type
argument_list|)
operator|+
literal|"/"
operator|+
name|id
operator|+
literal|"/"
operator|+
name|key
argument_list|)
decl_stmt|;
try|try
block|{
name|in
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|fs
operator|.
name|open
argument_list|(
name|p
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|line
init|=
literal|null
decl_stmt|;
name|String
name|val
init|=
literal|""
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|in
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|val
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|val
operator|+=
literal|"\n"
expr_stmt|;
block|}
name|val
operator|+=
name|line
expr_stmt|;
block|}
return|return
name|val
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|//don't print stack trace since clients poll for 'exitValue', 'completed',
comment|//files which are not there until job completes
name|LOG
operator|.
name|info
argument_list|(
literal|"Couldn't find "
operator|+
name|p
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|close
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getFields
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|BufferedReader
name|in
init|=
literal|null
decl_stmt|;
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|getPath
argument_list|(
name|type
argument_list|)
operator|+
literal|"/"
operator|+
name|id
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|FileStatus
name|status
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|p
argument_list|)
control|)
block|{
name|in
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|fs
operator|.
name|open
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|line
init|=
literal|null
decl_stmt|;
name|String
name|val
init|=
literal|""
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|in
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|val
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|val
operator|+=
literal|"\n"
expr_stmt|;
block|}
name|val
operator|+=
name|line
expr_stmt|;
block|}
name|map
operator|.
name|put
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
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
name|trace
argument_list|(
literal|"Couldn't find "
operator|+
name|p
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|close
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|delete
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|)
throws|throws
name|NotFoundException
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|getPath
argument_list|(
name|type
argument_list|)
operator|+
literal|"/"
operator|+
name|id
argument_list|)
decl_stmt|;
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|NotFoundException
argument_list|(
literal|"Node "
operator|+
name|p
operator|+
literal|" was not found: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAll
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Type
name|type
range|:
name|Type
operator|.
name|values
argument_list|()
control|)
block|{
name|allNodes
operator|.
name|addAll
argument_list|(
name|getAllForType
argument_list|(
name|type
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|allNodes
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllForType
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|FileStatus
name|status
range|:
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|getPath
argument_list|(
name|type
argument_list|)
argument_list|)
argument_list|)
control|)
block|{
name|allNodes
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Couldn't find children for type "
operator|+
name|type
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|allNodes
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllForKey
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|Type
name|type
range|:
name|Type
operator|.
name|values
argument_list|()
control|)
block|{
name|allNodes
operator|.
name|addAll
argument_list|(
name|getAllForTypeAndKey
argument_list|(
name|type
argument_list|,
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Couldn't find children for key "
operator|+
name|key
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|allNodes
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllForTypeAndKey
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|FileStatus
name|status
range|:
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|getPath
argument_list|(
name|type
argument_list|)
argument_list|)
argument_list|)
control|)
block|{
name|map
operator|=
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|getFields
argument_list|(
name|type
argument_list|,
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|allNodes
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Couldn't find children for key "
operator|+
name|key
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|allNodes
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|openStorage
parameter_list|(
name|Configuration
name|config
parameter_list|)
throws|throws
name|IOException
block|{
name|storage_root
operator|=
name|config
operator|.
name|get
argument_list|(
name|TempletonStorage
operator|.
name|STORAGE_ROOT
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|==
literal|null
condition|)
block|{
name|fs
operator|=
operator|new
name|Path
argument_list|(
name|storage_root
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|config
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeStorage
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Nothing to do here
block|}
comment|/**    * Get the path to storage based on the type.    * @param type    */
specifier|public
name|String
name|getPath
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
return|return
name|getPath
argument_list|(
name|type
argument_list|,
name|storage_root
argument_list|)
return|;
block|}
comment|/**    * Static method to get the path based on the type.    *    * @param type    * @param root    */
specifier|public
specifier|static
name|String
name|getPath
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|root
parameter_list|)
block|{
name|String
name|typepath
init|=
name|root
operator|+
name|OVERHEAD_PATH
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|JOB
case|:
name|typepath
operator|=
name|root
operator|+
name|JOB_PATH
expr_stmt|;
break|break;
case|case
name|JOBTRACKING
case|:
name|typepath
operator|=
name|root
operator|+
name|JOB_TRACKINGPATH
expr_stmt|;
break|break;
block|}
return|return
name|typepath
return|;
block|}
specifier|private
name|void
name|close
parameter_list|(
name|Closeable
name|is
parameter_list|)
block|{
if|if
condition|(
name|is
operator|==
literal|null
condition|)
block|{
return|return;
block|}
try|try
block|{
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Failed to close InputStream: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

