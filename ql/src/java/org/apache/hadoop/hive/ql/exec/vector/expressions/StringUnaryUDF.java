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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
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
name|udf
operator|.
name|IUDFUnaryString
import|;
end_import

begin_class
specifier|public
class|class
name|StringUnaryUDF
extends|extends
name|VectorExpression
block|{
name|int
name|colNum
decl_stmt|;
name|int
name|outputColumn
decl_stmt|;
name|IUDFUnaryString
name|func
decl_stmt|;
name|Text
name|s
decl_stmt|;
name|StringUnaryUDF
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|outputColumn
parameter_list|,
name|IUDFUnaryString
name|func
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
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
name|this
operator|.
name|func
operator|=
name|func
expr_stmt|;
name|s
operator|=
operator|new
name|Text
argument_list|()
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
name|start
index|[]
init|=
name|inputColVector
operator|.
name|start
decl_stmt|;
name|int
name|length
index|[]
init|=
name|inputColVector
operator|.
name|length
decl_stmt|;
name|BytesColumnVector
name|outV
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
name|byte
index|[]
index|[]
name|outputVector
init|=
name|outV
operator|.
name|vector
decl_stmt|;
name|Text
name|t
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
comment|//Nothing to do
return|return;
block|}
comment|// Design Note: In the future, if this function can be implemented
comment|// directly to translate input to output without creating new
comment|// objects, performance can probably be improved significantly.
comment|// It's implemented in the simplest way now, just calling the
comment|// existing built-in function.
if|if
condition|(
name|inputColVector
operator|.
name|noNulls
condition|)
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
name|outV
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|s
operator|.
name|set
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
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|outV
operator|.
name|setRef
argument_list|(
literal|0
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
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
name|s
operator|.
name|set
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
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|outV
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
name|s
operator|.
name|set
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
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|outV
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Handle case with nulls. Don't do function if the value is null, to save time,
comment|// because calling the function can be expensive.
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
name|outV
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|outV
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
name|inputColVector
operator|.
name|isNull
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|s
operator|.
name|set
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
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|outV
operator|.
name|setRef
argument_list|(
literal|0
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
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
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|s
operator|.
name|set
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
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|outV
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
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
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|s
operator|.
name|set
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
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|outV
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
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
name|outputColumn
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
literal|"String"
return|;
block|}
block|}
end_class

end_unit

