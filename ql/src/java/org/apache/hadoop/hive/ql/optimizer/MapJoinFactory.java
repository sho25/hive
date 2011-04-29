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
name|HashMap
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
name|Context
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
name|exec
operator|.
name|UnionOperator
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
name|optimizer
operator|.
name|GenMRProcContext
operator|.
name|GenMRMapJoinCtx
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
name|GenMRProcContext
operator|.
name|GenMapRedCtx
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
name|unionproc
operator|.
name|UnionProcContext
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
name|ErrorMsg
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
name|FileSinkDesc
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
name|MapredWork
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
name|TableDesc
import|;
end_import

begin_comment
comment|/**  * Operator factory for MapJoin processing.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|MapJoinFactory
block|{
specifier|public
specifier|static
name|int
name|getPositionParent
parameter_list|(
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|op
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
block|{
name|int
name|pos
init|=
literal|0
decl_stmt|;
name|int
name|size
init|=
name|stack
operator|.
name|size
argument_list|()
decl_stmt|;
assert|assert
name|size
operator|>=
literal|2
operator|&&
name|stack
operator|.
name|get
argument_list|(
name|size
operator|-
literal|1
argument_list|)
operator|==
name|op
assert|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|parent
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|stack
operator|.
name|get
argument_list|(
name|size
operator|-
literal|2
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parOp
init|=
name|op
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|pos
operator|=
name|parOp
operator|.
name|indexOf
argument_list|(
name|parent
argument_list|)
expr_stmt|;
assert|assert
name|pos
operator|<
name|parOp
operator|.
name|size
argument_list|()
assert|;
return|return
name|pos
return|;
block|}
comment|/**    * TableScan followed by MapJoin.    */
specifier|public
specifier|static
class|class
name|TableScanMapJoin
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
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
name|mapJoin
init|=
operator|(
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|procCtx
decl_stmt|;
comment|// find the branch on which this processor was invoked
name|int
name|pos
init|=
name|getPositionParent
argument_list|(
name|mapJoin
argument_list|,
name|stack
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|ctx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
name|GenMapRedCtx
name|mapredCtx
init|=
name|mapCurrCtx
operator|.
name|get
argument_list|(
name|mapJoin
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|mapredCtx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|MapredWork
name|currPlan
init|=
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTopOp
init|=
name|mapredCtx
operator|.
name|getCurrTopOp
argument_list|()
decl_stmt|;
name|String
name|currAliasId
init|=
name|mapredCtx
operator|.
name|getCurrAliasId
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|reducer
init|=
name|mapJoin
decl_stmt|;
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|opTaskMap
init|=
name|ctx
operator|.
name|getOpTaskMap
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|opMapTask
init|=
name|opTaskMap
operator|.
name|get
argument_list|(
name|reducer
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|setCurrTopOp
argument_list|(
name|currTopOp
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrAliasId
argument_list|(
name|currAliasId
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
comment|// If the plan for this reducer does not exist, initialize the plan
if|if
condition|(
name|opMapTask
operator|==
literal|null
condition|)
block|{
assert|assert
name|currPlan
operator|.
name|getReducer
argument_list|()
operator|==
literal|null
assert|;
name|GenMapRedUtils
operator|.
name|initMapJoinPlan
argument_list|(
name|mapJoin
argument_list|,
name|ctx
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|pos
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// The current plan can be thrown away after being merged with the
comment|// original plan
name|GenMapRedUtils
operator|.
name|joinPlan
argument_list|(
name|mapJoin
argument_list|,
literal|null
argument_list|,
name|opMapTask
argument_list|,
name|ctx
argument_list|,
name|pos
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|currTask
operator|=
name|opMapTask
expr_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
name|mapCurrCtx
operator|.
name|put
argument_list|(
name|mapJoin
argument_list|,
operator|new
name|GenMapRedCtx
argument_list|(
name|ctx
operator|.
name|getCurrTask
argument_list|()
argument_list|,
name|ctx
operator|.
name|getCurrTopOp
argument_list|()
argument_list|,
name|ctx
operator|.
name|getCurrAliasId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * ReduceSink followed by MapJoin.    */
specifier|public
specifier|static
class|class
name|ReduceSinkMapJoin
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
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
name|mapJoin
init|=
operator|(
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
name|GenMRProcContext
name|opProcCtx
init|=
operator|(
name|GenMRProcContext
operator|)
name|procCtx
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|opProcCtx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|MapredWork
name|cplan
init|=
name|GenMapRedUtils
operator|.
name|getMapRedWork
argument_list|(
name|parseCtx
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|redTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|cplan
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|opProcCtx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
comment|// find the branch on which this processor was invoked
name|int
name|pos
init|=
name|getPositionParent
argument_list|(
name|mapJoin
argument_list|,
name|stack
argument_list|)
decl_stmt|;
name|boolean
name|local
init|=
operator|(
name|pos
operator|==
operator|(
operator|(
name|mapJoin
operator|.
name|getConf
argument_list|()
operator|)
operator|)
operator|.
name|getPosBigTable
argument_list|()
operator|)
condition|?
literal|false
else|:
literal|true
decl_stmt|;
name|GenMapRedUtils
operator|.
name|splitTasks
argument_list|(
name|mapJoin
argument_list|,
name|currTask
argument_list|,
name|redTask
argument_list|,
name|opProcCtx
argument_list|,
literal|false
argument_list|,
name|local
argument_list|,
name|pos
argument_list|)
expr_stmt|;
name|currTask
operator|=
name|opProcCtx
operator|.
name|getCurrTask
argument_list|()
expr_stmt|;
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|opTaskMap
init|=
name|opProcCtx
operator|.
name|getOpTaskMap
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|opMapTask
init|=
name|opTaskMap
operator|.
name|get
argument_list|(
name|mapJoin
argument_list|)
decl_stmt|;
comment|// If the plan for this reducer does not exist, initialize the plan
if|if
condition|(
name|opMapTask
operator|==
literal|null
condition|)
block|{
assert|assert
name|cplan
operator|.
name|getReducer
argument_list|()
operator|==
literal|null
assert|;
name|opTaskMap
operator|.
name|put
argument_list|(
name|mapJoin
argument_list|,
name|currTask
argument_list|)
expr_stmt|;
name|opProcCtx
operator|.
name|setCurrMapJoinOp
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// The current plan can be thrown away after being merged with the
comment|// original plan
name|GenMapRedUtils
operator|.
name|joinPlan
argument_list|(
name|mapJoin
argument_list|,
name|currTask
argument_list|,
name|opMapTask
argument_list|,
name|opProcCtx
argument_list|,
name|pos
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|currTask
operator|=
name|opMapTask
expr_stmt|;
name|opProcCtx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * MapJoin followed by Select.    */
specifier|public
specifier|static
class|class
name|MapJoin
implements|implements
name|NodeProcessor
block|{
comment|/**      * Create a task by splitting the plan below the join. The reason, we have      * to do so in the processing of Select and not MapJoin is due to the      * walker. While processing a node, it is not safe to alter its children      * because that will decide the course of the walk. It is perfectly fine to      * muck around with its parents though, since those nodes have already been      * visited.      */
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
name|SelectOperator
name|sel
init|=
operator|(
name|SelectOperator
operator|)
name|nd
decl_stmt|;
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
name|mapJoin
init|=
operator|(
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
operator|)
name|sel
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|sel
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|procCtx
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|ctx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
comment|// is the mapjoin followed by a reducer
name|List
argument_list|<
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
argument_list|>
name|listMapJoinOps
init|=
name|parseCtx
operator|.
name|getListMapJoinOpsNoReducer
argument_list|()
decl_stmt|;
if|if
condition|(
name|listMapJoinOps
operator|.
name|contains
argument_list|(
name|mapJoin
argument_list|)
condition|)
block|{
name|ctx
operator|.
name|setCurrAliasId
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTopOp
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|ctx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
name|mapCurrCtx
operator|.
name|put
argument_list|(
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
argument_list|,
operator|new
name|GenMapRedCtx
argument_list|(
name|ctx
operator|.
name|getCurrTask
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|ctx
operator|.
name|setCurrMapJoinOp
argument_list|(
name|mapJoin
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|ctx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|GenMRMapJoinCtx
name|mjCtx
init|=
name|ctx
operator|.
name|getMapJoinCtx
argument_list|(
name|mapJoin
argument_list|)
decl_stmt|;
if|if
condition|(
name|mjCtx
operator|==
literal|null
condition|)
block|{
name|mjCtx
operator|=
operator|new
name|GenMRMapJoinCtx
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|setMapJoinCtx
argument_list|(
name|mapJoin
argument_list|,
name|mjCtx
argument_list|)
expr_stmt|;
block|}
name|MapredWork
name|mjPlan
init|=
name|GenMapRedUtils
operator|.
name|getMapRedWork
argument_list|(
name|parseCtx
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mjTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|mjPlan
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|TableDesc
name|tt_desc
init|=
name|PlanUtils
operator|.
name|getIntermediateFileTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromRowSchema
argument_list|(
name|mapJoin
operator|.
name|getSchema
argument_list|()
argument_list|,
literal|"temporarycol"
argument_list|)
argument_list|)
decl_stmt|;
comment|// generate the temporary file
name|Context
name|baseCtx
init|=
name|parseCtx
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|String
name|taskTmpDir
init|=
name|baseCtx
operator|.
name|getMRTmpFileURI
argument_list|()
decl_stmt|;
comment|// Add the path to alias mapping
name|mjCtx
operator|.
name|setTaskTmpDir
argument_list|(
name|taskTmpDir
argument_list|)
expr_stmt|;
name|mjCtx
operator|.
name|setTTDesc
argument_list|(
name|tt_desc
argument_list|)
expr_stmt|;
name|mjCtx
operator|.
name|setRootMapJoinOp
argument_list|(
name|sel
argument_list|)
expr_stmt|;
name|sel
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// Create a file sink operator for this file name
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|fs_op
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
operator|new
name|FileSinkDesc
argument_list|(
name|taskTmpDir
argument_list|,
name|tt_desc
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|COMPRESSINTERMEDIATE
argument_list|)
argument_list|)
argument_list|,
name|mapJoin
operator|.
name|getSchema
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
name|mapJoin
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|mapJoin
operator|.
name|getChildOperators
argument_list|()
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|fs_op
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentOpList
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|parentOpList
operator|.
name|add
argument_list|(
name|mapJoin
argument_list|)
expr_stmt|;
name|fs_op
operator|.
name|setParentOperators
argument_list|(
name|parentOpList
argument_list|)
expr_stmt|;
name|currTask
operator|.
name|addDependentTask
argument_list|(
name|mjTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|mjTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrAliasId
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTopOp
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|ctx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
name|mapCurrCtx
operator|.
name|put
argument_list|(
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
argument_list|,
operator|new
name|GenMapRedCtx
argument_list|(
name|ctx
operator|.
name|getCurrTask
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * MapJoin followed by MapJoin.    */
specifier|public
specifier|static
class|class
name|MapJoinMapJoin
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
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|mapJoin
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
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|procCtx
decl_stmt|;
name|ctx
operator|.
name|getParseCtx
argument_list|()
expr_stmt|;
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|oldMapJoin
init|=
name|ctx
operator|.
name|getCurrMapJoinOp
argument_list|()
decl_stmt|;
name|GenMRMapJoinCtx
name|mjCtx
init|=
name|ctx
operator|.
name|getMapJoinCtx
argument_list|(
name|mapJoin
argument_list|)
decl_stmt|;
if|if
condition|(
name|mjCtx
operator|!=
literal|null
condition|)
block|{
name|mjCtx
operator|.
name|setOldMapJoin
argument_list|(
name|oldMapJoin
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ctx
operator|.
name|setMapJoinCtx
argument_list|(
name|mapJoin
argument_list|,
operator|new
name|GenMRMapJoinCtx
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|oldMapJoin
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|setCurrMapJoinOp
argument_list|(
name|mapJoin
argument_list|)
expr_stmt|;
comment|// find the branch on which this processor was invoked
name|int
name|pos
init|=
name|getPositionParent
argument_list|(
name|mapJoin
argument_list|,
name|stack
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|ctx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
name|GenMapRedCtx
name|mapredCtx
init|=
name|mapCurrCtx
operator|.
name|get
argument_list|(
name|mapJoin
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|mapredCtx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|MapredWork
name|currPlan
init|=
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|mapredCtx
operator|.
name|getCurrAliasId
argument_list|()
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|reducer
init|=
name|mapJoin
decl_stmt|;
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|opTaskMap
init|=
name|ctx
operator|.
name|getOpTaskMap
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|opMapTask
init|=
name|opTaskMap
operator|.
name|get
argument_list|(
name|reducer
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
comment|// If the plan for this reducer does not exist, initialize the plan
if|if
condition|(
name|opMapTask
operator|==
literal|null
condition|)
block|{
assert|assert
name|currPlan
operator|.
name|getReducer
argument_list|()
operator|==
literal|null
assert|;
name|GenMapRedUtils
operator|.
name|initMapJoinPlan
argument_list|(
name|mapJoin
argument_list|,
name|ctx
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|pos
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// The current plan can be thrown away after being merged with the
comment|// original plan
name|GenMapRedUtils
operator|.
name|joinPlan
argument_list|(
name|mapJoin
argument_list|,
name|currTask
argument_list|,
name|opMapTask
argument_list|,
name|ctx
argument_list|,
name|pos
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|currTask
operator|=
name|opMapTask
expr_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
name|mapCurrCtx
operator|.
name|put
argument_list|(
name|mapJoin
argument_list|,
operator|new
name|GenMapRedCtx
argument_list|(
name|ctx
operator|.
name|getCurrTask
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Union followed by MapJoin.    */
specifier|public
specifier|static
class|class
name|UnionMapJoin
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
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|procCtx
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|ctx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|UnionProcContext
name|uCtx
init|=
name|parseCtx
operator|.
name|getUCtx
argument_list|()
decl_stmt|;
comment|// union was map only - no special processing needed
if|if
condition|(
name|uCtx
operator|.
name|isMapOnlySubq
argument_list|()
condition|)
block|{
return|return
operator|(
operator|new
name|TableScanMapJoin
argument_list|()
operator|)
operator|.
name|process
argument_list|(
name|nd
argument_list|,
name|stack
argument_list|,
name|procCtx
argument_list|,
name|nodeOutputs
argument_list|)
return|;
block|}
name|UnionOperator
name|currUnion
init|=
name|ctx
operator|.
name|getCurrUnionOp
argument_list|()
decl_stmt|;
assert|assert
name|currUnion
operator|!=
literal|null
assert|;
name|ctx
operator|.
name|getUnionTask
argument_list|(
name|currUnion
argument_list|)
expr_stmt|;
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
name|mapJoin
init|=
operator|(
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
comment|// find the branch on which this processor was invoked
name|int
name|pos
init|=
name|getPositionParent
argument_list|(
name|mapJoin
argument_list|,
name|stack
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|ctx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
name|GenMapRedCtx
name|mapredCtx
init|=
name|mapCurrCtx
operator|.
name|get
argument_list|(
name|mapJoin
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|mapredCtx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|MapredWork
name|currPlan
init|=
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|reducer
init|=
name|mapJoin
decl_stmt|;
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|opTaskMap
init|=
name|ctx
operator|.
name|getOpTaskMap
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|opMapTask
init|=
name|opTaskMap
operator|.
name|get
argument_list|(
name|reducer
argument_list|)
decl_stmt|;
comment|// union result cannot be a map table
name|boolean
name|local
init|=
operator|(
name|pos
operator|==
operator|(
name|mapJoin
operator|.
name|getConf
argument_list|()
operator|)
operator|.
name|getPosBigTable
argument_list|()
operator|)
condition|?
literal|false
else|:
literal|true
decl_stmt|;
if|if
condition|(
name|local
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_MAPJOIN_TABLE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
comment|// If the plan for this reducer does not exist, initialize the plan
if|if
condition|(
name|opMapTask
operator|==
literal|null
condition|)
block|{
assert|assert
name|currPlan
operator|.
name|getReducer
argument_list|()
operator|==
literal|null
assert|;
name|ctx
operator|.
name|setCurrMapJoinOp
argument_list|(
name|mapJoin
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|initMapJoinPlan
argument_list|(
name|mapJoin
argument_list|,
name|ctx
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|pos
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrUnionOp
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// The current plan can be thrown away after being merged with the
comment|// original plan
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|uTask
init|=
name|ctx
operator|.
name|getUnionTask
argument_list|(
name|ctx
operator|.
name|getCurrUnionOp
argument_list|()
argument_list|)
operator|.
name|getUTask
argument_list|()
decl_stmt|;
if|if
condition|(
name|uTask
operator|.
name|getId
argument_list|()
operator|.
name|equals
argument_list|(
name|opMapTask
operator|.
name|getId
argument_list|()
argument_list|)
condition|)
block|{
name|GenMapRedUtils
operator|.
name|joinPlan
argument_list|(
name|mapJoin
argument_list|,
literal|null
argument_list|,
name|opMapTask
argument_list|,
name|ctx
argument_list|,
name|pos
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|GenMapRedUtils
operator|.
name|joinPlan
argument_list|(
name|mapJoin
argument_list|,
name|uTask
argument_list|,
name|opMapTask
argument_list|,
name|ctx
argument_list|,
name|pos
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|currTask
operator|=
name|opMapTask
expr_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
name|mapCurrCtx
operator|.
name|put
argument_list|(
name|mapJoin
argument_list|,
operator|new
name|GenMapRedCtx
argument_list|(
name|ctx
operator|.
name|getCurrTask
argument_list|()
argument_list|,
name|ctx
operator|.
name|getCurrTopOp
argument_list|()
argument_list|,
name|ctx
operator|.
name|getCurrAliasId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getTableScanMapJoin
parameter_list|()
block|{
return|return
operator|new
name|TableScanMapJoin
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getUnionMapJoin
parameter_list|()
block|{
return|return
operator|new
name|UnionMapJoin
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getReduceSinkMapJoin
parameter_list|()
block|{
return|return
operator|new
name|ReduceSinkMapJoin
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getMapJoin
parameter_list|()
block|{
return|return
operator|new
name|MapJoin
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getMapJoinMapJoin
parameter_list|()
block|{
return|return
operator|new
name|MapJoinMapJoin
argument_list|()
return|;
block|}
specifier|private
name|MapJoinFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

