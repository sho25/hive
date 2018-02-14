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
package|;
end_package

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
name|ql
operator|.
name|exec
operator|.
name|FileSinkOperator
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
name|OperatorUtils
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
name|repl
operator|.
name|ReplStateLogWork
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
name|repl
operator|.
name|bootstrap
operator|.
name|ReplLoadWork
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
name|io
operator|.
name|AcidUtils
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
name|GenTezProcContext
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
name|GenTezWork
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
name|spark
operator|.
name|GenSparkWork
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
name|ArchiveWork
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
name|BaseWork
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
name|BasicStatsNoJobWork
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
name|BasicStatsWork
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
name|ColumnStatsUpdateWork
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
name|ConditionalWork
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
name|CopyWork
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
name|DDLWork
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
name|DependencyCollectionWork
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
name|ExplainSQRewriteWork
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
name|ExplainWork
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
name|FetchWork
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
name|FunctionWork
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
name|MapredLocalWork
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
name|MoveWork
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
name|SparkWork
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
name|TezWork
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Finds Acid FileSinkDesc objects which can be created in the physical (disconnected) plan, e.g.  * {@link org.apache.hadoop.hive.ql.parse.GenTezUtils#removeUnionOperators(GenTezProcContext, BaseWork, int)}  * so that statementId can be properly assigned to ensure unique ROW__IDs  * {@link org.apache.hadoop.hive.ql.optimizer.unionproc.UnionProcFactory} is another example where  * Union All optimizations create new FileSinkDescS  */
end_comment

begin_class
specifier|public
class|class
name|QueryPlanPostProcessor
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|QueryPlanPostProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|QueryPlanPostProcessor
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|Set
argument_list|<
name|FileSinkDesc
argument_list|>
name|acidSinks
parameter_list|,
name|String
name|executionId
parameter_list|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|t
range|:
name|rootTasks
control|)
block|{
comment|//Work
name|Object
name|work
init|=
name|t
operator|.
name|getWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|work
operator|instanceof
name|TezWork
condition|)
block|{
for|for
control|(
name|BaseWork
name|bw
range|:
operator|(
operator|(
name|TezWork
operator|)
name|work
operator|)
operator|.
name|getAllWorkUnsorted
argument_list|()
control|)
block|{
name|collectFileSinkDescs
argument_list|(
name|bw
operator|.
name|getAllLeafOperators
argument_list|()
argument_list|,
name|acidSinks
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|work
operator|instanceof
name|BaseWork
condition|)
block|{
name|collectFileSinkDescs
argument_list|(
operator|(
operator|(
name|BaseWork
operator|)
name|work
operator|)
operator|.
name|getAllLeafOperators
argument_list|()
argument_list|,
name|acidSinks
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|work
operator|instanceof
name|MapredWork
condition|)
block|{
name|MapredWork
name|w
init|=
operator|(
name|MapredWork
operator|)
name|work
decl_stmt|;
if|if
condition|(
name|w
operator|.
name|getMapWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|collectFileSinkDescs
argument_list|(
name|w
operator|.
name|getMapWork
argument_list|()
operator|.
name|getAllLeafOperators
argument_list|()
argument_list|,
name|acidSinks
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|w
operator|.
name|getReduceWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|collectFileSinkDescs
argument_list|(
name|w
operator|.
name|getReduceWork
argument_list|()
operator|.
name|getAllLeafOperators
argument_list|()
argument_list|,
name|acidSinks
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|work
operator|instanceof
name|SparkWork
condition|)
block|{
for|for
control|(
name|BaseWork
name|bw
range|:
operator|(
operator|(
name|SparkWork
operator|)
name|work
operator|)
operator|.
name|getRoots
argument_list|()
control|)
block|{
name|collectFileSinkDescs
argument_list|(
name|bw
operator|.
name|getAllLeafOperators
argument_list|()
argument_list|,
name|acidSinks
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|work
operator|instanceof
name|MapredLocalWork
condition|)
block|{
comment|//I don't think this can have any FileSinkOperatorS - more future proofing
name|Set
argument_list|<
name|FileSinkOperator
argument_list|>
name|fileSinkOperatorSet
init|=
name|OperatorUtils
operator|.
name|findOperators
argument_list|(
operator|(
operator|(
name|MapredLocalWork
operator|)
name|work
operator|)
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|values
argument_list|()
argument_list|,
name|FileSinkOperator
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|FileSinkOperator
name|fsop
range|:
name|fileSinkOperatorSet
control|)
block|{
name|collectFileSinkDescs
argument_list|(
name|fsop
argument_list|,
name|acidSinks
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|work
operator|instanceof
name|ExplainWork
condition|)
block|{
operator|new
name|QueryPlanPostProcessor
argument_list|(
operator|(
operator|(
name|ExplainWork
operator|)
name|work
operator|)
operator|.
name|getRootTasks
argument_list|()
argument_list|,
name|acidSinks
argument_list|,
name|executionId
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|work
operator|instanceof
name|ReplLoadWork
operator|||
name|work
operator|instanceof
name|ReplStateLogWork
operator|||
name|work
operator|instanceof
name|GenTezWork
operator|||
name|work
operator|instanceof
name|GenSparkWork
operator|||
name|work
operator|instanceof
name|ArchiveWork
operator|||
name|work
operator|instanceof
name|ColumnStatsUpdateWork
operator|||
name|work
operator|instanceof
name|BasicStatsWork
operator|||
name|work
operator|instanceof
name|ConditionalWork
operator|||
name|work
operator|instanceof
name|CopyWork
operator|||
name|work
operator|instanceof
name|DDLWork
operator|||
name|work
operator|instanceof
name|DependencyCollectionWork
operator|||
name|work
operator|instanceof
name|ExplainSQRewriteWork
operator|||
name|work
operator|instanceof
name|FetchWork
operator|||
name|work
operator|instanceof
name|FunctionWork
operator|||
name|work
operator|instanceof
name|MoveWork
operator|||
name|work
operator|instanceof
name|BasicStatsNoJobWork
operator|||
name|work
operator|instanceof
name|StatsWork
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found "
operator|+
name|work
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" - no FileSinkOperation can be present.  executionId="
operator|+
name|executionId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//if here, someone must have added new Work object - should it be walked to find FileSinks?
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unexpected Work object: "
operator|+
name|work
operator|.
name|getClass
argument_list|()
operator|+
literal|" executionId="
operator|+
name|executionId
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|collectFileSinkDescs
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|leaf
parameter_list|,
name|Set
argument_list|<
name|FileSinkDesc
argument_list|>
name|acidSinks
parameter_list|)
block|{
if|if
condition|(
name|leaf
operator|instanceof
name|FileSinkOperator
condition|)
block|{
name|FileSinkDesc
name|fsd
init|=
operator|(
operator|(
name|FileSinkOperator
operator|)
name|leaf
operator|)
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|fsd
operator|.
name|getWriteType
argument_list|()
operator|!=
name|AcidUtils
operator|.
name|Operation
operator|.
name|NOT_ACID
condition|)
block|{
if|if
condition|(
name|acidSinks
operator|.
name|add
argument_list|(
name|fsd
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found Acid Sink: "
operator|+
name|fsd
operator|.
name|getDirName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|private
name|void
name|collectFileSinkDescs
parameter_list|(
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|leaves
parameter_list|,
name|Set
argument_list|<
name|FileSinkDesc
argument_list|>
name|acidSinks
parameter_list|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|leaf
range|:
name|leaves
control|)
block|{
name|collectFileSinkDescs
argument_list|(
name|leaf
argument_list|,
name|acidSinks
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

