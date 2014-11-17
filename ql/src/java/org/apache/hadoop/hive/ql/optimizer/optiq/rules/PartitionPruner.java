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
name|optimizer
operator|.
name|optiq
operator|.
name|rules
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|Set
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|FunctionRegistry
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
name|optimizer
operator|.
name|optiq
operator|.
name|RelOptHiveTable
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
name|optimizer
operator|.
name|optiq
operator|.
name|translator
operator|.
name|SqlFunctionConverter
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
name|GenericUDF
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataTypeField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexInputRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexLiteral
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexVisitorImpl
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|sql
operator|.
name|fun
operator|.
name|SqlStdOperatorTable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|util
operator|.
name|Pair
import|;
end_import

begin_class
specifier|public
class|class
name|PartitionPruner
block|{
comment|/**    * Breaks the predicate into 2 pieces. The first piece is the expressions that    * only contain partition columns and can be used for Partition Pruning; the    * second piece is the predicates that are left.    *     * @param cluster    * @param hiveTable    * @param predicate    * @return a Pair of expressions, each of which maybe null. The 1st predicate    *         is expressions that only contain partition columns; the 2nd    *         predicate contains the remaining predicates.    */
specifier|public
specifier|static
name|Pair
argument_list|<
name|RexNode
argument_list|,
name|RexNode
argument_list|>
name|extractPartitionPredicates
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelOptHiveTable
name|hiveTable
parameter_list|,
name|RexNode
name|predicate
parameter_list|)
block|{
name|RexNode
name|partitionPruningPred
init|=
name|predicate
operator|.
name|accept
argument_list|(
operator|new
name|ExtractPartPruningPredicate
argument_list|(
name|cluster
argument_list|,
name|hiveTable
argument_list|)
argument_list|)
decl_stmt|;
name|RexNode
name|remainingPred
init|=
name|predicate
operator|.
name|accept
argument_list|(
operator|new
name|ExtractRemainingPredicate
argument_list|(
name|cluster
argument_list|,
name|partitionPruningPred
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|Pair
argument_list|<
name|RexNode
argument_list|,
name|RexNode
argument_list|>
argument_list|(
name|partitionPruningPred
argument_list|,
name|remainingPred
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|ExtractPartPruningPredicate
extends|extends
name|RexVisitorImpl
argument_list|<
name|RexNode
argument_list|>
block|{
specifier|final
name|RelOptHiveTable
name|hiveTable
decl_stmt|;
specifier|final
name|RelDataType
name|rType
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|partCols
decl_stmt|;
specifier|final
name|RelOptCluster
name|cluster
decl_stmt|;
specifier|public
name|ExtractPartPruningPredicate
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelOptHiveTable
name|hiveTable
parameter_list|)
block|{
name|super
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|hiveTable
operator|=
name|hiveTable
expr_stmt|;
name|rType
operator|=
name|hiveTable
operator|.
name|getRowType
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|pfs
init|=
name|hiveTable
operator|.
name|getHiveTableMD
argument_list|()
operator|.
name|getPartCols
argument_list|()
decl_stmt|;
name|partCols
operator|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|FieldSchema
name|pf
range|:
name|pfs
control|)
block|{
name|partCols
operator|.
name|add
argument_list|(
name|pf
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|cluster
operator|=
name|cluster
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RexNode
name|visitLiteral
parameter_list|(
name|RexLiteral
name|literal
parameter_list|)
block|{
return|return
name|literal
return|;
block|}
annotation|@
name|Override
specifier|public
name|RexNode
name|visitInputRef
parameter_list|(
name|RexInputRef
name|inputRef
parameter_list|)
block|{
name|RelDataTypeField
name|f
init|=
name|rType
operator|.
name|getFieldList
argument_list|()
operator|.
name|get
argument_list|(
name|inputRef
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|partCols
operator|.
name|contains
argument_list|(
name|f
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|inputRef
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RexNode
name|visitCall
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
if|if
condition|(
operator|!
name|deep
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|RexNode
argument_list|>
name|args
init|=
operator|new
name|LinkedList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|argsPruned
init|=
literal|false
decl_stmt|;
name|GenericUDF
name|hiveUDF
init|=
name|SqlFunctionConverter
operator|.
name|getHiveUDF
argument_list|(
name|call
operator|.
name|getOperator
argument_list|()
argument_list|,
name|call
operator|.
name|getType
argument_list|()
argument_list|,
name|call
operator|.
name|operands
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveUDF
operator|!=
literal|null
operator|&&
operator|!
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
name|hiveUDF
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|RexNode
name|operand
range|:
name|call
operator|.
name|operands
control|)
block|{
name|RexNode
name|n
init|=
name|operand
operator|.
name|accept
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|n
operator|!=
literal|null
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|argsPruned
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|call
operator|.
name|getOperator
argument_list|()
operator|!=
name|SqlStdOperatorTable
operator|.
name|AND
condition|)
block|{
return|return
name|argsPruned
condition|?
literal|null
else|:
name|call
return|;
block|}
else|else
block|{
if|if
condition|(
name|args
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|args
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|args
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeCall
argument_list|(
name|call
operator|.
name|getOperator
argument_list|()
argument_list|,
name|args
argument_list|)
return|;
block|}
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|ExtractRemainingPredicate
extends|extends
name|RexVisitorImpl
argument_list|<
name|RexNode
argument_list|>
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|pruningPredicates
decl_stmt|;
specifier|final
name|RelOptCluster
name|cluster
decl_stmt|;
specifier|public
name|ExtractRemainingPredicate
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RexNode
name|partPruningExpr
parameter_list|)
block|{
name|super
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|cluster
operator|=
name|cluster
expr_stmt|;
name|pruningPredicates
operator|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
expr_stmt|;
name|flattenPredicates
argument_list|(
name|partPruningExpr
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|flattenPredicates
parameter_list|(
name|RexNode
name|r
parameter_list|)
block|{
if|if
condition|(
name|r
operator|instanceof
name|RexCall
operator|&&
operator|(
operator|(
name|RexCall
operator|)
name|r
operator|)
operator|.
name|getOperator
argument_list|()
operator|==
name|SqlStdOperatorTable
operator|.
name|AND
condition|)
block|{
for|for
control|(
name|RexNode
name|c
range|:
operator|(
operator|(
name|RexCall
operator|)
name|r
operator|)
operator|.
name|getOperands
argument_list|()
control|)
block|{
name|flattenPredicates
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|pruningPredicates
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RexNode
name|visitLiteral
parameter_list|(
name|RexLiteral
name|literal
parameter_list|)
block|{
return|return
name|literal
return|;
block|}
annotation|@
name|Override
specifier|public
name|RexNode
name|visitInputRef
parameter_list|(
name|RexInputRef
name|inputRef
parameter_list|)
block|{
return|return
name|inputRef
return|;
block|}
annotation|@
name|Override
specifier|public
name|RexNode
name|visitCall
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
if|if
condition|(
operator|!
name|deep
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|call
operator|.
name|getOperator
argument_list|()
operator|!=
name|SqlStdOperatorTable
operator|.
name|AND
condition|)
block|{
if|if
condition|(
name|pruningPredicates
operator|.
name|contains
argument_list|(
name|call
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|call
return|;
block|}
block|}
name|List
argument_list|<
name|RexNode
argument_list|>
name|args
init|=
operator|new
name|LinkedList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|operand
range|:
name|call
operator|.
name|operands
control|)
block|{
name|RexNode
name|n
init|=
name|operand
operator|.
name|accept
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|n
operator|!=
literal|null
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|args
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|args
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|args
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeCall
argument_list|(
name|call
operator|.
name|getOperator
argument_list|()
argument_list|,
name|args
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

