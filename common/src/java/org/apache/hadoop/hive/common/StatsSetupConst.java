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
name|common
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
name|TreeMap
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
name|HiveConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|annotation
operator|.
name|JsonInclude
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
name|annotation
operator|.
name|JsonProperty
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
name|JsonGenerator
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
name|JsonParser
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
name|JsonProcessingException
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
name|DeserializationContext
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
name|JsonDeserializer
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
name|JsonSerializer
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
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectReader
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
name|ObjectWriter
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
name|SerializerProvider
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
name|annotation
operator|.
name|JsonDeserialize
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
name|annotation
operator|.
name|JsonSerialize
import|;
end_import

begin_comment
comment|/**  * A class that defines the constant strings used by the statistics implementation.  */
end_comment

begin_class
specifier|public
class|class
name|StatsSetupConst
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|StatsSetupConst
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
enum|enum
name|StatDB
block|{
name|fs
block|{
annotation|@
name|Override
specifier|public
name|String
name|getPublisher
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
literal|"org.apache.hadoop.hive.ql.stats.fs.FSStatsPublisher"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getAggregator
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
literal|"org.apache.hadoop.hive.ql.stats.fs.FSStatsAggregator"
return|;
block|}
block|}
block|,
name|custom
block|{
annotation|@
name|Override
specifier|public
name|String
name|getPublisher
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_DEFAULT_PUBLISHER
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getAggregator
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_DEFAULT_AGGREGATOR
argument_list|)
return|;
block|}
block|}
block|;
specifier|public
specifier|abstract
name|String
name|getPublisher
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|String
name|getAggregator
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
block|}
comment|// statistics stored in metastore
comment|/**    * The name of the statistic Num Files to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|NUM_FILES
init|=
literal|"numFiles"
decl_stmt|;
comment|/**    * The name of the statistic Num Partitions to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|NUM_PARTITIONS
init|=
literal|"numPartitions"
decl_stmt|;
comment|/**    * The name of the statistic Total Size to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|TOTAL_SIZE
init|=
literal|"totalSize"
decl_stmt|;
comment|/**    * The name of the statistic Row Count to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|ROW_COUNT
init|=
literal|"numRows"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RUN_TIME_ROW_COUNT
init|=
literal|"runTimeNumRows"
decl_stmt|;
comment|/**    * The name of the statistic Raw Data Size to be published or gathered.    */
specifier|public
specifier|static
specifier|final
name|String
name|RAW_DATA_SIZE
init|=
literal|"rawDataSize"
decl_stmt|;
comment|/**    * Temp dir for writing stats from tasks.    */
specifier|public
specifier|static
specifier|final
name|String
name|STATS_TMP_LOC
init|=
literal|"hive.stats.tmp.loc"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STATS_FILE_PREFIX
init|=
literal|"tmpstats-"
decl_stmt|;
comment|/**    * List of all supported statistics    */
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|supportedStats
init|=
block|{
name|NUM_FILES
block|,
name|ROW_COUNT
block|,
name|TOTAL_SIZE
block|,
name|RAW_DATA_SIZE
block|}
decl_stmt|;
comment|/**    * List of all statistics that need to be collected during query execution. These are    * statistics that inherently require a scan of the data.    */
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|statsRequireCompute
init|=
operator|new
name|String
index|[]
block|{
name|ROW_COUNT
block|,
name|RAW_DATA_SIZE
block|}
decl_stmt|;
comment|/**    * List of statistics that can be collected quickly without requiring a scan of the data.    */
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|fastStats
init|=
operator|new
name|String
index|[]
block|{
name|NUM_FILES
block|,
name|TOTAL_SIZE
block|}
decl_stmt|;
comment|// This string constant is used to indicate to AlterHandler that
comment|// alterPartition/alterTable is happening via statsTask or via user.
specifier|public
specifier|static
specifier|final
name|String
name|STATS_GENERATED
init|=
literal|"STATS_GENERATED"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TASK
init|=
literal|"TASK"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USER
init|=
literal|"USER"
decl_stmt|;
comment|// This string constant is used by AlterHandler to figure out that it should not attempt to
comment|// update stats. It is set by any client-side task which wishes to signal that no stats
comment|// update should take place, such as with replication.
specifier|public
specifier|static
specifier|final
name|String
name|DO_NOT_UPDATE_STATS
init|=
literal|"DO_NOT_UPDATE_STATS"
decl_stmt|;
comment|//This string constant will be persisted in metastore to indicate whether corresponding
comment|//table or partition's statistics and table or partition's column statistics are accurate or not.
specifier|public
specifier|static
specifier|final
name|String
name|COLUMN_STATS_ACCURATE
init|=
literal|"COLUMN_STATS_ACCURATE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COLUMN_STATS
init|=
literal|"COLUMN_STATS"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BASIC_STATS
init|=
literal|"BASIC_STATS"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CASCADE
init|=
literal|"CASCADE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TRUE
init|=
literal|"true"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FALSE
init|=
literal|"false"
decl_stmt|;
comment|// The parameter keys for the table statistics. Those keys are excluded from 'show create table' command output.
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|TABLE_PARAMS_STATS_KEYS
init|=
operator|new
name|String
index|[]
block|{
name|COLUMN_STATS_ACCURATE
block|,
name|NUM_FILES
block|,
name|TOTAL_SIZE
block|,
name|ROW_COUNT
block|,
name|RAW_DATA_SIZE
block|,
name|NUM_PARTITIONS
block|}
decl_stmt|;
specifier|private
specifier|static
class|class
name|ColumnStatsAccurate
block|{
specifier|private
specifier|static
name|ObjectReader
name|objectReader
decl_stmt|;
specifier|private
specifier|static
name|ObjectWriter
name|objectWriter
decl_stmt|;
static|static
block|{
name|ObjectMapper
name|objectMapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|objectReader
operator|=
name|objectMapper
operator|.
name|readerFor
argument_list|(
name|ColumnStatsAccurate
operator|.
name|class
argument_list|)
expr_stmt|;
name|objectWriter
operator|=
name|objectMapper
operator|.
name|writerFor
argument_list|(
name|ColumnStatsAccurate
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|BooleanSerializer
extends|extends
name|JsonSerializer
argument_list|<
name|Boolean
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|serialize
parameter_list|(
name|Boolean
name|value
parameter_list|,
name|JsonGenerator
name|jsonGenerator
parameter_list|,
name|SerializerProvider
name|serializerProvider
parameter_list|)
throws|throws
name|IOException
throws|,
name|JsonProcessingException
block|{
name|jsonGenerator
operator|.
name|writeString
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|BooleanDeserializer
extends|extends
name|JsonDeserializer
argument_list|<
name|Boolean
argument_list|>
block|{
specifier|public
name|Boolean
name|deserialize
parameter_list|(
name|JsonParser
name|jsonParser
parameter_list|,
name|DeserializationContext
name|deserializationContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|JsonProcessingException
block|{
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|jsonParser
operator|.
name|getValueAsString
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|JsonInclude
argument_list|(
name|JsonInclude
operator|.
name|Include
operator|.
name|NON_DEFAULT
argument_list|)
annotation|@
name|JsonSerialize
argument_list|(
name|using
operator|=
name|BooleanSerializer
operator|.
name|class
argument_list|)
annotation|@
name|JsonDeserialize
argument_list|(
name|using
operator|=
name|BooleanDeserializer
operator|.
name|class
argument_list|)
annotation|@
name|JsonProperty
argument_list|(
name|BASIC_STATS
argument_list|)
name|boolean
name|basicStats
decl_stmt|;
annotation|@
name|JsonInclude
argument_list|(
name|JsonInclude
operator|.
name|Include
operator|.
name|NON_EMPTY
argument_list|)
annotation|@
name|JsonProperty
argument_list|(
name|COLUMN_STATS
argument_list|)
annotation|@
name|JsonSerialize
argument_list|(
name|contentUsing
operator|=
name|BooleanSerializer
operator|.
name|class
argument_list|)
annotation|@
name|JsonDeserialize
argument_list|(
name|contentUsing
operator|=
name|BooleanDeserializer
operator|.
name|class
argument_list|)
name|TreeMap
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|columnStats
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
block|}
empty_stmt|;
specifier|public
specifier|static
name|boolean
name|areBasicStatsUptoDate
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ColumnStatsAccurate
name|stats
init|=
name|parseStatsAcc
argument_list|(
name|params
operator|.
name|get
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|stats
operator|.
name|basicStats
return|;
block|}
specifier|public
specifier|static
name|boolean
name|areColumnStatsUptoDate
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|String
name|colName
parameter_list|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ColumnStatsAccurate
name|stats
init|=
name|parseStatsAcc
argument_list|(
name|params
operator|.
name|get
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|stats
operator|.
name|columnStats
operator|.
name|containsKey
argument_list|(
name|colName
argument_list|)
return|;
block|}
comment|// It will only throw JSONException when stats.put(BASIC_STATS, TRUE)
comment|// has duplicate key, which is not possible
comment|// note that set basic stats false will wipe out column stats too.
specifier|public
specifier|static
name|void
name|setBasicStatsState
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|String
name|setting
parameter_list|)
block|{
if|if
condition|(
name|setting
operator|.
name|equals
argument_list|(
name|FALSE
argument_list|)
condition|)
block|{
if|if
condition|(
name|params
operator|!=
literal|null
operator|&&
name|params
operator|.
name|containsKey
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|)
condition|)
block|{
name|params
operator|.
name|remove
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"params are null...cant set columnstatstate!"
argument_list|)
throw|;
block|}
name|ColumnStatsAccurate
name|stats
init|=
name|parseStatsAcc
argument_list|(
name|params
operator|.
name|get
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|)
argument_list|)
decl_stmt|;
name|stats
operator|.
name|basicStats
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|params
operator|.
name|put
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|,
name|ColumnStatsAccurate
operator|.
name|objectWriter
operator|.
name|writeValueAsString
argument_list|(
name|stats
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|JsonProcessingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"can't serialize column stats"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|setColumnStatsState
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"params are null...cant set columnstatstate!"
argument_list|)
throw|;
block|}
if|if
condition|(
name|colNames
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|ColumnStatsAccurate
name|stats
init|=
name|parseStatsAcc
argument_list|(
name|params
operator|.
name|get
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|colNames
control|)
block|{
if|if
condition|(
operator|!
name|stats
operator|.
name|columnStats
operator|.
name|containsKey
argument_list|(
name|colName
argument_list|)
condition|)
block|{
name|stats
operator|.
name|columnStats
operator|.
name|put
argument_list|(
name|colName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|params
operator|.
name|put
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|,
name|ColumnStatsAccurate
operator|.
name|objectWriter
operator|.
name|writeValueAsString
argument_list|(
name|stats
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|JsonProcessingException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|clearColumnStatsState
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|ColumnStatsAccurate
name|stats
init|=
name|parseStatsAcc
argument_list|(
name|params
operator|.
name|get
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|)
argument_list|)
decl_stmt|;
name|stats
operator|.
name|columnStats
operator|.
name|clear
argument_list|()
expr_stmt|;
try|try
block|{
name|params
operator|.
name|put
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|,
name|ColumnStatsAccurate
operator|.
name|objectWriter
operator|.
name|writeValueAsString
argument_list|(
name|stats
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|JsonProcessingException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|removeColumnStatsState
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
return|return;
block|}
try|try
block|{
name|ColumnStatsAccurate
name|stats
init|=
name|parseStatsAcc
argument_list|(
name|params
operator|.
name|get
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|string
range|:
name|colNames
control|)
block|{
name|stats
operator|.
name|columnStats
operator|.
name|remove
argument_list|(
name|string
argument_list|)
expr_stmt|;
block|}
name|params
operator|.
name|put
argument_list|(
name|COLUMN_STATS_ACCURATE
argument_list|,
name|ColumnStatsAccurate
operator|.
name|objectWriter
operator|.
name|writeValueAsString
argument_list|(
name|stats
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|JsonProcessingException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|setStatsStateForCreateTable
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|cols
parameter_list|,
name|String
name|setting
parameter_list|)
block|{
if|if
condition|(
name|TRUE
operator|.
name|equals
argument_list|(
name|setting
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|stat
range|:
name|StatsSetupConst
operator|.
name|supportedStats
control|)
block|{
name|params
operator|.
name|put
argument_list|(
name|stat
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
block|}
block|}
name|setBasicStatsState
argument_list|(
name|params
argument_list|,
name|setting
argument_list|)
expr_stmt|;
name|setColumnStatsState
argument_list|(
name|params
argument_list|,
name|cols
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|ColumnStatsAccurate
name|parseStatsAcc
parameter_list|(
name|String
name|statsAcc
parameter_list|)
block|{
if|if
condition|(
name|statsAcc
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|ColumnStatsAccurate
argument_list|()
return|;
block|}
try|try
block|{
return|return
name|ColumnStatsAccurate
operator|.
name|objectReader
operator|.
name|readValue
argument_list|(
name|statsAcc
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|ColumnStatsAccurate
name|ret
init|=
operator|new
name|ColumnStatsAccurate
argument_list|()
decl_stmt|;
if|if
condition|(
name|TRUE
operator|.
name|equalsIgnoreCase
argument_list|(
name|statsAcc
argument_list|)
condition|)
block|{
name|ret
operator|.
name|basicStats
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
block|}
end_class

end_unit

