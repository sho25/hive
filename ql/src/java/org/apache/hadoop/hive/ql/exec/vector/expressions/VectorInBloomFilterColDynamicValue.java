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
name|expressions
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
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
name|common
operator|.
name|io
operator|.
name|NonSyncByteArrayInputStream
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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|BytesColumnVector
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
name|ColumnVector
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
name|DecimalColumnVector
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
name|DoubleColumnVector
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
name|LongColumnVector
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
name|TimestampColumnVector
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
name|VectorExpressionDescriptor
operator|.
name|Descriptor
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
name|VectorExpressionDescriptor
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
name|DynamicValue
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
operator|.
name|PrimitiveCategory
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
name|BinaryObjectInspector
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
name|io
operator|.
name|IOUtils
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
name|BloomKFilter
import|;
end_import

begin_class
specifier|public
class|class
name|VectorInBloomFilterColDynamicValue
extends|extends
name|VectorExpression
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
specifier|final
name|int
name|colNum
decl_stmt|;
specifier|protected
specifier|final
name|DynamicValue
name|bloomFilterDynamicValue
decl_stmt|;
specifier|protected
specifier|transient
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
specifier|protected
specifier|transient
name|BloomKFilter
name|bloomFilter
decl_stmt|;
specifier|protected
specifier|transient
name|BloomFilterCheck
name|bfCheck
decl_stmt|;
specifier|protected
specifier|transient
name|ColumnVector
operator|.
name|Type
name|colVectorType
decl_stmt|;
specifier|public
name|VectorInBloomFilterColDynamicValue
parameter_list|(
name|int
name|colNum
parameter_list|,
name|DynamicValue
name|bloomFilterDynamicValue
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
name|this
operator|.
name|bloomFilterDynamicValue
operator|=
name|bloomFilterDynamicValue
expr_stmt|;
block|}
specifier|public
name|VectorInBloomFilterColDynamicValue
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|colNum
operator|=
operator|-
literal|1
expr_stmt|;
name|bloomFilterDynamicValue
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|transientInit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|transientInit
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|colVectorType
operator|=
name|VectorizationContext
operator|.
name|getColumnVectorTypeFromTypeInfo
argument_list|(
name|inputTypeInfos
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|bloomFilterDynamicValue
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Instantiate BloomFilterCheck based on input column type
switch|switch
condition|(
name|colVectorType
condition|)
block|{
case|case
name|LONG
case|:
case|case
name|DECIMAL_64
case|:
name|bfCheck
operator|=
operator|new
name|LongBloomFilterCheck
argument_list|()
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|bfCheck
operator|=
operator|new
name|DoubleBloomFilterCheck
argument_list|()
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|bfCheck
operator|=
operator|new
name|DecimalBloomFilterCheck
argument_list|()
expr_stmt|;
break|break;
case|case
name|BYTES
case|:
name|bfCheck
operator|=
operator|new
name|BytesBloomFilterCheck
argument_list|()
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|bfCheck
operator|=
operator|new
name|TimestampBloomFilterCheck
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unsupported type "
operator|+
name|colVectorType
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|initValue
parameter_list|()
block|{
name|InputStream
name|in
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Object
name|val
init|=
name|bloomFilterDynamicValue
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|!=
literal|null
condition|)
block|{
name|BinaryObjectInspector
name|boi
init|=
operator|(
name|BinaryObjectInspector
operator|)
name|bloomFilterDynamicValue
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|boi
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|val
argument_list|)
decl_stmt|;
name|in
operator|=
operator|new
name|NonSyncByteArrayInputStream
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|bloomFilter
operator|=
name|BloomKFilter
operator|.
name|deserialize
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bloomFilter
operator|=
literal|null
expr_stmt|;
block|}
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|err
argument_list|)
throw|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
name|super
operator|.
name|evaluateChildren
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
name|initValue
argument_list|()
expr_stmt|;
block|}
name|ColumnVector
name|inputColVector
init|=
name|batch
operator|.
name|cols
index|[
name|colNum
index|]
decl_stmt|;
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|boolean
index|[]
name|nullPos
init|=
name|inputColVector
operator|.
name|isNull
decl_stmt|;
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
comment|// return immediately if batch is empty
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
return|return;
block|}
comment|// In case the dynamic value resolves to a null value
if|if
condition|(
name|bloomFilter
operator|==
literal|null
condition|)
block|{
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|inputColVector
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
comment|// All must be selected otherwise size would be zero. Repeating property will not change.
if|if
condition|(
operator|!
operator|(
name|bfCheck
operator|.
name|checkValue
argument_list|(
name|inputColVector
argument_list|,
literal|0
argument_list|)
operator|)
condition|)
block|{
comment|//Entire batch is filtered out.
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
name|bfCheck
operator|.
name|checkValue
argument_list|(
name|inputColVector
argument_list|,
name|i
argument_list|)
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
block|}
else|else
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|bfCheck
operator|.
name|checkValue
argument_list|(
name|inputColVector
argument_list|,
name|i
argument_list|)
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newSize
operator|<
name|n
condition|)
block|{
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
comment|// All must be selected otherwise size would be zero. Repeating property will not change.
if|if
condition|(
operator|!
name|nullPos
index|[
literal|0
index|]
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|bfCheck
operator|.
name|checkValue
argument_list|(
name|inputColVector
argument_list|,
literal|0
argument_list|)
operator|)
condition|)
block|{
comment|//Entire batch is filtered out.
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
block|}
else|else
block|{
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|nullPos
index|[
name|i
index|]
condition|)
block|{
if|if
condition|(
name|bfCheck
operator|.
name|checkValue
argument_list|(
name|inputColVector
argument_list|,
name|i
argument_list|)
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
comment|//Change the selected vector
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
block|}
else|else
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|nullPos
index|[
name|i
index|]
condition|)
block|{
if|if
condition|(
name|bfCheck
operator|.
name|checkValue
argument_list|(
name|inputColVector
argument_list|,
name|i
argument_list|)
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|newSize
operator|<
name|n
condition|)
block|{
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
name|VectorExpressionDescriptor
operator|.
name|Builder
name|b
init|=
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|b
operator|.
name|setMode
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|Mode
operator|.
name|FILTER
argument_list|)
operator|.
name|setNumArguments
argument_list|(
literal|2
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|ALL_FAMILY
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|BINARY
argument_list|)
operator|.
name|setInputExpressionTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|COLUMN
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|DYNAMICVALUE
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|build
argument_list|()
return|;
block|}
comment|// Type-specific handling
specifier|abstract
class|class
name|BloomFilterCheck
block|{
specifier|abstract
specifier|public
name|boolean
name|checkValue
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|idx
parameter_list|)
function_decl|;
block|}
class|class
name|BytesBloomFilterCheck
extends|extends
name|BloomFilterCheck
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|checkValue
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|idx
parameter_list|)
block|{
name|BytesColumnVector
name|col
init|=
operator|(
name|BytesColumnVector
operator|)
name|columnVector
decl_stmt|;
return|return
name|bloomFilter
operator|.
name|testBytes
argument_list|(
name|col
operator|.
name|vector
index|[
name|idx
index|]
argument_list|,
name|col
operator|.
name|start
index|[
name|idx
index|]
argument_list|,
name|col
operator|.
name|length
index|[
name|idx
index|]
argument_list|)
return|;
block|}
block|}
class|class
name|LongBloomFilterCheck
extends|extends
name|BloomFilterCheck
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|checkValue
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|idx
parameter_list|)
block|{
name|LongColumnVector
name|col
init|=
operator|(
name|LongColumnVector
operator|)
name|columnVector
decl_stmt|;
return|return
name|bloomFilter
operator|.
name|testLong
argument_list|(
name|col
operator|.
name|vector
index|[
name|idx
index|]
argument_list|)
return|;
block|}
block|}
class|class
name|DoubleBloomFilterCheck
extends|extends
name|BloomFilterCheck
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|checkValue
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|idx
parameter_list|)
block|{
name|DoubleColumnVector
name|col
init|=
operator|(
name|DoubleColumnVector
operator|)
name|columnVector
decl_stmt|;
return|return
name|bloomFilter
operator|.
name|testDouble
argument_list|(
name|col
operator|.
name|vector
index|[
name|idx
index|]
argument_list|)
return|;
block|}
block|}
class|class
name|DecimalBloomFilterCheck
extends|extends
name|BloomFilterCheck
block|{
specifier|private
name|byte
index|[]
name|scratchBuffer
init|=
operator|new
name|byte
index|[
name|HiveDecimal
operator|.
name|SCRATCH_BUFFER_LEN_TO_BYTES
index|]
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|checkValue
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|idx
parameter_list|)
block|{
name|DecimalColumnVector
name|col
init|=
operator|(
name|DecimalColumnVector
operator|)
name|columnVector
decl_stmt|;
name|int
name|startIdx
init|=
name|col
operator|.
name|vector
index|[
name|idx
index|]
operator|.
name|toBytes
argument_list|(
name|scratchBuffer
argument_list|)
decl_stmt|;
return|return
name|bloomFilter
operator|.
name|testBytes
argument_list|(
name|scratchBuffer
argument_list|,
name|startIdx
argument_list|,
name|scratchBuffer
operator|.
name|length
operator|-
name|startIdx
argument_list|)
return|;
block|}
block|}
class|class
name|TimestampBloomFilterCheck
extends|extends
name|BloomFilterCheck
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|checkValue
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|idx
parameter_list|)
block|{
name|TimestampColumnVector
name|col
init|=
operator|(
name|TimestampColumnVector
operator|)
name|columnVector
decl_stmt|;
return|return
name|bloomFilter
operator|.
name|testLong
argument_list|(
name|col
operator|.
name|time
index|[
name|idx
index|]
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

