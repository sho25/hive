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
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
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

begin_comment
comment|/**  * Constant is represented as a vector with repeating values.  */
end_comment

begin_class
specifier|public
class|class
name|DynamicValueVectorExpression
extends|extends
name|VectorExpression
block|{
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
name|DynamicValueVectorExpression
operator|.
name|class
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
specifier|private
specifier|final
name|DynamicValue
name|dynamicValue
decl_stmt|;
specifier|private
specifier|final
name|TypeInfo
name|typeInfo
decl_stmt|;
specifier|private
specifier|final
name|ColumnVector
operator|.
name|Type
name|type
decl_stmt|;
specifier|transient
specifier|private
name|boolean
name|initialized
init|=
literal|false
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
name|int
name|bytesValueLength
init|=
literal|0
decl_stmt|;
specifier|public
name|DynamicValueVectorExpression
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
name|dynamicValue
operator|=
literal|null
expr_stmt|;
name|typeInfo
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|DynamicValueVectorExpression
parameter_list|(
name|int
name|outputColumnNum
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|,
name|DynamicValue
name|dynamicValue
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
name|type
operator|=
name|VectorizationContext
operator|.
name|getColumnVectorTypeFromTypeInfo
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|dynamicValue
operator|=
name|dynamicValue
expr_stmt|;
name|this
operator|.
name|typeInfo
operator|=
name|typeInfo
expr_stmt|;
block|}
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
name|cv
operator|.
name|noNulls
operator|=
operator|!
name|isNullValue
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|cv
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|longValue
expr_stmt|;
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
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
name|cv
operator|.
name|noNulls
operator|=
operator|!
name|isNullValue
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|cv
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|doubleValue
expr_stmt|;
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
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
name|noNulls
operator|=
operator|!
name|isNullValue
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
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
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
name|dcv
operator|.
name|noNulls
operator|=
operator|!
name|isNullValue
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|set
argument_list|(
name|decimalValue
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
name|dcv
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
name|dcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|dcv
operator|.
name|noNulls
operator|=
operator|!
name|isNullValue
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|dcv
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|timestampValue
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
name|dcv
operator|.
name|noNulls
operator|=
operator|!
name|isNullValue
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|dcv
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|intervalDayTimeValue
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
block|}
block|}
specifier|private
name|void
name|initValue
parameter_list|()
block|{
name|Object
name|val
init|=
name|dynamicValue
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
name|isNullValue
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|PrimitiveObjectInspector
name|poi
init|=
name|dynamicValue
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytesVal
decl_stmt|;
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
name|longValue
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getLong
argument_list|(
name|val
argument_list|,
name|poi
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
name|doubleValue
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|val
argument_list|,
name|poi
argument_list|)
expr_stmt|;
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
name|bytesVal
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|val
argument_list|,
name|poi
argument_list|)
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|setBytesValue
argument_list|(
name|bytesVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|bytesVal
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getBinary
argument_list|(
name|val
argument_list|,
name|poi
argument_list|)
operator|.
name|copyBytes
argument_list|()
expr_stmt|;
name|setBytesValue
argument_list|(
name|bytesVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|decimalValue
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveDecimal
argument_list|(
name|val
argument_list|,
name|poi
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|longValue
operator|=
name|DateWritable
operator|.
name|dateToDays
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getDate
argument_list|(
name|val
argument_list|,
name|poi
argument_list|)
argument_list|)
expr_stmt|;
case|case
name|TIMESTAMP
case|:
name|timestampValue
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|val
argument_list|,
name|poi
argument_list|)
expr_stmt|;
break|break;
case|case
name|INTERVAL_YEAR_MONTH
case|:
name|longValue
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalYearMonth
argument_list|(
name|val
argument_list|,
name|poi
argument_list|)
operator|.
name|getTotalMonths
argument_list|()
expr_stmt|;
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
name|intervalDayTimeValue
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalDayTime
argument_list|(
name|val
argument_list|,
name|poi
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unsupported type "
operator|+
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|initialized
operator|=
literal|true
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
name|dynamicValue
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
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
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unsupported type "
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
specifier|public
name|String
name|getTypeString
parameter_list|()
block|{
return|return
name|outputTypeInfo
operator|.
name|toString
argument_list|()
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

