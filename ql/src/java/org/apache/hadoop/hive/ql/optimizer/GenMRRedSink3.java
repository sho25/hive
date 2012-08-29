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
name|HashMap
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
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * Processor for the rule - union followed by reduce sink.  */
end_comment

begin_class
specifier|public
class|class
name|GenMRRedSink3
implements|implements
name|NodeProcessor
block|{
specifier|public
name|GenMRRedSink3
parameter_list|()
block|{   }
comment|/**    * Reduce Scan encountered.    *    * @param nd    *          the reduce sink operator encountered    * @param opProcCtx    *          context    */
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
name|opProcCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ReduceSinkOperator
name|op
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|nd
decl_stmt|;
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|opProcCtx
decl_stmt|;
comment|// union consisted on a bunch of map-reduce jobs, and it has been split at
comment|// the union
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
init|=
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
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
name|ctx
operator|.
name|getCurrUnionOp
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|unionTask
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|mapredCtx
operator|!=
literal|null
condition|)
block|{
name|unionTask
operator|=
name|mapredCtx
operator|.
name|getCurrTask
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|unionTask
operator|=
name|ctx
operator|.
name|getCurrTask
argument_list|()
expr_stmt|;
block|}
name|MapredWork
name|plan
init|=
operator|(
name|MapredWork
operator|)
name|unionTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
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
name|reducerTask
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
name|unionTask
argument_list|)
expr_stmt|;
comment|// If the plan for this reducer does not exist, initialize the plan
if|if
condition|(
name|reducerTask
operator|==
literal|null
condition|)
block|{
comment|// When the reducer is encountered for the first time
if|if
condition|(
name|plan
operator|.
name|getReducer
argument_list|()
operator|==
literal|null
condition|)
block|{
name|GenMapRedUtils
operator|.
name|initUnionPlan
argument_list|(
name|op
argument_list|,
name|ctx
argument_list|,
name|unionTask
argument_list|)
expr_stmt|;
comment|// When union is followed by a multi-table insert
block|}
else|else
block|{
name|GenMapRedUtils
operator|.
name|splitPlan
argument_list|(
name|op
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|plan
operator|.
name|getReducer
argument_list|()
operator|==
name|reducer
condition|)
block|{
comment|// The union is already initialized. However, the union is walked from
comment|// another input
comment|// initUnionPlan is idempotent
name|GenMapRedUtils
operator|.
name|initUnionPlan
argument_list|(
name|op
argument_list|,
name|ctx
argument_list|,
name|unionTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|GenMapRedUtils
operator|.
name|joinUnionPlan
argument_list|(
name|ctx
argument_list|,
name|unionTask
argument_list|,
name|reducerTask
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|reducerTask
argument_list|)
expr_stmt|;
block|}
name|mapCurrCtx
operator|.
name|put
argument_list|(
name|op
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
comment|// the union operator has been processed
name|ctx
operator|.
name|setCurrUnionOp
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

