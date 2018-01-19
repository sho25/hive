begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|DropIndexMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
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

begin_comment
comment|/**  * JSON Implementation of DropIndexMessage.  */
end_comment

begin_class
specifier|public
class|class
name|JSONDropIndexMessage
extends|extends
name|DropIndexMessage
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
name|indexObjJson
decl_stmt|;
annotation|@
name|JsonProperty
name|Long
name|timestamp
decl_stmt|;
comment|/**    * Default constructor, required for Jackson.    */
specifier|public
name|JSONDropIndexMessage
parameter_list|()
block|{}
specifier|public
name|JSONDropIndexMessage
parameter_list|(
name|String
name|server
parameter_list|,
name|String
name|servicePrincipal
parameter_list|,
name|Index
name|index
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
name|index
operator|.
name|getDbName
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|indexObjJson
operator|=
name|JSONMessageFactory
operator|.
name|createIndexObjJson
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not serialize Index object"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
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
name|Long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
specifier|public
name|String
name|getIndexObjJson
parameter_list|()
block|{
return|return
name|indexObjJson
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

