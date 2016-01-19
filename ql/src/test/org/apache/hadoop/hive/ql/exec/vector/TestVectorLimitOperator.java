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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|conf
operator|.
name|Configuration
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
name|CompilationOpContext
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
name|util
operator|.
name|FakeVectorRowBatchFromObjectIterables
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
name|plan
operator|.
name|LimitDesc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Unit test for the vectorized LIMIT operator.  */
end_comment

begin_class
specifier|public
class|class
name|TestVectorLimitOperator
block|{
annotation|@
name|Test
specifier|public
name|void
name|testLimitLessThanBatchSize
parameter_list|()
throws|throws
name|HiveException
block|{
name|validateVectorLimitOperator
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLimitGreaterThanBatchSize
parameter_list|()
throws|throws
name|HiveException
block|{
name|validateVectorLimitOperator
argument_list|(
literal|100
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLimitWithZeroBatchSize
parameter_list|()
throws|throws
name|HiveException
block|{
name|validateVectorLimitOperator
argument_list|(
literal|5
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateVectorLimitOperator
parameter_list|(
name|int
name|limit
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|int
name|expectedBatchSize
parameter_list|)
throws|throws
name|HiveException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|FakeVectorRowBatchFromObjectIterables
name|frboi
init|=
operator|new
name|FakeVectorRowBatchFromObjectIterables
argument_list|(
name|batchSize
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"tinyint"
block|,
literal|"double"
block|}
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|323.0
block|,
literal|34.5
block|,
literal|null
block|,
literal|89.3
block|}
argument_list|)
argument_list|)
decl_stmt|;
comment|// Get next batch
name|VectorizedRowBatch
name|vrb
init|=
name|frboi
operator|.
name|produceNextBatch
argument_list|()
decl_stmt|;
comment|// Create limit desc with limit value
name|LimitDesc
name|ld
init|=
operator|new
name|LimitDesc
argument_list|(
name|limit
argument_list|)
decl_stmt|;
name|VectorLimitOperator
name|lo
init|=
operator|new
name|VectorLimitOperator
argument_list|(
operator|new
name|CompilationOpContext
argument_list|()
argument_list|,
literal|null
argument_list|,
name|ld
argument_list|)
decl_stmt|;
name|lo
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Process the batch
name|lo
operator|.
name|process
argument_list|(
name|vrb
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Verify batch size
name|Assert
operator|.
name|assertEquals
argument_list|(
name|vrb
operator|.
name|size
argument_list|,
name|expectedBatchSize
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

