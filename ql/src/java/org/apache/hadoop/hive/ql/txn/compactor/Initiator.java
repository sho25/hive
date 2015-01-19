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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|FileStatus
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
name|FileUtils
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
name|api
operator|.
name|CompactionRequest
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
name|CompactionType
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
name|ShowCompactRequest
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
name|ShowCompactResponse
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
name|ShowCompactResponseElement
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
name|hive_metastoreConstants
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
name|TxnHandler
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
name|io
operator|.
name|AcidUtils
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
comment|/**  * A class to initiate compactions.  This will run in a separate thread.  */
end_comment

begin_class
specifier|public
class|class
name|Initiator
extends|extends
name|CompactorThread
block|{
specifier|static
specifier|final
specifier|private
name|String
name|CLASS_NAME
init|=
name|Initiator
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|private
name|long
name|checkInterval
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// Make sure nothing escapes this run method and kills the metastore at large,
comment|// so wrap it in a big catch Throwable statement.
try|try
block|{
name|recoverFailedCompactions
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|int
name|abortedThreshold
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPACTOR_ABORTEDTXN_THRESHOLD
argument_list|)
decl_stmt|;
comment|// Make sure we run through the loop once before checking to stop as this makes testing
comment|// much easier.  The stop value is only for testing anyway and not used when called from
comment|// HiveMetaStore.
do|do
block|{
name|long
name|startedAt
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Wrap the inner parts of the loop in a catch throwable so that any errors in the loop
comment|// don't doom the entire thread.
try|try
block|{
name|ShowCompactResponse
name|currentCompactions
init|=
name|txnHandler
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
decl_stmt|;
name|ValidTxnList
name|txns
init|=
name|TxnHandler
operator|.
name|createValidTxnList
argument_list|(
name|txnHandler
operator|.
name|getOpenTxns
argument_list|()
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|CompactionInfo
argument_list|>
name|potentials
init|=
name|txnHandler
operator|.
name|findPotentialCompactions
argument_list|(
name|abortedThreshold
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found "
operator|+
name|potentials
operator|.
name|size
argument_list|()
operator|+
literal|" potential compactions, "
operator|+
literal|"checking to see if we should compact any of them"
argument_list|)
expr_stmt|;
for|for
control|(
name|CompactionInfo
name|ci
range|:
name|potentials
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Checking to see if we should compact "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|Table
name|t
init|=
name|resolveTable
argument_list|(
name|ci
argument_list|)
decl_stmt|;
comment|// check if no compaction set for this table
if|if
condition|(
name|noAutoCompactSet
argument_list|(
name|t
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|tableName
argument_list|(
name|t
argument_list|)
operator|+
literal|" marked true so we will not compact it."
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Check if we already have initiated or are working on a compaction for this partition
comment|// or table.  If so, skip it.  If we are just waiting on cleaning we can still check,
comment|// as it may be time to compact again even though we haven't cleaned.
if|if
condition|(
name|lookForCurrentCompactions
argument_list|(
name|currentCompactions
argument_list|,
name|ci
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found currently initiated or working compaction for "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
operator|+
literal|" so we will not initiate another compaction"
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Figure out who we should run the file operations as
name|Partition
name|p
init|=
name|resolvePartition
argument_list|(
name|ci
argument_list|)
decl_stmt|;
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
name|String
name|runAs
init|=
name|findUserToRunAs
argument_list|(
name|sd
operator|.
name|getLocation
argument_list|()
argument_list|,
name|t
argument_list|)
decl_stmt|;
name|CompactionType
name|compactionNeeded
init|=
name|checkForCompaction
argument_list|(
name|ci
argument_list|,
name|txns
argument_list|,
name|sd
argument_list|,
name|runAs
argument_list|)
decl_stmt|;
if|if
condition|(
name|compactionNeeded
operator|!=
literal|null
condition|)
name|requestCompaction
argument_list|(
name|ci
argument_list|,
name|runAs
argument_list|,
name|compactionNeeded
argument_list|)
expr_stmt|;
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
literal|"Caught exception while trying to determine if we should compact "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
operator|+
literal|".  Marking clean to avoid repeated failures, "
operator|+
literal|""
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|markCleaned
argument_list|(
name|ci
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Check for timed out remote workers.
name|recoverFailedCompactions
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Clean anything from the txns table that has no components left in txn_components.
name|txnHandler
operator|.
name|cleanEmptyAbortedTxns
argument_list|()
expr_stmt|;
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
literal|"Initiator loop caught unexpected exception this time through the loop: "
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
name|long
name|elapsedTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startedAt
decl_stmt|;
if|if
condition|(
name|elapsedTime
operator|>=
name|checkInterval
operator|||
name|stop
operator|.
name|get
argument_list|()
condition|)
continue|continue;
else|else
name|Thread
operator|.
name|sleep
argument_list|(
name|checkInterval
operator|-
name|elapsedTime
argument_list|)
expr_stmt|;
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
literal|"Caught an exception in the main loop of compactor initiator, exiting "
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
name|checkInterval
operator|=
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPACTOR_CHECK_INTERVAL
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|recoverFailedCompactions
parameter_list|(
name|boolean
name|remoteOnly
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
operator|!
name|remoteOnly
condition|)
name|txnHandler
operator|.
name|revokeFromLocalWorkers
argument_list|(
name|Worker
operator|.
name|hostname
argument_list|()
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|revokeTimedoutWorkers
argument_list|(
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPACTOR_WORKER_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Figure out if there are any currently running compactions on the same table or partition.
specifier|private
name|boolean
name|lookForCurrentCompactions
parameter_list|(
name|ShowCompactResponse
name|compactions
parameter_list|,
name|CompactionInfo
name|ci
parameter_list|)
block|{
if|if
condition|(
name|compactions
operator|.
name|getCompacts
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ShowCompactResponseElement
name|e
range|:
name|compactions
operator|.
name|getCompacts
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|e
operator|.
name|getState
argument_list|()
operator|.
name|equals
argument_list|(
name|TxnHandler
operator|.
name|CLEANING_RESPONSE
argument_list|)
operator|&&
name|e
operator|.
name|getDbname
argument_list|()
operator|.
name|equals
argument_list|(
name|ci
operator|.
name|dbname
argument_list|)
operator|&&
name|e
operator|.
name|getTablename
argument_list|()
operator|.
name|equals
argument_list|(
name|ci
operator|.
name|tableName
argument_list|)
operator|&&
operator|(
name|e
operator|.
name|getPartitionname
argument_list|()
operator|==
literal|null
operator|&&
name|ci
operator|.
name|partName
operator|==
literal|null
operator|||
name|e
operator|.
name|getPartitionname
argument_list|()
operator|.
name|equals
argument_list|(
name|ci
operator|.
name|partName
argument_list|)
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|CompactionType
name|checkForCompaction
parameter_list|(
specifier|final
name|CompactionInfo
name|ci
parameter_list|,
specifier|final
name|ValidTxnList
name|txns
parameter_list|,
specifier|final
name|StorageDescriptor
name|sd
parameter_list|,
specifier|final
name|String
name|runAs
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// If it's marked as too many aborted, we already know we need to compact
if|if
condition|(
name|ci
operator|.
name|tooManyAborts
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found too many aborted transactions for "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
operator|+
literal|", "
operator|+
literal|"initiating major compaction"
argument_list|)
expr_stmt|;
return|return
name|CompactionType
operator|.
name|MAJOR
return|;
block|}
if|if
condition|(
name|runJobAsSelf
argument_list|(
name|runAs
argument_list|)
condition|)
block|{
return|return
name|determineCompactionType
argument_list|(
name|ci
argument_list|,
name|txns
argument_list|,
name|sd
argument_list|)
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to initiate as user "
operator|+
name|runAs
argument_list|)
expr_stmt|;
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|createProxyUser
argument_list|(
name|runAs
argument_list|,
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|CompactionType
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|CompactionType
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|determineCompactionType
argument_list|(
name|ci
argument_list|,
name|txns
argument_list|,
name|sd
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
specifier|private
name|CompactionType
name|determineCompactionType
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|,
name|ValidTxnList
name|txns
parameter_list|,
name|StorageDescriptor
name|sd
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|boolean
name|noBase
init|=
literal|false
decl_stmt|;
name|Path
name|location
init|=
operator|new
name|Path
argument_list|(
name|sd
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|location
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|AcidUtils
operator|.
name|Directory
name|dir
init|=
name|AcidUtils
operator|.
name|getAcidState
argument_list|(
name|location
argument_list|,
name|conf
argument_list|,
name|txns
argument_list|)
decl_stmt|;
name|Path
name|base
init|=
name|dir
operator|.
name|getBaseDirectory
argument_list|()
decl_stmt|;
name|long
name|baseSize
init|=
literal|0
decl_stmt|;
name|FileStatus
name|stat
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|base
operator|!=
literal|null
condition|)
block|{
name|stat
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|base
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|stat
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Was assuming base "
operator|+
name|base
operator|.
name|toString
argument_list|()
operator|+
literal|" is directory, but it's a file!"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|baseSize
operator|=
name|sumDirSize
argument_list|(
name|fs
argument_list|,
name|base
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|FileStatus
argument_list|>
name|originals
init|=
name|dir
operator|.
name|getOriginalFiles
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|origStat
range|:
name|originals
control|)
block|{
name|baseSize
operator|+=
name|origStat
operator|.
name|getLen
argument_list|()
expr_stmt|;
block|}
name|long
name|deltaSize
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|AcidUtils
operator|.
name|ParsedDelta
argument_list|>
name|deltas
init|=
name|dir
operator|.
name|getCurrentDirectories
argument_list|()
decl_stmt|;
for|for
control|(
name|AcidUtils
operator|.
name|ParsedDelta
name|delta
range|:
name|deltas
control|)
block|{
name|stat
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|delta
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|stat
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Was assuming delta "
operator|+
name|delta
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" is a directory, "
operator|+
literal|"but it's a file!"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|deltaSize
operator|+=
name|sumDirSize
argument_list|(
name|fs
argument_list|,
name|delta
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|baseSize
operator|==
literal|0
operator|&&
name|deltaSize
operator|>
literal|0
condition|)
block|{
name|noBase
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|float
name|deltaPctThreshold
init|=
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
name|HIVE_COMPACTOR_DELTA_PCT_THRESHOLD
argument_list|)
decl_stmt|;
name|boolean
name|bigEnough
init|=
operator|(
name|float
operator|)
name|deltaSize
operator|/
operator|(
name|float
operator|)
name|baseSize
operator|>
name|deltaPctThreshold
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|StringBuffer
name|msg
init|=
operator|new
name|StringBuffer
argument_list|(
literal|"delta size: "
argument_list|)
decl_stmt|;
name|msg
operator|.
name|append
argument_list|(
name|deltaSize
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|" base size: "
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
name|baseSize
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|" threshold: "
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
name|deltaPctThreshold
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|" will major compact: "
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
name|bigEnough
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bigEnough
condition|)
return|return
name|CompactionType
operator|.
name|MAJOR
return|;
block|}
name|int
name|deltaNumThreshold
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPACTOR_DELTA_NUM_THRESHOLD
argument_list|)
decl_stmt|;
name|boolean
name|enough
init|=
name|deltas
operator|.
name|size
argument_list|()
operator|>
name|deltaNumThreshold
decl_stmt|;
if|if
condition|(
name|enough
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found "
operator|+
name|deltas
operator|.
name|size
argument_list|()
operator|+
literal|" delta files, threshold is "
operator|+
name|deltaNumThreshold
operator|+
operator|(
name|enough
condition|?
literal|""
else|:
literal|"not"
operator|)
operator|+
literal|" and no base, requesting "
operator|+
operator|(
name|noBase
condition|?
literal|"major"
else|:
literal|"minor"
operator|)
operator|+
literal|" compaction"
argument_list|)
expr_stmt|;
comment|// If there's no base file, do a major compaction
return|return
name|noBase
condition|?
name|CompactionType
operator|.
name|MAJOR
else|:
name|CompactionType
operator|.
name|MINOR
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|long
name|sumDirSize
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
name|FileStatus
index|[]
name|buckets
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dir
argument_list|,
name|FileUtils
operator|.
name|HIDDEN_FILES_PATH_FILTER
argument_list|)
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
name|buckets
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|size
operator|+=
name|buckets
index|[
name|i
index|]
operator|.
name|getLen
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
specifier|private
name|void
name|requestCompaction
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|,
name|String
name|runAs
parameter_list|,
name|CompactionType
name|type
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|s
init|=
literal|"Requesting "
operator|+
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
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|CompactionRequest
name|rqst
init|=
operator|new
name|CompactionRequest
argument_list|(
name|ci
operator|.
name|dbname
argument_list|,
name|ci
operator|.
name|tableName
argument_list|,
name|type
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
name|rqst
operator|.
name|setPartitionname
argument_list|(
name|ci
operator|.
name|partName
argument_list|)
expr_stmt|;
name|rqst
operator|.
name|setRunas
argument_list|(
name|runAs
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|compact
argument_list|(
name|rqst
argument_list|)
expr_stmt|;
block|}
comment|// Because TABLE_NO_AUTO_COMPACT was originally assumed to be NO_AUTO_COMPACT and then was moved
comment|// to no_auto_compact, we need to check it in both cases.
specifier|private
name|boolean
name|noAutoCompactSet
parameter_list|(
name|Table
name|t
parameter_list|)
block|{
name|String
name|noAutoCompact
init|=
name|t
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_NO_AUTO_COMPACT
argument_list|)
decl_stmt|;
if|if
condition|(
name|noAutoCompact
operator|==
literal|null
condition|)
block|{
name|noAutoCompact
operator|=
name|t
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_NO_AUTO_COMPACT
operator|.
name|toUpperCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|noAutoCompact
operator|!=
literal|null
operator|&&
name|noAutoCompact
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"true"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

