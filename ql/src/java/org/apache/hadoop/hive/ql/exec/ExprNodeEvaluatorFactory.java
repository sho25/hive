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
name|ExprNodeColumnDesc
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
name|ExprNodeConstantDesc
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
name|ExprNodeFieldDesc
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
name|ExprNodeGenericFuncDesc
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
name|ExprNodeNullDesc
import|;
end_import

begin_comment
comment|/**  * ExprNodeEvaluatorFactory.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ExprNodeEvaluatorFactory
block|{
specifier|private
name|ExprNodeEvaluatorFactory
parameter_list|()
block|{   }
specifier|public
specifier|static
name|ExprNodeEvaluator
name|get
parameter_list|(
name|ExprNodeDesc
name|desc
parameter_list|)
block|{
comment|// Constant node
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeConstantEvaluator
argument_list|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|desc
argument_list|)
return|;
block|}
comment|// Column-reference node, e.g. a column in the input row
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeColumnEvaluator
argument_list|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|desc
argument_list|)
return|;
block|}
comment|// Generic Function node, e.g. CASE, an operator or a UDF node
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeGenericFuncEvaluator
argument_list|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|desc
argument_list|)
return|;
block|}
comment|// Field node, e.g. get a.myfield1 from a
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeFieldDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeFieldEvaluator
argument_list|(
operator|(
name|ExprNodeFieldDesc
operator|)
name|desc
argument_list|)
return|;
block|}
comment|// Null node, a constant node with value NULL and no type information
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeNullDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeNullEvaluator
argument_list|(
operator|(
name|ExprNodeNullDesc
operator|)
name|desc
argument_list|)
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot find ExprNodeEvaluator for the exprNodeDesc = "
operator|+
name|desc
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

