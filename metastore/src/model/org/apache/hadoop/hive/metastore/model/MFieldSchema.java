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
comment|/**  * Represent a column or a type of a table or object  */
end_comment

begin_class
specifier|public
class|class
name|MFieldSchema
block|{
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|type
decl_stmt|;
specifier|private
name|String
name|comment
decl_stmt|;
specifier|public
name|MFieldSchema
parameter_list|()
block|{}
comment|/**    * @param comment    * @param name    * @param type    */
specifier|public
name|MFieldSchema
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|comment
parameter_list|)
block|{
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
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
comment|/**    * @return the comment    */
specifier|public
name|String
name|getComment
parameter_list|()
block|{
return|return
name|comment
return|;
block|}
comment|/**    * @param comment the comment to set    */
specifier|public
name|void
name|setComment
parameter_list|(
name|String
name|comment
parameter_list|)
block|{
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
block|}
comment|/**    * @return the type    */
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**    * @param type the type to set    */
specifier|public
name|void
name|setType
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|field
expr_stmt|;
block|}
block|}
end_class

end_unit

