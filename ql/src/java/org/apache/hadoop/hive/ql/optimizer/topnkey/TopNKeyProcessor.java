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
name|topnkey
package|;
end_package

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
name|OperatorFactory
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
name|RowSchema
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
name|TopNKeyOperator
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
name|SemanticNodeProcessor
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
name|ReduceSinkDesc
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
name|TopNKeyDesc
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
comment|/**  * TopNKeyProcessor is a processor for TopNKeyOperator.  * A TopNKeyOperator will be placed before any ReduceSinkOperator which has a topN property>= 0.  */
end_comment

begin_class
specifier|public
class|class
name|TopNKeyProcessor
implements|implements
name|SemanticNodeProcessor
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
name|TopNKeyProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|float
name|efficiencyThreshold
decl_stmt|;
specifier|private
name|long
name|checkEfficiencyNumBatches
decl_stmt|;
specifier|private
name|int
name|maxTopNAllowed
decl_stmt|;
specifier|private
name|int
name|maxNumberOfPartitions
decl_stmt|;
specifier|public
name|TopNKeyProcessor
parameter_list|()
block|{   }
specifier|public
name|TopNKeyProcessor
parameter_list|(
name|int
name|maxTopNAllowed
parameter_list|,
name|float
name|efficiencyThreshold
parameter_list|,
name|long
name|checkEfficiencyNumBatches
parameter_list|,
name|int
name|maxNumberOfPartitions
parameter_list|)
block|{
name|this
operator|.
name|maxTopNAllowed
operator|=
name|maxTopNAllowed
expr_stmt|;
name|this
operator|.
name|efficiencyThreshold
operator|=
name|efficiencyThreshold
expr_stmt|;
name|this
operator|.
name|checkEfficiencyNumBatches
operator|=
name|checkEfficiencyNumBatches
expr_stmt|;
name|this
operator|.
name|maxNumberOfPartitions
operator|=
name|maxNumberOfPartitions
expr_stmt|;
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
comment|// Get ReduceSinkOperator
name|ReduceSinkOperator
name|reduceSinkOperator
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|nd
decl_stmt|;
name|ReduceSinkDesc
name|reduceSinkDesc
init|=
name|reduceSinkOperator
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// Check whether the reduce sink operator contains top n
if|if
condition|(
name|reduceSinkDesc
operator|.
name|getTopN
argument_list|()
operator|<
literal|0
operator|||
operator|!
name|reduceSinkDesc
operator|.
name|isOrdering
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|reduceSinkDesc
operator|.
name|getTopN
argument_list|()
operator|>
name|maxTopNAllowed
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Check whether there already is a top n key operator
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOperator
init|=
name|reduceSinkOperator
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentOperator
operator|instanceof
name|TopNKeyOperator
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
if|if
condition|(
name|reduceSinkDesc
operator|.
name|isPTFReduceSink
argument_list|()
condition|)
block|{
comment|// All keys are partition keys or no keys at all
comment|// Note: partition cols are prefix of key cols
if|if
condition|(
name|reduceSinkDesc
operator|.
name|getPartitionCols
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
name|reduceSinkDesc
operator|.
name|getKeyCols
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|partitionCols
operator|=
name|reduceSinkDesc
operator|.
name|getPartitionCols
argument_list|()
expr_stmt|;
block|}
name|TopNKeyDesc
name|topNKeyDesc
init|=
operator|new
name|TopNKeyDesc
argument_list|(
name|reduceSinkDesc
operator|.
name|getTopN
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getOrder
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getNullOrder
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getKeyCols
argument_list|()
argument_list|,
name|partitionCols
argument_list|,
name|efficiencyThreshold
argument_list|,
name|checkEfficiencyNumBatches
argument_list|,
name|maxNumberOfPartitions
argument_list|)
decl_stmt|;
name|copyDown
argument_list|(
name|reduceSinkOperator
argument_list|,
name|topNKeyDesc
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|static
name|TopNKeyOperator
name|copyDown
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|child
parameter_list|,
name|OperatorDesc
name|operatorDesc
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parents
init|=
name|child
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
specifier|final
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|newOperator
init|=
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|child
operator|.
name|getCompilationOpContext
argument_list|()
argument_list|,
name|operatorDesc
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|parents
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSchema
argument_list|()
argument_list|)
argument_list|,
name|child
operator|.
name|getParentOperators
argument_list|()
argument_list|)
decl_stmt|;
name|newOperator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
range|:
name|parents
control|)
block|{
name|parent
operator|.
name|removeChild
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
name|child
operator|.
name|getParentOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|child
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|newOperator
argument_list|)
expr_stmt|;
return|return
operator|(
name|TopNKeyOperator
operator|)
name|newOperator
return|;
block|}
block|}
end_class

end_unit

