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
operator|.
name|persistence
package|;
end_package

begin_comment
comment|/**  * Doubly circular linked list item.  */
end_comment

begin_class
specifier|public
class|class
name|DCLLItem
block|{
name|DCLLItem
name|prev
decl_stmt|;
name|DCLLItem
name|next
decl_stmt|;
name|DCLLItem
parameter_list|()
block|{
name|prev
operator|=
name|next
operator|=
name|this
expr_stmt|;
block|}
comment|/**    * Get the next item.    *     * @return the next item.    */
specifier|public
name|DCLLItem
name|getNext
parameter_list|()
block|{
return|return
name|next
return|;
block|}
comment|/**    * Get the previous item.    *     * @return the previous item.    */
specifier|public
name|DCLLItem
name|getPrev
parameter_list|()
block|{
return|return
name|prev
return|;
block|}
comment|/**    * Set the next item as itm.    *     * @param itm    *          the item to be set as next.    */
specifier|public
name|void
name|setNext
parameter_list|(
name|DCLLItem
name|itm
parameter_list|)
block|{
name|next
operator|=
name|itm
expr_stmt|;
block|}
comment|/**    * Set the previous item as itm    *     * @param itm    *          the item to be set as previous.    */
specifier|public
name|void
name|setPrev
parameter_list|(
name|DCLLItem
name|itm
parameter_list|)
block|{
name|prev
operator|=
name|itm
expr_stmt|;
block|}
comment|/**    * Remove the current item from the doubly circular linked list.    */
specifier|public
name|void
name|remove
parameter_list|()
block|{
name|next
operator|.
name|prev
operator|=
name|prev
expr_stmt|;
name|prev
operator|.
name|next
operator|=
name|next
expr_stmt|;
name|prev
operator|=
name|next
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Add v as the previous of the current list item.    *     * @param v    *          inserted item.    */
specifier|public
name|void
name|insertBefore
parameter_list|(
name|DCLLItem
name|v
parameter_list|)
block|{
name|prev
operator|.
name|next
operator|=
name|v
expr_stmt|;
name|v
operator|.
name|prev
operator|=
name|prev
expr_stmt|;
name|v
operator|.
name|next
operator|=
name|this
expr_stmt|;
name|prev
operator|=
name|v
expr_stmt|;
block|}
comment|/**    * Add v as the previous of the current list item.    *     * @param v    *          inserted item.    */
specifier|public
name|void
name|insertAfter
parameter_list|(
name|DCLLItem
name|v
parameter_list|)
block|{
name|next
operator|.
name|prev
operator|=
name|v
expr_stmt|;
name|v
operator|.
name|next
operator|=
name|next
expr_stmt|;
name|v
operator|.
name|prev
operator|=
name|this
expr_stmt|;
name|next
operator|=
name|v
expr_stmt|;
block|}
block|}
end_class

end_unit

