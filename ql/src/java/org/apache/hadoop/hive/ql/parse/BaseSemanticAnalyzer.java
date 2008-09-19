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
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
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
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|CommonTree
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
name|Context
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
name|metadata
operator|.
name|*
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
name|Task
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
name|TaskFactory
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

begin_class
specifier|public
specifier|abstract
class|class
name|BaseSemanticAnalyzer
block|{
specifier|protected
name|String
name|scratchDir
decl_stmt|;
specifier|protected
name|int
name|randomid
decl_stmt|;
specifier|protected
name|int
name|pathid
decl_stmt|;
specifier|protected
specifier|final
name|Hive
name|db
decl_stmt|;
specifier|protected
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
decl_stmt|;
specifier|protected
specifier|final
name|Log
name|LOG
decl_stmt|;
specifier|protected
specifier|final
name|LogHelper
name|console
decl_stmt|;
specifier|protected
name|Context
name|ctx
decl_stmt|;
specifier|public
name|BaseSemanticAnalyzer
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|db
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|rootTasks
operator|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|LOG
operator|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
name|this
operator|.
name|scratchDir
operator|=
name|this
operator|.
name|db
operator|.
name|getConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
argument_list|)
expr_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|this
operator|.
name|randomid
operator|=
name|Math
operator|.
name|abs
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
operator|%
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|pathid
operator|=
literal|10000
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|abstract
name|void
name|analyze
parameter_list|(
name|CommonTree
name|ast
parameter_list|,
name|Context
name|ctx
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getRootTasks
parameter_list|()
block|{
return|return
name|rootTasks
return|;
block|}
specifier|protected
name|void
name|reset
parameter_list|()
block|{
name|rootTasks
operator|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|stripQuotes
parameter_list|(
name|String
name|val
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|val
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'\''
operator|&&
name|val
operator|.
name|charAt
argument_list|(
name|val
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
operator|==
literal|'\''
condition|)
block|{
name|val
operator|=
name|val
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|val
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|val
return|;
block|}
specifier|public
specifier|static
name|String
name|charSetString
parameter_list|(
name|String
name|charSetName
parameter_list|,
name|String
name|charSetString
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
comment|// The character set name starts with a _, so strip that
name|charSetName
operator|=
name|charSetName
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|charSetString
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'\''
condition|)
return|return
operator|new
name|String
argument_list|(
name|unescapeSQLString
argument_list|(
name|charSetString
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|,
name|charSetName
argument_list|)
return|;
else|else
comment|// hex input is also supported
block|{
assert|assert
name|charSetString
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'0'
assert|;
assert|assert
name|charSetString
operator|.
name|charAt
argument_list|(
literal|1
argument_list|)
operator|==
literal|'x'
assert|;
name|charSetString
operator|=
name|charSetString
operator|.
name|substring
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bArray
init|=
operator|new
name|byte
index|[
name|charSetString
operator|.
name|length
argument_list|()
operator|/
literal|2
index|]
decl_stmt|;
name|int
name|j
init|=
literal|0
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
name|charSetString
operator|.
name|length
argument_list|()
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|int
name|val
init|=
name|Character
operator|.
name|digit
argument_list|(
name|charSetString
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|,
literal|16
argument_list|)
operator|*
literal|16
operator|+
name|Character
operator|.
name|digit
argument_list|(
name|charSetString
operator|.
name|charAt
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|,
literal|16
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|>
literal|127
condition|)
name|val
operator|=
name|val
operator|-
literal|256
expr_stmt|;
name|bArray
index|[
name|j
operator|++
index|]
operator|=
operator|new
name|Integer
argument_list|(
name|val
argument_list|)
operator|.
name|byteValue
argument_list|()
expr_stmt|;
block|}
name|String
name|res
init|=
operator|new
name|String
argument_list|(
name|bArray
argument_list|,
name|charSetName
argument_list|)
decl_stmt|;
return|return
name|res
return|;
block|}
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
specifier|static
name|String
name|unescapeSQLString
parameter_list|(
name|String
name|b
parameter_list|)
block|{
assert|assert
operator|(
name|b
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'\''
operator|)
assert|;
assert|assert
operator|(
name|b
operator|.
name|charAt
argument_list|(
name|b
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
operator|==
literal|'\''
operator|)
assert|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|b
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|+
literal|1
operator|<
name|b
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|b
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'\\'
operator|&&
name|i
operator|+
literal|2
operator|<
name|b
operator|.
name|length
argument_list|()
condition|)
block|{
name|char
name|n
init|=
name|b
operator|.
name|charAt
argument_list|(
name|i
operator|+
literal|1
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|n
condition|)
block|{
case|case
literal|'0'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\0"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\''
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'"'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\""
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'b'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\b"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'n'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'r'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\r"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'t'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\t"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'Z'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\u001A"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\\'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\\"
argument_list|)
expr_stmt|;
break|break;
comment|// The following 2 lines are exactly what MySQL does
case|case
literal|'%'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\\%"
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'_'
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"\\_"
argument_list|)
expr_stmt|;
break|break;
default|default:
name|sb
operator|.
name|append
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
name|i
operator|++
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|b
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getTmpFileName
parameter_list|()
block|{
comment|// generate the temporary file
name|String
name|taskTmpDir
init|=
name|this
operator|.
name|scratchDir
operator|+
name|File
operator|.
name|separator
operator|+
name|this
operator|.
name|randomid
operator|+
literal|'.'
operator|+
name|this
operator|.
name|pathid
decl_stmt|;
name|this
operator|.
name|pathid
operator|++
expr_stmt|;
return|return
name|taskTmpDir
return|;
block|}
specifier|public
specifier|static
class|class
name|tableSpec
block|{
specifier|public
name|String
name|tableName
decl_stmt|;
specifier|public
name|Table
name|tableHandle
decl_stmt|;
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
specifier|public
name|Partition
name|partHandle
decl_stmt|;
specifier|public
name|tableSpec
parameter_list|(
name|Hive
name|db
parameter_list|,
name|CommonTree
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
assert|assert
operator|(
name|ast
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TAB
operator|)
assert|;
name|int
name|childIndex
init|=
literal|0
decl_stmt|;
try|try
block|{
comment|// get table metadata
name|tableName
operator|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
expr_stmt|;
name|tableHandle
operator|=
name|db
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// get partition metadata if partition specified
if|if
condition|(
name|ast
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
condition|)
block|{
name|childIndex
operator|=
literal|1
expr_stmt|;
name|CommonTree
name|partspec
init|=
operator|(
name|CommonTree
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|partSpec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
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
name|partspec
operator|.
name|getChildCount
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|CommonTree
name|partspec_val
init|=
operator|(
name|CommonTree
operator|)
name|partspec
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|val
init|=
name|stripQuotes
argument_list|(
name|partspec_val
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
name|partSpec
operator|.
name|put
argument_list|(
name|partspec_val
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
name|partHandle
operator|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartition
argument_list|(
name|tableHandle
argument_list|,
name|partSpec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InvalidTableException
name|ite
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_TABLE
operator|.
name|getMsg
argument_list|(
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
name|ite
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|GENERIC_ERROR
operator|.
name|getMsg
argument_list|(
name|ast
operator|.
name|getChild
argument_list|(
name|childIndex
argument_list|)
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|partHandle
operator|!=
literal|null
condition|)
return|return
name|partHandle
operator|.
name|toString
argument_list|()
return|;
else|else
return|return
name|tableHandle
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

