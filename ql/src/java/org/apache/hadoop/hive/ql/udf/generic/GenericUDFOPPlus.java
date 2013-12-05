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
name|expressions
operator|.
name|gen
operator|.
name|DoubleColAddDoubleColumn
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
name|DoubleColAddDoubleScalar
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
name|DoubleColAddLongColumn
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
name|DoubleColAddLongScalar
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
name|DoubleScalarAddDoubleColumn
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
name|DoubleScalarAddLongColumn
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
name|LongColAddDoubleColumn
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
name|LongColAddDoubleScalar
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
name|LongColAddLongColumn
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
name|LongColAddLongScalar
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
name|LongScalarAddDoubleColumn
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
name|LongScalarAddLongColumn
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

begin_comment
comment|/**  * The reason that we list evaluate methods with all numeric types is for both  * better performance and type checking (so we know int + int is still an int  * instead of a double); otherwise a single method that takes (Number a, Number  * b) and use a.doubleValue() == b.doubleValue() is enough.  *  * The case of int + double will be handled by implicit type casting using  * UDFRegistry.implicitConvertable method.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"+"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns a+b"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColAddLongColumn
operator|.
name|class
block|,
name|LongColAddDoubleColumn
operator|.
name|class
block|,
name|DoubleColAddLongColumn
operator|.
name|class
block|,
name|DoubleColAddDoubleColumn
operator|.
name|class
block|,
name|LongColAddLongScalar
operator|.
name|class
block|,
name|LongColAddDoubleScalar
operator|.
name|class
block|,
name|DoubleColAddLongScalar
operator|.
name|class
block|,
name|DoubleColAddDoubleScalar
operator|.
name|class
block|,
name|LongScalarAddLongColumn
operator|.
name|class
block|,
name|LongScalarAddDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarAddLongColumn
operator|.
name|class
block|,
name|DoubleScalarAddDoubleColumn
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFOPPlus
extends|extends
name|GenericUDFBaseNumeric
block|{
specifier|public
name|GenericUDFOPPlus
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"+"
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
operator|+
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
operator|+
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
operator|+
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
operator|+
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
operator|+
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
operator|+
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
name|add
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
name|int
name|intPart
init|=
name|Math
operator|.
name|max
argument_list|(
name|prec1
operator|-
name|scale1
argument_list|,
name|prec2
operator|-
name|scale2
argument_list|)
decl_stmt|;
name|int
name|scale
init|=
name|Math
operator|.
name|max
argument_list|(
name|scale1
argument_list|,
name|scale2
argument_list|)
decl_stmt|;
name|int
name|prec
init|=
name|Math
operator|.
name|min
argument_list|(
name|intPart
operator|+
name|scale
operator|+
literal|1
argument_list|,
name|HiveDecimal
operator|.
name|MAX_PRECISION
argument_list|)
decl_stmt|;
return|return
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
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

