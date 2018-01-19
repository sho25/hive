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
name|calcite
operator|.
name|rules
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
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
name|HashSet
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
name|RelOptCluster
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
name|RelOptRule
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
name|RelOptRuleCall
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
name|AggregateCall
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
name|type
operator|.
name|RelDataType
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
name|type
operator|.
name|RelDataTypeField
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
name|RexBuilder
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
name|CalciteSemanticException
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
name|TraitsUtil
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
name|HiveAggregate
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
name|HiveExcept
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
name|HiveFilter
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
name|HiveProject
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
name|HiveRelNode
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
name|HiveTableFunctionScan
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
name|HiveUnion
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
name|optimizer
operator|.
name|calcite
operator|.
name|translator
operator|.
name|TypeConverter
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
name|SemanticException
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
operator|.
name|Builder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Planner rule that rewrite  * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveExcept}  * Note, we only have 2 branches because of except's semantic.  * R1 Except(all) R2  * R1 introduce VCol ‘2’, R2 introduce VCol ‘1’  * R3 = GB(R1 on all keys + VCol + count(VCol) as c) union all GB(R2 on all keys + VCol + count(VCol) as c)  * R4 = GB(R3 on all keys + sum(c) as a + sum(VCol*c) as b) we  * have m+n=a, 2m+n=b where m is the #row in R1 and n is the #row in R2 then  * m=b-a, n=2a-b, m-n=2b-3a  * if it is except (distinct)  * then R5 = Fil (b-a>0&& 2a-b=0) R6 = select only keys from R5  * else R5 = Fil (2b-3a>0) R6 = UDTF (R5) which will explode the tuples based on 2b-3a.  * Note that NULLs are handled the same as other values. Please refer to the test cases.  */
end_comment

begin_class
specifier|public
class|class
name|HiveExceptRewriteRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveExceptRewriteRule
name|INSTANCE
init|=
operator|new
name|HiveExceptRewriteRule
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HiveIntersectRewriteRule
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// ~ Constructors -----------------------------------------------------------
specifier|private
name|HiveExceptRewriteRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveExcept
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// ~ Methods ----------------------------------------------------------------
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|HiveExcept
name|hiveExcept
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|RelOptCluster
name|cluster
init|=
name|hiveExcept
operator|.
name|getCluster
argument_list|()
decl_stmt|;
specifier|final
name|RexBuilder
name|rexBuilder
init|=
name|cluster
operator|.
name|getRexBuilder
argument_list|()
decl_stmt|;
name|Builder
argument_list|<
name|RelNode
argument_list|>
name|bldr
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelNode
argument_list|>
argument_list|()
decl_stmt|;
comment|// 1st level GB: create a GB(R1 on all keys + VCol + count() as c) for each
comment|// branch
try|try
block|{
name|bldr
operator|.
name|add
argument_list|(
name|createFirstGB
argument_list|(
name|hiveExcept
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|true
argument_list|,
name|cluster
argument_list|,
name|rexBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|bldr
operator|.
name|add
argument_list|(
name|createFirstGB
argument_list|(
name|hiveExcept
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|false
argument_list|,
name|cluster
argument_list|,
name|rexBuilder
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CalciteSemanticException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// create a union above all the branches
comment|// the schema of union looks like this
comment|// all keys + VCol + c
name|HiveRelNode
name|union
init|=
operator|new
name|HiveUnion
argument_list|(
name|cluster
argument_list|,
name|TraitsUtil
operator|.
name|getDefaultTraitSet
argument_list|(
name|cluster
argument_list|)
argument_list|,
name|bldr
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
comment|// 2nd level GB: create a GB (all keys + sum(c) as a + sum(VCol*c) as b) for
comment|// each branch
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|gbChildProjLst
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|groupSetPositions
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|int
name|unionColumnSize
init|=
name|union
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|cInd
init|=
literal|0
init|;
name|cInd
operator|<
name|unionColumnSize
condition|;
name|cInd
operator|++
control|)
block|{
name|gbChildProjLst
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|union
argument_list|,
name|cInd
argument_list|)
argument_list|)
expr_stmt|;
comment|// the last 2 columns are VCol and c
if|if
condition|(
name|cInd
operator|<
name|unionColumnSize
operator|-
literal|2
condition|)
block|{
name|groupSetPositions
operator|.
name|add
argument_list|(
name|cInd
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|gbChildProjLst
operator|.
name|add
argument_list|(
name|multiply
argument_list|(
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|union
argument_list|,
name|unionColumnSize
operator|-
literal|2
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|union
argument_list|,
name|unionColumnSize
operator|-
literal|1
argument_list|)
argument_list|,
name|cluster
argument_list|,
name|rexBuilder
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CalciteSemanticException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|RelNode
name|gbInputRel
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Here we create a project for the following reasons:
comment|// (1) GBy only accepts arg as a position of the input, however, we need to sum on VCol*c
comment|// (2) This can better reuse the function createSingleArgAggCall.
name|gbInputRel
operator|=
name|HiveProject
operator|.
name|create
argument_list|(
name|union
argument_list|,
name|gbChildProjLst
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CalciteSemanticException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// gbInputRel's schema is like this
comment|// all keys + VCol + c + VCol*c
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|aggregateCalls
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|RelDataType
name|aggFnRetType
init|=
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
decl_stmt|;
comment|// sum(c)
name|AggregateCall
name|aggregateCall
init|=
name|HiveCalciteUtil
operator|.
name|createSingleArgAggCall
argument_list|(
literal|"sum"
argument_list|,
name|cluster
argument_list|,
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|unionColumnSize
operator|-
literal|1
argument_list|,
name|aggFnRetType
argument_list|)
decl_stmt|;
name|aggregateCalls
operator|.
name|add
argument_list|(
name|aggregateCall
argument_list|)
expr_stmt|;
comment|// sum(VCol*c)
name|aggregateCall
operator|=
name|HiveCalciteUtil
operator|.
name|createSingleArgAggCall
argument_list|(
literal|"sum"
argument_list|,
name|cluster
argument_list|,
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|unionColumnSize
argument_list|,
name|aggFnRetType
argument_list|)
expr_stmt|;
name|aggregateCalls
operator|.
name|add
argument_list|(
name|aggregateCall
argument_list|)
expr_stmt|;
specifier|final
name|ImmutableBitSet
name|groupSet
init|=
name|ImmutableBitSet
operator|.
name|of
argument_list|(
name|groupSetPositions
argument_list|)
decl_stmt|;
name|HiveRelNode
name|aggregateRel
init|=
operator|new
name|HiveAggregate
argument_list|(
name|cluster
argument_list|,
name|cluster
operator|.
name|traitSetOf
argument_list|(
name|HiveRelNode
operator|.
name|CONVENTION
argument_list|)
argument_list|,
name|gbInputRel
argument_list|,
name|groupSet
argument_list|,
literal|null
argument_list|,
name|aggregateCalls
argument_list|)
decl_stmt|;
comment|// the schema after GB is like this
comment|// all keys + sum(c) as a + sum(VCol*c) as b
comment|// the column size is the same as unionColumnSize;
comment|// (1) for except distinct add a filter (b-a>0&& 2a-b=0)
comment|// i.e., a> 0&& 2a = b
comment|// then add the project
comment|// (2) for except all add a project to change it to
comment|// (2b-3a) + all keys
comment|// then add the UDTF
if|if
condition|(
operator|!
name|hiveExcept
operator|.
name|all
condition|)
block|{
name|RelNode
name|filterRel
init|=
literal|null
decl_stmt|;
try|try
block|{
name|filterRel
operator|=
operator|new
name|HiveFilter
argument_list|(
name|cluster
argument_list|,
name|cluster
operator|.
name|traitSetOf
argument_list|(
name|HiveRelNode
operator|.
name|CONVENTION
argument_list|)
argument_list|,
name|aggregateRel
argument_list|,
name|makeFilterExprForExceptDistinct
argument_list|(
name|aggregateRel
argument_list|,
name|unionColumnSize
argument_list|,
name|cluster
argument_list|,
name|rexBuilder
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CalciteSemanticException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// finally add a project to project out the last 2 columns
name|Set
argument_list|<
name|Integer
argument_list|>
name|projectOutColumnPositions
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|projectOutColumnPositions
operator|.
name|add
argument_list|(
name|filterRel
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
expr_stmt|;
name|projectOutColumnPositions
operator|.
name|add
argument_list|(
name|filterRel
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
try|try
block|{
name|call
operator|.
name|transformTo
argument_list|(
name|HiveCalciteUtil
operator|.
name|createProjectWithoutColumn
argument_list|(
name|filterRel
argument_list|,
name|projectOutColumnPositions
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CalciteSemanticException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
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
else|else
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|originalInputRefs
init|=
name|Lists
operator|.
name|transform
argument_list|(
name|aggregateRel
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
argument_list|,
operator|new
name|Function
argument_list|<
name|RelDataTypeField
argument_list|,
name|RexNode
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|RexNode
name|apply
parameter_list|(
name|RelDataTypeField
name|input
parameter_list|)
block|{
return|return
operator|new
name|RexInputRef
argument_list|(
name|input
operator|.
name|getIndex
argument_list|()
argument_list|,
name|input
operator|.
name|getType
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|copyInputRefs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
block|{
name|copyInputRefs
operator|.
name|add
argument_list|(
name|makeExprForExceptAll
argument_list|(
name|aggregateRel
argument_list|,
name|unionColumnSize
argument_list|,
name|cluster
argument_list|,
name|rexBuilder
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CalciteSemanticException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|originalInputRefs
operator|.
name|size
argument_list|()
operator|-
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|copyInputRefs
operator|.
name|add
argument_list|(
name|originalInputRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|RelNode
name|srcRel
init|=
literal|null
decl_stmt|;
try|try
block|{
name|srcRel
operator|=
name|HiveProject
operator|.
name|create
argument_list|(
name|aggregateRel
argument_list|,
name|copyInputRefs
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HiveTableFunctionScan
name|udtf
init|=
name|HiveCalciteUtil
operator|.
name|createUDTFForSetOp
argument_list|(
name|cluster
argument_list|,
name|srcRel
argument_list|)
decl_stmt|;
comment|// finally add a project to project out the 1st columns
name|Set
argument_list|<
name|Integer
argument_list|>
name|projectOutColumnPositions
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|projectOutColumnPositions
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|HiveCalciteUtil
operator|.
name|createProjectWithoutColumn
argument_list|(
name|udtf
argument_list|,
name|projectOutColumnPositions
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
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
specifier|private
name|RelNode
name|createFirstGB
parameter_list|(
name|RelNode
name|input
parameter_list|,
name|boolean
name|left
parameter_list|,
name|RelOptCluster
name|cluster
parameter_list|,
name|RexBuilder
name|rexBuilder
parameter_list|)
throws|throws
name|CalciteSemanticException
block|{
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|gbChildProjLst
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|groupSetPositions
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|cInd
init|=
literal|0
init|;
name|cInd
operator|<
name|input
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|cInd
operator|++
control|)
block|{
name|gbChildProjLst
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|input
argument_list|,
name|cInd
argument_list|)
argument_list|)
expr_stmt|;
name|groupSetPositions
operator|.
name|add
argument_list|(
name|cInd
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|left
condition|)
block|{
name|gbChildProjLst
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeBigintLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|gbChildProjLst
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeBigintLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// also add the last VCol
name|groupSetPositions
operator|.
name|add
argument_list|(
name|input
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// create the project before GB
name|RelNode
name|gbInputRel
init|=
name|HiveProject
operator|.
name|create
argument_list|(
name|input
argument_list|,
name|gbChildProjLst
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// groupSetPosition includes all the positions
specifier|final
name|ImmutableBitSet
name|groupSet
init|=
name|ImmutableBitSet
operator|.
name|of
argument_list|(
name|groupSetPositions
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|aggregateCalls
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|RelDataType
name|aggFnRetType
init|=
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
decl_stmt|;
name|AggregateCall
name|aggregateCall
init|=
name|HiveCalciteUtil
operator|.
name|createSingleArgAggCall
argument_list|(
literal|"count"
argument_list|,
name|cluster
argument_list|,
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|input
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|aggFnRetType
argument_list|)
decl_stmt|;
name|aggregateCalls
operator|.
name|add
argument_list|(
name|aggregateCall
argument_list|)
expr_stmt|;
return|return
operator|new
name|HiveAggregate
argument_list|(
name|cluster
argument_list|,
name|cluster
operator|.
name|traitSetOf
argument_list|(
name|HiveRelNode
operator|.
name|CONVENTION
argument_list|)
argument_list|,
name|gbInputRel
argument_list|,
name|groupSet
argument_list|,
literal|null
argument_list|,
name|aggregateCalls
argument_list|)
return|;
block|}
specifier|private
name|RexNode
name|multiply
parameter_list|(
name|RexNode
name|r1
parameter_list|,
name|RexNode
name|r2
parameter_list|,
name|RelOptCluster
name|cluster
parameter_list|,
name|RexBuilder
name|rexBuilder
parameter_list|)
throws|throws
name|CalciteSemanticException
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|childRexNodeLst
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|r1
argument_list|)
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|r2
argument_list|)
expr_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelDataType
argument_list|>
name|calciteArgTypesBldr
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelDataType
argument_list|>
argument_list|()
decl_stmt|;
name|calciteArgTypesBldr
operator|.
name|add
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|calciteArgTypesBldr
operator|.
name|add
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlFunctionConverter
operator|.
name|getCalciteFn
argument_list|(
literal|"*"
argument_list|,
name|calciteArgTypesBldr
operator|.
name|build
argument_list|()
argument_list|,
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
argument_list|,
name|childRexNodeLst
argument_list|)
return|;
block|}
specifier|private
name|RexNode
name|makeFilterExprForExceptDistinct
parameter_list|(
name|HiveRelNode
name|input
parameter_list|,
name|int
name|columnSize
parameter_list|,
name|RelOptCluster
name|cluster
parameter_list|,
name|RexBuilder
name|rexBuilder
parameter_list|)
throws|throws
name|CalciteSemanticException
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|childRexNodeLst
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|RexInputRef
name|a
init|=
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|input
argument_list|,
name|columnSize
operator|-
literal|2
argument_list|)
decl_stmt|;
name|RexLiteral
name|zero
init|=
name|rexBuilder
operator|.
name|makeBigintLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|a
argument_list|)
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|zero
argument_list|)
expr_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelDataType
argument_list|>
name|calciteArgTypesBldr
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelDataType
argument_list|>
argument_list|()
decl_stmt|;
name|calciteArgTypesBldr
operator|.
name|add
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|calciteArgTypesBldr
operator|.
name|add
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// a>0
name|RexNode
name|aMorethanZero
init|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlFunctionConverter
operator|.
name|getCalciteFn
argument_list|(
literal|">"
argument_list|,
name|calciteArgTypesBldr
operator|.
name|build
argument_list|()
argument_list|,
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|,
name|childRexNodeLst
argument_list|)
decl_stmt|;
name|childRexNodeLst
operator|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
expr_stmt|;
name|RexLiteral
name|two
init|=
name|rexBuilder
operator|.
name|makeBigintLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|a
argument_list|)
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|two
argument_list|)
expr_stmt|;
comment|// 2*a
name|RexNode
name|twoa
init|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlFunctionConverter
operator|.
name|getCalciteFn
argument_list|(
literal|"*"
argument_list|,
name|calciteArgTypesBldr
operator|.
name|build
argument_list|()
argument_list|,
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|,
name|childRexNodeLst
argument_list|)
decl_stmt|;
name|childRexNodeLst
operator|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
expr_stmt|;
name|RexInputRef
name|b
init|=
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|input
argument_list|,
name|columnSize
operator|-
literal|1
argument_list|)
decl_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|twoa
argument_list|)
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
comment|// 2a=b
name|RexNode
name|twoaEqualTob
init|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlFunctionConverter
operator|.
name|getCalciteFn
argument_list|(
literal|"="
argument_list|,
name|calciteArgTypesBldr
operator|.
name|build
argument_list|()
argument_list|,
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|,
name|childRexNodeLst
argument_list|)
decl_stmt|;
name|childRexNodeLst
operator|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|aMorethanZero
argument_list|)
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|twoaEqualTob
argument_list|)
expr_stmt|;
comment|// a>0&& 2a=b
return|return
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlFunctionConverter
operator|.
name|getCalciteFn
argument_list|(
literal|"and"
argument_list|,
name|calciteArgTypesBldr
operator|.
name|build
argument_list|()
argument_list|,
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|,
name|childRexNodeLst
argument_list|)
return|;
block|}
specifier|private
name|RexNode
name|makeExprForExceptAll
parameter_list|(
name|HiveRelNode
name|input
parameter_list|,
name|int
name|columnSize
parameter_list|,
name|RelOptCluster
name|cluster
parameter_list|,
name|RexBuilder
name|rexBuilder
parameter_list|)
throws|throws
name|CalciteSemanticException
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|childRexNodeLst
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelDataType
argument_list|>
name|calciteArgTypesBldr
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelDataType
argument_list|>
argument_list|()
decl_stmt|;
name|calciteArgTypesBldr
operator|.
name|add
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|calciteArgTypesBldr
operator|.
name|add
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|RexInputRef
name|a
init|=
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|input
argument_list|,
name|columnSize
operator|-
literal|2
argument_list|)
decl_stmt|;
name|RexLiteral
name|three
init|=
name|rexBuilder
operator|.
name|makeBigintLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|3
argument_list|)
argument_list|)
decl_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|three
argument_list|)
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|a
argument_list|)
expr_stmt|;
name|RexNode
name|threea
init|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlFunctionConverter
operator|.
name|getCalciteFn
argument_list|(
literal|"*"
argument_list|,
name|calciteArgTypesBldr
operator|.
name|build
argument_list|()
argument_list|,
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|,
name|childRexNodeLst
argument_list|)
decl_stmt|;
name|RexLiteral
name|two
init|=
name|rexBuilder
operator|.
name|makeBigintLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|RexInputRef
name|b
init|=
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|input
argument_list|,
name|columnSize
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// 2*b
name|childRexNodeLst
operator|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|two
argument_list|)
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|RexNode
name|twob
init|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlFunctionConverter
operator|.
name|getCalciteFn
argument_list|(
literal|"*"
argument_list|,
name|calciteArgTypesBldr
operator|.
name|build
argument_list|()
argument_list|,
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|,
name|childRexNodeLst
argument_list|)
decl_stmt|;
comment|// 2b-3a
name|childRexNodeLst
operator|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|twob
argument_list|)
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|threea
argument_list|)
expr_stmt|;
return|return
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlFunctionConverter
operator|.
name|getCalciteFn
argument_list|(
literal|"-"
argument_list|,
name|calciteArgTypesBldr
operator|.
name|build
argument_list|()
argument_list|,
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|,
name|childRexNodeLst
argument_list|)
return|;
block|}
block|}
end_class

end_unit

