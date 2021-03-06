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
name|index
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
name|plan
operator|.
name|ExprNodeColumnDesc
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
name|ExprNodeConstantDesc
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
name|ExprNodeGenericFuncDesc
import|;
end_import

begin_comment
comment|/**  * IndexSearchCondition represents an individual search condition  * found by {@link IndexPredicateAnalyzer}.  *  * @deprecated kept only because some storagehandlers are using it internally  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
class|class
name|IndexSearchCondition
block|{
specifier|private
name|ExprNodeColumnDesc
name|columnDesc
decl_stmt|;
specifier|private
name|String
name|comparisonOp
decl_stmt|;
specifier|private
name|ExprNodeConstantDesc
name|constantDesc
decl_stmt|;
specifier|private
name|ExprNodeGenericFuncDesc
name|indexExpr
decl_stmt|;
specifier|private
name|ExprNodeGenericFuncDesc
name|originalExpr
decl_stmt|;
specifier|private
name|String
index|[]
name|fields
decl_stmt|;
specifier|public
name|IndexSearchCondition
parameter_list|(
name|ExprNodeColumnDesc
name|columnDesc
parameter_list|,
name|String
name|comparisonOp
parameter_list|,
name|ExprNodeConstantDesc
name|constantDesc
parameter_list|,
name|ExprNodeGenericFuncDesc
name|comparisonExpr
parameter_list|)
block|{
name|this
argument_list|(
name|columnDesc
argument_list|,
name|comparisonOp
argument_list|,
name|constantDesc
argument_list|,
name|comparisonExpr
argument_list|,
name|comparisonExpr
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs a search condition, which takes the form    *<pre>column-ref comparison-op constant-value</pre>.    *    * @param columnDesc column being compared    *    * @param comparisonOp comparison operator, e.g. "="    * (taken from GenericUDFBridge.getUdfName())    *    * @param constantDesc constant value to search for    *    * @param indexExpr the comparison expression for the index    *    * @param originalExpr the original comparison expression    */
specifier|public
name|IndexSearchCondition
parameter_list|(
name|ExprNodeColumnDesc
name|columnDesc
parameter_list|,
name|String
name|comparisonOp
parameter_list|,
name|ExprNodeConstantDesc
name|constantDesc
parameter_list|,
name|ExprNodeGenericFuncDesc
name|indexExpr
parameter_list|,
name|ExprNodeGenericFuncDesc
name|originalExpr
parameter_list|,
name|String
index|[]
name|fields
parameter_list|)
block|{
name|this
operator|.
name|columnDesc
operator|=
name|columnDesc
expr_stmt|;
name|this
operator|.
name|comparisonOp
operator|=
name|comparisonOp
expr_stmt|;
name|this
operator|.
name|constantDesc
operator|=
name|constantDesc
expr_stmt|;
name|this
operator|.
name|indexExpr
operator|=
name|indexExpr
expr_stmt|;
name|this
operator|.
name|originalExpr
operator|=
name|originalExpr
expr_stmt|;
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
block|}
specifier|public
name|void
name|setColumnDesc
parameter_list|(
name|ExprNodeColumnDesc
name|columnDesc
parameter_list|)
block|{
name|this
operator|.
name|columnDesc
operator|=
name|columnDesc
expr_stmt|;
block|}
specifier|public
name|ExprNodeColumnDesc
name|getColumnDesc
parameter_list|()
block|{
return|return
name|columnDesc
return|;
block|}
specifier|public
name|void
name|setComparisonOp
parameter_list|(
name|String
name|comparisonOp
parameter_list|)
block|{
name|this
operator|.
name|comparisonOp
operator|=
name|comparisonOp
expr_stmt|;
block|}
specifier|public
name|String
name|getComparisonOp
parameter_list|()
block|{
return|return
name|comparisonOp
return|;
block|}
specifier|public
name|void
name|setConstantDesc
parameter_list|(
name|ExprNodeConstantDesc
name|constantDesc
parameter_list|)
block|{
name|this
operator|.
name|constantDesc
operator|=
name|constantDesc
expr_stmt|;
block|}
specifier|public
name|ExprNodeConstantDesc
name|getConstantDesc
parameter_list|()
block|{
return|return
name|constantDesc
return|;
block|}
specifier|public
name|void
name|setIndexExpr
parameter_list|(
name|ExprNodeGenericFuncDesc
name|indexExpr
parameter_list|)
block|{
name|this
operator|.
name|indexExpr
operator|=
name|indexExpr
expr_stmt|;
block|}
specifier|public
name|ExprNodeGenericFuncDesc
name|getIndexExpr
parameter_list|()
block|{
return|return
name|indexExpr
return|;
block|}
specifier|public
name|void
name|setOriginalExpr
parameter_list|(
name|ExprNodeGenericFuncDesc
name|originalExpr
parameter_list|)
block|{
name|this
operator|.
name|originalExpr
operator|=
name|originalExpr
expr_stmt|;
block|}
specifier|public
name|ExprNodeGenericFuncDesc
name|getOriginalExpr
parameter_list|()
block|{
return|return
name|originalExpr
return|;
block|}
specifier|public
name|String
index|[]
name|getFields
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|indexExpr
operator|.
name|getExprString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

