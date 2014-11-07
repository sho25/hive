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
name|int
name|compare
parameter_list|(
name|HiveDecimalWritable
name|writableLeft
parameter_list|,
name|HiveDecimal
name|right
parameter_list|)
block|{
return|return
name|writableLeft
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|compareTo
argument_list|(
name|right
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|compare
parameter_list|(
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimalWritable
name|writableRight
parameter_list|)
block|{
return|return
name|left
operator|.
name|compareTo
argument_list|(
name|writableRight
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
return|;
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
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|add
argument_list|(
name|right
argument_list|)
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
specifier|public
specifier|static
name|void
name|addChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|add
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
specifier|public
specifier|static
name|void
name|addChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|add
argument_list|(
name|right
argument_list|)
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
specifier|public
specifier|static
name|void
name|addChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|add
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|subtract
argument_list|(
name|right
argument_list|)
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
specifier|public
specifier|static
name|void
name|subtractChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|subtract
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
specifier|public
specifier|static
name|void
name|subtractChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|subtract
argument_list|(
name|right
argument_list|)
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
specifier|public
specifier|static
name|void
name|subtractChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|subtract
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|multiply
argument_list|(
name|right
argument_list|)
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
specifier|public
specifier|static
name|void
name|multiplyChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|multiply
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
specifier|public
specifier|static
name|void
name|multiplyChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|multiply
argument_list|(
name|right
argument_list|)
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
specifier|public
specifier|static
name|void
name|multiplyChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|multiply
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|divide
argument_list|(
name|right
argument_list|)
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
name|divideChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|divide
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
name|divideChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|divide
argument_list|(
name|right
argument_list|)
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
name|divideChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|divide
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|remainder
argument_list|(
name|right
argument_list|)
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
name|moduloChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|remainder
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
name|moduloChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|remainder
argument_list|(
name|right
argument_list|)
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
name|moduloChecked
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimalWritable
name|right
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|left
operator|.
name|remainder
argument_list|(
name|right
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
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
name|HiveDecimal
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|input
operator|.
name|setScale
argument_list|(
literal|0
argument_list|,
name|HiveDecimal
operator|.
name|ROUND_FLOOR
argument_list|)
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
name|floor
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|input
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|setScale
argument_list|(
literal|0
argument_list|,
name|HiveDecimal
operator|.
name|ROUND_FLOOR
argument_list|)
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
name|ceiling
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimal
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|input
operator|.
name|setScale
argument_list|(
literal|0
argument_list|,
name|HiveDecimal
operator|.
name|ROUND_CEILING
argument_list|)
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
name|ceiling
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|input
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|setScale
argument_list|(
literal|0
argument_list|,
name|HiveDecimal
operator|.
name|ROUND_CEILING
argument_list|)
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
name|round
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimal
name|input
parameter_list|,
name|int
name|decimalPlaces
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|RoundUtils
operator|.
name|round
argument_list|(
name|input
argument_list|,
name|decimalPlaces
argument_list|)
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
name|round
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|input
parameter_list|,
name|int
name|decimalPlaces
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|RoundUtils
operator|.
name|round
argument_list|(
name|input
operator|.
name|getHiveDecimal
argument_list|()
argument_list|,
name|decimalPlaces
argument_list|)
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
name|round
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimal
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|RoundUtils
operator|.
name|round
argument_list|(
name|input
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
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
name|round
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|RoundUtils
operator|.
name|round
argument_list|(
name|input
operator|.
name|getHiveDecimal
argument_list|()
argument_list|,
name|outputColVector
operator|.
name|scale
argument_list|)
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
name|sign
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimal
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
name|signum
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|sign
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
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
name|getHiveDecimal
argument_list|()
operator|.
name|signum
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
name|HiveDecimal
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|input
operator|.
name|abs
argument_list|()
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
name|abs
parameter_list|(
name|int
name|i
parameter_list|,
name|HiveDecimalWritable
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|input
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|abs
argument_list|()
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
name|HiveDecimal
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|input
operator|.
name|negate
argument_list|()
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
name|HiveDecimalWritable
name|input
parameter_list|,
name|DecimalColumnVector
name|outputColVector
parameter_list|)
block|{
try|try
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|input
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|negate
argument_list|()
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

