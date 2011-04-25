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
name|metadata
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|HiveConf
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|PrimitiveTypeInfo
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

begin_class
specifier|public
class|class
name|VirtualColumn
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
name|VirtualColumn
name|FILENAME
init|=
operator|new
name|VirtualColumn
argument_list|(
literal|"INPUT__FILE__NAME"
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|VirtualColumn
name|BLOCKOFFSET
init|=
operator|new
name|VirtualColumn
argument_list|(
literal|"BLOCK__OFFSET__INSIDE__FILE"
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|VirtualColumn
name|ROWOFFSET
init|=
operator|new
name|VirtualColumn
argument_list|(
literal|"ROW__OFFSET__INSIDE__BLOCK"
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|PrimitiveTypeInfo
name|typeInfo
decl_stmt|;
specifier|private
name|boolean
name|isHidden
init|=
literal|true
decl_stmt|;
specifier|public
name|VirtualColumn
parameter_list|()
block|{   }
specifier|public
name|VirtualColumn
parameter_list|(
name|String
name|name
parameter_list|,
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|typeInfo
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|VirtualColumn
parameter_list|(
name|String
name|name
parameter_list|,
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|,
name|boolean
name|isHidden
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|typeInfo
operator|=
name|typeInfo
expr_stmt|;
name|this
operator|.
name|isHidden
operator|=
name|isHidden
expr_stmt|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|getRegistry
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|VirtualColumn
argument_list|>
name|l
init|=
operator|new
name|ArrayList
argument_list|<
name|VirtualColumn
argument_list|>
argument_list|()
decl_stmt|;
name|l
operator|.
name|add
argument_list|(
name|BLOCKOFFSET
argument_list|)
expr_stmt|;
name|l
operator|.
name|add
argument_list|(
name|FILENAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEROWOFFSET
argument_list|)
condition|)
block|{
name|l
operator|.
name|add
argument_list|(
name|ROWOFFSET
argument_list|)
expr_stmt|;
block|}
return|return
name|l
return|;
block|}
specifier|public
name|PrimitiveTypeInfo
name|getTypeInfo
parameter_list|()
block|{
return|return
name|typeInfo
return|;
block|}
specifier|public
name|void
name|setTypeInfo
parameter_list|(
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|)
block|{
name|this
operator|.
name|typeInfo
operator|=
name|typeInfo
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|boolean
name|isHidden
parameter_list|()
block|{
return|return
name|isHidden
return|;
block|}
specifier|public
name|boolean
name|getIsHidden
parameter_list|()
block|{
return|return
name|isHidden
return|;
block|}
specifier|public
name|void
name|setIsHidden
parameter_list|(
name|boolean
name|isHidden
parameter_list|)
block|{
name|this
operator|.
name|isHidden
operator|=
name|isHidden
expr_stmt|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
name|VirtualColumn
name|c
init|=
operator|(
name|VirtualColumn
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|name
operator|.
name|equals
argument_list|(
name|c
operator|.
name|name
argument_list|)
operator|&&
name|this
operator|.
name|typeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|c
operator|.
name|getTypeInfo
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

