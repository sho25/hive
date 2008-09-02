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
name|plan
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
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * The reason that we have to store UDFClass as well as UDFMethod is because  * UDFMethod might be declared in a parent class of UDFClass. As a result,  * UDFMethod.getDeclaringClass() may not work.  */
end_comment

begin_class
specifier|public
class|class
name|exprNodeFuncDesc
extends|extends
name|exprNodeDesc
implements|implements
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
name|Class
name|UDFClass
decl_stmt|;
specifier|private
name|Method
name|UDFMethod
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|children
decl_stmt|;
specifier|public
name|exprNodeFuncDesc
parameter_list|()
block|{}
specifier|public
name|exprNodeFuncDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|Class
name|UDFClass
parameter_list|,
name|Method
name|UDFMethod
parameter_list|,
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|children
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|UDFClass
operator|!=
literal|null
operator|)
assert|;
name|this
operator|.
name|UDFClass
operator|=
name|UDFClass
expr_stmt|;
name|this
operator|.
name|UDFMethod
operator|=
name|UDFMethod
expr_stmt|;
name|this
operator|.
name|children
operator|=
name|children
expr_stmt|;
block|}
specifier|public
name|Class
name|getUDFClass
parameter_list|()
block|{
return|return
name|UDFClass
return|;
block|}
specifier|public
name|void
name|setUDFClass
parameter_list|(
name|Class
name|UDFClass
parameter_list|)
block|{
name|this
operator|.
name|UDFClass
operator|=
name|UDFClass
expr_stmt|;
block|}
specifier|public
name|Method
name|getUDFMethod
parameter_list|()
block|{
return|return
name|this
operator|.
name|UDFMethod
return|;
block|}
specifier|public
name|void
name|setUDFMethod
parameter_list|(
name|Method
name|method
parameter_list|)
block|{
name|this
operator|.
name|UDFMethod
operator|=
name|method
expr_stmt|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|getChildren
parameter_list|()
block|{
return|return
name|this
operator|.
name|children
return|;
block|}
specifier|public
name|void
name|setChildren
parameter_list|(
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|children
parameter_list|)
block|{
name|this
operator|.
name|children
operator|=
name|children
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|UDFClass
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|UDFMethod
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|children
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|")"
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

