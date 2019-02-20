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
name|exec
package|;
end_package

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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_comment
comment|/**  * Cache for storing boundaries found within a partition - used for PTF functions.  * Stores key-value pairs where key is the row index in the partition from which a range begins,  * value is the corresponding row value (based on what the user specified in the orderby column).  */
end_comment

begin_class
specifier|public
class|class
name|BoundaryCache
extends|extends
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|Object
argument_list|>
block|{
specifier|private
name|boolean
name|isComplete
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxSize
decl_stmt|;
specifier|private
specifier|final
name|LinkedList
argument_list|<
name|Integer
argument_list|>
name|queue
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|BoundaryCache
parameter_list|(
name|int
name|maxSize
parameter_list|)
block|{
if|if
condition|(
name|maxSize
operator|<=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cache size of 1 and below it doesn't make sense."
argument_list|)
throw|;
block|}
name|this
operator|.
name|maxSize
operator|=
name|maxSize
expr_stmt|;
block|}
comment|/**    * True if the last range(s) of the partition are loaded into the cache.    * @return    */
specifier|public
name|boolean
name|isComplete
parameter_list|()
block|{
return|return
name|isComplete
return|;
block|}
specifier|public
name|void
name|setComplete
parameter_list|(
name|boolean
name|complete
parameter_list|)
block|{
name|isComplete
operator|=
name|complete
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|put
parameter_list|(
name|Integer
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|Object
name|result
init|=
name|super
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
decl_stmt|;
comment|//Every new element is added to FIFO too.
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|//If FIFO size reaches maxSize we evict the eldest entry.
if|if
condition|(
name|queue
operator|.
name|size
argument_list|()
operator|>
name|maxSize
condition|)
block|{
name|evictOne
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Puts new key-value pair in cache.    * @param key    * @param value    * @return false if queue was full and put failed. True otherwise.    */
specifier|public
name|Boolean
name|putIfNotFull
parameter_list|(
name|Integer
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|isFull
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Checks if cache is full.    * @return true if full, false otherwise.    */
specifier|public
name|Boolean
name|isFull
parameter_list|()
block|{
return|return
name|queue
operator|.
name|size
argument_list|()
operator|>=
name|maxSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|isComplete
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|queue
operator|.
name|clear
argument_list|()
expr_stmt|;
name|super
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns entry corresponding to highest row index.    * @return max entry.    */
specifier|public
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Object
argument_list|>
name|getMaxEntry
parameter_list|()
block|{
return|return
name|floorEntry
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
comment|/**    * Removes eldest entry from the boundary cache.    */
specifier|public
name|void
name|evictOne
parameter_list|()
block|{
if|if
condition|(
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|Integer
name|elementToDelete
init|=
name|queue
operator|.
name|poll
argument_list|()
decl_stmt|;
name|this
operator|.
name|remove
argument_list|(
name|elementToDelete
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|evictThisAndAllBefore
parameter_list|(
name|int
name|rowIdx
parameter_list|)
block|{
while|while
condition|(
name|queue
operator|.
name|peek
argument_list|()
operator|<=
name|rowIdx
condition|)
block|{
name|evictOne
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

