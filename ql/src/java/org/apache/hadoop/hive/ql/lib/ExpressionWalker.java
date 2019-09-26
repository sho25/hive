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
name|java
operator|.
name|util
operator|.
name|ArrayDeque
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Deque
import|;
end_import

begin_comment
comment|/**  * class for traversing tree  * This class assumes that the given node represents TREE and not GRAPH  * i.e. there is only single path to reach a node  */
end_comment

begin_class
specifier|public
class|class
name|ExpressionWalker
extends|extends
name|DefaultGraphWalker
block|{
comment|/**    * Constructor.    *    * @param disp    * dispatcher to call for each op encountered    */
specifier|public
name|ExpressionWalker
parameter_list|(
name|Dispatcher
name|disp
parameter_list|)
block|{
name|super
argument_list|(
name|disp
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|NodeLabeled
block|{
specifier|final
specifier|private
name|Node
name|nd
decl_stmt|;
specifier|private
name|int
name|currChildIdx
decl_stmt|;
name|NodeLabeled
parameter_list|(
name|Node
name|nd
parameter_list|)
block|{
name|this
operator|.
name|nd
operator|=
name|nd
expr_stmt|;
name|this
operator|.
name|currChildIdx
operator|=
operator|-
literal|1
expr_stmt|;
block|}
specifier|public
name|void
name|incrementChildIdx
parameter_list|()
block|{
name|this
operator|.
name|currChildIdx
operator|++
expr_stmt|;
block|}
specifier|public
name|int
name|getCurrChildIdx
parameter_list|()
block|{
return|return
name|this
operator|.
name|currChildIdx
return|;
block|}
specifier|public
name|Node
name|getNd
parameter_list|()
block|{
return|return
name|this
operator|.
name|nd
return|;
block|}
block|}
specifier|protected
name|boolean
name|shouldByPass
parameter_list|(
name|Node
name|childNode
parameter_list|,
name|Node
name|parentNode
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
comment|/**    * walk the current operator and its descendants.    *    * @param nd    *          current operator in the tree    * @throws SemanticException    */
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
name|Deque
argument_list|<
name|NodeLabeled
argument_list|>
name|traversalStack
init|=
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
decl_stmt|;
name|traversalStack
operator|.
name|push
argument_list|(
operator|new
name|NodeLabeled
argument_list|(
name|nd
argument_list|)
argument_list|)
expr_stmt|;
name|opStack
operator|.
name|push
argument_list|(
name|nd
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|traversalStack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|NodeLabeled
name|currLabeledNode
init|=
name|traversalStack
operator|.
name|peek
argument_list|()
decl_stmt|;
name|Node
name|currNode
init|=
name|currLabeledNode
operator|.
name|getNd
argument_list|()
decl_stmt|;
name|int
name|currIdx
init|=
name|currLabeledNode
operator|.
name|getCurrChildIdx
argument_list|()
decl_stmt|;
if|if
condition|(
name|currNode
operator|.
name|getChildren
argument_list|()
operator|!=
literal|null
operator|&&
name|currNode
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
operator|>
name|currIdx
operator|+
literal|1
condition|)
block|{
name|Node
name|nextChild
init|=
name|currNode
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|currIdx
operator|+
literal|1
argument_list|)
decl_stmt|;
comment|//check if this node should be skipped and not dispatched
if|if
condition|(
name|shouldByPass
argument_list|(
name|nextChild
argument_list|,
name|currNode
argument_list|)
condition|)
block|{
name|retMap
operator|.
name|put
argument_list|(
name|nextChild
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|currLabeledNode
operator|.
name|incrementChildIdx
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|traversalStack
operator|.
name|push
argument_list|(
operator|new
name|NodeLabeled
argument_list|(
name|nextChild
argument_list|)
argument_list|)
expr_stmt|;
name|opStack
operator|.
name|push
argument_list|(
name|nextChild
argument_list|)
expr_stmt|;
name|currLabeledNode
operator|.
name|incrementChildIdx
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// dispatch the node
name|dispatch
argument_list|(
name|currNode
argument_list|,
name|opStack
argument_list|)
expr_stmt|;
name|opQueue
operator|.
name|add
argument_list|(
name|currNode
argument_list|)
expr_stmt|;
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
name|traversalStack
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

