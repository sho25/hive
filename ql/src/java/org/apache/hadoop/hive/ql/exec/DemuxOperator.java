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
name|Collection
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
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
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
name|DemuxDesc
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
name|TableDesc
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
name|Deserializer
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
name|SerDeUtils
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|ReflectionUtil
import|;
end_import

begin_comment
comment|/**  * DemuxOperator is an operator used by MapReduce Jobs optimized by  * CorrelationOptimizer. If used, DemuxOperator is the first operator in reduce  * phase. In the case that multiple operation paths are merged into a single one, it will dispatch  * the record to corresponding child operators (Join or GBY).  */
end_comment

begin_class
specifier|public
class|class
name|DemuxOperator
extends|extends
name|Operator
argument_list|<
name|DemuxDesc
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|DemuxOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Counters for debugging, we cannot use existing counters (cntr and nextCntr)
comment|// in Operator since we want to individually track the number of rows from
comment|// different paths.
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
comment|// The mapping from a newTag to its corresponding oldTag.
comment|// oldTag is the tag assigned to ReduceSinkOperators BEFORE Correlation Optimizer
comment|// optimizes the operator tree. newTag is the tag assigned to ReduceSinkOperators
comment|// AFTER Correlation Optimizer optimizes the operator tree.
comment|// Example: we have an operator tree shown below ...
comment|//        JOIN2
comment|//       /     \
comment|//   GBY1       JOIN1
comment|//    |         /    \
comment|//   RS1       RS2   RS3
comment|// If GBY1, JOIN1, and JOIN2 are executed in the same Reducer
comment|// (optimized by Correlation Optimizer), we will have ...
comment|// oldTag: RS1:0, RS2:0, RS3:1
comment|// newTag: RS1:0, RS2:1, RS3:2
comment|// We need to know the mapping from the newTag to oldTag and revert
comment|// the newTag to oldTag to make operators in the operator tree
comment|// function correctly.
specifier|private
name|int
index|[]
name|newTagToOldTag
decl_stmt|;
comment|// The mapping from a newTag to the index of the corresponding child
comment|// of this operator.
specifier|private
name|int
index|[]
name|newTagToChildIndex
decl_stmt|;
comment|// The mapping from the index of a child operator to its corresponding
comment|// inputObjectInspectors
specifier|private
name|ObjectInspector
index|[]
index|[]
name|childInputObjInspectors
decl_stmt|;
specifier|private
name|int
name|childrenDone
decl_stmt|;
comment|// The index of the child which the last row was forwarded to in a key group.
specifier|private
name|int
name|lastChildIndex
decl_stmt|;
comment|// Since DemuxOperator may appear multiple times in MuxOperator's parents list.
comment|// We use newChildIndexTag instead of childOperatorsTag.
comment|// Example:
comment|//         JOIN
comment|//           |
comment|//          MUX
comment|//         / | \
comment|//        /  |  \
comment|//       /   |   \
comment|//       |  GBY  |
comment|//       \   |   /
comment|//        \  |  /
comment|//         DEMUX
comment|// In this case, the parent list of MUX is [DEMUX, GBY, DEMUX],
comment|// so we need to have two childOperatorsTags (the index of this DemuxOperator in
comment|// its children's parents lists, also see childOperatorsTag in Operator) at here.
specifier|private
name|int
index|[]
index|[]
name|newChildOperatorsTag
decl_stmt|;
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
comment|// A DemuxOperator should have at least one child
if|if
condition|(
name|childOperatorsArray
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Expected number of children is at least 1. Found : "
operator|+
name|childOperatorsArray
operator|.
name|length
argument_list|)
throw|;
block|}
name|newTagToOldTag
operator|=
name|toArray
argument_list|(
name|conf
operator|.
name|getNewTagToOldTag
argument_list|()
argument_list|)
expr_stmt|;
name|newTagToChildIndex
operator|=
name|toArray
argument_list|(
name|conf
operator|.
name|getNewTagToChildIndex
argument_list|()
argument_list|)
expr_stmt|;
name|childInputObjInspectors
operator|=
operator|new
name|ObjectInspector
index|[
name|childOperators
operator|.
name|size
argument_list|()
index|]
index|[]
expr_stmt|;
name|cntrs
operator|=
operator|new
name|long
index|[
name|newTagToOldTag
operator|.
name|length
index|]
expr_stmt|;
name|nextCntrs
operator|=
operator|new
name|long
index|[
name|newTagToOldTag
operator|.
name|length
index|]
expr_stmt|;
try|try
block|{
comment|// We populate inputInspectors for all children of this DemuxOperator.
comment|// Those inputObjectInspectors are stored in childInputObjInspectors.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|newTagToOldTag
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|newTag
init|=
name|i
decl_stmt|;
name|int
name|oldTag
init|=
name|newTagToOldTag
index|[
name|i
index|]
decl_stmt|;
name|int
name|childIndex
init|=
name|newTagToChildIndex
index|[
name|newTag
index|]
decl_stmt|;
name|cntrs
index|[
name|newTag
index|]
operator|=
literal|0
expr_stmt|;
name|nextCntrs
index|[
name|newTag
index|]
operator|=
literal|0
expr_stmt|;
name|TableDesc
name|keyTableDesc
init|=
name|conf
operator|.
name|getKeysSerializeInfos
argument_list|()
operator|.
name|get
argument_list|(
name|newTag
argument_list|)
decl_stmt|;
name|Deserializer
name|inputKeyDeserializer
init|=
name|ReflectionUtil
operator|.
name|newInstance
argument_list|(
name|keyTableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|inputKeyDeserializer
argument_list|,
literal|null
argument_list|,
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|TableDesc
name|valueTableDesc
init|=
name|conf
operator|.
name|getValuesSerializeInfos
argument_list|()
operator|.
name|get
argument_list|(
name|newTag
argument_list|)
decl_stmt|;
name|Deserializer
name|inputValueDeserializer
init|=
name|ReflectionUtil
operator|.
name|newInstance
argument_list|(
name|valueTableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|inputValueDeserializer
argument_list|,
literal|null
argument_list|,
name|valueTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|oi
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|oi
operator|.
name|add
argument_list|(
name|inputKeyDeserializer
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
name|oi
operator|.
name|add
argument_list|(
name|inputValueDeserializer
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|childParentsCount
init|=
name|conf
operator|.
name|getChildIndexToOriginalNumParents
argument_list|()
operator|.
name|get
argument_list|(
name|childIndex
argument_list|)
decl_stmt|;
comment|// Multiple newTags can point to the same child (e.g. when the child is a JoinOperator).
comment|// So, we first check if childInputObjInspectors contains the key of childIndex.
if|if
condition|(
name|childInputObjInspectors
index|[
name|childIndex
index|]
operator|==
literal|null
condition|)
block|{
name|childInputObjInspectors
index|[
name|childIndex
index|]
operator|=
operator|new
name|ObjectInspector
index|[
name|childParentsCount
index|]
expr_stmt|;
block|}
name|ObjectInspector
index|[]
name|ois
init|=
name|childInputObjInspectors
index|[
name|childIndex
index|]
decl_stmt|;
name|ois
index|[
name|oldTag
index|]
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|Utilities
operator|.
name|reduceFieldNameList
argument_list|,
name|oi
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|childrenDone
operator|=
literal|0
expr_stmt|;
name|newChildOperatorsTag
operator|=
operator|new
name|int
index|[
name|childOperators
operator|.
name|size
argument_list|()
index|]
index|[]
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
name|childOperators
operator|.
name|size
argument_list|()
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
name|childOperators
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|childOperatorTags
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|child
operator|instanceof
name|MuxOperator
condition|)
block|{
comment|// This DemuxOperator can appear multiple times in MuxOperator's
comment|// parentOperators
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
range|:
name|child
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|this
operator|==
name|parent
condition|)
block|{
name|childOperatorTags
operator|.
name|add
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
name|index
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|childOperatorTags
operator|.
name|add
argument_list|(
name|child
operator|.
name|getParentOperators
argument_list|()
operator|.
name|indexOf
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|newChildOperatorsTag
index|[
name|i
index|]
operator|=
name|toArray
argument_list|(
name|childOperatorTags
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"newChildOperatorsTag "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|newChildOperatorsTag
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|int
index|[]
name|toArray
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|list
parameter_list|)
block|{
name|int
index|[]
name|array
init|=
operator|new
name|int
index|[
name|list
operator|.
name|size
argument_list|()
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
name|list
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|array
index|[
name|i
index|]
operator|=
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|array
return|;
block|}
specifier|private
name|int
index|[]
name|toArray
parameter_list|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|map
parameter_list|)
block|{
name|int
index|[]
name|array
init|=
operator|new
name|int
index|[
name|map
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|array
index|[
name|entry
operator|.
name|getKey
argument_list|()
index|]
operator|=
name|entry
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
return|return
name|array
return|;
block|}
comment|// Each child should has its own outputObjInspector
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
name|isLogInfoEnabled
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
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing child "
operator|+
name|i
operator|+
literal|" "
operator|+
name|childOperatorsArray
index|[
name|i
index|]
operator|.
name|getIdentifier
argument_list|()
operator|+
literal|" "
operator|+
name|childOperatorsArray
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|childInputObjInspectors
index|[
name|i
index|]
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|// We need to initialize those MuxOperators first because if we first
comment|// initialize other operators, the states of all parents of those MuxOperators
comment|// are INIT (including this DemuxOperator),
comment|// but the inputInspector of those MuxOperators has not been set.
if|if
condition|(
name|childOperatorsArray
index|[
name|i
index|]
operator|instanceof
name|MuxOperator
condition|)
block|{
comment|// If this DemuxOperator directly connects to a MuxOperator,
comment|// that MuxOperator must be the parent of a JoinOperator.
comment|// In this case, that MuxOperator should be initialized
comment|// by multiple parents (of that MuxOperator).
name|ObjectInspector
index|[]
name|ois
init|=
name|childInputObjInspectors
index|[
name|i
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|ois
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|ois
index|[
name|j
index|]
operator|!=
literal|null
condition|)
block|{
name|childOperatorsArray
index|[
name|i
index|]
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|ois
index|[
name|j
index|]
argument_list|,
name|j
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
continue|continue;
block|}
if|if
condition|(
name|reporter
operator|!=
literal|null
condition|)
block|{
name|childOperatorsArray
index|[
name|i
index|]
operator|.
name|setReporter
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
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
name|childOperatorsArray
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing child "
operator|+
name|i
operator|+
literal|" "
operator|+
name|childOperatorsArray
index|[
name|i
index|]
operator|.
name|getIdentifier
argument_list|()
operator|+
literal|" "
operator|+
name|childOperatorsArray
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|childInputObjInspectors
index|[
name|i
index|]
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
name|childOperatorsArray
index|[
name|i
index|]
operator|instanceof
name|MuxOperator
operator|)
condition|)
block|{
name|childOperatorsArray
index|[
name|i
index|]
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|childInputObjInspectors
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
continue|continue;
block|}
if|if
condition|(
name|reporter
operator|!=
literal|null
condition|)
block|{
name|childOperatorsArray
index|[
name|i
index|]
operator|.
name|setReporter
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
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
name|int
name|currentChildIndex
init|=
name|newTagToChildIndex
index|[
name|tag
index|]
decl_stmt|;
comment|// Check if we start to forward rows to a new child.
comment|// If so, in the current key group, rows will not be forwarded
comment|// to those children which have an index less than the currentChildIndex.
comment|// We can call flush the buffer of children from lastChildIndex (inclusive)
comment|// to currentChildIndex (exclusive) and propagate processGroup to those children.
name|endGroupIfNecessary
argument_list|(
name|currentChildIndex
argument_list|)
expr_stmt|;
name|int
name|oldTag
init|=
name|newTagToOldTag
index|[
name|tag
index|]
decl_stmt|;
if|if
condition|(
name|isLogDebugEnabled
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
name|debug
argument_list|(
name|id
operator|+
literal|" (newTag, childIndex, oldTag)=("
operator|+
name|tag
operator|+
literal|", "
operator|+
name|currentChildIndex
operator|+
literal|", "
operator|+
name|oldTag
operator|+
literal|"), forwarding "
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
name|currentChildIndex
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
name|child
operator|.
name|process
argument_list|(
name|row
argument_list|,
name|oldTag
argument_list|)
expr_stmt|;
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
comment|// DemuxOperator forwards a row to exactly one child in its children list
comment|// based on the tag and newTagToChildIndex in processOp() method.
comment|// So we need not to do anything in here.
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|newTagToOldTag
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|newTag
init|=
name|i
decl_stmt|;
name|int
name|oldTag
init|=
name|newTagToOldTag
index|[
name|i
index|]
decl_stmt|;
name|int
name|childIndex
init|=
name|newTagToChildIndex
index|[
name|newTag
index|]
decl_stmt|;
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|id
operator|+
literal|" (newTag, childIndex, oldTag)=("
operator|+
name|newTag
operator|+
literal|", "
operator|+
name|childIndex
operator|+
literal|", "
operator|+
name|oldTag
operator|+
literal|"),  forwarded "
operator|+
name|cntrs
index|[
name|newTag
index|]
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * We assume that the input rows associated with the same key are ordered by    * the tag. Because a tag maps to a childindex, when we see a new childIndex,    * we will not see the last childIndex (lastChildIndex) again before we start    * a new key group. So, we can call flush the buffer of children    * from lastChildIndex (inclusive) to currentChildIndex (exclusive) and    * propagate processGroup to those children.    * @param currentChildIndex the childIndex we have right now.    * @throws HiveException    */
specifier|private
name|void
name|endGroupIfNecessary
parameter_list|(
name|int
name|currentChildIndex
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|lastChildIndex
operator|!=
name|currentChildIndex
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|lastChildIndex
init|;
name|i
operator|<
name|currentChildIndex
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
for|for
control|(
name|int
name|childTag
range|:
name|newChildOperatorsTag
index|[
name|i
index|]
control|)
block|{
name|child
operator|.
name|processGroup
argument_list|(
name|childTag
argument_list|)
expr_stmt|;
block|}
block|}
name|lastChildIndex
operator|=
name|currentChildIndex
expr_stmt|;
block|}
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
name|lastChildIndex
operator|=
literal|0
expr_stmt|;
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
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// We will start a new key group. We can call flush the buffer
comment|// of children from lastChildIndex (inclusive) to the last child and
comment|// propagate processGroup to those children.
for|for
control|(
name|int
name|i
init|=
name|lastChildIndex
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
for|for
control|(
name|int
name|childTag
range|:
name|newChildOperatorsTag
index|[
name|i
index|]
control|)
block|{
name|child
operator|.
name|processGroup
argument_list|(
name|childTag
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
literal|"DEMUX"
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
name|DEMUX
return|;
block|}
block|}
end_class

end_unit

