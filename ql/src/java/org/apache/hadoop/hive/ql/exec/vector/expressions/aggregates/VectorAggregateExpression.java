begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|aggregates
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|common
operator|.
name|type
operator|.
name|DataTypePhysicalVariation
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
name|ColumnVector
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
name|VectorAggregationBufferRow
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
name|VectorAggregationDesc
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
name|AggregationDesc
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
name|TypeInfo
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
name|Mode
import|;
end_import

begin_comment
comment|/**  * Base class for aggregation expressions.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorAggregateExpression
implements|implements
name|Serializable
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
specifier|final
name|VectorAggregationDesc
name|vecAggrDesc
decl_stmt|;
specifier|protected
specifier|final
name|VectorExpression
name|inputExpression
decl_stmt|;
specifier|protected
specifier|final
name|TypeInfo
name|inputTypeInfo
decl_stmt|;
specifier|protected
specifier|final
name|TypeInfo
name|outputTypeInfo
decl_stmt|;
specifier|protected
specifier|final
name|DataTypePhysicalVariation
name|outputDataTypePhysicalVariation
decl_stmt|;
specifier|protected
specifier|final
name|GenericUDAFEvaluator
operator|.
name|Mode
name|mode
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|AVERAGE_COUNT_FIELD_INDEX
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|AVERAGE_SUM_FIELD_INDEX
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|AVERAGE_SOURCE_FIELD_INDEX
init|=
literal|2
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|VARIANCE_COUNT_FIELD_INDEX
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|VARIANCE_SUM_FIELD_INDEX
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|VARIANCE_VARIANCE_FIELD_INDEX
init|=
literal|2
decl_stmt|;
comment|// This constructor is used to momentarily create the object so match can be called.
specifier|public
name|VectorAggregateExpression
parameter_list|()
block|{
name|this
operator|.
name|vecAggrDesc
operator|=
literal|null
expr_stmt|;
comment|// Null out final members.
name|inputExpression
operator|=
literal|null
expr_stmt|;
name|inputTypeInfo
operator|=
literal|null
expr_stmt|;
name|outputTypeInfo
operator|=
literal|null
expr_stmt|;
name|outputDataTypePhysicalVariation
operator|=
literal|null
expr_stmt|;
name|mode
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|VectorAggregateExpression
parameter_list|(
name|VectorAggregationDesc
name|vecAggrDesc
parameter_list|)
block|{
name|this
operator|.
name|vecAggrDesc
operator|=
name|vecAggrDesc
expr_stmt|;
name|inputExpression
operator|=
name|vecAggrDesc
operator|.
name|getInputExpression
argument_list|()
expr_stmt|;
if|if
condition|(
name|inputExpression
operator|!=
literal|null
condition|)
block|{
name|inputTypeInfo
operator|=
name|inputExpression
operator|.
name|getOutputTypeInfo
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|inputTypeInfo
operator|=
literal|null
expr_stmt|;
block|}
name|outputTypeInfo
operator|=
name|vecAggrDesc
operator|.
name|getOutputTypeInfo
argument_list|()
expr_stmt|;
name|outputDataTypePhysicalVariation
operator|=
name|vecAggrDesc
operator|.
name|getOutputDataTypePhysicalVariation
argument_list|()
expr_stmt|;
name|mode
operator|=
name|vecAggrDesc
operator|.
name|getAggrDesc
argument_list|()
operator|.
name|getMode
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorExpression
name|getInputExpression
parameter_list|()
block|{
return|return
name|inputExpression
return|;
block|}
specifier|public
name|TypeInfo
name|getOutputTypeInfo
parameter_list|()
block|{
return|return
name|outputTypeInfo
return|;
block|}
specifier|public
name|DataTypePhysicalVariation
name|getOutputDataTypePhysicalVariation
parameter_list|()
block|{
return|return
name|outputDataTypePhysicalVariation
return|;
block|}
comment|/**    * Buffer interface to store aggregates.    */
specifier|public
specifier|static
interface|interface
name|AggregationBuffer
extends|extends
name|Serializable
block|{
name|int
name|getVariableSize
parameter_list|()
function_decl|;
name|void
name|reset
parameter_list|()
function_decl|;
block|}
empty_stmt|;
comment|/*    *    VectorAggregateExpression()    *    VectorAggregateExpression(VectorAggregationDesc vecAggrDesc)    *    *    AggregationBuffer getNewAggregationBuffer()    *    void aggregateInput(AggregationBuffer agg, VectorizedRowBatch unit)    *    void aggregateInputSelection(VectorAggregationBufferRow[] aggregationBufferSets,    *                int aggregateIndex, VectorizedRowBatch vrg)    *    void reset(AggregationBuffer agg)    *    long getAggregationBufferFixedSize()    *    *    boolean matches(String name, ColumnVector.Type inputColVectorType,    *                ColumnVector.Type outputColVectorType, Mode mode)    *    assignRowColumn(VectorizedRowBatch batch, int batchIndex, int columnNum,    *                AggregationBuffer agg)    *    */
specifier|public
specifier|abstract
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
function_decl|;
specifier|public
specifier|abstract
name|void
name|aggregateInput
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|VectorizedRowBatch
name|unit
parameter_list|)
throws|throws
name|HiveException
function_decl|;
specifier|public
specifier|abstract
name|void
name|aggregateInputSelection
parameter_list|(
name|VectorAggregationBufferRow
index|[]
name|aggregationBufferSets
parameter_list|,
name|int
name|aggregateIndex
parameter_list|,
name|VectorizedRowBatch
name|vrg
parameter_list|)
throws|throws
name|HiveException
function_decl|;
specifier|public
specifier|abstract
name|void
name|reset
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
function_decl|;
specifier|public
specifier|abstract
name|long
name|getAggregationBufferFixedSize
parameter_list|()
function_decl|;
specifier|public
name|boolean
name|hasVariableSize
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
specifier|abstract
name|boolean
name|matches
parameter_list|(
name|String
name|name
parameter_list|,
name|ColumnVector
operator|.
name|Type
name|inputColVectorType
parameter_list|,
name|ColumnVector
operator|.
name|Type
name|outputColVectorType
parameter_list|,
name|Mode
name|mode
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|void
name|assignRowColumn
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
name|batchIndex
parameter_list|,
name|int
name|columnNum
parameter_list|,
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
function_decl|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|vecAggrDesc
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

