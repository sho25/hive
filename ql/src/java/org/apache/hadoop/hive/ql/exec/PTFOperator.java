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
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|List
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
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|CompilationOpContext
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
name|PTFPartition
operator|.
name|PTFPartitionIterator
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
name|PTFDesc
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
name|PTFDeserializer
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
name|api
operator|.
name|OperatorType
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
name|ptf
operator|.
name|PTFExpressionDef
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
name|ptf
operator|.
name|PTFInputDef
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
name|ptf
operator|.
name|PartitionDef
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
name|ptf
operator|.
name|PartitionedTableFunctionDef
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
name|GenericUDFLeadLag
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
name|SerDe
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
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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

begin_class
specifier|public
class|class
name|PTFOperator
extends|extends
name|Operator
argument_list|<
name|PTFDesc
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
name|boolean
name|isMapOperator
decl_stmt|;
specifier|transient
name|KeyWrapperFactory
name|keyWrapperFactory
decl_stmt|;
specifier|protected
specifier|transient
name|KeyWrapper
name|currentKeys
decl_stmt|;
specifier|protected
specifier|transient
name|KeyWrapper
name|newKeys
decl_stmt|;
comment|/*    * for map-side invocation of PTFs, we cannot utilize the currentkeys null check    * to decide on invoking startPartition in streaming mode. Hence this extra flag.    */
specifier|transient
name|boolean
name|firstMapRow
decl_stmt|;
specifier|transient
name|Configuration
name|hiveConf
decl_stmt|;
specifier|transient
name|PTFInvocation
name|ptfInvocation
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|PTFOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|PTFOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
comment|/*    * 1. Find out if the operator is invoked at Map-Side or Reduce-side    * 2. Get the deserialized QueryDef    * 3. Reconstruct the transient variables in QueryDef    * 4. Create input partition to store rows coming from previous operator    */
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|jobConf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
name|jobConf
expr_stmt|;
name|isMapOperator
operator|=
name|conf
operator|.
name|isMapSide
argument_list|()
expr_stmt|;
name|reconstructQueryDef
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
if|if
condition|(
name|isMapOperator
condition|)
block|{
name|PartitionedTableFunctionDef
name|tDef
init|=
name|conf
operator|.
name|getStartOfChain
argument_list|()
decl_stmt|;
name|outputObjInspector
operator|=
name|tDef
operator|.
name|getRawInputShape
argument_list|()
operator|.
name|getOI
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|outputObjInspector
operator|=
name|conf
operator|.
name|getFuncDef
argument_list|()
operator|.
name|getOutputShape
argument_list|()
operator|.
name|getOI
argument_list|()
expr_stmt|;
block|}
name|setupKeysWrapper
argument_list|(
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|ptfInvocation
operator|=
name|setupChain
argument_list|()
expr_stmt|;
name|ptfInvocation
operator|.
name|initializeStreaming
argument_list|(
name|jobConf
argument_list|,
name|isMapOperator
argument_list|)
expr_stmt|;
name|firstMapRow
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
name|ptfInvocation
operator|.
name|finishPartition
argument_list|()
expr_stmt|;
name|ptfInvocation
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|isMapOperator
condition|)
block|{
comment|/*        * checkif current row belongs to the current accumulated Partition:        * - If not:        *  - process the current Partition        *  - reset input Partition        * - set currentKey to the newKey if it is null or has changed.        */
name|newKeys
operator|.
name|getNewKey
argument_list|(
name|row
argument_list|,
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|boolean
name|keysAreEqual
init|=
operator|(
name|currentKeys
operator|!=
literal|null
operator|&&
name|newKeys
operator|!=
literal|null
operator|)
condition|?
name|newKeys
operator|.
name|equals
argument_list|(
name|currentKeys
argument_list|)
else|:
literal|false
decl_stmt|;
if|if
condition|(
name|currentKeys
operator|!=
literal|null
operator|&&
operator|!
name|keysAreEqual
condition|)
block|{
name|ptfInvocation
operator|.
name|finishPartition
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|currentKeys
operator|==
literal|null
operator|||
operator|!
name|keysAreEqual
condition|)
block|{
name|ptfInvocation
operator|.
name|startPartition
argument_list|()
expr_stmt|;
if|if
condition|(
name|currentKeys
operator|==
literal|null
condition|)
block|{
name|currentKeys
operator|=
name|newKeys
operator|.
name|copyKey
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|currentKeys
operator|.
name|copyKey
argument_list|(
name|newKeys
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|firstMapRow
condition|)
block|{
name|ptfInvocation
operator|.
name|startPartition
argument_list|()
expr_stmt|;
name|firstMapRow
operator|=
literal|false
expr_stmt|;
block|}
name|ptfInvocation
operator|.
name|processRow
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initialize the visitor to use the QueryDefDeserializer Use the order    * defined in QueryDefWalker to visit the QueryDef    *    * @param hiveConf    * @throws HiveException    */
specifier|protected
name|void
name|reconstructQueryDef
parameter_list|(
name|Configuration
name|hiveConf
parameter_list|)
throws|throws
name|HiveException
block|{
name|PTFDeserializer
name|dS
init|=
operator|new
name|PTFDeserializer
argument_list|(
name|conf
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|,
name|hiveConf
argument_list|)
decl_stmt|;
name|dS
operator|.
name|initializePTFChain
argument_list|(
name|conf
operator|.
name|getFuncDef
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|setupKeysWrapper
parameter_list|(
name|ObjectInspector
name|inputOI
parameter_list|)
throws|throws
name|HiveException
block|{
name|PartitionDef
name|pDef
init|=
name|conf
operator|.
name|getStartOfChain
argument_list|()
operator|.
name|getPartition
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|PTFExpressionDef
argument_list|>
name|exprs
init|=
name|pDef
operator|.
name|getExpressions
argument_list|()
decl_stmt|;
name|int
name|numExprs
init|=
name|exprs
operator|.
name|size
argument_list|()
decl_stmt|;
name|ExprNodeEvaluator
index|[]
name|keyFields
init|=
operator|new
name|ExprNodeEvaluator
index|[
name|numExprs
index|]
decl_stmt|;
name|ObjectInspector
index|[]
name|keyOIs
init|=
operator|new
name|ObjectInspector
index|[
name|numExprs
index|]
decl_stmt|;
name|ObjectInspector
index|[]
name|currentKeyOIs
init|=
operator|new
name|ObjectInspector
index|[
name|numExprs
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numExprs
condition|;
name|i
operator|++
control|)
block|{
name|PTFExpressionDef
name|exprDef
init|=
name|exprs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|/*        * Why cannot we just use the ExprNodeEvaluator on the column?        * - because on the reduce-side it is initialized based on the rowOI of the HiveTable        *   and not the OI of the parent of this Operator on the reduce-side        */
name|keyFields
index|[
name|i
index|]
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|exprDef
operator|.
name|getExprNode
argument_list|()
argument_list|)
expr_stmt|;
name|keyOIs
index|[
name|i
index|]
operator|=
name|keyFields
index|[
name|i
index|]
operator|.
name|initialize
argument_list|(
name|inputOI
argument_list|)
expr_stmt|;
name|currentKeyOIs
index|[
name|i
index|]
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|keyOIs
index|[
name|i
index|]
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
block|}
name|keyWrapperFactory
operator|=
operator|new
name|KeyWrapperFactory
argument_list|(
name|keyFields
argument_list|,
name|keyOIs
argument_list|,
name|currentKeyOIs
argument_list|)
expr_stmt|;
name|newKeys
operator|=
name|keyWrapperFactory
operator|.
name|getKeyWrapper
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the name of the operator    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|PTFOperator
operator|.
name|getOperatorName
argument_list|()
return|;
block|}
specifier|static
specifier|public
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"PTF"
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|PTF
return|;
block|}
specifier|private
name|PTFInvocation
name|setupChain
parameter_list|()
block|{
name|Stack
argument_list|<
name|PartitionedTableFunctionDef
argument_list|>
name|fnDefs
init|=
operator|new
name|Stack
argument_list|<
name|PartitionedTableFunctionDef
argument_list|>
argument_list|()
decl_stmt|;
name|PTFInputDef
name|iDef
init|=
name|conf
operator|.
name|getFuncDef
argument_list|()
decl_stmt|;
while|while
condition|(
name|iDef
operator|instanceof
name|PartitionedTableFunctionDef
condition|)
block|{
name|fnDefs
operator|.
name|push
argument_list|(
operator|(
name|PartitionedTableFunctionDef
operator|)
name|iDef
argument_list|)
expr_stmt|;
name|iDef
operator|=
operator|(
operator|(
name|PartitionedTableFunctionDef
operator|)
name|iDef
operator|)
operator|.
name|getInput
argument_list|()
expr_stmt|;
block|}
name|PTFInvocation
name|curr
init|=
literal|null
decl_stmt|,
name|first
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|!
name|fnDefs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|PartitionedTableFunctionDef
name|currFn
init|=
name|fnDefs
operator|.
name|pop
argument_list|()
decl_stmt|;
name|curr
operator|=
operator|new
name|PTFInvocation
argument_list|(
name|curr
argument_list|,
name|currFn
operator|.
name|getTFunction
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|first
operator|==
literal|null
condition|)
block|{
name|first
operator|=
name|curr
expr_stmt|;
block|}
block|}
return|return
name|first
return|;
block|}
specifier|public
specifier|static
name|void
name|connectLeadLagFunctionsToPartition
parameter_list|(
name|PTFDesc
name|ptfDesc
parameter_list|,
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|pItr
parameter_list|)
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
name|llFnDescs
init|=
name|ptfDesc
operator|.
name|getLlInfo
argument_list|()
operator|.
name|getLeadLagExprs
argument_list|()
decl_stmt|;
if|if
condition|(
name|llFnDescs
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|ExprNodeGenericFuncDesc
name|llFnDesc
range|:
name|llFnDescs
control|)
block|{
name|GenericUDFLeadLag
name|llFn
init|=
operator|(
name|GenericUDFLeadLag
operator|)
name|llFnDesc
operator|.
name|getGenericUDF
argument_list|()
decl_stmt|;
name|llFn
operator|.
name|setpItr
argument_list|(
name|pItr
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Responsible for the flow of rows through the PTF Chain.    * An Invocation wraps a TableFunction.    * The PTFOp hands the chain each row through the processRow call.    * It also notifies the chain of when a Partition starts/finishes.    *    * There are several combinations depending    * whether the TableFunction and its successor support Streaming or Batch mode.    *    * Combination 1: Streaming + Streaming    * - Start Partition: invoke startPartition on tabFn.    * - Process Row: invoke process Row on tabFn.    *   Any output rows hand to next tabFn in chain or forward to next Operator.    * - Finish Partition: invoke finishPartition on tabFn.    *   Any output rows hand to next tabFn in chain or forward to next Operator.    *    * Combination 2: Streaming + Batch    * same as Combination 1    *    * Combination 3: Batch + Batch    * - Start Partition: create or reset the Input Partition for the tabFn    *   caveat is: if prev is also batch and it is not providing an Output Iterator    *   then we can just use its Output Partition.    * - Process Row: collect row in Input Partition    * - Finish Partition : invoke evaluate on tabFn on Input Partition    *   If function gives an Output Partition: set it on next Invocation's Input Partition    *   If function gives an Output Iterator: iterate and call processRow on next Invocation.    *   For last Invocation in chain: forward rows to next Operator.    *    * Combination 3: Batch + Stream    * Similar to Combination 3, except Finish Partition behavior slightly different    * - Finish Partition : invoke evaluate on tabFn on Input Partition    *   iterate output rows: hand to next tabFn in chain or forward to next Operator.    *    */
class|class
name|PTFInvocation
block|{
name|PTFInvocation
name|prev
decl_stmt|;
name|PTFInvocation
name|next
decl_stmt|;
name|TableFunctionEvaluator
name|tabFn
decl_stmt|;
name|PTFPartition
name|inputPart
decl_stmt|;
name|PTFPartition
name|outputPart
decl_stmt|;
name|Iterator
argument_list|<
name|Object
argument_list|>
name|outputPartRowsItr
decl_stmt|;
specifier|public
name|PTFInvocation
parameter_list|(
name|PTFInvocation
name|prev
parameter_list|,
name|TableFunctionEvaluator
name|tabFn
parameter_list|)
block|{
name|this
operator|.
name|prev
operator|=
name|prev
expr_stmt|;
name|this
operator|.
name|tabFn
operator|=
name|tabFn
expr_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|prev
operator|.
name|next
operator|=
name|this
expr_stmt|;
block|}
block|}
name|boolean
name|isOutputIterator
parameter_list|()
block|{
return|return
name|tabFn
operator|.
name|canAcceptInputAsStream
argument_list|()
operator|||
name|tabFn
operator|.
name|canIterateOutput
argument_list|()
return|;
block|}
name|boolean
name|isStreaming
parameter_list|()
block|{
return|return
name|tabFn
operator|.
name|canAcceptInputAsStream
argument_list|()
return|;
block|}
name|void
name|initializeStreaming
parameter_list|(
name|Configuration
name|cfg
parameter_list|,
name|boolean
name|isMapSide
parameter_list|)
throws|throws
name|HiveException
block|{
name|PartitionedTableFunctionDef
name|tabDef
init|=
name|tabFn
operator|.
name|getTableDef
argument_list|()
decl_stmt|;
name|PTFInputDef
name|inputDef
init|=
name|tabDef
operator|.
name|getInput
argument_list|()
decl_stmt|;
name|ObjectInspector
name|inputOI
init|=
name|conf
operator|.
name|getStartOfChain
argument_list|()
operator|==
name|tabDef
condition|?
name|inputObjInspectors
index|[
literal|0
index|]
else|:
name|inputDef
operator|.
name|getOutputShape
argument_list|()
operator|.
name|getOI
argument_list|()
decl_stmt|;
name|tabFn
operator|.
name|initializeStreaming
argument_list|(
name|cfg
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|inputOI
argument_list|,
name|isMapSide
argument_list|)
expr_stmt|;
if|if
condition|(
name|next
operator|!=
literal|null
condition|)
block|{
name|next
operator|.
name|initializeStreaming
argument_list|(
name|cfg
argument_list|,
name|isMapSide
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|startPartition
parameter_list|()
throws|throws
name|HiveException
block|{
if|if
condition|(
name|isStreaming
argument_list|()
condition|)
block|{
name|tabFn
operator|.
name|startPartition
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|prev
operator|==
literal|null
operator|||
name|prev
operator|.
name|isOutputIterator
argument_list|()
condition|)
block|{
if|if
condition|(
name|inputPart
operator|==
literal|null
condition|)
block|{
name|createInputPartition
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|inputPart
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|next
operator|!=
literal|null
condition|)
block|{
name|next
operator|.
name|startPartition
argument_list|()
expr_stmt|;
block|}
block|}
name|void
name|processRow
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|isStreaming
argument_list|()
condition|)
block|{
name|handleOutputRows
argument_list|(
name|tabFn
operator|.
name|processRow
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|inputPart
operator|.
name|append
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|handleOutputRows
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|outRows
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|outRows
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Object
name|orow
range|:
name|outRows
control|)
block|{
if|if
condition|(
name|next
operator|!=
literal|null
condition|)
block|{
name|next
operator|.
name|processRow
argument_list|(
name|orow
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|forward
argument_list|(
name|orow
argument_list|,
name|outputObjInspector
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|void
name|finishPartition
parameter_list|()
throws|throws
name|HiveException
block|{
if|if
condition|(
name|isStreaming
argument_list|()
condition|)
block|{
name|handleOutputRows
argument_list|(
name|tabFn
operator|.
name|finishPartition
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|tabFn
operator|.
name|canIterateOutput
argument_list|()
condition|)
block|{
name|outputPartRowsItr
operator|=
name|inputPart
operator|==
literal|null
condition|?
literal|null
else|:
name|tabFn
operator|.
name|iterator
argument_list|(
name|inputPart
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputPart
operator|=
name|inputPart
operator|==
literal|null
condition|?
literal|null
else|:
name|tabFn
operator|.
name|execute
argument_list|(
name|inputPart
argument_list|)
expr_stmt|;
name|outputPartRowsItr
operator|=
name|outputPart
operator|==
literal|null
condition|?
literal|null
else|:
name|outputPart
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|next
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|next
operator|.
name|isStreaming
argument_list|()
operator|&&
operator|!
name|isOutputIterator
argument_list|()
condition|)
block|{
name|next
operator|.
name|inputPart
operator|=
name|outputPart
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|outputPartRowsItr
operator|!=
literal|null
condition|)
block|{
while|while
condition|(
name|outputPartRowsItr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|next
operator|.
name|processRow
argument_list|(
name|outputPartRowsItr
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|next
operator|!=
literal|null
condition|)
block|{
name|next
operator|.
name|finishPartition
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|isStreaming
argument_list|()
condition|)
block|{
if|if
condition|(
name|outputPartRowsItr
operator|!=
literal|null
condition|)
block|{
while|while
condition|(
name|outputPartRowsItr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|forward
argument_list|(
name|outputPartRowsItr
operator|.
name|next
argument_list|()
argument_list|,
name|outputObjInspector
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**      * Create a new Partition.      * A partition has 2 OIs: the OI for the rows being put in and the OI for the rows      * coming out. You specify the output OI by giving the Serde to use to Serialize.      * Typically these 2 OIs are the same; but not always. For the      * first PTF in a chain the OI of the incoming rows is dictated by the Parent Op      * to this PTFOp. The output OI from the Partition is typically LazyBinaryStruct, but      * not always. In the case of Noop/NoopMap we keep the Strcuture the same as      * what is given to us.      *<p>      * The Partition we want to create here is for feeding the First table function in the chain.      * So for map-side processing use the Serde from the output Shape its InputDef.      * For reduce-side processing use the Serde from its RawInputShape(the shape      * after map-side processing).      * @param oi      * @param hiveConf      * @param isMapSide      * @return      * @throws HiveException      */
specifier|private
name|void
name|createInputPartition
parameter_list|()
throws|throws
name|HiveException
block|{
name|PartitionedTableFunctionDef
name|tabDef
init|=
name|tabFn
operator|.
name|getTableDef
argument_list|()
decl_stmt|;
name|PTFInputDef
name|inputDef
init|=
name|tabDef
operator|.
name|getInput
argument_list|()
decl_stmt|;
name|ObjectInspector
name|inputOI
init|=
name|conf
operator|.
name|getStartOfChain
argument_list|()
operator|==
name|tabDef
condition|?
name|inputObjInspectors
index|[
literal|0
index|]
else|:
name|inputDef
operator|.
name|getOutputShape
argument_list|()
operator|.
name|getOI
argument_list|()
decl_stmt|;
name|SerDe
name|serde
init|=
name|conf
operator|.
name|isMapSide
argument_list|()
condition|?
name|tabDef
operator|.
name|getInput
argument_list|()
operator|.
name|getOutputShape
argument_list|()
operator|.
name|getSerde
argument_list|()
else|:
name|tabDef
operator|.
name|getRawInputShape
argument_list|()
operator|.
name|getSerde
argument_list|()
decl_stmt|;
name|StructObjectInspector
name|outputOI
init|=
name|conf
operator|.
name|isMapSide
argument_list|()
condition|?
name|tabDef
operator|.
name|getInput
argument_list|()
operator|.
name|getOutputShape
argument_list|()
operator|.
name|getOI
argument_list|()
else|:
name|tabDef
operator|.
name|getRawInputShape
argument_list|()
operator|.
name|getOI
argument_list|()
decl_stmt|;
name|inputPart
operator|=
name|PTFPartition
operator|.
name|create
argument_list|(
name|conf
operator|.
name|getCfg
argument_list|()
argument_list|,
name|serde
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|inputOI
argument_list|,
name|outputOI
argument_list|)
expr_stmt|;
block|}
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|inputPart
operator|!=
literal|null
condition|)
block|{
name|inputPart
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|tabFn
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|next
operator|!=
literal|null
condition|)
block|{
name|next
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

