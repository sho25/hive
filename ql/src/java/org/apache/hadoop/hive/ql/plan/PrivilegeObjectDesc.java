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
name|util
operator|.
name|HashMap
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"privilege subject"
argument_list|)
specifier|public
class|class
name|PrivilegeObjectDesc
block|{
specifier|private
name|boolean
name|table
decl_stmt|;
specifier|private
name|String
name|object
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
specifier|public
name|PrivilegeObjectDesc
parameter_list|(
name|boolean
name|isTable
parameter_list|,
name|String
name|object
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|isTable
expr_stmt|;
name|this
operator|.
name|object
operator|=
name|object
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
specifier|public
name|PrivilegeObjectDesc
parameter_list|()
block|{   }
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"is table"
argument_list|)
specifier|public
name|boolean
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|void
name|setTable
parameter_list|(
name|boolean
name|isTable
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|isTable
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"object"
argument_list|)
specifier|public
name|String
name|getObject
parameter_list|()
block|{
return|return
name|object
return|;
block|}
specifier|public
name|void
name|setObject
parameter_list|(
name|String
name|object
parameter_list|)
block|{
name|this
operator|.
name|object
operator|=
name|object
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition spec"
argument_list|)
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpec
parameter_list|()
block|{
return|return
name|partSpec
return|;
block|}
specifier|public
name|void
name|setPartSpec
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
block|}
end_class

end_unit

