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
name|Arrays
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
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|plan
operator|.
name|*
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
name|AbstractMapJoinOperator
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
name|ConditionalTask
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
name|CommonMergeJoinOperator
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
name|JoinOperator
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
name|mr
operator|.
name|MapRedTask
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
name|plan
operator|.
name|TezEdgeProperty
operator|.
name|EdgeType
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/*  * Check each MapJoin and ShuffleJoin Operator to see they are performing a cross product.  * If yes, output a warning to the Session's console.  * The Checks made are the following:  * 1. MR, Shuffle Join:  * Check the parent ReduceSinkOp of the JoinOp. If its keys list is size = 0, then  * this is a cross product.  * The parent ReduceSinkOp is in the MapWork for the same Stage.  * 2. MR, MapJoin:  * If the keys expr list on the mapJoin Desc is an empty list for any input,  * this implies a cross product.  * 3. Tez, Shuffle Join:  * Check the parent ReduceSinkOp of the JoinOp. If its keys list is size = 0, then  * this is a cross product.  * The parent ReduceSinkOp checked is based on the ReduceWork.tagToInput map on the  * reduceWork that contains the JoinOp.  * 4. Tez, Map Join:  * If the keys expr list on the mapJoin Desc is an empty list for any input,  * this implies a cross product.  */
end_comment

begin_class
specifier|public
class|class
name|CrossProductHandler
implements|implements
name|PhysicalPlanResolver
implements|,
name|Dispatcher
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
name|CrossProductHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Boolean
name|cartesianProductEdgeEnabled
init|=
literal|null
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
name|cartesianProductEdgeEnabled
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TEZ_CARTESIAN_PRODUCT_EDGE_ENABLED
argument_list|)
expr_stmt|;
name|TaskGraphWalker
name|ogw
init|=
operator|new
name|TaskGraphWalker
argument_list|(
name|this
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
name|MapRedTask
condition|)
block|{
name|MapRedTask
name|mrTsk
init|=
operator|(
name|MapRedTask
operator|)
name|currTask
decl_stmt|;
name|MapredWork
name|mrWrk
init|=
name|mrTsk
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|checkMapJoins
argument_list|(
name|mrTsk
argument_list|)
expr_stmt|;
name|checkMRReducer
argument_list|(
name|currTask
operator|.
name|toString
argument_list|()
argument_list|,
name|mrWrk
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currTask
operator|instanceof
name|ConditionalTask
condition|)
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|taskListInConditionalTask
init|=
operator|(
operator|(
name|ConditionalTask
operator|)
name|currTask
operator|)
operator|.
name|getListTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|tsk
range|:
name|taskListInConditionalTask
control|)
block|{
name|dispatch
argument_list|(
name|tsk
argument_list|,
name|stack
argument_list|,
name|nodeOutputs
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|currTask
operator|instanceof
name|TezTask
condition|)
block|{
name|TezTask
name|tezTask
init|=
operator|(
name|TezTask
operator|)
name|currTask
decl_stmt|;
name|TezWork
name|tezWork
init|=
name|tezTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|checkMapJoins
argument_list|(
name|tezWork
argument_list|)
expr_stmt|;
name|checkTezReducer
argument_list|(
name|tezWork
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|warn
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Warning: "
operator|+
name|msg
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkMapJoins
parameter_list|(
name|MapRedTask
name|mrTsk
parameter_list|)
throws|throws
name|SemanticException
block|{
name|MapredWork
name|mrWrk
init|=
name|mrTsk
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|MapWork
name|mapWork
init|=
name|mrWrk
operator|.
name|getMapWork
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|warnings
init|=
operator|new
name|MapJoinCheck
argument_list|(
name|mrTsk
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|analyze
argument_list|(
name|mapWork
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|warnings
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|String
name|w
range|:
name|warnings
control|)
block|{
name|warn
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
name|ReduceWork
name|redWork
init|=
name|mrWrk
operator|.
name|getReduceWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|redWork
operator|!=
literal|null
condition|)
block|{
name|warnings
operator|=
operator|new
name|MapJoinCheck
argument_list|(
name|mrTsk
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|analyze
argument_list|(
name|redWork
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|warnings
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|String
name|w
range|:
name|warnings
control|)
block|{
name|warn
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|checkMapJoins
parameter_list|(
name|TezWork
name|tezWork
parameter_list|)
throws|throws
name|SemanticException
block|{
for|for
control|(
name|BaseWork
name|wrk
range|:
name|tezWork
operator|.
name|getAllWork
argument_list|()
control|)
block|{
if|if
condition|(
name|wrk
operator|instanceof
name|MergeJoinWork
condition|)
block|{
name|wrk
operator|=
operator|(
operator|(
name|MergeJoinWork
operator|)
name|wrk
operator|)
operator|.
name|getMainWork
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|warnings
init|=
operator|new
name|MapJoinCheck
argument_list|(
name|wrk
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|analyze
argument_list|(
name|wrk
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|warnings
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|String
name|w
range|:
name|warnings
control|)
block|{
name|warn
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|checkTezReducer
parameter_list|(
name|TezWork
name|tezWork
parameter_list|)
throws|throws
name|SemanticException
block|{
for|for
control|(
name|BaseWork
name|wrk
range|:
name|tezWork
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|BaseWork
name|origWrk
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|wrk
operator|instanceof
name|MergeJoinWork
condition|)
block|{
name|origWrk
operator|=
name|wrk
expr_stmt|;
name|wrk
operator|=
operator|(
operator|(
name|MergeJoinWork
operator|)
name|wrk
operator|)
operator|.
name|getMainWork
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
name|wrk
operator|instanceof
name|ReduceWork
operator|)
condition|)
block|{
continue|continue;
block|}
name|ReduceWork
name|rWork
init|=
operator|(
name|ReduceWork
operator|)
name|wrk
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
init|=
operator|(
operator|(
name|ReduceWork
operator|)
name|wrk
operator|)
operator|.
name|getReducer
argument_list|()
decl_stmt|;
if|if
condition|(
name|reducer
operator|instanceof
name|JoinOperator
operator|||
name|reducer
operator|instanceof
name|CommonMergeJoinOperator
condition|)
block|{
name|boolean
name|noOuterJoin
init|=
operator|(
operator|(
name|JoinDesc
operator|)
name|reducer
operator|.
name|getConf
argument_list|()
operator|)
operator|.
name|isNoOuterJoin
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExtractReduceSinkInfo
operator|.
name|Info
argument_list|>
name|rsInfo
init|=
operator|new
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|ExtractReduceSinkInfo
operator|.
name|Info
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|e
range|:
name|rWork
operator|.
name|getTagToInput
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|rsInfo
operator|.
name|putAll
argument_list|(
name|getReducerInfo
argument_list|(
name|tezWork
argument_list|,
name|rWork
operator|.
name|getName
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|checkForCrossProduct
argument_list|(
name|rWork
operator|.
name|getName
argument_list|()
argument_list|,
name|reducer
argument_list|,
name|rsInfo
argument_list|)
operator|&&
name|cartesianProductEdgeEnabled
operator|&&
name|noOuterJoin
condition|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|parents
init|=
name|tezWork
operator|.
name|getParents
argument_list|(
literal|null
operator|==
name|origWrk
condition|?
name|wrk
else|:
name|origWrk
argument_list|)
decl_stmt|;
for|for
control|(
name|BaseWork
name|p
range|:
name|parents
control|)
block|{
name|TezEdgeProperty
name|prop
init|=
name|tezWork
operator|.
name|getEdgeProperty
argument_list|(
name|p
argument_list|,
literal|null
operator|==
name|origWrk
condition|?
name|wrk
else|:
name|origWrk
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Edge Type: "
operator|+
name|prop
operator|.
name|getEdgeType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|prop
operator|.
name|getEdgeType
argument_list|()
operator|.
name|equals
argument_list|(
name|EdgeType
operator|.
name|CUSTOM_SIMPLE_EDGE
argument_list|)
operator|||
name|prop
operator|.
name|getEdgeType
argument_list|()
operator|.
name|equals
argument_list|(
name|EdgeType
operator|.
name|CUSTOM_EDGE
argument_list|)
condition|)
block|{
name|prop
operator|.
name|setEdgeType
argument_list|(
name|EdgeType
operator|.
name|XPROD_EDGE
argument_list|)
expr_stmt|;
name|rWork
operator|.
name|setNumReduceTasks
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|rWork
operator|.
name|setMaxReduceTasks
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|rWork
operator|.
name|setMinReduceTasks
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
specifier|private
name|void
name|checkMRReducer
parameter_list|(
name|String
name|taskName
parameter_list|,
name|MapredWork
name|mrWrk
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ReduceWork
name|rWrk
init|=
name|mrWrk
operator|.
name|getReduceWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|rWrk
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
init|=
name|rWrk
operator|.
name|getReducer
argument_list|()
decl_stmt|;
if|if
condition|(
name|reducer
operator|instanceof
name|JoinOperator
operator|||
name|reducer
operator|instanceof
name|CommonMergeJoinOperator
condition|)
block|{
name|BaseWork
name|parentWork
init|=
name|mrWrk
operator|.
name|getMapWork
argument_list|()
decl_stmt|;
name|checkForCrossProduct
argument_list|(
name|taskName
argument_list|,
name|reducer
argument_list|,
operator|new
name|ExtractReduceSinkInfo
argument_list|(
literal|null
argument_list|)
operator|.
name|analyze
argument_list|(
name|parentWork
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|checkForCrossProduct
parameter_list|(
name|String
name|taskName
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExtractReduceSinkInfo
operator|.
name|Info
argument_list|>
name|rsInfo
parameter_list|)
block|{
if|if
condition|(
name|rsInfo
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Iterator
argument_list|<
name|ExtractReduceSinkInfo
operator|.
name|Info
argument_list|>
name|it
init|=
name|rsInfo
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|ExtractReduceSinkInfo
operator|.
name|Info
name|info
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|info
operator|.
name|keyCols
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|iAliases
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|iAliases
operator|.
name|addAll
argument_list|(
name|info
operator|.
name|inputAliases
argument_list|)
expr_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|info
operator|=
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
name|iAliases
operator|.
name|addAll
argument_list|(
name|info
operator|.
name|inputAliases
argument_list|)
expr_stmt|;
block|}
name|String
name|warning
init|=
name|String
operator|.
name|format
argument_list|(
literal|"Shuffle Join %s[tables = %s] in Stage '%s' is a cross product"
argument_list|,
name|reducer
operator|.
name|toString
argument_list|()
argument_list|,
name|iAliases
argument_list|,
name|taskName
argument_list|)
decl_stmt|;
name|warn
argument_list|(
name|warning
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExtractReduceSinkInfo
operator|.
name|Info
argument_list|>
name|getReducerInfo
parameter_list|(
name|TezWork
name|tezWork
parameter_list|,
name|String
name|vertex
parameter_list|,
name|String
name|prntVertex
parameter_list|)
throws|throws
name|SemanticException
block|{
name|BaseWork
name|parentWork
init|=
name|tezWork
operator|.
name|getWorkMap
argument_list|()
operator|.
name|get
argument_list|(
name|prntVertex
argument_list|)
decl_stmt|;
return|return
operator|new
name|ExtractReduceSinkInfo
argument_list|(
name|vertex
argument_list|)
operator|.
name|analyze
argument_list|(
name|parentWork
argument_list|)
return|;
block|}
comment|/*    * Given a Work descriptor and the TaskName for the work    * this is responsible to check each MapJoinOp for cross products.    * The analyze call returns the warnings list.    *<p>    * For MR the taskname is the StageName, for Tez it is the vertex name.    */
specifier|public
specifier|static
class|class
name|MapJoinCheck
implements|implements
name|NodeProcessor
implements|,
name|NodeProcessorCtx
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|warnings
decl_stmt|;
specifier|final
name|String
name|taskName
decl_stmt|;
name|MapJoinCheck
parameter_list|(
name|String
name|taskName
parameter_list|)
block|{
name|this
operator|.
name|taskName
operator|=
name|taskName
expr_stmt|;
name|warnings
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|analyze
parameter_list|(
name|BaseWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
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
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|MapJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
operator|new
name|NoopProcessor
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|this
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
return|return
name|warnings
return|;
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|mjOp
init|=
operator|(
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
name|MapJoinDesc
name|mjDesc
init|=
name|mjOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|String
name|bigTablAlias
init|=
name|mjDesc
operator|.
name|getBigTableAlias
argument_list|()
decl_stmt|;
if|if
condition|(
name|bigTablAlias
operator|==
literal|null
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|mjOp
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|op
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|parent
operator|=
name|op
expr_stmt|;
block|}
block|}
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
name|TableScanDesc
name|tDesc
init|=
operator|(
operator|(
name|TableScanOperator
operator|)
name|parent
operator|)
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|bigTablAlias
operator|=
name|tDesc
operator|.
name|getAlias
argument_list|()
expr_stmt|;
block|}
block|}
name|bigTablAlias
operator|=
name|bigTablAlias
operator|==
literal|null
condition|?
literal|"?"
else|:
name|bigTablAlias
expr_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|joinExprs
init|=
name|mjDesc
operator|.
name|getKeys
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|joinExprs
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|warnings
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Map Join %s[bigTable=%s] in task '%s' is a cross product"
argument_list|,
name|mjOp
operator|.
name|toString
argument_list|()
argument_list|,
name|bigTablAlias
argument_list|,
name|taskName
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/*    * for a given Work Descriptor, it extracts information about the ReduceSinkOps    * in the Work. For Tez, you can restrict it to ReduceSinks for a particular output    * vertex.    */
specifier|public
specifier|static
class|class
name|ExtractReduceSinkInfo
implements|implements
name|NodeProcessor
implements|,
name|NodeProcessorCtx
block|{
specifier|static
class|class
name|Info
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|inputAliases
decl_stmt|;
name|Info
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|inputAliases
parameter_list|)
block|{
name|this
operator|.
name|keyCols
operator|=
name|keyCols
expr_stmt|;
name|this
operator|.
name|inputAliases
operator|=
name|inputAliases
operator|==
literal|null
condition|?
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
else|:
name|inputAliases
expr_stmt|;
block|}
name|Info
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
parameter_list|,
name|String
index|[]
name|inputAliases
parameter_list|)
block|{
name|this
operator|.
name|keyCols
operator|=
name|keyCols
expr_stmt|;
name|this
operator|.
name|inputAliases
operator|=
name|inputAliases
operator|==
literal|null
condition|?
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
else|:
name|Arrays
operator|.
name|asList
argument_list|(
name|inputAliases
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|String
name|outputTaskName
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|Info
argument_list|>
name|reduceSinkInfo
decl_stmt|;
name|ExtractReduceSinkInfo
parameter_list|(
name|String
name|parentTaskName
parameter_list|)
block|{
name|this
operator|.
name|outputTaskName
operator|=
name|parentTaskName
expr_stmt|;
name|reduceSinkInfo
operator|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|Info
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|Map
argument_list|<
name|Integer
argument_list|,
name|Info
argument_list|>
name|analyze
parameter_list|(
name|BaseWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
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
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
operator|new
name|NoopProcessor
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|this
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
return|return
name|reduceSinkInfo
return|;
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
name|ReduceSinkOperator
name|rsOp
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|nd
decl_stmt|;
name|ReduceSinkDesc
name|rsDesc
init|=
name|rsOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|outputTaskName
operator|!=
literal|null
condition|)
block|{
name|String
name|rOutputName
init|=
name|rsDesc
operator|.
name|getOutputName
argument_list|()
decl_stmt|;
if|if
condition|(
name|rOutputName
operator|==
literal|null
operator|||
operator|!
name|outputTaskName
operator|.
name|equals
argument_list|(
name|rOutputName
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
name|reduceSinkInfo
operator|.
name|put
argument_list|(
name|rsDesc
operator|.
name|getTag
argument_list|()
argument_list|,
operator|new
name|Info
argument_list|(
name|rsDesc
operator|.
name|getKeyCols
argument_list|()
argument_list|,
name|rsOp
operator|.
name|getInputAliases
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|static
class|class
name|NoopProcessor
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
specifier|final
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
name|nd
return|;
block|}
block|}
block|}
end_class

end_unit

