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
operator|.
name|reducesink
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
name|Arrays
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
name|ReduceSinkOperator
operator|.
name|Counter
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
name|TerminalOperator
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
name|VectorSerializeRow
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
name|keyseries
operator|.
name|VectorKeySeriesSerialized
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
name|HiveKey
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
name|BaseWork
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
name|VectorReduceSinkDesc
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
name|VectorReduceSinkInfo
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
name|serde
operator|.
name|serdeConstants
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
name|ByteStream
operator|.
name|Output
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
name|binarysortable
operator|.
name|BinarySortableSerDe
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
name|binarysortable
operator|.
name|fast
operator|.
name|BinarySortableSerializeWrite
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
name|lazybinary
operator|.
name|fast
operator|.
name|LazyBinarySerializeWrite
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
name|OutputCollector
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
name|HashCodeUtil
import|;
end_import

begin_comment
comment|/**  * This class is common operator class for native vectorized reduce sink.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorReduceSinkCommonOperator
extends|extends
name|TerminalOperator
argument_list|<
name|ReduceSinkDesc
argument_list|>
implements|implements
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
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|VectorReduceSinkCommonOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
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
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|protected
name|VectorReduceSinkDesc
name|vectorDesc
decl_stmt|;
comment|/**    * Information about our native vectorized reduce sink created by the Vectorizer class during    * it decision process and useful for execution.    */
specifier|protected
name|VectorReduceSinkInfo
name|vectorReduceSinkInfo
decl_stmt|;
specifier|protected
name|VectorizationContext
name|vContext
decl_stmt|;
comment|/**    * Reduce sink key vector expressions.    */
comment|// This is map of which vectorized row batch columns are the key columns.
comment|// And, their types.
specifier|protected
name|int
index|[]
name|reduceSinkKeyColumnMap
decl_stmt|;
specifier|protected
name|TypeInfo
index|[]
name|reduceSinkKeyTypeInfos
decl_stmt|;
comment|// Optional vectorized key expressions that need to be run on each batch.
specifier|protected
name|VectorExpression
index|[]
name|reduceSinkKeyExpressions
decl_stmt|;
comment|// This is map of which vectorized row batch columns are the value columns.
comment|// And, their types.
specifier|protected
name|int
index|[]
name|reduceSinkValueColumnMap
decl_stmt|;
specifier|protected
name|TypeInfo
index|[]
name|reduceSinkValueTypeInfos
decl_stmt|;
comment|// Optional vectorized value expressions that need to be run on each batch.
specifier|protected
name|VectorExpression
index|[]
name|reduceSinkValueExpressions
decl_stmt|;
comment|// The above members are initialized by the constructor and must not be
comment|// transient.
comment|//---------------------------------------------------------------------------
comment|// Whether there is to be a tag added to the end of each key and the tag value.
specifier|private
specifier|transient
name|boolean
name|reduceSkipTag
decl_stmt|;
specifier|private
specifier|transient
name|byte
name|reduceTagByte
decl_stmt|;
comment|// Binary sortable key serializer.
specifier|protected
specifier|transient
name|BinarySortableSerializeWrite
name|keyBinarySortableSerializeWrite
decl_stmt|;
comment|// The serialized all null key and its hash code.
specifier|private
specifier|transient
name|byte
index|[]
name|nullBytes
decl_stmt|;
specifier|private
specifier|transient
name|int
name|nullKeyHashCode
decl_stmt|;
comment|// Lazy binary value serializer.
specifier|private
specifier|transient
name|LazyBinarySerializeWrite
name|valueLazyBinarySerializeWrite
decl_stmt|;
comment|// This helper object serializes LazyBinary format reducer values from columns of a row
comment|// in a vectorized row batch.
specifier|private
specifier|transient
name|VectorSerializeRow
argument_list|<
name|LazyBinarySerializeWrite
argument_list|>
name|valueVectorSerializeRow
decl_stmt|;
comment|// The output buffer used to serialize a value into.
specifier|private
specifier|transient
name|Output
name|valueOutput
decl_stmt|;
comment|// The hive key and bytes writable value needed to pass the key and value to the collector.
specifier|private
specifier|transient
name|HiveKey
name|keyWritable
decl_stmt|;
specifier|private
specifier|transient
name|BytesWritable
name|valueBytesWritable
decl_stmt|;
comment|// Where to write our key and value pairs.
specifier|private
specifier|transient
name|OutputCollector
name|out
decl_stmt|;
comment|// The object that determines equal key series.
specifier|protected
specifier|transient
name|VectorKeySeriesSerialized
name|serializedKeySeries
decl_stmt|;
specifier|private
specifier|transient
name|long
name|numRows
init|=
literal|0
decl_stmt|;
specifier|private
specifier|transient
name|long
name|cntr
init|=
literal|1
decl_stmt|;
specifier|private
specifier|transient
name|long
name|logEveryNRows
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|LongWritable
name|recordCounter
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
comment|// For debug tracing: the name of the map or reduce task.
specifier|protected
specifier|transient
name|String
name|taskName
decl_stmt|;
comment|// Debug display.
specifier|protected
specifier|transient
name|long
name|batchCounter
decl_stmt|;
comment|//---------------------------------------------------------------------------
comment|/** Kryo ctor. */
specifier|protected
name|VectorReduceSinkCommonOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorReduceSinkCommonOperator
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
specifier|public
name|VectorReduceSinkCommonOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
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
argument_list|(
name|ctx
argument_list|)
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
name|vectorDesc
operator|=
operator|(
name|VectorReduceSinkDesc
operator|)
name|desc
operator|.
name|getVectorDesc
argument_list|()
expr_stmt|;
name|vectorReduceSinkInfo
operator|=
name|vectorDesc
operator|.
name|getVectorReduceSinkInfo
argument_list|()
expr_stmt|;
name|this
operator|.
name|vContext
operator|=
name|vContext
expr_stmt|;
comment|// Since a key expression can be a calculation and the key will go into a scratch column,
comment|// we need the mapping and type information.
name|reduceSinkKeyColumnMap
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkKeyColumnMap
argument_list|()
expr_stmt|;
name|reduceSinkKeyTypeInfos
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkKeyTypeInfos
argument_list|()
expr_stmt|;
name|reduceSinkKeyExpressions
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkKeyExpressions
argument_list|()
expr_stmt|;
name|reduceSinkValueColumnMap
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkValueColumnMap
argument_list|()
expr_stmt|;
name|reduceSinkValueTypeInfos
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkValueTypeInfos
argument_list|()
expr_stmt|;
name|reduceSinkValueExpressions
operator|=
name|vectorReduceSinkInfo
operator|.
name|getReduceSinkValueExpressions
argument_list|()
expr_stmt|;
block|}
comment|// Get the sort order
specifier|private
name|boolean
index|[]
name|getColumnSortOrder
parameter_list|(
name|Properties
name|properties
parameter_list|,
name|int
name|columnCount
parameter_list|)
block|{
name|String
name|columnSortOrder
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_SORT_ORDER
argument_list|)
decl_stmt|;
name|boolean
index|[]
name|columnSortOrderIsDesc
init|=
operator|new
name|boolean
index|[
name|columnCount
index|]
decl_stmt|;
if|if
condition|(
name|columnSortOrder
operator|==
literal|null
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|columnSortOrderIsDesc
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
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
name|columnSortOrderIsDesc
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|columnSortOrderIsDesc
index|[
name|i
index|]
operator|=
operator|(
name|columnSortOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'-'
operator|)
expr_stmt|;
block|}
block|}
return|return
name|columnSortOrderIsDesc
return|;
block|}
specifier|private
name|byte
index|[]
name|getColumnNullMarker
parameter_list|(
name|Properties
name|properties
parameter_list|,
name|int
name|columnCount
parameter_list|,
name|boolean
index|[]
name|columnSortOrder
parameter_list|)
block|{
name|String
name|columnNullOrder
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_SORT_ORDER
argument_list|)
decl_stmt|;
name|byte
index|[]
name|columnNullMarker
init|=
operator|new
name|byte
index|[
name|columnCount
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
name|columnNullMarker
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|columnSortOrder
index|[
name|i
index|]
condition|)
block|{
comment|// Descending
if|if
condition|(
name|columnNullOrder
operator|!=
literal|null
operator|&&
name|columnNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'a'
condition|)
block|{
comment|// Null first
name|columnNullMarker
index|[
name|i
index|]
operator|=
name|BinarySortableSerDe
operator|.
name|ONE
expr_stmt|;
block|}
else|else
block|{
comment|// Null last (default for descending order)
name|columnNullMarker
index|[
name|i
index|]
operator|=
name|BinarySortableSerDe
operator|.
name|ZERO
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Ascending
if|if
condition|(
name|columnNullOrder
operator|!=
literal|null
operator|&&
name|columnNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'z'
condition|)
block|{
comment|// Null last
name|columnNullMarker
index|[
name|i
index|]
operator|=
name|BinarySortableSerDe
operator|.
name|ONE
expr_stmt|;
block|}
else|else
block|{
comment|// Null first (default for ascending order)
name|columnNullMarker
index|[
name|i
index|]
operator|=
name|BinarySortableSerDe
operator|.
name|ZERO
expr_stmt|;
block|}
block|}
block|}
return|return
name|columnNullMarker
return|;
block|}
specifier|private
name|byte
index|[]
name|getColumnNotNullMarker
parameter_list|(
name|Properties
name|properties
parameter_list|,
name|int
name|columnCount
parameter_list|,
name|boolean
index|[]
name|columnSortOrder
parameter_list|)
block|{
name|String
name|columnNullOrder
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_SORT_ORDER
argument_list|)
decl_stmt|;
name|byte
index|[]
name|columnNotNullMarker
init|=
operator|new
name|byte
index|[
name|columnCount
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
name|columnNotNullMarker
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|columnSortOrder
index|[
name|i
index|]
condition|)
block|{
comment|// Descending
if|if
condition|(
name|columnNullOrder
operator|!=
literal|null
operator|&&
name|columnNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'a'
condition|)
block|{
comment|// Null first
name|columnNotNullMarker
index|[
name|i
index|]
operator|=
name|BinarySortableSerDe
operator|.
name|ZERO
expr_stmt|;
block|}
else|else
block|{
comment|// Null last (default for descending order)
name|columnNotNullMarker
index|[
name|i
index|]
operator|=
name|BinarySortableSerDe
operator|.
name|ONE
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Ascending
if|if
condition|(
name|columnNullOrder
operator|!=
literal|null
operator|&&
name|columnNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'z'
condition|)
block|{
comment|// Null last
name|columnNotNullMarker
index|[
name|i
index|]
operator|=
name|BinarySortableSerDe
operator|.
name|ZERO
expr_stmt|;
block|}
else|else
block|{
comment|// Null first (default for ascending order)
name|columnNotNullMarker
index|[
name|i
index|]
operator|=
name|BinarySortableSerDe
operator|.
name|ONE
expr_stmt|;
block|}
block|}
block|}
return|return
name|columnNotNullMarker
return|;
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
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
comment|// Determine the name of our map or reduce task for debug tracing.
name|BaseWork
name|work
init|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
if|if
condition|(
name|work
operator|==
literal|null
condition|)
block|{
name|work
operator|=
name|Utilities
operator|.
name|getReduceWork
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
name|taskName
operator|=
name|work
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|String
name|context
init|=
name|hconf
operator|.
name|get
argument_list|(
name|Operator
operator|.
name|CONTEXT_NAME_KEY
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|context
operator|!=
literal|null
operator|&&
operator|!
name|context
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|context
operator|=
literal|"_"
operator|+
name|context
operator|.
name|replace
argument_list|(
literal|" "
argument_list|,
literal|"_"
argument_list|)
expr_stmt|;
block|}
name|statsMap
operator|.
name|put
argument_list|(
name|Counter
operator|.
name|RECORDS_OUT_INTERMEDIATE
operator|+
name|context
argument_list|,
name|recordCounter
argument_list|)
expr_stmt|;
name|reduceSkipTag
operator|=
name|conf
operator|.
name|getSkipTag
argument_list|()
expr_stmt|;
name|reduceTagByte
operator|=
operator|(
name|byte
operator|)
name|conf
operator|.
name|getTag
argument_list|()
expr_stmt|;
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Using tag = "
operator|+
operator|(
name|int
operator|)
name|reduceTagByte
argument_list|)
expr_stmt|;
block|}
name|TableDesc
name|keyTableDesc
init|=
name|conf
operator|.
name|getKeySerializeInfo
argument_list|()
decl_stmt|;
name|boolean
index|[]
name|columnSortOrder
init|=
name|getColumnSortOrder
argument_list|(
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
name|reduceSinkKeyColumnMap
operator|.
name|length
argument_list|)
decl_stmt|;
name|byte
index|[]
name|columnNullMarker
init|=
name|getColumnNullMarker
argument_list|(
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
name|reduceSinkKeyColumnMap
operator|.
name|length
argument_list|,
name|columnSortOrder
argument_list|)
decl_stmt|;
name|byte
index|[]
name|columnNotNullMarker
init|=
name|getColumnNotNullMarker
argument_list|(
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
name|reduceSinkKeyColumnMap
operator|.
name|length
argument_list|,
name|columnSortOrder
argument_list|)
decl_stmt|;
name|keyBinarySortableSerializeWrite
operator|=
operator|new
name|BinarySortableSerializeWrite
argument_list|(
name|columnSortOrder
argument_list|,
name|columnNullMarker
argument_list|,
name|columnNotNullMarker
argument_list|)
expr_stmt|;
comment|// Create all nulls key.
try|try
block|{
name|Output
name|nullKeyOutput
init|=
operator|new
name|Output
argument_list|()
decl_stmt|;
name|keyBinarySortableSerializeWrite
operator|.
name|set
argument_list|(
name|nullKeyOutput
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
name|reduceSinkKeyColumnMap
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|keyBinarySortableSerializeWrite
operator|.
name|writeNull
argument_list|()
expr_stmt|;
block|}
name|int
name|nullBytesLength
init|=
name|nullKeyOutput
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|nullBytes
operator|=
operator|new
name|byte
index|[
name|nullBytesLength
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|nullKeyOutput
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullBytes
argument_list|,
literal|0
argument_list|,
name|nullBytesLength
argument_list|)
expr_stmt|;
name|nullKeyHashCode
operator|=
name|HashCodeUtil
operator|.
name|calculateBytesHashCode
argument_list|(
name|nullBytes
argument_list|,
literal|0
argument_list|,
name|nullBytesLength
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
name|valueLazyBinarySerializeWrite
operator|=
operator|new
name|LazyBinarySerializeWrite
argument_list|(
name|reduceSinkValueColumnMap
operator|.
name|length
argument_list|)
expr_stmt|;
name|valueVectorSerializeRow
operator|=
operator|new
name|VectorSerializeRow
argument_list|<
name|LazyBinarySerializeWrite
argument_list|>
argument_list|(
name|valueLazyBinarySerializeWrite
argument_list|)
expr_stmt|;
name|valueVectorSerializeRow
operator|.
name|init
argument_list|(
name|reduceSinkValueTypeInfos
argument_list|,
name|reduceSinkValueColumnMap
argument_list|)
expr_stmt|;
name|valueOutput
operator|=
operator|new
name|Output
argument_list|()
expr_stmt|;
name|valueVectorSerializeRow
operator|.
name|setOutput
argument_list|(
name|valueOutput
argument_list|)
expr_stmt|;
name|keyWritable
operator|=
operator|new
name|HiveKey
argument_list|()
expr_stmt|;
name|valueBytesWritable
operator|=
operator|new
name|BytesWritable
argument_list|()
expr_stmt|;
name|batchCounter
operator|=
literal|0
expr_stmt|;
block|}
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
try|try
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
name|batchCounter
operator|++
expr_stmt|;
if|if
condition|(
name|batch
operator|.
name|size
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|CLASS_NAME
operator|+
literal|" batch #"
operator|+
name|batchCounter
operator|+
literal|" empty"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// Perform any key expressions.  Results will go into scratch columns.
if|if
condition|(
name|reduceSinkKeyExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|reduceSinkKeyExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Perform any value expressions.  Results will go into scratch columns.
if|if
condition|(
name|reduceSinkValueExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|reduceSinkValueExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
name|serializedKeySeries
operator|.
name|processBatch
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|boolean
name|selectedInUse
init|=
name|batch
operator|.
name|selectedInUse
decl_stmt|;
name|int
index|[]
name|selected
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|int
name|keyLength
decl_stmt|;
name|int
name|logical
decl_stmt|;
name|int
name|end
decl_stmt|;
name|int
name|batchIndex
decl_stmt|;
do|do
block|{
if|if
condition|(
name|serializedKeySeries
operator|.
name|getCurrentIsAllNull
argument_list|()
condition|)
block|{
comment|// Use the same logic as ReduceSinkOperator.toHiveKey.
comment|//
if|if
condition|(
name|tag
operator|==
operator|-
literal|1
operator|||
name|reduceSkipTag
condition|)
block|{
name|keyWritable
operator|.
name|set
argument_list|(
name|nullBytes
argument_list|,
literal|0
argument_list|,
name|nullBytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|keyWritable
operator|.
name|setSize
argument_list|(
name|nullBytes
operator|.
name|length
operator|+
literal|1
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|nullBytes
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
name|nullBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|keyWritable
operator|.
name|get
argument_list|()
index|[
name|nullBytes
operator|.
name|length
index|]
operator|=
name|reduceTagByte
expr_stmt|;
block|}
name|keyWritable
operator|.
name|setDistKeyLength
argument_list|(
name|nullBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|keyWritable
operator|.
name|setHashCode
argument_list|(
name|nullKeyHashCode
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// One serialized key for 1 or more rows for the duplicate keys.
comment|// LOG.info("reduceSkipTag " + reduceSkipTag + " tag " + tag + " reduceTagByte " + (int) reduceTagByte + " keyLength " + serializedKeySeries.getSerializedLength());
comment|// LOG.info("process offset " + serializedKeySeries.getSerializedStart() + " length " + serializedKeySeries.getSerializedLength());
name|keyLength
operator|=
name|serializedKeySeries
operator|.
name|getSerializedLength
argument_list|()
expr_stmt|;
if|if
condition|(
name|tag
operator|==
operator|-
literal|1
operator|||
name|reduceSkipTag
condition|)
block|{
name|keyWritable
operator|.
name|set
argument_list|(
name|serializedKeySeries
operator|.
name|getSerializedBytes
argument_list|()
argument_list|,
name|serializedKeySeries
operator|.
name|getSerializedStart
argument_list|()
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|serializedKeySeries
operator|.
name|getSerializedBytes
argument_list|()
argument_list|,
name|serializedKeySeries
operator|.
name|getSerializedStart
argument_list|()
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
name|reduceTagByte
expr_stmt|;
block|}
name|keyWritable
operator|.
name|setDistKeyLength
argument_list|(
name|keyLength
argument_list|)
expr_stmt|;
name|keyWritable
operator|.
name|setHashCode
argument_list|(
name|serializedKeySeries
operator|.
name|getCurrentHashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|logical
operator|=
name|serializedKeySeries
operator|.
name|getCurrentLogical
argument_list|()
expr_stmt|;
name|end
operator|=
name|logical
operator|+
name|serializedKeySeries
operator|.
name|getCurrentDuplicateCount
argument_list|()
expr_stmt|;
do|do
block|{
name|batchIndex
operator|=
operator|(
name|selectedInUse
condition|?
name|selected
index|[
name|logical
index|]
else|:
name|logical
operator|)
expr_stmt|;
name|valueLazyBinarySerializeWrite
operator|.
name|reset
argument_list|()
expr_stmt|;
name|valueVectorSerializeRow
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
name|valueBytesWritable
operator|.
name|set
argument_list|(
name|valueOutput
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|valueOutput
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|valueBytesWritable
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|++
name|logical
operator|<
name|end
condition|)
do|;
if|if
condition|(
operator|!
name|serializedKeySeries
operator|.
name|next
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
do|while
condition|(
literal|true
condition|)
do|;
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
specifier|protected
name|void
name|collect
parameter_list|(
name|BytesWritable
name|keyWritable
parameter_list|,
name|Writable
name|valueWritable
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Since this is a terminal operator, update counters explicitly -
comment|// forward is not called
if|if
condition|(
literal|null
operator|!=
name|out
condition|)
block|{
name|numRows
operator|++
expr_stmt|;
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
if|if
condition|(
name|numRows
operator|==
name|cntr
condition|)
block|{
name|cntr
operator|=
name|logEveryNRows
operator|==
literal|0
condition|?
name|cntr
operator|*
literal|10
else|:
name|numRows
operator|+
name|logEveryNRows
expr_stmt|;
if|if
condition|(
name|cntr
operator|<
literal|0
operator|||
name|numRows
operator|<
literal|0
condition|)
block|{
name|cntr
operator|=
literal|0
expr_stmt|;
name|numRows
operator|=
literal|1
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|toString
argument_list|()
operator|+
literal|": records written - "
operator|+
name|numRows
argument_list|)
expr_stmt|;
block|}
block|}
comment|// BytesWritable valueBytesWritable = (BytesWritable) valueWritable;
comment|// LOG.info("VectorReduceSinkCommonOperator collect keyWritable " + keyWritable.getLength() + " " +
comment|//     VectorizedBatchUtil.displayBytes(keyWritable.getBytes(), 0, keyWritable.getLength()) +
comment|//     " valueWritable " + valueBytesWritable.getLength() +
comment|//     VectorizedBatchUtil.displayBytes(valueBytesWritable.getBytes(), 0, valueBytesWritable.getLength()));
name|out
operator|.
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|valueWritable
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
name|out
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|toString
argument_list|()
operator|+
literal|": records written - "
operator|+
name|numRows
argument_list|)
expr_stmt|;
block|}
name|recordCounter
operator|.
name|set
argument_list|(
name|numRows
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the name of the operator    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
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
literal|"RS"
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
name|REDUCESINK
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizationContext
name|getOuputVectorizationContext
parameter_list|()
block|{
return|return
name|vContext
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getIsReduceSink
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getReduceOutputName
parameter_list|()
block|{
return|return
name|conf
operator|.
name|getOutputName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOutputCollector
parameter_list|(
name|OutputCollector
name|_out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|_out
expr_stmt|;
block|}
block|}
end_class

end_unit

