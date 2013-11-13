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
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|ANTLRStringStream
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
name|CharStream
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
name|NoViableAltException
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
name|RecognitionException
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
name|Token
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
name|antlr
operator|.
name|runtime
operator|.
name|TokenStream
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
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|CommonTreeAdaptor
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
name|TreeAdaptor
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

begin_comment
comment|/**  * ParseDriver.  *  */
end_comment

begin_class
specifier|public
class|class
name|ParseDriver
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"hive.ql.parse.ParseDriver"
argument_list|)
decl_stmt|;
comment|/**    * ANTLRNoCaseStringStream.    *    */
comment|//This class provides and implementation for a case insensitive token checker
comment|//for the lexical analysis part of antlr. By converting the token stream into
comment|//upper case at the time when lexical rules are checked, this class ensures that the
comment|//lexical rules need to just match the token with upper case letters as opposed to
comment|//combination of upper case and lower case characteres. This is purely used for matching lexical
comment|//rules. The actual token text is stored in the same way as the user input without
comment|//actually converting it into an upper case. The token values are generated by the consume()
comment|//function of the super class ANTLRStringStream. The LA() function is the lookahead funtion
comment|//and is purely used for matching lexical rules. This also means that the grammar will only
comment|//accept capitalized tokens in case it is run from other tools like antlrworks which
comment|//do not have the ANTLRNoCaseStringStream implementation.
specifier|public
class|class
name|ANTLRNoCaseStringStream
extends|extends
name|ANTLRStringStream
block|{
specifier|public
name|ANTLRNoCaseStringStream
parameter_list|(
name|String
name|input
parameter_list|)
block|{
name|super
argument_list|(
name|input
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|LA
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|int
name|returnChar
init|=
name|super
operator|.
name|LA
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|returnChar
operator|==
name|CharStream
operator|.
name|EOF
condition|)
block|{
return|return
name|returnChar
return|;
block|}
elseif|else
if|if
condition|(
name|returnChar
operator|==
literal|0
condition|)
block|{
return|return
name|returnChar
return|;
block|}
return|return
name|Character
operator|.
name|toUpperCase
argument_list|(
operator|(
name|char
operator|)
name|returnChar
argument_list|)
return|;
block|}
block|}
comment|/**    * HiveLexerX.    *    */
specifier|public
class|class
name|HiveLexerX
extends|extends
name|HiveLexer
block|{
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|ParseError
argument_list|>
name|errors
decl_stmt|;
specifier|public
name|HiveLexerX
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|errors
operator|=
operator|new
name|ArrayList
argument_list|<
name|ParseError
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HiveLexerX
parameter_list|(
name|CharStream
name|input
parameter_list|)
block|{
name|super
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|errors
operator|=
operator|new
name|ArrayList
argument_list|<
name|ParseError
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|displayRecognitionError
parameter_list|(
name|String
index|[]
name|tokenNames
parameter_list|,
name|RecognitionException
name|e
parameter_list|)
block|{
name|errors
operator|.
name|add
argument_list|(
operator|new
name|ParseError
argument_list|(
name|this
argument_list|,
name|e
argument_list|,
name|tokenNames
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getErrorMessage
parameter_list|(
name|RecognitionException
name|e
parameter_list|,
name|String
index|[]
name|tokenNames
parameter_list|)
block|{
name|String
name|msg
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|NoViableAltException
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|NoViableAltException
name|nvae
init|=
operator|(
name|NoViableAltException
operator|)
name|e
decl_stmt|;
comment|// for development, can add
comment|// "decision=<<"+nvae.grammarDecisionDescription+">>"
comment|// and "(decision="+nvae.decisionNumber+") and
comment|// "state "+nvae.stateNumber
name|msg
operator|=
literal|"character "
operator|+
name|getCharErrorDisplay
argument_list|(
name|e
operator|.
name|c
argument_list|)
operator|+
literal|" not supported here"
expr_stmt|;
block|}
else|else
block|{
name|msg
operator|=
name|super
operator|.
name|getErrorMessage
argument_list|(
name|e
argument_list|,
name|tokenNames
argument_list|)
expr_stmt|;
block|}
return|return
name|msg
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|ParseError
argument_list|>
name|getErrors
parameter_list|()
block|{
return|return
name|errors
return|;
block|}
block|}
comment|/**    * Tree adaptor for making antlr return ASTNodes instead of CommonTree nodes    * so that the graph walking algorithms and the rules framework defined in    * ql.lib can be used with the AST Nodes.    */
specifier|static
specifier|final
name|TreeAdaptor
name|adaptor
init|=
operator|new
name|CommonTreeAdaptor
argument_list|()
block|{
comment|/**      * Creates an ASTNode for the given token. The ASTNode is a wrapper around      * antlr's CommonTree class that implements the Node interface.      *      * @param payload      *          The token.      * @return Object (which is actually an ASTNode) for the token.      */
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|(
name|Token
name|payload
parameter_list|)
block|{
return|return
operator|new
name|ASTNode
argument_list|(
name|payload
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|dupNode
parameter_list|(
name|Object
name|t
parameter_list|)
block|{
return|return
name|create
argument_list|(
operator|(
operator|(
name|CommonTree
operator|)
name|t
operator|)
operator|.
name|token
argument_list|)
return|;
block|}
empty_stmt|;
annotation|@
name|Override
specifier|public
name|Object
name|errorNode
parameter_list|(
name|TokenStream
name|input
parameter_list|,
name|Token
name|start
parameter_list|,
name|Token
name|stop
parameter_list|,
name|RecognitionException
name|e
parameter_list|)
block|{
return|return
operator|new
name|ASTErrorNode
argument_list|(
name|input
argument_list|,
name|start
argument_list|,
name|stop
argument_list|,
name|e
argument_list|)
return|;
block|}
empty_stmt|;
block|}
decl_stmt|;
specifier|public
name|ASTNode
name|parse
parameter_list|(
name|String
name|command
parameter_list|)
throws|throws
name|ParseException
block|{
return|return
name|parse
argument_list|(
name|command
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Parses a command, optionally assigning the parser's token stream to the    * given context.    *    * @param command    *          command to parse    *    * @param ctx    *          context with which to associate this parser's token stream, or    *          null if either no context is available or the context already has    *          an existing stream    *    * @return parsed AST    */
specifier|public
name|ASTNode
name|parse
parameter_list|(
name|String
name|command
parameter_list|,
name|Context
name|ctx
parameter_list|)
throws|throws
name|ParseException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Parsing command: "
operator|+
name|command
argument_list|)
expr_stmt|;
name|HiveLexerX
name|lexer
init|=
operator|new
name|HiveLexerX
argument_list|(
operator|new
name|ANTLRNoCaseStringStream
argument_list|(
name|command
argument_list|)
argument_list|)
decl_stmt|;
name|TokenRewriteStream
name|tokens
init|=
operator|new
name|TokenRewriteStream
argument_list|(
name|lexer
argument_list|)
decl_stmt|;
if|if
condition|(
name|ctx
operator|!=
literal|null
condition|)
block|{
name|ctx
operator|.
name|setTokenRewriteStream
argument_list|(
name|tokens
argument_list|)
expr_stmt|;
block|}
name|HiveParser
name|parser
init|=
operator|new
name|HiveParser
argument_list|(
name|tokens
argument_list|)
decl_stmt|;
name|parser
operator|.
name|setTreeAdaptor
argument_list|(
name|adaptor
argument_list|)
expr_stmt|;
name|HiveParser
operator|.
name|statement_return
name|r
init|=
literal|null
decl_stmt|;
try|try
block|{
name|r
operator|=
name|parser
operator|.
name|statement
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RecognitionException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|ParseException
argument_list|(
name|parser
operator|.
name|errors
argument_list|)
throw|;
block|}
if|if
condition|(
name|lexer
operator|.
name|getErrors
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|parser
operator|.
name|errors
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Parse Completed"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|lexer
operator|.
name|getErrors
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
name|lexer
operator|.
name|getErrors
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
name|parser
operator|.
name|errors
argument_list|)
throw|;
block|}
name|ASTNode
name|tree
init|=
operator|(
name|ASTNode
operator|)
name|r
operator|.
name|getTree
argument_list|()
decl_stmt|;
name|tree
operator|.
name|setUnknownTokenBoundaries
argument_list|()
expr_stmt|;
return|return
name|tree
return|;
block|}
comment|/*    * parse a String as a Select List. This allows table functions to be passed expression Strings    * that are translated in    * the context they define at invocation time. Currently used by NPath to allow users to specify    * what output they want.    * NPath allows expressions n 'tpath' a column that represents the matched set of rows. This    * column doesn't exist in    * the input schema and hence the Result Expression cannot be analyzed by the regular Hive    * translation process.    */
specifier|public
name|ASTNode
name|parseSelect
parameter_list|(
name|String
name|command
parameter_list|,
name|Context
name|ctx
parameter_list|)
throws|throws
name|ParseException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Parsing command: "
operator|+
name|command
argument_list|)
expr_stmt|;
name|HiveLexerX
name|lexer
init|=
operator|new
name|HiveLexerX
argument_list|(
operator|new
name|ANTLRNoCaseStringStream
argument_list|(
name|command
argument_list|)
argument_list|)
decl_stmt|;
name|TokenRewriteStream
name|tokens
init|=
operator|new
name|TokenRewriteStream
argument_list|(
name|lexer
argument_list|)
decl_stmt|;
if|if
condition|(
name|ctx
operator|!=
literal|null
condition|)
block|{
name|ctx
operator|.
name|setTokenRewriteStream
argument_list|(
name|tokens
argument_list|)
expr_stmt|;
block|}
name|HiveParser
name|parser
init|=
operator|new
name|HiveParser
argument_list|(
name|tokens
argument_list|)
decl_stmt|;
name|parser
operator|.
name|setTreeAdaptor
argument_list|(
name|adaptor
argument_list|)
expr_stmt|;
name|HiveParser_SelectClauseParser
operator|.
name|selectClause_return
name|r
init|=
literal|null
decl_stmt|;
try|try
block|{
name|r
operator|=
name|parser
operator|.
name|selectClause
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RecognitionException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|ParseException
argument_list|(
name|parser
operator|.
name|errors
argument_list|)
throw|;
block|}
if|if
condition|(
name|lexer
operator|.
name|getErrors
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|parser
operator|.
name|errors
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Parse Completed"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|lexer
operator|.
name|getErrors
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
name|lexer
operator|.
name|getErrors
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
name|parser
operator|.
name|errors
argument_list|)
throw|;
block|}
return|return
operator|(
name|ASTNode
operator|)
name|r
operator|.
name|getTree
argument_list|()
return|;
block|}
block|}
end_class

end_unit

