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

begin_class
specifier|public
class|class
name|OperatorUtils
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
name|ReduceSinkOperator
name|rs
init|=
operator|(
operator|(
name|ReduceSinkOperator
operator|)
name|op
operator|)
decl_stmt|;
if|if
condition|(
name|outMap
operator|.
name|containsKey
argument_list|(
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputName
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting output collector: "
operator|+
name|rs
operator|+
literal|" --> "
operator|+
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputName
argument_list|()
argument_list|)
expr_stmt|;
name|rs
operator|.
name|setOutputCollector
argument_list|(
name|outMap
operator|.
name|get
argument_list|(
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputName
argument_list|()
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
block|}
end_class

end_unit

