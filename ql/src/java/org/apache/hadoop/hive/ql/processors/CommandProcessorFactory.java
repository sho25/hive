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
name|processors
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
operator|.
name|isBlank
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

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
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|Driver
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

begin_comment
comment|/**  * CommandProcessorFactory.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|CommandProcessorFactory
block|{
specifier|private
name|CommandProcessorFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|HiveConf
argument_list|,
name|Driver
argument_list|>
name|mapDrivers
init|=
name|Collections
operator|.
name|synchronizedMap
argument_list|(
operator|new
name|HashMap
argument_list|<
name|HiveConf
argument_list|,
name|Driver
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|CommandProcessor
name|get
parameter_list|(
name|String
name|cmd
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|get
argument_list|(
name|cmd
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|CommandProcessor
name|getForHiveCommand
parameter_list|(
name|String
name|cmd
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SQLException
block|{
name|HiveCommand
name|hiveCommand
init|=
name|HiveCommand
operator|.
name|find
argument_list|(
name|cmd
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveCommand
operator|==
literal|null
operator|||
name|isBlank
argument_list|(
name|cmd
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|availableCommands
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|availableCommand
range|:
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SECURITY_COMMAND_WHITELIST
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|availableCommands
operator|.
name|add
argument_list|(
name|availableCommand
operator|.
name|toLowerCase
argument_list|()
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|availableCommands
operator|.
name|contains
argument_list|(
name|cmd
operator|.
name|trim
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Insufficient privileges to execute "
operator|+
name|cmd
argument_list|,
literal|"42000"
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|hiveCommand
condition|)
block|{
case|case
name|SET
case|:
return|return
operator|new
name|SetProcessor
argument_list|()
return|;
case|case
name|RESET
case|:
return|return
operator|new
name|ResetProcessor
argument_list|()
return|;
case|case
name|DFS
case|:
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
operator|new
name|DfsProcessor
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
argument_list|)
return|;
case|case
name|ADD
case|:
return|return
operator|new
name|AddResourceProcessor
argument_list|()
return|;
case|case
name|DELETE
case|:
return|return
operator|new
name|DeleteResourceProcessor
argument_list|()
return|;
case|case
name|COMPILE
case|:
return|return
operator|new
name|CompileProcessor
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unknown HiveCommand "
operator|+
name|hiveCommand
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|CommandProcessor
name|get
parameter_list|(
name|String
name|cmd
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SQLException
block|{
name|CommandProcessor
name|result
init|=
name|getForHiveCommand
argument_list|(
name|cmd
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
if|if
condition|(
name|isBlank
argument_list|(
name|cmd
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|Driver
argument_list|()
return|;
block|}
name|Driver
name|drv
init|=
name|mapDrivers
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|drv
operator|==
literal|null
condition|)
block|{
name|drv
operator|=
operator|new
name|Driver
argument_list|()
expr_stmt|;
name|mapDrivers
operator|.
name|put
argument_list|(
name|conf
argument_list|,
name|drv
argument_list|)
expr_stmt|;
block|}
name|drv
operator|.
name|init
argument_list|()
expr_stmt|;
return|return
name|drv
return|;
block|}
block|}
specifier|public
specifier|static
name|void
name|clean
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|Driver
name|drv
init|=
name|mapDrivers
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|drv
operator|!=
literal|null
condition|)
block|{
name|drv
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
name|mapDrivers
operator|.
name|remove
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

