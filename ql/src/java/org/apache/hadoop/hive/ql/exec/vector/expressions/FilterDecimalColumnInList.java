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
name|VectorExpressionDescriptor
operator|.
name|Descriptor
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
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_comment
comment|/**  * Evaluate IN filter on a batch for a vector of decimals.  */
end_comment

begin_class
specifier|public
class|class
name|FilterDecimalColumnInList
extends|extends
name|VectorExpression
implements|implements
name|IDecimalInExpr
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
name|inputCol
decl_stmt|;
specifier|private
name|HiveDecimal
index|[]
name|inListValues
decl_stmt|;
comment|// Transient members initialized by transientInit method.
comment|// The set object containing the IN list.
specifier|private
specifier|transient
name|HashSet
argument_list|<
name|HiveDecimalWritable
argument_list|>
name|inSet
decl_stmt|;
specifier|public
name|FilterDecimalColumnInList
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|inputCol
operator|=
operator|-
literal|1
expr_stmt|;
block|}
comment|/**    * After construction you must call setInListValues() to add the values to the IN set.    */
specifier|public
name|FilterDecimalColumnInList
parameter_list|(
name|int
name|colNum
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|inputCol
operator|=
name|colNum
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|transientInit
parameter_list|()
throws|throws
name|HiveException
block|{
name|super
operator|.
name|transientInit
argument_list|()
expr_stmt|;
name|inSet
operator|=
operator|new
name|HashSet
argument_list|<
name|HiveDecimalWritable
argument_list|>
argument_list|(
name|inListValues
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|HiveDecimal
name|val
range|:
name|inListValues
control|)
block|{
name|inSet
operator|.
name|add
argument_list|(
operator|new
name|HiveDecimalWritable
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|DecimalColumnVector
name|inputColVector
init|=
operator|(
name|DecimalColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|inputCol
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
name|HiveDecimalWritable
index|[]
name|vector
init|=
name|inputColVector
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
comment|// All must be selected otherwise size would be zero
comment|// Repeating property will not change.
if|if
condition|(
operator|!
operator|(
name|inSet
operator|.
name|contains
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|)
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
name|inSet
operator|.
name|contains
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
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
name|inSet
operator|.
name|contains
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
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
comment|//All must be selected otherwise size would be zero
comment|//Repeating property will not change.
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
name|inSet
operator|.
name|contains
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|)
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
name|inSet
operator|.
name|contains
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
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
comment|// Change the selected vector
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
name|inSet
operator|.
name|contains
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
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
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
comment|// This VectorExpression (IN) is a special case, so don't return a descriptor.
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|setInListValues
parameter_list|(
name|HiveDecimal
index|[]
name|a
parameter_list|)
block|{
name|this
operator|.
name|inListValues
operator|=
name|a
expr_stmt|;
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
name|inputCol
argument_list|)
operator|+
literal|", values "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|inListValues
argument_list|)
return|;
block|}
block|}
end_class

end_unit

