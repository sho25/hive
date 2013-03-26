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
name|IOException
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
name|HashMap
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
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|hcatalog
operator|.
name|data
operator|.
name|DefaultHCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
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
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
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
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatOutputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|InputJobInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|OutputJobInfo
import|;
end_import

begin_comment
comment|/**  * This is a map reduce test for testing hcat which goes against the "numbers"  * table and writes data to another table. It reads data from numbers which  * is an unpartitioned table and adds 10 to each field. It stores the result into  * the datestamp='20100101' partition of the numbers_part_empty_initially table if the second  * command line arg is "part". If the second cmdline arg is "nopart" then the  * result is stored into the 'numbers_nopart_empty_initially' (unpartitioned) table.  * If the second cmdline arg is "nopart_pig", then the result is stored into the  * 'numbers_nopart_pig_empty_initially' (unpartitioned) table with the tinyint  * and smallint columns in "numbers" being stored as "int" (since pig cannot handle  * tinyint and smallint)  *   * Usage: hadoop jar storenumbers<serveruri><part|nopart|nopart_pig><-libjars hive-hcat jar>         If the second argument is "part" data is written to datestamp = '2010101' partition of the numbers_part_empty_initially table.         If the second argument is "nopart", data is written to the unpartitioned numbers_nopart_empty_initially table.         If the second argument is "nopart_pig", data is written to the unpartitioned numbers_nopart_pig_empty_initially table.         The hcat jar location should be specified as file://<full path to jar>  */
end_comment

begin_class
specifier|public
class|class
name|StoreNumbers
block|{
specifier|private
specifier|static
specifier|final
name|String
name|NUMBERS_PARTITIONED_TABLE_NAME
init|=
literal|"numbers_part_empty_initially"
decl_stmt|;
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
name|NUMBERS_NON_PARTITIONED_TABLE_NAME
init|=
literal|"numbers_nopart_empty_initially"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NUMBERS_NON_PARTITIONED_PIG_TABLE_NAME
init|=
literal|"numbers_nopart_pig_empty_initially"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|IS_PIG_NON_PART_TABLE
init|=
literal|"is.pig.non.part.table"
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
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|>
block|{
name|Integer
name|intnum1000
decl_stmt|;
comment|// though id is given as a Short by hcat, the map will emit it as an
comment|// IntWritable so we can just sum in the reduce
name|Short
name|id
decl_stmt|;
comment|// though intnum5 is handed as a Byte by hcat, the map() will emit it as
comment|// an IntWritable so we can just sum in the reduce
name|Byte
name|intnum5
decl_stmt|;
name|Integer
name|intnum100
decl_stmt|;
name|Integer
name|intnum
decl_stmt|;
name|Long
name|longnum
decl_stmt|;
name|Float
name|floatnum
decl_stmt|;
name|Double
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
name|WritableComparable
argument_list|,
name|HCatRecord
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
name|boolean
name|isnoPartPig
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getBoolean
argument_list|(
name|IS_PIG_NON_PART_TABLE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|intnum1000
operator|=
operator|(
operator|(
name|Integer
operator|)
name|value
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
expr_stmt|;
name|id
operator|=
operator|(
operator|(
name|Short
operator|)
name|value
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|)
expr_stmt|;
name|intnum5
operator|=
operator|(
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
operator|)
expr_stmt|;
name|intnum100
operator|=
operator|(
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
operator|)
expr_stmt|;
name|intnum
operator|=
operator|(
operator|(
name|Integer
operator|)
name|value
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|)
expr_stmt|;
name|longnum
operator|=
operator|(
operator|(
name|Long
operator|)
name|value
operator|.
name|get
argument_list|(
literal|5
argument_list|)
operator|)
expr_stmt|;
name|floatnum
operator|=
operator|(
operator|(
name|Float
operator|)
name|value
operator|.
name|get
argument_list|(
literal|6
argument_list|)
operator|)
expr_stmt|;
name|doublenum
operator|=
operator|(
operator|(
name|Double
operator|)
name|value
operator|.
name|get
argument_list|(
literal|7
argument_list|)
operator|)
expr_stmt|;
name|HCatRecord
name|output
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|output
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|intnum1000
operator|+
literal|10
argument_list|)
expr_stmt|;
if|if
condition|(
name|isnoPartPig
condition|)
block|{
name|output
operator|.
name|set
argument_list|(
literal|1
argument_list|,
operator|(
call|(
name|int
call|)
argument_list|(
name|id
operator|+
literal|10
argument_list|)
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|output
operator|.
name|set
argument_list|(
literal|1
argument_list|,
operator|(
call|(
name|short
call|)
argument_list|(
name|id
operator|+
literal|10
argument_list|)
operator|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isnoPartPig
condition|)
block|{
name|output
operator|.
name|set
argument_list|(
literal|2
argument_list|,
call|(
name|int
call|)
argument_list|(
name|intnum5
operator|+
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|output
operator|.
name|set
argument_list|(
literal|2
argument_list|,
call|(
name|byte
call|)
argument_list|(
name|intnum5
operator|+
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|set
argument_list|(
literal|3
argument_list|,
name|intnum100
operator|+
literal|10
argument_list|)
expr_stmt|;
name|output
operator|.
name|set
argument_list|(
literal|4
argument_list|,
name|intnum
operator|+
literal|10
argument_list|)
expr_stmt|;
name|output
operator|.
name|set
argument_list|(
literal|5
argument_list|,
call|(
name|long
call|)
argument_list|(
name|longnum
operator|+
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|set
argument_list|(
literal|6
argument_list|,
call|(
name|float
call|)
argument_list|(
name|floatnum
operator|+
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|set
argument_list|(
literal|7
argument_list|,
call|(
name|double
call|)
argument_list|(
name|doublenum
operator|+
literal|10
argument_list|)
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
literal|8
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"XXX: class:"
operator|+
name|output
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|write
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|0
argument_list|)
argument_list|,
name|output
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
literal|2
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
literal|2
condition|)
block|{
name|usage
argument_list|()
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
if|if
condition|(
name|otherArgs
index|[
literal|1
index|]
operator|==
literal|null
operator|||
operator|(
operator|!
name|otherArgs
index|[
literal|1
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"part"
argument_list|)
operator|&&
operator|!
name|otherArgs
index|[
literal|1
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"nopart"
argument_list|)
operator|)
operator|&&
operator|!
name|otherArgs
index|[
literal|1
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"nopart_pig"
argument_list|)
condition|)
block|{
name|usage
argument_list|()
expr_stmt|;
block|}
name|boolean
name|writeToPartitionedTable
init|=
operator|(
name|otherArgs
index|[
literal|1
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"part"
argument_list|)
operator|)
decl_stmt|;
name|boolean
name|writeToNonPartPigTable
init|=
operator|(
name|otherArgs
index|[
literal|1
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"nopart_pig"
argument_list|)
operator|)
decl_stmt|;
name|String
name|tableName
init|=
name|NUMBERS_TABLE_NAME
decl_stmt|;
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|outputPartitionKvps
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|outputTableName
init|=
literal|null
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|IS_PIG_NON_PART_TABLE
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
if|if
condition|(
name|writeToPartitionedTable
condition|)
block|{
name|outputTableName
operator|=
name|NUMBERS_PARTITIONED_TABLE_NAME
expr_stmt|;
name|outputPartitionKvps
operator|.
name|put
argument_list|(
literal|"datestamp"
argument_list|,
literal|"20100101"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|writeToNonPartPigTable
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|IS_PIG_NON_PART_TABLE
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|outputTableName
operator|=
name|NUMBERS_NON_PARTITIONED_PIG_TABLE_NAME
expr_stmt|;
block|}
else|else
block|{
name|outputTableName
operator|=
name|NUMBERS_NON_PARTITIONED_TABLE_NAME
expr_stmt|;
block|}
comment|// test with null or empty randomly
if|if
condition|(
operator|new
name|Random
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|2
argument_list|)
operator|==
literal|0
condition|)
block|{
name|outputPartitionKvps
operator|=
literal|null
expr_stmt|;
block|}
block|}
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
literal|"storenumbers"
argument_list|)
decl_stmt|;
comment|// initialize HCatInputFormat
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
argument_list|,
name|serverUri
argument_list|,
name|principalID
argument_list|)
argument_list|)
expr_stmt|;
comment|// initialize HCatOutputFormat
name|HCatOutputFormat
operator|.
name|setOutput
argument_list|(
name|job
argument_list|,
name|OutputJobInfo
operator|.
name|create
argument_list|(
name|dbName
argument_list|,
name|outputTableName
argument_list|,
name|outputPartitionKvps
argument_list|,
name|serverUri
argument_list|,
name|principalID
argument_list|)
argument_list|)
expr_stmt|;
comment|// test with and without specifying schema randomly
name|HCatSchema
name|s
init|=
name|HCatInputFormat
operator|.
name|getTableSchema
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|writeToNonPartPigTable
condition|)
block|{
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|newHfsList
init|=
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|()
decl_stmt|;
comment|// change smallint and tinyint to int
for|for
control|(
name|HCatFieldSchema
name|hfs
range|:
name|s
operator|.
name|getFields
argument_list|()
control|)
block|{
if|if
condition|(
name|hfs
operator|.
name|getTypeString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"smallint"
argument_list|)
condition|)
block|{
name|newHfsList
operator|.
name|add
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
name|hfs
operator|.
name|getName
argument_list|()
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|INT
argument_list|,
name|hfs
operator|.
name|getComment
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|hfs
operator|.
name|getTypeString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"tinyint"
argument_list|)
condition|)
block|{
name|newHfsList
operator|.
name|add
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
name|hfs
operator|.
name|getName
argument_list|()
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|INT
argument_list|,
name|hfs
operator|.
name|getComment
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newHfsList
operator|.
name|add
argument_list|(
name|hfs
argument_list|)
expr_stmt|;
block|}
block|}
name|s
operator|=
operator|new
name|HCatSchema
argument_list|(
name|newHfsList
argument_list|)
expr_stmt|;
block|}
name|HCatOutputFormat
operator|.
name|setSchema
argument_list|(
name|job
argument_list|,
name|s
argument_list|)
expr_stmt|;
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
name|HCatOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|StoreNumbers
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
name|setOutputKeyClass
argument_list|(
name|IntWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|DefaultHCatRecord
operator|.
name|class
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
comment|/**      *       */
specifier|private
specifier|static
name|void
name|usage
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: hadoop jar storenumbers<serveruri><part|nopart|nopart_pig><-libjars hive-hcat jar>\n"
operator|+
literal|"\tIf the second argument is \"part\" data is written to datestamp = '2010101' partition of "
operator|+
literal|"the numbers_part_empty_initially table.\n\tIf the second argument is \"nopart\", data is written to "
operator|+
literal|"the unpartitioned numbers_nopart_empty_initially table.\n\tIf the second argument is \"nopart_pig\", "
operator|+
literal|"data is written to the unpartitioned numbers_nopart_pig_empty_initially table.\nt"
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
block|}
end_class

end_unit

