begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|avro
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
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
name|StandardStructObjectInspector
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
name|StructField
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
name|ListTypeInfo
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
name|MapTypeInfo
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
name|StructTypeInfo
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
name|TypeInfo
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
name|UnionTypeInfo
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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

begin_class
specifier|public
class|class
name|TestAvroObjectInspectorGenerator
block|{
specifier|private
specifier|final
name|TypeInfo
name|STRING
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"string"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TypeInfo
name|INT
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"int"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TypeInfo
name|BOOLEAN
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"boolean"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TypeInfo
name|LONG
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"bigint"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TypeInfo
name|FLOAT
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"float"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TypeInfo
name|DOUBLE
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"double"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TypeInfo
name|VOID
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"void"
argument_list|)
decl_stmt|;
comment|// These schemata are used in other tests
specifier|static
specifier|public
specifier|final
name|String
name|MAP_WITH_PRIMITIVE_VALUE_TYPE
init|=
literal|"{\n"
operator|+
literal|"  \"namespace\": \"testing\",\n"
operator|+
literal|"  \"name\": \"oneMap\",\n"
operator|+
literal|"  \"type\": \"record\",\n"
operator|+
literal|"  \"fields\": [\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aMap\",\n"
operator|+
literal|"      \"type\":{\"type\":\"map\",\n"
operator|+
literal|"      \"values\":\"long\"}\n"
operator|+
literal|"\t}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|static
specifier|public
specifier|final
name|String
name|ARRAY_WITH_PRIMITIVE_ELEMENT_TYPE
init|=
literal|"{\n"
operator|+
literal|"  \"namespace\": \"testing\",\n"
operator|+
literal|"  \"name\": \"oneArray\",\n"
operator|+
literal|"  \"type\": \"record\",\n"
operator|+
literal|"  \"fields\": [\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"anArray\",\n"
operator|+
literal|"      \"type\":{\"type\":\"array\",\n"
operator|+
literal|"      \"items\":\"string\"}\n"
operator|+
literal|"\t}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RECORD_SCHEMA
init|=
literal|"{\n"
operator|+
literal|"  \"namespace\": \"testing.test.mctesty\",\n"
operator|+
literal|"  \"name\": \"oneRecord\",\n"
operator|+
literal|"  \"type\": \"record\",\n"
operator|+
literal|"  \"fields\": [\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aRecord\",\n"
operator|+
literal|"      \"type\":{\"type\":\"record\",\n"
operator|+
literal|"              \"name\":\"recordWithinARecord\",\n"
operator|+
literal|"              \"fields\": [\n"
operator|+
literal|"                 {\n"
operator|+
literal|"                  \"name\":\"int1\",\n"
operator|+
literal|"                  \"type\":\"int\"\n"
operator|+
literal|"                },\n"
operator|+
literal|"                {\n"
operator|+
literal|"                  \"name\":\"boolean1\",\n"
operator|+
literal|"                  \"type\":\"boolean\"\n"
operator|+
literal|"                },\n"
operator|+
literal|"                {\n"
operator|+
literal|"                  \"name\":\"long1\",\n"
operator|+
literal|"                  \"type\":\"long\"\n"
operator|+
literal|"                }\n"
operator|+
literal|"      ]}\n"
operator|+
literal|"    }\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|UNION_SCHEMA
init|=
literal|"{\n"
operator|+
literal|"  \"namespace\": \"test.a.rossa\",\n"
operator|+
literal|"  \"name\": \"oneUnion\",\n"
operator|+
literal|"  \"type\": \"record\",\n"
operator|+
literal|"  \"fields\": [\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aUnion\",\n"
operator|+
literal|"      \"type\":[\"int\", \"string\"]\n"
operator|+
literal|"    }\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ENUM_SCHEMA
init|=
literal|"{\n"
operator|+
literal|"  \"namespace\": \"clever.namespace.name.in.space\",\n"
operator|+
literal|"  \"name\": \"oneEnum\",\n"
operator|+
literal|"  \"type\": \"record\",\n"
operator|+
literal|"  \"fields\": [\n"
operator|+
literal|"   {\n"
operator|+
literal|"      \"name\":\"baddies\",\n"
operator|+
literal|"      \"type\":{\"type\":\"enum\",\"name\":\"villians\", \"symbols\": "
operator|+
literal|"[\"DALEKS\", \"CYBERMEN\", \"SLITHEEN\", \"JAGRAFESS\"]}\n"
operator|+
literal|"      \n"
operator|+
literal|"      \n"
operator|+
literal|"    }\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FIXED_SCHEMA
init|=
literal|"{\n"
operator|+
literal|"  \"namespace\": \"ecapseman\",\n"
operator|+
literal|"  \"name\": \"oneFixed\",\n"
operator|+
literal|"  \"type\": \"record\",\n"
operator|+
literal|"  \"fields\": [\n"
operator|+
literal|"   {\n"
operator|+
literal|"      \"name\":\"hash\",\n"
operator|+
literal|"      \"type\":{\"type\": \"fixed\", \"name\": \"MD5\", \"size\": 16}\n"
operator|+
literal|"    }\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NULLABLE_STRING_SCHEMA
init|=
literal|"{\n"
operator|+
literal|"  \"type\": \"record\", \n"
operator|+
literal|"  \"name\": \"nullableUnionTest\",\n"
operator|+
literal|"  \"fields\" : [\n"
operator|+
literal|"    {\"name\":\"nullableString\", \"type\":[\"null\", \"string\"]}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAP_WITH_NULLABLE_PRIMITIVE_VALUE_TYPE_SCHEMA
init|=
literal|"{\n"
operator|+
literal|"  \"namespace\": \"testing\",\n"
operator|+
literal|"  \"name\": \"mapWithNullableUnionTest\",\n"
operator|+
literal|"  \"type\": \"record\",\n"
operator|+
literal|"  \"fields\": [\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aMap\",\n"
operator|+
literal|"      \"type\":{\"type\":\"map\",\n"
operator|+
literal|"      \"values\":[\"null\",\"long\"]}\n"
operator|+
literal|"\t}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BYTES_SCHEMA
init|=
literal|"{\n"
operator|+
literal|"  \"type\": \"record\", \n"
operator|+
literal|"  \"name\": \"bytesTest\",\n"
operator|+
literal|"  \"fields\" : [\n"
operator|+
literal|"    {\"name\":\"bytesField\", \"type\":\"bytes\"}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KITCHEN_SINK_SCHEMA
init|=
literal|"{\n"
operator|+
literal|"  \"namespace\": \"org.apache.hadoop.hive\",\n"
operator|+
literal|"  \"name\": \"kitchsink\",\n"
operator|+
literal|"  \"type\": \"record\",\n"
operator|+
literal|"  \"fields\": [\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"string1\",\n"
operator|+
literal|"      \"type\":\"string\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"string2\",\n"
operator|+
literal|"      \"type\":\"string\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"int1\",\n"
operator|+
literal|"      \"type\":\"int\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"boolean1\",\n"
operator|+
literal|"      \"type\":\"boolean\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"long1\",\n"
operator|+
literal|"      \"type\":\"long\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"float1\",\n"
operator|+
literal|"      \"type\":\"float\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"double1\",\n"
operator|+
literal|"      \"type\":\"double\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"inner_record1\",\n"
operator|+
literal|"      \"type\":{ \"type\":\"record\",\n"
operator|+
literal|"               \"name\":\"inner_record1_impl\",\n"
operator|+
literal|"               \"fields\": [\n"
operator|+
literal|"                          {\"name\":\"int_in_inner_record1\",\n"
operator|+
literal|"                           \"type\":\"int\"},\n"
operator|+
literal|"                          {\"name\":\"string_in_inner_record1\",\n"
operator|+
literal|"                           \"type\":\"string\"}\n"
operator|+
literal|"                         ]\n"
operator|+
literal|"       }\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"enum1\",\n"
operator|+
literal|"      \"type\":{\"type\":\"enum\", \"name\":\"enum1_values\", "
operator|+
literal|"\"symbols\":[\"ENUM1_VALUES_VALUE1\",\"ENUM1_VALUES_VALUE2\", \"ENUM1_VALUES_VALUE3\"]}\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"array1\",\n"
operator|+
literal|"      \"type\":{\"type\":\"array\", \"items\":\"string\"}\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"map1\",\n"
operator|+
literal|"      \"type\":{\"type\":\"map\", \"values\":\"string\"}\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"union1\",\n"
operator|+
literal|"      \"type\":[\"float\", \"boolean\", \"string\"]\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"fixed1\",\n"
operator|+
literal|"      \"type\":{\"type\":\"fixed\", \"name\":\"fourbytes\", \"size\":4}\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"null1\",\n"
operator|+
literal|"      \"type\":\"null\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"UnionNullInt\",\n"
operator|+
literal|"      \"type\":[\"int\", \"null\"]\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"bytes1\",\n"
operator|+
literal|"      \"type\":\"bytes\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
annotation|@
name|Test
comment|// that we can only process records
specifier|public
name|void
name|failOnNonRecords
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|nonRecordSchema
init|=
literal|"{ \"type\": \"enum\",\n"
operator|+
literal|"  \"name\": \"Suit\",\n"
operator|+
literal|"  \"symbols\" : [\"SPADES\", \"HEARTS\", \"DIAMONDS\", \"CLUBS\"]\n"
operator|+
literal|"}"
decl_stmt|;
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|nonRecordSchema
argument_list|)
decl_stmt|;
try|try
block|{
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should not be able to handle non-record Avro types"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|sde
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|sde
operator|.
name|getMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"Schema for table must be of type RECORD"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|primitiveTypesWorkCorrectly
parameter_list|()
throws|throws
name|SerDeException
block|{
specifier|final
name|String
name|bunchOfPrimitives
init|=
literal|"{\n"
operator|+
literal|"  \"namespace\": \"testing\",\n"
operator|+
literal|"  \"name\": \"PrimitiveTypes\",\n"
operator|+
literal|"  \"type\": \"record\",\n"
operator|+
literal|"  \"fields\": [\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aString\",\n"
operator|+
literal|"      \"type\":\"string\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"anInt\",\n"
operator|+
literal|"      \"type\":\"int\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aBoolean\",\n"
operator|+
literal|"      \"type\":\"boolean\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aLong\",\n"
operator|+
literal|"      \"type\":\"long\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aFloat\",\n"
operator|+
literal|"      \"type\":\"float\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aDouble\",\n"
operator|+
literal|"      \"type\":\"double\"\n"
operator|+
literal|"    },\n"
operator|+
literal|"    {\n"
operator|+
literal|"      \"name\":\"aNull\",\n"
operator|+
literal|"      \"type\":\"null\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|Schema
operator|.
name|parse
argument_list|(
name|bunchOfPrimitives
argument_list|)
argument_list|)
decl_stmt|;
name|String
index|[]
name|expectedColumnNames
init|=
block|{
literal|"aString"
block|,
literal|"anInt"
block|,
literal|"aBoolean"
block|,
literal|"aLong"
block|,
literal|"aFloat"
block|,
literal|"aDouble"
block|,
literal|"aNull"
block|}
decl_stmt|;
name|verifyColumnNames
argument_list|(
name|expectedColumnNames
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
argument_list|)
expr_stmt|;
name|TypeInfo
index|[]
name|expectedColumnTypes
init|=
block|{
name|STRING
block|,
name|INT
block|,
name|BOOLEAN
block|,
name|LONG
block|,
name|FLOAT
block|,
name|DOUBLE
block|,
name|VOID
block|}
decl_stmt|;
name|verifyColumnTypes
argument_list|(
name|expectedColumnTypes
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
argument_list|)
expr_stmt|;
comment|// Rip apart the object inspector, making sure we got what we expect.
specifier|final
name|ObjectInspector
name|oi
init|=
name|aoig
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|oi
operator|instanceof
name|StandardStructObjectInspector
argument_list|)
expr_stmt|;
specifier|final
name|StandardStructObjectInspector
name|ssoi
init|=
operator|(
name|StandardStructObjectInspector
operator|)
name|oi
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|structFields
init|=
name|ssoi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedColumnNames
operator|.
name|length
argument_list|,
name|structFields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expectedColumnNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
literal|"Column names don't match"
argument_list|,
name|expectedColumnNames
index|[
name|i
index|]
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|structFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Column types don't match"
argument_list|,
name|expectedColumnTypes
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
argument_list|,
name|structFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|verifyColumnTypes
parameter_list|(
name|TypeInfo
index|[]
name|expectedColumnTypes
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
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
name|expectedColumnTypes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|expectedColumnTypes
index|[
name|i
index|]
argument_list|,
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|verifyColumnNames
parameter_list|(
name|String
index|[]
name|expectedColumnNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
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
name|expectedColumnNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|expectedColumnNames
index|[
name|i
index|]
argument_list|,
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|canHandleMapsWithPrimitiveValueTypes
parameter_list|()
throws|throws
name|SerDeException
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|MAP_WITH_PRIMITIVE_VALUE_TYPE
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|verifyMap
argument_list|(
name|aoig
argument_list|,
literal|"aMap"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check a given AvroObjectInspectorGenerator to verify that it matches our test    * schema's expected map.    * @param aoig should already have been intitialized, may not be null    * @param fieldName name of the contianed column, will always fail if null.    */
specifier|private
name|void
name|verifyMap
parameter_list|(
specifier|final
name|AvroObjectInspectorGenerator
name|aoig
parameter_list|,
specifier|final
name|String
name|fieldName
parameter_list|)
block|{
comment|// Column names
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fieldName
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Column types
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|MAP
argument_list|,
name|typeInfo
operator|.
name|getCategory
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|typeInfo
operator|instanceof
name|MapTypeInfo
argument_list|)
expr_stmt|;
name|MapTypeInfo
name|mapTypeInfo
init|=
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|assertEquals
argument_list|(
literal|"bigint"
comment|/* == long in Avro */
argument_list|,
name|mapTypeInfo
operator|.
name|getMapValueTypeInfo
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"string"
argument_list|,
name|mapTypeInfo
operator|.
name|getMapKeyTypeInfo
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|canHandleArrays
parameter_list|()
throws|throws
name|SerDeException
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|ARRAY_WITH_PRIMITIVE_ELEMENT_TYPE
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
comment|// Column names
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"anArray"
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Column types
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|LIST
argument_list|,
name|typeInfo
operator|.
name|getCategory
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|typeInfo
operator|instanceof
name|ListTypeInfo
argument_list|)
expr_stmt|;
name|ListTypeInfo
name|listTypeInfo
init|=
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|assertEquals
argument_list|(
literal|"string"
argument_list|,
name|listTypeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|canHandleRecords
parameter_list|()
throws|throws
name|SerDeException
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|RECORD_SCHEMA
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
comment|// Column names
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"aRecord"
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Column types
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|STRUCT
argument_list|,
name|typeInfo
operator|.
name|getCategory
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|typeInfo
operator|instanceof
name|StructTypeInfo
argument_list|)
expr_stmt|;
name|StructTypeInfo
name|structTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
decl_stmt|;
comment|// Check individual elements of subrecord
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allStructFieldNames
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|allStructFieldTypeInfos
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|allStructFieldNames
operator|.
name|size
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|String
index|[]
name|names
init|=
operator|new
name|String
index|[]
block|{
literal|"int1"
block|,
literal|"boolean1"
block|,
literal|"long1"
block|}
decl_stmt|;
name|String
index|[]
name|typeInfoStrings
init|=
operator|new
name|String
index|[]
block|{
literal|"int"
block|,
literal|"boolean"
block|,
literal|"bigint"
block|}
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
name|allStructFieldNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
literal|"Fieldname "
operator|+
name|allStructFieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|" doesn't match expected "
operator|+
name|names
index|[
name|i
index|]
argument_list|,
name|names
index|[
name|i
index|]
argument_list|,
name|allStructFieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Typeinfo "
operator|+
name|allStructFieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|" doesn't match expected "
operator|+
name|typeInfoStrings
index|[
name|i
index|]
argument_list|,
name|typeInfoStrings
index|[
name|i
index|]
argument_list|,
name|allStructFieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|canHandleUnions
parameter_list|()
throws|throws
name|SerDeException
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|UNION_SCHEMA
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
comment|// Column names
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"aUnion"
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Column types
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|typeInfo
operator|instanceof
name|UnionTypeInfo
argument_list|)
expr_stmt|;
name|UnionTypeInfo
name|uti
init|=
operator|(
name|UnionTypeInfo
operator|)
name|typeInfo
decl_stmt|;
comment|// Check that the union has come out unscathed. No scathing of unions allowed.
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|typeInfos
init|=
name|uti
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|typeInfos
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|INT
argument_list|,
name|typeInfos
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|STRING
argument_list|,
name|typeInfos
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"uniontype<int,string>"
argument_list|,
name|uti
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// Enums are one of two Avro types that Hive doesn't have any native support for.
specifier|public
name|void
name|canHandleEnums
parameter_list|()
throws|throws
name|SerDeException
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|ENUM_SCHEMA
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
comment|// Column names - we lose the enumness of this schema
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"baddies"
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Column types
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|STRING
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// Hive has no concept of Avro's fixed type.  Fixed -> arrays of bytes
specifier|public
name|void
name|canHandleFixed
parameter_list|()
throws|throws
name|SerDeException
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|FIXED_SCHEMA
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
comment|// Column names
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hash"
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Column types
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|typeInfo
operator|instanceof
name|ListTypeInfo
argument_list|)
expr_stmt|;
name|ListTypeInfo
name|listTypeInfo
init|=
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|assertTrue
argument_list|(
name|listTypeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
operator|instanceof
name|PrimitiveTypeInfo
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tinyint"
argument_list|,
name|listTypeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// Avro considers bytes primitive, Hive doesn't. Make them list of tinyint.
specifier|public
name|void
name|canHandleBytes
parameter_list|()
throws|throws
name|SerDeException
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|BYTES_SCHEMA
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
comment|// Column names
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bytesField"
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Column types
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|typeInfo
operator|instanceof
name|ListTypeInfo
argument_list|)
expr_stmt|;
name|ListTypeInfo
name|listTypeInfo
init|=
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|assertTrue
argument_list|(
name|listTypeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
operator|instanceof
name|PrimitiveTypeInfo
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tinyint"
argument_list|,
name|listTypeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// That Union[T, NULL] is converted to just T.
specifier|public
name|void
name|convertsNullableTypes
parameter_list|()
throws|throws
name|SerDeException
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|NULLABLE_STRING_SCHEMA
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"nullableString"
argument_list|,
name|aoig
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Column types
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|aoig
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|typeInfo
operator|instanceof
name|PrimitiveTypeInfo
argument_list|)
expr_stmt|;
name|PrimitiveTypeInfo
name|pti
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
decl_stmt|;
comment|// Verify the union has been hidden and just the main type has been returned.
name|assertEquals
argument_list|(
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|STRING
argument_list|,
name|pti
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// That Union[T, NULL] is converted to just T, within a Map
specifier|public
name|void
name|convertsMapsWithNullablePrimitiveTypes
parameter_list|()
throws|throws
name|SerDeException
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|MAP_WITH_NULLABLE_PRIMITIVE_VALUE_TYPE_SCHEMA
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|verifyMap
argument_list|(
name|aoig
argument_list|,
literal|"aMap"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|objectInspectorsAreCached
parameter_list|()
throws|throws
name|SerDeException
block|{
comment|// Verify that Hive is caching the object inspectors for us.
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|KITCHEN_SINK_SCHEMA
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Schema
name|s2
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|KITCHEN_SINK_SCHEMA
argument_list|)
decl_stmt|;
name|AvroObjectInspectorGenerator
name|aoig2
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|s2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|aoig
operator|.
name|getObjectInspector
argument_list|()
argument_list|,
name|aoig2
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
comment|// For once we actually want reference equality in Java.
name|assertTrue
argument_list|(
name|aoig
operator|.
name|getObjectInspector
argument_list|()
operator|==
name|aoig2
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

