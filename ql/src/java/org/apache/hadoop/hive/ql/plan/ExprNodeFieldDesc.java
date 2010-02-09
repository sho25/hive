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
name|List
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
name|Utilities
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

begin_comment
comment|/**  * ExprNodeFieldDesc.  *  */
end_comment

begin_class
specifier|public
class|class
name|ExprNodeFieldDesc
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
name|ExprNodeDesc
name|desc
decl_stmt|;
name|String
name|fieldName
decl_stmt|;
comment|// Used to support a.b where a is a list of struct that contains a field
comment|// called b.
comment|// a.b will return an array that contains field b of all elements of array a.
name|Boolean
name|isList
decl_stmt|;
specifier|public
name|ExprNodeFieldDesc
parameter_list|()
block|{   }
specifier|public
name|ExprNodeFieldDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|ExprNodeDesc
name|desc
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|Boolean
name|isList
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|isList
operator|=
name|isList
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getChildren
parameter_list|()
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|desc
argument_list|)
expr_stmt|;
return|return
name|children
return|;
block|}
specifier|public
name|ExprNodeDesc
name|getDesc
parameter_list|()
block|{
return|return
name|desc
return|;
block|}
specifier|public
name|void
name|setDesc
parameter_list|(
name|ExprNodeDesc
name|desc
parameter_list|)
block|{
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|fieldName
return|;
block|}
specifier|public
name|void
name|setFieldName
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
block|}
specifier|public
name|Boolean
name|getIsList
parameter_list|()
block|{
return|return
name|isList
return|;
block|}
specifier|public
name|void
name|setIsList
parameter_list|(
name|Boolean
name|isList
parameter_list|)
block|{
name|this
operator|.
name|isList
operator|=
name|isList
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|desc
operator|.
name|toString
argument_list|()
operator|+
literal|"."
operator|+
name|fieldName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"expr"
argument_list|)
annotation|@
name|Override
specifier|public
name|String
name|getExprString
parameter_list|()
block|{
return|return
name|desc
operator|.
name|getExprString
argument_list|()
operator|+
literal|"."
operator|+
name|fieldName
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getCols
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|colList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|desc
operator|!=
literal|null
condition|)
block|{
name|colList
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|colList
argument_list|,
name|desc
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|colList
return|;
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
name|ExprNodeFieldDesc
argument_list|(
name|typeInfo
argument_list|,
name|desc
argument_list|,
name|fieldName
argument_list|,
name|isList
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
name|ExprNodeFieldDesc
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ExprNodeFieldDesc
name|dest
init|=
operator|(
name|ExprNodeFieldDesc
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
operator|!
name|fieldName
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|getFieldName
argument_list|()
argument_list|)
operator|||
operator|!
name|isList
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|getIsList
argument_list|()
argument_list|)
operator|||
operator|!
name|desc
operator|.
name|isSame
argument_list|(
name|dest
operator|.
name|getDesc
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
block|}
end_class

end_unit

