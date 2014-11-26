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
name|conf
operator|.
name|HiveConf
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
name|ql
operator|.
name|session
operator|.
name|SessionState
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
name|GenericUDF
operator|.
name|DeferredJavaObject
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
name|GenericUDF
operator|.
name|DeferredObject
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
name|HiveVarcharWritable
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
name|Assert
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
name|TestGenericUDFOPDivide
extends|extends
name|AbstractTestGenericUDFOPNumeric
block|{
annotation|@
name|Test
specifier|public
name|void
name|testByteDivideShort
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
argument_list|()
decl_stmt|;
name|ByteWritable
name|left
init|=
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|4
argument_list|)
decl_stmt|;
name|ShortWritable
name|right
init|=
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|6
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableByteObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableShortObjectInspector
block|}
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|left
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|right
argument_list|)
block|,     }
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|9
argument_list|,
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimalWritable
name|res
init|=
operator|(
name|HiveDecimalWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0.666667"
argument_list|)
argument_list|,
name|res
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVarcharDivideInt
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
argument_list|()
decl_stmt|;
name|HiveVarcharWritable
name|left
init|=
operator|new
name|HiveVarcharWritable
argument_list|()
decl_stmt|;
name|left
operator|.
name|set
argument_list|(
literal|"123"
argument_list|)
expr_stmt|;
name|IntWritable
name|right
init|=
operator|new
name|IntWritable
argument_list|(
literal|456
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableHiveVarcharObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
block|}
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|left
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|right
argument_list|)
block|,     }
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
expr_stmt|;
name|DoubleWritable
name|res
init|=
operator|(
name|DoubleWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|new
name|Double
argument_list|(
literal|123.0
operator|/
literal|456.0
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
name|res
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoubleDivideLong
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
argument_list|()
decl_stmt|;
name|DoubleWritable
name|left
init|=
operator|new
name|DoubleWritable
argument_list|(
literal|4.5
argument_list|)
decl_stmt|;
name|LongWritable
name|right
init|=
operator|new
name|LongWritable
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
block|}
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|left
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|right
argument_list|)
block|,     }
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|DoubleWritable
name|res
init|=
operator|(
name|DoubleWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|new
name|Double
argument_list|(
literal|0.45
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
name|res
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLongDivideDecimal
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
argument_list|()
decl_stmt|;
name|LongWritable
name|left
init|=
operator|new
name|LongWritable
argument_list|(
literal|104
argument_list|)
decl_stmt|;
name|HiveDecimalWritable
name|right
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"234.97"
argument_list|)
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|9
argument_list|,
literal|4
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|left
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|right
argument_list|)
block|,     }
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|33
argument_list|,
literal|10
argument_list|)
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimalWritable
name|res
init|=
operator|(
name|HiveDecimalWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0.4426096949"
argument_list|)
argument_list|,
name|res
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFloatDivideFloat
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
argument_list|()
decl_stmt|;
name|FloatWritable
name|f1
init|=
operator|new
name|FloatWritable
argument_list|(
literal|4.5f
argument_list|)
decl_stmt|;
name|FloatWritable
name|f2
init|=
operator|new
name|FloatWritable
argument_list|(
literal|1.5f
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableFloatObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableFloatObjectInspector
block|}
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|f1
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|f2
argument_list|)
block|,     }
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
expr_stmt|;
name|DoubleWritable
name|res
init|=
operator|(
name|DoubleWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|new
name|Double
argument_list|(
literal|3.0
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
name|res
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDouleDivideDecimal
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
argument_list|()
decl_stmt|;
name|DoubleWritable
name|left
init|=
operator|new
name|DoubleWritable
argument_list|(
literal|74.52
argument_list|)
decl_stmt|;
name|HiveDecimalWritable
name|right
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"234.97"
argument_list|)
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|5
argument_list|,
literal|2
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|left
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|right
argument_list|)
block|,     }
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|DoubleWritable
name|res
init|=
operator|(
name|DoubleWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|new
name|Double
argument_list|(
literal|74.52
operator|/
literal|234.97
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
name|res
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalDivideDecimal
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
argument_list|()
decl_stmt|;
name|HiveDecimalWritable
name|left
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"14.5"
argument_list|)
argument_list|)
decl_stmt|;
name|HiveDecimalWritable
name|right
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"234.97"
argument_list|)
argument_list|)
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
literal|3
argument_list|,
literal|1
argument_list|)
argument_list|)
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|5
argument_list|,
literal|2
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|left
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|right
argument_list|)
block|,     }
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|11
argument_list|,
literal|7
argument_list|)
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimalWritable
name|res
init|=
operator|(
name|HiveDecimalWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0.06171"
argument_list|)
argument_list|,
name|res
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalDivideDecimal2
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
argument_list|()
decl_stmt|;
name|HiveDecimalWritable
name|left
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"5"
argument_list|)
argument_list|)
decl_stmt|;
name|HiveDecimalWritable
name|right
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"25"
argument_list|)
argument_list|)
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
literal|1
argument_list|,
literal|0
argument_list|)
argument_list|)
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|2
argument_list|,
literal|0
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|left
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|right
argument_list|)
block|,     }
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
literal|6
argument_list|)
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimalWritable
name|res
init|=
operator|(
name|HiveDecimalWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0.2"
argument_list|)
argument_list|,
name|res
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalDivideDecimalSameParams
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
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
literal|5
argument_list|,
literal|2
argument_list|)
argument_list|)
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|5
argument_list|,
literal|2
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|13
argument_list|,
literal|8
argument_list|)
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalDivisionResultType
parameter_list|()
throws|throws
name|HiveException
block|{
name|testDecimalDivisionResultType
argument_list|(
literal|5
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|11
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|testDecimalDivisionResultType
argument_list|(
literal|38
argument_list|,
literal|18
argument_list|,
literal|38
argument_list|,
literal|18
argument_list|,
literal|38
argument_list|,
literal|18
argument_list|)
expr_stmt|;
name|testDecimalDivisionResultType
argument_list|(
literal|38
argument_list|,
literal|18
argument_list|,
literal|20
argument_list|,
literal|0
argument_list|,
literal|38
argument_list|,
literal|27
argument_list|)
expr_stmt|;
name|testDecimalDivisionResultType
argument_list|(
literal|20
argument_list|,
literal|0
argument_list|,
literal|8
argument_list|,
literal|5
argument_list|,
literal|34
argument_list|,
literal|9
argument_list|)
expr_stmt|;
name|testDecimalDivisionResultType
argument_list|(
literal|10
argument_list|,
literal|0
argument_list|,
literal|10
argument_list|,
literal|0
argument_list|,
literal|21
argument_list|,
literal|11
argument_list|)
expr_stmt|;
name|testDecimalDivisionResultType
argument_list|(
literal|5
argument_list|,
literal|2
argument_list|,
literal|5
argument_list|,
literal|5
argument_list|,
literal|16
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|testDecimalDivisionResultType
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|,
literal|5
argument_list|,
literal|0
argument_list|,
literal|16
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|testDecimalDivisionResultType
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|,
literal|5
argument_list|,
literal|5
argument_list|,
literal|21
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|testDecimalDivisionResultType
argument_list|(
literal|38
argument_list|,
literal|38
argument_list|,
literal|38
argument_list|,
literal|38
argument_list|,
literal|38
argument_list|,
literal|18
argument_list|)
expr_stmt|;
name|testDecimalDivisionResultType
argument_list|(
literal|38
argument_list|,
literal|0
argument_list|,
literal|38
argument_list|,
literal|0
argument_list|,
literal|38
argument_list|,
literal|18
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testDecimalDivisionResultType
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
parameter_list|,
name|int
name|prec3
parameter_list|,
name|int
name|scale3
parameter_list|)
throws|throws
name|HiveException
block|{
name|GenericUDFOPDivide
name|udf
init|=
operator|new
name|GenericUDFOPDivide
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
name|prec1
argument_list|,
name|scale1
argument_list|)
argument_list|)
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
name|prec2
argument_list|,
name|scale2
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|PrimitiveObjectInspector
name|oi
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
name|prec3
argument_list|,
name|scale3
argument_list|)
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReturnTypeBackwardCompat
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Disable ansi sql arithmetic changes
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPAT
argument_list|,
literal|"0.12"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"int"
argument_list|,
literal|"int"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
comment|// different from sql compat mode
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"int"
argument_list|,
literal|"float"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"int"
argument_list|,
literal|"double"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"int"
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"decimal(23,11)"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"float"
argument_list|,
literal|"float"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"float"
argument_list|,
literal|"double"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"float"
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"double"
argument_list|,
literal|"double"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"double"
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"decimal(23,13)"
argument_list|)
expr_stmt|;
comment|// Most tests are done with ANSI SQL mode enabled, set it back to true
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPAT
argument_list|,
literal|"latest"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReturnTypeAnsiSql
parameter_list|()
throws|throws
name|Exception
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPAT
argument_list|,
literal|"latest"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"int"
argument_list|,
literal|"int"
argument_list|,
literal|"decimal(21,11)"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"int"
argument_list|,
literal|"float"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"int"
argument_list|,
literal|"double"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"int"
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"decimal(23,11)"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"float"
argument_list|,
literal|"float"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"float"
argument_list|,
literal|"double"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"float"
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"double"
argument_list|,
literal|"double"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"double"
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|verifyReturnType
argument_list|(
operator|new
name|GenericUDFOPDivide
argument_list|()
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"decimal(10,2)"
argument_list|,
literal|"decimal(23,13)"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

