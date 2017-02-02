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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

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
name|sql
operator|.
name|Timestamp
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
name|Iterator
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
name|Map
operator|.
name|Entry
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|druid
operator|.
name|serde
operator|.
name|DruidGroupByQueryRecordReader
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
name|DruidQueryRecordReader
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
name|DruidSelectQueryRecordReader
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
name|druid
operator|.
name|serde
operator|.
name|DruidTimeseriesQueryRecordReader
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
name|DruidTopNQueryRecordReader
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
name|DruidWritable
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
name|ql
operator|.
name|exec
operator|.
name|Utilities
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
name|io
operator|.
name|ByteWritable
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
name|io
operator|.
name|DoubleWritable
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
name|io
operator|.
name|HiveDecimalWritable
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
name|io
operator|.
name|ShortWritable
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
name|io
operator|.
name|TimestampWritable
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|io
operator|.
name|FloatWritable
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
name|IntWritable
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
name|LongWritable
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
name|NullWritable
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
name|Text
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
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonParseException
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
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|JsonMappingException
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
name|databind
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|Row
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|jackson
operator|.
name|DefaultObjectMapper
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
name|Query
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
name|Result
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
name|groupby
operator|.
name|GroupByQuery
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
name|select
operator|.
name|SelectQuery
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
name|select
operator|.
name|SelectResultValue
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
name|timeseries
operator|.
name|TimeseriesQuery
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
name|timeseries
operator|.
name|TimeseriesResultValue
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
name|topn
operator|.
name|TopNQuery
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
name|topn
operator|.
name|TopNResultValue
import|;
end_import

begin_comment
comment|/**  * Basic tests for Druid SerDe. The examples are taken from Druid 0.9.1.1  * documentation.  */
end_comment

begin_class
specifier|public
class|class
name|TestDruidSerDe
block|{
comment|// Timeseries query
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
literal|" \"granularity\": \"day\", "
operator|+
literal|" \"descending\": \"true\", "
operator|+
literal|" \"filter\": {  "
operator|+
literal|"  \"type\": \"and\",  "
operator|+
literal|"  \"fields\": [   "
operator|+
literal|"   { \"type\": \"selector\", \"dimension\": \"sample_dimension1\", \"value\": \"sample_value1\" },   "
operator|+
literal|"   { \"type\": \"or\",    "
operator|+
literal|"    \"fields\": [     "
operator|+
literal|"     { \"type\": \"selector\", \"dimension\": \"sample_dimension2\", \"value\": \"sample_value2\" },     "
operator|+
literal|"     { \"type\": \"selector\", \"dimension\": \"sample_dimension3\", \"value\": \"sample_value3\" }    "
operator|+
literal|"    ]   "
operator|+
literal|"   }  "
operator|+
literal|"  ] "
operator|+
literal|" }, "
operator|+
literal|" \"aggregations\": [  "
operator|+
literal|"  { \"type\": \"longSum\", \"name\": \"sample_name1\", \"fieldName\": \"sample_fieldName1\" },  "
operator|+
literal|"  { \"type\": \"doubleSum\", \"name\": \"sample_name2\", \"fieldName\": \"sample_fieldName2\" } "
operator|+
literal|" ], "
operator|+
literal|" \"postAggregations\": [  "
operator|+
literal|"  { \"type\": \"arithmetic\",  "
operator|+
literal|"    \"name\": \"sample_divide\",  "
operator|+
literal|"    \"fn\": \"/\",  "
operator|+
literal|"    \"fields\": [   "
operator|+
literal|"     { \"type\": \"fieldAccess\", \"name\": \"postAgg__sample_name1\", \"fieldName\": \"sample_name1\" },   "
operator|+
literal|"     { \"type\": \"fieldAccess\", \"name\": \"postAgg__sample_name2\", \"fieldName\": \"sample_name2\" }  "
operator|+
literal|"    ]  "
operator|+
literal|"  } "
operator|+
literal|" ], "
operator|+
literal|" \"intervals\": [ \"2012-01-01T00:00:00.000/2012-01-03T00:00:00.000\" ]}"
decl_stmt|;
comment|// Timeseries query results
specifier|private
specifier|static
specifier|final
name|String
name|TIMESERIES_QUERY_RESULTS
init|=
literal|"[  "
operator|+
literal|"{   "
operator|+
literal|" \"timestamp\": \"2012-01-01T00:00:00.000Z\",   "
operator|+
literal|" \"result\": { \"sample_name1\": 0, \"sample_name2\": 1.0, \"sample_divide\": 2.2222 }   "
operator|+
literal|"},  "
operator|+
literal|"{   "
operator|+
literal|" \"timestamp\": \"2012-01-02T00:00:00.000Z\",   "
operator|+
literal|" \"result\": { \"sample_name1\": 2, \"sample_name2\": 3.32, \"sample_divide\": 4 }  "
operator|+
literal|"}]"
decl_stmt|;
comment|// Timeseries query results as records
specifier|private
specifier|static
specifier|final
name|Object
index|[]
index|[]
name|TIMESERIES_QUERY_RESULTS_RECORDS
init|=
operator|new
name|Object
index|[]
index|[]
block|{
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1325376000000L
argument_list|)
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|0
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|1.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|2.2222F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1325462400000L
argument_list|)
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|2
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|3.32F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|4F
argument_list|)
block|}
block|}
decl_stmt|;
comment|// TopN query
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
literal|" \"granularity\": \"all\", "
operator|+
literal|" \"filter\": {  "
operator|+
literal|"  \"type\": \"and\",  "
operator|+
literal|"  \"fields\": [   "
operator|+
literal|"   {    "
operator|+
literal|"    \"type\": \"selector\",    "
operator|+
literal|"    \"dimension\": \"dim1\",    "
operator|+
literal|"    \"value\": \"some_value\"   "
operator|+
literal|"   },   "
operator|+
literal|"   {    "
operator|+
literal|"    \"type\": \"selector\",    "
operator|+
literal|"    \"dimension\": \"dim2\",    "
operator|+
literal|"    \"value\": \"some_other_val\"   "
operator|+
literal|"   }  "
operator|+
literal|"  ] "
operator|+
literal|" }, "
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
literal|" \"postAggregations\": [  "
operator|+
literal|"  {   "
operator|+
literal|"   \"type\": \"arithmetic\",   "
operator|+
literal|"   \"name\": \"sample_divide\",   "
operator|+
literal|"   \"fn\": \"/\",   "
operator|+
literal|"   \"fields\": [    "
operator|+
literal|"    {     "
operator|+
literal|"     \"type\": \"fieldAccess\",     "
operator|+
literal|"     \"name\": \"some_metric\",     "
operator|+
literal|"     \"fieldName\": \"some_metric\"    "
operator|+
literal|"    },    "
operator|+
literal|"    {     "
operator|+
literal|"     \"type\": \"fieldAccess\",     "
operator|+
literal|"     \"name\": \"count\",     "
operator|+
literal|"     \"fieldName\": \"count\"    "
operator|+
literal|"    }   "
operator|+
literal|"   ]  "
operator|+
literal|"  } "
operator|+
literal|" ], "
operator|+
literal|" \"intervals\": [  "
operator|+
literal|"  \"2013-08-31T00:00:00.000/2013-09-03T00:00:00.000\" "
operator|+
literal|" ]}"
decl_stmt|;
comment|// TopN query results
specifier|private
specifier|static
specifier|final
name|String
name|TOPN_QUERY_RESULTS
init|=
literal|"[ "
operator|+
literal|" {  "
operator|+
literal|"  \"timestamp\": \"2013-08-31T00:00:00.000Z\",  "
operator|+
literal|"  \"result\": [   "
operator|+
literal|"   {   "
operator|+
literal|"     \"sample_dim\": \"dim1_val\",   "
operator|+
literal|"     \"count\": 111,   "
operator|+
literal|"     \"some_metric\": 10669,   "
operator|+
literal|"     \"sample_divide\": 96.11711711711712   "
operator|+
literal|"   },   "
operator|+
literal|"   {   "
operator|+
literal|"     \"sample_dim\": \"another_dim1_val\",   "
operator|+
literal|"     \"count\": 88,   "
operator|+
literal|"     \"some_metric\": 28344,   "
operator|+
literal|"     \"sample_divide\": 322.09090909090907   "
operator|+
literal|"   },   "
operator|+
literal|"   {   "
operator|+
literal|"     \"sample_dim\": \"dim1_val3\",   "
operator|+
literal|"     \"count\": 70,   "
operator|+
literal|"     \"some_metric\": 871,   "
operator|+
literal|"     \"sample_divide\": 12.442857142857143   "
operator|+
literal|"   },   "
operator|+
literal|"   {   "
operator|+
literal|"     \"sample_dim\": \"dim1_val4\",   "
operator|+
literal|"     \"count\": 62,   "
operator|+
literal|"     \"some_metric\": 815,   "
operator|+
literal|"     \"sample_divide\": 13.14516129032258   "
operator|+
literal|"   },   "
operator|+
literal|"   {   "
operator|+
literal|"     \"sample_dim\": \"dim1_val5\",   "
operator|+
literal|"     \"count\": 60,   "
operator|+
literal|"     \"some_metric\": 2787,   "
operator|+
literal|"     \"sample_divide\": 46.45   "
operator|+
literal|"   }  "
operator|+
literal|"  ] "
operator|+
literal|" }]"
decl_stmt|;
comment|// TopN query results as records
specifier|private
specifier|static
specifier|final
name|Object
index|[]
index|[]
name|TOPN_QUERY_RESULTS_RECORDS
init|=
operator|new
name|Object
index|[]
index|[]
block|{
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1377907200000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"dim1_val"
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|111
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|10669F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|96.11711711711712F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1377907200000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"another_dim1_val"
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|88
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|28344F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|322.09090909090907F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1377907200000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"dim1_val3"
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|70
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|871F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|12.442857142857143F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1377907200000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"dim1_val4"
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|62
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|815F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|13.14516129032258F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1377907200000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"dim1_val5"
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|60
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|2787F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|46.45F
argument_list|)
block|}
block|}
decl_stmt|;
comment|// GroupBy query
specifier|private
specifier|static
specifier|final
name|String
name|GROUP_BY_QUERY
init|=
literal|"{ "
operator|+
literal|" \"queryType\": \"groupBy\", "
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
literal|" \"filter\": {  "
operator|+
literal|"  \"type\": \"and\",  "
operator|+
literal|"  \"fields\": [   "
operator|+
literal|"   { \"type\": \"selector\", \"dimension\": \"carrier\", \"value\": \"AT&T\" },   "
operator|+
literal|"   { \"type\": \"or\",     "
operator|+
literal|"    \"fields\": [     "
operator|+
literal|"     { \"type\": \"selector\", \"dimension\": \"make\", \"value\": \"Apple\" },     "
operator|+
literal|"     { \"type\": \"selector\", \"dimension\": \"make\", \"value\": \"Samsung\" }    "
operator|+
literal|"    ]   "
operator|+
literal|"   }  "
operator|+
literal|"  ] "
operator|+
literal|" }, "
operator|+
literal|" \"aggregations\": [  "
operator|+
literal|"  { \"type\": \"longSum\", \"name\": \"total_usage\", \"fieldName\": \"user_count\" },  "
operator|+
literal|"  { \"type\": \"doubleSum\", \"name\": \"data_transfer\", \"fieldName\": \"data_transfer\" } "
operator|+
literal|" ], "
operator|+
literal|" \"postAggregations\": [  "
operator|+
literal|"  { \"type\": \"arithmetic\",  "
operator|+
literal|"    \"name\": \"avg_usage\",  "
operator|+
literal|"    \"fn\": \"/\",  "
operator|+
literal|"    \"fields\": [   "
operator|+
literal|"     { \"type\": \"fieldAccess\", \"fieldName\": \"data_transfer\" },   "
operator|+
literal|"     { \"type\": \"fieldAccess\", \"fieldName\": \"total_usage\" }  "
operator|+
literal|"    ]  "
operator|+
literal|"  } "
operator|+
literal|" ], "
operator|+
literal|" \"intervals\": [ \"2012-01-01T00:00:00.000/2012-01-03T00:00:00.000\" ], "
operator|+
literal|" \"having\": {  "
operator|+
literal|"  \"type\": \"greaterThan\",  "
operator|+
literal|"  \"aggregation\": \"total_usage\",  "
operator|+
literal|"  \"value\": 100 "
operator|+
literal|" }}"
decl_stmt|;
comment|// GroupBy query results
specifier|private
specifier|static
specifier|final
name|String
name|GROUP_BY_QUERY_RESULTS
init|=
literal|"[  "
operator|+
literal|" {  "
operator|+
literal|"  \"version\" : \"v1\",  "
operator|+
literal|"  \"timestamp\" : \"2012-01-01T00:00:00.000Z\",  "
operator|+
literal|"  \"event\" : {   "
operator|+
literal|"   \"country\" : \"India\",   "
operator|+
literal|"   \"device\" : \"phone\",   "
operator|+
literal|"   \"total_usage\" : 88,   "
operator|+
literal|"   \"data_transfer\" : 29.91233453,   "
operator|+
literal|"   \"avg_usage\" : 60.32  "
operator|+
literal|"  } "
operator|+
literal|" },  "
operator|+
literal|" {  "
operator|+
literal|"  \"version\" : \"v1\",  "
operator|+
literal|"  \"timestamp\" : \"2012-01-01T00:00:12.000Z\",  "
operator|+
literal|"  \"event\" : {   "
operator|+
literal|"   \"country\" : \"Spain\",   "
operator|+
literal|"   \"device\" : \"pc\",   "
operator|+
literal|"   \"total_usage\" : 16,   "
operator|+
literal|"   \"data_transfer\" : 172.93494959,   "
operator|+
literal|"   \"avg_usage\" : 6.333333  "
operator|+
literal|"  } "
operator|+
literal|" }]"
decl_stmt|;
comment|// GroupBy query results as records
specifier|private
specifier|static
specifier|final
name|Object
index|[]
index|[]
name|GROUP_BY_QUERY_RESULTS_RECORDS
init|=
operator|new
name|Object
index|[]
index|[]
block|{
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1325376000000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"India"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"phone"
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|88
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|29.91233453F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|60.32F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1325376012000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"Spain"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"pc"
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|16
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|172.93494959F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|6.333333F
argument_list|)
block|}
block|}
decl_stmt|;
comment|// Select query
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
literal|" \"dimensions\":[\"robot\",\"namespace\",\"anonymous\",\"unpatrolled\",\"page\",\"language\",\"newpage\",\"user\"],  "
operator|+
literal|" \"metrics\":[\"count\",\"added\",\"delta\",\"variation\",\"deleted\"],  "
operator|+
literal|" \"granularity\": \"all\",  "
operator|+
literal|" \"intervals\": [     \"2013-01-01/2013-01-02\"   ],  "
operator|+
literal|" \"pagingSpec\":{\"pagingIdentifiers\": {}, \"threshold\":5} }"
decl_stmt|;
comment|// Select query results
specifier|private
specifier|static
specifier|final
name|String
name|SELECT_QUERY_RESULTS
init|=
literal|"[{ "
operator|+
literal|" \"timestamp\" : \"2013-01-01T00:00:00.000Z\", "
operator|+
literal|" \"result\" : {  "
operator|+
literal|"  \"pagingIdentifiers\" : {   "
operator|+
literal|"   \"wikipedia_2012-12-29T00:00:00.000Z_2013-01-10T08:00:00.000Z_2013-01-10T08:13:47.830Z_v9\" : 4    }, "
operator|+
literal|"   \"events\" : [ {  "
operator|+
literal|"    \"segmentId\" : \"wikipedia_editstream_2012-12-29T00:00:00.000Z_2013-01-10T08:00:00.000Z_2013-01-10T08:13:47.830Z_v9\",  "
operator|+
literal|"    \"offset\" : 0,  "
operator|+
literal|"    \"event\" : {   "
operator|+
literal|"     \"timestamp\" : \"2013-01-01T00:00:00.000Z\",   "
operator|+
literal|"     \"robot\" : \"1\",   "
operator|+
literal|"     \"namespace\" : \"article\",   "
operator|+
literal|"     \"anonymous\" : \"0\",   "
operator|+
literal|"     \"unpatrolled\" : \"0\",   "
operator|+
literal|"     \"page\" : \"11._korpus_(NOVJ)\",   "
operator|+
literal|"     \"language\" : \"sl\",   "
operator|+
literal|"     \"newpage\" : \"0\",   "
operator|+
literal|"     \"user\" : \"EmausBot\",   "
operator|+
literal|"     \"count\" : 1.0,   "
operator|+
literal|"     \"added\" : 39.0,   "
operator|+
literal|"     \"delta\" : 39.0,   "
operator|+
literal|"     \"variation\" : 39.0,   "
operator|+
literal|"     \"deleted\" : 0.0  "
operator|+
literal|"    } "
operator|+
literal|"   }, {  "
operator|+
literal|"    \"segmentId\" : \"wikipedia_2012-12-29T00:00:00.000Z_2013-01-10T08:00:00.000Z_2013-01-10T08:13:47.830Z_v9\",  "
operator|+
literal|"    \"offset\" : 1,  "
operator|+
literal|"    \"event\" : {   "
operator|+
literal|"     \"timestamp\" : \"2013-01-01T00:00:00.000Z\",   "
operator|+
literal|"     \"robot\" : \"0\",   "
operator|+
literal|"     \"namespace\" : \"article\",   "
operator|+
literal|"     \"anonymous\" : \"0\",   "
operator|+
literal|"     \"unpatrolled\" : \"0\",   "
operator|+
literal|"     \"page\" : \"112_U.S._580\",   "
operator|+
literal|"     \"language\" : \"en\",   "
operator|+
literal|"     \"newpage\" : \"1\",   "
operator|+
literal|"     \"user\" : \"MZMcBride\",   "
operator|+
literal|"     \"count\" : 1.0,   "
operator|+
literal|"     \"added\" : 70.0,   "
operator|+
literal|"     \"delta\" : 70.0,   "
operator|+
literal|"     \"variation\" : 70.0,   "
operator|+
literal|"     \"deleted\" : 0.0  "
operator|+
literal|"    } "
operator|+
literal|"   }, {  "
operator|+
literal|"    \"segmentId\" : \"wikipedia_2012-12-29T00:00:00.000Z_2013-01-10T08:00:00.000Z_2013-01-10T08:13:47.830Z_v9\",  "
operator|+
literal|"    \"offset\" : 2,  "
operator|+
literal|"    \"event\" : {   "
operator|+
literal|"     \"timestamp\" : \"2013-01-01T00:00:12.000Z\",   "
operator|+
literal|"     \"robot\" : \"0\",   "
operator|+
literal|"     \"namespace\" : \"article\",   "
operator|+
literal|"     \"anonymous\" : \"0\",   "
operator|+
literal|"     \"unpatrolled\" : \"0\",   "
operator|+
literal|"     \"page\" : \"113_U.S._243\",   "
operator|+
literal|"     \"language\" : \"en\",   "
operator|+
literal|"     \"newpage\" : \"1\",   "
operator|+
literal|"     \"user\" : \"MZMcBride\",   "
operator|+
literal|"     \"count\" : 1.0,   "
operator|+
literal|"     \"added\" : 77.0,   "
operator|+
literal|"     \"delta\" : 77.0,   "
operator|+
literal|"     \"variation\" : 77.0,   "
operator|+
literal|"     \"deleted\" : 0.0  "
operator|+
literal|"    } "
operator|+
literal|"   }, {  "
operator|+
literal|"    \"segmentId\" : \"wikipedia_2012-12-29T00:00:00.000Z_2013-01-10T08:00:00.000Z_2013-01-10T08:13:47.830Z_v9\",  "
operator|+
literal|"    \"offset\" : 3,  "
operator|+
literal|"    \"event\" : {   "
operator|+
literal|"     \"timestamp\" : \"2013-01-01T00:00:12.000Z\",   "
operator|+
literal|"     \"robot\" : \"0\",   "
operator|+
literal|"     \"namespace\" : \"article\",   "
operator|+
literal|"     \"anonymous\" : \"0\",   "
operator|+
literal|"     \"unpatrolled\" : \"0\",   "
operator|+
literal|"     \"page\" : \"113_U.S._73\",   "
operator|+
literal|"     \"language\" : \"en\",   "
operator|+
literal|"     \"newpage\" : \"1\",   "
operator|+
literal|"     \"user\" : \"MZMcBride\",   "
operator|+
literal|"     \"count\" : 1.0,   "
operator|+
literal|"     \"added\" : 70.0,   "
operator|+
literal|"     \"delta\" : 70.0,   "
operator|+
literal|"     \"variation\" : 70.0,   "
operator|+
literal|"     \"deleted\" : 0.0  "
operator|+
literal|"    } "
operator|+
literal|"   }, {  "
operator|+
literal|"    \"segmentId\" : \"wikipedia_2012-12-29T00:00:00.000Z_2013-01-10T08:00:00.000Z_2013-01-10T08:13:47.830Z_v9\",  "
operator|+
literal|"    \"offset\" : 4,  "
operator|+
literal|"    \"event\" : {   "
operator|+
literal|"     \"timestamp\" : \"2013-01-01T00:00:12.000Z\",   "
operator|+
literal|"     \"robot\" : \"0\",   "
operator|+
literal|"     \"namespace\" : \"article\",   "
operator|+
literal|"     \"anonymous\" : \"0\",   "
operator|+
literal|"     \"unpatrolled\" : \"0\",   "
operator|+
literal|"     \"page\" : \"113_U.S._756\",   "
operator|+
literal|"     \"language\" : \"en\",   "
operator|+
literal|"     \"newpage\" : \"1\",   "
operator|+
literal|"     \"user\" : \"MZMcBride\",   "
operator|+
literal|"     \"count\" : 1.0,   "
operator|+
literal|"     \"added\" : 68.0,   "
operator|+
literal|"     \"delta\" : 68.0,   "
operator|+
literal|"     \"variation\" : 68.0,   "
operator|+
literal|"     \"deleted\" : 0.0  "
operator|+
literal|"    } "
operator|+
literal|"   } ]  }} ]"
decl_stmt|;
comment|// Select query results as records
specifier|private
specifier|static
specifier|final
name|Object
index|[]
index|[]
name|SELECT_QUERY_RESULTS_RECORDS
init|=
operator|new
name|Object
index|[]
index|[]
block|{
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1356998400000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"1"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"article"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"11._korpus_(NOVJ)"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"sl"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"EmausBot"
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|1.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|39.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|39.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|39.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|0.0F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1356998400000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"article"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"112_U.S._580"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"en"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"1"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"MZMcBride"
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|1.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|70.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|70.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|70.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|0.0F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1356998412000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"article"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"113_U.S._243"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"en"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"1"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"MZMcBride"
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|1.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|77.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|77.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|77.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|0.0F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1356998412000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"article"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"113_U.S._73"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"en"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"1"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"MZMcBride"
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|1.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|70.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|70.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|70.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|0.0F
argument_list|)
block|}
block|,
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1356998412000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"article"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"113_U.S._756"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"en"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"1"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"MZMcBride"
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|1.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|68.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|68.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|68.0F
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|0.0F
argument_list|)
block|}
block|}
decl_stmt|;
comment|/**    * Test the default behavior of the objects and object inspectors.    * @throws IOException    * @throws IllegalAccessException    * @throws IllegalArgumentException    * @throws SecurityException    * @throws NoSuchFieldException    * @throws JsonMappingException    * @throws JsonParseException    * @throws InvocationTargetException    * @throws NoSuchMethodException    */
annotation|@
name|Test
specifier|public
name|void
name|testDruidDeserializer
parameter_list|()
throws|throws
name|SerDeException
throws|,
name|JsonParseException
throws|,
name|JsonMappingException
throws|,
name|NoSuchFieldException
throws|,
name|SecurityException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
throws|,
name|IOException
throws|,
name|InterruptedException
throws|,
name|NoSuchMethodException
throws|,
name|InvocationTargetException
block|{
comment|// Create, initialize, and test the SerDe
name|DruidSerDe
name|serDe
init|=
operator|new
name|DruidSerDe
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|tbl
decl_stmt|;
comment|// Timeseries query
name|tbl
operator|=
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
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serDe
argument_list|,
name|conf
argument_list|,
name|tbl
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|deserializeQueryResults
argument_list|(
name|serDe
argument_list|,
name|Query
operator|.
name|TIMESERIES
argument_list|,
name|TIMESERIES_QUERY
argument_list|,
name|TIMESERIES_QUERY_RESULTS
argument_list|,
name|TIMESERIES_QUERY_RESULTS_RECORDS
argument_list|)
expr_stmt|;
comment|// TopN query
name|tbl
operator|=
name|createPropertiesQuery
argument_list|(
literal|"sample_data"
argument_list|,
name|Query
operator|.
name|TOPN
argument_list|,
name|TOPN_QUERY
argument_list|)
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serDe
argument_list|,
name|conf
argument_list|,
name|tbl
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|deserializeQueryResults
argument_list|(
name|serDe
argument_list|,
name|Query
operator|.
name|TOPN
argument_list|,
name|TOPN_QUERY
argument_list|,
name|TOPN_QUERY_RESULTS
argument_list|,
name|TOPN_QUERY_RESULTS_RECORDS
argument_list|)
expr_stmt|;
comment|// GroupBy query
name|tbl
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
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serDe
argument_list|,
name|conf
argument_list|,
name|tbl
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|deserializeQueryResults
argument_list|(
name|serDe
argument_list|,
name|Query
operator|.
name|GROUP_BY
argument_list|,
name|GROUP_BY_QUERY
argument_list|,
name|GROUP_BY_QUERY_RESULTS
argument_list|,
name|GROUP_BY_QUERY_RESULTS_RECORDS
argument_list|)
expr_stmt|;
comment|// Select query
name|tbl
operator|=
name|createPropertiesQuery
argument_list|(
literal|"wikipedia"
argument_list|,
name|Query
operator|.
name|SELECT
argument_list|,
name|SELECT_QUERY
argument_list|)
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serDe
argument_list|,
name|conf
argument_list|,
name|tbl
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|deserializeQueryResults
argument_list|(
name|serDe
argument_list|,
name|Query
operator|.
name|SELECT
argument_list|,
name|SELECT_QUERY
argument_list|,
name|SELECT_QUERY_RESULTS
argument_list|,
name|SELECT_QUERY_RESULTS_RECORDS
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Properties
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
name|Properties
name|tbl
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// Set the configuration parameters
name|tbl
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|DRUID_DATA_SOURCE
argument_list|,
name|dataSource
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_JSON
argument_list|,
name|jsonQuery
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|DRUID_QUERY_TYPE
argument_list|,
name|queryType
argument_list|)
expr_stmt|;
return|return
name|tbl
return|;
block|}
specifier|private
specifier|static
name|void
name|deserializeQueryResults
parameter_list|(
name|DruidSerDe
name|serDe
parameter_list|,
name|String
name|queryType
parameter_list|,
name|String
name|jsonQuery
parameter_list|,
name|String
name|resultString
parameter_list|,
name|Object
index|[]
index|[]
name|records
parameter_list|)
throws|throws
name|SerDeException
throws|,
name|JsonParseException
throws|,
name|JsonMappingException
throws|,
name|IOException
throws|,
name|NoSuchFieldException
throws|,
name|SecurityException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
throws|,
name|InterruptedException
throws|,
name|NoSuchMethodException
throws|,
name|InvocationTargetException
block|{
comment|// Initialize
name|Query
argument_list|<
name|?
argument_list|>
name|query
init|=
literal|null
decl_stmt|;
name|DruidQueryRecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|reader
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|resultsList
init|=
literal|null
decl_stmt|;
name|ObjectMapper
name|mapper
init|=
operator|new
name|DefaultObjectMapper
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|queryType
condition|)
block|{
case|case
name|Query
operator|.
name|TIMESERIES
case|:
name|query
operator|=
name|mapper
operator|.
name|readValue
argument_list|(
name|jsonQuery
argument_list|,
name|TimeseriesQuery
operator|.
name|class
argument_list|)
expr_stmt|;
name|reader
operator|=
operator|new
name|DruidTimeseriesQueryRecordReader
argument_list|()
expr_stmt|;
name|resultsList
operator|=
name|mapper
operator|.
name|readValue
argument_list|(
name|resultString
argument_list|,
operator|new
name|TypeReference
argument_list|<
name|List
argument_list|<
name|Result
argument_list|<
name|TimeseriesResultValue
argument_list|>
argument_list|>
argument_list|>
argument_list|()
block|{                 }
argument_list|)
expr_stmt|;
break|break;
case|case
name|Query
operator|.
name|TOPN
case|:
name|query
operator|=
name|mapper
operator|.
name|readValue
argument_list|(
name|jsonQuery
argument_list|,
name|TopNQuery
operator|.
name|class
argument_list|)
expr_stmt|;
name|reader
operator|=
operator|new
name|DruidTopNQueryRecordReader
argument_list|()
expr_stmt|;
name|resultsList
operator|=
name|mapper
operator|.
name|readValue
argument_list|(
name|resultString
argument_list|,
operator|new
name|TypeReference
argument_list|<
name|List
argument_list|<
name|Result
argument_list|<
name|TopNResultValue
argument_list|>
argument_list|>
argument_list|>
argument_list|()
block|{                 }
argument_list|)
expr_stmt|;
break|break;
case|case
name|Query
operator|.
name|GROUP_BY
case|:
name|query
operator|=
name|mapper
operator|.
name|readValue
argument_list|(
name|jsonQuery
argument_list|,
name|GroupByQuery
operator|.
name|class
argument_list|)
expr_stmt|;
name|reader
operator|=
operator|new
name|DruidGroupByQueryRecordReader
argument_list|()
expr_stmt|;
name|resultsList
operator|=
name|mapper
operator|.
name|readValue
argument_list|(
name|resultString
argument_list|,
operator|new
name|TypeReference
argument_list|<
name|List
argument_list|<
name|Row
argument_list|>
argument_list|>
argument_list|()
block|{                 }
argument_list|)
expr_stmt|;
break|break;
case|case
name|Query
operator|.
name|SELECT
case|:
name|query
operator|=
name|mapper
operator|.
name|readValue
argument_list|(
name|jsonQuery
argument_list|,
name|SelectQuery
operator|.
name|class
argument_list|)
expr_stmt|;
name|reader
operator|=
operator|new
name|DruidSelectQueryRecordReader
argument_list|()
expr_stmt|;
name|resultsList
operator|=
name|mapper
operator|.
name|readValue
argument_list|(
name|resultString
argument_list|,
operator|new
name|TypeReference
argument_list|<
name|List
argument_list|<
name|Result
argument_list|<
name|SelectResultValue
argument_list|>
argument_list|>
argument_list|>
argument_list|()
block|{                 }
argument_list|)
expr_stmt|;
break|break;
block|}
comment|// Set query and fields access
name|Field
name|field1
init|=
name|DruidQueryRecordReader
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"query"
argument_list|)
decl_stmt|;
name|field1
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|field1
operator|.
name|set
argument_list|(
name|reader
argument_list|,
name|query
argument_list|)
expr_stmt|;
if|if
condition|(
name|reader
operator|instanceof
name|DruidGroupByQueryRecordReader
condition|)
block|{
name|Method
name|method1
init|=
name|DruidGroupByQueryRecordReader
operator|.
name|class
operator|.
name|getDeclaredMethod
argument_list|(
literal|"initExtractors"
argument_list|)
decl_stmt|;
name|method1
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|method1
operator|.
name|invoke
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
name|Field
name|field2
init|=
name|DruidQueryRecordReader
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"results"
argument_list|)
decl_stmt|;
name|field2
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Get the row structure
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|serDe
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|oi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
comment|// Check mapred
name|Iterator
argument_list|<
name|?
argument_list|>
name|results
init|=
name|resultsList
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|field2
operator|.
name|set
argument_list|(
name|reader
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|DruidWritable
name|writable
init|=
operator|new
name|DruidWritable
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|NullWritable
operator|.
name|get
argument_list|()
argument_list|,
name|writable
argument_list|)
condition|)
block|{
name|Object
name|row
init|=
name|serDe
operator|.
name|deserialize
argument_list|(
name|writable
argument_list|)
decl_stmt|;
name|Object
index|[]
name|expectedFieldsData
init|=
name|records
index|[
name|pos
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedFieldsData
operator|.
name|length
argument_list|,
name|fieldRefs
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
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|fieldData
init|=
name|oi
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Field "
operator|+
name|i
argument_list|,
name|expectedFieldsData
index|[
name|i
index|]
argument_list|,
name|fieldData
argument_list|)
expr_stmt|;
block|}
name|pos
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|pos
argument_list|,
name|records
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Check mapreduce
name|results
operator|=
name|resultsList
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|field2
operator|.
name|set
argument_list|(
name|reader
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|pos
operator|=
literal|0
expr_stmt|;
while|while
condition|(
name|reader
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
name|Object
name|row
init|=
name|serDe
operator|.
name|deserialize
argument_list|(
name|reader
operator|.
name|getCurrentValue
argument_list|()
argument_list|)
decl_stmt|;
name|Object
index|[]
name|expectedFieldsData
init|=
name|records
index|[
name|pos
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedFieldsData
operator|.
name|length
argument_list|,
name|fieldRefs
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
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|fieldData
init|=
name|oi
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Field "
operator|+
name|i
argument_list|,
name|expectedFieldsData
index|[
name|i
index|]
argument_list|,
name|fieldData
argument_list|)
expr_stmt|;
block|}
name|pos
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|pos
argument_list|,
name|records
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_NAMES
init|=
literal|"__time,c0,c1,c2,c3,c4,c5,c6,c7"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_TYPES
init|=
literal|"timestamp,string,double,float,decimal(38,18),bigint,int,smallint,tinyint"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Object
index|[]
name|ROW_OBJECT
init|=
operator|new
name|Object
index|[]
block|{
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1377907200000L
argument_list|)
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"dim1_val"
argument_list|)
block|,
operator|new
name|DoubleWritable
argument_list|(
literal|10669.3D
argument_list|)
block|,
operator|new
name|FloatWritable
argument_list|(
literal|10669.45F
argument_list|)
block|,
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|1064.34D
argument_list|)
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|1113939
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|1112123
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|12
argument_list|)
block|,
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|,
operator|new
name|TimestampWritable
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1377907200000L
argument_list|)
argument_list|)
comment|// granularity
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|DruidWritable
name|DRUID_WRITABLE
init|=
operator|new
name|DruidWritable
argument_list|(
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"__time"
argument_list|,
literal|1377907200000L
argument_list|)
operator|.
name|put
argument_list|(
literal|"c0"
argument_list|,
literal|"dim1_val"
argument_list|)
operator|.
name|put
argument_list|(
literal|"c1"
argument_list|,
literal|10669.3D
argument_list|)
operator|.
name|put
argument_list|(
literal|"c2"
argument_list|,
literal|10669.45F
argument_list|)
operator|.
name|put
argument_list|(
literal|"c3"
argument_list|,
literal|1064.34D
argument_list|)
operator|.
name|put
argument_list|(
literal|"c4"
argument_list|,
literal|1113939L
argument_list|)
operator|.
name|put
argument_list|(
literal|"c5"
argument_list|,
literal|1112123
argument_list|)
operator|.
name|put
argument_list|(
literal|"c6"
argument_list|,
operator|(
name|short
operator|)
literal|12
argument_list|)
operator|.
name|put
argument_list|(
literal|"c7"
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
operator|.
name|put
argument_list|(
literal|"__time_granularity"
argument_list|,
literal|1377907200000L
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Test the default behavior of the objects and object inspectors.    * @throws IOException    * @throws IllegalAccessException    * @throws IllegalArgumentException    * @throws SecurityException    * @throws NoSuchFieldException    * @throws JsonMappingException    * @throws JsonParseException    * @throws InvocationTargetException    * @throws NoSuchMethodException    */
annotation|@
name|Test
specifier|public
name|void
name|testDruidSerializer
parameter_list|()
throws|throws
name|SerDeException
throws|,
name|JsonParseException
throws|,
name|JsonMappingException
throws|,
name|NoSuchFieldException
throws|,
name|SecurityException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
throws|,
name|IOException
throws|,
name|InterruptedException
throws|,
name|NoSuchMethodException
throws|,
name|InvocationTargetException
block|{
comment|// Create, initialize, and test the SerDe
name|DruidSerDe
name|serDe
init|=
operator|new
name|DruidSerDe
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|tbl
decl_stmt|;
comment|// Mixed source (all types)
name|tbl
operator|=
name|createPropertiesSource
argument_list|(
name|COLUMN_NAMES
argument_list|,
name|COLUMN_TYPES
argument_list|)
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serDe
argument_list|,
name|conf
argument_list|,
name|tbl
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|serializeObject
argument_list|(
name|tbl
argument_list|,
name|serDe
argument_list|,
name|ROW_OBJECT
argument_list|,
name|DRUID_WRITABLE
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Properties
name|createPropertiesSource
parameter_list|(
name|String
name|columnNames
parameter_list|,
name|String
name|columnTypes
parameter_list|)
block|{
name|Properties
name|tbl
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// Set the configuration parameters
name|tbl
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|columnNames
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|columnTypes
argument_list|)
expr_stmt|;
return|return
name|tbl
return|;
block|}
specifier|private
specifier|static
name|void
name|serializeObject
parameter_list|(
name|Properties
name|properties
parameter_list|,
name|DruidSerDe
name|serDe
parameter_list|,
name|Object
index|[]
name|rowObject
parameter_list|,
name|DruidWritable
name|druidWritable
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Build OI with timestamp granularity column
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|PrimitiveTypeInfo
argument_list|>
name|columnTypes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|inspectors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|columnNames
operator|.
name|addAll
argument_list|(
name|Utilities
operator|.
name|getColumnNames
argument_list|(
name|properties
argument_list|)
argument_list|)
expr_stmt|;
name|columnNames
operator|.
name|add
argument_list|(
name|Constants
operator|.
name|DRUID_TIMESTAMP_GRANULARITY_COL_NAME
argument_list|)
expr_stmt|;
name|columnTypes
operator|.
name|addAll
argument_list|(
name|Lists
operator|.
name|transform
argument_list|(
name|Utilities
operator|.
name|getColumnTypes
argument_list|(
name|properties
argument_list|)
argument_list|,
operator|new
name|Function
argument_list|<
name|String
argument_list|,
name|PrimitiveTypeInfo
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|PrimitiveTypeInfo
name|apply
parameter_list|(
name|String
name|type
parameter_list|)
block|{
return|return
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|type
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|columnTypes
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"timestamp"
argument_list|)
argument_list|)
expr_stmt|;
name|inspectors
operator|.
name|addAll
argument_list|(
name|Lists
operator|.
name|transform
argument_list|(
name|columnTypes
argument_list|,
operator|new
name|Function
argument_list|<
name|PrimitiveTypeInfo
argument_list|,
name|ObjectInspector
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|apply
parameter_list|(
name|PrimitiveTypeInfo
name|type
parameter_list|)
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|type
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|inspectors
argument_list|)
decl_stmt|;
comment|// Serialize
name|DruidWritable
name|writable
init|=
operator|(
name|DruidWritable
operator|)
name|serDe
operator|.
name|serialize
argument_list|(
name|rowObject
argument_list|,
name|inspector
argument_list|)
decl_stmt|;
comment|// Check result
name|assertEquals
argument_list|(
name|DRUID_WRITABLE
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|writable
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|e
range|:
name|DRUID_WRITABLE
operator|.
name|getValue
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|writable
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

