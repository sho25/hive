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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|conf
operator|.
name|SystemVariables
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

begin_class
specifier|public
class|class
name|VariableSubstitution
extends|extends
name|SystemVariables
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|VariableSubstitution
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|String
name|getSubstitute
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|var
parameter_list|)
block|{
name|String
name|val
init|=
name|super
operator|.
name|getSubstitute
argument_list|(
name|conf
argument_list|,
name|var
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
operator|&&
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|vars
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveVariables
argument_list|()
decl_stmt|;
if|if
condition|(
name|var
operator|.
name|startsWith
argument_list|(
name|HIVEVAR_PREFIX
argument_list|)
condition|)
block|{
name|val
operator|=
name|vars
operator|.
name|get
argument_list|(
name|var
operator|.
name|substring
argument_list|(
name|HIVEVAR_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|val
operator|=
name|vars
operator|.
name|get
argument_list|(
name|var
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|val
return|;
block|}
specifier|public
name|String
name|substitute
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|expr
parameter_list|)
block|{
if|if
condition|(
name|expr
operator|==
literal|null
condition|)
block|{
return|return
name|expr
return|;
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVEVARIABLESUBSTITUTE
argument_list|)
condition|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"Substitution is on: "
operator|+
name|expr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
name|expr
return|;
block|}
name|int
name|depth
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVEVARIABLESUBSTITUTEDEPTH
argument_list|)
decl_stmt|;
return|return
name|substitute
argument_list|(
name|conf
argument_list|,
name|expr
argument_list|,
name|depth
argument_list|)
return|;
block|}
block|}
end_class

end_unit

