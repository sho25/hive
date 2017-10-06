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
name|RawStore
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
name|RawStoreProxy
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
name|apache
operator|.
name|hadoop
operator|.
name|security
operator|.
name|AccessControlException
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
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * Superclass for all threads in the compactor.  */
end_comment

begin_class
specifier|abstract
class|class
name|CompactorThread
extends|extends
name|Thread
implements|implements
name|MetaStoreThread
block|{
specifier|static
specifier|final
specifier|private
name|String
name|CLASS_NAME
init|=
name|CompactorThread
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
specifier|protected
name|HiveConf
name|conf
decl_stmt|;
specifier|protected
name|TxnStore
name|txnHandler
decl_stmt|;
specifier|protected
name|RawStore
name|rs
decl_stmt|;
specifier|protected
name|int
name|threadId
decl_stmt|;
specifier|protected
name|AtomicBoolean
name|stop
decl_stmt|;
specifier|protected
name|AtomicBoolean
name|looped
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
comment|// TODO MS-SPLIT for now, keep a copy of HiveConf around as we need to call other methods with
comment|// it. This should be changed to Configuration once everything that this calls that requires
comment|// HiveConf is moved to the standalone metastore.
name|conf
operator|=
operator|(
name|configuration
operator|instanceof
name|HiveConf
operator|)
condition|?
operator|(
name|HiveConf
operator|)
name|configuration
else|:
operator|new
name|HiveConf
argument_list|(
name|configuration
argument_list|,
name|HiveConf
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setThreadId
parameter_list|(
name|int
name|threadId
parameter_list|)
block|{
name|this
operator|.
name|threadId
operator|=
name|threadId
expr_stmt|;
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
name|this
operator|.
name|stop
operator|=
name|stop
expr_stmt|;
name|this
operator|.
name|looped
operator|=
name|looped
expr_stmt|;
name|setPriority
argument_list|(
name|MIN_PRIORITY
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// this means the process will exit without waiting for this thread
comment|// Get our own instance of the transaction handler
name|txnHandler
operator|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Get our own connection to the database so we can get table and partition information.
name|rs
operator|=
name|RawStoreProxy
operator|.
name|getProxy
argument_list|(
name|conf
argument_list|,
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|RAW_STORE_IMPL
argument_list|)
argument_list|,
name|threadId
argument_list|)
expr_stmt|;
block|}
comment|/**    * Find the table being compacted    * @param ci compaction info returned from the compaction queue    * @return metastore table    * @throws org.apache.hadoop.hive.metastore.api.MetaException if the table cannot be found.    */
specifier|protected
name|Table
name|resolveTable
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|)
throws|throws
name|MetaException
block|{
try|try
block|{
return|return
name|rs
operator|.
name|getTable
argument_list|(
name|ci
operator|.
name|dbname
argument_list|,
name|ci
operator|.
name|tableName
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to find table "
operator|+
name|ci
operator|.
name|getFullTableName
argument_list|()
operator|+
literal|", "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Get the partition being compacted.    * @param ci compaction info returned from the compaction queue    * @return metastore partition, or null if there is not partition in this compaction info    * @throws Exception if underlying calls throw, or if the partition name resolves to more than    * one partition.    */
specifier|protected
name|Partition
name|resolvePartition
parameter_list|(
name|CompactionInfo
name|ci
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|ci
operator|.
name|partName
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
decl_stmt|;
try|try
block|{
name|parts
operator|=
name|rs
operator|.
name|getPartitionsByNames
argument_list|(
name|ci
operator|.
name|dbname
argument_list|,
name|ci
operator|.
name|tableName
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|ci
operator|.
name|partName
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|parts
operator|==
literal|null
operator|||
name|parts
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// The partition got dropped before we went looking for it.
return|return
literal|null
return|;
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
literal|"Unable to find partition "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
operator|+
literal|", "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
if|if
condition|(
name|parts
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ci
operator|.
name|getFullPartitionName
argument_list|()
operator|+
literal|" does not refer to a single partition. "
operator|+
name|parts
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Too many partitions for : "
operator|+
name|ci
operator|.
name|getFullPartitionName
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|parts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Get the storage descriptor for a compaction.    * @param t table from {@link #resolveTable(org.apache.hadoop.hive.metastore.txn.CompactionInfo)}    * @param p table from {@link #resolvePartition(org.apache.hadoop.hive.metastore.txn.CompactionInfo)}    * @return metastore storage descriptor.    */
specifier|protected
name|StorageDescriptor
name|resolveStorageDescriptor
parameter_list|(
name|Table
name|t
parameter_list|,
name|Partition
name|p
parameter_list|)
block|{
return|return
operator|(
name|p
operator|==
literal|null
operator|)
condition|?
name|t
operator|.
name|getSd
argument_list|()
else|:
name|p
operator|.
name|getSd
argument_list|()
return|;
block|}
comment|/**    * Determine which user to run an operation as, based on the owner of the directory to be    * compacted.  It is asserted that either the user running the hive metastore or the table    * owner must be able to stat the directory and determine the owner.    * @param location directory that will be read or written to.    * @param t metastore table object    * @return username of the owner of the location.    * @throws java.io.IOException if neither the hive metastore user nor the table owner can stat    * the location.    */
specifier|protected
name|String
name|findUserToRunAs
parameter_list|(
name|String
name|location
parameter_list|,
name|Table
name|t
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Determining who to run the job as."
argument_list|)
expr_stmt|;
specifier|final
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|location
argument_list|)
decl_stmt|;
specifier|final
name|FileSystem
name|fs
init|=
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|FileStatus
name|stat
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Running job as "
operator|+
name|stat
operator|.
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|stat
operator|.
name|getOwner
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|AccessControlException
name|e
parameter_list|)
block|{
comment|// TODO not sure this is the right exception
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unable to stat file as current user, trying as table owner"
argument_list|)
expr_stmt|;
comment|// Now, try it as the table owner and see if we get better luck.
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|wrapper
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
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
name|FileStatus
name|stat
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|wrapper
operator|.
name|add
argument_list|(
name|stat
operator|.
name|getOwner
argument_list|()
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
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|wrapper
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Running job as "
operator|+
name|wrapper
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|wrapper
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to stat file "
operator|+
name|p
operator|+
literal|" as either current user("
operator|+
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
operator|+
literal|") or table owner("
operator|+
name|t
operator|.
name|getOwner
argument_list|()
operator|+
literal|"), giving up"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to stat file: "
operator|+
name|p
argument_list|)
throw|;
block|}
comment|/**    * Determine whether to run this job as the current user or whether we need a doAs to switch    * users.    * @param owner of the directory we will be working in, as determined by    * {@link #findUserToRunAs(String, org.apache.hadoop.hive.metastore.api.Table)}    * @return true if the job should run as the current user, false if a doAs is needed.    */
specifier|protected
name|boolean
name|runJobAsSelf
parameter_list|(
name|String
name|owner
parameter_list|)
block|{
return|return
operator|(
name|owner
operator|.
name|equals
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
operator|)
return|;
block|}
specifier|protected
name|String
name|tableName
parameter_list|(
name|Table
name|t
parameter_list|)
block|{
return|return
name|Warehouse
operator|.
name|getQualifiedName
argument_list|(
name|t
argument_list|)
return|;
block|}
block|}
end_class

end_unit

