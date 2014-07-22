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
name|TestGenericUDFPosMod
extends|extends
name|TestGenericUDFOPNumeric
block|{
annotation|@
name|Test
specifier|public
name|void
name|testPosModByZero1
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFPosMod
name|udf
init|=
operator|new
name|GenericUDFPosMod
argument_list|()
decl_stmt|;
comment|// Byte
name|ByteWritable
name|b1
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
name|ByteWritable
name|b2
init|=
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|0
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
name|writableByteObjectInspector
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
name|b1
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|b2
argument_list|)
block|,     }
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|ByteWritable
name|b3
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
name|assertNull
argument_list|(
name|b3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPosModByZero2
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFPosMod
name|udf
init|=
operator|new
name|GenericUDFPosMod
argument_list|()
decl_stmt|;
comment|// Short
name|ShortWritable
name|s1
init|=
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|4
argument_list|)
decl_stmt|;
name|ShortWritable
name|s2
init|=
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0
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
name|s1
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|s2
argument_list|)
block|,     }
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|ShortWritable
name|s3
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
name|assertNull
argument_list|(
name|s3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPosModByZero3
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFPosMod
name|udf
init|=
operator|new
name|GenericUDFPosMod
argument_list|()
decl_stmt|;
comment|// Int
name|IntWritable
name|i1
init|=
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|IntWritable
name|i2
init|=
operator|new
name|IntWritable
argument_list|(
literal|0
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
name|i1
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|i2
argument_list|)
block|,     }
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|IntWritable
name|i3
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
name|assertNull
argument_list|(
name|i3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPosModByZero4
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFPosMod
name|udf
init|=
operator|new
name|GenericUDFPosMod
argument_list|()
decl_stmt|;
comment|// Long
name|LongWritable
name|l1
init|=
operator|new
name|LongWritable
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|LongWritable
name|l2
init|=
operator|new
name|LongWritable
argument_list|(
literal|0L
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
name|l1
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|l2
argument_list|)
block|,     }
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|LongWritable
name|l3
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
name|assertNull
argument_list|(
name|l3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPosModByZero5
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFPosMod
name|udf
init|=
operator|new
name|GenericUDFPosMod
argument_list|()
decl_stmt|;
comment|// Float
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
literal|0.0f
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
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|FloatWritable
name|f3
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
name|assertNull
argument_list|(
name|f3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPosModByZero6
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFPosMod
name|udf
init|=
operator|new
name|GenericUDFPosMod
argument_list|()
decl_stmt|;
comment|// Double
name|DoubleWritable
name|d1
init|=
operator|new
name|DoubleWritable
argument_list|(
literal|4.5
argument_list|)
decl_stmt|;
name|DoubleWritable
name|d2
init|=
operator|new
name|DoubleWritable
argument_list|(
literal|0.0
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
name|writableDoubleObjectInspector
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
name|d1
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|d2
argument_list|)
block|,     }
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|DoubleWritable
name|d3
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
name|assertNull
argument_list|(
name|d3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPosModByZero8
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFPosMod
name|udf
init|=
operator|new
name|GenericUDFPosMod
argument_list|()
decl_stmt|;
comment|// Decimal
name|HiveDecimalWritable
name|dec1
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"4.5"
argument_list|)
argument_list|)
decl_stmt|;
name|HiveDecimalWritable
name|dec2
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0"
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
literal|2
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
literal|1
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
name|dec1
argument_list|)
block|,
operator|new
name|DeferredJavaObject
argument_list|(
name|dec2
argument_list|)
block|,     }
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|HiveDecimalWritable
name|dec3
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
name|assertNull
argument_list|(
name|dec3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalPosModDecimal
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFPosMod
name|udf
init|=
operator|new
name|GenericUDFPosMod
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
literal|5
argument_list|,
literal|2
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
name|testDecimalPosModDecimalSameParams
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFPosMod
name|udf
init|=
operator|new
name|GenericUDFPosMod
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
literal|5
argument_list|,
literal|2
argument_list|)
argument_list|,
name|oi
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

