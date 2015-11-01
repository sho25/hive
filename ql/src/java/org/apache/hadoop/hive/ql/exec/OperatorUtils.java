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
name|Collection
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
name|NodeUtils
operator|.
name|Function
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
name|mapred
operator|.
name|OutputCollector
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
name|ImmutableMultimap
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
name|Multimap
import|;
end_import

begin_class
specifier|public
class|class
name|OperatorUtils
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|OperatorUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|findOperators
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
name|findOperators
argument_list|(
name|start
argument_list|,
name|clazz
argument_list|,
operator|new
name|HashSet
argument_list|<
name|T
argument_list|>
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|findSingleOperator
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
name|Set
argument_list|<
name|T
argument_list|>
name|found
init|=
name|findOperators
argument_list|(
name|start
argument_list|,
name|clazz
argument_list|,
operator|new
name|HashSet
argument_list|<
name|T
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|found
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|?
name|found
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
else|:
literal|null
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|findOperators
parameter_list|(
name|Collection
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|starts
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
name|Set
argument_list|<
name|T
argument_list|>
name|found
init|=
operator|new
name|HashSet
argument_list|<
name|T
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
range|:
name|starts
control|)
block|{
if|if
condition|(
name|start
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|findOperators
argument_list|(
name|start
argument_list|,
name|clazz
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
return|return
name|found
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|findOperators
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|,
name|Set
argument_list|<
name|T
argument_list|>
name|found
parameter_list|)
block|{
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|start
argument_list|)
condition|)
block|{
name|found
operator|.
name|add
argument_list|(
operator|(
name|T
operator|)
name|start
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|start
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|child
range|:
name|start
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
name|findOperators
argument_list|(
name|child
argument_list|,
name|clazz
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|found
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|findOperatorsUpstream
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
name|findOperatorsUpstream
argument_list|(
name|start
argument_list|,
name|clazz
argument_list|,
operator|new
name|HashSet
argument_list|<
name|T
argument_list|>
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|findSingleOperatorUpstream
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
name|Set
argument_list|<
name|T
argument_list|>
name|found
init|=
name|findOperatorsUpstream
argument_list|(
name|start
argument_list|,
name|clazz
argument_list|,
operator|new
name|HashSet
argument_list|<
name|T
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|found
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|?
name|found
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
else|:
literal|null
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|findOperatorsUpstream
parameter_list|(
name|Collection
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|starts
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
name|Set
argument_list|<
name|T
argument_list|>
name|found
init|=
operator|new
name|HashSet
argument_list|<
name|T
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
range|:
name|starts
control|)
block|{
name|findOperatorsUpstream
argument_list|(
name|start
argument_list|,
name|clazz
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
return|return
name|found
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|findOperatorsUpstream
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|,
name|Set
argument_list|<
name|T
argument_list|>
name|found
parameter_list|)
block|{
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|start
argument_list|)
condition|)
block|{
name|found
operator|.
name|add
argument_list|(
operator|(
name|T
operator|)
name|start
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|start
operator|.
name|getParentOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
range|:
name|start
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|findOperatorsUpstream
argument_list|(
name|parent
argument_list|,
name|clazz
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|found
return|;
block|}
specifier|public
specifier|static
name|void
name|setChildrenCollector
parameter_list|(
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOperators
parameter_list|,
name|OutputCollector
name|out
parameter_list|)
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
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
if|if
condition|(
name|op
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|ReduceSinkOperator
operator|.
name|getOperatorName
argument_list|()
argument_list|)
condition|)
block|{
name|op
operator|.
name|setOutputCollector
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setChildrenCollector
argument_list|(
name|op
operator|.
name|getChildOperators
argument_list|()
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|setChildrenCollector
parameter_list|(
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOperators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|OutputCollector
argument_list|>
name|outMap
parameter_list|)
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
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
if|if
condition|(
name|op
operator|.
name|getIsReduceSink
argument_list|()
condition|)
block|{
name|String
name|outputName
init|=
name|op
operator|.
name|getReduceOutputName
argument_list|()
decl_stmt|;
if|if
condition|(
name|outMap
operator|.
name|containsKey
argument_list|(
name|outputName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting output collector: "
operator|+
name|op
operator|+
literal|" --> "
operator|+
name|outputName
argument_list|)
expr_stmt|;
name|op
operator|.
name|setOutputCollector
argument_list|(
name|outMap
operator|.
name|get
argument_list|(
name|outputName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|setChildrenCollector
argument_list|(
name|op
operator|.
name|getChildOperators
argument_list|()
argument_list|,
name|outMap
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Starting at the input operator, finds the last operator in the stream that    * is an instance of the input class.    *    * @param op the starting operator    * @param clazz the class that the operator that we are looking for instantiates    * @return null if no such operator exists or multiple branches are found in    * the stream, the last operator otherwise    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|findLastOperator
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|currentOp
init|=
name|op
decl_stmt|;
name|T
name|lastOp
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|currentOp
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|currentOp
argument_list|)
condition|)
block|{
name|lastOp
operator|=
operator|(
name|T
operator|)
name|currentOp
expr_stmt|;
block|}
if|if
condition|(
name|currentOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|currentOp
operator|=
name|currentOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|currentOp
operator|=
literal|null
expr_stmt|;
block|}
block|}
return|return
name|lastOp
return|;
block|}
comment|/**    * Starting at the input operator, finds the last operator upstream that is    * an instance of the input class.    *    * @param op the starting operator    * @param clazz the class that the operator that we are looking for instantiates    * @return null if no such operator exists or multiple branches are found in    * the stream, the last operator otherwise    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|findLastOperatorUpstream
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|currentOp
init|=
name|op
decl_stmt|;
name|T
name|lastOp
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|currentOp
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|currentOp
argument_list|)
condition|)
block|{
name|lastOp
operator|=
operator|(
name|T
operator|)
name|currentOp
expr_stmt|;
block|}
if|if
condition|(
name|currentOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|currentOp
operator|=
name|currentOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|currentOp
operator|=
literal|null
expr_stmt|;
block|}
block|}
return|return
name|lastOp
return|;
block|}
specifier|public
specifier|static
name|void
name|iterateParents
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
parameter_list|,
name|Function
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|function
parameter_list|)
block|{
name|iterateParents
argument_list|(
name|operator
argument_list|,
name|function
argument_list|,
operator|new
name|HashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|iterateParents
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
parameter_list|,
name|Function
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|function
parameter_list|,
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|visited
parameter_list|)
block|{
if|if
condition|(
operator|!
name|visited
operator|.
name|add
argument_list|(
name|operator
argument_list|)
condition|)
block|{
return|return;
block|}
name|function
operator|.
name|apply
argument_list|(
name|operator
argument_list|)
expr_stmt|;
if|if
condition|(
name|operator
operator|.
name|getNumParent
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
range|:
name|operator
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|iterateParents
argument_list|(
name|parent
argument_list|,
name|function
argument_list|,
name|visited
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|boolean
name|sameRowSchema
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|operator1
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|operator2
parameter_list|)
block|{
return|return
name|operator1
operator|.
name|getSchema
argument_list|()
operator|.
name|equals
argument_list|(
name|operator2
operator|.
name|getSchema
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Given an operator and a set of classes, it classifies the operators it finds    * in the stream depending on the classes they instantiate.    *    * If a given operator object is an instance of more than one of the input classes,    * e.g. the operator instantiates one of the classes in the input set that is a    * subclass of another class in the set, the operator will be associated to both    * classes in the output map.    *    * @param start the start operator    * @param classes the set of classes    * @return a multimap from each of the classes to the operators that instantiate    * them    */
specifier|public
specifier|static
name|Multimap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|classifyOperators
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
parameter_list|,
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|classes
parameter_list|)
block|{
name|ImmutableMultimap
operator|.
name|Builder
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|resultMap
init|=
operator|new
name|ImmutableMultimap
operator|.
name|Builder
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|ops
operator|.
name|add
argument_list|(
name|start
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|ops
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|allChildren
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|ops
control|)
block|{
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|clazz
range|:
name|classes
control|)
block|{
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|resultMap
operator|.
name|put
argument_list|(
name|clazz
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
block|}
name|allChildren
operator|.
name|addAll
argument_list|(
name|op
operator|.
name|getChildOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ops
operator|=
name|allChildren
expr_stmt|;
block|}
return|return
name|resultMap
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Given an operator and a set of classes, it classifies the operators it finds    * upstream depending on the classes it instantiates.    *    * If a given operator object is an instance of more than one of the input classes,    * e.g. the operator instantiates one of the classes in the input set that is a    * subclass of another class in the set, the operator will be associated to both    * classes in the output map.    *    * @param start the start operator    * @param classes the set of classes    * @return a multimap from each of the classes to the operators that instantiate    * them    */
specifier|public
specifier|static
name|Multimap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|classifyOperatorsUpstream
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
parameter_list|,
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|classes
parameter_list|)
block|{
name|ImmutableMultimap
operator|.
name|Builder
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|resultMap
init|=
operator|new
name|ImmutableMultimap
operator|.
name|Builder
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|ops
operator|.
name|add
argument_list|(
name|start
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|ops
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|allParent
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|ops
control|)
block|{
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|clazz
range|:
name|classes
control|)
block|{
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|resultMap
operator|.
name|put
argument_list|(
name|clazz
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|op
operator|.
name|getParentOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|allParent
operator|.
name|addAll
argument_list|(
name|op
operator|.
name|getParentOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|ops
operator|=
name|allParent
expr_stmt|;
block|}
return|return
name|resultMap
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Given an operator and a set of classes, it returns the number of operators it finds    * upstream that instantiate any of the given classes.    *    * @param start the start operator    * @param classes the set of classes    * @return the number of operators    */
specifier|public
specifier|static
name|int
name|countOperatorsUpstream
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|start
parameter_list|,
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|classes
parameter_list|)
block|{
name|Multimap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
name|classifyOperatorsUpstream
argument_list|(
name|start
argument_list|,
name|classes
argument_list|)
decl_stmt|;
name|int
name|numberOperators
init|=
literal|0
decl_stmt|;
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|uniqueOperators
init|=
operator|new
name|HashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|ops
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|uniqueOperators
operator|.
name|add
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|numberOperators
operator|++
expr_stmt|;
block|}
block|}
return|return
name|numberOperators
return|;
block|}
block|}
end_class

end_unit

