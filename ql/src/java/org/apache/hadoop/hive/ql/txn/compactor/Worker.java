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
name|txn
operator|.
name|compactor
package|;
end_package

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
name|ValidTxnList
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
name|metastore
operator|.
name|Warehouse
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
name|Partition
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
name|txn
operator|.
name|CompactionInfo
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
name|txn
operator|.
name|CompactionTxnHandler
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
name|CommandNeedRetryException
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
name|Driver
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|session
operator|.
name|SessionState
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
name|security
operator|.
name|UserGroupInformation
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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * A class to do compactions.  This will run in a separate thread.  It will spin on the  * compaction queue and look for new work to do.  */
end_comment

begin_class
specifier|public
class|class
name|Worker
extends|extends
name|CompactorThread
block|{
specifier|static
specifier|final
specifier|private
name|String
name|CLASS_NAME
init|=
name|Worker
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|long
name|SLEEP_TIME
init|=
literal|5000
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|int
name|baseThreadNum
init|=
literal|10002
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
comment|/**    * Get the hostname that this worker is run on.  Made static and public so that other classes    * can use the same method to know what host their worker threads are running on.    * @return hostname    */
specifier|public
specifier|static
name|String
name|hostname
parameter_list|()
block|{
try|try
block|{
return|return
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
operator|.
name|getHostName
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to resolve my host name "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|//todo: this doesn;t check if compaction is already running (even though Initiator does but we
comment|// don't go  through Initiator for user initiated compactions)
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
do|do
block|{
name|boolean
name|launchedJob
init|=
literal|false
decl_stmt|;
comment|// Make sure nothing escapes this run method and kills the metastore at large,
comment|// so wrap it in a big catch Throwable statement.
try|try
block|{
specifier|final
name|CompactionInfo
name|ci
init|=
name|txnHandler
operator|.
name|findNextToCompact
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|ci
operator|==
literal|null
operator|&&
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
continue|continue;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Worker thread sleep interrupted "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
comment|// Find the table we will be working with.
name|Table
name|t1
init|=
literal|null
decl_stmt|;
try|try
block|{
name|t1
operator|=
name|resolveTable
argument_list|(
name|ci
argument_list|)
expr_stmt|;
if|if
condition|(
name|t1
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Unable to find table "
operator|+
name|ci
operator|.
name|getFullTableName
argument_list|()
operator|+
literal|", assuming it was dropped and moving on."
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|markCleaned
argument_list|(
name|ci
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|txnHandler
operator|.
name|markCleaned
argument_list|(
name|ci
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// This chicanery is to get around the fact that the table needs to be final in order to
comment|// go into the doAs below.
specifier|final
name|Table
name|t
init|=
name|t1
decl_stmt|;
comment|// Find the partition we will be working with, if there is one.
name|Partition
name|p
init|=
literal|null
decl_stmt|;
try|try
block|{
name|p
operator|=
name|resolvePartition
argument_list|(
name|ci
argument_list|)
expr_stmt|;
if|if
condition|(
name|p
operator|==
literal|null
operator|&&
name|ci
operator|.
name|partName
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Unable to find partition "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
operator|+
literal|", assuming it was dropped and moving on."
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|markCleaned
argument_list|(
name|ci
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|txnHandler
operator|.
name|markCleaned
argument_list|(
name|ci
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Find the appropriate storage descriptor
specifier|final
name|StorageDescriptor
name|sd
init|=
name|resolveStorageDescriptor
argument_list|(
name|t
argument_list|,
name|p
argument_list|)
decl_stmt|;
comment|// Check that the table or partition isn't sorted, as we don't yet support that.
if|if
condition|(
name|sd
operator|.
name|getSortCols
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|sd
operator|.
name|getSortCols
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Attempt to compact sorted table, which is not yet supported!"
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|markCleaned
argument_list|(
name|ci
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|boolean
name|isMajor
init|=
name|ci
operator|.
name|isMajorCompaction
argument_list|()
decl_stmt|;
specifier|final
name|ValidTxnList
name|txns
init|=
name|CompactionTxnHandler
operator|.
name|createValidCompactTxnList
argument_list|(
name|txnHandler
operator|.
name|getOpenTxnsInfo
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"ValidCompactTxnList: "
operator|+
name|txns
operator|.
name|writeToString
argument_list|()
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|setCompactionHighestTxnId
argument_list|(
name|ci
argument_list|,
name|txns
operator|.
name|getHighWatermark
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|StringBuilder
name|jobName
init|=
operator|new
name|StringBuilder
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|jobName
operator|.
name|append
argument_list|(
literal|"-compactor-"
argument_list|)
expr_stmt|;
name|jobName
operator|.
name|append
argument_list|(
name|ci
operator|.
name|getFullPartitionName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Determine who to run as
name|String
name|runAs
decl_stmt|;
if|if
condition|(
name|ci
operator|.
name|runAs
operator|==
literal|null
condition|)
block|{
name|runAs
operator|=
name|findUserToRunAs
argument_list|(
name|sd
operator|.
name|getLocation
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|setRunAs
argument_list|(
name|ci
operator|.
name|id
argument_list|,
name|runAs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|runAs
operator|=
name|ci
operator|.
name|runAs
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting "
operator|+
name|ci
operator|.
name|type
operator|.
name|toString
argument_list|()
operator|+
literal|" compaction for "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|StatsUpdater
name|su
init|=
name|StatsUpdater
operator|.
name|init
argument_list|(
name|ci
argument_list|,
name|txnHandler
operator|.
name|findColumnsWithStats
argument_list|(
name|ci
argument_list|)
argument_list|,
name|conf
argument_list|,
name|runJobAsSelf
argument_list|(
name|runAs
argument_list|)
condition|?
name|runAs
else|:
name|t
operator|.
name|getOwner
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|CompactorMR
name|mr
init|=
operator|new
name|CompactorMR
argument_list|()
decl_stmt|;
name|launchedJob
operator|=
literal|true
expr_stmt|;
try|try
block|{
if|if
condition|(
name|runJobAsSelf
argument_list|(
name|runAs
argument_list|)
condition|)
block|{
name|mr
operator|.
name|run
argument_list|(
name|conf
argument_list|,
name|jobName
operator|.
name|toString
argument_list|()
argument_list|,
name|t
argument_list|,
name|sd
argument_list|,
name|txns
argument_list|,
name|ci
argument_list|,
name|su
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|createProxyUser
argument_list|(
name|t
operator|.
name|getOwner
argument_list|()
argument_list|,
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
argument_list|)
decl_stmt|;
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|mr
operator|.
name|run
argument_list|(
name|conf
argument_list|,
name|jobName
operator|.
name|toString
argument_list|()
argument_list|,
name|t
argument_list|,
name|sd
argument_list|,
name|txns
argument_list|,
name|ci
argument_list|,
name|su
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|txnHandler
operator|.
name|markCompacted
argument_list|(
name|ci
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught exception while trying to compact "
operator|+
name|ci
operator|+
literal|".  Marking clean to avoid repeated failures, "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|markFailed
argument_list|(
name|ci
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught an exception in the main loop of compactor worker "
operator|+
name|name
operator|+
literal|", "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// If we didn't try to launch a job it either means there was no work to do or we got
comment|// here as the result of a communication failure with the DB.  Either way we want to wait
comment|// a bit before we restart the loop.
if|if
condition|(
operator|!
name|launchedJob
operator|&&
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_TIME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{         }
block|}
block|}
do|while
condition|(
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
do|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|AtomicBoolean
name|stop
parameter_list|,
name|AtomicBoolean
name|looped
parameter_list|)
throws|throws
name|MetaException
block|{
name|super
operator|.
name|init
argument_list|(
name|stop
argument_list|,
name|looped
argument_list|)
expr_stmt|;
name|StringBuilder
name|name
init|=
operator|new
name|StringBuilder
argument_list|(
name|hostname
argument_list|()
argument_list|)
decl_stmt|;
name|name
operator|.
name|append
argument_list|(
literal|"-"
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
operator|.
name|toString
argument_list|()
expr_stmt|;
name|setName
argument_list|(
name|name
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|final
class|class
name|StatsUpdater
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|StatsUpdater
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|StatsUpdater
name|init
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnListForStats
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|String
name|userName
parameter_list|)
block|{
return|return
operator|new
name|StatsUpdater
argument_list|(
name|ci
argument_list|,
name|columnListForStats
argument_list|,
name|conf
argument_list|,
name|userName
argument_list|)
return|;
block|}
comment|/**      * list columns for which to compute stats.  This maybe empty which means no stats gathering      * is needed.      */
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnList
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|String
name|userName
decl_stmt|;
specifier|private
specifier|final
name|CompactionInfo
name|ci
decl_stmt|;
specifier|private
name|StatsUpdater
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnListForStats
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|String
name|userName
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|userName
operator|=
name|userName
expr_stmt|;
name|this
operator|.
name|ci
operator|=
name|ci
expr_stmt|;
if|if
condition|(
operator|!
name|ci
operator|.
name|isMajorCompaction
argument_list|()
operator|||
name|columnListForStats
operator|==
literal|null
operator|||
name|columnListForStats
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|columnList
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
return|return;
block|}
name|columnList
operator|=
name|columnListForStats
expr_stmt|;
block|}
comment|/**      * todo: what should this do on failure?  Should it rethrow? Invalidate stats?      */
name|void
name|gatherStats
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|ci
operator|.
name|isMajorCompaction
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|columnList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No existing stats for "
operator|+
name|ci
operator|.
name|dbname
operator|+
literal|"."
operator|+
name|ci
operator|.
name|tableName
operator|+
literal|" found.  Will not run analyze."
argument_list|)
expr_stmt|;
return|return;
comment|//nothing to do
block|}
comment|//e.g. analyze table page_view partition(dt='10/15/2014',country=’US’)
comment|// compute statistics for columns viewtime
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"analyze table "
argument_list|)
operator|.
name|append
argument_list|(
name|ci
operator|.
name|dbname
argument_list|)
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
operator|.
name|append
argument_list|(
name|ci
operator|.
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|ci
operator|.
name|partName
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" partition("
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionColumnValues
init|=
name|Warehouse
operator|.
name|makeEscSpecFromName
argument_list|(
name|ci
operator|.
name|partName
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
name|ent
range|:
name|partitionColumnValues
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|ent
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"='"
argument_list|)
operator|.
name|append
argument_list|(
name|ent
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|" compute statistics for columns "
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|columnList
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|colName
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|setLength
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|//remove trailing ,
name|LOG
operator|.
name|info
argument_list|(
literal|"running '"
operator|+
name|sb
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|Driver
name|d
init|=
operator|new
name|Driver
argument_list|(
name|conf
argument_list|,
name|userName
argument_list|)
decl_stmt|;
name|SessionState
name|localSession
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
name|localSession
operator|=
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|SessionState
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|CommandProcessorResponse
name|cpr
init|=
name|d
operator|.
name|run
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not update stats for table "
operator|+
name|ci
operator|.
name|getFullTableName
argument_list|()
operator|+
operator|(
name|ci
operator|.
name|partName
operator|==
literal|null
condition|?
literal|""
else|:
literal|"/"
operator|+
name|ci
operator|.
name|partName
operator|)
operator|+
literal|" due to: "
operator|+
name|cpr
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|CommandNeedRetryException
name|cnre
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not update stats for table "
operator|+
name|ci
operator|.
name|getFullTableName
argument_list|()
operator|+
operator|(
name|ci
operator|.
name|partName
operator|==
literal|null
condition|?
literal|""
else|:
literal|"/"
operator|+
name|ci
operator|.
name|partName
operator|)
operator|+
literal|" due to: "
operator|+
name|cnre
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|localSession
operator|!=
literal|null
condition|)
block|{
name|localSession
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

