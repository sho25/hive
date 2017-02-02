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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|Future
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
comment|/**  * ObjectCache. Interface for maintaining objects associated with a task.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ObjectCache
block|{
comment|/**    * @param key    */
specifier|public
name|void
name|release
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
comment|/**    * Retrieve object from cache.    *    * @param<T>    * @param key    * @param fn    *          function to generate the object if it's not there    * @return the last cached object with the key, null if none.    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|retrieve
parameter_list|(
name|String
name|key
parameter_list|,
name|Callable
argument_list|<
name|T
argument_list|>
name|fn
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Retrieve object from cache.    *    * @param<T>    * @param key    *          function to generate the object if it's not there    * @return the last cached object with the key, null if none.    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|retrieve
parameter_list|(
name|String
name|key
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Retrieve object from cache asynchronously.    *    * @param<T>    * @param key    * @param fn    *          function to generate the object if it's not there    * @return the last cached object with the key, null if none.    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Future
argument_list|<
name|T
argument_list|>
name|retrieveAsync
parameter_list|(
name|String
name|key
parameter_list|,
name|Callable
argument_list|<
name|T
argument_list|>
name|fn
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Removes the specified key from the object cache.    *    * @param key - key to be removed    */
specifier|public
name|void
name|remove
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

