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
name|Collections
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
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|QueryProperties
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
name|QueryState
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
name|AbstractMapJoinOperator
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
name|FetchTask
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
name|ListSinkOperator
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
name|ReduceSinkOperator
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
name|SMBMapJoinOperator
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
name|SelectOperator
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
name|exec
operator|.
name|Task
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
name|hooks
operator|.
name|LineageInfo
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
name|hooks
operator|.
name|ReadEntity
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
name|unionproc
operator|.
name|UnionProcContext
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
operator|.
name|AnalyzeRewriteContext
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
name|CreateTableDesc
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
name|FileSinkDesc
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
name|FilterDesc
operator|.
name|SampleDesc
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
name|LoadFileDesc
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
name|LoadTableDesc
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
name|MapJoinDesc
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
name|TableDesc
import|;
end_import

begin_comment
comment|/**  * Parse Context: The current parse context. This is passed to the optimizer  * which then transforms the operator tree using the parse context. All the  * optimizations are performed sequentially and then the new parse context  * populated. Note that since the parse context contains the operator tree, it  * can be easily retrieved by the next optimization step or finally for task  * generation after the plan has been completely optimized.  *  **/
end_comment

begin_class
specifier|public
class|class
name|ParseContext
block|{
specifier|private
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|ExprNodeDesc
argument_list|>
name|opToPartPruner
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|PrunedPartitionList
argument_list|>
name|opToPartList
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|SampleDesc
argument_list|>
name|opToSamplePruner
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|>
name|opToPartToSkewedPruner
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|TableScanOperator
argument_list|>
name|topOps
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|joinOps
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|MapJoinOperator
argument_list|>
name|mapJoinOps
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|SMBMapJoinOperator
argument_list|>
name|smbMapJoinOps
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|reduceSinkOperatorsAddedByEnforceBucketingSorting
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
name|nameToSplitSample
decl_stmt|;
specifier|private
name|List
argument_list|<
name|LoadTableDesc
argument_list|>
name|loadTableWork
decl_stmt|;
specifier|private
name|List
argument_list|<
name|LoadFileDesc
argument_list|>
name|loadFileWork
decl_stmt|;
specifier|private
name|Context
name|ctx
decl_stmt|;
specifier|private
name|QueryState
name|queryState
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
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
argument_list|>
name|listMapJoinOpsNoReducer
decl_stmt|;
comment|// list of map join
comment|// operators with no
comment|// reducer
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|PrunedPartitionList
argument_list|>
name|prunedPartitions
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|ReadEntity
argument_list|>
name|viewAliasToInput
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|tabNameToTabObject
decl_stmt|;
comment|/**    * The lineage information.    */
specifier|private
name|LineageInfo
name|lInfo
decl_stmt|;
specifier|private
name|GlobalLimitCtx
name|globalLimitCtx
decl_stmt|;
specifier|private
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|semanticInputs
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
decl_stmt|;
specifier|private
name|FetchTask
name|fetchTask
decl_stmt|;
specifier|private
name|QueryProperties
name|queryProperties
decl_stmt|;
specifier|private
name|TableDesc
name|fetchTableDesc
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|fetchSource
decl_stmt|;
specifier|private
name|ListSinkOperator
name|fetchSink
decl_stmt|;
specifier|private
name|AnalyzeRewriteContext
name|analyzeRewrite
decl_stmt|;
specifier|private
name|CreateTableDesc
name|createTableDesc
decl_stmt|;
specifier|private
name|boolean
name|reduceSinkAddedBySortedDynPartition
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|SelectOperator
argument_list|,
name|Table
argument_list|>
name|viewProjectToViewSchema
decl_stmt|;
specifier|private
name|ColumnAccessInfo
name|columnAccessInfo
decl_stmt|;
specifier|private
name|boolean
name|needViewColumnAuthorization
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|FileSinkDesc
argument_list|>
name|acidFileSinks
init|=
name|Collections
operator|.
name|emptySet
argument_list|()
decl_stmt|;
specifier|public
name|ParseContext
parameter_list|()
block|{   }
comment|/**    * @param conf    * @param qb    *          current QB    * @param ast    *          current parse tree    * @param opToPartPruner    *          map from table scan operator to partition pruner    * @param opToPartList    * @param topOps    *          list of operators for the top query    * @param opParseCtx    *          operator parse context - contains a mapping from operator to    *          operator parse state (row resolver etc.)    * @param joinOps    *          context needed join processing (map join specifically)    * @param loadTableWork    *          list of destination tables being loaded    * @param loadFileWork    *          list of destination files being loaded    * @param ctx    *          parse context    * @param idToTableNameMap    * @param uCtx    * @param destTableId    * @param listMapJoinOpsNoReducer    *          list of map join operators with no reducer    * @param groupOpToInputTables    * @param prunedPartitions    * @param opToSamplePruner    *          operator to sample pruner map    * @param globalLimitCtx    * @param nameToSplitSample    * @param rootTasks    */
specifier|public
name|ParseContext
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|ExprNodeDesc
argument_list|>
name|opToPartPruner
parameter_list|,
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|PrunedPartitionList
argument_list|>
name|opToPartList
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|TableScanOperator
argument_list|>
name|topOps
parameter_list|,
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|joinOps
parameter_list|,
name|Set
argument_list|<
name|SMBMapJoinOperator
argument_list|>
name|smbMapJoinOps
parameter_list|,
name|List
argument_list|<
name|LoadTableDesc
argument_list|>
name|loadTableWork
parameter_list|,
name|List
argument_list|<
name|LoadFileDesc
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
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
argument_list|>
name|listMapJoinOpsNoReducer
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|PrunedPartitionList
argument_list|>
name|prunedPartitions
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|tabNameToTabObject
parameter_list|,
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|SampleDesc
argument_list|>
name|opToSamplePruner
parameter_list|,
name|GlobalLimitCtx
name|globalLimitCtx
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
name|nameToSplitSample
parameter_list|,
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|semanticInputs
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|>
name|opToPartToSkewedPruner
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ReadEntity
argument_list|>
name|viewAliasToInput
parameter_list|,
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|reduceSinkOperatorsAddedByEnforceBucketingSorting
parameter_list|,
name|AnalyzeRewriteContext
name|analyzeRewrite
parameter_list|,
name|CreateTableDesc
name|createTableDesc
parameter_list|,
name|QueryProperties
name|queryProperties
parameter_list|,
name|Map
argument_list|<
name|SelectOperator
argument_list|,
name|Table
argument_list|>
name|viewProjectToTableSchema
parameter_list|,
name|Set
argument_list|<
name|FileSinkDesc
argument_list|>
name|acidFileSinks
parameter_list|)
block|{
name|this
operator|.
name|queryState
operator|=
name|queryState
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|queryState
operator|.
name|getConf
argument_list|()
expr_stmt|;
name|this
operator|.
name|opToPartPruner
operator|=
name|opToPartPruner
expr_stmt|;
name|this
operator|.
name|opToPartList
operator|=
name|opToPartList
expr_stmt|;
name|this
operator|.
name|joinOps
operator|=
name|joinOps
expr_stmt|;
name|this
operator|.
name|smbMapJoinOps
operator|=
name|smbMapJoinOps
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
name|topOps
operator|=
name|topOps
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
name|prunedPartitions
operator|=
name|prunedPartitions
expr_stmt|;
name|this
operator|.
name|tabNameToTabObject
operator|=
name|tabNameToTabObject
expr_stmt|;
name|this
operator|.
name|opToSamplePruner
operator|=
name|opToSamplePruner
expr_stmt|;
name|this
operator|.
name|nameToSplitSample
operator|=
name|nameToSplitSample
expr_stmt|;
name|this
operator|.
name|globalLimitCtx
operator|=
name|globalLimitCtx
expr_stmt|;
name|this
operator|.
name|semanticInputs
operator|=
name|semanticInputs
expr_stmt|;
name|this
operator|.
name|rootTasks
operator|=
name|rootTasks
expr_stmt|;
name|this
operator|.
name|opToPartToSkewedPruner
operator|=
name|opToPartToSkewedPruner
expr_stmt|;
name|this
operator|.
name|viewAliasToInput
operator|=
name|viewAliasToInput
expr_stmt|;
name|this
operator|.
name|reduceSinkOperatorsAddedByEnforceBucketingSorting
operator|=
name|reduceSinkOperatorsAddedByEnforceBucketingSorting
expr_stmt|;
name|this
operator|.
name|analyzeRewrite
operator|=
name|analyzeRewrite
expr_stmt|;
name|this
operator|.
name|createTableDesc
operator|=
name|createTableDesc
expr_stmt|;
name|this
operator|.
name|queryProperties
operator|=
name|queryProperties
expr_stmt|;
name|this
operator|.
name|viewProjectToViewSchema
operator|=
name|viewProjectToTableSchema
expr_stmt|;
name|this
operator|.
name|needViewColumnAuthorization
operator|=
name|viewProjectToTableSchema
operator|!=
literal|null
operator|&&
operator|!
name|viewProjectToTableSchema
operator|.
name|isEmpty
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|needViewColumnAuthorization
condition|)
block|{
comment|// this will trigger the column pruner to collect view column
comment|// authorization info.
name|this
operator|.
name|columnAccessInfo
operator|=
operator|new
name|ColumnAccessInfo
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|acidFileSinks
operator|!=
literal|null
operator|&&
operator|!
name|acidFileSinks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|acidFileSinks
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|acidFileSinks
operator|.
name|addAll
argument_list|(
name|acidFileSinks
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Set
argument_list|<
name|FileSinkDesc
argument_list|>
name|getAcidSinks
parameter_list|()
block|{
return|return
name|acidFileSinks
return|;
block|}
specifier|public
name|boolean
name|hasAcidWrite
parameter_list|()
block|{
return|return
operator|!
name|acidFileSinks
operator|.
name|isEmpty
argument_list|()
return|;
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
comment|/**    * @return the hive conf    */
specifier|public
name|QueryState
name|getQueryState
parameter_list|()
block|{
return|return
name|queryState
return|;
block|}
comment|/**    * @return the opToPartPruner    */
specifier|public
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|ExprNodeDesc
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
name|ExprNodeDesc
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
specifier|public
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|PrunedPartitionList
argument_list|>
name|getOpToPartList
parameter_list|()
block|{
return|return
name|opToPartList
return|;
block|}
specifier|public
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|getReduceSinkOperatorsAddedByEnforceBucketingSorting
parameter_list|()
block|{
return|return
name|reduceSinkOperatorsAddedByEnforceBucketingSorting
return|;
block|}
specifier|public
name|void
name|setReduceSinkOperatorsAddedByEnforceBucketingSorting
parameter_list|(
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|reduceSinkOperatorsAddedByEnforceBucketingSorting
parameter_list|)
block|{
name|this
operator|.
name|reduceSinkOperatorsAddedByEnforceBucketingSorting
operator|=
name|reduceSinkOperatorsAddedByEnforceBucketingSorting
expr_stmt|;
block|}
comment|/**    * @return the topOps    */
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|TableScanOperator
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
name|TableScanOperator
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
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
name|getNameToSplitSample
parameter_list|()
block|{
return|return
name|nameToSplitSample
return|;
block|}
specifier|public
name|void
name|setNameToSplitSample
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
name|nameToSplitSample
parameter_list|)
block|{
name|this
operator|.
name|nameToSplitSample
operator|=
name|nameToSplitSample
expr_stmt|;
block|}
comment|/**    * @return the loadTableWork    */
specifier|public
name|List
argument_list|<
name|LoadTableDesc
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
name|LoadTableDesc
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
name|LoadFileDesc
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
name|LoadFileDesc
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
comment|/**    * @return the joinOps    */
specifier|public
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|getJoinOps
parameter_list|()
block|{
return|return
name|joinOps
return|;
block|}
comment|/**    * @param joinOps    *          the joinOps to set    */
specifier|public
name|void
name|setJoinOps
parameter_list|(
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|joinOps
parameter_list|)
block|{
name|this
operator|.
name|joinOps
operator|=
name|joinOps
expr_stmt|;
block|}
comment|/**    * @return the listMapJoinOpsNoReducer    */
specifier|public
name|List
argument_list|<
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
argument_list|>
name|getListMapJoinOpsNoReducer
parameter_list|()
block|{
return|return
name|listMapJoinOpsNoReducer
return|;
block|}
comment|/**    * @param listMapJoinOpsNoReducer    *          the listMapJoinOpsNoReducer to set    */
specifier|public
name|void
name|setListMapJoinOpsNoReducer
parameter_list|(
name|List
argument_list|<
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
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
comment|/**    * @return the opToSamplePruner    */
specifier|public
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|SampleDesc
argument_list|>
name|getOpToSamplePruner
parameter_list|()
block|{
return|return
name|opToSamplePruner
return|;
block|}
comment|/**    * @param opToSamplePruner    *          the opToSamplePruner to set    */
specifier|public
name|void
name|setOpToSamplePruner
parameter_list|(
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|SampleDesc
argument_list|>
name|opToSamplePruner
parameter_list|)
block|{
name|this
operator|.
name|opToSamplePruner
operator|=
name|opToSamplePruner
expr_stmt|;
block|}
comment|/**    * @return pruned partition map    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|PrunedPartitionList
argument_list|>
name|getPrunedPartitions
parameter_list|()
block|{
return|return
name|prunedPartitions
return|;
block|}
comment|/**    * @param prunedPartitions    */
specifier|public
name|void
name|setPrunedPartitions
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|PrunedPartitionList
argument_list|>
name|prunedPartitions
parameter_list|)
block|{
name|this
operator|.
name|prunedPartitions
operator|=
name|prunedPartitions
expr_stmt|;
block|}
comment|/**    * Sets the lineage information.    *    * @param lInfo The lineage information.    */
specifier|public
name|void
name|setLineageInfo
parameter_list|(
name|LineageInfo
name|lInfo
parameter_list|)
block|{
name|this
operator|.
name|lInfo
operator|=
name|lInfo
expr_stmt|;
block|}
comment|/**    * Gets the associated lineage information.    *    * @return LineageInfo    */
specifier|public
name|LineageInfo
name|getLineageInfo
parameter_list|()
block|{
return|return
name|lInfo
return|;
block|}
specifier|public
name|Set
argument_list|<
name|MapJoinOperator
argument_list|>
name|getMapJoinOps
parameter_list|()
block|{
return|return
name|mapJoinOps
return|;
block|}
specifier|public
name|void
name|setMapJoinOps
parameter_list|(
name|Set
argument_list|<
name|MapJoinOperator
argument_list|>
name|mapJoinOps
parameter_list|)
block|{
name|this
operator|.
name|mapJoinOps
operator|=
name|mapJoinOps
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|SMBMapJoinOperator
argument_list|>
name|getSmbMapJoinOps
parameter_list|()
block|{
return|return
name|smbMapJoinOps
return|;
block|}
specifier|public
name|void
name|setSmbMapJoinOps
parameter_list|(
name|Set
argument_list|<
name|SMBMapJoinOperator
argument_list|>
name|smbMapJoinOps
parameter_list|)
block|{
name|this
operator|.
name|smbMapJoinOps
operator|=
name|smbMapJoinOps
expr_stmt|;
block|}
specifier|public
name|GlobalLimitCtx
name|getGlobalLimitCtx
parameter_list|()
block|{
return|return
name|globalLimitCtx
return|;
block|}
specifier|public
name|void
name|setGlobalLimitCtx
parameter_list|(
name|GlobalLimitCtx
name|globalLimitCtx
parameter_list|)
block|{
name|this
operator|.
name|globalLimitCtx
operator|=
name|globalLimitCtx
expr_stmt|;
block|}
specifier|public
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|getSemanticInputs
parameter_list|()
block|{
return|return
name|semanticInputs
return|;
block|}
specifier|public
name|void
name|replaceRootTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rootTask
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
parameter_list|)
block|{
name|this
operator|.
name|rootTasks
operator|.
name|remove
argument_list|(
name|rootTask
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootTasks
operator|.
name|addAll
argument_list|(
name|tasks
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FetchTask
name|getFetchTask
parameter_list|()
block|{
return|return
name|fetchTask
return|;
block|}
specifier|public
name|void
name|setFetchTask
parameter_list|(
name|FetchTask
name|fetchTask
parameter_list|)
block|{
name|this
operator|.
name|fetchTask
operator|=
name|fetchTask
expr_stmt|;
block|}
specifier|public
name|PrunedPartitionList
name|getPrunedPartitions
parameter_list|(
name|TableScanOperator
name|ts
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|getPrunedPartitions
argument_list|(
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getAlias
argument_list|()
argument_list|,
name|ts
argument_list|)
return|;
block|}
specifier|public
name|PrunedPartitionList
name|getPrunedPartitions
parameter_list|(
name|String
name|alias
parameter_list|,
name|TableScanOperator
name|ts
parameter_list|)
throws|throws
name|SemanticException
block|{
name|PrunedPartitionList
name|partsList
init|=
name|opToPartList
operator|.
name|get
argument_list|(
name|ts
argument_list|)
decl_stmt|;
if|if
condition|(
name|partsList
operator|==
literal|null
condition|)
block|{
name|partsList
operator|=
name|PartitionPruner
operator|.
name|prune
argument_list|(
name|ts
argument_list|,
name|this
argument_list|,
name|alias
argument_list|)
expr_stmt|;
name|opToPartList
operator|.
name|put
argument_list|(
name|ts
argument_list|,
name|partsList
argument_list|)
expr_stmt|;
block|}
return|return
name|partsList
return|;
block|}
comment|/**    * @return the opToPartToSkewedPruner    */
specifier|public
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getOpToPartToSkewedPruner
parameter_list|()
block|{
return|return
name|opToPartToSkewedPruner
return|;
block|}
comment|/**    * @param opToPartToSkewedPruner    *          the opToSkewedPruner to set    */
specifier|public
name|void
name|setOpPartToSkewedPruner
parameter_list|(
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|>
name|opToPartToSkewedPruner
parameter_list|)
block|{
name|this
operator|.
name|opToPartToSkewedPruner
operator|=
name|opToPartToSkewedPruner
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ReadEntity
argument_list|>
name|getViewAliasToInput
parameter_list|()
block|{
return|return
name|viewAliasToInput
return|;
block|}
specifier|public
name|QueryProperties
name|getQueryProperties
parameter_list|()
block|{
return|return
name|queryProperties
return|;
block|}
specifier|public
name|void
name|setQueryProperties
parameter_list|(
name|QueryProperties
name|queryProperties
parameter_list|)
block|{
name|this
operator|.
name|queryProperties
operator|=
name|queryProperties
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getFetchTableDesc
parameter_list|()
block|{
return|return
name|fetchTableDesc
return|;
block|}
specifier|public
name|void
name|setFetchTabledesc
parameter_list|(
name|TableDesc
name|fetchTableDesc
parameter_list|)
block|{
name|this
operator|.
name|fetchTableDesc
operator|=
name|fetchTableDesc
expr_stmt|;
block|}
specifier|public
name|Operator
argument_list|<
name|?
argument_list|>
name|getFetchSource
parameter_list|()
block|{
return|return
name|fetchSource
return|;
block|}
specifier|public
name|void
name|setFetchSource
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|fetchSource
parameter_list|)
block|{
name|this
operator|.
name|fetchSource
operator|=
name|fetchSource
expr_stmt|;
block|}
specifier|public
name|ListSinkOperator
name|getFetchSink
parameter_list|()
block|{
return|return
name|fetchSink
return|;
block|}
specifier|public
name|void
name|setFetchSink
parameter_list|(
name|ListSinkOperator
name|fetchSink
parameter_list|)
block|{
name|this
operator|.
name|fetchSink
operator|=
name|fetchSink
expr_stmt|;
block|}
specifier|public
name|AnalyzeRewriteContext
name|getAnalyzeRewrite
parameter_list|()
block|{
return|return
name|this
operator|.
name|analyzeRewrite
return|;
block|}
specifier|public
name|void
name|setAnalyzeRewrite
parameter_list|(
name|AnalyzeRewriteContext
name|analyzeRewrite
parameter_list|)
block|{
name|this
operator|.
name|analyzeRewrite
operator|=
name|analyzeRewrite
expr_stmt|;
block|}
specifier|public
name|CreateTableDesc
name|getCreateTable
parameter_list|()
block|{
return|return
name|this
operator|.
name|createTableDesc
return|;
block|}
specifier|public
name|void
name|setCreateTable
parameter_list|(
name|CreateTableDesc
name|createTableDesc
parameter_list|)
block|{
name|this
operator|.
name|createTableDesc
operator|=
name|createTableDesc
expr_stmt|;
block|}
specifier|public
name|void
name|setReduceSinkAddedBySortedDynPartition
parameter_list|(
specifier|final
name|boolean
name|reduceSinkAddedBySortedDynPartition
parameter_list|)
block|{
name|this
operator|.
name|reduceSinkAddedBySortedDynPartition
operator|=
name|reduceSinkAddedBySortedDynPartition
expr_stmt|;
block|}
specifier|public
name|boolean
name|isReduceSinkAddedBySortedDynPartition
parameter_list|()
block|{
return|return
name|reduceSinkAddedBySortedDynPartition
return|;
block|}
specifier|public
name|Map
argument_list|<
name|SelectOperator
argument_list|,
name|Table
argument_list|>
name|getViewProjectToTableSchema
parameter_list|()
block|{
return|return
name|viewProjectToViewSchema
return|;
block|}
specifier|public
name|ColumnAccessInfo
name|getColumnAccessInfo
parameter_list|()
block|{
return|return
name|columnAccessInfo
return|;
block|}
specifier|public
name|void
name|setColumnAccessInfo
parameter_list|(
name|ColumnAccessInfo
name|columnAccessInfo
parameter_list|)
block|{
name|this
operator|.
name|columnAccessInfo
operator|=
name|columnAccessInfo
expr_stmt|;
block|}
specifier|public
name|boolean
name|isNeedViewColumnAuthorization
parameter_list|()
block|{
return|return
name|needViewColumnAuthorization
return|;
block|}
specifier|public
name|void
name|setNeedViewColumnAuthorization
parameter_list|(
name|boolean
name|needViewColumnAuthorization
parameter_list|)
block|{
name|this
operator|.
name|needViewColumnAuthorization
operator|=
name|needViewColumnAuthorization
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|getTabNameToTabObject
parameter_list|()
block|{
return|return
name|tabNameToTabObject
return|;
block|}
block|}
end_class

end_unit

