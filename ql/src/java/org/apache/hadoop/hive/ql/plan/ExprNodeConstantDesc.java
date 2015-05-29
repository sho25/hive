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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|builder
operator|.
name|HashCodeBuilder
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
name|serde
operator|.
name|serdeConstants
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ConstantObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|serde2
operator|.
name|typeinfo
operator|.
name|BaseCharTypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoUtils
import|;
end_import

begin_comment
comment|/**  * A constant expression.  */
end_comment

begin_class
specifier|public
class|class
name|ExprNodeConstantDesc
extends|extends
name|ExprNodeDesc
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
specifier|final
specifier|protected
specifier|transient
specifier|static
name|char
index|[]
name|hexArray
init|=
literal|"0123456789ABCDEF"
operator|.
name|toCharArray
argument_list|()
decl_stmt|;
specifier|private
name|Object
name|value
decl_stmt|;
comment|// If this constant was created while doing constant folding, foldedFromCol holds the name of
comment|// original column from which it was folded.
specifier|private
specifier|transient
name|String
name|foldedFromCol
decl_stmt|;
comment|// string representation of folding constant.
specifier|private
specifier|transient
name|String
name|foldedFromVal
decl_stmt|;
specifier|public
name|ExprNodeConstantDesc
name|setFoldedFromVal
parameter_list|(
name|String
name|foldedFromVal
parameter_list|)
block|{
name|this
operator|.
name|foldedFromVal
operator|=
name|foldedFromVal
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|String
name|getFoldedFromVal
parameter_list|()
block|{
return|return
name|foldedFromVal
return|;
block|}
specifier|public
name|String
name|getFoldedFromCol
parameter_list|()
block|{
return|return
name|foldedFromCol
return|;
block|}
specifier|public
name|void
name|setFoldedFromCol
parameter_list|(
name|String
name|foldedFromCol
parameter_list|)
block|{
name|this
operator|.
name|foldedFromCol
operator|=
name|foldedFromCol
expr_stmt|;
block|}
specifier|public
name|ExprNodeConstantDesc
parameter_list|()
block|{   }
specifier|public
name|ExprNodeConstantDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|ExprNodeConstantDesc
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfoFromJavaPrimitive
argument_list|(
name|value
operator|.
name|getClass
argument_list|()
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Object
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|ConstantObjectInspector
name|getWritableObjectInspector
parameter_list|()
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|getConstantObjectInspector
argument_list|(
name|TypeInfoUtils
operator|.
name|getStandardJavaObjectInspectorFromTypeInfo
argument_list|(
name|typeInfo
argument_list|)
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Const "
operator|+
name|typeInfo
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getExprString
parameter_list|()
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|"null"
return|;
block|}
if|if
condition|(
name|typeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
operator|||
name|typeInfo
operator|instanceof
name|BaseCharTypeInfo
condition|)
block|{
return|return
literal|"'"
operator|+
name|value
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|BINARY_TYPE_NAME
argument_list|)
condition|)
block|{
name|byte
index|[]
name|bytes
init|=
operator|(
name|byte
index|[]
operator|)
name|value
decl_stmt|;
name|char
index|[]
name|hexChars
init|=
operator|new
name|char
index|[
name|bytes
operator|.
name|length
operator|*
literal|2
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|bytes
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|int
name|v
init|=
name|bytes
index|[
name|j
index|]
operator|&
literal|0xFF
decl_stmt|;
name|hexChars
index|[
name|j
operator|*
literal|2
index|]
operator|=
name|hexArray
index|[
name|v
operator|>>>
literal|4
index|]
expr_stmt|;
name|hexChars
index|[
name|j
operator|*
literal|2
operator|+
literal|1
index|]
operator|=
name|hexArray
index|[
name|v
operator|&
literal|0x0F
index|]
expr_stmt|;
block|}
return|return
operator|new
name|String
argument_list|(
name|hexChars
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|value
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ExprNodeDesc
name|clone
parameter_list|()
block|{
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|typeInfo
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSame
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ExprNodeConstantDesc
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ExprNodeConstantDesc
name|dest
init|=
operator|(
name|ExprNodeConstantDesc
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|typeInfo
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|dest
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|value
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|getValue
argument_list|()
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
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|superHashCode
init|=
name|super
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|HashCodeBuilder
name|builder
init|=
operator|new
name|HashCodeBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|appendSuper
argument_list|(
name|superHashCode
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|toHashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

