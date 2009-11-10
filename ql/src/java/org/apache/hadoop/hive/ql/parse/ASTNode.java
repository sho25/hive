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
name|util
operator|.
name|Vector
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
name|Token
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
comment|/**  * @author athusoo  *  */
end_comment

begin_class
specifier|public
class|class
name|ASTNode
extends|extends
name|CommonTree
implements|implements
name|Node
block|{
specifier|public
name|ASTNode
parameter_list|()
block|{     }
comment|/**    * Constructor    * @param t Token for the CommonTree Node    */
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
comment|/* (non-Javadoc)    * @see org.apache.hadoop.hive.ql.lib.Node#getChildren()    */
specifier|public
name|Vector
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
name|Vector
argument_list|<
name|Node
argument_list|>
name|ret_vec
init|=
operator|new
name|Vector
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
comment|/* (non-Javadoc)    * @see org.apache.hadoop.hive.ql.lib.Node#getName()    */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
operator|(
operator|new
name|Integer
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
specifier|public
name|String
name|dump
parameter_list|()
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'('
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Vector
argument_list|<
name|Node
argument_list|>
name|children
init|=
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Node
name|node
range|:
name|getChildren
argument_list|()
control|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|ASTNode
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|ASTNode
operator|)
name|node
operator|)
operator|.
name|dump
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"NON-ASTNODE!!"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

