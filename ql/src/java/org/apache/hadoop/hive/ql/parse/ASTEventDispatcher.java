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
comment|/**  * Dispatches ParseTreeEvent to the appropriate ParseTreeEventProcessor  */
end_comment

begin_interface
specifier|public
interface|interface
name|ASTEventDispatcher
block|{
comment|/** 	 * Registers the event processor with the event 	 *  	 * @param evt The parse tree event 	 * @param evt_p The associated parse tree event processor 	 */
name|void
name|register
parameter_list|(
name|ASTEvent
name|evt
parameter_list|,
name|ASTEventProcessor
name|evt_p
parameter_list|)
function_decl|;
comment|/** 	 * Dispatches the parse tree event to a registered event processor 	 *  	 * @param evt The parse tree event to dispatch 	 * @param pt The parse subtree to dispatch to the event processor 	 */
name|void
name|dispatch
parameter_list|(
name|ASTEvent
name|evt
parameter_list|,
name|CommonTree
name|pt
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

