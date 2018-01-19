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
name|ColumnVector
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
name|ListColumnVector
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

begin_comment
comment|/**  * Vectorized instruction to get an element from a list with a scalar index and put  * the result in an output column.  */
end_comment

begin_class
specifier|public
class|class
name|ListIndexColScalar
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
name|listColumnNum
decl_stmt|;
specifier|private
name|int
name|index
decl_stmt|;
specifier|public
name|ListIndexColScalar
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ListIndexColScalar
parameter_list|(
name|int
name|listColumn
parameter_list|,
name|int
name|index
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
name|listColumnNum
operator|=
name|listColumn
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
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
name|ColumnVector
name|outV
init|=
name|batch
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|ListColumnVector
name|listV
init|=
operator|(
name|ListColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|listColumnNum
index|]
decl_stmt|;
name|ColumnVector
name|childV
init|=
name|listV
operator|.
name|child
decl_stmt|;
name|outV
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|listV
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|listV
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|outV
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|index
operator|>=
name|listV
operator|.
name|lengths
index|[
literal|0
index|]
condition|)
block|{
name|outV
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|outV
operator|.
name|setElement
argument_list|(
literal|0
argument_list|,
call|(
name|int
call|)
argument_list|(
name|listV
operator|.
name|offsets
index|[
literal|0
index|]
operator|+
name|index
argument_list|)
argument_list|,
name|childV
argument_list|)
expr_stmt|;
name|outV
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|true
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
operator|<
name|batch
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
name|int
name|j
init|=
operator|(
name|batch
operator|.
name|selectedInUse
operator|)
condition|?
name|batch
operator|.
name|selected
index|[
name|i
index|]
else|:
name|i
decl_stmt|;
if|if
condition|(
name|listV
operator|.
name|isNull
index|[
name|j
index|]
operator|||
name|index
operator|>=
name|listV
operator|.
name|lengths
index|[
name|j
index|]
condition|)
block|{
name|outV
operator|.
name|isNull
index|[
name|j
index|]
operator|=
literal|true
expr_stmt|;
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|outV
operator|.
name|setElement
argument_list|(
name|j
argument_list|,
call|(
name|int
call|)
argument_list|(
name|listV
operator|.
name|offsets
index|[
name|j
index|]
operator|+
name|index
argument_list|)
argument_list|,
name|childV
argument_list|)
expr_stmt|;
name|outV
operator|.
name|isNull
index|[
name|j
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
return|return
name|getColumnParamString
argument_list|(
literal|0
argument_list|,
name|listColumnNum
argument_list|)
operator|+
literal|", "
operator|+
name|getColumnParamString
argument_list|(
literal|1
argument_list|,
name|index
argument_list|)
return|;
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
return|return
operator|(
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
operator|)
operator|.
name|setMode
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|Mode
operator|.
name|PROJECTION
argument_list|)
operator|.
name|setNumArguments
argument_list|(
literal|2
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|LIST
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|INT_FAMILY
argument_list|)
operator|.
name|setInputExpressionTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|COLUMN
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|SCALAR
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

