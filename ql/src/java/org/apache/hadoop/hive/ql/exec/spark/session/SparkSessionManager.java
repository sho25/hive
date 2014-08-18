begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|exec
operator|.
name|spark
operator|.
name|session
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * Defines interface for managing multiple SparkSessions in Hive when multiple users  * are executing queries simultaneously on Spark execution engine.  */
end_comment

begin_interface
specifier|public
interface|interface
name|SparkSessionManager
block|{
comment|/**    * Initialize based on given configuration.    *    * @param hiveConf    */
specifier|public
name|void
name|setup
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Get a valid SparkSession. First try to check if existing session is reusable    * based on the given<i>conf</i>. If not release<i>existingSession</i> and return    * a new session based on session manager criteria and<i>conf</i>.    *    * @param existingSession Existing session (can be null)    * @param conf    * @param doOpen Should the session be opened before returning?    * @return    */
specifier|public
name|SparkSession
name|getSession
parameter_list|(
name|SparkSession
name|existingSession
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|boolean
name|doOpen
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Return the given<i>sparkSession</i> to pool. This is used when the client    * still holds references to session and may want to reuse it in future.    * When client wants to reuse the session, it should pass the it<i>getSession</i> method.    */
specifier|public
name|void
name|returnSession
parameter_list|(
name|SparkSession
name|sparkSession
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Close the given session and return it to pool. This is used when the client    * no longer needs a SparkSession.    */
specifier|public
name|void
name|closeSession
parameter_list|(
name|SparkSession
name|sparkSession
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Shutdown the session manager. Also closing up SparkSessions in pool.    */
specifier|public
name|void
name|shutdown
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

