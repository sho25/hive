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
name|HiveChar
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
name|common
operator|.
name|type
operator|.
name|HiveVarchar
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
name|HiveCharWritable
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
name|CharTypeInfo
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
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|VarcharTypeInfo
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
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
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
name|TestGenericUDFOPPositive
block|{
specifier|private
specifier|static
specifier|final
name|double
name|EPSILON
init|=
literal|1E
operator|-
literal|6
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testByte
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|ByteWritable
name|input
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
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableByteObjectInspector
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
name|byteTypeInfo
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|ByteWritable
name|res
init|=
operator|(
name|ByteWritable
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
operator|(
name|byte
operator|)
literal|4
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShort
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|ShortWritable
name|input
init|=
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|74
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableShortObjectInspector
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
name|shortTypeInfo
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|ShortWritable
name|res
init|=
operator|(
name|ShortWritable
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
operator|(
name|short
operator|)
literal|74
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInt
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|IntWritable
name|input
init|=
operator|new
name|IntWritable
argument_list|(
literal|747
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
name|intTypeInfo
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|IntWritable
name|res
init|=
operator|(
name|IntWritable
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
literal|747
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLong
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|LongWritable
name|input
init|=
operator|new
name|LongWritable
argument_list|(
literal|3234747
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
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
name|longTypeInfo
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|LongWritable
name|res
init|=
operator|(
name|LongWritable
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
literal|3234747L
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFloat
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|FloatWritable
name|input
init|=
operator|new
name|FloatWritable
argument_list|(
literal|323.4747f
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
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
name|floatTypeInfo
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|FloatWritable
name|res
init|=
operator|(
name|FloatWritable
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
literal|323.4747
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|,
name|EPSILON
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDouble
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|DoubleWritable
name|input
init|=
operator|new
name|DoubleWritable
argument_list|(
literal|32300.004747
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
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
literal|32300.004747
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|,
name|EPSILON
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimal
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|HiveDecimalWritable
name|input
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"32300.004747"
argument_list|)
argument_list|)
decl_stmt|;
name|DecimalTypeInfo
name|inputTypeInfo
init|=
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
literal|11
argument_list|,
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
name|getPrimitiveWritableObjectInspector
argument_list|(
name|inputTypeInfo
argument_list|)
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
name|inputTypeInfo
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
literal|"32300.004747"
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
name|testString
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|Text
name|input
init|=
operator|new
name|Text
argument_list|(
literal|"32300.004747"
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
literal|32300.004747
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|,
name|EPSILON
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVarchar
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|HiveVarchar
name|vc
init|=
operator|new
name|HiveVarchar
argument_list|(
literal|"32300.004747"
argument_list|,
literal|12
argument_list|)
decl_stmt|;
name|HiveVarcharWritable
name|input
init|=
operator|new
name|HiveVarcharWritable
argument_list|(
name|vc
argument_list|)
decl_stmt|;
name|VarcharTypeInfo
name|inputTypeInfo
init|=
name|TypeInfoFactory
operator|.
name|getVarcharTypeInfo
argument_list|(
literal|12
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
name|inputTypeInfo
argument_list|)
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
literal|32300.004747
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|,
name|EPSILON
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testChar
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFOPPositive
name|udf
init|=
operator|new
name|GenericUDFOPPositive
argument_list|()
decl_stmt|;
name|HiveChar
name|vc
init|=
operator|new
name|HiveChar
argument_list|(
literal|"32300.004747"
argument_list|,
literal|12
argument_list|)
decl_stmt|;
name|HiveCharWritable
name|input
init|=
operator|new
name|HiveCharWritable
argument_list|(
name|vc
argument_list|)
decl_stmt|;
name|CharTypeInfo
name|inputTypeInfo
init|=
name|TypeInfoFactory
operator|.
name|getCharTypeInfo
argument_list|(
literal|12
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
name|inputTypeInfo
argument_list|)
block|,     }
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
name|input
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
literal|32300.004747
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|,
name|EPSILON
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

