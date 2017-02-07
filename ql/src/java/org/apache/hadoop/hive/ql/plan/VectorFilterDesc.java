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

begin_comment
comment|/**  * VectorFilterDesc.  *  * Extra parameters beyond FilterDesc just for the VectorFilterOperator.  *  * We don't extend FilterDesc because the base OperatorDesc doesn't support  * clone and adding it is a lot work for little gain.  */
end_comment

begin_class
specifier|public
class|class
name|VectorFilterDesc
extends|extends
name|AbstractVectorDesc
block|{
specifier|private
specifier|static
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|VectorExpression
name|predicateExpression
decl_stmt|;
specifier|public
name|VectorFilterDesc
parameter_list|()
block|{   }
specifier|public
name|void
name|setPredicateExpression
parameter_list|(
name|VectorExpression
name|predicateExpression
parameter_list|)
block|{
name|this
operator|.
name|predicateExpression
operator|=
name|predicateExpression
expr_stmt|;
block|}
specifier|public
name|VectorExpression
name|getPredicateExpression
parameter_list|()
block|{
return|return
name|predicateExpression
return|;
block|}
block|}
end_class

end_unit

