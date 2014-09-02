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
name|exec
operator|.
name|vector
package|;
end_package

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
name|ql
operator|.
name|exec
operator|.
name|FileSinkOperator
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
name|expressions
operator|.
name|VectorExpressionWriter
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
name|expressions
operator|.
name|VectorExpressionWriterFactory
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
name|plan
operator|.
name|FileSinkDesc
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
name|serde2
operator|.
name|SerDeException
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
name|SerDeStats
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
name|ObjectWritable
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
name|Text
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * File Sink operator implementation.  **/
end_comment

begin_class
specifier|public
class|class
name|VectorFileSinkOperator
extends|extends
name|FileSinkOperator
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
specifier|transient
name|Object
index|[]
name|singleRow
decl_stmt|;
specifier|protected
specifier|transient
name|VectorExpressionWriter
index|[]
name|valueWriters
decl_stmt|;
specifier|public
name|VectorFileSinkOperator
parameter_list|(
name|VectorizationContext
name|context
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|(
name|FileSinkDesc
operator|)
name|conf
expr_stmt|;
block|}
specifier|public
name|VectorFileSinkOperator
parameter_list|()
block|{    }
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
name|valueWriters
operator|=
name|VectorExpressionWriterFactory
operator|.
name|getExpressionWriters
argument_list|(
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|singleRow
operator|=
operator|new
name|Object
index|[
name|valueWriters
operator|.
name|length
index|]
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processOp
parameter_list|(
name|Object
name|data
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorizedRowBatch
name|vrg
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|data
decl_stmt|;
name|Writable
index|[]
name|records
init|=
literal|null
decl_stmt|;
name|boolean
name|vectorizedSerde
init|=
literal|false
decl_stmt|;
try|try
block|{
if|if
condition|(
name|serializer
operator|instanceof
name|VectorizedSerde
condition|)
block|{
name|recordValue
operator|=
operator|(
operator|(
name|VectorizedSerde
operator|)
name|serializer
operator|)
operator|.
name|serializeVector
argument_list|(
name|vrg
argument_list|,
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|records
operator|=
operator|(
name|Writable
index|[]
operator|)
operator|(
operator|(
name|ObjectWritable
operator|)
name|recordValue
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|vectorizedSerde
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e1
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|vrg
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
name|Writable
name|row
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|vectorizedSerde
condition|)
block|{
name|row
operator|=
name|records
index|[
name|i
index|]
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|vrg
operator|.
name|valueWriters
operator|==
literal|null
condition|)
block|{
name|vrg
operator|.
name|setValueWriters
argument_list|(
name|this
operator|.
name|valueWriters
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|row
operator|=
name|serializer
operator|.
name|serialize
argument_list|(
name|getRowObject
argument_list|(
name|vrg
argument_list|,
name|i
argument_list|)
argument_list|,
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
comment|/* Create list bucketing sub-directory only if stored-as-directories is on. */
name|String
name|lbDirName
init|=
literal|null
decl_stmt|;
name|lbDirName
operator|=
operator|(
name|lbCtx
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|generateListBucketingDirName
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|FSPaths
name|fpaths
decl_stmt|;
if|if
condition|(
operator|!
name|bDynParts
operator|&&
operator|!
name|filesCreated
condition|)
block|{
if|if
condition|(
name|lbDirName
operator|!=
literal|null
condition|)
block|{
name|FSPaths
name|fsp2
init|=
name|lookupListBucketingPaths
argument_list|(
name|lbDirName
argument_list|)
decl_stmt|;
block|}
else|else
block|{
name|createBucketFiles
argument_list|(
name|fsp
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|updateProgress
argument_list|()
expr_stmt|;
comment|// if DP is enabled, get the final output writers and prepare the real output row
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
if|if
condition|(
name|bDynParts
condition|)
block|{
comment|// copy the DP column values from the input row to dpVals
name|dpVals
operator|.
name|clear
argument_list|()
expr_stmt|;
name|dpWritables
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ObjectInspectorUtils
operator|.
name|partialCopyToStandardObject
argument_list|(
name|dpWritables
argument_list|,
name|row
argument_list|,
name|dpStartCol
argument_list|,
name|numDynParts
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
comment|// get a set of RecordWriter based on the DP column values
comment|// pass the null value along to the escaping process to determine what the dir should be
for|for
control|(
name|Object
name|o
range|:
name|dpWritables
control|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|o
operator|.
name|toString
argument_list|()
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|dpVals
operator|.
name|add
argument_list|(
name|dpCtx
operator|.
name|getDefaultPartitionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dpVals
operator|.
name|add
argument_list|(
name|o
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|fpaths
operator|=
name|getDynOutPaths
argument_list|(
name|dpVals
argument_list|,
name|lbDirName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|lbDirName
operator|!=
literal|null
condition|)
block|{
name|fpaths
operator|=
name|lookupListBucketingPaths
argument_list|(
name|lbDirName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fpaths
operator|=
name|fsp
expr_stmt|;
block|}
block|}
name|rowOutWriters
operator|=
name|fpaths
operator|.
name|getOutWriters
argument_list|()
expr_stmt|;
comment|// check if all record writers implement statistics. if atleast one RW
comment|// doesn't implement stats interface we will fallback to conventional way
comment|// of gathering stats
name|isCollectRWStats
operator|=
name|areAllTrue
argument_list|(
name|statsFromRecordWriter
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|isGatherStats
argument_list|()
operator|&&
operator|!
name|isCollectRWStats
condition|)
block|{
if|if
condition|(
name|statsCollectRawDataSize
condition|)
block|{
name|SerDeStats
name|stats
init|=
name|serializer
operator|.
name|getSerDeStats
argument_list|()
decl_stmt|;
if|if
condition|(
name|stats
operator|!=
literal|null
condition|)
block|{
name|fpaths
operator|.
name|getStat
argument_list|()
operator|.
name|addToStat
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|,
name|stats
operator|.
name|getRawDataSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|fpaths
operator|.
name|getStat
argument_list|()
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
block|}
if|if
condition|(
name|row_count
operator|!=
literal|null
condition|)
block|{
name|row_count
operator|.
name|set
argument_list|(
name|row_count
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|multiFileSpray
condition|)
block|{
name|rowOutWriters
index|[
literal|0
index|]
operator|.
name|write
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|keyHashCode
init|=
literal|0
decl_stmt|;
name|key
operator|.
name|setHashCode
argument_list|(
name|keyHashCode
argument_list|)
expr_stmt|;
name|int
name|bucketNum
init|=
name|prtner
operator|.
name|getBucket
argument_list|(
name|key
argument_list|,
literal|null
argument_list|,
name|totalFiles
argument_list|)
decl_stmt|;
name|int
name|idx
init|=
name|bucketMap
operator|.
name|get
argument_list|(
name|bucketNum
argument_list|)
decl_stmt|;
name|rowOutWriters
index|[
name|idx
index|]
operator|.
name|write
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|Object
index|[]
name|getRowObject
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|,
name|int
name|rowIndex
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|batchIndex
init|=
name|rowIndex
decl_stmt|;
if|if
condition|(
name|vrg
operator|.
name|selectedInUse
condition|)
block|{
name|batchIndex
operator|=
name|vrg
operator|.
name|selected
index|[
name|rowIndex
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|vrg
operator|.
name|projectionSize
condition|;
name|i
operator|++
control|)
block|{
name|ColumnVector
name|vectorColumn
init|=
name|vrg
operator|.
name|cols
index|[
name|vrg
operator|.
name|projectedColumns
index|[
name|i
index|]
index|]
decl_stmt|;
name|singleRow
index|[
name|i
index|]
operator|=
name|vrg
operator|.
name|valueWriters
index|[
name|i
index|]
operator|.
name|writeValue
argument_list|(
name|vectorColumn
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
block|}
return|return
name|singleRow
return|;
block|}
block|}
end_class

end_unit

