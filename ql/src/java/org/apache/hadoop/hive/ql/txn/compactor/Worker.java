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
name|hive
operator|.
name|common
operator|.
name|JavaUtils
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
name|ValidCompactorWriteIdList
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
name|common
operator|.
name|ValidWriteIdList
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
name|HiveMetaStoreUtils
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
name|IMetaStoreClient
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
name|MetaStoreThread
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
name|api
operator|.
name|TxnType
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
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
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
name|TxnUtils
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
name|QueryState
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
name|hive
operator|.
name|ql
operator|.
name|stats
operator|.
name|StatsUtils
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
name|TimeUnit
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
name|RemoteCompactorThread
implements|implements
name|MetaStoreThread
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
literal|10000
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
name|workerName
decl_stmt|;
specifier|private
name|JobConf
name|mrJob
decl_stmt|;
comment|// the MR job for compaction
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting Worker thread"
argument_list|)
expr_stmt|;
do|do
block|{
name|boolean
name|launchedJob
init|=
literal|false
decl_stmt|;
comment|// Make sure nothing escapes this run method and kills the metastore at large,
comment|// so wrap it in a big catch Throwable statement.
name|CompactionHeartbeater
name|heartbeater
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|msc
operator|==
literal|null
condition|)
block|{
name|msc
operator|=
name|HiveMetaStoreUtils
operator|.
name|getHiveMetastoreClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|final
name|CompactionInfo
name|ci
init|=
name|CompactionInfo
operator|.
name|optionalCompactionInfoStructToInfo
argument_list|(
name|msc
operator|.
name|findNextCompact
argument_list|(
name|workerName
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Processing compaction request "
operator|+
name|ci
argument_list|)
expr_stmt|;
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
name|msc
operator|.
name|markCleaned
argument_list|(
name|CompactionInfo
operator|.
name|compactionInfoToStruct
argument_list|(
name|ci
argument_list|)
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
name|msc
operator|.
name|markCleaned
argument_list|(
name|CompactionInfo
operator|.
name|compactionInfoToStruct
argument_list|(
name|ci
argument_list|)
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
name|msc
operator|.
name|markCleaned
argument_list|(
name|CompactionInfo
operator|.
name|compactionInfoToStruct
argument_list|(
name|ci
argument_list|)
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
name|msc
operator|.
name|markCleaned
argument_list|(
name|CompactionInfo
operator|.
name|compactionInfoToStruct
argument_list|(
name|ci
argument_list|)
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
name|msc
operator|.
name|markCleaned
argument_list|(
name|CompactionInfo
operator|.
name|compactionInfoToStruct
argument_list|(
name|ci
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|String
name|fullTableName
init|=
name|TxnUtils
operator|.
name|getFullTableName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|,
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
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
name|ci
operator|.
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
block|}
comment|/**          * we cannot have Worker use HiveTxnManager (which is on ThreadLocal) since          * then the Driver would already have the an open txn but then this txn would have          * multiple statements in it (for query based compactor) which is not supported (and since          * this case some of the statements are DDL, even in the future will not be allowed in a          * multi-stmt txn. {@link Driver#setCompactionWriteIds(ValidWriteIdList, long)} */
name|long
name|compactorTxnId
init|=
name|msc
operator|.
name|openTxn
argument_list|(
name|ci
operator|.
name|runAs
argument_list|,
name|TxnType
operator|.
name|COMPACTION
argument_list|)
decl_stmt|;
name|heartbeater
operator|=
operator|new
name|CompactionHeartbeater
argument_list|(
name|compactorTxnId
argument_list|,
name|fullTableName
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|heartbeater
operator|.
name|start
argument_list|()
expr_stmt|;
name|ValidTxnList
name|validTxnList
init|=
name|msc
operator|.
name|getValidTxns
argument_list|(
name|compactorTxnId
argument_list|)
decl_stmt|;
comment|//with this ValidWriteIdList is capped at whatever HWM validTxnList has
specifier|final
name|ValidCompactorWriteIdList
name|tblValidWriteIds
init|=
name|TxnUtils
operator|.
name|createValidCompactWriteIdList
argument_list|(
name|msc
operator|.
name|getValidWriteIds
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|fullTableName
argument_list|)
argument_list|,
name|validTxnList
operator|.
name|writeToString
argument_list|()
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"ValidCompactWriteIdList: "
operator|+
name|tblValidWriteIds
operator|.
name|writeToString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ValidTxnList
operator|.
name|VALID_TXNS_KEY
argument_list|,
name|validTxnList
operator|.
name|writeToString
argument_list|()
argument_list|)
expr_stmt|;
name|ci
operator|.
name|highestWriteId
operator|=
name|tblValidWriteIds
operator|.
name|getHighWatermark
argument_list|()
expr_stmt|;
comment|//this writes TXN_COMPONENTS to ensure that if compactorTxnId fails, we keep metadata about
comment|//it until after any data written by it are physically removed
name|msc
operator|.
name|updateCompactorState
argument_list|(
name|CompactionInfo
operator|.
name|compactionInfoToStruct
argument_list|(
name|ci
argument_list|)
argument_list|,
name|compactorTxnId
argument_list|)
expr_stmt|;
specifier|final
name|StringBuilder
name|jobName
init|=
operator|new
name|StringBuilder
argument_list|(
name|workerName
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
operator|+
literal|" in "
operator|+
name|JavaUtils
operator|.
name|txnIdToString
argument_list|(
name|compactorTxnId
argument_list|)
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
name|msc
operator|.
name|findColumnsWithStats
argument_list|(
name|CompactionInfo
operator|.
name|compactionInfoToStruct
argument_list|(
name|ci
argument_list|)
argument_list|)
argument_list|,
name|conf
argument_list|,
name|runJobAsSelf
argument_list|(
name|ci
operator|.
name|runAs
argument_list|)
condition|?
name|ci
operator|.
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
name|ci
operator|.
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
name|p
argument_list|,
name|sd
argument_list|,
name|tblValidWriteIds
argument_list|,
name|ci
argument_list|,
name|su
argument_list|,
name|msc
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
specifier|final
name|Partition
name|fp
init|=
name|p
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
name|fp
argument_list|,
name|sd
argument_list|,
name|tblValidWriteIds
argument_list|,
name|ci
argument_list|,
name|su
argument_list|,
name|msc
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
try|try
block|{
name|FileSystem
operator|.
name|closeAllForUGI
argument_list|(
name|ugi
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not clean up file-system handles for UGI: "
operator|+
name|ugi
operator|+
literal|" for "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
name|heartbeater
operator|.
name|cancel
argument_list|()
expr_stmt|;
name|msc
operator|.
name|markCompacted
argument_list|(
name|CompactionInfo
operator|.
name|compactionInfoToStruct
argument_list|(
name|ci
argument_list|)
argument_list|)
expr_stmt|;
name|msc
operator|.
name|commitTxn
argument_list|(
name|compactorTxnId
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|)
condition|)
block|{
name|mrJob
operator|=
name|mr
operator|.
name|getMrJob
argument_list|()
expr_stmt|;
block|}
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
literal|".  Marking failed to avoid repeated failures, "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|msc
operator|.
name|markFailed
argument_list|(
name|CompactionInfo
operator|.
name|compactionInfoToStruct
argument_list|(
name|ci
argument_list|)
argument_list|)
expr_stmt|;
name|msc
operator|.
name|abortTxns
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|compactorTxnId
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|TException
decl||
name|IOException
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught an exception in the main loop of compactor worker "
operator|+
name|workerName
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
if|if
condition|(
name|msc
operator|!=
literal|null
condition|)
block|{
name|msc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|msc
operator|=
literal|null
expr_stmt|;
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
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Interrupted while sleeping to instantiate metastore client"
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
name|workerName
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
finally|finally
block|{
if|if
condition|(
name|heartbeater
operator|!=
literal|null
condition|)
block|{
name|heartbeater
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
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
name|Exception
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
name|workerName
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
specifier|public
name|JobConf
name|getMrJob
parameter_list|()
block|{
return|return
name|mrJob
return|;
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
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|//so that Driver doesn't think it's arleady in a transaction
name|this
operator|.
name|conf
operator|.
name|unset
argument_list|(
name|ValidTxnList
operator|.
name|VALID_TXNS_KEY
argument_list|)
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
comment|/**      * This doesn't throw any exceptions because we don't want the Compaction to appear as failed      * if stats gathering fails since this prevents Cleaner from doing it's job and if there are      * multiple failures, auto initiated compactions will stop which leads to problems that are      * much worse than stale stats.      *      * todo: longer term we should write something COMPACTION_QUEUE.CQ_META_INFO.  This is a binary      * field so need to figure out the msg format and how to surface it in SHOW COMPACTIONS, etc      */
name|void
name|gatherStats
parameter_list|()
block|{
try|try
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
name|ci
operator|+
literal|": No existing stats found.  Will not run analyze."
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
name|StatsUtils
operator|.
name|getFullyQualifiedTableName
argument_list|(
name|ci
operator|.
name|dbname
argument_list|,
name|ci
operator|.
name|tableName
argument_list|)
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
literal|"',"
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
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
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
name|ci
operator|+
literal|": running '"
operator|+
name|sb
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|""
argument_list|)
expr_stmt|;
comment|//todo: use DriverUtils.runOnDriver() here
name|Driver
name|d
init|=
operator|new
name|Driver
argument_list|(
operator|new
name|QueryState
operator|.
name|Builder
argument_list|()
operator|.
name|withGenerateNewQueryId
argument_list|(
literal|true
argument_list|)
operator|.
name|withHiveConf
argument_list|(
name|conf
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|userName
argument_list|)
decl_stmt|;
name|SessionState
name|localSession
init|=
literal|null
decl_stmt|;
try|try
block|{
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
operator|new
name|SessionState
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|localSession
argument_list|)
expr_stmt|;
block|}
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
name|LOG
operator|.
name|warn
argument_list|(
name|ci
operator|+
literal|": "
operator|+
name|sb
operator|.
name|toString
argument_list|()
operator|+
literal|" failed due to: "
operator|+
name|cpr
argument_list|)
expr_stmt|;
block|}
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
try|try
block|{
name|localSession
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|ci
operator|+
literal|": localSession.close() failed due to: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
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
name|ci
operator|+
literal|": gatherStats("
operator|+
name|ci
operator|.
name|dbname
operator|+
literal|","
operator|+
name|ci
operator|.
name|tableName
operator|+
literal|","
operator|+
name|ci
operator|.
name|partName
operator|+
literal|") failed due to: "
operator|+
name|t
operator|.
name|getMessage
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|static
specifier|final
class|class
name|CompactionHeartbeater
extends|extends
name|Thread
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
name|CompactionHeartbeater
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|long
name|compactorTxnId
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|long
name|interval
decl_stmt|;
specifier|public
name|CompactionHeartbeater
parameter_list|(
name|long
name|compactorTxnId
parameter_list|,
name|String
name|tableName
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|compactorTxnId
operator|=
name|compactorTxnId
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|interval
operator|=
name|MetastoreConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|TXN_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|/
literal|2
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|setPriority
argument_list|(
name|MIN_PRIORITY
argument_list|)
expr_stmt|;
name|setName
argument_list|(
literal|"CompactionHeartbeater-"
operator|+
name|compactorTxnId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
comment|// We need to create our own metastore client since the thrifts clients
comment|// are not thread safe.
name|IMetaStoreClient
name|msc
init|=
name|HiveMetaStoreUtils
operator|.
name|getHiveMetastoreClient
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Heartbeating compaction transaction id {} for table: {}"
argument_list|,
name|compactorTxnId
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
name|msc
operator|.
name|heartbeat
argument_list|(
name|compactorTxnId
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|interval
argument_list|)
expr_stmt|;
block|}
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
literal|"Error while heartbeating txn {} in {}, error: "
argument_list|,
name|compactorTxnId
argument_list|,
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|cancel
parameter_list|()
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Successfully stop the heartbeating the transaction {}"
argument_list|,
name|this
operator|.
name|compactorTxnId
argument_list|)
expr_stmt|;
name|this
operator|.
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

