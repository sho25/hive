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
name|ddl
operator|.
name|table
operator|.
name|storage
operator|.
name|skewed
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
name|Arrays
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|ErrorMsg
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
name|lib
operator|.
name|Node
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
name|ASTNode
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
name|BaseSemanticAnalyzer
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
name|HiveParser
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
name|SemanticException
import|;
end_import

begin_comment
comment|/**  * Utilities for skewed table related DDL.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|SkewedTableUtils
block|{
specifier|private
name|SkewedTableUtils
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"SkewedTableUtils should not be instantiated!"
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|analyzeSkewedTableDDLColNames
parameter_list|(
name|ASTNode
name|node
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ASTNode
name|child
init|=
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|SKEWED_TABLE_NO_COLUMN_NAME
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
name|child
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_TABCOLNAME
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|SKEWED_TABLE_NO_COLUMN_NAME
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
return|return
name|BaseSemanticAnalyzer
operator|.
name|getColumnNames
argument_list|(
name|child
argument_list|)
return|;
block|}
block|}
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|analyzeDDLSkewedValues
parameter_list|(
name|ASTNode
name|node
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ASTNode
name|child
init|=
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|SKEWED_TABLE_NO_COLUMN_VALUE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|skewedValues
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|child
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
name|TOK_TABCOLVALUE
case|:
for|for
control|(
name|String
name|skewedValue
range|:
name|getSkewedValueFromASTNode
argument_list|(
name|child
argument_list|)
control|)
block|{
name|skewedValues
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|skewedValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|HiveParser
operator|.
name|TOK_TABCOLVALUE_PAIR
case|:
for|for
control|(
name|Node
name|cvNode
range|:
name|child
operator|.
name|getChildren
argument_list|()
control|)
block|{
name|ASTNode
name|acvNode
init|=
operator|(
name|ASTNode
operator|)
name|cvNode
decl_stmt|;
if|if
condition|(
name|acvNode
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_TABCOLVALUES
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|SKEWED_TABLE_NO_COLUMN_VALUE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|skewedValues
operator|.
name|add
argument_list|(
name|getSkewedValuesFromASTNode
argument_list|(
name|acvNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
default|default:
break|break;
block|}
return|return
name|skewedValues
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getSkewedValuesFromASTNode
parameter_list|(
name|ASTNode
name|node
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ASTNode
name|child
init|=
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|SKEWED_TABLE_NO_COLUMN_VALUE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
name|child
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_TABCOLVALUE
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|SKEWED_TABLE_NO_COLUMN_VALUE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|getSkewedValueFromASTNode
argument_list|(
name|child
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
comment|/**    * Gets the skewed column list from the statement.    *   create table xyz list bucketed (col1) with skew (1,2,5)    *   AST Node is for (1,2,5)    */
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getSkewedValueFromASTNode
parameter_list|(
name|ASTNode
name|node
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|colList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|numCh
init|=
name|node
operator|.
name|getChildCount
argument_list|()
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
name|numCh
condition|;
name|i
operator|++
control|)
block|{
name|ASTNode
name|child
init|=
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|colList
operator|.
name|add
argument_list|(
name|BaseSemanticAnalyzer
operator|.
name|stripQuotes
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|)
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|colList
return|;
block|}
block|}
end_class

end_unit

