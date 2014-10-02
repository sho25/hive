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
name|tez
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
name|Arrays
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
name|ql
operator|.
name|exec
operator|.
name|CommonMergeJoinOperator
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
name|Operator
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedBatchUtil
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedRowBatchCtx
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
name|log
operator|.
name|PerfLogger
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
name|SerDe
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
name|SerDeUtils
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
name|BytesWritable
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
name|DataOutputBuffer
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ReflectionUtils
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|library
operator|.
name|api
operator|.
name|KeyValuesReader
import|;
end_import

begin_comment
comment|/**  * Process input from tez LogicalInput and write output - for a map plan  * Just pump the records through the query plan.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|ReduceRecordSource
implements|implements
name|RecordSource
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ReduceRecordSource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|ReduceRecordSource
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
name|byte
name|tag
decl_stmt|;
specifier|private
name|boolean
name|abort
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|Deserializer
name|inputKeyDeserializer
decl_stmt|;
comment|// Input value serde needs to be an array to support different SerDe
comment|// for different tags
specifier|private
name|SerDe
name|inputValueDeserializer
decl_stmt|;
name|TableDesc
name|keyTableDesc
decl_stmt|;
name|TableDesc
name|valueTableDesc
decl_stmt|;
name|ObjectInspector
name|rowObjectInspector
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
decl_stmt|;
specifier|private
name|Object
name|keyObject
init|=
literal|null
decl_stmt|;
specifier|private
name|BytesWritable
name|groupKey
decl_stmt|;
specifier|private
name|boolean
name|vectorized
init|=
literal|false
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|row
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|Utilities
operator|.
name|reduceFieldNameList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|DataOutputBuffer
name|buffer
decl_stmt|;
specifier|private
name|VectorizedRowBatchCtx
name|batchContext
decl_stmt|;
specifier|private
name|VectorizedRowBatch
name|batch
decl_stmt|;
comment|// number of columns pertaining to keys in a vectorized row batch
specifier|private
name|int
name|keysColumnOffset
decl_stmt|;
specifier|private
specifier|final
name|int
name|BATCH_SIZE
init|=
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
decl_stmt|;
specifier|private
name|StructObjectInspector
name|keyStructInspector
decl_stmt|;
specifier|private
name|StructObjectInspector
name|valueStructInspectors
decl_stmt|;
comment|/* this is only used in the error code path */
specifier|private
name|List
argument_list|<
name|VectorExpressionWriter
argument_list|>
name|valueStringWriters
decl_stmt|;
specifier|private
name|KeyValuesReader
name|reader
decl_stmt|;
specifier|private
name|boolean
name|handleGroupKey
decl_stmt|;
specifier|private
name|ObjectInspector
name|valueObjectInspector
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|PerfLogger
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|private
name|Iterable
argument_list|<
name|Object
argument_list|>
name|valueWritables
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|grouped
init|=
literal|true
decl_stmt|;
name|void
name|init
parameter_list|(
name|JobConf
name|jconf
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
parameter_list|,
name|boolean
name|vectorized
parameter_list|,
name|TableDesc
name|keyTableDesc
parameter_list|,
name|TableDesc
name|valueTableDesc
parameter_list|,
name|KeyValuesReader
name|reader
parameter_list|,
name|boolean
name|handleGroupKey
parameter_list|,
name|byte
name|tag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|>
name|scratchColumnVectorTypes
parameter_list|)
throws|throws
name|Exception
block|{
name|ObjectInspector
name|keyObjectInspector
decl_stmt|;
name|this
operator|.
name|reducer
operator|=
name|reducer
expr_stmt|;
name|this
operator|.
name|vectorized
operator|=
name|vectorized
expr_stmt|;
name|this
operator|.
name|keyTableDesc
operator|=
name|keyTableDesc
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|handleGroupKey
operator|=
name|handleGroupKey
expr_stmt|;
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
try|try
block|{
name|inputKeyDeserializer
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|keyTableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|inputKeyDeserializer
argument_list|,
literal|null
argument_list|,
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|keyObjectInspector
operator|=
name|inputKeyDeserializer
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
if|if
condition|(
name|vectorized
condition|)
block|{
name|keyStructInspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|keyObjectInspector
expr_stmt|;
name|keysColumnOffset
operator|=
name|keyStructInspector
operator|.
name|getAllStructFieldRefs
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|buffer
operator|=
operator|new
name|DataOutputBuffer
argument_list|()
expr_stmt|;
block|}
comment|// We should initialize the SerDe with the TypeInfo when available.
name|this
operator|.
name|valueTableDesc
operator|=
name|valueTableDesc
expr_stmt|;
name|inputValueDeserializer
operator|=
operator|(
name|SerDe
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|valueTableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|inputValueDeserializer
argument_list|,
literal|null
argument_list|,
name|valueTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|valueObjectInspector
operator|=
name|inputValueDeserializer
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|vectorized
condition|)
block|{
comment|/* vectorization only works with struct object inspectors */
name|valueStructInspectors
operator|=
operator|(
name|StructObjectInspector
operator|)
name|valueObjectInspector
expr_stmt|;
specifier|final
name|int
name|totalColumns
init|=
name|keysColumnOffset
operator|+
name|valueStructInspectors
operator|.
name|getAllStructFieldRefs
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|valueStringWriters
operator|=
operator|new
name|ArrayList
argument_list|<
name|VectorExpressionWriter
argument_list|>
argument_list|(
name|totalColumns
argument_list|)
expr_stmt|;
name|valueStringWriters
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|VectorExpressionWriterFactory
operator|.
name|genVectorStructExpressionWritables
argument_list|(
name|keyStructInspector
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|valueStringWriters
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|VectorExpressionWriterFactory
operator|.
name|genVectorStructExpressionWritables
argument_list|(
name|valueStructInspectors
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|/*          * The row object inspector used by ReduceWork needs to be a **standard**          * struct object inspector, not just any struct object inspector.          */
name|ArrayList
argument_list|<
name|String
argument_list|>
name|colNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|keyStructInspector
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
for|for
control|(
name|StructField
name|field
range|:
name|fields
control|)
block|{
name|colNames
operator|.
name|add
argument_list|(
name|Utilities
operator|.
name|ReduceField
operator|.
name|KEY
operator|.
name|toString
argument_list|()
operator|+
literal|"."
operator|+
name|field
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fields
operator|=
name|valueStructInspectors
operator|.
name|getAllStructFieldRefs
argument_list|()
expr_stmt|;
for|for
control|(
name|StructField
name|field
range|:
name|fields
control|)
block|{
name|colNames
operator|.
name|add
argument_list|(
name|Utilities
operator|.
name|ReduceField
operator|.
name|VALUE
operator|.
name|toString
argument_list|()
operator|+
literal|"."
operator|+
name|field
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rowObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|colNames
argument_list|,
name|ois
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|reduceShuffleScratchColumnTypeMap
init|=
name|scratchColumnVectorTypes
operator|.
name|get
argument_list|(
literal|"_REDUCE_SHUFFLE_"
argument_list|)
decl_stmt|;
name|batchContext
operator|=
operator|new
name|VectorizedRowBatchCtx
argument_list|()
expr_stmt|;
name|batchContext
operator|.
name|init
argument_list|(
name|reduceShuffleScratchColumnTypeMap
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|rowObjectInspector
argument_list|)
expr_stmt|;
name|batch
operator|=
name|batchContext
operator|.
name|createVectorizedRowBatch
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|ois
operator|.
name|add
argument_list|(
name|keyObjectInspector
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|valueObjectInspector
argument_list|)
expr_stmt|;
name|rowObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|Utilities
operator|.
name|reduceFieldNameList
argument_list|,
name|ois
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Reduce operator initialization failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_INIT_OPERATORS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|boolean
name|isGrouped
parameter_list|()
block|{
return|return
name|grouped
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|pushRecord
parameter_list|()
throws|throws
name|HiveException
block|{
name|BytesWritable
name|keyWritable
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|reader
operator|.
name|next
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
name|keyWritable
operator|=
operator|(
name|BytesWritable
operator|)
name|reader
operator|.
name|getCurrentKey
argument_list|()
expr_stmt|;
name|valueWritables
operator|=
name|reader
operator|.
name|getCurrentValues
argument_list|()
expr_stmt|;
block|}
comment|//Set the key, check if this is a new group or same group
try|try
block|{
name|keyObject
operator|=
name|inputKeyDeserializer
operator|.
name|deserialize
argument_list|(
name|keyWritable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive Runtime Error: Unable to deserialize reduce input key from "
operator|+
name|Utilities
operator|.
name|formatBinaryString
argument_list|(
name|keyWritable
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|getLength
argument_list|()
argument_list|)
operator|+
literal|" with properties "
operator|+
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|handleGroupKey
operator|&&
operator|!
name|keyWritable
operator|.
name|equals
argument_list|(
name|this
operator|.
name|groupKey
argument_list|)
condition|)
block|{
comment|// If a operator wants to do some work at the beginning of a group
if|if
condition|(
name|groupKey
operator|==
literal|null
condition|)
block|{
comment|// the first group
name|this
operator|.
name|groupKey
operator|=
operator|new
name|BytesWritable
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// If a operator wants to do some work at the end of a group
name|reducer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
name|groupKey
operator|.
name|set
argument_list|(
name|keyWritable
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|startGroup
argument_list|()
expr_stmt|;
name|reducer
operator|.
name|setGroupKeyObject
argument_list|(
name|keyObject
argument_list|)
expr_stmt|;
block|}
comment|/* this.keyObject passed via reference */
if|if
condition|(
name|vectorized
condition|)
block|{
name|processVectors
argument_list|(
name|valueWritables
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|processKeyValues
argument_list|(
name|valueWritables
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
operator|)
name|e
throw|;
block|}
else|else
block|{
name|l4j
operator|.
name|fatal
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|Object
name|deserializeValue
parameter_list|(
name|BytesWritable
name|valueWritable
parameter_list|,
name|byte
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
return|return
name|inputValueDeserializer
operator|.
name|deserialize
argument_list|(
name|valueWritable
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive Runtime Error: Unable to deserialize reduce input value (tag="
operator|+
name|tag
operator|+
literal|") from "
operator|+
name|Utilities
operator|.
name|formatBinaryString
argument_list|(
name|valueWritable
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|valueWritable
operator|.
name|getLength
argument_list|()
argument_list|)
operator|+
literal|" with properties "
operator|+
name|valueTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * @param values    * @return true if it is not done and can take more inputs    */
specifier|private
name|void
name|processKeyValues
parameter_list|(
name|Iterable
argument_list|<
name|Object
argument_list|>
name|values
parameter_list|,
name|byte
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|passDownKey
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Object
name|value
range|:
name|values
control|)
block|{
name|BytesWritable
name|valueWritable
init|=
operator|(
name|BytesWritable
operator|)
name|value
decl_stmt|;
name|row
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|passDownKey
operator|==
literal|null
condition|)
block|{
name|row
operator|.
name|add
argument_list|(
name|this
operator|.
name|keyObject
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|row
operator|.
name|add
argument_list|(
name|passDownKey
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|passDownKey
operator|==
literal|null
operator|)
operator|&&
operator|(
name|reducer
operator|instanceof
name|CommonMergeJoinOperator
operator|)
condition|)
block|{
name|passDownKey
operator|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|row
argument_list|,
name|reducer
operator|.
name|getInputObjInspectors
argument_list|()
index|[
name|tag
index|]
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
name|row
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|row
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|passDownKey
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|row
operator|.
name|add
argument_list|(
name|deserializeValue
argument_list|(
name|valueWritable
argument_list|,
name|tag
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|reducer
operator|.
name|processOp
argument_list|(
name|row
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|rowString
init|=
literal|null
decl_stmt|;
try|try
block|{
name|rowString
operator|=
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|row
argument_list|,
name|rowObjectInspector
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e2
parameter_list|)
block|{
name|rowString
operator|=
literal|"[Error getting row data with exception "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e2
argument_list|)
operator|+
literal|" ]"
expr_stmt|;
block|}
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive Runtime Error while processing row (tag="
operator|+
name|tag
operator|+
literal|") "
operator|+
name|rowString
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * @param values    * @return true if it is not done and can take more inputs    */
specifier|private
name|void
name|processVectors
parameter_list|(
name|Iterable
argument_list|<
name|Object
argument_list|>
name|values
parameter_list|,
name|byte
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|batch
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|/* deserialize key into columns */
name|VectorizedBatchUtil
operator|.
name|addRowToBatchFrom
argument_list|(
name|keyObject
argument_list|,
name|keyStructInspector
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|batch
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keysColumnOffset
condition|;
name|i
operator|++
control|)
block|{
name|VectorizedBatchUtil
operator|.
name|setRepeatingColumn
argument_list|(
name|batch
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|int
name|rowIdx
init|=
literal|0
decl_stmt|;
try|try
block|{
for|for
control|(
name|Object
name|value
range|:
name|values
control|)
block|{
comment|/* deserialize value into columns */
name|BytesWritable
name|valueWritable
init|=
operator|(
name|BytesWritable
operator|)
name|value
decl_stmt|;
name|Object
name|valueObj
init|=
name|deserializeValue
argument_list|(
name|valueWritable
argument_list|,
name|tag
argument_list|)
decl_stmt|;
name|VectorizedBatchUtil
operator|.
name|addRowToBatchFrom
argument_list|(
name|valueObj
argument_list|,
name|valueStructInspectors
argument_list|,
name|rowIdx
argument_list|,
name|keysColumnOffset
argument_list|,
name|batch
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
name|rowIdx
operator|++
expr_stmt|;
if|if
condition|(
name|rowIdx
operator|>=
name|BATCH_SIZE
condition|)
block|{
name|VectorizedBatchUtil
operator|.
name|setBatchSize
argument_list|(
name|batch
argument_list|,
name|rowIdx
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|processOp
argument_list|(
name|batch
argument_list|,
name|tag
argument_list|)
expr_stmt|;
name|rowIdx
operator|=
literal|0
expr_stmt|;
block|}
block|}
if|if
condition|(
name|rowIdx
operator|>
literal|0
condition|)
block|{
name|VectorizedBatchUtil
operator|.
name|setBatchSize
argument_list|(
name|batch
argument_list|,
name|rowIdx
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|processOp
argument_list|(
name|batch
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|rowString
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|/* batch.toString depends on this */
name|batch
operator|.
name|setValueWriters
argument_list|(
name|valueStringWriters
operator|.
name|toArray
argument_list|(
operator|new
name|VectorExpressionWriter
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|rowString
operator|=
name|batch
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e2
parameter_list|)
block|{
name|rowString
operator|=
literal|"[Error getting row data with exception "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e2
argument_list|)
operator|+
literal|" ]"
expr_stmt|;
block|}
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive Runtime Error while processing vector batch (tag="
operator|+
name|tag
operator|+
literal|") "
operator|+
name|rowString
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|boolean
name|close
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
if|if
condition|(
name|handleGroupKey
operator|&&
name|groupKey
operator|!=
literal|null
condition|)
block|{
comment|// If a operator wants to do some work at the end of a group
name|reducer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|abort
condition|)
block|{
comment|// signal new failure to map-reduce
name|l4j
operator|.
name|error
argument_list|(
literal|"Hit error while closing operators - failing tree"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive Runtime Error while closing operators: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|abort
return|;
block|}
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
block|{
return|return
name|rowObjectInspector
return|;
block|}
block|}
end_class

end_unit

