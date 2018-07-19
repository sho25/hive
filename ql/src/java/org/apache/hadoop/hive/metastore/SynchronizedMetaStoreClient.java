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
package|;
end_package

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
name|api
operator|.
name|AlreadyExistsException
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
name|EnvironmentContext
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
name|FireEventRequest
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
name|FireEventResponse
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
name|InvalidObjectException
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
name|LockRequest
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
name|LockResponse
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
name|UnknownTableException
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
name|WriteNotificationLogRequest
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

begin_comment
comment|/**  * Synchronized MetaStoreClient wrapper  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|SynchronizedMetaStoreClient
block|{
specifier|private
specifier|final
name|IMetaStoreClient
name|client
decl_stmt|;
specifier|public
name|SynchronizedMetaStoreClient
parameter_list|(
name|IMetaStoreClient
name|client
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|long
name|openTxn
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|openTxn
argument_list|(
name|user
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|commitTxn
parameter_list|(
name|long
name|txnid
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|commitTxn
argument_list|(
name|txnid
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|rollbackTxn
parameter_list|(
name|long
name|txnid
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|rollbackTxn
argument_list|(
name|txnid
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|heartbeat
parameter_list|(
name|long
name|txnid
parameter_list|,
name|long
name|lockid
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|heartbeat
argument_list|(
name|txnid
argument_list|,
name|lockid
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|ValidTxnList
name|getValidTxns
parameter_list|(
name|long
name|currentTxn
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|getValidTxns
argument_list|(
name|currentTxn
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|LockResponse
name|lock
parameter_list|(
name|LockRequest
name|request
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|lock
argument_list|(
name|request
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|Partition
name|add_partition
parameter_list|(
name|Partition
name|partition
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|add_partition
argument_list|(
name|partition
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|alter_partition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Partition
name|newPart
parameter_list|,
name|EnvironmentContext
name|environmentContext
parameter_list|,
name|String
name|writeIdList
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|alter_partition
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|newPart
argument_list|,
name|environmentContext
argument_list|,
name|writeIdList
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|LockResponse
name|checkLock
parameter_list|(
name|long
name|lockid
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|checkLock
argument_list|(
name|lockid
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|unlock
parameter_list|(
name|long
name|lockid
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|unlock
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|ShowLocksResponse
name|showLocks
parameter_list|(
name|ShowLocksRequest
name|showLocksRequest
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|showLocks
argument_list|(
name|showLocksRequest
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|Partition
name|getPartitionWithAuthInfo
parameter_list|(
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
name|pvals
parameter_list|,
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownTableException
throws|,
name|NoSuchObjectException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|getPartitionWithAuthInfo
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|pvals
argument_list|,
name|userName
argument_list|,
name|groupNames
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|Partition
name|appendPartition
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|table_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|AlreadyExistsException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|appendPartition
argument_list|(
name|db_name
argument_list|,
name|table_name
argument_list|,
name|part_vals
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|FireEventResponse
name|fireListenerEvent
parameter_list|(
name|FireEventRequest
name|rqst
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|fireListenerEvent
argument_list|(
name|rqst
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|addWriteNotificationLog
parameter_list|(
name|WriteNotificationLogRequest
name|rqst
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|addWriteNotificationLog
argument_list|(
name|rqst
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSameConfObj
parameter_list|(
name|Configuration
name|c
parameter_list|)
block|{
return|return
name|client
operator|.
name|isSameConfObj
argument_list|(
name|c
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isCompatibleWith
parameter_list|(
name|Configuration
name|c
parameter_list|)
block|{
return|return
name|client
operator|.
name|isCompatibleWith
argument_list|(
name|c
argument_list|)
return|;
block|}
block|}
end_class

end_unit

