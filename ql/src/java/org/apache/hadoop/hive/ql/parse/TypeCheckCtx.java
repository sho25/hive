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
name|NodeProcessorCtx
import|;
end_import

begin_comment
comment|/**  * This class implements the context information that is used for typechecking  * phase in query compilation.  */
end_comment

begin_class
specifier|public
class|class
name|TypeCheckCtx
implements|implements
name|NodeProcessorCtx
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TypeCheckCtx
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * The row resolver of the previous operator. This field is used to generate    * expression descriptors from the expression ASTs.    */
specifier|private
name|RowResolver
name|inputRR
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|useCaching
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|foldExpr
decl_stmt|;
comment|/**    * Receives translations which will need to be applied during unparse.    */
specifier|private
name|UnparseTranslator
name|unparseTranslator
decl_stmt|;
comment|/**    * Potential typecheck error reason.    */
specifier|private
name|String
name|error
decl_stmt|;
comment|/**    * The node that generated the potential typecheck error    */
specifier|private
name|ASTNode
name|errorSrcNode
decl_stmt|;
comment|/**    * Whether to allow stateful UDF invocations.    */
specifier|private
name|boolean
name|allowStatefulFunctions
decl_stmt|;
specifier|private
name|boolean
name|allowDistinctFunctions
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|allowGBExprElimination
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|allowAllColRef
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|allowFunctionStar
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|allowWindowing
decl_stmt|;
comment|// "[]" : LSQUARE/INDEX Expression
specifier|private
specifier|final
name|boolean
name|allowIndexExpr
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|allowSubQueryExpr
decl_stmt|;
comment|/**    * Constructor.    *    * @param inputRR    *          The input row resolver of the previous operator.    */
specifier|public
name|TypeCheckCtx
parameter_list|(
name|RowResolver
name|inputRR
parameter_list|)
block|{
name|this
argument_list|(
name|inputRR
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TypeCheckCtx
parameter_list|(
name|RowResolver
name|inputRR
parameter_list|,
name|boolean
name|useCaching
parameter_list|,
name|boolean
name|foldExpr
parameter_list|)
block|{
name|this
argument_list|(
name|inputRR
argument_list|,
name|useCaching
argument_list|,
name|foldExpr
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TypeCheckCtx
parameter_list|(
name|RowResolver
name|inputRR
parameter_list|,
name|boolean
name|useCaching
parameter_list|,
name|boolean
name|foldExpr
parameter_list|,
name|boolean
name|allowStatefulFunctions
parameter_list|,
name|boolean
name|allowDistinctFunctions
parameter_list|,
name|boolean
name|allowGBExprElimination
parameter_list|,
name|boolean
name|allowAllColRef
parameter_list|,
name|boolean
name|allowFunctionStar
parameter_list|,
name|boolean
name|allowWindowing
parameter_list|,
name|boolean
name|allowIndexExpr
parameter_list|,
name|boolean
name|allowSubQueryExpr
parameter_list|)
block|{
name|setInputRR
argument_list|(
name|inputRR
argument_list|)
expr_stmt|;
name|error
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|useCaching
operator|=
name|useCaching
expr_stmt|;
name|this
operator|.
name|foldExpr
operator|=
name|foldExpr
expr_stmt|;
name|this
operator|.
name|allowStatefulFunctions
operator|=
name|allowStatefulFunctions
expr_stmt|;
name|this
operator|.
name|allowDistinctFunctions
operator|=
name|allowDistinctFunctions
expr_stmt|;
name|this
operator|.
name|allowGBExprElimination
operator|=
name|allowGBExprElimination
expr_stmt|;
name|this
operator|.
name|allowAllColRef
operator|=
name|allowAllColRef
expr_stmt|;
name|this
operator|.
name|allowFunctionStar
operator|=
name|allowFunctionStar
expr_stmt|;
name|this
operator|.
name|allowWindowing
operator|=
name|allowWindowing
expr_stmt|;
name|this
operator|.
name|allowIndexExpr
operator|=
name|allowIndexExpr
expr_stmt|;
name|this
operator|.
name|allowSubQueryExpr
operator|=
name|allowSubQueryExpr
expr_stmt|;
block|}
comment|/**    * @param inputRR    *          the inputRR to set    */
specifier|public
name|void
name|setInputRR
parameter_list|(
name|RowResolver
name|inputRR
parameter_list|)
block|{
name|this
operator|.
name|inputRR
operator|=
name|inputRR
expr_stmt|;
block|}
comment|/**    * @return the inputRR    */
specifier|public
name|RowResolver
name|getInputRR
parameter_list|()
block|{
return|return
name|inputRR
return|;
block|}
comment|/**    * @param unparseTranslator    *          the unparseTranslator to set    */
specifier|public
name|void
name|setUnparseTranslator
parameter_list|(
name|UnparseTranslator
name|unparseTranslator
parameter_list|)
block|{
name|this
operator|.
name|unparseTranslator
operator|=
name|unparseTranslator
expr_stmt|;
block|}
comment|/**    * @return the unparseTranslator    */
specifier|public
name|UnparseTranslator
name|getUnparseTranslator
parameter_list|()
block|{
return|return
name|unparseTranslator
return|;
block|}
comment|/**    * @param allowStatefulFunctions    *          whether to allow stateful UDF invocations    */
specifier|public
name|void
name|setAllowStatefulFunctions
parameter_list|(
name|boolean
name|allowStatefulFunctions
parameter_list|)
block|{
name|this
operator|.
name|allowStatefulFunctions
operator|=
name|allowStatefulFunctions
expr_stmt|;
block|}
comment|/**    * @return whether to allow stateful UDF invocations    */
specifier|public
name|boolean
name|getAllowStatefulFunctions
parameter_list|()
block|{
return|return
name|allowStatefulFunctions
return|;
block|}
comment|/**    * @param error    *          the error to set    *    */
specifier|public
name|void
name|setError
parameter_list|(
name|String
name|error
parameter_list|,
name|ASTNode
name|errorSrcNode
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
comment|// Logger the callstack from which the error has been set.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting error: ["
operator|+
name|error
operator|+
literal|"] from "
operator|+
operator|(
operator|(
name|errorSrcNode
operator|==
literal|null
operator|)
condition|?
literal|"null"
else|:
name|errorSrcNode
operator|.
name|toStringTree
argument_list|()
operator|)
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|error
operator|=
name|error
expr_stmt|;
name|this
operator|.
name|errorSrcNode
operator|=
name|errorSrcNode
expr_stmt|;
block|}
comment|/**    * @return the error    */
specifier|public
name|String
name|getError
parameter_list|()
block|{
return|return
name|error
return|;
block|}
specifier|public
name|ASTNode
name|getErrorSrcNode
parameter_list|()
block|{
return|return
name|errorSrcNode
return|;
block|}
specifier|public
name|void
name|setAllowDistinctFunctions
parameter_list|(
name|boolean
name|allowDistinctFunctions
parameter_list|)
block|{
name|this
operator|.
name|allowDistinctFunctions
operator|=
name|allowDistinctFunctions
expr_stmt|;
block|}
specifier|public
name|boolean
name|getAllowDistinctFunctions
parameter_list|()
block|{
return|return
name|allowDistinctFunctions
return|;
block|}
specifier|public
name|boolean
name|getAllowGBExprElimination
parameter_list|()
block|{
return|return
name|allowGBExprElimination
return|;
block|}
specifier|public
name|boolean
name|getallowAllColRef
parameter_list|()
block|{
return|return
name|allowAllColRef
return|;
block|}
specifier|public
name|boolean
name|getallowFunctionStar
parameter_list|()
block|{
return|return
name|allowFunctionStar
return|;
block|}
specifier|public
name|boolean
name|getallowWindowing
parameter_list|()
block|{
return|return
name|allowWindowing
return|;
block|}
specifier|public
name|boolean
name|getallowIndexExpr
parameter_list|()
block|{
return|return
name|allowIndexExpr
return|;
block|}
specifier|public
name|boolean
name|getallowSubQueryExpr
parameter_list|()
block|{
return|return
name|allowSubQueryExpr
return|;
block|}
specifier|public
name|boolean
name|isUseCaching
parameter_list|()
block|{
return|return
name|useCaching
return|;
block|}
specifier|public
name|boolean
name|isFoldExpr
parameter_list|()
block|{
return|return
name|foldExpr
return|;
block|}
block|}
end_class

end_unit

