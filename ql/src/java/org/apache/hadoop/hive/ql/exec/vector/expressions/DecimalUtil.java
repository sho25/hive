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
name|expressions
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
name|Decimal128
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
name|SqlMathUtil
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
name|UnsignedInt128
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
name|udf
operator|.
name|generic
operator|.
name|RoundUtils
import|;
end_import

begin_comment
comment|/**  * Utility functions for vector operations on decimal values.  */
end_comment

begin_class
specifier|public
class|class
name|DecimalUtil
block|{
specifier|public
specifier|static
specifier|final
name|Decimal128
name|DECIMAL_ONE
init|=
operator|new
name|Decimal128
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|UnsignedInt128
name|scratchUInt128
init|=
operator|new
name|UnsignedInt128
argument_list|()
decl_stmt|;
static|static
block|{
name|DECIMAL_ONE
operator|.
name|update
argument_list|(
literal|1L
argument_list|,
operator|(
name|short
operator|)
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// Addition with overflow check. Overflow produces NULL output.
specifier|public
specifier|static
name|void
name|addChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|left
parameter_list|,
name|Decimal128
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|Decimal128
operator|.
name|add
argument_list|(
name|left
argument_list|,
name|right
argument_list|,
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|checkPrecisionOverflow
argument_list|(
name|outputColVector
operator|.
name|precision
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|e
parameter_list|)
block|{
comment|// catch on overflow
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|// Subtraction with overflow check. Overflow produces NULL output.
specifier|public
specifier|static
name|void
name|subtractChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|left
parameter_list|,
name|Decimal128
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|Decimal128
operator|.
name|subtract
argument_list|(
name|left
argument_list|,
name|right
argument_list|,
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|checkPrecisionOverflow
argument_list|(
name|outputColVector
operator|.
name|precision
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|e
parameter_list|)
block|{
comment|// catch on overflow
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|// Multiplication with overflow check. Overflow produces NULL output.
specifier|public
specifier|static
name|void
name|multiplyChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|left
parameter_list|,
name|Decimal128
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|Decimal128
operator|.
name|multiply
argument_list|(
name|left
argument_list|,
name|right
argument_list|,
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|checkPrecisionOverflow
argument_list|(
name|outputColVector
operator|.
name|precision
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|e
parameter_list|)
block|{
comment|// catch on overflow
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|// Division with overflow/zero-divide check. Error produces NULL output.
specifier|public
specifier|static
name|void
name|divideChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|left
parameter_list|,
name|Decimal128
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|Decimal128
operator|.
name|divide
argument_list|(
name|left
argument_list|,
name|right
argument_list|,
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|checkPrecisionOverflow
argument_list|(
name|outputColVector
operator|.
name|precision
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|e
parameter_list|)
block|{
comment|// catch on error
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|// Modulo operator with overflow/zero-divide check.
specifier|public
specifier|static
name|void
name|moduloChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|left
parameter_list|,
name|Decimal128
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|Decimal128
operator|.
name|modulo
argument_list|(
name|left
argument_list|,
name|right
argument_list|,
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|checkPrecisionOverflow
argument_list|(
name|outputColVector
operator|.
name|precision
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|e
parameter_list|)
block|{
comment|// catch on error
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|floor
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|Decimal128
name|result
init|=
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
decl_stmt|;
name|result
operator|.
name|update
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|result
operator|.
name|zeroFractionPart
argument_list|(
name|scratchUInt128
argument_list|)
expr_stmt|;
name|result
operator|.
name|changeScaleDestructive
argument_list|(
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|result
operator|.
name|compareTo
argument_list|(
name|input
argument_list|)
operator|!=
literal|0
operator|)
operator|&&
name|input
operator|.
name|getSignum
argument_list|()
operator|<
literal|0
condition|)
block|{
name|result
operator|.
name|subtractDestructive
argument_list|(
name|DECIMAL_ONE
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|e
parameter_list|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|ceiling
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|Decimal128
name|result
init|=
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
decl_stmt|;
name|result
operator|.
name|update
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|result
operator|.
name|zeroFractionPart
argument_list|(
name|scratchUInt128
argument_list|)
expr_stmt|;
name|result
operator|.
name|changeScaleDestructive
argument_list|(
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|result
operator|.
name|compareTo
argument_list|(
name|input
argument_list|)
operator|!=
literal|0
operator|)
operator|&&
name|input
operator|.
name|getSignum
argument_list|()
operator|>
literal|0
condition|)
block|{
name|result
operator|.
name|addDestructive
argument_list|(
name|DECIMAL_ONE
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|e
parameter_list|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|round
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
name|HiveDecimal
name|inputHD
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
name|input
operator|.
name|toBigDecimal
argument_list|()
argument_list|)
decl_stmt|;
name|HiveDecimal
name|result
init|=
name|RoundUtils
operator|.
name|round
argument_list|(
name|inputHD
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|update
argument_list|(
name|result
operator|.
name|bigDecimalValue
argument_list|()
operator|.
name|toPlainString
argument_list|()
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|sign
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|input
parameter_list|,
name|LongColumnVector
name|outputColVector
parameter_list|)
block|{
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|input
operator|.
name|getSignum
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|abs
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
name|Decimal128
name|result
init|=
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
decl_stmt|;
try|try
block|{
name|result
operator|.
name|update
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|result
operator|.
name|absDestructive
argument_list|()
expr_stmt|;
name|result
operator|.
name|changeScaleDestructive
argument_list|(
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|e
parameter_list|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|negate
parameter_list|(
name|int
name|i
parameter_list|,
name|Decimal128
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
name|Decimal128
name|result
init|=
name|outputColVector
operator|.
name|vector
index|[
name|i
index|]
decl_stmt|;
try|try
block|{
name|result
operator|.
name|update
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|result
operator|.
name|negateDestructive
argument_list|()
expr_stmt|;
name|result
operator|.
name|changeScaleDestructive
argument_list|(
name|outputColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|e
parameter_list|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

