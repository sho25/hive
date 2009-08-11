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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|exec
operator|.
name|JoinOperator
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
name|MapJoinOperator
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
name|exec
operator|.
name|TableScanOperator
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
name|exprNodeDesc
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
name|loadFileDesc
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
name|loadTableDesc
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
name|metadata
operator|.
name|Table
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
name|unionproc
operator|.
name|UnionProcContext
import|;
end_import

begin_comment
comment|/**  * Parse Context: The current parse context. This is passed to the optimizer  * which then transforms the operator tree using the parse context. All the  * optimizations are performed sequentially and then the new parse context  * populated. Note that since the parse context contains the operator tree, it  * can be easily retrieved by the next optimization step or finally for task  * generation after the plan has been completely optimized.  *   **/
end_comment

begin_class
specifier|public
class|class
name|ParseContext
block|{
specifier|private
name|QB
name|qb
decl_stmt|;
specifier|private
name|ASTNode
name|ast
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTPartitionPruner
argument_list|>
name|aliasToPruner
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|exprNodeDesc
argument_list|>
name|opToPartPruner
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|SamplePruner
argument_list|>
name|aliasToSamplePruner
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|topOps
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|topSelOps
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|opParseCtx
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|JoinOperator
argument_list|,
name|QBJoinTree
argument_list|>
name|joinContext
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|Table
argument_list|>
name|topToTable
decl_stmt|;
specifier|private
name|List
argument_list|<
name|loadTableDesc
argument_list|>
name|loadTableWork
decl_stmt|;
specifier|private
name|List
argument_list|<
name|loadFileDesc
argument_list|>
name|loadFileWork
decl_stmt|;
specifier|private
name|Context
name|ctx
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|idToTableNameMap
decl_stmt|;
specifier|private
name|int
name|destTableId
decl_stmt|;
specifier|private
name|UnionProcContext
name|uCtx
decl_stmt|;
specifier|private
name|List
argument_list|<
name|MapJoinOperator
argument_list|>
name|listMapJoinOpsNoReducer
decl_stmt|;
comment|// list of map join operators with no reducer
comment|// is set to true if the expression only contains partitioning columns and not any other column reference.
comment|// This is used to optimize select * from table where ... scenario, when the where condition only references
comment|// partitioning columns - the partitions are identified and streamed directly to the client without requiring
comment|// a map-reduce job
specifier|private
name|boolean
name|hasNonPartCols
decl_stmt|;
specifier|public
name|ParseContext
parameter_list|()
block|{     }
comment|/**    * @param qb    *          current QB    * @param ast    *          current parse tree    * @param aliasToPruner    *          partition pruner list    * @param opToPartPruner    *          map from table scan operator to partition pruner    * @param aliasToSamplePruner    *          sample pruner list    * @param loadFileWork    *          list of destination files being loaded    * @param loadTableWork    *          list of destination tables being loaded    * @param opParseCtx    *          operator parse context - contains a mapping from operator to    *          operator parse state (row resolver etc.)    * @param topOps    *          list of operators for the top query    * @param topSelOps    *          list of operators for the selects introduced for column pruning    * @param listMapJoinOpsNoReducer    *          list of map join operators with no reducer    * @param hasNonPartCols    *          the query has non partition columns    */
specifier|public
name|ParseContext
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|QB
name|qb
parameter_list|,
name|ASTNode
name|ast
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTPartitionPruner
argument_list|>
name|aliasToPruner
parameter_list|,
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|exprNodeDesc
argument_list|>
name|opToPartPruner
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|SamplePruner
argument_list|>
name|aliasToSamplePruner
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|topOps
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|topSelOps
parameter_list|,
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|opParseCtx
parameter_list|,
name|Map
argument_list|<
name|JoinOperator
argument_list|,
name|QBJoinTree
argument_list|>
name|joinContext
parameter_list|,
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|Table
argument_list|>
name|topToTable
parameter_list|,
name|List
argument_list|<
name|loadTableDesc
argument_list|>
name|loadTableWork
parameter_list|,
name|List
argument_list|<
name|loadFileDesc
argument_list|>
name|loadFileWork
parameter_list|,
name|Context
name|ctx
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|idToTableNameMap
parameter_list|,
name|int
name|destTableId
parameter_list|,
name|UnionProcContext
name|uCtx
parameter_list|,
name|List
argument_list|<
name|MapJoinOperator
argument_list|>
name|listMapJoinOpsNoReducer
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|qb
operator|=
name|qb
expr_stmt|;
name|this
operator|.
name|ast
operator|=
name|ast
expr_stmt|;
name|this
operator|.
name|aliasToPruner
operator|=
name|aliasToPruner
expr_stmt|;
name|this
operator|.
name|opToPartPruner
operator|=
name|opToPartPruner
expr_stmt|;
name|this
operator|.
name|aliasToSamplePruner
operator|=
name|aliasToSamplePruner
expr_stmt|;
name|this
operator|.
name|joinContext
operator|=
name|joinContext
expr_stmt|;
name|this
operator|.
name|topToTable
operator|=
name|topToTable
expr_stmt|;
name|this
operator|.
name|loadFileWork
operator|=
name|loadFileWork
expr_stmt|;
name|this
operator|.
name|loadTableWork
operator|=
name|loadTableWork
expr_stmt|;
name|this
operator|.
name|opParseCtx
operator|=
name|opParseCtx
expr_stmt|;
name|this
operator|.
name|topOps
operator|=
name|topOps
expr_stmt|;
name|this
operator|.
name|topSelOps
operator|=
name|topSelOps
expr_stmt|;
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
name|this
operator|.
name|idToTableNameMap
operator|=
name|idToTableNameMap
expr_stmt|;
name|this
operator|.
name|destTableId
operator|=
name|destTableId
expr_stmt|;
name|this
operator|.
name|uCtx
operator|=
name|uCtx
expr_stmt|;
name|this
operator|.
name|listMapJoinOpsNoReducer
operator|=
name|listMapJoinOpsNoReducer
expr_stmt|;
name|this
operator|.
name|hasNonPartCols
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * @return the qb    */
specifier|public
name|QB
name|getQB
parameter_list|()
block|{
return|return
name|qb
return|;
block|}
comment|/**    * @param qb    *          the qb to set    */
specifier|public
name|void
name|setQB
parameter_list|(
name|QB
name|qb
parameter_list|)
block|{
name|this
operator|.
name|qb
operator|=
name|qb
expr_stmt|;
block|}
comment|/**    * @return the context    */
specifier|public
name|Context
name|getContext
parameter_list|()
block|{
return|return
name|ctx
return|;
block|}
comment|/**    * @param ctx    *          the context to set    */
specifier|public
name|void
name|setContext
parameter_list|(
name|Context
name|ctx
parameter_list|)
block|{
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
block|}
comment|/**    * @return the hive conf    */
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**    * @param conf    *          the conf to set    */
specifier|public
name|void
name|setConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * @return the ast    */
specifier|public
name|ASTNode
name|getParseTree
parameter_list|()
block|{
return|return
name|ast
return|;
block|}
comment|/**    * @param ast    *          the parsetree to set    */
specifier|public
name|void
name|setParseTree
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|ast
operator|=
name|ast
expr_stmt|;
block|}
comment|/**    * @return the aliasToPruner    */
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTPartitionPruner
argument_list|>
name|getAliasToPruner
parameter_list|()
block|{
return|return
name|aliasToPruner
return|;
block|}
comment|/**    * @param aliasToPruner    *          the aliasToPruner to set    */
specifier|public
name|void
name|setAliasToPruner
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTPartitionPruner
argument_list|>
name|aliasToPruner
parameter_list|)
block|{
name|this
operator|.
name|aliasToPruner
operator|=
name|aliasToPruner
expr_stmt|;
block|}
comment|/**    * @return the opToPartPruner    */
specifier|public
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|exprNodeDesc
argument_list|>
name|getOpToPartPruner
parameter_list|()
block|{
return|return
name|opToPartPruner
return|;
block|}
comment|/**    * @param opToPartPruner    *          the opToPartPruner to set    */
specifier|public
name|void
name|setOpToPartPruner
parameter_list|(
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|exprNodeDesc
argument_list|>
name|opToPartPruner
parameter_list|)
block|{
name|this
operator|.
name|opToPartPruner
operator|=
name|opToPartPruner
expr_stmt|;
block|}
comment|/**    * @return the topToTable    */
specifier|public
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|Table
argument_list|>
name|getTopToTable
parameter_list|()
block|{
return|return
name|topToTable
return|;
block|}
comment|/**    * @param topToTable    *          the topToTable to set    */
specifier|public
name|void
name|setTopToTable
parameter_list|(
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|Table
argument_list|>
name|topToTable
parameter_list|)
block|{
name|this
operator|.
name|topToTable
operator|=
name|topToTable
expr_stmt|;
block|}
comment|/**    * @return the aliasToSamplePruner    */
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|SamplePruner
argument_list|>
name|getAliasToSamplePruner
parameter_list|()
block|{
return|return
name|aliasToSamplePruner
return|;
block|}
comment|/**    * @param aliasToSamplePruner    *          the aliasToSamplePruner to set    */
specifier|public
name|void
name|setAliasToSamplePruner
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|SamplePruner
argument_list|>
name|aliasToSamplePruner
parameter_list|)
block|{
name|this
operator|.
name|aliasToSamplePruner
operator|=
name|aliasToSamplePruner
expr_stmt|;
block|}
comment|/**    * @return the topOps    */
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getTopOps
parameter_list|()
block|{
return|return
name|topOps
return|;
block|}
comment|/**    * @param topOps    *          the topOps to set    */
specifier|public
name|void
name|setTopOps
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|topOps
parameter_list|)
block|{
name|this
operator|.
name|topOps
operator|=
name|topOps
expr_stmt|;
block|}
comment|/**    * @return the topSelOps    */
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getTopSelOps
parameter_list|()
block|{
return|return
name|topSelOps
return|;
block|}
comment|/**    * @param topSelOps    *          the topSelOps to set    */
specifier|public
name|void
name|setTopSelOps
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|topSelOps
parameter_list|)
block|{
name|this
operator|.
name|topSelOps
operator|=
name|topSelOps
expr_stmt|;
block|}
comment|/**    * @return the opParseCtx    */
specifier|public
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|getOpParseCtx
parameter_list|()
block|{
return|return
name|opParseCtx
return|;
block|}
comment|/**    * @param opParseCtx    *          the opParseCtx to set    */
specifier|public
name|void
name|setOpParseCtx
parameter_list|(
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|opParseCtx
parameter_list|)
block|{
name|this
operator|.
name|opParseCtx
operator|=
name|opParseCtx
expr_stmt|;
block|}
comment|/**    * @return the loadTableWork    */
specifier|public
name|List
argument_list|<
name|loadTableDesc
argument_list|>
name|getLoadTableWork
parameter_list|()
block|{
return|return
name|loadTableWork
return|;
block|}
comment|/**    * @param loadTableWork    *          the loadTableWork to set    */
specifier|public
name|void
name|setLoadTableWork
parameter_list|(
name|List
argument_list|<
name|loadTableDesc
argument_list|>
name|loadTableWork
parameter_list|)
block|{
name|this
operator|.
name|loadTableWork
operator|=
name|loadTableWork
expr_stmt|;
block|}
comment|/**    * @return the loadFileWork    */
specifier|public
name|List
argument_list|<
name|loadFileDesc
argument_list|>
name|getLoadFileWork
parameter_list|()
block|{
return|return
name|loadFileWork
return|;
block|}
comment|/**    * @param loadFileWork    *          the loadFileWork to set    */
specifier|public
name|void
name|setLoadFileWork
parameter_list|(
name|List
argument_list|<
name|loadFileDesc
argument_list|>
name|loadFileWork
parameter_list|)
block|{
name|this
operator|.
name|loadFileWork
operator|=
name|loadFileWork
expr_stmt|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getIdToTableNameMap
parameter_list|()
block|{
return|return
name|idToTableNameMap
return|;
block|}
specifier|public
name|void
name|setIdToTableNameMap
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|idToTableNameMap
parameter_list|)
block|{
name|this
operator|.
name|idToTableNameMap
operator|=
name|idToTableNameMap
expr_stmt|;
block|}
specifier|public
name|int
name|getDestTableId
parameter_list|()
block|{
return|return
name|destTableId
return|;
block|}
specifier|public
name|void
name|setDestTableId
parameter_list|(
name|int
name|destTableId
parameter_list|)
block|{
name|this
operator|.
name|destTableId
operator|=
name|destTableId
expr_stmt|;
block|}
specifier|public
name|UnionProcContext
name|getUCtx
parameter_list|()
block|{
return|return
name|uCtx
return|;
block|}
specifier|public
name|void
name|setUCtx
parameter_list|(
name|UnionProcContext
name|uCtx
parameter_list|)
block|{
name|this
operator|.
name|uCtx
operator|=
name|uCtx
expr_stmt|;
block|}
comment|/**    * @return the joinContext    */
specifier|public
name|Map
argument_list|<
name|JoinOperator
argument_list|,
name|QBJoinTree
argument_list|>
name|getJoinContext
parameter_list|()
block|{
return|return
name|joinContext
return|;
block|}
comment|/**    * @param joinContext the joinContext to set    */
specifier|public
name|void
name|setJoinContext
parameter_list|(
name|Map
argument_list|<
name|JoinOperator
argument_list|,
name|QBJoinTree
argument_list|>
name|joinContext
parameter_list|)
block|{
name|this
operator|.
name|joinContext
operator|=
name|joinContext
expr_stmt|;
block|}
comment|/**    * @return the listMapJoinOpsNoReducer    */
specifier|public
name|List
argument_list|<
name|MapJoinOperator
argument_list|>
name|getListMapJoinOpsNoReducer
parameter_list|()
block|{
return|return
name|listMapJoinOpsNoReducer
return|;
block|}
comment|/**    * @param listMapJoinOpsNoReducer the listMapJoinOpsNoReducer to set    */
specifier|public
name|void
name|setListMapJoinOpsNoReducer
parameter_list|(
name|List
argument_list|<
name|MapJoinOperator
argument_list|>
name|listMapJoinOpsNoReducer
parameter_list|)
block|{
name|this
operator|.
name|listMapJoinOpsNoReducer
operator|=
name|listMapJoinOpsNoReducer
expr_stmt|;
block|}
comment|/**    * Sets the hasNonPartCols flag    * @param val    */
specifier|public
name|void
name|setHasNonPartCols
parameter_list|(
name|boolean
name|val
parameter_list|)
block|{
name|this
operator|.
name|hasNonPartCols
operator|=
name|val
expr_stmt|;
block|}
comment|/**    * Gets the value of the hasNonPartCols flag    */
specifier|public
name|boolean
name|getHasNonPartCols
parameter_list|()
block|{
return|return
name|this
operator|.
name|hasNonPartCols
return|;
block|}
block|}
end_class

end_unit

