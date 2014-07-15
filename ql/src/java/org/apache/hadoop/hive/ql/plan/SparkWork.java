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
name|Iterator
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

begin_comment
comment|/**  * This class encapsulates all the work objects that can be executed  * in a single Spark job. Currently it's basically a tree with MapWork at the  * roots and and ReduceWork (or UnionWork) at all other nodes.  */
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
argument_list|)
specifier|public
class|class
name|SparkWork
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
specifier|static
name|int
name|counter
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
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
name|HashSet
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
name|HashSet
argument_list|<
name|BaseWork
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
name|workGraph
init|=
operator|new
name|HashMap
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
name|HashMap
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
specifier|public
name|SparkWork
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
operator|+
literal|":"
operator|+
operator|(
operator|++
name|counter
operator|)
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
name|name
return|;
block|}
comment|/**    * getWorkMap returns a map of "vertex name" to BaseWork    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Vertices"
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
comment|/**    * getAllWork returns a topologically sorted list of BaseWork    */
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
comment|/**    * add all nodes in the collection without any connections    */
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
comment|/**    * add all nodes in the collection without any connections    */
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
name|HashSet
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
name|HashSet
argument_list|<
name|BaseWork
argument_list|>
argument_list|(
name|leaves
argument_list|)
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
assert|assert
name|invertedWorkGraph
operator|.
name|containsKey
argument_list|(
name|work
argument_list|)
operator|&&
name|invertedWorkGraph
operator|.
name|get
argument_list|(
name|work
argument_list|)
operator|!=
literal|null
assert|;
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
assert|assert
name|workGraph
operator|.
name|containsKey
argument_list|(
name|work
argument_list|)
operator|&&
name|workGraph
operator|.
name|get
argument_list|(
name|work
argument_list|)
operator|!=
literal|null
assert|;
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
comment|/**    * connect adds an edge between a and b. Both nodes have    * to be added prior to calling connect.    * @param      */
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Edges"
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
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
name|String
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
name|Map
operator|.
name|Entry
argument_list|<
name|BaseWork
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|entry
range|:
name|invertedWorkGraph
operator|.
name|entrySet
argument_list|()
control|)
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
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
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
name|entry
operator|.
name|getKey
argument_list|()
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
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|dependencies
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
name|MapWork
name|getMapWork
parameter_list|()
block|{
name|Iterator
argument_list|<
name|BaseWork
argument_list|>
name|it
init|=
name|roots
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
return|return
operator|(
name|MapWork
operator|)
name|roots
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|setMapWork
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
block|{
name|roots
operator|.
name|add
argument_list|(
name|mapWork
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setReduceWork
parameter_list|(
name|ReduceWork
name|redWork
parameter_list|)
block|{
name|leaves
operator|.
name|add
argument_list|(
name|redWork
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReduceWork
name|getReduceWork
parameter_list|()
block|{
name|Iterator
argument_list|<
name|BaseWork
argument_list|>
name|it
init|=
name|leaves
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
return|return
operator|(
name|ReduceWork
operator|)
name|leaves
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

