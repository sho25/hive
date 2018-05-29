begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  *  */
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
name|ReplChangeManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

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
name|locationUri
decl_stmt|;
specifier|private
name|String
name|description
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
decl_stmt|;
specifier|private
name|String
name|ownerName
decl_stmt|;
specifier|private
name|String
name|ownerType
decl_stmt|;
specifier|private
name|String
name|catalogName
decl_stmt|;
comment|/**    * Default construction to keep jpox/jdo happy    */
specifier|public
name|MDatabase
parameter_list|()
block|{}
comment|/**    * To create a database object    * @param name of the database    * @param locationUri Location of the database in the warehouse    * @param description Comment describing the database    */
specifier|public
name|MDatabase
parameter_list|(
name|String
name|catalogName
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|locationUri
parameter_list|,
name|String
name|description
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
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
name|locationUri
operator|=
name|locationUri
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
name|this
operator|.
name|parameters
operator|=
name|parameters
expr_stmt|;
name|this
operator|.
name|catalogName
operator|=
name|catalogName
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
comment|/**    * @return the location_uri    */
specifier|public
name|String
name|getLocationUri
parameter_list|()
block|{
return|return
name|locationUri
return|;
block|}
comment|/**    * @param locationUri the locationUri to set    */
specifier|public
name|void
name|setLocationUri
parameter_list|(
name|String
name|locationUri
parameter_list|)
block|{
name|this
operator|.
name|locationUri
operator|=
name|locationUri
expr_stmt|;
block|}
comment|/**    * @return the description    */
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|description
return|;
block|}
comment|/**    * @param description the description to set    */
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
comment|/**    * @return the parameters mapping.    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getParameters
parameter_list|()
block|{
return|return
name|parameters
return|;
block|}
comment|/**    * @param parameters the parameters mapping.    */
specifier|public
name|void
name|setParameters
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
block|{
if|if
condition|(
name|parameters
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|parameters
operator|=
literal|null
expr_stmt|;
return|return;
block|}
name|this
operator|.
name|parameters
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|parameters
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|keys
control|)
block|{
comment|// Normalize the case for source of replication parameter
if|if
condition|(
name|ReplChangeManager
operator|.
name|SOURCE_OF_REPLICATION
operator|.
name|equalsIgnoreCase
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|// TODO :  Some extra validation can also be added as this is a user provided parameter.
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|ReplChangeManager
operator|.
name|SOURCE_OF_REPLICATION
argument_list|,
name|parameters
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|parameters
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|String
name|getOwnerName
parameter_list|()
block|{
return|return
name|ownerName
return|;
block|}
specifier|public
name|void
name|setOwnerName
parameter_list|(
name|String
name|ownerName
parameter_list|)
block|{
name|this
operator|.
name|ownerName
operator|=
name|ownerName
expr_stmt|;
block|}
specifier|public
name|String
name|getOwnerType
parameter_list|()
block|{
return|return
name|ownerType
return|;
block|}
specifier|public
name|void
name|setOwnerType
parameter_list|(
name|String
name|ownerType
parameter_list|)
block|{
name|this
operator|.
name|ownerType
operator|=
name|ownerType
expr_stmt|;
block|}
specifier|public
name|String
name|getCatalogName
parameter_list|()
block|{
return|return
name|catalogName
return|;
block|}
specifier|public
name|void
name|setCatalogName
parameter_list|(
name|String
name|catalogName
parameter_list|)
block|{
name|this
operator|.
name|catalogName
operator|=
name|catalogName
expr_stmt|;
block|}
block|}
end_class

end_unit

