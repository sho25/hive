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
operator|.
name|ptf
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
name|hive
operator|.
name|ql
operator|.
name|parse
operator|.
name|PTFInvocationSpec
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
name|Explain
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
name|Explain
operator|.
name|Level
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
name|ptf
operator|.
name|TableFunctionEvaluator
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Partition table definition"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|PartitionedTableFunctionDef
extends|extends
name|PTFInputDef
block|{
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|resolverClassName
decl_stmt|;
specifier|private
name|ShapeDetails
name|rawInputShape
decl_stmt|;
specifier|private
name|boolean
name|carryForwardNames
decl_stmt|;
specifier|private
name|PTFInputDef
name|input
decl_stmt|;
specifier|private
name|List
argument_list|<
name|PTFExpressionDef
argument_list|>
name|args
decl_stmt|;
specifier|private
name|PartitionDef
name|partition
decl_stmt|;
specifier|private
name|OrderDef
name|order
decl_stmt|;
specifier|private
name|TableFunctionEvaluator
name|tFunction
decl_stmt|;
name|boolean
name|transformsRawInput
decl_stmt|;
specifier|private
specifier|transient
name|List
argument_list|<
name|String
argument_list|>
name|referencedColumns
decl_stmt|;
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|ShapeDetails
name|getRawInputShape
parameter_list|()
block|{
return|return
name|rawInputShape
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"raw input shape"
argument_list|)
specifier|public
name|ShapeDetails
name|getRawInputShapeExplain
parameter_list|()
block|{
return|return
name|rawInputShape
return|;
block|}
specifier|public
name|void
name|setRawInputShape
parameter_list|(
name|ShapeDetails
name|rawInputShape
parameter_list|)
block|{
name|this
operator|.
name|rawInputShape
operator|=
name|rawInputShape
expr_stmt|;
block|}
specifier|public
name|boolean
name|isCarryForwardNames
parameter_list|()
block|{
return|return
name|carryForwardNames
return|;
block|}
specifier|public
name|void
name|setCarryForwardNames
parameter_list|(
name|boolean
name|carryForwardNames
parameter_list|)
block|{
name|this
operator|.
name|carryForwardNames
operator|=
name|carryForwardNames
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|PTFInputDef
name|getInput
parameter_list|()
block|{
return|return
name|input
return|;
block|}
specifier|public
name|void
name|setInput
parameter_list|(
name|PTFInputDef
name|input
parameter_list|)
block|{
name|this
operator|.
name|input
operator|=
name|input
expr_stmt|;
block|}
specifier|public
name|PartitionDef
name|getPartition
parameter_list|()
block|{
return|return
name|partition
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition by"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getPartitionExplain
parameter_list|()
block|{
if|if
condition|(
name|partition
operator|==
literal|null
operator|||
name|partition
operator|.
name|getExpressions
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|PTFExpressionDef
name|expression
range|:
name|partition
operator|.
name|getExpressions
argument_list|()
control|)
block|{
if|if
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|expression
operator|.
name|getExprNode
argument_list|()
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|setPartition
parameter_list|(
name|PartitionDef
name|partition
parameter_list|)
block|{
name|this
operator|.
name|partition
operator|=
name|partition
expr_stmt|;
block|}
specifier|public
name|OrderDef
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
name|OrderDef
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"order by"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getOrderExplain
parameter_list|()
block|{
if|if
condition|(
name|order
operator|==
literal|null
operator|||
name|order
operator|.
name|getExpressions
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|OrderExpressionDef
name|expression
range|:
name|order
operator|.
name|getExpressions
argument_list|()
control|)
block|{
if|if
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|expression
operator|.
name|getExprNode
argument_list|()
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|expression
operator|.
name|getOrder
argument_list|()
operator|==
name|PTFInvocationSpec
operator|.
name|Order
operator|.
name|DESC
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"(DESC)"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|TableFunctionEvaluator
name|getTFunction
parameter_list|()
block|{
return|return
name|tFunction
return|;
block|}
specifier|public
name|void
name|setTFunction
parameter_list|(
name|TableFunctionEvaluator
name|tFunction
parameter_list|)
block|{
name|this
operator|.
name|tFunction
operator|=
name|tFunction
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|PTFExpressionDef
argument_list|>
name|getArgs
parameter_list|()
block|{
return|return
name|args
return|;
block|}
specifier|public
name|void
name|setArgs
parameter_list|(
name|List
argument_list|<
name|PTFExpressionDef
argument_list|>
name|args
parameter_list|)
block|{
name|this
operator|.
name|args
operator|=
name|args
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"arguments"
argument_list|)
specifier|public
name|String
name|getArgsExplain
parameter_list|()
block|{
if|if
condition|(
name|args
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|PTFExpressionDef
name|expression
range|:
name|args
control|)
block|{
if|if
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|expression
operator|.
name|getExprNode
argument_list|()
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|addArg
parameter_list|(
name|PTFExpressionDef
name|arg
parameter_list|)
block|{
name|args
operator|=
name|args
operator|==
literal|null
condition|?
operator|new
name|ArrayList
argument_list|<
name|PTFExpressionDef
argument_list|>
argument_list|()
else|:
name|args
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|arg
argument_list|)
expr_stmt|;
block|}
specifier|public
name|PartitionedTableFunctionDef
name|getStartOfChain
parameter_list|()
block|{
if|if
condition|(
name|input
operator|instanceof
name|PartitionedTableFunctionDef
condition|)
block|{
return|return
operator|(
operator|(
name|PartitionedTableFunctionDef
operator|)
name|input
operator|)
operator|.
name|getStartOfChain
argument_list|()
return|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"transforms raw input"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|isTransformsRawInput
parameter_list|()
block|{
return|return
name|transformsRawInput
return|;
block|}
specifier|public
name|void
name|setTransformsRawInput
parameter_list|(
name|boolean
name|transformsRawInput
parameter_list|)
block|{
name|this
operator|.
name|transformsRawInput
operator|=
name|transformsRawInput
expr_stmt|;
block|}
specifier|public
name|String
name|getResolverClassName
parameter_list|()
block|{
return|return
name|resolverClassName
return|;
block|}
specifier|public
name|void
name|setResolverClassName
parameter_list|(
name|String
name|resolverClassName
parameter_list|)
block|{
name|this
operator|.
name|resolverClassName
operator|=
name|resolverClassName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"referenced columns"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getReferencedColumns
parameter_list|()
block|{
return|return
name|referencedColumns
return|;
block|}
specifier|public
name|void
name|setReferencedColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|referencedColumns
parameter_list|)
block|{
name|this
operator|.
name|referencedColumns
operator|=
name|referencedColumns
expr_stmt|;
block|}
block|}
end_class

end_unit

