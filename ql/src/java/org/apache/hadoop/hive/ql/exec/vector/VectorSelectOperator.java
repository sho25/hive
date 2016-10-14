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
name|Operator
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|Operator
argument_list|<
name|SelectDesc
argument_list|>
implements|implements
name|VectorizationContextRegion
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|VectorSelectDesc
name|vectorDesc
decl_stmt|;
specifier|protected
name|VectorExpression
index|[]
name|vExpressions
init|=
literal|null
decl_stmt|;
specifier|private
name|int
index|[]
name|projectedOutputColumns
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
comment|// Create a new outgoing vectorization context because column name map will change.
specifier|private
name|VectorizationContext
name|vOutContext
decl_stmt|;
specifier|public
name|VectorSelectOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
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
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|(
name|SelectDesc
operator|)
name|conf
expr_stmt|;
name|vectorDesc
operator|=
operator|(
name|VectorSelectDesc
operator|)
name|this
operator|.
name|conf
operator|.
name|getVectorDesc
argument_list|()
expr_stmt|;
name|vExpressions
operator|=
name|vectorDesc
operator|.
name|getSelectExpressions
argument_list|()
expr_stmt|;
name|projectedOutputColumns
operator|=
name|vectorDesc
operator|.
name|getProjectedOutputColumns
argument_list|()
expr_stmt|;
comment|/**      * Create a new vectorization context to create a new projection, but keep      * same output column manager must be inherited to track the scratch the columns.      */
name|vOutContext
operator|=
operator|new
name|VectorizationContext
argument_list|(
name|getName
argument_list|()
argument_list|,
name|vContext
argument_list|)
expr_stmt|;
name|vOutContext
operator|.
name|resetProjectionColumns
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
init|=
name|this
operator|.
name|conf
operator|.
name|getOutputColumnNames
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
name|projectedOutputColumns
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|String
name|columnName
init|=
name|outputColumnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|vOutContext
operator|.
name|addProjectionColumn
argument_list|(
name|columnName
argument_list|,
name|projectedOutputColumns
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Kryo ctor. */
annotation|@
name|VisibleForTesting
specifier|public
name|VectorSelectOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorSelectOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
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
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
comment|// Just forward the row as is
if|if
condition|(
name|conf
operator|.
name|isSelStarNoCompute
argument_list|()
condition|)
block|{
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
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
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
name|projectedOutputColumns
operator|.
name|length
expr_stmt|;
name|vrg
operator|.
name|projectedColumns
operator|=
name|this
operator|.
name|projectedOutputColumns
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
annotation|@
name|Override
specifier|public
name|VectorizationContext
name|getOuputVectorizationContext
parameter_list|()
block|{
return|return
name|vOutContext
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|SELECT
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|getOperatorName
argument_list|()
return|;
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
block|}
end_class

end_unit

