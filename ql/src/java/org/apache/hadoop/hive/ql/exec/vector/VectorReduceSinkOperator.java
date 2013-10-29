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
name|Random
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
name|ReduceSinkOperator
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
name|VectorExpression
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
name|ReduceSinkDesc
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
name|Serializer
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
name|Text
import|;
end_import

begin_class
specifier|public
class|class
name|VectorReduceSinkOperator
extends|extends
name|ReduceSinkOperator
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
name|VectorReduceSinkOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * The evaluators for the key columns. Key columns decide the sort order on    * the reducer side. Key columns are passed to the reducer in the "key".    */
specifier|protected
name|VectorExpression
index|[]
name|keyEval
decl_stmt|;
comment|/**    * The key value writers. These know how to write the necessary writable type    * based on key column metadata, from the primitive vector type.    */
specifier|protected
specifier|transient
name|VectorExpressionWriter
index|[]
name|keyWriters
decl_stmt|;
comment|/**    * The evaluators for the value columns. Value columns are passed to reducer    * in the "value".    */
specifier|protected
name|VectorExpression
index|[]
name|valueEval
decl_stmt|;
comment|/**    * The output value writers. These know how to write the necessary writable type    * based on value column metadata, from the primitive vector type.    */
specifier|protected
specifier|transient
name|VectorExpressionWriter
index|[]
name|valueWriters
decl_stmt|;
comment|/**    * The evaluators for the partition columns (CLUSTER BY or DISTRIBUTE BY in    * Hive language). Partition columns decide the reducer that the current row    * goes to. Partition columns are not passed to reducer.    */
specifier|protected
name|VectorExpression
index|[]
name|partitionEval
decl_stmt|;
comment|/**    * The partition value writers. These know how to write the necessary writable type    * based on partition column metadata, from the primitive vector type.    */
specifier|protected
specifier|transient
name|VectorExpressionWriter
index|[]
name|partitionWriters
decl_stmt|;
specifier|transient
name|ObjectInspector
name|keyObjectInspector
decl_stmt|;
specifier|transient
name|ObjectInspector
name|valueObjectInspector
decl_stmt|;
specifier|transient
name|int
index|[]
name|keyHashCode
init|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|public
name|VectorReduceSinkOperator
parameter_list|(
name|VectorizationContext
name|vContext
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|()
expr_stmt|;
name|ReduceSinkDesc
name|desc
init|=
operator|(
name|ReduceSinkDesc
operator|)
name|conf
decl_stmt|;
name|this
operator|.
name|conf
operator|=
name|desc
expr_stmt|;
name|keyEval
operator|=
name|vContext
operator|.
name|getVectorExpressions
argument_list|(
name|desc
operator|.
name|getKeyCols
argument_list|()
argument_list|)
expr_stmt|;
name|valueEval
operator|=
name|vContext
operator|.
name|getVectorExpressions
argument_list|(
name|desc
operator|.
name|getValueCols
argument_list|()
argument_list|)
expr_stmt|;
name|partitionEval
operator|=
name|vContext
operator|.
name|getVectorExpressions
argument_list|(
name|desc
operator|.
name|getPartitionCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|VectorReduceSinkOperator
parameter_list|()
block|{
name|super
argument_list|()
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
try|try
block|{
name|numDistributionKeys
operator|=
name|conf
operator|.
name|getNumDistributionKeys
argument_list|()
expr_stmt|;
name|distinctColIndices
operator|=
name|conf
operator|.
name|getDistinctColumnIndices
argument_list|()
expr_stmt|;
name|numDistinctExprs
operator|=
name|distinctColIndices
operator|.
name|size
argument_list|()
expr_stmt|;
name|TableDesc
name|keyTableDesc
init|=
name|conf
operator|.
name|getKeySerializeInfo
argument_list|()
decl_stmt|;
name|keySerializer
operator|=
operator|(
name|Serializer
operator|)
name|keyTableDesc
operator|.
name|getDeserializerClass
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|keySerializer
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|keyIsText
operator|=
name|keySerializer
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|equals
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
comment|/*        * Compute and assign the key writers and the key object inspector        */
name|VectorExpressionWriterFactory
operator|.
name|processVectorExpressions
argument_list|(
name|conf
operator|.
name|getKeyCols
argument_list|()
argument_list|,
name|conf
operator|.
name|getOutputKeyColumnNames
argument_list|()
argument_list|,
operator|new
name|VectorExpressionWriterFactory
operator|.
name|SingleOIDClosure
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|assign
parameter_list|(
name|VectorExpressionWriter
index|[]
name|writers
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|)
block|{
name|keyWriters
operator|=
name|writers
expr_stmt|;
name|keyObjectInspector
operator|=
name|objectInspector
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|String
name|colNames
init|=
literal|""
decl_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|conf
operator|.
name|getOutputKeyColumnNames
argument_list|()
control|)
block|{
name|colNames
operator|=
name|String
operator|.
name|format
argument_list|(
literal|"%s %s"
argument_list|,
name|colNames
argument_list|,
name|colName
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"keyObjectInspector [%s]%s => %s"
argument_list|,
name|keyObjectInspector
operator|.
name|getClass
argument_list|()
argument_list|,
name|keyObjectInspector
argument_list|,
name|colNames
argument_list|)
argument_list|)
expr_stmt|;
name|partitionWriters
operator|=
name|VectorExpressionWriterFactory
operator|.
name|getExpressionWriters
argument_list|(
name|conf
operator|.
name|getPartitionCols
argument_list|()
argument_list|)
expr_stmt|;
name|TableDesc
name|valueTableDesc
init|=
name|conf
operator|.
name|getValueSerializeInfo
argument_list|()
decl_stmt|;
name|valueSerializer
operator|=
operator|(
name|Serializer
operator|)
name|valueTableDesc
operator|.
name|getDeserializerClass
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|valueSerializer
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|valueTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
comment|/*        * Compute and assign the value writers and the value object inspector        */
name|VectorExpressionWriterFactory
operator|.
name|processVectorExpressions
argument_list|(
name|conf
operator|.
name|getValueCols
argument_list|()
argument_list|,
name|conf
operator|.
name|getOutputValueColumnNames
argument_list|()
argument_list|,
operator|new
name|VectorExpressionWriterFactory
operator|.
name|SingleOIDClosure
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|assign
parameter_list|(
name|VectorExpressionWriter
index|[]
name|writers
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|)
block|{
name|valueWriters
operator|=
name|writers
expr_stmt|;
name|valueObjectInspector
operator|=
name|objectInspector
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|colNames
operator|=
literal|""
expr_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|conf
operator|.
name|getOutputValueColumnNames
argument_list|()
control|)
block|{
name|colNames
operator|=
name|String
operator|.
name|format
argument_list|(
literal|"%s %s"
argument_list|,
name|colNames
argument_list|,
name|colName
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"valueObjectInspector [%s]%s => %s"
argument_list|,
name|valueObjectInspector
operator|.
name|getClass
argument_list|()
argument_list|,
name|valueObjectInspector
argument_list|,
name|colNames
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|numKeys
init|=
name|numDistinctExprs
operator|>
literal|0
condition|?
name|numDistinctExprs
else|:
literal|1
decl_stmt|;
name|int
name|keyLen
init|=
name|numDistinctExprs
operator|>
literal|0
condition|?
name|numDistributionKeys
operator|+
literal|1
else|:
name|numDistributionKeys
decl_stmt|;
name|cachedKeys
operator|=
operator|new
name|Object
index|[
name|numKeys
index|]
index|[
name|keyLen
index|]
expr_stmt|;
name|cachedValues
operator|=
operator|new
name|Object
index|[
name|valueEval
operator|.
name|length
index|]
expr_stmt|;
name|int
name|tag
init|=
name|conf
operator|.
name|getTag
argument_list|()
decl_stmt|;
name|tagByte
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|tag
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Using tag = "
operator|+
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
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|processOp
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
name|VectorizedRowBatch
name|vrg
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"sinking %d rows, %d values, %d keys, %d parts"
argument_list|,
name|vrg
operator|.
name|size
argument_list|,
name|valueEval
operator|.
name|length
argument_list|,
name|keyEval
operator|.
name|length
argument_list|,
name|partitionEval
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
try|try
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
name|partitionEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|partitionEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
block|}
comment|// run the vector evaluations
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valueEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|valueEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
block|}
comment|// Evaluate the keys
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keyEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|keyEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
block|}
name|Object
index|[]
name|distributionKeys
init|=
operator|new
name|Object
index|[
name|numDistributionKeys
index|]
decl_stmt|;
comment|// Emit a (k,v) pair for each row in the batch
comment|//
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|vrg
operator|.
name|size
condition|;
operator|++
name|j
control|)
block|{
name|int
name|rowIndex
init|=
name|j
decl_stmt|;
if|if
condition|(
name|vrg
operator|.
name|selectedInUse
condition|)
block|{
name|rowIndex
operator|=
name|vrg
operator|.
name|selected
index|[
name|j
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
name|valueEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|batchColumn
init|=
name|valueEval
index|[
name|i
index|]
operator|.
name|getOutputColumn
argument_list|()
decl_stmt|;
name|ColumnVector
name|vectorColumn
init|=
name|vrg
operator|.
name|cols
index|[
name|batchColumn
index|]
decl_stmt|;
name|cachedValues
index|[
name|i
index|]
operator|=
name|valueWriters
index|[
name|i
index|]
operator|.
name|writeValue
argument_list|(
name|vectorColumn
argument_list|,
name|rowIndex
argument_list|)
expr_stmt|;
block|}
comment|// Serialize the value
name|value
operator|=
name|valueSerializer
operator|.
name|serialize
argument_list|(
name|cachedValues
argument_list|,
name|valueObjectInspector
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
name|keyEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|batchColumn
init|=
name|keyEval
index|[
name|i
index|]
operator|.
name|getOutputColumn
argument_list|()
decl_stmt|;
name|ColumnVector
name|vectorColumn
init|=
name|vrg
operator|.
name|cols
index|[
name|batchColumn
index|]
decl_stmt|;
name|distributionKeys
index|[
name|i
index|]
operator|=
name|keyWriters
index|[
name|i
index|]
operator|.
name|writeValue
argument_list|(
name|vectorColumn
argument_list|,
name|rowIndex
argument_list|)
expr_stmt|;
block|}
comment|// no distinct key
name|System
operator|.
name|arraycopy
argument_list|(
name|distributionKeys
argument_list|,
literal|0
argument_list|,
name|cachedKeys
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
name|numDistributionKeys
argument_list|)
expr_stmt|;
comment|// Serialize the keys and append the tag
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cachedKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|keyIsText
condition|)
block|{
name|Text
name|key
init|=
operator|(
name|Text
operator|)
name|keySerializer
operator|.
name|serialize
argument_list|(
name|cachedKeys
index|[
name|i
index|]
argument_list|,
name|keyObjectInspector
argument_list|)
decl_stmt|;
if|if
condition|(
name|tag
operator|==
operator|-
literal|1
condition|)
block|{
name|keyWritable
operator|.
name|set
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|keyLength
init|=
name|key
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|keyWritable
operator|.
name|setSize
argument_list|(
name|keyLength
operator|+
literal|1
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
name|keyWritable
operator|.
name|get
argument_list|()
index|[
name|keyLength
index|]
operator|=
name|tagByte
index|[
literal|0
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Must be BytesWritable
name|BytesWritable
name|key
init|=
operator|(
name|BytesWritable
operator|)
name|keySerializer
operator|.
name|serialize
argument_list|(
name|cachedKeys
index|[
name|i
index|]
argument_list|,
name|keyObjectInspector
argument_list|)
decl_stmt|;
if|if
condition|(
name|tag
operator|==
operator|-
literal|1
condition|)
block|{
name|keyWritable
operator|.
name|set
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|keyLength
init|=
name|key
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|keyWritable
operator|.
name|setSize
argument_list|(
name|keyLength
operator|+
literal|1
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
name|keyWritable
operator|.
name|get
argument_list|()
index|[
name|keyLength
index|]
operator|=
name|tagByte
index|[
literal|0
index|]
expr_stmt|;
block|}
block|}
comment|// Evaluate the HashCode
name|int
name|keyHashCode
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|partitionEval
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// If no partition cols, just distribute the data uniformly to provide
comment|// better
comment|// load balance. If the requirement is to have a single reducer, we
comment|// should set
comment|// the number of reducers to 1.
comment|// Use a constant seed to make the code deterministic.
if|if
condition|(
name|random
operator|==
literal|null
condition|)
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|12345
argument_list|)
expr_stmt|;
block|}
name|keyHashCode
operator|=
name|random
operator|.
name|nextInt
argument_list|()
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|p
init|=
literal|0
init|;
name|p
operator|<
name|partitionEval
operator|.
name|length
condition|;
name|p
operator|++
control|)
block|{
name|ColumnVector
name|columnVector
init|=
name|vrg
operator|.
name|cols
index|[
name|partitionEval
index|[
name|p
index|]
operator|.
name|getOutputColumn
argument_list|()
index|]
decl_stmt|;
name|Object
name|partitionValue
init|=
name|partitionWriters
index|[
name|p
index|]
operator|.
name|writeValue
argument_list|(
name|columnVector
argument_list|,
name|rowIndex
argument_list|)
decl_stmt|;
name|keyHashCode
operator|=
name|keyHashCode
operator|*
literal|31
operator|+
name|ObjectInspectorUtils
operator|.
name|hashCode
argument_list|(
name|partitionValue
argument_list|,
name|partitionWriters
index|[
name|p
index|]
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|keyWritable
operator|.
name|setHashCode
argument_list|(
name|keyHashCode
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// Since this is a terminal operator, update counters explicitly -
comment|// forward is not called
if|if
condition|(
name|counterNameToEnum
operator|!=
literal|null
condition|)
block|{
operator|++
name|outputRows
expr_stmt|;
if|if
condition|(
name|outputRows
operator|%
literal|1000
operator|==
literal|0
condition|)
block|{
name|incrCounter
argument_list|(
name|numOutputRowsCntr
argument_list|,
name|outputRows
argument_list|)
expr_stmt|;
name|outputRows
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
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
name|e
argument_list|)
throw|;
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
specifier|static
specifier|public
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"RS"
return|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getPartitionEval
parameter_list|()
block|{
return|return
name|partitionEval
return|;
block|}
specifier|public
name|void
name|setPartitionEval
parameter_list|(
name|VectorExpression
index|[]
name|partitionEval
parameter_list|)
block|{
name|this
operator|.
name|partitionEval
operator|=
name|partitionEval
expr_stmt|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getValueEval
parameter_list|()
block|{
return|return
name|valueEval
return|;
block|}
specifier|public
name|void
name|setValueEval
parameter_list|(
name|VectorExpression
index|[]
name|valueEval
parameter_list|)
block|{
name|this
operator|.
name|valueEval
operator|=
name|valueEval
expr_stmt|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getKeyEval
parameter_list|()
block|{
return|return
name|keyEval
return|;
block|}
specifier|public
name|void
name|setKeyEval
parameter_list|(
name|VectorExpression
index|[]
name|keyEval
parameter_list|)
block|{
name|this
operator|.
name|keyEval
operator|=
name|keyEval
expr_stmt|;
block|}
block|}
end_class

end_unit

