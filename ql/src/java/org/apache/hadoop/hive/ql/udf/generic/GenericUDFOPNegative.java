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
name|udf
operator|.
name|generic
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
name|Description
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
name|VectorizedExpressions
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
name|gen
operator|.
name|DoubleColUnaryMinus
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
name|gen
operator|.
name|FuncNegateDecimalToDecimal
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
name|gen
operator|.
name|LongColUnaryMinus
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
name|io
operator|.
name|ByteWritable
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
name|DoubleWritable
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
name|HiveDecimalWritable
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
name|HiveIntervalDayTimeWritable
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
name|HiveIntervalYearMonthWritable
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
name|ShortWritable
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
name|FloatWritable
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
name|IntWritable
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

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"-"
argument_list|,
name|value
operator|=
literal|"_FUNC_ a - Returns -a"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColUnaryMinus
operator|.
name|class
block|,
name|DoubleColUnaryMinus
operator|.
name|class
block|,
name|FuncNegateDecimalToDecimal
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFOPNegative
extends|extends
name|GenericUDFBaseUnary
block|{
specifier|public
name|GenericUDFOPNegative
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"-"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Object
name|input
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|input
operator|=
name|converter
operator|.
name|convert
argument_list|(
name|input
argument_list|)
expr_stmt|;
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|resultOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BYTE
case|:
name|byteWritable
operator|.
name|set
argument_list|(
operator|(
name|byte
operator|)
operator|-
operator|(
operator|(
operator|(
name|ByteWritable
operator|)
name|input
operator|)
operator|.
name|get
argument_list|()
operator|)
argument_list|)
expr_stmt|;
return|return
name|byteWritable
return|;
case|case
name|SHORT
case|:
name|shortWritable
operator|.
name|set
argument_list|(
operator|(
name|short
operator|)
operator|-
operator|(
operator|(
operator|(
name|ShortWritable
operator|)
name|input
operator|)
operator|.
name|get
argument_list|()
operator|)
argument_list|)
expr_stmt|;
return|return
name|shortWritable
return|;
case|case
name|INT
case|:
name|intWritable
operator|.
name|set
argument_list|(
operator|-
operator|(
operator|(
operator|(
name|IntWritable
operator|)
name|input
operator|)
operator|.
name|get
argument_list|()
operator|)
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
case|case
name|LONG
case|:
name|longWritable
operator|.
name|set
argument_list|(
operator|-
operator|(
operator|(
operator|(
name|LongWritable
operator|)
name|input
operator|)
operator|.
name|get
argument_list|()
operator|)
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
case|case
name|FLOAT
case|:
name|floatWritable
operator|.
name|set
argument_list|(
operator|-
operator|(
operator|(
operator|(
name|FloatWritable
operator|)
name|input
operator|)
operator|.
name|get
argument_list|()
operator|)
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
case|case
name|DOUBLE
case|:
name|doubleWritable
operator|.
name|set
argument_list|(
operator|-
operator|(
operator|(
operator|(
name|DoubleWritable
operator|)
name|input
operator|)
operator|.
name|get
argument_list|()
operator|)
argument_list|)
expr_stmt|;
return|return
name|doubleWritable
return|;
case|case
name|DECIMAL
case|:
name|decimalWritable
operator|.
name|set
argument_list|(
operator|(
name|HiveDecimalWritable
operator|)
name|input
argument_list|)
expr_stmt|;
name|decimalWritable
operator|.
name|mutateNegate
argument_list|()
expr_stmt|;
return|return
name|decimalWritable
return|;
case|case
name|INTERVAL_YEAR_MONTH
case|:
name|HiveIntervalYearMonth
name|intervalYearMonth
init|=
operator|(
operator|(
name|HiveIntervalYearMonthWritable
operator|)
name|input
operator|)
operator|.
name|getHiveIntervalYearMonth
argument_list|()
decl_stmt|;
name|this
operator|.
name|intervalYearMonthWritable
operator|.
name|set
argument_list|(
name|intervalYearMonth
operator|.
name|negate
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|intervalYearMonthWritable
return|;
case|case
name|INTERVAL_DAY_TIME
case|:
name|HiveIntervalDayTime
name|intervalDayTime
init|=
operator|(
operator|(
name|HiveIntervalDayTimeWritable
operator|)
name|input
operator|)
operator|.
name|getHiveIntervalDayTime
argument_list|()
decl_stmt|;
name|this
operator|.
name|intervalDayTimeWritable
operator|.
name|set
argument_list|(
name|intervalDayTime
operator|.
name|negate
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|intervalDayTimeWritable
return|;
default|default:
comment|// Should never happen.
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected type in evaluating "
operator|+
name|opName
operator|+
literal|": "
operator|+
name|resultOI
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

