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
name|serde2
operator|.
name|columnar
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
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|conf
operator|.
name|Configuration
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
name|SerDeException
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
name|SerDeUtils
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
name|CrossMapEqualComparer
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
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
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
name|serde2
operator|.
name|objectinspector
operator|.
name|SimpleMapEqualComparer
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
name|StructObjectInspector
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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

begin_comment
comment|/**  * LazyBinaryColumnarSerDe Test.  */
end_comment

begin_class
specifier|public
class|class
name|TestLazyBinaryColumnarSerDe
block|{
specifier|private
specifier|static
class|class
name|InnerStruct
block|{
specifier|public
name|InnerStruct
parameter_list|(
name|Integer
name|i
parameter_list|,
name|Long
name|l
parameter_list|)
block|{
name|mInt
operator|=
name|i
expr_stmt|;
name|mLong
operator|=
name|l
expr_stmt|;
block|}
name|Integer
name|mInt
decl_stmt|;
name|Long
name|mLong
decl_stmt|;
block|}
specifier|private
specifier|static
class|class
name|OuterStruct
block|{
name|Byte
name|mByte
decl_stmt|;
name|Short
name|mShort
decl_stmt|;
name|Integer
name|mInt
decl_stmt|;
name|Long
name|mLong
decl_stmt|;
name|Float
name|mFloat
decl_stmt|;
name|Double
name|mDouble
decl_stmt|;
name|String
name|mString
decl_stmt|;
name|byte
index|[]
name|mBA
decl_stmt|;
name|List
argument_list|<
name|InnerStruct
argument_list|>
name|mArray
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|InnerStruct
argument_list|>
name|mMap
decl_stmt|;
name|InnerStruct
name|mStruct
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|OuterStruct
operator|.
name|class
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|String
name|cols
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|oi
argument_list|)
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|cols
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|LazyBinaryColumnarSerDe
name|serde
init|=
operator|new
name|LazyBinaryColumnarSerDe
argument_list|()
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|OuterStruct
name|outerStruct
init|=
operator|new
name|OuterStruct
argument_list|()
decl_stmt|;
name|outerStruct
operator|.
name|mByte
operator|=
literal|1
expr_stmt|;
name|outerStruct
operator|.
name|mShort
operator|=
literal|2
expr_stmt|;
name|outerStruct
operator|.
name|mInt
operator|=
literal|3
expr_stmt|;
name|outerStruct
operator|.
name|mLong
operator|=
literal|4l
expr_stmt|;
name|outerStruct
operator|.
name|mFloat
operator|=
literal|5.01f
expr_stmt|;
name|outerStruct
operator|.
name|mDouble
operator|=
literal|6.001d
expr_stmt|;
name|outerStruct
operator|.
name|mString
operator|=
literal|"seven"
expr_stmt|;
name|outerStruct
operator|.
name|mBA
operator|=
operator|new
name|byte
index|[]
block|{
literal|'2'
block|}
expr_stmt|;
name|InnerStruct
name|is1
init|=
operator|new
name|InnerStruct
argument_list|(
literal|8
argument_list|,
literal|9l
argument_list|)
decl_stmt|;
name|InnerStruct
name|is2
init|=
operator|new
name|InnerStruct
argument_list|(
literal|10
argument_list|,
literal|11l
argument_list|)
decl_stmt|;
name|outerStruct
operator|.
name|mArray
operator|=
operator|new
name|ArrayList
argument_list|<
name|InnerStruct
argument_list|>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mArray
operator|.
name|add
argument_list|(
name|is1
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mArray
operator|.
name|add
argument_list|(
name|is2
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|InnerStruct
argument_list|>
argument_list|()
expr_stmt|;
name|outerStruct
operator|.
name|mMap
operator|.
name|put
argument_list|(
operator|new
name|String
argument_list|(
literal|"twelve"
argument_list|)
argument_list|,
operator|new
name|InnerStruct
argument_list|(
literal|13
argument_list|,
literal|14l
argument_list|)
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mMap
operator|.
name|put
argument_list|(
operator|new
name|String
argument_list|(
literal|"fifteen"
argument_list|)
argument_list|,
operator|new
name|InnerStruct
argument_list|(
literal|16
argument_list|,
literal|17l
argument_list|)
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mStruct
operator|=
operator|new
name|InnerStruct
argument_list|(
literal|18
argument_list|,
literal|19l
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|braw
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|ObjectInspector
name|out_oi
init|=
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|out_o
init|=
name|serde
operator|.
name|deserialize
argument_list|(
name|braw
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|,
name|out_o
argument_list|,
name|out_oi
argument_list|,
operator|new
name|CrossMapEqualComparer
argument_list|()
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"expected = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"actual = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|out_o
argument_list|,
name|out_oi
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Deserialized object does not compare"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerDeEmpties
parameter_list|()
throws|throws
name|SerDeException
block|{
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|OuterStruct
operator|.
name|class
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|String
name|cols
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|oi
argument_list|)
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|cols
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|LazyBinaryColumnarSerDe
name|serde
init|=
operator|new
name|LazyBinaryColumnarSerDe
argument_list|()
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|OuterStruct
name|outerStruct
init|=
operator|new
name|OuterStruct
argument_list|()
decl_stmt|;
name|outerStruct
operator|.
name|mByte
operator|=
literal|101
expr_stmt|;
name|outerStruct
operator|.
name|mShort
operator|=
literal|2002
expr_stmt|;
name|outerStruct
operator|.
name|mInt
operator|=
literal|3003
expr_stmt|;
name|outerStruct
operator|.
name|mLong
operator|=
literal|4004l
expr_stmt|;
name|outerStruct
operator|.
name|mFloat
operator|=
literal|5005.01f
expr_stmt|;
name|outerStruct
operator|.
name|mDouble
operator|=
literal|6006.001d
expr_stmt|;
name|outerStruct
operator|.
name|mString
operator|=
literal|""
expr_stmt|;
name|outerStruct
operator|.
name|mBA
operator|=
operator|new
name|byte
index|[]
block|{
literal|'a'
block|}
expr_stmt|;
name|outerStruct
operator|.
name|mArray
operator|=
operator|new
name|ArrayList
argument_list|<
name|InnerStruct
argument_list|>
argument_list|()
expr_stmt|;
name|outerStruct
operator|.
name|mMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|InnerStruct
argument_list|>
argument_list|()
expr_stmt|;
name|outerStruct
operator|.
name|mStruct
operator|=
operator|new
name|InnerStruct
argument_list|(
literal|180018
argument_list|,
literal|190019l
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|braw
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|ObjectInspector
name|out_oi
init|=
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|out_o
init|=
name|serde
operator|.
name|deserialize
argument_list|(
name|braw
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|,
name|out_o
argument_list|,
name|out_oi
argument_list|,
operator|new
name|SimpleMapEqualComparer
argument_list|()
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"expected = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"actual = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|out_o
argument_list|,
name|out_oi
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Deserialized object does not compare"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLazyBinaryColumnarSerDeWithEmptyBinary
parameter_list|()
throws|throws
name|SerDeException
block|{
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|OuterStruct
operator|.
name|class
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|String
name|cols
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|oi
argument_list|)
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|cols
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|LazyBinaryColumnarSerDe
name|serde
init|=
operator|new
name|LazyBinaryColumnarSerDe
argument_list|()
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|OuterStruct
name|outerStruct
init|=
operator|new
name|OuterStruct
argument_list|()
decl_stmt|;
name|outerStruct
operator|.
name|mByte
operator|=
literal|101
expr_stmt|;
name|outerStruct
operator|.
name|mShort
operator|=
literal|2002
expr_stmt|;
name|outerStruct
operator|.
name|mInt
operator|=
literal|3003
expr_stmt|;
name|outerStruct
operator|.
name|mLong
operator|=
literal|4004l
expr_stmt|;
name|outerStruct
operator|.
name|mFloat
operator|=
literal|5005.01f
expr_stmt|;
name|outerStruct
operator|.
name|mDouble
operator|=
literal|6006.001d
expr_stmt|;
name|outerStruct
operator|.
name|mString
operator|=
literal|""
expr_stmt|;
name|outerStruct
operator|.
name|mBA
operator|=
operator|new
name|byte
index|[]
block|{}
expr_stmt|;
name|outerStruct
operator|.
name|mArray
operator|=
operator|new
name|ArrayList
argument_list|<
name|InnerStruct
argument_list|>
argument_list|()
expr_stmt|;
name|outerStruct
operator|.
name|mMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|InnerStruct
argument_list|>
argument_list|()
expr_stmt|;
name|outerStruct
operator|.
name|mStruct
operator|=
operator|new
name|InnerStruct
argument_list|(
literal|180018
argument_list|,
literal|190019l
argument_list|)
expr_stmt|;
try|try
block|{
name|serde
operator|.
name|serialize
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|re
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|re
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"LazyBinaryColumnarSerde cannot serialize a non-null "
operator|+
literal|"zero length binary field. Consider using either LazyBinarySerde or ColumnarSerde."
argument_list|)
expr_stmt|;
return|return;
block|}
assert|assert
literal|false
assert|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerDeOuterNulls
parameter_list|()
throws|throws
name|SerDeException
block|{
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|OuterStruct
operator|.
name|class
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|String
name|cols
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|oi
argument_list|)
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|cols
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|LazyBinaryColumnarSerDe
name|serde
init|=
operator|new
name|LazyBinaryColumnarSerDe
argument_list|()
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|OuterStruct
name|outerStruct
init|=
operator|new
name|OuterStruct
argument_list|()
decl_stmt|;
name|BytesRefArrayWritable
name|braw
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|ObjectInspector
name|out_oi
init|=
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|out_o
init|=
name|serde
operator|.
name|deserialize
argument_list|(
name|braw
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|,
name|out_o
argument_list|,
name|out_oi
argument_list|,
operator|new
name|SimpleMapEqualComparer
argument_list|()
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"expected = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"actual = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|out_o
argument_list|,
name|out_oi
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Deserialized object does not compare"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerDeInnerNulls
parameter_list|()
throws|throws
name|SerDeException
block|{
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|OuterStruct
operator|.
name|class
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|String
name|cols
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|oi
argument_list|)
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|cols
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|LazyBinaryColumnarSerDe
name|serde
init|=
operator|new
name|LazyBinaryColumnarSerDe
argument_list|()
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|OuterStruct
name|outerStruct
init|=
operator|new
name|OuterStruct
argument_list|()
decl_stmt|;
name|outerStruct
operator|.
name|mByte
operator|=
literal|1
expr_stmt|;
name|outerStruct
operator|.
name|mShort
operator|=
literal|2
expr_stmt|;
name|outerStruct
operator|.
name|mInt
operator|=
literal|3
expr_stmt|;
name|outerStruct
operator|.
name|mLong
operator|=
literal|4l
expr_stmt|;
name|outerStruct
operator|.
name|mFloat
operator|=
literal|5.01f
expr_stmt|;
name|outerStruct
operator|.
name|mDouble
operator|=
literal|6.001d
expr_stmt|;
name|outerStruct
operator|.
name|mString
operator|=
literal|"seven"
expr_stmt|;
name|outerStruct
operator|.
name|mBA
operator|=
operator|new
name|byte
index|[]
block|{
literal|'3'
block|}
expr_stmt|;
name|InnerStruct
name|is1
init|=
operator|new
name|InnerStruct
argument_list|(
literal|null
argument_list|,
literal|9l
argument_list|)
decl_stmt|;
name|InnerStruct
name|is2
init|=
operator|new
name|InnerStruct
argument_list|(
literal|10
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|outerStruct
operator|.
name|mArray
operator|=
operator|new
name|ArrayList
argument_list|<
name|InnerStruct
argument_list|>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mArray
operator|.
name|add
argument_list|(
name|is1
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mArray
operator|.
name|add
argument_list|(
name|is2
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|InnerStruct
argument_list|>
argument_list|()
expr_stmt|;
name|outerStruct
operator|.
name|mMap
operator|.
name|put
argument_list|(
literal|null
argument_list|,
operator|new
name|InnerStruct
argument_list|(
literal|13
argument_list|,
literal|14l
argument_list|)
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mMap
operator|.
name|put
argument_list|(
operator|new
name|String
argument_list|(
literal|"fifteen"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|outerStruct
operator|.
name|mStruct
operator|=
operator|new
name|InnerStruct
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|braw
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|ObjectInspector
name|out_oi
init|=
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|out_o
init|=
name|serde
operator|.
name|deserialize
argument_list|(
name|braw
argument_list|)
decl_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|,
name|out_o
argument_list|,
name|out_oi
argument_list|,
operator|new
name|SimpleMapEqualComparer
argument_list|()
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"expected = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|outerStruct
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"actual = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|out_o
argument_list|,
name|out_oi
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Deserialized object does not compare"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|BeforeStruct
block|{
name|Long
name|l1
decl_stmt|;
name|Long
name|l2
decl_stmt|;
block|}
specifier|private
specifier|static
class|class
name|AfterStruct
block|{
name|Long
name|l1
decl_stmt|;
name|Long
name|l2
decl_stmt|;
name|Long
name|l3
decl_stmt|;
block|}
comment|/**    * HIVE-5788    *<p>    * Background: in cases of "add column", table metadata changes but data does not.  Columns    * missing from the data but which are required by metadata are interpreted as null.    *<p>    * This tests the use-case of altering columns of a table with already some data, then adding more data    * in the new schema, and seeing if this serde can to read both types of data from the resultant table.    * @throws SerDeException    */
annotation|@
name|Test
specifier|public
name|void
name|testHandlingAlteredSchemas
parameter_list|()
throws|throws
name|SerDeException
block|{
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|BeforeStruct
operator|.
name|class
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|String
name|cols
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|oi
argument_list|)
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|cols
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|oi
argument_list|)
argument_list|)
expr_stmt|;
comment|// serialize some data in the schema before it is altered.
name|LazyBinaryColumnarSerDe
name|serde
init|=
operator|new
name|LazyBinaryColumnarSerDe
argument_list|()
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|BeforeStruct
name|bs1
init|=
operator|new
name|BeforeStruct
argument_list|()
decl_stmt|;
name|bs1
operator|.
name|l1
operator|=
literal|1L
expr_stmt|;
name|bs1
operator|.
name|l2
operator|=
literal|2L
expr_stmt|;
name|BytesRefArrayWritable
name|braw1
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|bs1
argument_list|,
name|oi
argument_list|)
decl_stmt|;
comment|// alter table add column: change the metadata
name|oi
operator|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|AfterStruct
operator|.
name|class
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
expr_stmt|;
name|cols
operator|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|props
operator|=
operator|new
name|Properties
argument_list|()
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|cols
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|serde
operator|=
operator|new
name|LazyBinaryColumnarSerDe
argument_list|()
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// serialize some data in the schema after it is altered.
name|AfterStruct
name|as
init|=
operator|new
name|AfterStruct
argument_list|()
decl_stmt|;
name|as
operator|.
name|l1
operator|=
literal|11L
expr_stmt|;
name|as
operator|.
name|l2
operator|=
literal|12L
expr_stmt|;
name|as
operator|.
name|l3
operator|=
literal|13L
expr_stmt|;
name|BytesRefArrayWritable
name|braw2
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|as
argument_list|,
name|oi
argument_list|)
decl_stmt|;
comment|// fetch operator
name|serde
operator|=
operator|new
name|LazyBinaryColumnarSerDe
argument_list|()
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|//fetch the row inserted before schema is altered and verify
name|LazyBinaryColumnarStruct
name|struct1
init|=
operator|(
name|LazyBinaryColumnarStruct
operator|)
name|serde
operator|.
name|deserialize
argument_list|(
name|braw1
argument_list|)
decl_stmt|;
name|oi
operator|=
operator|(
name|StructObjectInspector
operator|)
name|serde
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|objs1
init|=
name|oi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|struct1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
operator|(
name|LongWritable
operator|)
name|objs1
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
operator|(
name|LongWritable
operator|)
name|objs1
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|,
literal|2L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|objs1
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|//fetch the row inserted after schema is altered and verify
name|LazyBinaryColumnarStruct
name|struct2
init|=
operator|(
name|LazyBinaryColumnarStruct
operator|)
name|serde
operator|.
name|deserialize
argument_list|(
name|braw2
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|objs2
init|=
name|struct2
operator|.
name|getFieldsAsList
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
operator|(
name|LongWritable
operator|)
name|objs2
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|,
literal|11L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
operator|(
name|LongWritable
operator|)
name|objs2
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|,
literal|12L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
operator|(
name|LongWritable
operator|)
name|objs2
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|,
literal|13L
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

