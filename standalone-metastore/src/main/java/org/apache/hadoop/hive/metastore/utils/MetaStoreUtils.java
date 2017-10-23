begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|utils
package|;
end_package

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
name|Predicates
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|*
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
name|metastore
operator|.
name|api
operator|.
name|ColumnStatistics
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
name|metastore
operator|.
name|api
operator|.
name|ColumnStatisticsObj
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
name|metastore
operator|.
name|api
operator|.
name|Decimal
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|Order
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
name|metastore
operator|.
name|api
operator|.
name|SerDeInfo
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
name|metastore
operator|.
name|api
operator|.
name|SkewedInfo
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
name|metastore
operator|.
name|api
operator|.
name|StorageDescriptor
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|metastore
operator|.
name|columnstats
operator|.
name|aggr
operator|.
name|ColumnStatsAggregator
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
name|metastore
operator|.
name|columnstats
operator|.
name|aggr
operator|.
name|ColumnStatsAggregatorFactory
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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|MessageDigest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|DateFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
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
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
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
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_class
specifier|public
class|class
name|MetaStoreUtils
block|{
comment|/** A fixed date format to be used for hive partition column values. */
specifier|public
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|DateFormat
argument_list|>
name|PARTITION_DATE_FORMAT
init|=
operator|new
name|ThreadLocal
argument_list|<
name|DateFormat
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|DateFormat
name|initialValue
parameter_list|()
block|{
name|DateFormat
name|val
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd"
argument_list|)
decl_stmt|;
name|val
operator|.
name|setLenient
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Without this, 2020-20-20 becomes 2021-08-20.
return|return
name|val
return|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Charset
name|ENCODING
init|=
name|StandardCharsets
operator|.
name|UTF_8
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MetaStoreUtils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Catches exceptions that can't be handled and bundles them to MetaException    *    * @param e exception to wrap.    * @throws MetaException wrapper for the exception    */
specifier|public
specifier|static
name|void
name|logAndThrowMetaException
parameter_list|(
name|Exception
name|e
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|exInfo
init|=
literal|"Got exception: "
operator|+
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|exInfo
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Converting exception to MetaException"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
name|exInfo
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|String
name|encodeTableName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
comment|// The encoding method is simple, e.g., replace
comment|// all the special characters with the corresponding number in ASCII.
comment|// Note that unicode is not supported in table names. And we have explicit
comment|// checks for it.
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|char
name|ch
range|:
name|name
operator|.
name|toCharArray
argument_list|()
control|)
block|{
if|if
condition|(
name|Character
operator|.
name|isLetterOrDigit
argument_list|(
name|ch
argument_list|)
operator|||
name|ch
operator|==
literal|'_'
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'-'
argument_list|)
operator|.
name|append
argument_list|(
operator|(
name|int
operator|)
name|ch
argument_list|)
operator|.
name|append
argument_list|(
literal|'-'
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * convert Exception to MetaException, which sets the cause to such exception    * @param e cause of the exception    * @return  the MetaException with the specified exception as the cause    */
specifier|public
specifier|static
name|MetaException
name|newMetaException
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|newMetaException
argument_list|(
name|e
operator|!=
literal|null
condition|?
name|e
operator|.
name|getMessage
argument_list|()
else|:
literal|null
argument_list|,
name|e
argument_list|)
return|;
block|}
comment|/**    * convert Exception to MetaException, which sets the cause to such exception    * @param errorMessage  the error message for this MetaException    * @param e             cause of the exception    * @return  the MetaException with the specified exception as the cause    */
specifier|public
specifier|static
name|MetaException
name|newMetaException
parameter_list|(
name|String
name|errorMessage
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|MetaException
name|metaException
init|=
operator|new
name|MetaException
argument_list|(
name|errorMessage
argument_list|)
decl_stmt|;
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|metaException
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|metaException
return|;
block|}
comment|/**    * Helper function to transform Nulls to empty strings.    */
specifier|private
specifier|static
specifier|final
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|transFormNullsToEmptyString
init|=
operator|new
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|java
operator|.
name|lang
operator|.
name|String
name|apply
parameter_list|(
annotation|@
name|Nullable
name|java
operator|.
name|lang
operator|.
name|String
name|string
parameter_list|)
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
operator|.
name|defaultString
argument_list|(
name|string
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|/**    * We have aneed to sanity-check the map before conversion from persisted objects to    * metadata thrift objects because null values in maps will cause a NPE if we send    * across thrift. Pruning is appropriate for most cases except for databases such as    * Oracle where Empty strings are stored as nulls, in which case we need to handle that.    * See HIVE-8485 for motivations for this.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|trimMapNulls
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dnMap
parameter_list|,
name|boolean
name|retrieveMapNullsAsEmptyStrings
parameter_list|)
block|{
if|if
condition|(
name|dnMap
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Must be deterministic order map - see HIVE-8707
comment|//   => we use Maps.newLinkedHashMap instead of Maps.newHashMap
if|if
condition|(
name|retrieveMapNullsAsEmptyStrings
condition|)
block|{
comment|// convert any nulls present in map values to empty strings - this is done in the case
comment|// of backing dbs like oracle which persist empty strings as nulls.
return|return
name|Maps
operator|.
name|newLinkedHashMap
argument_list|(
name|Maps
operator|.
name|transformValues
argument_list|(
name|dnMap
argument_list|,
name|transFormNullsToEmptyString
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
comment|// prune any nulls present in map values - this is the typical case.
return|return
name|Maps
operator|.
name|newLinkedHashMap
argument_list|(
name|Maps
operator|.
name|filterValues
argument_list|(
name|dnMap
argument_list|,
name|Predicates
operator|.
name|notNull
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|// given a list of partStats, this function will give you an aggr stats
specifier|public
specifier|static
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|aggrPartitionStats
parameter_list|(
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|partStats
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|boolean
name|useDensityFunctionForNDVEstimation
parameter_list|,
name|double
name|ndvTuner
parameter_list|)
throws|throws
name|MetaException
block|{
comment|// 1. group by the stats by colNames
comment|// map the colName to List<ColumnStatistics>
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ColumnStatistics
name|css
range|:
name|partStats
control|)
block|{
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|objs
init|=
name|css
operator|.
name|getStatsObj
argument_list|()
decl_stmt|;
for|for
control|(
name|ColumnStatisticsObj
name|obj
range|:
name|objs
control|)
block|{
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|singleObj
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|singleObj
operator|.
name|add
argument_list|(
name|obj
argument_list|)
expr_stmt|;
name|ColumnStatistics
name|singleCS
init|=
operator|new
name|ColumnStatistics
argument_list|(
name|css
operator|.
name|getStatsDesc
argument_list|()
argument_list|,
name|singleObj
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|map
operator|.
name|containsKey
argument_list|(
name|obj
operator|.
name|getColName
argument_list|()
argument_list|)
condition|)
block|{
name|map
operator|.
name|put
argument_list|(
name|obj
operator|.
name|getColName
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|ColumnStatistics
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|map
operator|.
name|get
argument_list|(
name|obj
operator|.
name|getColName
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|singleCS
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|MetaStoreUtils
operator|.
name|aggrPartitionStats
argument_list|(
name|map
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partNames
argument_list|,
name|colNames
argument_list|,
name|useDensityFunctionForNDVEstimation
argument_list|,
name|ndvTuner
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|aggrPartitionStats
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
argument_list|>
name|map
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
specifier|final
name|boolean
name|useDensityFunctionForNDVEstimation
parameter_list|,
specifier|final
name|double
name|ndvTuner
parameter_list|)
throws|throws
name|MetaException
block|{
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|colStats
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// 2. Aggregate stats for each column in a separate thread
if|if
condition|(
name|map
operator|.
name|size
argument_list|()
operator|<
literal|1
condition|)
block|{
comment|//stats are absent in RDBMS
name|LOG
operator|.
name|debug
argument_list|(
literal|"No stats data found for: dbName="
operator|+
name|dbName
operator|+
literal|" tblName="
operator|+
name|tableName
operator|+
literal|" partNames= "
operator|+
name|partNames
operator|+
literal|" colNames="
operator|+
name|colNames
argument_list|)
expr_stmt|;
return|return
name|colStats
return|;
block|}
specifier|final
name|ExecutorService
name|pool
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|map
operator|.
name|size
argument_list|()
argument_list|,
literal|16
argument_list|)
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"aggr-col-stats-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Future
argument_list|<
name|ColumnStatisticsObj
argument_list|>
argument_list|>
name|futures
init|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|futures
operator|.
name|add
argument_list|(
name|pool
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|ColumnStatisticsObj
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ColumnStatisticsObj
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|css
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|ColumnStatsAggregator
name|aggregator
init|=
name|ColumnStatsAggregatorFactory
operator|.
name|getColumnStatsAggregator
argument_list|(
name|css
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getStatsObj
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getStatsData
argument_list|()
operator|.
name|getSetField
argument_list|()
argument_list|,
name|useDensityFunctionForNDVEstimation
argument_list|,
name|ndvTuner
argument_list|)
decl_stmt|;
name|ColumnStatisticsObj
name|statsObj
init|=
name|aggregator
operator|.
name|aggregate
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|partNames
argument_list|,
name|css
argument_list|)
decl_stmt|;
return|return
name|statsObj
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
for|for
control|(
name|Future
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|future
range|:
name|futures
control|)
block|{
try|try
block|{
name|colStats
operator|.
name|add
argument_list|(
name|future
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
decl||
name|ExecutionException
name|e
parameter_list|)
block|{
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Time for aggr col stats in seconds: {} Threads used: {}"
argument_list|,
operator|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
operator|(
name|double
operator|)
name|start
operator|)
operator|)
operator|/
literal|1000
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|map
operator|.
name|size
argument_list|()
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|colStats
return|;
block|}
specifier|public
specifier|static
name|double
name|decimalToDouble
parameter_list|(
name|Decimal
name|decimal
parameter_list|)
block|{
return|return
operator|new
name|BigDecimal
argument_list|(
operator|new
name|BigInteger
argument_list|(
name|decimal
operator|.
name|getUnscaled
argument_list|()
argument_list|)
argument_list|,
name|decimal
operator|.
name|getScale
argument_list|()
argument_list|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
index|[]
name|getQualifiedName
parameter_list|(
name|String
name|defaultDbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|String
index|[]
name|names
init|=
name|tableName
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
if|if
condition|(
name|names
operator|.
name|length
operator|==
literal|1
condition|)
block|{
return|return
operator|new
name|String
index|[]
block|{
name|defaultDbName
block|,
name|tableName
block|}
return|;
block|}
return|return
name|names
return|;
block|}
specifier|public
specifier|static
name|void
name|validatePartitionNameCharacters
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|,
name|Pattern
name|partitionValidationPattern
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|invalidPartitionVal
init|=
name|getPartitionValWithInvalidCharacter
argument_list|(
name|partVals
argument_list|,
name|partitionValidationPattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|invalidPartitionVal
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Partition value '"
operator|+
name|invalidPartitionVal
operator|+
literal|"' contains a character "
operator|+
literal|"not matched by whitelist pattern '"
operator|+
name|partitionValidationPattern
operator|.
name|toString
argument_list|()
operator|+
literal|"'.  "
operator|+
literal|"(configure with "
operator|+
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|PARTITION_NAME_WHITELIST_PATTERN
operator|.
name|getVarname
argument_list|()
operator|+
literal|")"
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|String
name|getPartitionValWithInvalidCharacter
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|,
name|Pattern
name|partitionValidationPattern
parameter_list|)
block|{
if|if
condition|(
name|partitionValidationPattern
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|String
name|partVal
range|:
name|partVals
control|)
block|{
if|if
condition|(
operator|!
name|partitionValidationPattern
operator|.
name|matcher
argument_list|(
name|partVal
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|partVal
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Produce a hash for the storage descriptor    * @param sd storage descriptor to hash    * @param md message descriptor to use to generate the hash    * @return the hash as a byte array    */
specifier|public
specifier|static
name|byte
index|[]
name|hashStorageDescriptor
parameter_list|(
name|StorageDescriptor
name|sd
parameter_list|,
name|MessageDigest
name|md
parameter_list|)
block|{
comment|// Note all maps and lists have to be absolutely sorted.  Otherwise we'll produce different
comment|// results for hashes based on the OS or JVM being used.
name|md
operator|.
name|reset
argument_list|()
expr_stmt|;
for|for
control|(
name|FieldSchema
name|fs
range|:
name|sd
operator|.
name|getCols
argument_list|()
control|)
block|{
name|md
operator|.
name|update
argument_list|(
name|fs
operator|.
name|getName
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|md
operator|.
name|update
argument_list|(
name|fs
operator|.
name|getType
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|getComment
argument_list|()
operator|!=
literal|null
condition|)
name|md
operator|.
name|update
argument_list|(
name|fs
operator|.
name|getComment
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sd
operator|.
name|getInputFormat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|md
operator|.
name|update
argument_list|(
name|sd
operator|.
name|getInputFormat
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sd
operator|.
name|getOutputFormat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|md
operator|.
name|update
argument_list|(
name|sd
operator|.
name|getOutputFormat
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|md
operator|.
name|update
argument_list|(
name|sd
operator|.
name|isCompressed
argument_list|()
condition|?
literal|"true"
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
else|:
literal|"false"
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|md
operator|.
name|update
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|sd
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SerDeInfo
name|serde
init|=
name|sd
operator|.
name|getSerdeInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|serde
operator|.
name|getName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|md
operator|.
name|update
argument_list|(
name|serde
operator|.
name|getName
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serde
operator|.
name|getSerializationLib
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|md
operator|.
name|update
argument_list|(
name|serde
operator|.
name|getSerializationLib
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serde
operator|.
name|getParameters
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SortedMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|serde
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|param
range|:
name|params
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|md
operator|.
name|update
argument_list|(
name|param
operator|.
name|getKey
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|md
operator|.
name|update
argument_list|(
name|param
operator|.
name|getValue
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|sd
operator|.
name|getBucketCols
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|sd
operator|.
name|getBucketCols
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|bucket
range|:
name|bucketCols
control|)
name|md
operator|.
name|update
argument_list|(
name|bucket
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sd
operator|.
name|getSortCols
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SortedSet
argument_list|<
name|Order
argument_list|>
name|orders
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|sd
operator|.
name|getSortCols
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Order
name|order
range|:
name|orders
control|)
block|{
name|md
operator|.
name|update
argument_list|(
name|order
operator|.
name|getCol
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|md
operator|.
name|update
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|order
operator|.
name|getOrder
argument_list|()
argument_list|)
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|sd
operator|.
name|getSkewedInfo
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SkewedInfo
name|skewed
init|=
name|sd
operator|.
name|getSkewedInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|skewed
operator|.
name|getSkewedColNames
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|colnames
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|skewed
operator|.
name|getSkewedColNames
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|colname
range|:
name|colnames
control|)
name|md
operator|.
name|update
argument_list|(
name|colname
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|skewed
operator|.
name|getSkewedColValues
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|sortedOuterList
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|innerList
range|:
name|skewed
operator|.
name|getSkewedColValues
argument_list|()
control|)
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|sortedInnerList
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|innerList
argument_list|)
decl_stmt|;
name|sortedOuterList
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
operator|.
name|join
argument_list|(
name|sortedInnerList
argument_list|,
literal|"."
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|colval
range|:
name|sortedOuterList
control|)
name|md
operator|.
name|update
argument_list|(
name|colval
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|skewed
operator|.
name|getSkewedColValueLocationMaps
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SortedMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sortedMap
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|,
name|String
argument_list|>
name|smap
range|:
name|skewed
operator|.
name|getSkewedColValueLocationMaps
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|sortedKey
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|smap
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|sortedMap
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
operator|.
name|join
argument_list|(
name|sortedKey
argument_list|,
literal|"."
argument_list|)
argument_list|,
name|smap
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|sortedMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|md
operator|.
name|update
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|md
operator|.
name|update
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|md
operator|.
name|update
argument_list|(
name|sd
operator|.
name|isStoredAsSubDirectories
argument_list|()
condition|?
literal|"true"
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
else|:
literal|"false"
operator|.
name|getBytes
argument_list|(
name|ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|md
operator|.
name|digest
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getColumnNamesForTable
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|FieldSchema
argument_list|>
name|colsIterator
init|=
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getColsIterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|colsIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|colNames
operator|.
name|add
argument_list|(
name|colsIterator
operator|.
name|next
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|colNames
return|;
block|}
block|}
end_class

end_unit

