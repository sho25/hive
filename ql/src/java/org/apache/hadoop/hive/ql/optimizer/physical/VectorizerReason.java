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
name|optimizer
operator|.
name|physical
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
name|plan
operator|.
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * Why a node did not vectorize.  *  */
end_comment

begin_class
specifier|public
class|class
name|VectorizerReason
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|VectorizerNodeIssue
block|{
name|NONE
block|,
name|NODE_ISSUE
block|,
name|OPERATOR_ISSUE
block|,
name|EXPRESSION_ISSUE
block|}
specifier|private
specifier|final
name|VectorizerNodeIssue
name|vectorizerNodeIssue
decl_stmt|;
specifier|private
specifier|final
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
decl_stmt|;
specifier|private
specifier|final
name|String
name|expressionTitle
decl_stmt|;
specifier|private
specifier|final
name|String
name|issue
decl_stmt|;
specifier|private
name|VectorizerReason
parameter_list|(
name|VectorizerNodeIssue
name|vectorizerNodeIssue
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
parameter_list|,
name|String
name|expressionTitle
parameter_list|,
name|String
name|issue
parameter_list|)
block|{
name|this
operator|.
name|vectorizerNodeIssue
operator|=
name|vectorizerNodeIssue
expr_stmt|;
name|this
operator|.
name|operator
operator|=
name|operator
expr_stmt|;
name|this
operator|.
name|expressionTitle
operator|=
name|expressionTitle
expr_stmt|;
name|this
operator|.
name|issue
operator|=
name|issue
expr_stmt|;
block|}
specifier|public
specifier|static
name|VectorizerReason
name|createNodeIssue
parameter_list|(
name|String
name|issue
parameter_list|)
block|{
return|return
operator|new
name|VectorizerReason
argument_list|(
name|VectorizerNodeIssue
operator|.
name|NODE_ISSUE
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|issue
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|VectorizerReason
name|createOperatorIssue
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
parameter_list|,
name|String
name|issue
parameter_list|)
block|{
return|return
operator|new
name|VectorizerReason
argument_list|(
name|VectorizerNodeIssue
operator|.
name|OPERATOR_ISSUE
argument_list|,
name|operator
argument_list|,
literal|null
argument_list|,
name|issue
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|VectorizerReason
name|createExpressionIssue
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
parameter_list|,
name|String
name|expressionTitle
parameter_list|,
name|String
name|issue
parameter_list|)
block|{
return|return
operator|new
name|VectorizerReason
argument_list|(
name|VectorizerNodeIssue
operator|.
name|EXPRESSION_ISSUE
argument_list|,
name|operator
argument_list|,
name|expressionTitle
argument_list|,
name|issue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizerReason
name|clone
parameter_list|()
block|{
return|return
operator|new
name|VectorizerReason
argument_list|(
name|vectorizerNodeIssue
argument_list|,
name|operator
argument_list|,
name|expressionTitle
argument_list|,
name|issue
argument_list|)
return|;
block|}
specifier|public
name|VectorizerNodeIssue
name|getVectorizerNodeIssue
parameter_list|()
block|{
return|return
name|vectorizerNodeIssue
return|;
block|}
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|getOperator
parameter_list|()
block|{
return|return
name|operator
return|;
block|}
specifier|public
name|String
name|getExpressionTitle
parameter_list|()
block|{
return|return
name|expressionTitle
return|;
block|}
specifier|public
name|String
name|getIssue
parameter_list|()
block|{
return|return
name|issue
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|reason
decl_stmt|;
switch|switch
condition|(
name|vectorizerNodeIssue
condition|)
block|{
case|case
name|NODE_ISSUE
case|:
name|reason
operator|=
operator|(
name|issue
operator|==
literal|null
condition|?
literal|"unknown"
else|:
name|issue
operator|)
expr_stmt|;
break|break;
case|case
name|OPERATOR_ISSUE
case|:
name|reason
operator|=
operator|(
name|operator
operator|==
literal|null
condition|?
literal|"Unknown"
else|:
name|operator
operator|.
name|getType
argument_list|()
operator|)
operator|+
literal|" operator: "
operator|+
operator|(
name|issue
operator|==
literal|null
condition|?
literal|"unknown"
else|:
name|issue
operator|)
expr_stmt|;
break|break;
case|case
name|EXPRESSION_ISSUE
case|:
name|reason
operator|=
name|expressionTitle
operator|+
literal|" expression for "
operator|+
operator|(
name|operator
operator|==
literal|null
condition|?
literal|"Unknown"
else|:
name|operator
operator|.
name|getType
argument_list|()
operator|)
operator|+
literal|" operator: "
operator|+
operator|(
name|issue
operator|==
literal|null
condition|?
literal|"unknown"
else|:
name|issue
operator|)
expr_stmt|;
break|break;
default|default:
name|reason
operator|=
literal|"Unknown "
operator|+
name|vectorizerNodeIssue
expr_stmt|;
block|}
return|return
name|reason
return|;
block|}
block|}
end_class

end_unit

