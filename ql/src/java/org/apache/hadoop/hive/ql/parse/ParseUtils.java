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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|plan
operator|.
name|ExprNodeDesc
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
name|serde2
operator|.
name|objectinspector
operator|.
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|serde2
operator|.
name|typeinfo
operator|.
name|BaseCharTypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|CharTypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|DecimalTypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|serde2
operator|.
name|typeinfo
operator|.
name|VarcharTypeInfo
import|;
end_import

begin_comment
comment|/**  * Library of utility functions used in the parse code.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ParseUtils
block|{
comment|/**    * Tests whether the parse tree node is a join token.    *    * @param node    *          The parse tree node    * @return boolean    */
specifier|public
specifier|static
name|boolean
name|isJoinToken
parameter_list|(
name|ASTNode
name|node
parameter_list|)
block|{
switch|switch
condition|(
name|node
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
name|TOK_JOIN
case|:
case|case
name|HiveParser
operator|.
name|TOK_LEFTOUTERJOIN
case|:
case|case
name|HiveParser
operator|.
name|TOK_RIGHTOUTERJOIN
case|:
case|case
name|HiveParser
operator|.
name|TOK_FULLOUTERJOIN
case|:
return|return
literal|true
return|;
default|default:
return|return
literal|false
return|;
block|}
block|}
comment|/**    * Performs a descent of the leftmost branch of a tree, stopping when either a    * node with a non-null token is found or the leaf level is encountered.    *    * @param tree    *          candidate node from which to start searching    *    * @return node at which descent stopped    */
specifier|public
specifier|static
name|ASTNode
name|findRootNonNullToken
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
block|{
while|while
condition|(
operator|(
name|tree
operator|.
name|getToken
argument_list|()
operator|==
literal|null
operator|)
operator|&&
operator|(
name|tree
operator|.
name|getChildCount
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
name|tree
operator|=
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|tree
return|;
block|}
specifier|private
name|ParseUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|validateColumnNameUniqueness
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// no duplicate column names
comment|// currently, it is a simple n*n algorithm - this can be optimized later if
comment|// need be
comment|// but it should not be a major bottleneck as the number of columns are
comment|// anyway not so big
name|Iterator
argument_list|<
name|FieldSchema
argument_list|>
name|iterCols
init|=
name|fieldSchemas
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterCols
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|colName
init|=
name|iterCols
operator|.
name|next
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|iter
init|=
name|colNames
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|oldColName
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|colName
operator|.
name|equalsIgnoreCase
argument_list|(
name|oldColName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|DUPLICATE_COLUMN_NAMES
operator|.
name|getMsg
argument_list|(
name|oldColName
argument_list|)
argument_list|)
throw|;
block|}
block|}
name|colNames
operator|.
name|add
argument_list|(
name|colName
argument_list|)
expr_stmt|;
block|}
return|return
name|colNames
return|;
block|}
comment|/**    * @param column  column expression to convert    * @param tableFieldTypeInfo TypeInfo to convert to    * @return Expression converting column to the type specified by tableFieldTypeInfo    */
specifier|static
name|ExprNodeDesc
name|createConversionCast
parameter_list|(
name|ExprNodeDesc
name|column
parameter_list|,
name|PrimitiveTypeInfo
name|tableFieldTypeInfo
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Get base type, since type string may be parameterized
name|String
name|baseType
init|=
name|TypeInfoUtils
operator|.
name|getBaseName
argument_list|(
name|tableFieldTypeInfo
operator|.
name|getTypeName
argument_list|()
argument_list|)
decl_stmt|;
comment|// If the type cast UDF is for a parameterized type, then it should implement
comment|// the SettableUDF interface so that we can pass in the params.
comment|// Not sure if this is the cleanest solution, but there does need to be a way
comment|// to provide the type params to the type cast.
return|return
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDescWithUdfData
argument_list|(
name|baseType
argument_list|,
name|tableFieldTypeInfo
argument_list|,
name|column
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|VarcharTypeInfo
name|getVarcharTypeInfo
parameter_list|(
name|ASTNode
name|node
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Bad params for type varchar"
argument_list|)
throw|;
block|}
name|String
name|lengthStr
init|=
name|node
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
decl_stmt|;
return|return
name|TypeInfoFactory
operator|.
name|getVarcharTypeInfo
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|lengthStr
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|CharTypeInfo
name|getCharTypeInfo
parameter_list|(
name|ASTNode
name|node
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Bad params for type char"
argument_list|)
throw|;
block|}
name|String
name|lengthStr
init|=
name|node
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
decl_stmt|;
return|return
name|TypeInfoFactory
operator|.
name|getCharTypeInfo
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|lengthStr
argument_list|)
argument_list|)
return|;
block|}
specifier|static
name|int
name|getIndex
parameter_list|(
name|String
index|[]
name|list
parameter_list|,
name|String
name|elem
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|list
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|list
index|[
name|i
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|elem
argument_list|)
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/*    * if the given filterCondn refers to only 1 table alias in the QBJoinTree,    * we return that alias's position. Otherwise we return -1    */
specifier|static
name|int
name|checkJoinFilterRefersOneAlias
parameter_list|(
name|String
index|[]
name|tabAliases
parameter_list|,
name|ASTNode
name|filterCondn
parameter_list|)
block|{
switch|switch
condition|(
name|filterCondn
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
case|:
name|String
name|tableOrCol
init|=
name|SemanticAnalyzer
operator|.
name|unescapeIdentifier
argument_list|(
name|filterCondn
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|getIndex
argument_list|(
name|tabAliases
argument_list|,
name|tableOrCol
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|Identifier
case|:
case|case
name|HiveParser
operator|.
name|Number
case|:
case|case
name|HiveParser
operator|.
name|StringLiteral
case|:
case|case
name|HiveParser
operator|.
name|BigintLiteral
case|:
case|case
name|HiveParser
operator|.
name|SmallintLiteral
case|:
case|case
name|HiveParser
operator|.
name|TinyintLiteral
case|:
case|case
name|HiveParser
operator|.
name|DecimalLiteral
case|:
case|case
name|HiveParser
operator|.
name|TOK_STRINGLITERALSEQUENCE
case|:
case|case
name|HiveParser
operator|.
name|TOK_CHARSETLITERAL
case|:
case|case
name|HiveParser
operator|.
name|TOK_DATELITERAL
case|:
case|case
name|HiveParser
operator|.
name|KW_TRUE
case|:
case|case
name|HiveParser
operator|.
name|KW_FALSE
case|:
case|case
name|HiveParser
operator|.
name|TOK_NULL
case|:
return|return
operator|-
literal|1
return|;
default|default:
name|int
name|idx
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|i
init|=
name|filterCondn
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_FUNCTION
condition|?
literal|1
else|:
literal|0
decl_stmt|;
for|for
control|(
init|;
name|i
operator|<
name|filterCondn
operator|.
name|getChildCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|cIdx
init|=
name|checkJoinFilterRefersOneAlias
argument_list|(
name|tabAliases
argument_list|,
operator|(
name|ASTNode
operator|)
name|filterCondn
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|cIdx
operator|!=
name|idx
condition|)
block|{
if|if
condition|(
name|idx
operator|!=
operator|-
literal|1
operator|&&
name|cIdx
operator|!=
operator|-
literal|1
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|idx
operator|=
name|idx
operator|==
operator|-
literal|1
condition|?
name|cIdx
else|:
name|idx
expr_stmt|;
block|}
block|}
return|return
name|idx
return|;
block|}
block|}
specifier|public
specifier|static
name|DecimalTypeInfo
name|getDecimalTypeTypeInfo
parameter_list|(
name|ASTNode
name|node
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|>
literal|2
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Bad params for type decimal"
argument_list|)
throw|;
block|}
name|int
name|precision
init|=
name|HiveDecimal
operator|.
name|DEFAULT_PRECISION
decl_stmt|;
name|int
name|scale
init|=
name|HiveDecimal
operator|.
name|DEFAULT_SCALE
decl_stmt|;
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|>=
literal|1
condition|)
block|{
name|String
name|precStr
init|=
name|node
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
decl_stmt|;
name|precision
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|precStr
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
condition|)
block|{
name|String
name|scaleStr
init|=
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
decl_stmt|;
name|scale
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|scaleStr
argument_list|)
expr_stmt|;
block|}
return|return
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
name|precision
argument_list|,
name|scale
argument_list|)
return|;
block|}
block|}
end_class

end_unit

