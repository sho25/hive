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
package|;
end_package

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
name|DummyStoreOperator
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
name|GenTezProcContext
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
name|TezEdgeProperty
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
operator|.
name|VertexType
import|;
end_import

begin_class
specifier|public
class|class
name|MergeJoinProc
implements|implements
name|SemanticNodeProcessor
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
name|GenTezProcContext
name|context
init|=
operator|(
name|GenTezProcContext
operator|)
name|procCtx
decl_stmt|;
name|CommonMergeJoinOperator
name|mergeJoinOp
init|=
operator|(
name|CommonMergeJoinOperator
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
condition|)
block|{
comment|// safety check for L53 to get parentOp, although it is very unlikely that
comment|// stack size is less than 2, i.e., there is only one MergeJoinOperator in the stack.
name|context
operator|.
name|currentMergeJoinOperator
operator|=
name|mergeJoinOp
expr_stmt|;
return|return
literal|null
return|;
block|}
name|TezWork
name|tezWork
init|=
name|context
operator|.
name|currentTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
init|=
call|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
call|)
argument_list|(
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
operator|)
argument_list|)
decl_stmt|;
comment|// we need to set the merge work that has been created as part of the dummy store walk. If a
comment|// merge work already exists for this merge join operator, add the dummy store work to the
comment|// merge work. Else create a merge work, add above work to the merge work
name|MergeJoinWork
name|mergeWork
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|opMergeJoinWorkMap
operator|.
name|containsKey
argument_list|(
name|mergeJoinOp
argument_list|)
condition|)
block|{
comment|// we already have the merge work corresponding to this merge join operator
name|mergeWork
operator|=
name|context
operator|.
name|opMergeJoinWorkMap
operator|.
name|get
argument_list|(
name|mergeJoinOp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mergeWork
operator|=
operator|new
name|MergeJoinWork
argument_list|()
expr_stmt|;
name|tezWork
operator|.
name|add
argument_list|(
name|mergeWork
argument_list|)
expr_stmt|;
name|context
operator|.
name|opMergeJoinWorkMap
operator|.
name|put
argument_list|(
name|mergeJoinOp
argument_list|,
name|mergeWork
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
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
name|DummyStoreOperator
operator|)
condition|)
block|{
comment|/* this may happen in one of the following case:       TS[0], FIL[26], SEL[2], DUMMY_STORE[30], MERGEJOIN[29]]                                               /                                     TS[3], FIL[27], SEL[5], ---------------       */
name|context
operator|.
name|currentMergeJoinOperator
operator|=
name|mergeJoinOp
expr_stmt|;
name|mergeWork
operator|.
name|setTag
argument_list|(
name|mergeJoinOp
operator|.
name|getTagForOperator
argument_list|(
name|parentOp
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// Guaranteed to be just 1 because each DummyStoreOperator can be part of only one work.
name|BaseWork
name|parentWork
init|=
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|parentOp
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|mergeWork
operator|.
name|addMergedWork
argument_list|(
literal|null
argument_list|,
name|parentWork
argument_list|,
name|context
operator|.
name|leafOperatorToFollowingWork
argument_list|)
expr_stmt|;
name|mergeWork
operator|.
name|setMergeJoinOperator
argument_list|(
name|mergeJoinOp
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|setVertexType
argument_list|(
name|mergeWork
argument_list|,
name|VertexType
operator|.
name|MULTI_INPUT_UNINITIALIZED_EDGES
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|grandParentWork
range|:
name|tezWork
operator|.
name|getParents
argument_list|(
name|parentWork
argument_list|)
control|)
block|{
name|TezEdgeProperty
name|edgeProp
init|=
name|tezWork
operator|.
name|getEdgeProperty
argument_list|(
name|grandParentWork
argument_list|,
name|parentWork
argument_list|)
decl_stmt|;
name|tezWork
operator|.
name|disconnect
argument_list|(
name|grandParentWork
argument_list|,
name|parentWork
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|grandParentWork
argument_list|,
name|mergeWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|BaseWork
name|childWork
range|:
name|tezWork
operator|.
name|getChildren
argument_list|(
name|parentWork
argument_list|)
control|)
block|{
name|TezEdgeProperty
name|edgeProp
init|=
name|tezWork
operator|.
name|getEdgeProperty
argument_list|(
name|parentWork
argument_list|,
name|childWork
argument_list|)
decl_stmt|;
name|tezWork
operator|.
name|disconnect
argument_list|(
name|parentWork
argument_list|,
name|childWork
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|mergeWork
argument_list|,
name|childWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
block|}
name|tezWork
operator|.
name|remove
argument_list|(
name|parentWork
argument_list|)
expr_stmt|;
name|DummyStoreOperator
name|dummyOp
init|=
call|(
name|DummyStoreOperator
call|)
argument_list|(
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
argument_list|)
decl_stmt|;
name|parentWork
operator|.
name|setTag
argument_list|(
name|mergeJoinOp
operator|.
name|getTagForOperator
argument_list|(
name|dummyOp
argument_list|)
argument_list|)
expr_stmt|;
name|mergeJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|remove
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

