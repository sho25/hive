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
name|parse
package|;
end_package

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|CommonTree
import|;
end_import

begin_comment
comment|/**  * Interface that a parse tree processor needs to implement  */
end_comment

begin_interface
specifier|public
interface|interface
name|ASTProcessor
block|{
comment|/** 	 * Sets the event dispatcher for the processors 	 *  	 * @param dispatcher The parse tree event dispatcher 	 */
name|void
name|setDispatcher
parameter_list|(
name|ASTEventDispatcher
name|dispatcher
parameter_list|)
function_decl|;
comment|/** 	 * Processes the parse tree and calls the registered event processors 	 * for the associated parse tree events 	 *  	 * @param pt The parse tree to process 	 */
name|void
name|process
parameter_list|(
name|CommonTree
name|pt
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

