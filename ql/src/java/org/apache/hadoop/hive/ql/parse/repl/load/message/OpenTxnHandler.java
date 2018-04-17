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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
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
name|messaging
operator|.
name|OpenTxnMessage
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
name|ReplTxnWork
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
name|Task
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
name|TaskFactory
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
name|io
operator|.
name|AcidUtils
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
name|HiveUtils
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
name|parse
operator|.
name|SemanticException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
comment|/**  * OpenTxnHandler  * Target(Load) side handler for open transaction event.  */
end_comment

begin_class
specifier|public
class|class
name|OpenTxnHandler
extends|extends
name|AbstractMessageHandler
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|handle
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
operator|!
name|AcidUtils
operator|.
name|isAcidEnabled
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|)
condition|)
block|{
name|context
operator|.
name|log
operator|.
name|error
argument_list|(
literal|"Cannot load transaction events as acid is not enabled"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Cannot load transaction events as acid is not enabled"
argument_list|)
throw|;
block|}
name|OpenTxnMessage
name|msg
init|=
name|deserializer
operator|.
name|getOpenTxnMessage
argument_list|(
name|context
operator|.
name|dmd
operator|.
name|getPayload
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|ReplTxnWork
argument_list|>
name|openTxnTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|ReplTxnWork
argument_list|(
name|HiveUtils
operator|.
name|getReplPolicy
argument_list|(
name|context
operator|.
name|dbName
argument_list|,
name|context
operator|.
name|tableName
argument_list|)
argument_list|,
name|context
operator|.
name|dbName
argument_list|,
name|context
operator|.
name|tableName
argument_list|,
name|msg
operator|.
name|getTxnIds
argument_list|()
argument_list|,
name|ReplTxnWork
operator|.
name|OperationType
operator|.
name|REPL_OPEN_TXN
argument_list|,
name|context
operator|.
name|eventOnlyReplicationSpec
argument_list|()
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|updatedMetadata
operator|.
name|set
argument_list|(
name|context
operator|.
name|dmd
operator|.
name|getEventTo
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|context
operator|.
name|dbName
argument_list|,
name|context
operator|.
name|tableName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|context
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Added Open txn task : {}"
argument_list|,
name|openTxnTask
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|openTxnTask
argument_list|)
return|;
block|}
block|}
end_class

end_unit

