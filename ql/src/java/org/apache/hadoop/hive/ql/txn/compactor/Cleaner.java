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
name|ValidReadTxnList
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
name|ShowLocksRequest
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
name|ShowLocksResponse
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
name|ShowLocksResponseElement
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
name|HashSet
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

begin_comment
comment|/**  * A class to clean directories after compactions.  This will run in a separate thread.  */
end_comment

begin_class
specifier|public
class|class
name|Cleaner
extends|extends
name|CompactorThread
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
name|cleanerCheckInterval
init|=
literal|0
decl_stmt|;
comment|// List of compactions to clean.
specifier|private
name|Map
argument_list|<
name|Long
argument_list|,
name|Set
argument_list|<
name|Long
argument_list|>
argument_list|>
name|compactId2LockMap
init|=
operator|new
name|HashMap
argument_list|<
name|Long
argument_list|,
name|Set
argument_list|<
name|Long
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Long
argument_list|,
name|CompactionInfo
argument_list|>
name|compactId2CompactInfoMap
init|=
operator|new
name|HashMap
argument_list|<
name|Long
argument_list|,
name|CompactionInfo
argument_list|>
argument_list|()
decl_stmt|;
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
name|long
name|startedAt
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Make sure nothing escapes this run method and kills the metastore at large,
comment|// so wrap it in a big catch Throwable statement.
try|try
block|{
comment|// First look for all the compactions that are waiting to be cleaned.  If we have not
comment|// seen an entry before, look for all the locks held on that table or partition and
comment|// record them.  We will then only clean the partition once all of those locks have been
comment|// released.  This way we avoid removing the files while they are in use,
comment|// while at the same time avoiding starving the cleaner as new readers come along.
comment|// This works because we know that any reader who comes along after the worker thread has
comment|// done the compaction will read the more up to date version of the data (either in a
comment|// newer delta or in a newer base).
name|List
argument_list|<
name|CompactionInfo
argument_list|>
name|toClean
init|=
name|txnHandler
operator|.
name|findReadyToClean
argument_list|()
decl_stmt|;
if|if
condition|(
name|toClean
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|||
name|compactId2LockMap
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|ShowLocksResponse
name|locksResponse
init|=
name|txnHandler
operator|.
name|showLocks
argument_list|(
operator|new
name|ShowLocksRequest
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|CompactionInfo
name|ci
range|:
name|toClean
control|)
block|{
comment|// Check to see if we have seen this request before.  If so, ignore it.  If not,
comment|// add it to our queue.
if|if
condition|(
operator|!
name|compactId2LockMap
operator|.
name|containsKey
argument_list|(
name|ci
operator|.
name|id
argument_list|)
condition|)
block|{
name|compactId2LockMap
operator|.
name|put
argument_list|(
name|ci
operator|.
name|id
argument_list|,
name|findRelatedLocks
argument_list|(
name|ci
argument_list|,
name|locksResponse
argument_list|)
argument_list|)
expr_stmt|;
name|compactId2CompactInfoMap
operator|.
name|put
argument_list|(
name|ci
operator|.
name|id
argument_list|,
name|ci
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Now, for each entry in the queue, see if all of the associated locks are clear so we
comment|// can clean
name|Set
argument_list|<
name|Long
argument_list|>
name|currentLocks
init|=
name|buildCurrentLockSet
argument_list|(
name|locksResponse
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|expiredLocks
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|compactionsCleaned
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|Set
argument_list|<
name|Long
argument_list|>
argument_list|>
name|queueEntry
range|:
name|compactId2LockMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|boolean
name|sawLock
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Long
name|lockId
range|:
name|queueEntry
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
name|currentLocks
operator|.
name|contains
argument_list|(
name|lockId
argument_list|)
condition|)
block|{
name|sawLock
operator|=
literal|true
expr_stmt|;
break|break;
block|}
else|else
block|{
name|expiredLocks
operator|.
name|add
argument_list|(
name|lockId
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|sawLock
condition|)
block|{
comment|// Remember to remove this when we're out of the loop,
comment|// we can't do it in the loop or we'll get a concurrent modification exception.
name|compactionsCleaned
operator|.
name|add
argument_list|(
name|queueEntry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|clean
argument_list|(
name|compactId2CompactInfoMap
operator|.
name|get
argument_list|(
name|queueEntry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Remove the locks we didn't see so we don't look for them again next time
for|for
control|(
name|Long
name|lockId
range|:
name|expiredLocks
control|)
block|{
name|queueEntry
operator|.
name|getValue
argument_list|()
operator|.
name|remove
argument_list|(
name|lockId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|compactionsCleaned
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|Long
name|compactId
range|:
name|compactionsCleaned
control|)
block|{
name|compactId2LockMap
operator|.
name|remove
argument_list|(
name|compactId
argument_list|)
expr_stmt|;
name|compactId2CompactInfoMap
operator|.
name|remove
argument_list|(
name|compactId
argument_list|)
expr_stmt|;
block|}
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
name|Set
argument_list|<
name|Long
argument_list|>
name|findRelatedLocks
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|,
name|ShowLocksResponse
name|locksResponse
parameter_list|)
block|{
name|Set
argument_list|<
name|Long
argument_list|>
name|relatedLocks
init|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShowLocksResponseElement
name|lock
range|:
name|locksResponse
operator|.
name|getLocks
argument_list|()
control|)
block|{
if|if
condition|(
name|ci
operator|.
name|dbname
operator|.
name|equals
argument_list|(
name|lock
operator|.
name|getDbname
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
operator|(
name|ci
operator|.
name|tableName
operator|==
literal|null
operator|&&
name|lock
operator|.
name|getTablename
argument_list|()
operator|==
literal|null
operator|)
operator|||
operator|(
name|ci
operator|.
name|tableName
operator|!=
literal|null
operator|&&
name|ci
operator|.
name|tableName
operator|.
name|equals
argument_list|(
name|lock
operator|.
name|getTablename
argument_list|()
argument_list|)
operator|)
condition|)
block|{
if|if
condition|(
operator|(
name|ci
operator|.
name|partName
operator|==
literal|null
operator|&&
name|lock
operator|.
name|getPartname
argument_list|()
operator|==
literal|null
operator|)
operator|||
operator|(
name|ci
operator|.
name|partName
operator|!=
literal|null
operator|&&
name|ci
operator|.
name|partName
operator|.
name|equals
argument_list|(
name|lock
operator|.
name|getPartname
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|relatedLocks
operator|.
name|add
argument_list|(
name|lock
operator|.
name|getLockid
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|relatedLocks
return|;
block|}
specifier|private
name|Set
argument_list|<
name|Long
argument_list|>
name|buildCurrentLockSet
parameter_list|(
name|ShowLocksResponse
name|locksResponse
parameter_list|)
block|{
name|Set
argument_list|<
name|Long
argument_list|>
name|currentLocks
init|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|(
name|locksResponse
operator|.
name|getLocks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ShowLocksResponseElement
name|lock
range|:
name|locksResponse
operator|.
name|getLocks
argument_list|()
control|)
block|{
name|currentLocks
operator|.
name|add
argument_list|(
name|lock
operator|.
name|getLockid
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|currentLocks
return|;
block|}
specifier|private
name|void
name|clean
parameter_list|(
name|CompactionInfo
name|ci
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
literal|", assuming it was dropped"
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
literal|", assuming it was dropped"
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
comment|// Create a bogus validTxnList with a high water mark set to MAX_LONG and no open
comment|// transactions.  This assures that all deltas are treated as valid and all we return are
comment|// obsolete files.
specifier|final
name|ValidTxnList
name|txnList
init|=
operator|new
name|ValidReadTxnList
argument_list|()
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
name|txnList
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
name|txnList
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
literal|"Caught exception when cleaning, unable to complete cleaning "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// We need to clean this out one way or another.
name|txnHandler
operator|.
name|markCleaned
argument_list|(
name|ci
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|removeFiles
parameter_list|(
name|String
name|location
parameter_list|,
name|ValidTxnList
name|txnList
parameter_list|)
throws|throws
name|IOException
block|{
name|AcidUtils
operator|.
name|Directory
name|dir
init|=
name|AcidUtils
operator|.
name|getAcidState
argument_list|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
argument_list|,
name|conf
argument_list|,
name|txnList
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|obsoleteDirs
init|=
name|dir
operator|.
name|getObsolete
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|filesToDelete
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|(
name|obsoleteDirs
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
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
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|LOG
operator|.
name|info
argument_list|(
literal|"About to remove "
operator|+
name|filesToDelete
operator|.
name|size
argument_list|()
operator|+
literal|" obsolete directories from "
operator|+
name|location
argument_list|)
expr_stmt|;
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
literal|"Doing to delete path "
operator|+
name|dead
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
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

