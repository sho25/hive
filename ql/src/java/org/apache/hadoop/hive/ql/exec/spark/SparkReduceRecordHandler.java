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
name|spark
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
name|Iterator
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
name|MapredContext
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
name|ObjectCache
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
name|ObjectCacheFactory
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
name|OperatorUtils
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
name|mr
operator|.
name|ExecMapper
operator|.
name|ReportStats
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
name|mr
operator|.
name|ExecMapperContext
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
name|mr
operator|.
name|MapredLocalTask
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
name|MapredLocalWork
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
name|ReduceWork
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
name|mapred
operator|.
name|OutputCollector
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
name|Reporter
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

begin_comment
comment|/**  * Clone from ExecReducer, it is the bridge between the spark framework and  * the Hive operator pipeline at execution time. It's main responsibilities are:  *  * - Load and setup the operator pipeline from XML  * - Run the pipeline by transforming key, value pairs to records and forwarding them to the operators  * - Sending start and end group messages to separate records with same key from one another  * - Catch and handle errors during execution of the operators.  *  */
end_comment

begin_class
specifier|public
class|class
name|SparkReduceRecordHandler
extends|extends
name|SparkRecordHandler
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
name|SparkReduceRecordHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PLAN_KEY
init|=
literal|"__REDUCE_PLAN__"
decl_stmt|;
comment|// Input value serde needs to be an array to support different SerDe
comment|// for different tags
specifier|private
specifier|final
name|Deserializer
index|[]
name|inputValueDeserializer
init|=
operator|new
name|Deserializer
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
decl_stmt|;
specifier|private
specifier|final
name|Object
index|[]
name|valueObject
init|=
operator|new
name|Object
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
decl_stmt|;
specifier|private
specifier|final
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
specifier|final
name|boolean
name|isLogInfoEnabled
init|=
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
decl_stmt|;
comment|// TODO: move to DynamicSerDe when it's ready
specifier|private
name|Deserializer
name|inputKeyDeserializer
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
decl_stmt|;
specifier|private
name|boolean
name|isTagged
init|=
literal|false
decl_stmt|;
specifier|private
name|TableDesc
name|keyTableDesc
decl_stmt|;
specifier|private
name|TableDesc
index|[]
name|valueTableDesc
decl_stmt|;
specifier|private
name|ObjectInspector
index|[]
name|rowObjectInspector
decl_stmt|;
specifier|private
name|boolean
name|vectorized
init|=
literal|false
decl_stmt|;
comment|// runtime objects
specifier|private
specifier|transient
name|Object
name|keyObject
decl_stmt|;
specifier|private
specifier|transient
name|BytesWritable
name|groupKey
decl_stmt|;
specifier|private
name|DataOutputBuffer
name|buffer
decl_stmt|;
specifier|private
name|VectorizedRowBatch
index|[]
name|batches
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
index|[]
name|valueStructInspectors
decl_stmt|;
comment|/* this is only used in the error code path */
specifier|private
name|List
argument_list|<
name|VectorExpressionWriter
argument_list|>
index|[]
name|valueStringWriters
decl_stmt|;
specifier|private
name|MapredLocalWork
name|localWork
init|=
literal|null
decl_stmt|;
specifier|public
name|void
name|init
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|OutputCollector
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
block|{
name|super
operator|.
name|init
argument_list|(
name|job
argument_list|,
name|output
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|rowObjectInspector
operator|=
operator|new
name|ObjectInspector
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
expr_stmt|;
name|ObjectInspector
index|[]
name|valueObjectInspector
init|=
operator|new
name|ObjectInspector
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
decl_stmt|;
name|ObjectInspector
name|keyObjectInspector
decl_stmt|;
name|ObjectCache
name|cache
init|=
name|ObjectCacheFactory
operator|.
name|getCache
argument_list|(
name|jc
argument_list|)
decl_stmt|;
name|ReduceWork
name|gWork
init|=
operator|(
name|ReduceWork
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|PLAN_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|gWork
operator|==
literal|null
condition|)
block|{
name|gWork
operator|=
name|Utilities
operator|.
name|getReduceWork
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|cache
operator|.
name|cache
argument_list|(
name|PLAN_KEY
argument_list|,
name|gWork
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Utilities
operator|.
name|setReduceWork
argument_list|(
name|job
argument_list|,
name|gWork
argument_list|)
expr_stmt|;
block|}
name|reducer
operator|=
name|gWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
name|vectorized
operator|=
name|gWork
operator|.
name|getVectorMode
argument_list|()
expr_stmt|;
name|reducer
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// clear out any parents as reducer is the
comment|// root
name|isTagged
operator|=
name|gWork
operator|.
name|getNeedsTagging
argument_list|()
expr_stmt|;
try|try
block|{
name|keyTableDesc
operator|=
name|gWork
operator|.
name|getKeyDesc
argument_list|()
expr_stmt|;
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
name|valueTableDesc
operator|=
operator|new
name|TableDesc
index|[
name|gWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
if|if
condition|(
name|vectorized
condition|)
block|{
specifier|final
name|int
name|maxTags
init|=
name|gWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|keyStructInspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|keyObjectInspector
expr_stmt|;
name|batches
operator|=
operator|new
name|VectorizedRowBatch
index|[
name|maxTags
index|]
expr_stmt|;
name|valueStructInspectors
operator|=
operator|new
name|StructObjectInspector
index|[
name|maxTags
index|]
expr_stmt|;
name|valueStringWriters
operator|=
operator|(
name|List
argument_list|<
name|VectorExpressionWriter
argument_list|>
index|[]
operator|)
operator|new
name|List
index|[
name|maxTags
index|]
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
for|for
control|(
name|int
name|tag
init|=
literal|0
init|;
name|tag
operator|<
name|gWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|tag
operator|++
control|)
block|{
comment|// We should initialize the SerDe with the TypeInfo when available.
name|valueTableDesc
index|[
name|tag
index|]
operator|=
name|gWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|inputValueDeserializer
index|[
name|tag
index|]
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|valueTableDesc
index|[
name|tag
index|]
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
index|[
name|tag
index|]
argument_list|,
literal|null
argument_list|,
name|valueTableDesc
index|[
name|tag
index|]
operator|.
name|getProperties
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|valueObjectInspector
index|[
name|tag
index|]
operator|=
name|inputValueDeserializer
index|[
name|tag
index|]
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
index|[
name|tag
index|]
operator|=
operator|(
name|StructObjectInspector
operator|)
name|valueObjectInspector
index|[
name|tag
index|]
expr_stmt|;
name|batches
index|[
name|tag
index|]
operator|=
name|VectorizedBatchUtil
operator|.
name|constructVectorizedRowBatch
argument_list|(
name|keyStructInspector
argument_list|,
name|valueStructInspectors
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
specifier|final
name|int
name|totalColumns
init|=
name|keysColumnOffset
operator|+
name|valueStructInspectors
index|[
name|tag
index|]
operator|.
name|getAllStructFieldRefs
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|valueStringWriters
index|[
name|tag
index|]
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
index|[
name|tag
index|]
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
index|[
name|tag
index|]
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
index|[
name|tag
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|/* 	   * The row object inspector used by ReduceWork needs to be a 	   * **standard** struct object inspector, not just any struct object 	   * inspector. 	   */
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
index|[
name|tag
index|]
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
index|[
name|tag
index|]
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
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
comment|//reducer.setGroupKeyObjectInspector(keyObjectInspector);
name|rowObjectInspector
index|[
name|tag
index|]
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
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|ExecMapperContext
name|execContext
init|=
operator|new
name|ExecMapperContext
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|localWork
operator|=
name|gWork
operator|.
name|getMapRedLocalWork
argument_list|()
expr_stmt|;
name|execContext
operator|.
name|setJc
argument_list|(
name|jc
argument_list|)
expr_stmt|;
name|execContext
operator|.
name|setLocalWork
argument_list|(
name|localWork
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|setExecContext
argument_list|(
name|execContext
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|setReporter
argument_list|(
name|rp
argument_list|)
expr_stmt|;
name|OperatorUtils
operator|.
name|setChildrenCollector
argument_list|(
name|Arrays
operator|.
expr|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|>
name|asList
argument_list|(
name|reducer
argument_list|)
operator|,
name|output
argument_list|)
expr_stmt|;
comment|// initialize reduce operator tree
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
name|reducer
operator|.
name|dump
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
name|rowObjectInspector
argument_list|)
expr_stmt|;
if|if
condition|(
name|localWork
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|dummyOp
range|:
name|localWork
operator|.
name|getDummyParentOp
argument_list|()
control|)
block|{
name|dummyOp
operator|.
name|setExecContext
argument_list|(
name|execContext
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
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
block|}
annotation|@
name|Override
specifier|public
name|void
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Do not support this method in SparkReduceRecordHandler."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
name|Iterator
name|values
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|reducer
operator|.
name|getDone
argument_list|()
condition|)
block|{
return|return;
block|}
try|try
block|{
name|BytesWritable
name|keyWritable
init|=
operator|(
name|BytesWritable
operator|)
name|key
decl_stmt|;
name|byte
name|tag
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|isTagged
condition|)
block|{
comment|// remove the tag from key coming out of reducer
comment|// and store it in separate variable.
name|int
name|size
init|=
name|keyWritable
operator|.
name|getSize
argument_list|()
operator|-
literal|1
decl_stmt|;
name|tag
operator|=
name|keyWritable
operator|.
name|get
argument_list|()
index|[
name|size
index|]
expr_stmt|;
name|keyWritable
operator|.
name|setSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|keyWritable
operator|.
name|equals
argument_list|(
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
name|LOG
operator|.
name|trace
argument_list|(
literal|"End Group"
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
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
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|getSize
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
name|groupKey
operator|.
name|set
argument_list|(
name|keyWritable
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"Start Group"
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|setGroupKeyObject
argument_list|(
name|keyObject
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|startGroup
argument_list|()
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
name|values
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|processKeyValues
argument_list|(
name|values
argument_list|,
name|tag
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
name|LOG
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
comment|/**    * @param values    * @return true if it is not done and can take more inputs    */
specifier|private
name|boolean
name|processKeyValues
parameter_list|(
name|Iterator
name|values
parameter_list|,
name|byte
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// System.err.print(keyObject.toString());
while|while
condition|(
name|values
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|BytesWritable
name|valueWritable
init|=
operator|(
name|BytesWritable
operator|)
name|values
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// System.err.print(who.getHo().toString());
try|try
block|{
name|valueObject
index|[
name|tag
index|]
operator|=
name|inputValueDeserializer
index|[
name|tag
index|]
operator|.
name|deserialize
argument_list|(
name|valueWritable
argument_list|)
expr_stmt|;
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
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|valueWritable
operator|.
name|getSize
argument_list|()
argument_list|)
operator|+
literal|" with properties "
operator|+
name|valueTableDesc
index|[
name|tag
index|]
operator|.
name|getProperties
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|row
operator|.
name|clear
argument_list|()
expr_stmt|;
name|row
operator|.
name|add
argument_list|(
name|keyObject
argument_list|)
expr_stmt|;
name|row
operator|.
name|add
argument_list|(
name|valueObject
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|logMemoryInfo
argument_list|()
expr_stmt|;
block|}
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
index|[
name|tag
index|]
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
return|return
literal|true
return|;
comment|// give me more
block|}
comment|/**    * @param values    * @return true if it is not done and can take more inputs    */
specifier|private
name|boolean
name|processVectors
parameter_list|(
name|Iterator
name|values
parameter_list|,
name|byte
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorizedRowBatch
name|batch
init|=
name|batches
index|[
name|tag
index|]
decl_stmt|;
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
while|while
condition|(
name|values
operator|.
name|hasNext
argument_list|()
condition|)
block|{
comment|/* deserialize value into columns */
name|BytesWritable
name|valueWritable
init|=
operator|(
name|BytesWritable
operator|)
name|values
operator|.
name|next
argument_list|()
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
index|[
name|tag
index|]
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
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|logMemoryInfo
argument_list|()
expr_stmt|;
block|}
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
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|logMemoryInfo
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
index|[
name|tag
index|]
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
return|return
literal|true
return|;
comment|// give me more
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
index|[
name|tag
index|]
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
index|[
name|tag
index|]
operator|.
name|getProperties
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// No row was processed
if|if
condition|(
name|oc
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Close called without any rows processed"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|groupKey
operator|!=
literal|null
condition|)
block|{
comment|// If a operator wants to do some work at the end of a group
name|LOG
operator|.
name|trace
argument_list|(
literal|"End Group"
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|logCloseInfo
argument_list|()
expr_stmt|;
block|}
name|reducer
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
if|if
condition|(
name|localWork
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|dummyOp
range|:
name|localWork
operator|.
name|getDummyParentOp
argument_list|()
control|)
block|{
name|dummyOp
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
block|}
name|ReportStats
name|rps
init|=
operator|new
name|ReportStats
argument_list|(
name|rp
argument_list|,
name|jc
argument_list|)
decl_stmt|;
name|reducer
operator|.
name|preorderMap
argument_list|(
name|rps
argument_list|)
expr_stmt|;
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
name|LOG
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
finally|finally
block|{
name|MapredContext
operator|.
name|close
argument_list|()
expr_stmt|;
name|Utilities
operator|.
name|clearWorkMap
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getDone
parameter_list|()
block|{
return|return
name|reducer
operator|.
name|getDone
argument_list|()
return|;
block|}
block|}
end_class

end_unit

