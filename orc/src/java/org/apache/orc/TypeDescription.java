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
name|orc
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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|BytesColumnVector
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
name|ColumnVector
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
name|DecimalColumnVector
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
name|DoubleColumnVector
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
name|ListColumnVector
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
name|LongColumnVector
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
name|MapColumnVector
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
name|StructColumnVector
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
name|TimestampColumnVector
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
name|UnionColumnVector
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
name|VectorizedRowBatch
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
name|Collections
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

begin_comment
comment|/**  * This is the description of the types in an ORC file.  */
end_comment

begin_class
specifier|public
class|class
name|TypeDescription
block|{
specifier|private
specifier|static
specifier|final
name|int
name|MAX_PRECISION
init|=
literal|38
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_SCALE
init|=
literal|38
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_PRECISION
init|=
literal|38
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_SCALE
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_LENGTH
init|=
literal|256
decl_stmt|;
specifier|public
enum|enum
name|Category
block|{
name|BOOLEAN
argument_list|(
literal|"boolean"
argument_list|,
literal|true
argument_list|)
block|,
name|BYTE
argument_list|(
literal|"tinyint"
argument_list|,
literal|true
argument_list|)
block|,
name|SHORT
argument_list|(
literal|"smallint"
argument_list|,
literal|true
argument_list|)
block|,
name|INT
argument_list|(
literal|"int"
argument_list|,
literal|true
argument_list|)
block|,
name|LONG
argument_list|(
literal|"bigint"
argument_list|,
literal|true
argument_list|)
block|,
name|FLOAT
argument_list|(
literal|"float"
argument_list|,
literal|true
argument_list|)
block|,
name|DOUBLE
argument_list|(
literal|"double"
argument_list|,
literal|true
argument_list|)
block|,
name|STRING
argument_list|(
literal|"string"
argument_list|,
literal|true
argument_list|)
block|,
name|DATE
argument_list|(
literal|"date"
argument_list|,
literal|true
argument_list|)
block|,
name|TIMESTAMP
argument_list|(
literal|"timestamp"
argument_list|,
literal|true
argument_list|)
block|,
name|BINARY
argument_list|(
literal|"binary"
argument_list|,
literal|true
argument_list|)
block|,
name|DECIMAL
argument_list|(
literal|"decimal"
argument_list|,
literal|true
argument_list|)
block|,
name|VARCHAR
argument_list|(
literal|"varchar"
argument_list|,
literal|true
argument_list|)
block|,
name|CHAR
argument_list|(
literal|"char"
argument_list|,
literal|true
argument_list|)
block|,
name|LIST
argument_list|(
literal|"array"
argument_list|,
literal|false
argument_list|)
block|,
name|MAP
argument_list|(
literal|"map"
argument_list|,
literal|false
argument_list|)
block|,
name|STRUCT
argument_list|(
literal|"struct"
argument_list|,
literal|false
argument_list|)
block|,
name|UNION
argument_list|(
literal|"uniontype"
argument_list|,
literal|false
argument_list|)
block|;
name|Category
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|isPrimitive
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|isPrimitive
operator|=
name|isPrimitive
expr_stmt|;
block|}
specifier|final
name|boolean
name|isPrimitive
decl_stmt|;
specifier|final
name|String
name|name
decl_stmt|;
specifier|public
name|boolean
name|isPrimitive
parameter_list|()
block|{
return|return
name|isPrimitive
return|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
block|}
specifier|public
specifier|static
name|TypeDescription
name|createBoolean
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|BOOLEAN
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createByte
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|BYTE
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createShort
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|SHORT
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createInt
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|INT
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createLong
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|LONG
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createFloat
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|FLOAT
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createDouble
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|DOUBLE
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createString
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|STRING
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createDate
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|DATE
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createTimestamp
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|TIMESTAMP
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createBinary
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|BINARY
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createDecimal
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|DECIMAL
argument_list|)
return|;
block|}
comment|/**    * For decimal types, set the precision.    * @param precision the new precision    * @return this    */
specifier|public
name|TypeDescription
name|withPrecision
parameter_list|(
name|int
name|precision
parameter_list|)
block|{
if|if
condition|(
name|category
operator|!=
name|Category
operator|.
name|DECIMAL
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"precision is only allowed on decimal"
operator|+
literal|" and not "
operator|+
name|category
operator|.
name|name
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|precision
argument_list|<
literal|1
operator|||
name|precision
argument_list|>
name|MAX_PRECISION
operator|||
name|scale
operator|>
name|precision
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"precision "
operator|+
name|precision
operator|+
literal|" is out of range 1 .. "
operator|+
name|scale
argument_list|)
throw|;
block|}
name|this
operator|.
name|precision
operator|=
name|precision
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * For decimal types, set the scale.    * @param scale the new scale    * @return this    */
specifier|public
name|TypeDescription
name|withScale
parameter_list|(
name|int
name|scale
parameter_list|)
block|{
if|if
condition|(
name|category
operator|!=
name|Category
operator|.
name|DECIMAL
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"scale is only allowed on decimal"
operator|+
literal|" and not "
operator|+
name|category
operator|.
name|name
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|scale
argument_list|<
literal|0
operator|||
name|scale
argument_list|>
name|MAX_SCALE
operator|||
name|scale
operator|>
name|precision
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"scale is out of range at "
operator|+
name|scale
argument_list|)
throw|;
block|}
name|this
operator|.
name|scale
operator|=
name|scale
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createVarchar
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|VARCHAR
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createChar
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|CHAR
argument_list|)
return|;
block|}
comment|/**    * Set the maximum length for char and varchar types.    * @param maxLength the maximum value    * @return this    */
specifier|public
name|TypeDescription
name|withMaxLength
parameter_list|(
name|int
name|maxLength
parameter_list|)
block|{
if|if
condition|(
name|category
operator|!=
name|Category
operator|.
name|VARCHAR
operator|&&
name|category
operator|!=
name|Category
operator|.
name|CHAR
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"maxLength is only allowed on char"
operator|+
literal|" and varchar and not "
operator|+
name|category
operator|.
name|name
argument_list|)
throw|;
block|}
name|this
operator|.
name|maxLength
operator|=
name|maxLength
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createList
parameter_list|(
name|TypeDescription
name|childType
parameter_list|)
block|{
name|TypeDescription
name|result
init|=
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|LIST
argument_list|)
decl_stmt|;
name|result
operator|.
name|children
operator|.
name|add
argument_list|(
name|childType
argument_list|)
expr_stmt|;
name|childType
operator|.
name|parent
operator|=
name|result
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createMap
parameter_list|(
name|TypeDescription
name|keyType
parameter_list|,
name|TypeDescription
name|valueType
parameter_list|)
block|{
name|TypeDescription
name|result
init|=
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|MAP
argument_list|)
decl_stmt|;
name|result
operator|.
name|children
operator|.
name|add
argument_list|(
name|keyType
argument_list|)
expr_stmt|;
name|result
operator|.
name|children
operator|.
name|add
argument_list|(
name|valueType
argument_list|)
expr_stmt|;
name|keyType
operator|.
name|parent
operator|=
name|result
expr_stmt|;
name|valueType
operator|.
name|parent
operator|=
name|result
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createUnion
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|UNION
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeDescription
name|createStruct
parameter_list|()
block|{
return|return
operator|new
name|TypeDescription
argument_list|(
name|Category
operator|.
name|STRUCT
argument_list|)
return|;
block|}
comment|/**    * Add a child to a union type.    * @param child a new child type to add    * @return the union type.    */
specifier|public
name|TypeDescription
name|addUnionChild
parameter_list|(
name|TypeDescription
name|child
parameter_list|)
block|{
if|if
condition|(
name|category
operator|!=
name|Category
operator|.
name|UNION
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can only add types to union type"
operator|+
literal|" and not "
operator|+
name|category
argument_list|)
throw|;
block|}
name|children
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
name|child
operator|.
name|parent
operator|=
name|this
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Add a field to a struct type as it is built.    * @param field the field name    * @param fieldType the type of the field    * @return the struct type    */
specifier|public
name|TypeDescription
name|addField
parameter_list|(
name|String
name|field
parameter_list|,
name|TypeDescription
name|fieldType
parameter_list|)
block|{
if|if
condition|(
name|category
operator|!=
name|Category
operator|.
name|STRUCT
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can only add fields to struct type"
operator|+
literal|" and not "
operator|+
name|category
argument_list|)
throw|;
block|}
name|fieldNames
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|children
operator|.
name|add
argument_list|(
name|fieldType
argument_list|)
expr_stmt|;
name|fieldType
operator|.
name|parent
operator|=
name|this
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get the id for this type.    * The first call will cause all of the the ids in tree to be assigned, so    * it should not be called before the type is completely built.    * @return the sequential id    */
specifier|public
name|int
name|getId
parameter_list|()
block|{
comment|// if the id hasn't been assigned, assign all of the ids from the root
if|if
condition|(
name|id
operator|==
operator|-
literal|1
condition|)
block|{
name|TypeDescription
name|root
init|=
name|this
decl_stmt|;
while|while
condition|(
name|root
operator|.
name|parent
operator|!=
literal|null
condition|)
block|{
name|root
operator|=
name|root
operator|.
name|parent
expr_stmt|;
block|}
name|root
operator|.
name|assignIds
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|id
return|;
block|}
specifier|public
name|TypeDescription
name|clone
parameter_list|()
block|{
name|TypeDescription
name|result
init|=
operator|new
name|TypeDescription
argument_list|(
name|category
argument_list|)
decl_stmt|;
name|result
operator|.
name|maxLength
operator|=
name|maxLength
expr_stmt|;
name|result
operator|.
name|precision
operator|=
name|precision
expr_stmt|;
name|result
operator|.
name|scale
operator|=
name|scale
expr_stmt|;
if|if
condition|(
name|fieldNames
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|fieldNames
operator|.
name|addAll
argument_list|(
name|fieldNames
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TypeDescription
name|child
range|:
name|children
control|)
block|{
name|TypeDescription
name|clone
init|=
name|child
operator|.
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|parent
operator|=
name|result
expr_stmt|;
name|result
operator|.
name|children
operator|.
name|add
argument_list|(
name|clone
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
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
name|getId
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
operator|||
name|other
operator|.
name|getClass
argument_list|()
operator|!=
name|TypeDescription
operator|.
name|class
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|other
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
name|TypeDescription
name|castOther
init|=
operator|(
name|TypeDescription
operator|)
name|other
decl_stmt|;
if|if
condition|(
name|category
operator|!=
name|castOther
operator|.
name|category
operator|||
name|getId
argument_list|()
operator|!=
name|castOther
operator|.
name|getId
argument_list|()
operator|||
name|getMaximumId
argument_list|()
operator|!=
name|castOther
operator|.
name|getMaximumId
argument_list|()
operator|||
name|maxLength
operator|!=
name|castOther
operator|.
name|maxLength
operator|||
name|scale
operator|!=
name|castOther
operator|.
name|scale
operator|||
name|precision
operator|!=
name|castOther
operator|.
name|precision
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|children
operator|.
name|size
argument_list|()
operator|!=
name|castOther
operator|.
name|children
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|children
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|equals
argument_list|(
name|castOther
operator|.
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
if|if
condition|(
name|category
operator|==
name|Category
operator|.
name|STRUCT
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
name|fieldNames
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|fieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|equals
argument_list|(
name|castOther
operator|.
name|fieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Get the maximum id assigned to this type or its children.    * The first call will cause all of the the ids in tree to be assigned, so    * it should not be called before the type is completely built.    * @return the maximum id assigned under this type    */
specifier|public
name|int
name|getMaximumId
parameter_list|()
block|{
comment|// if the id hasn't been assigned, assign all of the ids from the root
if|if
condition|(
name|maxId
operator|==
operator|-
literal|1
condition|)
block|{
name|TypeDescription
name|root
init|=
name|this
decl_stmt|;
while|while
condition|(
name|root
operator|.
name|parent
operator|!=
literal|null
condition|)
block|{
name|root
operator|=
name|root
operator|.
name|parent
expr_stmt|;
block|}
name|root
operator|.
name|assignIds
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|maxId
return|;
block|}
specifier|private
name|ColumnVector
name|createColumn
parameter_list|(
name|int
name|maxSize
parameter_list|)
block|{
switch|switch
condition|(
name|category
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|DATE
case|:
return|return
operator|new
name|LongColumnVector
argument_list|(
name|maxSize
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
return|return
operator|new
name|TimestampColumnVector
argument_list|(
name|maxSize
argument_list|)
return|;
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
return|return
operator|new
name|DoubleColumnVector
argument_list|(
name|maxSize
argument_list|)
return|;
case|case
name|DECIMAL
case|:
return|return
operator|new
name|DecimalColumnVector
argument_list|(
name|maxSize
argument_list|,
name|precision
argument_list|,
name|scale
argument_list|)
return|;
case|case
name|STRING
case|:
case|case
name|BINARY
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
return|return
operator|new
name|BytesColumnVector
argument_list|(
name|maxSize
argument_list|)
return|;
case|case
name|STRUCT
case|:
block|{
name|ColumnVector
index|[]
name|fieldVector
init|=
operator|new
name|ColumnVector
index|[
name|children
operator|.
name|size
argument_list|()
index|]
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
name|fieldVector
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fieldVector
index|[
name|i
index|]
operator|=
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|createColumn
argument_list|(
name|maxSize
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|StructColumnVector
argument_list|(
name|maxSize
argument_list|,
name|fieldVector
argument_list|)
return|;
block|}
case|case
name|UNION
case|:
block|{
name|ColumnVector
index|[]
name|fieldVector
init|=
operator|new
name|ColumnVector
index|[
name|children
operator|.
name|size
argument_list|()
index|]
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
name|fieldVector
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fieldVector
index|[
name|i
index|]
operator|=
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|createColumn
argument_list|(
name|maxSize
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|UnionColumnVector
argument_list|(
name|maxSize
argument_list|,
name|fieldVector
argument_list|)
return|;
block|}
case|case
name|LIST
case|:
return|return
operator|new
name|ListColumnVector
argument_list|(
name|maxSize
argument_list|,
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|createColumn
argument_list|(
name|maxSize
argument_list|)
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
operator|new
name|MapColumnVector
argument_list|(
name|maxSize
argument_list|,
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|createColumn
argument_list|(
name|maxSize
argument_list|)
argument_list|,
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|createColumn
argument_list|(
name|maxSize
argument_list|)
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown type "
operator|+
name|category
argument_list|)
throw|;
block|}
block|}
specifier|public
name|VectorizedRowBatch
name|createRowBatch
parameter_list|(
name|int
name|maxSize
parameter_list|)
block|{
name|VectorizedRowBatch
name|result
decl_stmt|;
if|if
condition|(
name|category
operator|==
name|Category
operator|.
name|STRUCT
condition|)
block|{
name|result
operator|=
operator|new
name|VectorizedRowBatch
argument_list|(
name|children
operator|.
name|size
argument_list|()
argument_list|,
name|maxSize
argument_list|)
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
name|result
operator|.
name|cols
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|result
operator|.
name|cols
index|[
name|i
index|]
operator|=
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|createColumn
argument_list|(
name|maxSize
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|result
operator|=
operator|new
name|VectorizedRowBatch
argument_list|(
literal|1
argument_list|,
name|maxSize
argument_list|)
expr_stmt|;
name|result
operator|.
name|cols
index|[
literal|0
index|]
operator|=
name|createColumn
argument_list|(
name|maxSize
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|VectorizedRowBatch
name|createRowBatch
parameter_list|()
block|{
return|return
name|createRowBatch
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
return|;
block|}
comment|/**    * Get the kind of this type.    * @return get the category for this type.    */
specifier|public
name|Category
name|getCategory
parameter_list|()
block|{
return|return
name|category
return|;
block|}
comment|/**    * Get the maximum length of the type. Only used for char and varchar types.    * @return the maximum length of the string type    */
specifier|public
name|int
name|getMaxLength
parameter_list|()
block|{
return|return
name|maxLength
return|;
block|}
comment|/**    * Get the precision of the decimal type.    * @return the number of digits for the precision.    */
specifier|public
name|int
name|getPrecision
parameter_list|()
block|{
return|return
name|precision
return|;
block|}
comment|/**    * Get the scale of the decimal type.    * @return the number of digits for the scale.    */
specifier|public
name|int
name|getScale
parameter_list|()
block|{
return|return
name|scale
return|;
block|}
comment|/**    * For struct types, get the list of field names.    * @return the list of field names.    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getFieldNames
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|fieldNames
argument_list|)
return|;
block|}
comment|/**    * Get the subtypes of this type.    * @return the list of children types    */
specifier|public
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|getChildren
parameter_list|()
block|{
return|return
name|children
operator|==
literal|null
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|children
argument_list|)
return|;
block|}
comment|/**    * Assign ids to all of the nodes under this one.    * @param startId the lowest id to assign    * @return the next available id    */
specifier|private
name|int
name|assignIds
parameter_list|(
name|int
name|startId
parameter_list|)
block|{
name|id
operator|=
name|startId
operator|++
expr_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TypeDescription
name|child
range|:
name|children
control|)
block|{
name|startId
operator|=
name|child
operator|.
name|assignIds
argument_list|(
name|startId
argument_list|)
expr_stmt|;
block|}
block|}
name|maxId
operator|=
name|startId
operator|-
literal|1
expr_stmt|;
return|return
name|startId
return|;
block|}
specifier|private
name|TypeDescription
parameter_list|(
name|Category
name|category
parameter_list|)
block|{
name|this
operator|.
name|category
operator|=
name|category
expr_stmt|;
if|if
condition|(
name|category
operator|.
name|isPrimitive
condition|)
block|{
name|children
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|children
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|category
operator|==
name|Category
operator|.
name|STRUCT
condition|)
block|{
name|fieldNames
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|fieldNames
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|private
name|int
name|id
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|int
name|maxId
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|TypeDescription
name|parent
decl_stmt|;
specifier|private
specifier|final
name|Category
name|category
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|children
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
decl_stmt|;
specifier|private
name|int
name|maxLength
init|=
name|DEFAULT_LENGTH
decl_stmt|;
specifier|private
name|int
name|precision
init|=
name|DEFAULT_PRECISION
decl_stmt|;
specifier|private
name|int
name|scale
init|=
name|DEFAULT_SCALE
decl_stmt|;
specifier|public
name|void
name|printToBuffer
parameter_list|(
name|StringBuilder
name|buffer
parameter_list|)
block|{
name|buffer
operator|.
name|append
argument_list|(
name|category
operator|.
name|name
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|category
condition|)
block|{
case|case
name|DECIMAL
case|:
name|buffer
operator|.
name|append
argument_list|(
literal|'('
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|precision
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|scale
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
name|buffer
operator|.
name|append
argument_list|(
literal|'('
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|maxLength
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
case|case
name|MAP
case|:
case|case
name|UNION
case|:
name|buffer
operator|.
name|append
argument_list|(
literal|'<'
argument_list|)
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
name|children
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|printToBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|'>'
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRUCT
case|:
name|buffer
operator|.
name|append
argument_list|(
literal|'<'
argument_list|)
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
name|children
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
name|fieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|printToBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|'>'
argument_list|)
expr_stmt|;
break|break;
default|default:
break|break;
block|}
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|printToBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|printJsonToBuffer
parameter_list|(
name|String
name|prefix
parameter_list|,
name|StringBuilder
name|buffer
parameter_list|,
name|int
name|indent
parameter_list|)
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
name|indent
condition|;
operator|++
name|i
control|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
name|prefix
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"{\"category\": \""
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|category
operator|.
name|name
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"\", \"id\": "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|", \"max\": "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|maxId
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|category
condition|)
block|{
case|case
name|DECIMAL
case|:
name|buffer
operator|.
name|append
argument_list|(
literal|", \"precision\": "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|precision
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|", \"scale\": "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|scale
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
name|buffer
operator|.
name|append
argument_list|(
literal|", \"length\": "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|maxLength
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
case|case
name|MAP
case|:
case|case
name|UNION
case|:
name|buffer
operator|.
name|append
argument_list|(
literal|", \"children\": ["
argument_list|)
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
name|children
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|printJsonToBuffer
argument_list|(
literal|""
argument_list|,
name|buffer
argument_list|,
name|indent
operator|+
literal|2
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|!=
name|children
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRUCT
case|:
name|buffer
operator|.
name|append
argument_list|(
literal|", \"fields\": ["
argument_list|)
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
name|children
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|printJsonToBuffer
argument_list|(
literal|"\""
operator|+
name|fieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|"\": "
argument_list|,
name|buffer
argument_list|,
name|indent
operator|+
literal|2
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|!=
name|children
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
break|break;
default|default:
break|break;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|'}'
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|toJson
parameter_list|()
block|{
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|printJsonToBuffer
argument_list|(
literal|""
argument_list|,
name|buffer
argument_list|,
literal|0
argument_list|)
expr_stmt|;
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

