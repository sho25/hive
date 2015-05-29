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
name|api
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
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
name|MetaStoreUtils
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
name|Warehouse
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
name|FieldSchema
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
name|MetaException
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
name|Order
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
name|StorageDescriptor
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
name|common
operator|.
name|HCatException
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
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
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
name|data
operator|.
name|schema
operator|.
name|HCatSchemaUtils
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

begin_comment
comment|/**  * The HCatPartition is a wrapper around org.apache.hadoop.hive.metastore.api.Partition.  */
end_comment

begin_class
specifier|public
class|class
name|HCatPartition
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
name|HCatPartition
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HCatTable
name|hcatTable
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|String
name|dbName
init|=
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|values
decl_stmt|;
specifier|private
name|int
name|createTime
decl_stmt|;
specifier|private
name|int
name|lastAccessTime
decl_stmt|;
specifier|private
name|StorageDescriptor
name|sd
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|columns
decl_stmt|;
comment|// Cache column-list from this.sd.
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
decl_stmt|;
comment|// For use from within HCatClient.getPartitions().
name|HCatPartition
parameter_list|(
name|HCatTable
name|hcatTable
parameter_list|,
name|Partition
name|partition
parameter_list|)
throws|throws
name|HCatException
block|{
name|this
operator|.
name|hcatTable
operator|=
name|hcatTable
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|partition
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|partition
operator|.
name|getDbName
argument_list|()
expr_stmt|;
name|this
operator|.
name|createTime
operator|=
name|partition
operator|.
name|getCreateTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|lastAccessTime
operator|=
name|partition
operator|.
name|getLastAccessTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|parameters
operator|=
name|partition
operator|.
name|getParameters
argument_list|()
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|partition
operator|.
name|getValues
argument_list|()
expr_stmt|;
if|if
condition|(
name|hcatTable
operator|!=
literal|null
operator|&&
name|partition
operator|.
name|getValuesSize
argument_list|()
operator|!=
name|hcatTable
operator|.
name|getPartCols
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Mismatched number of partition columns between table:"
operator|+
name|hcatTable
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|hcatTable
operator|.
name|getTableName
argument_list|()
operator|+
literal|" and partition "
operator|+
name|partition
operator|.
name|getValues
argument_list|()
argument_list|)
throw|;
block|}
name|this
operator|.
name|sd
operator|=
name|partition
operator|.
name|getSd
argument_list|()
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|getColumns
argument_list|(
name|this
operator|.
name|sd
argument_list|)
expr_stmt|;
block|}
comment|// For constructing HCatPartitions afresh, as an argument to HCatClient.addPartitions().
specifier|public
name|HCatPartition
parameter_list|(
name|HCatTable
name|hcatTable
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionKeyValues
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|HCatException
block|{
name|this
operator|.
name|hcatTable
operator|=
name|hcatTable
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|hcatTable
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|hcatTable
operator|.
name|getDbName
argument_list|()
expr_stmt|;
name|this
operator|.
name|sd
operator|=
operator|new
name|StorageDescriptor
argument_list|(
name|hcatTable
operator|.
name|getSd
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|sd
operator|.
name|setLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|getColumns
argument_list|(
name|this
operator|.
name|sd
argument_list|)
expr_stmt|;
name|this
operator|.
name|createTime
operator|=
call|(
name|int
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastAccessTime
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|values
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|hcatTable
operator|.
name|getPartCols
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HCatFieldSchema
name|partField
range|:
name|hcatTable
operator|.
name|getPartCols
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|partitionKeyValues
operator|.
name|containsKey
argument_list|(
name|partField
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Missing value for partition-key \'"
operator|+
name|partField
operator|.
name|getName
argument_list|()
operator|+
literal|"\' in table: "
operator|+
name|hcatTable
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|hcatTable
operator|.
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|values
operator|.
name|add
argument_list|(
name|partitionKeyValues
operator|.
name|get
argument_list|(
name|partField
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// For replicating an HCatPartition definition.
specifier|public
name|HCatPartition
parameter_list|(
name|HCatPartition
name|rhs
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionKeyValues
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|HCatException
block|{
name|this
operator|.
name|hcatTable
operator|=
name|rhs
operator|.
name|hcatTable
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|rhs
operator|.
name|tableName
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|rhs
operator|.
name|dbName
expr_stmt|;
name|this
operator|.
name|sd
operator|=
operator|new
name|StorageDescriptor
argument_list|(
name|rhs
operator|.
name|sd
argument_list|)
expr_stmt|;
name|this
operator|.
name|sd
operator|.
name|setLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|getColumns
argument_list|(
name|this
operator|.
name|sd
argument_list|)
expr_stmt|;
name|this
operator|.
name|createTime
operator|=
call|(
name|int
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastAccessTime
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|values
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|hcatTable
operator|.
name|getPartCols
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HCatFieldSchema
name|partField
range|:
name|hcatTable
operator|.
name|getPartCols
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|partitionKeyValues
operator|.
name|containsKey
argument_list|(
name|partField
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Missing value for partition-key \'"
operator|+
name|partField
operator|.
name|getName
argument_list|()
operator|+
literal|"\' in table: "
operator|+
name|hcatTable
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|hcatTable
operator|.
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|values
operator|.
name|add
argument_list|(
name|partitionKeyValues
operator|.
name|get
argument_list|(
name|partField
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|getColumns
parameter_list|(
name|StorageDescriptor
name|sd
parameter_list|)
throws|throws
name|HCatException
block|{
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|(
name|sd
operator|.
name|getColsSize
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FieldSchema
name|fieldSchema
range|:
name|sd
operator|.
name|getCols
argument_list|()
control|)
block|{
name|columns
operator|.
name|add
argument_list|(
name|HCatSchemaUtils
operator|.
name|getHCatFieldSchema
argument_list|(
name|fieldSchema
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|columns
return|;
block|}
comment|// For use from HCatClient.addPartitions(), to construct from user-input.
name|Partition
name|toHivePartition
parameter_list|()
throws|throws
name|HCatException
block|{
name|Partition
name|hivePtn
init|=
operator|new
name|Partition
argument_list|()
decl_stmt|;
name|hivePtn
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|hivePtn
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|hivePtn
operator|.
name|setValues
argument_list|(
name|values
argument_list|)
expr_stmt|;
name|hivePtn
operator|.
name|setParameters
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
if|if
condition|(
name|sd
operator|.
name|getLocation
argument_list|()
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Partition location is not set! Attempting to construct default partition location."
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|partName
init|=
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|HCatSchemaUtils
operator|.
name|getFieldSchemas
argument_list|(
name|hcatTable
operator|.
name|getPartCols
argument_list|()
argument_list|)
argument_list|,
name|values
argument_list|)
decl_stmt|;
name|sd
operator|.
name|setLocation
argument_list|(
operator|new
name|Path
argument_list|(
name|hcatTable
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
name|partName
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Could not construct default partition-path for "
operator|+
name|hcatTable
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|hcatTable
operator|.
name|getTableName
argument_list|()
operator|+
literal|"["
operator|+
name|values
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
name|hivePtn
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|hivePtn
operator|.
name|setCreateTime
argument_list|(
call|(
name|int
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
name|hivePtn
operator|.
name|setLastAccessTimeIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|hivePtn
return|;
block|}
specifier|public
name|HCatTable
name|hcatTable
parameter_list|()
block|{
return|return
name|hcatTable
return|;
block|}
specifier|public
name|HCatPartition
name|hcatTable
parameter_list|(
name|HCatTable
name|hcatTable
parameter_list|)
block|{
name|this
operator|.
name|hcatTable
operator|=
name|hcatTable
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|hcatTable
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|hcatTable
operator|.
name|getDbName
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Gets the table name.    *    * @return the table name    */
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableName
return|;
block|}
comment|/**    * Gets the database name.    *    * @return the database name    */
specifier|public
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|this
operator|.
name|dbName
return|;
block|}
comment|/**    * Gets the columns of the table.    *    * @return the columns    */
specifier|public
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|getColumns
parameter_list|()
block|{
return|return
name|columns
return|;
block|}
comment|/**    * Gets the partition columns of the table.    *    * @return the partition columns    */
specifier|public
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|getPartColumns
parameter_list|()
block|{
return|return
name|hcatTable
operator|.
name|getPartCols
argument_list|()
return|;
block|}
comment|/**    * Gets the input format.    *    * @return the input format    */
specifier|public
name|String
name|getInputFormat
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getInputFormat
argument_list|()
return|;
block|}
comment|/**    * Gets the output format.    *    * @return the output format    */
specifier|public
name|String
name|getOutputFormat
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getOutputFormat
argument_list|()
return|;
block|}
comment|/**    * Gets the storage handler.    *    * @return the storage handler    */
specifier|public
name|String
name|getStorageHandler
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
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
name|hive_metastoreConstants
operator|.
name|META_TABLE_STORAGE
argument_list|)
return|;
block|}
comment|/**    * Gets the location.    *    * @return the location    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getLocation
argument_list|()
return|;
block|}
comment|/**    * Setter for partition directory location.    */
specifier|public
name|HCatPartition
name|location
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|sd
operator|.
name|setLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Gets the serde.    *    * @return the serde    */
specifier|public
name|String
name|getSerDe
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getSerializationLib
argument_list|()
return|;
block|}
comment|/**    * Getter for SerDe parameters.    * @return The SerDe parameters.    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSerdeParams
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
return|;
block|}
specifier|public
name|HCatPartition
name|parameters
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
name|this
operator|.
name|parameters
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|parameters
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|this
operator|.
name|parameters
operator|.
name|equals
argument_list|(
name|parameters
argument_list|)
condition|)
block|{
name|this
operator|.
name|parameters
operator|.
name|clear
argument_list|()
expr_stmt|;
name|this
operator|.
name|parameters
operator|.
name|putAll
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
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
name|this
operator|.
name|parameters
return|;
block|}
comment|/**    * Gets the last access time.    *    * @return the last access time    */
specifier|public
name|int
name|getLastAccessTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|lastAccessTime
return|;
block|}
comment|/**    * Gets the creates the time.    *    * @return the creates the time    */
specifier|public
name|int
name|getCreateTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|createTime
return|;
block|}
comment|/**    * Gets the values.    *    * @return the values    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getValues
parameter_list|()
block|{
return|return
name|this
operator|.
name|values
return|;
block|}
comment|/**    * Getter for partition-spec map.    */
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartitionKeyValMap
parameter_list|()
block|{
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|hcatTable
operator|.
name|getPartCols
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
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
name|hcatTable
operator|.
name|getPartCols
argument_list|()
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|hcatTable
operator|.
name|getPartCols
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
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
comment|/**    * Setter for partition key-values.    */
specifier|public
name|HCatPartition
name|setPartitionKeyValues
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionKeyValues
parameter_list|)
throws|throws
name|HCatException
block|{
for|for
control|(
name|HCatFieldSchema
name|partField
range|:
name|hcatTable
operator|.
name|getPartCols
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|partitionKeyValues
operator|.
name|containsKey
argument_list|(
name|partField
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Missing value for partition-key \'"
operator|+
name|partField
operator|.
name|getName
argument_list|()
operator|+
literal|"\' in table: "
operator|+
name|hcatTable
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|hcatTable
operator|.
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|values
operator|.
name|add
argument_list|(
name|partitionKeyValues
operator|.
name|get
argument_list|(
name|partField
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Keep partKeyValMap in synch as well.
block|}
block|}
return|return
name|this
return|;
block|}
comment|/**    * Gets the bucket columns.    *    * @return the bucket columns    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getBucketCols
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getBucketCols
argument_list|()
return|;
block|}
comment|/**    * Gets the number of buckets.    *    * @return the number of buckets    */
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getNumBuckets
argument_list|()
return|;
block|}
comment|/**    * Gets the sort columns.    *    * @return the sort columns    */
specifier|public
name|List
argument_list|<
name|Order
argument_list|>
name|getSortCols
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getSortCols
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"HCatPartition [ "
operator|+
literal|"tableName="
operator|+
name|tableName
operator|+
literal|","
operator|+
literal|"dbName="
operator|+
name|dbName
operator|+
literal|","
operator|+
literal|"values="
operator|+
name|values
operator|+
literal|","
operator|+
literal|"createTime="
operator|+
name|createTime
operator|+
literal|","
operator|+
literal|"lastAccessTime="
operator|+
name|lastAccessTime
operator|+
literal|","
operator|+
literal|"sd="
operator|+
name|sd
operator|+
literal|","
operator|+
literal|"parameters="
operator|+
name|parameters
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

