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
name|antlr
operator|.
name|runtime
operator|.
name|tree
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

begin_class
specifier|public
class|class
name|SemanticAnalyzerFactory
block|{
specifier|public
specifier|static
name|BaseSemanticAnalyzer
name|get
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|CommonTree
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|tree
operator|.
name|getToken
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Empty Syntax Tree"
argument_list|)
throw|;
block|}
else|else
block|{
switch|switch
condition|(
name|tree
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_EXPLAIN
case|:
return|return
operator|new
name|ExplainSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_LOAD
case|:
return|return
operator|new
name|LoadSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_CREATETABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATEEXTTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_REPLACECOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAME
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_DROPPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWTABLES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWPARTITIONS
case|:
return|return
operator|new
name|DDLSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_CREATEFUNCTION
case|:
return|return
operator|new
name|FunctionSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
default|default:
return|return
operator|new
name|SemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

