begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_class
specifier|public
class|class
name|IfExprNullColumn
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
specifier|final
name|int
name|arg1Column
decl_stmt|;
specifier|private
specifier|final
name|int
name|arg2Column
decl_stmt|;
specifier|private
specifier|final
name|int
name|outputColumn
decl_stmt|;
specifier|public
name|IfExprNullColumn
parameter_list|(
name|int
name|arg1Column
parameter_list|,
name|int
name|arg2Column
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|this
operator|.
name|arg1Column
operator|=
name|arg1Column
expr_stmt|;
name|this
operator|.
name|arg2Column
operator|=
name|arg2Column
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
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
specifier|final
name|LongColumnVector
name|arg1ColVector
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
specifier|final
name|ColumnVector
name|arg2ColVector
init|=
name|batch
operator|.
name|cols
index|[
name|arg2Column
index|]
decl_stmt|;
specifier|final
name|ColumnVector
name|outputColVector
init|=
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
specifier|final
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
specifier|final
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
specifier|final
name|boolean
index|[]
name|null1
init|=
name|arg1ColVector
operator|.
name|isNull
decl_stmt|;
specifier|final
name|long
index|[]
name|vector1
init|=
name|arg1ColVector
operator|.
name|vector
decl_stmt|;
specifier|final
name|boolean
index|[]
name|isNull
init|=
name|outputColVector
operator|.
name|isNull
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|arg2ColVector
operator|.
name|flatten
argument_list|(
name|batch
operator|.
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|n
argument_list|)
expr_stmt|;
if|if
condition|(
name|arg1ColVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
operator|!
name|null1
index|[
literal|0
index|]
operator|&&
name|vector1
index|[
literal|0
index|]
operator|==
literal|1
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|outputColVector
operator|.
name|setElement
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
name|arg2ColVector
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
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
name|null1
index|[
literal|0
index|]
operator|&&
name|vector1
index|[
name|i
index|]
operator|==
literal|1
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|outputColVector
operator|.
name|setElement
argument_list|(
name|i
argument_list|,
name|i
argument_list|,
name|arg2ColVector
argument_list|)
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
name|null1
index|[
literal|0
index|]
operator|&&
name|vector1
index|[
name|i
index|]
operator|==
literal|1
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|outputColVector
operator|.
name|setElement
argument_list|(
name|i
argument_list|,
name|i
argument_list|,
name|arg2ColVector
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|arg2ColVector
operator|.
name|unFlatten
argument_list|()
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
name|outputColumn
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
return|return
literal|"col "
operator|+
name|arg1Column
operator|+
literal|", null, col "
operator|+
name|arg2Column
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
