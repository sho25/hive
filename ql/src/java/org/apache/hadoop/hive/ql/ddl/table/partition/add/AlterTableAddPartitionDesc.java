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
name|ddl
operator|.
name|table
operator|.
name|partition
operator|.
name|add
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|ColumnStatistics
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
name|ddl
operator|.
name|DDLDesc
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
name|plan
operator|.
name|Explain
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
name|plan
operator|.
name|Explain
operator|.
name|Level
import|;
end_import

begin_comment
comment|/**  * DDL task description for ALTER TABLE ... ADD PARTITION ... commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Add Partition"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|AlterTableAddPartitionDesc
implements|implements
name|DDLDesc
implements|,
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * Description of a partition to add.    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Partition"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
specifier|static
class|class
name|PartitionDesc
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
comment|// TODO: make location final too
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
decl_stmt|;
specifier|private
specifier|final
name|String
name|inputFormat
decl_stmt|;
specifier|private
specifier|final
name|String
name|outputFormat
decl_stmt|;
specifier|private
specifier|final
name|int
name|numBuckets
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|columns
decl_stmt|;
specifier|private
specifier|final
name|String
name|serializationLib
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeParams
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|bucketColumns
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Order
argument_list|>
name|sortColumns
decl_stmt|;
specifier|private
specifier|final
name|ColumnStatistics
name|columnStats
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeId
decl_stmt|;
specifier|public
name|PartitionDesc
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
name|String
name|location
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|this
argument_list|(
name|partitionSpec
argument_list|,
name|location
argument_list|,
name|params
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|PartitionDesc
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
name|String
name|location
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|String
name|inputFormat
parameter_list|,
name|String
name|outputFormat
parameter_list|,
name|int
name|numBuckets
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|columns
parameter_list|,
name|String
name|serializationLib
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeParams
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|bucketColumns
parameter_list|,
name|List
argument_list|<
name|Order
argument_list|>
name|sortColumns
parameter_list|,
name|ColumnStatistics
name|columnStats
parameter_list|,
name|long
name|writeId
parameter_list|)
block|{
name|this
operator|.
name|partitionSpec
operator|=
name|partitionSpec
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
name|this
operator|.
name|inputFormat
operator|=
name|inputFormat
expr_stmt|;
name|this
operator|.
name|outputFormat
operator|=
name|outputFormat
expr_stmt|;
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
name|this
operator|.
name|serializationLib
operator|=
name|serializationLib
expr_stmt|;
name|this
operator|.
name|serdeParams
operator|=
name|serdeParams
expr_stmt|;
name|this
operator|.
name|bucketColumns
operator|=
name|bucketColumns
expr_stmt|;
name|this
operator|.
name|sortColumns
operator|=
name|sortColumns
expr_stmt|;
name|this
operator|.
name|columnStats
operator|=
name|columnStats
expr_stmt|;
name|this
operator|.
name|writeId
operator|=
name|writeId
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpec
parameter_list|()
block|{
return|return
name|partitionSpec
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition spec"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getPartSpecForExplain
parameter_list|()
block|{
return|return
name|partitionSpec
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * @return location of partition in relation to table      */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"location"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
specifier|public
name|void
name|setLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartParams
parameter_list|()
block|{
return|return
name|params
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"params"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getPartParamsForExplain
parameter_list|()
block|{
return|return
name|params
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"input format"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getInputFormat
parameter_list|()
block|{
return|return
name|inputFormat
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"output format"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getOutputFormat
parameter_list|()
block|{
return|return
name|outputFormat
return|;
block|}
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"num buckets"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|Integer
name|getNumBucketsExplain
parameter_list|()
block|{
return|return
name|numBuckets
operator|==
operator|-
literal|1
condition|?
literal|null
else|:
name|numBuckets
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"columns"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getCols
parameter_list|()
block|{
return|return
name|columns
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"serialization lib"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getSerializationLib
parameter_list|()
block|{
return|return
name|serializationLib
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"serde params"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
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
name|serdeParams
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"bucket columns"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getBucketCols
parameter_list|()
block|{
return|return
name|bucketColumns
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"sort columns"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|Order
argument_list|>
name|getSortCols
parameter_list|()
block|{
return|return
name|sortColumns
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"column stats"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|ColumnStatistics
name|getColStats
parameter_list|()
block|{
return|return
name|columnStats
return|;
block|}
specifier|public
name|long
name|getWriteId
parameter_list|()
block|{
return|return
name|writeId
return|;
block|}
block|}
specifier|private
specifier|final
name|String
name|dbName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|ifNotExists
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|partitions
decl_stmt|;
specifier|private
name|ReplicationSpec
name|replicationSpec
init|=
literal|null
decl_stmt|;
comment|// TODO: make replicationSpec final too
specifier|public
name|AlterTableAddPartitionDesc
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|,
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|partitions
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
name|this
operator|.
name|partitions
operator|=
name|partitions
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"db name"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table name"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"if not exists"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|boolean
name|isIfNotExists
parameter_list|()
block|{
return|return
name|ifNotExists
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partitions"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|getPartitions
parameter_list|()
block|{
return|return
name|partitions
return|;
block|}
comment|/**    * @param replicationSpec Sets the replication spec governing this create.    * This parameter will have meaningful values only for creates happening as a result of a replication.    */
specifier|public
name|void
name|setReplicationSpec
parameter_list|(
name|ReplicationSpec
name|replicationSpec
parameter_list|)
block|{
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
expr_stmt|;
block|}
comment|/**    * @return what kind of replication scope this drop is running under.    * This can result in a "CREATE/REPLACE IF NEWER THAN" kind of semantic    */
specifier|public
name|ReplicationSpec
name|getReplicationSpec
parameter_list|()
block|{
if|if
condition|(
name|replicationSpec
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|replicationSpec
operator|=
operator|new
name|ReplicationSpec
argument_list|()
expr_stmt|;
block|}
return|return
name|replicationSpec
return|;
block|}
block|}
end_class

end_unit

