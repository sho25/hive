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
name|BitSet
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
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|FilterOperator
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
name|TableScanOperator
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
name|UDFArgumentException
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
name|io
operator|.
name|sarg
operator|.
name|ConvertAstToSearchArg
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
name|io
operator|.
name|sarg
operator|.
name|ExpressionTree
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
name|io
operator|.
name|sarg
operator|.
name|ExpressionTree
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
name|io
operator|.
name|sarg
operator|.
name|PredicateLeaf
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
name|io
operator|.
name|sarg
operator|.
name|SearchArgument
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|metadata
operator|.
name|Partition
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
name|Table
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
name|PrunerOperatorFactory
operator|.
name|FilterPruner
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
name|ppr
operator|.
name|PartitionPruner
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
name|ParseContext
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
name|PrunedPartitionList
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspectorConverters
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
name|objectinspector
operator|.
name|ObjectInspectorConverters
operator|.
name|Converter
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
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|objectinspector
operator|.
name|PrimitiveObjectInspector
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
name|objectinspector
operator|.
name|StructField
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
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Fixed bucket pruning optimizer goes through all the table scans and annotates them  * with a bucketing inclusion bit-set.  */
end_comment

begin_class
specifier|public
class|class
name|FixedBucketPruningOptimizer
extends|extends
name|Transform
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FixedBucketPruningOptimizer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|compat
decl_stmt|;
specifier|public
name|FixedBucketPruningOptimizer
parameter_list|(
name|boolean
name|compat
parameter_list|)
block|{
name|this
operator|.
name|compat
operator|=
name|compat
expr_stmt|;
block|}
specifier|public
class|class
name|NoopWalker
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// do nothing
return|return
literal|null
return|;
block|}
block|}
specifier|public
class|class
name|FixedBucketPartitionWalker
extends|extends
name|FilterPruner
block|{
annotation|@
name|Override
specifier|protected
name|void
name|generatePredicate
parameter_list|(
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|FilterOperator
name|fop
parameter_list|,
name|TableScanOperator
name|top
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|UDFArgumentException
block|{
name|FixedBucketPruningOptimizerCtxt
name|ctxt
init|=
operator|(
operator|(
name|FixedBucketPruningOptimizerCtxt
operator|)
name|procCtx
operator|)
decl_stmt|;
name|Table
name|tbl
init|=
name|top
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
if|if
condition|(
name|tbl
operator|.
name|getNumBuckets
argument_list|()
operator|>
literal|0
condition|)
block|{
specifier|final
name|int
name|nbuckets
init|=
name|tbl
operator|.
name|getNumBuckets
argument_list|()
decl_stmt|;
name|ctxt
operator|.
name|setNumBuckets
argument_list|(
name|nbuckets
argument_list|)
expr_stmt|;
name|ctxt
operator|.
name|setBucketCols
argument_list|(
name|tbl
operator|.
name|getBucketCols
argument_list|()
argument_list|)
expr_stmt|;
name|ctxt
operator|.
name|setSchema
argument_list|(
name|tbl
operator|.
name|getFields
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tbl
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
comment|// Run partition pruner to get partitions
name|ParseContext
name|parseCtx
init|=
name|ctxt
operator|.
name|pctx
decl_stmt|;
name|PrunedPartitionList
name|prunedPartList
decl_stmt|;
try|try
block|{
name|String
name|alias
init|=
operator|(
name|String
operator|)
name|parseCtx
operator|.
name|getTopOps
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|prunedPartList
operator|=
name|PartitionPruner
operator|.
name|prune
argument_list|(
name|top
argument_list|,
name|parseCtx
argument_list|,
name|alias
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|prunedPartList
operator|!=
literal|null
condition|)
block|{
name|ctxt
operator|.
name|setPartitions
argument_list|(
name|prunedPartList
argument_list|)
expr_stmt|;
for|for
control|(
name|Partition
name|p
range|:
name|prunedPartList
operator|.
name|getPartitions
argument_list|()
control|)
block|{
if|if
condition|(
name|nbuckets
operator|!=
name|p
operator|.
name|getBucketCount
argument_list|()
condition|)
block|{
comment|// disable feature
name|ctxt
operator|.
name|setNumBuckets
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|BucketBitsetGenerator
extends|extends
name|FilterPruner
block|{
annotation|@
name|Override
specifier|protected
name|void
name|generatePredicate
parameter_list|(
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|FilterOperator
name|fop
parameter_list|,
name|TableScanOperator
name|top
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|UDFArgumentException
block|{
name|FixedBucketPruningOptimizerCtxt
name|ctxt
init|=
operator|(
operator|(
name|FixedBucketPruningOptimizerCtxt
operator|)
name|procCtx
operator|)
decl_stmt|;
if|if
condition|(
name|ctxt
operator|.
name|getNumBuckets
argument_list|()
operator|<=
literal|0
operator|||
name|ctxt
operator|.
name|getBucketCols
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
comment|// bucketing isn't consistent or there are>1 bucket columns
comment|// optimizer does not extract multiple column predicates for this
return|return;
block|}
name|ExprNodeGenericFuncDesc
name|filter
init|=
name|top
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterExpr
argument_list|()
decl_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// the sargs are closely tied to hive.optimize.index.filter
name|SearchArgument
name|sarg
init|=
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|filter
argument_list|)
decl_stmt|;
if|if
condition|(
name|sarg
operator|==
literal|null
condition|)
block|{
return|return;
block|}
specifier|final
name|String
name|bucketCol
init|=
name|ctxt
operator|.
name|getBucketCols
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|StructField
name|bucketField
init|=
literal|null
decl_stmt|;
for|for
control|(
name|StructField
name|fs
range|:
name|ctxt
operator|.
name|getSchema
argument_list|()
control|)
block|{
if|if
condition|(
name|fs
operator|.
name|getFieldName
argument_list|()
operator|.
name|equals
argument_list|(
name|bucketCol
argument_list|)
condition|)
block|{
name|bucketField
operator|=
name|fs
expr_stmt|;
block|}
block|}
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|bucketField
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|literals
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|PredicateLeaf
argument_list|>
name|bucketLeaves
init|=
operator|new
name|HashSet
argument_list|<
name|PredicateLeaf
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|PredicateLeaf
name|l
range|:
name|leaves
control|)
block|{
if|if
condition|(
name|bucketCol
operator|.
name|equals
argument_list|(
name|l
operator|.
name|getColumnName
argument_list|()
argument_list|)
condition|)
block|{
switch|switch
condition|(
name|l
operator|.
name|getOperator
argument_list|()
condition|)
block|{
case|case
name|EQUALS
case|:
case|case
name|IN
case|:
comment|// supported
break|break;
case|case
name|IS_NULL
case|:
comment|// TODO: (a = 1) and NOT (a is NULL) can be potentially folded earlier into a NO-OP
comment|// fall through
case|case
name|BETWEEN
case|:
comment|// TODO: for ordinal types you can produce a range (BETWEEN 1444442100 1444442107)
comment|// fall through
default|default:
comment|// cannot optimize any others
return|return;
block|}
name|bucketLeaves
operator|.
name|add
argument_list|(
name|l
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|bucketLeaves
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
comment|// TODO: Add support for AND clauses under OR clauses
comment|// first-cut takes a known minimal tree and no others.
comment|// $expr = (a=1)
comment|//         (a=1 or a=2)
comment|//         (a in (1,2))
comment|//         ($expr and *)
comment|//         (* and $expr)
name|ExpressionTree
name|expr
init|=
name|sarg
operator|.
name|getExpression
argument_list|()
decl_stmt|;
if|if
condition|(
name|expr
operator|.
name|getOperator
argument_list|()
operator|==
name|Operator
operator|.
name|LEAF
condition|)
block|{
name|PredicateLeaf
name|l
init|=
name|leaves
operator|.
name|get
argument_list|(
name|expr
operator|.
name|getLeaf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|addLiteral
argument_list|(
name|literals
argument_list|,
name|l
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|expr
operator|.
name|getOperator
argument_list|()
operator|==
name|Operator
operator|.
name|AND
condition|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ExpressionTree
name|subExpr
range|:
name|expr
operator|.
name|getChildren
argument_list|()
control|)
block|{
if|if
condition|(
name|subExpr
operator|.
name|getOperator
argument_list|()
operator|!=
name|Operator
operator|.
name|LEAF
condition|)
block|{
return|return;
block|}
comment|// one of the branches is definitely a bucket-leaf
name|PredicateLeaf
name|l
init|=
name|leaves
operator|.
name|get
argument_list|(
name|subExpr
operator|.
name|getLeaf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketLeaves
operator|.
name|contains
argument_list|(
name|l
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|addLiteral
argument_list|(
name|literals
argument_list|,
name|l
argument_list|)
condition|)
block|{
return|return;
block|}
name|found
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|expr
operator|.
name|getOperator
argument_list|()
operator|==
name|Operator
operator|.
name|OR
condition|)
block|{
for|for
control|(
name|ExpressionTree
name|subExpr
range|:
name|expr
operator|.
name|getChildren
argument_list|()
control|)
block|{
if|if
condition|(
name|subExpr
operator|.
name|getOperator
argument_list|()
operator|!=
name|Operator
operator|.
name|LEAF
condition|)
block|{
return|return;
block|}
name|PredicateLeaf
name|l
init|=
name|leaves
operator|.
name|get
argument_list|(
name|subExpr
operator|.
name|getLeaf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketLeaves
operator|.
name|contains
argument_list|(
name|l
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|addLiteral
argument_list|(
name|literals
argument_list|,
name|l
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
else|else
block|{
comment|// all of the OR branches need to be bucket-leaves
return|return;
block|}
block|}
block|}
comment|// invariant: bucket-col IN literals of type bucketField
name|BitSet
name|bs
init|=
operator|new
name|BitSet
argument_list|(
name|ctxt
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
decl_stmt|;
name|bs
operator|.
name|clear
argument_list|()
expr_stmt|;
name|PrimitiveObjectInspector
name|bucketOI
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|bucketField
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|PrimitiveObjectInspector
name|constOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|bucketOI
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|literal
range|:
name|literals
control|)
block|{
name|PrimitiveObjectInspector
name|origOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveObjectInspectorFromClass
argument_list|(
name|literal
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|Converter
name|conv
init|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|origOI
argument_list|,
name|constOI
argument_list|)
decl_stmt|;
comment|// exact type conversion or get out
if|if
condition|(
name|conv
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|Object
name|convCols
index|[]
init|=
operator|new
name|Object
index|[]
block|{
name|conv
operator|.
name|convert
argument_list|(
name|literal
argument_list|)
block|}
decl_stmt|;
name|int
name|n
init|=
name|ObjectInspectorUtils
operator|.
name|getBucketNumber
argument_list|(
name|convCols
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|constOI
block|}
argument_list|,
name|ctxt
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
decl_stmt|;
name|bs
operator|.
name|set
argument_list|(
name|n
argument_list|)
expr_stmt|;
if|if
condition|(
name|ctxt
operator|.
name|isCompat
argument_list|()
condition|)
block|{
name|int
name|h
init|=
name|ObjectInspectorUtils
operator|.
name|getBucketHashCode
argument_list|(
name|convCols
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|constOI
block|}
argument_list|)
decl_stmt|;
comment|// -ve hashcodes had conversion to positive done in different ways in the past
comment|// abs() is now obsolete and all inserts now use& Integer.MAX_VALUE
comment|// the compat mode assumes that old data could've been loaded using the other conversion
name|n
operator|=
name|ObjectInspectorUtils
operator|.
name|getBucketNumber
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|h
argument_list|)
argument_list|,
name|ctxt
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
expr_stmt|;
name|bs
operator|.
name|set
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|bs
operator|.
name|cardinality
argument_list|()
operator|<
name|ctxt
operator|.
name|getNumBuckets
argument_list|()
condition|)
block|{
comment|// there is a valid bucket pruning filter
name|top
operator|.
name|getConf
argument_list|()
operator|.
name|setIncludedBuckets
argument_list|(
name|bs
argument_list|)
expr_stmt|;
name|top
operator|.
name|getConf
argument_list|()
operator|.
name|setNumBuckets
argument_list|(
name|ctxt
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|addLiteral
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|literals
parameter_list|,
name|PredicateLeaf
name|leaf
parameter_list|)
block|{
switch|switch
condition|(
name|leaf
operator|.
name|getOperator
argument_list|()
condition|)
block|{
case|case
name|EQUALS
case|:
return|return
name|literals
operator|.
name|add
argument_list|(
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
return|;
case|case
name|IN
case|:
return|return
name|literals
operator|.
name|addAll
argument_list|(
name|leaf
operator|.
name|getLiteralList
argument_list|()
argument_list|)
return|;
default|default:
return|return
literal|false
return|;
block|}
block|}
block|}
specifier|public
specifier|final
class|class
name|FixedBucketPruningOptimizerCtxt
implements|implements
name|NodeProcessorCtx
block|{
specifier|public
specifier|final
name|ParseContext
name|pctx
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|compat
decl_stmt|;
specifier|private
name|int
name|numBuckets
decl_stmt|;
specifier|private
name|PrunedPartitionList
name|partitions
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
decl_stmt|;
specifier|private
name|List
argument_list|<
name|StructField
argument_list|>
name|schema
decl_stmt|;
specifier|public
name|FixedBucketPruningOptimizerCtxt
parameter_list|(
name|boolean
name|compat
parameter_list|,
name|ParseContext
name|pctx
parameter_list|)
block|{
name|this
operator|.
name|compat
operator|=
name|compat
expr_stmt|;
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
block|}
specifier|public
name|void
name|setSchema
parameter_list|(
name|ArrayList
argument_list|<
name|StructField
argument_list|>
name|fields
parameter_list|)
block|{
name|this
operator|.
name|schema
operator|=
name|fields
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|StructField
argument_list|>
name|getSchema
parameter_list|()
block|{
return|return
name|this
operator|.
name|schema
return|;
block|}
specifier|public
name|void
name|setBucketCols
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
parameter_list|)
block|{
name|this
operator|.
name|bucketCols
operator|=
name|bucketCols
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getBucketCols
parameter_list|()
block|{
return|return
name|this
operator|.
name|bucketCols
return|;
block|}
specifier|public
name|void
name|setPartitions
parameter_list|(
name|PrunedPartitionList
name|partitions
parameter_list|)
block|{
name|this
operator|.
name|partitions
operator|=
name|partitions
expr_stmt|;
block|}
specifier|public
name|PrunedPartitionList
name|getPartitions
parameter_list|()
block|{
return|return
name|this
operator|.
name|partitions
return|;
block|}
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
specifier|public
name|void
name|setNumBuckets
parameter_list|(
name|int
name|numBuckets
parameter_list|)
block|{
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
block|}
comment|// compatibility mode enabled
specifier|public
name|boolean
name|isCompat
parameter_list|()
block|{
return|return
name|this
operator|.
name|compat
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// create a the context for walking operators
name|FixedBucketPruningOptimizerCtxt
name|opPartWalkerCtx
init|=
operator|new
name|FixedBucketPruningOptimizerCtxt
argument_list|(
name|compat
argument_list|,
name|pctx
argument_list|)
decl_stmt|;
comment|// Retrieve all partitions generated from partition pruner and partition
comment|// column pruner
name|PrunerUtils
operator|.
name|walkOperatorTree
argument_list|(
name|pctx
argument_list|,
name|opPartWalkerCtx
argument_list|,
operator|new
name|FixedBucketPartitionWalker
argument_list|()
argument_list|,
operator|new
name|NoopWalker
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|opPartWalkerCtx
operator|.
name|getNumBuckets
argument_list|()
operator|<
literal|0
condition|)
block|{
comment|// bail out
return|return
name|pctx
return|;
block|}
else|else
block|{
comment|// walk operator tree to create expression tree for filter buckets
name|PrunerUtils
operator|.
name|walkOperatorTree
argument_list|(
name|pctx
argument_list|,
name|opPartWalkerCtx
argument_list|,
operator|new
name|BucketBitsetGenerator
argument_list|()
argument_list|,
operator|new
name|NoopWalker
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|pctx
return|;
block|}
block|}
end_class

end_unit

