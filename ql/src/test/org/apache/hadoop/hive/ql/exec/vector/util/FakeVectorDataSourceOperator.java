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
name|util
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
name|api
operator|.
name|OperatorType
import|;
end_import

begin_comment
comment|/**  * Vectorized data source operator for testing.  * Used in unit test only.  *  */
end_comment

begin_class
specifier|public
class|class
name|FakeVectorDataSourceOperator
extends|extends
name|Operator
argument_list|<
name|FakeVectorDataSourceOperatorDesc
argument_list|>
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
specifier|private
specifier|transient
name|Iterable
argument_list|<
name|VectorizedRowBatch
argument_list|>
name|source
decl_stmt|;
specifier|public
specifier|static
name|FakeVectorDataSourceOperator
name|addFakeVectorDataSourceParent
parameter_list|(
name|Iterable
argument_list|<
name|VectorizedRowBatch
argument_list|>
name|source
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
name|FakeVectorDataSourceOperator
name|parent
init|=
operator|new
name|FakeVectorDataSourceOperator
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|listParents
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
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|listParents
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|op
operator|.
name|setParentOperators
argument_list|(
name|listParents
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|listChildren
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
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|listChildren
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
name|parent
operator|.
name|setChildOperators
argument_list|(
name|listChildren
argument_list|)
expr_stmt|;
return|return
name|parent
return|;
block|}
specifier|public
name|FakeVectorDataSourceOperator
parameter_list|(
name|Iterable
argument_list|<
name|VectorizedRowBatch
argument_list|>
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{   }
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
for|for
control|(
name|VectorizedRowBatch
name|unit
range|:
name|source
control|)
block|{
name|forward
argument_list|(
name|unit
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

