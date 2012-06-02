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

begin_class
class|class
name|SchemaResolutionProblem
block|{
specifier|static
specifier|final
name|String
name|sentinelString
init|=
literal|"{\n"
operator|+
literal|"    \"namespace\": \"org.apache.hadoop.hive\",\n"
operator|+
literal|"    \"name\": \"CannotDetermineSchemaSentinel\",\n"
operator|+
literal|"    \"type\": \"record\",\n"
operator|+
literal|"    \"fields\": [\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"Cannot_determine_schema\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"check\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"schema\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"url\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"and\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        },\n"
operator|+
literal|"        {\n"
operator|+
literal|"            \"name\":\"literal\",\n"
operator|+
literal|"            \"type\":\"string\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    ]\n"
operator|+
literal|"}"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|Schema
name|SIGNAL_BAD_SCHEMA
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|sentinelString
argument_list|)
decl_stmt|;
block|}
end_class

end_unit

