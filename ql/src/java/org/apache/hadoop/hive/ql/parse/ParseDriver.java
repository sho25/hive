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
name|HashMap
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
name|*
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
name|*
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

begin_class
specifier|public
class|class
name|ParseDriver
block|{
specifier|static
specifier|final
specifier|private
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
specifier|private
specifier|static
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|xlateMap
decl_stmt|;
static|static
block|{
name|xlateMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
comment|// Keywords
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_TRUE"
argument_list|,
literal|"TRUE"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_FALSE"
argument_list|,
literal|"FALSE"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_ALL"
argument_list|,
literal|"ALL"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_AND"
argument_list|,
literal|"AND"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_OR"
argument_list|,
literal|"OR"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_NOT"
argument_list|,
literal|"NOT"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_LIKE"
argument_list|,
literal|"LIKE"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_ASC"
argument_list|,
literal|"ASC"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_DESC"
argument_list|,
literal|"DESC"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_ORDER"
argument_list|,
literal|"ORDER"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_BY"
argument_list|,
literal|"BY"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_GROUP"
argument_list|,
literal|"GROUP"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_WHERE"
argument_list|,
literal|"WHERE"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_FROM"
argument_list|,
literal|"FROM"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_AS"
argument_list|,
literal|"AS"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_SELECT"
argument_list|,
literal|"SELECT"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_DISTINCT"
argument_list|,
literal|"DISTINCT"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_INSERT"
argument_list|,
literal|"INSERT"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_OVERWRITE"
argument_list|,
literal|"OVERWRITE"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_OUTER"
argument_list|,
literal|"OUTER"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_JOIN"
argument_list|,
literal|"JOIN"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_LEFT"
argument_list|,
literal|"LEFT"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_RIGHT"
argument_list|,
literal|"RIGHT"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_FULL"
argument_list|,
literal|"FULL"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_ON"
argument_list|,
literal|"ON"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_PARTITION"
argument_list|,
literal|"PARTITION"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_TABLE"
argument_list|,
literal|"TABLE"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_DIRECTORY"
argument_list|,
literal|"DIRECTORY"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_LOCAL"
argument_list|,
literal|"LOCAL"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_TRANSFORM"
argument_list|,
literal|"TRANSFORM"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_USING"
argument_list|,
literal|"USING"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_CLUSTER"
argument_list|,
literal|"CLUSTER"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"KW_UNION"
argument_list|,
literal|"UNION"
argument_list|)
expr_stmt|;
comment|// Operators
name|xlateMap
operator|.
name|put
argument_list|(
literal|"DOT"
argument_list|,
literal|"."
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"COLON"
argument_list|,
literal|":"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"COMMA"
argument_list|,
literal|","
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"SEMICOLON"
argument_list|,
literal|");"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"LPAREN"
argument_list|,
literal|"("
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"RPAREN"
argument_list|,
literal|")"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"LSQUARE"
argument_list|,
literal|"["
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"RSQUARE"
argument_list|,
literal|"]"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"EQUAL"
argument_list|,
literal|"="
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"NOTEQUAL"
argument_list|,
literal|"<>"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"LESSTHANOREQUALTO"
argument_list|,
literal|"<="
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"LESSTHAN"
argument_list|,
literal|"<"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"GREATERTHANOREQUALTO"
argument_list|,
literal|">="
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"GREATERTHAN"
argument_list|,
literal|">"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"DIVIDE"
argument_list|,
literal|"/"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"PLUS"
argument_list|,
literal|"+"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"MINUS"
argument_list|,
literal|"-"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"STAR"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"MOD"
argument_list|,
literal|"%"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"AMPERSAND"
argument_list|,
literal|"&"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"TILDE"
argument_list|,
literal|"~"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"BITWISEOR"
argument_list|,
literal|"|"
argument_list|)
expr_stmt|;
name|xlateMap
operator|.
name|put
argument_list|(
literal|"BITWISEXOR"
argument_list|,
literal|"^"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|xlate
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|String
name|ret
init|=
name|xlateMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|==
literal|null
condition|)
block|{
name|ret
operator|=
name|name
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
comment|// This class provides and implementation for a case insensitive token checker for
comment|// the lexical analysis part of antlr. By converting the token stream into upper case
comment|// at the time when lexical rules are checked, this class ensures that the lexical rules
comment|// need to just match the token with upper case letters as opposed to combination of upper
comment|// case and lower case characteres. This is purely used for matching lexical rules. The
comment|// actual token text is stored in the same way as the user input without actually converting
comment|// it into an upper case. The token values are generated by the consume() function of the
comment|// super class ANTLRStringStream. The LA() function is the lookahead funtion and is purely
comment|// used for matching lexical rules. This also means that the grammar will only accept
comment|// capitalized tokens in case it is run from other tools like antlrworks which do not
comment|// have the ANTLRNoCaseStringStream implementation.
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
specifier|public
class|class
name|HiveLexerX
extends|extends
name|HiveLexer
block|{
specifier|private
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
comment|// for development, can add "decision=<<"+nvae.grammarDecisionDescription+">>"
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
specifier|public
class|class
name|HiveParserX
extends|extends
name|HiveParser
block|{
specifier|private
name|ArrayList
argument_list|<
name|ParseError
argument_list|>
name|errors
decl_stmt|;
specifier|public
name|HiveParserX
parameter_list|(
name|TokenStream
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
specifier|protected
name|void
name|mismatch
parameter_list|(
name|IntStream
name|input
parameter_list|,
name|int
name|ttype
parameter_list|,
name|BitSet
name|follow
parameter_list|)
throws|throws
name|RecognitionException
block|{
throw|throw
operator|new
name|MismatchedTokenException
argument_list|(
name|ttype
argument_list|,
name|input
argument_list|)
throw|;
block|}
specifier|public
name|void
name|recoverFromMismatchedSet
parameter_list|(
name|IntStream
name|input
parameter_list|,
name|RecognitionException
name|re
parameter_list|,
name|BitSet
name|follow
parameter_list|)
throws|throws
name|RecognitionException
block|{
throw|throw
name|re
throw|;
block|}
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
comment|// Transalate the token names to something that the user can understand
name|String
index|[]
name|xlateNames
init|=
operator|new
name|String
index|[
name|tokenNames
operator|.
name|length
index|]
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
name|tokenNames
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|xlateNames
index|[
name|i
index|]
operator|=
name|ParseDriver
operator|.
name|xlate
argument_list|(
name|tokenNames
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
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
comment|// for development, can add "decision=<<"+nvae.grammarDecisionDescription+">>"
comment|// and "(decision="+nvae.decisionNumber+") and
comment|// "state "+nvae.stateNumber
name|msg
operator|=
literal|"cannot recognize input "
operator|+
name|getTokenErrorDisplay
argument_list|(
name|e
operator|.
name|token
argument_list|)
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
name|xlateNames
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
comment|/**      * Creates an ASTNode for the given token. The ASTNode is a wrapper around antlr's      * CommonTree class that implements the Node interface.      *       * @param payload The token.      * @return Object (which is actually an ASTNode) for the token.      */
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
name|TokenStream
name|tokens
init|=
operator|new
name|TokenRewriteStream
argument_list|(
name|lexer
argument_list|)
decl_stmt|;
name|HiveParserX
name|parser
init|=
operator|new
name|HiveParserX
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
throw|throw
operator|new
name|ParseException
argument_list|(
name|parser
operator|.
name|getErrors
argument_list|()
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
name|getErrors
argument_list|()
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
name|getErrors
argument_list|()
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

