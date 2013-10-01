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
name|FilterLongColEqualLongScalar
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
name|FilterLongColGreaterLongColumn
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
name|FilterLongColGreaterLongScalar
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
name|FilterLongColLessLongColumn
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
name|FilterLongScalarGreaterLongColumn
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
name|FilterLongScalarLessLongColumn
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
name|VectorizedRowGroupGenUtil
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

begin_comment
comment|/**  * Unit tests for filter expressions.  */
end_comment

begin_class
specifier|public
class|class
name|TestVectorFilterExpressions
block|{
annotation|@
name|Test
specifier|public
name|void
name|testFilterLongColEqualLongScalar
parameter_list|()
block|{
name|VectorizedRowBatch
name|vrg
init|=
name|VectorizedRowGroupGenUtil
operator|.
name|getVectorizedRowBatch
argument_list|(
literal|1024
argument_list|,
literal|1
argument_list|,
literal|23
argument_list|)
decl_stmt|;
name|FilterLongColEqualLongScalar
name|expr
init|=
operator|new
name|FilterLongColEqualLongScalar
argument_list|(
literal|0
argument_list|,
literal|46
argument_list|)
decl_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrg
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrg
operator|.
name|selected
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
name|testFilterLongColEqualLongColumn
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
literal|2
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
name|FilterLongColGreaterLongColumn
name|expr
init|=
operator|new
name|FilterLongColGreaterLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
comment|//Basic case
name|lcv0
operator|.
name|vector
index|[
literal|1
index|]
operator|=
literal|23
expr_stmt|;
name|lcv1
operator|.
name|vector
index|[
literal|1
index|]
operator|=
literal|19
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|5
index|]
operator|=
literal|23
expr_stmt|;
name|lcv1
operator|.
name|vector
index|[
literal|5
index|]
operator|=
literal|19
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|vrg
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrg
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
name|vrg
operator|.
name|selected
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
comment|//handle null
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
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrg
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|vrg
operator|.
name|selected
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
name|testColOpScalarNumericFilterNullAndRepeatingLogic
parameter_list|()
block|{
comment|// No nulls, not repeating
name|FilterLongColGreaterLongScalar
name|f
init|=
operator|new
name|FilterLongColGreaterLongScalar
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|VectorizedRowBatch
name|batch
init|=
name|this
operator|.
name|getSimpleLongBatch
argument_list|()
decl_stmt|;
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
literal|false
expr_stmt|;
name|f
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// only last 2 rows qualify
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
comment|// show that their positions are recorded
name|Assert
operator|.
name|assertTrue
argument_list|(
name|batch
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|Assert
operator|.
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
name|Assert
operator|.
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
comment|// make everything qualify and ensure selected is not in use
name|f
operator|=
operator|new
name|FilterLongColGreaterLongScalar
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// col> -1
name|batch
operator|=
name|getSimpleLongBatch
argument_list|()
expr_stmt|;
name|f
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
name|batch
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
comment|// has nulls, not repeating
name|batch
operator|=
name|getSimpleLongBatch
argument_list|()
expr_stmt|;
name|f
operator|=
operator|new
name|FilterLongColGreaterLongScalar
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// col> 1
name|batch
operator|.
name|cols
index|[
literal|0
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
literal|0
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
literal|0
index|]
operator|.
name|isNull
index|[
literal|3
index|]
operator|=
literal|true
expr_stmt|;
name|f
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
name|batch
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
name|Assert
operator|.
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
comment|// no nulls, is repeating
name|batch
operator|=
name|getSimpleLongBatch
argument_list|()
expr_stmt|;
name|f
operator|=
operator|new
name|FilterLongColGreaterLongScalar
argument_list|(
literal|0
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// col> -1
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
name|f
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
name|batch
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
comment|// everything qualifies (4 rows, all with value -1)
comment|// has nulls, is repeating
name|batch
operator|=
name|getSimpleLongBatch
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
literal|false
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
name|f
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
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
comment|// all values are null so none qualify
block|}
specifier|private
name|VectorizedRowBatch
name|getSimpleLongBatch
parameter_list|()
block|{
name|VectorizedRowBatch
name|batch
init|=
name|VectorizedRowGroupGenUtil
operator|.
name|getVectorizedRowBatch
argument_list|(
literal|4
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|LongColumnVector
name|lcv0
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
literal|0
index|]
decl_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|0
index|]
operator|=
literal|0
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|1
index|]
operator|=
literal|1
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|2
index|]
operator|=
literal|2
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|3
index|]
operator|=
literal|3
expr_stmt|;
return|return
name|batch
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFilterLongColLessLongColumn
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
literal|5
argument_list|,
literal|3
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
name|FilterLongColLessLongColumn
name|expr
init|=
operator|new
name|FilterLongColLessLongColumn
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|LongColAddLongScalar
name|childExpr
init|=
operator|new
name|LongColAddLongScalar
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|expr
operator|.
name|setChildExpressions
argument_list|(
operator|new
name|VectorExpression
index|[]
block|{
name|childExpr
block|}
argument_list|)
expr_stmt|;
comment|//Basic case
name|lcv0
operator|.
name|vector
index|[
literal|0
index|]
operator|=
literal|10
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|1
index|]
operator|=
literal|20
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|2
index|]
operator|=
literal|9
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|3
index|]
operator|=
literal|20
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|4
index|]
operator|=
literal|10
expr_stmt|;
name|lcv1
operator|.
name|vector
index|[
literal|0
index|]
operator|=
literal|20
expr_stmt|;
name|lcv1
operator|.
name|vector
index|[
literal|1
index|]
operator|=
literal|10
expr_stmt|;
name|lcv1
operator|.
name|vector
index|[
literal|2
index|]
operator|=
literal|20
expr_stmt|;
name|lcv1
operator|.
name|vector
index|[
literal|3
index|]
operator|=
literal|10
expr_stmt|;
name|lcv1
operator|.
name|vector
index|[
literal|4
index|]
operator|=
literal|20
expr_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrg
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|vrg
operator|.
name|selected
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
name|testFilterLongScalarLessLongColumn
parameter_list|()
block|{
name|int
name|seed
init|=
literal|17
decl_stmt|;
name|VectorizedRowBatch
name|vrb
init|=
name|VectorizedRowGroupGenUtil
operator|.
name|getVectorizedRowBatch
argument_list|(
literal|5
argument_list|,
literal|2
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
name|vrb
operator|.
name|cols
index|[
literal|0
index|]
decl_stmt|;
name|FilterLongScalarLessLongColumn
name|expr1
init|=
operator|new
name|FilterLongScalarLessLongColumn
argument_list|(
literal|0
argument_list|,
literal|15
argument_list|)
decl_stmt|;
comment|//Basic case
name|lcv0
operator|.
name|vector
index|[
literal|0
index|]
operator|=
literal|5
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|1
index|]
operator|=
literal|20
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|2
index|]
operator|=
literal|17
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|3
index|]
operator|=
literal|15
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|4
index|]
operator|=
literal|10
expr_stmt|;
name|expr1
operator|.
name|evaluate
argument_list|(
name|vrb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|vrb
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|vrb
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrb
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
name|vrb
operator|.
name|selected
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|FilterLongScalarGreaterLongColumn
name|expr2
init|=
operator|new
name|FilterLongScalarGreaterLongColumn
argument_list|(
literal|0
argument_list|,
literal|18
argument_list|)
decl_stmt|;
name|expr2
operator|.
name|evaluate
argument_list|(
name|vrb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrb
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|vrb
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|vrb
operator|.
name|selected
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|//With nulls
name|VectorizedRowBatch
name|vrb1
init|=
name|VectorizedRowGroupGenUtil
operator|.
name|getVectorizedRowBatch
argument_list|(
literal|5
argument_list|,
literal|2
argument_list|,
name|seed
argument_list|)
decl_stmt|;
name|lcv0
operator|=
operator|(
name|LongColumnVector
operator|)
name|vrb1
operator|.
name|cols
index|[
literal|0
index|]
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|0
index|]
operator|=
literal|5
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|1
index|]
operator|=
literal|20
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|2
index|]
operator|=
literal|17
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|3
index|]
operator|=
literal|15
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|4
index|]
operator|=
literal|10
expr_stmt|;
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
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv0
operator|.
name|isNull
index|[
literal|2
index|]
operator|=
literal|true
expr_stmt|;
name|expr1
operator|.
name|evaluate
argument_list|(
name|vrb1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrb1
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|vrb1
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrb1
operator|.
name|selected
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|//With nulls and selected
name|VectorizedRowBatch
name|vrb2
init|=
name|VectorizedRowGroupGenUtil
operator|.
name|getVectorizedRowBatch
argument_list|(
literal|7
argument_list|,
literal|2
argument_list|,
name|seed
argument_list|)
decl_stmt|;
name|vrb2
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
name|vrb2
operator|.
name|selected
index|[
literal|0
index|]
operator|=
literal|1
expr_stmt|;
name|vrb2
operator|.
name|selected
index|[
literal|1
index|]
operator|=
literal|2
expr_stmt|;
name|vrb2
operator|.
name|selected
index|[
literal|2
index|]
operator|=
literal|4
expr_stmt|;
name|vrb2
operator|.
name|size
operator|=
literal|3
expr_stmt|;
name|lcv0
operator|=
operator|(
name|LongColumnVector
operator|)
name|vrb2
operator|.
name|cols
index|[
literal|0
index|]
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|0
index|]
operator|=
literal|5
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|1
index|]
operator|=
literal|20
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|2
index|]
operator|=
literal|17
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|3
index|]
operator|=
literal|15
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|4
index|]
operator|=
literal|10
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|5
index|]
operator|=
literal|19
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|6
index|]
operator|=
literal|21
expr_stmt|;
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
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|lcv0
operator|.
name|isNull
index|[
literal|2
index|]
operator|=
literal|true
expr_stmt|;
name|lcv0
operator|.
name|isNull
index|[
literal|5
index|]
operator|=
literal|true
expr_stmt|;
name|expr1
operator|.
name|evaluate
argument_list|(
name|vrb2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrb2
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|vrb2
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|vrb2
operator|.
name|selected
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|//Repeating non null
name|VectorizedRowBatch
name|vrb3
init|=
name|VectorizedRowGroupGenUtil
operator|.
name|getVectorizedRowBatch
argument_list|(
literal|7
argument_list|,
literal|2
argument_list|,
name|seed
argument_list|)
decl_stmt|;
name|lcv0
operator|=
operator|(
name|LongColumnVector
operator|)
name|vrb3
operator|.
name|cols
index|[
literal|0
index|]
expr_stmt|;
name|lcv0
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|0
index|]
operator|=
literal|17
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|1
index|]
operator|=
literal|20
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|2
index|]
operator|=
literal|17
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|3
index|]
operator|=
literal|15
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|4
index|]
operator|=
literal|10
expr_stmt|;
name|expr1
operator|.
name|evaluate
argument_list|(
name|vrb3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|vrb3
operator|.
name|size
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|vrb3
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lcv0
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
comment|//Repeating null
name|lcv0
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|lcv0
operator|.
name|vector
index|[
literal|0
index|]
operator|=
literal|17
expr_stmt|;
name|lcv0
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|expr1
operator|.
name|evaluate
argument_list|(
name|vrb3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|vrb3
operator|.
name|size
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

