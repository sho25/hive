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
name|fs
operator|.
name|FSDataOutputStream
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
name|fs
operator|.
name|Path
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Properties
import|;
end_import

begin_import
import|import static
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
operator|.
name|AvroSerdeUtils
operator|.
name|EXCEPTION_MESSAGE
import|;
end_import

begin_import
import|import static
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
operator|.
name|AvroSerdeUtils
operator|.
name|SCHEMA_NONE
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
name|avro
operator|.
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
import|;
end_import

begin_import
import|import static
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
operator|.
name|AvroSerdeUtils
operator|.
name|determineSchemaOrThrowException
import|;
end_import

begin_import
import|import static
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
operator|.
name|AvroSerdeUtils
operator|.
name|getOtherTypeFromNullableType
import|;
end_import

begin_import
import|import static
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
operator|.
name|AvroSerdeUtils
operator|.
name|isNullableType
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
name|assertNotNull
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
name|TestAvroSerdeUtils
block|{
specifier|private
specifier|final
name|String
name|NULLABLE_UNION
init|=
literal|"{\n"
operator|+
literal|"  \"type\": \"record\", \n"
operator|+
literal|"  \"name\": \"nullTest\",\n"
operator|+
literal|"  \"fields\" : [\n"
operator|+
literal|"    {\"name\":\"mayBeNull\", \"type\":[\"string\", \"null\"]}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
comment|// Same union, order reveresed
specifier|private
specifier|final
name|String
name|NULLABLE_UNION2
init|=
literal|"{\n"
operator|+
literal|"  \"type\": \"record\", \n"
operator|+
literal|"  \"name\": \"nullTest\",\n"
operator|+
literal|"  \"fields\" : [\n"
operator|+
literal|"    {\"name\":\"mayBeNull\", \"type\":[\"null\", \"string\"]}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|private
name|void
name|testField
parameter_list|(
name|String
name|schemaString
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|boolean
name|shouldBeNullable
parameter_list|)
block|{
name|Schema
name|s
init|=
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|schemaString
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|shouldBeNullable
argument_list|,
name|isNullableType
argument_list|(
name|s
operator|.
name|getField
argument_list|(
name|fieldName
argument_list|)
operator|.
name|schema
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|isNullableTypeAcceptsNullableUnions
parameter_list|()
block|{
name|testField
argument_list|(
name|NULLABLE_UNION
argument_list|,
literal|"mayBeNull"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|testField
argument_list|(
name|NULLABLE_UNION2
argument_list|,
literal|"mayBeNull"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|isNullableTypeIdentifiesUnionsOfMoreThanTwoTypes
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|schemaStrings
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"{\n"
operator|+
literal|"  \"type\": \"record\", \n"
operator|+
literal|"  \"name\": \"shouldNotPass\",\n"
operator|+
literal|"  \"fields\" : [\n"
operator|+
literal|"    {\"name\":\"mayBeNull\", \"type\":[\"string\", \"int\", \"null\"]}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
argument_list|,
literal|"{\n"
operator|+
literal|"  \"type\": \"record\", \n"
operator|+
literal|"  \"name\": \"shouldNotPass\",\n"
operator|+
literal|"  \"fields\" : [\n"
operator|+
literal|"    {\"name\":\"mayBeNull\", \"type\":[\"string\", \"null\", \"int\"]}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
argument_list|,
literal|"{\n"
operator|+
literal|"  \"type\": \"record\", \n"
operator|+
literal|"  \"name\": \"shouldNotPass\",\n"
operator|+
literal|"  \"fields\" : [\n"
operator|+
literal|"    {\"name\":\"mayBeNull\", \"type\":[\"null\", \"string\", \"int\"]}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|schemaString
range|:
name|schemaStrings
control|)
block|{
name|testField
argument_list|(
name|schemaString
argument_list|,
literal|"mayBeNull"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|isNullableTypeIdentifiesUnionsWithoutNulls
parameter_list|()
block|{
name|String
name|s
init|=
literal|"{\n"
operator|+
literal|"  \"type\": \"record\", \n"
operator|+
literal|"  \"name\": \"unionButNoNull\",\n"
operator|+
literal|"  \"fields\" : [\n"
operator|+
literal|"    {\"name\":\"a\", \"type\":[\"int\", \"string\"]}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
name|testField
argument_list|(
name|s
argument_list|,
literal|"a"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|isNullableTypeIdentifiesNonUnionTypes
parameter_list|()
block|{
name|String
name|schemaString
init|=
literal|"{\n"
operator|+
literal|"  \"type\": \"record\", \n"
operator|+
literal|"  \"name\": \"nullTest2\",\n"
operator|+
literal|"  \"fields\" : [\n"
operator|+
literal|"    {\"name\":\"justAnInt\", \"type\":\"int\"}\n"
operator|+
literal|"  ]\n"
operator|+
literal|"}"
decl_stmt|;
name|testField
argument_list|(
name|schemaString
argument_list|,
literal|"justAnInt"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getTypeFromNullableTypePositiveCase
parameter_list|()
block|{
name|Schema
name|s
init|=
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|NULLABLE_UNION
argument_list|)
decl_stmt|;
name|Schema
name|typeFromNullableType
init|=
name|getOtherTypeFromNullableType
argument_list|(
name|s
operator|.
name|getField
argument_list|(
literal|"mayBeNull"
argument_list|)
operator|.
name|schema
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|typeFromNullableType
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|s
operator|=
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|NULLABLE_UNION2
argument_list|)
expr_stmt|;
name|typeFromNullableType
operator|=
name|getOtherTypeFromNullableType
argument_list|(
name|s
operator|.
name|getField
argument_list|(
literal|"mayBeNull"
argument_list|)
operator|.
name|schema
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|typeFromNullableType
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|AvroSerdeException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|determineSchemaThrowsExceptionIfNoSchema
parameter_list|()
throws|throws
name|IOException
throws|,
name|AvroSerdeException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|prop
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|AvroSerdeUtils
operator|.
name|determineSchemaOrThrowException
argument_list|(
name|conf
argument_list|,
name|prop
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|determineSchemaFindsLiterals
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|schema
init|=
name|TestAvroObjectInspectorGenerator
operator|.
name|RECORD_SCHEMA
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
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
name|put
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|,
name|schema
argument_list|)
expr_stmt|;
name|Schema
name|expected
init|=
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|AvroSerdeUtils
operator|.
name|determineSchemaOrThrowException
argument_list|(
name|conf
argument_list|,
name|props
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|detemineSchemaTriesToOpenUrl
parameter_list|()
throws|throws
name|AvroSerdeException
throws|,
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
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
name|put
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|,
literal|"not:///a.real.url"
argument_list|)
expr_stmt|;
try|try
block|{
name|AvroSerdeUtils
operator|.
name|determineSchemaOrThrowException
argument_list|(
name|conf
argument_list|,
name|props
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have tried to open that URL"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AvroSerdeException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Unable to read schema from given path: not:///a.real.url"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|noneOptionWorksForSpecifyingSchemas
parameter_list|()
throws|throws
name|IOException
throws|,
name|AvroSerdeException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// Combo 1: Both set to none
name|props
operator|.
name|put
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|,
name|SCHEMA_NONE
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|,
name|SCHEMA_NONE
argument_list|)
expr_stmt|;
try|try
block|{
name|determineSchemaOrThrowException
argument_list|(
name|conf
argument_list|,
name|props
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown exception with none set for both url and literal"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AvroSerdeException
name|he
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|EXCEPTION_MESSAGE
argument_list|,
name|he
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Combo 2: Literal set, url set to none
name|props
operator|.
name|put
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|,
name|TestAvroObjectInspectorGenerator
operator|.
name|RECORD_SCHEMA
argument_list|)
expr_stmt|;
name|Schema
name|s
decl_stmt|;
try|try
block|{
name|s
operator|=
name|determineSchemaOrThrowException
argument_list|(
name|conf
argument_list|,
name|props
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|TestAvroObjectInspectorGenerator
operator|.
name|RECORD_SCHEMA
argument_list|)
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AvroSerdeException
name|he
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Should have parsed schema literal, not thrown exception."
argument_list|)
expr_stmt|;
block|}
comment|// Combo 3: url set, literal set to none
name|props
operator|.
name|put
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|,
name|SCHEMA_NONE
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|,
literal|"not:///a.real.url"
argument_list|)
expr_stmt|;
try|try
block|{
name|determineSchemaOrThrowException
argument_list|(
name|conf
argument_list|,
name|props
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have tried to open that bogus URL"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AvroSerdeException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Unable to read schema from given path: not:///a.real.url"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|determineSchemaCanReadSchemaFromHDFS
parameter_list|()
throws|throws
name|IOException
throws|,
name|AvroSerdeException
throws|,
name|URISyntaxException
block|{
name|String
name|schemaString
init|=
name|TestAvroObjectInspectorGenerator
operator|.
name|RECORD_SCHEMA
decl_stmt|;
name|MiniDFSCluster
name|miniDfs
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// MiniDFSCluster litters files and folders all over the place.
name|miniDfs
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|miniDfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/path/to/schema"
argument_list|)
argument_list|)
expr_stmt|;
name|FSDataOutputStream
name|out
init|=
name|miniDfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/path/to/schema/schema.avsc"
argument_list|)
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|schemaString
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|String
name|onHDFS
init|=
name|miniDfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|+
literal|"/path/to/schema/schema.avsc"
decl_stmt|;
name|Schema
name|schemaFromHDFS
init|=
name|AvroSerdeUtils
operator|.
name|getSchemaFromFS
argument_list|(
name|onHDFS
argument_list|,
name|miniDfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Schema
name|expectedSchema
init|=
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|schemaString
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedSchema
argument_list|,
name|schemaFromHDFS
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|miniDfs
operator|!=
literal|null
condition|)
name|miniDfs
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

