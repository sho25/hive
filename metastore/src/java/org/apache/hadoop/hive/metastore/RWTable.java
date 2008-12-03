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
name|Properties
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
name|MetaStoreUtils
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

begin_class
specifier|public
class|class
name|RWTable
extends|extends
name|ROTable
block|{
comment|// bugbug - make this a param
name|boolean
name|use_trash_
init|=
literal|false
decl_stmt|;
specifier|protected
name|RWTable
parameter_list|()
block|{
comment|// used internally for creates
block|}
specifier|protected
name|boolean
name|o_rdonly_
decl_stmt|;
comment|// not called directly -- use DB.getTable
specifier|protected
name|RWTable
parameter_list|(
name|DB
name|parent
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|boolean
name|o_rdonly
parameter_list|)
throws|throws
name|UnknownTableException
throws|,
name|MetaException
block|{
name|super
argument_list|(
name|parent
argument_list|,
name|tableName
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|o_rdonly_
operator|=
name|o_rdonly
expr_stmt|;
block|}
comment|//    protected void finalize() {    }
specifier|public
name|void
name|createPartition
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|whPath_
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|path
operator|.
name|getFileSystem
argument_list|(
name|this
operator|.
name|conf_
argument_list|)
operator|.
name|mkdirs
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
comment|/**    * drop    *    * delete the schema for this table and optionally delete the data. Note the data is actually moved to the    * Trash, not really deleted.    *    * @param deleteData should we delete the underlying data or just the schema?    * @exception MetaException if any problems instantiating this object    *    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|void
name|drop
parameter_list|(
name|boolean
name|deleteData
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|this
operator|.
name|o_rdonly_
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"cannot perform write operation on a read-only table"
argument_list|)
throw|;
block|}
if|if
condition|(
name|deleteData
condition|)
block|{
name|MetaStoreUtils
operator|.
name|deleteWHDirectory
argument_list|(
name|this
operator|.
name|whPath_
argument_list|,
name|this
operator|.
name|conf_
argument_list|,
name|this
operator|.
name|use_trash_
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|this
operator|.
name|store_
operator|.
name|drop
argument_list|(
name|this
operator|.
name|parent_
argument_list|,
name|this
operator|.
name|tableName_
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
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|this
operator|.
name|o_rdonly_
operator|=
literal|true
expr_stmt|;
comment|// the table is dropped, so can only do reads now
block|}
comment|/**    * drop    *    * delete the schema for this table and optionally delete the data. Note the data is actually moved to the    * Trash, not really deleted.    *    * @exception MetaException if any problems instantiating this object    *    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|void
name|drop
parameter_list|()
throws|throws
name|MetaException
block|{
comment|//external table, don't delete the data;
name|boolean
name|isExternal
init|=
literal|"TRUE"
operator|.
name|equalsIgnoreCase
argument_list|(
name|this
operator|.
name|schema_
operator|.
name|getProperty
argument_list|(
literal|"EXTERNAL"
argument_list|)
argument_list|)
decl_stmt|;
name|drop
argument_list|(
operator|!
name|isExternal
argument_list|)
expr_stmt|;
block|}
comment|/**    * truncate    *    * delete the data, but not the schema    * Can be applied on a partition by partition basis    *    * @param partition partition in that table or "" or null    * @exception MetaException if any problems instantiating this object    *    */
specifier|public
name|void
name|truncate
parameter_list|()
throws|throws
name|MetaException
block|{
name|truncate
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|void
name|truncate
parameter_list|(
name|String
name|partition
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|this
operator|.
name|o_rdonly_
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"cannot perform write operation on a read-only table"
argument_list|)
throw|;
block|}
try|try
block|{
name|MetaStoreUtils
operator|.
name|deleteWHDirectory
argument_list|(
operator|(
name|partition
operator|==
literal|null
operator|||
name|partition
operator|.
name|length
argument_list|()
operator|==
literal|0
operator|)
condition|?
name|this
operator|.
name|whPath_
else|:
operator|new
name|Path
argument_list|(
name|this
operator|.
name|whPath_
argument_list|,
name|partition
argument_list|)
argument_list|,
name|this
operator|.
name|conf_
argument_list|,
name|this
operator|.
name|use_trash_
argument_list|)
expr_stmt|;
comment|// ensure the directory is re-made
if|if
condition|(
name|partition
operator|==
literal|null
operator|||
name|partition
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|whPath_
operator|.
name|getFileSystem
argument_list|(
name|this
operator|.
name|conf_
argument_list|)
operator|.
name|mkdirs
argument_list|(
name|this
operator|.
name|whPath_
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
throw|throw
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * alter    *    * Add column names to a column set ser de table.    *    * @param tableName the name of the table to alter    * @param columns the name of the columns    * @exception MetaException if any problems altering the table    *    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|void
name|alter
parameter_list|(
name|Properties
name|schema
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|this
operator|.
name|o_rdonly_
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"cannot perform write operation on a read-only table"
argument_list|)
throw|;
block|}
comment|// check if a rename, in which case, we move the schema file and the data
name|String
name|newName
init|=
name|schema
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|String
name|newLoc
init|=
name|schema
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|META_TABLE_LOCATION
argument_list|)
decl_stmt|;
if|if
condition|(
name|newName
operator|.
name|equals
argument_list|(
name|this
operator|.
name|tableName_
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// RENAME
name|Path
name|newPath
init|=
name|newLoc
operator|.
name|equals
argument_list|(
name|this
operator|.
name|whPath_
operator|.
name|toUri
argument_list|()
operator|.
name|toASCIIString
argument_list|()
argument_list|)
condition|?
name|this
operator|.
name|parent_
operator|.
name|getDefaultTablePath
argument_list|(
name|newName
argument_list|)
else|:
operator|new
name|Path
argument_list|(
name|newLoc
argument_list|)
decl_stmt|;
try|try
block|{
comment|// bugbug cannot move from one DFS to another I don't think - ask dhruba how to support this
name|this
operator|.
name|whPath_
operator|.
name|getFileSystem
argument_list|(
name|this
operator|.
name|conf_
argument_list|)
operator|.
name|rename
argument_list|(
name|this
operator|.
name|whPath_
argument_list|,
name|newPath
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
name|warn
argument_list|(
literal|"got IOException in rename table: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|whPath_
operator|=
name|newPath
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|META_TABLE_LOCATION
argument_list|,
name|newPath
operator|.
name|toUri
argument_list|()
operator|.
name|toASCIIString
argument_list|()
argument_list|)
expr_stmt|;
comment|// for now no support for moving between dbs!
comment|// NOTE - bugbug, slight window when wrong schema on disk
name|this
operator|.
name|store_
operator|.
name|rename
argument_list|(
name|this
operator|.
name|parent_
argument_list|,
name|this
operator|.
name|tableName_
argument_list|,
name|this
operator|.
name|parent_
argument_list|,
name|newName
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName_
operator|=
name|newName
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|newLoc
operator|.
name|equals
argument_list|(
name|this
operator|.
name|schema_
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|META_TABLE_LOCATION
argument_list|)
argument_list|)
condition|)
block|{
comment|// just location change
name|Path
name|newPath
init|=
operator|new
name|Path
argument_list|(
name|newLoc
argument_list|)
decl_stmt|;
try|try
block|{
comment|// bugbug cannot move from one DFS to another I don't think - ask dhruba how to support this
name|this
operator|.
name|whPath_
operator|.
name|getFileSystem
argument_list|(
name|this
operator|.
name|conf_
argument_list|)
operator|.
name|rename
argument_list|(
name|this
operator|.
name|whPath_
argument_list|,
name|newPath
argument_list|)
expr_stmt|;
name|this
operator|.
name|whPath_
operator|=
name|newPath
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|META_TABLE_LOCATION
argument_list|,
name|this
operator|.
name|whPath_
operator|.
name|toUri
argument_list|()
operator|.
name|toASCIIString
argument_list|()
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
name|warn
argument_list|(
literal|"got IOException in rename table: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|schema_
operator|=
name|schema
expr_stmt|;
name|save
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * create    *    *    *    *    * @exception MetaException if any problems encountered during the creation    *    */
specifier|static
specifier|public
name|Table
name|create
parameter_list|(
name|DB
name|parent
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Properties
name|schema
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
name|Table
name|newTable
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|newTable
operator|.
name|parent_
operator|=
name|parent
expr_stmt|;
name|newTable
operator|.
name|tableName_
operator|=
name|tableName
expr_stmt|;
name|newTable
operator|.
name|conf_
operator|=
name|conf
expr_stmt|;
name|newTable
operator|.
name|o_rdonly_
operator|=
literal|false
expr_stmt|;
name|newTable
operator|.
name|schema_
operator|=
name|schema
expr_stmt|;
name|newTable
operator|.
name|store_
operator|=
operator|new
name|FileStore
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|MetaStoreUtils
operator|.
name|validateName
argument_list|(
name|tableName
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Invalid table name: "
operator|+
name|tableName
operator|+
literal|" - allowed characters are \\w and _"
argument_list|)
throw|;
block|}
name|String
name|location
init|=
name|schema
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|META_TABLE_LOCATION
argument_list|)
decl_stmt|;
if|if
condition|(
name|location
operator|==
literal|null
condition|)
block|{
name|newTable
operator|.
name|whPath_
operator|=
name|parent
operator|.
name|getDefaultTablePath
argument_list|(
name|tableName
argument_list|,
operator|(
name|String
operator|)
literal|null
argument_list|)
expr_stmt|;
name|newTable
operator|.
name|schema_
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|META_TABLE_LOCATION
argument_list|,
name|newTable
operator|.
name|whPath_
operator|.
name|toUri
argument_list|()
operator|.
name|toASCIIString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newTable
operator|.
name|whPath_
operator|=
operator|new
name|Path
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|newTable
operator|.
name|whPath_
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|exists
argument_list|(
name|newTable
operator|.
name|whPath_
argument_list|)
condition|)
block|{
comment|// current unit tests will fail
comment|// throw new MetaException("for new table: " + tableName + " " + newTable.whPath_ + " already exists cannot create??");
block|}
else|else
block|{
name|newTable
operator|.
name|whPath_
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|mkdirs
argument_list|(
name|newTable
operator|.
name|whPath_
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
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|newTable
operator|.
name|save
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|newTable
return|;
block|}
comment|/**    * save    *    * Save the schema. Note this will save the schema in the format of its choice, potentially doing some rewrites    * from this code's version to a previous version for backwards compatability.    *    * @param overwrite - should this be a create or an alter basically    * @exception MetaException if any problems saving the schema    *    */
specifier|protected
name|void
name|save
parameter_list|(
name|boolean
name|overwrite
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|this
operator|.
name|o_rdonly_
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"cannot perform write operation on a read-only table"
argument_list|)
throw|;
block|}
comment|// bugbug - should check for optomistic concurrency somewhere around here.
name|this
operator|.
name|store_
operator|.
name|store
argument_list|(
name|this
operator|.
name|parent_
argument_list|,
name|this
operator|.
name|tableName_
argument_list|,
name|this
operator|.
name|schema_
argument_list|,
name|overwrite
argument_list|)
expr_stmt|;
return|return ;
block|}
block|}
end_class

end_unit

