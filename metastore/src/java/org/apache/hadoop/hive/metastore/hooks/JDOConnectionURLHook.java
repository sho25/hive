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
operator|.
name|hooks
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

begin_comment
comment|/**  * JDOConnectURLHook is used to get the URL that JDO uses to connect to the  * database that stores the metastore data. Classes implementing this must be  * thread-safe (for Thrift server).  */
end_comment

begin_interface
specifier|public
interface|interface
name|JDOConnectionURLHook
block|{
comment|/**    * Gets the connection URL to supply to JDO. In addition to initialization,    * this method will be called after a connection failure for each reconnect    * attempt.    *    * @param conf The configuration used to initialize this instance of the HMS    * @return the connection URL    * @throws Exception    */
specifier|public
name|String
name|getJdoConnectionUrl
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**    * Alerts this that the connection URL was bad. Can be used to collect stats,    * etc.    *    * @param url    */
specifier|public
name|void
name|notifyBadConnectionUrl
parameter_list|(
name|String
name|url
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

