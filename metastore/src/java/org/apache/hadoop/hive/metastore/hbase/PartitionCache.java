begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|metastore
operator|.
name|hbase
package|;
end_package

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
name|common
operator|.
name|ObjectPair
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
name|Partition
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
name|HashMap
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

begin_comment
comment|/**  * A cache for partition objects.  This is separate from  * {@link org.apache.hadoop.hive.metastore.hbase.ObjectCache} because we need to access it  * differently (always by table) and because we need to be able to track whether we are caching  * all of the partitions for a table or not.  Like ObjectCache it is local to a particular thread  * and thus not synchronized.  Also like ObjectCache it is intended to be flushed before each query.  */
end_comment

begin_class
class|class
name|PartitionCache
block|{
comment|// This is a trie.  The key to the first map is (dbname, tablename), since partitions are
comment|// always accessed within the context of the table they belong to.  The second map maps
comment|// partition values (not names) to partitions.
specifier|private
name|Map
argument_list|<
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|,
name|TrieValue
argument_list|>
name|cache
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxSize
decl_stmt|;
specifier|private
name|int
name|cacheSize
decl_stmt|;
specifier|private
name|Counter
name|misses
decl_stmt|;
specifier|private
name|Counter
name|hits
decl_stmt|;
specifier|private
name|Counter
name|overflows
decl_stmt|;
comment|/**    *    * @param max maximum number of objects to store in the cache.  When max is reached, eviction    *            policy is MRU.    * @param hits counter to increment when we find an element in the cache    * @param misses counter to increment when we do not find an element in the cache    * @param overflows counter to increment when we do not have room for an element in the cache    */
name|PartitionCache
parameter_list|(
name|int
name|max
parameter_list|,
name|Counter
name|hits
parameter_list|,
name|Counter
name|misses
parameter_list|,
name|Counter
name|overflows
parameter_list|)
block|{
name|maxSize
operator|=
name|max
expr_stmt|;
name|cache
operator|=
operator|new
name|HashMap
argument_list|<
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|,
name|TrieValue
argument_list|>
argument_list|()
expr_stmt|;
name|cacheSize
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|hits
operator|=
name|hits
expr_stmt|;
name|this
operator|.
name|misses
operator|=
name|misses
expr_stmt|;
name|this
operator|.
name|overflows
operator|=
name|overflows
expr_stmt|;
block|}
comment|/**    * Put a single partition into the cache    * @param dbName    * @param tableName    * @param part    */
name|void
name|put
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Partition
name|part
parameter_list|)
block|{
if|if
condition|(
name|cacheSize
operator|<
name|maxSize
condition|)
block|{
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|key
init|=
operator|new
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|TrieValue
name|entry
init|=
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
name|entry
operator|=
operator|new
name|TrieValue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
name|entry
operator|.
name|map
operator|.
name|put
argument_list|(
name|part
operator|.
name|getValues
argument_list|()
argument_list|,
name|part
argument_list|)
expr_stmt|;
name|cacheSize
operator|++
expr_stmt|;
block|}
else|else
block|{
name|overflows
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    *    * @param dbName    * @param tableName    * @param parts    * @param allForTable if true indicates that all partitions for this table are present    */
name|void
name|put
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
parameter_list|,
name|boolean
name|allForTable
parameter_list|)
block|{
if|if
condition|(
name|cacheSize
operator|+
name|parts
operator|.
name|size
argument_list|()
operator|<
name|maxSize
condition|)
block|{
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|key
init|=
operator|new
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|TrieValue
name|entry
init|=
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
name|entry
operator|=
operator|new
name|TrieValue
argument_list|(
name|allForTable
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Partition
name|part
range|:
name|parts
control|)
name|entry
operator|.
name|map
operator|.
name|put
argument_list|(
name|part
operator|.
name|getValues
argument_list|()
argument_list|,
name|part
argument_list|)
expr_stmt|;
name|cacheSize
operator|+=
name|parts
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|overflows
operator|.
name|incr
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Will only return a value if all partitions for this table are in the cache.  Otherwise you    * should call {@link #get} individually    * @param dbName    * @param tableName    * @return    */
name|Collection
argument_list|<
name|Partition
argument_list|>
name|getAllForTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|TrieValue
name|entry
init|=
name|cache
operator|.
name|get
argument_list|(
operator|new
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|!=
literal|null
operator|&&
name|entry
operator|.
name|hasAllPartitionsForTable
condition|)
block|{
name|hits
operator|.
name|incr
argument_list|()
expr_stmt|;
return|return
name|entry
operator|.
name|map
operator|.
name|values
argument_list|()
return|;
block|}
else|else
block|{
name|misses
operator|.
name|incr
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
name|Partition
name|get
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|)
block|{
name|TrieValue
name|entry
init|=
name|cache
operator|.
name|get
argument_list|(
operator|new
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|!=
literal|null
condition|)
block|{
name|hits
operator|.
name|incr
argument_list|()
expr_stmt|;
return|return
name|entry
operator|.
name|map
operator|.
name|get
argument_list|(
name|partVals
argument_list|)
return|;
block|}
else|else
block|{
name|misses
operator|.
name|incr
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
name|void
name|remove
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|key
init|=
operator|new
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|TrieValue
name|entry
init|=
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|!=
literal|null
condition|)
block|{
name|cacheSize
operator|-=
name|entry
operator|.
name|map
operator|.
name|size
argument_list|()
expr_stmt|;
name|cache
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|remove
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|)
block|{
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|key
init|=
operator|new
name|ObjectPair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|TrieValue
name|entry
init|=
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|!=
literal|null
operator|&&
name|entry
operator|.
name|map
operator|.
name|remove
argument_list|(
name|partVals
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|cacheSize
operator|--
expr_stmt|;
name|entry
operator|.
name|hasAllPartitionsForTable
operator|=
literal|false
expr_stmt|;
block|}
block|}
name|void
name|flush
parameter_list|()
block|{
name|cache
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cacheSize
operator|=
literal|0
expr_stmt|;
block|}
specifier|static
class|class
name|TrieValue
block|{
name|boolean
name|hasAllPartitionsForTable
decl_stmt|;
name|Map
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|,
name|Partition
argument_list|>
name|map
decl_stmt|;
name|TrieValue
parameter_list|(
name|boolean
name|hasAll
parameter_list|)
block|{
name|hasAllPartitionsForTable
operator|=
name|hasAll
expr_stmt|;
name|map
operator|=
operator|new
name|HashMap
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|,
name|Partition
argument_list|>
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

