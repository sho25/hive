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

begin_comment
comment|/**  * The representation of a vectorized column of list objects.  *  * Each list is composed of a range of elements in the underlying child  * ColumnVector. The range for list i is  * offsets[i]..offsets[i]+lengths[i]-1 inclusive.  */
end_comment

begin_class
specifier|public
class|class
name|ListColumnVector
extends|extends
name|MultiValuedColumnVector
block|{
specifier|public
name|ColumnVector
name|child
decl_stmt|;
specifier|public
name|ListColumnVector
parameter_list|()
block|{
name|this
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor for ListColumnVector.    *    * @param len Vector length    * @param child The child vector    */
specifier|public
name|ListColumnVector
parameter_list|(
name|int
name|len
parameter_list|,
name|ColumnVector
name|child
parameter_list|)
block|{
name|super
argument_list|(
name|Type
operator|.
name|LIST
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|this
operator|.
name|child
operator|=
name|child
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|childFlatten
parameter_list|(
name|boolean
name|useSelected
parameter_list|,
name|int
index|[]
name|selected
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|child
operator|.
name|flatten
argument_list|(
name|useSelected
argument_list|,
name|selected
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setElement
parameter_list|(
name|int
name|outElementNum
parameter_list|,
name|int
name|inputElementNum
parameter_list|,
name|ColumnVector
name|inputVector
parameter_list|)
block|{
name|ListColumnVector
name|input
init|=
operator|(
name|ListColumnVector
operator|)
name|inputVector
decl_stmt|;
if|if
condition|(
name|input
operator|.
name|isRepeating
condition|)
block|{
name|inputElementNum
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|input
operator|.
name|noNulls
operator|&&
name|input
operator|.
name|isNull
index|[
name|inputElementNum
index|]
condition|)
block|{
name|isNull
index|[
name|outElementNum
index|]
operator|=
literal|true
expr_stmt|;
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|isNull
index|[
name|outElementNum
index|]
operator|=
literal|false
expr_stmt|;
name|int
name|offset
init|=
name|childCount
decl_stmt|;
name|int
name|length
init|=
operator|(
name|int
operator|)
name|input
operator|.
name|lengths
index|[
name|inputElementNum
index|]
decl_stmt|;
name|int
name|inputOffset
init|=
operator|(
name|int
operator|)
name|input
operator|.
name|offsets
index|[
name|inputElementNum
index|]
decl_stmt|;
name|offsets
index|[
name|outElementNum
index|]
operator|=
name|offset
expr_stmt|;
name|childCount
operator|+=
name|length
expr_stmt|;
name|lengths
index|[
name|outElementNum
index|]
operator|=
name|length
expr_stmt|;
name|child
operator|.
name|ensureSize
argument_list|(
name|childCount
argument_list|,
literal|true
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
operator|<
name|length
condition|;
operator|++
name|i
control|)
block|{
name|child
operator|.
name|setElement
argument_list|(
name|i
operator|+
name|offset
argument_list|,
name|inputOffset
operator|+
name|i
argument_list|,
name|input
operator|.
name|child
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stringifyValue
parameter_list|(
name|StringBuilder
name|buffer
parameter_list|,
name|int
name|row
parameter_list|)
block|{
if|if
condition|(
name|isRepeating
condition|)
block|{
name|row
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|noNulls
operator|||
operator|!
name|isNull
index|[
name|row
index|]
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
expr_stmt|;
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
for|for
control|(
name|long
name|i
init|=
name|offsets
index|[
name|row
index|]
init|;
name|i
operator|<
name|offsets
index|[
name|row
index|]
operator|+
name|lengths
index|[
name|row
index|]
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|isFirst
condition|)
block|{
name|isFirst
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|child
operator|.
name|stringifyValue
argument_list|(
name|buffer
argument_list|,
operator|(
name|int
operator|)
name|i
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
block|{
name|super
operator|.
name|init
argument_list|()
expr_stmt|;
name|child
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|child
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|unFlatten
parameter_list|()
block|{
name|super
operator|.
name|unFlatten
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|isRepeating
operator|||
name|noNulls
operator|||
operator|!
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|child
operator|.
name|unFlatten
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

