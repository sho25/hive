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
operator|.
name|gen
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
name|exec
operator|.
name|vector
operator|.
name|expressions
operator|.
name|StringExpr
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
name|BytesColumnVector
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

begin_comment
comment|/**  * This is a generated class to evaluate a< comparison on a vector of strings.  */
end_comment

begin_class
specifier|public
class|class
name|FilterStringColLessStringScalar
extends|extends
name|VectorExpression
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
name|int
name|colNum
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
specifier|public
name|FilterStringColLessStringScalar
parameter_list|(
name|int
name|colNum
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|FilterStringColLessStringScalar
parameter_list|()
block|{   }
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
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
name|super
operator|.
name|evaluateChildren
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
name|BytesColumnVector
name|inputColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colNum
index|]
decl_stmt|;
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|boolean
index|[]
name|nullPos
init|=
name|inputColVector
operator|.
name|isNull
decl_stmt|;
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
name|byte
index|[]
index|[]
name|vector
init|=
name|inputColVector
operator|.
name|vector
decl_stmt|;
name|int
index|[]
name|length
init|=
name|inputColVector
operator|.
name|length
decl_stmt|;
name|int
index|[]
name|start
init|=
name|inputColVector
operator|.
name|start
decl_stmt|;
comment|// return immediately if batch is empty
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|inputColVector
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
comment|// All must be selected otherwise size would be zero. Repeating property will not change.
if|if
condition|(
operator|!
operator|(
name|StringExpr
operator|.
name|compare
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|,
name|start
index|[
literal|0
index|]
argument_list|,
name|length
index|[
literal|0
index|]
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
operator|<
literal|0
operator|)
condition|)
block|{
comment|//Entire batch is filtered out.
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
name|newSize
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
operator|!=
name|n
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
if|if
condition|(
name|StringExpr
operator|.
name|compare
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
operator|<
literal|0
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
block|}
else|else
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|StringExpr
operator|.
name|compare
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
operator|<
literal|0
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newSize
operator|<
name|n
condition|)
block|{
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
comment|// All must be selected otherwise size would be zero. Repeating property will not change.
if|if
condition|(
operator|!
name|nullPos
index|[
literal|0
index|]
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|StringExpr
operator|.
name|compare
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|,
name|start
index|[
literal|0
index|]
argument_list|,
name|length
index|[
literal|0
index|]
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
operator|<
literal|0
operator|)
condition|)
block|{
comment|//Entire batch is filtered out.
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
block|}
else|else
block|{
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
name|newSize
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
operator|!=
name|n
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
if|if
condition|(
operator|!
name|nullPos
index|[
name|i
index|]
condition|)
block|{
if|if
condition|(
name|StringExpr
operator|.
name|compare
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
operator|<
literal|0
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
comment|//Change the selected vector
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
block|}
else|else
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|nullPos
index|[
name|i
index|]
condition|)
block|{
if|if
condition|(
name|StringExpr
operator|.
name|compare
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|,
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
operator|<
literal|0
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|newSize
operator|<
name|n
condition|)
block|{
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
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
specifier|public
name|int
name|getColNum
parameter_list|()
block|{
return|return
name|colNum
return|;
block|}
specifier|public
name|void
name|setColNum
parameter_list|(
name|int
name|colNum
parameter_list|)
block|{
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
specifier|public
name|void
name|setValue
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
block|}
end_class

end_unit

