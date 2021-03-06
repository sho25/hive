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
name|Arrays
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
name|commons
operator|.
name|collections
operator|.
name|CollectionUtils
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
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * This is a level-wise walker implementation which dispatches the node in the order  * that the node will only get dispatched after all the parents are dispatched.  *  * Each node will be accessed once while it could be dispatched multiple times.  * e.g., for a lineage generator with operator tree, 2 levels of current node's  * ancestors need to keep in the operator stack.  *                  FIL(2) FIL(4)  *                      |    |  *                    RS(3) RS(5)  *                       \  /  *                      JOIN(7)  * The join lineage needs to be called twice for JOIN(7) node with different operator  * ancestors.  */
end_comment

begin_class
specifier|public
class|class
name|LevelOrderWalker
extends|extends
name|DefaultGraphWalker
block|{
comment|// Only specified nodes of these types will be walked.
comment|// Empty set means all the nodes will be walked.
specifier|private
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
argument_list|>
name|nodeTypes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|// How many levels of ancestors to keep in the stack during dispatching
specifier|private
specifier|final
name|int
name|numLevels
decl_stmt|;
comment|/**    * Constructor with keeping all the ancestors in the operator stack during    * dispatching.    *    * @param disp Dispatcher to call for each op encountered    */
specifier|public
name|LevelOrderWalker
parameter_list|(
name|SemanticDispatcher
name|disp
parameter_list|)
block|{
name|super
argument_list|(
name|disp
argument_list|)
expr_stmt|;
name|this
operator|.
name|numLevels
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
comment|/**    * Constructor with specified number of ancestor levels to keep in the    * operator stack during dispatching.    *    * @param disp Dispatcher to call for each op encountered    * @param numLevels Number of ancestor levels    */
specifier|public
name|LevelOrderWalker
parameter_list|(
name|SemanticDispatcher
name|disp
parameter_list|,
name|int
name|numLevels
parameter_list|)
block|{
name|super
argument_list|(
name|disp
argument_list|)
expr_stmt|;
name|this
operator|.
name|numLevels
operator|=
name|numLevels
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|setNodeTypes
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
modifier|...
name|nodeTypes
parameter_list|)
block|{
name|this
operator|.
name|nodeTypes
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|nodeTypes
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * starting point for walking.    *    * @throws SemanticException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
comment|// Starting from the startNodes, add the children whose parents have been
comment|// included in the list.
name|Set
argument_list|<
name|Node
argument_list|>
name|addedNodes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|startNodes
argument_list|)
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|index
operator|<
name|toWalk
operator|.
name|size
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|children
init|=
name|toWalk
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|children
argument_list|)
condition|)
block|{
for|for
control|(
name|Node
name|child
range|:
name|children
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|childOP
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|child
decl_stmt|;
if|if
condition|(
operator|!
name|addedNodes
operator|.
name|contains
argument_list|(
name|child
argument_list|)
operator|&&
operator|(
name|childOP
operator|.
name|getParentOperators
argument_list|()
operator|==
literal|null
operator|||
name|addedNodes
operator|.
name|containsAll
argument_list|(
name|childOP
operator|.
name|getParentOperators
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|toWalk
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
name|addedNodes
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
block|}
block|}
operator|++
name|index
expr_stmt|;
block|}
for|for
control|(
name|Node
name|nd
range|:
name|toWalk
control|)
block|{
if|if
condition|(
operator|!
name|nodeTypes
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|nodeTypes
operator|.
name|contains
argument_list|(
name|nd
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|opStack
operator|.
name|clear
argument_list|()
expr_stmt|;
name|opStack
operator|.
name|push
argument_list|(
name|nd
argument_list|)
expr_stmt|;
name|walk
argument_list|(
name|nd
argument_list|,
literal|0
argument_list|,
name|opStack
argument_list|)
expr_stmt|;
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
block|}
comment|/**    * Enumerate numLevels of ancestors by putting them in the stack and dispatch    * the current node.    *    * @param nd current operator in the ancestor tree    * @param level how many level of ancestors included in the stack    * @param stack operator stack    * @throws SemanticException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|void
name|walk
parameter_list|(
name|Node
name|nd
parameter_list|,
name|int
name|level
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parents
init|=
operator|(
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|nd
operator|)
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|level
operator|>=
name|numLevels
operator|||
name|CollectionUtils
operator|.
name|isEmpty
argument_list|(
name|parents
argument_list|)
condition|)
block|{
name|dispatch
argument_list|(
name|stack
operator|.
name|peek
argument_list|()
argument_list|,
name|stack
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|Node
name|parent
range|:
name|parents
control|)
block|{
name|stack
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|walk
argument_list|(
name|parent
argument_list|,
name|level
operator|+
literal|1
argument_list|,
name|stack
argument_list|)
expr_stmt|;
name|stack
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

