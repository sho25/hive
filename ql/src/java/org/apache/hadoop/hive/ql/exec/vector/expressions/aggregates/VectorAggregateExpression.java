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
operator|.
name|aggregates
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|VectorAggregateExpression
block|{
specifier|public
specifier|static
interface|interface
name|AggregationBuffer
block|{
name|int
name|getVariableSize
parameter_list|()
function_decl|;
block|}
empty_stmt|;
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
name|Object
name|evaluateOutput
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
function_decl|;
specifier|public
specifier|abstract
name|ObjectInspector
name|getOutputObjectInspector
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|int
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
name|void
name|init
parameter_list|(
name|AggregationDesc
name|desc
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
end_class

end_unit

