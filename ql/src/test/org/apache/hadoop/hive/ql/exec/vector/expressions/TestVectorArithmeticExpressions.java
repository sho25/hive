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
name|expressions
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|TestVectorizedRowBatch
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
name|expressions
operator|.
name|gen
operator|.
name|LongColAddLongColumn
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
name|expressions
operator|.
name|gen
operator|.
name|LongColAddLongScalar
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

begin_class
specifier|public
class|class
name|TestVectorArithmeticExpressions
block|{
annotation|@
name|Test
specifier|public
name|void
name|testLongColAddLongScalarNoNulls
parameter_list|()
block|{
name|VectorizedRowBatch
name|vrg
init|=
name|getVectorizedRowBatchSingleLongVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
name|LongColAddLongScalar
name|expr
init|=
operator|new
name|LongColAddLongScalar
argument_list|(
literal|0
argument_list|,
literal|23
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
comment|//verify
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i
operator|*
literal|37
operator|+
literal|23
argument_list|,
operator|(
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
operator|)
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|(
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
operator|)
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
operator|(
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
operator|)
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
block|}
specifier|private
name|VectorizedRowBatch
name|getVectorizedRowBatchSingleLongVector
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|VectorizedRowBatch
name|vrg
init|=
operator|new
name|VectorizedRowBatch
argument_list|(
literal|2
argument_list|,
name|size
argument_list|)
decl_stmt|;
name|LongColumnVector
name|lcv
init|=
operator|new
name|LongColumnVector
argument_list|(
name|size
argument_list|)
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
name|i
operator|++
control|)
block|{
name|lcv
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|i
operator|*
literal|37
expr_stmt|;
block|}
name|vrg
operator|.
name|cols
index|[
literal|0
index|]
operator|=
name|lcv
expr_stmt|;
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|vrg
operator|.
name|size
operator|=
name|size
expr_stmt|;
return|return
name|vrg
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLongColAddLongScalarWithNulls
parameter_list|()
block|{
name|VectorizedRowBatch
name|vrg
init|=
name|getVectorizedRowBatchSingleLongVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|0
index|]
decl_stmt|;
name|TestVectorizedRowBatch
operator|.
name|addRandomNulls
argument_list|(
name|lcv
argument_list|)
expr_stmt|;
name|LongColAddLongScalar
name|expr
init|=
operator|new
name|LongColAddLongScalar
argument_list|(
literal|0
argument_list|,
literal|23
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
comment|//verify
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|lcv
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|i
operator|*
literal|37
operator|+
literal|23
argument_list|,
operator|(
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
operator|)
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|(
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
operator|)
operator|.
name|isNull
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertFalse
argument_list|(
operator|(
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
operator|)
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
operator|(
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
operator|)
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLongColAddLongScalarWithRepeating
parameter_list|()
block|{
name|LongColumnVector
name|in
decl_stmt|,
name|out
decl_stmt|;
name|VectorizedRowBatch
name|batch
decl_stmt|;
name|LongColAddLongScalar
name|expr
decl_stmt|;
comment|// Case 1: is repeating, no nulls
name|batch
operator|=
name|getVectorizedRowBatchSingleLongVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
name|in
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|0
index|]
expr_stmt|;
name|in
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|out
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|1
index|]
expr_stmt|;
name|out
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|expr
operator|=
operator|new
name|LongColAddLongScalar
argument_list|(
literal|0
argument_list|,
literal|23
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// verify
name|Assert
operator|.
name|assertTrue
argument_list|(
name|out
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|out
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|out
operator|.
name|vector
index|[
literal|0
index|]
argument_list|,
literal|0
operator|*
literal|37
operator|+
literal|23
argument_list|)
expr_stmt|;
comment|// Case 2: is repeating, has nulls
name|batch
operator|=
name|getVectorizedRowBatchSingleLongVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
name|in
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|0
index|]
expr_stmt|;
name|in
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|in
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|in
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|out
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|1
index|]
expr_stmt|;
name|out
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|out
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|out
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|expr
operator|=
operator|new
name|LongColAddLongScalar
argument_list|(
literal|0
argument_list|,
literal|23
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// verify
name|Assert
operator|.
name|assertTrue
argument_list|(
name|out
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|out
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|out
operator|.
name|isNull
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLongColAddLongColumn
parameter_list|()
block|{
name|int
name|seed
init|=
literal|17
decl_stmt|;
name|VectorizedRowBatch
name|vrg
init|=
name|VectorizedRowGroupGenUtil
operator|.
name|getVectorizedRowBatch
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|,
literal|6
argument_list|,
name|seed
argument_list|)
decl_stmt|;
name|LongColumnVector
name|lcv0
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|0
index|]
decl_stmt|;
name|LongColumnVector
name|lcv1
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
decl_stmt|;
name|LongColumnVector
name|lcv2
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|2
index|]
decl_stmt|;
name|LongColumnVector
name|lcv3
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|3
index|]
decl_stmt|;
name|LongColumnVector
name|lcv4
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|4
index|]
decl_stmt|;
name|LongColumnVector
name|lcv5
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|5
index|]
decl_stmt|;
name|LongColAddLongColumn
name|expr
init|=
operator|new
name|LongColAddLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
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
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
name|seed
operator|*
literal|3
argument_list|,
name|lcv2
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|lcv2
operator|.
name|noNulls
argument_list|)
expr_stmt|;
comment|//Now set one column nullable
name|lcv1
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv1
operator|.
name|isNull
index|[
literal|1
index|]
operator|=
literal|true
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lcv2
operator|.
name|isNull
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|lcv2
operator|.
name|noNulls
argument_list|)
expr_stmt|;
comment|//Now set other column nullable too
name|lcv0
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv0
operator|.
name|isNull
index|[
literal|1
index|]
operator|=
literal|true
expr_stmt|;
name|lcv0
operator|.
name|isNull
index|[
literal|3
index|]
operator|=
literal|true
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lcv2
operator|.
name|isNull
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lcv2
operator|.
name|isNull
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|lcv2
operator|.
name|noNulls
argument_list|)
expr_stmt|;
comment|//Now test with repeating flag
name|lcv3
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|LongColAddLongColumn
name|expr2
init|=
operator|new
name|LongColAddLongColumn
argument_list|(
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|expr2
operator|.
name|evaluate
argument_list|(
name|vrg
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
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|seed
operator|*
operator|(
literal|4
operator|+
literal|5
operator|*
operator|(
name|i
operator|+
literal|1
operator|)
operator|)
argument_list|,
name|lcv5
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|//Repeating with other as nullable
name|lcv4
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv4
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|expr2
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lcv5
operator|.
name|isNull
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|lcv5
operator|.
name|noNulls
argument_list|)
expr_stmt|;
comment|//Repeating null value
name|lcv3
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|lcv3
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv3
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|expr2
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|lcv5
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lcv5
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lcv5
operator|.
name|isNull
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

