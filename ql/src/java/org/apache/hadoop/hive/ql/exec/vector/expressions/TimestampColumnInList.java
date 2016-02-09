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
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|PisaTimestamp
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
name|TimestampColumnVector
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
name|VectorizedRowBatch
import|;
end_import

begin_comment
comment|/**  * Output a boolean value indicating if a column is IN a list of constants.  */
end_comment

begin_class
specifier|public
class|class
name|TimestampColumnInList
extends|extends
name|VectorExpression
implements|implements
name|ITimestampInExpr
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
name|inputCol
decl_stmt|;
specifier|private
name|Timestamp
index|[]
name|inListValues
decl_stmt|;
specifier|private
name|int
name|outputColumn
decl_stmt|;
specifier|private
specifier|transient
name|PisaTimestamp
name|scratchTimestamp
decl_stmt|;
comment|// The set object containing the IN list.
specifier|private
specifier|transient
name|HashSet
argument_list|<
name|PisaTimestamp
argument_list|>
name|inSet
decl_stmt|;
specifier|public
name|TimestampColumnInList
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|inSet
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * After construction you must call setInListValues() to add the values to the IN set.    */
specifier|public
name|TimestampColumnInList
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|this
operator|.
name|inputCol
operator|=
name|colNum
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
name|inSet
operator|=
literal|null
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
if|if
condition|(
name|inSet
operator|==
literal|null
condition|)
block|{
name|inSet
operator|=
operator|new
name|HashSet
argument_list|<
name|PisaTimestamp
argument_list|>
argument_list|(
name|inListValues
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Timestamp
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
name|PisaTimestamp
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|scratchTimestamp
operator|=
operator|new
name|PisaTimestamp
argument_list|()
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
name|inputCol
index|]
decl_stmt|;
name|LongColumnVector
name|outputColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumn
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
name|boolean
index|[]
name|outNulls
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
name|long
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
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
name|inputColVector
operator|.
name|noNulls
expr_stmt|;
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
name|inputColVector
operator|.
name|pisaTimestampUpdate
argument_list|(
name|scratchTimestamp
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|outputVector
index|[
literal|0
index|]
operator|=
name|inSet
operator|.
name|contains
argument_list|(
name|scratchTimestamp
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
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
name|inputColVector
operator|.
name|pisaTimestampUpdate
argument_list|(
name|scratchTimestamp
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|inSet
operator|.
name|contains
argument_list|(
name|scratchTimestamp
argument_list|)
condition|?
literal|1
else|:
literal|0
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
name|inputColVector
operator|.
name|pisaTimestampUpdate
argument_list|(
name|scratchTimestamp
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|inSet
operator|.
name|contains
argument_list|(
name|scratchTimestamp
argument_list|)
condition|?
literal|1
else|:
literal|0
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
name|inputColVector
operator|.
name|pisaTimestampUpdate
argument_list|(
name|scratchTimestamp
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|outputVector
index|[
literal|0
index|]
operator|=
name|inSet
operator|.
name|contains
argument_list|(
name|scratchTimestamp
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
name|outNulls
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|outNulls
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
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
name|outNulls
index|[
name|i
index|]
operator|=
name|nullPos
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|nullPos
index|[
name|i
index|]
condition|)
block|{
name|inputColVector
operator|.
name|pisaTimestampUpdate
argument_list|(
name|scratchTimestamp
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|inSet
operator|.
name|contains
argument_list|(
name|scratchTimestamp
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|nullPos
argument_list|,
literal|0
argument_list|,
name|outNulls
argument_list|,
literal|0
argument_list|,
name|n
argument_list|)
expr_stmt|;
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
name|inputColVector
operator|.
name|pisaTimestampUpdate
argument_list|(
name|scratchTimestamp
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|inSet
operator|.
name|contains
argument_list|(
name|scratchTimestamp
argument_list|)
condition|?
literal|1
else|:
literal|0
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
name|getOutputType
parameter_list|()
block|{
return|return
literal|"boolean"
return|;
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
name|Timestamp
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
block|}
end_class

end_unit

