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
operator|.
name|util
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
comment|/**  * VectorizedRowBatch test source from individual column values (as RLE)  * Used in unit test only.  */
end_comment

begin_class
specifier|public
class|class
name|FakeVectorRowBatchFromRepeats
extends|extends
name|FakeVectorRowBatchBase
block|{
specifier|private
name|Long
index|[]
name|values
decl_stmt|;
specifier|private
name|int
name|count
decl_stmt|;
specifier|private
name|int
name|batchSize
decl_stmt|;
specifier|private
name|VectorizedRowBatch
name|vrg
decl_stmt|;
specifier|private
specifier|final
name|int
name|numCols
decl_stmt|;
specifier|public
name|FakeVectorRowBatchFromRepeats
parameter_list|(
name|Long
index|[]
name|values
parameter_list|,
name|int
name|count
parameter_list|,
name|int
name|batchSize
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
name|this
operator|.
name|batchSize
operator|=
name|batchSize
expr_stmt|;
name|this
operator|.
name|numCols
operator|=
name|values
operator|.
name|length
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
if|if
condition|(
name|count
operator|>
literal|0
condition|)
block|{
name|vrg
operator|.
name|size
operator|=
name|batchSize
operator|<
name|count
condition|?
name|batchSize
else|:
name|count
expr_stmt|;
name|count
operator|-=
name|vrg
operator|.
name|size
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
name|col
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|Long
name|value
init|=
name|values
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|col
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|col
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|col
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|col
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|value
expr_stmt|;
block|}
block|}
block|}
return|return
name|vrg
return|;
block|}
block|}
end_class

end_unit

