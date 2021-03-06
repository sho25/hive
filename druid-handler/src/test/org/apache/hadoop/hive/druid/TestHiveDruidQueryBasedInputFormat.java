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
name|druid
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|conf
operator|.
name|Constants
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
name|conf
operator|.
name|HiveConf
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
name|io
operator|.
name|DruidQueryBasedInputFormat
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
name|io
operator|.
name|HiveDruidSplit
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
name|mapreduce
operator|.
name|lib
operator|.
name|input
operator|.
name|FileInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|query
operator|.
name|Query
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
comment|/**  * Test Class.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"SameParameterValue"
argument_list|)
specifier|public
class|class
name|TestHiveDruidQueryBasedInputFormat
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TIMESERIES_QUERY
init|=
literal|"{  \"queryType\": \"timeseries\", "
operator|+
literal|" \"dataSource\": \"sample_datasource\", "
operator|+
literal|" \"granularity\": \"DAY\", "
operator|+
literal|" \"descending\": \"true\", "
operator|+
literal|" \"intervals\": [ \"2012-01-01T00:00:00.000-08:00/2012-01-03T00:00:00.000-08:00\" ]}"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TIMESERIES_QUERY_SPLIT
init|=
literal|"[HiveDruidSplit{{\"queryType\":\"timeseries\","
operator|+
literal|"\"dataSource\":{\"type\":\"table\",\"name\":\"sample_datasource\"},"
operator|+
literal|"\"intervals\":{\"type\":\"LegacySegmentSpec\",\"intervals\":"
operator|+
literal|"[\"2012-01-01T08:00:00.000Z/2012-01-03T08:00:00.000Z\"]},"
operator|+
literal|"\"descending\":true,"
operator|+
literal|"\"virtualColumns\":[],"
operator|+
literal|"\"filter\":null,"
operator|+
literal|"\"granularity\":\"DAY\","
operator|+
literal|"\"aggregations\":[],"
operator|+
literal|"\"postAggregations\":[],"
operator|+
literal|"\"limit\":2147483647,"
operator|+
literal|"\"context\":{\"queryId\":\"\"}}, [localhost:8082]}]"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TOPN_QUERY
init|=
literal|"{  \"queryType\": \"topN\", "
operator|+
literal|" \"dataSource\": \"sample_data\", "
operator|+
literal|" \"dimension\": \"sample_dim\", "
operator|+
literal|" \"threshold\": 5, "
operator|+
literal|" \"metric\": \"count\", "
operator|+
literal|" \"aggregations\": [  "
operator|+
literal|"  {   "
operator|+
literal|"   \"type\": \"longSum\",   "
operator|+
literal|"   \"name\": \"count\",   "
operator|+
literal|"   \"fieldName\": \"count\"  "
operator|+
literal|"  },  "
operator|+
literal|"  {   "
operator|+
literal|"   \"type\": \"doubleSum\",   "
operator|+
literal|"   \"name\": \"some_metric\",   "
operator|+
literal|"   \"fieldName\": \"some_metric\"  "
operator|+
literal|"  } "
operator|+
literal|" ], "
operator|+
literal|" \"granularity\": \"all\", "
operator|+
literal|" \"intervals\": [  "
operator|+
literal|"  \"2013-08-31T00:00:00.000-07:00/2013-09-03T00:00:00.000-07:00\" "
operator|+
literal|" ]}"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TOPN_QUERY_SPLIT
init|=
literal|"[HiveDruidSplit{{\"queryType\":\"topN\","
operator|+
literal|"\"dataSource\":{\"type\":\"table\",\"name\":\"sample_data\"},"
operator|+
literal|"\"virtualColumns\":[],"
operator|+
literal|"\"dimension\":{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"sample_dim\","
operator|+
literal|"\"outputName\":\"sample_dim\",\"outputType\":\"STRING\"},"
operator|+
literal|"\"metric\":{\"type\":\"LegacyTopNMetricSpec\",\"metric\":\"count\"},"
operator|+
literal|"\"threshold\":5,"
operator|+
literal|"\"intervals\":{\"type\":\"LegacySegmentSpec\",\"intervals\":[\"2013-08-31T07:00:00"
operator|+
literal|".000Z/2013-09-03T07:00:00.000Z\"]},"
operator|+
literal|"\"filter\":null,"
operator|+
literal|"\"granularity\":{\"type\":\"all\"},"
operator|+
literal|"\"aggregations\":[{\"type\":\"longSum\",\"name\":\"count\",\"fieldName\":\"count\",\"expression\":null},"
operator|+
literal|"{\"type\":\"doubleSum\",\"name\":\"some_metric\",\"fieldName\":\"some_metric\",\"expression\":null}],"
operator|+
literal|"\"postAggregations\":[],"
operator|+
literal|"\"context\":{\"queryId\":\"\"},"
operator|+
literal|"\"descending\":false}, [localhost:8082]}]"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GROUP_BY_QUERY
init|=
literal|"{  \"queryType\": \"groupBy\", "
operator|+
literal|" \"dataSource\": \"sample_datasource\", "
operator|+
literal|" \"granularity\": \"day\", "
operator|+
literal|" \"dimensions\": [\"country\", \"device\"], "
operator|+
literal|" \"limitSpec\": {"
operator|+
literal|" \"type\": \"default\","
operator|+
literal|" \"limit\": 5000,"
operator|+
literal|" \"columns\": [\"country\", \"data_transfer\"] }, "
operator|+
literal|" \"aggregations\": [  "
operator|+
literal|"  { \"type\": \"longSum\", \"name\": \"total_usage\", \"fieldName\": \"user_count\" },  "
operator|+
literal|"  { \"type\": \"doubleSum\", \"name\": \"data_transfer\", \"fieldName\": \"data_transfer\" } "
operator|+
literal|" ], "
operator|+
literal|" \"intervals\": [ \"2012-01-01T00:00:00.000-08:00/2012-01-03T00:00:00.000-08:00\" ]"
operator|+
literal|" }"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GROUP_BY_QUERY_SPLIT
init|=
literal|"[HiveDruidSplit{{\"queryType\":\"groupBy\","
operator|+
literal|"\"dataSource\":{\"type\":\"table\",\"name\":\"sample_datasource\"},"
operator|+
literal|"\"intervals\":{\"type\":\"LegacySegmentSpec\",\"intervals\":[\"2012-01-01T08:00:00"
operator|+
literal|".000Z/2012-01-03T08:00:00.000Z\"]},"
operator|+
literal|"\"virtualColumns\":[],"
operator|+
literal|"\"filter\":null,"
operator|+
literal|"\"granularity\":\"DAY\","
operator|+
literal|"\"dimensions\":[{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"country\",\"outputName\":\"country\","
operator|+
literal|"\"outputType\":\"STRING\"},"
operator|+
literal|"{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"device\",\"outputName\":\"device\","
operator|+
literal|"\"outputType\":\"STRING\"}],"
operator|+
literal|"\"aggregations\":[{\"type\":\"longSum\",\"name\":\"total_usage\",\"fieldName\":\"user_count\","
operator|+
literal|"\"expression\":null},"
operator|+
literal|"{\"type\":\"doubleSum\",\"name\":\"data_transfer\",\"fieldName\":\"data_transfer\",\"expression\":null}],"
operator|+
literal|"\"postAggregations\":[],"
operator|+
literal|"\"having\":null,"
operator|+
literal|"\"limitSpec\":{\"type\":\"default\",\"columns\":[{\"dimension\":\"country\",\"direction\":\"ascending\","
operator|+
literal|"\"dimensionOrder\":{\"type\":\"lexicographic\"}},"
operator|+
literal|"{\"dimension\":\"data_transfer\",\"direction\":\"ascending\","
operator|+
literal|"\"dimensionOrder\":{\"type\":\"lexicographic\"}}],\"limit\":5000},"
operator|+
literal|"\"context\":{\"queryId\":\"\"},"
operator|+
literal|"\"descending\":false}, [localhost:8082]}]"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SELECT_QUERY
init|=
literal|"{   \"queryType\": \"select\",  "
operator|+
literal|" \"dataSource\": \"wikipedia\",   \"descending\": \"false\",  "
operator|+
literal|" \"dimensions\":[\"robot\",\"namespace\",\"anonymous\",\"unpatrolled\",\"page\",\"language\","
operator|+
literal|"\"newpage\",\"user\"],  "
operator|+
literal|" \"metrics\":[\"count\",\"added\",\"delta\",\"variation\",\"deleted\"],  "
operator|+
literal|" \"granularity\": \"all\",  "
operator|+
literal|" \"intervals\": [     \"2013-01-01T00:00:00.000-08:00/2013-01-02T00:00:00.000-08:00\"   ],  "
operator|+
literal|" \"pagingSpec\":{\"pagingIdentifiers\": {}, \"threshold\":5}, "
operator|+
literal|" \"context\":{\"druid.query.fetch\":true}}"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SELECT_QUERY_SPLIT
init|=
literal|"[HiveDruidSplit{{\"queryType\":\"select\","
operator|+
literal|"\"dataSource\":{\"type\":\"table\",\"name\":\"wikipedia\"},"
operator|+
literal|"\"intervals\":{\"type\":\"LegacySegmentSpec\",\"intervals\":[\"2013-01-01T08:00:00"
operator|+
literal|".000Z/2013-01-02T08:00:00.000Z\"]},"
operator|+
literal|"\"descending\":false,"
operator|+
literal|"\"filter\":null,"
operator|+
literal|"\"granularity\":{\"type\":\"all\"},"
operator|+
literal|"\"dimensions\":[{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"robot\",\"outputName\":\"robot\","
operator|+
literal|"\"outputType\":\"STRING\"},"
operator|+
literal|"{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"namespace\",\"outputName\":\"namespace\","
operator|+
literal|"\"outputType\":\"STRING\"},"
operator|+
literal|"{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"anonymous\",\"outputName\":\"anonymous\","
operator|+
literal|"\"outputType\":\"STRING\"},"
operator|+
literal|"{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"unpatrolled\",\"outputName\":\"unpatrolled\","
operator|+
literal|"\"outputType\":\"STRING\"},"
operator|+
literal|"{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"page\",\"outputName\":\"page\","
operator|+
literal|"\"outputType\":\"STRING\"},"
operator|+
literal|"{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"language\",\"outputName\":\"language\","
operator|+
literal|"\"outputType\":\"STRING\"},"
operator|+
literal|"{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"newpage\",\"outputName\":\"newpage\","
operator|+
literal|"\"outputType\":\"STRING\"},"
operator|+
literal|"{\"type\":\"LegacyDimensionSpec\",\"dimension\":\"user\",\"outputName\":\"user\","
operator|+
literal|"\"outputType\":\"STRING\"}],"
operator|+
literal|"\"metrics\":[\"count\",\"added\",\"delta\",\"variation\",\"deleted\"],"
operator|+
literal|"\"virtualColumns\":[],"
operator|+
literal|"\"pagingSpec\":{\"pagingIdentifiers\":{},\"threshold\":5,\"fromNext\":false},"
operator|+
literal|"\"context\":{\"druid.query.fetch\":true,\"queryId\":\"\"}}, [localhost:8082]}]"
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testTimeZone
parameter_list|()
throws|throws
name|Exception
block|{
name|DruidQueryBasedInputFormat
name|input
init|=
operator|new
name|DruidQueryBasedInputFormat
argument_list|()
decl_stmt|;
name|Method
name|method1
init|=
name|DruidQueryBasedInputFormat
operator|.
name|class
operator|.
name|getDeclaredMethod
argument_list|(
literal|"getInputSplits"
argument_list|,
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|method1
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Create, initialize, and test
name|Configuration
name|conf
init|=
name|createPropertiesQuery
argument_list|(
literal|"sample_datasource"
argument_list|,
name|Query
operator|.
name|TIMESERIES
argument_list|,
name|TIMESERIES_QUERY
argument_list|)
decl_stmt|;
name|HiveDruidSplit
index|[]
name|resultSplits
init|=
operator|(
name|HiveDruidSplit
index|[]
operator|)
name|method1
operator|.
name|invoke
argument_list|(
name|input
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|TIMESERIES_QUERY_SPLIT
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|resultSplits
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|=
name|createPropertiesQuery
argument_list|(
literal|"sample_datasource"
argument_list|,
name|Query
operator|.
name|TOPN
argument_list|,
name|TOPN_QUERY
argument_list|)
expr_stmt|;
name|resultSplits
operator|=
operator|(
name|HiveDruidSplit
index|[]
operator|)
name|method1
operator|.
name|invoke
argument_list|(
name|input
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TOPN_QUERY_SPLIT
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|resultSplits
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|=
name|createPropertiesQuery
argument_list|(
literal|"sample_datasource"
argument_list|,
name|Query
operator|.
name|GROUP_BY
argument_list|,
name|GROUP_BY_QUERY
argument_list|)
expr_stmt|;
name|resultSplits
operator|=
operator|(
name|HiveDruidSplit
index|[]
operator|)
name|method1
operator|.
name|invoke
argument_list|(
name|input
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|GROUP_BY_QUERY_SPLIT
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|resultSplits
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|=
name|createPropertiesQuery
argument_list|(
literal|"sample_datasource"
argument_list|,
name|Query
operator|.
name|SELECT
argument_list|,
name|SELECT_QUERY
argument_list|)
expr_stmt|;
name|resultSplits
operator|=
operator|(
name|HiveDruidSplit
index|[]
operator|)
name|method1
operator|.
name|invoke
argument_list|(
name|input
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SELECT_QUERY_SPLIT
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|resultSplits
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Configuration
name|createPropertiesQuery
parameter_list|(
name|String
name|dataSource
parameter_list|,
name|String
name|queryType
parameter_list|,
name|String
name|jsonQuery
parameter_list|)
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
comment|// Set the configuration parameters
name|conf
operator|.
name|set
argument_list|(
name|FileInputFormat
operator|.
name|INPUT_DIR
argument_list|,
literal|"/my/dir"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_BROKER_DEFAULT_ADDRESS
operator|.
name|varname
argument_list|,
literal|"localhost:8082"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|Constants
operator|.
name|DRUID_DATA_SOURCE
argument_list|,
name|dataSource
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_JSON
argument_list|,
name|jsonQuery
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_TYPE
argument_list|,
name|queryType
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
block|}
end_class

end_unit

