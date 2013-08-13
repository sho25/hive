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
name|Iterator
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
name|metadata
operator|.
name|VirtualColumn
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
name|PrunerUtils
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
name|SerDeException
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
comment|/* Move logic to PrunerUtils.walkOperatorTree() so that it can be reused. */
name|PrunerUtils
operator|.
name|walkOperatorTree
argument_list|(
name|pctx
argument_list|,
name|opWalkerCtx
argument_list|,
name|OpProcFactory
operator|.
name|getFilterProc
argument_list|()
argument_list|,
name|OpProcFactory
operator|.
name|getDefaultProc
argument_list|()
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
comment|/**    * Get the partition list for the TS operator that satisfies the partition pruner    * condition.    */
specifier|public
specifier|static
name|PrunedPartitionList
name|prune
parameter_list|(
name|TableScanOperator
name|ts
parameter_list|,
name|ParseContext
name|parseCtx
parameter_list|,
name|String
name|alias
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|prune
argument_list|(
name|parseCtx
operator|.
name|getTopToTable
argument_list|()
operator|.
name|get
argument_list|(
name|ts
argument_list|)
argument_list|,
name|parseCtx
operator|.
name|getOpToPartPruner
argument_list|()
operator|.
name|get
argument_list|(
name|ts
argument_list|)
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|,
name|alias
argument_list|,
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getVirtualCols
argument_list|()
argument_list|,
name|parseCtx
operator|.
name|getPrunedPartitions
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get the partition list for the table that satisfies the partition pruner    * condition.    *    * @param tab    *          the table object for the alias    * @param prunerExpr    *          the pruner expression for the alias    * @param conf    *          for checking whether "strict" mode is on.    * @param alias    *          for generating error message only.    * @param vcs    *          virtual columns referenced    * @param prunedPartitionsMap    *          cached result for the table    * @return the partition list for the table that satisfies the partition    *         pruner condition.    * @throws HiveException    */
specifier|private
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
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|vcs
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
name|ret
operator|=
name|getPartitionsFromServer
argument_list|(
name|tab
argument_list|,
name|prunerExpr
argument_list|,
name|vcs
argument_list|,
name|conf
argument_list|,
name|alias
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
name|expr
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
specifier|private
specifier|static
name|PrunedPartitionList
name|getPartitionsFromServer
parameter_list|(
name|Table
name|tab
parameter_list|,
name|ExprNodeDesc
name|prunerExpr
parameter_list|,
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|vcs
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|String
name|alias
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
operator|!
name|tab
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
comment|// If the table is not partitioned, return everything.
return|return
operator|new
name|PrunedPartitionList
argument_list|(
name|tab
argument_list|,
operator|new
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
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
argument_list|,
literal|false
argument_list|)
return|;
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
operator|&&
operator|!
name|hasColumnExpr
argument_list|(
name|prunerExpr
argument_list|)
condition|)
block|{
comment|// If the "strict" mode is on, we have to provide partition pruner for each table.
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
if|if
condition|(
name|prunerExpr
operator|==
literal|null
condition|)
block|{
comment|// This can happen when hive.mapred.mode=nonstrict and there is no predicates at all.
return|return
operator|new
name|PrunedPartitionList
argument_list|(
name|tab
argument_list|,
operator|new
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
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
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|// Remove non-partition columns.
name|ExprNodeDesc
name|compactExpr
init|=
name|compactExpr
argument_list|(
name|prunerExpr
operator|.
name|clone
argument_list|()
argument_list|)
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
name|compactExpr
operator|==
literal|null
condition|)
block|{
comment|// This could happen when hive.mapred.mode=nonstrict and all the predicates
comment|// are on non-partition columns.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter "
operator|+
name|oldFilter
operator|+
literal|" was null after compacting"
argument_list|)
expr_stmt|;
return|return
operator|new
name|PrunedPartitionList
argument_list|(
name|tab
argument_list|,
operator|new
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
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
argument_list|,
literal|true
argument_list|)
return|;
block|}
name|Set
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|hasUnknownPartitions
init|=
literal|false
decl_stmt|;
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
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|message
operator|!=
literal|null
condition|)
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
name|hasUnknownPartitions
operator|=
name|pruneBySequentialScan
argument_list|(
name|tab
argument_list|,
name|partitions
argument_list|,
name|prunerExpr
argument_list|,
name|vcs
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|filter
init|=
name|compactExpr
operator|.
name|getExprString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter w/ compacting: "
operator|+
name|filter
operator|+
literal|"; filter w/o compacting: "
operator|+
name|oldFilter
argument_list|)
expr_stmt|;
name|hasUnknownPartitions
operator|=
operator|!
name|filter
operator|.
name|equals
argument_list|(
name|oldFilter
argument_list|)
expr_stmt|;
name|partitions
operator|.
name|addAll
argument_list|(
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartitionsByFilter
argument_list|(
name|tab
argument_list|,
name|filter
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|PrunedPartitionList
argument_list|(
name|tab
argument_list|,
name|partitions
argument_list|,
name|hasUnknownPartitions
argument_list|)
return|;
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
block|}
comment|/**    * Pruning partition by getting the partition names first and pruning using Hive expression    * evaluator.    * @param tab the table containing the partitions.    * @param partitions the resulting partitions.    * @param prunerExpr the SQL predicate that involves partition columns.    * @param vcs virtual columns referenced    * @param conf Hive Configuration object, can not be NULL.    * @return true iff the partition pruning expression contains non-partition columns.    */
specifier|static
specifier|private
name|boolean
name|pruneBySequentialScan
parameter_list|(
name|Table
name|tab
parameter_list|,
name|Set
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|,
name|ExprNodeDesc
name|prunerExpr
parameter_list|,
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|vcs
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
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
name|String
name|defaultPartitionName
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DEFAULTPARTITIONNAME
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
name|boolean
name|hasUnknownPartitions
init|=
name|prunePartitionNames
argument_list|(
name|partCols
argument_list|,
name|prunerExpr
argument_list|,
name|vcs
argument_list|,
name|defaultPartitionName
argument_list|,
name|partNames
argument_list|)
decl_stmt|;
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
operator|!
name|partNames
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|partitions
operator|.
name|addAll
argument_list|(
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartitionsByNames
argument_list|(
name|tab
argument_list|,
name|partNames
argument_list|)
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
return|return
name|hasUnknownPartitions
return|;
block|}
comment|/**    * Prunes partition names to see if they match the prune expression.    * @param tab Table.    * @param prunerExpr The expression to match.    * @param conf Hive configuration.    * @param partNames Partition names to filter. The list is modified in place.    * @return Whether the list has any partitions for which the expression may or may not match.    */
specifier|public
specifier|static
name|boolean
name|prunePartitionNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|ExprNodeDesc
name|prunerExpr
parameter_list|,
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|vcs
parameter_list|,
name|String
name|defaultPartitionName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|)
throws|throws
name|HiveException
throws|,
name|MetaException
block|{
comment|// Prepare the expression to filter on the columns.
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
name|columnNames
argument_list|,
name|vcs
argument_list|)
decl_stmt|;
comment|// Filter the name list.
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
name|columnNames
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|Object
name|evalArg
init|=
name|values
decl_stmt|;
name|boolean
name|hasVC
init|=
name|vcs
operator|!=
literal|null
operator|&&
operator|!
name|vcs
operator|.
name|isEmpty
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasVC
condition|)
block|{
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
name|objectWithPart
index|[
literal|0
index|]
operator|=
name|values
expr_stmt|;
name|evalArg
operator|=
name|objectWithPart
expr_stmt|;
block|}
name|boolean
name|hasUnknownPartitions
init|=
literal|false
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|partIter
init|=
name|partNames
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|partIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|partName
init|=
name|partIter
operator|.
name|next
argument_list|()
decl_stmt|;
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
name|values
operator|.
name|addAll
argument_list|(
name|partSpec
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
comment|// Evaluate the expression tree.
name|Boolean
name|isNeeded
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
name|evalArg
argument_list|)
decl_stmt|;
name|boolean
name|isUnknown
init|=
operator|(
name|isNeeded
operator|==
literal|null
operator|)
decl_stmt|;
if|if
condition|(
operator|!
name|isUnknown
operator|&&
operator|!
name|isNeeded
condition|)
block|{
name|partIter
operator|.
name|remove
argument_list|()
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|isUnknown
operator|&&
name|values
operator|.
name|contains
argument_list|(
name|defaultPartitionName
argument_list|)
condition|)
block|{
comment|// Reject default partitions if we couldn't determine whether we should include it or not.
comment|// Note that predicate would only contains partition column parts of original predicate.
name|LOG
operator|.
name|debug
argument_list|(
literal|"skipping default/bad partition: "
operator|+
name|partName
argument_list|)
expr_stmt|;
name|partIter
operator|.
name|remove
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|hasUnknownPartitions
operator||=
name|isUnknown
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"retained "
operator|+
operator|(
name|isUnknown
condition|?
literal|"unknown "
else|:
literal|""
operator|)
operator|+
literal|"partition: "
operator|+
name|partName
argument_list|)
expr_stmt|;
block|}
return|return
name|hasUnknownPartitions
return|;
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

