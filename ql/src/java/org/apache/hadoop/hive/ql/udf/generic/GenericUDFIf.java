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
name|UDFArgumentException
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
name|UDFArgumentLengthException
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
name|UDFArgumentTypeException
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
name|serde
operator|.
name|serdeConstants
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
name|ObjectInspector
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
name|PrimitiveObjectInspector
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
name|primitive
operator|.
name|BooleanObjectInspector
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
name|IfExprDecimalColumnColumn
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
name|IfExprDecimalColumnScalar
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
name|IfExprDecimalScalarColumn
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
name|IfExprDecimalScalarScalar
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
name|IfExprLongColumnLongScalar
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
name|IfExprDoubleColumnDoubleScalar
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
name|IfExprDoubleColumnLongScalar
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
name|IfExprLongColumnDoubleScalar
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
name|IfExprLongScalarLongColumn
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
name|IfExprDoubleScalarDoubleColumn
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
name|IfExprDoubleScalarLongColumn
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
name|IfExprLongScalarDoubleColumn
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
name|IfExprLongScalarLongScalar
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
name|IfExprDoubleScalarDoubleScalar
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
name|IfExprDoubleScalarLongScalar
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
name|IfExprLongScalarDoubleScalar
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
name|IfExprDoubleColumnDoubleColumn
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
name|IfExprIntervalDayTimeColumnColumn
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
name|IfExprIntervalDayTimeColumnScalar
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
name|IfExprIntervalDayTimeScalarColumn
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
name|IfExprIntervalDayTimeScalarScalar
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
name|IfExprDecimal64ColumnDecimal64Column
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
name|IfExprDecimal64ColumnDecimal64Scalar
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
name|IfExprDecimal64ScalarDecimal64Column
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
name|IfExprDecimal64ScalarDecimal64Scalar
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
name|IfExprLongColumnLongColumn
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
name|IfExprStringGroupColumnStringGroupColumn
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
name|IfExprStringGroupColumnStringScalar
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
name|IfExprStringGroupColumnCharScalar
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
name|IfExprStringGroupColumnVarCharScalar
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
name|IfExprStringScalarStringGroupColumn
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
name|IfExprCharScalarStringGroupColumn
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
name|IfExprTimestampColumnColumn
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
name|IfExprTimestampColumnScalar
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
name|IfExprTimestampScalarColumn
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
name|IfExprTimestampScalarScalar
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
name|IfExprVarCharScalarStringGroupColumn
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
name|IfExprStringScalarStringScalar
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
name|IfExprStringScalarCharScalar
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
name|IfExprStringScalarVarCharScalar
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
name|IfExprCharScalarStringScalar
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
name|IfExprVarCharScalarStringScalar
import|;
end_import

begin_comment
comment|/**  * IF(expr1,expr2,expr3)<br>  * If expr1 is TRUE (expr1&lt;&gt; 0 and expr1&lt;&gt; NULL) then IF() returns expr2;  * otherwise it returns expr3. IF() returns a numeric or string value, depending  * on the context in which it is used.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"if"
argument_list|,
name|value
operator|=
literal|"IF(expr1,expr2,expr3) - If expr1 is TRUE (expr1<> 0 and expr1<> NULL) then"
operator|+
literal|" IF() returns expr2; otherwise it returns expr3. IF() returns a numeric or string value,"
operator|+
literal|" depending on the context in which it is used."
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|IfExprLongColumnLongColumn
operator|.
name|class
block|,
name|IfExprDoubleColumnDoubleColumn
operator|.
name|class
block|,
name|IfExprLongColumnLongScalar
operator|.
name|class
block|,
name|IfExprDoubleColumnDoubleScalar
operator|.
name|class
block|,
name|IfExprLongColumnDoubleScalar
operator|.
name|class
block|,
name|IfExprDoubleColumnLongScalar
operator|.
name|class
block|,
name|IfExprLongScalarLongColumn
operator|.
name|class
block|,
name|IfExprDoubleScalarDoubleColumn
operator|.
name|class
block|,
name|IfExprLongScalarDoubleColumn
operator|.
name|class
block|,
name|IfExprDoubleScalarLongColumn
operator|.
name|class
block|,
name|IfExprLongScalarLongScalar
operator|.
name|class
block|,
name|IfExprDoubleScalarDoubleScalar
operator|.
name|class
block|,
name|IfExprLongScalarDoubleScalar
operator|.
name|class
block|,
name|IfExprDoubleScalarLongScalar
operator|.
name|class
block|,
name|IfExprDecimal64ColumnDecimal64Column
operator|.
name|class
block|,
name|IfExprDecimal64ColumnDecimal64Scalar
operator|.
name|class
block|,
name|IfExprDecimal64ScalarDecimal64Column
operator|.
name|class
block|,
name|IfExprDecimal64ScalarDecimal64Scalar
operator|.
name|class
block|,
name|IfExprStringGroupColumnStringGroupColumn
operator|.
name|class
block|,
name|IfExprStringGroupColumnStringScalar
operator|.
name|class
block|,
name|IfExprStringGroupColumnCharScalar
operator|.
name|class
block|,
name|IfExprStringGroupColumnVarCharScalar
operator|.
name|class
block|,
name|IfExprStringScalarStringGroupColumn
operator|.
name|class
block|,
name|IfExprCharScalarStringGroupColumn
operator|.
name|class
block|,
name|IfExprVarCharScalarStringGroupColumn
operator|.
name|class
block|,
name|IfExprStringScalarStringScalar
operator|.
name|class
block|,
name|IfExprStringScalarCharScalar
operator|.
name|class
block|,
name|IfExprStringScalarVarCharScalar
operator|.
name|class
block|,
name|IfExprCharScalarStringScalar
operator|.
name|class
block|,
name|IfExprVarCharScalarStringScalar
operator|.
name|class
block|,
name|IfExprDecimalColumnColumn
operator|.
name|class
block|,
name|IfExprDecimalColumnScalar
operator|.
name|class
block|,
name|IfExprDecimalScalarColumn
operator|.
name|class
block|,
name|IfExprDecimalScalarScalar
operator|.
name|class
block|,
name|IfExprIntervalDayTimeColumnColumn
operator|.
name|class
block|,
name|IfExprIntervalDayTimeColumnScalar
operator|.
name|class
block|,
name|IfExprIntervalDayTimeScalarColumn
operator|.
name|class
block|,
name|IfExprIntervalDayTimeScalarScalar
operator|.
name|class
block|,
name|IfExprTimestampColumnColumn
operator|.
name|class
block|,
name|IfExprTimestampColumnScalar
operator|.
name|class
block|,
name|IfExprTimestampScalarColumn
operator|.
name|class
block|,
name|IfExprTimestampScalarScalar
operator|.
name|class
block|,
block|}
argument_list|)
annotation|@
name|VectorizedExpressionsSupportDecimal64
argument_list|()
specifier|public
class|class
name|GenericUDFIf
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|ObjectInspector
index|[]
name|argumentOIs
decl_stmt|;
specifier|private
specifier|transient
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
name|returnOIResolver
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|argumentOIs
operator|=
name|arguments
expr_stmt|;
name|returnOIResolver
operator|=
operator|new
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|arguments
operator|.
name|length
operator|!=
literal|3
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The function IF(expr1,expr2,expr3) accepts exactly 3 arguments."
argument_list|)
throw|;
block|}
name|boolean
name|conditionTypeIsOk
init|=
operator|(
name|arguments
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
operator|)
decl_stmt|;
if|if
condition|(
name|conditionTypeIsOk
condition|)
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
operator|)
decl_stmt|;
name|conditionTypeIsOk
operator|=
operator|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|BOOLEAN
operator|||
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|VOID
operator|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|conditionTypeIsOk
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"The first argument of function IF should be \""
operator|+
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
operator|+
literal|"\", but \""
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" is found"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
operator|(
name|returnOIResolver
operator|.
name|update
argument_list|(
name|arguments
index|[
literal|1
index|]
argument_list|)
operator|&&
name|returnOIResolver
operator|.
name|update
argument_list|(
name|arguments
index|[
literal|2
index|]
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|2
argument_list|,
literal|"The second and the third arguments of function IF should have the same type, "
operator|+
literal|"but they are different: \""
operator|+
name|arguments
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" and \""
operator|+
name|arguments
index|[
literal|2
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\""
argument_list|)
throw|;
block|}
return|return
name|returnOIResolver
operator|.
name|get
argument_list|()
return|;
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
name|condition
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|condition
operator|!=
literal|null
operator|&&
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|argumentOIs
index|[
literal|0
index|]
operator|)
operator|.
name|get
argument_list|(
name|condition
argument_list|)
condition|)
block|{
return|return
name|returnOIResolver
operator|.
name|convertIfNecessary
argument_list|(
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|argumentOIs
index|[
literal|1
index|]
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|returnOIResolver
operator|.
name|convertIfNecessary
argument_list|(
name|arguments
index|[
literal|2
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|argumentOIs
index|[
literal|2
index|]
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
assert|assert
operator|(
name|children
operator|.
name|length
operator|==
literal|3
operator|)
assert|;
return|return
name|getStandardDisplayString
argument_list|(
literal|"if"
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

end_unit

