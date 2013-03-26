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
name|hcatalog
operator|.
name|data
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Iterator
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
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|DataBag
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|DefaultBagFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|DefaultDataBag
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|DefaultTuple
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|Tuple
import|;
end_import

begin_class
specifier|public
class|class
name|HCatArrayBag
parameter_list|<
name|T
parameter_list|>
implements|implements
name|DataBag
block|{
specifier|private
specifier|static
specifier|final
name|long
name|DUMMY_SIZE
init|=
literal|40
decl_stmt|;
name|List
argument_list|<
name|T
argument_list|>
name|rawItemList
init|=
literal|null
decl_stmt|;
name|DataBag
name|convertedBag
init|=
literal|null
decl_stmt|;
comment|//  List<Tuple> tupleList = null;
specifier|public
class|class
name|HowlArrayBagIterator
implements|implements
name|Iterator
argument_list|<
name|Tuple
argument_list|>
block|{
name|Iterator
argument_list|<
name|T
argument_list|>
name|iter
init|=
literal|null
decl_stmt|;
specifier|public
name|HowlArrayBagIterator
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|rawItemList
parameter_list|)
block|{
name|iter
operator|=
name|rawItemList
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|iter
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Tuple
name|next
parameter_list|()
block|{
name|Tuple
name|t
init|=
operator|new
name|DefaultTuple
argument_list|()
decl_stmt|;
name|t
operator|.
name|append
argument_list|(
name|iter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
name|iter
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|HCatArrayBag
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|list
parameter_list|)
block|{
name|rawItemList
operator|=
name|list
expr_stmt|;
block|}
specifier|private
name|void
name|convertFromRawToTupleForm
parameter_list|()
block|{
if|if
condition|(
name|convertedBag
operator|==
literal|null
condition|)
block|{
name|List
argument_list|<
name|Tuple
argument_list|>
name|ltuples
init|=
operator|new
name|ArrayList
argument_list|<
name|Tuple
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|T
name|item
range|:
name|rawItemList
control|)
block|{
name|Tuple
name|t
init|=
operator|new
name|DefaultTuple
argument_list|()
decl_stmt|;
name|t
operator|.
name|append
argument_list|(
name|item
argument_list|)
expr_stmt|;
name|ltuples
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
name|convertedBag
operator|=
name|DefaultBagFactory
operator|.
name|getInstance
argument_list|()
operator|.
name|newDefaultBag
argument_list|(
name|ltuples
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO : throw exception or be silent? Currently going with silence, but needs revisiting.
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|Tuple
name|t
parameter_list|)
block|{
if|if
condition|(
name|convertedBag
operator|==
literal|null
condition|)
block|{
name|convertFromRawToTupleForm
argument_list|()
expr_stmt|;
block|}
name|convertedBag
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addAll
parameter_list|(
name|DataBag
name|db
parameter_list|)
block|{
name|Tuple
name|t
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|dbi
init|=
name|db
operator|.
name|iterator
argument_list|()
init|;
name|dbi
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|this
operator|.
name|add
argument_list|(
name|dbi
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|rawItemList
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|convertedBag
operator|!=
literal|null
condition|)
block|{
name|convertedBag
operator|.
name|clear
argument_list|()
expr_stmt|;
name|convertedBag
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDistinct
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSorted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|iterator
parameter_list|()
block|{
if|if
condition|(
name|convertedBag
operator|!=
literal|null
condition|)
block|{
return|return
name|convertedBag
operator|.
name|iterator
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|HowlArrayBagIterator
argument_list|(
name|rawItemList
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|markStale
parameter_list|(
name|boolean
name|arg0
parameter_list|)
block|{
comment|// TODO Auto-generated method stub
block|}
annotation|@
name|Override
specifier|public
name|long
name|size
parameter_list|()
block|{
return|return
operator|(
name|convertedBag
operator|==
literal|null
condition|?
operator|(
name|rawItemList
operator|==
literal|null
condition|?
literal|0
else|:
name|rawItemList
operator|.
name|size
argument_list|()
operator|)
else|:
name|convertedBag
operator|.
name|size
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemorySize
parameter_list|()
block|{
comment|// FIXME: put in actual impl
if|if
condition|(
name|convertedBag
operator|!=
literal|null
condition|)
block|{
return|return
name|convertedBag
operator|.
name|getMemorySize
argument_list|()
operator|+
name|DUMMY_SIZE
return|;
block|}
else|else
block|{
return|return
name|DUMMY_SIZE
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|spill
parameter_list|()
block|{
comment|// FIXME: put in actual spill impl even for the list case
if|if
condition|(
name|convertedBag
operator|!=
literal|null
condition|)
block|{
return|return
name|convertedBag
operator|.
name|spill
argument_list|()
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
name|convertedBag
operator|=
operator|new
name|DefaultDataBag
argument_list|()
expr_stmt|;
name|convertedBag
operator|.
name|readFields
argument_list|(
name|arg0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
name|convertFromRawToTupleForm
argument_list|()
expr_stmt|;
name|convertedBag
operator|.
name|write
argument_list|(
name|arg0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|arg0
parameter_list|)
block|{
comment|// TODO Auto-generated method stub - really need to put in a better implementation here, also, equality case not considered yet
return|return
name|arg0
operator|.
name|hashCode
argument_list|()
operator|<
name|this
operator|.
name|hashCode
argument_list|()
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
block|}
end_class

end_unit

