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
name|exec
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
name|common
operator|.
name|FileUtils
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
name|StatsSetupConst
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
name|ql
operator|.
name|CompilationOpContext
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
name|exec
operator|.
name|vector
operator|.
name|VectorizationContext
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
name|vector
operator|.
name|VectorizationContextRegion
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
name|vector
operator|.
name|VectorizedRowBatch
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
name|plan
operator|.
name|OperatorDesc
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
name|TableScanDesc
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
name|api
operator|.
name|OperatorType
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
name|stats
operator|.
name|StatsCollectionContext
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
name|stats
operator|.
name|StatsPublisher
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructField
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructObjectInspector
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
name|io
operator|.
name|LongWritable
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_comment
comment|/**  * Table Scan Operator If the data is coming from the map-reduce framework, just  * forward it. This will be needed as part of local work when data is not being  * read as part of map-reduce framework  **/
end_comment

begin_class
specifier|public
class|class
name|TableScanOperator
extends|extends
name|Operator
argument_list|<
name|TableScanDesc
argument_list|>
implements|implements
name|Serializable
implements|,
name|VectorizationContextRegion
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|VectorizationContext
name|taskVectorizationContext
decl_stmt|;
specifier|protected
specifier|transient
name|JobConf
name|jc
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|inputFileChanged
init|=
literal|false
decl_stmt|;
specifier|private
name|TableDesc
name|tableDesc
decl_stmt|;
specifier|private
specifier|transient
name|Stat
name|currentStat
decl_stmt|;
specifier|private
specifier|transient
name|Map
argument_list|<
name|String
argument_list|,
name|Stat
argument_list|>
name|stats
decl_stmt|;
specifier|private
specifier|transient
name|int
name|rowLimit
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|transient
name|int
name|currCount
init|=
literal|0
decl_stmt|;
comment|// insiderView will tell this TableScan is inside a view or not.
specifier|private
specifier|transient
name|boolean
name|insideView
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|vectorized
decl_stmt|;
specifier|private
name|String
name|defaultPartitionName
decl_stmt|;
comment|/**    * These values are saved during MapWork, FetchWork, etc preparation and later added to the the    * JobConf of each task.    */
specifier|private
name|String
name|schemaEvolutionColumns
decl_stmt|;
specifier|private
name|String
name|schemaEvolutionColumnsTypes
decl_stmt|;
specifier|public
name|TableDesc
name|getTableDescSkewJoin
parameter_list|()
block|{
return|return
name|tableDesc
return|;
block|}
specifier|public
name|void
name|setTableDescSkewJoin
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|)
block|{
name|this
operator|.
name|tableDesc
operator|=
name|tableDesc
expr_stmt|;
block|}
specifier|public
name|void
name|setSchemaEvolution
parameter_list|(
name|String
name|schemaEvolutionColumns
parameter_list|,
name|String
name|schemaEvolutionColumnsTypes
parameter_list|)
block|{
name|this
operator|.
name|schemaEvolutionColumns
operator|=
name|schemaEvolutionColumns
expr_stmt|;
name|this
operator|.
name|schemaEvolutionColumnsTypes
operator|=
name|schemaEvolutionColumnsTypes
expr_stmt|;
block|}
specifier|public
name|String
name|getSchemaEvolutionColumns
parameter_list|()
block|{
return|return
name|schemaEvolutionColumns
return|;
block|}
specifier|public
name|String
name|getSchemaEvolutionColumnsTypes
parameter_list|()
block|{
return|return
name|schemaEvolutionColumnsTypes
return|;
block|}
comment|/**    * Other than gathering statistics for the ANALYZE command, the table scan operator    * does not do anything special other than just forwarding the row. Since the table    * data is always read as part of the map-reduce framework by the mapper. But, when this    * assumption stops to be true, i.e table data won't be only read by the mapper, this    * operator will be enhanced to read the table.    **/
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|rowLimit
operator|>=
literal|0
condition|)
block|{
if|if
condition|(
name|checkSetDone
argument_list|(
name|row
argument_list|,
name|tag
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
if|if
condition|(
name|conf
operator|!=
literal|null
operator|&&
name|conf
operator|.
name|isGatherStats
argument_list|()
condition|)
block|{
name|gatherStats
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
name|forward
argument_list|(
name|row
argument_list|,
name|inputObjInspectors
index|[
name|tag
index|]
argument_list|,
name|vectorized
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|checkSetDone
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
block|{
if|if
condition|(
name|row
operator|instanceof
name|VectorizedRowBatch
condition|)
block|{
comment|// We need to check with 'instanceof' instead of just checking
comment|// vectorized because the row can be a VectorizedRowBatch when
comment|// FetchOptimizer kicks in even if the operator pipeline is not
comment|// vectorized
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
if|if
condition|(
name|currCount
operator|>=
name|rowLimit
condition|)
block|{
name|setDone
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
name|currCount
operator|+
name|batch
operator|.
name|size
operator|>
name|rowLimit
condition|)
block|{
name|batch
operator|.
name|size
operator|=
name|rowLimit
operator|-
name|currCount
expr_stmt|;
block|}
name|currCount
operator|+=
name|batch
operator|.
name|size
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currCount
operator|++
operator|>=
name|rowLimit
condition|)
block|{
name|setDone
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|// Change the table partition for collecting stats
annotation|@
name|Override
specifier|public
name|void
name|cleanUpInputFileChangedOp
parameter_list|()
throws|throws
name|HiveException
block|{
name|inputFileChanged
operator|=
literal|true
expr_stmt|;
comment|// If the file name to bucket number mapping is maintained, store the bucket number
comment|// in the execution context. This is needed for the following scenario:
comment|// insert overwrite table T1 select * from T2;
comment|// where T1 and T2 are sorted/bucketed by the same keys into the same number of buckets
comment|// Although one mapper per file is used (BucketizedInputHiveInput), it is possible that
comment|// any mapper can pick up any file (depending on the size of the files). The bucket number
comment|// corresponding to the input file is stored to name the output bucket file appropriately.
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|bucketNameMapping
init|=
operator|(
name|conf
operator|!=
literal|null
operator|)
condition|?
name|conf
operator|.
name|getBucketFileNameMapping
argument_list|()
else|:
literal|null
decl_stmt|;
if|if
condition|(
operator|(
name|bucketNameMapping
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|bucketNameMapping
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|Path
name|currentInputPath
init|=
name|getExecContext
argument_list|()
operator|.
name|getCurrentInputPath
argument_list|()
decl_stmt|;
name|getExecContext
argument_list|()
operator|.
name|setFileId
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|bucketNameMapping
operator|.
name|get
argument_list|(
name|currentInputPath
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|gatherStats
parameter_list|(
name|Object
name|row
parameter_list|)
block|{
comment|// first row/call or a new partition
if|if
condition|(
operator|(
name|currentStat
operator|==
literal|null
operator|)
operator|||
name|inputFileChanged
condition|)
block|{
name|String
name|partitionSpecs
decl_stmt|;
name|inputFileChanged
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getPartColumns
argument_list|()
operator|==
literal|null
operator|||
name|conf
operator|.
name|getPartColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|partitionSpecs
operator|=
literal|""
expr_stmt|;
comment|// non-partitioned
block|}
else|else
block|{
comment|// Figure out the partition spec from the input.
comment|// This is only done once for the first row (when stat == null)
comment|// since all rows in the same mapper should be from the same partition.
name|List
argument_list|<
name|Object
argument_list|>
name|writable
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|values
decl_stmt|;
name|int
name|dpStartCol
decl_stmt|;
comment|// the first position of partition column
assert|assert
name|inputObjInspectors
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|STRUCT
operator|:
literal|"input object inspector is not struct"
assert|;
name|writable
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|conf
operator|.
name|getPartColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|values
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|conf
operator|.
name|getPartColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|dpStartCol
operator|=
literal|0
expr_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
decl_stmt|;
for|for
control|(
name|StructField
name|sf
range|:
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
name|String
name|fn
init|=
name|sf
operator|.
name|getFieldName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|conf
operator|.
name|getPartColumns
argument_list|()
operator|.
name|contains
argument_list|(
name|fn
argument_list|)
condition|)
block|{
name|dpStartCol
operator|++
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
name|ObjectInspectorUtils
operator|.
name|partialCopyToStandardObject
argument_list|(
name|writable
argument_list|,
name|row
argument_list|,
name|dpStartCol
argument_list|,
name|conf
operator|.
name|getPartColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|writable
control|)
block|{
comment|// It's possible that a parition column may have NULL value, in which case the row belongs
comment|// to the special partition, __HIVE_DEFAULT_PARTITION__.
name|values
operator|.
name|add
argument_list|(
name|o
operator|==
literal|null
condition|?
name|defaultPartitionName
else|:
name|o
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|partitionSpecs
operator|=
name|FileUtils
operator|.
name|makePartName
argument_list|(
name|conf
operator|.
name|getPartColumns
argument_list|()
argument_list|,
name|values
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stats Gathering found a new partition spec = "
operator|+
name|partitionSpecs
argument_list|)
expr_stmt|;
block|}
block|}
comment|// find which column contains the raw data size (both partitioned and non partitioned
name|int
name|uSizeColumn
init|=
operator|-
literal|1
decl_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
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
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
operator|.
name|equals
argument_list|(
name|VirtualColumn
operator|.
name|RAWDATASIZE
operator|.
name|getName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
name|uSizeColumn
operator|=
name|i
expr_stmt|;
break|break;
block|}
block|}
name|currentStat
operator|=
name|stats
operator|.
name|get
argument_list|(
name|partitionSpecs
argument_list|)
expr_stmt|;
if|if
condition|(
name|currentStat
operator|==
literal|null
condition|)
block|{
name|currentStat
operator|=
operator|new
name|Stat
argument_list|()
expr_stmt|;
name|currentStat
operator|.
name|setBookkeepingInfo
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|,
name|uSizeColumn
argument_list|)
expr_stmt|;
name|stats
operator|.
name|put
argument_list|(
name|partitionSpecs
argument_list|,
name|currentStat
argument_list|)
expr_stmt|;
block|}
block|}
comment|// increase the row count
name|currentStat
operator|.
name|addToStat
argument_list|(
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// extract the raw data size, and update the stats for the current partition
name|int
name|rdSizeColumn
init|=
name|currentStat
operator|.
name|getBookkeepingInfo
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|rdSizeColumn
operator|!=
operator|-
literal|1
condition|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|rdSize
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ObjectInspectorUtils
operator|.
name|partialCopyToStandardObject
argument_list|(
name|rdSize
argument_list|,
name|row
argument_list|,
name|rdSizeColumn
argument_list|,
literal|1
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
name|currentStat
operator|.
name|addToStat
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|,
operator|(
operator|(
operator|(
name|LongWritable
operator|)
name|rdSize
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|get
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Kryo ctor. */
specifier|protected
name|TableScanOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TableScanOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|inputFileChanged
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|rowLimit
operator|=
name|conf
operator|.
name|getRowLimit
argument_list|()
expr_stmt|;
if|if
condition|(
name|hconf
operator|instanceof
name|JobConf
condition|)
block|{
name|jc
operator|=
operator|(
name|JobConf
operator|)
name|hconf
expr_stmt|;
block|}
else|else
block|{
comment|// test code path
name|jc
operator|=
operator|new
name|JobConf
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
name|defaultPartitionName
operator|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DEFAULTPARTITIONNAME
argument_list|)
expr_stmt|;
name|currentStat
operator|=
literal|null
expr_stmt|;
name|stats
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Stat
argument_list|>
argument_list|()
expr_stmt|;
comment|/*      * This TableScanDesc flag is strictly set by the Vectorizer class for vectorized MapWork      * vertices.      */
name|vectorized
operator|=
name|conf
operator|.
name|isVectorized
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|conf
operator|.
name|isGatherStats
argument_list|()
operator|&&
name|stats
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|publishStats
argument_list|()
expr_stmt|;
block|}
block|}
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
comment|/**    * The operator name for this operator type. This is used to construct the    * rule for an operator    *    * @return the operator name    **/
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
return|;
block|}
specifier|static
specifier|public
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"TS"
return|;
block|}
specifier|public
name|void
name|setNeededColumnIDs
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|orign_columns
parameter_list|)
block|{
name|conf
operator|.
name|setNeededColumnIDs
argument_list|(
name|orign_columns
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getNeededColumnIDs
parameter_list|()
block|{
return|return
name|conf
operator|.
name|getNeededColumnIDs
argument_list|()
return|;
block|}
specifier|public
name|void
name|setNeededColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|)
block|{
name|conf
operator|.
name|setNeededColumns
argument_list|(
name|columnNames
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getNeededNestedColumnPaths
parameter_list|()
block|{
return|return
name|conf
operator|.
name|getNeededNestedColumnPaths
argument_list|()
return|;
block|}
specifier|public
name|void
name|setNeededNestedColumnPaths
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|nestedColumnPaths
parameter_list|)
block|{
name|conf
operator|.
name|setNeededNestedColumnPaths
argument_list|(
name|nestedColumnPaths
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getNeededColumns
parameter_list|()
block|{
return|return
name|conf
operator|.
name|getNeededColumns
argument_list|()
return|;
block|}
specifier|public
name|void
name|setReferencedColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|referencedColumns
parameter_list|)
block|{
name|conf
operator|.
name|setReferencedColumns
argument_list|(
name|referencedColumns
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getReferencedColumns
parameter_list|()
block|{
return|return
name|conf
operator|.
name|getReferencedColumns
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|TABLESCAN
return|;
block|}
specifier|private
name|void
name|publishStats
parameter_list|()
throws|throws
name|HiveException
block|{
name|boolean
name|isStatsReliable
init|=
name|conf
operator|.
name|isStatsReliable
argument_list|()
decl_stmt|;
comment|// Initializing a stats publisher
name|StatsPublisher
name|statsPublisher
init|=
name|Utilities
operator|.
name|getStatsPublisher
argument_list|(
name|jc
argument_list|)
decl_stmt|;
name|StatsCollectionContext
name|sc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|jc
argument_list|)
decl_stmt|;
name|sc
operator|.
name|setStatsTmpDir
argument_list|(
name|conf
operator|.
name|getTmpStatsDir
argument_list|()
argument_list|)
expr_stmt|;
name|sc
operator|.
name|setContextSuffix
argument_list|(
name|getOperatorId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|statsPublisher
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
condition|)
block|{
comment|// just return, stats gathering should not block the main query.
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"StatsPublishing error: cannot connect to database."
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isStatsReliable
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|STATSPUBLISHER_CONNECTION_ERROR
operator|.
name|getErrorCodedMsg
argument_list|()
argument_list|)
throw|;
block|}
return|return;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|statsToPublish
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|pspecs
range|:
name|stats
operator|.
name|keySet
argument_list|()
control|)
block|{
name|statsToPublish
operator|.
name|clear
argument_list|()
expr_stmt|;
name|String
name|prefix
init|=
name|Utilities
operator|.
name|join
argument_list|(
name|conf
operator|.
name|getStatsAggPrefix
argument_list|()
argument_list|,
name|pspecs
argument_list|)
decl_stmt|;
name|String
name|key
init|=
name|prefix
operator|.
name|endsWith
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
condition|?
name|prefix
else|:
name|prefix
operator|+
name|Path
operator|.
name|SEPARATOR
decl_stmt|;
for|for
control|(
name|String
name|statType
range|:
name|stats
operator|.
name|get
argument_list|(
name|pspecs
argument_list|)
operator|.
name|getStoredStats
argument_list|()
control|)
block|{
name|statsToPublish
operator|.
name|put
argument_list|(
name|statType
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|stats
operator|.
name|get
argument_list|(
name|pspecs
argument_list|)
operator|.
name|getStat
argument_list|(
name|statType
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|statsPublisher
operator|.
name|publishStat
argument_list|(
name|key
argument_list|,
name|statsToPublish
argument_list|)
condition|)
block|{
if|if
condition|(
name|isStatsReliable
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|STATSPUBLISHER_PUBLISHING_ERROR
operator|.
name|getErrorCodedMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"publishing : "
operator|+
name|key
operator|+
literal|" : "
operator|+
name|statsToPublish
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|statsPublisher
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
condition|)
block|{
if|if
condition|(
name|isStatsReliable
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|STATSPUBLISHER_CLOSING_ERROR
operator|.
name|getErrorCodedMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportSkewJoinOptimization
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportAutomaticSortMergeJoin
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|clone
parameter_list|()
throws|throws
name|CloneNotSupportedException
block|{
name|TableScanOperator
name|ts
init|=
operator|(
name|TableScanOperator
operator|)
name|super
operator|.
name|clone
argument_list|()
decl_stmt|;
name|ts
operator|.
name|setNeededColumnIDs
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|getNeededColumnIDs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ts
operator|.
name|setNeededColumns
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|getNeededColumns
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ts
operator|.
name|setReferencedColumns
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|getReferencedColumns
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|ts
return|;
block|}
specifier|public
name|boolean
name|isInsideView
parameter_list|()
block|{
return|return
name|insideView
return|;
block|}
specifier|public
name|void
name|setInsideView
parameter_list|(
name|boolean
name|insiderView
parameter_list|)
block|{
name|this
operator|.
name|insideView
operator|=
name|insiderView
expr_stmt|;
block|}
specifier|public
name|void
name|setTaskVectorizationContext
parameter_list|(
name|VectorizationContext
name|taskVectorizationContext
parameter_list|)
block|{
name|this
operator|.
name|taskVectorizationContext
operator|=
name|taskVectorizationContext
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizationContext
name|getOutputVectorizationContext
parameter_list|()
block|{
return|return
name|taskVectorizationContext
return|;
block|}
block|}
end_class

end_unit

