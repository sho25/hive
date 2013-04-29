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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|ColumnVector
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
comment|/**  * VectorizedRowBatch test source from individual column values (as iterables)  * Used in unit test only.  */
end_comment

begin_class
specifier|public
class|class
name|FakeVectorRowBatchFromIterables
extends|extends
name|FakeVectorRowBatchBase
block|{
specifier|private
name|VectorizedRowBatch
name|vrg
decl_stmt|;
specifier|private
specifier|final
name|int
name|numCols
decl_stmt|;
specifier|private
specifier|final
name|int
name|batchSize
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Iterator
argument_list|<
name|Long
argument_list|>
argument_list|>
name|iterators
decl_stmt|;
specifier|private
name|boolean
name|eof
decl_stmt|;
specifier|public
name|FakeVectorRowBatchFromIterables
parameter_list|(
name|int
name|batchSize
parameter_list|,
name|Iterable
argument_list|<
name|Long
argument_list|>
modifier|...
name|iterables
parameter_list|)
block|{
name|numCols
operator|=
name|iterables
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|batchSize
operator|=
name|batchSize
expr_stmt|;
name|iterators
operator|=
operator|new
name|ArrayList
argument_list|<
name|Iterator
argument_list|<
name|Long
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|vrg
operator|=
operator|new
name|VectorizedRowBatch
argument_list|(
name|numCols
argument_list|,
name|batchSize
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
name|numCols
condition|;
name|i
operator|++
control|)
block|{
name|vrg
operator|.
name|cols
index|[
name|i
index|]
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|iterators
operator|.
name|add
argument_list|(
name|iterables
index|[
name|i
index|]
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|VectorizedRowBatch
name|produceNextBatch
parameter_list|()
block|{
name|vrg
operator|.
name|size
operator|=
literal|0
expr_stmt|;
name|vrg
operator|.
name|selectedInUse
operator|=
literal|false
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
name|numCols
condition|;
operator|++
name|i
control|)
block|{
name|ColumnVector
name|col
init|=
name|vrg
operator|.
name|cols
index|[
name|i
index|]
decl_stmt|;
name|col
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|col
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
while|while
condition|(
operator|!
name|eof
operator|&&
name|vrg
operator|.
name|size
operator|<
name|this
operator|.
name|batchSize
condition|)
block|{
name|int
name|r
init|=
name|vrg
operator|.
name|size
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
name|numCols
condition|;
operator|++
name|i
control|)
block|{
name|Iterator
argument_list|<
name|Long
argument_list|>
name|it
init|=
name|iterators
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|eof
operator|=
literal|true
expr_stmt|;
break|break;
block|}
name|LongColumnVector
name|col
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|i
index|]
decl_stmt|;
name|Long
name|value
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|value
condition|)
block|{
name|col
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|col
operator|.
name|isNull
index|[
name|vrg
operator|.
name|size
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|long
index|[]
name|vector
init|=
name|col
operator|.
name|vector
decl_stmt|;
name|vector
index|[
name|r
index|]
operator|=
name|value
expr_stmt|;
name|col
operator|.
name|isNull
index|[
name|vrg
operator|.
name|size
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|eof
condition|)
block|{
name|vrg
operator|.
name|size
operator|+=
literal|1
expr_stmt|;
block|}
block|}
return|return
name|vrg
return|;
block|}
block|}
end_class

end_unit

