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

begin_comment
comment|/**  * FunctionWork.  *  */
end_comment

begin_class
specifier|public
class|class
name|FunctionWork
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
name|CreateFunctionDesc
name|createFunctionDesc
decl_stmt|;
specifier|private
name|DropFunctionDesc
name|dropFunctionDesc
decl_stmt|;
specifier|private
name|ReloadFunctionDesc
name|reloadFunctionDesc
decl_stmt|;
specifier|private
name|CreateMacroDesc
name|createMacroDesc
decl_stmt|;
specifier|private
name|DropMacroDesc
name|dropMacroDesc
decl_stmt|;
comment|/**    * For serialization only.    */
specifier|public
name|FunctionWork
parameter_list|()
block|{   }
specifier|public
name|FunctionWork
parameter_list|(
name|CreateFunctionDesc
name|createFunctionDesc
parameter_list|)
block|{
name|this
operator|.
name|createFunctionDesc
operator|=
name|createFunctionDesc
expr_stmt|;
block|}
specifier|public
name|FunctionWork
parameter_list|(
name|DropFunctionDesc
name|dropFunctionDesc
parameter_list|)
block|{
name|this
operator|.
name|dropFunctionDesc
operator|=
name|dropFunctionDesc
expr_stmt|;
block|}
specifier|public
name|FunctionWork
parameter_list|(
name|ReloadFunctionDesc
name|reloadFunctionDesc
parameter_list|)
block|{
name|this
operator|.
name|reloadFunctionDesc
operator|=
name|reloadFunctionDesc
expr_stmt|;
block|}
specifier|public
name|FunctionWork
parameter_list|(
name|CreateMacroDesc
name|createMacroDesc
parameter_list|)
block|{
name|this
operator|.
name|createMacroDesc
operator|=
name|createMacroDesc
expr_stmt|;
block|}
specifier|public
name|FunctionWork
parameter_list|(
name|DropMacroDesc
name|dropMacroDesc
parameter_list|)
block|{
name|this
operator|.
name|dropMacroDesc
operator|=
name|dropMacroDesc
expr_stmt|;
block|}
specifier|public
name|CreateFunctionDesc
name|getCreateFunctionDesc
parameter_list|()
block|{
return|return
name|createFunctionDesc
return|;
block|}
specifier|public
name|void
name|setCreateFunctionDesc
parameter_list|(
name|CreateFunctionDesc
name|createFunctionDesc
parameter_list|)
block|{
name|this
operator|.
name|createFunctionDesc
operator|=
name|createFunctionDesc
expr_stmt|;
block|}
specifier|public
name|DropFunctionDesc
name|getDropFunctionDesc
parameter_list|()
block|{
return|return
name|dropFunctionDesc
return|;
block|}
specifier|public
name|void
name|setDropFunctionDesc
parameter_list|(
name|DropFunctionDesc
name|dropFunctionDesc
parameter_list|)
block|{
name|this
operator|.
name|dropFunctionDesc
operator|=
name|dropFunctionDesc
expr_stmt|;
block|}
specifier|public
name|ReloadFunctionDesc
name|getReloadFunctionDesc
parameter_list|()
block|{
return|return
name|reloadFunctionDesc
return|;
block|}
specifier|public
name|void
name|setReloadFunctionDesc
parameter_list|(
name|ReloadFunctionDesc
name|reloadFunctionDesc
parameter_list|)
block|{
name|this
operator|.
name|reloadFunctionDesc
operator|=
name|reloadFunctionDesc
expr_stmt|;
block|}
specifier|public
name|CreateMacroDesc
name|getCreateMacroDesc
parameter_list|()
block|{
return|return
name|createMacroDesc
return|;
block|}
specifier|public
name|DropMacroDesc
name|getDropMacroDesc
parameter_list|()
block|{
return|return
name|dropMacroDesc
return|;
block|}
block|}
end_class

end_unit

