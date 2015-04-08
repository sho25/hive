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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|partition
operator|.
name|spec
operator|.
name|PartitionSpecProxy
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
name|AddPartitionMessage
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
name|AlterPartitionMessage
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
name|AlterTableMessage
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
name|CreateDatabaseMessage
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
name|CreateTableMessage
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
name|DropDatabaseMessage
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
name|DropPartitionMessage
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
name|DropTableMessage
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
name|InsertMessage
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
name|MessageDeserializer
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
name|MessageFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * The JSON implementation of the MessageFactory. Constructs JSON implementations of  * each message-type.  */
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
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
name|getVersion
parameter_list|()
block|{
return|return
literal|"0.1"
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
literal|"json"
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
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
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
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
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
parameter_list|)
block|{
return|return
operator|new
name|JSONCreateTableMessage
argument_list|(
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
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
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
argument_list|,
name|before
operator|.
name|getDbName
argument_list|()
argument_list|,
name|before
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
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
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
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
block|{
return|return
operator|new
name|JSONAddPartitionMessage
argument_list|(
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
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
name|getPartitionKeyValues
argument_list|(
name|table
argument_list|,
name|partitions
argument_list|)
argument_list|,
name|now
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
literal|"Hive"
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
name|AddPartitionMessage
name|buildAddPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|PartitionSpecProxy
name|partitionSpec
parameter_list|)
block|{
return|return
operator|new
name|JSONAddPartitionMessage
argument_list|(
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
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
name|getPartitionKeyValues
argument_list|(
name|table
argument_list|,
name|partitionSpec
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
name|AlterPartitionMessage
name|buildAlterPartitionMessage
parameter_list|(
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
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
argument_list|,
name|before
operator|.
name|getDbName
argument_list|()
argument_list|,
name|before
operator|.
name|getTableName
argument_list|()
argument_list|,
name|before
operator|.
name|getValues
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
name|DropPartitionMessage
name|buildDropPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|)
block|{
return|return
operator|new
name|JSONDropPartitionMessage
argument_list|(
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
argument_list|,
name|partition
operator|.
name|getDbName
argument_list|()
argument_list|,
name|partition
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|getPartitionKeyValues
argument_list|(
name|table
argument_list|,
name|partition
argument_list|)
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
name|InsertMessage
name|buildInsertMessage
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|files
parameter_list|)
block|{
return|return
operator|new
name|JSONInsertMessage
argument_list|(
name|HCAT_SERVER_URL
argument_list|,
name|HCAT_SERVICE_PRINCIPAL
argument_list|,
name|db
argument_list|,
name|table
argument_list|,
name|partVals
argument_list|,
name|files
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
specifier|private
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
specifier|private
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
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitionList
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|(
name|partitions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Partition
name|partition
range|:
name|partitions
control|)
name|partitionList
operator|.
name|add
argument_list|(
name|getPartitionKeyValues
argument_list|(
name|table
argument_list|,
name|partition
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|partitionList
return|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
literal|"Hive"
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|private
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
name|Table
name|table
parameter_list|,
name|PartitionSpecProxy
name|partitionSpec
parameter_list|)
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitionList
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|PartitionSpecProxy
operator|.
name|PartitionIterator
name|iterator
init|=
name|partitionSpec
operator|.
name|getPartitionIterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Partition
name|partition
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|partitionList
operator|.
name|add
argument_list|(
name|getPartitionKeyValues
argument_list|(
name|table
argument_list|,
name|partition
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|partitionList
return|;
block|}
block|}
end_class

end_unit

