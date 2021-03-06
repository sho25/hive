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
name|VectorizedExpressionsSupportDecimal64
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
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|DecimalTypeInfo
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
literal|"*"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Multiplies a by b"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColMultiplyLongColumn
operator|.
name|class
block|,
name|LongColMultiplyDoubleColumn
operator|.
name|class
block|,
name|LongColMultiplyLongColumnChecked
operator|.
name|class
block|,
name|LongColMultiplyDoubleColumnChecked
operator|.
name|class
block|,
name|DoubleColMultiplyLongColumn
operator|.
name|class
block|,
name|DoubleColMultiplyDoubleColumn
operator|.
name|class
block|,
name|DoubleColMultiplyLongColumnChecked
operator|.
name|class
block|,
name|DoubleColMultiplyDoubleColumnChecked
operator|.
name|class
block|,
name|LongColMultiplyLongScalar
operator|.
name|class
block|,
name|LongColMultiplyDoubleScalar
operator|.
name|class
block|,
name|LongColMultiplyLongScalarChecked
operator|.
name|class
block|,
name|LongColMultiplyDoubleScalarChecked
operator|.
name|class
block|,
name|DoubleColMultiplyLongScalar
operator|.
name|class
block|,
name|DoubleColMultiplyDoubleScalar
operator|.
name|class
block|,
name|DoubleColMultiplyLongScalarChecked
operator|.
name|class
block|,
name|DoubleColMultiplyDoubleScalarChecked
operator|.
name|class
block|,
name|LongScalarMultiplyLongColumn
operator|.
name|class
block|,
name|LongScalarMultiplyDoubleColumn
operator|.
name|class
block|,
name|LongScalarMultiplyLongColumnChecked
operator|.
name|class
block|,
name|LongScalarMultiplyDoubleColumnChecked
operator|.
name|class
block|,
name|DoubleScalarMultiplyLongColumn
operator|.
name|class
block|,
name|DoubleScalarMultiplyDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarMultiplyLongColumnChecked
operator|.
name|class
block|,
name|DoubleScalarMultiplyDoubleColumnChecked
operator|.
name|class
block|,
name|DecimalColMultiplyDecimalColumn
operator|.
name|class
block|,
name|DecimalColMultiplyDecimalScalar
operator|.
name|class
block|,
name|DecimalScalarMultiplyDecimalColumn
operator|.
name|class
block|,
name|Decimal64ColMultiplyDecimal64ScalarUnscaled
operator|.
name|class
block|,
name|Decimal64ColMultiplyDecimal64Column
operator|.
name|class
block|,
name|Decimal64ScalarMultiplyDecimal64ColumnUnscaled
operator|.
name|class
block|}
argument_list|)
annotation|@
name|VectorizedExpressionsSupportDecimal64
argument_list|()
specifier|public
class|class
name|GenericUDFOPMultiply
extends|extends
name|GenericUDFBaseNumeric
block|{
specifier|public
name|GenericUDFOPMultiply
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"*"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|ByteWritable
name|evaluate
parameter_list|(
name|ByteWritable
name|left
parameter_list|,
name|ByteWritable
name|right
parameter_list|)
block|{
name|byteWritable
operator|.
name|set
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|left
operator|.
name|get
argument_list|()
operator|*
name|right
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|byteWritable
return|;
block|}
annotation|@
name|Override
specifier|protected
name|ShortWritable
name|evaluate
parameter_list|(
name|ShortWritable
name|left
parameter_list|,
name|ShortWritable
name|right
parameter_list|)
block|{
name|shortWritable
operator|.
name|set
argument_list|(
call|(
name|short
call|)
argument_list|(
name|left
operator|.
name|get
argument_list|()
operator|*
name|right
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|shortWritable
return|;
block|}
annotation|@
name|Override
specifier|protected
name|IntWritable
name|evaluate
parameter_list|(
name|IntWritable
name|left
parameter_list|,
name|IntWritable
name|right
parameter_list|)
block|{
name|intWritable
operator|.
name|set
argument_list|(
name|left
operator|.
name|get
argument_list|()
operator|*
name|right
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
annotation|@
name|Override
specifier|protected
name|LongWritable
name|evaluate
parameter_list|(
name|LongWritable
name|left
parameter_list|,
name|LongWritable
name|right
parameter_list|)
block|{
name|longWritable
operator|.
name|set
argument_list|(
name|left
operator|.
name|get
argument_list|()
operator|*
name|right
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
block|}
annotation|@
name|Override
specifier|protected
name|FloatWritable
name|evaluate
parameter_list|(
name|FloatWritable
name|left
parameter_list|,
name|FloatWritable
name|right
parameter_list|)
block|{
name|floatWritable
operator|.
name|set
argument_list|(
name|left
operator|.
name|get
argument_list|()
operator|*
name|right
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
annotation|@
name|Override
specifier|protected
name|DoubleWritable
name|evaluate
parameter_list|(
name|DoubleWritable
name|left
parameter_list|,
name|DoubleWritable
name|right
parameter_list|)
block|{
name|doubleWritable
operator|.
name|set
argument_list|(
name|left
operator|.
name|get
argument_list|()
operator|*
name|right
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|doubleWritable
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveDecimalWritable
name|evaluate
parameter_list|(
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|)
block|{
name|HiveDecimal
name|dec
init|=
name|left
operator|.
name|multiply
argument_list|(
name|right
argument_list|)
decl_stmt|;
if|if
condition|(
name|dec
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|decimalWritable
operator|.
name|set
argument_list|(
name|dec
argument_list|)
expr_stmt|;
return|return
name|decimalWritable
return|;
block|}
annotation|@
name|Override
specifier|protected
name|DecimalTypeInfo
name|deriveResultDecimalTypeInfo
parameter_list|(
name|int
name|prec1
parameter_list|,
name|int
name|scale1
parameter_list|,
name|int
name|prec2
parameter_list|,
name|int
name|scale2
parameter_list|)
block|{
comment|// From https://msdn.microsoft.com/en-us/library/ms190476.aspx
comment|// e1 * e2
comment|// Precision: p1 + p2 + 1
comment|// Scale: s1 + s2
name|int
name|scale
init|=
name|scale1
operator|+
name|scale2
decl_stmt|;
name|int
name|prec
init|=
name|prec1
operator|+
name|prec2
operator|+
literal|1
decl_stmt|;
return|return
name|adjustPrecScale
argument_list|(
name|prec
argument_list|,
name|scale
argument_list|)
return|;
block|}
block|}
end_class

end_unit

