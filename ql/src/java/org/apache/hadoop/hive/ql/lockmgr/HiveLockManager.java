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
name|hive
operator|.
name|ql
operator|.
name|Driver
operator|.
name|LockedDriverState
import|;
end_import

begin_comment
comment|/**  * Manager for locks in Hive.  Users should not instantiate a lock manager  * directly.  Instead they should get an instance from their instance of  * {@link HiveTxnManager}.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveLockManager
block|{
specifier|public
name|void
name|setContext
parameter_list|(
name|HiveLockManagerCtx
name|ctx
parameter_list|)
throws|throws
name|LockException
function_decl|;
comment|/**    * @param key        object to be locked    * @param mode       mode of the lock (SHARED/EXCLUSIVE)    * @param keepAlive  if the lock needs to be persisted after the statement    */
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
function_decl|;
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
function_decl|;
specifier|public
name|void
name|unlock
parameter_list|(
name|HiveLock
name|hiveLock
parameter_list|)
throws|throws
name|LockException
function_decl|;
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
function_decl|;
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
function_decl|;
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
function_decl|;
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|LockException
function_decl|;
specifier|public
name|void
name|prepareRetry
parameter_list|()
throws|throws
name|LockException
function_decl|;
comment|/**    * refresh to enable new configurations.    */
specifier|public
name|void
name|refresh
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

