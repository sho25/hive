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
name|ObjectOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractCollection
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
name|Collection
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
name|java
operator|.
name|util
operator|.
name|ListIterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|metadata
operator|.
name|HiveException
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
name|AbstractSerDe
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
name|BytesWritable
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
name|FlatRowContainer
extends|extends
name|AbstractCollection
argument_list|<
name|Object
argument_list|>
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
implements|,
name|List
argument_list|<
name|Object
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
specifier|static
specifier|final
name|int
name|UNKNOWN
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FlatRowContainer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * In lazy mode, 0s element contains context for deserialization and all the other    * elements contains byte arrays to be deserialized. After deserialization, the array    * contains row count * row size elements - a matrix of rows stored.    */
specifier|private
name|Object
index|[]
name|array
decl_stmt|;
comment|/**    * This is kind of tricky. UNKNOWN number means unknown. Other positive numbers represent    * row length (see array javadoc). Non-positive numbers mean row length is zero (thus,    * array is empty); they represent (negated) number of rows (for joins w/o projections).    */
specifier|private
name|int
name|rowLength
init|=
name|UNKNOWN
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
name|boolean
name|isAliasFilterSet
init|=
literal|true
decl_stmt|;
comment|// by default assume no filter tag so we are good
specifier|public
name|FlatRowContainer
parameter_list|()
block|{
name|this
operator|.
name|array
operator|=
name|EMPTY_OBJECT_ARRAY
expr_stmt|;
block|}
comment|/** Called when loading the hashtable. */
specifier|public
name|void
name|add
parameter_list|(
name|MapJoinObjectSerDeContext
name|context
parameter_list|,
name|BytesWritable
name|value
parameter_list|)
throws|throws
name|HiveException
block|{
name|AbstractSerDe
name|serde
init|=
name|context
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|isAliasFilterSet
operator|=
operator|!
name|context
operator|.
name|hasFilterTag
argument_list|()
expr_stmt|;
comment|// has tag => need to set later
if|if
condition|(
name|rowLength
operator|==
name|UNKNOWN
condition|)
block|{
try|try
block|{
name|rowLength
operator|=
name|ObjectInspectorUtils
operator|.
name|getStructSize
argument_list|(
name|serde
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Get structure size error"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
if|if
condition|(
name|rowLength
operator|==
literal|0
condition|)
block|{
name|array
operator|=
name|EMPTY_OBJECT_ARRAY
expr_stmt|;
block|}
block|}
if|if
condition|(
name|rowLength
operator|>
literal|0
condition|)
block|{
name|int
name|rowCount
init|=
operator|(
name|array
operator|.
name|length
operator|/
name|rowLength
operator|)
decl_stmt|;
name|listRealloc
argument_list|(
name|array
operator|.
name|length
operator|+
name|rowLength
argument_list|)
expr_stmt|;
name|read
argument_list|(
name|serde
argument_list|,
name|value
argument_list|,
name|rowCount
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|--
name|rowLength
expr_stmt|;
comment|// see rowLength javadoc
block|}
block|}
comment|// Implementation of AbstractRowContainer and assorted methods
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
throws|throws
name|HiveException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Add is called with "
operator|+
name|t
operator|.
name|size
argument_list|()
operator|+
literal|" objects"
argument_list|)
expr_stmt|;
comment|// This is not called when building HashTable; we don't expect it to be called ever.
name|int
name|offset
init|=
name|prepareForAdd
argument_list|(
name|t
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|<
literal|0
condition|)
return|return;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|t
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|this
operator|.
name|array
index|[
name|offset
operator|+
name|i
index|]
operator|=
name|t
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|addRow
parameter_list|(
name|Object
index|[]
name|value
parameter_list|)
throws|throws
name|HiveException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Add is called with "
operator|+
name|value
operator|.
name|length
operator|+
literal|" objects"
argument_list|)
expr_stmt|;
comment|// This is not called when building HashTable; we don't expect it to be called ever.
name|int
name|offset
init|=
name|prepareForAdd
argument_list|(
name|value
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|<
literal|0
condition|)
return|return;
name|System
operator|.
name|arraycopy
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|array
argument_list|,
name|offset
argument_list|,
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|prepareForAdd
parameter_list|(
name|int
name|len
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|rowLength
operator|<
literal|0
condition|)
block|{
if|if
condition|(
name|len
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Different size rows: 0 and "
operator|+
name|len
argument_list|)
throw|;
block|}
operator|--
name|rowLength
expr_stmt|;
comment|// see rowLength javadoc
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|rowLength
operator|!=
name|len
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Different size rows: "
operator|+
name|rowLength
operator|+
literal|" and "
operator|+
name|len
argument_list|)
throw|;
block|}
name|int
name|oldLen
init|=
name|this
operator|.
name|array
operator|.
name|length
decl_stmt|;
name|listRealloc
argument_list|(
name|oldLen
operator|+
name|len
argument_list|)
expr_stmt|;
return|return
name|oldLen
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|MapJoinObjectSerDeContext
name|valueContext
parameter_list|,
name|ObjectOutputStream
name|out
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" cannot be serialized"
argument_list|)
throw|;
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
throws|throws
name|HiveException
block|{
if|if
condition|(
name|array
operator|.
name|length
operator|==
name|rowLength
condition|)
block|{
comment|// optimize for common case - just one row for a key, container acts as iterator
return|return
name|this
return|;
block|}
return|return
name|rowLength
operator|>
literal|0
condition|?
operator|new
name|RowIterator
argument_list|()
else|:
operator|new
name|EmptyRowIterator
argument_list|(
operator|-
name|rowLength
argument_list|)
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
throws|throws
name|HiveException
block|{
if|if
condition|(
name|array
operator|.
name|length
operator|!=
name|rowLength
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Incorrect iterator usage, not single-row"
argument_list|)
throw|;
block|}
return|return
name|this
return|;
comment|// optimize for common case - just one row for a key, container acts as row
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
return|return
literal|null
return|;
comment|// single-row case, there's no next
block|}
comment|/** Iterator for row length 0. */
specifier|private
specifier|static
class|class
name|EmptyRowIterator
implements|implements
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
name|List
argument_list|<
name|Object
argument_list|>
name|EMPTY_ROW
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|int
name|rowCount
decl_stmt|;
specifier|public
name|EmptyRowIterator
parameter_list|(
name|int
name|rowCount
parameter_list|)
block|{
name|this
operator|.
name|rowCount
operator|=
name|rowCount
expr_stmt|;
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
throws|throws
name|HiveException
block|{
return|return
name|next
argument_list|()
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
throws|throws
name|HiveException
block|{
return|return
operator|(
operator|--
name|rowCount
operator|<
literal|0
operator|)
condition|?
literal|null
else|:
name|EMPTY_ROW
return|;
block|}
block|}
comment|/** Row iterator for non-zero-length rows. */
specifier|private
class|class
name|RowIterator
implements|implements
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
name|int
name|index
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|first
parameter_list|()
throws|throws
name|HiveException
block|{
name|index
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|array
operator|.
name|length
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|ReadOnlySubList
argument_list|(
literal|0
argument_list|,
name|rowLength
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
operator|+=
name|rowLength
expr_stmt|;
if|if
condition|(
name|index
operator|<
name|array
operator|.
name|length
condition|)
block|{
return|return
operator|new
name|ReadOnlySubList
argument_list|(
name|index
argument_list|,
name|rowLength
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|void
name|read
parameter_list|(
name|AbstractSerDe
name|serde
parameter_list|,
name|Writable
name|writable
parameter_list|,
name|int
name|rowOffset
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|ObjectInspectorUtils
operator|.
name|copyStructToArray
argument_list|(
name|serde
operator|.
name|deserialize
argument_list|(
name|writable
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
argument_list|,
name|this
operator|.
name|array
argument_list|,
name|rowOffset
operator|*
name|rowLength
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Lazy deserialize error"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasRows
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
name|rowCount
argument_list|()
operator|>
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSingleRow
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
name|rowCount
argument_list|()
operator|==
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|rowCount
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
name|rowLength
operator|>
literal|0
condition|?
operator|(
name|array
operator|.
name|length
operator|/
name|rowLength
operator|)
else|:
operator|-
name|rowLength
return|;
comment|// see rowLength javadoc
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearRows
parameter_list|()
block|{
name|array
operator|=
name|EMPTY_OBJECT_ARRAY
expr_stmt|;
name|rowLength
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getAliasFilter
parameter_list|()
throws|throws
name|HiveException
block|{
name|ensureAliasFilter
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|aliasFilter
return|;
block|}
specifier|private
name|void
name|ensureAliasFilter
parameter_list|()
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|isAliasFilterSet
operator|&&
name|rowLength
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|int
name|offset
init|=
name|rowLength
operator|-
literal|1
init|;
name|offset
operator|<
name|array
operator|.
name|length
condition|;
name|offset
operator|+=
name|rowLength
control|)
block|{
name|aliasFilter
operator|&=
operator|(
operator|(
name|ShortWritable
operator|)
name|array
index|[
name|offset
index|]
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
name|isAliasFilterSet
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|MapJoinRowContainer
name|copy
parameter_list|()
throws|throws
name|HiveException
block|{
name|FlatRowContainer
name|result
init|=
operator|new
name|FlatRowContainer
argument_list|()
decl_stmt|;
name|result
operator|.
name|array
operator|=
operator|new
name|Object
index|[
name|this
operator|.
name|array
operator|.
name|length
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|this
operator|.
name|array
argument_list|,
literal|0
argument_list|,
name|result
operator|.
name|array
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|array
operator|.
name|length
argument_list|)
expr_stmt|;
name|result
operator|.
name|rowLength
operator|=
name|rowLength
expr_stmt|;
name|result
operator|.
name|aliasFilter
operator|=
name|aliasFilter
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Implementation of List<Object> and assorted methods
specifier|private
name|void
name|listRealloc
parameter_list|(
name|int
name|length
parameter_list|)
block|{
name|Object
index|[]
name|array
init|=
operator|new
name|Object
index|[
name|length
index|]
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|array
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|this
operator|.
name|array
argument_list|,
literal|0
argument_list|,
name|array
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|array
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
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
name|int
name|size
parameter_list|()
block|{
name|checkSingleRow
argument_list|()
expr_stmt|;
return|return
name|array
operator|.
name|length
return|;
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
specifier|private
class|class
name|ReadOnlySubList
extends|extends
name|AbstractList
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
name|int
name|offset
decl_stmt|;
specifier|private
name|int
name|size
decl_stmt|;
name|ReadOnlySubList
parameter_list|(
name|int
name|from
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|this
operator|.
name|offset
operator|=
name|from
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
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
operator|+
name|offset
index|]
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
specifier|public
name|Iterator
argument_list|<
name|Object
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|listIterator
argument_list|()
return|;
block|}
specifier|public
name|ListIterator
argument_list|<
name|Object
argument_list|>
name|listIterator
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|listIteratorInternal
argument_list|(
name|offset
operator|+
name|index
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|size
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|subList
parameter_list|(
name|int
name|fromIndex
parameter_list|,
name|int
name|toIndex
parameter_list|)
block|{
return|return
operator|new
name|ReadOnlySubList
argument_list|(
name|offset
operator|+
name|fromIndex
argument_list|,
name|toIndex
operator|-
name|fromIndex
argument_list|)
return|;
block|}
specifier|public
name|Object
index|[]
name|toArray
parameter_list|()
block|{
name|Object
index|[]
name|result
init|=
operator|new
name|Object
index|[
name|size
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|array
argument_list|,
name|offset
argument_list|,
name|result
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
comment|// end ReadOnlySubList
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|toArray
parameter_list|()
block|{
name|checkSingleRow
argument_list|()
expr_stmt|;
return|return
name|array
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Object
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|listIterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListIterator
argument_list|<
name|Object
argument_list|>
name|listIterator
parameter_list|()
block|{
return|return
name|listIterator
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListIterator
argument_list|<
name|Object
argument_list|>
name|listIterator
parameter_list|(
specifier|final
name|int
name|index
parameter_list|)
block|{
name|checkSingleRow
argument_list|()
expr_stmt|;
return|return
name|listIteratorInternal
argument_list|(
name|index
argument_list|,
literal|0
argument_list|,
name|array
operator|.
name|length
argument_list|)
return|;
block|}
specifier|private
name|ListIterator
argument_list|<
name|Object
argument_list|>
name|listIteratorInternal
parameter_list|(
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|int
name|iterMinPos
parameter_list|,
specifier|final
name|int
name|iterMaxPos
parameter_list|)
block|{
return|return
operator|new
name|ListIterator
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|private
name|int
name|pos
init|=
name|index
operator|-
literal|1
decl_stmt|;
specifier|public
name|int
name|nextIndex
parameter_list|()
block|{
return|return
name|pos
operator|+
literal|1
return|;
block|}
specifier|public
name|int
name|previousIndex
parameter_list|()
block|{
return|return
name|pos
operator|-
literal|1
return|;
block|}
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|nextIndex
argument_list|()
operator|<
name|iterMaxPos
return|;
block|}
specifier|public
name|boolean
name|hasPrevious
parameter_list|()
block|{
return|return
name|previousIndex
argument_list|()
operator|>=
name|iterMinPos
return|;
block|}
specifier|public
name|Object
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
return|return
name|get
argument_list|(
operator|++
name|pos
argument_list|)
return|;
block|}
specifier|public
name|Object
name|previous
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasPrevious
argument_list|()
condition|)
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
return|return
name|get
argument_list|(
operator|--
name|pos
argument_list|)
return|;
block|}
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|Object
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|Object
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
return|;
comment|// end ListIterator
block|}
annotation|@
name|Override
specifier|public
name|int
name|indexOf
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|checkSingleRow
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|array
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|array
index|[
name|i
index|]
operator|==
literal|null
condition|)
return|return
name|i
return|;
block|}
else|else
block|{
if|if
condition|(
name|o
operator|.
name|equals
argument_list|(
name|array
index|[
name|i
index|]
argument_list|)
condition|)
return|return
name|i
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
specifier|private
name|void
name|checkSingleRow
parameter_list|()
throws|throws
name|AssertionError
block|{
if|if
condition|(
name|array
operator|.
name|length
operator|!=
name|rowLength
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Incorrect list usage, not single-row"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|lastIndexOf
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|checkSingleRow
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|array
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|array
index|[
name|i
index|]
operator|==
literal|null
condition|)
return|return
name|i
return|;
block|}
else|else
block|{
if|if
condition|(
name|o
operator|.
name|equals
argument_list|(
name|array
index|[
name|i
index|]
argument_list|)
condition|)
return|return
name|i
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|subList
parameter_list|(
name|int
name|fromIndex
parameter_list|,
name|int
name|toIndex
parameter_list|)
block|{
name|checkSingleRow
argument_list|()
expr_stmt|;
return|return
operator|new
name|ReadOnlySubList
argument_list|(
name|fromIndex
argument_list|,
name|toIndex
operator|-
name|fromIndex
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|addAll
parameter_list|(
name|int
name|index
parameter_list|,
name|Collection
argument_list|<
name|?
extends|extends
name|Object
argument_list|>
name|c
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|Object
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|Object
name|element
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|int
name|index
parameter_list|,
name|Object
name|element
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|Object
name|remove
parameter_list|(
name|int
name|index
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

