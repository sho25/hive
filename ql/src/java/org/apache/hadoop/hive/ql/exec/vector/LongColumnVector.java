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
literal|null
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
block|}
end_class

end_unit

