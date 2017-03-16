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
name|udf
operator|.
name|generic
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
operator|.
name|javaDoubleObjectInspector
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
name|assertNull
import|;
end_import

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
name|parse
operator|.
name|SemanticException
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFEvaluator
operator|.
name|AggregationBuffer
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
name|serde2
operator|.
name|io
operator|.
name|DoubleWritable
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|LongWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_import
import|import
name|jersey
operator|.
name|repackaged
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestGenericUDAFBinarySetFunctions
block|{
specifier|private
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|rowSet
decl_stmt|;
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{0}"
argument_list|)
specifier|public
specifier|static
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|getParameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"seq/seq"
block|,
name|RowSetGenerator
operator|.
name|generate
argument_list|(
literal|10
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"seq/ones"
block|,
name|RowSetGenerator
operator|.
name|generate
argument_list|(
literal|10
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|ConstantSequence
argument_list|(
literal|1.0
argument_list|)
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"ones/seq"
block|,
name|RowSetGenerator
operator|.
name|generate
argument_list|(
literal|10
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|ConstantSequence
argument_list|(
literal|1.0
argument_list|)
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"empty"
block|,
name|RowSetGenerator
operator|.
name|generate
argument_list|(
literal|0
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"lonely"
block|,
name|RowSetGenerator
operator|.
name|generate
argument_list|(
literal|1
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"seq/seq+10"
block|,
name|RowSetGenerator
operator|.
name|generate
argument_list|(
literal|10
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|10
argument_list|)
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"seq/null"
block|,
name|RowSetGenerator
operator|.
name|generate
argument_list|(
literal|10
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|ConstantSequence
argument_list|(
literal|null
argument_list|)
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"null/seq0"
block|,
name|RowSetGenerator
operator|.
name|generate
argument_list|(
literal|10
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|ConstantSequence
argument_list|(
literal|null
argument_list|)
argument_list|,
operator|new
name|RowSetGenerator
operator|.
name|DoubleSequence
argument_list|(
literal|0
argument_list|)
argument_list|)
block|}
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFExecutor
block|{
specifier|private
name|GenericUDAFResolver2
name|evaluatorFactory
decl_stmt|;
specifier|private
name|GenericUDAFParameterInfo
name|info
decl_stmt|;
specifier|private
name|ObjectInspector
index|[]
name|partialOIs
decl_stmt|;
specifier|public
name|GenericUDAFExecutor
parameter_list|(
name|GenericUDAFResolver2
name|evaluatorFactory
parameter_list|,
name|GenericUDAFParameterInfo
name|info
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|evaluatorFactory
operator|=
name|evaluatorFactory
expr_stmt|;
name|this
operator|.
name|info
operator|=
name|info
expr_stmt|;
name|GenericUDAFEvaluator
name|eval0
init|=
name|evaluatorFactory
operator|.
name|getEvaluator
argument_list|(
name|info
argument_list|)
decl_stmt|;
name|partialOIs
operator|=
operator|new
name|ObjectInspector
index|[]
block|{
name|eval0
operator|.
name|init
argument_list|(
name|GenericUDAFEvaluator
operator|.
name|Mode
operator|.
name|PARTIAL1
argument_list|,
name|info
operator|.
name|getParameterObjectInspectors
argument_list|()
argument_list|)
block|}
expr_stmt|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|run
parameter_list|(
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|values
parameter_list|)
throws|throws
name|Exception
block|{
name|Object
name|r1
init|=
name|runComplete
argument_list|(
name|values
argument_list|)
decl_stmt|;
name|Object
name|r2
init|=
name|runPartialFinal
argument_list|(
name|values
argument_list|)
decl_stmt|;
name|Object
name|r3
init|=
name|runPartial2Final
argument_list|(
name|values
argument_list|)
decl_stmt|;
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|r1
argument_list|,
name|r2
argument_list|,
name|r3
argument_list|)
return|;
block|}
specifier|private
name|Object
name|runComplete
parameter_list|(
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|values
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|HiveException
block|{
name|GenericUDAFEvaluator
name|eval
init|=
name|evaluatorFactory
operator|.
name|getEvaluator
argument_list|(
name|info
argument_list|)
decl_stmt|;
name|eval
operator|.
name|init
argument_list|(
name|GenericUDAFEvaluator
operator|.
name|Mode
operator|.
name|COMPLETE
argument_list|,
name|info
operator|.
name|getParameterObjectInspectors
argument_list|()
argument_list|)
expr_stmt|;
name|AggregationBuffer
name|agg
init|=
name|eval
operator|.
name|getNewAggregationBuffer
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
index|[]
name|parameters
range|:
name|values
control|)
block|{
name|eval
operator|.
name|iterate
argument_list|(
name|agg
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
block|}
return|return
name|eval
operator|.
name|terminate
argument_list|(
name|agg
argument_list|)
return|;
block|}
specifier|private
name|Object
name|runPartialFinal
parameter_list|(
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|values
parameter_list|)
throws|throws
name|Exception
block|{
name|GenericUDAFEvaluator
name|eval
init|=
name|evaluatorFactory
operator|.
name|getEvaluator
argument_list|(
name|info
argument_list|)
decl_stmt|;
name|eval
operator|.
name|init
argument_list|(
name|GenericUDAFEvaluator
operator|.
name|Mode
operator|.
name|FINAL
argument_list|,
name|partialOIs
argument_list|)
expr_stmt|;
name|AggregationBuffer
name|buf
init|=
name|eval
operator|.
name|getNewAggregationBuffer
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|partialResult
range|:
name|runPartial1
argument_list|(
name|values
argument_list|)
control|)
block|{
name|eval
operator|.
name|merge
argument_list|(
name|buf
argument_list|,
name|partialResult
argument_list|)
expr_stmt|;
block|}
return|return
name|eval
operator|.
name|terminate
argument_list|(
name|buf
argument_list|)
return|;
block|}
specifier|private
name|Object
name|runPartial2Final
parameter_list|(
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|values
parameter_list|)
throws|throws
name|Exception
block|{
name|GenericUDAFEvaluator
name|eval
init|=
name|evaluatorFactory
operator|.
name|getEvaluator
argument_list|(
name|info
argument_list|)
decl_stmt|;
name|eval
operator|.
name|init
argument_list|(
name|GenericUDAFEvaluator
operator|.
name|Mode
operator|.
name|FINAL
argument_list|,
name|partialOIs
argument_list|)
expr_stmt|;
name|AggregationBuffer
name|buf
init|=
name|eval
operator|.
name|getNewAggregationBuffer
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|partialResult
range|:
name|runPartial2
argument_list|(
name|runPartial1
argument_list|(
name|values
argument_list|)
argument_list|)
control|)
block|{
name|eval
operator|.
name|merge
argument_list|(
name|buf
argument_list|,
name|partialResult
argument_list|)
expr_stmt|;
block|}
return|return
name|eval
operator|.
name|terminate
argument_list|(
name|buf
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|runPartial1
parameter_list|(
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|values
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|batchSize
init|=
literal|1
decl_stmt|;
name|Iterator
argument_list|<
name|Object
index|[]
argument_list|>
name|iter
init|=
name|values
operator|.
name|iterator
argument_list|()
decl_stmt|;
do|do
block|{
name|GenericUDAFEvaluator
name|eval
init|=
name|evaluatorFactory
operator|.
name|getEvaluator
argument_list|(
name|info
argument_list|)
decl_stmt|;
name|eval
operator|.
name|init
argument_list|(
name|GenericUDAFEvaluator
operator|.
name|Mode
operator|.
name|PARTIAL1
argument_list|,
name|info
operator|.
name|getParameterObjectInspectors
argument_list|()
argument_list|)
expr_stmt|;
name|AggregationBuffer
name|buf
init|=
name|eval
operator|.
name|getNewAggregationBuffer
argument_list|()
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
name|batchSize
operator|-
literal|1
operator|&&
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|eval
operator|.
name|iterate
argument_list|(
name|buf
argument_list|,
name|iter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|batchSize
operator|<<=
literal|1
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
name|eval
operator|.
name|terminatePartial
argument_list|(
name|buf
argument_list|)
argument_list|)
expr_stmt|;
comment|// back-check to force at least 1 output; and this should have a partial which is empty
block|}
do|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
do|;
return|return
name|ret
return|;
block|}
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|runPartial2
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|values
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|batchSize
init|=
literal|1
decl_stmt|;
name|Iterator
argument_list|<
name|Object
argument_list|>
name|iter
init|=
name|values
operator|.
name|iterator
argument_list|()
decl_stmt|;
do|do
block|{
name|GenericUDAFEvaluator
name|eval
init|=
name|evaluatorFactory
operator|.
name|getEvaluator
argument_list|(
name|info
argument_list|)
decl_stmt|;
name|eval
operator|.
name|init
argument_list|(
name|GenericUDAFEvaluator
operator|.
name|Mode
operator|.
name|PARTIAL2
argument_list|,
name|partialOIs
argument_list|)
expr_stmt|;
name|AggregationBuffer
name|buf
init|=
name|eval
operator|.
name|getNewAggregationBuffer
argument_list|()
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
name|batchSize
operator|-
literal|1
operator|&&
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|eval
operator|.
name|merge
argument_list|(
name|buf
argument_list|,
name|iter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|batchSize
operator|<<=
literal|1
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
name|eval
operator|.
name|terminatePartial
argument_list|(
name|buf
argument_list|)
argument_list|)
expr_stmt|;
comment|// back-check to force at least 1 output; and this should have a partial which is empty
block|}
do|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
do|;
return|return
name|ret
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|RowSetGenerator
block|{
specifier|public
specifier|static
interface|interface
name|FieldGenerator
block|{
specifier|public
name|Object
name|apply
parameter_list|(
name|int
name|rowIndex
parameter_list|)
function_decl|;
block|}
specifier|public
specifier|static
class|class
name|ConstantSequence
implements|implements
name|FieldGenerator
block|{
specifier|private
name|Object
name|constant
decl_stmt|;
specifier|public
name|ConstantSequence
parameter_list|(
name|Object
name|constant
parameter_list|)
block|{
name|this
operator|.
name|constant
operator|=
name|constant
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|apply
parameter_list|(
name|int
name|rowIndex
parameter_list|)
block|{
return|return
name|constant
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|DoubleSequence
implements|implements
name|FieldGenerator
block|{
specifier|private
name|int
name|offset
decl_stmt|;
specifier|public
name|DoubleSequence
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|apply
parameter_list|(
name|int
name|rowIndex
parameter_list|)
block|{
name|double
name|d
init|=
name|rowIndex
operator|+
name|offset
decl_stmt|;
return|return
name|d
return|;
block|}
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|generate
parameter_list|(
name|int
name|numRows
parameter_list|,
name|FieldGenerator
modifier|...
name|generators
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numRows
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|rowIdx
init|=
literal|0
init|;
name|rowIdx
operator|<
name|numRows
condition|;
name|rowIdx
operator|++
control|)
block|{
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|row
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldGenerator
name|g
range|:
name|generators
control|)
block|{
name|row
operator|.
name|add
argument_list|(
name|g
operator|.
name|apply
argument_list|(
name|rowIdx
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|add
argument_list|(
name|row
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
specifier|public
name|TestGenericUDAFBinarySetFunctions
parameter_list|(
name|String
name|label
parameter_list|,
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|rowSet
parameter_list|)
block|{
name|this
operator|.
name|rowSet
operator|=
name|rowSet
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|regr_count
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|count
argument_list|()
argument_list|,
operator|new
name|GenericUDAFBinarySetFunctions
operator|.
name|RegrCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|regr_sxx
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|sxx
argument_list|()
argument_list|,
operator|new
name|GenericUDAFBinarySetFunctions
operator|.
name|RegrSXX
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|regr_syy
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|syy
argument_list|()
argument_list|,
operator|new
name|GenericUDAFBinarySetFunctions
operator|.
name|RegrSYY
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|regr_sxy
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|sxy
argument_list|()
argument_list|,
operator|new
name|GenericUDAFBinarySetFunctions
operator|.
name|RegrSXY
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|regr_avgx
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|avgx
argument_list|()
argument_list|,
operator|new
name|GenericUDAFBinarySetFunctions
operator|.
name|RegrAvgX
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|regr_avgy
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|avgy
argument_list|()
argument_list|,
operator|new
name|GenericUDAFBinarySetFunctions
operator|.
name|RegrAvgY
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|regr_slope
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|slope
argument_list|()
argument_list|,
operator|new
name|GenericUDAFBinarySetFunctions
operator|.
name|RegrSlope
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|regr_r2
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|r2
argument_list|()
argument_list|,
operator|new
name|GenericUDAFBinarySetFunctions
operator|.
name|RegrR2
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|regr_intercept
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|intercept
argument_list|()
argument_list|,
operator|new
name|GenericUDAFBinarySetFunctions
operator|.
name|RegrIntercept
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Ignore
argument_list|(
literal|"HIVE-16178 should fix this"
argument_list|)
specifier|public
name|void
name|corr
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|corr
argument_list|()
argument_list|,
operator|new
name|GenericUDAFCorrelation
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|covar_pop
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|covar_pop
argument_list|()
argument_list|,
operator|new
name|GenericUDAFCovariance
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Ignore
argument_list|(
literal|"HIVE-16178 should fix this"
argument_list|)
specifier|public
name|void
name|covar_samp
parameter_list|()
throws|throws
name|Exception
block|{
name|RegrIntermediate
name|expected
init|=
name|RegrIntermediate
operator|.
name|computeFor
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
name|validateUDAF
argument_list|(
name|expected
operator|.
name|covar_samp
argument_list|()
argument_list|,
operator|new
name|GenericUDAFCovarianceSample
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateUDAF
parameter_list|(
name|Double
name|expectedResult
parameter_list|,
name|GenericUDAFResolver2
name|udaf
parameter_list|)
throws|throws
name|Exception
block|{
name|ObjectInspector
index|[]
name|params
init|=
operator|new
name|ObjectInspector
index|[]
block|{
name|javaDoubleObjectInspector
block|,
name|javaDoubleObjectInspector
block|}
decl_stmt|;
name|GenericUDAFParameterInfo
name|gpi
init|=
operator|new
name|SimpleGenericUDAFParameterInfo
argument_list|(
name|params
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|GenericUDAFExecutor
name|executor
init|=
operator|new
name|GenericUDAFExecutor
argument_list|(
name|udaf
argument_list|,
name|gpi
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|executor
operator|.
name|run
argument_list|(
name|rowSet
argument_list|)
decl_stmt|;
if|if
condition|(
name|expectedResult
operator|==
literal|null
condition|)
block|{
for|for
control|(
name|Object
name|v
range|:
name|values
control|)
block|{
name|assertNull
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|Object
name|v
range|:
name|values
control|)
block|{
if|if
condition|(
name|v
operator|instanceof
name|DoubleWritable
condition|)
block|{
name|assertEquals
argument_list|(
name|expectedResult
argument_list|,
operator|(
operator|(
name|DoubleWritable
operator|)
name|v
operator|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1e-10
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|expectedResult
argument_list|,
operator|(
operator|(
name|LongWritable
operator|)
name|v
operator|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1e-10
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|static
class|class
name|RegrIntermediate
block|{
specifier|public
name|double
name|sum_x2
decl_stmt|,
name|sum_y2
decl_stmt|;
specifier|public
name|double
name|sum_x
decl_stmt|,
name|sum_y
decl_stmt|;
specifier|public
name|double
name|sum_xy
decl_stmt|;
specifier|public
name|double
name|n
decl_stmt|;
specifier|public
name|void
name|add
parameter_list|(
name|Double
name|y
parameter_list|,
name|Double
name|x
parameter_list|)
block|{
if|if
condition|(
name|x
operator|==
literal|null
operator|||
name|y
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|sum_x2
operator|+=
name|x
operator|*
name|x
expr_stmt|;
name|sum_y2
operator|+=
name|y
operator|*
name|y
expr_stmt|;
name|sum_x
operator|+=
name|x
expr_stmt|;
name|sum_y
operator|+=
name|y
expr_stmt|;
name|sum_xy
operator|+=
name|x
operator|*
name|y
expr_stmt|;
name|n
operator|++
expr_stmt|;
block|}
specifier|public
name|Double
name|intercept
parameter_list|()
block|{
name|double
name|xx
init|=
name|n
operator|*
name|sum_x2
operator|-
name|sum_x
operator|*
name|sum_x
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|0
condition|)
return|return
literal|null
return|;
return|return
operator|(
name|sum_y
operator|*
name|sum_x2
operator|-
name|sum_x
operator|*
name|sum_xy
operator|)
operator|/
name|xx
return|;
block|}
specifier|public
name|Double
name|sxy
parameter_list|()
block|{
if|if
condition|(
name|n
operator|==
literal|0
condition|)
return|return
literal|null
return|;
return|return
name|sum_xy
operator|-
name|sum_x
operator|*
name|sum_y
operator|/
name|n
return|;
block|}
specifier|public
name|Double
name|covar_pop
parameter_list|()
block|{
if|if
condition|(
name|n
operator|==
literal|0
condition|)
return|return
literal|null
return|;
return|return
operator|(
name|sum_xy
operator|-
name|sum_x
operator|*
name|sum_y
operator|/
name|n
operator|)
operator|/
name|n
return|;
block|}
specifier|public
name|Double
name|covar_samp
parameter_list|()
block|{
if|if
condition|(
name|n
operator|<=
literal|1
condition|)
return|return
literal|null
return|;
return|return
operator|(
name|sum_xy
operator|-
name|sum_x
operator|*
name|sum_y
operator|/
name|n
operator|)
operator|/
operator|(
name|n
operator|-
literal|1
operator|)
return|;
block|}
specifier|public
name|Double
name|corr
parameter_list|()
block|{
name|double
name|xx
init|=
name|n
operator|*
name|sum_x2
operator|-
name|sum_x
operator|*
name|sum_x
decl_stmt|;
name|double
name|yy
init|=
name|n
operator|*
name|sum_y2
operator|-
name|sum_y
operator|*
name|sum_y
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|0
operator|||
name|xx
operator|==
literal|0.0d
operator|||
name|yy
operator|==
literal|0.0d
condition|)
return|return
literal|null
return|;
name|double
name|c
init|=
name|n
operator|*
name|sum_xy
operator|-
name|sum_x
operator|*
name|sum_y
decl_stmt|;
return|return
name|Math
operator|.
name|sqrt
argument_list|(
name|c
operator|*
name|c
operator|/
name|xx
operator|/
name|yy
argument_list|)
return|;
block|}
specifier|public
name|Double
name|r2
parameter_list|()
block|{
name|double
name|xx
init|=
name|n
operator|*
name|sum_x2
operator|-
name|sum_x
operator|*
name|sum_x
decl_stmt|;
name|double
name|yy
init|=
name|n
operator|*
name|sum_y2
operator|-
name|sum_y
operator|*
name|sum_y
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|0
operator|||
name|xx
operator|==
literal|0.0d
condition|)
return|return
literal|null
return|;
if|if
condition|(
name|yy
operator|==
literal|0.0d
condition|)
return|return
literal|1.0d
return|;
name|double
name|c
init|=
name|n
operator|*
name|sum_xy
operator|-
name|sum_x
operator|*
name|sum_y
decl_stmt|;
return|return
name|c
operator|*
name|c
operator|/
name|xx
operator|/
name|yy
return|;
block|}
specifier|public
name|Double
name|slope
parameter_list|()
block|{
if|if
condition|(
name|n
operator|==
literal|0
operator|||
name|n
operator|*
name|sum_x2
operator|==
name|sum_x
operator|*
name|sum_x
condition|)
return|return
literal|null
return|;
return|return
operator|(
name|n
operator|*
name|sum_xy
operator|-
name|sum_x
operator|*
name|sum_y
operator|)
operator|/
operator|(
name|n
operator|*
name|sum_x2
operator|-
name|sum_x
operator|*
name|sum_x
operator|)
return|;
block|}
specifier|public
name|Double
name|avgx
parameter_list|()
block|{
if|if
condition|(
name|n
operator|==
literal|0
condition|)
return|return
literal|null
return|;
return|return
name|sum_x
operator|/
name|n
return|;
block|}
specifier|public
name|Double
name|avgy
parameter_list|()
block|{
if|if
condition|(
name|n
operator|==
literal|0
condition|)
return|return
literal|null
return|;
return|return
name|sum_y
operator|/
name|n
return|;
block|}
specifier|public
name|Double
name|count
parameter_list|()
block|{
return|return
name|n
return|;
block|}
specifier|public
name|Double
name|sxx
parameter_list|()
block|{
if|if
condition|(
name|n
operator|==
literal|0
condition|)
return|return
literal|null
return|;
return|return
name|sum_x2
operator|-
name|sum_x
operator|*
name|sum_x
operator|/
name|n
return|;
block|}
specifier|public
name|Double
name|syy
parameter_list|()
block|{
if|if
condition|(
name|n
operator|==
literal|0
condition|)
return|return
literal|null
return|;
return|return
name|sum_y2
operator|-
name|sum_y
operator|*
name|sum_y
operator|/
name|n
return|;
block|}
specifier|public
specifier|static
name|RegrIntermediate
name|computeFor
parameter_list|(
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|rows
parameter_list|)
block|{
name|RegrIntermediate
name|ri
init|=
operator|new
name|RegrIntermediate
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
index|[]
name|objects
range|:
name|rows
control|)
block|{
name|ri
operator|.
name|add
argument_list|(
operator|(
name|Double
operator|)
name|objects
index|[
literal|0
index|]
argument_list|,
operator|(
name|Double
operator|)
name|objects
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|ri
return|;
block|}
block|}
block|}
end_class

end_unit

