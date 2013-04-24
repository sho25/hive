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
name|exec
operator|.
name|vector
operator|.
name|expressions
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedRowBatch
import|;
end_import

begin_comment
comment|/**  * This class represents an NOT filter expression. This applies short circuit optimization.  */
end_comment

begin_class
specifier|public
class|class
name|FilterNotExpr
extends|extends
name|VectorExpression
block|{
name|VectorExpression
name|childExpr1
decl_stmt|;
name|int
index|[]
name|tmpSelect1
init|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
name|int
index|[]
name|unselected
init|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
name|int
index|[]
name|tmp
init|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|public
name|FilterNotExpr
parameter_list|(
name|VectorExpression
name|childExpr1
parameter_list|)
block|{
name|this
operator|.
name|childExpr1
operator|=
name|childExpr1
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
block|{
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|n
operator|<=
literal|0
condition|)
block|{
return|return;
block|}
comment|//Clone the selected vector
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
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
name|n
condition|;
name|i
operator|++
control|)
block|{
name|tmpSelect1
index|[
name|i
index|]
operator|=
name|sel
index|[
name|i
index|]
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
name|n
condition|;
name|i
operator|++
control|)
block|{
name|tmpSelect1
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
name|sel
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
block|}
name|childExpr1
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|//Calculate unselected ones in last evaluate.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tmp
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|tmp
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|batch
operator|.
name|size
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|tmp
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
block|}
name|int
name|unselectedSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|tmpSelect1
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
name|tmp
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
name|unselected
index|[
name|unselectedSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
comment|//The unselected is the new selected
name|batch
operator|.
name|selected
operator|=
name|unselected
expr_stmt|;
name|batch
operator|.
name|size
operator|=
name|unselectedSize
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOutputColumn
parameter_list|()
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getOutputType
parameter_list|()
block|{
return|return
literal|"boolean"
return|;
block|}
block|}
end_class

end_unit

