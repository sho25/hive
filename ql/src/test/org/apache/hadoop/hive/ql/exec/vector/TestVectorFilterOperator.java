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
name|ArrayList
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
name|conf
operator|.
name|HiveConf
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
name|FilterExprAndExpr
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
name|VectorExpression
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
name|FilterLongColEqualDoubleScalar
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
name|ExprNodeColumnDesc
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
name|FilterDesc
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
comment|/**  * Test cases for vectorized filter operator.  */
end_comment

begin_class
specifier|public
class|class
name|TestVectorFilterOperator
block|{
name|HiveConf
name|hconf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
comment|/**    * Fundamental logic and performance tests for vector filters belong here.    *    * For tests about filters to cover specific operator and data type combinations,    * see also the other filter tests under org.apache.hadoop.hive.ql.exec.vector.expressions    */
specifier|public
specifier|static
class|class
name|FakeDataReader
block|{
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
specifier|private
specifier|final
name|VectorizedRowBatch
name|vrg
decl_stmt|;
specifier|private
name|int
name|currentSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|int
name|numCols
decl_stmt|;
specifier|private
specifier|final
name|int
name|len
init|=
literal|1024
decl_stmt|;
specifier|public
name|FakeDataReader
parameter_list|(
name|int
name|size
parameter_list|,
name|int
name|numCols
parameter_list|)
block|{
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|numCols
operator|=
name|numCols
expr_stmt|;
name|vrg
operator|=
operator|new
name|VectorizedRowBatch
argument_list|(
name|numCols
argument_list|,
name|len
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
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ignore
parameter_list|)
block|{}
name|vrg
operator|.
name|cols
index|[
name|i
index|]
operator|=
name|getLongVector
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|VectorizedRowBatch
name|getNext
parameter_list|()
block|{
if|if
condition|(
name|currentSize
operator|>=
name|size
condition|)
block|{
name|vrg
operator|.
name|size
operator|=
literal|0
expr_stmt|;
return|return
name|vrg
return|;
block|}
else|else
block|{
name|vrg
operator|.
name|size
operator|=
name|len
expr_stmt|;
name|currentSize
operator|+=
name|vrg
operator|.
name|size
expr_stmt|;
name|vrg
operator|.
name|selectedInUse
operator|=
literal|false
expr_stmt|;
return|return
name|vrg
return|;
block|}
block|}
specifier|private
name|LongColumnVector
name|getLongVector
parameter_list|(
name|int
name|len
parameter_list|)
block|{
name|LongColumnVector
name|lcv
init|=
operator|new
name|LongColumnVector
argument_list|(
name|len
argument_list|)
decl_stmt|;
name|TestVectorizedRowBatch
operator|.
name|setRandomLongCol
argument_list|(
name|lcv
argument_list|)
expr_stmt|;
return|return
name|lcv
return|;
block|}
block|}
specifier|private
name|VectorFilterOperator
name|getAVectorFilterOperator
parameter_list|()
throws|throws
name|HiveException
block|{
name|ExprNodeColumnDesc
name|col1Expr
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|Long
operator|.
name|class
argument_list|,
literal|"col1"
argument_list|,
literal|"table"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|columns
operator|.
name|add
argument_list|(
literal|"col1"
argument_list|)
expr_stmt|;
name|VectorizationContext
name|vc
init|=
operator|new
name|VectorizationContext
argument_list|(
name|columns
argument_list|)
decl_stmt|;
name|FilterDesc
name|fdesc
init|=
operator|new
name|FilterDesc
argument_list|()
decl_stmt|;
name|fdesc
operator|.
name|setPredicate
argument_list|(
name|col1Expr
argument_list|)
expr_stmt|;
return|return
operator|new
name|VectorFilterOperator
argument_list|(
name|vc
argument_list|,
name|fdesc
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicFilterOperator
parameter_list|()
throws|throws
name|HiveException
block|{
name|VectorFilterOperator
name|vfo
init|=
name|getAVectorFilterOperator
argument_list|()
decl_stmt|;
name|vfo
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|VectorExpression
name|ve1
init|=
operator|new
name|FilterLongColGreaterLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|VectorExpression
name|ve2
init|=
operator|new
name|FilterLongColEqualDoubleScalar
argument_list|(
literal|2
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|VectorExpression
name|ve3
init|=
operator|new
name|FilterExprAndExpr
argument_list|()
decl_stmt|;
name|ve3
operator|.
name|setChildExpressions
argument_list|(
operator|new
name|VectorExpression
index|[]
block|{
name|ve1
block|,
name|ve2
block|}
argument_list|)
expr_stmt|;
name|vfo
operator|.
name|setFilterCondition
argument_list|(
name|ve3
argument_list|)
expr_stmt|;
name|FakeDataReader
name|fdr
init|=
operator|new
name|FakeDataReader
argument_list|(
literal|1024
operator|*
literal|1
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|VectorizedRowBatch
name|vrg
init|=
name|fdr
operator|.
name|getNext
argument_list|()
decl_stmt|;
name|vfo
operator|.
name|getConditionEvaluator
argument_list|()
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
comment|//Verify
name|int
name|rows
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
literal|1024
condition|;
name|i
operator|++
control|)
block|{
name|LongColumnVector
name|l1
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
name|l2
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
name|l3
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
if|if
condition|(
operator|(
name|l1
operator|.
name|vector
index|[
name|i
index|]
operator|>
name|l2
operator|.
name|vector
index|[
name|i
index|]
operator|)
operator|&&
operator|(
name|l3
operator|.
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|rows
operator|++
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|rows
argument_list|,
name|vrg
operator|.
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicFilterLargeData
parameter_list|()
throws|throws
name|HiveException
block|{
name|VectorFilterOperator
name|vfo
init|=
name|getAVectorFilterOperator
argument_list|()
decl_stmt|;
name|vfo
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|VectorExpression
name|ve1
init|=
operator|new
name|FilterLongColGreaterLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|VectorExpression
name|ve2
init|=
operator|new
name|FilterLongColEqualDoubleScalar
argument_list|(
literal|2
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|VectorExpression
name|ve3
init|=
operator|new
name|FilterExprAndExpr
argument_list|()
decl_stmt|;
name|ve3
operator|.
name|setChildExpressions
argument_list|(
operator|new
name|VectorExpression
index|[]
block|{
name|ve1
block|,
name|ve2
block|}
argument_list|)
expr_stmt|;
name|vfo
operator|.
name|setFilterCondition
argument_list|(
name|ve3
argument_list|)
expr_stmt|;
name|FakeDataReader
name|fdr
init|=
operator|new
name|FakeDataReader
argument_list|(
literal|16
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|VectorizedRowBatch
name|vrg
init|=
name|fdr
operator|.
name|getNext
argument_list|()
decl_stmt|;
while|while
condition|(
name|vrg
operator|.
name|size
operator|>
literal|0
condition|)
block|{
name|vfo
operator|.
name|process
argument_list|(
name|vrg
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|vrg
operator|=
name|fdr
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"testBaseFilterOperator Op Time = "
operator|+
operator|(
name|endTime
operator|-
name|startTime
operator|)
argument_list|)
expr_stmt|;
comment|//Base time
name|fdr
operator|=
operator|new
name|FakeDataReader
argument_list|(
literal|16
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|long
name|startTime1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|vrg
operator|=
name|fdr
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|LongColumnVector
name|l1
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
name|l2
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
name|l3
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
name|int
name|rows
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|16
operator|*
literal|1024
condition|;
name|j
operator|++
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|l1
operator|.
name|vector
operator|.
name|length
operator|&&
name|i
operator|<
name|l2
operator|.
name|vector
operator|.
name|length
operator|&&
name|i
operator|<
name|l3
operator|.
name|vector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|(
name|l1
operator|.
name|vector
index|[
name|i
index|]
operator|>
name|l2
operator|.
name|vector
index|[
name|i
index|]
operator|)
operator|&&
operator|(
name|l3
operator|.
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|rows
operator|++
expr_stmt|;
block|}
block|}
block|}
name|long
name|endTime1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"testBaseFilterOperator base Op Time = "
operator|+
operator|(
name|endTime1
operator|-
name|startTime1
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

