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
operator|.
name|spark
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
name|parse
operator|.
name|spark
operator|.
name|OptimizeSparkProcContext
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
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_comment
comment|/**  * Converts a common join operator to an SMB join if eligible.  Handles auto SMB conversion.  */
end_comment

begin_class
specifier|public
class|class
name|SparkSortMergeJoinOptimizer
extends|extends
name|AbstractSMBJoinProc
implements|implements
name|NodeProcessor
block|{
specifier|public
name|SparkSortMergeJoinOptimizer
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
name|SparkSortMergeJoinOptimizer
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
name|JoinOperator
name|joinOp
init|=
operator|(
name|JoinOperator
operator|)
name|nd
decl_stmt|;
name|HiveConf
name|conf
init|=
operator|(
operator|(
name|OptimizeSparkProcContext
operator|)
name|procCtx
operator|)
operator|.
name|getParseContext
argument_list|()
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTO_SORTMERGE_JOIN
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SortBucketJoinProcCtx
name|smbJoinContext
init|=
operator|new
name|SortBucketJoinProcCtx
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|convert
init|=
name|canConvertJoinToSMBJoin
argument_list|(
name|joinOp
argument_list|,
name|smbJoinContext
argument_list|,
name|pGraphContext
argument_list|,
name|stack
argument_list|)
decl_stmt|;
if|if
condition|(
name|convert
condition|)
block|{
return|return
name|convertJoinToSMBJoinAndReturn
argument_list|(
name|joinOp
argument_list|,
name|smbJoinContext
argument_list|,
name|pGraphContext
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|protected
name|boolean
name|canConvertJoinToSMBJoin
parameter_list|(
name|JoinOperator
name|joinOperator
parameter_list|,
name|SortBucketJoinProcCtx
name|smbJoinContext
parameter_list|,
name|ParseContext
name|pGraphContext
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
operator|!
name|supportBucketMapJoin
argument_list|(
name|stack
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|canConvertJoinToSMBJoin
argument_list|(
name|joinOperator
argument_list|,
name|smbJoinContext
argument_list|,
name|pGraphContext
argument_list|)
return|;
block|}
comment|//Preliminary checks.  In the MR version of the code, these used to be done via another walk,
comment|//here it is done inline.
specifier|private
name|boolean
name|supportBucketMapJoin
parameter_list|(
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
block|{
name|int
name|size
init|=
name|stack
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|stack
operator|.
name|get
argument_list|(
name|size
operator|-
literal|1
argument_list|)
operator|instanceof
name|JoinOperator
operator|)
operator|||
operator|!
operator|(
name|stack
operator|.
name|get
argument_list|(
name|size
operator|-
literal|2
argument_list|)
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// If any operator in the stack does not support a auto-conversion, this join should
comment|// not be converted.
for|for
control|(
name|int
name|pos
init|=
name|size
operator|-
literal|3
init|;
name|pos
operator|>=
literal|0
condition|;
name|pos
operator|--
control|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|stack
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|op
operator|.
name|supportAutomaticSortMergeJoin
argument_list|()
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
specifier|protected
name|SMBMapJoinOperator
name|convertJoinToSMBJoinAndReturn
parameter_list|(
name|JoinOperator
name|joinOp
parameter_list|,
name|SortBucketJoinProcCtx
name|smbJoinContext
parameter_list|,
name|ParseContext
name|parseContext
parameter_list|)
throws|throws
name|SemanticException
block|{
name|MapJoinOperator
name|mapJoinOp
init|=
name|convertJoinToBucketMapJoin
argument_list|(
name|joinOp
argument_list|,
name|smbJoinContext
argument_list|,
name|parseContext
argument_list|)
decl_stmt|;
name|SMBMapJoinOperator
name|smbMapJoinOp
init|=
name|convertBucketMapJoinToSMBJoin
argument_list|(
name|mapJoinOp
argument_list|,
name|smbJoinContext
argument_list|,
name|parseContext
argument_list|)
decl_stmt|;
name|smbMapJoinOp
operator|.
name|setConvertedAutomaticallySMBJoin
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|smbMapJoinOp
return|;
block|}
block|}
end_class

end_unit

