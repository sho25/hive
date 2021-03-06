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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|BooleanWritable
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
name|Writable
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
name|TestGenericUDFOPAnd
block|{
annotation|@
name|Test
specifier|public
name|void
name|testTrueAndTrue
parameter_list|()
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|GenericUDFOPAnd
name|udf
init|=
operator|new
name|GenericUDFOPAnd
argument_list|()
decl_stmt|;
name|BooleanWritable
name|left
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|BooleanWritable
name|right
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
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
name|booleanTypeInfo
argument_list|)
expr_stmt|;
name|BooleanWritable
name|res
init|=
operator|(
name|BooleanWritable
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
literal|true
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|udf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrueAndFalse
parameter_list|()
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|GenericUDFOPAnd
name|udf
init|=
operator|new
name|GenericUDFOPAnd
argument_list|()
decl_stmt|;
name|BooleanWritable
name|left
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|BooleanWritable
name|right
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
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
name|booleanTypeInfo
argument_list|)
expr_stmt|;
name|BooleanWritable
name|res
init|=
operator|(
name|BooleanWritable
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
literal|false
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|udf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFalseAndFalse
parameter_list|()
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|GenericUDFOPAnd
name|udf
init|=
operator|new
name|GenericUDFOPAnd
argument_list|()
decl_stmt|;
name|BooleanWritable
name|left
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|BooleanWritable
name|right
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
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
name|booleanTypeInfo
argument_list|)
expr_stmt|;
name|BooleanWritable
name|res
init|=
operator|(
name|BooleanWritable
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
literal|false
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|udf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrueAndNull
parameter_list|()
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|GenericUDFOPAnd
name|udf
init|=
operator|new
name|GenericUDFOPAnd
argument_list|()
decl_stmt|;
name|BooleanWritable
name|left
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|Writable
name|right
init|=
literal|null
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableVoidObjectInspector
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
name|booleanTypeInfo
argument_list|)
expr_stmt|;
name|BooleanWritable
name|res
init|=
operator|(
name|BooleanWritable
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
literal|null
argument_list|,
name|res
argument_list|)
expr_stmt|;
name|udf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFalseAndNull
parameter_list|()
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|GenericUDFOPAnd
name|udf
init|=
operator|new
name|GenericUDFOPAnd
argument_list|()
decl_stmt|;
name|BooleanWritable
name|left
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Writable
name|right
init|=
literal|null
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableVoidObjectInspector
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
name|booleanTypeInfo
argument_list|)
expr_stmt|;
name|BooleanWritable
name|res
init|=
operator|(
name|BooleanWritable
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
literal|false
argument_list|,
name|res
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|udf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNullAndNull
parameter_list|()
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|GenericUDFOPAnd
name|udf
init|=
operator|new
name|GenericUDFOPAnd
argument_list|()
decl_stmt|;
name|Writable
name|left
init|=
literal|null
decl_stmt|;
name|Writable
name|right
init|=
literal|null
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableVoidObjectInspector
block|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableVoidObjectInspector
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
name|booleanTypeInfo
argument_list|)
expr_stmt|;
name|BooleanWritable
name|res
init|=
operator|(
name|BooleanWritable
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
literal|null
argument_list|,
name|res
argument_list|)
expr_stmt|;
name|udf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

