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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * The representation of a vectorized column of multi-valued objects, such  * as lists and maps.  *  * Each object is composed of a range of elements in the underlying child  * ColumnVector. The range for list i is  * offsets[i]..offsets[i]+lengths[i]-1 inclusive.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|MultiValuedColumnVector
extends|extends
name|ColumnVector
block|{
specifier|public
name|long
index|[]
name|offsets
decl_stmt|;
specifier|public
name|long
index|[]
name|lengths
decl_stmt|;
comment|// the number of children slots used
specifier|public
name|int
name|childCount
decl_stmt|;
comment|/**    * Constructor for MultiValuedColumnVector.    *    * @param len Vector length    */
specifier|public
name|MultiValuedColumnVector
parameter_list|(
name|Type
name|type
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|super
argument_list|(
name|type
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|childCount
operator|=
literal|0
expr_stmt|;
name|offsets
operator|=
operator|new
name|long
index|[
name|len
index|]
expr_stmt|;
name|lengths
operator|=
operator|new
name|long
index|[
name|len
index|]
expr_stmt|;
block|}
specifier|protected
specifier|abstract
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
function_decl|;
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
if|if
condition|(
name|isRepeating
condition|)
block|{
if|if
condition|(
name|noNulls
operator|||
operator|!
name|isNull
index|[
literal|0
index|]
condition|)
block|{
if|if
condition|(
name|selectedInUse
condition|)
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
name|size
condition|;
operator|++
name|i
control|)
block|{
name|int
name|row
init|=
name|sel
index|[
name|i
index|]
decl_stmt|;
name|offsets
index|[
name|row
index|]
operator|=
name|offsets
index|[
literal|0
index|]
expr_stmt|;
name|lengths
index|[
name|row
index|]
operator|=
name|lengths
index|[
literal|0
index|]
expr_stmt|;
name|isNull
index|[
name|row
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|offsets
argument_list|,
literal|0
argument_list|,
name|size
argument_list|,
name|offsets
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|lengths
argument_list|,
literal|0
argument_list|,
name|size
argument_list|,
name|lengths
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|isNull
argument_list|,
literal|0
argument_list|,
name|size
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// We optimize by assuming that a repeating list/map will run from
comment|// from 0 .. lengths[0] in the child vector.
comment|// Sanity check the assumption that we can start at 0.
if|if
condition|(
name|offsets
index|[
literal|0
index|]
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Repeating offset isn't 0, but "
operator|+
name|offsets
index|[
literal|0
index|]
argument_list|)
throw|;
block|}
name|childFlatten
argument_list|(
literal|false
argument_list|,
literal|null
argument_list|,
operator|(
name|int
operator|)
name|lengths
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|selectedInUse
condition|)
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
name|size
condition|;
operator|++
name|i
control|)
block|{
name|isNull
index|[
name|sel
index|[
name|i
index|]
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|isNull
argument_list|,
literal|0
argument_list|,
name|size
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|selectedInUse
condition|)
block|{
name|int
name|childSize
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
operator|<
name|size
condition|;
operator|++
name|i
control|)
block|{
name|childSize
operator|+=
name|lengths
index|[
name|sel
index|[
name|i
index|]
index|]
expr_stmt|;
block|}
name|int
index|[]
name|childSelection
init|=
operator|new
name|int
index|[
name|childSize
index|]
decl_stmt|;
name|int
name|idx
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
operator|<
name|size
condition|;
operator|++
name|i
control|)
block|{
name|int
name|row
init|=
name|sel
index|[
name|i
index|]
decl_stmt|;
for|for
control|(
name|int
name|elem
init|=
literal|0
init|;
name|elem
operator|<
name|lengths
index|[
name|row
index|]
condition|;
operator|++
name|elem
control|)
block|{
name|childSelection
index|[
name|idx
operator|++
index|]
operator|=
call|(
name|int
call|)
argument_list|(
name|offsets
index|[
name|row
index|]
operator|+
name|elem
argument_list|)
expr_stmt|;
block|}
block|}
name|childFlatten
argument_list|(
literal|true
argument_list|,
name|childSelection
argument_list|,
name|childSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|childFlatten
argument_list|(
literal|false
argument_list|,
literal|null
argument_list|,
name|childCount
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
name|size
operator|>
name|offsets
operator|.
name|length
condition|)
block|{
name|long
index|[]
name|oldOffsets
init|=
name|offsets
decl_stmt|;
name|offsets
operator|=
operator|new
name|long
index|[
name|size
index|]
expr_stmt|;
name|long
name|oldLengths
index|[]
init|=
name|lengths
decl_stmt|;
name|lengths
operator|=
operator|new
name|long
index|[
name|size
index|]
expr_stmt|;
if|if
condition|(
name|preserveData
condition|)
block|{
if|if
condition|(
name|isRepeating
condition|)
block|{
name|offsets
index|[
literal|0
index|]
operator|=
name|oldOffsets
index|[
literal|0
index|]
expr_stmt|;
name|lengths
index|[
literal|0
index|]
operator|=
name|oldLengths
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|oldOffsets
argument_list|,
literal|0
argument_list|,
name|offsets
argument_list|,
literal|0
argument_list|,
name|oldOffsets
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|oldLengths
argument_list|,
literal|0
argument_list|,
name|lengths
argument_list|,
literal|0
argument_list|,
name|oldLengths
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Initializee the vector    */
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
name|childCount
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Reset the vector for the next batch.    */
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
name|childCount
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shallowCopyTo
parameter_list|(
name|ColumnVector
name|otherCv
parameter_list|)
block|{
name|MultiValuedColumnVector
name|other
init|=
operator|(
name|MultiValuedColumnVector
operator|)
name|otherCv
decl_stmt|;
name|super
operator|.
name|shallowCopyTo
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|other
operator|.
name|offsets
operator|=
name|offsets
expr_stmt|;
name|other
operator|.
name|lengths
operator|=
name|lengths
expr_stmt|;
name|other
operator|.
name|childCount
operator|=
name|childCount
expr_stmt|;
block|}
block|}
end_class

end_unit

