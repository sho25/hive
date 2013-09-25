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
name|avro
operator|.
name|generic
operator|.
name|GenericData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericRecord
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestSchemaReEncoder
block|{
annotation|@
name|Test
specifier|public
name|void
name|schemasCanAddFields
parameter_list|()
throws|throws
name|SerDeException
block|{
name|String
name|original
init|=
literal|"{\n"
operator|+
literal|"    \"namespace\": \"org.apache.hadoop.hive\",\n"
operator|+
literal|"    \"name\": \"Line\",\n"
operator|+
literal|"    \"type\": \"record\",\n"
operator|+
literal|"    \"fields\": [\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"text\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    ]\n"
operator|+
literal|"}"
decl_stmt|;
name|String
name|evolved
init|=
literal|"{\n"
operator|+
literal|"    \"namespace\": \"org.apache.hadoop.hive\",\n"
operator|+
literal|"    \"name\": \"Line\",\n"
operator|+
literal|"    \"type\": \"record\",\n"
operator|+
literal|"    \"fields\": [\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"text\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"new_kid\",\n"
operator|+
literal|"            \"type\":\"string\",\n"
operator|+
literal|"            \"default\":\"Hi!\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    ]\n"
operator|+
literal|"}"
decl_stmt|;
name|Schema
name|originalSchema
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|Schema
name|evolvedSchema
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|evolved
argument_list|)
decl_stmt|;
name|GenericRecord
name|record
init|=
operator|new
name|GenericData
operator|.
name|Record
argument_list|(
name|originalSchema
argument_list|)
decl_stmt|;
name|record
operator|.
name|put
argument_list|(
literal|"text"
argument_list|,
literal|"it is a far better thing I do, yadda, yadda"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|GenericData
operator|.
name|get
argument_list|()
operator|.
name|validate
argument_list|(
name|originalSchema
argument_list|,
name|record
argument_list|)
argument_list|)
expr_stmt|;
name|AvroDeserializer
operator|.
name|SchemaReEncoder
name|schemaReEncoder
init|=
operator|new
name|AvroDeserializer
operator|.
name|SchemaReEncoder
argument_list|(
name|record
operator|.
name|getSchema
argument_list|()
argument_list|,
name|evolvedSchema
argument_list|)
decl_stmt|;
name|GenericRecord
name|r2
init|=
name|schemaReEncoder
operator|.
name|reencode
argument_list|(
name|record
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|GenericData
operator|.
name|get
argument_list|()
operator|.
name|validate
argument_list|(
name|evolvedSchema
argument_list|,
name|r2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Hi!"
argument_list|,
name|r2
operator|.
name|get
argument_list|(
literal|"new_kid"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now make sure that we can re-use the re-encoder against a completely
comment|// different record to save resources
name|String
name|original2
init|=
literal|"{\n"
operator|+
literal|"    \"namespace\": \"somebody.else\",\n"
operator|+
literal|"    \"name\": \"something_else\",\n"
operator|+
literal|"    \"type\": \"record\",\n"
operator|+
literal|"    \"fields\": [\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"a\",\n"
operator|+
literal|"            \"type\":\"int\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    ]\n"
operator|+
literal|"}"
decl_stmt|;
name|String
name|evolved2
init|=
literal|"{\n"
operator|+
literal|"    \"namespace\": \"somebody.else\",\n"
operator|+
literal|"    \"name\": \"something_else\",\n"
operator|+
literal|"    \"type\": \"record\",\n"
operator|+
literal|"    \"fields\": [\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"a\",\n"
operator|+
literal|"            \"type\":\"int\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"b\",\n"
operator|+
literal|"            \"type\":\"long\",\n"
operator|+
literal|"            \"default\":42\n"
operator|+
literal|"        }\n"
operator|+
literal|"    ]\n"
operator|+
literal|"}"
decl_stmt|;
name|Schema
name|originalSchema2
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|original2
argument_list|)
decl_stmt|;
name|Schema
name|evolvedSchema2
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|evolved2
argument_list|)
decl_stmt|;
name|record
operator|=
operator|new
name|GenericData
operator|.
name|Record
argument_list|(
name|originalSchema2
argument_list|)
expr_stmt|;
name|record
operator|.
name|put
argument_list|(
literal|"a"
argument_list|,
literal|19
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|GenericData
operator|.
name|get
argument_list|()
operator|.
name|validate
argument_list|(
name|originalSchema2
argument_list|,
name|record
argument_list|)
argument_list|)
expr_stmt|;
name|schemaReEncoder
operator|=
operator|new
name|AvroDeserializer
operator|.
name|SchemaReEncoder
argument_list|(
name|record
operator|.
name|getSchema
argument_list|()
argument_list|,
name|evolvedSchema2
argument_list|)
expr_stmt|;
name|r2
operator|=
name|schemaReEncoder
operator|.
name|reencode
argument_list|(
name|record
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|GenericData
operator|.
name|get
argument_list|()
operator|.
name|validate
argument_list|(
name|evolvedSchema2
argument_list|,
name|r2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|42l
argument_list|,
name|r2
operator|.
name|get
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

