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
name|LongWritable
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
name|NullWritable
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * This class represents a nullable int column vector.  * This class will be used for operations on all integer types (tinyint, smallint, int, bigint)  * and as such will use a 64-bit long value to hold the biggest possible value.  * During copy-in/copy-out, smaller int types will be converted as needed. This will  * reduce the amount of code that needs to be generated and also will run fast since the  * machine operates with 64-bit words.  *  * The vector[] field is public by design for high-performance access in the inner  * loop of query execution.  */
end_comment

begin_class
specifier|public
class|class
name|LongColumnVector
extends|extends
name|ColumnVector
block|{
specifier|public
name|long
index|[]
name|vector
decl_stmt|;
specifier|private
specifier|final
name|LongWritable
name|writableObj
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|NULL_VALUE
init|=
literal|1
decl_stmt|;
comment|/**    * Use this constructor by default. All column vectors    * should normally be the default size.    */
specifier|public
name|LongColumnVector
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
name|LongColumnVector
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
name|long
index|[
name|len
index|]
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|getWritableObject
parameter_list|(
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|isRepeating
condition|)
block|{
name|index
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|noNulls
operator|&&
name|isNull
index|[
name|index
index|]
condition|)
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
else|else
block|{
name|writableObj
operator|.
name|set
argument_list|(
name|vector
index|[
name|index
index|]
argument_list|)
expr_stmt|;
return|return
name|writableObj
return|;
block|}
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
name|LongColumnVector
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
comment|// automatic conversion to double is done here
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
name|long
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
name|long
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
name|vector
index|[
name|outElementNum
index|]
operator|=
operator|(
operator|(
name|LongColumnVector
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
block|}
end_class

end_unit

