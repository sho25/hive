begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|api
operator|.
name|Order
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
name|ErrorMsg
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
name|DummyStoreOperator
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
name|lib
operator|.
name|DefaultGraphWalker
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
name|DefaultRuleDispatcher
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
name|Dispatcher
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
name|GraphWalker
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
name|lib
operator|.
name|Rule
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
name|RuleRegExp
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
name|HiveException
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
name|QBJoinTree
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
name|parse
operator|.
name|TableAccessAnalyzer
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
name|OperatorDesc
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
name|SMBJoinDesc
import|;
end_import

begin_comment
comment|//try to replace a bucket map join with a sorted merge map join
end_comment

begin_class
specifier|public
class|class
name|SortedMergeBucketMapJoinOptimizer
implements|implements
name|Transform
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
name|SortedMergeBucketMapJoinOptimizer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|SortedMergeBucketMapJoinOptimizer
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
comment|// go through all map joins and find out all which have enabled bucket map
comment|// join.
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|MapJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getSortedMergeBucketMapjoinProc
argument_list|(
name|pctx
argument_list|)
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of topop nodes
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pctx
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
specifier|private
name|NodeProcessor
name|getSortedMergeBucketMapjoinProc
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
block|{
return|return
operator|new
name|SortedMergeBucketMapjoinProc
argument_list|(
name|pctx
argument_list|)
return|;
block|}
specifier|private
name|NodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|NodeProcessor
argument_list|()
block|{
annotation|@
name|Override
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
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
literal|null
return|;
block|}
block|}
return|;
block|}
class|class
name|SortedMergeBucketMapjoinProc
extends|extends
name|AbstractBucketJoinProc
implements|implements
name|NodeProcessor
block|{
specifier|private
name|ParseContext
name|pGraphContext
decl_stmt|;
specifier|public
name|SortedMergeBucketMapjoinProc
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
block|{
name|this
operator|.
name|pGraphContext
operator|=
name|pctx
expr_stmt|;
block|}
specifier|public
name|SortedMergeBucketMapjoinProc
parameter_list|()
block|{     }
comment|// Return true or false based on whether the mapjoin was converted successfully to
comment|// a sort-merge map join operator.
specifier|private
name|boolean
name|convertSMBJoin
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
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|nd
operator|instanceof
name|SMBMapJoinOperator
condition|)
block|{
return|return
literal|false
return|;
block|}
name|MapJoinOperator
name|mapJoinOp
init|=
operator|(
name|MapJoinOperator
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getAliasBucketFileNameMapping
argument_list|()
operator|==
literal|null
operator|||
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getAliasBucketFileNameMapping
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
name|boolean
name|tableSorted
init|=
literal|true
decl_stmt|;
name|QBJoinTree
name|joinCxt
init|=
name|this
operator|.
name|pGraphContext
operator|.
name|getMapJoinContext
argument_list|()
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
decl_stmt|;
if|if
condition|(
name|joinCxt
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
index|[]
name|srcs
init|=
name|joinCxt
operator|.
name|getBaseSrc
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|srcPos
init|=
literal|0
init|;
name|srcPos
operator|<
name|srcs
operator|.
name|length
condition|;
name|srcPos
operator|++
control|)
block|{
name|srcs
index|[
name|srcPos
index|]
operator|=
name|QB
operator|.
name|getAppendedAliasFromId
argument_list|(
name|joinCxt
operator|.
name|getId
argument_list|()
argument_list|,
name|srcs
index|[
name|srcPos
index|]
argument_list|)
expr_stmt|;
block|}
comment|// All the tables/partitions columns should be sorted in the same order
comment|// For example, if tables A and B are being joined on columns c1, c2 and c3
comment|// which are the sorted and bucketed columns. The join would work, as long
comment|// c1, c2 and c3 are sorted in the same order.
name|List
argument_list|<
name|Order
argument_list|>
name|sortColumnsFirstTable
init|=
operator|new
name|ArrayList
argument_list|<
name|Order
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|srcs
operator|.
name|length
condition|;
name|pos
operator|++
control|)
block|{
name|tableSorted
operator|=
name|tableSorted
operator|&&
name|isTableSorted
argument_list|(
name|this
operator|.
name|pGraphContext
argument_list|,
name|mapJoinOp
argument_list|,
name|joinCxt
argument_list|,
name|pos
argument_list|,
name|sortColumnsFirstTable
argument_list|,
name|srcs
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|tableSorted
condition|)
block|{
comment|//this is a mapjoin but not suit for a sort merge bucket map join. check outer joins
name|MapJoinProcessor
operator|.
name|checkMapJoin
argument_list|(
operator|(
operator|(
name|MapJoinOperator
operator|)
name|nd
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getPosBigTable
argument_list|()
argument_list|,
operator|(
operator|(
name|MapJoinOperator
operator|)
name|nd
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getConds
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// convert a bucket map join operator to a sorted merge bucket map join
comment|// operator
name|convertToSMBJoin
argument_list|(
name|mapJoinOp
argument_list|,
name|srcs
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
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
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|boolean
name|convert
init|=
name|convertSMBJoin
argument_list|(
name|nd
argument_list|,
name|stack
argument_list|,
name|procCtx
argument_list|,
name|nodeOutputs
argument_list|)
decl_stmt|;
comment|// Throw an error if the user asked for sort merge bucketed mapjoin to be enforced
comment|// and sort merge bucketed mapjoin cannot be performed
if|if
condition|(
operator|!
name|convert
operator|&&
name|pGraphContext
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEENFORCESORTMERGEBUCKETMAPJOIN
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|SORTMERGE_MAPJOIN_FAILED
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|SMBMapJoinOperator
name|convertToSMBJoin
parameter_list|(
name|MapJoinOperator
name|mapJoinOp
parameter_list|,
name|String
index|[]
name|srcs
parameter_list|)
block|{
name|SMBMapJoinOperator
name|smbJop
init|=
operator|new
name|SMBMapJoinOperator
argument_list|(
name|mapJoinOp
argument_list|)
decl_stmt|;
name|SMBJoinDesc
name|smbJoinDesc
init|=
operator|new
name|SMBJoinDesc
argument_list|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|smbJop
operator|.
name|setConf
argument_list|(
name|smbJoinDesc
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|tagToAlias
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|srcs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|tagToAlias
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|,
name|srcs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|smbJoinDesc
operator|.
name|setTagToAlias
argument_list|(
name|tagToAlias
argument_list|)
expr_stmt|;
name|int
name|indexInListMapJoinNoReducer
init|=
name|this
operator|.
name|pGraphContext
operator|.
name|getListMapJoinOpsNoReducer
argument_list|()
operator|.
name|indexOf
argument_list|(
name|mapJoinOp
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexInListMapJoinNoReducer
operator|>=
literal|0
condition|)
block|{
name|this
operator|.
name|pGraphContext
operator|.
name|getListMapJoinOpsNoReducer
argument_list|()
operator|.
name|remove
argument_list|(
name|indexInListMapJoinNoReducer
argument_list|)
expr_stmt|;
name|this
operator|.
name|pGraphContext
operator|.
name|getListMapJoinOpsNoReducer
argument_list|()
operator|.
name|add
argument_list|(
name|indexInListMapJoinNoReducer
argument_list|,
name|smbJop
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|DummyStoreOperator
argument_list|>
name|aliasToSink
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|DummyStoreOperator
argument_list|>
argument_list|()
decl_stmt|;
comment|// For all parents (other than the big table), insert a dummy store operator
comment|/* Consider a query like:         *         * select * from         *   (subq1 --> has a filter)         *   join         *   (subq2 --> has a filter)         * on some key         *         * Let us assume that subq1 is the small table (either specified by the user or inferred         * automatically). The following operator tree will be created:         *         * TableScan (subq1) --> Select --> Filter --> DummyStore         *                                                         \         *                                                          \     SMBJoin         *                                                          /         *                                                         /         * TableScan (subq2) --> Select --> Filter         */
name|List
argument_list|<
name|?
extends|extends
name|Operator
argument_list|>
name|parentOperators
init|=
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parentOperators
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Operator
name|par
init|=
name|parentOperators
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|index
init|=
name|par
operator|.
name|getChildOperators
argument_list|()
operator|.
name|indexOf
argument_list|(
name|mapJoinOp
argument_list|)
decl_stmt|;
name|par
operator|.
name|getChildOperators
argument_list|()
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|smbJoinDesc
operator|.
name|getPosBigTable
argument_list|()
condition|)
block|{
name|par
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|smbJop
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|DummyStoreOperator
name|dummyStoreOp
init|=
operator|new
name|DummyStoreOperator
argument_list|()
decl_stmt|;
name|par
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|dummyStoreOp
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childrenOps
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|childrenOps
operator|.
name|add
argument_list|(
name|smbJop
argument_list|)
expr_stmt|;
name|dummyStoreOp
operator|.
name|setChildOperators
argument_list|(
name|childrenOps
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parentOps
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|parentOps
operator|.
name|add
argument_list|(
name|par
argument_list|)
expr_stmt|;
name|dummyStoreOp
operator|.
name|setParentOperators
argument_list|(
name|parentOps
argument_list|)
expr_stmt|;
name|aliasToSink
operator|.
name|put
argument_list|(
name|srcs
index|[
name|i
index|]
argument_list|,
name|dummyStoreOp
argument_list|)
expr_stmt|;
name|smbJop
operator|.
name|getParentOperators
argument_list|()
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|smbJop
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|dummyStoreOp
argument_list|)
expr_stmt|;
block|}
block|}
name|smbJoinDesc
operator|.
name|setAliasToSink
argument_list|(
name|aliasToSink
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Operator
argument_list|>
name|childOps
init|=
name|mapJoinOp
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|childOps
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Operator
name|child
init|=
name|childOps
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|index
init|=
name|child
operator|.
name|getParentOperators
argument_list|()
operator|.
name|indexOf
argument_list|(
name|mapJoinOp
argument_list|)
decl_stmt|;
name|child
operator|.
name|getParentOperators
argument_list|()
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|child
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|smbJop
argument_list|)
expr_stmt|;
block|}
return|return
name|smbJop
return|;
block|}
comment|/**      * Whether this table is eligible for a sort-merge join.      *      * @param pctx                  parse context      * @param op                    map join operator being considered      * @param joinTree              join tree being considered      * @param alias                 table alias in the join tree being checked      * @param pos                   position of the table      * @param sortColumnsFirstTable The names and order of the sorted columns for the first table.      *                              It is not initialized when pos = 0.      * @return      * @throws SemanticException      */
specifier|private
name|boolean
name|isTableSorted
parameter_list|(
name|ParseContext
name|pctx
parameter_list|,
name|MapJoinOperator
name|op
parameter_list|,
name|QBJoinTree
name|joinTree
parameter_list|,
name|int
name|pos
parameter_list|,
name|List
argument_list|<
name|Order
argument_list|>
name|sortColumnsFirstTable
parameter_list|,
name|String
index|[]
name|aliases
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|alias
init|=
name|aliases
index|[
name|pos
index|]
decl_stmt|;
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|Table
argument_list|>
name|topToTable
init|=
name|this
operator|.
name|pGraphContext
operator|.
name|getTopToTable
argument_list|()
decl_stmt|;
comment|/*        * Consider a query like:        *        * select -- mapjoin(subq1) --  * from        * (select a.key, a.value from tbl1 a) subq1        *   join        * (select a.key, a.value from tbl2 a) subq2        * on subq1.key = subq2.key;        *        * aliasToOpInfo contains the SelectOperator for subq1 and subq2.        * We need to traverse the tree (using TableAccessAnalyzer) to get to the base        * table. If the object being map-joined is a base table, then aliasToOpInfo        * contains the TableScanOperator, and TableAccessAnalyzer is a no-op.        */
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|topOp
init|=
name|joinTree
operator|.
name|getAliasToOpInfo
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|topOp
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|joinCols
init|=
name|toColumns
argument_list|(
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getKeys
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|pos
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|joinCols
operator|==
literal|null
operator|||
name|joinCols
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TableScanOperator
name|tso
init|=
name|TableAccessAnalyzer
operator|.
name|genRootTableScan
argument_list|(
name|topOp
argument_list|,
name|joinCols
argument_list|)
decl_stmt|;
if|if
condition|(
name|tso
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// For nested sub-queries, the alias mapping is not maintained in QB currently.
comment|/*        * Consider a query like:        *        * select count(*) from        *   (        *     select key, count(*) from        *       (        *         select --mapjoin(a)-- a.key as key, a.value as val1, b.value as val2        *         from tbl1 a join tbl2 b on a.key = b.key        *       ) subq1        *     group by key        *   ) subq2;        *        * The table alias should be subq2:subq1:a which needs to be fetched from topOps.        */
if|if
condition|(
name|pGraphContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|containsValue
argument_list|(
name|tso
argument_list|)
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|topOpEntry
range|:
name|this
operator|.
name|pGraphContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|topOpEntry
operator|.
name|getValue
argument_list|()
operator|==
name|tso
condition|)
block|{
name|alias
operator|=
name|topOpEntry
operator|.
name|getKey
argument_list|()
expr_stmt|;
name|aliases
index|[
name|pos
index|]
operator|=
name|alias
expr_stmt|;
break|break;
block|}
block|}
block|}
else|else
block|{
comment|// Ideally, this should never happen, and this should be an assert.
return|return
literal|false
return|;
block|}
name|Table
name|tbl
init|=
name|topToTable
operator|.
name|get
argument_list|(
name|tso
argument_list|)
decl_stmt|;
if|if
condition|(
name|tbl
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|PrunedPartitionList
name|prunedParts
init|=
literal|null
decl_stmt|;
try|try
block|{
name|prunedParts
operator|=
name|pGraphContext
operator|.
name|getOpToPartList
argument_list|()
operator|.
name|get
argument_list|(
name|tso
argument_list|)
expr_stmt|;
if|if
condition|(
name|prunedParts
operator|==
literal|null
condition|)
block|{
name|prunedParts
operator|=
name|PartitionPruner
operator|.
name|prune
argument_list|(
name|tbl
argument_list|,
name|pGraphContext
operator|.
name|getOpToPartPruner
argument_list|()
operator|.
name|get
argument_list|(
name|tso
argument_list|)
argument_list|,
name|pGraphContext
operator|.
name|getConf
argument_list|()
argument_list|,
name|alias
argument_list|,
name|pGraphContext
operator|.
name|getPrunedPartitions
argument_list|()
argument_list|)
expr_stmt|;
name|pGraphContext
operator|.
name|getOpToPartList
argument_list|()
operator|.
name|put
argument_list|(
name|tso
argument_list|,
name|prunedParts
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
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
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|prunedParts
operator|.
name|getNotDeniedPartns
argument_list|()
decl_stmt|;
comment|// Populate the names and order of columns for the first partition of the
comment|// first table
if|if
condition|(
operator|(
name|pos
operator|==
literal|0
operator|)
operator|&&
operator|(
name|partitions
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|partitions
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|Partition
name|firstPartition
init|=
name|partitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|sortColumnsFirstTable
operator|.
name|addAll
argument_list|(
name|firstPartition
operator|.
name|getSortCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Partition
name|partition
range|:
name|prunedParts
operator|.
name|getNotDeniedPartns
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|checkSortColsAndJoinCols
argument_list|(
name|partition
operator|.
name|getSortCols
argument_list|()
argument_list|,
name|joinCols
argument_list|,
name|sortColumnsFirstTable
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|// Populate the names and order of columns for the first table
if|if
condition|(
name|pos
operator|==
literal|0
condition|)
block|{
name|sortColumnsFirstTable
operator|.
name|addAll
argument_list|(
name|tbl
operator|.
name|getSortCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|checkSortColsAndJoinCols
argument_list|(
name|tbl
operator|.
name|getSortCols
argument_list|()
argument_list|,
name|joinCols
argument_list|,
name|sortColumnsFirstTable
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|checkSortColsAndJoinCols
parameter_list|(
name|List
argument_list|<
name|Order
argument_list|>
name|sortCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|joinCols
parameter_list|,
name|List
argument_list|<
name|Order
argument_list|>
name|sortColumnsFirstPartition
parameter_list|)
block|{
if|if
condition|(
name|sortCols
operator|==
literal|null
operator|||
name|sortCols
operator|.
name|size
argument_list|()
operator|!=
name|joinCols
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|sortColNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// The join columns should contain all the sort columns
comment|// The sort columns of all the tables should be in the same order
comment|// compare the column names and the order with the first table/partition.
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|sortCols
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
name|Order
name|o
init|=
name|sortCols
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
if|if
condition|(
name|o
operator|.
name|getOrder
argument_list|()
operator|!=
name|sortColumnsFirstPartition
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getOrder
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|sortColNames
operator|.
name|add
argument_list|(
name|o
operator|.
name|getCol
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// The column names and order (ascending/descending) matched
comment|// The join columns should contain sort columns
return|return
name|sortColNames
operator|.
name|containsAll
argument_list|(
name|joinCols
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

