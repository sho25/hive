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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|hbase
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
name|hbase
operator|.
name|client
operator|.
name|HTableInterface
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
name|List
import|;
end_import

begin_comment
comment|/**  * A connection to HBase.  Separated out as an interface so we can slide different transaction  * managers between our code and HBase.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HBaseConnection
extends|extends
name|Configurable
block|{
comment|/**    * Connects to HBase.  This must be called after {@link #setConf} has been called.    * @throws IOException    */
name|void
name|connect
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Close the connection.  No further operations are possible after this is done.    * @throws IOException    */
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Begin a transaction.    * @throws IOException    */
name|void
name|beginTransaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Commit a transaction    * @throws IOException indicates the commit has failed    */
name|void
name|commitTransaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Rollback a transaction    * @throws IOException    */
name|void
name|rollbackTransaction
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Flush commits.  A no-op for transaction implementations since they will write at commit time.    * @param htab Table to flush    * @throws IOException    */
name|void
name|flush
parameter_list|(
name|HTableInterface
name|htab
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Create a new table    * @param tableName name of the table    * @param columnFamilies name of the column families in the table    * @throws IOException    */
name|void
name|createHBaseTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|columnFamilies
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Fetch an existing HBase table.    * @param tableName name of the table    * @return table handle    * @throws IOException    */
name|HTableInterface
name|getHBaseTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Fetch an existing HBase table and force a connection to it.  This should be used only in    * cases where you want to assure that the table exists (ie at install).    * @param tableName name of the table    * @param force if true, force a connection by fetching a non-existant key    * @return table handle    * @throws IOException    */
name|HTableInterface
name|getHBaseTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

