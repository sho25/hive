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
name|Map
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
name|AlterPartitionMessage
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

begin_comment
comment|/**  * JSON alter table message  */
end_comment

begin_class
specifier|public
class|class
name|JSONAlterPartitionMessage
extends|extends
name|AlterPartitionMessage
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
decl_stmt|;
annotation|@
name|JsonProperty
name|String
name|isTruncateOp
decl_stmt|;
annotation|@
name|JsonProperty
name|Long
name|timestamp
decl_stmt|,
name|writeId
decl_stmt|;
annotation|@
name|JsonProperty
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|keyValues
decl_stmt|;
annotation|@
name|JsonProperty
name|String
name|partitionObjBeforeJson
decl_stmt|,
name|partitionObjAfterJson
decl_stmt|;
comment|/**    * Default constructor, needed for Jackson.    */
specifier|public
name|JSONAlterPartitionMessage
parameter_list|()
block|{   }
specifier|public
name|JSONAlterPartitionMessage
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
name|partitionObjBefore
parameter_list|,
name|Partition
name|partitionObjAfter
parameter_list|,
name|boolean
name|isTruncateOp
parameter_list|,
name|Long
name|writeId
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
name|this
operator|.
name|isTruncateOp
operator|=
name|Boolean
operator|.
name|toString
argument_list|(
name|isTruncateOp
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|keyValues
operator|=
name|JSONMessageFactory
operator|.
name|getPartitionKeyValues
argument_list|(
name|tableObj
argument_list|,
name|partitionObjBefore
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeId
operator|=
name|writeId
expr_stmt|;
try|try
block|{
name|this
operator|.
name|tableObjJson
operator|=
name|JSONMessageFactory
operator|.
name|createTableObjJson
argument_list|(
name|tableObj
argument_list|)
expr_stmt|;
name|this
operator|.
name|partitionObjBeforeJson
operator|=
name|JSONMessageFactory
operator|.
name|createPartitionObjJson
argument_list|(
name|partitionObjBefore
argument_list|)
expr_stmt|;
name|this
operator|.
name|partitionObjAfterJson
operator|=
name|JSONMessageFactory
operator|.
name|createPartitionObjJson
argument_list|(
name|partitionObjAfter
argument_list|)
expr_stmt|;
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
name|boolean
name|getIsTruncateOp
parameter_list|()
block|{
return|return
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|isTruncateOp
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getKeyValues
parameter_list|()
block|{
return|return
name|keyValues
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
name|JSONMessageFactory
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
name|getPtnObjBefore
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|(
name|Partition
operator|)
name|JSONMessageFactory
operator|.
name|getTObj
argument_list|(
name|partitionObjBeforeJson
argument_list|,
name|Partition
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|getPtnObjAfter
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|(
name|Partition
operator|)
name|JSONMessageFactory
operator|.
name|getTObj
argument_list|(
name|partitionObjAfterJson
argument_list|,
name|Partition
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|String
name|getTableObjJson
parameter_list|()
block|{
return|return
name|tableObjJson
return|;
block|}
specifier|public
name|String
name|getPartitionObjBeforeJson
parameter_list|()
block|{
return|return
name|partitionObjBeforeJson
return|;
block|}
specifier|public
name|String
name|getPartitionObjAfterJson
parameter_list|()
block|{
return|return
name|partitionObjAfterJson
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|getWriteId
parameter_list|()
block|{
return|return
name|writeId
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
block|}
block|}
end_class

end_unit

