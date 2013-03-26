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
name|HashMap
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
comment|/**  * This is a map reduce test for testing hcat which goes against the "complex"  * table and writes to "complex_nopart_empty_initially" table. It reads data from complex which  * is an unpartitioned table and stores the data as-is into complex_empty_initially table  * (which is also unpartitioned)  *  * Usage: hadoop jar testudf.jar storecomplex<serveruri><-libjars hive-hcat jar>    The hcat jar location should be specified as file://<full path to jar>  */
end_comment

begin_class
specifier|public
class|class
name|StoreComplex
block|{
specifier|private
specifier|static
specifier|final
name|String
name|COMPLEX_TABLE_NAME
init|=
literal|"complex"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COMPLEX_NOPART_EMPTY_INITIALLY_TABLE_NAME
init|=
literal|"complex_nopart_empty_initially"
decl_stmt|;
specifier|public
specifier|static
class|class
name|ComplexMapper
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
comment|// just write out the value as-is
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
name|value
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
literal|1
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
literal|1
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
name|String
name|tableName
init|=
name|COMPLEX_TABLE_NAME
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
name|outputTableName
operator|=
name|COMPLEX_NOPART_EMPTY_INITIALLY_TABLE_NAME
expr_stmt|;
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
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"INFO: output partition keys set to null for writing"
argument_list|)
expr_stmt|;
name|outputPartitionKvps
operator|=
literal|null
expr_stmt|;
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
literal|"storecomplex"
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
argument_list|)
argument_list|)
expr_stmt|;
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
name|StoreComplex
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|ComplexMapper
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
comment|/**      *      */
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
literal|"Usage: hadoop jar testudf.jar storecomplex<serveruri><-libjars hive-hcat jar>\n"
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

