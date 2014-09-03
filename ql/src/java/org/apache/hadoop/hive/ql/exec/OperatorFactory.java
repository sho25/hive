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
name|exec
operator|.
name|vector
operator|.
name|VectorExtractOperator
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
name|VectorFileSinkOperator
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
name|VectorFilterOperator
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
name|VectorGroupByOperator
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
name|VectorLimitOperator
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
name|VectorMapJoinOperator
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
name|VectorReduceSinkOperator
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
name|VectorSMBMapJoinOperator
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
name|VectorSelectOperator
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
name|VectorizationContext
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
name|AppMasterEventDesc
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
name|CollectDesc
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
name|DemuxDesc
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
name|DummyStoreDesc
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
name|DynamicPruningEventDesc
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
name|FileSinkDesc
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
name|ForwardDesc
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
name|GroupByDesc
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
name|HashTableDummyDesc
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
name|HashTableSinkDesc
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
name|JoinDesc
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
name|LateralViewForwardDesc
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
name|LateralViewJoinDesc
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
name|LimitDesc
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
name|MapJoinDesc
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
name|MuxDesc
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
name|PTFDesc
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
name|ql
operator|.
name|plan
operator|.
name|SMBJoinDesc
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
name|ScriptDesc
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
name|TableScanDesc
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
name|UDTFDesc
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
name|UnionDesc
import|;
end_import

begin_comment
comment|/**  * OperatorFactory.  *  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
specifier|final
class|class
name|OperatorFactory
block|{
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|OpTuple
argument_list|>
name|opvec
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|OpTuple
argument_list|>
name|vectorOpvec
decl_stmt|;
static|static
block|{
name|opvec
operator|=
operator|new
name|ArrayList
argument_list|<
name|OpTuple
argument_list|>
argument_list|()
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|FilterDesc
argument_list|>
argument_list|(
name|FilterDesc
operator|.
name|class
argument_list|,
name|FilterOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|SelectDesc
argument_list|>
argument_list|(
name|SelectDesc
operator|.
name|class
argument_list|,
name|SelectOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|ForwardDesc
argument_list|>
argument_list|(
name|ForwardDesc
operator|.
name|class
argument_list|,
name|ForwardOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|FileSinkDesc
argument_list|>
argument_list|(
name|FileSinkDesc
operator|.
name|class
argument_list|,
name|FileSinkOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|CollectDesc
argument_list|>
argument_list|(
name|CollectDesc
operator|.
name|class
argument_list|,
name|CollectOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|ScriptDesc
argument_list|>
argument_list|(
name|ScriptDesc
operator|.
name|class
argument_list|,
name|ScriptOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|PTFDesc
argument_list|>
argument_list|(
name|PTFDesc
operator|.
name|class
argument_list|,
name|PTFOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|ReduceSinkDesc
argument_list|>
argument_list|(
name|ReduceSinkDesc
operator|.
name|class
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|ExtractDesc
argument_list|>
argument_list|(
name|ExtractDesc
operator|.
name|class
argument_list|,
name|ExtractOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|GroupByDesc
argument_list|>
argument_list|(
name|GroupByDesc
operator|.
name|class
argument_list|,
name|GroupByOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|JoinDesc
argument_list|>
argument_list|(
name|JoinDesc
operator|.
name|class
argument_list|,
name|JoinOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|MapJoinDesc
argument_list|>
argument_list|(
name|MapJoinDesc
operator|.
name|class
argument_list|,
name|MapJoinOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|SMBJoinDesc
argument_list|>
argument_list|(
name|SMBJoinDesc
operator|.
name|class
argument_list|,
name|SMBMapJoinOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|LimitDesc
argument_list|>
argument_list|(
name|LimitDesc
operator|.
name|class
argument_list|,
name|LimitOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|TableScanDesc
argument_list|>
argument_list|(
name|TableScanDesc
operator|.
name|class
argument_list|,
name|TableScanOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|UnionDesc
argument_list|>
argument_list|(
name|UnionDesc
operator|.
name|class
argument_list|,
name|UnionOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|UDTFDesc
argument_list|>
argument_list|(
name|UDTFDesc
operator|.
name|class
argument_list|,
name|UDTFOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|LateralViewJoinDesc
argument_list|>
argument_list|(
name|LateralViewJoinDesc
operator|.
name|class
argument_list|,
name|LateralViewJoinOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|LateralViewForwardDesc
argument_list|>
argument_list|(
name|LateralViewForwardDesc
operator|.
name|class
argument_list|,
name|LateralViewForwardOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|HashTableDummyDesc
argument_list|>
argument_list|(
name|HashTableDummyDesc
operator|.
name|class
argument_list|,
name|HashTableDummyOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|HashTableSinkDesc
argument_list|>
argument_list|(
name|HashTableSinkDesc
operator|.
name|class
argument_list|,
name|HashTableSinkOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|DummyStoreDesc
argument_list|>
argument_list|(
name|DummyStoreDesc
operator|.
name|class
argument_list|,
name|DummyStoreOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|DemuxDesc
argument_list|>
argument_list|(
name|DemuxDesc
operator|.
name|class
argument_list|,
name|DemuxOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|MuxDesc
argument_list|>
argument_list|(
name|MuxDesc
operator|.
name|class
argument_list|,
name|MuxOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|AppMasterEventDesc
argument_list|>
argument_list|(
name|AppMasterEventDesc
operator|.
name|class
argument_list|,
name|AppMasterEventOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|DynamicPruningEventDesc
argument_list|>
argument_list|(
name|DynamicPruningEventDesc
operator|.
name|class
argument_list|,
name|AppMasterEventOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
static|static
block|{
name|vectorOpvec
operator|=
operator|new
name|ArrayList
argument_list|<
name|OpTuple
argument_list|>
argument_list|()
expr_stmt|;
name|vectorOpvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|SelectDesc
argument_list|>
argument_list|(
name|SelectDesc
operator|.
name|class
argument_list|,
name|VectorSelectOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|vectorOpvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|GroupByDesc
argument_list|>
argument_list|(
name|GroupByDesc
operator|.
name|class
argument_list|,
name|VectorGroupByOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|vectorOpvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|MapJoinDesc
argument_list|>
argument_list|(
name|MapJoinDesc
operator|.
name|class
argument_list|,
name|VectorMapJoinOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|vectorOpvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|SMBJoinDesc
argument_list|>
argument_list|(
name|SMBJoinDesc
operator|.
name|class
argument_list|,
name|VectorSMBMapJoinOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|vectorOpvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|ReduceSinkDesc
argument_list|>
argument_list|(
name|ReduceSinkDesc
operator|.
name|class
argument_list|,
name|VectorReduceSinkOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|vectorOpvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|FileSinkDesc
argument_list|>
argument_list|(
name|FileSinkDesc
operator|.
name|class
argument_list|,
name|VectorFileSinkOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|vectorOpvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|FilterDesc
argument_list|>
argument_list|(
name|FilterDesc
operator|.
name|class
argument_list|,
name|VectorFilterOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|vectorOpvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|LimitDesc
argument_list|>
argument_list|(
name|LimitDesc
operator|.
name|class
argument_list|,
name|VectorLimitOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|vectorOpvec
operator|.
name|add
argument_list|(
operator|new
name|OpTuple
argument_list|<
name|ExtractDesc
argument_list|>
argument_list|(
name|ExtractDesc
operator|.
name|class
argument_list|,
name|VectorExtractOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|OpTuple
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
block|{
specifier|private
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|descClass
decl_stmt|;
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opClass
decl_stmt|;
specifier|public
name|OpTuple
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|descClass
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opClass
parameter_list|)
block|{
name|this
operator|.
name|descClass
operator|=
name|descClass
expr_stmt|;
name|this
operator|.
name|opClass
operator|=
name|opClass
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|getVectorOperator
parameter_list|(
name|T
name|conf
parameter_list|,
name|VectorizationContext
name|vContext
parameter_list|)
throws|throws
name|HiveException
block|{
name|Class
argument_list|<
name|T
argument_list|>
name|descClass
init|=
operator|(
name|Class
argument_list|<
name|T
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|()
decl_stmt|;
for|for
control|(
name|OpTuple
name|o
range|:
name|vectorOpvec
control|)
block|{
if|if
condition|(
name|o
operator|.
name|descClass
operator|==
name|descClass
condition|)
block|{
try|try
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|op
init|=
operator|(
name|Operator
argument_list|<
name|T
argument_list|>
operator|)
name|o
operator|.
name|opClass
operator|.
name|getDeclaredConstructor
argument_list|(
name|VectorizationContext
operator|.
name|class
argument_list|,
name|OperatorDesc
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|(
name|vContext
argument_list|,
name|conf
argument_list|)
decl_stmt|;
return|return
name|op
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"No vector operator for descriptor class "
operator|+
name|descClass
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|opClass
parameter_list|)
block|{
for|for
control|(
name|OpTuple
name|o
range|:
name|opvec
control|)
block|{
if|if
condition|(
name|o
operator|.
name|descClass
operator|==
name|opClass
condition|)
block|{
try|try
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|op
init|=
operator|(
name|Operator
argument_list|<
name|T
argument_list|>
operator|)
name|o
operator|.
name|opClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
return|return
name|op
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"No operator for descriptor class "
operator|+
name|opClass
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|opClass
parameter_list|,
name|RowSchema
name|rwsch
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
name|opClass
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setSchema
argument_list|(
name|rwsch
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of children operators.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|T
name|conf
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
modifier|...
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
operator|(
name|Class
argument_list|<
name|T
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|makeChild
argument_list|(
name|ret
argument_list|,
name|oplist
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of children operators.    */
specifier|public
specifier|static
name|void
name|makeChild
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|ret
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
modifier|...
name|oplist
parameter_list|)
block|{
if|if
condition|(
name|oplist
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|clist
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|oplist
control|)
block|{
name|clist
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|setChildOperators
argument_list|(
name|clist
argument_list|)
expr_stmt|;
comment|// Add this parent to the children
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|oplist
control|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parents
init|=
name|op
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|parents
operator|==
literal|null
condition|)
block|{
name|parents
operator|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|parents
operator|.
name|add
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|op
operator|.
name|setParentOperators
argument_list|(
name|parents
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Returns an operator given the conf and a list of children operators.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|T
name|conf
parameter_list|,
name|RowSchema
name|rwsch
parameter_list|,
name|Operator
modifier|...
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
name|conf
argument_list|,
name|oplist
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setSchema
argument_list|(
name|rwsch
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of parent operators.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|getAndMakeChild
parameter_list|(
name|T
name|conf
parameter_list|,
name|Operator
modifier|...
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
operator|(
name|Class
argument_list|<
name|T
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|oplist
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|// Add the new operator as child of each of the passed in operators
for|for
control|(
name|Operator
name|op
range|:
name|oplist
control|)
block|{
name|List
argument_list|<
name|Operator
argument_list|>
name|children
init|=
name|op
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
block|{
name|children
operator|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|children
operator|.
name|add
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|op
operator|.
name|setChildOperators
argument_list|(
name|children
argument_list|)
expr_stmt|;
block|}
comment|// add parents for the newly created operator
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parent
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
name|op
range|:
name|oplist
control|)
block|{
name|parent
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|setParentOperators
argument_list|(
name|parent
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of parent operators.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|getAndMakeChild
parameter_list|(
name|T
name|conf
parameter_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
operator|(
name|Class
argument_list|<
name|T
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|oplist
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|// Add the new operator as child of each of the passed in operators
for|for
control|(
name|Operator
name|op
range|:
name|oplist
control|)
block|{
name|List
argument_list|<
name|Operator
argument_list|>
name|children
init|=
name|op
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
block|{
name|children
operator|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|children
operator|.
name|add
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|op
operator|.
name|setChildOperators
argument_list|(
name|children
argument_list|)
expr_stmt|;
block|}
comment|// add parents for the newly created operator
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parent
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
name|op
range|:
name|oplist
control|)
block|{
name|parent
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|setParentOperators
argument_list|(
name|parent
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of parent operators.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|getAndMakeChild
parameter_list|(
name|T
name|conf
parameter_list|,
name|RowSchema
name|rwsch
parameter_list|,
name|Operator
modifier|...
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|getAndMakeChild
argument_list|(
name|conf
argument_list|,
name|oplist
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setSchema
argument_list|(
name|rwsch
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of parent operators.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|getAndMakeChild
parameter_list|(
name|T
name|conf
parameter_list|,
name|RowSchema
name|rwsch
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|colExprMap
parameter_list|,
name|Operator
modifier|...
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|getAndMakeChild
argument_list|(
name|conf
argument_list|,
name|rwsch
argument_list|,
name|oplist
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setColumnExprMap
argument_list|(
name|colExprMap
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of parent operators.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|getAndMakeChild
parameter_list|(
name|T
name|conf
parameter_list|,
name|RowSchema
name|rwsch
parameter_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|getAndMakeChild
argument_list|(
name|conf
argument_list|,
name|oplist
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setSchema
argument_list|(
name|rwsch
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of parent operators.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|OperatorDesc
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|getAndMakeChild
parameter_list|(
name|T
name|conf
parameter_list|,
name|RowSchema
name|rwsch
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|colExprMap
parameter_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|getAndMakeChild
argument_list|(
name|conf
argument_list|,
name|rwsch
argument_list|,
name|oplist
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setColumnExprMap
argument_list|(
name|colExprMap
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
specifier|private
name|OperatorFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

