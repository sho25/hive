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
name|optimizer
operator|.
name|index
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|ParseContext
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
name|ParseDriver
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
name|ParseException
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
name|ParseUtils
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
name|SemanticAnalyzer
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
name|SemanticAnalyzerFactory
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
comment|/**  * RewriteParseContextGenerator is a class that offers methods to generate operator tree  * for input queries. It is implemented on lines of the analyzeInternal(..) method  * of {@link SemanticAnalyzer} but it creates only the ParseContext for the input query command.  * It does not optimize or generate map-reduce tasks for the input query.  * This can be used when you need to create operator tree for an internal query.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|RewriteParseContextGenerator
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
name|RewriteParseContextGenerator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|RewriteParseContextGenerator
parameter_list|()
block|{   }
comment|/**    * Parse the input {@link String} command and generate a ASTNode tree.    * @param conf    * @param command    * @return the parse context    * @throws SemanticException    */
specifier|public
specifier|static
name|ParseContext
name|generateOperatorTree
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|command
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Context
name|ctx
decl_stmt|;
name|ParseContext
name|subPCtx
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ctx
operator|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|ASTNode
name|tree
init|=
name|pd
operator|.
name|parse
argument_list|(
name|command
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
name|tree
operator|=
name|ParseUtils
operator|.
name|findRootNonNullToken
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|BaseSemanticAnalyzer
name|sem
init|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|conf
argument_list|,
name|tree
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|sem
operator|instanceof
name|SemanticAnalyzer
operator|)
assert|;
name|doSemanticAnalysis
argument_list|(
operator|(
name|SemanticAnalyzer
operator|)
name|sem
argument_list|,
name|tree
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|subPCtx
operator|=
operator|(
operator|(
name|SemanticAnalyzer
operator|)
name|sem
operator|)
operator|.
name|getParseContext
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Sub-query Semantic Analysis Completed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"IOException in generating the operator "
operator|+
literal|"tree for input command - "
operator|+
name|command
operator|+
literal|" "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ParseException in generating the operator "
operator|+
literal|"tree for input command - "
operator|+
name|command
operator|+
literal|" "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"SemanticException in generating the operator "
operator|+
literal|"tree for input command - "
operator|+
name|command
operator|+
literal|" "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|subPCtx
return|;
block|}
comment|/**    * For the input ASTNode tree, perform a semantic analysis and check metadata    * Generate a operator tree and return the {@link ParseContext} instance for the operator tree.    *    * @param ctx    * @param sem    * @param ast    * @return    * @throws SemanticException    */
specifier|private
specifier|static
name|void
name|doSemanticAnalysis
parameter_list|(
name|SemanticAnalyzer
name|sem
parameter_list|,
name|ASTNode
name|ast
parameter_list|,
name|Context
name|ctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|QB
name|qb
init|=
operator|new
name|QB
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ASTNode
name|child
init|=
name|ast
decl_stmt|;
name|ParseContext
name|subPCtx
init|=
operator|(
operator|(
name|SemanticAnalyzer
operator|)
name|sem
operator|)
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
name|subPCtx
operator|.
name|setContext
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
operator|(
operator|(
name|SemanticAnalyzer
operator|)
name|sem
operator|)
operator|.
name|initParseCtx
argument_list|(
name|subPCtx
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting Sub-query Semantic Analysis"
argument_list|)
expr_stmt|;
name|sem
operator|.
name|doPhase1
argument_list|(
name|child
argument_list|,
name|qb
argument_list|,
name|sem
operator|.
name|initPhase1Ctx
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Completed phase 1 of Sub-query Semantic Analysis"
argument_list|)
expr_stmt|;
name|sem
operator|.
name|getMetaData
argument_list|(
name|qb
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Completed getting MetaData in Sub-query Semantic Analysis"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Sub-query Abstract syntax tree: "
operator|+
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|sem
operator|.
name|genPlan
argument_list|(
name|qb
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Sub-query Completed plan generation"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

