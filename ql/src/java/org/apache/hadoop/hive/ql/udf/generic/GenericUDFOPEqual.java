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
name|LongColEqualLongColumn
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
name|LongColEqualLongScalar
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
name|LongScalarEqualLongColumn
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
comment|/**  * GenericUDF Class for operation EQUAL.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"="
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns TRUE if a equals b and false otherwise"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColEqualLongColumn
operator|.
name|class
block|,
name|LongColEqualDoubleColumn
operator|.
name|class
block|,
name|DoubleColEqualLongColumn
operator|.
name|class
block|,
name|DoubleColEqualDoubleColumn
operator|.
name|class
block|,
name|LongColEqualLongScalar
operator|.
name|class
block|,
name|LongColEqualDoubleScalar
operator|.
name|class
block|,
name|DoubleColEqualLongScalar
operator|.
name|class
block|,
name|DoubleColEqualDoubleScalar
operator|.
name|class
block|,
name|LongScalarEqualLongColumn
operator|.
name|class
block|,
name|LongScalarEqualDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarEqualLongColumn
operator|.
name|class
block|,
name|DoubleScalarEqualDoubleColumn
operator|.
name|class
block|,
name|StringGroupColEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColEqualStringGroupColumn
operator|.
name|class
block|,
name|StringGroupColEqualStringScalar
operator|.
name|class
block|,
name|StringGroupColEqualVarCharScalar
operator|.
name|class
block|,
name|StringGroupColEqualCharScalar
operator|.
name|class
block|,
name|StringScalarEqualStringGroupColumn
operator|.
name|class
block|,
name|VarCharScalarEqualStringGroupColumn
operator|.
name|class
block|,
name|CharScalarEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColEqualStringScalar
operator|.
name|class
block|,
name|FilterStringScalarEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColEqualVarCharScalar
operator|.
name|class
block|,
name|FilterVarCharScalarEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterStringGroupColEqualCharScalar
operator|.
name|class
block|,
name|FilterCharScalarEqualStringGroupColumn
operator|.
name|class
block|,
name|FilterLongColEqualLongColumn
operator|.
name|class
block|,
name|FilterLongColEqualDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleColEqualLongColumn
operator|.
name|class
block|,
name|FilterDoubleColEqualDoubleColumn
operator|.
name|class
block|,
name|FilterLongColEqualLongScalar
operator|.
name|class
block|,
name|FilterLongColEqualDoubleScalar
operator|.
name|class
block|,
name|FilterDoubleColEqualLongScalar
operator|.
name|class
block|,
name|FilterDoubleColEqualDoubleScalar
operator|.
name|class
block|,
name|FilterLongScalarEqualLongColumn
operator|.
name|class
block|,
name|FilterLongScalarEqualDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleScalarEqualLongColumn
operator|.
name|class
block|,
name|FilterDoubleScalarEqualDoubleColumn
operator|.
name|class
block|,
name|FilterDecimalColEqualDecimalColumn
operator|.
name|class
block|,
name|FilterDecimalColEqualDecimalScalar
operator|.
name|class
block|,
name|FilterDecimalScalarEqualDecimalColumn
operator|.
name|class
block|,
name|TimestampColEqualTimestampScalar
operator|.
name|class
block|,
name|TimestampScalarEqualTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColEqualTimestampScalar
operator|.
name|class
block|,
name|FilterTimestampScalarEqualTimestampColumn
operator|.
name|class
block|,
name|TimestampColEqualLongScalar
operator|.
name|class
block|,
name|LongScalarEqualTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColEqualLongScalar
operator|.
name|class
block|,
name|FilterLongScalarEqualTimestampColumn
operator|.
name|class
block|,
name|TimestampColEqualDoubleScalar
operator|.
name|class
block|,
name|DoubleScalarEqualTimestampColumn
operator|.
name|class
block|,
name|FilterTimestampColEqualDoubleScalar
operator|.
name|class
block|,
name|FilterDoubleScalarEqualTimestampColumn
operator|.
name|class
block|,
name|IntervalYearMonthScalarEqualIntervalYearMonthColumn
operator|.
name|class
block|,
name|FilterIntervalYearMonthScalarEqualIntervalYearMonthColumn
operator|.
name|class
block|,
name|IntervalYearMonthColEqualIntervalYearMonthScalar
operator|.
name|class
block|,
name|FilterIntervalYearMonthColEqualIntervalYearMonthScalar
operator|.
name|class
block|,
name|IntervalDayTimeScalarEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|FilterIntervalDayTimeScalarEqualIntervalDayTimeColumn
operator|.
name|class
block|,
name|IntervalDayTimeColEqualIntervalDayTimeScalar
operator|.
name|class
block|,
name|FilterIntervalDayTimeColEqualIntervalDayTimeScalar
operator|.
name|class
block|,
name|DateColEqualDateScalar
operator|.
name|class
block|,
name|FilterDateColEqualDateScalar
operator|.
name|class
block|,
name|DateScalarEqualDateColumn
operator|.
name|class
block|,
name|FilterDateScalarEqualDateColumn
operator|.
name|class
block|,
block|}
argument_list|)
annotation|@
name|NDV
argument_list|(
name|maxNdv
operator|=
literal|2
argument_list|)
specifier|public
class|class
name|GenericUDFOPEqual
extends|extends
name|GenericUDFBaseCompare
block|{
specifier|public
name|GenericUDFOPEqual
parameter_list|()
block|{
name|this
operator|.
name|opName
operator|=
literal|"EQUAL"
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"="
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
operator|==
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
operator|==
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
operator|==
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
operator|==
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
operator|==
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
operator|==
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
name|GenericUDFOPNotEqual
argument_list|()
return|;
block|}
block|}
end_class

end_unit

