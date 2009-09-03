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
name|ExprNodeEvaluatorFactory
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
name|exprNodeColumnDesc
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
name|exprNodeDesc
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
name|ObjectInspectorFactory
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
comment|/* (non-Javadoc)    * @see org.apache.hadoop.hive.ql.optimizer.Transform#transform(org.apache.hadoop.hive.ql.parse.ParseContext)    */
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
literal|"(TS%FIL%)|(TS%FIL%FIL%)"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getFilterProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching rule and passes the context along
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
comment|/**    * Get the partition list for the table that satisfies the partition pruner    * condition.    *     * @param tab    the table object for the alias    * @param prunerExpr  the pruner expression for the alias    * @param conf   for checking whether "strict" mode is on.    * @param alias  for generating error message only.    * @return the partition list for the table that satisfies the partition pruner condition.    * @throws HiveException    */
specifier|public
specifier|static
name|PrunedPartitionList
name|prune
parameter_list|(
name|Table
name|tab
parameter_list|,
name|exprNodeDesc
name|prunerExpr
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
literal|"tabname = "
operator|+
name|tab
operator|.
name|getName
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
for|for
control|(
name|String
name|partName
range|:
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
name|getName
argument_list|()
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
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
comment|// Create the row object
name|ArrayList
argument_list|<
name|String
argument_list|>
name|partNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|partValues
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|partObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
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
name|entry
range|:
name|partSpec
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|partNames
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|partValues
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|partObjectInspectors
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|partObjectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|partNames
argument_list|,
name|partObjectInspectors
argument_list|)
decl_stmt|;
name|rowWithPart
index|[
literal|1
index|]
operator|=
name|partValues
expr_stmt|;
name|ArrayList
argument_list|<
name|StructObjectInspector
argument_list|>
name|ois
init|=
operator|new
name|ArrayList
argument_list|<
name|StructObjectInspector
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|rowObjectInspector
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|partObjectInspector
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|rowWithPartObjectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getUnionStructObjectInspector
argument_list|(
name|ois
argument_list|)
decl_stmt|;
comment|// If the "strict" mode is on, we have to provide partition pruner for each table.
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
name|getName
argument_list|()
operator|+
literal|"\""
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|// evaluate the expression tree
if|if
condition|(
name|prunerExpr
operator|!=
literal|null
condition|)
block|{
name|ExprNodeEvaluator
name|evaluator
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|prunerExpr
argument_list|)
decl_stmt|;
name|ObjectInspector
name|evaluateResultOI
init|=
name|evaluator
operator|.
name|initialize
argument_list|(
name|rowWithPartObjectInspector
argument_list|)
decl_stmt|;
name|Object
name|evaluateResultO
init|=
name|evaluator
operator|.
name|evaluate
argument_list|(
name|rowWithPart
argument_list|)
decl_stmt|;
name|Boolean
name|r
init|=
call|(
name|Boolean
call|)
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|evaluateResultOI
argument_list|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|evaluateResultO
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"prune result for partition "
operator|+
name|partSpec
operator|+
literal|": "
operator|+
name|r
argument_list|)
expr_stmt|;
if|if
condition|(
name|Boolean
operator|.
name|FALSE
operator|.
name|equals
argument_list|(
name|r
argument_list|)
condition|)
block|{
if|if
condition|(
name|denied_parts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Partition
name|part
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartition
argument_list|(
name|tab
argument_list|,
name|partSpec
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
decl_stmt|;
name|denied_parts
operator|.
name|add
argument_list|(
name|part
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|trace
argument_list|(
literal|"pruned partition: "
operator|+
name|partSpec
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Partition
name|part
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartition
argument_list|(
name|tab
argument_list|,
name|partSpec
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
decl_stmt|;
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"retained partition: "
operator|+
name|partSpec
argument_list|)
expr_stmt|;
name|true_parts
operator|.
name|add
argument_list|(
name|part
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"unknown partition: "
operator|+
name|partSpec
argument_list|)
expr_stmt|;
name|unkn_parts
operator|.
name|add
argument_list|(
name|part
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// is there is no parition pruning, all of them are needed
name|true_parts
operator|.
name|add
argument_list|(
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartition
argument_list|(
name|tab
argument_list|,
name|partSpec
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
return|return
operator|new
name|PrunedPartitionList
argument_list|(
name|true_parts
argument_list|,
name|unkn_parts
argument_list|,
name|denied_parts
argument_list|)
return|;
block|}
comment|/**    * Whether the expression contains a column node or not.    */
specifier|public
specifier|static
name|boolean
name|hasColumnExpr
parameter_list|(
name|exprNodeDesc
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
name|exprNodeColumnDesc
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Return true in case one of the children is column expr.
name|List
argument_list|<
name|exprNodeDesc
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

