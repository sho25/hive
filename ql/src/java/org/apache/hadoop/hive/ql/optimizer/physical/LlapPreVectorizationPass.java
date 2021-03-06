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
name|optimizer
operator|.
name|physical
package|;
end_package

begin_import
import|import static
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
name|optimizer
operator|.
name|physical
operator|.
name|LlapDecider
operator|.
name|LlapMode
operator|.
name|none
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
name|LinkedHashMap
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Stack
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
name|conf
operator|.
name|HiveConf
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
name|MapJoinOperator
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
name|tez
operator|.
name|TezTask
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
name|lib
operator|.
name|DefaultGraphWalker
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
name|lib
operator|.
name|DefaultRuleDispatcher
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
name|lib
operator|.
name|SemanticDispatcher
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
name|lib
operator|.
name|SemanticGraphWalker
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|SemanticNodeProcessor
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|lib
operator|.
name|SemanticRule
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
name|lib
operator|.
name|RuleRegExp
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
name|optimizer
operator|.
name|physical
operator|.
name|LlapDecider
operator|.
name|LlapMode
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
name|plan
operator|.
name|BaseWork
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
name|plan
operator|.
name|TezWork
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * For any LLAP-related transformations which need to occur before vectorization.  */
end_comment

begin_class
specifier|public
class|class
name|LlapPreVectorizationPass
implements|implements
name|PhysicalPlanResolver
block|{
specifier|protected
specifier|static
specifier|transient
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LlapPreVectorizationPass
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|PhysicalContext
name|resolve
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|HiveConf
name|conf
init|=
name|pctx
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|LlapMode
name|mode
init|=
name|LlapMode
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_EXECUTION_MODE
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|mode
operator|==
name|none
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"LLAP disabled."
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
name|SemanticDispatcher
name|disp
init|=
operator|new
name|LlapPreVectorizationPassDispatcher
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
name|SemanticGraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pctx
operator|.
name|getRootTasks
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
class|class
name|LlapPreVectorizationPassDispatcher
implements|implements
name|SemanticDispatcher
block|{
name|HiveConf
name|conf
decl_stmt|;
name|LlapPreVectorizationPassDispatcher
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
block|{
name|conf
operator|=
name|pctx
operator|.
name|getConf
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|dispatch
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Task
argument_list|<
name|?
argument_list|>
name|currTask
init|=
operator|(
name|Task
argument_list|<
name|?
argument_list|>
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|currTask
operator|instanceof
name|TezTask
condition|)
block|{
name|TezWork
name|work
init|=
operator|(
operator|(
name|TezTask
operator|)
name|currTask
operator|)
operator|.
name|getWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|work
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|handleWork
argument_list|(
name|work
argument_list|,
name|w
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|handleWork
parameter_list|(
name|TezWork
name|tezWork
parameter_list|,
name|BaseWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_EXECUTION_MODE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"only"
argument_list|)
operator|&&
operator|!
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_ENABLE_GRACE_JOIN_IN_LLAP
argument_list|)
condition|)
block|{
comment|// In LLAP only mode, grace hash join will be disabled later on by the LlapDispatcher anyway.
comment|// Since the presence of Grace Hash Join disables some "native" vectorization optimizations,
comment|// we will disable the grace hash join now, before vectorization is done.
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"Disable grace hash join if LLAP mode and not dynamic partition hash join"
argument_list|,
name|MapJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|SemanticNodeProcessor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|n
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|s
parameter_list|,
name|NodeProcessorCtx
name|c
parameter_list|,
name|Object
modifier|...
name|os
parameter_list|)
block|{
name|MapJoinOperator
name|mapJoinOp
init|=
operator|(
name|MapJoinOperator
operator|)
name|n
decl_stmt|;
if|if
condition|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isHybridHashJoin
argument_list|()
operator|&&
operator|!
operator|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isDynamicPartitionHashJoin
argument_list|()
operator|)
condition|)
block|{
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|setHybridHashJoin
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
return|return
name|Boolean
operator|.
name|TRUE
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|opRules
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|SemanticDispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|opRules
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|SemanticGraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|work
operator|.
name|getAllRootOperators
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

