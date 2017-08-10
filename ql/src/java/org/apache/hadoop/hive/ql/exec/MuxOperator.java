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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|MuxDesc
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

begin_comment
comment|/**  * MuxOperator is used in the Reduce side of MapReduce jobs optimized by Correlation Optimizer.  * Correlation Optimizer will remove unnecessary ReduceSinkOperaotrs,  * and MuxOperators are used to replace those ReduceSinkOperaotrs.  * Example: The original operator tree is ...  *      JOIN2  *      /    \  *     RS4   RS5  *    /        \  *   GBY1     JOIN1  *    |       /    \  *   RS1     RS2   RS3  * If GBY1, JOIN1, and JOIN2 can be executed in the same reducer  * (optimized by Correlation Optimizer).  * The new operator tree will be ...  *      JOIN2  *        |  *       MUX  *      /   \  *    GBY1  JOIN1  *      \    /  *       DEMUX  *      /  |  \  *     /   |   \  *    /    |    \  *   RS1   RS2   RS3  *  * A MuxOperator has two functions.  * First, it will construct key, value and tag structure for  * the input of Join Operators.  * Second, it is a part of operator coordination mechanism which makes sure the operator tree  * in the Reducer can work correctly.  */
end_comment

begin_class
specifier|public
class|class
name|MuxOperator
extends|extends
name|Operator
argument_list|<
name|MuxDesc
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
name|MuxOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Handler is used to construct the key-value structure.    * This structure is needed by child JoinOperators and GroupByOperators of    * a MuxOperator to function correctly.    */
specifier|protected
specifier|static
class|class
name|Handler
block|{
specifier|private
specifier|final
name|ObjectInspector
name|outputObjInspector
decl_stmt|;
specifier|private
specifier|final
name|int
name|tag
decl_stmt|;
comment|/**      * The evaluators for the key columns. Key columns decide the sort order on      * the reducer side. Key columns are passed to the reducer in the "key".      */
specifier|private
specifier|final
name|ExprNodeEvaluator
index|[]
name|keyEval
decl_stmt|;
comment|/**      * The evaluators for the value columns. Value columns are passed to reducer      * in the "value".      */
specifier|private
specifier|final
name|ExprNodeEvaluator
index|[]
name|valueEval
decl_stmt|;
specifier|private
specifier|final
name|Object
index|[]
name|outputKey
decl_stmt|;
specifier|private
specifier|final
name|Object
index|[]
name|outputValue
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|forwardedRow
decl_stmt|;
specifier|public
name|Handler
parameter_list|(
name|ObjectInspector
name|inputObjInspector
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valueCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputKeyColumnNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputValueColumnNames
parameter_list|,
name|Integer
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|keyEval
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|keyCols
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|e
range|:
name|keyCols
control|)
block|{
name|keyEval
index|[
name|i
operator|++
index|]
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|outputKey
operator|=
operator|new
name|Object
index|[
name|keyEval
operator|.
name|length
index|]
expr_stmt|;
name|valueEval
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|valueCols
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|i
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|ExprNodeDesc
name|e
range|:
name|valueCols
control|)
block|{
name|valueEval
index|[
name|i
operator|++
index|]
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|outputValue
operator|=
operator|new
name|Object
index|[
name|valueEval
operator|.
name|length
index|]
expr_stmt|;
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
name|ObjectInspector
name|keyObjectInspector
init|=
name|initEvaluatorsAndReturnStruct
argument_list|(
name|keyEval
argument_list|,
name|outputKeyColumnNames
argument_list|,
name|inputObjInspector
argument_list|)
decl_stmt|;
name|ObjectInspector
name|valueObjectInspector
init|=
name|initEvaluatorsAndReturnStruct
argument_list|(
name|valueEval
argument_list|,
name|outputValueColumnNames
argument_list|,
name|inputObjInspector
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|keyObjectInspector
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|valueObjectInspector
argument_list|)
expr_stmt|;
name|this
operator|.
name|outputObjInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|Utilities
operator|.
name|reduceFieldNameList
argument_list|,
name|ois
argument_list|)
expr_stmt|;
name|this
operator|.
name|forwardedRow
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|Utilities
operator|.
name|reduceFieldNameList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ObjectInspector
name|getOutputObjInspector
parameter_list|()
block|{
return|return
name|outputObjInspector
return|;
block|}
specifier|public
name|int
name|getTag
parameter_list|()
block|{
return|return
name|tag
return|;
block|}
specifier|public
name|Object
name|process
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Evaluate the keys
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keyEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|outputKey
index|[
name|i
index|]
operator|=
name|keyEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
comment|// Evaluate the value
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valueEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|outputValue
index|[
name|i
index|]
operator|=
name|valueEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
name|forwardedRow
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// JoinOperator assumes the key is backed by an list.
comment|// To be consistent, the value array is also converted
comment|// to a list.
name|forwardedRow
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|outputKey
argument_list|)
argument_list|)
expr_stmt|;
name|forwardedRow
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|outputValue
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|forwardedRow
return|;
block|}
block|}
specifier|private
specifier|transient
name|ObjectInspector
index|[]
name|outputObjectInspectors
decl_stmt|;
specifier|private
specifier|transient
name|int
name|numParents
decl_stmt|;
specifier|private
specifier|transient
name|boolean
index|[]
name|forward
decl_stmt|;
specifier|private
specifier|transient
name|boolean
index|[]
name|processGroupCalled
decl_stmt|;
specifier|private
name|Handler
index|[]
name|handlers
decl_stmt|;
comment|// Counters for debugging, we cannot use existing counters (cntr and nextCntr)
comment|// in Operator since we want to individually track the number of rows from different inputs.
specifier|private
specifier|transient
name|long
index|[]
name|cntrs
decl_stmt|;
specifier|private
specifier|transient
name|long
index|[]
name|nextCntrs
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|MuxOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|MuxOperator
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
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
comment|// A MuxOperator should only have a single child
if|if
condition|(
name|childOperatorsArray
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Expected number of children is 1. Found : "
operator|+
name|childOperatorsArray
operator|.
name|length
argument_list|)
throw|;
block|}
name|numParents
operator|=
name|getNumParent
argument_list|()
expr_stmt|;
name|forward
operator|=
operator|new
name|boolean
index|[
name|numParents
index|]
expr_stmt|;
name|processGroupCalled
operator|=
operator|new
name|boolean
index|[
name|numParents
index|]
expr_stmt|;
name|outputObjectInspectors
operator|=
operator|new
name|ObjectInspector
index|[
name|numParents
index|]
expr_stmt|;
name|handlers
operator|=
operator|new
name|Handler
index|[
name|numParents
index|]
expr_stmt|;
name|cntrs
operator|=
operator|new
name|long
index|[
name|numParents
index|]
expr_stmt|;
name|nextCntrs
operator|=
operator|new
name|long
index|[
name|numParents
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numParents
condition|;
name|i
operator|++
control|)
block|{
name|processGroupCalled
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getParentToKeyCols
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// We do not need to evaluate the input row for this parent.
comment|// So, we can just forward it to the child of this MuxOperator.
name|handlers
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
name|forward
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|outputObjectInspectors
index|[
name|i
index|]
operator|=
name|inputObjInspectors
index|[
name|i
index|]
expr_stmt|;
block|}
else|else
block|{
name|handlers
index|[
name|i
index|]
operator|=
operator|new
name|Handler
argument_list|(
name|inputObjInspectors
index|[
name|i
index|]
argument_list|,
name|conf
operator|.
name|getParentToKeyCols
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|conf
operator|.
name|getParentToValueCols
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|conf
operator|.
name|getParentToOutputKeyColumnNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|conf
operator|.
name|getParentToOutputValueColumnNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|conf
operator|.
name|getParentToTag
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|forward
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|outputObjectInspectors
index|[
name|i
index|]
operator|=
name|handlers
index|[
name|i
index|]
operator|.
name|getOutputObjInspector
argument_list|()
expr_stmt|;
block|}
name|cntrs
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|nextCntrs
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
block|}
block|}
comment|/**    * Calls initialize on each of the children with outputObjetInspector as the    * output row format.    */
annotation|@
name|Override
specifier|protected
name|void
name|initializeChildren
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|state
operator|=
name|State
operator|.
name|INIT
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Operator "
operator|+
name|id
operator|+
literal|" "
operator|+
name|getName
argument_list|()
operator|+
literal|" initialized"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|childOperators
operator|==
literal|null
operator|||
name|childOperators
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing children of "
operator|+
name|id
operator|+
literal|" "
operator|+
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|childOperatorsArray
index|[
literal|0
index|]
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|outputObjectInspectors
argument_list|)
expr_stmt|;
if|if
condition|(
name|reporter
operator|!=
literal|null
condition|)
block|{
name|childOperatorsArray
index|[
literal|0
index|]
operator|.
name|setReporter
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
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
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|cntrs
index|[
name|tag
index|]
operator|++
expr_stmt|;
if|if
condition|(
name|cntrs
index|[
name|tag
index|]
operator|==
name|nextCntrs
index|[
name|tag
index|]
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|id
operator|+
literal|", tag="
operator|+
name|tag
operator|+
literal|", forwarding "
operator|+
name|cntrs
index|[
name|tag
index|]
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
name|nextCntrs
index|[
name|tag
index|]
operator|=
name|getNextCntr
argument_list|(
name|cntrs
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|childrenDone
init|=
literal|0
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
name|childOperatorsArray
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|child
init|=
name|childOperatorsArray
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|child
operator|.
name|getDone
argument_list|()
condition|)
block|{
name|childrenDone
operator|++
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|forward
index|[
name|tag
index|]
condition|)
block|{
comment|// No need to evaluate, just forward it.
name|child
operator|.
name|process
argument_list|(
name|row
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Call the corresponding handler to evaluate this row and
comment|// forward the result
name|child
operator|.
name|process
argument_list|(
name|handlers
index|[
name|tag
index|]
operator|.
name|process
argument_list|(
name|row
argument_list|)
argument_list|,
name|handlers
index|[
name|tag
index|]
operator|.
name|getTag
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// if all children are done, this operator is also done
if|if
condition|(
name|childrenDone
operator|==
name|childOperatorsArray
operator|.
name|length
condition|)
block|{
name|setDone
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|forward
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Because we need to revert the tag of a row to its old tag and
comment|// we cannot pass new tag to this method which is used to get
comment|// the old tag from the mapping of newTagToOldTag, we bypass
comment|// this method in MuxOperator and directly call process on children
comment|// in process() method..
block|}
annotation|@
name|Override
specifier|public
name|void
name|startGroup
parameter_list|()
throws|throws
name|HiveException
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
name|numParents
condition|;
name|i
operator|++
control|)
block|{
name|processGroupCalled
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
block|}
name|super
operator|.
name|startGroup
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|endGroup
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// do nothing
block|}
annotation|@
name|Override
specifier|public
name|void
name|processGroup
parameter_list|(
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|processGroupCalled
index|[
name|tag
index|]
operator|=
literal|true
expr_stmt|;
name|boolean
name|shouldProceed
init|=
literal|true
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
name|numParents
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|processGroupCalled
index|[
name|i
index|]
condition|)
block|{
name|shouldProceed
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|shouldProceed
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|child
init|=
name|childOperatorsArray
index|[
literal|0
index|]
decl_stmt|;
name|int
name|childTag
init|=
name|childOperatorsTag
index|[
literal|0
index|]
decl_stmt|;
name|child
operator|.
name|flush
argument_list|()
expr_stmt|;
name|child
operator|.
name|endGroup
argument_list|()
expr_stmt|;
name|child
operator|.
name|processGroup
argument_list|(
name|childTag
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
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
name|numParents
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|id
operator|+
literal|", tag="
operator|+
name|i
operator|+
literal|", forwarded "
operator|+
name|cntrs
index|[
name|i
index|]
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
block|}
block|}
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
name|MuxOperator
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
literal|"MUX"
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
name|MUX
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|logicalEquals
parameter_list|(
name|Operator
name|other
parameter_list|)
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

