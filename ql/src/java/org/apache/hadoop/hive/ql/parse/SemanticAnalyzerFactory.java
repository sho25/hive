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
name|HashMap
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * SemanticAnalyzerFactory.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|SemanticAnalyzerFactory
block|{
specifier|static
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|commandType
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_EXPLAIN
argument_list|,
literal|"EXPLAIN"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_LOAD
argument_list|,
literal|"LOAD"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATETABLE
argument_list|,
literal|"CREATETABLE"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPTABLE
argument_list|,
literal|"DROPTABLE"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DESCTABLE
argument_list|,
literal|"DESCTABLE"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DESCFUNCTION
argument_list|,
literal|"DESCFUNCTION"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_MSCK
argument_list|,
literal|"MSCK"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCOLS
argument_list|,
literal|"ALTERTABLE_ADDCOLS"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_REPLACECOLS
argument_list|,
literal|"ALTERTABLE_REPLACECOLS"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAMECOL
argument_list|,
literal|"ALTERTABLE_RENAMECOL"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAME
argument_list|,
literal|"ALTERTABLE_RENAME"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_DROPPARTS
argument_list|,
literal|"ALTERTABLE_DROPPARTS"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDPARTS
argument_list|,
literal|"ALTERTABLE_ADDPARTS"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_TOUCH
argument_list|,
literal|"ALTERTABLE_TOUCH"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ARCHIVE
argument_list|,
literal|"ALTERTABLE_ARCHIVE"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_UNARCHIVE
argument_list|,
literal|"ALTERTABLE_UNARCHIVE"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PROPERTIES
argument_list|,
literal|"ALTERTABLE_PROPERTIES"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERIALIZER
argument_list|,
literal|"ALTERTABLE_SERIALIZER"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERDEPROPERTIES
argument_list|,
literal|"ALTERTABLE_SERDEPROPERTIES"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWTABLES
argument_list|,
literal|"SHOWTABLES"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOW_TABLESTATUS
argument_list|,
literal|"SHOW_TABLESTATUS"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWFUNCTIONS
argument_list|,
literal|"SHOWFUNCTIONS"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWPARTITIONS
argument_list|,
literal|"SHOWPARTITIONS"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEFUNCTION
argument_list|,
literal|"CREATEFUNCTION"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPFUNCTION
argument_list|,
literal|"DROPFUNCTION"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEVIEW
argument_list|,
literal|"CREATEVIEW"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPVIEW
argument_list|,
literal|"DROPVIEW"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEINDEX
argument_list|,
literal|"CREATEINDEX"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPINDEX
argument_list|,
literal|"DROPINDEX"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERINDEX_REBUILD
argument_list|,
literal|"ALTERINDEX_REBUILD"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERVIEW_PROPERTIES
argument_list|,
literal|"ALTERVIEW_PROPERTIES"
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_QUERY
argument_list|,
literal|"QUERY"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|BaseSemanticAnalyzer
name|get
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|ASTNode
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
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|setCommandType
argument_list|(
name|commandType
operator|.
name|get
argument_list|(
name|tree
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|TOK_DROPTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPVIEW
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCFUNCTION
case|:
case|case
name|HiveParser
operator|.
name|TOK_MSCK
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAMECOL
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
name|TOK_ALTERTABLE_ADDPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ALTERPARTS_PROTECTMODE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERIALIZER
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERDEPROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERINDEX_REBUILD
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWTABLES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_TABLESTATUS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWFUNCTIONS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWPARTITIONS
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATEINDEX
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPINDEX
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_FILEFORMAT
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PROTECTMODE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_CLUSTER_SORT
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_TOUCH
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ARCHIVE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_UNARCHIVE
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
case|case
name|HiveParser
operator|.
name|TOK_DROPFUNCTION
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
specifier|private
name|SemanticAnalyzerFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

