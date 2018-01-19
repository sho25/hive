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
name|lockmgr
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
name|ql
operator|.
name|Driver
operator|.
name|LockedDriverState
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
name|lockmgr
operator|.
name|HiveLockObject
operator|.
name|HiveLockObjectData
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
name|metadata
operator|.
name|*
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
name|Comparator
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
name|Stack
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
name|locks
operator|.
name|ReentrantLock
import|;
end_import

begin_comment
comment|/**  * shared lock manager for dedicated hive server. all locks are managed in memory  */
end_comment

begin_class
specifier|public
class|class
name|EmbeddedLockManager
implements|implements
name|HiveLockManager
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
literal|"EmbeddedHiveLockManager"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Node
name|root
init|=
operator|new
name|Node
argument_list|()
decl_stmt|;
specifier|private
name|HiveLockManagerCtx
name|ctx
decl_stmt|;
specifier|private
name|long
name|sleepTime
init|=
literal|1000
decl_stmt|;
specifier|private
name|int
name|numRetriesForLock
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|numRetriesForUnLock
init|=
literal|0
decl_stmt|;
specifier|public
name|EmbeddedLockManager
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|setContext
parameter_list|(
name|HiveLockManagerCtx
name|ctx
parameter_list|)
throws|throws
name|LockException
block|{
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveLock
name|lock
parameter_list|(
name|HiveLockObject
name|key
parameter_list|,
name|HiveLockMode
name|mode
parameter_list|,
name|boolean
name|keepAlive
parameter_list|)
throws|throws
name|LockException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Acquiring lock for {} with mode {} {}"
argument_list|,
name|key
operator|.
name|getName
argument_list|()
argument_list|,
name|mode
argument_list|,
name|key
operator|.
name|getData
argument_list|()
operator|.
name|getLockMode
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|lock
argument_list|(
name|key
argument_list|,
name|mode
argument_list|,
name|numRetriesForLock
argument_list|,
name|sleepTime
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|lock
parameter_list|(
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|objs
parameter_list|,
name|boolean
name|keepAlive
parameter_list|,
name|LockedDriverState
name|lDrvState
parameter_list|)
throws|throws
name|LockException
block|{
return|return
name|lock
argument_list|(
name|objs
argument_list|,
name|numRetriesForLock
argument_list|,
name|sleepTime
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|unlock
parameter_list|(
name|HiveLock
name|hiveLock
parameter_list|)
throws|throws
name|LockException
block|{
name|unlock
argument_list|(
name|hiveLock
argument_list|,
name|numRetriesForUnLock
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseLocks
parameter_list|(
name|List
argument_list|<
name|HiveLock
argument_list|>
name|hiveLocks
parameter_list|)
block|{
name|releaseLocks
argument_list|(
name|hiveLocks
argument_list|,
name|numRetriesForUnLock
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|boolean
name|verifyTablePartitions
parameter_list|,
name|boolean
name|fetchData
parameter_list|)
throws|throws
name|LockException
block|{
return|return
name|getLocks
argument_list|(
name|verifyTablePartitions
argument_list|,
name|fetchData
argument_list|,
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|HiveLockObject
name|key
parameter_list|,
name|boolean
name|verifyTablePartitions
parameter_list|,
name|boolean
name|fetchData
parameter_list|)
throws|throws
name|LockException
block|{
return|return
name|getLocks
argument_list|(
name|key
argument_list|,
name|verifyTablePartitions
argument_list|,
name|fetchData
argument_list|,
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepareRetry
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|refresh
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
name|ctx
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|sleepTime
operator|=
name|conf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_LOCK_SLEEP_BETWEEN_RETRIES
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|numRetriesForLock
operator|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_LOCK_NUMRETRIES
argument_list|)
expr_stmt|;
name|numRetriesForUnLock
operator|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_UNLOCK_NUMRETRIES
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveLock
name|lock
parameter_list|(
name|HiveLockObject
name|key
parameter_list|,
name|HiveLockMode
name|mode
parameter_list|,
name|int
name|numRetriesForLock
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|LockException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|numRetriesForLock
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
name|HiveLock
name|lock
init|=
name|lockPrimitive
argument_list|(
name|key
argument_list|,
name|mode
argument_list|)
decl_stmt|;
if|if
condition|(
name|lock
operator|!=
literal|null
condition|)
block|{
return|return
name|lock
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|sleep
parameter_list|(
name|long
name|sleepTime
parameter_list|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
specifier|private
name|List
argument_list|<
name|HiveLock
argument_list|>
name|lock
parameter_list|(
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|objs
parameter_list|,
name|int
name|numRetriesForLock
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|LockException
block|{
name|sortLocks
argument_list|(
name|objs
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
for|for
control|(
name|HiveLockObj
name|obj
range|:
name|objs
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Acquiring lock for {} with mode {}"
argument_list|,
name|obj
operator|.
name|getObj
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|obj
operator|.
name|getMode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|numRetriesForLock
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|lockPrimitive
argument_list|(
name|objs
argument_list|,
name|numRetriesForLock
argument_list|,
name|sleepTime
argument_list|)
decl_stmt|;
if|if
condition|(
name|locks
operator|!=
literal|null
condition|)
block|{
return|return
name|locks
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|HiveLock
name|lockPrimitive
parameter_list|(
name|HiveLockObject
name|key
parameter_list|,
name|HiveLockMode
name|mode
parameter_list|)
throws|throws
name|LockException
block|{
if|if
condition|(
name|root
operator|.
name|lock
argument_list|(
name|key
operator|.
name|getPaths
argument_list|()
argument_list|,
name|key
operator|.
name|getData
argument_list|()
argument_list|,
name|mode
operator|==
name|HiveLockMode
operator|.
name|EXCLUSIVE
argument_list|)
condition|)
block|{
return|return
operator|new
name|SimpleHiveLock
argument_list|(
name|key
argument_list|,
name|mode
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|List
argument_list|<
name|HiveLock
argument_list|>
name|lockPrimitive
parameter_list|(
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|objs
parameter_list|,
name|int
name|numRetriesForLock
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|LockException
block|{
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveLock
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HiveLockObj
name|obj
range|:
name|objs
control|)
block|{
name|HiveLock
name|lock
init|=
name|lockPrimitive
argument_list|(
name|obj
operator|.
name|getObj
argument_list|()
argument_list|,
name|obj
operator|.
name|getMode
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|lock
operator|==
literal|null
condition|)
block|{
name|releaseLocks
argument_list|(
name|locks
argument_list|,
name|numRetriesForLock
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|locks
operator|.
name|add
argument_list|(
name|lock
argument_list|)
expr_stmt|;
block|}
return|return
name|locks
return|;
block|}
specifier|private
name|void
name|sortLocks
parameter_list|(
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|objs
parameter_list|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|objs
argument_list|,
operator|new
name|Comparator
argument_list|<
name|HiveLockObj
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|HiveLockObj
name|o1
parameter_list|,
name|HiveLockObj
name|o2
parameter_list|)
block|{
name|int
name|cmp
init|=
name|o1
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|o1
operator|.
name|getMode
argument_list|()
operator|==
name|o2
operator|.
name|getMode
argument_list|()
condition|)
block|{
return|return
name|cmp
return|;
block|}
comment|// EXCLUSIVE locks occur before SHARED locks
if|if
condition|(
name|o1
operator|.
name|getMode
argument_list|()
operator|==
name|HiveLockMode
operator|.
name|EXCLUSIVE
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
operator|+
literal|1
return|;
block|}
return|return
name|cmp
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|unlock
parameter_list|(
name|HiveLock
name|hiveLock
parameter_list|,
name|int
name|numRetriesForUnLock
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|LockException
block|{
name|String
index|[]
name|paths
init|=
name|hiveLock
operator|.
name|getHiveLockObject
argument_list|()
operator|.
name|getPaths
argument_list|()
decl_stmt|;
name|HiveLockObjectData
name|data
init|=
name|hiveLock
operator|.
name|getHiveLockObject
argument_list|()
operator|.
name|getData
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|numRetriesForUnLock
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|root
operator|.
name|unlock
argument_list|(
name|paths
argument_list|,
name|data
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
throw|throw
operator|new
name|LockException
argument_list|(
literal|"Failed to release lock "
operator|+
name|hiveLock
argument_list|)
throw|;
block|}
specifier|public
name|void
name|releaseLocks
parameter_list|(
name|List
argument_list|<
name|HiveLock
argument_list|>
name|hiveLocks
parameter_list|,
name|int
name|numRetriesForUnLock
parameter_list|,
name|long
name|sleepTime
parameter_list|)
block|{
for|for
control|(
name|HiveLock
name|locked
range|:
name|hiveLocks
control|)
block|{
try|try
block|{
name|unlock
argument_list|(
name|locked
argument_list|,
name|numRetriesForUnLock
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to unlock "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|boolean
name|verifyTablePartitions
parameter_list|,
name|boolean
name|fetchData
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|LockException
block|{
return|return
name|root
operator|.
name|getLocks
argument_list|(
name|verifyTablePartitions
argument_list|,
name|fetchData
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|HiveLockObject
name|key
parameter_list|,
name|boolean
name|verifyTablePartitions
parameter_list|,
name|boolean
name|fetchData
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|LockException
block|{
return|return
name|root
operator|.
name|getLocks
argument_list|(
name|key
operator|.
name|getPaths
argument_list|()
argument_list|,
name|verifyTablePartitions
argument_list|,
name|fetchData
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|// from ZooKeeperHiveLockManager
specifier|private
name|HiveLockObject
name|verify
parameter_list|(
name|boolean
name|verify
parameter_list|,
name|String
index|[]
name|names
parameter_list|,
name|HiveLockObjectData
name|data
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|LockException
block|{
if|if
condition|(
operator|!
name|verify
condition|)
block|{
return|return
operator|new
name|HiveLockObject
argument_list|(
name|names
argument_list|,
name|data
argument_list|)
return|;
block|}
name|String
name|database
init|=
name|names
index|[
literal|0
index|]
decl_stmt|;
name|String
name|table
init|=
name|names
index|[
literal|1
index|]
decl_stmt|;
try|try
block|{
name|Hive
name|db
init|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Table
name|tab
init|=
name|db
operator|.
name|getTable
argument_list|(
name|database
argument_list|,
name|table
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|tab
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|names
operator|.
name|length
operator|==
literal|2
condition|)
block|{
return|return
operator|new
name|HiveLockObject
argument_list|(
name|tab
argument_list|,
name|data
argument_list|)
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
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
for|for
control|(
name|int
name|indx
init|=
literal|2
init|;
name|indx
operator|<
name|names
operator|.
name|length
condition|;
name|indx
operator|++
control|)
block|{
name|String
index|[]
name|partVals
init|=
name|names
index|[
name|indx
index|]
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
name|partSpec
operator|.
name|put
argument_list|(
name|partVals
index|[
literal|0
index|]
argument_list|,
name|partVals
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|Partition
name|partn
decl_stmt|;
try|try
block|{
name|partn
operator|=
name|db
operator|.
name|getPartition
argument_list|(
name|tab
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|partn
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|partn
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|HiveLockObject
argument_list|(
operator|new
name|DummyPartition
argument_list|(
name|tab
argument_list|,
literal|null
argument_list|,
name|partSpec
argument_list|)
argument_list|,
name|data
argument_list|)
return|;
block|}
return|return
operator|new
name|HiveLockObject
argument_list|(
name|partn
argument_list|,
name|data
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|root
operator|.
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|root
operator|.
name|datas
operator|=
literal|null
expr_stmt|;
name|root
operator|.
name|children
operator|=
literal|null
expr_stmt|;
block|}
finally|finally
block|{
name|root
operator|.
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|Node
block|{
specifier|private
name|boolean
name|exclusive
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|children
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|HiveLockObjectData
argument_list|>
name|datas
decl_stmt|;
specifier|private
specifier|final
name|ReentrantLock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|public
name|Node
parameter_list|()
block|{     }
specifier|public
name|void
name|set
parameter_list|(
name|HiveLockObjectData
name|data
parameter_list|,
name|boolean
name|exclusive
parameter_list|)
block|{
name|this
operator|.
name|exclusive
operator|=
name|exclusive
expr_stmt|;
if|if
condition|(
name|datas
operator|==
literal|null
condition|)
block|{
name|datas
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|HiveLockObjectData
argument_list|>
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
name|datas
operator|.
name|put
argument_list|(
name|data
operator|.
name|getQueryId
argument_list|()
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|lock
parameter_list|(
name|String
index|[]
name|paths
parameter_list|,
name|HiveLockObjectData
name|data
parameter_list|,
name|boolean
name|exclusive
parameter_list|)
block|{
return|return
name|lock
argument_list|(
name|paths
argument_list|,
literal|0
argument_list|,
name|data
argument_list|,
name|exclusive
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|unlock
parameter_list|(
name|String
index|[]
name|paths
parameter_list|,
name|HiveLockObjectData
name|data
parameter_list|)
block|{
return|return
name|unlock
argument_list|(
name|paths
argument_list|,
literal|0
argument_list|,
name|data
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|boolean
name|verify
parameter_list|,
name|boolean
name|fetchData
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|LockException
block|{
if|if
condition|(
operator|!
name|root
operator|.
name|hasChild
argument_list|()
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveLock
argument_list|>
argument_list|()
decl_stmt|;
name|getLocks
argument_list|(
operator|new
name|Stack
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|,
name|verify
argument_list|,
name|fetchData
argument_list|,
name|locks
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|locks
return|;
block|}
specifier|private
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|String
index|[]
name|paths
parameter_list|,
name|boolean
name|verify
parameter_list|,
name|boolean
name|fetchData
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|LockException
block|{
if|if
condition|(
operator|!
name|root
operator|.
name|hasChild
argument_list|()
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveLock
argument_list|>
argument_list|()
decl_stmt|;
name|getLocks
argument_list|(
name|paths
argument_list|,
literal|0
argument_list|,
name|verify
argument_list|,
name|fetchData
argument_list|,
name|locks
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|locks
return|;
block|}
specifier|private
name|boolean
name|lock
parameter_list|(
name|String
index|[]
name|paths
parameter_list|,
name|int
name|index
parameter_list|,
name|HiveLockObjectData
name|data
parameter_list|,
name|boolean
name|exclusive
parameter_list|)
block|{
if|if
condition|(
operator|!
name|lock
operator|.
name|tryLock
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
try|try
block|{
if|if
condition|(
name|index
operator|==
name|paths
operator|.
name|length
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|exclusive
operator|||
name|exclusive
operator|&&
name|hasLock
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|set
argument_list|(
name|data
argument_list|,
name|exclusive
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|Node
name|child
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
block|{
name|children
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|children
operator|.
name|put
argument_list|(
name|paths
index|[
name|index
index|]
argument_list|,
name|child
operator|=
operator|new
name|Node
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|child
operator|=
name|children
operator|.
name|get
argument_list|(
name|paths
index|[
name|index
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|child
operator|==
literal|null
condition|)
block|{
name|children
operator|.
name|put
argument_list|(
name|paths
index|[
name|index
index|]
argument_list|,
name|child
operator|=
operator|new
name|Node
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|child
operator|.
name|lock
argument_list|(
name|paths
argument_list|,
name|index
operator|+
literal|1
argument_list|,
name|data
argument_list|,
name|exclusive
argument_list|)
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|unlock
parameter_list|(
name|String
index|[]
name|paths
parameter_list|,
name|int
name|index
parameter_list|,
name|HiveLockObjectData
name|data
parameter_list|)
block|{
if|if
condition|(
operator|!
name|lock
operator|.
name|tryLock
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
try|try
block|{
if|if
condition|(
name|index
operator|==
name|paths
operator|.
name|length
condition|)
block|{
if|if
condition|(
name|hasLock
argument_list|()
condition|)
block|{
name|datas
operator|.
name|remove
argument_list|(
name|data
operator|.
name|getQueryId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
name|Node
name|child
init|=
name|children
operator|==
literal|null
condition|?
literal|null
else|:
name|children
operator|.
name|get
argument_list|(
name|paths
index|[
name|index
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
comment|// should not happen
block|}
if|if
condition|(
name|child
operator|.
name|unlock
argument_list|(
name|paths
argument_list|,
name|index
operator|+
literal|1
argument_list|,
name|data
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|child
operator|.
name|hasLock
argument_list|()
operator|&&
operator|!
name|child
operator|.
name|hasChild
argument_list|()
condition|)
block|{
name|children
operator|.
name|remove
argument_list|(
name|paths
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|getLocks
parameter_list|(
name|Stack
argument_list|<
name|String
argument_list|>
name|names
parameter_list|,
name|boolean
name|verify
parameter_list|,
name|boolean
name|fetchData
parameter_list|,
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|LockException
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|hasLock
argument_list|()
condition|)
block|{
name|getLocks
argument_list|(
name|names
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|names
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|verify
argument_list|,
name|fetchData
argument_list|,
name|locks
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|entry
range|:
name|children
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|names
operator|.
name|push
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getLocks
argument_list|(
name|names
argument_list|,
name|verify
argument_list|,
name|fetchData
argument_list|,
name|locks
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|names
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|getLocks
parameter_list|(
name|String
index|[]
name|paths
parameter_list|,
name|int
name|index
parameter_list|,
name|boolean
name|verify
parameter_list|,
name|boolean
name|fetchData
parameter_list|,
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|LockException
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|index
operator|==
name|paths
operator|.
name|length
condition|)
block|{
name|getLocks
argument_list|(
name|paths
argument_list|,
name|verify
argument_list|,
name|fetchData
argument_list|,
name|locks
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return;
block|}
name|Node
name|child
init|=
name|children
operator|.
name|get
argument_list|(
name|paths
index|[
name|index
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|!=
literal|null
condition|)
block|{
name|child
operator|.
name|getLocks
argument_list|(
name|paths
argument_list|,
name|index
operator|+
literal|1
argument_list|,
name|verify
argument_list|,
name|fetchData
argument_list|,
name|locks
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|getLocks
parameter_list|(
name|String
index|[]
name|paths
parameter_list|,
name|boolean
name|verify
parameter_list|,
name|boolean
name|fetchData
parameter_list|,
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|LockException
block|{
name|HiveLockMode
name|lockMode
init|=
name|getLockMode
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchData
condition|)
block|{
for|for
control|(
name|HiveLockObjectData
name|data
range|:
name|datas
operator|.
name|values
argument_list|()
control|)
block|{
name|HiveLockObject
name|lock
init|=
name|verify
argument_list|(
name|verify
argument_list|,
name|paths
argument_list|,
name|data
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|lock
operator|!=
literal|null
condition|)
block|{
name|locks
operator|.
name|add
argument_list|(
operator|new
name|SimpleHiveLock
argument_list|(
name|lock
argument_list|,
name|lockMode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|HiveLockObject
name|lock
init|=
name|verify
argument_list|(
name|verify
argument_list|,
name|paths
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|lock
operator|!=
literal|null
condition|)
block|{
name|locks
operator|.
name|add
argument_list|(
operator|new
name|SimpleHiveLock
argument_list|(
name|lock
argument_list|,
name|lockMode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|HiveLockMode
name|getLockMode
parameter_list|()
block|{
return|return
name|exclusive
condition|?
name|HiveLockMode
operator|.
name|EXCLUSIVE
else|:
name|HiveLockMode
operator|.
name|SHARED
return|;
block|}
specifier|private
name|boolean
name|hasLock
parameter_list|()
block|{
return|return
name|datas
operator|!=
literal|null
operator|&&
operator|!
name|datas
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|hasChild
parameter_list|()
block|{
return|return
name|children
operator|!=
literal|null
operator|&&
operator|!
name|children
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|SimpleHiveLock
extends|extends
name|HiveLock
block|{
specifier|private
specifier|final
name|HiveLockObject
name|lockObj
decl_stmt|;
specifier|private
specifier|final
name|HiveLockMode
name|lockMode
decl_stmt|;
specifier|public
name|SimpleHiveLock
parameter_list|(
name|HiveLockObject
name|lockObj
parameter_list|,
name|HiveLockMode
name|lockMode
parameter_list|)
block|{
name|this
operator|.
name|lockObj
operator|=
name|lockObj
expr_stmt|;
name|this
operator|.
name|lockMode
operator|=
name|lockMode
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveLockObject
name|getHiveLockObject
parameter_list|()
block|{
return|return
name|lockObj
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveLockMode
name|getHiveLockMode
parameter_list|()
block|{
return|return
name|lockMode
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
name|lockMode
operator|+
literal|"="
operator|+
name|lockObj
operator|.
name|getDisplayName
argument_list|()
operator|+
literal|"("
operator|+
name|lockObj
operator|.
name|getData
argument_list|()
operator|+
literal|")"
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|SimpleHiveLock
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|SimpleHiveLock
name|simpleLock
init|=
operator|(
name|SimpleHiveLock
operator|)
name|o
decl_stmt|;
return|return
name|lockObj
operator|.
name|equals
argument_list|(
name|simpleLock
operator|.
name|getHiveLockObject
argument_list|()
argument_list|)
operator|&&
name|lockMode
operator|==
name|simpleLock
operator|.
name|getHiveLockMode
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

