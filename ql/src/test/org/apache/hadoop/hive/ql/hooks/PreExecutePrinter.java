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
name|hooks
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|security
operator|.
name|UserGroupInformation
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
name|session
operator|.
name|SessionState
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
import|;
end_import

begin_comment
comment|/**  * Implementation of a pre execute hook that simply prints out its  * parameters to standard output.  */
end_comment

begin_class
specifier|public
class|class
name|PreExecutePrinter
implements|implements
name|PreExecute
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|SessionState
name|sess
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|)
throws|throws
name|Exception
block|{
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
return|return;
if|if
condition|(
name|sess
operator|!=
literal|null
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"PREHOOK: query: "
operator|+
name|sess
operator|.
name|getCmd
argument_list|()
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"PREHOOK: type: "
operator|+
name|sess
operator|.
name|getCommandType
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ReadEntity
name|re
range|:
name|inputs
control|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"PREHOOK: Input: "
operator|+
name|re
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|WriteEntity
name|we
range|:
name|outputs
control|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"PREHOOK: Output: "
operator|+
name|we
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

