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
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|rmi
operator|.
name|server
operator|.
name|UID
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestGenericAvroRecordWritable
block|{
specifier|private
specifier|static
specifier|final
name|String
name|schemaJSON
init|=
literal|"{\n"
operator|+
literal|"    \"namespace\": \"gallifrey\",\n"
operator|+
literal|"    \"name\": \"TestPerson\",\n"
operator|+
literal|"    \"type\": \"record\",\n"
operator|+
literal|"    \"fields\": [\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"first\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"last\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    ]\n"
operator|+
literal|"}"
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|writableContractIsImplementedCorrectly
parameter_list|()
throws|throws
name|IOException
block|{
name|Schema
name|schema
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|schemaJSON
argument_list|)
decl_stmt|;
name|GenericRecord
name|gr
init|=
operator|new
name|GenericData
operator|.
name|Record
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|gr
operator|.
name|put
argument_list|(
literal|"first"
argument_list|,
literal|"The"
argument_list|)
expr_stmt|;
name|gr
operator|.
name|put
argument_list|(
literal|"last"
argument_list|,
literal|"Doctor"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"The"
argument_list|,
name|gr
operator|.
name|get
argument_list|(
literal|"first"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Doctor"
argument_list|,
name|gr
operator|.
name|get
argument_list|(
literal|"last"
argument_list|)
argument_list|)
expr_stmt|;
name|AvroGenericRecordWritable
name|garw
init|=
operator|new
name|AvroGenericRecordWritable
argument_list|(
name|gr
argument_list|)
decl_stmt|;
name|garw
operator|.
name|setFileSchema
argument_list|(
name|gr
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
name|garw
operator|.
name|setRecordReaderID
argument_list|(
operator|new
name|UID
argument_list|()
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|daos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|garw
operator|.
name|write
argument_list|(
name|daos
argument_list|)
expr_stmt|;
name|AvroGenericRecordWritable
name|garw2
init|=
operator|new
name|AvroGenericRecordWritable
argument_list|(
name|gr
argument_list|)
decl_stmt|;
name|garw
operator|.
name|setFileSchema
argument_list|(
name|gr
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
name|garw2
operator|.
name|setRecordReaderID
argument_list|(
operator|new
name|UID
argument_list|()
argument_list|)
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
name|DataInputStream
name|dais
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
name|garw2
operator|.
name|readFields
argument_list|(
name|dais
argument_list|)
expr_stmt|;
name|GenericRecord
name|gr2
init|=
name|garw2
operator|.
name|getRecord
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"The"
argument_list|,
name|gr2
operator|.
name|get
argument_list|(
literal|"first"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Doctor"
argument_list|,
name|gr2
operator|.
name|get
argument_list|(
literal|"last"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

