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
name|ql
operator|.
name|CompilationOpContext
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
name|LimitOperator
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
name|LimitDesc
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
name|VectorDesc
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
name|VectorLimitDesc
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Limit operator implementation Limits the number of rows to be passed on.  **/
end_comment

begin_class
specifier|public
class|class
name|VectorLimitOperator
extends|extends
name|LimitOperator
implements|implements
name|VectorizationOperator
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
name|VectorizationContext
name|vContext
decl_stmt|;
specifier|private
name|VectorLimitDesc
name|vectorDesc
decl_stmt|;
comment|/** Kryo ctor. */
annotation|@
name|VisibleForTesting
specifier|public
name|VectorLimitOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorLimitOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
specifier|public
name|VectorLimitOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|,
name|VectorizationContext
name|vContext
parameter_list|,
name|VectorDesc
name|vectorDesc
parameter_list|)
block|{
name|this
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|(
name|LimitDesc
operator|)
name|conf
expr_stmt|;
name|this
operator|.
name|vContext
operator|=
name|vContext
expr_stmt|;
name|this
operator|.
name|vectorDesc
operator|=
operator|(
name|VectorLimitDesc
operator|)
name|vectorDesc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizationContext
name|getInputVectorizationContext
parameter_list|()
block|{
return|return
name|vContext
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
comment|// We should skip number of rows equal to offset value
comment|// skip until sum of current read count and current batch size less than or equal offset value
if|if
condition|(
name|currCount
operator|+
name|batch
operator|.
name|size
operator|<=
name|offset
condition|)
block|{
name|currCount
operator|+=
name|batch
operator|.
name|size
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currCount
operator|>=
name|offset
operator|+
name|limit
condition|)
block|{
name|setDone
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|skipSize
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|currCount
operator|<
name|offset
condition|)
block|{
name|skipSize
operator|=
name|offset
operator|-
name|currCount
expr_stmt|;
block|}
comment|//skip skipSize rows of batch
name|batch
operator|.
name|size
operator|=
name|Math
operator|.
name|min
argument_list|(
name|batch
operator|.
name|size
argument_list|,
name|offset
operator|+
name|limit
operator|-
name|currCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|batch
operator|.
name|selectedInUse
operator|==
literal|false
condition|)
block|{
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|batch
operator|.
name|size
operator|-
name|skipSize
condition|;
name|i
operator|++
control|)
block|{
name|batch
operator|.
name|selected
index|[
name|i
index|]
operator|=
name|skipSize
operator|+
name|i
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|batch
operator|.
name|size
operator|-
name|skipSize
condition|;
name|i
operator|++
control|)
block|{
name|batch
operator|.
name|selected
index|[
name|i
index|]
operator|=
name|batch
operator|.
name|selected
index|[
name|skipSize
operator|+
name|i
index|]
expr_stmt|;
block|}
block|}
name|vectorForward
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|currCount
operator|+=
name|batch
operator|.
name|size
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|VectorDesc
name|getVectorDesc
parameter_list|()
block|{
return|return
name|vectorDesc
return|;
block|}
block|}
end_class

end_unit

