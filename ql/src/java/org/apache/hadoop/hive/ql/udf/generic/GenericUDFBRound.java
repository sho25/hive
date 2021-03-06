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
name|expressions
operator|.
name|BRoundWithNumDigitsDoubleToDouble
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
name|FuncBRoundWithNumDigitsDecimalToDecimal
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
name|FuncBRoundDecimalToDecimal
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
name|FuncBRoundDoubleToDouble
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

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"bround"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x[, d]) - round x to d decimal places using HALF_EVEN rounding mode."
argument_list|,
name|extended
operator|=
literal|"Banker's rounding. The value is rounded to the nearest even number. Also known as Gaussian rounding.\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(12.25, 1);\n  12.2"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|FuncBRoundDoubleToDouble
operator|.
name|class
block|,
name|BRoundWithNumDigitsDoubleToDouble
operator|.
name|class
block|,
name|FuncBRoundWithNumDigitsDecimalToDecimal
operator|.
name|class
block|,
name|FuncBRoundDecimalToDecimal
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFBRound
extends|extends
name|GenericUDFRound
block|{
annotation|@
name|Override
specifier|protected
name|HiveDecimalWritable
name|round
parameter_list|(
name|HiveDecimalWritable
name|inputDecWritable
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|HiveDecimalWritable
name|result
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|inputDecWritable
argument_list|)
decl_stmt|;
name|result
operator|.
name|mutateSetScale
argument_list|(
name|scale
argument_list|,
name|HiveDecimal
operator|.
name|ROUND_HALF_EVEN
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|protected
name|long
name|round
parameter_list|(
name|long
name|input
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
return|return
name|RoundUtils
operator|.
name|bround
argument_list|(
name|input
argument_list|,
name|scale
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|double
name|round
parameter_list|(
name|double
name|input
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
return|return
name|RoundUtils
operator|.
name|bround
argument_list|(
name|input
argument_list|,
name|scale
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|DoubleWritable
name|round
parameter_list|(
name|DoubleWritable
name|input
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|double
name|d
init|=
name|input
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|Double
operator|.
name|isNaN
argument_list|(
name|d
argument_list|)
operator|||
name|Double
operator|.
name|isInfinite
argument_list|(
name|d
argument_list|)
condition|)
block|{
return|return
operator|new
name|DoubleWritable
argument_list|(
name|d
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|DoubleWritable
argument_list|(
name|RoundUtils
operator|.
name|bround
argument_list|(
name|d
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
return|return
name|getStandardDisplayString
argument_list|(
literal|"bround"
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

end_unit

