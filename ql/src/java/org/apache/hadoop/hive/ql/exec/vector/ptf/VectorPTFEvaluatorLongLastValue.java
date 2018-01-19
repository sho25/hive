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
name|exec
operator|.
name|vector
operator|.
name|ptf
package|;
end_package

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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|LongColumnVector
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
name|vector
operator|.
name|VectorizedRowBatch
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
name|vector
operator|.
name|ColumnVector
operator|.
name|Type
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
name|vector
operator|.
name|expressions
operator|.
name|VectorExpression
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
name|ptf
operator|.
name|WindowFrameDef
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * This class evaluates long first_value() for a PTF group.  *  * We capture the last value from the last batch.  It can be NULL.  * It becomes the group value.  */
end_comment

begin_class
specifier|public
class|class
name|VectorPTFEvaluatorLongLastValue
extends|extends
name|VectorPTFEvaluatorBase
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|VectorPTFEvaluatorLongLastValue
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
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
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|protected
name|boolean
name|isGroupResultNull
decl_stmt|;
specifier|protected
name|long
name|lastValue
decl_stmt|;
specifier|public
name|VectorPTFEvaluatorLongLastValue
parameter_list|(
name|WindowFrameDef
name|windowFrameDef
parameter_list|,
name|VectorExpression
name|inputVecExpr
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|windowFrameDef
argument_list|,
name|inputVecExpr
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
name|resetEvaluator
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|evaluateGroupBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|boolean
name|isLastGroupBatch
parameter_list|)
block|{
name|evaluateInputExpr
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// Last row of last batch determines isGroupResultNull and long lastValue.
comment|// We do not filter when PTF is in reducer.
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|!
name|batch
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isLastGroupBatch
condition|)
block|{
return|return;
block|}
specifier|final
name|int
name|size
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|LongColumnVector
name|longColVector
init|=
operator|(
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|inputColumnNum
index|]
operator|)
decl_stmt|;
if|if
condition|(
name|longColVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|longColVector
operator|.
name|noNulls
condition|)
block|{
name|lastValue
operator|=
name|longColVector
operator|.
name|vector
index|[
literal|0
index|]
expr_stmt|;
name|isGroupResultNull
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|isGroupResultNull
operator|=
literal|true
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|longColVector
operator|.
name|noNulls
condition|)
block|{
name|lastValue
operator|=
name|longColVector
operator|.
name|vector
index|[
name|size
operator|-
literal|1
index|]
expr_stmt|;
name|isGroupResultNull
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|int
name|lastBatchIndex
init|=
name|size
operator|-
literal|1
decl_stmt|;
if|if
condition|(
operator|!
name|longColVector
operator|.
name|isNull
index|[
name|lastBatchIndex
index|]
condition|)
block|{
name|lastValue
operator|=
name|longColVector
operator|.
name|vector
index|[
name|lastBatchIndex
index|]
expr_stmt|;
name|isGroupResultNull
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|isGroupResultNull
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isGroupResultNull
parameter_list|()
block|{
return|return
name|isGroupResultNull
return|;
block|}
annotation|@
name|Override
specifier|public
name|Type
name|getResultColumnVectorType
parameter_list|()
block|{
return|return
name|Type
operator|.
name|LONG
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLongGroupResult
parameter_list|()
block|{
return|return
name|lastValue
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resetEvaluator
parameter_list|()
block|{
name|isGroupResultNull
operator|=
literal|true
expr_stmt|;
name|lastValue
operator|=
literal|0
expr_stmt|;
block|}
block|}
end_class

end_unit

