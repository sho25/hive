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
name|metastore
operator|.
name|model
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

begin_class
specifier|public
class|class
name|MConstraint
block|{
name|String
name|constraintName
decl_stmt|;
name|int
name|constraintType
decl_stmt|;
name|int
name|position
decl_stmt|;
name|Integer
name|deleteRule
decl_stmt|;
name|Integer
name|updateRule
decl_stmt|;
name|MTable
name|parentTable
decl_stmt|;
name|MTable
name|childTable
decl_stmt|;
name|MColumnDescriptor
name|parentColumn
decl_stmt|;
name|MColumnDescriptor
name|childColumn
decl_stmt|;
name|Integer
name|childIntegerIndex
decl_stmt|;
name|Integer
name|parentIntegerIndex
decl_stmt|;
name|int
name|enableValidateRely
decl_stmt|;
comment|// 0 - Primary Key
comment|// 1 - PK-FK relationship
comment|// 2 - Unique Constraint
comment|// 3 - Not Null Constraint
specifier|public
specifier|final
specifier|static
name|int
name|PRIMARY_KEY_CONSTRAINT
init|=
literal|0
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|FOREIGN_KEY_CONSTRAINT
init|=
literal|1
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|UNIQUE_CONSTRAINT
init|=
literal|2
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|NOT_NULL_CONSTRAINT
init|=
literal|3
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
specifier|static
class|class
name|PK
implements|implements
name|Serializable
block|{
specifier|public
name|String
name|constraintName
decl_stmt|;
specifier|public
name|int
name|position
decl_stmt|;
specifier|public
name|PK
parameter_list|()
block|{}
specifier|public
name|PK
parameter_list|(
name|String
name|constraintName
parameter_list|,
name|int
name|position
parameter_list|)
block|{
name|this
operator|.
name|constraintName
operator|=
name|constraintName
expr_stmt|;
name|this
operator|.
name|position
operator|=
name|position
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|constraintName
operator|+
literal|":"
operator|+
name|position
return|;
block|}
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|toString
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|!=
literal|null
operator|&&
operator|(
name|other
operator|instanceof
name|PK
operator|)
condition|)
block|{
name|PK
name|otherPK
init|=
operator|(
name|PK
operator|)
name|other
decl_stmt|;
return|return
name|otherPK
operator|.
name|constraintName
operator|.
name|equals
argument_list|(
name|constraintName
argument_list|)
operator|&&
name|otherPK
operator|.
name|position
operator|==
name|position
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|MConstraint
parameter_list|()
block|{}
specifier|public
name|MConstraint
parameter_list|(
name|String
name|constraintName
parameter_list|,
name|int
name|constraintType
parameter_list|,
name|int
name|position
parameter_list|,
name|Integer
name|deleteRule
parameter_list|,
name|Integer
name|updateRule
parameter_list|,
name|int
name|enableRelyValidate
parameter_list|,
name|MTable
name|parentTable
parameter_list|,
name|MTable
name|childTable
parameter_list|,
name|MColumnDescriptor
name|parentColumn
parameter_list|,
name|MColumnDescriptor
name|childColumn
parameter_list|,
name|Integer
name|childIntegerIndex
parameter_list|,
name|Integer
name|parentIntegerIndex
parameter_list|)
block|{
name|this
operator|.
name|constraintName
operator|=
name|constraintName
expr_stmt|;
name|this
operator|.
name|constraintType
operator|=
name|constraintType
expr_stmt|;
name|this
operator|.
name|parentTable
operator|=
name|parentTable
expr_stmt|;
name|this
operator|.
name|childTable
operator|=
name|childTable
expr_stmt|;
name|this
operator|.
name|parentColumn
operator|=
name|parentColumn
expr_stmt|;
name|this
operator|.
name|childColumn
operator|=
name|childColumn
expr_stmt|;
name|this
operator|.
name|position
operator|=
name|position
expr_stmt|;
name|this
operator|.
name|deleteRule
operator|=
name|deleteRule
expr_stmt|;
name|this
operator|.
name|updateRule
operator|=
name|updateRule
expr_stmt|;
name|this
operator|.
name|enableValidateRely
operator|=
name|enableRelyValidate
expr_stmt|;
name|this
operator|.
name|childIntegerIndex
operator|=
name|childIntegerIndex
expr_stmt|;
name|this
operator|.
name|parentIntegerIndex
operator|=
name|parentIntegerIndex
expr_stmt|;
block|}
specifier|public
name|String
name|getConstraintName
parameter_list|()
block|{
return|return
name|constraintName
return|;
block|}
specifier|public
name|void
name|setConstraintName
parameter_list|(
name|String
name|fkName
parameter_list|)
block|{
name|this
operator|.
name|constraintName
operator|=
name|fkName
expr_stmt|;
block|}
specifier|public
name|int
name|getConstraintType
parameter_list|()
block|{
return|return
name|constraintType
return|;
block|}
specifier|public
name|void
name|setConstraintType
parameter_list|(
name|int
name|ct
parameter_list|)
block|{
name|this
operator|.
name|constraintType
operator|=
name|ct
expr_stmt|;
block|}
specifier|public
name|int
name|getPosition
parameter_list|()
block|{
return|return
name|position
return|;
block|}
specifier|public
name|void
name|setPosition
parameter_list|(
name|int
name|po
parameter_list|)
block|{
name|this
operator|.
name|position
operator|=
name|po
expr_stmt|;
block|}
specifier|public
name|Integer
name|getDeleteRule
parameter_list|()
block|{
return|return
name|deleteRule
return|;
block|}
specifier|public
name|void
name|setDeleteRule
parameter_list|(
name|Integer
name|de
parameter_list|)
block|{
name|this
operator|.
name|deleteRule
operator|=
name|de
expr_stmt|;
block|}
specifier|public
name|int
name|getEnableValidateRely
parameter_list|()
block|{
return|return
name|enableValidateRely
return|;
block|}
specifier|public
name|void
name|setEnableValidateRely
parameter_list|(
name|int
name|enableValidateRely
parameter_list|)
block|{
name|this
operator|.
name|enableValidateRely
operator|=
name|enableValidateRely
expr_stmt|;
block|}
specifier|public
name|Integer
name|getChildIntegerIndex
parameter_list|()
block|{
return|return
name|childIntegerIndex
return|;
block|}
specifier|public
name|void
name|setChildIntegerIndex
parameter_list|(
name|Integer
name|childIntegerIndex
parameter_list|)
block|{
name|this
operator|.
name|childIntegerIndex
operator|=
name|childIntegerIndex
expr_stmt|;
block|}
specifier|public
name|Integer
name|getParentIntegerIndex
parameter_list|()
block|{
return|return
name|parentIntegerIndex
return|;
block|}
specifier|public
name|void
name|setParentIntegerIndex
parameter_list|(
name|Integer
name|parentIntegerIndex
parameter_list|)
block|{
name|this
operator|.
name|parentIntegerIndex
operator|=
name|parentIntegerIndex
expr_stmt|;
block|}
specifier|public
name|Integer
name|getUpdateRule
parameter_list|()
block|{
return|return
name|updateRule
return|;
block|}
specifier|public
name|void
name|setUpdateRule
parameter_list|(
name|Integer
name|ur
parameter_list|)
block|{
name|this
operator|.
name|updateRule
operator|=
name|ur
expr_stmt|;
block|}
specifier|public
name|MTable
name|getChildTable
parameter_list|()
block|{
return|return
name|childTable
return|;
block|}
specifier|public
name|void
name|setChildTable
parameter_list|(
name|MTable
name|ft
parameter_list|)
block|{
name|this
operator|.
name|childTable
operator|=
name|ft
expr_stmt|;
block|}
specifier|public
name|MTable
name|getParentTable
parameter_list|()
block|{
return|return
name|parentTable
return|;
block|}
specifier|public
name|void
name|setParentTable
parameter_list|(
name|MTable
name|pt
parameter_list|)
block|{
name|this
operator|.
name|parentTable
operator|=
name|pt
expr_stmt|;
block|}
specifier|public
name|MColumnDescriptor
name|getParentColumn
parameter_list|()
block|{
return|return
name|parentColumn
return|;
block|}
specifier|public
name|void
name|setParentColumn
parameter_list|(
name|MColumnDescriptor
name|name
parameter_list|)
block|{
name|this
operator|.
name|parentColumn
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|MColumnDescriptor
name|getChildColumn
parameter_list|()
block|{
return|return
name|childColumn
return|;
block|}
specifier|public
name|void
name|setChildColumn
parameter_list|(
name|MColumnDescriptor
name|name
parameter_list|)
block|{
name|this
operator|.
name|childColumn
operator|=
name|name
expr_stmt|;
block|}
block|}
end_class

end_unit

