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
name|Writable
import|;
end_import

begin_comment
comment|/**  * ColumnVector contains the shared structure for the sub-types,  * including NULL information, and whether this vector  * repeats, i.e. has all values the same, so only the first  * one is set. This is used to accelerate query performance  * by handling a whole vector in O(1) time when applicable.  *  * The fields are public by design since this is a performance-critical  * structure that is used in the inner loop of query execution.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ColumnVector
block|{
comment|/*    * The current kinds of column vectors.    */
specifier|public
specifier|static
enum|enum
name|Type
block|{
name|LONG
block|,
name|DOUBLE
block|,
name|BYTES
block|,
name|DECIMAL
block|}
comment|/*    * If hasNulls is true, then this array contains true if the value    * is null, otherwise false. The array is always allocated, so a batch can be re-used    * later and nulls added.    */
specifier|public
name|boolean
index|[]
name|isNull
decl_stmt|;
comment|// If the whole column vector has no nulls, this is true, otherwise false.
specifier|public
name|boolean
name|noNulls
decl_stmt|;
comment|/*    * True if same value repeats for whole column vector.    * If so, vector[0] holds the repeating value.    */
specifier|public
name|boolean
name|isRepeating
decl_stmt|;
comment|// Variables to hold state from before flattening so it can be easily restored.
specifier|private
name|boolean
name|preFlattenIsRepeating
decl_stmt|;
specifier|private
name|boolean
name|preFlattenNoNulls
decl_stmt|;
specifier|public
specifier|abstract
name|Writable
name|getWritableObject
parameter_list|(
name|int
name|index
parameter_list|)
function_decl|;
comment|/**    * Constructor for super-class ColumnVector. This is not called directly,    * but used to initialize inherited fields.    *    * @param len Vector length    */
specifier|public
name|ColumnVector
parameter_list|(
name|int
name|len
parameter_list|)
block|{
name|isNull
operator|=
operator|new
name|boolean
index|[
name|len
index|]
expr_stmt|;
name|noNulls
operator|=
literal|true
expr_stmt|;
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
comment|/**      * Resets the column to default state      *  - fills the isNull array with false      *  - sets noNulls to true      *  - sets isRepeating to false      */
specifier|public
name|void
name|reset
parameter_list|()
block|{
if|if
condition|(
literal|false
operator|==
name|noNulls
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|isNull
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|noNulls
operator|=
literal|true
expr_stmt|;
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
specifier|abstract
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
function_decl|;
comment|// Simplify vector by brute-force flattening noNulls if isRepeating
comment|// This can be used to reduce combinatorial explosion of code paths in VectorExpressions
comment|// with many arguments.
specifier|public
name|void
name|flattenRepeatingNulls
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
name|boolean
name|nullFillValue
decl_stmt|;
if|if
condition|(
name|noNulls
condition|)
block|{
name|nullFillValue
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|nullFillValue
operator|=
name|isNull
index|[
literal|0
index|]
expr_stmt|;
block|}
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
name|isNull
index|[
name|i
index|]
operator|=
name|nullFillValue
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
name|nullFillValue
argument_list|)
expr_stmt|;
block|}
comment|// all nulls are now explicit
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|flattenNoNulls
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
if|if
condition|(
name|noNulls
condition|)
block|{
name|noNulls
operator|=
literal|false
expr_stmt|;
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
name|isNull
index|[
name|i
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
block|}
block|}
comment|/**      * Restore the state of isRepeating and noNulls to what it was      * before flattening. This must only be called just after flattening      * and then evaluating a VectorExpression on the column vector.      * It is an optimization that allows other operations on the same      * column to continue to benefit from the isRepeating and noNulls      * indicators.      */
specifier|public
name|void
name|unFlatten
parameter_list|()
block|{
name|isRepeating
operator|=
name|preFlattenIsRepeating
expr_stmt|;
name|noNulls
operator|=
name|preFlattenNoNulls
expr_stmt|;
block|}
comment|// Record repeating and no nulls state to be restored later.
specifier|protected
name|void
name|flattenPush
parameter_list|()
block|{
name|preFlattenIsRepeating
operator|=
name|isRepeating
expr_stmt|;
name|preFlattenNoNulls
operator|=
name|noNulls
expr_stmt|;
block|}
comment|/**      * Set the element in this column vector from the given input vector.      */
specifier|public
specifier|abstract
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
function_decl|;
comment|/**      * Initialize the column vector. This method can be overridden by specific column vector types.      * Use this method only if the individual type of the column vector is not known, otherwise its      * preferable to call specific initialization methods.      */
specifier|public
name|void
name|init
parameter_list|()
block|{
comment|// Do nothing by default
block|}
comment|/**      * Print the value for this column into the given string builder.      * @param buffer the buffer to print into      * @param row the id of the row to print      */
specifier|public
specifier|abstract
name|void
name|stringifyValue
parameter_list|(
name|StringBuilder
name|buffer
parameter_list|,
name|int
name|row
parameter_list|)
function_decl|;
block|}
end_class

end_unit

