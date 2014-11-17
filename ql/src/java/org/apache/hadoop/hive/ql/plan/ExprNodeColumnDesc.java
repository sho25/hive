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
name|ql
operator|.
name|exec
operator|.
name|ColumnInfo
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

begin_comment
comment|/**  * ExprNodeColumnDesc.  *  */
end_comment

begin_class
specifier|public
class|class
name|ExprNodeColumnDesc
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
comment|/**    * The column name.    */
specifier|private
name|String
name|column
decl_stmt|;
comment|/**    * The alias of the table.    */
specifier|private
name|String
name|tabAlias
decl_stmt|;
comment|/**    * Is the column a partitioned column.    */
specifier|private
name|boolean
name|isPartitionColOrVirtualCol
decl_stmt|;
comment|/**    * Is the column a skewed column    */
specifier|private
name|boolean
name|isSkewedCol
decl_stmt|;
specifier|public
name|ExprNodeColumnDesc
parameter_list|()
block|{   }
specifier|public
name|ExprNodeColumnDesc
parameter_list|(
name|ColumnInfo
name|ci
parameter_list|)
block|{
name|this
argument_list|(
name|ci
operator|.
name|getType
argument_list|()
argument_list|,
name|ci
operator|.
name|getInternalName
argument_list|()
argument_list|,
name|ci
operator|.
name|getTabAlias
argument_list|()
argument_list|,
name|ci
operator|.
name|getIsVirtualCol
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ExprNodeColumnDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|String
name|column
parameter_list|,
name|String
name|tabAlias
parameter_list|,
name|boolean
name|isPartitionColOrVirtualCol
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|tabAlias
operator|=
name|tabAlias
expr_stmt|;
name|this
operator|.
name|isPartitionColOrVirtualCol
operator|=
name|isPartitionColOrVirtualCol
expr_stmt|;
block|}
specifier|public
name|ExprNodeColumnDesc
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|,
name|String
name|column
parameter_list|,
name|String
name|tabAlias
parameter_list|,
name|boolean
name|isPartitionColOrVirtualCol
parameter_list|)
block|{
name|super
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfoFromJavaPrimitive
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|tabAlias
operator|=
name|tabAlias
expr_stmt|;
name|this
operator|.
name|isPartitionColOrVirtualCol
operator|=
name|isPartitionColOrVirtualCol
expr_stmt|;
block|}
specifier|public
name|ExprNodeColumnDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|String
name|column
parameter_list|,
name|String
name|tabAlias
parameter_list|,
name|boolean
name|isPartitionColOrVirtualCol
parameter_list|,
name|boolean
name|isSkewedCol
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|tabAlias
operator|=
name|tabAlias
expr_stmt|;
name|this
operator|.
name|isPartitionColOrVirtualCol
operator|=
name|isPartitionColOrVirtualCol
expr_stmt|;
name|this
operator|.
name|isSkewedCol
operator|=
name|isSkewedCol
expr_stmt|;
block|}
specifier|public
name|String
name|getColumn
parameter_list|()
block|{
return|return
name|column
return|;
block|}
specifier|public
name|void
name|setColumn
parameter_list|(
name|String
name|column
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
block|}
specifier|public
name|String
name|getTabAlias
parameter_list|()
block|{
return|return
name|tabAlias
return|;
block|}
specifier|public
name|void
name|setTabAlias
parameter_list|(
name|String
name|tabAlias
parameter_list|)
block|{
name|this
operator|.
name|tabAlias
operator|=
name|tabAlias
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsPartitionColOrVirtualCol
parameter_list|()
block|{
return|return
name|isPartitionColOrVirtualCol
return|;
block|}
specifier|public
name|void
name|setIsPartitionColOrVirtualCol
parameter_list|(
name|boolean
name|isPartitionCol
parameter_list|)
block|{
name|this
operator|.
name|isPartitionColOrVirtualCol
operator|=
name|isPartitionCol
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
literal|"Column["
operator|+
name|column
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getExprString
parameter_list|()
block|{
return|return
name|getColumn
argument_list|()
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
name|lst
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|lst
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
return|return
name|lst
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
name|ExprNodeColumnDesc
argument_list|(
name|typeInfo
argument_list|,
name|column
argument_list|,
name|tabAlias
argument_list|,
name|isPartitionColOrVirtualCol
argument_list|,
name|isSkewedCol
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
name|ExprNodeColumnDesc
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ExprNodeColumnDesc
name|dest
init|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|column
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|getColumn
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
name|tabAlias
operator|!=
literal|null
operator|&&
name|dest
operator|.
name|tabAlias
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|tabAlias
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|tabAlias
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
comment|/**    * @return the isSkewedCol    */
specifier|public
name|boolean
name|isSkewedCol
parameter_list|()
block|{
return|return
name|isSkewedCol
return|;
block|}
comment|/**    * @param isSkewedCol the isSkewedCol to set    */
specifier|public
name|void
name|setSkewedCol
parameter_list|(
name|boolean
name|isSkewedCol
parameter_list|)
block|{
name|this
operator|.
name|isSkewedCol
operator|=
name|isSkewedCol
expr_stmt|;
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
name|column
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|tabAlias
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

