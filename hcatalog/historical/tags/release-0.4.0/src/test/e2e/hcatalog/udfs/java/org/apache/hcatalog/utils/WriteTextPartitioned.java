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
name|conf
operator|.
name|Configured
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
name|hadoop
operator|.
name|util
operator|.
name|Tool
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
name|ToolRunner
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
comment|/**  * This is a map reduce test for testing hcat writing to partitioned tables.  * table. It performs a group by on the first column and a SUM operation on the  * other columns. This is to simulate a typical operation in a map reduce  * program to test that hcat hands the right data to the map reduce program  *   * Usage: hadoop jar org.apache.hcatalog.utils.HBaseReadWrite -libjars  *&lt;hcat_jar&gt; *&lt;serveruri&gt;&lt;input_tablename&gt;&lt;output_tablename&gt; [filter]  * If filter is given it will be provided as the partition to write to.  */
end_comment

begin_class
specifier|public
class|class
name|WriteTextPartitioned
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|static
name|String
name|filter
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
class|class
name|Map
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
name|String
name|name
init|=
operator|(
name|String
operator|)
name|value
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|age
init|=
operator|(
name|Integer
operator|)
name|value
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|ds
init|=
operator|(
name|String
operator|)
name|value
operator|.
name|get
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|HCatRecord
name|record
init|=
operator|(
name|filter
operator|==
literal|null
condition|?
operator|new
name|DefaultHCatRecord
argument_list|(
literal|3
argument_list|)
else|:
operator|new
name|DefaultHCatRecord
argument_list|(
literal|2
argument_list|)
operator|)
decl_stmt|;
name|record
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|record
operator|.
name|set
argument_list|(
literal|1
argument_list|,
name|age
argument_list|)
expr_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
name|record
operator|.
name|set
argument_list|(
literal|2
argument_list|,
name|ds
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
literal|null
argument_list|,
name|record
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|run
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
name|getConf
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
name|serverUri
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|String
name|inputTableName
init|=
name|args
index|[
literal|1
index|]
decl_stmt|;
name|String
name|outputTableName
init|=
name|args
index|[
literal|2
index|]
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|3
condition|)
name|filter
operator|=
name|args
index|[
literal|3
index|]
expr_stmt|;
name|String
name|dbName
init|=
literal|null
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
literal|"WriteTextPartitioned"
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
name|inputTableName
argument_list|,
name|filter
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
name|setJarByClass
argument_list|(
name|WriteTextPartitioned
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|Map
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|WritableComparable
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
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|java
operator|.
name|util
operator|.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionVals
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|s
init|=
name|filter
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
name|String
name|val
init|=
name|s
index|[
literal|1
index|]
operator|.
name|replace
argument_list|(
literal|'"'
argument_list|,
literal|' '
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|partitionVals
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|partitionVals
operator|.
name|put
argument_list|(
name|s
index|[
literal|0
index|]
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
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
name|partitionVals
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
comment|// Build the schema for this table, which is slightly different than the
comment|// schema for the input table
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|fss
init|=
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|fss
operator|.
name|add
argument_list|(
name|s
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|fss
operator|.
name|add
argument_list|(
name|s
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|fss
operator|.
name|add
argument_list|(
name|s
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|setSchema
argument_list|(
name|job
argument_list|,
operator|new
name|HCatSchema
argument_list|(
name|fss
argument_list|)
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
return|return
operator|(
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
operator|)
return|;
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
name|int
name|exitCode
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
operator|new
name|WriteTextPartitioned
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|exitCode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

