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
name|ppr
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
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|Map
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
name|conf
operator|.
name|HiveConf
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
name|Warehouse
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|NoSuchObjectException
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
name|ErrorMsg
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
name|ExprNodeEvaluator
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
name|exec
operator|.
name|Utilities
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
name|lib
operator|.
name|DefaultGraphWalker
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
name|DefaultRuleDispatcher
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
name|Dispatcher
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
name|GraphWalker
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
name|Rule
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
name|RuleRegExp
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
name|log
operator|.
name|PerfLogger
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
name|Hive
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
name|Transform
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
name|GenericUDFOPAnd
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
name|GenericUDFOPOr
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
name|StructObjectInspector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_comment
comment|/**  * The transformation step that does partition pruning.  *  */
end_comment

begin_class
specifier|public
class|class
name|PartitionPruner
implements|implements
name|Transform
block|{
comment|// The log
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
literal|"hive.ql.optimizer.ppr.PartitionPruner"
argument_list|)
decl_stmt|;
comment|/*    * (non-Javadoc)    *    * @see    * org.apache.hadoop.hive.ql.optimizer.Transform#transform(org.apache.hadoop    * .hive.ql.parse.ParseContext)    */
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
name|OpWalkerCtx
name|opWalkerCtx
init|=
operator|new
name|OpWalkerCtx
argument_list|(
name|pctx
operator|.
name|getOpToPartPruner
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
literal|"("
operator|+
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%)|("
operator|+
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%)"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getFilterProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|OpProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|opWalkerCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of topop nodes
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pctx
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|pctx
operator|.
name|setHasNonPartCols
argument_list|(
name|opWalkerCtx
operator|.
name|getHasNonPartCols
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
comment|/**    * Find out whether the condition only contains partitioned columns. Note that    * if the table is not partitioned, the function always returns true.    * condition.    *    * @param tab    *          the table object    * @param expr    *          the pruner expression for the table    */
specifier|public
specifier|static
name|boolean
name|onlyContainsPartnCols
parameter_list|(
name|Table
name|tab
parameter_list|,
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
if|if
condition|(
operator|!
name|tab
operator|.
name|isPartitioned
argument_list|()
operator|||
operator|(
name|expr
operator|==
literal|null
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
name|String
name|colName
init|=
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|expr
operator|)
operator|.
name|getColumn
argument_list|()
decl_stmt|;
return|return
name|tab
operator|.
name|isPartitionKey
argument_list|(
name|colName
argument_list|)
return|;
block|}
comment|// It cannot contain a non-deterministic function
if|if
condition|(
operator|(
name|expr
operator|instanceof
name|ExprNodeGenericFuncDesc
operator|)
operator|&&
operator|!
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|expr
operator|)
operator|.
name|getGenericUDF
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// All columns of the expression must be parttioned columns
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
name|expr
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|children
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|onlyContainsPartnCols
argument_list|(
name|tab
argument_list|,
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Get the partition list for the table that satisfies the partition pruner    * condition.    *    * @param tab    *          the table object for the alias    * @param prunerExpr    *          the pruner expression for the alias    * @param conf    *          for checking whether "strict" mode is on.    * @param alias    *          for generating error message only.    * @return the partition list for the table that satisfies the partition    *         pruner condition.    * @throws HiveException    */
specifier|public
specifier|static
name|PrunedPartitionList
name|prune
parameter_list|(
name|Table
name|tab
parameter_list|,
name|ExprNodeDesc
name|prunerExpr
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|String
name|alias
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|PrunedPartitionList
argument_list|>
name|prunedPartitionsMap
parameter_list|)
throws|throws
name|HiveException
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Started pruning partiton"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"dbname = "
operator|+
name|tab
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"tabname = "
operator|+
name|tab
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"prune Expression = "
operator|+
name|prunerExpr
argument_list|)
expr_stmt|;
name|String
name|key
init|=
name|tab
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|tab
operator|.
name|getTableName
argument_list|()
operator|+
literal|";"
decl_stmt|;
if|if
condition|(
name|prunerExpr
operator|!=
literal|null
condition|)
block|{
name|key
operator|=
name|key
operator|+
name|prunerExpr
operator|.
name|getExprString
argument_list|()
expr_stmt|;
block|}
name|PrunedPartitionList
name|ret
init|=
name|prunedPartitionsMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|null
condition|)
block|{
return|return
name|ret
return|;
block|}
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
name|true_parts
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
name|unkn_parts
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
name|denied_parts
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|StructObjectInspector
name|rowObjectInspector
init|=
operator|(
name|StructObjectInspector
operator|)
name|tab
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|Object
index|[]
name|rowWithPart
init|=
operator|new
name|Object
index|[
literal|2
index|]
decl_stmt|;
if|if
condition|(
name|tab
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
comment|// If the "strict" mode is on, we have to provide partition pruner for
comment|// each table.
if|if
condition|(
literal|"strict"
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPREDMODE
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|hasColumnExpr
argument_list|(
name|prunerExpr
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|NO_PARTITION_PREDICATE
operator|.
name|getMsg
argument_list|(
literal|"for Alias \""
operator|+
name|alias
operator|+
literal|"\" Table \""
operator|+
name|tab
operator|.
name|getTableName
argument_list|()
operator|+
literal|"\""
argument_list|)
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|prunerExpr
operator|==
literal|null
condition|)
block|{
comment|// This can happen when hive.mapred.mode=nonstrict and there is no predicates at all
comment|// Add all partitions to the unknown_parts so that a MR job is generated.
name|true_parts
operator|.
name|addAll
argument_list|(
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartitions
argument_list|(
name|tab
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// remove non-partition columns
name|ExprNodeDesc
name|compactExpr
init|=
name|prunerExpr
operator|.
name|clone
argument_list|()
decl_stmt|;
name|compactExpr
operator|=
name|compactExpr
argument_list|(
name|compactExpr
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter w/ compacting: "
operator|+
operator|(
operator|(
name|compactExpr
operator|!=
literal|null
operator|)
condition|?
name|compactExpr
operator|.
name|getExprString
argument_list|()
else|:
literal|"null"
operator|)
operator|+
literal|"; filter w/o compacting: "
operator|+
operator|(
operator|(
name|prunerExpr
operator|!=
literal|null
operator|)
condition|?
name|prunerExpr
operator|.
name|getExprString
argument_list|()
else|:
literal|"null"
operator|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|compactExpr
operator|==
literal|null
condition|)
block|{
comment|// This could happen when hive.mapred.mode=nonstrict and all the predicates
comment|// are on non-partition columns.
name|unkn_parts
operator|.
name|addAll
argument_list|(
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartitions
argument_list|(
name|tab
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|message
init|=
name|Utilities
operator|.
name|checkJDOPushDown
argument_list|(
name|tab
argument_list|,
name|compactExpr
argument_list|)
decl_stmt|;
if|if
condition|(
name|message
operator|==
literal|null
condition|)
block|{
name|String
name|filter
init|=
name|compactExpr
operator|.
name|getExprString
argument_list|()
decl_stmt|;
name|String
name|oldFilter
init|=
name|prunerExpr
operator|.
name|getExprString
argument_list|()
decl_stmt|;
if|if
condition|(
name|filter
operator|.
name|equals
argument_list|(
name|oldFilter
argument_list|)
condition|)
block|{
comment|// pruneExpr contains only partition columns
name|pruneByPushDown
argument_list|(
name|tab
argument_list|,
name|true_parts
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// pruneExpr contains non-partition columns
name|pruneByPushDown
argument_list|(
name|tab
argument_list|,
name|unkn_parts
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_JDO_FILTER_EXPRESSION
operator|.
name|getMsg
argument_list|(
literal|"by condition '"
operator|+
name|message
operator|+
literal|"'"
argument_list|)
argument_list|)
expr_stmt|;
name|pruneBySequentialScan
argument_list|(
name|tab
argument_list|,
name|true_parts
argument_list|,
name|unkn_parts
argument_list|,
name|denied_parts
argument_list|,
name|prunerExpr
argument_list|,
name|rowObjectInspector
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"tabname = "
operator|+
name|tab
operator|.
name|getTableName
argument_list|()
operator|+
literal|" is partitioned"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|true_parts
operator|.
name|addAll
argument_list|(
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartitions
argument_list|(
name|tab
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// Now return the set of partitions
name|ret
operator|=
operator|new
name|PrunedPartitionList
argument_list|(
name|tab
argument_list|,
name|true_parts
argument_list|,
name|unkn_parts
argument_list|,
name|denied_parts
argument_list|)
expr_stmt|;
name|prunedPartitionsMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|ret
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
comment|/**    * Taking a partition pruning expression, remove the null operands.    * @param expr original partition pruning expression.    * @return partition pruning expression that only contains partition columns.    */
specifier|static
specifier|private
name|ExprNodeDesc
name|compactExpr
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|expr
operator|)
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|expr
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
name|GenericUDF
name|udf
init|=
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|expr
operator|)
operator|.
name|getGenericUDF
argument_list|()
decl_stmt|;
if|if
condition|(
name|udf
operator|instanceof
name|GenericUDFOPAnd
operator|||
name|udf
operator|instanceof
name|GenericUDFOPOr
condition|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|expr
operator|)
operator|.
name|getChildren
argument_list|()
decl_stmt|;
name|ExprNodeDesc
name|left
init|=
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|children
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|compactExpr
argument_list|(
name|left
argument_list|)
argument_list|)
expr_stmt|;
name|ExprNodeDesc
name|right
init|=
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|children
operator|.
name|set
argument_list|(
literal|1
argument_list|,
name|compactExpr
argument_list|(
name|right
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|==
literal|null
operator|&&
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
return|return
name|expr
return|;
block|}
return|return
name|expr
return|;
block|}
comment|/**    * Pruning partition using JDO filtering.    * @param tab the table containing the partitions.    * @param true_parts the resulting partitions.    * @param filter the SQL predicate that involves only partition columns    * @throws HiveException    * @throws MetaException    * @throws NoSuchObjectException    * @throws TException    */
specifier|static
specifier|private
name|void
name|pruneByPushDown
parameter_list|(
name|Table
name|tab
parameter_list|,
name|Set
argument_list|<
name|Partition
argument_list|>
name|true_parts
parameter_list|,
name|String
name|filter
parameter_list|)
throws|throws
name|HiveException
throws|,
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|TException
block|{
name|Hive
name|db
init|=
name|Hive
operator|.
name|get
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
name|db
operator|.
name|getPartitionsByFilter
argument_list|(
name|tab
argument_list|,
name|filter
argument_list|)
decl_stmt|;
name|true_parts
operator|.
name|addAll
argument_list|(
name|parts
argument_list|)
expr_stmt|;
return|return;
block|}
comment|/**    * Pruning partition by getting the partition names first and pruning using Hive expression    * evaluator.    * @param tab the table containing the partitions.    * @param true_parts the resulting partitions if the partition pruning expression only contains    *        partition columns.    * @param unkn_parts the resulting partitions if the partition pruning expression that only contains    *        non-partition columns.    * @param denied_parts pruned out partitions.    * @param prunerExpr the SQL predicate that involves partition columns.    * @param rowObjectInspector object inspector used by the evaluator    * @throws Exception    */
specifier|static
specifier|private
name|void
name|pruneBySequentialScan
parameter_list|(
name|Table
name|tab
parameter_list|,
name|Set
argument_list|<
name|Partition
argument_list|>
name|true_parts
parameter_list|,
name|Set
argument_list|<
name|Partition
argument_list|>
name|unkn_parts
parameter_list|,
name|Set
argument_list|<
name|Partition
argument_list|>
name|denied_parts
parameter_list|,
name|ExprNodeDesc
name|prunerExpr
parameter_list|,
name|StructObjectInspector
name|rowObjectInspector
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|trueNames
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|unknNames
init|=
literal|null
decl_stmt|;
name|PerfLogger
name|perfLogger
init|=
name|PerfLogger
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|LOG
argument_list|,
name|PerfLogger
operator|.
name|PRUNE_LISTING
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartitionNames
argument_list|(
name|tab
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tab
operator|.
name|getTableName
argument_list|()
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|pCols
init|=
name|tab
operator|.
name|getPartCols
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|pCols
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|pCols
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|Object
index|[]
name|objectWithPart
init|=
operator|new
name|Object
index|[
literal|2
index|]
decl_stmt|;
for|for
control|(
name|FieldSchema
name|pCol
range|:
name|pCols
control|)
block|{
name|partCols
operator|.
name|add
argument_list|(
name|pCol
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|PrimitiveObjectInspector
argument_list|,
name|ExprNodeEvaluator
argument_list|>
name|handle
init|=
name|PartExprEvalUtils
operator|.
name|prepareExpr
argument_list|(
name|prunerExpr
argument_list|,
name|partCols
argument_list|,
name|rowObjectInspector
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|partName
range|:
name|partNames
control|)
block|{
comment|// Set all the variables here
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|Warehouse
operator|.
name|makeSpecFromName
argument_list|(
name|partName
argument_list|)
decl_stmt|;
name|values
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kv
range|:
name|partSpec
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|objectWithPart
index|[
literal|1
index|]
operator|=
name|values
expr_stmt|;
comment|// evaluate the expression tree
name|Boolean
name|r
init|=
operator|(
name|Boolean
operator|)
name|PartExprEvalUtils
operator|.
name|evaluateExprOnPart
argument_list|(
name|handle
argument_list|,
name|objectWithPart
argument_list|)
decl_stmt|;
if|if
condition|(
name|r
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|unknNames
operator|==
literal|null
condition|)
block|{
name|unknNames
operator|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|unknNames
operator|.
name|add
argument_list|(
name|partName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"retained unknown partition: "
operator|+
name|partName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Boolean
operator|.
name|TRUE
operator|.
name|equals
argument_list|(
name|r
argument_list|)
condition|)
block|{
if|if
condition|(
name|trueNames
operator|==
literal|null
condition|)
block|{
name|trueNames
operator|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|trueNames
operator|.
name|add
argument_list|(
name|partName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"retained partition: "
operator|+
name|partName
argument_list|)
expr_stmt|;
block|}
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|LOG
argument_list|,
name|PerfLogger
operator|.
name|PRUNE_LISTING
argument_list|)
expr_stmt|;
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|LOG
argument_list|,
name|PerfLogger
operator|.
name|PARTITION_RETRIEVING
argument_list|)
expr_stmt|;
if|if
condition|(
name|trueNames
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartitionsByNames
argument_list|(
name|tab
argument_list|,
name|trueNames
argument_list|)
decl_stmt|;
name|true_parts
operator|.
name|addAll
argument_list|(
name|parts
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|unknNames
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartitionsByNames
argument_list|(
name|tab
argument_list|,
name|unknNames
argument_list|)
decl_stmt|;
name|unkn_parts
operator|.
name|addAll
argument_list|(
name|parts
argument_list|)
expr_stmt|;
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|LOG
argument_list|,
name|PerfLogger
operator|.
name|PARTITION_RETRIEVING
argument_list|)
expr_stmt|;
block|}
comment|/**    * Whether the expression contains a column node or not.    */
specifier|public
specifier|static
name|boolean
name|hasColumnExpr
parameter_list|(
name|ExprNodeDesc
name|desc
parameter_list|)
block|{
comment|// Return false for null
if|if
condition|(
name|desc
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Return true for exprNodeColumnDesc
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Return true in case one of the children is column expr.
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
name|desc
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|children
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|hasColumnExpr
argument_list|(
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
comment|// Return false otherwise
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

