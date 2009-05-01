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
name|util
operator|.
name|List
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
name|Configurable
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
name|InvalidObjectException
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
name|NoSuchObjectException
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
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Type
import|;
end_import

begin_interface
specifier|public
interface|interface
name|RawStore
extends|extends
name|Configurable
block|{
specifier|public
specifier|abstract
name|void
name|shutdown
parameter_list|()
function_decl|;
comment|/**    * Opens a new one or the one already created    * Every call of this function must have corresponding commit or rollback function call    * @return an active transaction    */
specifier|public
specifier|abstract
name|boolean
name|openTransaction
parameter_list|()
function_decl|;
comment|/**    * if this is the commit of the first open call then an actual commit is called.     * @return true or false    */
specifier|public
specifier|abstract
name|boolean
name|commitTransaction
parameter_list|()
function_decl|;
comment|/**    * Rolls back the current transaction if it is active    */
specifier|public
specifier|abstract
name|void
name|rollbackTransaction
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|createDatabase
parameter_list|(
name|Database
name|db
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|createDatabase
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|Database
name|getDatabase
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|NoSuchObjectException
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|dropDatabase
parameter_list|(
name|String
name|dbname
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|List
argument_list|<
name|String
argument_list|>
name|getDatabases
parameter_list|()
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|createType
parameter_list|(
name|Type
name|type
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|Type
name|getType
parameter_list|(
name|String
name|typeName
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|dropType
parameter_list|(
name|String
name|typeName
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|void
name|createTable
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|dropTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|Table
name|getTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|addPartition
parameter_list|(
name|Partition
name|part
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|Partition
name|getPartition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|dropPartition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitions
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|int
name|max
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|void
name|alterTable
parameter_list|(
name|String
name|dbname
parameter_list|,
name|String
name|name
parameter_list|,
name|Table
name|newTable
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
function_decl|;
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTables
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|pattern
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|List
argument_list|<
name|String
argument_list|>
name|listPartitionNames
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|short
name|max_parts
parameter_list|)
throws|throws
name|MetaException
function_decl|;
specifier|public
specifier|abstract
name|void
name|alterPartition
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|Partition
name|new_part
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
function_decl|;
block|}
end_interface

end_unit

