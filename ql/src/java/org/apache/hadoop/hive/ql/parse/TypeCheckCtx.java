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
comment|/**    * The row resolver of the previous operator. This field is used to generate    * expression descriptors from the expression ASTs.    */
specifier|private
name|RowResolver
name|inputRR
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
comment|/**    * Constructor.    *    * @param inputRR    *          The input row resolver of the previous operator.    */
specifier|public
name|TypeCheckCtx
parameter_list|(
name|RowResolver
name|inputRR
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
name|allowStatefulFunctions
operator|=
literal|false
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
comment|/**    * @param allowStatefulFunctions whether to allow stateful UDF invocations    */
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
block|}
end_class

end_unit

