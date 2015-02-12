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
name|io
operator|.
name|ObjectInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractList
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
name|ConcurrentModificationException
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
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|SerDe
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
name|serde2
operator|.
name|SerDeException
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
name|serde2
operator|.
name|io
operator|.
name|ShortWritable
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|io
operator|.
name|Writable
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|MapJoinEagerRowContainer
implements|implements
name|MapJoinRowContainer
implements|,
name|AbstractRowContainer
operator|.
name|RowIterator
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Object
index|[]
name|EMPTY_OBJECT_ARRAY
init|=
operator|new
name|Object
index|[
literal|0
index|]
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|list
decl_stmt|;
specifier|private
name|byte
name|aliasFilter
init|=
operator|(
name|byte
operator|)
literal|0xff
decl_stmt|;
specifier|private
name|int
name|index
init|=
literal|0
decl_stmt|;
specifier|public
name|MapJoinEagerRowContainer
parameter_list|()
block|{
name|index
operator|=
literal|0
expr_stmt|;
name|list
operator|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addRow
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|t
parameter_list|)
block|{
name|list
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
name|addRow
parameter_list|(
name|Object
index|[]
name|t
parameter_list|)
block|{
name|addRow
argument_list|(
name|toList
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|AbstractRowContainer
operator|.
name|RowIterator
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|rowIter
parameter_list|()
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|first
parameter_list|()
block|{
name|index
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|index
operator|<
name|list
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|list
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|next
parameter_list|()
block|{
name|index
operator|++
expr_stmt|;
if|if
condition|(
name|index
operator|<
name|list
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|list
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Get the number of elements in the RowContainer.    *    * @return number of elements in the RowContainer    */
annotation|@
name|Override
specifier|public
name|int
name|rowCount
parameter_list|()
block|{
return|return
name|list
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Remove all elements in the RowContainer.    */
annotation|@
name|Override
specifier|public
name|void
name|clearRows
parameter_list|()
block|{
name|list
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getAliasFilter
parameter_list|()
block|{
return|return
name|aliasFilter
return|;
block|}
annotation|@
name|Override
specifier|public
name|MapJoinRowContainer
name|copy
parameter_list|()
block|{
name|MapJoinEagerRowContainer
name|result
init|=
operator|new
name|MapJoinEagerRowContainer
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Object
argument_list|>
name|item
range|:
name|list
control|)
block|{
name|result
operator|.
name|addRow
argument_list|(
name|item
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|void
name|read
parameter_list|(
name|MapJoinObjectSerDeContext
name|context
parameter_list|,
name|ObjectInputStream
name|in
parameter_list|,
name|Writable
name|container
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
name|long
name|numRows
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|rowIndex
init|=
literal|0L
init|;
name|rowIndex
operator|<
name|numRows
condition|;
name|rowIndex
operator|++
control|)
block|{
name|container
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|read
argument_list|(
name|context
argument_list|,
name|container
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|read
parameter_list|(
name|MapJoinObjectSerDeContext
name|context
parameter_list|,
name|Writable
name|currentValue
parameter_list|)
throws|throws
name|SerDeException
block|{
name|SerDe
name|serde
init|=
name|context
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|value
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|serde
operator|.
name|deserialize
argument_list|(
name|currentValue
argument_list|)
argument_list|,
name|serde
operator|.
name|getObjectInspector
argument_list|()
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|addRow
argument_list|(
name|toList
argument_list|(
name|EMPTY_OBJECT_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Object
index|[]
name|valuesArray
init|=
name|value
operator|.
name|toArray
argument_list|()
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|hasFilterTag
argument_list|()
condition|)
block|{
name|aliasFilter
operator|&=
operator|(
operator|(
name|ShortWritable
operator|)
name|valuesArray
index|[
name|valuesArray
operator|.
name|length
operator|-
literal|1
index|]
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|addRow
argument_list|(
name|toList
argument_list|(
name|valuesArray
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|MapJoinObjectSerDeContext
name|context
parameter_list|,
name|ObjectOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
name|SerDe
name|serde
init|=
name|context
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueObjectInspector
init|=
name|context
operator|.
name|getStandardOI
argument_list|()
decl_stmt|;
name|long
name|numRows
init|=
name|rowCount
argument_list|()
decl_stmt|;
name|long
name|numRowsWritten
init|=
literal|0L
decl_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|numRows
argument_list|)
expr_stmt|;
for|for
control|(
name|List
argument_list|<
name|Object
argument_list|>
name|row
init|=
name|first
argument_list|()
init|;
name|row
operator|!=
literal|null
condition|;
name|row
operator|=
name|next
argument_list|()
control|)
block|{
name|serde
operator|.
name|serialize
argument_list|(
name|row
operator|.
name|toArray
argument_list|()
argument_list|,
name|valueObjectInspector
argument_list|)
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
operator|++
name|numRowsWritten
expr_stmt|;
block|}
if|if
condition|(
name|numRows
operator|!=
name|rowCount
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ConcurrentModificationException
argument_list|(
literal|"Values was modifified while persisting"
argument_list|)
throw|;
block|}
if|if
condition|(
name|numRowsWritten
operator|!=
name|numRows
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Expected to write "
operator|+
name|numRows
operator|+
literal|" but wrote "
operator|+
name|numRowsWritten
argument_list|)
throw|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|toList
parameter_list|(
name|Object
index|[]
name|array
parameter_list|)
block|{
return|return
operator|new
name|NoCopyingArrayList
argument_list|(
name|array
argument_list|)
return|;
block|}
comment|/**    * In this use case our objects will not be modified    * so we don't care about copying in and out.    */
specifier|private
specifier|static
class|class
name|NoCopyingArrayList
extends|extends
name|AbstractList
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
name|Object
index|[]
name|array
decl_stmt|;
specifier|public
name|NoCopyingArrayList
parameter_list|(
name|Object
index|[]
name|array
parameter_list|)
block|{
name|this
operator|.
name|array
operator|=
name|array
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|array
index|[
name|index
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|array
operator|.
name|length
return|;
block|}
specifier|public
name|Object
index|[]
name|toArray
parameter_list|()
block|{
return|return
name|array
return|;
block|}
block|}
block|}
end_class

end_unit

