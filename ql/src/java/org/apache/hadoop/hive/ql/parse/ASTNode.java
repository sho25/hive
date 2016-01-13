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
name|parse
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
name|ArrayDeque
import|;
end_import

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
name|Deque
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
name|antlr
operator|.
name|runtime
operator|.
name|Token
import|;
end_import

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|CommonTree
import|;
end_import

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|Tree
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
name|lang3
operator|.
name|StringUtils
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

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|ASTNode
extends|extends
name|CommonTree
implements|implements
name|Node
implements|,
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|transient
name|StringBuilder
name|astStr
decl_stmt|;
specifier|private
specifier|transient
name|ASTNodeOrigin
name|origin
decl_stmt|;
specifier|private
specifier|transient
name|int
name|startIndx
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|transient
name|int
name|endIndx
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|transient
name|ASTNode
name|rootNode
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|isValidASTStr
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|visited
init|=
literal|false
decl_stmt|;
specifier|public
name|ASTNode
parameter_list|()
block|{   }
comment|/**    * Constructor.    *    * @param t    *          Token for the CommonTree Node    */
specifier|public
name|ASTNode
parameter_list|(
name|Token
name|t
parameter_list|)
block|{
name|super
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ASTNode
parameter_list|(
name|ASTNode
name|node
parameter_list|)
block|{
name|super
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|this
operator|.
name|origin
operator|=
name|node
operator|.
name|origin
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Tree
name|dupNode
parameter_list|()
block|{
return|return
operator|new
name|ASTNode
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see org.apache.hadoop.hive.ql.lib.Node#getChildren()    */
annotation|@
name|Override
specifier|public
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|getChildren
parameter_list|()
block|{
if|if
condition|(
name|super
operator|.
name|getChildCount
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|ret_vec
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|super
operator|.
name|getChildCount
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|ret_vec
operator|.
name|add
argument_list|(
operator|(
name|Node
operator|)
name|super
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret_vec
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see org.apache.hadoop.hive.ql.lib.Node#getName()    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
operator|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|super
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|)
operator|)
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * For every node in this subtree, make sure it's start/stop token's    * are set.  Walk depth first, visit bottom up.  Only updates nodes    * with at least one token index< 0.    *    * In contrast to the method in the parent class, this method is    * iterative.    */
annotation|@
name|Override
specifier|public
name|void
name|setUnknownTokenBoundaries
parameter_list|()
block|{
name|Deque
argument_list|<
name|ASTNode
argument_list|>
name|stack1
init|=
operator|new
name|ArrayDeque
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
decl_stmt|;
name|Deque
argument_list|<
name|ASTNode
argument_list|>
name|stack2
init|=
operator|new
name|ArrayDeque
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
decl_stmt|;
name|stack1
operator|.
name|push
argument_list|(
name|this
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|stack1
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ASTNode
name|next
init|=
name|stack1
operator|.
name|pop
argument_list|()
decl_stmt|;
name|stack2
operator|.
name|push
argument_list|(
name|next
argument_list|)
expr_stmt|;
if|if
condition|(
name|next
operator|.
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|next
operator|.
name|children
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|stack1
operator|.
name|push
argument_list|(
operator|(
name|ASTNode
operator|)
name|next
operator|.
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
while|while
condition|(
operator|!
name|stack2
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ASTNode
name|next
init|=
name|stack2
operator|.
name|pop
argument_list|()
decl_stmt|;
if|if
condition|(
name|next
operator|.
name|children
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|next
operator|.
name|startIndex
operator|<
literal|0
operator|||
name|next
operator|.
name|stopIndex
operator|<
literal|0
condition|)
block|{
name|next
operator|.
name|startIndex
operator|=
name|next
operator|.
name|stopIndex
operator|=
name|next
operator|.
name|token
operator|.
name|getTokenIndex
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|next
operator|.
name|startIndex
operator|>=
literal|0
operator|&&
name|next
operator|.
name|stopIndex
operator|>=
literal|0
condition|)
block|{
continue|continue;
block|}
elseif|else
if|if
condition|(
name|next
operator|.
name|children
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|ASTNode
name|firstChild
init|=
operator|(
name|ASTNode
operator|)
name|next
operator|.
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ASTNode
name|lastChild
init|=
operator|(
name|ASTNode
operator|)
name|next
operator|.
name|children
operator|.
name|get
argument_list|(
name|next
operator|.
name|children
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
name|next
operator|.
name|startIndex
operator|=
name|firstChild
operator|.
name|getTokenStartIndex
argument_list|()
expr_stmt|;
name|next
operator|.
name|stopIndex
operator|=
name|lastChild
operator|.
name|getTokenStopIndex
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @return information about the object from which this ASTNode originated, or    *         null if this ASTNode was not expanded from an object reference    */
specifier|public
name|ASTNodeOrigin
name|getOrigin
parameter_list|()
block|{
return|return
name|origin
return|;
block|}
comment|/**    * Tag this ASTNode with information about the object from which this node    * originated.    */
specifier|public
name|void
name|setOrigin
parameter_list|(
name|ASTNodeOrigin
name|origin
parameter_list|)
block|{
name|this
operator|.
name|origin
operator|=
name|origin
expr_stmt|;
block|}
specifier|public
name|String
name|dump
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"\n"
argument_list|)
decl_stmt|;
name|dump
argument_list|(
name|sb
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|StringBuilder
name|dump
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|Deque
argument_list|<
name|ASTNode
argument_list|>
name|stack
init|=
operator|new
name|ArrayDeque
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
decl_stmt|;
name|stack
operator|.
name|push
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|int
name|tabLength
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|stack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ASTNode
name|next
init|=
name|stack
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|next
operator|.
name|visited
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|repeat
argument_list|(
literal|" "
argument_list|,
name|tabLength
operator|*
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|next
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
if|if
condition|(
name|next
operator|.
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|next
operator|.
name|children
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|stack
operator|.
name|push
argument_list|(
operator|(
name|ASTNode
operator|)
name|next
operator|.
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|tabLength
operator|++
expr_stmt|;
name|next
operator|.
name|visited
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|tabLength
operator|--
expr_stmt|;
name|next
operator|.
name|visited
operator|=
literal|false
expr_stmt|;
name|stack
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|sb
return|;
block|}
specifier|private
name|void
name|getRootNodeWithValidASTStr
parameter_list|()
block|{
if|if
condition|(
name|rootNode
operator|!=
literal|null
operator|&&
name|rootNode
operator|.
name|parent
operator|==
literal|null
operator|&&
name|rootNode
operator|.
name|hasValidMemoizedString
argument_list|()
condition|)
block|{
return|return;
block|}
name|ASTNode
name|retNode
init|=
name|this
decl_stmt|;
while|while
condition|(
name|retNode
operator|.
name|parent
operator|!=
literal|null
condition|)
block|{
name|retNode
operator|=
operator|(
name|ASTNode
operator|)
name|retNode
operator|.
name|parent
expr_stmt|;
block|}
name|rootNode
operator|=
name|retNode
expr_stmt|;
if|if
condition|(
operator|!
name|rootNode
operator|.
name|isValidASTStr
condition|)
block|{
name|rootNode
operator|.
name|astStr
operator|=
operator|new
name|StringBuilder
argument_list|()
expr_stmt|;
name|rootNode
operator|.
name|toStringTree
argument_list|(
name|rootNode
argument_list|)
expr_stmt|;
name|rootNode
operator|.
name|isValidASTStr
operator|=
literal|true
expr_stmt|;
block|}
return|return;
block|}
specifier|private
name|boolean
name|hasValidMemoizedString
parameter_list|()
block|{
return|return
name|isValidASTStr
operator|&&
name|astStr
operator|!=
literal|null
return|;
block|}
specifier|private
name|void
name|resetRootInformation
parameter_list|()
block|{
comment|// Reset the previously stored rootNode string
if|if
condition|(
name|rootNode
operator|!=
literal|null
condition|)
block|{
name|rootNode
operator|.
name|astStr
operator|=
literal|null
expr_stmt|;
name|rootNode
operator|.
name|isValidASTStr
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|private
name|int
name|getMemoizedStringLen
parameter_list|()
block|{
return|return
name|astStr
operator|==
literal|null
condition|?
literal|0
else|:
name|astStr
operator|.
name|length
argument_list|()
return|;
block|}
specifier|private
name|String
name|getMemoizedSubString
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
return|return
operator|(
name|astStr
operator|==
literal|null
operator|||
name|start
argument_list|<
literal|0
operator|||
name|end
argument_list|>
name|astStr
operator|.
name|length
argument_list|()
operator|||
name|start
operator|>=
name|end
operator|)
condition|?
literal|null
else|:
name|astStr
operator|.
name|subSequence
argument_list|(
name|start
argument_list|,
name|end
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|addtoMemoizedString
parameter_list|(
name|String
name|string
parameter_list|)
block|{
if|if
condition|(
name|astStr
operator|==
literal|null
condition|)
block|{
name|astStr
operator|=
operator|new
name|StringBuilder
argument_list|()
expr_stmt|;
block|}
name|astStr
operator|.
name|append
argument_list|(
name|string
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setParent
parameter_list|(
name|Tree
name|t
parameter_list|)
block|{
name|super
operator|.
name|setParent
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|resetRootInformation
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addChild
parameter_list|(
name|Tree
name|t
parameter_list|)
block|{
name|super
operator|.
name|addChild
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|resetRootInformation
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addChildren
parameter_list|(
name|List
name|kids
parameter_list|)
block|{
name|super
operator|.
name|addChildren
argument_list|(
name|kids
argument_list|)
expr_stmt|;
name|resetRootInformation
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setChild
parameter_list|(
name|int
name|i
parameter_list|,
name|Tree
name|t
parameter_list|)
block|{
name|super
operator|.
name|setChild
argument_list|(
name|i
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|resetRootInformation
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|insertChild
parameter_list|(
name|int
name|i
parameter_list|,
name|Object
name|t
parameter_list|)
block|{
name|super
operator|.
name|insertChild
argument_list|(
name|i
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|resetRootInformation
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|deleteChild
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|Object
name|ret
init|=
name|super
operator|.
name|deleteChild
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|resetRootInformation
argument_list|()
expr_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|replaceChildren
parameter_list|(
name|int
name|startChildIndex
parameter_list|,
name|int
name|stopChildIndex
parameter_list|,
name|Object
name|t
parameter_list|)
block|{
name|super
operator|.
name|replaceChildren
argument_list|(
name|startChildIndex
argument_list|,
name|stopChildIndex
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|resetRootInformation
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toStringTree
parameter_list|()
block|{
comment|// The root might have changed because of tree modifications.
comment|// Compute the new root for this tree and set the astStr.
name|getRootNodeWithValidASTStr
argument_list|()
expr_stmt|;
comment|// If rootNotModified is false, then startIndx and endIndx will be stale.
if|if
condition|(
name|startIndx
operator|>=
literal|0
operator|&&
name|endIndx
operator|<=
name|rootNode
operator|.
name|getMemoizedStringLen
argument_list|()
condition|)
block|{
return|return
name|rootNode
operator|.
name|getMemoizedSubString
argument_list|(
name|startIndx
argument_list|,
name|endIndx
argument_list|)
return|;
block|}
return|return
name|toStringTree
argument_list|(
name|rootNode
argument_list|)
return|;
block|}
specifier|private
name|String
name|toStringTree
parameter_list|(
name|ASTNode
name|rootNode
parameter_list|)
block|{
name|Deque
argument_list|<
name|ASTNode
argument_list|>
name|stack
init|=
operator|new
name|ArrayDeque
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
decl_stmt|;
name|stack
operator|.
name|push
argument_list|(
name|this
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|stack
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ASTNode
name|next
init|=
name|stack
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|next
operator|.
name|visited
condition|)
block|{
if|if
condition|(
name|next
operator|.
name|parent
operator|!=
literal|null
operator|&&
name|next
operator|.
name|parent
operator|.
name|getChildCount
argument_list|()
operator|>
literal|1
operator|&&
name|next
operator|!=
name|next
operator|.
name|parent
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
condition|)
block|{
name|rootNode
operator|.
name|addtoMemoizedString
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|next
operator|.
name|rootNode
operator|=
name|rootNode
expr_stmt|;
name|next
operator|.
name|startIndx
operator|=
name|rootNode
operator|.
name|getMemoizedStringLen
argument_list|()
expr_stmt|;
comment|// Leaf
if|if
condition|(
name|next
operator|.
name|children
operator|==
literal|null
operator|||
name|next
operator|.
name|children
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|String
name|str
init|=
name|next
operator|.
name|toString
argument_list|()
decl_stmt|;
name|rootNode
operator|.
name|addtoMemoizedString
argument_list|(
name|next
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|StringLiteral
condition|?
name|str
operator|.
name|toLowerCase
argument_list|()
else|:
name|str
argument_list|)
expr_stmt|;
name|next
operator|.
name|endIndx
operator|=
name|rootNode
operator|.
name|getMemoizedStringLen
argument_list|()
expr_stmt|;
name|stack
operator|.
name|pop
argument_list|()
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
operator|!
name|next
operator|.
name|isNil
argument_list|()
condition|)
block|{
name|rootNode
operator|.
name|addtoMemoizedString
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
name|String
name|str
init|=
name|next
operator|.
name|toString
argument_list|()
decl_stmt|;
name|rootNode
operator|.
name|addtoMemoizedString
argument_list|(
operator|(
name|next
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|StringLiteral
operator|||
literal|null
operator|==
name|str
operator|)
condition|?
name|str
else|:
name|str
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|rootNode
operator|.
name|addtoMemoizedString
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|next
operator|.
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|next
operator|.
name|children
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|stack
operator|.
name|push
argument_list|(
operator|(
name|ASTNode
operator|)
name|next
operator|.
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|next
operator|.
name|visited
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|next
operator|.
name|isNil
argument_list|()
condition|)
block|{
name|rootNode
operator|.
name|addtoMemoizedString
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
name|next
operator|.
name|endIndx
operator|=
name|rootNode
operator|.
name|getMemoizedStringLen
argument_list|()
expr_stmt|;
name|next
operator|.
name|visited
operator|=
literal|false
expr_stmt|;
name|stack
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|rootNode
operator|.
name|getMemoizedSubString
argument_list|(
name|startIndx
argument_list|,
name|endIndx
argument_list|)
return|;
block|}
block|}
end_class

end_unit

