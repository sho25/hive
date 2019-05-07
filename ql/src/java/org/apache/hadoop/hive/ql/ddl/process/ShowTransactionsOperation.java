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
name|ddl
operator|.
name|process
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|ql
operator|.
name|ddl
operator|.
name|DDLUtils
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
name|ql
operator|.
name|exec
operator|.
name|Utilities
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|GetOpenTxnsInfoResponse
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
name|TxnInfo
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperation
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * Operation process of showing transactions.  */
end_comment

begin_class
specifier|public
class|class
name|ShowTransactionsOperation
extends|extends
name|DDLOperation
block|{
specifier|private
specifier|final
name|ShowTransactionsDesc
name|desc
decl_stmt|;
specifier|public
name|ShowTransactionsOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|ShowTransactionsDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// Call the metastore to get the currently queued and running compactions.
name|GetOpenTxnsInfoResponse
name|rsp
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|showTransactions
argument_list|()
decl_stmt|;
comment|// Write the results into the file
try|try
init|(
name|DataOutputStream
name|os
init|=
name|DDLUtils
operator|.
name|getOutputStream
argument_list|(
operator|new
name|Path
argument_list|(
name|desc
operator|.
name|getResFile
argument_list|()
argument_list|)
argument_list|,
name|context
argument_list|)
init|)
block|{
name|writeHeader
argument_list|(
name|os
argument_list|)
expr_stmt|;
for|for
control|(
name|TxnInfo
name|txn
range|:
name|rsp
operator|.
name|getOpen_txns
argument_list|()
control|)
block|{
name|writeRow
argument_list|(
name|os
argument_list|,
name|txn
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
name|warn
argument_list|(
literal|"show transactions: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|writeHeader
parameter_list|(
name|DataOutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Transaction ID"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Transaction State"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Started Time"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Last Heartbeat Time"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"User"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Hostname"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|newLineCode
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeRow
parameter_list|(
name|DataOutputStream
name|os
parameter_list|,
name|TxnInfo
name|txn
parameter_list|)
throws|throws
name|IOException
block|{
name|os
operator|.
name|writeBytes
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|txn
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|txn
operator|.
name|getState
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|txn
operator|.
name|getStartedTime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|txn
operator|.
name|getLastHeartbeatTime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|txn
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|txn
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|newLineCode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

