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
name|hive
operator|.
name|shims
operator|.
name|ShimLoader
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
comment|/**  * GenericUDF Class for operation LessThan.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"<"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns TRUE if a is less than b"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColLessLongColumn
operator|.
name|class
block|,
name|LongColLessDoubleColumn
operator|.
name|class
block|,
name|DoubleColLessLongColumn
operator|.
name|class
block|,
name|DoubleColLessDoubleColumn
operator|.
name|class
block|,
name|LongColLessLongScalar
operator|.
name|class
block|,
name|LongColLessDoubleScalar
operator|.
name|class
block|,
name|DoubleColLessLongScalar
operator|.
name|class
block|,
name|DoubleColLessDoubleScalar
operator|.
name|class
block|,
name|LongScalarLessLongColumn
operator|.
name|class
block|,
name|LongScalarLessDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarLessLongColumn
operator|.
name|class
block|,
name|DoubleScalarLessDoubleColumn
operator|.
name|class
block|,
name|StringColLessStringColumn
operator|.
name|class
block|,
name|StringColLessStringScalar
operator|.
name|class
block|,
name|StringScalarLessStringColumn
operator|.
name|class
block|,
name|FilterStringColLessStringColumn
operator|.
name|class
block|,
name|FilterStringColLessStringScalar
operator|.
name|class
block|,
name|FilterStringScalarLessStringColumn
operator|.
name|class
block|,
name|FilterLongColLessLongColumn
operator|.
name|class
block|,
name|FilterLongColLessDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleColLessLongColumn
operator|.
name|class
block|,
name|FilterDoubleColLessDoubleColumn
operator|.
name|class
block|,
name|FilterLongColLessLongScalar
operator|.
name|class
block|,
name|FilterLongColLessDoubleScalar
operator|.
name|class
block|,
name|FilterDoubleColLessLongScalar
operator|.
name|class
block|,
name|FilterDoubleColLessDoubleScalar
operator|.
name|class
block|,
name|FilterLongScalarLessLongColumn
operator|.
name|class
block|,
name|FilterLongScalarLessDoubleColumn
operator|.
name|class
block|,
name|FilterDoubleScalarLessLongColumn
operator|.
name|class
block|,
name|FilterDoubleScalarLessDoubleColumn
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFOPLessThan
extends|extends
name|GenericUDFBaseCompare
block|{
specifier|public
name|GenericUDFOPLessThan
parameter_list|()
block|{
name|this
operator|.
name|opName
operator|=
literal|"LESS THAN"
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"<"
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
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|compareText
argument_list|(
name|t0
argument_list|,
name|t1
argument_list|)
operator|<
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
operator|<
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
operator|<
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
operator|<
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
operator|!
name|b0
operator|&&
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
operator|<
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
operator|<
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
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

