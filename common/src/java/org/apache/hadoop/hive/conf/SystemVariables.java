begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|conf
package|;
end_package

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
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|valcoersion
operator|.
name|JavaIOTmpdirVariableCoercion
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
name|valcoersion
operator|.
name|VariableCoercion
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

begin_class
specifier|public
class|class
name|SystemVariables
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|l4j
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SystemVariables
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|Pattern
name|varPat
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\$\\{[^\\}\\$\u0020]+\\}"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|SystemVariables
name|INSTANCE
init|=
operator|new
name|SystemVariables
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|VariableCoercion
argument_list|>
name|COERCIONS
init|=
name|ImmutableMap
operator|.
expr|<
name|String
decl_stmt|,
name|VariableCoercion
decl|>
name|builder
argument_list|()
decl|.
name|put
argument_list|(
name|JavaIOTmpdirVariableCoercion
operator|.
name|INSTANCE
operator|.
name|getName
argument_list|()
argument_list|,
name|JavaIOTmpdirVariableCoercion
operator|.
name|INSTANCE
argument_list|)
decl|.
name|build
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ENV_PREFIX
init|=
literal|"env:"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SYSTEM_PREFIX
init|=
literal|"system:"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVECONF_PREFIX
init|=
literal|"hiveconf:"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVEVAR_PREFIX
init|=
literal|"hivevar:"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|METACONF_PREFIX
init|=
literal|"metaconf:"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SET_COLUMN_NAME
init|=
literal|"set"
decl_stmt|;
specifier|protected
name|String
name|getSubstitute
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|variableName
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|variableName
operator|.
name|startsWith
argument_list|(
name|SYSTEM_PREFIX
argument_list|)
condition|)
block|{
name|String
name|propertyName
init|=
name|variableName
operator|.
name|substring
argument_list|(
name|SYSTEM_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|originalValue
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
return|return
name|applyCoercion
argument_list|(
name|variableName
argument_list|,
name|originalValue
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|SecurityException
name|se
parameter_list|)
block|{
name|l4j
operator|.
name|warn
argument_list|(
literal|"Unexpected SecurityException in Configuration"
argument_list|,
name|se
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|variableName
operator|.
name|startsWith
argument_list|(
name|ENV_PREFIX
argument_list|)
condition|)
block|{
return|return
name|System
operator|.
name|getenv
argument_list|(
name|variableName
operator|.
name|substring
argument_list|(
name|ENV_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|conf
operator|!=
literal|null
operator|&&
name|variableName
operator|.
name|startsWith
argument_list|(
name|HIVECONF_PREFIX
argument_list|)
condition|)
block|{
return|return
name|conf
operator|.
name|get
argument_list|(
name|variableName
operator|.
name|substring
argument_list|(
name|HIVECONF_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|String
name|applyCoercion
parameter_list|(
name|String
name|variableName
parameter_list|,
name|String
name|originalValue
parameter_list|)
block|{
if|if
condition|(
name|COERCIONS
operator|.
name|containsKey
argument_list|(
name|variableName
argument_list|)
condition|)
block|{
return|return
name|COERCIONS
operator|.
name|get
argument_list|(
name|variableName
argument_list|)
operator|.
name|getCoerced
argument_list|(
name|originalValue
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|originalValue
return|;
block|}
block|}
specifier|public
specifier|static
name|boolean
name|containsVar
parameter_list|(
name|String
name|expr
parameter_list|)
block|{
return|return
name|expr
operator|!=
literal|null
operator|&&
name|varPat
operator|.
name|matcher
argument_list|(
name|expr
argument_list|)
operator|.
name|find
argument_list|()
return|;
block|}
specifier|static
name|String
name|substitute
parameter_list|(
name|String
name|expr
parameter_list|)
block|{
return|return
name|expr
operator|==
literal|null
condition|?
literal|null
else|:
name|INSTANCE
operator|.
name|substitute
argument_list|(
literal|null
argument_list|,
name|expr
argument_list|,
literal|1
argument_list|)
return|;
block|}
specifier|static
name|String
name|substitute
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|expr
parameter_list|)
block|{
return|return
name|expr
operator|==
literal|null
condition|?
literal|null
else|:
name|INSTANCE
operator|.
name|substitute
argument_list|(
name|conf
argument_list|,
name|expr
argument_list|,
literal|1
argument_list|)
return|;
block|}
specifier|protected
specifier|final
name|String
name|substitute
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|expr
parameter_list|,
name|int
name|depth
parameter_list|)
block|{
name|long
name|maxLength
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
name|maxLength
operator|=
name|HiveConf
operator|.
name|getSizeVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_QUERY_MAX_LENGTH
argument_list|)
expr_stmt|;
block|}
name|Matcher
name|match
init|=
name|varPat
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|String
name|eval
init|=
name|expr
decl_stmt|;
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|s
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
name|s
operator|<=
name|depth
condition|;
name|s
operator|++
control|)
block|{
name|match
operator|.
name|reset
argument_list|(
name|eval
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|int
name|prev
init|=
literal|0
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|match
operator|.
name|find
argument_list|(
name|prev
argument_list|)
condition|)
block|{
name|String
name|group
init|=
name|match
operator|.
name|group
argument_list|()
decl_stmt|;
name|String
name|var
init|=
name|group
operator|.
name|substring
argument_list|(
literal|2
argument_list|,
name|group
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// remove ${ .. }
name|String
name|substitute
init|=
name|getSubstitute
argument_list|(
name|conf
argument_list|,
name|var
argument_list|)
decl_stmt|;
if|if
condition|(
name|substitute
operator|==
literal|null
condition|)
block|{
name|substitute
operator|=
name|group
expr_stmt|;
comment|// append as-is
block|}
else|else
block|{
name|found
operator|=
literal|true
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|eval
operator|.
name|substring
argument_list|(
name|prev
argument_list|,
name|match
operator|.
name|start
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
name|substitute
argument_list|)
expr_stmt|;
if|if
condition|(
name|maxLength
operator|>
literal|0
operator|&&
name|builder
operator|.
name|length
argument_list|()
operator|>
name|maxLength
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Query length longer than hive.query.max.length ("
operator|+
name|builder
operator|.
name|length
argument_list|()
operator|+
literal|">"
operator|+
name|maxLength
operator|+
literal|")."
argument_list|)
throw|;
block|}
name|prev
operator|=
name|match
operator|.
name|end
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
return|return
name|eval
return|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|eval
operator|.
name|substring
argument_list|(
name|prev
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|maxLength
operator|>
literal|0
operator|&&
name|builder
operator|.
name|length
argument_list|()
operator|>
name|maxLength
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Query length longer than hive.query.max.length ("
operator|+
name|builder
operator|.
name|length
argument_list|()
operator|+
literal|">"
operator|+
name|maxLength
operator|+
literal|")."
argument_list|)
throw|;
block|}
name|eval
operator|=
name|builder
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|s
operator|>
name|depth
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Variable substitution depth is deeper than "
operator|+
name|depth
operator|+
literal|" for expression "
operator|+
name|expr
argument_list|)
throw|;
block|}
return|return
name|eval
return|;
block|}
block|}
end_class

end_unit

