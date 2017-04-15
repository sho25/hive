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
name|LinkedHashMap
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

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|Iterables
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
name|Database
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
name|Function
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
name|Index
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
name|NotificationEvent
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
name|AddPartitionMessage
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
name|AlterIndexMessage
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|messaging
operator|.
name|AlterTableMessage
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
name|CreateDatabaseMessage
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
name|CreateFunctionMessage
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
name|CreateIndexMessage
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
name|CreateTableMessage
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
name|DropDatabaseMessage
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
name|DropFunctionMessage
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
name|DropIndexMessage
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
name|DropPartitionMessage
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
name|DropTableMessage
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|messaging
operator|.
name|MessageDeserializer
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
name|MessageFactory
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
name|PartitionFiles
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
name|TBase
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
name|TDeserializer
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
name|apache
operator|.
name|thrift
operator|.
name|TSerializer
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
name|protocol
operator|.
name|TJSONProtocol
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
name|JsonFactory
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
name|JsonNode
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
name|JsonParser
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
name|map
operator|.
name|ObjectMapper
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
name|node
operator|.
name|ObjectNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|Iterators
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
comment|/**  * The JSON implementation of the MessageFactory. Constructs JSON implementations of each  * message-type.  */
end_comment

begin_class
specifier|public
class|class
name|JSONMessageFactory
extends|extends
name|MessageFactory
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JSONMessageFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|JSONMessageDeserializer
name|deserializer
init|=
operator|new
name|JSONMessageDeserializer
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|TDeserializer
name|thriftDeSerializer
init|=
operator|new
name|TDeserializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|MessageDeserializer
name|getDeserializer
parameter_list|()
block|{
return|return
name|deserializer
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMessageFormat
parameter_list|()
block|{
return|return
literal|"json-0.2"
return|;
block|}
annotation|@
name|Override
specifier|public
name|CreateDatabaseMessage
name|buildCreateDatabaseMessage
parameter_list|(
name|Database
name|db
parameter_list|)
block|{
return|return
operator|new
name|JSONCreateDatabaseMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|db
operator|.
name|getName
argument_list|()
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DropDatabaseMessage
name|buildDropDatabaseMessage
parameter_list|(
name|Database
name|db
parameter_list|)
block|{
return|return
operator|new
name|JSONDropDatabaseMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|db
operator|.
name|getName
argument_list|()
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CreateTableMessage
name|buildCreateTableMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Iterator
argument_list|<
name|String
argument_list|>
name|fileIter
parameter_list|)
block|{
return|return
operator|new
name|JSONCreateTableMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|table
argument_list|,
name|fileIter
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AlterTableMessage
name|buildAlterTableMessage
parameter_list|(
name|Table
name|before
parameter_list|,
name|Table
name|after
parameter_list|)
block|{
return|return
operator|new
name|JSONAlterTableMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|before
argument_list|,
name|after
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DropTableMessage
name|buildDropTableMessage
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
return|return
operator|new
name|JSONDropTableMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AddPartitionMessage
name|buildAddPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Iterator
argument_list|<
name|Partition
argument_list|>
name|partitionsIterator
parameter_list|,
name|Iterator
argument_list|<
name|PartitionFiles
argument_list|>
name|partitionFileIter
parameter_list|)
block|{
return|return
operator|new
name|JSONAddPartitionMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|table
argument_list|,
name|partitionsIterator
argument_list|,
name|partitionFileIter
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AlterPartitionMessage
name|buildAlterPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|before
parameter_list|,
name|Partition
name|after
parameter_list|)
block|{
return|return
operator|new
name|JSONAlterPartitionMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|table
argument_list|,
name|before
argument_list|,
name|after
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DropPartitionMessage
name|buildDropPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Iterator
argument_list|<
name|Partition
argument_list|>
name|partitionsIterator
parameter_list|)
block|{
return|return
operator|new
name|JSONDropPartitionMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|table
argument_list|,
name|getPartitionKeyValues
argument_list|(
name|table
argument_list|,
name|partitionsIterator
argument_list|)
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CreateFunctionMessage
name|buildCreateFunctionMessage
parameter_list|(
name|Function
name|fn
parameter_list|)
block|{
return|return
operator|new
name|JSONCreateFunctionMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|fn
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DropFunctionMessage
name|buildDropFunctionMessage
parameter_list|(
name|Function
name|fn
parameter_list|)
block|{
return|return
operator|new
name|JSONDropFunctionMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|fn
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CreateIndexMessage
name|buildCreateIndexMessage
parameter_list|(
name|Index
name|idx
parameter_list|)
block|{
return|return
operator|new
name|JSONCreateIndexMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|idx
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DropIndexMessage
name|buildDropIndexMessage
parameter_list|(
name|Index
name|idx
parameter_list|)
block|{
return|return
operator|new
name|JSONDropIndexMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|idx
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AlterIndexMessage
name|buildAlterIndexMessage
parameter_list|(
name|Index
name|before
parameter_list|,
name|Index
name|after
parameter_list|)
block|{
return|return
operator|new
name|JSONAlterIndexMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|before
argument_list|,
name|after
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InsertMessage
name|buildInsertMessage
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|table
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partKeyVals
parameter_list|,
name|boolean
name|replace
parameter_list|,
name|Iterator
argument_list|<
name|String
argument_list|>
name|fileIter
parameter_list|)
block|{
return|return
operator|new
name|JSONInsertMessage
argument_list|(
name|MS_SERVER_URL
argument_list|,
name|MS_SERVICE_PRINCIPAL
argument_list|,
name|db
argument_list|,
name|table
argument_list|,
name|partKeyVals
argument_list|,
name|replace
argument_list|,
name|fileIter
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|long
name|now
parameter_list|()
block|{
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
return|;
block|}
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartitionKeyValues
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionKeys
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|table
operator|.
name|getPartitionKeysSize
argument_list|()
condition|;
operator|++
name|i
control|)
name|partitionKeys
operator|.
name|put
argument_list|(
name|table
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
name|partition
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|partitionKeys
return|;
block|}
specifier|static
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|getPartitionKeyValues
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|,
name|Iterator
argument_list|<
name|Partition
argument_list|>
name|iterator
parameter_list|)
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|Iterators
operator|.
name|transform
argument_list|(
name|iterator
argument_list|,
operator|new
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|Partition
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|apply
parameter_list|(
annotation|@
name|Nullable
name|Partition
name|partition
parameter_list|)
block|{
return|return
name|getPartitionKeyValues
argument_list|(
name|table
argument_list|,
name|partition
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
return|;
block|}
specifier|static
name|String
name|createTableObjJson
parameter_list|(
name|Table
name|tableObj
parameter_list|)
throws|throws
name|TException
block|{
name|TSerializer
name|serializer
init|=
operator|new
name|TSerializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|serializer
operator|.
name|toString
argument_list|(
name|tableObj
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
specifier|static
name|String
name|createPartitionObjJson
parameter_list|(
name|Partition
name|partitionObj
parameter_list|)
throws|throws
name|TException
block|{
name|TSerializer
name|serializer
init|=
operator|new
name|TSerializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|serializer
operator|.
name|toString
argument_list|(
name|partitionObj
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
specifier|static
name|String
name|createFunctionObjJson
parameter_list|(
name|Function
name|functionObj
parameter_list|)
throws|throws
name|TException
block|{
name|TSerializer
name|serializer
init|=
operator|new
name|TSerializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|serializer
operator|.
name|toString
argument_list|(
name|functionObj
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
specifier|static
name|String
name|createIndexObjJson
parameter_list|(
name|Index
name|indexObj
parameter_list|)
throws|throws
name|TException
block|{
name|TSerializer
name|serializer
init|=
operator|new
name|TSerializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|serializer
operator|.
name|toString
argument_list|(
name|indexObj
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ObjectNode
name|getJsonTree
parameter_list|(
name|NotificationEvent
name|event
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|getJsonTree
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ObjectNode
name|getJsonTree
parameter_list|(
name|String
name|eventMessage
parameter_list|)
throws|throws
name|Exception
block|{
name|JsonParser
name|jsonParser
init|=
operator|(
operator|new
name|JsonFactory
argument_list|()
operator|)
operator|.
name|createJsonParser
argument_list|(
name|eventMessage
argument_list|)
decl_stmt|;
name|ObjectMapper
name|mapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|jsonParser
argument_list|,
name|ObjectNode
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Table
name|getTableObj
parameter_list|(
name|ObjectNode
name|jsonTree
parameter_list|)
throws|throws
name|Exception
block|{
name|TDeserializer
name|deSerializer
init|=
operator|new
name|TDeserializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|tableObj
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|String
name|tableJson
init|=
name|jsonTree
operator|.
name|get
argument_list|(
literal|"tableObjJson"
argument_list|)
operator|.
name|asText
argument_list|()
decl_stmt|;
name|deSerializer
operator|.
name|deserialize
argument_list|(
name|tableObj
argument_list|,
name|tableJson
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
return|return
name|tableObj
return|;
block|}
comment|/*    * TODO: Some thoughts here : We have a current todo to move some of these methods over to    * MessageFactory instead of being here, so we can override them, but before we move them over,    * we should keep the following in mind:    *    * a) We should return Iterables, not Lists. That makes sure that we can be memory-safe when    * implementing it rather than forcing ourselves down a path wherein returning List is part of    * our interface, and then people use .size() or somesuch which makes us need to materialize    * the entire list and not change. Also, returning Iterables allows us to do things like    * Iterables.transform for some of these.    * b) We should not have "magic" names like "tableObjJson", because that breaks expectation of a    * couple of things - firstly, that of serialization format, although that is fine for this    * JSONMessageFactory, and secondly, that makes us just have a number of mappings, one for each    * obj type, and sometimes, as the case is with alter, have multiples. Also, any event-specific    * item belongs in that event message / event itself, as opposed to in the factory. It's okay to    * have utility accessor methods here that are used by each of the messages to provide accessors.    * I'm adding a couple of those here.    *    */
specifier|public
specifier|static
name|TBase
name|getTObj
parameter_list|(
name|String
name|tSerialized
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TBase
argument_list|>
name|objClass
parameter_list|)
throws|throws
name|Exception
block|{
name|TBase
name|obj
init|=
name|objClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|thriftDeSerializer
operator|.
name|deserialize
argument_list|(
name|obj
argument_list|,
name|tSerialized
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
return|return
name|obj
return|;
block|}
specifier|public
specifier|static
name|Iterable
argument_list|<
name|?
extends|extends
name|TBase
argument_list|>
name|getTObjs
parameter_list|(
name|Iterable
argument_list|<
name|String
argument_list|>
name|objRefStrs
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|TBase
argument_list|>
name|objClass
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
return|return
name|Iterables
operator|.
name|transform
argument_list|(
name|objRefStrs
argument_list|,
operator|new
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|String
argument_list|,
name|TBase
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TBase
name|apply
parameter_list|(
annotation|@
name|Nullable
name|String
name|objStr
parameter_list|)
block|{
try|try
block|{
return|return
name|getTObj
argument_list|(
name|objStr
argument_list|,
name|objClass
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|re
parameter_list|)
block|{
comment|// We have to add this bit of exception handling here, because Function.apply does not allow us to throw
comment|// the actual exception that might be a checked exception, so we wind up needing to throw a RuntimeException
comment|// with the previously thrown exception as its cause. However, since RuntimeException.getCause() returns
comment|// a throwable instead of an Exception, we have to account for the possibility that the underlying code
comment|// might have thrown a Throwable that we wrapped instead, in which case, continuing to throw the
comment|// RuntimeException is the best thing we can do.
name|Throwable
name|t
init|=
name|re
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|t
operator|instanceof
name|Exception
condition|)
block|{
throw|throw
operator|(
name|Exception
operator|)
name|t
throw|;
block|}
else|else
block|{
throw|throw
name|re
throw|;
block|}
block|}
block|}
comment|// If we do not need this format of accessor using ObjectNode, this is a candidate for removal as well
specifier|public
specifier|static
name|Iterable
argument_list|<
name|?
extends|extends
name|TBase
argument_list|>
name|getTObjs
parameter_list|(
name|ObjectNode
name|jsonTree
parameter_list|,
name|String
name|objRefListName
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|TBase
argument_list|>
name|objClass
parameter_list|)
throws|throws
name|Exception
block|{
name|Iterable
argument_list|<
name|JsonNode
argument_list|>
name|jsonArrayIterator
init|=
name|jsonTree
operator|.
name|get
argument_list|(
name|objRefListName
argument_list|)
decl_stmt|;
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|JsonNode
argument_list|,
name|String
argument_list|>
name|textExtractor
init|=
operator|new
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|JsonNode
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|apply
parameter_list|(
annotation|@
name|Nullable
name|JsonNode
name|input
parameter_list|)
block|{
return|return
name|input
operator|.
name|asText
argument_list|()
return|;
block|}
block|}
decl_stmt|;
return|return
name|getTObjs
argument_list|(
name|Iterables
operator|.
name|transform
argument_list|(
name|jsonArrayIterator
argument_list|,
name|textExtractor
argument_list|)
argument_list|,
name|objClass
argument_list|)
return|;
block|}
block|}
end_class

end_unit

