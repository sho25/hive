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
name|junit
operator|.
name|Test
import|;
end_import

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
block|}
end_class

end_unit

