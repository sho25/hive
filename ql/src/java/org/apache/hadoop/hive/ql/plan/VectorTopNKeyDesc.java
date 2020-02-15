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
name|plan
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
name|expressions
operator|.
name|VectorExpression
import|;
end_import

begin_class
specifier|public
class|class
name|VectorTopNKeyDesc
extends|extends
name|AbstractVectorDesc
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
name|VectorExpression
index|[]
name|keyExpressions
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|partitionKeyColumns
decl_stmt|;
specifier|public
name|VectorTopNKeyDesc
parameter_list|()
block|{   }
specifier|public
name|VectorExpression
index|[]
name|getKeyExpressions
parameter_list|()
block|{
return|return
name|keyExpressions
return|;
block|}
specifier|public
name|void
name|setKeyExpressions
parameter_list|(
name|VectorExpression
index|[]
name|keyExpressions
parameter_list|)
block|{
name|this
operator|.
name|keyExpressions
operator|=
name|keyExpressions
expr_stmt|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getPartitionKeyColumns
parameter_list|()
block|{
return|return
name|partitionKeyColumns
return|;
block|}
specifier|public
name|void
name|setPartitionKeyColumns
parameter_list|(
name|VectorExpression
index|[]
name|partitionKeyColumns
parameter_list|)
block|{
name|this
operator|.
name|partitionKeyColumns
operator|=
name|partitionKeyColumns
expr_stmt|;
block|}
block|}
end_class

end_unit

