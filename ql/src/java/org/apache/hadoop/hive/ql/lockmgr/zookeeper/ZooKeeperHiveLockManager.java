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
name|lockmgr
operator|.
name|zookeeper
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|CreateMode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|Watcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
operator|.
name|Ids
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
name|ArrayList
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
name|Map
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
name|LinkedHashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
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
name|StringEscapeUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|parse
operator|.
name|ErrorMsg
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
name|parse
operator|.
name|SemanticException
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
name|HiveLockManager
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
name|HiveLockManagerCtx
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
name|HiveLock
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
name|lockmgr
operator|.
name|HiveLockMode
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
name|LockException
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|ql
operator|.
name|metadata
operator|.
name|DummyPartition
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|HiveException
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
name|MetaStoreUtils
import|;
end_import

begin_class
specifier|public
class|class
name|ZooKeeperHiveLockManager
implements|implements
name|HiveLockManager
block|{
name|HiveLockManagerCtx
name|ctx
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"ZooKeeperHiveLockManager"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|LogHelper
name|console
init|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
decl_stmt|;
specifier|private
name|ZooKeeper
name|zooKeeper
decl_stmt|;
comment|// All the locks are created under this parent
specifier|private
name|String
name|parent
decl_stmt|;
specifier|private
name|int
name|sessionTimeout
decl_stmt|;
specifier|private
name|String
name|quorumServers
decl_stmt|;
specifier|public
name|ZooKeeperHiveLockManager
parameter_list|()
block|{   }
comment|/**    * @param conf  The hive configuration    * Get the quorum server address from the configuration. The format is:    * host1:port, host2:port..    **/
specifier|private
specifier|static
name|String
name|getQuorumServers
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|String
name|hosts
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_QUORUM
argument_list|)
decl_stmt|;
name|String
name|port
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CLIENT_PORT
argument_list|)
decl_stmt|;
return|return
name|hosts
operator|+
literal|":"
operator|+
name|port
return|;
block|}
comment|/**    * @param ctx  The lock manager context (containing the Hive configuration file)    * Start the ZooKeeper client based on the zookeeper cluster specified in the conf.    **/
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
name|HiveConf
name|conf
init|=
name|ctx
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|sessionTimeout
operator|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_SESSION_TIMEOUT
argument_list|)
expr_stmt|;
name|quorumServers
operator|=
name|ZooKeeperHiveLockManager
operator|.
name|getQuorumServers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
name|renewZookeeperInstance
argument_list|(
name|sessionTimeout
argument_list|,
name|quorumServers
argument_list|)
expr_stmt|;
name|parent
operator|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_NAMESPACE
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|par
init|=
name|zooKeeper
operator|.
name|create
argument_list|(
literal|"/"
operator|+
name|parent
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
decl_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
comment|// ignore if the parent already exists
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
literal|"Failed to create ZooKeeper object: "
operator|+
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|ErrorMsg
operator|.
name|ZOOKEEPER_CLIENT_COULD_NOT_BE_INITIALIZED
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|renewZookeeperInstance
parameter_list|(
name|int
name|sessionTimeout
parameter_list|,
name|String
name|quorumServers
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
if|if
condition|(
name|zooKeeper
operator|!=
literal|null
condition|)
block|{
name|zooKeeper
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|zooKeeper
operator|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
operator|new
name|DummyWatcher
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Since partition names can contain "/", which need all the parent directories to be created by ZooKeeper,    * replace "/" by a dummy name to ensure a single hierarchy.    **/
specifier|private
name|String
name|getObjectName
parameter_list|(
name|HiveLockObject
name|key
parameter_list|,
name|HiveLockMode
name|mode
parameter_list|)
block|{
return|return
literal|"/"
operator|+
name|parent
operator|+
literal|"/"
operator|+
name|key
operator|.
name|getName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"/"
argument_list|,
name|ctx
operator|.
name|getConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DEFAULT_ZOOKEEPER_PARTITION_NAME
argument_list|)
argument_list|)
operator|+
literal|"-"
operator|+
name|mode
operator|+
literal|"-"
return|;
block|}
comment|/**    * @param  key          The object to be locked    * @param  mode         The mode of the lock    * @param  keepAlive    Whether the lock is to be persisted after the statement    * @param  numRetries   number of retries when the lock can not be acquired    * @param  sleepTime    sleep time between retries    *    * Acuire the lock. Return null if a conflicting lock is present.    **/
specifier|public
name|ZooKeeperHiveLock
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
parameter_list|,
name|int
name|numRetries
parameter_list|,
name|int
name|sleepTime
parameter_list|)
throws|throws
name|LockException
block|{
name|String
name|name
init|=
name|getObjectName
argument_list|(
name|key
argument_list|,
name|mode
argument_list|)
decl_stmt|;
name|String
name|res
init|=
literal|null
decl_stmt|;
try|try
block|{
name|int
name|tryNum
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|String
name|msg
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|keepAlive
condition|)
block|{
name|res
operator|=
name|zooKeeper
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|key
operator|.
name|getData
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|PERSISTENT_SEQUENTIAL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|res
operator|=
name|zooKeeper
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|key
operator|.
name|getData
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|,
name|CreateMode
operator|.
name|EPHEMERAL_SEQUENTIAL
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
name|msg
operator|=
name|e
operator|.
name|getLocalizedMessage
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|res
operator|!=
literal|null
condition|)
block|{
break|break;
block|}
name|renewZookeeperInstance
argument_list|(
name|sessionTimeout
argument_list|,
name|quorumServers
argument_list|)
expr_stmt|;
if|if
condition|(
name|tryNum
operator|==
name|numRetries
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Lock for "
operator|+
name|key
operator|.
name|getName
argument_list|()
operator|+
literal|" cannot be acquired in "
operator|+
name|mode
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|LOCK_CANNOT_BE_ACQUIRED
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|tryNum
operator|++
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Lock for "
operator|+
name|key
operator|.
name|getName
argument_list|()
operator|+
literal|" cannot be acquired in "
operator|+
name|mode
operator|+
literal|", will retry again later..., more info: "
operator|+
name|msg
argument_list|)
expr_stmt|;
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
block|{         }
block|}
name|int
name|seqNo
init|=
name|getSequenceNumber
argument_list|(
name|res
argument_list|,
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|seqNo
operator|==
operator|-
literal|1
condition|)
block|{
name|zooKeeper
operator|.
name|delete
argument_list|(
name|res
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|children
init|=
name|zooKeeper
operator|.
name|getChildren
argument_list|(
literal|"/"
operator|+
name|parent
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|String
name|exLock
init|=
name|getObjectName
argument_list|(
name|key
argument_list|,
name|HiveLockMode
operator|.
name|EXCLUSIVE
argument_list|)
decl_stmt|;
name|String
name|shLock
init|=
name|getObjectName
argument_list|(
name|key
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
name|child
operator|=
literal|"/"
operator|+
name|parent
operator|+
literal|"/"
operator|+
name|child
expr_stmt|;
comment|// Is there a conflicting lock on the same object with a lower sequence number
name|int
name|childSeq
init|=
name|seqNo
decl_stmt|;
if|if
condition|(
name|child
operator|.
name|startsWith
argument_list|(
name|exLock
argument_list|)
condition|)
block|{
name|childSeq
operator|=
name|getSequenceNumber
argument_list|(
name|child
argument_list|,
name|exLock
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|mode
operator|==
name|HiveLockMode
operator|.
name|EXCLUSIVE
operator|)
operator|&&
name|child
operator|.
name|startsWith
argument_list|(
name|shLock
argument_list|)
condition|)
block|{
name|childSeq
operator|=
name|getSequenceNumber
argument_list|(
name|child
argument_list|,
name|shLock
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|childSeq
operator|>=
literal|0
operator|)
operator|&&
operator|(
name|childSeq
operator|<
name|seqNo
operator|)
condition|)
block|{
name|zooKeeper
operator|.
name|delete
argument_list|(
name|res
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"conflicting lock present for "
operator|+
name|key
operator|.
name|getName
argument_list|()
operator|+
literal|" mode "
operator|+
name|mode
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
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
literal|"Failed to get ZooKeeper lock: "
operator|+
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|ZooKeeperHiveLock
argument_list|(
name|res
argument_list|,
name|key
argument_list|,
name|mode
argument_list|)
return|;
block|}
comment|/* Remove the lock specified */
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
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|zooKeeper
argument_list|,
name|hiveLock
argument_list|)
expr_stmt|;
block|}
comment|/* Remove the lock specified */
specifier|private
specifier|static
name|void
name|unlock
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|ZooKeeper
name|zkpClient
parameter_list|,
name|HiveLock
name|hiveLock
parameter_list|)
throws|throws
name|LockException
block|{
name|ZooKeeperHiveLock
name|zLock
init|=
operator|(
name|ZooKeeperHiveLock
operator|)
name|hiveLock
decl_stmt|;
try|try
block|{
name|zkpClient
operator|.
name|delete
argument_list|(
name|zLock
operator|.
name|getPath
argument_list|()
argument_list|,
operator|-
literal|1
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
literal|"Failed to release ZooKeeper lock: "
operator|+
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* Release all locks - including PERSISTENT locks */
specifier|public
specifier|static
name|void
name|releaseAllLocks
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|int
name|sessionTimeout
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_SESSION_TIMEOUT
argument_list|)
decl_stmt|;
name|String
name|quorumServers
init|=
name|getQuorumServers
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ZooKeeper
name|zkpClient
init|=
operator|new
name|ZooKeeper
argument_list|(
name|quorumServers
argument_list|,
name|sessionTimeout
argument_list|,
operator|new
name|DummyWatcher
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|parent
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_NAMESPACE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|getLocks
argument_list|(
name|conf
argument_list|,
name|zkpClient
argument_list|,
literal|null
argument_list|,
name|parent
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|locks
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HiveLock
name|lock
range|:
name|locks
control|)
block|{
name|unlock
argument_list|(
name|conf
argument_list|,
name|zkpClient
argument_list|,
name|lock
argument_list|)
expr_stmt|;
block|}
block|}
name|zkpClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|zkpClient
operator|=
literal|null
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
literal|"Failed to release all locks: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|ErrorMsg
operator|.
name|ZOOKEEPER_CLIENT_COULD_NOT_BE_INITIALIZED
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/* Get all locks */
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|boolean
name|verifyTablePartition
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
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|zooKeeper
argument_list|,
literal|null
argument_list|,
name|parent
argument_list|,
name|verifyTablePartition
argument_list|,
name|fetchData
argument_list|)
return|;
block|}
comment|/* Get all locks for a particular object */
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
name|ctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|zooKeeper
argument_list|,
name|key
argument_list|,
name|parent
argument_list|,
name|verifyTablePartitions
argument_list|,
name|fetchData
argument_list|)
return|;
block|}
comment|/**    * @param conf        Hive configuration    * @param zkpClient   The ZooKeeper client    * @param key         The object to be compared against - if key is null, then get all locks    **/
specifier|private
specifier|static
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getLocks
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|ZooKeeper
name|zkpClient
parameter_list|,
name|HiveLockObject
name|key
parameter_list|,
name|String
name|parent
parameter_list|,
name|boolean
name|verifyTablePartition
parameter_list|,
name|boolean
name|fetchData
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
name|List
argument_list|<
name|String
argument_list|>
name|children
decl_stmt|;
try|try
block|{
name|children
operator|=
name|zkpClient
operator|.
name|getChildren
argument_list|(
literal|"/"
operator|+
name|parent
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// no locks present
return|return
name|locks
return|;
block|}
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
name|child
operator|=
literal|"/"
operator|+
name|parent
operator|+
literal|"/"
operator|+
name|child
expr_stmt|;
name|HiveLockMode
name|mode
init|=
name|getLockMode
argument_list|(
name|conf
argument_list|,
name|child
argument_list|)
decl_stmt|;
if|if
condition|(
name|mode
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|HiveLockObjectData
name|data
init|=
literal|null
decl_stmt|;
comment|//set the lock object with a dummy data, and then do a set if needed.
name|HiveLockObject
name|obj
init|=
name|getLockObject
argument_list|(
name|conf
argument_list|,
name|child
argument_list|,
name|mode
argument_list|,
name|data
argument_list|,
name|verifyTablePartition
argument_list|)
decl_stmt|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|(
name|key
operator|==
literal|null
operator|)
operator|||
operator|(
name|obj
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|key
operator|.
name|getName
argument_list|()
argument_list|)
operator|)
condition|)
block|{
if|if
condition|(
name|fetchData
condition|)
block|{
try|try
block|{
name|data
operator|=
operator|new
name|HiveLockObjectData
argument_list|(
operator|new
name|String
argument_list|(
name|zkpClient
operator|.
name|getData
argument_list|(
name|child
argument_list|,
operator|new
name|DummyWatcher
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
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
literal|"Error in getting data for "
operator|+
name|child
operator|+
literal|" "
operator|+
name|e
argument_list|)
expr_stmt|;
comment|// ignore error
block|}
block|}
name|obj
operator|.
name|setData
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|HiveLock
name|lck
init|=
call|(
name|HiveLock
call|)
argument_list|(
operator|new
name|ZooKeeperHiveLock
argument_list|(
name|child
argument_list|,
name|obj
argument_list|,
name|mode
argument_list|)
argument_list|)
decl_stmt|;
name|locks
operator|.
name|add
argument_list|(
name|lck
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|locks
return|;
block|}
comment|/* Release all transient locks, by simply closing the client */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|LockException
block|{
try|try
block|{
if|if
condition|(
name|zooKeeper
operator|!=
literal|null
condition|)
block|{
name|zooKeeper
operator|.
name|close
argument_list|()
expr_stmt|;
name|zooKeeper
operator|=
literal|null
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
literal|"Failed to close zooKeeper client: "
operator|+
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Get the sequence number from the path. The sequence number is always at the end of the path.    **/
specifier|private
name|int
name|getSequenceNumber
parameter_list|(
name|String
name|resPath
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|String
name|tst
init|=
name|resPath
operator|.
name|substring
argument_list|(
name|path
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
return|return
operator|(
operator|new
name|Integer
argument_list|(
name|tst
argument_list|)
operator|)
operator|.
name|intValue
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|-
literal|1
return|;
comment|// invalid number
block|}
block|}
comment|/**    * Get the object from the path of the lock.    * The object may correspond to a table, a partition or a parent to a partition.    * For eg: if Table T is partitioned by ds, hr and ds=1/hr=1 is a valid partition,    * the lock may also correspond to T@ds=1, which is not a valid object    * @param verifyTablePartition    **/
specifier|private
specifier|static
name|HiveLockObject
name|getLockObject
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|path
parameter_list|,
name|HiveLockMode
name|mode
parameter_list|,
name|HiveLockObjectData
name|data
parameter_list|,
name|boolean
name|verifyTablePartition
parameter_list|)
throws|throws
name|LockException
block|{
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
name|int
name|indx
init|=
name|path
operator|.
name|lastIndexOf
argument_list|(
name|mode
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|objName
init|=
name|path
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|indx
operator|-
literal|1
argument_list|)
decl_stmt|;
name|String
index|[]
name|names
init|=
name|objName
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
index|[
literal|1
index|]
operator|.
name|split
argument_list|(
literal|"@"
argument_list|)
decl_stmt|;
if|if
condition|(
name|names
operator|.
name|length
operator|<
literal|2
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|verifyTablePartition
condition|)
block|{
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
name|names
argument_list|,
name|data
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|HiveLockObject
argument_list|(
name|objName
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
index|[
literal|1
index|]
operator|.
name|replaceAll
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DEFAULT_ZOOKEEPER_PARTITION_NAME
argument_list|)
argument_list|,
literal|"/"
argument_list|)
operator|.
name|split
argument_list|(
literal|"@"
argument_list|)
argument_list|,
name|data
argument_list|)
return|;
block|}
block|}
name|Table
name|tab
init|=
name|db
operator|.
name|getTable
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
comment|//need to change to names[0]
name|names
index|[
literal|1
index|]
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// do not throw exception if table does not exist
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
name|String
index|[]
name|parts
init|=
name|names
index|[
literal|2
index|]
operator|.
name|split
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DEFAULT_ZOOKEEPER_PARTITION_NAME
argument_list|)
argument_list|)
decl_stmt|;
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
name|indx
operator|=
literal|0
init|;
name|indx
operator|<
name|parts
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
name|parts
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
name|objName
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
index|[
literal|1
index|]
operator|.
name|replaceAll
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DEFAULT_ZOOKEEPER_PARTITION_NAME
argument_list|)
argument_list|,
literal|"/"
argument_list|)
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to create ZooKeeper object: "
operator|+
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|LockException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|Pattern
name|shMode
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^.*-(SHARED)-([0-9]+)$"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Pattern
name|exMode
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^.*-(EXCLUSIVE)-([0-9]+)$"
argument_list|)
decl_stmt|;
comment|/* Get the mode of the lock encoded in the path */
specifier|private
specifier|static
name|HiveLockMode
name|getLockMode
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|Matcher
name|shMatcher
init|=
name|shMode
operator|.
name|matcher
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|Matcher
name|exMatcher
init|=
name|exMode
operator|.
name|matcher
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|shMatcher
operator|.
name|matches
argument_list|()
condition|)
return|return
name|HiveLockMode
operator|.
name|SHARED
return|;
if|if
condition|(
name|exMatcher
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|HiveLockMode
operator|.
name|EXCLUSIVE
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
class|class
name|DummyWatcher
implements|implements
name|Watcher
block|{
specifier|public
name|void
name|process
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|WatchedEvent
name|event
parameter_list|)
block|{     }
block|}
block|}
end_class

end_unit

