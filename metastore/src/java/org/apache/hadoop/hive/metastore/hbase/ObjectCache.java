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
name|Map
import|;
end_import

begin_comment
comment|/**  * A generic class for caching objects obtained from HBase.  Currently a set of  * convenience methods around a {@link java.util.HashMap} with a max size but built  * as a separate class in case we want to switch out the implementation to something more  * efficient.  The cache has a max size; when this is exceeded any additional entries are dropped  * on the floor.  *  * This cache is local to a particular thread and thus is not synchronized.  It is intended to be  * flushed before a query begins to make sure it doesn't carry old versions of objects between  * queries (that is, an object may have changed between two queries, we want to get the newest  * version).  */
end_comment

begin_class
class|class
name|ObjectCache
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
specifier|private
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|cache
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxSize
decl_stmt|;
specifier|private
name|Counter
name|hits
decl_stmt|;
specifier|private
name|Counter
name|misses
decl_stmt|;
specifier|private
name|Counter
name|overflows
decl_stmt|;
comment|/**    *    * @param max maximum number of objects to store in the cache.  When max is reached, eviction    *            policy is MRU.    * @param hits counter to increment when we find an element in the cache    * @param misses counter to increment when we do not find an element in the cache    * @param overflows counter to increment when we do not have room for an element in the cache    */
name|ObjectCache
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
name|K
argument_list|,
name|V
argument_list|>
argument_list|()
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
name|void
name|put
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
if|if
condition|(
name|cache
operator|.
name|size
argument_list|()
operator|<
name|maxSize
condition|)
block|{
name|cache
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
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
name|V
name|get
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|V
name|val
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
name|val
operator|==
literal|null
condition|)
name|misses
operator|.
name|incr
argument_list|()
expr_stmt|;
else|else
name|hits
operator|.
name|incr
argument_list|()
expr_stmt|;
return|return
name|val
return|;
block|}
name|void
name|remove
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|cache
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
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
block|}
block|}
end_class

end_unit

