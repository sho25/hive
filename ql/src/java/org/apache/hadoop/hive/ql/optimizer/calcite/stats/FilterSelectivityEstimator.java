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
name|calcite
operator|.
name|stats
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
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptUtil
operator|.
name|InputReferencedVisitor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Project
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|RelMetadataQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
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
name|apache
operator|.
name|calcite
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
name|apache
operator|.
name|calcite
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
name|apache
operator|.
name|calcite
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
name|apache
operator|.
name|calcite
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
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlKind
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlOperator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlTypeUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|util
operator|.
name|ImmutableBitSet
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
name|calcite
operator|.
name|HiveCalciteUtil
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
name|calcite
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
name|calcite
operator|.
name|reloperators
operator|.
name|HiveTableScan
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
name|ColStatistics
import|;
end_import

begin_class
specifier|public
class|class
name|FilterSelectivityEstimator
extends|extends
name|RexVisitorImpl
argument_list|<
name|Double
argument_list|>
block|{
specifier|private
specifier|final
name|RelNode
name|childRel
decl_stmt|;
specifier|private
specifier|final
name|double
name|childCardinality
decl_stmt|;
specifier|protected
name|FilterSelectivityEstimator
parameter_list|(
name|RelNode
name|childRel
parameter_list|)
block|{
name|super
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|childRel
operator|=
name|childRel
expr_stmt|;
name|this
operator|.
name|childCardinality
operator|=
name|RelMetadataQuery
operator|.
name|instance
argument_list|()
operator|.
name|getRowCount
argument_list|(
name|childRel
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Double
name|estimateSelectivity
parameter_list|(
name|RexNode
name|predicate
parameter_list|)
block|{
return|return
name|predicate
operator|.
name|accept
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
name|Double
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
literal|1.0
return|;
block|}
comment|/*      * Ignore any predicates on partition columns because we have already      * accounted for these in the Table row count.      */
if|if
condition|(
name|isPartitionPredicate
argument_list|(
name|call
argument_list|,
name|this
operator|.
name|childRel
argument_list|)
condition|)
block|{
return|return
literal|1.0
return|;
block|}
name|Double
name|selectivity
init|=
literal|null
decl_stmt|;
name|SqlKind
name|op
init|=
name|getOp
argument_list|(
name|call
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|op
condition|)
block|{
case|case
name|AND
case|:
block|{
name|selectivity
operator|=
name|computeConjunctionSelectivity
argument_list|(
name|call
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|OR
case|:
block|{
name|selectivity
operator|=
name|computeDisjunctionSelectivity
argument_list|(
name|call
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|NOT
case|:
case|case
name|NOT_EQUALS
case|:
block|{
name|selectivity
operator|=
name|computeNotEqualitySelectivity
argument_list|(
name|call
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|IS_NOT_NULL
case|:
block|{
if|if
condition|(
name|childRel
operator|instanceof
name|HiveTableScan
condition|)
block|{
name|double
name|noOfNulls
init|=
name|getMaxNulls
argument_list|(
name|call
argument_list|,
operator|(
name|HiveTableScan
operator|)
name|childRel
argument_list|)
decl_stmt|;
name|double
name|totalNoOfTuples
init|=
name|childRel
operator|.
name|getRows
argument_list|()
decl_stmt|;
if|if
condition|(
name|totalNoOfTuples
operator|>=
name|noOfNulls
condition|)
block|{
name|selectivity
operator|=
operator|(
name|totalNoOfTuples
operator|-
name|noOfNulls
operator|)
operator|/
name|Math
operator|.
name|max
argument_list|(
name|totalNoOfTuples
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid Stats number of null> no of tuples"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|selectivity
operator|=
name|computeNotEqualitySelectivity
argument_list|(
name|call
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
case|case
name|LESS_THAN_OR_EQUAL
case|:
case|case
name|GREATER_THAN_OR_EQUAL
case|:
case|case
name|LESS_THAN
case|:
case|case
name|GREATER_THAN
case|:
block|{
name|selectivity
operator|=
operator|(
operator|(
name|double
operator|)
literal|1
operator|/
operator|(
name|double
operator|)
literal|3
operator|)
expr_stmt|;
break|break;
block|}
case|case
name|IN
case|:
block|{
comment|// TODO: 1) check for duplicates 2) We assume in clause values to be
comment|// present in NDV which may not be correct (Range check can find it) 3) We
comment|// assume values in NDV set is uniformly distributed over col values
comment|// (account for skewness - histogram).
name|selectivity
operator|=
name|computeFunctionSelectivity
argument_list|(
name|call
argument_list|)
operator|*
operator|(
name|call
operator|.
name|operands
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|)
expr_stmt|;
if|if
condition|(
name|selectivity
operator|<=
literal|0.0
condition|)
block|{
name|selectivity
operator|=
literal|0.10
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|selectivity
operator|>=
literal|1.0
condition|)
block|{
name|selectivity
operator|=
literal|1.0
expr_stmt|;
block|}
break|break;
block|}
default|default:
name|selectivity
operator|=
name|computeFunctionSelectivity
argument_list|(
name|call
argument_list|)
expr_stmt|;
block|}
return|return
name|selectivity
return|;
block|}
comment|/**    * NDV of "f1(x, y, z) != f2(p, q, r)" ->    * "(maxNDV(x,y,z,p,q,r) - 1)/maxNDV(x,y,z,p,q,r)".    *<p>    *     * @param call    * @return    */
specifier|private
name|Double
name|computeNotEqualitySelectivity
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
name|double
name|tmpNDV
init|=
name|getMaxNDV
argument_list|(
name|call
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmpNDV
operator|>
literal|1
condition|)
return|return
operator|(
name|tmpNDV
operator|-
operator|(
name|double
operator|)
literal|1
operator|)
operator|/
name|tmpNDV
return|;
else|else
return|return
literal|1.0
return|;
block|}
comment|/**    * Selectivity of f(X,y,z) -> 1/maxNDV(x,y,z).    *<p>    * Note that>,>=,<,<=, = ... are considered generic functions and uses    * this method to find their selectivity.    *     * @param call    * @return    */
specifier|private
name|Double
name|computeFunctionSelectivity
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
return|return
literal|1
operator|/
name|getMaxNDV
argument_list|(
name|call
argument_list|)
return|;
block|}
comment|/**    * Disjunction Selectivity -> (1 D(1-m1/n)(1-m2/n)) where n is the total    * number of tuples from child and m1 and m2 is the expected number of tuples    * from each part of the disjunction predicate.    *<p>    * Note we compute m1. m2.. by applying selectivity of the disjunctive element    * on the cardinality from child.    *     * @param call    * @return    */
specifier|private
name|Double
name|computeDisjunctionSelectivity
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
name|Double
name|tmpCardinality
decl_stmt|;
name|Double
name|tmpSelectivity
decl_stmt|;
name|double
name|selectivity
init|=
literal|1
decl_stmt|;
for|for
control|(
name|RexNode
name|dje
range|:
name|call
operator|.
name|getOperands
argument_list|()
control|)
block|{
name|tmpSelectivity
operator|=
name|dje
operator|.
name|accept
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmpSelectivity
operator|==
literal|null
condition|)
block|{
name|tmpSelectivity
operator|=
literal|0.99
expr_stmt|;
block|}
name|tmpCardinality
operator|=
name|childCardinality
operator|*
name|tmpSelectivity
expr_stmt|;
if|if
condition|(
name|tmpCardinality
operator|>
literal|1
operator|&&
name|tmpCardinality
operator|<
name|childCardinality
condition|)
block|{
name|tmpSelectivity
operator|=
operator|(
literal|1
operator|-
name|tmpCardinality
operator|/
name|childCardinality
operator|)
expr_stmt|;
block|}
else|else
block|{
name|tmpSelectivity
operator|=
literal|1.0
expr_stmt|;
block|}
name|selectivity
operator|*=
name|tmpSelectivity
expr_stmt|;
block|}
if|if
condition|(
name|selectivity
operator|<
literal|0.0
condition|)
name|selectivity
operator|=
literal|0.0
expr_stmt|;
return|return
operator|(
literal|1
operator|-
name|selectivity
operator|)
return|;
block|}
comment|/**    * Selectivity of conjunctive predicate -> (selectivity of conjunctive    * element1) * (selectivity of conjunctive element2)...    *     * @param call    * @return    */
specifier|private
name|Double
name|computeConjunctionSelectivity
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
name|Double
name|tmpSelectivity
decl_stmt|;
name|double
name|selectivity
init|=
literal|1
decl_stmt|;
for|for
control|(
name|RexNode
name|cje
range|:
name|call
operator|.
name|getOperands
argument_list|()
control|)
block|{
name|tmpSelectivity
operator|=
name|cje
operator|.
name|accept
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmpSelectivity
operator|!=
literal|null
condition|)
block|{
name|selectivity
operator|*=
name|tmpSelectivity
expr_stmt|;
block|}
block|}
return|return
name|selectivity
return|;
block|}
comment|/**    * Given a RexCall& TableScan find max no of nulls. Currently it picks the    * col with max no of nulls.    *     * TODO: improve this    *     * @param call    * @param t    * @return    */
specifier|private
name|long
name|getMaxNulls
parameter_list|(
name|RexCall
name|call
parameter_list|,
name|HiveTableScan
name|t
parameter_list|)
block|{
name|long
name|tmpNoNulls
init|=
literal|0
decl_stmt|;
name|long
name|maxNoNulls
init|=
literal|0
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|iRefSet
init|=
name|HiveCalciteUtil
operator|.
name|getInputRefs
argument_list|(
name|call
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|colStats
init|=
name|t
operator|.
name|getColStat
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|iRefSet
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|ColStatistics
name|cs
range|:
name|colStats
control|)
block|{
name|tmpNoNulls
operator|=
name|cs
operator|.
name|getNumNulls
argument_list|()
expr_stmt|;
if|if
condition|(
name|tmpNoNulls
operator|>
name|maxNoNulls
condition|)
block|{
name|maxNoNulls
operator|=
name|tmpNoNulls
expr_stmt|;
block|}
block|}
return|return
name|maxNoNulls
return|;
block|}
specifier|private
name|Double
name|getMaxNDV
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
name|double
name|tmpNDV
decl_stmt|;
name|double
name|maxNDV
init|=
literal|1.0
decl_stmt|;
name|InputReferencedVisitor
name|irv
decl_stmt|;
name|RelMetadataQuery
name|mq
init|=
name|RelMetadataQuery
operator|.
name|instance
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|op
range|:
name|call
operator|.
name|getOperands
argument_list|()
control|)
block|{
if|if
condition|(
name|op
operator|instanceof
name|RexInputRef
condition|)
block|{
name|tmpNDV
operator|=
name|HiveRelMdDistinctRowCount
operator|.
name|getDistinctRowCount
argument_list|(
name|this
operator|.
name|childRel
argument_list|,
name|mq
argument_list|,
operator|(
operator|(
name|RexInputRef
operator|)
name|op
operator|)
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmpNDV
operator|>
name|maxNDV
condition|)
name|maxNDV
operator|=
name|tmpNDV
expr_stmt|;
block|}
else|else
block|{
name|irv
operator|=
operator|new
name|InputReferencedVisitor
argument_list|()
expr_stmt|;
name|irv
operator|.
name|apply
argument_list|(
name|op
argument_list|)
expr_stmt|;
for|for
control|(
name|Integer
name|childProjIndx
range|:
name|irv
operator|.
name|inputPosReferenced
control|)
block|{
name|tmpNDV
operator|=
name|HiveRelMdDistinctRowCount
operator|.
name|getDistinctRowCount
argument_list|(
name|this
operator|.
name|childRel
argument_list|,
name|mq
argument_list|,
name|childProjIndx
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmpNDV
operator|>
name|maxNDV
condition|)
name|maxNDV
operator|=
name|tmpNDV
expr_stmt|;
block|}
block|}
block|}
return|return
name|maxNDV
return|;
block|}
specifier|private
name|boolean
name|isPartitionPredicate
parameter_list|(
name|RexNode
name|expr
parameter_list|,
name|RelNode
name|r
parameter_list|)
block|{
if|if
condition|(
name|r
operator|instanceof
name|Project
condition|)
block|{
name|expr
operator|=
name|RelOptUtil
operator|.
name|pushFilterPastProject
argument_list|(
name|expr
argument_list|,
operator|(
name|Project
operator|)
name|r
argument_list|)
expr_stmt|;
return|return
name|isPartitionPredicate
argument_list|(
name|expr
argument_list|,
operator|(
operator|(
name|Project
operator|)
name|r
operator|)
operator|.
name|getInput
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|r
operator|instanceof
name|Filter
condition|)
block|{
return|return
name|isPartitionPredicate
argument_list|(
name|expr
argument_list|,
operator|(
operator|(
name|Filter
operator|)
name|r
operator|)
operator|.
name|getInput
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|r
operator|instanceof
name|HiveTableScan
condition|)
block|{
name|RelOptHiveTable
name|table
init|=
call|(
name|RelOptHiveTable
call|)
argument_list|(
operator|(
name|HiveTableScan
operator|)
name|r
argument_list|)
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|ImmutableBitSet
name|cols
init|=
name|RelOptUtil
operator|.
name|InputFinder
operator|.
name|bits
argument_list|(
name|expr
argument_list|)
decl_stmt|;
return|return
name|table
operator|.
name|containsPartitionColumnsOnly
argument_list|(
name|cols
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|SqlKind
name|getOp
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
name|SqlKind
name|op
init|=
name|call
operator|.
name|getKind
argument_list|()
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|getKind
argument_list|()
operator|.
name|equals
argument_list|(
name|SqlKind
operator|.
name|OTHER_FUNCTION
argument_list|)
operator|&&
name|SqlTypeUtil
operator|.
name|inBooleanFamily
argument_list|(
name|call
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
name|SqlOperator
name|sqlOp
init|=
name|call
operator|.
name|getOperator
argument_list|()
decl_stmt|;
name|String
name|opName
init|=
operator|(
name|sqlOp
operator|!=
literal|null
operator|)
condition|?
name|sqlOp
operator|.
name|getName
argument_list|()
else|:
literal|""
decl_stmt|;
if|if
condition|(
name|opName
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"in"
argument_list|)
condition|)
block|{
name|op
operator|=
name|SqlKind
operator|.
name|IN
expr_stmt|;
block|}
block|}
return|return
name|op
return|;
block|}
specifier|public
name|Double
name|visitLiteral
parameter_list|(
name|RexLiteral
name|literal
parameter_list|)
block|{
if|if
condition|(
name|literal
operator|.
name|isAlwaysFalse
argument_list|()
condition|)
block|{
return|return
literal|0.0
return|;
block|}
elseif|else
if|if
condition|(
name|literal
operator|.
name|isAlwaysTrue
argument_list|()
condition|)
block|{
return|return
literal|1.0
return|;
block|}
else|else
block|{
assert|assert
literal|false
assert|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

