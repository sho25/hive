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
package|;
end_package

begin_comment
comment|/**  * The representation of a vectorized column of struct objects.  *  * Each field is represented by a separate inner ColumnVector. Since this  * ColumnVector doesn't own any per row data other that the isNull flag, the  * isRepeating only covers the isNull array.  */
end_comment

begin_class
specifier|public
class|class
name|UnionColumnVector
extends|extends
name|ColumnVector
block|{
specifier|public
name|int
index|[]
name|tags
decl_stmt|;
specifier|public
name|ColumnVector
index|[]
name|fields
decl_stmt|;
specifier|public
name|UnionColumnVector
parameter_list|()
block|{
name|this
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor for UnionColumnVector    *    * @param len Vector length    * @param fields the field column vectors    */
specifier|public
name|UnionColumnVector
parameter_list|(
name|int
name|len
parameter_list|,
name|ColumnVector
modifier|...
name|fields
parameter_list|)
block|{
name|super
argument_list|(
name|len
argument_list|)
expr_stmt|;
name|tags
operator|=
operator|new
name|int
index|[
name|len
index|]
expr_stmt|;
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|flatten
parameter_list|(
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|flattenPush
argument_list|()
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
name|fields
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fields
index|[
name|i
index|]
operator|.
name|flatten
argument_list|(
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
name|flattenNoNulls
argument_list|(
name|selectedInUse
argument_list|,
name|sel
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
if|if
condition|(
name|inputVector
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
name|inputVector
operator|.
name|noNulls
operator|||
operator|!
name|inputVector
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
literal|false
expr_stmt|;
name|UnionColumnVector
name|input
init|=
operator|(
name|UnionColumnVector
operator|)
name|inputVector
decl_stmt|;
name|tags
index|[
name|outElementNum
index|]
operator|=
name|input
operator|.
name|tags
index|[
name|inputElementNum
index|]
expr_stmt|;
name|fields
index|[
name|tags
index|[
name|outElementNum
index|]
index|]
operator|.
name|setElement
argument_list|(
name|outElementNum
argument_list|,
name|inputElementNum
argument_list|,
name|input
operator|.
name|fields
index|[
name|tags
index|[
name|outElementNum
index|]
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|noNulls
operator|=
literal|false
expr_stmt|;
name|isNull
index|[
name|outElementNum
index|]
operator|=
literal|true
expr_stmt|;
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
literal|"{\"tag\": "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|tags
index|[
name|row
index|]
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|", \"value\": "
argument_list|)
expr_stmt|;
name|fields
index|[
name|tags
index|[
name|row
index|]
index|]
operator|.
name|stringifyValue
argument_list|(
name|buffer
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|'}'
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
name|ensureSize
parameter_list|(
name|int
name|size
parameter_list|,
name|boolean
name|preserveData
parameter_list|)
block|{
name|super
operator|.
name|ensureSize
argument_list|(
name|size
argument_list|,
name|preserveData
argument_list|)
expr_stmt|;
if|if
condition|(
name|tags
operator|.
name|length
operator|<
name|size
condition|)
block|{
if|if
condition|(
name|preserveData
condition|)
block|{
name|int
index|[]
name|oldTags
init|=
name|tags
decl_stmt|;
name|tags
operator|=
operator|new
name|int
index|[
name|size
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|oldTags
argument_list|,
literal|0
argument_list|,
name|tags
argument_list|,
literal|0
argument_list|,
name|oldTags
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tags
operator|=
operator|new
name|int
index|[
name|size
index|]
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
operator|<
name|fields
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fields
index|[
name|i
index|]
operator|.
name|ensureSize
argument_list|(
name|size
argument_list|,
name|preserveData
argument_list|)
expr_stmt|;
block|}
block|}
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fields
index|[
name|i
index|]
operator|.
name|reset
argument_list|()
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fields
index|[
name|i
index|]
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fields
index|[
name|i
index|]
operator|.
name|unFlatten
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setRepeating
parameter_list|(
name|boolean
name|isRepeating
parameter_list|)
block|{
name|super
operator|.
name|setRepeating
argument_list|(
name|isRepeating
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
name|fields
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fields
index|[
name|i
index|]
operator|.
name|setRepeating
argument_list|(
name|isRepeating
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

