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
name|calcite
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_comment
comment|/**  * This encapsulate subquery expression which consists of  *  Relnode for subquery.  *  type (IN, EXISTS )  *  LHS operand  */
end_comment

begin_class
specifier|public
class|class
name|ExprNodeSubQueryDesc
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
specifier|public
specifier|static
enum|enum
name|SubqueryType
block|{
name|IN
block|,
name|EXISTS
block|,
name|SCALAR
block|}
empty_stmt|;
comment|/**    * RexNode corresponding to subquery.    */
specifier|private
name|RelNode
name|rexSubQuery
decl_stmt|;
specifier|private
name|ExprNodeDesc
name|subQueryLhs
decl_stmt|;
specifier|private
name|SubqueryType
name|type
decl_stmt|;
specifier|public
name|ExprNodeSubQueryDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|RelNode
name|subQuery
parameter_list|,
name|SubqueryType
name|type
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|rexSubQuery
operator|=
name|subQuery
expr_stmt|;
name|this
operator|.
name|subQueryLhs
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|ExprNodeSubQueryDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|RelNode
name|subQuery
parameter_list|,
name|SubqueryType
name|type
parameter_list|,
name|ExprNodeDesc
name|lhs
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|rexSubQuery
operator|=
name|subQuery
expr_stmt|;
name|this
operator|.
name|subQueryLhs
operator|=
name|lhs
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|SubqueryType
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|ExprNodeDesc
name|getSubQueryLhs
parameter_list|()
block|{
return|return
name|subQueryLhs
return|;
block|}
specifier|public
name|RelNode
name|getRexSubQuery
parameter_list|()
block|{
return|return
name|rexSubQuery
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
name|ExprNodeSubQueryDesc
argument_list|(
name|typeInfo
argument_list|,
name|rexSubQuery
argument_list|,
name|type
argument_list|,
name|subQueryLhs
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
name|ExprNodeSubQueryDesc
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ExprNodeSubQueryDesc
name|dest
init|=
operator|(
name|ExprNodeSubQueryDesc
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|subQueryLhs
operator|!=
literal|null
operator|&&
name|dest
operator|.
name|getSubQueryLhs
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|subQueryLhs
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|getSubQueryLhs
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
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
name|rexSubQuery
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|getRexSubQuery
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
name|type
operator|!=
name|dest
operator|.
name|getType
argument_list|()
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

