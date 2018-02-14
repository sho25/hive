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
name|ql
operator|.
name|metadata
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|exec
operator|.
name|Utilities
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
name|exec
operator|.
name|tez
operator|.
name|TezContext
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
name|HadoopDefaultAuthenticator
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
name|HiveAuthenticationProvider
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
name|DefaultHiveAuthorizationProvider
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
name|HiveAuthorizationProvider
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
name|HiveMetastoreAuthorizationProvider
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
name|HiveAuthorizerFactory
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
name|sqlstd
operator|.
name|SQLStdHiveAuthorizerFactory
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
name|io
operator|.
name|Text
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
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * General collection of helper functions.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|HiveUtils
block|{
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
specifier|static
specifier|final
name|byte
index|[]
name|escapeEscapeBytes
init|=
literal|"\\\\"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
empty_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|escapeUnescapeBytes
init|=
literal|"\\"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|newLineEscapeBytes
init|=
literal|"\\n"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
empty_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|newLineUnescapeBytes
init|=
literal|"\n"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|carriageReturnEscapeBytes
init|=
literal|"\\r"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
empty_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|carriageReturnUnescapeBytes
init|=
literal|"\r"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|tabEscapeBytes
init|=
literal|"\\t"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
empty_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|tabUnescapeBytes
init|=
literal|"\t"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|ctrlABytes
init|=
literal|"\u0001"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
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
name|HiveUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Text
name|escapeText
parameter_list|(
name|Text
name|text
parameter_list|)
block|{
name|int
name|length
init|=
name|text
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|byte
index|[]
name|textBytes
init|=
name|text
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|Text
name|escape
init|=
operator|new
name|Text
argument_list|(
name|text
argument_list|)
decl_stmt|;
name|escape
operator|.
name|clear
argument_list|()
expr_stmt|;
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
name|int
name|c
init|=
name|text
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|byte
index|[]
name|escaped
decl_stmt|;
name|int
name|start
decl_stmt|;
name|int
name|len
decl_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
literal|'\\'
case|:
name|escaped
operator|=
name|escapeEscapeBytes
expr_stmt|;
name|start
operator|=
literal|0
expr_stmt|;
name|len
operator|=
name|escaped
operator|.
name|length
expr_stmt|;
break|break;
case|case
literal|'\n'
case|:
name|escaped
operator|=
name|newLineEscapeBytes
expr_stmt|;
name|start
operator|=
literal|0
expr_stmt|;
name|len
operator|=
name|escaped
operator|.
name|length
expr_stmt|;
break|break;
case|case
literal|'\r'
case|:
name|escaped
operator|=
name|carriageReturnEscapeBytes
expr_stmt|;
name|start
operator|=
literal|0
expr_stmt|;
name|len
operator|=
name|escaped
operator|.
name|length
expr_stmt|;
break|break;
case|case
literal|'\t'
case|:
name|escaped
operator|=
name|tabEscapeBytes
expr_stmt|;
name|start
operator|=
literal|0
expr_stmt|;
name|len
operator|=
name|escaped
operator|.
name|length
expr_stmt|;
break|break;
case|case
literal|'\u0001'
case|:
name|escaped
operator|=
name|tabUnescapeBytes
expr_stmt|;
name|start
operator|=
literal|0
expr_stmt|;
name|len
operator|=
name|escaped
operator|.
name|length
expr_stmt|;
break|break;
default|default:
name|escaped
operator|=
name|textBytes
expr_stmt|;
name|start
operator|=
name|i
expr_stmt|;
name|len
operator|=
literal|1
expr_stmt|;
break|break;
block|}
name|escape
operator|.
name|append
argument_list|(
name|escaped
argument_list|,
name|start
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
return|return
name|escape
return|;
block|}
specifier|public
specifier|static
name|int
name|unescapeText
parameter_list|(
name|Text
name|text
parameter_list|)
block|{
name|Text
name|escape
init|=
operator|new
name|Text
argument_list|(
name|text
argument_list|)
decl_stmt|;
name|text
operator|.
name|clear
argument_list|()
expr_stmt|;
name|int
name|length
init|=
name|escape
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|byte
index|[]
name|textBytes
init|=
name|escape
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|boolean
name|hadSlash
init|=
literal|false
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
name|int
name|c
init|=
name|escape
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
literal|'\\'
case|:
if|if
condition|(
name|hadSlash
condition|)
block|{
name|text
operator|.
name|append
argument_list|(
name|textBytes
argument_list|,
name|i
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|hadSlash
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|hadSlash
operator|=
literal|true
expr_stmt|;
block|}
break|break;
case|case
literal|'n'
case|:
if|if
condition|(
name|hadSlash
condition|)
block|{
name|byte
index|[]
name|newLine
init|=
name|newLineUnescapeBytes
decl_stmt|;
name|text
operator|.
name|append
argument_list|(
name|newLine
argument_list|,
literal|0
argument_list|,
name|newLine
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|text
operator|.
name|append
argument_list|(
name|textBytes
argument_list|,
name|i
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|hadSlash
operator|=
literal|false
expr_stmt|;
break|break;
case|case
literal|'r'
case|:
if|if
condition|(
name|hadSlash
condition|)
block|{
name|byte
index|[]
name|carriageReturn
init|=
name|carriageReturnUnescapeBytes
decl_stmt|;
name|text
operator|.
name|append
argument_list|(
name|carriageReturn
argument_list|,
literal|0
argument_list|,
name|carriageReturn
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|text
operator|.
name|append
argument_list|(
name|textBytes
argument_list|,
name|i
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|hadSlash
operator|=
literal|false
expr_stmt|;
break|break;
case|case
literal|'t'
case|:
if|if
condition|(
name|hadSlash
condition|)
block|{
name|byte
index|[]
name|tab
init|=
name|tabUnescapeBytes
decl_stmt|;
name|text
operator|.
name|append
argument_list|(
name|tab
argument_list|,
literal|0
argument_list|,
name|tab
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|text
operator|.
name|append
argument_list|(
name|textBytes
argument_list|,
name|i
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|hadSlash
operator|=
literal|false
expr_stmt|;
break|break;
case|case
literal|'\t'
case|:
if|if
condition|(
name|hadSlash
condition|)
block|{
name|text
operator|.
name|append
argument_list|(
name|textBytes
argument_list|,
name|i
operator|-
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|hadSlash
operator|=
literal|false
expr_stmt|;
block|}
name|byte
index|[]
name|ctrlA
init|=
name|ctrlABytes
decl_stmt|;
name|text
operator|.
name|append
argument_list|(
name|ctrlA
argument_list|,
literal|0
argument_list|,
name|ctrlA
operator|.
name|length
argument_list|)
expr_stmt|;
break|break;
default|default:
if|if
condition|(
name|hadSlash
condition|)
block|{
name|text
operator|.
name|append
argument_list|(
name|textBytes
argument_list|,
name|i
operator|-
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|hadSlash
operator|=
literal|false
expr_stmt|;
block|}
name|text
operator|.
name|append
argument_list|(
name|textBytes
argument_list|,
name|i
argument_list|,
literal|1
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
return|return
name|text
operator|.
name|getLength
argument_list|()
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
return|return
name|unparseIdentifier
argument_list|(
name|identifier
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|unparseIdentifier
parameter_list|(
name|String
name|identifier
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
comment|// In the future, if we support arbitrary characters in
comment|// identifiers, then we'll need to escape any backticks
comment|// in identifier by doubling them up.
comment|// the time has come
name|String
name|qIdSupport
init|=
name|conf
operator|==
literal|null
condition|?
literal|null
else|:
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_QUOTEDID_SUPPORT
argument_list|)
decl_stmt|;
if|if
condition|(
name|qIdSupport
operator|!=
literal|null
operator|&&
operator|!
literal|"none"
operator|.
name|equals
argument_list|(
name|qIdSupport
argument_list|)
condition|)
block|{
name|identifier
operator|=
name|identifier
operator|.
name|replaceAll
argument_list|(
literal|"`"
argument_list|,
literal|"``"
argument_list|)
expr_stmt|;
block|}
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
name|Utilities
operator|.
name|getSessionSpecifiedClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|HiveStorageHandler
name|storageHandler
init|=
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|List
argument_list|<
name|HiveMetastoreAuthorizationProvider
argument_list|>
name|getMetaStoreAuthorizeProviderManagers
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HiveConf
operator|.
name|ConfVars
name|authorizationProviderConfKey
parameter_list|,
name|HiveAuthenticationProvider
name|authenticator
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|clsStrs
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|authorizationProviderConfKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|clsStrs
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|HiveMetastoreAuthorizationProvider
argument_list|>
name|authProviders
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveMetastoreAuthorizationProvider
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|clsStr
range|:
name|clsStrs
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding metastore authorization provider: "
operator|+
name|clsStr
argument_list|)
expr_stmt|;
name|authProviders
operator|.
name|add
argument_list|(
operator|(
name|HiveMetastoreAuthorizationProvider
operator|)
name|getAuthorizeProviderManager
argument_list|(
name|conf
argument_list|,
name|clsStr
argument_list|,
name|authenticator
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|authProviders
return|;
block|}
comment|/**    * Create a new instance of HiveAuthorizationProvider    * @param conf    * @param authzClassName - authorization provider class name    * @param authenticator    * @param nullIfOtherClass - return null if configuration    *  does not point to a HiveAuthorizationProvider subclass    * @return new instance of HiveAuthorizationProvider    * @throws HiveException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|HiveAuthorizationProvider
name|getAuthorizeProviderManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|authzClassName
parameter_list|,
name|HiveAuthenticationProvider
name|authenticator
parameter_list|,
name|boolean
name|nullIfOtherClass
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveAuthorizationProvider
name|ret
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|HiveAuthorizationProvider
argument_list|>
name|cls
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|authzClassName
operator|==
literal|null
operator|||
name|authzClassName
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|cls
operator|=
name|DefaultHiveAuthorizationProvider
operator|.
name|class
expr_stmt|;
block|}
else|else
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|configClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|authzClassName
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|nullIfOtherClass
operator|&&
operator|!
name|HiveAuthorizationProvider
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|configClass
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|cls
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HiveAuthorizationProvider
argument_list|>
operator|)
name|configClass
expr_stmt|;
block|}
if|if
condition|(
name|cls
operator|!=
literal|null
condition|)
block|{
name|ret
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|cls
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|ret
operator|.
name|setAuthenticator
argument_list|(
name|authenticator
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
comment|/**    * Return HiveAuthorizerFactory used by new authorization plugin interface.    * @param conf    * @param authorizationProviderConfKey    * @return    * @throws HiveException if HiveAuthorizerFactory specified in configuration could not    */
specifier|public
specifier|static
name|HiveAuthorizerFactory
name|getAuthorizerFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HiveConf
operator|.
name|ConfVars
name|authorizationProviderConfKey
parameter_list|)
throws|throws
name|HiveException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|HiveAuthorizerFactory
argument_list|>
name|cls
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|authorizationProviderConfKey
operator|.
name|varname
argument_list|,
name|SQLStdHiveAuthorizerFactory
operator|.
name|class
argument_list|,
name|HiveAuthorizerFactory
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|cls
operator|==
literal|null
condition|)
block|{
comment|//should not happen as default value is set
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Configuration value "
operator|+
name|authorizationProviderConfKey
operator|.
name|varname
operator|+
literal|" is not set to valid HiveAuthorizerFactory subclass"
argument_list|)
throw|;
block|}
name|HiveAuthorizerFactory
name|authFactory
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|cls
argument_list|,
name|conf
argument_list|)
decl_stmt|;
return|return
name|authFactory
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|HiveAuthenticationProvider
name|getAuthenticator
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HiveConf
operator|.
name|ConfVars
name|authenticatorConfKey
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|clsStr
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|authenticatorConfKey
argument_list|)
decl_stmt|;
name|HiveAuthenticationProvider
name|ret
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|HiveAuthenticationProvider
argument_list|>
name|cls
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|clsStr
operator|==
literal|null
operator|||
name|clsStr
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|cls
operator|=
name|HadoopDefaultAuthenticator
operator|.
name|class
expr_stmt|;
block|}
else|else
block|{
name|cls
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HiveAuthenticationProvider
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|clsStr
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cls
operator|!=
literal|null
condition|)
block|{
name|ret
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|cls
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
specifier|static
name|String
name|getLocalDirList
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
condition|)
block|{
name|TezContext
name|tezContext
init|=
operator|(
name|TezContext
operator|)
name|TezContext
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|tezContext
operator|!=
literal|null
operator|&&
name|tezContext
operator|.
name|getTezProcessorContext
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|StringUtils
operator|.
name|arrayToString
argument_list|(
name|tezContext
operator|.
name|getTezProcessorContext
argument_list|()
operator|.
name|getWorkDirs
argument_list|()
argument_list|)
return|;
block|}
comment|// otherwise fall back to return null, i.e. to use local tmp dir only
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

