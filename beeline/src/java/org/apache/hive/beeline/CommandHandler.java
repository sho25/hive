begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * See SQLLine notice in LICENSE  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
package|;
end_package

begin_import
import|import
name|jline
operator|.
name|Completor
import|;
end_import

begin_comment
comment|/**  * A generic command to be executed. Execution of the command  * should be dispatched to the {@link #execute(java.lang.String)} method after determining that  * the command is appropriate with  * the {@link #matches(java.lang.String)} method.  *  */
end_comment

begin_interface
interface|interface
name|CommandHandler
block|{
comment|/**    * @return the name of the command    */
specifier|public
name|String
name|getName
parameter_list|()
function_decl|;
comment|/**    * @return all the possible names of this command.    */
specifier|public
name|String
index|[]
name|getNames
parameter_list|()
function_decl|;
comment|/**    * @return the short help description for this command.    */
specifier|public
name|String
name|getHelpText
parameter_list|()
function_decl|;
comment|/**    * Check to see if the specified string can be dispatched to this    * command.    *    * @param line    *          the command line to check.    * @return the command string that matches, or null if it no match    */
specifier|public
name|String
name|matches
parameter_list|(
name|String
name|line
parameter_list|)
function_decl|;
comment|/**    * Execute the specified command.    *    * @param line    *          the full command line to execute.    */
specifier|public
name|boolean
name|execute
parameter_list|(
name|String
name|line
parameter_list|)
function_decl|;
comment|/**    * Returns the completors that can handle parameters.    */
specifier|public
name|Completor
index|[]
name|getParameterCompletors
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

