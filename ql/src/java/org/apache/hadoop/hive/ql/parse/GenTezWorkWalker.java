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
name|parse
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
name|Collection
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
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * Walks the operator tree in DFS fashion.  */
end_comment

begin_class
specifier|public
class|class
name|GenTezWorkWalker
extends|extends
name|DefaultGraphWalker
block|{
specifier|private
specifier|final
name|GenTezProcContext
name|ctx
decl_stmt|;
comment|/**    * constructor of the walker - the dispatcher is passed.    *    * @param disp the dispatcher to be called for each node visited    * @param ctx the context where we'll set the current root operator    *    */
specifier|public
name|GenTezWorkWalker
parameter_list|(
name|Dispatcher
name|disp
parameter_list|,
name|GenTezProcContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|disp
argument_list|)
expr_stmt|;
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
block|}
specifier|private
name|void
name|setRoot
parameter_list|(
name|Node
name|nd
parameter_list|)
block|{
name|ctx
operator|.
name|currentRootOperator
operator|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|nd
expr_stmt|;
name|ctx
operator|.
name|preceedingWork
operator|=
literal|null
expr_stmt|;
name|ctx
operator|.
name|parentOfRoot
operator|=
literal|null
expr_stmt|;
name|ctx
operator|.
name|currentUnionOperators
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/**    * starting point for walking.    *    * @throws SemanticException    */
annotation|@
name|Override
specifier|public
name|void
name|startWalking
parameter_list|(
name|Collection
argument_list|<
name|Node
argument_list|>
name|startNodes
parameter_list|,
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|nodeOutput
parameter_list|)
throws|throws
name|SemanticException
block|{
name|toWalk
operator|.
name|addAll
argument_list|(
name|startNodes
argument_list|)
expr_stmt|;
while|while
condition|(
name|toWalk
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Node
name|nd
init|=
name|toWalk
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|setRoot
argument_list|(
name|nd
argument_list|)
expr_stmt|;
name|walk
argument_list|(
name|nd
argument_list|)
expr_stmt|;
if|if
condition|(
name|nodeOutput
operator|!=
literal|null
condition|)
block|{
name|nodeOutput
operator|.
name|put
argument_list|(
name|nd
argument_list|,
name|retMap
operator|.
name|get
argument_list|(
name|nd
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Walk the given operator.    *    * @param nd operator being walked    */
annotation|@
name|Override
specifier|protected
name|void
name|walk
parameter_list|(
name|Node
name|nd
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|children
init|=
name|nd
operator|.
name|getChildren
argument_list|()
decl_stmt|;
comment|// maintain the stack of operators encountered
name|opStack
operator|.
name|push
argument_list|(
name|nd
argument_list|)
expr_stmt|;
name|Boolean
name|skip
init|=
name|dispatchAndReturn
argument_list|(
name|nd
argument_list|,
name|opStack
argument_list|)
decl_stmt|;
comment|// save some positional state
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currentRoot
init|=
name|ctx
operator|.
name|currentRootOperator
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOfRoot
init|=
name|ctx
operator|.
name|parentOfRoot
decl_stmt|;
name|List
argument_list|<
name|UnionOperator
argument_list|>
name|currentUnionOperators
init|=
name|ctx
operator|.
name|currentUnionOperators
decl_stmt|;
name|BaseWork
name|preceedingWork
init|=
name|ctx
operator|.
name|preceedingWork
decl_stmt|;
if|if
condition|(
name|skip
operator|==
literal|null
operator|||
operator|!
name|skip
condition|)
block|{
comment|// move all the children to the front of queue
for|for
control|(
name|Node
name|ch
range|:
name|children
control|)
block|{
comment|// and restore the state before walking each child
name|ctx
operator|.
name|currentRootOperator
operator|=
name|currentRoot
expr_stmt|;
name|ctx
operator|.
name|parentOfRoot
operator|=
name|parentOfRoot
expr_stmt|;
name|ctx
operator|.
name|preceedingWork
operator|=
name|preceedingWork
expr_stmt|;
name|ctx
operator|.
name|currentUnionOperators
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|currentUnionOperators
operator|.
name|addAll
argument_list|(
name|currentUnionOperators
argument_list|)
expr_stmt|;
name|walk
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
block|}
comment|// done with this operator
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

