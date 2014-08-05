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
name|ExprNodeEvaluator
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
name|ExprNodeEvaluatorFactory
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
name|ExtractOperator
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
name|Utilities
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
name|ExtractDesc
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
name|StructField
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

begin_comment
comment|/**  * Vectorized extract operator implementation.  Consumes rows and outputs a  * vectorized batch of subobjects.  **/
end_comment

begin_class
specifier|public
class|class
name|VectorExtractOperator
extends|extends
name|ExtractOperator
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
name|int
name|keyColCount
decl_stmt|;
specifier|private
name|int
name|valueColCount
decl_stmt|;
specifier|private
specifier|transient
name|int
index|[]
name|projectedColumns
init|=
literal|null
decl_stmt|;
specifier|public
name|VectorExtractOperator
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
name|this
operator|.
name|conf
operator|=
operator|(
name|ExtractDesc
operator|)
name|conf
expr_stmt|;
block|}
specifier|public
name|VectorExtractOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|private
name|StructObjectInspector
name|makeStandardStructObjectInspector
parameter_list|(
name|StructObjectInspector
name|structObjectInspector
parameter_list|)
block|{
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|structObjectInspector
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|colNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|StructField
name|field
range|:
name|fields
control|)
block|{
name|colNames
operator|.
name|add
argument_list|(
name|field
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|colNames
argument_list|,
name|ois
argument_list|)
return|;
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
name|outputObjInspector
operator|=
name|inputObjInspectors
index|[
literal|0
index|]
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"VectorExtractOperator class of outputObjInspector is "
operator|+
name|outputObjInspector
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|projectedColumns
operator|=
operator|new
name|int
index|[
name|valueColCount
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
name|valueColCount
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
name|keyColCount
operator|+
name|i
expr_stmt|;
block|}
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setKeyAndValueColCounts
parameter_list|(
name|int
name|keyColCount
parameter_list|,
name|int
name|valueColCount
parameter_list|)
block|{
name|this
operator|.
name|keyColCount
operator|=
name|keyColCount
expr_stmt|;
name|this
operator|.
name|valueColCount
operator|=
name|valueColCount
expr_stmt|;
block|}
annotation|@
name|Override
comment|// Evaluate vectorized batches of rows and forward them.
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
name|VectorizedRowBatch
name|vrg
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
comment|// Project away the key columns...
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
name|valueColCount
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
block|}
block|}
end_class

end_unit

