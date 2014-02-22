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
name|metastore
operator|.
name|api
operator|.
name|ResourceUri
import|;
end_import

begin_comment
comment|/**  * CreateFunctionDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create Function"
argument_list|)
specifier|public
class|class
name|CreateFunctionDesc
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
name|String
name|functionName
decl_stmt|;
specifier|private
name|String
name|className
decl_stmt|;
specifier|private
name|boolean
name|isTemp
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
decl_stmt|;
comment|/**    * For serialization only.    */
specifier|public
name|CreateFunctionDesc
parameter_list|()
block|{   }
specifier|public
name|CreateFunctionDesc
parameter_list|(
name|String
name|functionName
parameter_list|,
name|boolean
name|isTemp
parameter_list|,
name|String
name|className
parameter_list|,
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
parameter_list|)
block|{
name|this
operator|.
name|functionName
operator|=
name|functionName
expr_stmt|;
name|this
operator|.
name|isTemp
operator|=
name|isTemp
expr_stmt|;
name|this
operator|.
name|className
operator|=
name|className
expr_stmt|;
name|this
operator|.
name|resources
operator|=
name|resources
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
argument_list|)
specifier|public
name|String
name|getFunctionName
parameter_list|()
block|{
return|return
name|functionName
return|;
block|}
specifier|public
name|void
name|setFunctionName
parameter_list|(
name|String
name|functionName
parameter_list|)
block|{
name|this
operator|.
name|functionName
operator|=
name|functionName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"class"
argument_list|)
specifier|public
name|String
name|getClassName
parameter_list|()
block|{
return|return
name|className
return|;
block|}
specifier|public
name|void
name|setClassName
parameter_list|(
name|String
name|className
parameter_list|)
block|{
name|this
operator|.
name|className
operator|=
name|className
expr_stmt|;
block|}
specifier|public
name|boolean
name|isTemp
parameter_list|()
block|{
return|return
name|isTemp
return|;
block|}
specifier|public
name|void
name|setTemp
parameter_list|(
name|boolean
name|isTemp
parameter_list|)
block|{
name|this
operator|.
name|isTemp
operator|=
name|isTemp
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|getResources
parameter_list|()
block|{
return|return
name|resources
return|;
block|}
specifier|public
name|void
name|setResources
parameter_list|(
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
parameter_list|)
block|{
name|this
operator|.
name|resources
operator|=
name|resources
expr_stmt|;
block|}
block|}
end_class

end_unit

