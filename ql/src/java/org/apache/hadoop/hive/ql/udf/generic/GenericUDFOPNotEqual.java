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
name|LongColNotEqualLongColumn
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
name|LongColNotEqualLongScalar
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
name|LongScalarNotEqualLongColumn
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

begin_comment
comment|/**  * GenericUDF Class for operation Not EQUAL.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"<>"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns TRUE if a is not equal to b"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColNotEqualLongColumn
operator|.
name|class
block|,
name|LongColNotEqualDoubleColumn
operator|.
name|class
block|,
name|DoubleColNotEqualLongColumn
operator|.
name|class
block|,
name|DoubleColNotEqualDoubleColumn
operator|.
name|class
block|,
name|LongColNotEqualLongScalar
operator|.
name|class
block|,
name|LongColNotEqualDoubleScalar
operator|.
name|class
block|,
name|DoubleColNotEqualLongScalar
operator|.
name|class
block|,
name|DoubleColNotEqualDoubleScalar
operator|.
name|class
block|,
name|LongScalarNotEqualLongColumn
operator|.
name|class
block|,
name|LongScalarNotEqualDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarNotEqualLongColumn
operator|.
name|class
block|,
name|DoubleScalarNotEqualDoubleColumn
operator|.
name|class
block|,
name|StringGroupColNotEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColNotEqualStringGroupColumn
operator|.
name|class
block|,
name|StringGroupColNotEqualStringScalar
operator|.
name|class
block|,
name|StringGroupColNotEqualVarCharScalar
operator|.
name|class
block|,
name|StringGroupColNotEqualCharScalar
operator|.
name|class
block|,
name|StringScalarNotEqualStringGroupColumn
operator|.
name|class
block|,
name|VarCharScalarNotEqualStringGroupColumn
operator|.
name|class
block|,
name|CharScalarNotEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColNotEqualStringScalar
operator|.
name|class
block|,
name|FilterStringScalarNotEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColNotEqualVarCharScalar
operator|.
name|class
block|,
name|FilterVarCharScalarNotEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColNotEqualCharScalar
operator|.
name|class
block|,
name|FilterCharScalarNotEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterLongColNotEqualLongColumn
operator|.
name|class
block|,
name|FilterLongColNotEqualDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleColNotEqualLongColumn
operator|.
name|class
block|,
name|FilterDoubleColNotEqualDoubleColumn
operator|.
name|class
block|,
name|FilterLongColNotEqualLongScalar
operator|.
name|class
block|,
name|FilterLongColNotEqualDoubleScalar
operator|.
name|class
block|,
name|FilterDoubleColNotEqualLongScalar
operator|.
name|class
block|,
name|FilterDoubleColNotEqualDoubleScalar
operator|.
name|class
block|,
name|FilterLongScalarNotEqualLongColumn
operator|.
name|class
block|,
name|FilterLongScalarNotEqualDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleScalarNotEqualLongColumn
operator|.
name|class
block|,
name|FilterDoubleScalarNotEqualDoubleColumn
operator|.
name|class
block|,
name|FilterDecimalColNotEqualDecimalColumn
operator|.
name|class
block|,
name|FilterDecimalColNotEqualDecimalScalar
operator|.
name|class
block|,
name|FilterDecimalScalarNotEqualDecimalColumn
operator|.
name|class
block|,
name|FilterDecimal64ColNotEqualDecimal64Column
operator|.
name|class
block|,
name|FilterDecimal64ColNotEqualDecimal64Scalar
operator|.
name|class
block|,
name|FilterDecimal64ScalarNotEqualDecimal64Column
operator|.
name|class
block|,
name|TimestampColNotEqualTimestampColumn
operator|.
name|class
block|,
name|TimestampColNotEqualTimestampScalar
operator|.
name|class
block|,
name|TimestampScalarNotEqualTimestampColumn
operator|.
name|class
block|,
name|TimestampColNotEqualLongColumn
operator|.
name|class
block|,
name|TimestampColNotEqualLongScalar
operator|.
name|class
block|,
name|TimestampScalarNotEqualLongColumn
operator|.
name|class
block|,
name|TimestampColNotEqualDoubleColumn
operator|.
name|class
block|,
name|TimestampColNotEqualDoubleScalar
operator|.
name|class
block|,
name|TimestampScalarNotEqualDoubleColumn
operator|.
name|class
block|,
name|LongColNotEqualTimestampColumn
operator|.
name|class
block|,
name|LongColNotEqualTimestampScalar
operator|.
name|class
block|,
name|LongScalarNotEqualTimestampColumn
operator|.
name|class
block|,
name|DoubleColNotEqualTimestampColumn
operator|.
name|class
block|,
name|DoubleColNotEqualTimestampScalar
operator|.
name|class
block|,
name|DoubleScalarNotEqualTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColNotEqualTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColNotEqualTimestampScalar
operator|.
name|class
block|,
name|FilterTimestampScalarNotEqualTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColNotEqualLongColumn
operator|.
name|class
block|,
name|FilterTimestampColNotEqualLongScalar
operator|.
name|class
block|,
name|FilterTimestampScalarNotEqualLongColumn
operator|.
name|class
block|,
name|FilterTimestampColNotEqualDoubleColumn
operator|.
name|class
block|,
name|FilterTimestampColNotEqualDoubleScalar
operator|.
name|class
block|,
name|FilterTimestampScalarNotEqualDoubleColumn
operator|.
name|class
block|,
name|FilterLongColNotEqualTimestampColumn
operator|.
name|class
block|,
name|FilterLongColNotEqualTimestampScalar
operator|.
name|class
block|,
name|FilterLongScalarNotEqualTimestampColumn
operator|.
name|class
block|,
name|FilterDoubleColNotEqualTimestampColumn
operator|.
name|class
block|,
name|FilterDoubleColNotEqualTimestampScalar
operator|.
name|class
block|,
name|FilterDoubleScalarNotEqualTimestampColumn
operator|.
name|class
block|,
name|IntervalYearMonthScalarNotEqualIntervalYearMonthColumn
operator|.
name|class
block|,
name|FilterIntervalYearMonthScalarNotEqualIntervalYearMonthColumn
operator|.
name|class
block|,
name|IntervalYearMonthColNotEqualIntervalYearMonthScalar
operator|.
name|class
block|,
name|FilterIntervalYearMonthColNotEqualIntervalYearMonthScalar
operator|.
name|class
block|,
name|IntervalDayTimeColNotEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|FilterIntervalDayTimeColNotEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|IntervalDayTimeScalarNotEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|FilterIntervalDayTimeScalarNotEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|IntervalDayTimeColNotEqualIntervalDayTimeScalar
operator|.
name|class
block|,
name|FilterIntervalDayTimeColNotEqualIntervalDayTimeScalar
operator|.
name|class
block|,
name|DateColNotEqualDateScalar
operator|.
name|class
block|,
name|FilterDateColNotEqualDateScalar
operator|.
name|class
block|,
name|DateScalarNotEqualDateColumn
operator|.
name|class
block|,
name|FilterDateScalarNotEqualDateColumn
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
name|GenericUDFOPNotEqual
extends|extends
name|GenericUDFBaseCompare
block|{
specifier|public
name|GenericUDFOPNotEqual
parameter_list|()
block|{
name|this
operator|.
name|opName
operator|=
literal|"NOT EQUAL"
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"<>"
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
name|result
operator|.
name|set
argument_list|(
operator|!
name|soi0
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o0
argument_list|)
operator|.
name|equals
argument_list|(
name|soi1
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o1
argument_list|)
argument_list|)
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
operator|!=
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
operator|!=
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
operator|!=
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
name|result
operator|.
name|set
argument_list|(
name|boi0
operator|.
name|get
argument_list|(
name|o0
argument_list|)
operator|!=
name|boi1
operator|.
name|get
argument_list|(
name|o1
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|COMPARE_STRING
case|:
name|result
operator|.
name|set
argument_list|(
operator|!
name|soi0
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o0
argument_list|)
operator|.
name|equals
argument_list|(
name|soi1
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o1
argument_list|)
argument_list|)
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
operator|!=
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
operator|!=
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
name|negative
parameter_list|()
block|{
return|return
operator|new
name|GenericUDFOPEqual
argument_list|()
return|;
block|}
block|}
end_class

end_unit

