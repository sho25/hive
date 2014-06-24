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
name|optimizer
operator|.
name|lineage
operator|.
name|Generator
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
name|pcr
operator|.
name|PartitionConditionRemover
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
name|ppr
operator|.
name|PartitionPruner
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
name|stats
operator|.
name|annotation
operator|.
name|AnnotateWithStatistics
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
name|ppd
operator|.
name|PredicatePushDown
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
name|ppd
operator|.
name|PredicateTransitivePropagate
import|;
end_import

begin_comment
comment|/*  * do PredicatePushdown, PartitionPruning and Column Pruning before CBO   */
end_comment

begin_class
specifier|public
class|class
name|PreCBOOptimizer
block|{
specifier|private
name|ParseContext
name|pctx
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Transform
argument_list|>
name|transformations
decl_stmt|;
comment|/**    * Create the list of transformations.    *     * @param hiveConf    */
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|transformations
operator|=
operator|new
name|ArrayList
argument_list|<
name|Transform
argument_list|>
argument_list|()
expr_stmt|;
comment|// Add the transformation that computes the lineage information.
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|Generator
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|PredicateTransitivePropagate
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|PredicatePushDown
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|PartitionPruner
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|PartitionConditionRemover
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|ColumnPruner
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|AnnotateWithStatistics
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Invoke all the transformations one-by-one, and alter the query plan.    *     * @return ParseContext    * @throws SemanticException    */
specifier|public
name|ParseContext
name|optimize
parameter_list|()
throws|throws
name|SemanticException
block|{
for|for
control|(
name|Transform
name|t
range|:
name|transformations
control|)
block|{
name|pctx
operator|=
name|t
operator|.
name|transform
argument_list|(
name|pctx
argument_list|)
expr_stmt|;
block|}
return|return
name|pctx
return|;
block|}
comment|/**    * @return the pctx    */
specifier|public
name|ParseContext
name|getPctx
parameter_list|()
block|{
return|return
name|pctx
return|;
block|}
comment|/**    * @param pctx    *          the pctx to set    */
specifier|public
name|void
name|setPctx
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
block|{
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
block|}
block|}
end_class

end_unit

