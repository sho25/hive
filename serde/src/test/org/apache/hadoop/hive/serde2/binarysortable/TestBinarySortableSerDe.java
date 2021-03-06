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
name|binarysortable
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
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|AbstractSerDe
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
name|binarysortable
operator|.
name|MyTestPrimitiveClass
operator|.
name|ExtraTypeInfo
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
name|BytesWritable
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
comment|/**  * TestBinarySortableSerDe.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestBinarySortableSerDe
block|{
specifier|private
specifier|static
specifier|final
name|String
name|DECIMAL_CHARS
init|=
literal|"0123456789"
decl_stmt|;
specifier|public
specifier|static
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|makeHashMap
parameter_list|(
name|String
modifier|...
name|params
parameter_list|)
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|r
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
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
name|params
operator|.
name|length
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|r
operator|.
name|put
argument_list|(
name|params
index|[
name|i
index|]
argument_list|,
name|params
index|[
name|i
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|r
return|;
block|}
specifier|public
specifier|static
name|String
name|hexString
parameter_list|(
name|BytesWritable
name|bytes
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|bytes
operator|.
name|getSize
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|b
init|=
name|bytes
operator|.
name|get
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|int
name|v
init|=
operator|(
name|b
operator|<
literal|0
condition|?
literal|256
operator|+
name|b
else|:
name|b
operator|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"x%02x"
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|AbstractSerDe
name|getSerDe
parameter_list|(
name|String
name|fieldNames
parameter_list|,
name|String
name|fieldTypes
parameter_list|,
name|String
name|order
parameter_list|,
name|String
name|nullOrder
parameter_list|)
throws|throws
name|Throwable
block|{
name|Properties
name|schema
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|fieldNames
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|fieldTypes
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_SORT_ORDER
argument_list|,
name|order
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_SORT_ORDER
argument_list|,
name|nullOrder
argument_list|)
expr_stmt|;
name|BinarySortableSerDe
name|serde
init|=
operator|new
name|BinarySortableSerDe
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
name|schema
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|serde
return|;
block|}
specifier|private
name|void
name|testBinarySortableSerDe
parameter_list|(
name|Object
index|[]
name|rows
parameter_list|,
name|ObjectInspector
name|rowOI
parameter_list|,
name|AbstractSerDe
name|serde
parameter_list|,
name|boolean
name|ascending
parameter_list|)
throws|throws
name|Throwable
block|{
name|ObjectInspector
name|serdeOI
init|=
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
comment|// Try to serialize
name|BytesWritable
name|bytes
index|[]
init|=
operator|new
name|BytesWritable
index|[
name|rows
operator|.
name|length
index|]
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
name|rows
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BytesWritable
name|s
init|=
operator|(
name|BytesWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|,
name|rowOI
argument_list|)
decl_stmt|;
name|bytes
index|[
name|i
index|]
operator|=
operator|new
name|BytesWritable
argument_list|()
expr_stmt|;
name|bytes
index|[
name|i
index|]
operator|.
name|set
argument_list|(
name|s
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|int
name|compareResult
init|=
name|bytes
index|[
name|i
operator|-
literal|1
index|]
operator|.
name|compareTo
argument_list|(
name|bytes
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|compareResult
operator|<
literal|0
operator|&&
operator|!
name|ascending
operator|)
operator|||
operator|(
name|compareResult
operator|>
literal|0
operator|&&
name|ascending
operator|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Test failed in "
operator|+
operator|(
name|ascending
condition|?
literal|"ascending"
else|:
literal|"descending"
operator|)
operator|+
literal|" order with "
operator|+
operator|(
name|i
operator|-
literal|1
operator|)
operator|+
literal|" and "
operator|+
name|i
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"serialized data ["
operator|+
operator|(
name|i
operator|-
literal|1
operator|)
operator|+
literal|"] = "
operator|+
name|hexString
argument_list|(
name|bytes
index|[
name|i
operator|-
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"serialized data ["
operator|+
name|i
operator|+
literal|"] = "
operator|+
name|hexString
argument_list|(
name|bytes
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"deserialized data ["
operator|+
operator|(
name|i
operator|-
literal|1
operator|)
operator|+
literal|" = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|rows
index|[
name|i
operator|-
literal|1
index|]
argument_list|,
name|rowOI
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"deserialized data ["
operator|+
name|i
operator|+
literal|" = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|,
name|rowOI
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Sort order of serialized "
operator|+
operator|(
name|i
operator|-
literal|1
operator|)
operator|+
literal|" and "
operator|+
name|i
operator|+
literal|" are reversed!"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Try to deserialize
name|Object
index|[]
name|deserialized
init|=
operator|new
name|Object
index|[
name|rows
operator|.
name|length
index|]
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
name|rows
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|deserialized
index|[
name|i
index|]
operator|=
name|serde
operator|.
name|deserialize
argument_list|(
name|bytes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
literal|0
operator|!=
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|,
name|rowOI
argument_list|,
name|deserialized
index|[
name|i
index|]
argument_list|,
name|serdeOI
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"structs["
operator|+
name|i
operator|+
literal|"] = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|,
name|rowOI
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"deserialized["
operator|+
name|i
operator|+
literal|"] = "
operator|+
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|deserialized
index|[
name|i
index|]
argument_list|,
name|serdeOI
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"serialized["
operator|+
name|i
operator|+
literal|"] = "
operator|+
name|hexString
argument_list|(
name|bytes
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|,
name|deserialized
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|sort
parameter_list|(
name|Object
index|[]
name|structs
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|structs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
name|i
operator|+
literal|1
init|;
name|j
operator|<
name|structs
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|structs
index|[
name|i
index|]
argument_list|,
name|oi
argument_list|,
name|structs
index|[
name|j
index|]
argument_list|,
name|oi
argument_list|)
operator|>
literal|0
condition|)
block|{
name|Object
name|t
init|=
name|structs
index|[
name|i
index|]
decl_stmt|;
name|structs
index|[
name|i
index|]
operator|=
name|structs
index|[
name|j
index|]
expr_stmt|;
name|structs
index|[
name|j
index|]
operator|=
name|t
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBinarySortableSerDe
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Beginning Test testBinarySortableSerDe:"
argument_list|)
expr_stmt|;
name|int
name|num
init|=
literal|1000
decl_stmt|;
name|Random
name|r
init|=
operator|new
name|Random
argument_list|(
literal|1234
argument_list|)
decl_stmt|;
name|MyTestClass
name|rows
index|[]
init|=
operator|new
name|MyTestClass
index|[
name|num
index|]
decl_stmt|;
name|int
name|i
decl_stmt|;
comment|// First try non-random values
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|MyTestClass
operator|.
name|nrDecimal
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MyTestClass
name|t
init|=
operator|new
name|MyTestClass
argument_list|()
decl_stmt|;
name|t
operator|.
name|nonRandomFill
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|rows
index|[
name|i
index|]
operator|=
name|t
expr_stmt|;
block|}
for|for
control|(
init|;
name|i
operator|<
name|num
condition|;
name|i
operator|++
control|)
block|{
name|MyTestClass
name|t
init|=
operator|new
name|MyTestClass
argument_list|()
decl_stmt|;
name|ExtraTypeInfo
name|extraTypeInfo
init|=
operator|new
name|ExtraTypeInfo
argument_list|()
decl_stmt|;
name|t
operator|.
name|randomFill
argument_list|(
name|r
argument_list|,
name|extraTypeInfo
argument_list|)
expr_stmt|;
name|rows
index|[
name|i
index|]
operator|=
name|t
expr_stmt|;
block|}
name|StructObjectInspector
name|rowOI
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|MyTestClass
operator|.
name|class
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|sort
argument_list|(
name|rows
argument_list|,
name|rowOI
argument_list|)
expr_stmt|;
name|String
name|fieldNames
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|rowOI
argument_list|)
decl_stmt|;
name|String
name|fieldTypes
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|rowOI
argument_list|)
decl_stmt|;
name|String
name|order
decl_stmt|;
name|order
operator|=
name|StringUtils
operator|.
name|leftPad
argument_list|(
literal|""
argument_list|,
name|MyTestClass
operator|.
name|fieldCount
argument_list|,
literal|'+'
argument_list|)
expr_stmt|;
name|String
name|nullOrder
decl_stmt|;
name|nullOrder
operator|=
name|StringUtils
operator|.
name|leftPad
argument_list|(
literal|""
argument_list|,
name|MyTestClass
operator|.
name|fieldCount
argument_list|,
literal|'a'
argument_list|)
expr_stmt|;
name|testBinarySortableSerDe
argument_list|(
name|rows
argument_list|,
name|rowOI
argument_list|,
name|getSerDe
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypes
argument_list|,
name|order
argument_list|,
name|nullOrder
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|order
operator|=
name|StringUtils
operator|.
name|leftPad
argument_list|(
literal|""
argument_list|,
name|MyTestClass
operator|.
name|fieldCount
argument_list|,
literal|'-'
argument_list|)
expr_stmt|;
name|nullOrder
operator|=
name|StringUtils
operator|.
name|leftPad
argument_list|(
literal|""
argument_list|,
name|MyTestClass
operator|.
name|fieldCount
argument_list|,
literal|'z'
argument_list|)
expr_stmt|;
name|testBinarySortableSerDe
argument_list|(
name|rows
argument_list|,
name|rowOI
argument_list|,
name|getSerDe
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypes
argument_list|,
name|order
argument_list|,
name|nullOrder
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Test testTBinarySortableProtocol passed!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit

