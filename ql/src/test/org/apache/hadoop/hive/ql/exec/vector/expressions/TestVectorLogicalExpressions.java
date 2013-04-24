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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|TestVectorLogicalExpressions
block|{
annotation|@
name|Test
specifier|public
name|void
name|testLongColOrLongCol
parameter_list|()
block|{
name|VectorizedRowBatch
name|batch
init|=
name|getBatchThreeBooleanCols
argument_list|()
decl_stmt|;
name|ColOrCol
name|expr
init|=
operator|new
name|ColOrCol
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|LongColumnVector
name|outCol
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|2
index|]
decl_stmt|;
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
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|isNull
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|isNull
index|[
literal|4
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|5
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|isNull
index|[
literal|6
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|7
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|isNull
index|[
literal|8
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|batch
operator|.
name|size
argument_list|,
literal|9
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
comment|// try non-null path
name|batch
operator|=
name|getBatchThreeBooleanCols
argument_list|()
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|1
index|]
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|2
index|]
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outCol
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|2
index|]
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// spot check
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
comment|// try isRepeating path (left input only), no nulls
name|batch
operator|=
name|getBatchThreeBooleanCols
argument_list|()
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|1
index|]
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|1
index|]
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|2
index|]
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|2
index|]
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|outCol
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|2
index|]
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// spot check
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get a batch with three boolean (long) columns.    */
specifier|private
name|VectorizedRowBatch
name|getBatchThreeBooleanCols
parameter_list|()
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|new
name|VectorizedRowBatch
argument_list|(
literal|3
argument_list|,
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
name|LongColumnVector
name|v0
decl_stmt|,
name|v1
decl_stmt|,
name|v2
decl_stmt|;
name|v0
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
name|v1
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
name|v2
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|=
name|v0
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|1
index|]
operator|=
name|v1
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|2
index|]
operator|=
name|v2
expr_stmt|;
comment|// add some data and nulls
name|int
name|i
decl_stmt|;
name|i
operator|=
literal|0
expr_stmt|;
name|v0
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|v0
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|v1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|v1
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
comment|// 0 0
name|i
operator|=
literal|1
expr_stmt|;
name|v0
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|v0
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|v1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
name|v1
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
comment|// 0 1
name|i
operator|=
literal|2
expr_stmt|;
name|v0
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
name|v0
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|v1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|v1
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
comment|// 1 0
name|i
operator|=
literal|3
expr_stmt|;
name|v0
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
name|v0
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|v1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
name|v1
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
comment|// 1 1
name|i
operator|=
literal|4
expr_stmt|;
name|v0
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|v0
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|v1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|v1
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
comment|// NULL 0
name|i
operator|=
literal|5
expr_stmt|;
name|v0
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|v0
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|v1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
name|v1
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
comment|// NULL 1
name|i
operator|=
literal|6
expr_stmt|;
name|v0
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|v0
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|v1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|v1
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
comment|// 0 NULL
name|i
operator|=
literal|7
expr_stmt|;
name|v0
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
name|v0
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|v1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
name|v1
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
comment|// 1 NULL
name|i
operator|=
literal|8
expr_stmt|;
name|v0
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
name|v0
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|v1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
name|v1
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
comment|// NULL NULL
name|v0
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|v1
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|v0
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|v1
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|v2
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
comment|// this value should get over-written with correct value
name|v2
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
comment|// ditto
name|batch
operator|.
name|size
operator|=
literal|9
expr_stmt|;
return|return
name|batch
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBooleanNot
parameter_list|()
block|{
name|VectorizedRowBatch
name|batch
init|=
name|getBatchThreeBooleanCols
argument_list|()
decl_stmt|;
name|NotCol
name|expr
init|=
operator|new
name|NotCol
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|LongColumnVector
name|outCol
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|2
index|]
decl_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// Case with nulls
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|isNull
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|isNull
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|isNull
index|[
literal|4
index|]
argument_list|)
expr_stmt|;
comment|// No nulls case
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
comment|// isRepeating, and there are nulls
name|batch
operator|=
name|getBatchThreeBooleanCols
argument_list|()
expr_stmt|;
name|outCol
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|2
index|]
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
empty_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|isNull
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// isRepeating, and no nulls
name|batch
operator|=
name|getBatchThreeBooleanCols
argument_list|()
expr_stmt|;
name|outCol
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|2
index|]
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
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
name|testIsNullExpr
parameter_list|()
block|{
comment|// has nulls, not repeating
name|VectorizedRowBatch
name|batch
init|=
name|getBatchThreeBooleanCols
argument_list|()
decl_stmt|;
name|IsNull
name|expr
init|=
operator|new
name|IsNull
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|LongColumnVector
name|outCol
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|2
index|]
decl_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|4
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
comment|// No nulls case, not repeating
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|4
index|]
argument_list|)
expr_stmt|;
comment|// isRepeating, and there are nulls
name|batch
operator|=
name|getBatchThreeBooleanCols
argument_list|()
expr_stmt|;
name|outCol
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|2
index|]
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
empty_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|outCol
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|noNulls
argument_list|)
expr_stmt|;
comment|// isRepeating, and no nulls
name|batch
operator|=
name|getBatchThreeBooleanCols
argument_list|()
expr_stmt|;
name|outCol
operator|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|2
index|]
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|outCol
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|outCol
operator|.
name|vector
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
name|testBooleanFiltersOnColumns
parameter_list|()
block|{
name|VectorizedRowBatch
name|batch
init|=
name|getBatchThreeBooleanCols
argument_list|()
decl_stmt|;
name|SelectColumnIsTrue
name|expr
init|=
operator|new
name|SelectColumnIsTrue
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|batch
operator|=
name|getBatchThreeBooleanCols
argument_list|()
expr_stmt|;
name|SelectColumnIsFalse
name|expr1
init|=
operator|new
name|SelectColumnIsFalse
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|expr1
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|batch
operator|=
name|getBatchThreeBooleanCols
argument_list|()
expr_stmt|;
name|SelectColumnIsNull
name|expr2
init|=
operator|new
name|SelectColumnIsNull
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|expr2
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|batch
operator|=
name|getBatchThreeBooleanCols
argument_list|()
expr_stmt|;
name|SelectColumnIsNotNull
name|expr3
init|=
operator|new
name|SelectColumnIsNotNull
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|expr3
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|4
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|batch
operator|.
name|selected
index|[
literal|5
index|]
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

