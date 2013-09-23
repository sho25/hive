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
name|metastore
operator|.
name|model
package|;
end_package

begin_class
specifier|public
class|class
name|MVersionTable
block|{
specifier|private
name|String
name|schemaVersion
decl_stmt|;
specifier|private
name|String
name|versionComment
decl_stmt|;
specifier|public
name|MVersionTable
parameter_list|()
block|{}
specifier|public
name|MVersionTable
parameter_list|(
name|String
name|schemaVersion
parameter_list|,
name|String
name|versionComment
parameter_list|)
block|{
name|this
operator|.
name|schemaVersion
operator|=
name|schemaVersion
expr_stmt|;
name|this
operator|.
name|versionComment
operator|=
name|versionComment
expr_stmt|;
block|}
comment|/**    * @return the versionComment    */
specifier|public
name|String
name|getVersionComment
parameter_list|()
block|{
return|return
name|versionComment
return|;
block|}
comment|/**    * @param versionComment the versionComment to set    */
specifier|public
name|void
name|setVersionComment
parameter_list|(
name|String
name|versionComment
parameter_list|)
block|{
name|this
operator|.
name|versionComment
operator|=
name|versionComment
expr_stmt|;
block|}
comment|/**    * @return the schemaVersion    */
specifier|public
name|String
name|getSchemaVersion
parameter_list|()
block|{
return|return
name|schemaVersion
return|;
block|}
comment|/**    * @param schemaVersion the schemaVersion to set    */
specifier|public
name|void
name|setSchemaVersion
parameter_list|(
name|String
name|schemaVersion
parameter_list|)
block|{
name|this
operator|.
name|schemaVersion
operator|=
name|schemaVersion
expr_stmt|;
block|}
block|}
end_class

end_unit

