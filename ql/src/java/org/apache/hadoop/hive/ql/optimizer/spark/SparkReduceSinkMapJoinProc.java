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
name|spark
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
name|HashTableDummyOperator
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
name|RowSchema
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
name|SparkHashTableSinkOperator
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
name|GenSparkProcContext
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
name|HashTableDummyDesc
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
name|MapJoinDesc
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
name|SparkEdgeProperty
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
name|SparkHashTableSinkDesc
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
name|SparkWork
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
name|TableDesc
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_class
specifier|public
class|class
name|SparkReduceSinkMapJoinProc
implements|implements
name|NodeProcessor
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SparkReduceSinkMapJoinProc
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
name|SparkMapJoinFollowedByGroupByProcessor
implements|implements
name|NodeProcessor
block|{
specifier|private
name|boolean
name|hasGroupBy
init|=
literal|false
decl_stmt|;
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
name|GenSparkProcContext
name|context
init|=
operator|(
name|GenSparkProcContext
operator|)
name|procCtx
decl_stmt|;
name|hasGroupBy
operator|=
literal|true
expr_stmt|;
name|GroupByOperator
name|op
init|=
operator|(
name|GroupByOperator
operator|)
name|nd
decl_stmt|;
name|float
name|groupByMemoryUsage
init|=
name|context
operator|.
name|conf
operator|.
name|getFloatVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPJOINFOLLOWEDBYMAPAGGRHASHMEMORY
argument_list|)
decl_stmt|;
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|setGroupByMemoryUsage
argument_list|(
name|groupByMemoryUsage
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|public
name|boolean
name|getHasGroupBy
parameter_list|()
block|{
return|return
name|hasGroupBy
return|;
block|}
block|}
specifier|private
name|boolean
name|hasGroupBy
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|mapjoinOp
parameter_list|,
name|GenSparkProcContext
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOps
init|=
name|mapjoinOp
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
name|Map
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
name|SparkMapJoinFollowedByGroupByProcessor
name|processor
init|=
operator|new
name|SparkMapJoinFollowedByGroupByProcessor
argument_list|()
decl_stmt|;
name|rules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"GBY"
argument_list|,
name|GroupByOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|processor
argument_list|)
expr_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|rules
argument_list|,
name|context
argument_list|)
decl_stmt|;
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
name|childOps
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
name|processor
operator|.
name|getHasGroupBy
argument_list|()
return|;
block|}
comment|/* (non-Javadoc)    * This processor addresses the RS-MJ case that occurs in spark on the small/hash    * table side of things. The work that RS will be a part of must be connected    * to the MJ work via be a broadcast edge.    * We should not walk down the tree when we encounter this pattern because:    * the type of work (map work or reduce work) needs to be determined    * on the basis of the big table side because it may be a mapwork (no need for shuffle)    * or reduce work.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
name|procContext
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GenSparkProcContext
name|context
init|=
operator|(
name|GenSparkProcContext
operator|)
name|procContext
decl_stmt|;
if|if
condition|(
operator|!
name|nd
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|MapJoinOperator
operator|.
name|class
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|MapJoinOperator
name|mapJoinOp
init|=
operator|(
name|MapJoinOperator
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|stack
operator|.
name|size
argument_list|()
operator|<
literal|2
operator|||
operator|!
operator|(
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
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
name|context
operator|.
name|currentMapJoinOperators
operator|.
name|add
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|context
operator|.
name|preceedingWork
operator|=
literal|null
expr_stmt|;
name|context
operator|.
name|currentRootOperator
operator|=
literal|null
expr_stmt|;
name|ReduceSinkOperator
name|parentRS
init|=
operator|(
name|ReduceSinkOperator
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
comment|// remove the tag for in-memory side of mapjoin
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|setSkipTag
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|parentRS
operator|.
name|setSkipTag
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// remember the original parent list before we start modifying it.
if|if
condition|(
operator|!
name|context
operator|.
name|mapJoinParentMap
operator|.
name|containsKey
argument_list|(
name|mapJoinOp
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|parents
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|mapJoinParentMap
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|parents
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|BaseWork
argument_list|>
name|mapJoinWork
decl_stmt|;
comment|/*      *  If there was a pre-existing work generated for the big-table mapjoin side,      *  we need to hook the work generated for the RS (associated with the RS-MJ pattern)      *  with the pre-existing work.      *      *  Otherwise, we need to associate that the mapjoin op      *  to be linked to the RS work (associated with the RS-MJ pattern).      *      */
name|mapJoinWork
operator|=
name|context
operator|.
name|mapJoinWorkMap
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
name|int
name|workMapSize
init|=
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|parentRS
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|workMapSize
operator|==
literal|1
argument_list|,
literal|"AssertionError: expected context.childToWorkMap.get(parentRS).size() to be 1, but was "
operator|+
name|workMapSize
argument_list|)
expr_stmt|;
name|BaseWork
name|parentWork
init|=
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|parentRS
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// set the link between mapjoin and parent vertex
name|int
name|pos
init|=
name|context
operator|.
name|mapJoinParentMap
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
operator|.
name|indexOf
argument_list|(
name|parentRS
argument_list|)
decl_stmt|;
if|if
condition|(
name|pos
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Cannot find position of parent in mapjoin"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Mapjoin "
operator|+
name|mapJoinOp
operator|+
literal|", pos: "
operator|+
name|pos
operator|+
literal|" --> "
operator|+
name|parentWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getParentToInput
argument_list|()
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|parentWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|SparkEdgeProperty
name|edgeProp
init|=
operator|new
name|SparkEdgeProperty
argument_list|(
name|SparkEdgeProperty
operator|.
name|SHUFFLE_NONE
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapJoinWork
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|myWork
range|:
name|mapJoinWork
control|)
block|{
comment|// link the work with the work associated with the reduce sink that triggered this rule
name|SparkWork
name|sparkWork
init|=
name|context
operator|.
name|currentTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"connecting "
operator|+
name|parentWork
operator|.
name|getName
argument_list|()
operator|+
literal|" with "
operator|+
name|myWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sparkWork
operator|.
name|connect
argument_list|(
name|parentWork
argument_list|,
name|myWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
block|}
block|}
comment|// remember in case we need to connect additional work later
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|SparkEdgeProperty
argument_list|>
name|linkWorkMap
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|containsKey
argument_list|(
name|mapJoinOp
argument_list|)
condition|)
block|{
name|linkWorkMap
operator|=
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|linkWorkMap
operator|=
operator|new
name|HashMap
argument_list|<
name|BaseWork
argument_list|,
name|SparkEdgeProperty
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|linkWorkMap
operator|.
name|put
argument_list|(
name|parentWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|linkWorkMap
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|reduceSinks
init|=
name|context
operator|.
name|linkWorkWithReduceSinkMap
operator|.
name|get
argument_list|(
name|parentWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|reduceSinks
operator|==
literal|null
condition|)
block|{
name|reduceSinks
operator|=
operator|new
name|ArrayList
argument_list|<
name|ReduceSinkOperator
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|reduceSinks
operator|.
name|add
argument_list|(
name|parentRS
argument_list|)
expr_stmt|;
name|context
operator|.
name|linkWorkWithReduceSinkMap
operator|.
name|put
argument_list|(
name|parentWork
argument_list|,
name|reduceSinks
argument_list|)
expr_stmt|;
comment|// create the dummy operators
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|dummyOperators
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// create an new operator: HashTableDummyOperator, which share the table desc
name|HashTableDummyDesc
name|desc
init|=
operator|new
name|HashTableDummyDesc
argument_list|()
decl_stmt|;
name|HashTableDummyOperator
name|dummyOp
init|=
operator|(
name|HashTableDummyOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|mapJoinOp
operator|.
name|getCompilationOpContext
argument_list|()
argument_list|,
name|desc
argument_list|)
decl_stmt|;
name|TableDesc
name|tbl
decl_stmt|;
comment|// need to create the correct table descriptor for key/value
name|RowSchema
name|rowSchema
init|=
name|parentRS
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|tbl
operator|=
name|PlanUtils
operator|.
name|getReduceValueTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromRowSchema
argument_list|(
name|rowSchema
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|getConf
argument_list|()
operator|.
name|setTbl
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|keyExprMap
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getKeys
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
init|=
name|keyExprMap
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|StringBuilder
name|keyOrder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StringBuilder
name|keyNullOrder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keyCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|keyOrder
operator|.
name|append
argument_list|(
literal|"+"
argument_list|)
expr_stmt|;
name|keyNullOrder
operator|.
name|append
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
block|}
name|TableDesc
name|keyTableDesc
init|=
name|PlanUtils
operator|.
name|getReduceKeyTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromColumnList
argument_list|(
name|keyCols
argument_list|,
literal|"mapjoinkey"
argument_list|)
argument_list|,
name|keyOrder
operator|.
name|toString
argument_list|()
argument_list|,
name|keyNullOrder
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|setKeyTableDesc
argument_list|(
name|keyTableDesc
argument_list|)
expr_stmt|;
comment|// let the dummy op be the parent of mapjoin op
name|mapJoinOp
operator|.
name|replaceParent
argument_list|(
name|parentRS
argument_list|,
name|dummyOp
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|dummyChildren
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|dummyChildren
operator|.
name|add
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|setChildOperators
argument_list|(
name|dummyChildren
argument_list|)
expr_stmt|;
name|dummyOperators
operator|.
name|add
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
comment|// cut the operator tree so as to not retain connections from the parent RS downstream
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOperators
init|=
name|parentRS
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
name|int
name|childIndex
init|=
name|childOperators
operator|.
name|indexOf
argument_list|(
name|mapJoinOp
argument_list|)
decl_stmt|;
name|childOperators
operator|.
name|remove
argument_list|(
name|childIndex
argument_list|)
expr_stmt|;
comment|// the "work" needs to know about the dummy operators. They have to be separately initialized
comment|// at task startup
if|if
condition|(
name|mapJoinWork
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|myWork
range|:
name|mapJoinWork
control|)
block|{
name|myWork
operator|.
name|addDummyOp
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|containsKey
argument_list|(
name|mapJoinOp
argument_list|)
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
control|)
block|{
name|dummyOperators
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|dummyOperators
argument_list|)
expr_stmt|;
comment|// replace ReduceSinkOp with HashTableSinkOp for the RSops which are parents of MJop
name|MapJoinDesc
name|mjDesc
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|HiveConf
name|conf
init|=
name|context
operator|.
name|conf
decl_stmt|;
comment|// Unlike in MR, we may call this method multiple times, for each
comment|// small table HTS. But, since it's idempotent, it should be OK.
name|mjDesc
operator|.
name|resetOrder
argument_list|()
expr_stmt|;
name|float
name|hashtableMemoryUsage
decl_stmt|;
if|if
condition|(
name|hasGroupBy
argument_list|(
name|mapJoinOp
argument_list|,
name|context
argument_list|)
condition|)
block|{
name|hashtableMemoryUsage
operator|=
name|conf
operator|.
name|getFloatVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLEFOLLOWBYGBYMAXMEMORYUSAGE
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hashtableMemoryUsage
operator|=
name|conf
operator|.
name|getFloatVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLEMAXMEMORYUSAGE
argument_list|)
expr_stmt|;
block|}
name|mjDesc
operator|.
name|setHashTableMemoryUsage
argument_list|(
name|hashtableMemoryUsage
argument_list|)
expr_stmt|;
name|SparkHashTableSinkDesc
name|hashTableSinkDesc
init|=
operator|new
name|SparkHashTableSinkDesc
argument_list|(
name|mjDesc
argument_list|)
decl_stmt|;
name|SparkHashTableSinkOperator
name|hashTableSinkOp
init|=
operator|(
name|SparkHashTableSinkOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|mapJoinOp
operator|.
name|getCompilationOpContext
argument_list|()
argument_list|,
name|hashTableSinkDesc
argument_list|)
decl_stmt|;
name|byte
name|tag
init|=
operator|(
name|byte
operator|)
name|pos
decl_stmt|;
name|int
index|[]
name|valueIndex
init|=
name|mjDesc
operator|.
name|getValueIndex
argument_list|(
name|tag
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueIndex
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|newValues
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|values
init|=
name|hashTableSinkDesc
operator|.
name|getExprs
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|values
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
name|valueIndex
index|[
name|index
index|]
operator|<
literal|0
condition|)
block|{
name|newValues
operator|.
name|add
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|hashTableSinkDesc
operator|.
name|getExprs
argument_list|()
operator|.
name|put
argument_list|(
name|tag
argument_list|,
name|newValues
argument_list|)
expr_stmt|;
block|}
comment|//get all parents of reduce sink
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|rsParentOps
init|=
name|parentRS
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
range|:
name|rsParentOps
control|)
block|{
name|parent
operator|.
name|replaceChild
argument_list|(
name|parentRS
argument_list|,
name|hashTableSinkOp
argument_list|)
expr_stmt|;
block|}
name|hashTableSinkOp
operator|.
name|setParentOperators
argument_list|(
name|rsParentOps
argument_list|)
expr_stmt|;
name|hashTableSinkOp
operator|.
name|getConf
argument_list|()
operator|.
name|setTag
argument_list|(
name|tag
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

