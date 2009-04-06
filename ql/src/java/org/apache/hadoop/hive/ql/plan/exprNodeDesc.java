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
name|lib
operator|.
name|Node
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

begin_class
specifier|public
specifier|abstract
class|class
name|exprNodeDesc
implements|implements
name|Serializable
implements|,
name|Node
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
name|TypeInfo
name|typeInfo
decl_stmt|;
specifier|public
name|exprNodeDesc
parameter_list|()
block|{}
specifier|public
name|exprNodeDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|this
operator|.
name|typeInfo
operator|=
name|typeInfo
expr_stmt|;
if|if
condition|(
name|typeInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"typeInfo cannot be null!"
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|abstract
name|exprNodeDesc
name|clone
parameter_list|()
function_decl|;
specifier|public
name|TypeInfo
name|getTypeInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|typeInfo
return|;
block|}
specifier|public
name|void
name|setTypeInfo
parameter_list|(
name|TypeInfo
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
name|getExprString
parameter_list|()
block|{
assert|assert
operator|(
literal|false
operator|)
assert|;
return|return
literal|null
return|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"type"
argument_list|)
specifier|public
name|String
name|getTypeString
parameter_list|()
block|{
return|return
name|typeInfo
operator|.
name|getTypeName
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getCols
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|getChildren
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
end_class

end_unit

