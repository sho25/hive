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
name|ByteArrayOutputStream
import|;
end_import

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
name|Utilities
import|;
end_import

begin_comment
comment|/**  * MapredWork.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Map Reduce"
argument_list|)
specifier|public
class|class
name|MapredWork
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
specifier|private
name|String
name|command
decl_stmt|;
comment|// map side work
comment|// use LinkedHashMap to make sure the iteration order is
comment|// deterministic, to ease testing
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|aliasToWork
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|aliasToPartnInfo
decl_stmt|;
comment|// map<->reduce interface
comment|// schema of the map-reduce 'key' object - this is homogeneous
specifier|private
name|TableDesc
name|keyDesc
decl_stmt|;
comment|// schema of the map-reduce 'val' object - this is heterogeneous
specifier|private
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tagToValueDesc
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
decl_stmt|;
specifier|private
name|Integer
name|numReduceTasks
decl_stmt|;
specifier|private
name|Integer
name|numMapTasks
decl_stmt|;
specifier|private
name|Integer
name|minSplitSize
decl_stmt|;
specifier|private
name|boolean
name|needsTagging
decl_stmt|;
specifier|private
name|MapredLocalWork
name|mapLocalWork
decl_stmt|;
specifier|private
name|String
name|inputformat
decl_stmt|;
specifier|public
name|MapredWork
parameter_list|()
block|{
name|aliasToPartnInfo
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|MapredWork
parameter_list|(
specifier|final
name|String
name|command
parameter_list|,
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
parameter_list|,
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|,
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|aliasToWork
parameter_list|,
specifier|final
name|TableDesc
name|keyDesc
parameter_list|,
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tagToValueDesc
parameter_list|,
specifier|final
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
parameter_list|,
specifier|final
name|Integer
name|numReduceTasks
parameter_list|,
specifier|final
name|MapredLocalWork
name|mapLocalWork
parameter_list|)
block|{
name|this
operator|.
name|command
operator|=
name|command
expr_stmt|;
name|this
operator|.
name|pathToAliases
operator|=
name|pathToAliases
expr_stmt|;
name|this
operator|.
name|pathToPartitionInfo
operator|=
name|pathToPartitionInfo
expr_stmt|;
name|this
operator|.
name|aliasToWork
operator|=
name|aliasToWork
expr_stmt|;
name|this
operator|.
name|keyDesc
operator|=
name|keyDesc
expr_stmt|;
name|this
operator|.
name|tagToValueDesc
operator|=
name|tagToValueDesc
expr_stmt|;
name|this
operator|.
name|reducer
operator|=
name|reducer
expr_stmt|;
name|this
operator|.
name|numReduceTasks
operator|=
name|numReduceTasks
expr_stmt|;
name|this
operator|.
name|mapLocalWork
operator|=
name|mapLocalWork
expr_stmt|;
name|aliasToPartnInfo
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|getCommand
parameter_list|()
block|{
return|return
name|command
return|;
block|}
specifier|public
name|void
name|setCommand
parameter_list|(
specifier|final
name|String
name|command
parameter_list|)
block|{
name|this
operator|.
name|command
operator|=
name|command
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Path -> Alias"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|getPathToAliases
parameter_list|()
block|{
return|return
name|pathToAliases
return|;
block|}
specifier|public
name|void
name|setPathToAliases
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
parameter_list|)
block|{
name|this
operator|.
name|pathToAliases
operator|=
name|pathToAliases
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Path -> Partition"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|getPathToPartitionInfo
parameter_list|()
block|{
return|return
name|pathToPartitionInfo
return|;
block|}
specifier|public
name|void
name|setPathToPartitionInfo
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|)
block|{
name|this
operator|.
name|pathToPartitionInfo
operator|=
name|pathToPartitionInfo
expr_stmt|;
block|}
comment|/**    * @return the aliasToPartnInfo    */
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|getAliasToPartnInfo
parameter_list|()
block|{
return|return
name|aliasToPartnInfo
return|;
block|}
comment|/**    * @param aliasToPartnInfo    *          the aliasToPartnInfo to set    */
specifier|public
name|void
name|setAliasToPartnInfo
parameter_list|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|aliasToPartnInfo
parameter_list|)
block|{
name|this
operator|.
name|aliasToPartnInfo
operator|=
name|aliasToPartnInfo
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alias -> Map Operator Tree"
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getAliasToWork
parameter_list|()
block|{
return|return
name|aliasToWork
return|;
block|}
specifier|public
name|void
name|setAliasToWork
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|aliasToWork
parameter_list|)
block|{
name|this
operator|.
name|aliasToWork
operator|=
name|aliasToWork
expr_stmt|;
block|}
comment|/**    * @return the mapredLocalWork    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Local Work"
argument_list|)
specifier|public
name|MapredLocalWork
name|getMapLocalWork
parameter_list|()
block|{
return|return
name|mapLocalWork
return|;
block|}
comment|/**    * @param mapLocalWork    *          the mapredLocalWork to set    */
specifier|public
name|void
name|setMapLocalWork
parameter_list|(
specifier|final
name|MapredLocalWork
name|mapLocalWork
parameter_list|)
block|{
name|this
operator|.
name|mapLocalWork
operator|=
name|mapLocalWork
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getKeyDesc
parameter_list|()
block|{
return|return
name|keyDesc
return|;
block|}
specifier|public
name|void
name|setKeyDesc
parameter_list|(
specifier|final
name|TableDesc
name|keyDesc
parameter_list|)
block|{
name|this
operator|.
name|keyDesc
operator|=
name|keyDesc
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|TableDesc
argument_list|>
name|getTagToValueDesc
parameter_list|()
block|{
return|return
name|tagToValueDesc
return|;
block|}
specifier|public
name|void
name|setTagToValueDesc
parameter_list|(
specifier|final
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tagToValueDesc
parameter_list|)
block|{
name|this
operator|.
name|tagToValueDesc
operator|=
name|tagToValueDesc
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Reduce Operator Tree"
argument_list|)
specifier|public
name|Operator
argument_list|<
name|?
argument_list|>
name|getReducer
parameter_list|()
block|{
return|return
name|reducer
return|;
block|}
specifier|public
name|void
name|setReducer
parameter_list|(
specifier|final
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
parameter_list|)
block|{
name|this
operator|.
name|reducer
operator|=
name|reducer
expr_stmt|;
block|}
specifier|public
name|Integer
name|getNumMapTasks
parameter_list|()
block|{
return|return
name|numMapTasks
return|;
block|}
specifier|public
name|void
name|setNumMapTasks
parameter_list|(
name|Integer
name|numMapTasks
parameter_list|)
block|{
name|this
operator|.
name|numMapTasks
operator|=
name|numMapTasks
expr_stmt|;
block|}
comment|/**    * If the number of reducers is -1, the runtime will automatically figure it    * out by input data size.    *     * The number of reducers will be a positive number only in case the target    * table is bucketed into N buckets (through CREATE TABLE). This feature is    * not supported yet, so the number of reducers will always be -1 for now.    */
specifier|public
name|Integer
name|getNumReduceTasks
parameter_list|()
block|{
return|return
name|numReduceTasks
return|;
block|}
specifier|public
name|void
name|setNumReduceTasks
parameter_list|(
specifier|final
name|Integer
name|numReduceTasks
parameter_list|)
block|{
name|this
operator|.
name|numReduceTasks
operator|=
name|numReduceTasks
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|void
name|addMapWork
parameter_list|(
name|String
name|path
parameter_list|,
name|String
name|alias
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|work
parameter_list|,
name|PartitionDesc
name|pd
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|curAliases
init|=
name|pathToAliases
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|curAliases
operator|==
literal|null
condition|)
block|{
assert|assert
operator|(
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|==
literal|null
operator|)
assert|;
name|curAliases
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|pathToAliases
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|curAliases
argument_list|)
expr_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|pd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
operator|(
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|!=
literal|null
operator|)
assert|;
block|}
for|for
control|(
name|String
name|oneAlias
range|:
name|curAliases
control|)
block|{
if|if
condition|(
name|oneAlias
operator|.
name|equals
argument_list|(
name|alias
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Multiple aliases named: "
operator|+
name|alias
operator|+
literal|" for path: "
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
name|curAliases
operator|.
name|add
argument_list|(
name|alias
argument_list|)
expr_stmt|;
if|if
condition|(
name|aliasToWork
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Existing work for alias: "
operator|+
name|alias
argument_list|)
throw|;
block|}
name|aliasToWork
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|work
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|String
name|isInvalid
parameter_list|()
block|{
if|if
condition|(
operator|(
name|getNumReduceTasks
argument_list|()
operator|>=
literal|1
operator|)
operator|&&
operator|(
name|getReducer
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
return|return
literal|"Reducers> 0 but no reduce operator"
return|;
block|}
if|if
condition|(
operator|(
name|getNumReduceTasks
argument_list|()
operator|==
literal|0
operator|)
operator|&&
operator|(
name|getReducer
argument_list|()
operator|!=
literal|null
operator|)
condition|)
block|{
return|return
literal|"Reducers == 0 but reduce operator specified"
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|toXML
parameter_list|()
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|Utilities
operator|.
name|serializeMapRedWork
argument_list|(
name|this
argument_list|,
name|baos
argument_list|)
expr_stmt|;
return|return
operator|(
name|baos
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
comment|// non bean
comment|/**    * For each map side operator - stores the alias the operator is working on    * behalf of in the operator runtime state. This is used by reducesink    * operator - but could be useful for debugging as well.    */
specifier|private
name|void
name|setAliases
parameter_list|()
block|{
for|for
control|(
name|String
name|oneAlias
range|:
name|aliasToWork
operator|.
name|keySet
argument_list|()
control|)
block|{
name|aliasToWork
operator|.
name|get
argument_list|(
name|oneAlias
argument_list|)
operator|.
name|setAlias
argument_list|(
name|oneAlias
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Derive additional attributes to be rendered by EXPLAIN.    */
specifier|public
name|void
name|deriveExplainAttributes
parameter_list|()
block|{
if|if
condition|(
name|pathToPartitionInfo
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|entry
range|:
name|pathToPartitionInfo
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|deriveBaseFileName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|initialize
parameter_list|()
block|{
name|setAliases
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Needs Tagging"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|boolean
name|getNeedsTagging
parameter_list|()
block|{
return|return
name|needsTagging
return|;
block|}
specifier|public
name|void
name|setNeedsTagging
parameter_list|(
name|boolean
name|needsTagging
parameter_list|)
block|{
name|this
operator|.
name|needsTagging
operator|=
name|needsTagging
expr_stmt|;
block|}
specifier|public
name|Integer
name|getMinSplitSize
parameter_list|()
block|{
return|return
name|minSplitSize
return|;
block|}
specifier|public
name|void
name|setMinSplitSize
parameter_list|(
name|Integer
name|minSplitSize
parameter_list|)
block|{
name|this
operator|.
name|minSplitSize
operator|=
name|minSplitSize
expr_stmt|;
block|}
specifier|public
name|String
name|getInputformat
parameter_list|()
block|{
return|return
name|inputformat
return|;
block|}
specifier|public
name|void
name|setInputformat
parameter_list|(
name|String
name|inputformat
parameter_list|)
block|{
name|this
operator|.
name|inputformat
operator|=
name|inputformat
expr_stmt|;
block|}
block|}
end_class

end_unit

