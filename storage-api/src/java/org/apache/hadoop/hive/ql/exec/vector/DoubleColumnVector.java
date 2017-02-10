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
comment|/**  * This class represents a nullable double precision floating point column vector.  * This class will be used for operations on all floating point types (float, double)  * and as such will use a 64-bit double value to hold the biggest possible value.  * During copy-in/copy-out, smaller types (i.e. float) will be converted as needed. This will  * reduce the amount of code that needs to be generated and also will run fast since the  * machine operates with 64-bit words.  *  * The vector[] field is public by design for high-performance access in the inner  * loop of query execution.  */
end_comment

begin_class
specifier|public
class|class
name|DoubleColumnVector
extends|extends
name|ColumnVector
block|{
specifier|public
name|double
index|[]
name|vector
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|double
name|NULL_VALUE
init|=
name|Double
operator|.
name|NaN
decl_stmt|;
comment|/**    * Use this constructor by default. All column vectors    * should normally be the default size.    */
specifier|public
name|DoubleColumnVector
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
comment|/**    * Don't use this except for testing purposes.    *    * @param len    */
specifier|public
name|DoubleColumnVector
parameter_list|(
name|int
name|len
parameter_list|)
block|{
name|super
argument_list|(
name|len
argument_list|)
expr_stmt|;
name|vector
operator|=
operator|new
name|double
index|[
name|len
index|]
expr_stmt|;
block|}
comment|// Copy the current object contents into the output. Only copy selected entries,
comment|// as indicated by selectedInUse and the sel array.
specifier|public
name|void
name|copySelected
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
parameter_list|,
name|DoubleColumnVector
name|output
parameter_list|)
block|{
comment|// Output has nulls if and only if input has nulls.
name|output
operator|.
name|noNulls
operator|=
name|noNulls
expr_stmt|;
name|output
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
comment|// Handle repeating case
if|if
condition|(
name|isRepeating
condition|)
block|{
name|output
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|vector
index|[
literal|0
index|]
expr_stmt|;
name|output
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
name|isNull
index|[
literal|0
index|]
expr_stmt|;
name|output
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
return|return;
block|}
comment|// Handle normal case
comment|// Copy data values over
if|if
condition|(
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
name|size
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
name|output
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|vector
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|vector
argument_list|,
literal|0
argument_list|,
name|output
operator|.
name|vector
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
comment|// Copy nulls over if needed
if|if
condition|(
operator|!
name|noNulls
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
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size
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
name|output
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|isNull
argument_list|,
literal|0
argument_list|,
name|output
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Fill the column vector with the provided value
specifier|public
name|void
name|fill
parameter_list|(
name|double
name|value
parameter_list|)
block|{
name|noNulls
operator|=
literal|true
expr_stmt|;
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|vector
index|[
literal|0
index|]
operator|=
name|value
expr_stmt|;
block|}
comment|// Fill the column vector with nulls
specifier|public
name|void
name|fillWithNulls
parameter_list|()
block|{
name|noNulls
operator|=
literal|false
expr_stmt|;
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|vector
index|[
literal|0
index|]
operator|=
name|NULL_VALUE
expr_stmt|;
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
comment|// Simplify vector by brute-force flattening noNulls and isRepeating
comment|// This can be used to reduce combinatorial explosion of code paths in VectorExpressions
comment|// with many arguments.
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
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|double
name|repeatVal
init|=
name|vector
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
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
name|size
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
name|vector
index|[
name|i
index|]
operator|=
name|repeatVal
expr_stmt|;
block|}
block|}
else|else
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|vector
argument_list|,
literal|0
argument_list|,
name|size
argument_list|,
name|repeatVal
argument_list|)
expr_stmt|;
block|}
name|flattenRepeatingNulls
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
name|vector
index|[
name|outElementNum
index|]
operator|=
operator|(
operator|(
name|DoubleColumnVector
operator|)
name|inputVector
operator|)
operator|.
name|vector
index|[
name|inputElementNum
index|]
expr_stmt|;
block|}
else|else
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
name|vector
index|[
name|row
index|]
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
name|size
operator|>
name|vector
operator|.
name|length
condition|)
block|{
name|double
index|[]
name|oldArray
init|=
name|vector
decl_stmt|;
name|vector
operator|=
operator|new
name|double
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
name|vector
index|[
literal|0
index|]
operator|=
name|oldArray
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
name|oldArray
argument_list|,
literal|0
argument_list|,
name|vector
argument_list|,
literal|0
argument_list|,
name|oldArray
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|DoubleColumnVector
name|other
init|=
operator|(
name|DoubleColumnVector
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
name|vector
operator|=
name|vector
expr_stmt|;
block|}
block|}
end_class

end_unit

