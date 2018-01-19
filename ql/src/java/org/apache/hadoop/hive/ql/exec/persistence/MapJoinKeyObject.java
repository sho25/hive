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
name|Arrays
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
name|ql
operator|.
name|exec
operator|.
name|ExprNodeEvaluator
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
name|vector
operator|.
name|VectorHashKeyWrapper
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
name|vector
operator|.
name|VectorHashKeyWrapperBatch
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
name|vector
operator|.
name|expressions
operator|.
name|VectorExpression
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
name|vector
operator|.
name|expressions
operator|.
name|VectorExpressionWriter
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
name|MapJoinKeyObject
extends|extends
name|MapJoinKey
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
name|Object
index|[]
name|key
decl_stmt|;
specifier|public
name|MapJoinKeyObject
parameter_list|(
name|Object
index|[]
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
specifier|public
name|MapJoinKeyObject
parameter_list|()
block|{
name|this
argument_list|(
name|EMPTY_OBJECT_ARRAY
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Object
index|[]
name|getKeyObjects
parameter_list|()
block|{
return|return
name|key
return|;
block|}
specifier|public
name|void
name|setKeyObjects
parameter_list|(
name|Object
index|[]
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
specifier|public
name|int
name|getKeyLength
parameter_list|()
block|{
return|return
name|key
operator|.
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasAnyNulls
parameter_list|(
name|int
name|fieldCount
parameter_list|,
name|boolean
index|[]
name|nullsafes
parameter_list|)
block|{
assert|assert
name|fieldCount
operator|==
name|key
operator|.
name|length
assert|;
if|if
condition|(
name|key
operator|!=
literal|null
operator|&&
name|key
operator|.
name|length
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|key
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|key
index|[
name|i
index|]
operator|==
literal|null
operator|&&
operator|(
name|nullsafes
operator|==
literal|null
operator|||
operator|!
name|nullsafes
index|[
name|i
index|]
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|writableArrayHashCode
argument_list|(
name|key
argument_list|)
return|;
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
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|MapJoinKeyObject
name|other
init|=
operator|(
name|MapJoinKeyObject
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|key
argument_list|,
name|other
operator|.
name|key
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
specifier|public
name|void
name|read
parameter_list|(
name|MapJoinObjectSerDeContext
name|context
parameter_list|,
name|Writable
name|container
parameter_list|)
throws|throws
name|SerDeException
block|{
name|read
argument_list|(
name|context
operator|.
name|getSerDe
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
argument_list|,
name|context
operator|.
name|getSerDe
argument_list|()
operator|.
name|deserialize
argument_list|(
name|container
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|read
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|Object
name|obj
parameter_list|)
throws|throws
name|SerDeException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
name|obj
argument_list|,
name|oi
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
name|key
operator|=
name|EMPTY_OBJECT_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|key
operator|=
name|value
operator|.
name|toArray
argument_list|()
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
name|AbstractSerDe
name|serde
init|=
name|context
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|ObjectInspector
name|objectInspector
init|=
name|context
operator|.
name|getStandardOI
argument_list|()
decl_stmt|;
name|Writable
name|container
init|=
name|serde
operator|.
name|serialize
argument_list|(
name|key
argument_list|,
name|objectInspector
argument_list|)
decl_stmt|;
name|container
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|readFromRow
parameter_list|(
name|Object
index|[]
name|fieldObjs
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|keyFieldsOI
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|key
operator|==
literal|null
operator|||
name|key
operator|.
name|length
operator|!=
name|fieldObjs
operator|.
name|length
condition|)
block|{
name|key
operator|=
operator|new
name|Object
index|[
name|fieldObjs
operator|.
name|length
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|keyIndex
init|=
literal|0
init|;
name|keyIndex
operator|<
name|fieldObjs
operator|.
name|length
condition|;
operator|++
name|keyIndex
control|)
block|{
name|key
index|[
name|keyIndex
index|]
operator|=
operator|(
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|fieldObjs
index|[
name|keyIndex
index|]
argument_list|,
name|keyFieldsOI
operator|.
name|get
argument_list|(
name|keyIndex
argument_list|)
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
operator|)
expr_stmt|;
block|}
block|}
specifier|protected
name|boolean
index|[]
name|getNulls
parameter_list|()
block|{
name|boolean
index|[]
name|nulls
init|=
literal|null
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
name|key
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|key
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|nulls
operator|==
literal|null
condition|)
block|{
name|nulls
operator|=
operator|new
name|boolean
index|[
name|key
operator|.
name|length
index|]
expr_stmt|;
block|}
name|nulls
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|nulls
return|;
block|}
specifier|public
name|void
name|readFromVector
parameter_list|(
name|VectorHashKeyWrapper
name|kw
parameter_list|,
name|VectorExpressionWriter
index|[]
name|keyOutputWriters
parameter_list|,
name|VectorHashKeyWrapperBatch
name|keyWrapperBatch
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|key
operator|==
literal|null
operator|||
name|key
operator|.
name|length
operator|!=
name|keyOutputWriters
operator|.
name|length
condition|)
block|{
name|key
operator|=
operator|new
name|Object
index|[
name|keyOutputWriters
operator|.
name|length
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|keyIndex
init|=
literal|0
init|;
name|keyIndex
operator|<
name|keyOutputWriters
operator|.
name|length
condition|;
operator|++
name|keyIndex
control|)
block|{
name|key
index|[
name|keyIndex
index|]
operator|=
name|keyWrapperBatch
operator|.
name|getWritableKeyValue
argument_list|(
name|kw
argument_list|,
name|keyIndex
argument_list|,
name|keyOutputWriters
index|[
name|keyIndex
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

