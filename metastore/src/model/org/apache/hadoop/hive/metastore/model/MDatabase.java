begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  *   */
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

begin_comment
comment|/**  * Storage Class representing the Hive MDatabase in a rdbms  *  */
end_comment

begin_class
specifier|public
class|class
name|MDatabase
block|{
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
comment|/**    * Default construction to keep jpox/jdo happy    */
specifier|public
name|MDatabase
parameter_list|()
block|{}
comment|/**    * To create a database object    * @param name of the database    * @param location future use    */
specifier|public
name|MDatabase
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|location
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
name|location
operator|=
name|location
expr_stmt|;
block|}
comment|/**    * @return the name    */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * @param name the name to set    */
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
comment|/**    * @return the location    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
comment|/**    * @param location the location to set    */
specifier|public
name|void
name|setLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
block|}
end_class

end_unit

