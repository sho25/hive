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
name|java
operator|.
name|util
operator|.
name|Properties
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
name|serde2
operator|.
name|Deserializer
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
name|ObjectInspectorFactory
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
name|PrimitiveObjectInspector
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
name|io
operator|.
name|Writable
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

begin_comment
comment|/**  * Context for Vectorized row batch. this calss does eager deserialization of row data using serde  * in the RecordReader layer.  * It has supports partitions in this layer so that the vectorized batch is populated correctly with  * the partition column.  * VectorizedRowBatchCtx.  *  */
end_comment

begin_class
specifier|public
class|class
name|VectorizedRowBatchCtx
block|{
comment|// OI for raw row data (EG without partition cols)
specifier|private
name|StructObjectInspector
name|rawRowOI
decl_stmt|;
comment|// OI for the row (Raw row OI + partition OI)
specifier|private
name|StructObjectInspector
name|rowOI
decl_stmt|;
comment|// Deserializer for the row data
specifier|private
name|Deserializer
name|deserializer
decl_stmt|;
comment|// Hash map of partition values. Key=TblColName value=PartitionValue
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
decl_stmt|;
comment|/**    * Constructor for VectorizedRowBatchCtx    *    * @param rawRowOI    *          OI for raw row data (EG without partition cols)    * @param rowOI    *          OI for the row (Raw row OI + partition OI)    * @param deserializer    *          Deserializer for the row data    * @param partitionValues    *          Hash map of partition values. Key=TblColName value=PartitionValue    */
specifier|public
name|VectorizedRowBatchCtx
parameter_list|(
name|StructObjectInspector
name|rawRowOI
parameter_list|,
name|StructObjectInspector
name|rowOI
parameter_list|,
name|Deserializer
name|deserializer
parameter_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
parameter_list|)
block|{
name|this
operator|.
name|rowOI
operator|=
name|rowOI
expr_stmt|;
name|this
operator|.
name|rawRowOI
operator|=
name|rawRowOI
expr_stmt|;
name|this
operator|.
name|deserializer
operator|=
name|deserializer
expr_stmt|;
name|this
operator|.
name|partitionValues
operator|=
name|partitionValues
expr_stmt|;
block|}
comment|/**    * Constructor for VectorizedRowBatchCtx    */
specifier|public
name|VectorizedRowBatchCtx
parameter_list|()
block|{    }
comment|/**    * Initializes VectorizedRowBatch context based on the    * split and Hive configuration (Job conf with hive Plan).    *    * @param hiveConf    *          Hive configuration using Hive plan is extracted    * @param split    *          File split of the file being read    * @throws ClassNotFoundException    * @throws IOException    * @throws SerDeException    * @throws InstantiationException    * @throws IllegalAccessException    * @throws HiveException    */
specifier|public
name|void
name|Init
parameter_list|(
name|Configuration
name|hiveConf
parameter_list|,
name|FileSplit
name|split
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|IOException
throws|,
name|SerDeException
throws|,
name|InstantiationException
throws|,
name|IllegalAccessException
throws|,
name|HiveException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
init|=
name|Utilities
operator|.
name|getMapRedWork
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|getPathToPartitionInfo
argument_list|()
decl_stmt|;
name|PartitionDesc
name|part
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
name|Class
name|serdeclass
init|=
name|part
operator|.
name|getDeserializerClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|serdeclass
operator|==
literal|null
condition|)
block|{
name|String
name|className
init|=
name|part
operator|.
name|getSerdeClassName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|className
operator|==
literal|null
operator|)
operator|||
operator|(
name|className
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"SerDe class or the SerDe class name is not set for table: "
operator|+
name|part
operator|.
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
literal|"name"
argument_list|)
argument_list|)
throw|;
block|}
name|serdeclass
operator|=
name|hiveConf
operator|.
name|getClassByName
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
name|Properties
name|partProps
init|=
operator|(
name|part
operator|.
name|getPartSpec
argument_list|()
operator|==
literal|null
operator|||
name|part
operator|.
name|getPartSpec
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|?
name|part
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getProperties
argument_list|()
else|:
name|part
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|Deserializer
name|partDeserializer
init|=
operator|(
name|Deserializer
operator|)
name|serdeclass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|partDeserializer
operator|.
name|initialize
argument_list|(
name|hiveConf
argument_list|,
name|partProps
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|partRawRowObjectInspector
init|=
operator|(
name|StructObjectInspector
operator|)
name|partDeserializer
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|deserializer
operator|=
name|partDeserializer
expr_stmt|;
comment|// Check to see if this split is part of a partition of a table
name|String
name|pcols
init|=
name|partProps
operator|.
name|getProperty
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
name|META_TABLE_PARTITION_COLUMNS
argument_list|)
decl_stmt|;
if|if
condition|(
name|pcols
operator|!=
literal|null
operator|&&
name|pcols
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// Partitions exist for this table. Get the partition object inspector and
comment|// raw row object inspector (row with out partition col)
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|part
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|String
index|[]
name|partKeys
init|=
name|pcols
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|partKeys
operator|.
name|length
argument_list|)
decl_stmt|;
name|partitionValues
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|partObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|partKeys
operator|.
name|length
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
name|partKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|partKeys
index|[
name|i
index|]
decl_stmt|;
name|partNames
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|partitionValues
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|partSpec
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|partObjectInspectors
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|)
expr_stmt|;
block|}
comment|// Create partition OI
name|StructObjectInspector
name|partObjectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|partNames
argument_list|,
name|partObjectInspectors
argument_list|)
decl_stmt|;
comment|// Get row OI from partition OI and raw row OI
name|StructObjectInspector
name|rowObjectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getUnionStructObjectInspector
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|StructObjectInspector
index|[]
block|{
name|partRawRowObjectInspector
block|,
name|partObjectInspector
block|}
argument_list|)
argument_list|)
decl_stmt|;
name|rowOI
operator|=
name|rowObjectInspector
expr_stmt|;
name|rawRowOI
operator|=
name|partRawRowObjectInspector
expr_stmt|;
block|}
else|else
block|{
comment|// No partitions for this table, hence row OI equals raw row OI
name|rowOI
operator|=
name|partRawRowObjectInspector
expr_stmt|;
name|rawRowOI
operator|=
name|partRawRowObjectInspector
expr_stmt|;
block|}
block|}
comment|/**    * Creates a Vectorized row batch and the column vectors.    *    * @return VectorizedRowBatch    * @throws HiveException    */
specifier|public
name|VectorizedRowBatch
name|CreateVectorizedRowBatch
parameter_list|()
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|rowOI
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|VectorizedRowBatch
name|result
init|=
operator|new
name|VectorizedRowBatch
argument_list|(
name|fieldRefs
operator|.
name|size
argument_list|()
argument_list|)
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
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|ObjectInspector
name|foi
init|=
name|fieldRefs
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|foi
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|foi
decl_stmt|;
comment|// Vectorization currently only supports the following data types:
comment|// BOOLEAN, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, STRING and TIMESTAMP
switch|switch
condition|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|TIMESTAMP
case|:
name|result
operator|.
name|cols
index|[
name|j
index|]
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
name|result
operator|.
name|cols
index|[
name|j
index|]
operator|=
operator|new
name|DoubleColumnVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|result
operator|.
name|cols
index|[
name|j
index|]
operator|=
operator|new
name|BytesColumnVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Vectorizaton is not supported for datatype:"
operator|+
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
break|break;
block|}
case|case
name|LIST
case|:
case|case
name|MAP
case|:
case|case
name|STRUCT
case|:
case|case
name|UNION
case|:
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Vectorizaton is not supported for datatype:"
operator|+
name|foi
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
default|default:
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unknown ObjectInspector category!"
argument_list|)
throw|;
block|}
block|}
name|result
operator|.
name|numCols
operator|=
name|fieldRefs
operator|.
name|size
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Adds the row to the batch after deserializing the row    *    * @param rowIndex    *          Row index in the batch to which the row is added    * @param rowBlob    *          Row blob (serialized version of row)    * @param batch    *          Vectorized batch to which the row is added    * @throws HiveException    * @throws SerDeException    */
specifier|public
name|void
name|AddRowToBatch
parameter_list|(
name|int
name|rowIndex
parameter_list|,
name|Writable
name|rowBlob
parameter_list|,
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
throws|,
name|SerDeException
block|{
name|Object
name|row
init|=
name|this
operator|.
name|deserializer
operator|.
name|deserialize
argument_list|(
name|rowBlob
argument_list|)
decl_stmt|;
name|VectorizedBatchUtil
operator|.
name|AddRowToBatch
argument_list|(
name|row
argument_list|,
name|this
operator|.
name|rawRowOI
argument_list|,
name|rowIndex
argument_list|,
name|batch
argument_list|)
expr_stmt|;
block|}
comment|/**    * Deserialized set of rows and populates the batch    *    * @param rowBlob    *          to deserialize    * @param batch    *          Vectorized row batch which contains deserialized data    * @throws SerDeException    */
specifier|public
name|void
name|ConvertRowBatchBlobToVectorizedBatch
parameter_list|(
name|Object
name|rowBlob
parameter_list|,
name|int
name|rowsInBlob
parameter_list|,
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|deserializer
operator|instanceof
name|VectorizedSerde
condition|)
block|{
operator|(
operator|(
name|VectorizedSerde
operator|)
name|deserializer
operator|)
operator|.
name|deserializeVector
argument_list|(
name|rowBlob
argument_list|,
name|rowsInBlob
argument_list|,
name|batch
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Not able to deserialize row batch. Serde does not implement VectorizedSerde"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|int
name|GetColIndexBasedOnColName
parameter_list|(
name|String
name|colName
parameter_list|)
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|rowOI
operator|.
name|getAllStructFieldRefs
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
name|fieldRefs
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
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
operator|==
name|colName
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Not able to find column name in row object inspector"
argument_list|)
throw|;
block|}
comment|/**    * Add the partition values to the batch    *    * @param batch    * @throws HiveException    */
specifier|public
name|void
name|AddPartitionColsToBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|colIndex
decl_stmt|;
name|String
name|value
decl_stmt|;
name|BytesColumnVector
name|bcv
decl_stmt|;
if|if
condition|(
name|partitionValues
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|key
range|:
name|partitionValues
operator|.
name|keySet
argument_list|()
control|)
block|{
name|colIndex
operator|=
name|GetColIndexBasedOnColName
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|value
operator|=
name|partitionValues
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|bcv
operator|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colIndex
index|]
expr_stmt|;
name|bcv
operator|.
name|setRef
argument_list|(
literal|0
argument_list|,
name|value
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|bcv
operator|.
name|isRepeating
operator|=
literal|true
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
name|bcv
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

