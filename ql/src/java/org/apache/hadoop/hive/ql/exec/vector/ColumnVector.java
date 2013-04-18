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
comment|/**  * ColumnVector contains the shared structure for the sub-types,  * including NULL information, and whether this vector  * repeats, i.e. has all values the same, so only the first  * one is set. This is used to accelerate query performance  * by handling a whole vector in O(1) time when applicable.  *   * The fields are public by design since this is a performance-critical  * structure that is used in the inner loop of query execution.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ColumnVector
block|{
comment|/*    * If hasNulls is true, then this array contains true if the value     * is null, otherwise false. The array is always allocated, so a batch can be re-used     * later and nulls added.    */
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
comment|/*     * True if same value repeats for whole column vector.     * If so, vector[0] holds the repeating value.    */
specifier|public
name|boolean
name|isRepeating
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
comment|/**    * Constructor for super-class ColumnVector. This is not called directly,    * but used to initialize inherited fields.    *     * @param len Vector length    */
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
block|}
end_class

end_unit

