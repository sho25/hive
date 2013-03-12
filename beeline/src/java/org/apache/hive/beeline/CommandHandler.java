begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  Copyright (c) 2002,2003,2004,2005 Marc Prud'hommeaux  *  All rights reserved.  *  *  *  Redistribution and use in source and binary forms,  *  with or without modification, are permitted provided  *  that the following conditions are met:  *  *  Redistributions of source code must retain the above  *  copyright notice, this list of conditions and the following  *  disclaimer.  *  Redistributions in binary form must reproduce the above  *  copyright notice, this list of conditions and the following  *  disclaimer in the documentation and/or other materials  *  provided with the distribution.  *  Neither the name of the<ORGANIZATION> nor the names  *  of its contributors may be used to endorse or promote  *  products derived from this software without specific  *  prior written permission.  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS  *  AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED  *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR  *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT  *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,  *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE  *  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR  *  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY  *  OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,  *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING  *  IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  *  *  This software is hosted by SourceForge.  *  SourceForge is a trademark of VA Linux Systems, Inc.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * The license above originally appeared in src/sqlline/SqlLine.java  * http://sqlline.sourceforge.net/  */
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

