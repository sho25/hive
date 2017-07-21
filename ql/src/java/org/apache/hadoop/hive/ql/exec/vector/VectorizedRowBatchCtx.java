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
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|hive
operator|.
name|common
operator|.
name|type
operator|.
name|HiveChar
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
name|typeinfo
operator|.
name|CharTypeInfo
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
name|type
operator|.
name|HiveDecimal
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
name|type
operator|.
name|HiveIntervalDayTime
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
name|type
operator|.
name|HiveIntervalYearMonth
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
name|io
operator|.
name|HiveFileFormatUtils
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
name|io
operator|.
name|IOPrepareCache
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
name|MapWork
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
name|Explain
operator|.
name|Level
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
name|Vectorization
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
name|ColumnProjectionUtils
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
name|io
operator|.
name|DateWritable
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
name|ObjectInspectorConverters
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|FileSplit
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
name|common
operator|.
name|util
operator|.
name|DateUtils
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Context for Vectorized row batch. this class does eager deserialization of row data using serde  * in the RecordReader layer.  * It has supports partitions in this layer so that the vectorized batch is populated correctly  * with the partition column.  */
end_comment

begin_class
specifier|public
class|class
name|VectorizedRowBatchCtx
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
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VectorizedRowBatchCtx
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// The following information is for creating VectorizedRowBatch and for helping with
comment|// knowing how the table is partitioned.
comment|//
comment|// It will be stored in MapWork and ReduceWork.
specifier|private
name|String
index|[]
name|rowColumnNames
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|rowColumnTypeInfos
decl_stmt|;
specifier|private
name|int
index|[]
name|dataColumnNums
decl_stmt|;
specifier|private
name|int
name|dataColumnCount
decl_stmt|;
specifier|private
name|int
name|partitionColumnCount
decl_stmt|;
specifier|private
name|int
name|virtualColumnCount
decl_stmt|;
specifier|private
name|VirtualColumn
index|[]
name|neededVirtualColumns
decl_stmt|;
specifier|private
name|String
index|[]
name|scratchColumnTypeNames
decl_stmt|;
comment|/**    * Constructor for VectorizedRowBatchCtx    */
specifier|public
name|VectorizedRowBatchCtx
parameter_list|()
block|{   }
specifier|public
name|VectorizedRowBatchCtx
parameter_list|(
name|String
index|[]
name|rowColumnNames
parameter_list|,
name|TypeInfo
index|[]
name|rowColumnTypeInfos
parameter_list|,
name|int
index|[]
name|dataColumnNums
parameter_list|,
name|int
name|partitionColumnCount
parameter_list|,
name|VirtualColumn
index|[]
name|neededVirtualColumns
parameter_list|,
name|String
index|[]
name|scratchColumnTypeNames
parameter_list|)
block|{
name|this
operator|.
name|rowColumnNames
operator|=
name|rowColumnNames
expr_stmt|;
name|this
operator|.
name|rowColumnTypeInfos
operator|=
name|rowColumnTypeInfos
expr_stmt|;
name|this
operator|.
name|dataColumnNums
operator|=
name|dataColumnNums
expr_stmt|;
name|this
operator|.
name|partitionColumnCount
operator|=
name|partitionColumnCount
expr_stmt|;
name|this
operator|.
name|neededVirtualColumns
operator|=
name|neededVirtualColumns
expr_stmt|;
name|this
operator|.
name|virtualColumnCount
operator|=
name|neededVirtualColumns
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|scratchColumnTypeNames
operator|=
name|scratchColumnTypeNames
expr_stmt|;
name|dataColumnCount
operator|=
name|rowColumnTypeInfos
operator|.
name|length
operator|-
name|partitionColumnCount
operator|-
name|virtualColumnCount
expr_stmt|;
block|}
specifier|public
name|String
index|[]
name|getRowColumnNames
parameter_list|()
block|{
return|return
name|rowColumnNames
return|;
block|}
specifier|public
name|TypeInfo
index|[]
name|getRowColumnTypeInfos
parameter_list|()
block|{
return|return
name|rowColumnTypeInfos
return|;
block|}
specifier|public
name|int
index|[]
name|getDataColumnNums
parameter_list|()
block|{
return|return
name|dataColumnNums
return|;
block|}
specifier|public
name|int
name|getDataColumnCount
parameter_list|()
block|{
return|return
name|dataColumnCount
return|;
block|}
specifier|public
name|int
name|getPartitionColumnCount
parameter_list|()
block|{
return|return
name|partitionColumnCount
return|;
block|}
specifier|public
name|int
name|getVirtualColumnCount
parameter_list|()
block|{
return|return
name|virtualColumnCount
return|;
block|}
specifier|public
name|VirtualColumn
index|[]
name|getNeededVirtualColumns
parameter_list|()
block|{
return|return
name|neededVirtualColumns
return|;
block|}
specifier|public
name|String
index|[]
name|getScratchColumnTypeNames
parameter_list|()
block|{
return|return
name|scratchColumnTypeNames
return|;
block|}
comment|/**    * Initializes the VectorizedRowBatch context based on an scratch column type names and    * object inspector.    * @param structObjectInspector    * @param scratchColumnTypeNames    *          Object inspector that shapes the column types    * @throws HiveException    */
specifier|public
name|void
name|init
parameter_list|(
name|StructObjectInspector
name|structObjectInspector
parameter_list|,
name|String
index|[]
name|scratchColumnTypeNames
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Row column information.
name|rowColumnNames
operator|=
name|VectorizedBatchUtil
operator|.
name|columnNamesFromStructObjectInspector
argument_list|(
name|structObjectInspector
argument_list|)
expr_stmt|;
name|rowColumnTypeInfos
operator|=
name|VectorizedBatchUtil
operator|.
name|typeInfosFromStructObjectInspector
argument_list|(
name|structObjectInspector
argument_list|)
expr_stmt|;
name|dataColumnNums
operator|=
literal|null
expr_stmt|;
name|partitionColumnCount
operator|=
literal|0
expr_stmt|;
name|virtualColumnCount
operator|=
literal|0
expr_stmt|;
name|neededVirtualColumns
operator|=
operator|new
name|VirtualColumn
index|[
literal|0
index|]
expr_stmt|;
name|dataColumnCount
operator|=
name|rowColumnTypeInfos
operator|.
name|length
expr_stmt|;
comment|// Scratch column information.
name|this
operator|.
name|scratchColumnTypeNames
operator|=
name|scratchColumnTypeNames
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|getPartitionValues
parameter_list|(
name|VectorizedRowBatchCtx
name|vrbCtx
parameter_list|,
name|Configuration
name|hiveConf
parameter_list|,
name|FileSplit
name|split
parameter_list|,
name|Object
index|[]
name|partitionValues
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: this is invalid for SMB. Keep this for now for legacy reasons. See the other overload.
name|MapWork
name|mapWork
init|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|getPartitionValues
argument_list|(
name|vrbCtx
argument_list|,
name|mapWork
argument_list|,
name|split
argument_list|,
name|partitionValues
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|getPartitionValues
parameter_list|(
name|VectorizedRowBatchCtx
name|vrbCtx
parameter_list|,
name|MapWork
name|mapWork
parameter_list|,
name|FileSplit
name|split
parameter_list|,
name|Object
index|[]
name|partitionValues
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
init|=
name|mapWork
operator|.
name|getPathToPartitionInfo
argument_list|()
decl_stmt|;
name|PartitionDesc
name|partDesc
init|=
name|HiveFileFormatUtils
operator|.
name|getPartitionDescFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|split
operator|.
name|getPath
argument_list|()
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|getPartitionDescMap
argument_list|()
argument_list|)
decl_stmt|;
name|getPartitionValues
argument_list|(
name|vrbCtx
argument_list|,
name|partDesc
argument_list|,
name|partitionValues
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|getPartitionValues
parameter_list|(
name|VectorizedRowBatchCtx
name|vrbCtx
parameter_list|,
name|PartitionDesc
name|partDesc
parameter_list|,
name|Object
index|[]
name|partitionValues
parameter_list|)
block|{
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|partDesc
operator|.
name|getPartSpec
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
name|vrbCtx
operator|.
name|partitionColumnCount
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|objectValue
decl_stmt|;
if|if
condition|(
name|partSpec
operator|==
literal|null
condition|)
block|{
comment|// For partition-less table, initialize partValue to empty string.
comment|// We can have partition-less table even if we have partition keys
comment|// when there is only only partition selected and the partition key is not
comment|// part of the projection/include list.
name|objectValue
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|String
name|key
init|=
name|vrbCtx
operator|.
name|rowColumnNames
index|[
name|vrbCtx
operator|.
name|dataColumnCount
operator|+
name|i
index|]
decl_stmt|;
comment|// Create a Standard java object Inspector
name|TypeInfo
name|partColTypeInfo
init|=
name|vrbCtx
operator|.
name|rowColumnTypeInfos
index|[
name|vrbCtx
operator|.
name|dataColumnCount
operator|+
name|i
index|]
decl_stmt|;
name|ObjectInspector
name|objectInspector
init|=
name|TypeInfoUtils
operator|.
name|getStandardJavaObjectInspectorFromTypeInfo
argument_list|(
name|partColTypeInfo
argument_list|)
decl_stmt|;
name|objectValue
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|objectInspector
argument_list|)
operator|.
name|convert
argument_list|(
name|partSpec
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|partColTypeInfo
operator|instanceof
name|CharTypeInfo
condition|)
block|{
name|objectValue
operator|=
operator|(
operator|(
name|HiveChar
operator|)
name|objectValue
operator|)
operator|.
name|getStrippedValue
argument_list|()
expr_stmt|;
block|}
block|}
name|partitionValues
index|[
name|i
index|]
operator|=
name|objectValue
expr_stmt|;
block|}
block|}
comment|/**    * Creates a Vectorized row batch and the column vectors.    *    * @return VectorizedRowBatch    * @throws HiveException    */
specifier|public
name|VectorizedRowBatch
name|createVectorizedRowBatch
parameter_list|()
block|{
specifier|final
name|int
name|nonScratchColumnCount
init|=
name|rowColumnTypeInfos
operator|.
name|length
decl_stmt|;
specifier|final
name|int
name|totalColumnCount
init|=
name|nonScratchColumnCount
operator|+
name|scratchColumnTypeNames
operator|.
name|length
decl_stmt|;
name|VectorizedRowBatch
name|result
init|=
operator|new
name|VectorizedRowBatch
argument_list|(
name|totalColumnCount
argument_list|)
decl_stmt|;
if|if
condition|(
name|dataColumnNums
operator|==
literal|null
condition|)
block|{
comment|// All data and partition columns.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nonScratchColumnCount
condition|;
name|i
operator|++
control|)
block|{
name|TypeInfo
name|typeInfo
init|=
name|rowColumnTypeInfos
index|[
name|i
index|]
decl_stmt|;
name|result
operator|.
name|cols
index|[
name|i
index|]
operator|=
name|VectorizedBatchUtil
operator|.
name|createColumnVector
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Create only needed/included columns data columns.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|dataColumnNums
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|columnNum
init|=
name|dataColumnNums
index|[
name|i
index|]
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|columnNum
operator|<
name|nonScratchColumnCount
argument_list|)
expr_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|rowColumnTypeInfos
index|[
name|columnNum
index|]
decl_stmt|;
name|result
operator|.
name|cols
index|[
name|columnNum
index|]
operator|=
name|VectorizedBatchUtil
operator|.
name|createColumnVector
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
block|}
comment|// Always create partition and virtual columns.
specifier|final
name|int
name|partitionEndColumnNum
init|=
name|dataColumnCount
operator|+
name|partitionColumnCount
decl_stmt|;
for|for
control|(
name|int
name|partitionColumnNum
init|=
name|dataColumnCount
init|;
name|partitionColumnNum
operator|<
name|partitionEndColumnNum
condition|;
name|partitionColumnNum
operator|++
control|)
block|{
name|TypeInfo
name|typeInfo
init|=
name|rowColumnTypeInfos
index|[
name|partitionColumnNum
index|]
decl_stmt|;
name|result
operator|.
name|cols
index|[
name|partitionColumnNum
index|]
operator|=
name|VectorizedBatchUtil
operator|.
name|createColumnVector
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
name|virtualEndColumnNum
init|=
name|partitionEndColumnNum
operator|+
name|virtualColumnCount
decl_stmt|;
for|for
control|(
name|int
name|virtualColumnNum
init|=
name|partitionEndColumnNum
init|;
name|virtualColumnNum
operator|<
name|virtualEndColumnNum
condition|;
name|virtualColumnNum
operator|++
control|)
block|{
name|TypeInfo
name|typeInfo
init|=
name|rowColumnTypeInfos
index|[
name|virtualColumnNum
index|]
decl_stmt|;
name|result
operator|.
name|cols
index|[
name|virtualColumnNum
index|]
operator|=
name|VectorizedBatchUtil
operator|.
name|createColumnVector
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
block|}
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
name|scratchColumnTypeNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|typeName
init|=
name|scratchColumnTypeNames
index|[
name|i
index|]
decl_stmt|;
name|result
operator|.
name|cols
index|[
name|nonScratchColumnCount
operator|+
name|i
index|]
operator|=
name|VectorizedBatchUtil
operator|.
name|createColumnVector
argument_list|(
name|typeName
argument_list|)
expr_stmt|;
block|}
comment|// UNDONE: Also remember virtualColumnCount...
name|result
operator|.
name|setPartitionInfo
argument_list|(
name|dataColumnCount
argument_list|,
name|partitionColumnCount
argument_list|)
expr_stmt|;
name|result
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Add the partition values to the batch    *    * @param batch    * @param partitionValues    * @throws HiveException    */
specifier|public
name|void
name|addPartitionColsToBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|Object
index|[]
name|partitionValues
parameter_list|)
block|{
if|if
condition|(
name|partitionValues
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partitionColumnCount
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|value
init|=
name|partitionValues
index|[
name|i
index|]
decl_stmt|;
name|int
name|colIndex
init|=
name|dataColumnCount
operator|+
name|i
decl_stmt|;
name|String
name|partitionColumnName
init|=
name|rowColumnNames
index|[
name|colIndex
index|]
decl_stmt|;
name|PrimitiveTypeInfo
name|primitiveTypeInfo
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|rowColumnTypeInfos
index|[
name|colIndex
index|]
decl_stmt|;
switch|switch
condition|(
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|lcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|lcv
operator|.
name|fill
argument_list|(
operator|(
name|Boolean
operator|)
name|value
operator|==
literal|true
condition|?
literal|1
else|:
literal|0
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|BYTE
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|lcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|lcv
operator|.
name|fill
argument_list|(
operator|(
name|Byte
operator|)
name|value
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|SHORT
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|lcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|lcv
operator|.
name|fill
argument_list|(
operator|(
name|Short
operator|)
name|value
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|INT
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|lcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|lcv
operator|.
name|fill
argument_list|(
operator|(
name|Integer
operator|)
name|value
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|LONG
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|lcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|lcv
operator|.
name|fill
argument_list|(
operator|(
name|Long
operator|)
name|value
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|DATE
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|lcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|lcv
operator|.
name|fill
argument_list|(
name|DateWritable
operator|.
name|dateToDays
argument_list|(
operator|(
name|Date
operator|)
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|TIMESTAMP
case|:
block|{
name|TimestampColumnVector
name|lcv
init|=
operator|(
name|TimestampColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|lcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|lcv
operator|.
name|fill
argument_list|(
operator|(
name|Timestamp
operator|)
name|value
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|INTERVAL_YEAR_MONTH
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|lcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|lcv
operator|.
name|fill
argument_list|(
operator|(
operator|(
name|HiveIntervalYearMonth
operator|)
name|value
operator|)
operator|.
name|getTotalMonths
argument_list|()
argument_list|)
expr_stmt|;
name|lcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
case|case
name|INTERVAL_DAY_TIME
case|:
block|{
name|IntervalDayTimeColumnVector
name|icv
init|=
operator|(
name|IntervalDayTimeColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|icv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|icv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|icv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|icv
operator|.
name|fill
argument_list|(
operator|(
operator|(
name|HiveIntervalDayTime
operator|)
name|value
operator|)
argument_list|)
expr_stmt|;
name|icv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
case|case
name|FLOAT
case|:
block|{
name|DoubleColumnVector
name|dcv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|dcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|dcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|dcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|dcv
operator|.
name|fill
argument_list|(
operator|(
name|Float
operator|)
name|value
argument_list|)
expr_stmt|;
name|dcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|DOUBLE
case|:
block|{
name|DoubleColumnVector
name|dcv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|dcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|dcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|dcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|dcv
operator|.
name|fill
argument_list|(
operator|(
name|Double
operator|)
name|value
argument_list|)
expr_stmt|;
name|dcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|DECIMAL
case|:
block|{
name|DecimalColumnVector
name|dv
init|=
operator|(
name|DecimalColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|dv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|dv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|dv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|HiveDecimal
name|hd
init|=
operator|(
name|HiveDecimal
operator|)
name|value
decl_stmt|;
name|dv
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|hd
argument_list|)
expr_stmt|;
name|dv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|dv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|BINARY
case|:
block|{
name|BytesColumnVector
name|bcv
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|(
name|byte
index|[]
operator|)
name|value
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
name|bcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|bcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|bcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|bcv
operator|.
name|fill
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|bcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
break|break;
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
block|{
name|BytesColumnVector
name|bcv
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
decl_stmt|;
name|String
name|sVal
init|=
name|value
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|sVal
operator|==
literal|null
condition|)
block|{
name|bcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|bcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|bcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|bcv
operator|.
name|setVal
argument_list|(
literal|0
argument_list|,
name|sVal
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|bcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
block|}
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unable to recognize the partition type "
operator|+
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
operator|+
literal|" for column "
operator|+
name|partitionColumnName
argument_list|)
throw|;
block|}
block|}
block|}
block|}
comment|/**    * Determine whether a given column is a partition column    * @param colNum column number in    * {@link org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch}s created by this context.    * @return true if it is a partition column, false otherwise    */
specifier|public
specifier|final
name|boolean
name|isPartitionCol
parameter_list|(
name|int
name|colNum
parameter_list|)
block|{
return|return
name|colNum
operator|>=
name|dataColumnCount
operator|&&
name|colNum
operator|<
name|rowColumnTypeInfos
operator|.
name|length
return|;
block|}
block|}
end_class

end_unit

