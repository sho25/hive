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
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|DecimalColumnVector
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
name|plan
operator|.
name|ptf
operator|.
name|WindowFrameDef
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
name|serde2
operator|.
name|io
operator|.
name|HiveDecimalWritable
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
comment|/**  * This class evaluates HiveDecimal max() for a PTF group.  */
end_comment

begin_class
specifier|public
class|class
name|VectorPTFEvaluatorDecimalMax
extends|extends
name|VectorPTFEvaluatorBase
block|{
specifier|protected
name|boolean
name|isGroupResultNull
decl_stmt|;
specifier|protected
name|HiveDecimalWritable
name|max
decl_stmt|;
specifier|public
name|VectorPTFEvaluatorDecimalMax
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
name|max
operator|=
operator|new
name|HiveDecimalWritable
argument_list|()
expr_stmt|;
name|resetEvaluator
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluateGroupBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
name|evaluateInputExpr
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// Determine maximum of all non-null decimal column values; maintain isGroupResultNull.
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
name|DecimalColumnVector
name|decimalColVector
init|=
operator|(
operator|(
name|DecimalColumnVector
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
name|decimalColVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|decimalColVector
operator|.
name|noNulls
operator|||
operator|!
name|decimalColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
if|if
condition|(
name|isGroupResultNull
condition|)
block|{
name|max
operator|.
name|set
argument_list|(
name|decimalColVector
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|isGroupResultNull
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|HiveDecimalWritable
name|repeatedMax
init|=
name|decimalColVector
operator|.
name|vector
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|repeatedMax
operator|.
name|compareTo
argument_list|(
name|max
argument_list|)
operator|==
literal|1
condition|)
block|{
name|max
operator|.
name|set
argument_list|(
name|repeatedMax
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|decimalColVector
operator|.
name|noNulls
condition|)
block|{
name|HiveDecimalWritable
index|[]
name|vector
init|=
name|decimalColVector
operator|.
name|vector
decl_stmt|;
if|if
condition|(
name|isGroupResultNull
condition|)
block|{
name|max
operator|.
name|set
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|isGroupResultNull
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|HiveDecimalWritable
name|dec
init|=
name|vector
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|dec
operator|.
name|compareTo
argument_list|(
name|max
argument_list|)
operator|==
literal|1
condition|)
block|{
name|max
operator|.
name|set
argument_list|(
name|dec
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|HiveDecimalWritable
name|dec
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|dec
operator|.
name|compareTo
argument_list|(
name|max
argument_list|)
operator|==
literal|1
condition|)
block|{
name|max
operator|.
name|set
argument_list|(
name|dec
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|boolean
index|[]
name|batchIsNull
init|=
name|decimalColVector
operator|.
name|isNull
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|batchIsNull
index|[
name|i
index|]
condition|)
block|{
if|if
condition|(
operator|++
name|i
operator|>=
name|size
condition|)
block|{
return|return;
block|}
block|}
name|HiveDecimalWritable
index|[]
name|vector
init|=
name|decimalColVector
operator|.
name|vector
decl_stmt|;
specifier|final
name|HiveDecimalWritable
name|firstValue
init|=
name|vector
index|[
name|i
operator|++
index|]
decl_stmt|;
if|if
condition|(
name|isGroupResultNull
condition|)
block|{
name|max
operator|.
name|set
argument_list|(
name|firstValue
argument_list|)
expr_stmt|;
name|isGroupResultNull
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|firstValue
operator|.
name|compareTo
argument_list|(
name|max
argument_list|)
operator|==
literal|1
condition|)
block|{
name|max
operator|.
name|set
argument_list|(
name|firstValue
argument_list|)
expr_stmt|;
block|}
for|for
control|(
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|batchIsNull
index|[
name|i
index|]
condition|)
block|{
specifier|final
name|HiveDecimalWritable
name|dec
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|dec
operator|.
name|compareTo
argument_list|(
name|max
argument_list|)
operator|==
literal|1
condition|)
block|{
name|max
operator|.
name|set
argument_list|(
name|dec
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|streamsResult
parameter_list|()
block|{
comment|// We must evaluate whole group before producing a result.
return|return
literal|false
return|;
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
name|DECIMAL
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveDecimalWritable
name|getDecimalGroupResult
parameter_list|()
block|{
return|return
name|max
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
name|max
operator|.
name|setFromLong
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

