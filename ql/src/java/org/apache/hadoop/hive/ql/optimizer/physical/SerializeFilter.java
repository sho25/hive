begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|LinkedHashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|ql
operator|.
name|exec
operator|.
name|SerializationUtilities
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
name|StatsTask
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
name|TableScanOperator
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
name|Dispatcher
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
name|GraphWalker
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
name|NodeProcessor
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
name|Rule
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
name|lib
operator|.
name|TaskGraphWalker
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
name|MapWork
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
name|MergeJoinWork
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
name|ReduceWork
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
comment|/**  * SerializeFilter is a simple physical optimizer that serializes all filter expressions in  * Tablescan Operators.  */
end_comment

begin_class
specifier|public
class|class
name|SerializeFilter
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
name|SerializeFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
class|class
name|Serializer
implements|implements
name|Dispatcher
block|{
specifier|private
specifier|final
name|PhysicalContext
name|pctx
decl_stmt|;
specifier|public
name|Serializer
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
block|{
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
operator|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|currTask
operator|instanceof
name|StatsTask
condition|)
block|{
name|currTask
operator|=
operator|(
operator|(
name|StatsTask
operator|)
name|currTask
operator|)
operator|.
name|getWork
argument_list|()
operator|.
name|getSourceTask
argument_list|()
expr_stmt|;
block|}
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
name|evaluateWork
argument_list|(
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
name|evaluateWork
parameter_list|(
name|BaseWork
name|w
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|w
operator|instanceof
name|MapWork
condition|)
block|{
name|evaluateMapWork
argument_list|(
operator|(
name|MapWork
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|ReduceWork
condition|)
block|{
name|evaluateReduceWork
argument_list|(
operator|(
name|ReduceWork
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|MergeJoinWork
condition|)
block|{
name|evaluateMergeWork
argument_list|(
operator|(
name|MergeJoinWork
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"We are not going to evaluate this work type: "
operator|+
name|w
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateMergeWork
parameter_list|(
name|MergeJoinWork
name|w
parameter_list|)
throws|throws
name|SemanticException
block|{
for|for
control|(
name|BaseWork
name|baseWork
range|:
name|w
operator|.
name|getBaseWorkList
argument_list|()
control|)
block|{
name|evaluateOperators
argument_list|(
name|baseWork
argument_list|,
name|pctx
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateReduceWork
parameter_list|(
name|ReduceWork
name|w
parameter_list|)
throws|throws
name|SemanticException
block|{
name|evaluateOperators
argument_list|(
name|w
argument_list|,
name|pctx
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|evaluateMapWork
parameter_list|(
name|MapWork
name|w
parameter_list|)
throws|throws
name|SemanticException
block|{
name|evaluateOperators
argument_list|(
name|w
argument_list|,
name|pctx
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|evaluateOperators
parameter_list|(
name|BaseWork
name|w
parameter_list|,
name|PhysicalContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Dispatcher
name|disp
init|=
literal|null
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|TableScanOperator
argument_list|>
name|tableScans
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|TableScanOperator
argument_list|>
argument_list|()
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|rules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|rules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"TS finder"
argument_list|,
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|NodeProcessor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
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
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
block|{
name|tableScans
operator|.
name|add
argument_list|(
operator|(
name|TableScanOperator
operator|)
name|nd
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|disp
operator|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|rules
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|GraphWalker
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
name|w
operator|.
name|getAllRootOperators
argument_list|()
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|nodeOutput
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
name|nodeOutput
argument_list|)
expr_stmt|;
for|for
control|(
name|TableScanOperator
name|ts
range|:
name|tableScans
control|)
block|{
if|if
condition|(
name|ts
operator|.
name|getConf
argument_list|()
operator|!=
literal|null
operator|&&
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterExpr
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Serializing: "
operator|+
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterExpr
argument_list|()
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|setSerializedFilterExpr
argument_list|(
name|SerializationUtilities
operator|.
name|serializeExpression
argument_list|(
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterExpr
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ts
operator|.
name|getConf
argument_list|()
operator|!=
literal|null
operator|&&
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterObject
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Serializing: "
operator|+
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|setSerializedFilterObject
argument_list|(
name|SerializationUtilities
operator|.
name|serializeObject
argument_list|(
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
class|class
name|DefaultRule
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
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
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
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
name|pctx
operator|.
name|getConf
argument_list|()
expr_stmt|;
comment|// create dispatcher and graph walker
name|Dispatcher
name|disp
init|=
operator|new
name|Serializer
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
name|TaskGraphWalker
name|ogw
init|=
operator|new
name|TaskGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// get all the tasks nodes from root task
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
comment|// begin to walk through the task tree.
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
block|}
end_class

end_unit

