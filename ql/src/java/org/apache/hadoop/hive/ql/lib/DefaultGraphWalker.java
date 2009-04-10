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
name|HashSet
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
comment|/**  * base class for operator graph walker  * this class takes list of starting ops and walks them one by one. it maintains list of walked  * operators (dispatchedList) and a list of operators that are discovered but not yet dispatched  */
end_comment

begin_class
specifier|public
class|class
name|DefaultGraphWalker
implements|implements
name|GraphWalker
block|{
specifier|protected
name|Stack
argument_list|<
name|Node
argument_list|>
name|opStack
decl_stmt|;
specifier|private
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
specifier|private
name|Set
argument_list|<
name|Node
argument_list|>
name|seenList
init|=
operator|new
name|HashSet
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|retMap
init|=
operator|new
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Dispatcher
name|dispatcher
decl_stmt|;
comment|/**    * Constructor    * @param disp dispatcher to call for each op encountered    */
specifier|public
name|DefaultGraphWalker
parameter_list|(
name|Dispatcher
name|disp
parameter_list|)
block|{
name|this
operator|.
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
block|}
comment|/**    * @return the toWalk    */
specifier|public
name|List
argument_list|<
name|Node
argument_list|>
name|getToWalk
parameter_list|()
block|{
return|return
name|toWalk
return|;
block|}
comment|/**    * @return the doneList    */
specifier|public
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
comment|/**    * Dispatch the current operator    * @param nd node being walked    * @param ndStack stack of nodes encountered    * @throws SemanticException    */
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
block|}
comment|/**    * starting point for walking    * @throws SemanticException    */
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
comment|/**    * walk the current operator and its descendants    * @param nd current operator in the graph    * @throws SemanticException    */
specifier|public
name|void
name|walk
parameter_list|(
name|Node
name|nd
parameter_list|)
throws|throws
name|SemanticException
block|{
name|opStack
operator|.
name|push
argument_list|(
name|nd
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|nd
operator|.
name|getChildren
argument_list|()
operator|==
literal|null
operator|)
operator|||
name|getDispatchedList
argument_list|()
operator|.
name|containsAll
argument_list|(
name|nd
operator|.
name|getChildren
argument_list|()
argument_list|)
condition|)
block|{
comment|// all children are done or no need to walk the children
if|if
condition|(
name|getDispatchedList
argument_list|()
operator|.
name|contains
argument_list|(
name|nd
argument_list|)
condition|)
comment|// sanity check
assert|assert
literal|false
assert|;
name|dispatch
argument_list|(
name|nd
argument_list|,
name|opStack
argument_list|)
expr_stmt|;
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// add children, self to the front of the queue in that order
name|getToWalk
argument_list|()
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|nd
argument_list|)
expr_stmt|;
name|getToWalk
argument_list|()
operator|.
name|removeAll
argument_list|(
name|nd
operator|.
name|getChildren
argument_list|()
argument_list|)
expr_stmt|;
name|getToWalk
argument_list|()
operator|.
name|addAll
argument_list|(
literal|0
argument_list|,
name|nd
operator|.
name|getChildren
argument_list|()
argument_list|)
expr_stmt|;
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

