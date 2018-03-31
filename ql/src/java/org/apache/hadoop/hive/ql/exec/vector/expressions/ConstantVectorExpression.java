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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|DataTypePhysicalVariation
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
name|HiveVarchar
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
name|*
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
name|TypeInfoFactory
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

begin_comment
comment|/**  * Constant is represented as a vector with repeating values.  */
end_comment

begin_class
specifier|public
class|class
name|ConstantVectorExpression
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
name|long
name|longValue
init|=
literal|0
decl_stmt|;
specifier|private
name|double
name|doubleValue
init|=
literal|0
decl_stmt|;
specifier|private
name|byte
index|[]
name|bytesValue
init|=
literal|null
decl_stmt|;
specifier|private
name|HiveDecimal
name|decimalValue
init|=
literal|null
decl_stmt|;
specifier|private
name|Timestamp
name|timestampValue
init|=
literal|null
decl_stmt|;
specifier|private
name|HiveIntervalDayTime
name|intervalDayTimeValue
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|isNullValue
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|ColumnVector
operator|.
name|Type
name|type
decl_stmt|;
specifier|private
name|int
name|bytesValueLength
init|=
literal|0
decl_stmt|;
specifier|public
name|ConstantVectorExpression
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|type
operator|=
literal|null
expr_stmt|;
block|}
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
argument_list|(
name|outputColumnNum
argument_list|)
expr_stmt|;
name|this
operator|.
name|outputTypeInfo
operator|=
name|outputTypeInfo
expr_stmt|;
name|outputDataTypePhysicalVariation
operator|=
name|DataTypePhysicalVariation
operator|.
name|NONE
expr_stmt|;
name|type
operator|=
name|VectorizationContext
operator|.
name|getColumnVectorTypeFromTypeInfo
argument_list|(
name|outputTypeInfo
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|long
name|value
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|outputColumnNum
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|longValue
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|double
name|value
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|outputColumnNum
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|doubleValue
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|outputColumnNum
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
name|setBytesValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|HiveChar
name|value
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|outputColumnNum
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
name|setBytesValue
argument_list|(
name|value
operator|.
name|getStrippedValue
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|HiveVarchar
name|value
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|outputColumnNum
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
name|setBytesValue
argument_list|(
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Include type name for precision/scale.
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|HiveDecimal
name|value
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|outputColumnNum
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
name|setDecimalValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|Timestamp
name|value
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|outputColumnNum
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
name|setTimestampValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|HiveIntervalDayTime
name|value
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|outputColumnNum
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
name|setIntervalDayTimeValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
comment|/*    * Support for null constant object    */
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|boolean
name|isNull
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|outputColumnNum
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
name|isNullValue
operator|=
name|isNull
expr_stmt|;
block|}
comment|/*    * In the following evaluate* methods, since we are supporting scratch column reuse, we must    * assume the column may have noNulls of false and some isNull entries true.    *    * So, do a proper assignments.    */
specifier|private
name|void
name|evaluateLong
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|LongColumnVector
name|cv
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|cv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|cv
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|longValue
expr_stmt|;
block|}
else|else
block|{
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|cv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateDouble
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|DoubleColumnVector
name|cv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|cv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|cv
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|doubleValue
expr_stmt|;
block|}
else|else
block|{
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|cv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateBytes
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|BytesColumnVector
name|cv
init|=
operator|(
name|BytesColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|cv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|cv
operator|.
name|initBuffer
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|cv
operator|.
name|setVal
argument_list|(
literal|0
argument_list|,
name|bytesValue
argument_list|,
literal|0
argument_list|,
name|bytesValueLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|cv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateDecimal
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|(
name|DecimalColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|dcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|dcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|dcv
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|decimalValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateTimestamp
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|TimestampColumnVector
name|tcv
init|=
operator|(
name|TimestampColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|tcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|tcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|tcv
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|timestampValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|tcv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateIntervalDayTime
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|IntervalDayTimeColumnVector
name|dcv
init|=
operator|(
name|IntervalDayTimeColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|dcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|dcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|dcv
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|intervalDayTimeValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateVoid
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|VoidColumnVector
name|voidColVector
init|=
operator|(
name|VoidColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|voidColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|voidColVector
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|voidColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|LONG
case|:
name|evaluateLong
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|evaluateDouble
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTES
case|:
name|evaluateBytes
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|evaluateDecimal
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|evaluateTimestamp
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
name|evaluateIntervalDayTime
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
name|VOID
case|:
name|evaluateVoid
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected column vector type "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
specifier|public
name|long
name|getLongValue
parameter_list|()
block|{
return|return
name|longValue
return|;
block|}
specifier|public
name|void
name|setLongValue
parameter_list|(
name|long
name|longValue
parameter_list|)
block|{
name|this
operator|.
name|longValue
operator|=
name|longValue
expr_stmt|;
block|}
specifier|public
name|double
name|getDoubleValue
parameter_list|()
block|{
return|return
name|doubleValue
return|;
block|}
specifier|public
name|void
name|setDoubleValue
parameter_list|(
name|double
name|doubleValue
parameter_list|)
block|{
name|this
operator|.
name|doubleValue
operator|=
name|doubleValue
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getBytesValue
parameter_list|()
block|{
return|return
name|bytesValue
return|;
block|}
specifier|public
name|void
name|setBytesValue
parameter_list|(
name|byte
index|[]
name|bytesValue
parameter_list|)
block|{
name|this
operator|.
name|bytesValue
operator|=
name|bytesValue
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|bytesValueLength
operator|=
name|bytesValue
operator|.
name|length
expr_stmt|;
block|}
specifier|public
name|void
name|setDecimalValue
parameter_list|(
name|HiveDecimal
name|decimalValue
parameter_list|)
block|{
name|this
operator|.
name|decimalValue
operator|=
name|decimalValue
expr_stmt|;
block|}
specifier|public
name|HiveDecimal
name|getDecimalValue
parameter_list|()
block|{
return|return
name|decimalValue
return|;
block|}
specifier|public
name|void
name|setTimestampValue
parameter_list|(
name|Timestamp
name|timestampValue
parameter_list|)
block|{
name|this
operator|.
name|timestampValue
operator|=
name|timestampValue
expr_stmt|;
block|}
specifier|public
name|Timestamp
name|getTimestampValue
parameter_list|()
block|{
return|return
name|timestampValue
return|;
block|}
specifier|public
name|void
name|setIntervalDayTimeValue
parameter_list|(
name|HiveIntervalDayTime
name|intervalDayTimeValue
parameter_list|)
block|{
name|this
operator|.
name|intervalDayTimeValue
operator|=
name|intervalDayTimeValue
expr_stmt|;
block|}
specifier|public
name|HiveIntervalDayTime
name|getIntervalDayTimeValue
parameter_list|()
block|{
return|return
name|intervalDayTimeValue
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
name|String
name|value
decl_stmt|;
if|if
condition|(
name|isNullValue
condition|)
block|{
name|value
operator|=
literal|"null"
expr_stmt|;
block|}
else|else
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|LONG
case|:
name|value
operator|=
name|Long
operator|.
name|toString
argument_list|(
name|longValue
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|value
operator|=
name|Double
operator|.
name|toString
argument_list|(
name|doubleValue
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTES
case|:
name|value
operator|=
operator|new
name|String
argument_list|(
name|bytesValue
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|value
operator|=
name|decimalValue
operator|.
name|toString
argument_list|()
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|value
operator|=
name|timestampValue
operator|.
name|toString
argument_list|()
expr_stmt|;
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
name|value
operator|=
name|intervalDayTimeValue
operator|.
name|toString
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown vector column type "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
return|return
literal|"val "
operator|+
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorExpressionDescriptor
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
return|return
operator|(
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
operator|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

