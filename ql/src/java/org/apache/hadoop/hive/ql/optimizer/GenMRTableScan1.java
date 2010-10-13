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
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Stack
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
name|exec
operator|.
name|TaskFactory
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
name|GenMRProcContext
operator|.
name|GenMapRedCtx
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
name|tableSpec
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
name|PrunedPartitionList
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
name|QBParseInfo
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
name|MapredWork
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
name|StatsWork
import|;
end_import

begin_comment
comment|/**  * Processor for the rule - table scan.  */
end_comment

begin_class
specifier|public
class|class
name|GenMRTableScan1
implements|implements
name|NodeProcessor
block|{
specifier|public
name|GenMRTableScan1
parameter_list|()
block|{   }
comment|/**    * Table Sink encountered.    *    * @param nd    *          the table sink operator encountered    * @param opProcCtx    *          context    */
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|opProcCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|TableScanOperator
name|op
init|=
operator|(
name|TableScanOperator
operator|)
name|nd
decl_stmt|;
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|opProcCtx
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|ctx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|ctx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
comment|// create a dummy MapReduce task
name|MapredWork
name|currWork
init|=
name|GenMapRedUtils
operator|.
name|getMapRedWork
argument_list|(
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|currWork
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTopOp
init|=
name|op
decl_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTopOp
argument_list|(
name|currTopOp
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|parseCtx
operator|.
name|getTopOps
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currOp
init|=
name|parseCtx
operator|.
name|getTopOps
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|currOp
operator|==
name|op
condition|)
block|{
name|String
name|currAliasId
init|=
name|alias
decl_stmt|;
name|ctx
operator|.
name|setCurrAliasId
argument_list|(
name|currAliasId
argument_list|)
expr_stmt|;
name|mapCurrCtx
operator|.
name|put
argument_list|(
name|op
argument_list|,
operator|new
name|GenMapRedCtx
argument_list|(
name|currTask
argument_list|,
name|currTopOp
argument_list|,
name|currAliasId
argument_list|)
argument_list|)
expr_stmt|;
name|QBParseInfo
name|parseInfo
init|=
name|parseCtx
operator|.
name|getQB
argument_list|()
operator|.
name|getParseInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|parseInfo
operator|.
name|isAnalyzeCommand
argument_list|()
condition|)
block|{
comment|//   ANALYZE TABLE T [PARTITION (...)] COMPUTE STATISTICS;
comment|// The plan consists of a simple MapRedTask followed by a StatsTask.
comment|// The MR task is just a simple TableScanOperator
name|StatsWork
name|statsWork
init|=
operator|new
name|StatsWork
argument_list|(
name|parseCtx
operator|.
name|getQB
argument_list|()
operator|.
name|getParseInfo
argument_list|()
operator|.
name|getTableSpec
argument_list|()
argument_list|)
decl_stmt|;
name|statsWork
operator|.
name|setAggKey
argument_list|(
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getStatsAggPrefix
argument_list|()
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|StatsWork
argument_list|>
name|statsTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|statsWork
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|currTask
operator|.
name|addDependentTask
argument_list|(
name|statsTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|add
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|currWork
operator|.
name|setGatheringStats
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// NOTE: here we should use the new partition predicate pushdown API to get a list of pruned list,
comment|// and pass it to setTaskPlan as the last parameter
name|Set
argument_list|<
name|Partition
argument_list|>
name|confirmedPartns
init|=
operator|new
name|HashSet
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
name|tableSpec
name|tblSpec
init|=
name|parseInfo
operator|.
name|getTableSpec
argument_list|()
decl_stmt|;
if|if
condition|(
name|tblSpec
operator|.
name|specType
operator|==
name|tableSpec
operator|.
name|SpecType
operator|.
name|STATIC_PARTITION
condition|)
block|{
comment|// static partition
name|confirmedPartns
operator|.
name|add
argument_list|(
name|tblSpec
operator|.
name|partHandle
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|tblSpec
operator|.
name|specType
operator|==
name|tableSpec
operator|.
name|SpecType
operator|.
name|DYNAMIC_PARTITION
condition|)
block|{
comment|// dynamic partition
name|confirmedPartns
operator|.
name|addAll
argument_list|(
name|tblSpec
operator|.
name|partitions
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|confirmedPartns
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|PrunedPartitionList
name|partList
init|=
operator|new
name|PrunedPartitionList
argument_list|(
name|confirmedPartns
argument_list|,
operator|new
name|HashSet
argument_list|<
name|Partition
argument_list|>
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
name|currWork
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|,
name|partList
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// non-partitioned table
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
name|currWork
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
assert|assert
literal|false
assert|;
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

