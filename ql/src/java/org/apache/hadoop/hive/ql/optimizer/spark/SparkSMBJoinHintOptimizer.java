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
name|spark
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
name|Utilities
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
name|optimizer
operator|.
name|AbstractSMBJoinProc
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
name|SortBucketJoinProcCtx
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

begin_import
import|import
name|com
operator|.
name|clearspring
operator|.
name|analytics
operator|.
name|util
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Converts from a bucket-mapjoin created from hints to SMB mapjoin.  */
end_comment

begin_class
specifier|public
class|class
name|SparkSMBJoinHintOptimizer
extends|extends
name|AbstractSMBJoinProc
implements|implements
name|NodeProcessor
block|{
specifier|public
name|SparkSMBJoinHintOptimizer
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
block|{
name|super
argument_list|(
name|pctx
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SparkSMBJoinHintOptimizer
parameter_list|()
block|{   }
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
name|MapJoinOperator
name|mapJoinOp
init|=
operator|(
name|MapJoinOperator
operator|)
name|nd
decl_stmt|;
name|SortBucketJoinProcCtx
name|smbJoinContext
init|=
operator|(
name|SortBucketJoinProcCtx
operator|)
name|procCtx
decl_stmt|;
name|boolean
name|convert
init|=
name|canConvertBucketMapJoinToSMBJoin
argument_list|(
name|mapJoinOp
argument_list|,
name|stack
argument_list|,
name|smbJoinContext
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
if|if
condition|(
name|convert
condition|)
block|{
name|removeSmallTableReduceSink
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
name|convertBucketMapJoinToSMBJoin
argument_list|(
name|mapJoinOp
argument_list|,
name|smbJoinContext
argument_list|,
name|pGraphContext
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * In bucket mapjoin, there are ReduceSinks that mark a small table parent (Reduce Sink are removed from big-table).    * In SMB join these are not expected for any parents, either from small or big tables.    * @param mapJoinOp    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|void
name|removeSmallTableReduceSink
parameter_list|(
name|MapJoinOperator
name|mapJoinOp
parameter_list|)
block|{
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
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
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
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|par
init|=
name|parentOperators
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|!=
name|smbJoinDesc
operator|.
name|getPosBigTable
argument_list|()
condition|)
block|{
if|if
condition|(
name|par
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|grandParents
init|=
name|par
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|grandParents
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|,
literal|"AssertionError: expect # of parents to be 1, but was "
operator|+
name|grandParents
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|grandParent
init|=
name|grandParents
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|grandParent
operator|.
name|removeChild
argument_list|(
name|par
argument_list|)
expr_stmt|;
name|grandParent
operator|.
name|setChildOperators
argument_list|(
name|Utilities
operator|.
name|makeList
argument_list|(
name|mapJoinOp
argument_list|)
argument_list|)
expr_stmt|;
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|grandParent
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

