begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
package|;
end_package

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
name|WriteEntity
extends|extends
name|EntityBase
operator|.
name|Entity
block|{
comment|/**    * Don't instantiate {@link WriteEntity} directly. Use, {@link Builder} to    * build {@link WriteEntity}.    */
specifier|private
name|WriteEntity
parameter_list|()
block|{
comment|// Not allowed.
block|}
specifier|private
name|WriteEntity
parameter_list|(
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|region
operator|=
name|builder
operator|.
name|region
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|builder
operator|.
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|builder
operator|.
name|tableName
expr_stmt|;
name|this
operator|.
name|partitionKVs
operator|=
name|builder
operator|.
name|partitionKVs
expr_stmt|;
block|}
comment|/**    * This class should be used to build {@link WriteEntity}. It follows builder    * pattern, letting you build your {@link WriteEntity} with whatever level of    * detail you want.    *    */
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|EntityBase
block|{
specifier|public
name|Builder
name|withRegion
parameter_list|(
specifier|final
name|String
name|region
parameter_list|)
block|{
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withDatabase
parameter_list|(
specifier|final
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withTable
parameter_list|(
specifier|final
name|String
name|tblName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tblName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withPartition
parameter_list|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partKVs
parameter_list|)
block|{
name|this
operator|.
name|partitionKVs
operator|=
name|partKVs
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|WriteEntity
name|build
parameter_list|()
block|{
return|return
operator|new
name|WriteEntity
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

