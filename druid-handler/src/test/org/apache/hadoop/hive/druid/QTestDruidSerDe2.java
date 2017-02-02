begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|druid
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
name|hadoop
operator|.
name|hive
operator|.
name|druid
operator|.
name|serde
operator|.
name|DruidSerDe
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|type
operator|.
name|TypeReference
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|query
operator|.
name|metadata
operator|.
name|metadata
operator|.
name|SegmentAnalysis
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|query
operator|.
name|metadata
operator|.
name|metadata
operator|.
name|SegmentMetadataQuery
import|;
end_import

begin_comment
comment|/**  * Druid SerDe to be used in tests.  */
end_comment

begin_class
specifier|public
class|class
name|QTestDruidSerDe2
extends|extends
name|DruidSerDe
block|{
comment|// Request :
comment|//        "{\"queryType\":\"segmentMetadata\",\"dataSource\":{\"type\":\"table\",\"name\":\"wikipedia\"},"
comment|//        + "\"intervals\":{\"type\":\"intervals\","
comment|//        + "\"intervals\":[\"-146136543-09-08T00:30:34.096-07:52:58/146140482-04-24T08:36:27.903-07:00\"]},"
comment|//        + "\"toInclude\":{\"type\":\"all\"},\"merge\":true,\"context\":null,\"analysisTypes\":[],"
comment|//        + "\"usingDefaultInterval\":true,\"lenientAggregatorMerge\":false,\"descending\":false}";
specifier|private
specifier|static
specifier|final
name|String
name|RESPONSE
init|=
literal|"[ {\r\n "
operator|+
literal|" \"id\" : \"merged\",\r\n "
operator|+
literal|" \"intervals\" : [ \"2010-01-01T00:00:00.000Z/2015-12-31T00:00:00.000Z\" ],\r\n "
operator|+
literal|" \"columns\" : {\r\n  "
operator|+
literal|"  \"__time\" : { \"type\" : \"LONG\", \"hasMultipleValues\" : false, \"size\" : 407240380, \"cardinality\" : null, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"robot\" : { \"type\" : \"STRING\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : 1944, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"namespace\" : { \"type\" : \"STRING\", \"hasMultipleValues\" : true, \"size\" : 100000, \"cardinality\" : 1504, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"anonymous\" : { \"type\" : \"STRING\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : 1944, \"errorMessage\" : null },\r\n  "
comment|// Next column has a similar name as previous, but different casing.
comment|// This is allowed in Druid, but it should fail in Hive.
operator|+
literal|"  \"Anonymous\" : { \"type\" : \"STRING\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : 1944, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"page\" : { \"type\" : \"STRING\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : 1944, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"language\" : { \"type\" : \"STRING\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : 1944, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"newpage\" : { \"type\" : \"STRING\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : 1944, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"user\" : { \"type\" : \"STRING\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : 1944, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"count\" : { \"type\" : \"FLOAT\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : null, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"added\" : { \"type\" : \"FLOAT\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : null, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"delta\" : { \"type\" : \"FLOAT\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : null, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"variation\" : { \"type\" : \"FLOAT\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : null, \"errorMessage\" : null },\r\n  "
operator|+
literal|"  \"deleted\" : { \"type\" : \"FLOAT\", \"hasMultipleValues\" : false, \"size\" : 100000, \"cardinality\" : null, \"errorMessage\" : null }\r\n "
operator|+
literal|" },\r\n "
operator|+
literal|" \"aggregators\" : {\r\n  "
operator|+
literal|"  \"count\" : { \"type\" : \"longSum\", \"name\" : \"count\", \"fieldName\" : \"count\" },\r\n  "
operator|+
literal|"  \"added\" : { \"type\" : \"doubleSum\", \"name\" : \"added\", \"fieldName\" : \"added\" },\r\n  "
operator|+
literal|"  \"delta\" : { \"type\" : \"doubleSum\", \"name\" : \"delta\", \"fieldName\" : \"delta\" },\r\n  "
operator|+
literal|"  \"variation\" : { \"type\" : \"doubleSum\", \"name\" : \"variation\", \"fieldName\" : \"variation\" },\r\n  "
operator|+
literal|"  \"deleted\" : { \"type\" : \"doubleSum\", \"name\" : \"deleted\", \"fieldName\" : \"deleted\" }\r\n "
operator|+
literal|" },\r\n "
operator|+
literal|" \"queryGranularity\" : {\r\n    \"type\": \"none\"\r\n  },\r\n "
operator|+
literal|" \"size\" : 300000,\r\n "
operator|+
literal|" \"numRows\" : 5000000\r\n} ]"
decl_stmt|;
comment|/* Submits the request and returns */
annotation|@
name|Override
specifier|protected
name|SegmentAnalysis
name|submitMetadataRequest
parameter_list|(
name|String
name|address
parameter_list|,
name|SegmentMetadataQuery
name|query
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Retrieve results
name|List
argument_list|<
name|SegmentAnalysis
argument_list|>
name|resultsList
decl_stmt|;
try|try
block|{
name|resultsList
operator|=
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|readValue
argument_list|(
name|RESPONSE
argument_list|,
operator|new
name|TypeReference
argument_list|<
name|List
argument_list|<
name|SegmentAnalysis
argument_list|>
argument_list|>
argument_list|()
block|{               }
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|resultsList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
end_class

end_unit

