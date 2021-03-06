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
name|plan
operator|.
name|ptf
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
name|parse
operator|.
name|PTFInvocationSpec
operator|.
name|NullOrder
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
name|parse
operator|.
name|PTFInvocationSpec
operator|.
name|Order
import|;
end_import

begin_class
specifier|public
class|class
name|OrderExpressionDef
extends|extends
name|PTFExpressionDef
block|{
specifier|private
name|Order
name|order
decl_stmt|;
specifier|private
name|NullOrder
name|nullOrder
decl_stmt|;
specifier|public
name|OrderExpressionDef
parameter_list|()
block|{}
specifier|public
name|OrderExpressionDef
parameter_list|(
name|PTFExpressionDef
name|e
parameter_list|)
block|{
name|super
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|order
operator|=
name|Order
operator|.
name|ASC
expr_stmt|;
name|nullOrder
operator|=
name|NullOrder
operator|.
name|NULLS_FIRST
expr_stmt|;
block|}
specifier|public
name|Order
name|getOrder
parameter_list|()
block|{
return|return
name|order
return|;
block|}
specifier|public
name|void
name|setOrder
parameter_list|(
name|Order
name|order
parameter_list|)
block|{
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
block|}
specifier|public
name|NullOrder
name|getNullOrder
parameter_list|()
block|{
return|return
name|nullOrder
return|;
block|}
specifier|public
name|void
name|setNullOrder
parameter_list|(
name|NullOrder
name|nullOrder
parameter_list|)
block|{
name|this
operator|.
name|nullOrder
operator|=
name|nullOrder
expr_stmt|;
block|}
block|}
end_class

end_unit

