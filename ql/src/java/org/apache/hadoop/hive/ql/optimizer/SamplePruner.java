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
name|fs
operator|.
name|Path
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
name|FilterOperator
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
name|plan
operator|.
name|filterDesc
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
name|filterDesc
operator|.
name|sampleDesc
import|;
end_import

begin_comment
comment|/**  * The transformation step that does sample pruning.  *   */
end_comment

begin_class
specifier|public
class|class
name|SamplePruner
implements|implements
name|Transform
block|{
specifier|public
specifier|static
class|class
name|SamplePrunerCtx
implements|implements
name|NodeProcessorCtx
block|{
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|sampleDesc
argument_list|>
name|opToSamplePruner
decl_stmt|;
specifier|public
name|SamplePrunerCtx
parameter_list|(
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|sampleDesc
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
comment|/**      * @return the opToSamplePruner      */
specifier|public
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|sampleDesc
argument_list|>
name|getOpToSamplePruner
parameter_list|()
block|{
return|return
name|opToSamplePruner
return|;
block|}
comment|/**      * @param opToSamplePruner      *          the opToSamplePruner to set      */
specifier|public
name|void
name|setOpToSamplePruner
parameter_list|(
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|sampleDesc
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
block|}
comment|// The log
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
literal|"hive.ql.optimizer.SamplePruner"
argument_list|)
decl_stmt|;
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hive.ql.optimizer.Transform#transform(org.apache.hadoop    * .hive.ql.parse.ParseContext)    */
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
comment|// create a the context for walking operators
name|SamplePrunerCtx
name|samplePrunerCtx
init|=
operator|new
name|SamplePrunerCtx
argument_list|(
name|pctx
operator|.
name|getOpToSamplePruner
argument_list|()
argument_list|)
decl_stmt|;
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
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
literal|"(TS%FIL%FIL%)"
argument_list|)
argument_list|,
name|getFilterProc
argument_list|()
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
name|samplePrunerCtx
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
comment|// Filter processor
specifier|public
specifier|static
class|class
name|FilterPPR
implements|implements
name|NodeProcessor
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
name|FilterOperator
name|filOp
init|=
operator|(
name|FilterOperator
operator|)
name|nd
decl_stmt|;
name|filterDesc
name|filOpDesc
init|=
name|filOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|sampleDesc
name|sampleDescr
init|=
name|filOpDesc
operator|.
name|getSampleDescr
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|sampleDescr
operator|==
literal|null
operator|)
operator|||
operator|!
name|sampleDescr
operator|.
name|getInputPruning
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
assert|assert
name|stack
operator|.
name|size
argument_list|()
operator|==
literal|3
assert|;
name|TableScanOperator
name|tsOp
init|=
operator|(
name|TableScanOperator
operator|)
name|stack
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
operator|(
operator|(
name|SamplePrunerCtx
operator|)
name|procCtx
operator|)
operator|.
name|getOpToSamplePruner
argument_list|()
operator|.
name|put
argument_list|(
name|tsOp
argument_list|,
name|sampleDescr
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getFilterProc
parameter_list|()
block|{
return|return
operator|new
name|FilterPPR
argument_list|()
return|;
block|}
comment|// Default processor which does nothing
specifier|public
specifier|static
class|class
name|DefaultPPR
implements|implements
name|NodeProcessor
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
comment|// Nothing needs to be done.
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|DefaultPPR
argument_list|()
return|;
block|}
comment|/**    * Prunes to get all the files in the partition that satisfy the TABLESAMPLE    * clause    *     * @param part    *          The partition to prune    * @return Path[]    * @throws SemanticException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
specifier|static
name|Path
index|[]
name|prune
parameter_list|(
name|Partition
name|part
parameter_list|,
name|sampleDesc
name|sampleDescr
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|num
init|=
name|sampleDescr
operator|.
name|getNumerator
argument_list|()
decl_stmt|;
name|int
name|den
init|=
name|sampleDescr
operator|.
name|getDenominator
argument_list|()
decl_stmt|;
name|int
name|bucketCount
init|=
name|part
operator|.
name|getBucketCount
argument_list|()
decl_stmt|;
name|String
name|fullScanMsg
init|=
literal|""
decl_stmt|;
comment|// check if input pruning is possible
if|if
condition|(
name|sampleDescr
operator|.
name|getInputPruning
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"numerator = "
operator|+
name|num
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"denominator = "
operator|+
name|den
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"bucket count = "
operator|+
name|bucketCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|bucketCount
operator|==
name|den
condition|)
block|{
name|Path
index|[]
name|ret
init|=
operator|new
name|Path
index|[
literal|1
index|]
decl_stmt|;
name|ret
index|[
literal|0
index|]
operator|=
name|part
operator|.
name|getBucketPath
argument_list|(
name|num
operator|-
literal|1
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
elseif|else
if|if
condition|(
name|bucketCount
operator|>
name|den
operator|&&
name|bucketCount
operator|%
name|den
operator|==
literal|0
condition|)
block|{
name|int
name|numPathsInSample
init|=
name|bucketCount
operator|/
name|den
decl_stmt|;
name|Path
index|[]
name|ret
init|=
operator|new
name|Path
index|[
name|numPathsInSample
index|]
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
name|numPathsInSample
condition|;
name|i
operator|++
control|)
block|{
name|ret
index|[
name|i
index|]
operator|=
name|part
operator|.
name|getBucketPath
argument_list|(
name|i
operator|*
name|den
operator|+
name|num
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
elseif|else
if|if
condition|(
name|bucketCount
operator|<
name|den
operator|&&
name|den
operator|%
name|bucketCount
operator|==
literal|0
condition|)
block|{
name|Path
index|[]
name|ret
init|=
operator|new
name|Path
index|[
literal|1
index|]
decl_stmt|;
name|ret
index|[
literal|0
index|]
operator|=
name|part
operator|.
name|getBucketPath
argument_list|(
operator|(
name|num
operator|-
literal|1
operator|)
operator|%
name|bucketCount
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
else|else
block|{
comment|// need to do full scan
name|fullScanMsg
operator|=
literal|"Tablesample denominator "
operator|+
name|den
operator|+
literal|" is not multiple/divisor of bucket count "
operator|+
name|bucketCount
operator|+
literal|" of table "
operator|+
name|part
operator|.
name|getTable
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// need to do full scan
name|fullScanMsg
operator|=
literal|"Tablesample not on clustered columns"
expr_stmt|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
name|fullScanMsg
operator|+
literal|", using full table scan"
argument_list|)
expr_stmt|;
name|Path
index|[]
name|ret
init|=
name|part
operator|.
name|getPath
argument_list|()
decl_stmt|;
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

