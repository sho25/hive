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
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|util
operator|.
name|VectorizedRowGroupGenUtil
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
name|ExprNodeDesc
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
name|ExprNodeGenericFuncDesc
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
name|OperatorDesc
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
name|SelectDesc
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
name|VectorSelectDesc
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
name|GenericUDF
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
name|GenericUDFOPPlus
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
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
comment|/**  * Unit tests for vectorized select operator.  */
end_comment

begin_class
specifier|public
class|class
name|TestVectorSelectOperator
block|{
specifier|static
class|class
name|ValidatorVectorSelectOperator
extends|extends
name|VectorSelectOperator
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|ValidatorVectorSelectOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
name|VectorizationContext
name|ctxt
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
argument_list|(
name|ctx
argument_list|,
name|ctxt
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|initializeOp
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * Override forward to do validation      */
annotation|@
name|Override
specifier|public
name|void
name|forward
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorizedRowBatch
name|vrg
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
name|int
index|[]
name|projections
init|=
name|vrg
operator|.
name|projectedColumns
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|vrg
operator|.
name|projectionSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|projections
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|projections
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|LongColumnVector
name|out0
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|projections
index|[
literal|0
index|]
index|]
decl_stmt|;
name|LongColumnVector
name|out1
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|projections
index|[
literal|1
index|]
index|]
decl_stmt|;
name|LongColumnVector
name|in0
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
name|in1
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
name|in2
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
name|in3
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
name|in0
operator|.
name|vector
index|[
name|i
index|]
operator|+
name|in1
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|out0
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|in3
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|out0
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|in2
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|out1
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSelectOperator
parameter_list|()
throws|throws
name|HiveException
block|{
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
literal|"a"
argument_list|)
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
literal|"c"
argument_list|)
expr_stmt|;
name|VectorizationContext
name|vc
init|=
operator|new
name|VectorizationContext
argument_list|(
literal|"name"
argument_list|,
name|columns
argument_list|)
decl_stmt|;
name|SelectDesc
name|selDesc
init|=
operator|new
name|SelectDesc
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|colList
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|ExprNodeColumnDesc
name|colDesc1
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|Long
operator|.
name|class
argument_list|,
literal|"a"
argument_list|,
literal|"table"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ExprNodeColumnDesc
name|colDesc2
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|Long
operator|.
name|class
argument_list|,
literal|"b"
argument_list|,
literal|"table"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ExprNodeColumnDesc
name|colDesc3
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|Long
operator|.
name|class
argument_list|,
literal|"c"
argument_list|,
literal|"table"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ExprNodeGenericFuncDesc
name|plusDesc
init|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|()
decl_stmt|;
name|GenericUDF
name|gudf
init|=
operator|new
name|GenericUDFOPPlus
argument_list|()
decl_stmt|;
name|plusDesc
operator|.
name|setGenericUDF
argument_list|(
name|gudf
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|colDesc1
argument_list|)
expr_stmt|;
name|children
operator|.
name|add
argument_list|(
name|colDesc2
argument_list|)
expr_stmt|;
name|plusDesc
operator|.
name|setChildren
argument_list|(
name|children
argument_list|)
expr_stmt|;
name|plusDesc
operator|.
name|setTypeInfo
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
expr_stmt|;
name|colList
operator|.
name|add
argument_list|(
name|plusDesc
argument_list|)
expr_stmt|;
name|colList
operator|.
name|add
argument_list|(
name|colDesc3
argument_list|)
expr_stmt|;
name|selDesc
operator|.
name|setColList
argument_list|(
name|colList
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|outputColNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|outputColNames
operator|.
name|add
argument_list|(
literal|"_col0"
argument_list|)
expr_stmt|;
name|outputColNames
operator|.
name|add
argument_list|(
literal|"_col1"
argument_list|)
expr_stmt|;
name|selDesc
operator|.
name|setOutputColumnNames
argument_list|(
name|outputColNames
argument_list|)
expr_stmt|;
comment|// CONSIDER unwinding ValidatorVectorSelectOperator as a subclass of VectorSelectOperator.
name|VectorSelectDesc
name|vectorSelectDesc
init|=
operator|new
name|VectorSelectDesc
argument_list|()
decl_stmt|;
name|selDesc
operator|.
name|setVectorDesc
argument_list|(
name|vectorSelectDesc
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|selectColList
init|=
name|selDesc
operator|.
name|getColList
argument_list|()
decl_stmt|;
name|VectorExpression
index|[]
name|vectorSelectExprs
init|=
operator|new
name|VectorExpression
index|[
name|selectColList
operator|.
name|size
argument_list|()
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
name|selectColList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ExprNodeDesc
name|expr
init|=
name|selectColList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|VectorExpression
name|ve
init|=
name|vc
operator|.
name|getVectorExpression
argument_list|(
name|expr
argument_list|)
decl_stmt|;
name|vectorSelectExprs
index|[
name|i
index|]
operator|=
name|ve
expr_stmt|;
block|}
name|vectorSelectDesc
operator|.
name|setSelectExpressions
argument_list|(
name|vectorSelectExprs
argument_list|)
expr_stmt|;
name|vectorSelectDesc
operator|.
name|setProjectedOutputColumns
argument_list|(
operator|new
name|int
index|[]
block|{
literal|3
block|,
literal|2
block|}
argument_list|)
expr_stmt|;
name|ValidatorVectorSelectOperator
name|vso
init|=
operator|new
name|ValidatorVectorSelectOperator
argument_list|(
operator|new
name|CompilationOpContext
argument_list|()
argument_list|,
name|vc
argument_list|,
name|selDesc
argument_list|)
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
literal|4
argument_list|,
literal|17
argument_list|)
decl_stmt|;
name|vso
operator|.
name|process
argument_list|(
name|vrg
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

