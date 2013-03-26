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
name|hcatalog
operator|.
name|hbase
operator|.
name|snapshot
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
name|List
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

begin_comment
comment|/**  * This interface provides APIs for implementing revision management.  */
end_comment

begin_interface
specifier|public
interface|interface
name|RevisionManager
block|{
specifier|public
specifier|static
specifier|final
name|String
name|REVISION_MGR_IMPL_CLASS
init|=
literal|"revision.manager.impl.class"
decl_stmt|;
comment|/**      * Initialize the revision manager.      */
specifier|public
name|void
name|initialize
parameter_list|(
name|Properties
name|properties
parameter_list|)
function_decl|;
comment|/**      * Opens the revision manager.      *      * @throws IOException      */
specifier|public
name|void
name|open
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Closes the revision manager.      *      * @throws IOException      */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Start the write transaction.      *      * @param table      * @param families      * @return a new Transaction      * @throws IOException      */
specifier|public
name|Transaction
name|beginWriteTransaction
parameter_list|(
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|families
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Start the write transaction.      *      * @param table      * @param families      * @param keepAlive      * @return a new Transaction      * @throws IOException      */
specifier|public
name|Transaction
name|beginWriteTransaction
parameter_list|(
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|families
parameter_list|,
name|long
name|keepAlive
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Commit the write transaction.      *      * @param transaction      * @throws IOException      */
specifier|public
name|void
name|commitWriteTransaction
parameter_list|(
name|Transaction
name|transaction
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Abort the write transaction.      *      * @param transaction      * @throws IOException      */
specifier|public
name|void
name|abortWriteTransaction
parameter_list|(
name|Transaction
name|transaction
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Get the list of aborted Transactions for a column family      *      * @param table the table name      * @param columnFamily the column family name      * @return a list of aborted WriteTransactions      * @throws java.io.IOException      */
specifier|public
name|List
argument_list|<
name|FamilyRevision
argument_list|>
name|getAbortedWriteTransactions
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|columnFamily
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Create the latest snapshot of the table.      *      * @param tableName      * @return a new snapshot      * @throws IOException      */
specifier|public
name|TableSnapshot
name|createSnapshot
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Create the snapshot of the table using the revision number.      *      * @param tableName      * @param revision      * @return a new snapshot      * @throws IOException      */
specifier|public
name|TableSnapshot
name|createSnapshot
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|revision
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Extends the expiration of a transaction by the time indicated by keep alive.      *      * @param transaction      * @throws IOException      */
specifier|public
name|void
name|keepAlive
parameter_list|(
name|Transaction
name|transaction
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

