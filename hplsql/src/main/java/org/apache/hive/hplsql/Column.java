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
name|hive
operator|.
name|hplsql
package|;
end_package

begin_comment
comment|/**  * Table column  */
end_comment

begin_class
specifier|public
class|class
name|Column
block|{
name|String
name|name
decl_stmt|;
name|String
name|type
decl_stmt|;
name|Var
name|value
decl_stmt|;
name|Column
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type
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
name|type
operator|=
name|type
expr_stmt|;
block|}
comment|/**    * Set the column value    */
name|void
name|setValue
parameter_list|(
name|Var
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * Get the column name    */
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * Get the column type    */
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**    * Get the column value    */
name|Var
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
block|}
end_class

end_unit

