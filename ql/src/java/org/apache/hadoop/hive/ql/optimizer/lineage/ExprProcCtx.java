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
name|lineage
package|;
end_package

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
operator|.
name|Operator
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
comment|/**  * The processor context for the lineage information. This contains the  * lineage context and the column info and operator information that is  * being used for the current expression.  */
end_comment

begin_class
specifier|public
class|class
name|ExprProcCtx
implements|implements
name|NodeProcessorCtx
block|{
comment|/**    * The lineage context that is being populated.    */
specifier|private
name|LineageCtx
name|lctx
decl_stmt|;
comment|/**    * The input operator in case the current operator is not a leaf.    */
specifier|private
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|inpOp
decl_stmt|;
comment|/**    * Constructor.    *     * @param lctx The lineage context thatcontains the dependencies for the inputs.    * @param inpOp The input operator to the current operator.    */
specifier|public
name|ExprProcCtx
parameter_list|(
name|LineageCtx
name|lctx
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|inpOp
parameter_list|)
block|{
name|this
operator|.
name|lctx
operator|=
name|lctx
expr_stmt|;
name|this
operator|.
name|inpOp
operator|=
name|inpOp
expr_stmt|;
block|}
comment|/**    * Gets the lineage context.    *     * @return LineageCtx The lineage context.    */
specifier|public
name|LineageCtx
name|getLineageCtx
parameter_list|()
block|{
return|return
name|lctx
return|;
block|}
comment|/**    * Gets the input operator.    *     * @return Operator The input operator - this is null in case the current     * operator is a leaf.    */
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getInputOperator
parameter_list|()
block|{
return|return
name|inpOp
return|;
block|}
block|}
end_class

end_unit

