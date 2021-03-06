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
name|table
operator|.
name|lock
operator|.
name|show
package|;
end_package

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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|QueryState
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
name|DDLSemanticAnalyzerFactory
operator|.
name|DDLType
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
name|ddl
operator|.
name|DDLWork
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
name|lockmgr
operator|.
name|HiveTxnManager
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
name|lockmgr
operator|.
name|LockException
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
name|lockmgr
operator|.
name|TxnManagerFactory
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
name|ASTNode
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
name|BaseSemanticAnalyzer
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
name|HiveParser
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

begin_comment
comment|/**  * Analyzer for show locks commands.  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|types
operator|=
name|HiveParser
operator|.
name|TOK_SHOWLOCKS
argument_list|)
specifier|public
class|class
name|ShowLocksAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|public
name|ShowLocksAnalyzer
parameter_list|(
name|QueryState
name|queryState
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|queryState
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|analyzeInternal
parameter_list|(
name|ASTNode
name|root
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ctx
operator|.
name|setResFile
argument_list|(
name|ctx
operator|.
name|getLocalTmpPath
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
init|=
literal|null
decl_stmt|;
name|boolean
name|isExtended
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|root
operator|.
name|getChildCount
argument_list|()
operator|>=
literal|1
condition|)
block|{
comment|// table for which show locks is being executed
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|root
operator|.
name|getChildCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ASTNode
name|child
init|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TABTYPE
condition|)
block|{
name|tableName
operator|=
name|DDLUtils
operator|.
name|getFQName
argument_list|(
operator|(
name|ASTNode
operator|)
name|child
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// get partition metadata if partition specified
if|if
condition|(
name|child
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
condition|)
block|{
name|ASTNode
name|partitionSpecNode
init|=
operator|(
name|ASTNode
operator|)
name|child
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|partitionSpec
operator|=
name|getValidatedPartSpec
argument_list|(
name|getTable
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|partitionSpecNode
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|KW_EXTENDED
condition|)
block|{
name|isExtended
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
name|HiveTxnManager
name|txnManager
init|=
literal|null
decl_stmt|;
try|try
block|{
name|txnManager
operator|=
name|TxnManagerFactory
operator|.
name|getTxnManagerFactory
argument_list|()
operator|.
name|getTxnManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|ShowLocksDesc
name|desc
init|=
operator|new
name|ShowLocksDesc
argument_list|(
name|ctx
operator|.
name|getResFile
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|partitionSpec
argument_list|,
name|isExtended
argument_list|,
name|txnManager
operator|.
name|useNewShowLocksFormat
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|DDLWork
argument_list|>
name|task
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
name|getInputs
argument_list|()
argument_list|,
name|getOutputs
argument_list|()
argument_list|,
name|desc
argument_list|)
argument_list|)
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|task
operator|.
name|setFetchSource
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|setFetchTask
argument_list|(
name|createFetchTask
argument_list|(
name|desc
operator|.
name|getSchema
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Need to initialize the lock manager
name|ctx
operator|.
name|setNeedLockMgr
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

