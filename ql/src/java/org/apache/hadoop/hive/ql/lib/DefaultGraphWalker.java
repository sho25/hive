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
name|lib
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
name|IdentityHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|Queue
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
name|parse
operator|.
name|SemanticException
import|;
end_import

begin_comment
comment|/**  * base class for operator graph walker this class takes list of starting ops  * and walks them one by one. it maintains list of walked operators  * (dispatchedList) and a list of operators that are discovered but not yet  * dispatched  */
end_comment

begin_class
specifier|public
class|class
name|DefaultGraphWalker
implements|implements
name|SemanticGraphWalker
block|{
comment|/**    * opStack keeps the nodes that have been visited, but have not been    * dispatched yet    */
specifier|protected
specifier|final
name|Stack
argument_list|<
name|Node
argument_list|>
name|opStack
decl_stmt|;
comment|/**    * opQueue keeps the nodes in the order that the were dispatched.    * Then it is used to go through the processed nodes and store    * the results that the dispatcher has produced (if any)    */
specifier|protected
specifier|final
name|Queue
argument_list|<
name|Node
argument_list|>
name|opQueue
decl_stmt|;
comment|/**    * toWalk stores the starting nodes for the graph that needs to be    * traversed    */
specifier|protected
specifier|final
name|List
argument_list|<
name|Node
argument_list|>
name|toWalk
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|IdentityHashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|retMap
init|=
operator|new
name|IdentityHashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|SemanticDispatcher
name|dispatcher
decl_stmt|;
comment|/**    * Constructor.    *    * @param disp    *          dispatcher to call for each op encountered    */
specifier|public
name|DefaultGraphWalker
parameter_list|(
name|SemanticDispatcher
name|disp
parameter_list|)
block|{
name|dispatcher
operator|=
name|disp
expr_stmt|;
name|opStack
operator|=
operator|new
name|Stack
argument_list|<
name|Node
argument_list|>
argument_list|()
expr_stmt|;
name|opQueue
operator|=
operator|new
name|LinkedList
argument_list|<
name|Node
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the doneList    */
specifier|protected
name|Set
argument_list|<
name|Node
argument_list|>
name|getDispatchedList
parameter_list|()
block|{
return|return
name|retMap
operator|.
name|keySet
argument_list|()
return|;
block|}
comment|/**    * Dispatch the current operator.    *    * @param nd    *          node being walked    * @param ndStack    *          stack of nodes encountered    * @throws SemanticException    */
specifier|public
name|void
name|dispatch
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|ndStack
parameter_list|)
throws|throws
name|SemanticException
block|{
name|dispatchAndReturn
argument_list|(
name|nd
argument_list|,
name|ndStack
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns dispatch result    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|dispatchAndReturn
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|ndStack
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Object
index|[]
name|nodeOutputs
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|nd
operator|.
name|getChildren
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nodeOutputs
operator|=
operator|new
name|Object
index|[
name|nd
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Node
name|child
range|:
name|nd
operator|.
name|getChildren
argument_list|()
control|)
block|{
name|nodeOutputs
index|[
name|i
operator|++
index|]
operator|=
name|retMap
operator|.
name|get
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
block|}
name|Object
name|retVal
init|=
name|dispatcher
operator|.
name|dispatch
argument_list|(
name|nd
argument_list|,
name|ndStack
argument_list|,
name|nodeOutputs
argument_list|)
decl_stmt|;
name|retMap
operator|.
name|put
argument_list|(
name|nd
argument_list|,
name|retVal
argument_list|)
expr_stmt|;
return|return
operator|(
name|T
operator|)
name|retVal
return|;
block|}
comment|/**    * starting point for walking.    *    * @throws SemanticException    */
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
name|walk
argument_list|(
name|nd
argument_list|)
expr_stmt|;
comment|// Some walkers extending DefaultGraphWalker e.g. ForwardWalker
comment|// do not use opQueue and rely uniquely in the toWalk structure,
comment|// thus we store the results produced by the dispatcher here
comment|// TODO: rewriting the logic of those walkers to use opQueue
if|if
condition|(
name|nodeOutput
operator|!=
literal|null
operator|&&
name|getDispatchedList
argument_list|()
operator|.
name|contains
argument_list|(
name|nd
argument_list|)
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
comment|// Store the results produced by the dispatcher
while|while
condition|(
operator|!
name|opQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Node
name|node
init|=
name|opQueue
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|nodeOutput
operator|!=
literal|null
operator|&&
name|getDispatchedList
argument_list|()
operator|.
name|contains
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|nodeOutput
operator|.
name|put
argument_list|(
name|node
argument_list|,
name|retMap
operator|.
name|get
argument_list|(
name|node
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * walk the current operator and its descendants.    *    * @param nd    *          current operator in the graph    * @throws SemanticException    */
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
comment|// Push the node in the stack
name|opStack
operator|.
name|push
argument_list|(
name|nd
argument_list|)
expr_stmt|;
comment|// While there are still nodes to dispatch...
while|while
condition|(
operator|!
name|opStack
operator|.
name|empty
argument_list|()
condition|)
block|{
name|Node
name|node
init|=
name|opStack
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|node
operator|.
name|getChildren
argument_list|()
operator|==
literal|null
operator|||
name|getDispatchedList
argument_list|()
operator|.
name|containsAll
argument_list|(
name|node
operator|.
name|getChildren
argument_list|()
argument_list|)
condition|)
block|{
comment|// Dispatch current node
if|if
condition|(
operator|!
name|getDispatchedList
argument_list|()
operator|.
name|contains
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|dispatch
argument_list|(
name|node
argument_list|,
name|opStack
argument_list|)
expr_stmt|;
name|opQueue
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
continue|continue;
block|}
comment|// Add a single child and restart the loop
for|for
control|(
name|Node
name|childNode
range|:
name|node
operator|.
name|getChildren
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|getDispatchedList
argument_list|()
operator|.
name|contains
argument_list|(
name|childNode
argument_list|)
condition|)
block|{
name|opStack
operator|.
name|push
argument_list|(
name|childNode
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
comment|// end while
block|}
block|}
end_class

end_unit

