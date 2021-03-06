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
name|VectorExpressionDescriptor
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * Base class that supports conditional execution of the THEN/ELSE vector expressions of  * a SQL IF statement.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|IfExprCondExprBase
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
specifier|protected
specifier|final
name|int
name|arg1Column
decl_stmt|;
comment|// Whether the IF statement boolean expression was repeating.
specifier|protected
specifier|transient
name|boolean
name|isIfStatementResultRepeated
decl_stmt|;
specifier|protected
specifier|transient
name|boolean
name|isIfStatementResultThen
decl_stmt|;
comment|// The batchIndex for the rows that are for the THEN/ELSE rows respectively.
comment|// Temporary work arrays.
specifier|protected
specifier|transient
name|int
name|thenSelectedCount
decl_stmt|;
specifier|protected
specifier|transient
name|int
index|[]
name|thenSelected
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|elseSelectedCount
decl_stmt|;
specifier|protected
specifier|transient
name|int
index|[]
name|elseSelected
decl_stmt|;
specifier|public
name|IfExprCondExprBase
parameter_list|(
name|int
name|arg1Column
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|outputColumnNum
argument_list|)
expr_stmt|;
name|this
operator|.
name|arg1Column
operator|=
name|arg1Column
expr_stmt|;
block|}
specifier|public
name|IfExprCondExprBase
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|arg1Column
operator|=
operator|-
literal|1
expr_stmt|;
block|}
specifier|public
name|void
name|conditionalEvaluate
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|VectorExpression
name|condVecExpr
parameter_list|,
name|int
index|[]
name|condSelected
parameter_list|,
name|int
name|condSize
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|saveSize
init|=
name|batch
operator|.
name|size
decl_stmt|;
name|boolean
name|saveSelectedInUse
init|=
name|batch
operator|.
name|selectedInUse
decl_stmt|;
name|int
index|[]
name|saveSelected
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|batch
operator|.
name|size
operator|=
name|condSize
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|selected
operator|=
name|condSelected
expr_stmt|;
name|condVecExpr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|batch
operator|.
name|size
operator|=
name|saveSize
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
name|saveSelectedInUse
expr_stmt|;
name|batch
operator|.
name|selected
operator|=
name|saveSelected
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
throws|throws
name|HiveException
block|{
comment|// NOTE: We do conditional vector expression so we do not call super.evaluateChildren(batch).
name|thenSelectedCount
operator|=
literal|0
expr_stmt|;
name|elseSelectedCount
operator|=
literal|0
expr_stmt|;
name|isIfStatementResultRepeated
operator|=
literal|false
expr_stmt|;
name|isIfStatementResultThen
operator|=
literal|false
expr_stmt|;
comment|// Give it a value.
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
comment|// Nothing to do
return|return;
block|}
comment|// Child #1 is the IF boolean expression.
name|childExpressions
index|[
literal|0
index|]
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|LongColumnVector
name|ifExprColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|arg1Column
index|]
decl_stmt|;
if|if
condition|(
name|ifExprColVector
operator|.
name|isRepeating
condition|)
block|{
name|isIfStatementResultRepeated
operator|=
literal|true
expr_stmt|;
name|isIfStatementResultThen
operator|=
operator|(
operator|(
name|ifExprColVector
operator|.
name|noNulls
operator|||
operator|!
name|ifExprColVector
operator|.
name|isNull
index|[
literal|0
index|]
operator|)
operator|&&
name|ifExprColVector
operator|.
name|vector
index|[
literal|0
index|]
operator|==
literal|1
operator|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|thenSelected
operator|==
literal|null
operator|||
name|n
operator|>
name|thenSelected
operator|.
name|length
condition|)
block|{
comment|// (Re)allocate larger to be a multiple of 1024 (DEFAULT_SIZE).
specifier|final
name|int
name|roundUpSize
init|=
operator|(
operator|(
name|n
operator|+
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
operator|-
literal|1
operator|)
operator|/
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
operator|)
operator|*
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
decl_stmt|;
name|thenSelected
operator|=
operator|new
name|int
index|[
name|roundUpSize
index|]
expr_stmt|;
name|elseSelected
operator|=
operator|new
name|int
index|[
name|roundUpSize
index|]
expr_stmt|;
block|}
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|long
index|[]
name|vector
init|=
name|ifExprColVector
operator|.
name|vector
decl_stmt|;
if|if
condition|(
name|ifExprColVector
operator|.
name|noNulls
condition|)
block|{
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
specifier|final
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
name|vector
index|[
name|i
index|]
operator|==
literal|1
condition|)
block|{
name|thenSelected
index|[
name|thenSelectedCount
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
else|else
block|{
name|elseSelected
index|[
name|elseSelectedCount
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
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
if|if
condition|(
name|vector
index|[
name|i
index|]
operator|==
literal|1
condition|)
block|{
name|thenSelected
index|[
name|thenSelectedCount
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
else|else
block|{
name|elseSelected
index|[
name|elseSelectedCount
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
name|boolean
index|[]
name|isNull
init|=
name|ifExprColVector
operator|.
name|isNull
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
specifier|final
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
name|isNull
index|[
name|i
index|]
operator|&&
name|vector
index|[
name|i
index|]
operator|==
literal|1
condition|)
block|{
name|thenSelected
index|[
name|thenSelectedCount
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
else|else
block|{
name|elseSelected
index|[
name|elseSelectedCount
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
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
if|if
condition|(
operator|!
name|isNull
index|[
name|i
index|]
operator|&&
name|vector
index|[
name|i
index|]
operator|==
literal|1
condition|)
block|{
name|thenSelected
index|[
name|thenSelectedCount
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
else|else
block|{
name|elseSelected
index|[
name|elseSelectedCount
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|thenSelectedCount
operator|==
literal|0
condition|)
block|{
name|isIfStatementResultRepeated
operator|=
literal|true
expr_stmt|;
name|isIfStatementResultThen
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|elseSelectedCount
operator|==
literal|0
condition|)
block|{
name|isIfStatementResultRepeated
operator|=
literal|true
expr_stmt|;
name|isIfStatementResultThen
operator|=
literal|true
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|VectorExpressionDescriptor
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
comment|// Descriptor is not defined because it takes variable number of arguments with different
comment|// data types.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Undefined descriptor"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

