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
name|SelectOperator
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
name|api
operator|.
name|OperatorType
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
name|ObjectInspectorFactory
import|;
end_import

begin_comment
comment|/**  * Select operator implementation.  */
end_comment

begin_class
specifier|public
class|class
name|VectorSelectOperator
extends|extends
name|SelectOperator
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
name|VectorExpression
index|[]
name|vExpressions
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|int
index|[]
name|projectedColumns
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|VectorExpressionWriter
index|[]
name|valueWriters
init|=
literal|null
decl_stmt|;
specifier|public
name|VectorSelectOperator
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
operator|.
name|conf
operator|=
operator|(
name|SelectDesc
operator|)
name|conf
expr_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|colList
init|=
name|this
operator|.
name|conf
operator|.
name|getColList
argument_list|()
decl_stmt|;
name|vContext
operator|.
name|setOperatorType
argument_list|(
name|OperatorType
operator|.
name|SELECT
argument_list|)
expr_stmt|;
name|vExpressions
operator|=
operator|new
name|VectorExpression
index|[
name|colList
operator|.
name|size
argument_list|()
index|]
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
name|colList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|vExpressions
index|[
name|i
index|]
operator|=
name|vContext
operator|.
name|getVectorExpression
argument_list|(
name|colList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|columnName
init|=
name|this
operator|.
name|conf
operator|.
name|getOutputColumnNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|// Update column map with output column names
name|vContext
operator|.
name|addToColumnMap
argument_list|(
name|columnName
argument_list|,
name|vExpressions
index|[
name|i
index|]
operator|.
name|getOutputColumn
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|VectorSelectOperator
parameter_list|()
block|{   }
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
comment|// Just forward the row as is
if|if
condition|(
name|conf
operator|.
name|isSelStarNoCompute
argument_list|()
condition|)
block|{
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
return|return;
block|}
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|objectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|colList
init|=
name|conf
operator|.
name|getColList
argument_list|()
decl_stmt|;
name|valueWriters
operator|=
name|VectorExpressionWriterFactory
operator|.
name|getExpressionWriters
argument_list|(
name|colList
argument_list|)
expr_stmt|;
for|for
control|(
name|VectorExpressionWriter
name|vew
range|:
name|valueWriters
control|)
block|{
name|objectInspectors
operator|.
name|add
argument_list|(
name|vew
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|outputFieldNames
init|=
name|conf
operator|.
name|getOutputColumnNames
argument_list|()
decl_stmt|;
name|outputObjInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|outputFieldNames
argument_list|,
name|objectInspectors
argument_list|)
expr_stmt|;
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|projectedColumns
operator|=
operator|new
name|int
index|[
name|vExpressions
operator|.
name|length
index|]
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
name|projectedColumns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|projectedColumns
index|[
name|i
index|]
operator|=
name|vExpressions
index|[
name|i
index|]
operator|.
name|getOutputColumn
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|processOp
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Just forward the row as is
if|if
condition|(
name|conf
operator|.
name|isSelStarNoCompute
argument_list|()
condition|)
block|{
name|forward
argument_list|(
name|row
argument_list|,
name|inputObjInspectors
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
return|return;
block|}
name|VectorizedRowBatch
name|vrg
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
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
name|vExpressions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|vExpressions
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Error evaluating "
operator|+
name|conf
operator|.
name|getColList
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getExprString
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// Prepare output, set the projections
name|VectorExpressionWriter
index|[]
name|originalValueWriters
init|=
name|vrg
operator|.
name|valueWriters
decl_stmt|;
name|vrg
operator|.
name|setValueWriters
argument_list|(
name|valueWriters
argument_list|)
expr_stmt|;
name|int
index|[]
name|originalProjections
init|=
name|vrg
operator|.
name|projectedColumns
decl_stmt|;
name|int
name|originalProjectionSize
init|=
name|vrg
operator|.
name|projectionSize
decl_stmt|;
name|vrg
operator|.
name|projectionSize
operator|=
name|vExpressions
operator|.
name|length
expr_stmt|;
name|vrg
operator|.
name|projectedColumns
operator|=
name|this
operator|.
name|projectedColumns
expr_stmt|;
name|forward
argument_list|(
name|vrg
argument_list|,
name|outputObjInspector
argument_list|)
expr_stmt|;
comment|// Revert the projected columns back, because vrg will be re-used.
name|vrg
operator|.
name|projectionSize
operator|=
name|originalProjectionSize
expr_stmt|;
name|vrg
operator|.
name|projectedColumns
operator|=
name|originalProjections
expr_stmt|;
name|vrg
operator|.
name|valueWriters
operator|=
name|originalValueWriters
expr_stmt|;
block|}
specifier|static
specifier|public
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"SEL"
return|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getvExpressions
parameter_list|()
block|{
return|return
name|vExpressions
return|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getVExpressions
parameter_list|()
block|{
return|return
name|vExpressions
return|;
block|}
specifier|public
name|void
name|setvExpressions
parameter_list|(
name|VectorExpression
index|[]
name|vExpressions
parameter_list|)
block|{
name|this
operator|.
name|vExpressions
operator|=
name|vExpressions
expr_stmt|;
block|}
specifier|public
name|void
name|setVExpressions
parameter_list|(
name|VectorExpression
index|[]
name|vExpressions
parameter_list|)
block|{
name|this
operator|.
name|vExpressions
operator|=
name|vExpressions
expr_stmt|;
block|}
block|}
end_class

end_unit

