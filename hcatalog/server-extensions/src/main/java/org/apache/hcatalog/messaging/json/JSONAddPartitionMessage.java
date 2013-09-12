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
name|hcatalog
operator|.
name|messaging
operator|.
name|json
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|AddPartitionMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonProperty
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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

begin_comment
comment|/**  * JSON implementation of AddPartitionMessage.  * @deprecated Use/modify {@link org.apache.hcatalog.messaging.json.JSONAddPartitionMessage} instead  */
end_comment

begin_class
specifier|public
class|class
name|JSONAddPartitionMessage
extends|extends
name|AddPartitionMessage
block|{
annotation|@
name|JsonProperty
name|String
name|server
decl_stmt|,
name|servicePrincipal
decl_stmt|,
name|db
decl_stmt|,
name|table
decl_stmt|;
annotation|@
name|JsonProperty
name|Long
name|timestamp
decl_stmt|;
annotation|@
name|JsonProperty
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitions
decl_stmt|;
comment|/**    * Default Constructor. Required for Jackson.    */
specifier|public
name|JSONAddPartitionMessage
parameter_list|()
block|{}
specifier|public
name|JSONAddPartitionMessage
parameter_list|(
name|String
name|server
parameter_list|,
name|String
name|servicePrincipal
parameter_list|,
name|String
name|db
parameter_list|,
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitions
parameter_list|,
name|Long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|servicePrincipal
operator|=
name|servicePrincipal
expr_stmt|;
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partitions
operator|=
name|partitions
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|checkValid
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getServer
parameter_list|()
block|{
return|return
name|server
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getServicePrincipal
parameter_list|()
block|{
return|return
name|servicePrincipal
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDB
parameter_list|()
block|{
return|return
name|db
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|getPartitions
parameter_list|()
block|{
return|return
name|partitions
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
try|try
block|{
return|return
name|JSONMessageDeserializer
operator|.
name|mapper
operator|.
name|writeValueAsString
argument_list|(
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not serialize: "
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

