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
operator|.
name|util
operator|.
name|rowobjects
package|;
end_package

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
name|SortedMap
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|util
operator|.
name|rowobjects
operator|.
name|RowTestObjects
import|;
end_import

begin_class
specifier|public
class|class
name|RowTestObjectsMap
block|{
specifier|private
name|SortedMap
argument_list|<
name|RowTestObjects
argument_list|,
name|Object
argument_list|>
name|sortedMap
decl_stmt|;
specifier|public
name|RowTestObjectsMap
parameter_list|()
block|{
name|sortedMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|RowTestObjects
argument_list|,
name|Object
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Object
name|find
parameter_list|(
name|RowTestObjects
name|testRow
parameter_list|)
block|{
return|return
name|sortedMap
operator|.
name|get
argument_list|(
name|testRow
argument_list|)
return|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|RowTestObjects
name|testRow
parameter_list|,
name|Object
name|object
parameter_list|)
block|{
name|sortedMap
operator|.
name|put
argument_list|(
name|testRow
argument_list|,
name|object
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|RowTestObjectsMap
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
specifier|final
name|RowTestObjectsMap
name|other
init|=
operator|(
name|RowTestObjectsMap
operator|)
name|obj
decl_stmt|;
specifier|final
name|int
name|thisSize
init|=
name|this
operator|.
name|sortedMap
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|int
name|otherSize
init|=
name|other
operator|.
name|sortedMap
operator|.
name|size
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|RowTestObjects
argument_list|,
name|Object
argument_list|>
argument_list|>
name|thisIterator
init|=
name|this
operator|.
name|sortedMap
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|RowTestObjects
argument_list|,
name|Object
argument_list|>
argument_list|>
name|otherIterator
init|=
name|other
operator|.
name|sortedMap
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|thisSize
condition|;
name|i
operator|++
control|)
block|{
name|Entry
argument_list|<
name|RowTestObjects
argument_list|,
name|Object
argument_list|>
name|thisEntry
init|=
name|thisIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|Entry
argument_list|<
name|RowTestObjects
argument_list|,
name|Object
argument_list|>
name|otherEntry
init|=
name|otherIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|thisEntry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|otherEntry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Check object.
if|if
condition|(
operator|!
name|thisEntry
operator|.
name|getValue
argument_list|()
operator|.
name|equals
argument_list|(
name|otherEntry
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|thisSize
operator|!=
name|otherSize
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
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
name|sortedMap
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

