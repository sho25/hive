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
name|plan
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|LinkedHashMap
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|lang3
operator|.
name|tuple
operator|.
name|ImmutablePair
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
name|lang3
operator|.
name|tuple
operator|.
name|Pair
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
name|Explain
operator|.
name|Vectorization
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
name|Explain
operator|.
name|Level
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
comment|/**  * This class encapsulates all the work objects that can be executed  * in a single Spark job. Currently it's basically a tree with MapWork at the  * roots and and ReduceWork at all other nodes.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Spark"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|,
name|vectorization
operator|=
name|Vectorization
operator|.
name|SUMMARY_PATH
argument_list|)
specifier|public
class|class
name|SparkWork
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|dagName
decl_stmt|;
specifier|private
specifier|final
name|String
name|queryId
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|roots
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|leaves
init|=
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|workGraph
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|BaseWork
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|invertedWorkGraph
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|BaseWork
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|Map
argument_list|<
name|Pair
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
argument_list|,
name|SparkEdgeProperty
argument_list|>
name|edgeProperties
init|=
operator|new
name|HashMap
argument_list|<
name|Pair
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
argument_list|,
name|SparkEdgeProperty
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|requiredCounterPrefix
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
name|cloneToWork
decl_stmt|;
specifier|public
name|SparkWork
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
name|this
operator|.
name|queryId
operator|=
name|queryId
expr_stmt|;
name|this
operator|.
name|dagName
operator|=
name|queryId
operator|+
literal|":"
operator|+
name|counter
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
name|cloneToWork
operator|=
operator|new
name|HashMap
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"DagName"
argument_list|)
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|dagName
return|;
block|}
specifier|public
name|String
name|getQueryId
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryId
return|;
block|}
comment|/**    * @return a map of "vertex name" to BaseWork    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Vertices"
argument_list|,
name|explainLevels
operator|=
block|{
name|Explain
operator|.
name|Level
operator|.
name|USER
block|,
name|Explain
operator|.
name|Level
operator|.
name|DEFAULT
block|,
name|Explain
operator|.
name|Level
operator|.
name|EXTENDED
block|}
argument_list|,
name|vectorization
operator|=
name|Vectorization
operator|.
name|SUMMARY_PATH
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|BaseWork
argument_list|>
name|getWorkMap
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|BaseWork
argument_list|>
name|result
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|getAllWork
argument_list|()
control|)
block|{
name|result
operator|.
name|put
argument_list|(
name|w
operator|.
name|getName
argument_list|()
argument_list|,
name|w
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * @return a topologically sorted list of BaseWork    */
specifier|public
name|List
argument_list|<
name|BaseWork
argument_list|>
name|getAllWork
parameter_list|()
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|result
init|=
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|seen
init|=
operator|new
name|HashSet
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|leaf
range|:
name|leaves
control|)
block|{
comment|// make sure all leaves are visited at least once
name|visit
argument_list|(
name|leaf
argument_list|,
name|seen
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|BaseWork
argument_list|>
name|getAllWorkUnsorted
parameter_list|()
block|{
return|return
name|workGraph
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|private
name|void
name|visit
parameter_list|(
name|BaseWork
name|child
parameter_list|,
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|seen
parameter_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
name|result
parameter_list|)
block|{
if|if
condition|(
name|seen
operator|.
name|contains
argument_list|(
name|child
argument_list|)
condition|)
block|{
comment|// don't visit multiple times
return|return;
block|}
name|seen
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|parent
range|:
name|getParents
argument_list|(
name|child
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|seen
operator|.
name|contains
argument_list|(
name|parent
argument_list|)
condition|)
block|{
name|visit
argument_list|(
name|parent
argument_list|,
name|seen
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
name|result
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add all nodes in the collection without any connections.    */
specifier|public
name|void
name|addAll
parameter_list|(
name|Collection
argument_list|<
name|BaseWork
argument_list|>
name|c
parameter_list|)
block|{
for|for
control|(
name|BaseWork
name|w
range|:
name|c
control|)
block|{
name|this
operator|.
name|add
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Add all nodes in the collection without any connections.    */
specifier|public
name|void
name|addAll
parameter_list|(
name|BaseWork
index|[]
name|bws
parameter_list|)
block|{
for|for
control|(
name|BaseWork
name|w
range|:
name|bws
control|)
block|{
name|this
operator|.
name|add
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Whether the specified BaseWork is a vertex in this graph    * @param w the BaseWork to check    * @return whether specified BaseWork is in this graph    */
specifier|public
name|boolean
name|contains
parameter_list|(
name|BaseWork
name|w
parameter_list|)
block|{
return|return
name|workGraph
operator|.
name|containsKey
argument_list|(
name|w
argument_list|)
return|;
block|}
comment|/**    * add creates a new node in the graph without any connections    */
specifier|public
name|void
name|add
parameter_list|(
name|BaseWork
name|w
parameter_list|)
block|{
if|if
condition|(
name|workGraph
operator|.
name|containsKey
argument_list|(
name|w
argument_list|)
condition|)
block|{
return|return;
block|}
name|workGraph
operator|.
name|put
argument_list|(
name|w
argument_list|,
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|invertedWorkGraph
operator|.
name|put
argument_list|(
name|w
argument_list|,
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|roots
operator|.
name|add
argument_list|(
name|w
argument_list|)
expr_stmt|;
name|leaves
operator|.
name|add
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
comment|/**    * disconnect removes an edge between a and b. Both a and    * b have to be in the graph. If there is no matching edge    * no change happens.    */
specifier|public
name|void
name|disconnect
parameter_list|(
name|BaseWork
name|a
parameter_list|,
name|BaseWork
name|b
parameter_list|)
block|{
name|workGraph
operator|.
name|get
argument_list|(
name|a
argument_list|)
operator|.
name|remove
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|b
argument_list|)
operator|.
name|remove
argument_list|(
name|a
argument_list|)
expr_stmt|;
if|if
condition|(
name|getParents
argument_list|(
name|b
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|roots
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getChildren
argument_list|(
name|a
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|leaves
operator|.
name|add
argument_list|(
name|a
argument_list|)
expr_stmt|;
block|}
name|edgeProperties
operator|.
name|remove
argument_list|(
operator|new
name|ImmutablePair
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * getRoots returns all nodes that do not have a parent.    */
specifier|public
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|getRoots
parameter_list|()
block|{
return|return
operator|new
name|LinkedHashSet
argument_list|<
name|BaseWork
argument_list|>
argument_list|(
name|roots
argument_list|)
return|;
block|}
comment|/**    * getLeaves returns all nodes that do not have a child    */
specifier|public
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|getLeaves
parameter_list|()
block|{
return|return
operator|new
name|LinkedHashSet
argument_list|<
name|BaseWork
argument_list|>
argument_list|(
name|leaves
argument_list|)
return|;
block|}
specifier|public
name|void
name|setRequiredCounterPrefix
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|requiredCounterPrefix
parameter_list|)
block|{
name|this
operator|.
name|requiredCounterPrefix
operator|=
name|requiredCounterPrefix
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getRequiredCounterPrefix
parameter_list|()
block|{
return|return
name|requiredCounterPrefix
return|;
block|}
comment|/**    * getParents returns all the nodes with edges leading into work    */
specifier|public
name|List
argument_list|<
name|BaseWork
argument_list|>
name|getParents
parameter_list|(
name|BaseWork
name|work
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|invertedWorkGraph
operator|.
name|containsKey
argument_list|(
name|work
argument_list|)
argument_list|,
literal|"AssertionError: expected invertedWorkGraph.containsKey(work) to be true"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|work
argument_list|)
operator|!=
literal|null
argument_list|,
literal|"AssertionError: expected invertedWorkGraph.get(work) to be not null"
argument_list|)
expr_stmt|;
return|return
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|(
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|work
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * getChildren returns all the nodes with edges leading out of work    */
specifier|public
name|List
argument_list|<
name|BaseWork
argument_list|>
name|getChildren
parameter_list|(
name|BaseWork
name|work
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|workGraph
operator|.
name|containsKey
argument_list|(
name|work
argument_list|)
argument_list|,
literal|"AssertionError: expected workGraph.containsKey(work) to be true"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|workGraph
operator|.
name|get
argument_list|(
name|work
argument_list|)
operator|!=
literal|null
argument_list|,
literal|"AssertionError: expected workGraph.get(work) to be not null"
argument_list|)
expr_stmt|;
return|return
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|(
name|workGraph
operator|.
name|get
argument_list|(
name|work
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * remove removes a node from the graph and removes all edges with    * work as start or end point. No change to the graph if the node    * doesn't exist.    */
specifier|public
name|void
name|remove
parameter_list|(
name|BaseWork
name|work
parameter_list|)
block|{
if|if
condition|(
operator|!
name|workGraph
operator|.
name|containsKey
argument_list|(
name|work
argument_list|)
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|BaseWork
argument_list|>
name|children
init|=
name|getChildren
argument_list|(
name|work
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|parents
init|=
name|getParents
argument_list|(
name|work
argument_list|)
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|children
control|)
block|{
name|edgeProperties
operator|.
name|remove
argument_list|(
operator|new
name|ImmutablePair
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
argument_list|(
name|work
argument_list|,
name|w
argument_list|)
argument_list|)
expr_stmt|;
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|w
argument_list|)
operator|.
name|remove
argument_list|(
name|work
argument_list|)
expr_stmt|;
if|if
condition|(
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|w
argument_list|)
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|roots
operator|.
name|add
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|BaseWork
name|w
range|:
name|parents
control|)
block|{
name|edgeProperties
operator|.
name|remove
argument_list|(
operator|new
name|ImmutablePair
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
argument_list|(
name|w
argument_list|,
name|work
argument_list|)
argument_list|)
expr_stmt|;
name|workGraph
operator|.
name|get
argument_list|(
name|w
argument_list|)
operator|.
name|remove
argument_list|(
name|work
argument_list|)
expr_stmt|;
if|if
condition|(
name|workGraph
operator|.
name|get
argument_list|(
name|w
argument_list|)
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|leaves
operator|.
name|add
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
name|roots
operator|.
name|remove
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|leaves
operator|.
name|remove
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|workGraph
operator|.
name|remove
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|invertedWorkGraph
operator|.
name|remove
argument_list|(
name|work
argument_list|)
expr_stmt|;
block|}
comment|/**    * returns the edge type connecting work a and b    */
specifier|public
name|SparkEdgeProperty
name|getEdgeProperty
parameter_list|(
name|BaseWork
name|a
parameter_list|,
name|BaseWork
name|b
parameter_list|)
block|{
return|return
name|edgeProperties
operator|.
name|get
argument_list|(
operator|new
name|ImmutablePair
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * connect adds an edge between a and b. Both nodes have    * to be added prior to calling connect.    */
specifier|public
name|void
name|connect
parameter_list|(
name|BaseWork
name|a
parameter_list|,
name|BaseWork
name|b
parameter_list|,
name|SparkEdgeProperty
name|edgeProp
parameter_list|)
block|{
name|workGraph
operator|.
name|get
argument_list|(
name|a
argument_list|)
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|b
argument_list|)
operator|.
name|add
argument_list|(
name|a
argument_list|)
expr_stmt|;
name|roots
operator|.
name|remove
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|leaves
operator|.
name|remove
argument_list|(
name|a
argument_list|)
expr_stmt|;
name|ImmutablePair
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
name|workPair
init|=
operator|new
name|ImmutablePair
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
decl_stmt|;
name|edgeProperties
operator|.
name|put
argument_list|(
name|workPair
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
block|}
comment|/*    * Dependency is a class used for explain    */
specifier|public
class|class
name|Dependency
implements|implements
name|Serializable
implements|,
name|Comparable
argument_list|<
name|Dependency
argument_list|>
block|{
specifier|public
name|BaseWork
name|w
decl_stmt|;
specifier|public
name|SparkEdgeProperty
name|prop
decl_stmt|;
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Name"
argument_list|)
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|w
operator|.
name|getName
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Shuffle Type"
argument_list|)
specifier|public
name|String
name|getShuffleType
parameter_list|()
block|{
return|return
name|prop
operator|.
name|getShuffleType
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Number of Partitions"
argument_list|)
specifier|public
name|String
name|getNumPartitions
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|toString
argument_list|(
name|prop
operator|.
name|getNumPartitions
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Dependency
name|o
parameter_list|)
block|{
name|int
name|compare
init|=
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|compare
operator|==
literal|0
condition|)
block|{
name|compare
operator|=
name|getShuffleType
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getShuffleType
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|compare
return|;
block|}
block|}
comment|/**    * Task name is usually sorted by natural order, which is the same    * as the topological order in most cases. However, with Spark, some    * tasks may be converted, so have new names. The natural order may    * be different from the topological order. This class is to make    * sure all tasks to be sorted by topological order deterministically.    */
specifier|private
specifier|static
class|class
name|ComparableName
implements|implements
name|Comparable
argument_list|<
name|ComparableName
argument_list|>
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dependencies
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
name|ComparableName
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dependencies
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|dependencies
operator|=
name|dependencies
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**      * Check if task n1 depends on task n2      */
name|boolean
name|dependsOn
parameter_list|(
name|String
name|n1
parameter_list|,
name|String
name|n2
parameter_list|)
block|{
for|for
control|(
name|String
name|p
init|=
name|dependencies
operator|.
name|get
argument_list|(
name|n1
argument_list|)
init|;
name|p
operator|!=
literal|null
condition|;
name|p
operator|=
name|dependencies
operator|.
name|get
argument_list|(
name|p
argument_list|)
control|)
block|{
if|if
condition|(
name|p
operator|.
name|equals
argument_list|(
name|n2
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**      * Get the number of parents of task n      */
name|int
name|getDepth
parameter_list|(
name|String
name|n
parameter_list|)
block|{
name|int
name|depth
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|p
init|=
name|dependencies
operator|.
name|get
argument_list|(
name|n
argument_list|)
init|;
name|p
operator|!=
literal|null
condition|;
name|p
operator|=
name|dependencies
operator|.
name|get
argument_list|(
name|p
argument_list|)
control|)
block|{
name|depth
operator|++
expr_stmt|;
block|}
return|return
name|depth
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|ComparableName
name|o
parameter_list|)
block|{
if|if
condition|(
name|dependsOn
argument_list|(
name|name
argument_list|,
name|o
operator|.
name|name
argument_list|)
condition|)
block|{
comment|// this depends on o
return|return
literal|1
return|;
block|}
if|if
condition|(
name|dependsOn
argument_list|(
name|o
operator|.
name|name
argument_list|,
name|name
argument_list|)
condition|)
block|{
comment|// o depends on this
return|return
operator|-
literal|1
return|;
block|}
comment|// No dependency, check depth
name|int
name|d1
init|=
name|getDepth
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|int
name|d2
init|=
name|getDepth
argument_list|(
name|o
operator|.
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|d1
operator|==
name|d2
condition|)
block|{
comment|// Same depth, using natural order
return|return
name|name
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|name
argument_list|)
return|;
block|}
comment|// Deep one is bigger, i.e. less to the top
return|return
name|d1
operator|>
name|d2
condition|?
literal|1
else|:
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
return|;
block|}
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Edges"
argument_list|,
name|explainLevels
operator|=
block|{
name|Explain
operator|.
name|Level
operator|.
name|USER
block|,
name|Explain
operator|.
name|Level
operator|.
name|DEFAULT
block|,
name|Explain
operator|.
name|Level
operator|.
name|EXTENDED
block|}
argument_list|,
name|vectorization
operator|=
name|Vectorization
operator|.
name|SUMMARY_PATH
argument_list|)
specifier|public
name|Map
argument_list|<
name|ComparableName
argument_list|,
name|List
argument_list|<
name|Dependency
argument_list|>
argument_list|>
name|getDependencyMap
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|allDependencies
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|ComparableName
argument_list|,
name|List
argument_list|<
name|Dependency
argument_list|>
argument_list|>
name|result
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|ComparableName
argument_list|,
name|List
argument_list|<
name|Dependency
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|baseWork
range|:
name|getAllWork
argument_list|()
control|)
block|{
if|if
condition|(
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|baseWork
argument_list|)
operator|!=
literal|null
operator|&&
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|baseWork
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|List
argument_list|<
name|Dependency
argument_list|>
name|dependencies
init|=
operator|new
name|LinkedList
argument_list|<
name|Dependency
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|d
range|:
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|baseWork
argument_list|)
control|)
block|{
name|allDependencies
operator|.
name|put
argument_list|(
name|baseWork
operator|.
name|getName
argument_list|()
argument_list|,
name|d
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Dependency
name|dependency
init|=
operator|new
name|Dependency
argument_list|()
decl_stmt|;
name|dependency
operator|.
name|w
operator|=
name|d
expr_stmt|;
name|dependency
operator|.
name|prop
operator|=
name|getEdgeProperty
argument_list|(
name|d
argument_list|,
name|baseWork
argument_list|)
expr_stmt|;
name|dependencies
operator|.
name|add
argument_list|(
name|dependency
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|dependencies
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|dependencies
argument_list|)
expr_stmt|;
name|result
operator|.
name|put
argument_list|(
operator|new
name|ComparableName
argument_list|(
name|allDependencies
argument_list|,
name|baseWork
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|dependencies
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * @return all reduce works of this spark work, in sorted order.    */
specifier|public
name|List
argument_list|<
name|ReduceWork
argument_list|>
name|getAllReduceWork
parameter_list|()
block|{
name|List
argument_list|<
name|ReduceWork
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|ReduceWork
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|work
range|:
name|getAllWork
argument_list|()
control|)
block|{
if|if
condition|(
name|work
operator|instanceof
name|ReduceWork
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|(
name|ReduceWork
operator|)
name|work
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
name|getCloneToWork
parameter_list|()
block|{
return|return
name|cloneToWork
return|;
block|}
specifier|public
name|void
name|setCloneToWork
parameter_list|(
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
name|cloneToWork
parameter_list|)
block|{
name|this
operator|.
name|cloneToWork
operator|=
name|cloneToWork
expr_stmt|;
block|}
block|}
end_class

end_unit

