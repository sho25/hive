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
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Arrays
operator|.
name|asList
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
name|Date
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
name|io
operator|.
name|DateWritableV2
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
name|ObjectInspectorFactory
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
name|TestGenericUDFSortArray
block|{
specifier|private
specifier|final
name|GenericUDFSortArray
name|udf
init|=
operator|new
name|GenericUDFSortArray
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testSortPrimitive
parameter_list|()
throws|throws
name|HiveException
block|{
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|)
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|Object
name|i1
init|=
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|Object
name|i2
init|=
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|Object
name|i3
init|=
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Object
name|i4
init|=
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|runAndVerify
argument_list|(
name|asList
argument_list|(
name|i1
argument_list|,
name|i2
argument_list|,
name|i3
argument_list|,
name|i4
argument_list|)
argument_list|,
name|asList
argument_list|(
name|i4
argument_list|,
name|i3
argument_list|,
name|i1
argument_list|,
name|i2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSortList
parameter_list|()
throws|throws
name|HiveException
block|{
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|Object
name|i1
init|=
name|asList
argument_list|(
operator|new
name|Text
argument_list|(
literal|"aa"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"dd"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"cc"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"bb"
argument_list|)
argument_list|)
decl_stmt|;
name|Object
name|i2
init|=
name|asList
argument_list|(
operator|new
name|Text
argument_list|(
literal|"aa"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"cc"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"ba"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"dd"
argument_list|)
argument_list|)
decl_stmt|;
name|Object
name|i3
init|=
name|asList
argument_list|(
operator|new
name|Text
argument_list|(
literal|"aa"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"cc"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"dd"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"ee"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"bb"
argument_list|)
argument_list|)
decl_stmt|;
name|Object
name|i4
init|=
name|asList
argument_list|(
operator|new
name|Text
argument_list|(
literal|"aa"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"cc"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"ddd"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"bb"
argument_list|)
argument_list|)
decl_stmt|;
name|runAndVerify
argument_list|(
name|asList
argument_list|(
name|i1
argument_list|,
name|i2
argument_list|,
name|i3
argument_list|,
name|i4
argument_list|)
argument_list|,
name|asList
argument_list|(
name|i2
argument_list|,
name|i3
argument_list|,
name|i4
argument_list|,
name|i1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSortStruct
parameter_list|()
throws|throws
name|HiveException
block|{
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|asList
argument_list|(
literal|"f1"
argument_list|,
literal|"f2"
argument_list|,
literal|"f3"
argument_list|,
literal|"f4"
argument_list|)
argument_list|,
name|asList
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDateObjectInspector
argument_list|,
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|)
argument_list|)
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|Object
name|i1
init|=
name|asList
argument_list|(
operator|new
name|Text
argument_list|(
literal|"a"
argument_list|)
argument_list|,
operator|new
name|DoubleWritable
argument_list|(
literal|3.1415
argument_list|)
argument_list|,
operator|new
name|DateWritableV2
argument_list|(
name|Date
operator|.
name|of
argument_list|(
literal|2015
argument_list|,
literal|5
argument_list|,
literal|26
argument_list|)
argument_list|)
argument_list|,
name|asList
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Object
name|i2
init|=
name|asList
argument_list|(
operator|new
name|Text
argument_list|(
literal|"b"
argument_list|)
argument_list|,
operator|new
name|DoubleWritable
argument_list|(
literal|3.14
argument_list|)
argument_list|,
operator|new
name|DateWritableV2
argument_list|(
name|Date
operator|.
name|of
argument_list|(
literal|2015
argument_list|,
literal|5
argument_list|,
literal|26
argument_list|)
argument_list|)
argument_list|,
name|asList
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Object
name|i3
init|=
name|asList
argument_list|(
operator|new
name|Text
argument_list|(
literal|"a"
argument_list|)
argument_list|,
operator|new
name|DoubleWritable
argument_list|(
literal|3.1415
argument_list|)
argument_list|,
operator|new
name|DateWritableV2
argument_list|(
name|Date
operator|.
name|of
argument_list|(
literal|2015
argument_list|,
literal|5
argument_list|,
literal|25
argument_list|)
argument_list|)
argument_list|,
name|asList
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Object
name|i4
init|=
name|asList
argument_list|(
operator|new
name|Text
argument_list|(
literal|"a"
argument_list|)
argument_list|,
operator|new
name|DoubleWritable
argument_list|(
literal|3.1415
argument_list|)
argument_list|,
operator|new
name|DateWritableV2
argument_list|(
name|Date
operator|.
name|of
argument_list|(
literal|2015
argument_list|,
literal|5
argument_list|,
literal|25
argument_list|)
argument_list|)
argument_list|,
name|asList
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|runAndVerify
argument_list|(
name|asList
argument_list|(
name|i1
argument_list|,
name|i2
argument_list|,
name|i3
argument_list|,
name|i4
argument_list|)
argument_list|,
name|asList
argument_list|(
name|i4
argument_list|,
name|i3
argument_list|,
name|i1
argument_list|,
name|i2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSortMap
parameter_list|()
throws|throws
name|HiveException
block|{
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardMapObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|)
argument_list|)
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|inputOIs
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Text
argument_list|,
name|IntWritable
argument_list|>
name|m1
init|=
operator|new
name|HashMap
argument_list|<
name|Text
argument_list|,
name|IntWritable
argument_list|>
argument_list|()
decl_stmt|;
name|m1
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"a"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|m1
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"b"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|m1
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"c"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|m1
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"d"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Text
argument_list|,
name|IntWritable
argument_list|>
name|m2
init|=
operator|new
name|HashMap
argument_list|<
name|Text
argument_list|,
name|IntWritable
argument_list|>
argument_list|()
decl_stmt|;
name|m2
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"d"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|m2
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"b"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|m2
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"a"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|m2
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"c"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Text
argument_list|,
name|IntWritable
argument_list|>
name|m3
init|=
operator|new
name|HashMap
argument_list|<
name|Text
argument_list|,
name|IntWritable
argument_list|>
argument_list|()
decl_stmt|;
name|m3
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"d"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|m3
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"b"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|m3
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"a"
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
name|asList
argument_list|(
operator|(
name|Object
operator|)
name|m1
argument_list|,
name|m2
argument_list|,
name|m3
argument_list|)
argument_list|,
name|asList
argument_list|(
operator|(
name|Object
operator|)
name|m3
argument_list|,
name|m2
argument_list|,
name|m1
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runAndVerify
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|actual
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|expected
parameter_list|)
throws|throws
name|HiveException
block|{
name|GenericUDF
operator|.
name|DeferredJavaObject
index|[]
name|args
init|=
block|{
operator|new
name|GenericUDF
operator|.
name|DeferredJavaObject
argument_list|(
name|actual
argument_list|)
block|}
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|result
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
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
literal|"Check size"
argument_list|,
name|expected
operator|.
name|size
argument_list|()
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
literal|"Check content"
argument_list|,
name|expected
operator|.
name|toArray
argument_list|()
argument_list|,
name|result
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

