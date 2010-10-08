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
name|metadata
package|;
end_package

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
name|common
operator|.
name|JavaUtils
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
name|index
operator|.
name|HiveIndexHandler
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
name|parse
operator|.
name|AbstractSemanticAnalyzerHook
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * General collection of helper functions.  *   */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|HiveUtils
block|{
specifier|public
specifier|static
specifier|final
name|char
name|QUOTE
init|=
literal|'"'
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|char
name|COLON
init|=
literal|':'
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LBRACKET
init|=
literal|"["
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RBRACKET
init|=
literal|"]"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LBRACE
init|=
literal|"{"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RBRACE
init|=
literal|"}"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LINE_SEP
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|String
name|escapeString
parameter_list|(
name|String
name|str
parameter_list|)
block|{
name|int
name|length
init|=
name|str
operator|.
name|length
argument_list|()
decl_stmt|;
name|StringBuilder
name|escape
init|=
operator|new
name|StringBuilder
argument_list|(
name|length
operator|+
literal|16
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
operator|++
name|i
control|)
block|{
name|char
name|c
init|=
name|str
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
literal|'"'
case|:
case|case
literal|'\\'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\b'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'b'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\f'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'f'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\n'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'n'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\r'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'r'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\t'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'t'
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// Control characeters! According to JSON RFC u0020
if|if
condition|(
name|c
operator|<
literal|' '
condition|)
block|{
name|String
name|hex
init|=
name|Integer
operator|.
name|toHexString
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'u'
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|4
init|;
name|j
operator|>
name|hex
operator|.
name|length
argument_list|()
condition|;
operator|--
name|j
control|)
block|{
name|escape
operator|.
name|append
argument_list|(
literal|'0'
argument_list|)
expr_stmt|;
block|}
name|escape
operator|.
name|append
argument_list|(
name|hex
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|escape
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
return|return
operator|(
name|escape
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
specifier|public
specifier|static
name|String
name|lightEscapeString
parameter_list|(
name|String
name|str
parameter_list|)
block|{
name|int
name|length
init|=
name|str
operator|.
name|length
argument_list|()
decl_stmt|;
name|StringBuilder
name|escape
init|=
operator|new
name|StringBuilder
argument_list|(
name|length
operator|+
literal|16
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
operator|++
name|i
control|)
block|{
name|char
name|c
init|=
name|str
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
literal|'\n'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'n'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\r'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'r'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\t'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'t'
argument_list|)
expr_stmt|;
break|break;
default|default:
name|escape
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
return|return
operator|(
name|escape
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
comment|/**    * Regenerate an identifier as part of unparsing it back to SQL text.    */
specifier|public
specifier|static
name|String
name|unparseIdentifier
parameter_list|(
name|String
name|identifier
parameter_list|)
block|{
comment|// In the future, if we support arbitrary characters in
comment|// identifiers, then we'll need to escape any backticks
comment|// in identifier by doubling them up.
return|return
literal|"`"
operator|+
name|identifier
operator|+
literal|"`"
return|;
block|}
specifier|public
specifier|static
name|HiveStorageHandler
name|getStorageHandler
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|className
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|className
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|HiveStorageHandler
argument_list|>
name|handlerClass
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HiveStorageHandler
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|HiveStorageHandler
name|storageHandler
init|=
operator|(
name|HiveStorageHandler
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|handlerClass
argument_list|,
name|conf
argument_list|)
decl_stmt|;
return|return
name|storageHandler
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Error in loading storage handler."
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|HiveUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|public
specifier|static
name|HiveIndexHandler
name|getIndexHandler
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|indexHandlerClass
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|indexHandlerClass
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|HiveIndexHandler
argument_list|>
name|handlerClass
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HiveIndexHandler
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|indexHandlerClass
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|HiveIndexHandler
name|indexHandler
init|=
operator|(
name|HiveIndexHandler
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|handlerClass
argument_list|,
name|conf
argument_list|)
decl_stmt|;
return|return
name|indexHandler
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Error in loading index handler."
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|AbstractSemanticAnalyzerHook
name|getSemanticAnalyzerHook
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|hookName
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|AbstractSemanticAnalyzerHook
argument_list|>
name|hookClass
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|AbstractSemanticAnalyzerHook
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|hookName
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|(
name|AbstractSemanticAnalyzerHook
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|hookClass
argument_list|,
name|conf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Error in loading semantic analyzer hook: "
operator|+
name|hookName
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

