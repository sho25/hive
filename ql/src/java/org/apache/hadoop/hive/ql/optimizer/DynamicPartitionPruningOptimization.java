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
name|ql
operator|.
name|optimizer
package|;
end_package

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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|List
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|FilterOperator
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
name|GroupByOperator
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
name|Operator
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
name|OperatorFactory
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
name|ReduceSinkOperator
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
name|SelectOperator
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
name|metadata
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
name|ql
operator|.
name|metadata
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
name|ql
operator|.
name|optimizer
operator|.
name|spark
operator|.
name|SparkPartitionPruningSinkDesc
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
name|OptimizeTezProcContext
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
name|ParseContext
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
name|PrunedPartitionList
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
name|parse
operator|.
name|spark
operator|.
name|OptimizeSparkProcContext
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
name|AggregationDesc
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
name|DynamicPruningEventDesc
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
name|ExprNodeColumnDesc
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
name|ExprNodeConstantDesc
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
name|ExprNodeDesc
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
name|ExprNodeDynamicListDesc
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
name|FilterDesc
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
name|GroupByDesc
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
name|OperatorDesc
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
name|PlanUtils
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
name|SelectDesc
import|;
end_import

begin_comment
comment|/**  * This optimization looks for expressions of the kind "x IN (RS[n])". If such  * an expression made it to a table scan operator and x is a partition column we  * can use an existing join to dynamically prune partitions. This class sets up  * the infrastructure for that.  */
end_comment

begin_class
specifier|public
class|class
name|DynamicPartitionPruningOptimization
implements|implements
name|NodeProcessor
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|DynamicPartitionPruningOptimization
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
class|class
name|DynamicPartitionPrunerProc
implements|implements
name|NodeProcessor
block|{
comment|/**      * process simply remembers all the dynamic partition pruning expressions      * found      */
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
name|ExprNodeDynamicListDesc
name|desc
init|=
operator|(
name|ExprNodeDynamicListDesc
operator|)
name|nd
decl_stmt|;
name|DynamicPartitionPrunerContext
name|context
init|=
operator|(
name|DynamicPartitionPrunerContext
operator|)
name|procCtx
decl_stmt|;
comment|// Rule is searching for dynamic pruning expr. There's at least an IN
comment|// expression wrapping it.
name|ExprNodeDesc
name|parent
init|=
operator|(
name|ExprNodeDesc
operator|)
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|grandParent
init|=
name|stack
operator|.
name|size
argument_list|()
operator|>=
literal|3
condition|?
operator|(
name|ExprNodeDesc
operator|)
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|3
argument_list|)
else|:
literal|null
decl_stmt|;
name|context
operator|.
name|addDynamicList
argument_list|(
name|desc
argument_list|,
name|parent
argument_list|,
name|grandParent
argument_list|,
operator|(
name|ReduceSinkOperator
operator|)
name|desc
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|context
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|DynamicListContext
block|{
specifier|public
name|ExprNodeDynamicListDesc
name|desc
decl_stmt|;
specifier|public
name|ExprNodeDesc
name|parent
decl_stmt|;
specifier|public
name|ExprNodeDesc
name|grandParent
decl_stmt|;
specifier|public
name|ReduceSinkOperator
name|generator
decl_stmt|;
specifier|public
name|DynamicListContext
parameter_list|(
name|ExprNodeDynamicListDesc
name|desc
parameter_list|,
name|ExprNodeDesc
name|parent
parameter_list|,
name|ExprNodeDesc
name|grandParent
parameter_list|,
name|ReduceSinkOperator
name|generator
parameter_list|)
block|{
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|grandParent
operator|=
name|grandParent
expr_stmt|;
name|this
operator|.
name|generator
operator|=
name|generator
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|DynamicPartitionPrunerContext
implements|implements
name|NodeProcessorCtx
implements|,
name|Iterable
argument_list|<
name|DynamicListContext
argument_list|>
block|{
specifier|public
name|List
argument_list|<
name|DynamicListContext
argument_list|>
name|dynLists
init|=
operator|new
name|ArrayList
argument_list|<
name|DynamicListContext
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|addDynamicList
parameter_list|(
name|ExprNodeDynamicListDesc
name|desc
parameter_list|,
name|ExprNodeDesc
name|parent
parameter_list|,
name|ExprNodeDesc
name|grandParent
parameter_list|,
name|ReduceSinkOperator
name|generator
parameter_list|)
block|{
name|dynLists
operator|.
name|add
argument_list|(
operator|new
name|DynamicListContext
argument_list|(
name|desc
argument_list|,
name|parent
argument_list|,
name|grandParent
argument_list|,
name|generator
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|DynamicListContext
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|dynLists
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
specifier|private
name|String
name|extractColName
parameter_list|(
name|ExprNodeDesc
name|root
parameter_list|)
block|{
if|if
condition|(
name|root
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|root
operator|)
operator|.
name|getColumn
argument_list|()
return|;
block|}
else|else
block|{
if|if
condition|(
name|root
operator|.
name|getChildren
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|column
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|d
range|:
name|root
operator|.
name|getChildren
argument_list|()
control|)
block|{
name|String
name|candidate
init|=
name|extractColName
argument_list|(
name|d
argument_list|)
decl_stmt|;
if|if
condition|(
name|column
operator|!=
literal|null
operator|&&
name|candidate
operator|!=
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|candidate
operator|!=
literal|null
condition|)
block|{
name|column
operator|=
name|candidate
expr_stmt|;
block|}
block|}
return|return
name|column
return|;
block|}
block|}
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
name|ParseContext
name|parseContext
decl_stmt|;
if|if
condition|(
name|procCtx
operator|instanceof
name|OptimizeTezProcContext
condition|)
block|{
name|parseContext
operator|=
operator|(
operator|(
name|OptimizeTezProcContext
operator|)
name|procCtx
operator|)
operator|.
name|parseContext
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|procCtx
operator|instanceof
name|OptimizeSparkProcContext
condition|)
block|{
name|parseContext
operator|=
operator|(
operator|(
name|OptimizeSparkProcContext
operator|)
name|procCtx
operator|)
operator|.
name|getParseContext
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"expected parseContext to be either "
operator|+
literal|"OptimizeTezProcContext or OptimizeSparkProcContext, but found "
operator|+
name|procCtx
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
name|FilterOperator
name|filter
init|=
operator|(
name|FilterOperator
operator|)
name|nd
decl_stmt|;
name|FilterDesc
name|desc
init|=
name|filter
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|TableScanOperator
name|ts
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|parseContext
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|TEZ_DYNAMIC_PARTITION_PRUNING
argument_list|)
operator|&&
operator|!
name|parseContext
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|SPARK_DYNAMIC_PARTITION_PRUNING
argument_list|)
condition|)
block|{
comment|// nothing to do when the optimization is off
return|return
literal|null
return|;
block|}
name|DynamicPartitionPrunerContext
name|removerContext
init|=
operator|new
name|DynamicPartitionPrunerContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|filter
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|filter
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|ts
operator|=
operator|(
name|TableScanOperator
operator|)
name|filter
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
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
literal|"Parent: "
operator|+
name|filter
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter: "
operator|+
name|desc
operator|.
name|getPredicateString
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"TableScan: "
operator|+
name|ts
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ts
operator|==
literal|null
condition|)
block|{
comment|// could be a reduce sink
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not find the table scan for "
operator|+
name|filter
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
else|else
block|{
name|Table
name|table
init|=
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
operator|&&
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
comment|// table is not partitioned, skip optimizer
return|return
literal|null
return|;
block|}
block|}
comment|// collect the dynamic pruning conditions
name|removerContext
operator|.
name|dynLists
operator|.
name|clear
argument_list|()
expr_stmt|;
name|walkExprTree
argument_list|(
name|desc
operator|.
name|getPredicate
argument_list|()
argument_list|,
name|removerContext
argument_list|)
expr_stmt|;
for|for
control|(
name|DynamicListContext
name|ctx
range|:
name|removerContext
control|)
block|{
name|String
name|column
init|=
name|extractColName
argument_list|(
name|ctx
operator|.
name|parent
argument_list|)
decl_stmt|;
if|if
condition|(
name|ts
operator|!=
literal|null
operator|&&
name|column
operator|!=
literal|null
condition|)
block|{
name|Table
name|table
init|=
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
operator|&&
name|table
operator|.
name|isPartitionKey
argument_list|(
name|column
argument_list|)
condition|)
block|{
name|String
name|alias
init|=
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getAlias
argument_list|()
decl_stmt|;
name|PrunedPartitionList
name|plist
init|=
name|parseContext
operator|.
name|getPrunedPartitions
argument_list|(
name|alias
argument_list|,
name|ts
argument_list|)
decl_stmt|;
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
literal|"alias: "
operator|+
name|alias
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"pruned partition list: "
argument_list|)
expr_stmt|;
if|if
condition|(
name|plist
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Partition
name|p
range|:
name|plist
operator|.
name|getPartitions
argument_list|()
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|p
operator|.
name|getCompleteName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|plist
operator|==
literal|null
operator|||
name|plist
operator|.
name|getPartitions
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Dynamic partitioning: "
operator|+
name|table
operator|.
name|getCompleteName
argument_list|()
operator|+
literal|"."
operator|+
name|column
argument_list|)
expr_stmt|;
name|generateEventOperatorPlan
argument_list|(
name|ctx
argument_list|,
name|parseContext
argument_list|,
name|ts
argument_list|,
name|column
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// all partitions have been statically removed
name|LOG
operator|.
name|debug
argument_list|(
literal|"No partition pruning necessary."
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Column "
operator|+
name|column
operator|+
literal|" is not a partition column"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// we always remove the condition by replacing it with "true"
name|ExprNodeDesc
name|constNode
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|ctx
operator|.
name|parent
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|ctx
operator|.
name|grandParent
operator|==
literal|null
condition|)
block|{
name|desc
operator|.
name|setPredicate
argument_list|(
name|constNode
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|i
init|=
name|ctx
operator|.
name|grandParent
operator|.
name|getChildren
argument_list|()
operator|.
name|indexOf
argument_list|(
name|ctx
operator|.
name|parent
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|grandParent
operator|.
name|getChildren
argument_list|()
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|grandParent
operator|.
name|getChildren
argument_list|()
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|constNode
argument_list|)
expr_stmt|;
block|}
block|}
comment|// if we pushed the predicate into the table scan we need to remove the
comment|// synthetic conditions there.
name|cleanTableScanFilters
argument_list|(
name|ts
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
specifier|private
name|void
name|cleanTableScanFilters
parameter_list|(
name|TableScanOperator
name|ts
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|ts
operator|==
literal|null
operator|||
name|ts
operator|.
name|getConf
argument_list|()
operator|==
literal|null
operator|||
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterExpr
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// nothing to do
return|return;
block|}
name|DynamicPartitionPrunerContext
name|removerContext
init|=
operator|new
name|DynamicPartitionPrunerContext
argument_list|()
decl_stmt|;
comment|// collect the dynamic pruning conditions
name|removerContext
operator|.
name|dynLists
operator|.
name|clear
argument_list|()
expr_stmt|;
name|walkExprTree
argument_list|(
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterExpr
argument_list|()
argument_list|,
name|removerContext
argument_list|)
expr_stmt|;
for|for
control|(
name|DynamicListContext
name|ctx
range|:
name|removerContext
control|)
block|{
comment|// remove the condition by replacing it with "true"
name|ExprNodeDesc
name|constNode
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|ctx
operator|.
name|parent
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|ctx
operator|.
name|grandParent
operator|==
literal|null
condition|)
block|{
comment|// we're the only node, just clear out the expression
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|setFilterExpr
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|i
init|=
name|ctx
operator|.
name|grandParent
operator|.
name|getChildren
argument_list|()
operator|.
name|indexOf
argument_list|(
name|ctx
operator|.
name|parent
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|grandParent
operator|.
name|getChildren
argument_list|()
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|grandParent
operator|.
name|getChildren
argument_list|()
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|constNode
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|generateEventOperatorPlan
parameter_list|(
name|DynamicListContext
name|ctx
parameter_list|,
name|ParseContext
name|parseContext
parameter_list|,
name|TableScanOperator
name|ts
parameter_list|,
name|String
name|column
parameter_list|)
block|{
comment|// we will put a fork in the plan at the source of the reduce sink
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOfRS
init|=
name|ctx
operator|.
name|generator
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// we need the expr that generated the key of the reduce sink
name|ExprNodeDesc
name|key
init|=
name|ctx
operator|.
name|generator
operator|.
name|getConf
argument_list|()
operator|.
name|getKeyCols
argument_list|()
operator|.
name|get
argument_list|(
name|ctx
operator|.
name|desc
operator|.
name|getKeyIndex
argument_list|()
argument_list|)
decl_stmt|;
comment|// we also need the expr for the partitioned table
name|ExprNodeDesc
name|partKey
init|=
name|ctx
operator|.
name|parent
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
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
literal|"key expr: "
operator|+
name|key
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"partition key expr: "
operator|+
name|partKey
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyExprs
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|keyExprs
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
comment|// group by requires "ArrayList", don't ask.
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|outputNames
operator|.
name|add
argument_list|(
name|HiveConf
operator|.
name|getColumnInternalName
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// project the relevant key column
name|SelectDesc
name|select
init|=
operator|new
name|SelectDesc
argument_list|(
name|keyExprs
argument_list|,
name|outputNames
argument_list|)
decl_stmt|;
name|SelectOperator
name|selectOp
init|=
operator|(
name|SelectOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|select
argument_list|,
name|parentOfRS
argument_list|)
decl_stmt|;
comment|// do a group by on the list to dedup
name|float
name|groupByMemoryUsage
init|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|parseContext
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPAGGRHASHMEMORY
argument_list|)
decl_stmt|;
name|float
name|memoryThreshold
init|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|parseContext
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPAGGRMEMORYTHRESHOLD
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|groupByExprs
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|ExprNodeDesc
name|groupByExpr
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|key
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|outputNames
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|groupByExprs
operator|.
name|add
argument_list|(
name|groupByExpr
argument_list|)
expr_stmt|;
name|GroupByDesc
name|groupBy
init|=
operator|new
name|GroupByDesc
argument_list|(
name|GroupByDesc
operator|.
name|Mode
operator|.
name|HASH
argument_list|,
name|outputNames
argument_list|,
name|groupByExprs
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|AggregationDesc
argument_list|>
argument_list|()
argument_list|,
literal|false
argument_list|,
name|groupByMemoryUsage
argument_list|,
name|memoryThreshold
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|GroupByOperator
name|groupByOp
init|=
operator|(
name|GroupByOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|groupBy
argument_list|,
name|selectOp
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|colMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|colMap
operator|.
name|put
argument_list|(
name|outputNames
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|groupByExpr
argument_list|)
expr_stmt|;
name|groupByOp
operator|.
name|setColumnExprMap
argument_list|(
name|colMap
argument_list|)
expr_stmt|;
comment|// finally add the event broadcast operator
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|parseContext
operator|.
name|getConf
argument_list|()
argument_list|,
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
condition|)
block|{
name|DynamicPruningEventDesc
name|eventDesc
init|=
operator|new
name|DynamicPruningEventDesc
argument_list|()
decl_stmt|;
name|eventDesc
operator|.
name|setTableScan
argument_list|(
name|ts
argument_list|)
expr_stmt|;
name|eventDesc
operator|.
name|setTable
argument_list|(
name|PlanUtils
operator|.
name|getReduceValueTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromColumnList
argument_list|(
name|keyExprs
argument_list|,
literal|"key"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|eventDesc
operator|.
name|setTargetColumnName
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|eventDesc
operator|.
name|setPartKey
argument_list|(
name|partKey
argument_list|)
expr_stmt|;
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|eventDesc
argument_list|,
name|groupByOp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Must be spark branch
name|SparkPartitionPruningSinkDesc
name|desc
init|=
operator|new
name|SparkPartitionPruningSinkDesc
argument_list|()
decl_stmt|;
name|desc
operator|.
name|setTableScan
argument_list|(
name|ts
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setTable
argument_list|(
name|PlanUtils
operator|.
name|getReduceValueTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromColumnList
argument_list|(
name|keyExprs
argument_list|,
literal|"key"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setTargetColumnName
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setPartKey
argument_list|(
name|partKey
argument_list|)
expr_stmt|;
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|desc
argument_list|,
name|groupByOp
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Map
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|walkExprTree
parameter_list|(
name|ExprNodeDesc
name|pred
parameter_list|,
name|NodeProcessorCtx
name|ctx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack. The dispatcher
comment|// generates the plan from the operator tree
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|exprRules
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
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|ExprNodeDynamicListDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|DynamicPartitionPrunerProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|exprRules
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
name|GraphWalker
name|egw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|startNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|startNodes
operator|.
name|add
argument_list|(
name|pred
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|outputMap
init|=
operator|new
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|egw
operator|.
name|startWalking
argument_list|(
name|startNodes
argument_list|,
name|outputMap
argument_list|)
expr_stmt|;
return|return
name|outputMap
return|;
block|}
block|}
end_class

end_unit

