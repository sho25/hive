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

begin_comment
comment|/**  * HiveMetaHook defines notification methods which are invoked as part  * of transactions against the metastore, allowing external catalogs  * such as HBase to be kept in sync with Hive's metastore.  *  *<p>  *  * Implementations can use {@link MetaStoreUtils#isExternalTable} to  * distinguish external tables from managed tables.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveMetaHook
block|{
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
block|}
end_interface

end_unit

