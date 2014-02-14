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
name|LongColDivideLongColumn
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
name|LongColDivideLongScalar
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
name|LongScalarDivideLongColumn
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
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_comment
comment|/**  * Note that in SQL, the return type of divide is not necessarily the same  * as the parameters. For example, 3 / 2 = 1.5, not 1. To follow SQL, we always  * return a decimal for divide.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"/"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Divide a by b"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT 3 _FUNC_ 2 FROM src LIMIT 1;\n"
operator|+
literal|"  1.5"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColDivideLongColumn
operator|.
name|class
block|,
name|LongColDivideDoubleColumn
operator|.
name|class
block|,
name|DoubleColDivideLongColumn
operator|.
name|class
block|,
name|DoubleColDivideDoubleColumn
operator|.
name|class
block|,
name|LongColDivideLongScalar
operator|.
name|class
block|,
name|LongColDivideDoubleScalar
operator|.
name|class
block|,
name|DoubleColDivideLongScalar
operator|.
name|class
block|,
name|DoubleColDivideDoubleScalar
operator|.
name|class
block|,
name|LongScalarDivideLongColumn
operator|.
name|class
block|,
name|LongScalarDivideDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarDivideLongColumn
operator|.
name|class
block|,
name|DoubleScalarDivideDoubleColumn
operator|.
name|class
block|,
name|DecimalColDivideDecimalColumn
operator|.
name|class
block|,
name|DecimalColDivideDecimalScalar
operator|.
name|class
block|,
name|DecimalScalarDivideDecimalColumn
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFOPDivide
extends|extends
name|GenericUDFBaseNumeric
block|{
specifier|public
name|GenericUDFOPDivide
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"/"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|PrimitiveTypeInfo
name|deriveResultExactTypeInfo
parameter_list|()
block|{
comment|// No type promotion. Everything goes to decimal.
return|return
name|deriveResultDecimalTypeInfo
argument_list|()
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
if|if
condition|(
name|right
operator|.
name|get
argument_list|()
operator|==
literal|0.0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|doubleWritable
operator|.
name|set
argument_list|(
name|left
operator|.
name|get
argument_list|()
operator|/
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
if|if
condition|(
name|right
operator|.
name|compareTo
argument_list|(
name|HiveDecimal
operator|.
name|ZERO
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HiveDecimal
name|dec
init|=
name|left
operator|.
name|divide
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
comment|/**    * A balanced way to determine the precision/scale of decimal division result. Integer digits and    * decimal digits are computed independently. However, when the precision from above reaches above    * HiveDecimal.MAX_PRECISION, interger digit and decimal digits are shrunk equally to fit.    */
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
name|intDig
init|=
name|Math
operator|.
name|min
argument_list|(
name|HiveDecimal
operator|.
name|MAX_SCALE
argument_list|,
name|prec1
operator|-
name|scale1
operator|+
name|scale2
argument_list|)
decl_stmt|;
name|int
name|decDig
init|=
name|Math
operator|.
name|min
argument_list|(
name|HiveDecimal
operator|.
name|MAX_SCALE
argument_list|,
name|Math
operator|.
name|max
argument_list|(
literal|6
argument_list|,
name|scale1
operator|+
name|prec2
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|diff
init|=
name|intDig
operator|+
name|decDig
operator|-
name|HiveDecimal
operator|.
name|MAX_SCALE
decl_stmt|;
if|if
condition|(
name|diff
operator|>
literal|0
condition|)
block|{
name|decDig
operator|-=
name|diff
operator|/
literal|2
operator|+
literal|1
expr_stmt|;
comment|// Slight negative bias.
name|intDig
operator|=
name|HiveDecimal
operator|.
name|MAX_SCALE
operator|-
name|decDig
expr_stmt|;
block|}
return|return
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
name|intDig
operator|+
name|decDig
argument_list|,
name|decDig
argument_list|)
return|;
block|}
block|}
end_class

end_unit

