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
name|optimizer
operator|.
name|listbucketingpruner
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|Node
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
name|NodeProcessor
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
name|Partition
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
name|optimizer
operator|.
name|PrunerExpressionOperatorFactory
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
name|optimizer
operator|.
name|PrunerUtils
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
name|ExprNodeDesc
import|;
end_import

begin_comment
comment|/**  * Expression processor factory for list bucketing pruning. Each processor tries to  * convert the expression subtree into a list bucketing pruning expression. This  * expression is then used to figure out which skewed value to be used  */
end_comment

begin_class
specifier|public
class|class
name|LBExprProcFactory
extends|extends
name|PrunerExpressionOperatorFactory
block|{
specifier|private
name|LBExprProcFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
comment|/**    * Processor for lbpr column expressions.    */
specifier|public
specifier|static
class|class
name|LBPRColumnExprProcessor
extends|extends
name|ColumnExprProcessor
block|{
annotation|@
name|Override
specifier|protected
name|ExprNodeDesc
name|processColumnDesc
parameter_list|(
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|ExprNodeColumnDesc
name|cd
parameter_list|)
block|{
name|ExprNodeDesc
name|newcd
decl_stmt|;
name|LBExprProcCtx
name|ctx
init|=
operator|(
name|LBExprProcCtx
operator|)
name|procCtx
decl_stmt|;
name|Partition
name|part
init|=
name|ctx
operator|.
name|getPart
argument_list|()
decl_stmt|;
if|if
condition|(
name|cd
operator|.
name|getTabAlias
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|ctx
operator|.
name|getTabAlias
argument_list|()
argument_list|)
operator|&&
name|isPruneForListBucketing
argument_list|(
name|part
argument_list|,
name|cd
operator|.
name|getColumn
argument_list|()
argument_list|)
condition|)
block|{
name|newcd
operator|=
name|cd
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|newcd
operator|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|cd
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|newcd
return|;
block|}
comment|/**      * Check if we prune it for list bucketing      * 1. column name is part of skewed column      * 2. partition has skewed value to location map      * @param part      * @param columnName      * @return      */
specifier|private
name|boolean
name|isPruneForListBucketing
parameter_list|(
name|Partition
name|part
parameter_list|,
name|String
name|columnName
parameter_list|)
block|{
return|return
name|ListBucketingPrunerUtils
operator|.
name|isListBucketingPart
argument_list|(
name|part
argument_list|)
operator|&&
operator|(
name|part
operator|.
name|getSkewedColNames
argument_list|()
operator|.
name|contains
argument_list|(
name|columnName
argument_list|)
operator|)
return|;
block|}
block|}
comment|/**    * Generates the list bucketing pruner for the expression tree.    *    * @param tabAlias    *          The table alias of the partition table that is being considered    *          for pruning    * @param pred    *          The predicate from which the list bucketing pruner needs to be    *          generated    * @param part    *          The partition this walker is walking    * @throws SemanticException    */
specifier|public
specifier|static
name|ExprNodeDesc
name|genPruner
parameter_list|(
name|String
name|tabAlias
parameter_list|,
name|ExprNodeDesc
name|pred
parameter_list|,
name|Partition
name|part
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Create the walker, the rules dispatcher and the context.
name|NodeProcessorCtx
name|lbprCtx
init|=
operator|new
name|LBExprProcCtx
argument_list|(
name|tabAlias
argument_list|,
name|part
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|outputMap
init|=
name|PrunerUtils
operator|.
name|walkExprTree
argument_list|(
name|pred
argument_list|,
name|lbprCtx
argument_list|,
name|getColumnProcessor
argument_list|()
argument_list|,
name|getFieldProcessor
argument_list|()
argument_list|,
name|getGenericFuncProcessor
argument_list|()
argument_list|,
name|getDefaultExprProcessor
argument_list|()
argument_list|)
decl_stmt|;
comment|// Get the exprNodeDesc corresponding to the first start node;
return|return
operator|(
name|ExprNodeDesc
operator|)
name|outputMap
operator|.
name|get
argument_list|(
name|pred
argument_list|)
return|;
block|}
comment|/**    * Instantiate column processor.    *    * @return    */
specifier|public
specifier|static
name|NodeProcessor
name|getColumnProcessor
parameter_list|()
block|{
return|return
operator|new
name|LBPRColumnExprProcessor
argument_list|()
return|;
block|}
block|}
end_class

end_unit

