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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|EnvironmentContext
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
name|Table
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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

begin_comment
comment|/**  * HiveMetaHook defines notification methods which are invoked as part  * of transactions against the metastore, allowing external catalogs  * such as HBase to be kept in sync with Hive's metastore.  *  *<p>  *  * Implementations can use {@link MetaStoreUtils#isExternalTable} to  * distinguish external tables from managed tables.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
interface|interface
name|HiveMetaHook
block|{
specifier|public
name|String
name|ALTER_TABLE_OPERATION_TYPE
init|=
literal|"alterTableOpType"
decl_stmt|;
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|allowedAlterTypes
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"ADDPROPS"
argument_list|,
literal|"DROPPROPS"
argument_list|)
decl_stmt|;
comment|/**    * Called before a new table definition is added to the metastore    * during CREATE TABLE.    *    * @param table new table definition    */
specifier|public
name|void
name|preCreateTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Called after failure adding a new table definition to the metastore    * during CREATE TABLE.    *    * @param table new table definition    */
specifier|public
name|void
name|rollbackCreateTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Called after successfully adding a new table definition to the metastore    * during CREATE TABLE.    *    * @param table new table definition    */
specifier|public
name|void
name|commitCreateTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Called before a table definition is removed from the metastore    * during DROP TABLE.    *    * @param table table definition    */
specifier|public
name|void
name|preDropTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Called after failure removing a table definition from the metastore    * during DROP TABLE.    *    * @param table table definition    */
specifier|public
name|void
name|rollbackDropTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Called after successfully removing a table definition from the metastore    * during DROP TABLE.    *    * @param table table definition    *    * @param deleteData whether to delete data as well; this should typically    * be ignored in the case of an external table    */
specifier|public
name|void
name|commitDropTable
parameter_list|(
name|Table
name|table
parameter_list|,
name|boolean
name|deleteData
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Called before a table is altered in the metastore    * during ALTER TABLE.    *    * @param table new table definition    */
specifier|public
specifier|default
name|void
name|preAlterTable
parameter_list|(
name|Table
name|table
parameter_list|,
name|EnvironmentContext
name|context
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|alterOpType
init|=
name|context
operator|==
literal|null
condition|?
literal|null
else|:
name|context
operator|.
name|getProperties
argument_list|()
operator|.
name|get
argument_list|(
name|ALTER_TABLE_OPERATION_TYPE
argument_list|)
decl_stmt|;
comment|// By default allow only ADDPROPS and DROPPROPS.
comment|// alterOpType is null in case of stats update.
if|if
condition|(
name|alterOpType
operator|!=
literal|null
operator|&&
operator|!
name|allowedAlterTypes
operator|.
name|contains
argument_list|(
name|alterOpType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"ALTER TABLE can not be used for "
operator|+
name|alterOpType
operator|+
literal|" to a non-native table "
argument_list|)
throw|;
block|}
block|}
block|}
end_interface

end_unit

