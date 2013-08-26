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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|shell
operator|.
name|Command
import|;
end_import

begin_comment
comment|/**  * A {@link Command} implementation that uses reflection to  * determine the method to dispatch the command.  *  */
end_comment

begin_class
specifier|public
class|class
name|ReflectiveCommandHandler
extends|extends
name|AbstractCommandHandler
block|{
specifier|private
specifier|final
name|BeeLine
name|beeLine
decl_stmt|;
specifier|public
name|ReflectiveCommandHandler
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|,
name|String
index|[]
name|cmds
parameter_list|,
name|Completor
index|[]
name|completor
parameter_list|)
block|{
name|super
argument_list|(
name|beeLine
argument_list|,
name|cmds
argument_list|,
name|beeLine
operator|.
name|loc
argument_list|(
literal|"help-"
operator|+
name|cmds
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|completor
argument_list|)
expr_stmt|;
name|this
operator|.
name|beeLine
operator|=
name|beeLine
expr_stmt|;
block|}
specifier|public
name|boolean
name|execute
parameter_list|(
name|String
name|line
parameter_list|)
block|{
try|try
block|{
name|Object
name|ob
init|=
name|beeLine
operator|.
name|getCommands
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
name|getName
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|String
operator|.
name|class
block|}
argument_list|)
operator|.
name|invoke
argument_list|(
name|beeLine
operator|.
name|getCommands
argument_list|()
argument_list|,
operator|new
name|Object
index|[]
block|{
name|line
block|}
argument_list|)
decl_stmt|;
return|return
name|ob
operator|!=
literal|null
operator|&&
name|ob
operator|instanceof
name|Boolean
operator|&&
operator|(
operator|(
name|Boolean
operator|)
name|ob
operator|)
operator|.
name|booleanValue
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
return|return
name|beeLine
operator|.
name|error
argument_list|(
name|e
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

