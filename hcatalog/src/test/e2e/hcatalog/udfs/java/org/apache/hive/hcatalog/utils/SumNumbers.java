begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|utils
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|Text
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
name|Writable
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
name|WritableComparable
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
name|Job
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
name|Mapper
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
name|Reducer
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
name|output
operator|.
name|FileOutputFormat
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
name|output
operator|.
name|TextOutputFormat
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
name|GenericOptionsParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|InputJobInfo
import|;
end_import

begin_comment
comment|/**  * This is a map reduce test for testing hcat which goes against the "numbers"  * table. It performs a group by on the first column and a SUM operation on the  * other columns. This is to simulate a typical operation in a map reduce program  * to test that hcat hands the right data to the map reduce program  *  * Usage: hadoop jar sumnumbers<serveruri><output dir><-libjars hive-hcat jar>  The<tab|ctrla> argument controls the output delimiter  The hcat jar location should be specified as file://<full path to jar>  */
end_comment

begin_class
specifier|public
class|class
name|SumNumbers
block|{
specifier|private
specifier|static
specifier|final
name|String
name|NUMBERS_TABLE_NAME
init|=
literal|"numbers"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TAB
init|=
literal|"\t"
decl_stmt|;
specifier|public
specifier|static
class|class
name|SumMapper
extends|extends
name|Mapper
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|,
name|IntWritable
argument_list|,
name|SumNumbers
operator|.
name|ArrayWritable
argument_list|>
block|{
name|IntWritable
name|intnum1000
decl_stmt|;
comment|// though id is given as a Short by hcat, the map will emit it as an
comment|// IntWritable so we can just sum in the reduce
name|IntWritable
name|id
decl_stmt|;
comment|// though intnum5 is handed as a Byte by hcat, the map() will emit it as
comment|// an IntWritable so we can just sum in the reduce
name|IntWritable
name|intnum5
decl_stmt|;
name|IntWritable
name|intnum100
decl_stmt|;
name|IntWritable
name|intnum
decl_stmt|;
name|LongWritable
name|longnum
decl_stmt|;
name|FloatWritable
name|floatnum
decl_stmt|;
name|DoubleWritable
name|doublenum
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|map
parameter_list|(
name|WritableComparable
name|key
parameter_list|,
name|HCatRecord
name|value
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|Mapper
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|,
name|IntWritable
argument_list|,
name|SumNumbers
operator|.
name|ArrayWritable
argument_list|>
operator|.
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|intnum1000
operator|=
operator|new
name|IntWritable
argument_list|(
operator|(
name|Integer
operator|)
name|value
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|id
operator|=
operator|new
name|IntWritable
argument_list|(
operator|(
name|Short
operator|)
name|value
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|intnum5
operator|=
operator|new
name|IntWritable
argument_list|(
operator|(
operator|(
name|Byte
operator|)
name|value
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|)
argument_list|)
expr_stmt|;
name|intnum100
operator|=
operator|new
name|IntWritable
argument_list|(
operator|(
operator|(
name|Integer
operator|)
name|value
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|)
argument_list|)
expr_stmt|;
name|intnum
operator|=
operator|new
name|IntWritable
argument_list|(
operator|(
name|Integer
operator|)
name|value
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|longnum
operator|=
operator|new
name|LongWritable
argument_list|(
operator|(
name|Long
operator|)
name|value
operator|.
name|get
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|floatnum
operator|=
operator|new
name|FloatWritable
argument_list|(
operator|(
name|Float
operator|)
name|value
operator|.
name|get
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|doublenum
operator|=
operator|new
name|DoubleWritable
argument_list|(
operator|(
name|Double
operator|)
name|value
operator|.
name|get
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|SumNumbers
operator|.
name|ArrayWritable
name|outputValue
init|=
operator|new
name|SumNumbers
operator|.
name|ArrayWritable
argument_list|(
name|id
argument_list|,
name|intnum5
argument_list|,
name|intnum100
argument_list|,
name|intnum
argument_list|,
name|longnum
argument_list|,
name|floatnum
argument_list|,
name|doublenum
argument_list|)
decl_stmt|;
name|context
operator|.
name|write
argument_list|(
name|intnum1000
argument_list|,
name|outputValue
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|SumReducer
extends|extends
name|Reducer
argument_list|<
name|IntWritable
argument_list|,
name|SumNumbers
operator|.
name|ArrayWritable
argument_list|,
name|LongWritable
argument_list|,
name|Text
argument_list|>
block|{
name|LongWritable
name|dummyLong
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|reduce
parameter_list|(
name|IntWritable
name|key
parameter_list|,
name|java
operator|.
name|lang
operator|.
name|Iterable
argument_list|<
name|ArrayWritable
argument_list|>
name|values
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|Reducer
argument_list|<
name|IntWritable
argument_list|,
name|ArrayWritable
argument_list|,
name|LongWritable
argument_list|,
name|Text
argument_list|>
operator|.
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|output
init|=
name|key
operator|.
name|toString
argument_list|()
operator|+
name|TAB
decl_stmt|;
name|Long
name|sumid
init|=
literal|0l
decl_stmt|;
name|Long
name|sumintnum5
init|=
literal|0l
decl_stmt|;
name|Long
name|sumintnum100
init|=
literal|0l
decl_stmt|;
name|Long
name|sumintnum
init|=
literal|0l
decl_stmt|;
name|Long
name|sumlongnum
init|=
literal|0l
decl_stmt|;
name|Float
name|sumfloatnum
init|=
literal|0.0f
decl_stmt|;
name|Double
name|sumdoublenum
init|=
literal|0.0
decl_stmt|;
for|for
control|(
name|ArrayWritable
name|value
range|:
name|values
control|)
block|{
name|sumid
operator|+=
name|value
operator|.
name|id
operator|.
name|get
argument_list|()
expr_stmt|;
name|sumintnum5
operator|+=
name|value
operator|.
name|intnum5
operator|.
name|get
argument_list|()
expr_stmt|;
name|sumintnum100
operator|+=
name|value
operator|.
name|intnum100
operator|.
name|get
argument_list|()
expr_stmt|;
name|sumintnum
operator|+=
name|value
operator|.
name|intnum
operator|.
name|get
argument_list|()
expr_stmt|;
name|sumlongnum
operator|+=
name|value
operator|.
name|longnum
operator|.
name|get
argument_list|()
expr_stmt|;
name|sumfloatnum
operator|+=
name|value
operator|.
name|floatnum
operator|.
name|get
argument_list|()
expr_stmt|;
name|sumdoublenum
operator|+=
name|value
operator|.
name|doublenum
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|output
operator|+=
name|sumid
operator|+
name|TAB
expr_stmt|;
name|output
operator|+=
name|sumintnum5
operator|+
name|TAB
expr_stmt|;
name|output
operator|+=
name|sumintnum100
operator|+
name|TAB
expr_stmt|;
name|output
operator|+=
name|sumintnum
operator|+
name|TAB
expr_stmt|;
name|output
operator|+=
name|sumlongnum
operator|+
name|TAB
expr_stmt|;
name|output
operator|+=
name|sumfloatnum
operator|+
name|TAB
expr_stmt|;
name|output
operator|+=
name|sumdoublenum
operator|+
name|TAB
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
name|dummyLong
argument_list|,
operator|new
name|Text
argument_list|(
name|output
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|args
operator|=
operator|new
name|GenericOptionsParser
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
operator|.
name|getRemainingArgs
argument_list|()
expr_stmt|;
name|String
index|[]
name|otherArgs
init|=
operator|new
name|String
index|[
literal|4
index|]
decl_stmt|;
name|int
name|j
init|=
literal|0
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
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
literal|"-libjars"
argument_list|)
condition|)
block|{
comment|// generic options parser doesn't seem to work!
name|conf
operator|.
name|set
argument_list|(
literal|"tmpjars"
argument_list|,
name|args
index|[
name|i
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
name|i
operator|=
name|i
operator|+
literal|1
expr_stmt|;
comment|// skip it , the for loop will skip its value
block|}
else|else
block|{
name|otherArgs
index|[
name|j
operator|++
index|]
operator|=
name|args
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
if|if
condition|(
name|otherArgs
operator|.
name|length
operator|!=
literal|4
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: hadoop jar sumnumbers<serveruri><output dir><-libjars hive-hcat jar>\n"
operator|+
literal|"The<tab|ctrla> argument controls the output delimiter.\n"
operator|+
literal|"The hcat jar location should be specified as file://<full path to jar>\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|String
name|serverUri
init|=
name|otherArgs
index|[
literal|0
index|]
decl_stmt|;
name|String
name|tableName
init|=
name|NUMBERS_TABLE_NAME
decl_stmt|;
name|String
name|outputDir
init|=
name|otherArgs
index|[
literal|1
index|]
decl_stmt|;
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
name|String
name|principalID
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_METASTORE_PRINCIPAL
argument_list|)
decl_stmt|;
if|if
condition|(
name|principalID
operator|!=
literal|null
condition|)
name|conf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_METASTORE_PRINCIPAL
argument_list|,
name|principalID
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
literal|"sumnumbers"
argument_list|)
decl_stmt|;
name|HCatInputFormat
operator|.
name|setInput
argument_list|(
name|job
argument_list|,
name|InputJobInfo
operator|.
name|create
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// initialize HCatOutputFormat
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|HCatInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|TextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|SumNumbers
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|SumMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|SumReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|IntWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|ArrayWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|LongWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|outputDir
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
condition|?
literal|0
else|:
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|ArrayWritable
implements|implements
name|Writable
block|{
comment|// though id is given as a Short by hcat, the map will emit it as an
comment|// IntWritable so we can just sum in the reduce
name|IntWritable
name|id
decl_stmt|;
comment|// though intnum5 is handed as a Byte by hcat, the map() will emit it as
comment|// an IntWritable so we can just sum in the reduce
name|IntWritable
name|intnum5
decl_stmt|;
name|IntWritable
name|intnum100
decl_stmt|;
name|IntWritable
name|intnum
decl_stmt|;
name|LongWritable
name|longnum
decl_stmt|;
name|FloatWritable
name|floatnum
decl_stmt|;
name|DoubleWritable
name|doublenum
decl_stmt|;
comment|/**      *      */
specifier|public
name|ArrayWritable
parameter_list|()
block|{
name|id
operator|=
operator|new
name|IntWritable
argument_list|()
expr_stmt|;
name|intnum5
operator|=
operator|new
name|IntWritable
argument_list|()
expr_stmt|;
name|intnum100
operator|=
operator|new
name|IntWritable
argument_list|()
expr_stmt|;
name|intnum
operator|=
operator|new
name|IntWritable
argument_list|()
expr_stmt|;
name|longnum
operator|=
operator|new
name|LongWritable
argument_list|()
expr_stmt|;
name|floatnum
operator|=
operator|new
name|FloatWritable
argument_list|()
expr_stmt|;
name|doublenum
operator|=
operator|new
name|DoubleWritable
argument_list|()
expr_stmt|;
block|}
comment|/**      * @param id      * @param intnum5      * @param intnum100      * @param intnum      * @param longnum      * @param floatnum      * @param doublenum      */
specifier|public
name|ArrayWritable
parameter_list|(
name|IntWritable
name|id
parameter_list|,
name|IntWritable
name|intnum5
parameter_list|,
name|IntWritable
name|intnum100
parameter_list|,
name|IntWritable
name|intnum
parameter_list|,
name|LongWritable
name|longnum
parameter_list|,
name|FloatWritable
name|floatnum
parameter_list|,
name|DoubleWritable
name|doublenum
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|intnum5
operator|=
name|intnum5
expr_stmt|;
name|this
operator|.
name|intnum100
operator|=
name|intnum100
expr_stmt|;
name|this
operator|.
name|intnum
operator|=
name|intnum
expr_stmt|;
name|this
operator|.
name|longnum
operator|=
name|longnum
expr_stmt|;
name|this
operator|.
name|floatnum
operator|=
name|floatnum
expr_stmt|;
name|this
operator|.
name|doublenum
operator|=
name|doublenum
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|id
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|intnum5
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|intnum100
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|intnum
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|longnum
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|floatnum
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|doublenum
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|id
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|intnum5
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|intnum100
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|intnum
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|longnum
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|floatnum
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|doublenum
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

