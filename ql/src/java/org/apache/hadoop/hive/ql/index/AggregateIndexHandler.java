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
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Set
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
name|conf
operator|.
name|HiveConf
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|compact
operator|.
name|CompactIndexHandler
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
name|optimizer
operator|.
name|IndexUtils
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
name|session
operator|.
name|LineageState
import|;
end_import

begin_comment
comment|/**  * Index handler for indexes that have aggregate functions on indexed columns.  *  */
end_comment

begin_class
specifier|public
class|class
name|AggregateIndexHandler
extends|extends
name|CompactIndexHandler
block|{
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
name|deepCopy
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|paraList
init|=
name|index
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|paraList
operator|!=
literal|null
operator|&&
name|paraList
operator|.
name|containsKey
argument_list|(
literal|"AGGREGATES"
argument_list|)
condition|)
block|{
name|String
name|propValue
init|=
name|paraList
operator|.
name|get
argument_list|(
literal|"AGGREGATES"
argument_list|)
decl_stmt|;
if|if
condition|(
name|propValue
operator|.
name|contains
argument_list|(
literal|","
argument_list|)
condition|)
block|{
name|String
index|[]
name|aggFuncs
init|=
name|propValue
operator|.
name|split
argument_list|(
literal|","
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
name|aggFuncs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|createAggregationFunction
argument_list|(
name|indexTblCols
argument_list|,
name|aggFuncs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|createAggregationFunction
argument_list|(
name|indexTblCols
argument_list|,
name|propValue
argument_list|)
expr_stmt|;
block|}
block|}
name|indexTable
operator|.
name|setSd
argument_list|(
name|indexTableSd
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|createAggregationFunction
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|indexTblCols
parameter_list|,
name|String
name|property
parameter_list|)
block|{
name|String
index|[]
name|aggFuncCol
init|=
name|property
operator|.
name|split
argument_list|(
literal|"\\("
argument_list|)
decl_stmt|;
name|String
name|funcName
init|=
name|aggFuncCol
index|[
literal|0
index|]
decl_stmt|;
name|String
name|colName
init|=
name|aggFuncCol
index|[
literal|1
index|]
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|aggFuncCol
index|[
literal|1
index|]
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|colName
operator|.
name|contains
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
name|colName
operator|=
name|colName
operator|.
name|replace
argument_list|(
literal|"*"
argument_list|,
literal|"all"
argument_list|)
expr_stmt|;
block|}
name|FieldSchema
name|aggregationFunction
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"_"
operator|+
name|funcName
operator|+
literal|"_of_"
operator|+
name|colName
operator|+
literal|""
argument_list|,
literal|"bigint"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|indexTblCols
operator|.
name|add
argument_list|(
name|aggregationFunction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Task
argument_list|<
name|?
argument_list|>
name|getIndexBuilderMapRedTask
parameter_list|(
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|Index
name|index
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
name|String
name|dbName
parameter_list|,
name|LineageState
name|lineageState
parameter_list|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|indexField
init|=
name|index
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
decl_stmt|;
name|String
name|indexCols
init|=
name|HiveUtils
operator|.
name|getUnparsedColumnNamesFromFieldSchema
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
name|Map
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
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|indexTableName
argument_list|)
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
operator|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
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
block|{
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
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
literal|","
argument_list|)
expr_stmt|;
assert|assert
name|indexField
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|paraList
init|=
name|index
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|paraList
operator|!=
literal|null
operator|&&
name|paraList
operator|.
name|containsKey
argument_list|(
literal|"AGGREGATES"
argument_list|)
condition|)
block|{
name|command
operator|.
name|append
argument_list|(
name|paraList
operator|.
name|get
argument_list|(
literal|"AGGREGATES"
argument_list|)
operator|+
literal|" "
argument_list|)
expr_stmt|;
block|}
name|command
operator|.
name|append
argument_list|(
literal|" FROM "
operator|+
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|baseTableName
argument_list|)
argument_list|)
expr_stmt|;
name|Map
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
operator|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
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
block|{
name|command
operator|.
name|append
argument_list|(
literal|" AND "
argument_list|)
expr_stmt|;
block|}
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
name|HiveConf
name|builderConf
init|=
operator|new
name|HiveConf
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|AggregateIndexHandler
operator|.
name|class
argument_list|)
decl_stmt|;
name|builderConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGEMAPFILES
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|builderConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGEMAPREDFILES
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|builderConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGETEZFILES
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|rootTask
init|=
name|IndexUtils
operator|.
name|createRootTask
argument_list|(
name|builderConf
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|,
name|command
argument_list|,
operator|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|partSpec
argument_list|,
name|indexTableName
argument_list|,
name|dbName
argument_list|,
name|lineageState
argument_list|)
decl_stmt|;
return|return
name|rootTask
return|;
block|}
block|}
end_class

end_unit

