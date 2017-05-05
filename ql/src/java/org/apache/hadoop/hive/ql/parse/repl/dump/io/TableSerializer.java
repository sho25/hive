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
name|ql
operator|.
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|io
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
name|TableType
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
name|ql
operator|.
name|ErrorMsg
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
name|ql
operator|.
name|metadata
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
name|ql
operator|.
name|parse
operator|.
name|ReplicationSpec
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
name|ql
operator|.
name|parse
operator|.
name|SemanticException
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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|TableSerializer
implements|implements
name|JsonWriter
operator|.
name|Serializer
block|{
specifier|public
specifier|static
specifier|final
name|String
name|FIELD_NAME
init|=
literal|"table"
decl_stmt|;
specifier|private
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|Table
name|tableHandle
decl_stmt|;
specifier|private
specifier|final
name|Iterable
argument_list|<
name|Partition
argument_list|>
name|partitions
decl_stmt|;
specifier|public
name|TableSerializer
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|Table
name|tableHandle
parameter_list|,
name|Iterable
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
block|{
name|this
operator|.
name|tableHandle
operator|=
name|tableHandle
expr_stmt|;
name|this
operator|.
name|partitions
operator|=
name|partitions
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|JsonWriter
name|writer
parameter_list|,
name|ReplicationSpec
name|additionalPropertiesProvider
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|IOException
block|{
if|if
condition|(
name|cannotReplicateTable
argument_list|(
name|additionalPropertiesProvider
argument_list|)
condition|)
block|{
return|return;
block|}
name|Table
name|tTable
init|=
name|tableHandle
operator|.
name|getTTable
argument_list|()
decl_stmt|;
name|tTable
operator|=
name|addPropertiesToTable
argument_list|(
name|tTable
argument_list|,
name|additionalPropertiesProvider
argument_list|)
expr_stmt|;
try|try
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
name|writer
operator|.
name|jsonGenerator
operator|.
name|writeStringField
argument_list|(
name|FIELD_NAME
argument_list|,
name|serializer
operator|.
name|toString
argument_list|(
name|tTable
argument_list|,
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|jsonGenerator
operator|.
name|writeFieldName
argument_list|(
name|PartitionSerializer
operator|.
name|FIELD_NAME
argument_list|)
expr_stmt|;
name|writePartitions
argument_list|(
name|writer
argument_list|,
name|additionalPropertiesProvider
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
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|ERROR_SERIALIZE_METASTORE
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|cannotReplicateTable
parameter_list|(
name|ReplicationSpec
name|additionalPropertiesProvider
parameter_list|)
block|{
return|return
name|tableHandle
operator|==
literal|null
operator|||
name|additionalPropertiesProvider
operator|.
name|isNoop
argument_list|()
return|;
block|}
specifier|private
name|Table
name|addPropertiesToTable
parameter_list|(
name|Table
name|table
parameter_list|,
name|ReplicationSpec
name|additionalPropertiesProvider
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|IOException
block|{
if|if
condition|(
name|additionalPropertiesProvider
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
name|table
operator|.
name|putToParameters
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|,
name|additionalPropertiesProvider
operator|.
name|getCurrentReplicationState
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isExternalTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
comment|// Replication destination will not be external - override if set
name|table
operator|.
name|putToParameters
argument_list|(
literal|"EXTERNAL"
argument_list|,
literal|"FALSE"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isExternalTableType
argument_list|(
name|table
argument_list|)
condition|)
block|{
comment|// Replication dest will not be external - override if set
name|table
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// ReplicationSpec.KEY scopeKey = ReplicationSpec.KEY.REPL_SCOPE;
comment|// write(out, ",\""+ scopeKey.toString() +"\":\"" + replicationSpec.get(scopeKey) + "\"");
comment|// TODO: if we want to be explicit about this dump not being a replication dump, we can
comment|// uncomment this else section, but currently unnneeded. Will require a lot of golden file
comment|// regen if we do so.
block|}
return|return
name|table
return|;
block|}
specifier|private
name|boolean
name|isExternalTableType
parameter_list|(
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
name|table
parameter_list|)
block|{
return|return
name|table
operator|.
name|isSetTableType
argument_list|()
operator|&&
name|table
operator|.
name|getTableType
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|isExternalTable
parameter_list|(
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
name|table
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|table
operator|.
name|getParameters
argument_list|()
decl_stmt|;
return|return
name|params
operator|.
name|containsKey
argument_list|(
literal|"EXTERNAL"
argument_list|)
operator|&&
name|params
operator|.
name|get
argument_list|(
literal|"EXTERNAL"
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"TRUE"
argument_list|)
return|;
block|}
specifier|private
name|void
name|writePartitions
parameter_list|(
name|JsonWriter
name|writer
parameter_list|,
name|ReplicationSpec
name|additionalPropertiesProvider
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|IOException
block|{
name|writer
operator|.
name|jsonGenerator
operator|.
name|writeStartArray
argument_list|()
expr_stmt|;
if|if
condition|(
name|partitions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|Partition
name|partition
range|:
name|partitions
control|)
block|{
operator|new
name|PartitionSerializer
argument_list|(
name|partition
operator|.
name|getTPartition
argument_list|()
argument_list|)
operator|.
name|writeTo
argument_list|(
name|writer
argument_list|,
name|additionalPropertiesProvider
argument_list|)
expr_stmt|;
block|}
block|}
name|writer
operator|.
name|jsonGenerator
operator|.
name|writeEndArray
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

