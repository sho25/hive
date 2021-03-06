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

begin_class
specifier|public
class|class
name|ForwardWalker
extends|extends
name|DefaultGraphWalker
block|{
comment|/**    * Constructor.    *    * @param disp    * dispatcher to call for each op encountered    */
specifier|public
name|ForwardWalker
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
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|protected
name|boolean
name|allParentsDispatched
parameter_list|(
name|Node
name|nd
parameter_list|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|getParentOperators
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
for|for
control|(
name|Node
name|pNode
range|:
name|op
operator|.
name|getParentOperators
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
name|pNode
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|protected
name|void
name|addAllParents
parameter_list|(
name|Node
name|nd
parameter_list|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
name|toWalk
operator|.
name|removeAll
argument_list|(
name|op
operator|.
name|getParentOperators
argument_list|()
argument_list|)
expr_stmt|;
name|toWalk
operator|.
name|addAll
argument_list|(
literal|0
argument_list|,
name|op
operator|.
name|getParentOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * walk the current operator and its descendants.    *    * @param nd    * current operator in the graph    * @throws SemanticException    */
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
if|if
condition|(
name|opStack
operator|.
name|empty
argument_list|()
operator|||
name|nd
operator|!=
name|opStack
operator|.
name|peek
argument_list|()
condition|)
block|{
name|opStack
operator|.
name|push
argument_list|(
name|nd
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allParentsDispatched
argument_list|(
name|nd
argument_list|)
condition|)
block|{
comment|// all children are done or no need to walk the children
if|if
condition|(
operator|!
name|getDispatchedList
argument_list|()
operator|.
name|contains
argument_list|(
name|nd
argument_list|)
condition|)
block|{
name|toWalk
operator|.
name|addAll
argument_list|(
name|nd
operator|.
name|getChildren
argument_list|()
argument_list|)
expr_stmt|;
name|dispatch
argument_list|(
name|nd
argument_list|,
name|opStack
argument_list|)
expr_stmt|;
block|}
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// add children, self to the front of the queue in that order
name|toWalk
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|nd
argument_list|)
expr_stmt|;
name|addAllParents
argument_list|(
name|nd
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

