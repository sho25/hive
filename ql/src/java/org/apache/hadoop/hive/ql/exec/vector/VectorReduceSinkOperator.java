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
name|exec
operator|.
name|ReduceSinkOperator
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
name|VectorExpressionWriter
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
name|VectorExpressionWriterFactory
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
name|ReduceSinkDesc
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
name|objectinspector
operator|.
name|StructObjectInspector
import|;
end_import

begin_class
specifier|public
class|class
name|VectorReduceSinkOperator
extends|extends
name|ReduceSinkOperator
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// Writer for producing row from input batch.
specifier|private
name|VectorExpressionWriter
index|[]
name|rowWriters
decl_stmt|;
specifier|protected
specifier|transient
name|Object
index|[]
name|singleRow
decl_stmt|;
specifier|public
name|VectorReduceSinkOperator
parameter_list|(
name|VectorizationContext
name|vContext
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|()
expr_stmt|;
name|ReduceSinkDesc
name|desc
init|=
operator|(
name|ReduceSinkDesc
operator|)
name|conf
decl_stmt|;
name|this
operator|.
name|conf
operator|=
name|desc
expr_stmt|;
block|}
specifier|public
name|VectorReduceSinkOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// We need a input object inspector that is for the row we will extract out of the
comment|// vectorized row batch, not for example, an original inspector for an ORC table, etc.
name|VectorExpressionWriterFactory
operator|.
name|processVectorInspector
argument_list|(
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|,
operator|new
name|VectorExpressionWriterFactory
operator|.
name|SingleOIDClosure
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|assign
parameter_list|(
name|VectorExpressionWriter
index|[]
name|writers
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|)
block|{
name|rowWriters
operator|=
name|writers
expr_stmt|;
name|inputObjInspectors
index|[
literal|0
index|]
operator|=
name|objectInspector
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|singleRow
operator|=
operator|new
name|Object
index|[
name|rowWriters
operator|.
name|length
index|]
expr_stmt|;
comment|// Call ReduceSinkOperator with new input inspector.
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processOp
parameter_list|(
name|Object
name|data
parameter_list|,
name|int
name|tag
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
name|data
decl_stmt|;
for|for
control|(
name|int
name|batchIndex
init|=
literal|0
init|;
name|batchIndex
operator|<
name|vrg
operator|.
name|size
condition|;
operator|++
name|batchIndex
control|)
block|{
name|Object
name|row
init|=
name|getRowObject
argument_list|(
name|vrg
argument_list|,
name|batchIndex
argument_list|)
decl_stmt|;
name|super
operator|.
name|processOp
argument_list|(
name|row
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Object
index|[]
name|getRowObject
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|,
name|int
name|rowIndex
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|batchIndex
init|=
name|rowIndex
decl_stmt|;
if|if
condition|(
name|vrg
operator|.
name|selectedInUse
condition|)
block|{
name|batchIndex
operator|=
name|vrg
operator|.
name|selected
index|[
name|rowIndex
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|vrg
operator|.
name|projectionSize
condition|;
name|i
operator|++
control|)
block|{
name|ColumnVector
name|vectorColumn
init|=
name|vrg
operator|.
name|cols
index|[
name|vrg
operator|.
name|projectedColumns
index|[
name|i
index|]
index|]
decl_stmt|;
if|if
condition|(
name|vectorColumn
operator|!=
literal|null
condition|)
block|{
name|singleRow
index|[
name|i
index|]
operator|=
name|rowWriters
index|[
name|i
index|]
operator|.
name|writeValue
argument_list|(
name|vectorColumn
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Some columns from tables are not used.
name|singleRow
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
return|return
name|singleRow
return|;
block|}
block|}
end_class

end_unit

