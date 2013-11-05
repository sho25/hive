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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|parse
operator|.
name|TypeCheckProcFactory
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
name|plan
operator|.
name|ExprNodeConstantDesc
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
name|plan
operator|.
name|ExprNodeDesc
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
name|testutil
operator|.
name|BaseScalarUdfTest
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
name|testutil
operator|.
name|DataBuilder
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
name|testutil
operator|.
name|OperatorTestUtils
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
name|ByteWritable
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
name|io
operator|.
name|ShortWritable
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
name|InspectableObject
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
name|PrimitiveObjectInspectorFactory
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
name|TypeInfoFactory
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
name|FloatWritable
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
name|IntWritable
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
name|LongWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestGenericUDFRound
extends|extends
name|BaseScalarUdfTest
block|{
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|cols
init|=
block|{
literal|"s"
block|,
literal|"i"
block|,
literal|"d"
block|,
literal|"f"
block|,
literal|"b"
block|,
literal|"sh"
block|,
literal|"l"
block|,
literal|"dec"
block|}
decl_stmt|;
annotation|@
name|Override
specifier|public
name|InspectableObject
index|[]
name|getBaseTable
parameter_list|()
block|{
name|DataBuilder
name|db
init|=
operator|new
name|DataBuilder
argument_list|()
decl_stmt|;
name|db
operator|.
name|setColumnNames
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|db
operator|.
name|setColumnTypes
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaIntObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaDoubleObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaFloatObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaByteObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaShortObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaLongObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|15
argument_list|,
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|"one"
argument_list|,
literal|170
argument_list|,
operator|new
name|Double
argument_list|(
literal|"1.1"
argument_list|)
argument_list|,
operator|new
name|Float
argument_list|(
literal|"32.1234"
argument_list|)
argument_list|,
operator|new
name|Byte
argument_list|(
literal|"25"
argument_list|)
argument_list|,
operator|new
name|Short
argument_list|(
literal|"1234"
argument_list|)
argument_list|,
literal|123456L
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"983.7235"
argument_list|)
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|"-234"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|Float
argument_list|(
literal|"0.347232"
argument_list|)
argument_list|,
operator|new
name|Byte
argument_list|(
literal|"109"
argument_list|)
argument_list|,
operator|new
name|Short
argument_list|(
literal|"551"
argument_list|)
argument_list|,
literal|923L
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"983723.005"
argument_list|)
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|"454.45"
argument_list|,
literal|22345
argument_list|,
operator|new
name|Double
argument_list|(
literal|"-23.00009"
argument_list|)
argument_list|,
operator|new
name|Float
argument_list|(
literal|"-3.4"
argument_list|)
argument_list|,
operator|new
name|Byte
argument_list|(
literal|"76"
argument_list|)
argument_list|,
operator|new
name|Short
argument_list|(
literal|"2321"
argument_list|)
argument_list|,
literal|9232L
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-932032.7"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|db
operator|.
name|createRows
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|InspectableObject
index|[]
name|getExpectedResult
parameter_list|()
block|{
name|DataBuilder
name|db
init|=
operator|new
name|DataBuilder
argument_list|()
decl_stmt|;
name|db
operator|.
name|setColumnNames
argument_list|(
literal|"_col1"
argument_list|,
literal|"_col2"
argument_list|,
literal|"_col3"
argument_list|,
literal|"_col4"
argument_list|,
literal|"_col5"
argument_list|,
literal|"_col6"
argument_list|,
literal|"_col7"
argument_list|,
literal|"_col8"
argument_list|)
expr_stmt|;
name|db
operator|.
name|setColumnTypes
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableFloatObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableByteObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableShortObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableHiveDecimalObjectInspector
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|null
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|170
argument_list|)
argument_list|,
operator|new
name|DoubleWritable
argument_list|(
literal|1.1
argument_list|)
argument_list|,
operator|new
name|FloatWritable
argument_list|(
literal|32f
argument_list|)
argument_list|,
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|1234
argument_list|)
argument_list|,
operator|new
name|LongWritable
argument_list|(
literal|123500L
argument_list|)
argument_list|,
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"983.724"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
operator|-
literal|200
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|FloatWritable
argument_list|(
literal|0f
argument_list|)
argument_list|,
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|100
argument_list|)
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|551
argument_list|)
argument_list|,
operator|new
name|LongWritable
argument_list|(
literal|900L
argument_list|)
argument_list|,
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"983723.005"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
literal|500
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|22345
argument_list|)
argument_list|,
operator|new
name|DoubleWritable
argument_list|(
operator|-
literal|23.000
argument_list|)
argument_list|,
operator|new
name|FloatWritable
argument_list|(
operator|-
literal|3f
argument_list|)
argument_list|,
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|100
argument_list|)
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|2321
argument_list|)
argument_list|,
operator|new
name|LongWritable
argument_list|(
literal|9200L
argument_list|)
argument_list|,
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-932032.7"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|db
operator|.
name|createRows
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getExpressionList
parameter_list|()
throws|throws
name|UDFArgumentException
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|(
name|cols
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cols
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|exprs
operator|.
name|add
argument_list|(
name|OperatorTestUtils
operator|.
name|getStringColumn
argument_list|(
name|cols
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ExprNodeDesc
index|[]
name|scales
init|=
block|{
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
operator|-
literal|2
argument_list|)
block|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|byteTypeInfo
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|shortTypeInfo
argument_list|,
operator|(
name|short
operator|)
literal|3
argument_list|)
block|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
literal|0
argument_list|)
block|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
operator|-
literal|2L
argument_list|)
block|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
literal|0
argument_list|)
block|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
operator|-
literal|2
argument_list|)
block|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
literal|3
argument_list|)
block|}
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|earr
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|cols
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|ExprNodeDesc
name|r
init|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"round"
argument_list|,
name|exprs
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|,
name|scales
index|[
name|j
index|]
argument_list|)
decl_stmt|;
name|earr
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
return|return
name|earr
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalRoundingMetaData
parameter_list|()
throws|throws
name|UDFArgumentException
block|{
name|GenericUDFRound
name|udf
init|=
operator|new
name|GenericUDFRound
argument_list|()
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|3
argument_list|)
argument_list|)
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableConstantObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|PrimitiveObjectInspector
name|outputOI
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
decl_stmt|;
name|DecimalTypeInfo
name|outputTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|outputOI
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|2
argument_list|)
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalRoundingMetaData1
parameter_list|()
throws|throws
name|UDFArgumentException
block|{
name|GenericUDFRound
name|udf
init|=
operator|new
name|GenericUDFRound
argument_list|()
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|3
argument_list|)
argument_list|)
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableConstantObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
operator|new
name|IntWritable
argument_list|(
operator|-
literal|2
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|PrimitiveObjectInspector
name|outputOI
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
decl_stmt|;
name|DecimalTypeInfo
name|outputTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|outputOI
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|5
argument_list|,
literal|0
argument_list|)
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalRoundingMetaData2
parameter_list|()
throws|throws
name|UDFArgumentException
block|{
name|GenericUDFRound
name|udf
init|=
operator|new
name|GenericUDFRound
argument_list|()
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|3
argument_list|)
argument_list|)
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableConstantObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|5
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|PrimitiveObjectInspector
name|outputOI
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
decl_stmt|;
name|DecimalTypeInfo
name|outputTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|outputOI
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|9
argument_list|,
literal|5
argument_list|)
argument_list|,
name|outputTypeInfo
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

