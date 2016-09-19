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
name|metastore
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
comment|/**  * A thread that runs in the metastore, separate from the threads in the thrift service.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetaStoreThread
block|{
comment|/**    * Set the Hive configuration for this thread.    * @param conf    */
name|void
name|setHiveConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
function_decl|;
comment|/**    * Set the id for this thread.    * @param threadId    */
name|void
name|setThreadId
parameter_list|(
name|int
name|threadId
parameter_list|)
function_decl|;
comment|/**    * Initialize the thread.  This must not be called until after    * {@link #setHiveConf(org.apache.hadoop.hive.conf.HiveConf)} and  {@link #setThreadId(int)}    * have been called.    * @param stop a flag to watch for when to stop.  If this value is set to true,    *             the thread will terminate the next time through its main loop.    * @param looped a flag that is set to true everytime a thread goes through it's main loop.    *               This is purely for testing so that tests can assure themselves that the thread    *               has run through it's loop once.  The test can set this value to false.  The    *               thread should then assure that the loop has been gone completely through at    *               least once.    */
comment|// TODO: move these test parameters to more specific places... there's no need to have them here
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
function_decl|;
comment|/**    * Run the thread in the background.  This must not be called until    * {@link ##init(java.util.concurrent.atomic.AtomicBoolean, java.util.concurrent.atomic.AtomicBoolean)} has    * been called.    */
name|void
name|start
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

