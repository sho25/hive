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
name|TableName
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
name|metastore
operator|.
name|ReplChangeManager
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
name|GetValidWriteIdsRequest
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
name|GetValidWriteIdsResponse
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
name|NoSuchObjectException
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
name|TxnCommonUtils
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
name|TxnStore
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
name|common
operator|.
name|ValidReaderWriteIdList
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
name|Database
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
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|Ref
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
name|ArrayList
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

begin_import
import|import static
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
name|HiveMetaStore
operator|.
name|HMSHandler
operator|.
name|getMSForConf
import|;
end_import

begin_import
import|import static
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
operator|.
name|MetaStoreUtils
operator|.
name|getDefaultCatalog
import|;
end_import

begin_comment
comment|/**  * A class to clean directories after compactions.  This will run in a separate thread.  */
end_comment

begin_class
specifier|public
class|class
name|Cleaner
extends|extends
name|MetaStoreCompactorThread
block|{
specifier|static
specifier|final
specifier|private
name|String
name|CLASS_NAME
init|=
name|Cleaner
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
specifier|private
name|long
name|cleanerCheckInterval
init|=
literal|0
decl_stmt|;
specifier|private
name|ReplChangeManager
name|replChangeManager
decl_stmt|;
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
name|replChangeManager
operator|=
name|ReplChangeManager
operator|.
name|getInstance
argument_list|(
name|conf
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
if|if
condition|(
name|cleanerCheckInterval
operator|==
literal|0
condition|)
block|{
name|cleanerCheckInterval
operator|=
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPACTOR_CLEANER_RUN_INTERVAL
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
do|do
block|{
comment|// This is solely for testing.  It checks if the test has set the looped value to false,
comment|// and if so remembers that and then sets it to true at the end.  We have to check here
comment|// first to make sure we go through a complete iteration of the loop before resetting it.
name|boolean
name|setLooped
init|=
operator|!
name|looped
operator|.
name|get
argument_list|()
decl_stmt|;
name|TxnStore
operator|.
name|MutexAPI
operator|.
name|LockHandle
name|handle
init|=
literal|null
decl_stmt|;
name|long
name|startedAt
init|=
operator|-
literal|1
decl_stmt|;
comment|// Make sure nothing escapes this run method and kills the metastore at large,
comment|// so wrap it in a big catch Throwable statement.
try|try
block|{
name|handle
operator|=
name|txnHandler
operator|.
name|getMutexAPI
argument_list|()
operator|.
name|acquireLock
argument_list|(
name|TxnStore
operator|.
name|MUTEX_KEY
operator|.
name|Cleaner
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|startedAt
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|long
name|minOpenTxnId
init|=
name|txnHandler
operator|.
name|findMinOpenTxnId
argument_list|()
decl_stmt|;
for|for
control|(
name|CompactionInfo
name|compactionInfo
range|:
name|txnHandler
operator|.
name|findReadyToClean
argument_list|()
control|)
block|{
name|clean
argument_list|(
name|compactionInfo
argument_list|,
name|minOpenTxnId
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
literal|"Caught an exception in the main loop of compactor cleaner, "
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
name|handle
operator|!=
literal|null
condition|)
block|{
name|handle
operator|.
name|releaseLocks
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|setLooped
condition|)
block|{
name|looped
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Now, go back to bed until it's time to do this again
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
name|cleanerCheckInterval
operator|||
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
continue|continue;
block|}
else|else
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|cleanerCheckInterval
operator|-
name|elapsedTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
comment|// What can I do about it?
block|}
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
specifier|private
name|void
name|clean
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|,
name|long
name|minOpenTxnGLB
parameter_list|)
throws|throws
name|MetaException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting cleaning for "
operator|+
name|ci
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
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
comment|// The table was dropped before we got around to cleaning it.
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
literal|", assuming it was dropped."
operator|+
name|idWatermark
argument_list|(
name|ci
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
return|return;
block|}
name|Partition
name|p
init|=
literal|null
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
condition|)
block|{
comment|// The partition was dropped before we got around to cleaning it.
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
literal|", assuming it was dropped."
operator|+
name|idWatermark
argument_list|(
name|ci
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
return|return;
block|}
block|}
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
specifier|final
name|String
name|location
init|=
name|sd
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|ValidTxnList
name|validTxnList
init|=
name|TxnUtils
operator|.
name|createValidTxnListForCleaner
argument_list|(
name|txnHandler
operator|.
name|getOpenTxns
argument_list|()
argument_list|,
name|minOpenTxnGLB
argument_list|)
decl_stmt|;
comment|//save it so that getAcidState() sees it
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
comment|/**        * {@code validTxnList} is capped by minOpenTxnGLB so if        * {@link AcidUtils#getAcidState(Path, Configuration, ValidWriteIdList)} sees a base/delta        * produced by a compactor, that means every reader that could be active right now see it        * as well.  That means if this base/delta shadows some earlier base/delta, the it will be        * used in favor of any files that it shadows.  Thus the shadowed files are safe to delete.        *        *        * The metadata about aborted writeIds (and consequently aborted txn IDs) cannot be deleted        * above COMPACTION_QUEUE.CQ_HIGHEST_WRITE_ID.        * See {@link TxnStore#markCleaned(CompactionInfo)} for details.        * For example given partition P1, txnid:150 starts and sees txnid:149 as open.        * Say compactor runs in txnid:160, but 149 is still open and P1 has the largest resolved        * writeId:17.  Compactor will produce base_17_c160.        * Suppose txnid:149 writes delta_18_18        * to P1 and aborts.  Compactor can only remove TXN_COMPONENTS entries        * up to (inclusive) writeId:17 since delta_18_18 may be on disk (and perhaps corrupted) but        * not visible based on 'validTxnList' capped at minOpenTxn so it will not not be cleaned by        * {@link #removeFiles(String, ValidWriteIdList, CompactionInfo)} and so we must keep the        * metadata that says that 18 is aborted.        * In a slightly different case, whatever txn created delta_18 (and all other txn) may have        * committed by the time cleaner runs and so cleaner will indeed see delta_18_18 and remove        * it (since it has nothing but aborted data).  But we can't tell which actually happened        * in markCleaned() so make sure it doesn't delete meta above CG_CQ_HIGHEST_WRITE_ID.        *        * We could perhaps make cleaning of aborted and obsolete and remove all aborted files up        * to the current Min Open Write Id, this way aborted TXN_COMPONENTS meta can be removed        * as well up to that point which may be higher than CQ_HIGHEST_WRITE_ID.  This could be        * useful if there is all of a sudden a flood of aborted txns.  (For another day).        */
name|List
argument_list|<
name|String
argument_list|>
name|tblNames
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|TableName
operator|.
name|getDbTable
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
argument_list|)
decl_stmt|;
name|GetValidWriteIdsRequest
name|rqst
init|=
operator|new
name|GetValidWriteIdsRequest
argument_list|(
name|tblNames
argument_list|)
decl_stmt|;
name|rqst
operator|.
name|setValidTxnList
argument_list|(
name|validTxnList
operator|.
name|writeToString
argument_list|()
argument_list|)
expr_stmt|;
name|GetValidWriteIdsResponse
name|rsp
init|=
name|txnHandler
operator|.
name|getValidWriteIds
argument_list|(
name|rqst
argument_list|)
decl_stmt|;
comment|//we could have no write IDs for a table if it was never written to but
comment|// since we are in the Cleaner phase of compactions, there must have
comment|// been some delta/base dirs
assert|assert
name|rsp
operator|!=
literal|null
operator|&&
name|rsp
operator|.
name|getTblValidWriteIdsSize
argument_list|()
operator|==
literal|1
assert|;
comment|//Creating 'reader' list since we are interested in the set of 'obsolete' files
name|ValidReaderWriteIdList
name|validWriteIdList
init|=
name|TxnCommonUtils
operator|.
name|createValidReaderWriteIdList
argument_list|(
name|rsp
operator|.
name|getTblValidWriteIds
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
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
name|removeFiles
argument_list|(
name|location
argument_list|,
name|validWriteIdList
argument_list|,
name|ci
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaning as user "
operator|+
name|ci
operator|.
name|runAs
operator|+
literal|" for "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
argument_list|)
expr_stmt|;
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|createProxyUser
argument_list|(
name|ci
operator|.
name|runAs
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
name|removeFiles
argument_list|(
name|location
argument_list|,
name|validWriteIdList
argument_list|,
name|ci
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
operator|+
name|idWatermark
argument_list|(
name|ci
argument_list|)
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
name|txnHandler
operator|.
name|markCleaned
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
literal|"Caught exception when cleaning, unable to complete cleaning of "
operator|+
name|ci
operator|+
literal|" "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|ci
operator|.
name|errorMessage
operator|=
name|e
operator|.
name|getMessage
argument_list|()
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
specifier|private
specifier|static
name|String
name|idWatermark
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|)
block|{
return|return
literal|" id="
operator|+
name|ci
operator|.
name|id
return|;
block|}
specifier|private
name|void
name|removeFiles
parameter_list|(
name|String
name|location
parameter_list|,
name|ValidWriteIdList
name|writeIdList
parameter_list|,
name|CompactionInfo
name|ci
parameter_list|)
throws|throws
name|IOException
throws|,
name|NoSuchObjectException
throws|,
name|MetaException
block|{
name|Path
name|locPath
init|=
operator|new
name|Path
argument_list|(
name|location
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
name|locPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|locPath
argument_list|,
name|conf
argument_list|,
name|writeIdList
argument_list|,
name|Ref
operator|.
name|from
argument_list|(
literal|false
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|obsoleteDirs
init|=
name|dir
operator|.
name|getObsolete
argument_list|()
decl_stmt|;
comment|/**      * add anything in 'dir'  that only has data from aborted transactions - no one should be      * trying to read anything in that dir (except getAcidState() that only reads the name of      * this dir itself)      * So this may run ahead of {@link CompactionInfo#highestWriteId} but it's ok (suppose there      * are no active txns when cleaner runs).  The key is to not delete metadata about aborted      * txns with write IDs> {@link CompactionInfo#highestWriteId}.      * See {@link TxnStore#markCleaned(CompactionInfo)}      */
name|obsoleteDirs
operator|.
name|addAll
argument_list|(
name|dir
operator|.
name|getAbortedDirectories
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|filesToDelete
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|obsoleteDirs
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|StringBuilder
name|extraDebugInfo
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"["
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|stat
range|:
name|obsoleteDirs
control|)
block|{
name|filesToDelete
operator|.
name|add
argument_list|(
name|stat
argument_list|)
expr_stmt|;
name|extraDebugInfo
operator|.
name|append
argument_list|(
name|stat
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|stat
argument_list|,
name|locPath
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|idWatermark
argument_list|(
name|ci
argument_list|)
operator|+
literal|" found unexpected file: "
operator|+
name|stat
argument_list|)
expr_stmt|;
block|}
block|}
name|extraDebugInfo
operator|.
name|setCharAt
argument_list|(
name|extraDebugInfo
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|,
literal|']'
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|idWatermark
argument_list|(
name|ci
argument_list|)
operator|+
literal|" About to remove "
operator|+
name|filesToDelete
operator|.
name|size
argument_list|()
operator|+
literal|" obsolete directories from "
operator|+
name|location
operator|+
literal|". "
operator|+
name|extraDebugInfo
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|filesToDelete
operator|.
name|size
argument_list|()
operator|<
literal|1
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Hmm, nothing to delete in the cleaner for directory "
operator|+
name|location
operator|+
literal|", that hardly seems right."
argument_list|)
expr_stmt|;
return|return;
block|}
name|FileSystem
name|fs
init|=
name|filesToDelete
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Database
name|db
init|=
name|getMSForConf
argument_list|(
name|conf
argument_list|)
operator|.
name|getDatabase
argument_list|(
name|getDefaultCatalog
argument_list|(
name|conf
argument_list|)
argument_list|,
name|ci
operator|.
name|dbname
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|getMSForConf
argument_list|(
name|conf
argument_list|)
operator|.
name|getTable
argument_list|(
name|getDefaultCatalog
argument_list|(
name|conf
argument_list|)
argument_list|,
name|ci
operator|.
name|dbname
argument_list|,
name|ci
operator|.
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|dead
range|:
name|filesToDelete
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Going to delete path "
operator|+
name|dead
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|ReplChangeManager
operator|.
name|shouldEnableCm
argument_list|(
name|db
argument_list|,
name|table
argument_list|)
condition|)
block|{
name|replChangeManager
operator|.
name|recycle
argument_list|(
name|dead
argument_list|,
name|ReplChangeManager
operator|.
name|RecycleType
operator|.
name|MOVE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|fs
operator|.
name|delete
argument_list|(
name|dead
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

