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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_comment
comment|/**  * Unit test for the vectorized conversion to and from row object[].  */
end_comment

begin_class
specifier|public
class|class
name|TestVectorRowObject
extends|extends
name|TestCase
block|{
name|void
name|examineBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|VectorExtractRow
name|vectorExtractRow
parameter_list|,
name|Object
index|[]
index|[]
name|randomRows
parameter_list|,
name|int
name|firstRandomRowIndex
parameter_list|)
block|{
name|int
name|rowSize
init|=
name|vectorExtractRow
operator|.
name|getCount
argument_list|()
decl_stmt|;
name|Object
index|[]
name|row
init|=
operator|new
name|Object
index|[
name|rowSize
index|]
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
name|batch
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
name|vectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|i
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|Object
index|[]
name|expectedRow
init|=
name|randomRows
index|[
name|firstRandomRowIndex
operator|+
name|i
index|]
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|rowSize
condition|;
name|c
operator|++
control|)
block|{
name|Object
name|actualValue
init|=
name|row
index|[
name|c
index|]
decl_stmt|;
name|Object
name|expectedValue
init|=
name|expectedRow
index|[
name|c
index|]
decl_stmt|;
if|if
condition|(
name|actualValue
operator|==
literal|null
operator|||
name|expectedValue
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|actualValue
operator|!=
name|expectedValue
condition|)
block|{
name|fail
argument_list|(
literal|"Row "
operator|+
operator|(
name|firstRandomRowIndex
operator|+
name|i
operator|)
operator|+
literal|" and column "
operator|+
name|c
operator|+
literal|" mismatch"
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|actualValue
operator|.
name|equals
argument_list|(
name|expectedValue
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"Row "
operator|+
operator|(
name|firstRandomRowIndex
operator|+
name|i
operator|)
operator|+
literal|" and column "
operator|+
name|c
operator|+
literal|" mismatch"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|void
name|testVectorRowObject
parameter_list|(
name|int
name|caseNum
parameter_list|,
name|boolean
name|sort
parameter_list|,
name|Random
name|r
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
index|[]
name|emptyScratchTypeNames
init|=
operator|new
name|String
index|[
literal|0
index|]
decl_stmt|;
name|VectorRandomRowSource
name|source
init|=
operator|new
name|VectorRandomRowSource
argument_list|()
decl_stmt|;
name|source
operator|.
name|init
argument_list|(
name|r
argument_list|,
name|VectorRandomRowSource
operator|.
name|SupportedTypes
operator|.
name|ALL
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|VectorizedRowBatchCtx
name|batchContext
init|=
operator|new
name|VectorizedRowBatchCtx
argument_list|()
decl_stmt|;
name|batchContext
operator|.
name|init
argument_list|(
name|source
operator|.
name|rowStructObjectInspector
argument_list|()
argument_list|,
name|emptyScratchTypeNames
argument_list|)
expr_stmt|;
name|VectorizedRowBatch
name|batch
init|=
name|batchContext
operator|.
name|createVectorizedRowBatch
argument_list|()
decl_stmt|;
comment|// junk the destination for the 1st pass
for|for
control|(
name|ColumnVector
name|cv
range|:
name|batch
operator|.
name|cols
control|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|cv
operator|.
name|isNull
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|cv
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|VectorAssignRow
name|vectorAssignRow
init|=
operator|new
name|VectorAssignRow
argument_list|()
decl_stmt|;
name|vectorAssignRow
operator|.
name|init
argument_list|(
name|source
operator|.
name|typeNames
argument_list|()
argument_list|)
expr_stmt|;
name|VectorExtractRow
name|vectorExtractRow
init|=
operator|new
name|VectorExtractRow
argument_list|()
decl_stmt|;
name|vectorExtractRow
operator|.
name|init
argument_list|(
name|source
operator|.
name|typeNames
argument_list|()
argument_list|)
expr_stmt|;
name|Object
index|[]
index|[]
name|randomRows
init|=
name|source
operator|.
name|randomRows
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
if|if
condition|(
name|sort
condition|)
block|{
name|source
operator|.
name|sort
argument_list|(
name|randomRows
argument_list|)
expr_stmt|;
block|}
name|int
name|firstRandomRowIndex
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
name|randomRows
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Object
index|[]
name|row
init|=
name|randomRows
index|[
name|i
index|]
decl_stmt|;
name|vectorAssignRow
operator|.
name|assignRow
argument_list|(
name|batch
argument_list|,
name|batch
operator|.
name|size
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|batch
operator|.
name|size
operator|++
expr_stmt|;
if|if
condition|(
name|batch
operator|.
name|size
operator|==
name|batch
operator|.
name|DEFAULT_SIZE
condition|)
block|{
name|examineBatch
argument_list|(
name|batch
argument_list|,
name|vectorExtractRow
argument_list|,
name|randomRows
argument_list|,
name|firstRandomRowIndex
argument_list|)
expr_stmt|;
name|firstRandomRowIndex
operator|=
name|i
operator|+
literal|1
expr_stmt|;
name|batch
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|batch
operator|.
name|size
operator|>
literal|0
condition|)
block|{
name|examineBatch
argument_list|(
name|batch
argument_list|,
name|vectorExtractRow
argument_list|,
name|randomRows
argument_list|,
name|firstRandomRowIndex
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testVectorRowObject
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|Random
name|r
init|=
operator|new
name|Random
argument_list|(
literal|5678
argument_list|)
decl_stmt|;
name|int
name|caseNum
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|testVectorRowObject
argument_list|(
name|caseNum
argument_list|,
literal|false
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|caseNum
operator|++
expr_stmt|;
block|}
comment|// Try one sorted.
name|testVectorRowObject
argument_list|(
name|caseNum
argument_list|,
literal|true
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|caseNum
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit

