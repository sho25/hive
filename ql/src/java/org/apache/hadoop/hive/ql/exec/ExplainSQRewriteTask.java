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
name|exec
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|fs
operator|.
name|Path
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
name|DriverContext
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
name|QB
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
name|QBSubQuery
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
name|SubQueryDiagnostic
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
name|ExplainSQRewriteWork
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
name|api
operator|.
name|StageType
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
name|IOUtils
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

begin_class
specifier|public
class|class
name|ExplainSQRewriteTask
extends|extends
name|Task
argument_list|<
name|ExplainSQRewriteWork
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|EXPLAIN
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|PrintStream
name|out
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Path
name|resFile
init|=
operator|new
name|Path
argument_list|(
name|work
operator|.
name|getResFile
argument_list|()
argument_list|)
decl_stmt|;
name|OutputStream
name|outS
init|=
name|resFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|create
argument_list|(
name|resFile
argument_list|)
decl_stmt|;
name|out
operator|=
operator|new
name|PrintStream
argument_list|(
name|outS
argument_list|)
expr_stmt|;
name|QB
name|qb
init|=
name|work
operator|.
name|getQb
argument_list|()
decl_stmt|;
name|TokenRewriteStream
name|stream
init|=
name|work
operator|.
name|getCtx
argument_list|()
operator|.
name|getTokenRewriteStream
argument_list|()
decl_stmt|;
name|String
name|program
init|=
literal|"sq rewrite"
decl_stmt|;
name|ASTNode
name|ast
init|=
name|work
operator|.
name|getAst
argument_list|()
decl_stmt|;
try|try
block|{
name|addRewrites
argument_list|(
name|stream
argument_list|,
name|qb
argument_list|,
name|program
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"\nRewritten Query:\n"
operator|+
name|stream
operator|.
name|toString
argument_list|(
name|program
argument_list|,
name|ast
operator|.
name|getTokenStartIndex
argument_list|()
argument_list|,
name|ast
operator|.
name|getTokenStopIndex
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stream
operator|.
name|deleteProgram
argument_list|(
name|program
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|out
operator|=
literal|null
expr_stmt|;
return|return
operator|(
literal|0
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Failed with exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|(
literal|1
operator|)
return|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|addRewrites
parameter_list|(
name|TokenRewriteStream
name|stream
parameter_list|,
name|QB
name|qb
parameter_list|,
name|String
name|program
parameter_list|,
name|PrintStream
name|out
parameter_list|)
block|{
name|QBSubQuery
name|sqW
init|=
name|qb
operator|.
name|getWhereClauseSubQueryPredicate
argument_list|()
decl_stmt|;
name|QBSubQuery
name|sqH
init|=
name|qb
operator|.
name|getHavingClauseSubQueryPredicate
argument_list|()
decl_stmt|;
if|if
condition|(
name|sqW
operator|!=
literal|null
operator|||
name|sqH
operator|!=
literal|null
condition|)
block|{
name|ASTNode
name|sqNode
init|=
name|sqW
operator|!=
literal|null
condition|?
name|sqW
operator|.
name|getOriginalSubQueryASTForRewrite
argument_list|()
else|:
name|sqH
operator|.
name|getOriginalSubQueryASTForRewrite
argument_list|()
decl_stmt|;
name|ASTNode
name|tokQry
init|=
name|getQueryASTNode
argument_list|(
name|sqNode
argument_list|)
decl_stmt|;
name|ASTNode
name|tokFrom
init|=
operator|(
name|ASTNode
operator|)
name|tokQry
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|StringBuilder
name|addedJoins
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|sqW
operator|!=
literal|null
condition|)
block|{
name|addRewrites
argument_list|(
name|stream
argument_list|,
name|sqW
argument_list|,
name|program
argument_list|,
name|out
argument_list|,
name|qb
operator|.
name|getId
argument_list|()
argument_list|,
literal|true
argument_list|,
name|addedJoins
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sqH
operator|!=
literal|null
condition|)
block|{
name|addRewrites
argument_list|(
name|stream
argument_list|,
name|sqH
argument_list|,
name|program
argument_list|,
name|out
argument_list|,
name|qb
operator|.
name|getId
argument_list|()
argument_list|,
literal|false
argument_list|,
name|addedJoins
argument_list|)
expr_stmt|;
block|}
name|stream
operator|.
name|insertAfter
argument_list|(
name|program
argument_list|,
name|tokFrom
operator|.
name|getTokenStopIndex
argument_list|()
argument_list|,
name|addedJoins
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|sqAliases
init|=
name|qb
operator|.
name|getSubqAliases
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|sqAlias
range|:
name|sqAliases
control|)
block|{
name|addRewrites
argument_list|(
name|stream
argument_list|,
name|qb
operator|.
name|getSubqForAlias
argument_list|(
name|sqAlias
argument_list|)
operator|.
name|getQB
argument_list|()
argument_list|,
name|program
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|addRewrites
parameter_list|(
name|TokenRewriteStream
name|stream
parameter_list|,
name|QBSubQuery
name|sq
parameter_list|,
name|String
name|program
parameter_list|,
name|PrintStream
name|out
parameter_list|,
name|String
name|qbAlias
parameter_list|,
name|boolean
name|isWhere
parameter_list|,
name|StringBuilder
name|addedJoins
parameter_list|)
block|{
name|ASTNode
name|sqNode
init|=
name|sq
operator|.
name|getOriginalSubQueryASTForRewrite
argument_list|()
decl_stmt|;
name|ASTNode
name|tokQry
init|=
name|getQueryASTNode
argument_list|(
name|sqNode
argument_list|)
decl_stmt|;
name|ASTNode
name|tokInsert
init|=
operator|(
name|ASTNode
operator|)
name|tokQry
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ASTNode
name|tokWhere
init|=
literal|null
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
name|tokInsert
operator|.
name|getChildCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|tokInsert
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_WHERE
condition|)
block|{
name|tokWhere
operator|=
operator|(
name|ASTNode
operator|)
name|tokInsert
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|SubQueryDiagnostic
operator|.
name|QBSubQueryRewrite
name|diag
init|=
name|sq
operator|.
name|getDiagnostic
argument_list|()
decl_stmt|;
name|String
name|sqStr
init|=
name|diag
operator|.
name|getRewrittenQuery
argument_list|()
decl_stmt|;
name|String
name|joinCond
init|=
name|diag
operator|.
name|getJoiningCondition
argument_list|()
decl_stmt|;
comment|/*        * the SubQuery predicate has been hoisted as a Join. The SubQuery predicate is replaced        * by a 'true' predicate in the Outer QB's where/having clause.        */
name|stream
operator|.
name|replace
argument_list|(
name|program
argument_list|,
name|sqNode
operator|.
name|getTokenStartIndex
argument_list|()
argument_list|,
name|sqNode
operator|.
name|getTokenStopIndex
argument_list|()
argument_list|,
literal|"1 = 1"
argument_list|)
expr_stmt|;
name|String
name|sqJoin
init|=
literal|" "
operator|+
name|getJoinKeyWord
argument_list|(
name|sq
argument_list|)
operator|+
literal|" "
operator|+
name|sqStr
operator|+
literal|" "
operator|+
name|joinCond
decl_stmt|;
name|addedJoins
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
operator|.
name|append
argument_list|(
name|sqJoin
argument_list|)
expr_stmt|;
name|String
name|postJoinCond
init|=
name|diag
operator|.
name|getOuterQueryPostJoinCond
argument_list|()
decl_stmt|;
if|if
condition|(
name|postJoinCond
operator|!=
literal|null
condition|)
block|{
name|stream
operator|.
name|insertAfter
argument_list|(
name|program
argument_list|,
name|tokWhere
operator|.
name|getTokenStopIndex
argument_list|()
argument_list|,
literal|" and "
operator|+
name|postJoinCond
argument_list|)
expr_stmt|;
block|}
name|String
name|qualifier
init|=
name|isWhere
condition|?
literal|"Where Clause "
else|:
literal|"Having Clause "
decl_stmt|;
if|if
condition|(
name|qbAlias
operator|!=
literal|null
condition|)
block|{
name|qualifier
operator|=
name|qualifier
operator|+
literal|"for Query Block '"
operator|+
name|qbAlias
operator|+
literal|"' "
expr_stmt|;
block|}
name|out
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"\n%s Rewritten SubQuery:\n%s"
argument_list|,
name|qualifier
argument_list|,
name|diag
operator|.
name|getRewrittenQuery
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"\n%s SubQuery Joining Condition:\n%s"
argument_list|,
name|qualifier
argument_list|,
name|diag
operator|.
name|getJoiningCondition
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getJoinKeyWord
parameter_list|(
name|QBSubQuery
name|sq
parameter_list|)
block|{
switch|switch
condition|(
name|sq
operator|.
name|getJoinType
argument_list|()
condition|)
block|{
case|case
name|LEFTOUTER
case|:
return|return
literal|"left outer join"
return|;
case|case
name|LEFTSEMI
case|:
return|return
literal|"left semi join"
return|;
case|case
name|RIGHTOUTER
case|:
return|return
literal|"right outer join"
return|;
case|case
name|FULLOUTER
case|:
return|return
literal|"full outer join"
return|;
case|case
name|INNER
case|:
default|default:
return|return
literal|"inner join"
return|;
block|}
block|}
specifier|private
name|ASTNode
name|getQueryASTNode
parameter_list|(
name|ASTNode
name|node
parameter_list|)
block|{
while|while
condition|(
name|node
operator|!=
literal|null
operator|&&
name|node
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_QUERY
condition|)
block|{
name|node
operator|=
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
return|return
name|node
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"EXPLAIN REWRITE"
return|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getResultSchema
parameter_list|()
block|{
name|FieldSchema
name|tmpFieldSchema
init|=
operator|new
name|FieldSchema
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|colList
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|tmpFieldSchema
operator|.
name|setName
argument_list|(
name|ExplainTask
operator|.
name|EXPL_COLUMN_NAME
argument_list|)
expr_stmt|;
name|tmpFieldSchema
operator|.
name|setType
argument_list|(
name|STRING_TYPE_NAME
argument_list|)
expr_stmt|;
name|colList
operator|.
name|add
argument_list|(
name|tmpFieldSchema
argument_list|)
expr_stmt|;
return|return
name|colList
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|canExecuteInParallel
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

