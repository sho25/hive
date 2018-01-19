begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|MMetastoreDBProperties
block|{
specifier|private
name|String
name|propertyKey
decl_stmt|;
specifier|private
name|String
name|propertyValue
decl_stmt|;
specifier|private
name|String
name|description
decl_stmt|;
specifier|public
name|MMetastoreDBProperties
parameter_list|()
block|{}
specifier|public
name|MMetastoreDBProperties
parameter_list|(
name|String
name|propertykey
parameter_list|,
name|String
name|propertyValue
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|propertyKey
operator|=
name|propertykey
expr_stmt|;
name|this
operator|.
name|propertyValue
operator|=
name|propertyValue
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
specifier|public
name|String
name|getPropertykey
parameter_list|()
block|{
return|return
name|propertyKey
return|;
block|}
specifier|public
name|void
name|setPropertykey
parameter_list|(
name|String
name|propertykey
parameter_list|)
block|{
name|this
operator|.
name|propertyKey
operator|=
name|propertykey
expr_stmt|;
block|}
specifier|public
name|String
name|getPropertyValue
parameter_list|()
block|{
return|return
name|propertyValue
return|;
block|}
specifier|public
name|void
name|setPropertyValue
parameter_list|(
name|String
name|propertyValue
parameter_list|)
block|{
name|this
operator|.
name|propertyValue
operator|=
name|propertyValue
expr_stmt|;
block|}
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|description
return|;
block|}
specifier|public
name|void
name|setDescription
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
block|}
end_class

end_unit

