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
name|Stack
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

begin_comment
comment|/**  * Rule interface for Operators Used in operator dispatching to dispatch  * process/visitor functions for operators  */
end_comment

begin_interface
specifier|public
interface|interface
name|Rule
block|{
comment|/**    * @return the cost of the rule - the lower the cost, the better the rule    *         matches    * @throws SemanticException    */
specifier|public
name|int
name|cost
parameter_list|(
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
comment|/**    * @return the name of the rule - may be useful for debugging    */
specifier|public
name|String
name|getName
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

