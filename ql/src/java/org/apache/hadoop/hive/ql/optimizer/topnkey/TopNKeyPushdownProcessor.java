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
name|topnkey
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
name|exec
operator|.
name|CommonJoinOperator
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
name|TopNKeyOperator
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
name|JoinCondDesc
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
name|JoinDesc
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
name|ReduceSinkDesc
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
name|TopNKeyDesc
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
name|api
operator|.
name|OperatorType
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
name|topnkey
operator|.
name|TopNKeyProcessor
operator|.
name|copyDown
import|;
end_import

begin_comment
comment|/**  * Implementation of TopNKey operator pushdown.  */
end_comment

begin_class
specifier|public
class|class
name|TopNKeyPushdownProcessor
implements|implements
name|SemanticNodeProcessor
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TopNKeyPushdownProcessor
operator|.
name|class
argument_list|)
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
name|pushdown
argument_list|(
operator|(
name|TopNKeyOperator
operator|)
name|nd
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|pushdown
parameter_list|(
name|TopNKeyOperator
name|topNKey
parameter_list|)
throws|throws
name|SemanticException
block|{
specifier|final
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
init|=
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|parent
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|SELECT
case|:
name|pushdownThroughSelect
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
break|break;
case|case
name|FORWARD
case|:
name|pushdownThroughParent
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
break|break;
case|case
name|GROUPBY
case|:
name|pushdownThroughGroupBy
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
break|break;
case|case
name|REDUCESINK
case|:
name|pushdownThroughReduceSink
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
break|break;
case|case
name|MERGEJOIN
case|:
case|case
name|JOIN
case|:
name|pushDownThroughJoin
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
break|break;
case|case
name|TOPNKEY
case|:
name|pushdownThroughTopNKey
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
break|break;
default|default:
break|break;
block|}
block|}
comment|/**    * Push through Project if expression(s) in TopNKey can be mapped to expression(s) based on Project input.    *    * @param topNKey TopNKey operator to push    * @throws SemanticException when removeChildAndAdoptItsChildren was not successful in the method pushdown    */
specifier|private
name|void
name|pushdownThroughSelect
parameter_list|(
name|TopNKeyOperator
name|topNKey
parameter_list|)
throws|throws
name|SemanticException
block|{
specifier|final
name|SelectOperator
name|select
init|=
operator|(
name|SelectOperator
operator|)
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|TopNKeyDesc
name|topNKeyDesc
init|=
name|topNKey
operator|.
name|getConf
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|mappedColumns
init|=
name|mapColumns
argument_list|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
argument_list|,
name|select
operator|.
name|getColumnExprMap
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mappedColumns
operator|.
name|size
argument_list|()
operator|!=
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Pushing {} through {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|select
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|topNKeyDesc
operator|.
name|setKeyColumns
argument_list|(
name|mappedColumns
argument_list|)
expr_stmt|;
name|moveDown
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
name|pushdown
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|mapColumns
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|columns
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|colExprMap
parameter_list|)
block|{
if|if
condition|(
name|colExprMap
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|0
argument_list|)
return|;
block|}
specifier|final
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|mappedColumns
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|column
range|:
name|columns
control|)
block|{
specifier|final
name|String
name|columnName
init|=
name|column
operator|.
name|getExprString
argument_list|()
decl_stmt|;
if|if
condition|(
name|colExprMap
operator|.
name|containsKey
argument_list|(
name|columnName
argument_list|)
condition|)
block|{
name|mappedColumns
operator|.
name|add
argument_list|(
name|colExprMap
operator|.
name|get
argument_list|(
name|columnName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|mappedColumns
return|;
block|}
specifier|private
name|void
name|pushdownThroughParent
parameter_list|(
name|TopNKeyOperator
name|topNKey
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
init|=
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Pushing {} through {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|parent
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|moveDown
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
name|pushdown
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
block|}
comment|/**    * Push through GroupBy. No grouping sets. If TopNKey expression is same as GroupBy expression,    * we can push it and remove it from above GroupBy. If expression in TopNKey shared common    * prefix with GroupBy, TopNKey could be pushed through GroupBy using that prefix and kept above    * it.    *    * @param topNKey TopNKey operator to push    * @throws SemanticException when removeChildAndAdoptItsChildren was not successful    */
specifier|private
name|void
name|pushdownThroughGroupBy
parameter_list|(
name|TopNKeyOperator
name|topNKey
parameter_list|)
throws|throws
name|SemanticException
block|{
specifier|final
name|GroupByOperator
name|groupBy
init|=
operator|(
name|GroupByOperator
operator|)
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|GroupByDesc
name|groupByDesc
init|=
name|groupBy
operator|.
name|getConf
argument_list|()
decl_stmt|;
specifier|final
name|TopNKeyDesc
name|topNKeyDesc
init|=
name|topNKey
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|CommonKeyPrefix
name|commonKeyPrefix
init|=
name|CommonKeyPrefix
operator|.
name|map
argument_list|(
name|topNKeyDesc
argument_list|,
name|groupByDesc
argument_list|)
decl_stmt|;
if|if
condition|(
name|commonKeyPrefix
operator|.
name|isEmpty
argument_list|()
operator|||
name|commonKeyPrefix
operator|.
name|size
argument_list|()
operator|==
name|topNKeyDesc
operator|.
name|getPartitionKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Pushing a copy of {} through {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|groupBy
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|TopNKeyDesc
name|newTopNKeyDesc
init|=
name|topNKeyDesc
operator|.
name|combine
argument_list|(
name|commonKeyPrefix
argument_list|)
decl_stmt|;
name|pushdown
argument_list|(
name|copyDown
argument_list|(
name|groupBy
argument_list|,
name|newTopNKeyDesc
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|commonKeyPrefix
operator|.
name|size
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing {} above {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|groupBy
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|groupBy
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Push through ReduceSink. If TopNKey expression is same as ReduceSink expression and order is    * the same, we can push it and remove it from above ReduceSink. If expression in TopNKey shared    * common prefix with ReduceSink including same order, TopNKey could be pushed through    * ReduceSink using that prefix and kept above it.    *    * @param topNKey TopNKey operator to push    * @throws SemanticException when removeChildAndAdoptItsChildren was not successful    */
specifier|private
name|void
name|pushdownThroughReduceSink
parameter_list|(
name|TopNKeyOperator
name|topNKey
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ReduceSinkOperator
name|reduceSink
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|ReduceSinkDesc
name|reduceSinkDesc
init|=
name|reduceSink
operator|.
name|getConf
argument_list|()
decl_stmt|;
specifier|final
name|TopNKeyDesc
name|topNKeyDesc
init|=
name|topNKey
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|CommonKeyPrefix
name|commonKeyPrefix
init|=
name|CommonKeyPrefix
operator|.
name|map
argument_list|(
name|topNKeyDesc
argument_list|,
name|reduceSinkDesc
argument_list|)
decl_stmt|;
if|if
condition|(
name|commonKeyPrefix
operator|.
name|isEmpty
argument_list|()
operator|||
name|commonKeyPrefix
operator|.
name|size
argument_list|()
operator|==
name|topNKeyDesc
operator|.
name|getPartitionKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Pushing a copy of {} through {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|reduceSink
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|TopNKeyDesc
name|newTopNKeyDesc
init|=
name|topNKeyDesc
operator|.
name|combine
argument_list|(
name|commonKeyPrefix
argument_list|)
decl_stmt|;
name|pushdown
argument_list|(
name|copyDown
argument_list|(
name|reduceSink
argument_list|,
name|newTopNKeyDesc
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|commonKeyPrefix
operator|.
name|size
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing {} above {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|reduceSink
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|reduceSink
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Only push down through Left Outer Join is supported.
comment|// Right and Full Outer Join support will be added in a follow up patch.
specifier|private
name|void
name|pushDownThroughJoin
parameter_list|(
name|TopNKeyOperator
name|topNKey
parameter_list|)
throws|throws
name|SemanticException
block|{
name|CommonJoinOperator
argument_list|<
name|?
extends|extends
name|JoinDesc
argument_list|>
name|parent
init|=
operator|(
name|CommonJoinOperator
argument_list|<
name|?
extends|extends
name|JoinDesc
argument_list|>
operator|)
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|JoinCondDesc
index|[]
name|joinConds
init|=
name|parent
operator|.
name|getConf
argument_list|()
operator|.
name|getConds
argument_list|()
decl_stmt|;
name|JoinCondDesc
name|firstJoinCond
init|=
name|joinConds
index|[
literal|0
index|]
decl_stmt|;
for|for
control|(
name|JoinCondDesc
name|joinCond
range|:
name|joinConds
control|)
block|{
if|if
condition|(
operator|!
name|firstJoinCond
operator|.
name|equals
argument_list|(
name|joinCond
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
if|if
condition|(
name|firstJoinCond
operator|.
name|getType
argument_list|()
operator|==
name|JoinDesc
operator|.
name|LEFT_OUTER_JOIN
condition|)
block|{
name|pushdownThroughLeftOuterJoin
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Push through LOJ. If TopNKey expression refers fully to expressions from left input, push    * with rewriting of expressions and remove from top of LOJ. If TopNKey expression has a prefix    * that refers to expressions from left input, push with rewriting of those expressions and keep    * on top of LOJ.    *    * @param topNKey TopNKey operator to push    * @throws SemanticException when removeChildAndAdoptItsChildren was not successful    */
specifier|private
name|void
name|pushdownThroughLeftOuterJoin
parameter_list|(
name|TopNKeyOperator
name|topNKey
parameter_list|)
throws|throws
name|SemanticException
block|{
specifier|final
name|TopNKeyDesc
name|topNKeyDesc
init|=
name|topNKey
operator|.
name|getConf
argument_list|()
decl_stmt|;
specifier|final
name|CommonJoinOperator
argument_list|<
name|?
extends|extends
name|JoinDesc
argument_list|>
name|join
init|=
operator|(
name|CommonJoinOperator
argument_list|<
name|?
extends|extends
name|JoinDesc
argument_list|>
operator|)
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|joinInputs
init|=
name|join
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
specifier|final
name|ReduceSinkOperator
name|reduceSinkOperator
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|joinInputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|ReduceSinkDesc
name|reduceSinkDesc
init|=
name|reduceSinkOperator
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|CommonKeyPrefix
name|commonKeyPrefix
init|=
name|CommonKeyPrefix
operator|.
name|map
argument_list|(
name|mapUntilColumnEquals
argument_list|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
argument_list|,
name|join
operator|.
name|getColumnExprMap
argument_list|()
argument_list|)
argument_list|,
name|topNKeyDesc
operator|.
name|getColumnSortOrder
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getNullOrder
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getKeyCols
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getColumnExprMap
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getOrder
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getNullOrder
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|commonKeyPrefix
operator|.
name|isEmpty
argument_list|()
operator|||
name|commonKeyPrefix
operator|.
name|size
argument_list|()
operator|==
name|topNKeyDesc
operator|.
name|getPartitionKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Pushing a copy of {} through {} and {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|join
operator|.
name|getName
argument_list|()
argument_list|,
name|reduceSinkOperator
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|TopNKeyDesc
name|newTopNKeyDesc
init|=
name|topNKeyDesc
operator|.
name|combine
argument_list|(
name|commonKeyPrefix
argument_list|)
decl_stmt|;
name|pushdown
argument_list|(
name|copyDown
argument_list|(
name|reduceSinkOperator
argument_list|,
name|newTopNKeyDesc
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|commonKeyPrefix
operator|.
name|size
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing {} above {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|join
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|join
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|mapUntilColumnEquals
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|columns
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|colExprMap
parameter_list|)
block|{
if|if
condition|(
name|colExprMap
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|0
argument_list|)
return|;
block|}
specifier|final
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|mappedColumns
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|column
range|:
name|columns
control|)
block|{
specifier|final
name|String
name|columnName
init|=
name|column
operator|.
name|getExprString
argument_list|()
decl_stmt|;
if|if
condition|(
name|colExprMap
operator|.
name|containsKey
argument_list|(
name|columnName
argument_list|)
condition|)
block|{
name|mappedColumns
operator|.
name|add
argument_list|(
name|colExprMap
operator|.
name|get
argument_list|(
name|columnName
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
name|mappedColumns
return|;
block|}
block|}
return|return
name|mappedColumns
return|;
block|}
comment|/**    * Push through another Top N Key operator.    * If the TNK operators are the same one of them will be removed. See {@link TopNKeyDesc#isSame}    * else If expression in<code>topnKey</code> is a common prefix in it's parent TNK op and topN property is same    * then<code>topnkey</code> could be pushed through parent.    * If the Top N Key operator can not be pushed through this method tries to remove one of them:    * - if topN property is the same and the keys of one of the operators are subset of the other then the operator    *   can be removed    * - if the keys are the same operator with higher topN value can be removed    * @param topNKey TopNKey operator to push    * @throws SemanticException when removeChildAndAdoptItsChildren was not successful    */
specifier|private
name|void
name|pushdownThroughTopNKey
parameter_list|(
name|TopNKeyOperator
name|topNKey
parameter_list|)
throws|throws
name|SemanticException
block|{
name|TopNKeyOperator
name|parent
init|=
operator|(
name|TopNKeyOperator
operator|)
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasSameTopNKeyDesc
argument_list|(
name|parent
argument_list|,
name|topNKey
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing {} above same operator: {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|parent
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|parent
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
return|return;
block|}
name|TopNKeyDesc
name|topNKeyDesc
init|=
name|topNKey
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|TopNKeyDesc
name|parentTopNKeyDesc
init|=
name|parent
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|CommonKeyPrefix
name|commonKeyPrefix
init|=
name|CommonKeyPrefix
operator|.
name|map
argument_list|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getColumnSortOrder
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getNullOrder
argument_list|()
argument_list|,
name|parentTopNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
argument_list|,
name|parentTopNKeyDesc
operator|.
name|getColumnSortOrder
argument_list|()
argument_list|,
name|parentTopNKeyDesc
operator|.
name|getNullOrder
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|topNKeyDesc
operator|.
name|getTopN
argument_list|()
operator|==
name|parentTopNKeyDesc
operator|.
name|getTopN
argument_list|()
condition|)
block|{
if|if
condition|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|commonKeyPrefix
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// TNK keys are subset of the parent TNK keys
name|pushdownThroughParent
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
if|if
condition|(
name|topNKey
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
operator|==
name|OperatorType
operator|.
name|TOPNKEY
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing {} since child {} supersedes it"
argument_list|,
name|parent
operator|.
name|getName
argument_list|()
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parentTopNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|commonKeyPrefix
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// parent TNK keys are subset of TNK keys
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing parent of {} since it supersedes"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|parent
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|commonKeyPrefix
operator|.
name|size
argument_list|()
operator|&&
name|parentTopNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|commonKeyPrefix
operator|.
name|size
argument_list|()
condition|)
block|{
if|if
condition|(
name|topNKeyDesc
operator|.
name|getTopN
argument_list|()
operator|>
name|parentTopNKeyDesc
operator|.
name|getTopN
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing {}. Parent {} has same keys but lower topN {}> {}"
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|parent
operator|.
name|getName
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getTopN
argument_list|()
argument_list|,
name|parentTopNKeyDesc
operator|.
name|getTopN
argument_list|()
argument_list|)
expr_stmt|;
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing parent {}. {} has same keys but lower topN {}< {}"
argument_list|,
name|parent
operator|.
name|getName
argument_list|()
argument_list|,
name|topNKey
operator|.
name|getName
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getTopN
argument_list|()
argument_list|,
name|parentTopNKeyDesc
operator|.
name|getTopN
argument_list|()
argument_list|)
expr_stmt|;
name|parent
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|boolean
name|hasSameTopNKeyDesc
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
parameter_list|,
name|TopNKeyDesc
name|desc
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|operator
operator|instanceof
name|TopNKeyOperator
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
specifier|final
name|TopNKeyOperator
name|topNKey
init|=
operator|(
name|TopNKeyOperator
operator|)
name|operator
decl_stmt|;
specifier|final
name|TopNKeyDesc
name|opDesc
init|=
name|topNKey
operator|.
name|getConf
argument_list|()
decl_stmt|;
return|return
name|opDesc
operator|.
name|isSame
argument_list|(
name|desc
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|void
name|moveDown
parameter_list|(
name|TopNKeyOperator
name|topNKey
parameter_list|)
throws|throws
name|SemanticException
block|{
assert|assert
name|topNKey
operator|.
name|getNumParent
argument_list|()
operator|==
literal|1
assert|;
specifier|final
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
init|=
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|grandParents
init|=
name|parent
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|parent
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|grandParent
range|:
name|grandParents
control|)
block|{
name|grandParent
operator|.
name|replaceChild
argument_list|(
name|parent
argument_list|,
name|topNKey
argument_list|)
expr_stmt|;
block|}
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|topNKey
operator|.
name|getParentOperators
argument_list|()
operator|.
name|addAll
argument_list|(
name|grandParents
argument_list|)
expr_stmt|;
name|topNKey
operator|.
name|getChildOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|topNKey
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|parent
operator|.
name|getParentOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|parent
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|topNKey
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

