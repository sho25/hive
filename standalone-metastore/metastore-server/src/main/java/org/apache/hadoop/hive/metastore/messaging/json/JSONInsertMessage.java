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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|messaging
operator|.
name|json
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Partition
import|;
end_import

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
name|Table
import|;
end_import

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
name|messaging
operator|.
name|MessageBuilder
import|;
end_import

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
name|messaging
operator|.
name|InsertMessage
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
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|annotation
operator|.
name|JsonProperty
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * JSON implementation of InsertMessage  */
end_comment

begin_class
specifier|public
class|class
name|JSONInsertMessage
extends|extends
name|InsertMessage
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
decl_stmt|,
name|tableType
decl_stmt|,
name|tableObjJson
decl_stmt|,
name|ptnObjJson
decl_stmt|;
annotation|@
name|JsonProperty
name|Long
name|timestamp
decl_stmt|;
annotation|@
name|JsonProperty
name|String
name|replace
decl_stmt|;
annotation|@
name|JsonProperty
name|List
argument_list|<
name|String
argument_list|>
name|files
decl_stmt|;
comment|/**    * Default constructor, needed for Jackson.    */
specifier|public
name|JSONInsertMessage
parameter_list|()
block|{   }
specifier|public
name|JSONInsertMessage
parameter_list|(
name|String
name|server
parameter_list|,
name|String
name|servicePrincipal
parameter_list|,
name|Table
name|tableObj
parameter_list|,
name|Partition
name|ptnObj
parameter_list|,
name|boolean
name|replace
parameter_list|,
name|Iterator
argument_list|<
name|String
argument_list|>
name|fileIter
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
if|if
condition|(
literal|null
operator|==
name|tableObj
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Table not valid."
argument_list|)
throw|;
block|}
name|this
operator|.
name|db
operator|=
name|tableObj
operator|.
name|getDbName
argument_list|()
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|tableObj
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableType
operator|=
name|tableObj
operator|.
name|getTableType
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|tableObjJson
operator|=
name|MessageBuilder
operator|.
name|createTableObjJson
argument_list|(
name|tableObj
argument_list|)
expr_stmt|;
if|if
condition|(
literal|null
operator|!=
name|ptnObj
condition|)
block|{
name|this
operator|.
name|ptnObjJson
operator|=
name|MessageBuilder
operator|.
name|createPartitionObjJson
argument_list|(
name|ptnObj
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|ptnObjJson
operator|=
literal|null
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not serialize: "
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|replace
operator|=
name|Boolean
operator|.
name|toString
argument_list|(
name|replace
argument_list|)
expr_stmt|;
name|this
operator|.
name|files
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|fileIter
argument_list|)
expr_stmt|;
name|checkValid
argument_list|()
expr_stmt|;
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
name|String
name|getTableType
parameter_list|()
block|{
if|if
condition|(
name|tableType
operator|!=
literal|null
condition|)
block|{
return|return
name|tableType
return|;
block|}
else|else
block|{
return|return
literal|""
return|;
block|}
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
name|Iterable
argument_list|<
name|String
argument_list|>
name|getFiles
parameter_list|()
block|{
return|return
name|files
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
name|boolean
name|isReplace
parameter_list|()
block|{
return|return
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|replace
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Table
name|getTableObj
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|(
name|Table
operator|)
name|MessageBuilder
operator|.
name|getTObj
argument_list|(
name|tableObjJson
argument_list|,
name|Table
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|getPtnObj
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|(
operator|(
literal|null
operator|==
name|ptnObjJson
operator|)
condition|?
literal|null
else|:
operator|(
name|Partition
operator|)
name|MessageBuilder
operator|.
name|getTObj
argument_list|(
name|ptnObjJson
argument_list|,
name|Partition
operator|.
name|class
argument_list|)
operator|)
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

