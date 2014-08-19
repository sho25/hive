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
name|avro
operator|.
name|file
operator|.
name|DataFileStream
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
name|file
operator|.
name|DataFileWriter
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
name|GenericDatumReader
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
name|GenericDatumWriter
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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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

begin_class
specifier|public
class|class
name|TestThatEvolvedSchemasActAsWeWant
block|{
annotation|@
name|Test
specifier|public
name|void
name|resolvedSchemasShouldReturnReaderSchema
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Need to verify that when reading a datum with an updated reader schema
comment|// that the datum then returns the reader schema as its own, since we
comment|// depend on this behavior in order to avoid re-encoding the datum
comment|// in the serde.
name|String
name|v0
init|=
literal|"{\n"
operator|+
literal|"    \"namespace\": \"org.apache.hadoop.hive\",\n"
operator|+
literal|"    \"name\": \"SomeStuff\",\n"
operator|+
literal|"    \"type\": \"record\",\n"
operator|+
literal|"    \"fields\": [\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"v0\",\n"
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
name|v1
init|=
literal|"{\n"
operator|+
literal|"    \"namespace\": \"org.apache.hadoop.hive\",\n"
operator|+
literal|"    \"name\": \"SomeStuff\",\n"
operator|+
literal|"    \"type\": \"record\",\n"
operator|+
literal|"    \"fields\": [\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"v0\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"v1\",\n"
operator|+
literal|"            \"type\":\"string\",\n"
operator|+
literal|"            \"default\":\"v1_default\""
operator|+
literal|"        }\n"
operator|+
literal|"    ]\n"
operator|+
literal|"}"
decl_stmt|;
name|Schema
index|[]
name|schemas
init|=
block|{
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|v0
argument_list|)
block|,
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|v1
argument_list|)
block|}
decl_stmt|;
comment|// Encode a schema with v0, write out.
name|GenericRecord
name|record
init|=
operator|new
name|GenericData
operator|.
name|Record
argument_list|(
name|schemas
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|record
operator|.
name|put
argument_list|(
literal|"v0"
argument_list|,
literal|"v0 value"
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
name|schemas
index|[
literal|0
index|]
argument_list|,
name|record
argument_list|)
argument_list|)
expr_stmt|;
comment|// Write datum out to a stream
name|GenericDatumWriter
argument_list|<
name|GenericRecord
argument_list|>
name|gdw
init|=
operator|new
name|GenericDatumWriter
argument_list|<
name|GenericRecord
argument_list|>
argument_list|(
name|schemas
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|DataFileWriter
argument_list|<
name|GenericRecord
argument_list|>
name|dfw
init|=
operator|new
name|DataFileWriter
argument_list|<
name|GenericRecord
argument_list|>
argument_list|(
name|gdw
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|dfw
operator|.
name|create
argument_list|(
name|schemas
index|[
literal|0
index|]
argument_list|,
name|baos
argument_list|)
expr_stmt|;
name|dfw
operator|.
name|append
argument_list|(
name|record
argument_list|)
expr_stmt|;
name|dfw
operator|.
name|close
argument_list|()
expr_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|GenericDatumReader
argument_list|<
name|GenericRecord
argument_list|>
name|gdr
init|=
operator|new
name|GenericDatumReader
argument_list|<
name|GenericRecord
argument_list|>
argument_list|()
decl_stmt|;
name|gdr
operator|.
name|setExpected
argument_list|(
name|schemas
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|DataFileStream
argument_list|<
name|GenericRecord
argument_list|>
name|dfs
init|=
operator|new
name|DataFileStream
argument_list|<
name|GenericRecord
argument_list|>
argument_list|(
name|bais
argument_list|,
name|gdr
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|dfs
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|GenericRecord
name|next
init|=
name|dfs
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"v0 value"
argument_list|,
name|next
operator|.
name|get
argument_list|(
literal|"v0"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"v1_default"
argument_list|,
name|next
operator|.
name|get
argument_list|(
literal|"v1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now the most important check - when we query this record for its schema,
comment|// we should get back the latest, reader schema:
name|assertEquals
argument_list|(
name|schemas
index|[
literal|1
index|]
argument_list|,
name|next
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

