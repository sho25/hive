begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|index
operator|.
name|compact
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
operator|.
name|Entry
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
name|conf
operator|.
name|Configuration
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
name|StorageDescriptor
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
name|Driver
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
name|exec
operator|.
name|Task
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
name|exec
operator|.
name|Utilities
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
name|index
operator|.
name|AbstractIndexHandler
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
name|Hive
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
name|HiveException
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
name|HiveUtils
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
name|metadata
operator|.
name|VirtualColumn
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|plan
operator|.
name|PartitionDesc
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
name|TableDesc
import|;
end_import

begin_class
specifier|public
class|class
name|CompactIndexHandler
extends|extends
name|AbstractIndexHandler
block|{
specifier|private
name|Configuration
name|configuration
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|analyzeIndexDefinition
parameter_list|(
name|Table
name|baseTable
parameter_list|,
name|Index
name|index
parameter_list|,
name|Table
name|indexTable
parameter_list|)
throws|throws
name|HiveException
block|{
name|StorageDescriptor
name|storageDesc
init|=
name|index
operator|.
name|getSd
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|usesIndexTable
argument_list|()
operator|&&
name|indexTable
operator|!=
literal|null
condition|)
block|{
name|StorageDescriptor
name|indexTableSd
init|=
name|storageDesc
operator|.
name|clone
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|indexTblCols
init|=
name|indexTableSd
operator|.
name|getCols
argument_list|()
decl_stmt|;
name|FieldSchema
name|bucketFileName
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"_bucketname"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|indexTblCols
operator|.
name|add
argument_list|(
name|bucketFileName
argument_list|)
expr_stmt|;
name|FieldSchema
name|offSets
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"_offsets"
argument_list|,
literal|"array<bigint>"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|indexTblCols
operator|.
name|add
argument_list|(
name|offSets
argument_list|)
expr_stmt|;
name|indexTable
operator|.
name|setSd
argument_list|(
name|indexTableSd
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|generateIndexBuildTaskList
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
name|baseTbl
parameter_list|,
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
name|index
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|indexTblPartitions
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|baseTblPartitions
parameter_list|,
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
name|indexTbl
parameter_list|,
name|Hive
name|db
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|TableDesc
name|desc
init|=
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|indexTbl
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|newBaseTblPartitions
init|=
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|indexBuilderTasks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|baseTbl
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
comment|// the table does not have any partition, then create index for the
comment|// whole table
name|Task
argument_list|<
name|?
argument_list|>
name|indexBuilder
init|=
name|getIndexBuilderMapRedTask
argument_list|(
name|index
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|,
literal|false
argument_list|,
operator|new
name|PartitionDesc
argument_list|(
name|desc
argument_list|,
literal|null
argument_list|)
argument_list|,
name|indexTbl
operator|.
name|getTableName
argument_list|()
argument_list|,
operator|new
name|PartitionDesc
argument_list|(
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|baseTbl
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|,
name|baseTbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|db
argument_list|,
name|indexTbl
operator|.
name|getDbName
argument_list|()
argument_list|)
decl_stmt|;
name|indexBuilderTasks
operator|.
name|add
argument_list|(
name|indexBuilder
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// check whether the index table partitions are still exists in base
comment|// table
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indexTblPartitions
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Partition
name|indexPart
init|=
name|indexTblPartitions
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Partition
name|basePart
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|baseTblPartitions
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|baseTblPartitions
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|indexPart
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|basePart
operator|=
name|baseTblPartitions
operator|.
name|get
argument_list|(
name|j
argument_list|)
expr_stmt|;
name|newBaseTblPartitions
operator|.
name|add
argument_list|(
name|baseTblPartitions
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|basePart
operator|==
literal|null
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Partitions of base table and index table are inconsistent."
argument_list|)
throw|;
comment|// for each partition, spawn a map reduce task.
name|Task
argument_list|<
name|?
argument_list|>
name|indexBuilder
init|=
name|getIndexBuilderMapRedTask
argument_list|(
name|index
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|,
literal|true
argument_list|,
operator|new
name|PartitionDesc
argument_list|(
name|indexPart
argument_list|)
argument_list|,
name|indexTbl
operator|.
name|getTableName
argument_list|()
argument_list|,
operator|new
name|PartitionDesc
argument_list|(
name|basePart
argument_list|)
argument_list|,
name|baseTbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|db
argument_list|,
name|indexTbl
operator|.
name|getDbName
argument_list|()
argument_list|)
decl_stmt|;
name|indexBuilderTasks
operator|.
name|add
argument_list|(
name|indexBuilder
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|indexBuilderTasks
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
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Task
argument_list|<
name|?
argument_list|>
name|getIndexBuilderMapRedTask
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|indexField
parameter_list|,
name|boolean
name|partitioned
parameter_list|,
name|PartitionDesc
name|indexTblPartDesc
parameter_list|,
name|String
name|indexTableName
parameter_list|,
name|PartitionDesc
name|baseTablePartDesc
parameter_list|,
name|String
name|baseTableName
parameter_list|,
name|Hive
name|db
parameter_list|,
name|String
name|dbName
parameter_list|)
block|{
name|String
name|indexCols
init|=
name|MetaStoreUtils
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|indexField
argument_list|)
decl_stmt|;
comment|//form a new insert overwrite query.
name|StringBuilder
name|command
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|indexTblPartDesc
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|"INSERT OVERWRITE TABLE "
operator|+
name|indexTableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|partitioned
operator|&&
name|indexTblPartDesc
operator|!=
literal|null
condition|)
block|{
name|command
operator|.
name|append
argument_list|(
literal|" PARTITION ( "
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|ret
init|=
name|getPartKVPairStringArray
argument_list|(
name|partSpec
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
name|ret
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|partKV
init|=
name|ret
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|command
operator|.
name|append
argument_list|(
name|partKV
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|<
name|ret
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|command
operator|.
name|append
argument_list|(
literal|" ) "
argument_list|)
expr_stmt|;
block|}
name|command
operator|.
name|append
argument_list|(
literal|" SELECT "
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|indexCols
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|VirtualColumn
operator|.
name|FILENAME
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|" collect_set ("
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|VirtualColumn
operator|.
name|BLOCKOFFSET
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|") "
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|" FROM "
operator|+
name|baseTableName
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|basePartSpec
init|=
name|baseTablePartDesc
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
if|if
condition|(
name|basePartSpec
operator|!=
literal|null
condition|)
block|{
name|command
operator|.
name|append
argument_list|(
literal|" WHERE "
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|pkv
init|=
name|getPartKVPairStringArray
argument_list|(
name|basePartSpec
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
name|pkv
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|partKV
init|=
name|pkv
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|command
operator|.
name|append
argument_list|(
name|partKV
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|<
name|pkv
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
name|command
operator|.
name|append
argument_list|(
literal|" AND "
argument_list|)
expr_stmt|;
block|}
block|}
name|command
operator|.
name|append
argument_list|(
literal|" GROUP BY "
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|indexCols
operator|+
literal|", "
operator|+
name|VirtualColumn
operator|.
name|FILENAME
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|" SORT BY "
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|indexCols
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|db
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|driver
operator|.
name|compile
argument_list|(
name|command
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|rootTask
init|=
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getRootTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|IndexMetadataChangeWork
name|indexMetaChange
init|=
operator|new
name|IndexMetadataChangeWork
argument_list|(
name|partSpec
argument_list|,
name|indexTableName
argument_list|,
name|dbName
argument_list|)
decl_stmt|;
name|IndexMetadataChangeTask
name|indexMetaChangeTsk
init|=
operator|new
name|IndexMetadataChangeTask
argument_list|()
decl_stmt|;
name|indexMetaChangeTsk
operator|.
name|setWork
argument_list|(
name|indexMetaChange
argument_list|)
expr_stmt|;
name|rootTask
operator|.
name|addDependentTask
argument_list|(
name|indexMetaChangeTsk
argument_list|)
expr_stmt|;
return|return
name|rootTask
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getPartKVPairStringArray
parameter_list|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|partSpec
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iter
init|=
name|partSpec
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|p
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|p
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" = "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|p
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|usesIndexTable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|configuration
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|configuration
operator|=
name|conf
expr_stmt|;
block|}
block|}
end_class

end_unit

