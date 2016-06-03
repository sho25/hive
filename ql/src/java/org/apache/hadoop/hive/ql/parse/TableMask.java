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
name|List
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
name|TokenRewriteStream
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
name|metadata
operator|.
name|Table
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
name|HiveAuthorizer
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
name|HivePrivilegeObject
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
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
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
name|HiveAuthzContext
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
comment|/**  * The main purpose for this class is for authorization. More specifically, row  * filtering and column masking are done through this class. We first call  * create function to create the corresponding strings for row filtering and  * column masking. We then replace the TAB_REF with the strings.  */
end_comment

begin_class
specifier|public
class|class
name|TableMask
block|{
specifier|protected
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TableMask
operator|.
name|class
argument_list|)
decl_stmt|;
name|HiveAuthorizer
name|authorizer
decl_stmt|;
specifier|private
name|UnparseTranslator
name|translator
decl_stmt|;
specifier|private
name|boolean
name|enable
decl_stmt|;
specifier|private
name|boolean
name|needsRewrite
decl_stmt|;
specifier|private
name|HiveAuthzContext
name|queryContext
decl_stmt|;
specifier|public
name|TableMask
parameter_list|(
name|SemanticAnalyzer
name|analyzer
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|authorizer
operator|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getAuthorizerV2
argument_list|()
expr_stmt|;
name|String
name|cmdString
init|=
name|analyzer
operator|.
name|ctx
operator|.
name|getCmd
argument_list|()
decl_stmt|;
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|HiveAuthzContext
operator|.
name|Builder
name|ctxBuilder
init|=
operator|new
name|HiveAuthzContext
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|ctxBuilder
operator|.
name|setCommandString
argument_list|(
name|cmdString
argument_list|)
expr_stmt|;
name|ctxBuilder
operator|.
name|setUserIpAddress
argument_list|(
name|ss
operator|.
name|getUserIpAddress
argument_list|()
argument_list|)
expr_stmt|;
name|ctxBuilder
operator|.
name|setForwardedAddresses
argument_list|(
name|ss
operator|.
name|getForwardedAddresses
argument_list|()
argument_list|)
expr_stmt|;
name|queryContext
operator|=
name|ctxBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
if|if
condition|(
name|authorizer
operator|!=
literal|null
operator|&&
name|needTransform
argument_list|()
condition|)
block|{
name|enable
operator|=
literal|true
expr_stmt|;
name|translator
operator|=
operator|new
name|UnparseTranslator
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|translator
operator|.
name|enable
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to initialize masking policy"
argument_list|)
expr_stmt|;
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
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|applyRowFilterAndColumnMasking
parameter_list|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|privObjs
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|authorizer
operator|.
name|applyRowFilterAndColumnMasking
argument_list|(
name|queryContext
argument_list|,
name|privObjs
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isEnabled
parameter_list|()
throws|throws
name|SemanticException
block|{
return|return
name|enable
return|;
block|}
specifier|public
name|boolean
name|needTransform
parameter_list|()
throws|throws
name|SemanticException
block|{
return|return
name|authorizer
operator|.
name|needTransform
argument_list|()
return|;
block|}
specifier|public
name|String
name|create
parameter_list|(
name|HivePrivilegeObject
name|privObject
parameter_list|,
name|MaskAndFilterInfo
name|maskAndFilterInfo
parameter_list|)
throws|throws
name|SemanticException
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"(SELECT "
argument_list|)
expr_stmt|;
name|boolean
name|firstOne
init|=
literal|true
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|exprs
init|=
name|privObject
operator|.
name|getCellValueTransformers
argument_list|()
decl_stmt|;
if|if
condition|(
name|exprs
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|exprs
operator|.
name|size
argument_list|()
operator|!=
name|privObject
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Expect "
operator|+
name|privObject
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|" columns in "
operator|+
name|privObject
operator|.
name|getObjectName
argument_list|()
operator|+
literal|", but only find "
operator|+
name|exprs
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|colTypes
init|=
name|maskAndFilterInfo
operator|.
name|colTypes
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|exprs
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|String
name|expr
init|=
name|exprs
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|expr
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Expect string type CellValueTransformer in "
operator|+
name|privObject
operator|.
name|getObjectName
argument_list|()
operator|+
literal|", but only find null"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|firstOne
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|firstOne
operator|=
literal|false
expr_stmt|;
block|}
name|String
name|colName
init|=
name|privObject
operator|.
name|getColumns
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|expr
operator|.
name|equals
argument_list|(
name|colName
argument_list|)
condition|)
block|{
comment|// CAST(expr AS COLTYPE) AS COLNAME
name|sb
operator|.
name|append
argument_list|(
literal|"CAST("
operator|+
name|expr
operator|+
literal|" AS "
operator|+
name|colTypes
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|+
literal|") AS `"
operator|+
name|colName
operator|+
literal|"`"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|expr
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|privObject
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|String
name|expr
init|=
name|privObject
operator|.
name|getColumns
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|firstOne
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|firstOne
operator|=
literal|false
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|expr
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|" FROM `"
operator|+
name|privObject
operator|.
name|getDbname
argument_list|()
operator|+
literal|"`.`"
operator|+
name|privObject
operator|.
name|getObjectName
argument_list|()
operator|+
literal|"`"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" "
operator|+
name|maskAndFilterInfo
operator|.
name|additionalTabInfo
argument_list|)
expr_stmt|;
name|String
name|filter
init|=
name|privObject
operator|.
name|getRowFilterExpression
argument_list|()
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" WHERE "
operator|+
name|filter
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
operator|+
name|maskAndFilterInfo
operator|.
name|alias
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"TableMask creates `"
operator|+
name|sb
operator|.
name|toString
argument_list|()
operator|+
literal|"`"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
name|void
name|addTableMasking
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|String
name|replacementText
parameter_list|)
throws|throws
name|SemanticException
block|{
name|translator
operator|.
name|addTranslation
argument_list|(
name|node
argument_list|,
name|replacementText
argument_list|)
expr_stmt|;
block|}
name|void
name|applyTableMasking
parameter_list|(
name|TokenRewriteStream
name|tokenRewriteStream
parameter_list|)
throws|throws
name|SemanticException
block|{
name|translator
operator|.
name|applyTranslations
argument_list|(
name|tokenRewriteStream
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|needsRewrite
parameter_list|()
block|{
return|return
name|needsRewrite
return|;
block|}
specifier|public
name|void
name|setNeedsRewrite
parameter_list|(
name|boolean
name|needsRewrite
parameter_list|)
block|{
name|this
operator|.
name|needsRewrite
operator|=
name|needsRewrite
expr_stmt|;
block|}
block|}
end_class

end_unit

