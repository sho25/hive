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
name|lib
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Vector
import|;
end_import

begin_comment
comment|/**  * This interface defines the functions needed by the walkers and dispatchers.  * These are implemented by the node of the graph that needs to be walked.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Node
block|{
comment|/**    * Gets the vector of children nodes. This is used in the graph walker algorithms.    *     * @return Vector<Node>    */
specifier|public
name|Vector
argument_list|<
name|Node
argument_list|>
name|getChildren
parameter_list|()
function_decl|;
comment|/**    * Gets the name of the node. This is used in the rule dispatchers.    *     * @return String    */
specifier|public
name|String
name|getName
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

