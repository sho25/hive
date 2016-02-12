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
name|java
operator|.
name|util
operator|.
name|List
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
name|junit
operator|.
name|Rule
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
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
import|;
end_import

begin_class
specifier|public
class|class
name|TestSchemaToTypeInfo
block|{
annotation|@
name|Rule
specifier|public
name|ExpectedException
name|expect
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testDisallowRecursiveSchema
parameter_list|()
throws|throws
name|AvroSerdeException
block|{
name|expect
operator|.
name|expect
argument_list|(
name|AvroSerdeException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expect
operator|.
name|expectMessage
argument_list|(
literal|"Recursive schemas are not supported"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|schemaString
init|=
literal|"{\n"
operator|+
literal|"  \"type\" : \"record\",\n"
operator|+
literal|"  \"name\" : \"Cycle\",\n"
operator|+
literal|"  \"namespace\" : \"org.apache.hadoop.hive.serde2.avro\",\n"
operator|+
literal|"  \"fields\" : [ {\n"
operator|+
literal|"    \"name\" : \"child\",\n"
operator|+
literal|"    \"type\" : [ \"null\", \"Cycle\"],\n"
operator|+
literal|"    \"default\" : null\n"
operator|+
literal|"  } ]\n"
operator|+
literal|"}"
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|types
init|=
name|SchemaToTypeInfo
operator|.
name|generateColumnTypes
argument_list|(
operator|new
name|Schema
operator|.
name|Parser
argument_list|()
operator|.
name|parse
argument_list|(
name|schemaString
argument_list|)
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

