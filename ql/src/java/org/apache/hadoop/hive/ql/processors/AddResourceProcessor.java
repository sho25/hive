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
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|HiveVariableSource
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
name|VariableSubstitution
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveOperationType
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * AddResourceProcessor.  *  */
end_comment

begin_class
specifier|public
class|class
name|AddResourceProcessor
implements|implements
name|CommandProcessor
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|AddResourceProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LogHelper
name|console
init|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|CommandProcessorResponse
name|run
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|command
operator|=
operator|new
name|VariableSubstitution
argument_list|(
operator|new
name|HiveVariableSource
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveVariable
parameter_list|()
block|{
return|return
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveVariables
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|substitute
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
argument_list|,
name|command
argument_list|)
expr_stmt|;
name|String
index|[]
name|tokens
init|=
name|command
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|ResourceType
name|t
decl_stmt|;
if|if
condition|(
name|tokens
operator|.
name|length
operator|<
literal|2
operator|||
operator|(
name|t
operator|=
name|SessionState
operator|.
name|find_resource_type
argument_list|(
name|tokens
index|[
literal|0
index|]
argument_list|)
operator|)
operator|==
literal|null
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Usage: add ["
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|SessionState
operator|.
name|ResourceType
operator|.
name|values
argument_list|()
argument_list|,
literal|"|"
argument_list|)
operator|+
literal|"]<value> [<value>]*"
argument_list|)
expr_stmt|;
return|return
operator|new
name|CommandProcessorResponse
argument_list|(
literal|1
argument_list|)
return|;
block|}
name|CommandProcessorResponse
name|authErrResp
init|=
name|CommandUtil
operator|.
name|authorizeCommand
argument_list|(
name|ss
argument_list|,
name|HiveOperationType
operator|.
name|ADD
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|tokens
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|authErrResp
operator|!=
literal|null
condition|)
block|{
comment|// there was an authorization issue
return|return
name|authErrResp
return|;
block|}
try|try
block|{
name|ss
operator|.
name|add_resources
argument_list|(
name|t
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|tokens
argument_list|,
literal|1
argument_list|,
name|tokens
operator|.
name|length
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|CommandProcessorResponse
operator|.
name|create
argument_list|(
name|e
argument_list|)
return|;
block|}
return|return
operator|new
name|CommandProcessorResponse
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
end_class

end_unit

