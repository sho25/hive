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

begin_comment
comment|// hadoop stuff
end_comment

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
name|Properties
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
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Constants
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
name|UnknownTableException
import|;
end_import

begin_class
specifier|public
class|class
name|ROTable
block|{
specifier|protected
name|String
name|tableName_
decl_stmt|;
specifier|protected
name|Properties
name|schema_
decl_stmt|;
specifier|protected
name|Configuration
name|conf_
decl_stmt|;
specifier|protected
name|Path
name|whPath_
decl_stmt|;
specifier|protected
name|DB
name|parent_
decl_stmt|;
comment|//    protected FileSystem warehouse_;
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MetaStore
operator|.
name|LogKey
argument_list|)
decl_stmt|;
specifier|protected
name|FileStore
name|store_
decl_stmt|;
specifier|protected
name|ROTable
parameter_list|()
block|{ }
specifier|public
name|boolean
name|equals
parameter_list|(
name|Table
name|other
parameter_list|)
block|{
return|return
name|schema_
operator|.
name|equals
argument_list|(
name|other
operator|.
name|schema_
argument_list|)
operator|&&
name|tableName_
operator|.
name|equals
argument_list|(
name|other
operator|.
name|tableName_
argument_list|)
operator|&&
name|whPath_
operator|.
name|equals
argument_list|(
name|other
operator|.
name|whPath_
argument_list|)
return|;
block|}
specifier|protected
name|ROTable
parameter_list|(
name|DB
name|parent
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|UnknownTableException
throws|,
name|MetaException
block|{
name|parent_
operator|=
name|parent
expr_stmt|;
name|tableName_
operator|=
name|tableName
expr_stmt|;
name|conf_
operator|=
name|conf
expr_stmt|;
name|store_
operator|=
operator|new
name|FileStore
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// only choice for now
comment|// check and load the schema
if|if
condition|(
name|store_
operator|.
name|tableExists
argument_list|(
name|parent_
operator|.
name|getName
argument_list|()
argument_list|,
name|tableName
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|UnknownTableException
argument_list|(
literal|"metadata does not exist"
argument_list|)
throw|;
block|}
name|schema_
operator|=
name|store_
operator|.
name|load
argument_list|(
name|parent
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// check the table location is on the default dfs server for safety
name|whPath_
operator|=
operator|new
name|Path
argument_list|(
name|schema_
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|META_TABLE_LOCATION
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|whPath_
operator|.
name|toUri
argument_list|()
operator|.
name|relativize
argument_list|(
name|parent
operator|.
name|whRoot_
operator|.
name|toUri
argument_list|()
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// something potentially wrong as the stored warehouse not the same as our default
comment|// in general we want this, but in the short term, it can't happen
name|LOG
operator|.
name|warn
argument_list|(
name|whPath_
operator|+
literal|" is not the current default fs"
argument_list|)
expr_stmt|;
block|}
comment|// check the data directory is there
try|try
block|{
if|if
condition|(
name|whPath_
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|exists
argument_list|(
name|whPath_
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|UnknownTableException
argument_list|(
literal|"data does not exist:"
operator|+
name|whPath_
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
specifier|public
name|Properties
name|getSchema
parameter_list|()
block|{
return|return
name|schema_
return|;
block|}
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
return|return
name|whPath_
return|;
block|}
comment|/**    * getPartitions    *    * Scan the file system and find all the partitions of this table    * Not recursive right now - needs to be!    *    * @return a list of partitions - not full paths    * @exception MetaException if gneneral problem or this table does not exist.    */
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|getPartitions
parameter_list|()
throws|throws
name|MetaException
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|dirs
decl_stmt|;
try|try
block|{
name|dirs
operator|=
name|whPath_
operator|.
name|getFileSystem
argument_list|(
name|conf_
argument_list|)
operator|.
name|listStatus
argument_list|(
name|whPath_
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
name|MetaException
argument_list|(
literal|"DB Error: Table "
operator|+
name|whPath_
operator|+
literal|" missing?"
argument_list|)
throw|;
block|}
if|if
condition|(
name|dirs
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"FATAL: "
operator|+
name|whPath_
operator|+
literal|" does not seem to exist or maybe has no partitions in DFS"
argument_list|)
throw|;
block|}
name|Boolean
name|equalsSeen
init|=
literal|null
decl_stmt|;
name|String
name|partKey
init|=
literal|null
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
name|dirs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|dname
init|=
name|dirs
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|int
name|sepidx
init|=
name|dname
operator|.
name|indexOf
argument_list|(
literal|'='
argument_list|)
decl_stmt|;
if|if
condition|(
name|sepidx
operator|==
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|equalsSeen
operator|!=
literal|null
operator|&&
name|equalsSeen
operator|.
name|booleanValue
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"DB Error: Table "
operator|+
name|tableName_
operator|+
literal|" dir corrupted?"
argument_list|)
throw|;
block|}
name|equalsSeen
operator|=
name|Boolean
operator|.
name|valueOf
argument_list|(
literal|false
argument_list|)
expr_stmt|;
continue|continue;
block|}
else|else
block|{
if|if
condition|(
name|equalsSeen
operator|!=
literal|null
operator|&&
operator|!
name|equalsSeen
operator|.
name|booleanValue
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"DB Error: Table "
operator|+
name|tableName_
operator|+
literal|" dir corrupted?"
argument_list|)
throw|;
block|}
name|equalsSeen
operator|=
name|Boolean
operator|.
name|valueOf
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|String
name|partVal
init|=
name|dname
operator|.
name|substring
argument_list|(
name|sepidx
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|partKey
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|partKey
operator|.
name|equals
argument_list|(
name|dname
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sepidx
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"DB Error: Directory "
operator|+
name|dirs
index|[
name|i
index|]
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|partKey
operator|=
name|dname
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sepidx
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|add
argument_list|(
name|partKey
operator|+
literal|"="
operator|+
name|partVal
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

begin_empty_stmt
empty_stmt|;
end_empty_stmt

end_unit

