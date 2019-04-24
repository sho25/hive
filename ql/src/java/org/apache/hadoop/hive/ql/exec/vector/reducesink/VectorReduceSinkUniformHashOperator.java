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
operator|.
name|vector
operator|.
name|reducesink
package|;
end_package

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
name|VectorDesc
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|Murmur3
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
comment|/**  * This class is uniform hash (common) operator class for native vectorized reduce sink.  * There are variation operators for Long, String, and MultiKey.  And, a special case operator  * for no key (VectorReduceSinkEmptyKeyOperator).  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorReduceSinkUniformHashOperator
extends|extends
name|VectorReduceSinkCommonOperator
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
name|VectorReduceSinkUniformHashOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|CLASS_NAME
argument_list|)
decl_stmt|;
comment|// The above members are initialized by the constructor and must not be
comment|// transient.
comment|//---------------------------------------------------------------------------
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
comment|// The object that determines equal key series.
specifier|protected
specifier|transient
name|VectorKeySeriesSerialized
name|serializedKeySeries
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|VectorReduceSinkUniformHashOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorReduceSinkUniformHashOperator
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
name|VectorReduceSinkUniformHashOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|,
name|VectorizationContext
name|vContext
parameter_list|,
name|VectorDesc
name|vectorDesc
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
argument_list|(
name|ctx
argument_list|,
name|conf
argument_list|,
name|vContext
argument_list|,
name|vectorDesc
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
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|!
name|isEmptyKey
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
name|Murmur3
operator|.
name|hash32
argument_list|(
name|nullBytes
argument_list|,
literal|0
argument_list|,
name|nullBytesLength
argument_list|,
literal|0
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
name|logical
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
specifier|final
name|int
name|keyLength
init|=
name|serializedKeySeries
operator|.
name|getSerializedLength
argument_list|()
decl_stmt|;
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
specifier|final
name|int
name|end
init|=
name|logical
operator|+
name|serializedKeySeries
operator|.
name|getCurrentDuplicateCount
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|isEmptyValue
condition|)
block|{
if|if
condition|(
name|selectedInUse
condition|)
block|{
do|do
block|{
specifier|final
name|int
name|batchIndex
init|=
name|selected
index|[
name|logical
index|]
decl_stmt|;
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
block|}
else|else
block|{
do|do
block|{
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
name|logical
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
block|}
block|}
else|else
block|{
comment|// Empty value, too.
do|do
block|{
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
block|}
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
block|}
end_class

end_unit

