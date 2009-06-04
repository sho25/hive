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
name|Map
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
name|Stack
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
name|plan
operator|.
name|mapredWork
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
name|optimizer
operator|.
name|GenMRProcContext
operator|.
name|GenMapRedCtx
import|;
end_import

begin_comment
comment|/**  * Processor for the rule - table scan followed by reduce sink  */
end_comment

begin_class
specifier|public
class|class
name|GenMRRedSink1
implements|implements
name|NodeProcessor
block|{
specifier|public
name|GenMRRedSink1
parameter_list|()
block|{   }
comment|/**    * Reduce Scan encountered     * @param nd the reduce sink operator encountered    * @param opProcCtx context    */
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
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
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
name|mapredWork
name|currPlan
init|=
operator|(
name|mapredWork
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
if|if
condition|(
name|currPlan
operator|.
name|getReducer
argument_list|()
operator|==
literal|null
condition|)
name|GenMapRedUtils
operator|.
name|initPlan
argument_list|(
name|op
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
else|else
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
comment|// This will happen in case of joins. The current plan can be thrown away after being merged with the
comment|// original plan
else|else
block|{
name|GenMapRedUtils
operator|.
name|joinPlan
argument_list|(
name|op
argument_list|,
literal|null
argument_list|,
name|opMapTask
argument_list|,
name|ctx
argument_list|,
operator|-
literal|1
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
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

