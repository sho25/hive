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
name|LongColGreaterEqualLongColumn
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
name|LongColGreaterEqualLongScalar
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
name|LongScalarGreaterEqualLongColumn
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
comment|/**  * GenericUDF Class for operation EqualOrGreaterThan.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|">="
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns TRUE if a is not smaller than b"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColGreaterEqualLongColumn
operator|.
name|class
block|,
name|LongColGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|DoubleColGreaterEqualLongColumn
operator|.
name|class
block|,
name|DoubleColGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|LongColGreaterEqualLongScalar
operator|.
name|class
block|,
name|LongColGreaterEqualDoubleScalar
operator|.
name|class
block|,
name|DoubleColGreaterEqualLongScalar
operator|.
name|class
block|,
name|DoubleColGreaterEqualDoubleScalar
operator|.
name|class
block|,
name|LongScalarGreaterEqualLongColumn
operator|.
name|class
block|,
name|LongScalarGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarGreaterEqualLongColumn
operator|.
name|class
block|,
name|DoubleScalarGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|StringGroupColGreaterEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColGreaterEqualStringGroupColumn
operator|.
name|class
block|,
name|StringGroupColGreaterEqualStringScalar
operator|.
name|class
block|,
name|StringGroupColGreaterEqualVarCharScalar
operator|.
name|class
block|,
name|StringGroupColGreaterEqualCharScalar
operator|.
name|class
block|,
name|StringScalarGreaterEqualStringGroupColumn
operator|.
name|class
block|,
name|VarCharScalarGreaterEqualStringGroupColumn
operator|.
name|class
block|,
name|CharScalarGreaterEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColGreaterEqualStringScalar
operator|.
name|class
block|,
name|FilterStringScalarGreaterEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColGreaterEqualVarCharScalar
operator|.
name|class
block|,
name|FilterVarCharScalarGreaterEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColGreaterEqualCharScalar
operator|.
name|class
block|,
name|FilterCharScalarGreaterEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterLongColGreaterEqualLongColumn
operator|.
name|class
block|,
name|FilterLongColGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleColGreaterEqualLongColumn
operator|.
name|class
block|,
name|FilterDoubleColGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|FilterLongColGreaterEqualLongScalar
operator|.
name|class
block|,
name|FilterLongColGreaterEqualDoubleScalar
operator|.
name|class
block|,
name|FilterDoubleColGreaterEqualLongScalar
operator|.
name|class
block|,
name|FilterDoubleColGreaterEqualDoubleScalar
operator|.
name|class
block|,
name|FilterLongScalarGreaterEqualLongColumn
operator|.
name|class
block|,
name|FilterLongScalarGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleScalarGreaterEqualLongColumn
operator|.
name|class
block|,
name|FilterDoubleScalarGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|FilterDecimalColGreaterEqualDecimalColumn
operator|.
name|class
block|,
name|FilterDecimalColGreaterEqualDecimalScalar
operator|.
name|class
block|,
name|FilterDecimalScalarGreaterEqualDecimalColumn
operator|.
name|class
block|,
name|FilterDecimal64ColGreaterEqualDecimal64Column
operator|.
name|class
block|,
name|FilterDecimal64ColGreaterEqualDecimal64Scalar
operator|.
name|class
block|,
name|FilterDecimal64ScalarGreaterEqualDecimal64Column
operator|.
name|class
block|,
name|TimestampColGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|TimestampColGreaterEqualTimestampScalar
operator|.
name|class
block|,
name|TimestampScalarGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|TimestampColGreaterEqualLongColumn
operator|.
name|class
block|,
name|TimestampColGreaterEqualLongScalar
operator|.
name|class
block|,
name|TimestampScalarGreaterEqualLongColumn
operator|.
name|class
block|,
name|TimestampColGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|TimestampColGreaterEqualDoubleScalar
operator|.
name|class
block|,
name|TimestampScalarGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|LongColGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|LongColGreaterEqualTimestampScalar
operator|.
name|class
block|,
name|LongScalarGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|DoubleColGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|DoubleColGreaterEqualTimestampScalar
operator|.
name|class
block|,
name|DoubleScalarGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterEqualTimestampScalar
operator|.
name|class
block|,
name|FilterTimestampScalarGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterEqualLongColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterEqualLongScalar
operator|.
name|class
block|,
name|FilterTimestampScalarGreaterEqualLongColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|FilterTimestampColGreaterEqualDoubleScalar
operator|.
name|class
block|,
name|FilterTimestampScalarGreaterEqualDoubleColumn
operator|.
name|class
block|,
name|FilterLongColGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|FilterLongColGreaterEqualTimestampScalar
operator|.
name|class
block|,
name|FilterLongScalarGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|FilterDoubleColGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|FilterDoubleColGreaterEqualTimestampScalar
operator|.
name|class
block|,
name|FilterDoubleScalarGreaterEqualTimestampColumn
operator|.
name|class
block|,
name|IntervalYearMonthScalarGreaterEqualIntervalYearMonthColumn
operator|.
name|class
block|,
name|FilterIntervalYearMonthScalarGreaterEqualIntervalYearMonthColumn
operator|.
name|class
block|,
name|IntervalYearMonthColGreaterEqualIntervalYearMonthScalar
operator|.
name|class
block|,
name|FilterIntervalYearMonthColGreaterEqualIntervalYearMonthScalar
operator|.
name|class
block|,
name|IntervalDayTimeColGreaterEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|FilterIntervalDayTimeColGreaterEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|IntervalDayTimeScalarGreaterEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|FilterIntervalDayTimeScalarGreaterEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|IntervalDayTimeColGreaterEqualIntervalDayTimeScalar
operator|.
name|class
block|,
name|FilterIntervalDayTimeColGreaterEqualIntervalDayTimeScalar
operator|.
name|class
block|,
name|DateColGreaterEqualDateScalar
operator|.
name|class
block|,
name|FilterDateColGreaterEqualDateScalar
operator|.
name|class
block|,
name|DateScalarGreaterEqualDateColumn
operator|.
name|class
block|,
name|FilterDateScalarGreaterEqualDateColumn
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
name|GenericUDFOPEqualOrGreaterThan
extends|extends
name|GenericUDFBaseCompare
block|{
specifier|public
name|GenericUDFOPEqualOrGreaterThan
parameter_list|()
block|{
name|this
operator|.
name|opName
operator|=
literal|"EQUAL OR GREATER THAN"
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|">="
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
operator|>=
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
operator|>=
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
operator|>=
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
operator|>=
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
operator|||
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
operator|>=
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
operator|>=
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
operator|>=
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
name|GenericUDFOPEqualOrLessThan
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
name|GenericUDFOPLessThan
argument_list|()
return|;
block|}
block|}
end_class

end_unit

