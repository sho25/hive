begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|api
operator|.
name|request
package|;
end_package

begin_class
specifier|public
class|class
name|TestStartRequest
block|{
specifier|private
name|String
name|profile
decl_stmt|;
specifier|private
name|String
name|testHandle
decl_stmt|;
specifier|private
name|String
name|patchURL
decl_stmt|;
specifier|private
name|String
name|jiraName
decl_stmt|;
specifier|private
name|boolean
name|clearLibraryCache
decl_stmt|;
specifier|public
name|TestStartRequest
parameter_list|()
block|{    }
specifier|public
name|TestStartRequest
parameter_list|(
name|String
name|profile
parameter_list|,
name|String
name|testHandle
parameter_list|,
name|String
name|jiraName
parameter_list|,
name|String
name|patchURL
parameter_list|,
name|boolean
name|clearLibraryCache
parameter_list|)
block|{
name|this
operator|.
name|profile
operator|=
name|profile
expr_stmt|;
name|this
operator|.
name|testHandle
operator|=
name|testHandle
expr_stmt|;
name|this
operator|.
name|jiraName
operator|=
name|jiraName
expr_stmt|;
name|this
operator|.
name|patchURL
operator|=
name|patchURL
expr_stmt|;
name|this
operator|.
name|clearLibraryCache
operator|=
name|clearLibraryCache
expr_stmt|;
block|}
specifier|public
name|String
name|getProfile
parameter_list|()
block|{
return|return
name|profile
return|;
block|}
specifier|public
name|void
name|setProfile
parameter_list|(
name|String
name|profile
parameter_list|)
block|{
name|this
operator|.
name|profile
operator|=
name|profile
expr_stmt|;
block|}
specifier|public
name|String
name|getPatchURL
parameter_list|()
block|{
return|return
name|patchURL
return|;
block|}
specifier|public
name|void
name|setPatchURL
parameter_list|(
name|String
name|patchURL
parameter_list|)
block|{
name|this
operator|.
name|patchURL
operator|=
name|patchURL
expr_stmt|;
block|}
specifier|public
name|boolean
name|isClearLibraryCache
parameter_list|()
block|{
return|return
name|clearLibraryCache
return|;
block|}
specifier|public
name|void
name|setClearLibraryCache
parameter_list|(
name|boolean
name|clearLibraryCache
parameter_list|)
block|{
name|this
operator|.
name|clearLibraryCache
operator|=
name|clearLibraryCache
expr_stmt|;
block|}
specifier|public
name|String
name|getJiraName
parameter_list|()
block|{
return|return
name|jiraName
return|;
block|}
specifier|public
name|void
name|setJiraName
parameter_list|(
name|String
name|jiraName
parameter_list|)
block|{
name|this
operator|.
name|jiraName
operator|=
name|jiraName
expr_stmt|;
block|}
specifier|public
name|String
name|getTestHandle
parameter_list|()
block|{
return|return
name|testHandle
return|;
block|}
specifier|public
name|void
name|setTestHandle
parameter_list|(
name|String
name|testHandle
parameter_list|)
block|{
name|this
operator|.
name|testHandle
operator|=
name|testHandle
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
literal|"TestStartRequest [profile="
operator|+
name|profile
operator|+
literal|", testHandle="
operator|+
name|testHandle
operator|+
literal|", patchURL="
operator|+
name|patchURL
operator|+
literal|", jiraName="
operator|+
name|jiraName
operator|+
literal|", clearLibraryCache="
operator|+
name|clearLibraryCache
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

