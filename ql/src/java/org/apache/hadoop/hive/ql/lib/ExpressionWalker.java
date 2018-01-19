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
name|ASTNode
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
name|HiveParser
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
comment|/**    * We should bypass subquery since we have already processed and created logical plan    * (in genLogicalPlan) for subquery at this point.    * SubQueryExprProcessor will use generated plan and creates appropriate ExprNodeSubQueryDesc.    */
specifier|private
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
if|if
condition|(
name|parentNode
operator|instanceof
name|ASTNode
operator|&&
operator|(
operator|(
name|ASTNode
operator|)
name|parentNode
operator|)
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_SUBQUERY_EXPR
condition|)
block|{
name|ASTNode
name|parentOp
init|=
operator|(
name|ASTNode
operator|)
name|parentNode
decl_stmt|;
comment|//subquery either in WHERE<LHS> IN<SUBQUERY> form OR WHERE EXISTS<SUBQUERY> form
comment|//in first case LHS should not be bypassed
assert|assert
operator|(
name|parentOp
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
operator|||
name|parentOp
operator|.
name|getChildCount
argument_list|()
operator|==
literal|3
operator|)
assert|;
if|if
condition|(
name|parentOp
operator|.
name|getChildCount
argument_list|()
operator|==
literal|3
operator|&&
operator|(
name|ASTNode
operator|)
name|childNode
operator|==
name|parentOp
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
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
if|if
condition|(
name|shouldByPass
argument_list|(
name|childNode
argument_list|,
name|node
argument_list|)
condition|)
block|{
name|retMap
operator|.
name|put
argument_list|(
name|childNode
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|opStack
operator|.
name|push
argument_list|(
name|childNode
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
block|}
comment|// end while
block|}
block|}
end_class

end_unit

