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
name|ql
operator|.
name|stats
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|FileSystem
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
name|hive
operator|.
name|common
operator|.
name|StatsSetupConst
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|plan
operator|.
name|Statistics
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
name|plan
operator|.
name|Statistics
operator|.
name|State
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
name|util
operator|.
name|concurrent
operator|.
name|MoreExecutors
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

begin_class
specifier|public
class|class
name|BasicStats
block|{
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
name|BasicStats
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
class|class
name|Factory
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|IStatsEnhancer
argument_list|>
name|enhancers
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|Factory
parameter_list|(
name|IStatsEnhancer
modifier|...
name|enhancers
parameter_list|)
block|{
name|this
operator|.
name|enhancers
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|enhancers
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addEnhancer
parameter_list|(
name|IStatsEnhancer
name|enhancer
parameter_list|)
block|{
name|enhancers
operator|.
name|add
argument_list|(
name|enhancer
argument_list|)
expr_stmt|;
block|}
specifier|public
name|BasicStats
name|build
parameter_list|(
name|Partish
name|p
parameter_list|)
block|{
name|BasicStats
name|ret
init|=
operator|new
name|BasicStats
argument_list|(
name|p
argument_list|)
decl_stmt|;
for|for
control|(
name|IStatsEnhancer
name|enhancer
range|:
name|enhancers
control|)
block|{
name|ret
operator|.
name|apply
argument_list|(
name|enhancer
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
name|List
argument_list|<
name|BasicStats
argument_list|>
name|buildAll
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Collection
argument_list|<
name|Partish
argument_list|>
name|parts
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of partishes : "
operator|+
name|parts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|BasicStats
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|parts
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
for|for
control|(
name|Partish
name|partish
range|:
name|parts
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|build
argument_list|(
name|partish
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
name|List
argument_list|<
name|Future
argument_list|<
name|BasicStats
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|threads
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_FS_HANDLER_THREADS_COUNT
argument_list|)
decl_stmt|;
specifier|final
name|ExecutorService
name|pool
decl_stmt|;
if|if
condition|(
name|threads
operator|<=
literal|1
condition|)
block|{
name|pool
operator|=
name|MoreExecutors
operator|.
name|newDirectExecutorService
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|pool
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|threads
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
literal|"Get-Partitions-Size-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
specifier|final
name|Partish
name|part
range|:
name|parts
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
name|BasicStats
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|BasicStats
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|build
argument_list|(
name|part
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|futures
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|futures
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
decl||
name|ExecutionException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception in processing files "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
specifier|public
specifier|static
interface|interface
name|IStatsEnhancer
block|{
name|void
name|apply
parameter_list|(
name|BasicStats
name|stats
parameter_list|)
function_decl|;
block|}
specifier|public
specifier|static
class|class
name|SetMinRowNumber
implements|implements
name|IStatsEnhancer
block|{
annotation|@
name|Override
specifier|public
name|void
name|apply
parameter_list|(
name|BasicStats
name|stats
parameter_list|)
block|{
if|if
condition|(
name|stats
operator|.
name|getNumRows
argument_list|()
operator|==
literal|0
condition|)
block|{
name|stats
operator|.
name|setNumRows
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|SetMinRowNumber01
implements|implements
name|IStatsEnhancer
block|{
annotation|@
name|Override
specifier|public
name|void
name|apply
parameter_list|(
name|BasicStats
name|stats
parameter_list|)
block|{
if|if
condition|(
name|stats
operator|.
name|getNumRows
argument_list|()
operator|==
literal|0
operator|||
name|stats
operator|.
name|getNumRows
argument_list|()
operator|==
operator|-
literal|1
condition|)
block|{
name|stats
operator|.
name|setNumRows
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|RowNumEstimator
implements|implements
name|IStatsEnhancer
block|{
specifier|private
name|long
name|avgRowSize
decl_stmt|;
specifier|public
name|RowNumEstimator
parameter_list|(
name|long
name|avgRowSize
parameter_list|)
block|{
name|this
operator|.
name|avgRowSize
operator|=
name|avgRowSize
expr_stmt|;
if|if
condition|(
name|avgRowSize
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Estimated average row size: "
operator|+
name|avgRowSize
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|apply
parameter_list|(
name|BasicStats
name|stats
parameter_list|)
block|{
comment|// FIXME: there were different logic for part/table; merge these logics later
if|if
condition|(
name|stats
operator|.
name|partish
operator|.
name|getPartition
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|stats
operator|.
name|getNumRows
argument_list|()
operator|<
literal|0
operator|&&
name|avgRowSize
operator|>
literal|0
condition|)
block|{
name|stats
operator|.
name|setNumRows
argument_list|(
name|stats
operator|.
name|getDataSize
argument_list|()
operator|/
name|avgRowSize
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|avgRowSize
operator|>
literal|0
condition|)
block|{
name|long
name|rc
init|=
name|stats
operator|.
name|getNumRows
argument_list|()
decl_stmt|;
name|long
name|s
init|=
name|stats
operator|.
name|getDataSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|rc
operator|<=
literal|0
operator|&&
name|s
operator|>
literal|0
condition|)
block|{
name|rc
operator|=
name|s
operator|/
name|avgRowSize
expr_stmt|;
name|stats
operator|.
name|setNumRows
argument_list|(
name|rc
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|s
operator|<=
literal|0
operator|&&
name|rc
operator|>
literal|0
condition|)
block|{
name|s
operator|=
name|StatsUtils
operator|.
name|safeMult
argument_list|(
name|rc
argument_list|,
name|avgRowSize
argument_list|)
expr_stmt|;
name|stats
operator|.
name|setDataSize
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|stats
operator|.
name|getNumRows
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// FIXME: this promotion process should be removed later
if|if
condition|(
name|State
operator|.
name|PARTIAL
operator|.
name|morePreciseThan
argument_list|(
name|stats
operator|.
name|state
argument_list|)
condition|)
block|{
name|stats
operator|.
name|state
operator|=
name|State
operator|.
name|PARTIAL
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|DataSizeEstimator
implements|implements
name|IStatsEnhancer
block|{
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|float
name|deserFactor
decl_stmt|;
specifier|public
name|DataSizeEstimator
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|deserFactor
operator|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_DESERIALIZATION_FACTOR
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|apply
parameter_list|(
name|BasicStats
name|stats
parameter_list|)
block|{
name|long
name|ds
init|=
name|stats
operator|.
name|getRawDataSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|ds
operator|<=
literal|0
condition|)
block|{
name|ds
operator|=
name|stats
operator|.
name|getTotalFileSize
argument_list|()
expr_stmt|;
comment|// if data size is still 0 then get file size
if|if
condition|(
name|ds
operator|<=
literal|0
condition|)
block|{
name|Path
name|path
init|=
name|stats
operator|.
name|partish
operator|.
name|getPath
argument_list|()
decl_stmt|;
try|try
block|{
name|ds
operator|=
name|getFileSizeForPath
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|ds
operator|=
literal|0L
expr_stmt|;
block|}
block|}
name|ds
operator|=
call|(
name|long
call|)
argument_list|(
name|ds
operator|*
name|deserFactor
argument_list|)
expr_stmt|;
name|stats
operator|.
name|setDataSize
argument_list|(
name|ds
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|long
name|getFileSizeForPath
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|fs
operator|.
name|getContentSummary
argument_list|(
name|path
argument_list|)
operator|.
name|getLength
argument_list|()
return|;
block|}
block|}
specifier|private
name|Partish
name|partish
decl_stmt|;
specifier|private
name|long
name|rowCount
decl_stmt|;
specifier|private
name|long
name|totalSize
decl_stmt|;
specifier|private
name|long
name|rawDataSize
decl_stmt|;
specifier|private
name|long
name|currentNumRows
decl_stmt|;
specifier|private
name|long
name|currentDataSize
decl_stmt|;
specifier|private
name|long
name|currentFileSize
decl_stmt|;
specifier|private
name|Statistics
operator|.
name|State
name|state
decl_stmt|;
specifier|public
name|BasicStats
parameter_list|(
name|Partish
name|p
parameter_list|)
block|{
name|partish
operator|=
name|p
expr_stmt|;
name|rowCount
operator|=
name|parseLong
argument_list|(
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
expr_stmt|;
name|rawDataSize
operator|=
name|parseLong
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
expr_stmt|;
name|totalSize
operator|=
name|parseLong
argument_list|(
name|StatsSetupConst
operator|.
name|TOTAL_SIZE
argument_list|)
expr_stmt|;
name|currentNumRows
operator|=
name|rowCount
expr_stmt|;
name|currentDataSize
operator|=
name|rawDataSize
expr_stmt|;
name|currentFileSize
operator|=
name|totalSize
expr_stmt|;
if|if
condition|(
name|currentNumRows
operator|>
literal|0
condition|)
block|{
name|state
operator|=
name|State
operator|.
name|COMPLETE
expr_stmt|;
block|}
else|else
block|{
name|state
operator|=
name|State
operator|.
name|NONE
expr_stmt|;
block|}
block|}
specifier|public
name|BasicStats
parameter_list|(
name|List
argument_list|<
name|BasicStats
argument_list|>
name|partStats
parameter_list|)
block|{
name|partish
operator|=
literal|null
expr_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|nrIn
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|dsIn
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|fsIn
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|state
operator|=
operator|(
name|partStats
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|?
name|State
operator|.
name|COMPLETE
else|:
literal|null
expr_stmt|;
for|for
control|(
name|BasicStats
name|ps
range|:
name|partStats
control|)
block|{
name|nrIn
operator|.
name|add
argument_list|(
name|ps
operator|.
name|getNumRows
argument_list|()
argument_list|)
expr_stmt|;
name|dsIn
operator|.
name|add
argument_list|(
name|ps
operator|.
name|getDataSize
argument_list|()
argument_list|)
expr_stmt|;
name|fsIn
operator|.
name|add
argument_list|(
name|ps
operator|.
name|getTotalFileSize
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|state
operator|==
literal|null
condition|)
block|{
name|state
operator|=
name|ps
operator|.
name|getState
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|state
operator|=
name|state
operator|.
name|merge
argument_list|(
name|ps
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|currentNumRows
operator|=
name|StatsUtils
operator|.
name|getSumIgnoreNegatives
argument_list|(
name|nrIn
argument_list|)
expr_stmt|;
name|currentDataSize
operator|=
name|StatsUtils
operator|.
name|getSumIgnoreNegatives
argument_list|(
name|dsIn
argument_list|)
expr_stmt|;
name|currentFileSize
operator|=
name|StatsUtils
operator|.
name|getSumIgnoreNegatives
argument_list|(
name|fsIn
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getNumRows
parameter_list|()
block|{
return|return
name|currentNumRows
return|;
block|}
specifier|public
name|long
name|getDataSize
parameter_list|()
block|{
return|return
name|currentDataSize
return|;
block|}
specifier|public
name|Statistics
operator|.
name|State
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
name|void
name|apply
parameter_list|(
name|IStatsEnhancer
name|estimator
parameter_list|)
block|{
name|estimator
operator|.
name|apply
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|setNumRows
parameter_list|(
name|long
name|l
parameter_list|)
block|{
name|currentNumRows
operator|=
name|l
expr_stmt|;
block|}
specifier|protected
name|void
name|setDataSize
parameter_list|(
name|long
name|ds
parameter_list|)
block|{
name|currentDataSize
operator|=
name|ds
expr_stmt|;
block|}
specifier|protected
name|long
name|getTotalFileSize
parameter_list|()
block|{
return|return
name|currentFileSize
return|;
block|}
specifier|public
name|void
name|setTotalFileSize
parameter_list|(
specifier|final
name|long
name|totalFileSize
parameter_list|)
block|{
name|this
operator|.
name|currentFileSize
operator|=
name|totalFileSize
expr_stmt|;
block|}
specifier|protected
name|long
name|getRawDataSize
parameter_list|()
block|{
return|return
name|rawDataSize
return|;
block|}
specifier|private
name|long
name|parseLong
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|partish
operator|.
name|getPartParameters
argument_list|()
decl_stmt|;
name|long
name|result
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|params
operator|!=
literal|null
condition|)
block|{
name|String
name|val
init|=
name|params
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|val
argument_list|)
condition|)
block|{
try|try
block|{
name|result
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
comment|// Pass-through. This should not happen and we will LOG it,
comment|// but do not fail query.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error parsing {} value: {}"
argument_list|,
name|fieldName
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|BasicStats
name|buildFrom
parameter_list|(
name|List
argument_list|<
name|BasicStats
argument_list|>
name|partStats
parameter_list|)
block|{
return|return
operator|new
name|BasicStats
argument_list|(
name|partStats
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"BasicStats: %d, %d %s"
argument_list|,
name|getNumRows
argument_list|()
argument_list|,
name|getDataSize
argument_list|()
argument_list|,
name|getState
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

