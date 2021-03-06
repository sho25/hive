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
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|Text
import|;
end_import

begin_comment
comment|/**  * GenericUDF Class for operation GreaterThan.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|">"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns TRUE if a is greater than b"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColGreaterLongColumn
operator|.
name|class
block|,
name|LongColGreaterDoubleColumn
operator|.
name|class
block|,
name|DoubleColGreaterLongColumn
operator|.
name|class
block|,
name|DoubleColGreaterDoubleColumn
operator|.
name|class
block|,
name|LongColGreaterLongScalar
operator|.
name|class
block|,
name|LongColGreaterDoubleScalar
operator|.
name|class
block|,
name|DoubleColGreaterLongScalar
operator|.
name|class
block|,
name|DoubleColGreaterDoubleScalar
operator|.
name|class
block|,
name|LongScalarGreaterLongColumn
operator|.
name|class
block|,
name|LongScalarGreaterDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarGreaterLongColumn
operator|.
name|class
block|,
name|DoubleScalarGreaterDoubleColumn
operator|.
name|class
block|,
name|DecimalColGreaterDecimalColumn
operator|.
name|class
block|,
name|DecimalColGreaterDecimalScalar
operator|.
name|class
block|,
name|DecimalScalarGreaterDecimalColumn
operator|.
name|class
block|,
name|Decimal64ColGreaterDecimal64Column
operator|.
name|class
block|,
name|Decimal64ColGreaterDecimal64Scalar
operator|.
name|class
block|,
name|Decimal64ScalarGreaterDecimal64Column
operator|.
name|class
block|,
name|StringGroupColGreaterStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColGreaterStringGroupColumn
operator|.
name|class
block|,
name|StringGroupColGreaterStringScalar
operator|.
name|class
block|,
name|StringGroupColGreaterVarCharScalar
operator|.
name|class
block|,
name|StringGroupColGreaterCharScalar
operator|.
name|class
block|,
name|StringScalarGreaterStringGroupColumn
operator|.
name|class
block|,
name|VarCharScalarGreaterStringGroupColumn
operator|.
name|class
block|,
name|CharScalarGreaterStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColGreaterStringScalar
operator|.
name|class
block|,
name|FilterStringScalarGreaterStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColGreaterVarCharScalar
operator|.
name|class
block|,
name|FilterVarCharScalarGreaterStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColGreaterCharScalar
operator|.
name|class
block|,
name|FilterCharScalarGreaterStringGroupColumn
operator|.
name|class
block|,
name|FilterLongColGreaterLongColumn
operator|.
name|class
block|,
name|FilterLongColGreaterDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleColGreaterLongColumn
operator|.
name|class
block|,
name|FilterDoubleColGreaterDoubleColumn
operator|.
name|class
block|,
name|FilterLongColGreaterLongScalar
operator|.
name|class
block|,
name|FilterLongColGreaterDoubleScalar
operator|.
name|class
block|,
name|FilterDoubleColGreaterLongScalar
operator|.
name|class
block|,
name|FilterDoubleColGreaterDoubleScalar
operator|.
name|class
block|,
name|FilterLongScalarGreaterLongColumn
operator|.
name|class
block|,
name|FilterLongScalarGreaterDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleScalarGreaterLongColumn
operator|.
name|class
block|,
name|FilterDoubleScalarGreaterDoubleColumn
operator|.
name|class
block|,
name|FilterDecimalColGreaterDecimalColumn
operator|.
name|class
block|,
name|FilterDecimalColGreaterDecimalScalar
operator|.
name|class
block|,
name|FilterDecimalScalarGreaterDecimalColumn
operator|.
name|class
block|,
name|FilterDecimal64ColGreaterDecimal64Column
operator|.
name|class
block|,
name|FilterDecimal64ColGreaterDecimal64Scalar
operator|.
name|class
block|,
name|FilterDecimal64ScalarGreaterDecimal64Column
operator|.
name|class
block|,
name|TimestampColGreaterTimestampColumn
operator|.
name|class
block|,
name|TimestampColGreaterTimestampScalar
operator|.
name|class
block|,
name|TimestampScalarGreaterTimestampColumn
operator|.
name|class
block|,
name|TimestampColGreaterLongColumn
operator|.
name|class
block|,
name|TimestampColGreaterLongScalar
operator|.
name|class
block|,
name|TimestampScalarGreaterLongColumn
operator|.
name|class
block|,
name|TimestampColGreaterDoubleColumn
operator|.
name|class
block|,
name|TimestampColGreaterDoubleScalar
operator|.
name|class
block|,
name|TimestampScalarGreaterDoubleColumn
operator|.
name|class
block|,
name|LongColGreaterTimestampColumn
operator|.
name|class
block|,
name|LongColGreaterTimestampScalar
operator|.
name|class
block|,
name|LongScalarGreaterTimestampColumn
operator|.
name|class
block|,
name|DoubleColGreaterTimestampColumn
operator|.
name|class
block|,
name|DoubleColGreaterTimestampScalar
operator|.
name|class
block|,
name|DoubleScalarGreaterTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterTimestampScalar
operator|.
name|class
block|,
name|FilterTimestampScalarGreaterTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterLongColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterLongScalar
operator|.
name|class
block|,
name|FilterTimestampScalarGreaterLongColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterDoubleColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterDoubleScalar
operator|.
name|class
block|,
name|FilterTimestampScalarGreaterDoubleColumn
operator|.
name|class
block|,
name|FilterLongColGreaterTimestampColumn
operator|.
name|class
block|,
name|FilterLongColGreaterTimestampScalar
operator|.
name|class
block|,
name|FilterLongScalarGreaterTimestampColumn
operator|.
name|class
block|,
name|FilterDoubleColGreaterTimestampColumn
operator|.
name|class
block|,
name|FilterDoubleColGreaterTimestampScalar
operator|.
name|class
block|,
name|FilterDoubleScalarGreaterTimestampColumn
operator|.
name|class
block|,
name|IntervalYearMonthScalarGreaterIntervalYearMonthColumn
operator|.
name|class
block|,
name|FilterIntervalYearMonthScalarGreaterIntervalYearMonthColumn
operator|.
name|class
block|,
name|IntervalYearMonthColGreaterIntervalYearMonthScalar
operator|.
name|class
block|,
name|FilterIntervalYearMonthColGreaterIntervalYearMonthScalar
operator|.
name|class
block|,
name|IntervalDayTimeColGreaterIntervalDayTimeColumn
operator|.
name|class
block|,
name|FilterIntervalDayTimeColGreaterIntervalDayTimeColumn
operator|.
name|class
block|,
name|IntervalDayTimeScalarGreaterIntervalDayTimeColumn
operator|.
name|class
block|,
name|FilterIntervalDayTimeScalarGreaterIntervalDayTimeColumn
operator|.
name|class
block|,
name|IntervalDayTimeColGreaterIntervalDayTimeScalar
operator|.
name|class
block|,
name|FilterIntervalDayTimeColGreaterIntervalDayTimeScalar
operator|.
name|class
block|,
name|DateColGreaterDateScalar
operator|.
name|class
block|,
name|FilterDateColGreaterDateScalar
operator|.
name|class
block|,
name|DateScalarGreaterDateColumn
operator|.
name|class
block|,
name|FilterDateScalarGreaterDateColumn
operator|.
name|class
block|,
block|}
argument_list|)
annotation|@
name|VectorizedExpressionsSupportDecimal64
argument_list|()
annotation|@
name|NDV
argument_list|(
name|maxNdv
operator|=
literal|2
argument_list|)
specifier|public
class|class
name|GenericUDFOPGreaterThan
extends|extends
name|GenericUDFBaseCompare
block|{
specifier|public
name|GenericUDFOPGreaterThan
parameter_list|()
block|{
name|this
operator|.
name|opName
operator|=
literal|"GREATER THAN"
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|">"
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
name|Object
name|o0
decl_stmt|,
name|o1
decl_stmt|;
name|o0
operator|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|o0
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|o1
operator|=
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|o1
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
name|compareType
condition|)
block|{
case|case
name|COMPARE_TEXT
case|:
name|Text
name|t0
decl_stmt|,
name|t1
decl_stmt|;
name|t0
operator|=
name|soi0
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o0
argument_list|)
expr_stmt|;
name|t1
operator|=
name|soi1
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o1
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|t0
operator|.
name|compareTo
argument_list|(
name|t1
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
break|break;
case|case
name|COMPARE_INT
case|:
name|result
operator|.
name|set
argument_list|(
name|ioi0
operator|.
name|get
argument_list|(
name|o0
argument_list|)
operator|>
name|ioi1
operator|.
name|get
argument_list|(
name|o1
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|COMPARE_LONG
case|:
name|result
operator|.
name|set
argument_list|(
name|loi0
operator|.
name|get
argument_list|(
name|o0
argument_list|)
operator|>
name|loi1
operator|.
name|get
argument_list|(
name|o1
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|COMPARE_BYTE
case|:
name|result
operator|.
name|set
argument_list|(
name|byoi0
operator|.
name|get
argument_list|(
name|o0
argument_list|)
operator|>
name|byoi1
operator|.
name|get
argument_list|(
name|o1
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|COMPARE_BOOL
case|:
name|boolean
name|b0
init|=
name|boi0
operator|.
name|get
argument_list|(
name|o0
argument_list|)
decl_stmt|;
name|boolean
name|b1
init|=
name|boi1
operator|.
name|get
argument_list|(
name|o1
argument_list|)
decl_stmt|;
name|result
operator|.
name|set
argument_list|(
name|b0
operator|&&
operator|!
name|b1
argument_list|)
expr_stmt|;
break|break;
case|case
name|COMPARE_STRING
case|:
name|String
name|s0
decl_stmt|,
name|s1
decl_stmt|;
name|s0
operator|=
name|soi0
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o0
argument_list|)
expr_stmt|;
name|s1
operator|=
name|soi1
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o1
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|s0
operator|.
name|compareTo
argument_list|(
name|s1
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
break|break;
case|case
name|SAME_TYPE
case|:
name|result
operator|.
name|set
argument_list|(
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|o0
argument_list|,
name|argumentOIs
index|[
literal|0
index|]
argument_list|,
name|o1
argument_list|,
name|argumentOIs
index|[
literal|1
index|]
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
break|break;
default|default:
name|Object
name|converted_o0
init|=
name|converter0
operator|.
name|convert
argument_list|(
name|o0
argument_list|)
decl_stmt|;
if|if
condition|(
name|converted_o0
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Object
name|converted_o1
init|=
name|converter1
operator|.
name|convert
argument_list|(
name|o1
argument_list|)
decl_stmt|;
if|if
condition|(
name|converted_o1
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|result
operator|.
name|set
argument_list|(
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|converted_o0
argument_list|,
name|compareOI
argument_list|,
name|converted_o1
argument_list|,
name|compareOI
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|GenericUDF
name|flip
parameter_list|()
block|{
return|return
operator|new
name|GenericUDFOPLessThan
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|GenericUDF
name|negative
parameter_list|()
block|{
return|return
operator|new
name|GenericUDFOPEqualOrLessThan
argument_list|()
return|;
block|}
block|}
end_class

end_unit

