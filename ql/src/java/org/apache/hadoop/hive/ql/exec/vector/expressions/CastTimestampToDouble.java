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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|*
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

begin_class
specifier|public
class|class
name|CastTimestampToDouble
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
name|colNum
decl_stmt|;
specifier|public
name|CastTimestampToDouble
parameter_list|(
name|int
name|colNum
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
name|colNum
operator|=
name|colNum
expr_stmt|;
block|}
specifier|public
name|CastTimestampToDouble
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|colNum
operator|=
operator|-
literal|1
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
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|evaluateChildren
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
name|TimestampColumnVector
name|inputColVector
init|=
operator|(
name|TimestampColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colNum
index|]
decl_stmt|;
name|DoubleColumnVector
name|outputColVector
init|=
operator|(
name|DoubleColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumnNum
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
name|inputIsNull
init|=
name|inputColVector
operator|.
name|isNull
decl_stmt|;
name|boolean
index|[]
name|outputIsNull
init|=
name|outputColVector
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
name|double
index|[]
name|outputVector
init|=
name|outputColVector
operator|.
name|vector
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
comment|// We do not need to do a column reset since we are carefully changing the output.
name|outputColVector
operator|.
name|isRepeating
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
if|if
condition|(
name|inputColVector
operator|.
name|noNulls
operator|||
operator|!
name|inputIsNull
index|[
literal|0
index|]
condition|)
block|{
name|outputIsNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
index|[
literal|0
index|]
operator|=
name|inputColVector
operator|.
name|getDouble
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputIsNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
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
name|batch
operator|.
name|selectedInUse
condition|)
block|{
comment|// CONSIDER: For large n, fill n or all of isNull array and use the tighter ELSE loop.
if|if
condition|(
operator|!
name|outputColVector
operator|.
name|noNulls
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
specifier|final
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
comment|// Set isNull before call in case it changes it mind.
name|outputIsNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|inputColVector
operator|.
name|getDouble
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
else|else
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
specifier|final
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|inputColVector
operator|.
name|getDouble
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|outputColVector
operator|.
name|noNulls
condition|)
block|{
comment|// Assume it is almost always a performance win to fill all of isNull so we can
comment|// safely reset noNulls.
name|Arrays
operator|.
name|fill
argument_list|(
name|outputIsNull
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
block|}
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
name|outputVector
index|[
name|i
index|]
operator|=
name|inputColVector
operator|.
name|getDouble
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
comment|/* there are NULLs in the inputColVector */
block|{
comment|/*        * Do careful maintenance of the outputColVector.noNulls flag.        */
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
name|inputIsNull
index|[
name|i
index|]
condition|)
block|{
name|outputIsNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|inputColVector
operator|.
name|getDouble
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputIsNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
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
name|inputIsNull
index|[
name|i
index|]
condition|)
block|{
name|outputIsNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|inputColVector
operator|.
name|getDouble
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputIsNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
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
name|colNum
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
literal|1
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|getType
argument_list|(
literal|"timestamp"
argument_list|)
argument_list|)
operator|.
name|setInputExpressionTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|COLUMN
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

