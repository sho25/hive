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
name|Map
import|;
end_import

begin_class
specifier|public
class|class
name|MRegionStorageDescriptor
implements|implements
name|Serializable
block|{
specifier|private
name|String
name|regionName
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
specifier|public
name|MRegionStorageDescriptor
parameter_list|()
block|{}
comment|/**    * @param regionName    * @param location    */
specifier|public
name|MRegionStorageDescriptor
parameter_list|(
name|String
name|regionName
parameter_list|,
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
comment|/**    * @return region name    */
specifier|public
name|String
name|getRegionName
parameter_list|()
block|{
return|return
name|regionName
return|;
block|}
comment|/**    * @param region    */
specifier|public
name|void
name|setRegionName
parameter_list|(
name|String
name|regionName
parameter_list|)
block|{
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
block|}
comment|/**    * @return data location stored in this region descriptor    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
comment|/**    * @param location in this region descriptor    */
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

