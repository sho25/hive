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
name|common
package|;
end_package

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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/** Java linked list iterator interface is convoluted, and moreover concurrent modifications  * of the same list by multiple iterators are impossible. Hence, this.  * Java also doesn't support multiple inheritance, so this cannot be done as "aspect"... */
end_comment

begin_class
specifier|public
class|class
name|DiskRangeList
extends|extends
name|DiskRange
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
name|DiskRangeList
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|DiskRangeList
name|prev
decl_stmt|,
name|next
decl_stmt|;
specifier|public
name|DiskRangeList
parameter_list|(
name|long
name|offset
parameter_list|,
name|long
name|end
parameter_list|)
block|{
name|super
argument_list|(
name|offset
argument_list|,
name|end
argument_list|)
expr_stmt|;
block|}
comment|/** Replaces this element with another in the list; returns the new element. */
specifier|public
name|DiskRangeList
name|replaceSelfWith
parameter_list|(
name|DiskRangeList
name|other
parameter_list|)
block|{
name|other
operator|.
name|prev
operator|=
name|this
operator|.
name|prev
expr_stmt|;
name|other
operator|.
name|next
operator|=
name|this
operator|.
name|next
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|prev
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|prev
operator|.
name|next
operator|=
name|other
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|next
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|next
operator|.
name|prev
operator|=
name|other
expr_stmt|;
block|}
name|this
operator|.
name|next
operator|=
name|this
operator|.
name|prev
operator|=
literal|null
expr_stmt|;
return|return
name|other
return|;
block|}
comment|/**    * Inserts an intersecting range before current in the list and adjusts offset accordingly.    * @returns the new element.    */
specifier|public
name|DiskRangeList
name|insertPartBefore
parameter_list|(
name|DiskRangeList
name|other
parameter_list|)
block|{
assert|assert
name|other
operator|.
name|end
operator|>=
name|this
operator|.
name|offset
assert|;
name|this
operator|.
name|offset
operator|=
name|other
operator|.
name|end
expr_stmt|;
name|other
operator|.
name|prev
operator|=
name|this
operator|.
name|prev
expr_stmt|;
name|other
operator|.
name|next
operator|=
name|this
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|prev
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|prev
operator|.
name|next
operator|=
name|other
expr_stmt|;
block|}
name|this
operator|.
name|prev
operator|=
name|other
expr_stmt|;
return|return
name|other
return|;
block|}
comment|/**    * Inserts an element after current in the list.    * @returns the new element.    * */
specifier|public
name|DiskRangeList
name|insertAfter
parameter_list|(
name|DiskRangeList
name|other
parameter_list|)
block|{
name|other
operator|.
name|next
operator|=
name|this
operator|.
name|next
expr_stmt|;
name|other
operator|.
name|prev
operator|=
name|this
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|next
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|next
operator|.
name|prev
operator|=
name|other
expr_stmt|;
block|}
name|this
operator|.
name|next
operator|=
name|other
expr_stmt|;
return|return
name|other
return|;
block|}
comment|/**    * Inserts an intersecting range after current in the list and adjusts offset accordingly.    * @returns the new element.    */
specifier|public
name|DiskRangeList
name|insertPartAfter
parameter_list|(
name|DiskRangeList
name|other
parameter_list|)
block|{
assert|assert
name|other
operator|.
name|offset
operator|<=
name|this
operator|.
name|end
assert|;
name|this
operator|.
name|end
operator|=
name|other
operator|.
name|offset
expr_stmt|;
return|return
name|insertAfter
argument_list|(
name|other
argument_list|)
return|;
block|}
comment|/** Removes an element after current from the list. */
specifier|public
name|void
name|removeAfter
parameter_list|()
block|{
name|DiskRangeList
name|other
init|=
name|this
operator|.
name|next
decl_stmt|;
name|this
operator|.
name|next
operator|=
name|other
operator|.
name|next
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|next
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|next
operator|.
name|prev
operator|=
name|this
expr_stmt|;
block|}
name|other
operator|.
name|next
operator|=
name|other
operator|.
name|prev
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Removes the current element from the list. */
specifier|public
name|void
name|removeSelf
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|prev
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|prev
operator|.
name|next
operator|=
name|this
operator|.
name|next
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|next
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|next
operator|.
name|prev
operator|=
name|this
operator|.
name|prev
expr_stmt|;
block|}
name|this
operator|.
name|next
operator|=
name|this
operator|.
name|prev
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Splits current element in the list, using DiskRange::slice */
specifier|public
specifier|final
name|DiskRangeList
name|split
parameter_list|(
name|long
name|cOffset
parameter_list|)
block|{
name|insertAfter
argument_list|(
operator|(
name|DiskRangeList
operator|)
name|this
operator|.
name|sliceAndShift
argument_list|(
name|cOffset
argument_list|,
name|end
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|replaceSelfWith
argument_list|(
operator|(
name|DiskRangeList
operator|)
name|this
operator|.
name|sliceAndShift
argument_list|(
name|offset
argument_list|,
name|cOffset
argument_list|,
literal|0
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|hasContiguousNext
parameter_list|()
block|{
return|return
name|next
operator|!=
literal|null
operator|&&
name|end
operator|==
name|next
operator|.
name|offset
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|int
name|listSize
parameter_list|()
block|{
name|int
name|result
init|=
literal|1
decl_stmt|;
name|DiskRangeList
name|current
init|=
name|this
operator|.
name|next
decl_stmt|;
while|while
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
operator|++
name|result
expr_stmt|;
name|current
operator|=
name|current
operator|.
name|next
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|long
name|getTotalLength
parameter_list|()
block|{
name|long
name|totalLength
init|=
name|getLength
argument_list|()
decl_stmt|;
name|DiskRangeList
name|current
init|=
name|next
decl_stmt|;
while|while
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
name|totalLength
operator|+=
name|current
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|current
operator|=
name|current
operator|.
name|next
expr_stmt|;
block|}
return|return
name|totalLength
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|DiskRangeList
index|[]
name|listToArray
parameter_list|()
block|{
name|DiskRangeList
index|[]
name|result
init|=
operator|new
name|DiskRangeList
index|[
name|listSize
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|DiskRangeList
name|current
init|=
name|this
operator|.
name|next
decl_stmt|;
while|while
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|current
expr_stmt|;
operator|++
name|i
expr_stmt|;
name|current
operator|=
name|current
operator|.
name|next
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
class|class
name|DiskRangeListCreateHelper
block|{
specifier|private
name|DiskRangeList
name|tail
init|=
literal|null
decl_stmt|,
name|head
decl_stmt|;
specifier|public
name|DiskRangeListCreateHelper
parameter_list|()
block|{     }
specifier|public
name|DiskRangeList
name|getTail
parameter_list|()
block|{
return|return
name|tail
return|;
block|}
specifier|public
name|void
name|addOrMerge
parameter_list|(
name|long
name|offset
parameter_list|,
name|long
name|end
parameter_list|,
name|boolean
name|doMerge
parameter_list|,
name|boolean
name|doLogNew
parameter_list|)
block|{
if|if
condition|(
name|doMerge
operator|&&
name|tail
operator|!=
literal|null
operator|&&
name|tail
operator|.
name|merge
argument_list|(
name|offset
argument_list|,
name|end
argument_list|)
condition|)
return|return;
if|if
condition|(
name|doLogNew
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating new range; last range (which can include some previous adds) was "
operator|+
name|tail
argument_list|)
expr_stmt|;
block|}
name|DiskRangeList
name|node
init|=
operator|new
name|DiskRangeList
argument_list|(
name|offset
argument_list|,
name|end
argument_list|)
decl_stmt|;
if|if
condition|(
name|tail
operator|==
literal|null
condition|)
block|{
name|head
operator|=
name|tail
operator|=
name|node
expr_stmt|;
block|}
else|else
block|{
name|tail
operator|=
name|tail
operator|.
name|insertAfter
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|DiskRangeList
name|get
parameter_list|()
block|{
return|return
name|head
return|;
block|}
specifier|public
name|DiskRangeList
name|extract
parameter_list|()
block|{
name|DiskRangeList
name|result
init|=
name|head
decl_stmt|;
name|head
operator|=
literal|null
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
comment|/**    * List in-place mutation helper - a bogus first element that is inserted before list head,    * and thus remains constant even if head is replaced with some new range via in-place list    * mutation. extract() can be used to obtain the modified list.    */
specifier|public
specifier|static
class|class
name|DiskRangeListMutateHelper
extends|extends
name|DiskRangeList
block|{
specifier|public
name|DiskRangeListMutateHelper
parameter_list|(
name|DiskRangeList
name|head
parameter_list|)
block|{
name|super
argument_list|(
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
assert|assert
name|head
operator|!=
literal|null
assert|;
assert|assert
name|head
operator|.
name|prev
operator|==
literal|null
assert|;
name|this
operator|.
name|next
operator|=
name|head
expr_stmt|;
name|head
operator|.
name|prev
operator|=
name|this
expr_stmt|;
block|}
specifier|public
name|DiskRangeList
name|get
parameter_list|()
block|{
return|return
name|next
return|;
block|}
specifier|public
name|DiskRangeList
name|extract
parameter_list|()
block|{
name|DiskRangeList
name|result
init|=
name|this
operator|.
name|next
decl_stmt|;
assert|assert
name|result
operator|!=
literal|null
assert|;
name|this
operator|.
name|next
operator|=
name|result
operator|.
name|prev
operator|=
literal|null
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

