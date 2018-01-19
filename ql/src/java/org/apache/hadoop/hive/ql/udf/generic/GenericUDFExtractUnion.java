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
name|udf
operator|.
name|generic
package|;
end_package

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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|Description
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
name|UDFArgumentException
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
name|objectinspector
operator|.
name|ListObjectInspector
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
name|MapObjectInspector
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
name|ObjectInspector
operator|.
name|Category
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
name|ObjectInspectorFactory
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
name|SettableListObjectInspector
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
name|SettableMapObjectInspector
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
name|SettableStructObjectInspector
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
name|StructField
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
name|StructObjectInspector
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
name|UnionObjectInspector
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
name|primitive
operator|.
name|WritableConstantIntObjectInspector
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

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"extract_union"
argument_list|,
name|value
operator|=
literal|"_FUNC_(union[, tag])"
operator|+
literal|" - Recursively explodes unions into structs or simply extracts the given tag."
argument_list|,
name|extended
operator|=
literal|"> SELECT _FUNC_({0:\"foo\"}).tag_0 FROM src;\n  foo\n"
operator|+
literal|"> SELECT _FUNC_({0:\"foo\"}).tag_1 FROM src;\n  null\n"
operator|+
literal|"> SELECT _FUNC_({0:\"foo\"}, 0) FROM src;\n  foo\n"
operator|+
literal|"> SELECT _FUNC_({0:\"foo\"}, 1) FROM src;\n  null"
argument_list|)
specifier|public
class|class
name|GenericUDFExtractUnion
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|static
specifier|final
name|int
name|ALL_TAGS
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspectorConverter
name|objectInspectorConverter
decl_stmt|;
specifier|private
specifier|final
name|ValueConverter
name|valueConverter
decl_stmt|;
specifier|private
name|int
name|tag
init|=
name|ALL_TAGS
decl_stmt|;
specifier|private
name|UnionObjectInspector
name|unionOI
decl_stmt|;
specifier|private
name|ObjectInspector
name|sourceOI
decl_stmt|;
specifier|public
name|GenericUDFExtractUnion
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|ObjectInspectorConverter
argument_list|()
argument_list|,
operator|new
name|ValueConverter
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|GenericUDFExtractUnion
parameter_list|(
name|ObjectInspectorConverter
name|objectInspectorConverter
parameter_list|,
name|ValueConverter
name|valueConverter
parameter_list|)
block|{
name|this
operator|.
name|objectInspectorConverter
operator|=
name|objectInspectorConverter
expr_stmt|;
name|this
operator|.
name|valueConverter
operator|=
name|valueConverter
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|arguments
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|sourceOI
operator|=
name|arguments
index|[
literal|0
index|]
expr_stmt|;
return|return
name|objectInspectorConverter
operator|.
name|convert
argument_list|(
name|sourceOI
argument_list|)
return|;
block|}
if|if
condition|(
name|arguments
operator|.
name|length
operator|==
literal|2
operator|&&
operator|(
name|arguments
index|[
literal|0
index|]
operator|instanceof
name|UnionObjectInspector
operator|)
operator|&&
operator|(
name|arguments
index|[
literal|1
index|]
operator|instanceof
name|WritableConstantIntObjectInspector
operator|)
condition|)
block|{
name|tag
operator|=
operator|(
operator|(
name|WritableConstantIntObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|unionOI
operator|=
operator|(
name|UnionObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIs
init|=
operator|(
operator|(
name|UnionObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
operator|)
operator|.
name|getObjectInspectors
argument_list|()
decl_stmt|;
if|if
condition|(
name|tag
operator|<
literal|0
operator|||
name|tag
operator|>=
name|fieldOIs
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"int constant must be a valid union tag for "
operator|+
name|unionOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|". Expected 0-"
operator|+
operator|(
name|fieldOIs
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|)
operator|+
literal|" got: "
operator|+
name|tag
argument_list|)
throw|;
block|}
return|return
name|fieldOIs
operator|.
name|get
argument_list|(
name|tag
argument_list|)
return|;
block|}
name|String
name|argumentTypes
init|=
literal|"nothing"
decl_stmt|;
if|if
condition|(
name|arguments
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|typeNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ObjectInspector
name|oi
range|:
name|arguments
control|)
block|{
name|typeNames
operator|.
name|add
argument_list|(
name|oi
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|argumentTypes
operator|=
name|typeNames
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"Unsupported arguments. Expected a type containing a union or a union and an int constant, got: "
operator|+
name|argumentTypes
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
name|Object
name|value
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|tag
operator|==
name|ALL_TAGS
condition|)
block|{
return|return
name|valueConverter
operator|.
name|convert
argument_list|(
name|value
argument_list|,
name|sourceOI
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|tag
operator|==
name|unionOI
operator|.
name|getTag
argument_list|(
name|value
argument_list|)
condition|)
block|{
return|return
name|unionOI
operator|.
name|getField
argument_list|(
name|value
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"extract_union("
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
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|static
class|class
name|ObjectInspectorConverter
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TAG_FIELD_PREFIX
init|=
literal|"tag_"
decl_stmt|;
name|ObjectInspector
name|convert
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|)
block|{
name|AtomicBoolean
name|foundUnion
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|ObjectInspector
name|result
init|=
name|convert
argument_list|(
name|inspector
argument_list|,
name|foundUnion
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|foundUnion
operator|.
name|get
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No unions found in "
operator|+
name|inspector
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|ObjectInspector
name|convert
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|,
name|AtomicBoolean
name|foundUnion
parameter_list|)
block|{
name|Category
name|category
init|=
name|inspector
operator|.
name|getCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|category
condition|)
block|{
case|case
name|PRIMITIVE
case|:
return|return
name|inspector
return|;
case|case
name|LIST
case|:
return|return
name|convertList
argument_list|(
name|inspector
argument_list|,
name|foundUnion
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
name|convertMap
argument_list|(
name|inspector
argument_list|,
name|foundUnion
argument_list|)
return|;
case|case
name|STRUCT
case|:
return|return
name|convertStruct
argument_list|(
name|inspector
argument_list|,
name|foundUnion
argument_list|)
return|;
case|case
name|UNION
case|:
name|foundUnion
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|convertUnion
argument_list|(
name|inspector
argument_list|,
name|foundUnion
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unknown category: "
operator|+
name|category
argument_list|)
throw|;
block|}
block|}
specifier|private
name|ObjectInspector
name|convertList
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|,
name|AtomicBoolean
name|foundUnion
parameter_list|)
block|{
name|ListObjectInspector
name|listOI
init|=
operator|(
name|ListObjectInspector
operator|)
name|inspector
decl_stmt|;
name|ObjectInspector
name|elementOI
init|=
name|convert
argument_list|(
name|listOI
operator|.
name|getListElementObjectInspector
argument_list|()
argument_list|,
name|foundUnion
argument_list|)
decl_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|elementOI
argument_list|)
return|;
block|}
specifier|private
name|ObjectInspector
name|convertMap
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|,
name|AtomicBoolean
name|foundUnion
parameter_list|)
block|{
name|MapObjectInspector
name|mapOI
init|=
operator|(
name|MapObjectInspector
operator|)
name|inspector
decl_stmt|;
name|ObjectInspector
name|keyOI
init|=
name|convert
argument_list|(
name|mapOI
operator|.
name|getMapKeyObjectInspector
argument_list|()
argument_list|,
name|foundUnion
argument_list|)
decl_stmt|;
name|ObjectInspector
name|valueOI
init|=
name|convert
argument_list|(
name|mapOI
operator|.
name|getMapValueObjectInspector
argument_list|()
argument_list|,
name|foundUnion
argument_list|)
decl_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardMapObjectInspector
argument_list|(
name|keyOI
argument_list|,
name|valueOI
argument_list|)
return|;
block|}
specifier|private
name|ObjectInspector
name|convertStruct
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|,
name|AtomicBoolean
name|foundUnion
parameter_list|)
block|{
name|StructObjectInspector
name|structOI
init|=
operator|(
name|StructObjectInspector
operator|)
name|inspector
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|structOI
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|inspectors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|StructField
name|field
range|:
name|fields
control|)
block|{
name|names
operator|.
name|add
argument_list|(
name|field
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|inspectors
operator|.
name|add
argument_list|(
name|convert
argument_list|(
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|foundUnion
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|names
argument_list|,
name|inspectors
argument_list|)
return|;
block|}
specifier|private
name|ObjectInspector
name|convertUnion
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|,
name|AtomicBoolean
name|foundUnion
parameter_list|)
block|{
name|UnionObjectInspector
name|unionOI
init|=
operator|(
name|UnionObjectInspector
operator|)
name|inspector
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIs
init|=
name|unionOI
operator|.
name|getObjectInspectors
argument_list|()
decl_stmt|;
name|int
name|tags
init|=
name|fieldOIs
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tags
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|inspectors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tags
argument_list|)
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
name|tags
condition|;
name|i
operator|++
control|)
block|{
name|names
operator|.
name|add
argument_list|(
name|TAG_FIELD_PREFIX
operator|+
name|i
argument_list|)
expr_stmt|;
name|inspectors
operator|.
name|add
argument_list|(
name|convert
argument_list|(
name|fieldOIs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|foundUnion
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|names
argument_list|,
name|inspectors
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|ValueConverter
block|{
name|Object
name|convert
parameter_list|(
name|Object
name|value
parameter_list|,
name|ObjectInspector
name|inspector
parameter_list|)
block|{
name|Category
name|category
init|=
name|inspector
operator|.
name|getCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|category
condition|)
block|{
case|case
name|PRIMITIVE
case|:
return|return
name|value
return|;
case|case
name|LIST
case|:
return|return
name|convertList
argument_list|(
name|value
argument_list|,
name|inspector
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
name|convertMap
argument_list|(
name|value
argument_list|,
name|inspector
argument_list|)
return|;
case|case
name|STRUCT
case|:
return|return
name|convertStruct
argument_list|(
name|value
argument_list|,
name|inspector
argument_list|)
return|;
case|case
name|UNION
case|:
return|return
name|convertUnion
argument_list|(
name|value
argument_list|,
name|inspector
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unknown category: "
operator|+
name|category
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Object
name|convertList
parameter_list|(
name|Object
name|list
parameter_list|,
name|ObjectInspector
name|inspector
parameter_list|)
block|{
name|SettableListObjectInspector
name|listOI
init|=
operator|(
name|SettableListObjectInspector
operator|)
name|inspector
decl_stmt|;
name|int
name|size
init|=
name|listOI
operator|.
name|getListLength
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|Object
name|result
init|=
name|listOI
operator|.
name|create
argument_list|(
name|size
argument_list|)
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|listOI
operator|.
name|set
argument_list|(
name|result
argument_list|,
name|i
argument_list|,
name|convert
argument_list|(
name|listOI
operator|.
name|getListElement
argument_list|(
name|list
argument_list|,
name|i
argument_list|)
argument_list|,
name|listOI
operator|.
name|getListElementObjectInspector
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|Object
name|convertMap
parameter_list|(
name|Object
name|map
parameter_list|,
name|ObjectInspector
name|inspector
parameter_list|)
block|{
name|SettableMapObjectInspector
name|mapOI
init|=
operator|(
name|SettableMapObjectInspector
operator|)
name|inspector
decl_stmt|;
name|Object
name|result
init|=
name|mapOI
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|key
range|:
name|mapOI
operator|.
name|getMap
argument_list|(
name|map
argument_list|)
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Object
name|value
init|=
name|mapOI
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
name|key
argument_list|)
decl_stmt|;
name|mapOI
operator|.
name|put
argument_list|(
name|result
argument_list|,
name|convert
argument_list|(
name|key
argument_list|,
name|mapOI
operator|.
name|getMapKeyObjectInspector
argument_list|()
argument_list|)
argument_list|,
name|convert
argument_list|(
name|value
argument_list|,
name|mapOI
operator|.
name|getMapValueObjectInspector
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|Object
name|convertStruct
parameter_list|(
name|Object
name|struct
parameter_list|,
name|ObjectInspector
name|inspector
parameter_list|)
block|{
name|SettableStructObjectInspector
name|structOI
init|=
operator|(
name|SettableStructObjectInspector
operator|)
name|inspector
decl_stmt|;
name|Object
name|result
init|=
name|structOI
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
name|StructField
name|field
range|:
name|structOI
operator|.
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
name|Object
name|value
init|=
name|structOI
operator|.
name|getStructFieldData
argument_list|(
name|struct
argument_list|,
name|field
argument_list|)
decl_stmt|;
name|structOI
operator|.
name|setStructFieldData
argument_list|(
name|result
argument_list|,
name|field
argument_list|,
name|convert
argument_list|(
name|value
argument_list|,
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|Object
name|convertUnion
parameter_list|(
name|Object
name|union
parameter_list|,
name|ObjectInspector
name|inspector
parameter_list|)
block|{
name|UnionObjectInspector
name|unionOI
init|=
operator|(
name|UnionObjectInspector
operator|)
name|inspector
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|childOIs
init|=
name|unionOI
operator|.
name|getObjectInspectors
argument_list|()
decl_stmt|;
name|byte
name|tag
init|=
name|unionOI
operator|.
name|getTag
argument_list|(
name|union
argument_list|)
decl_stmt|;
name|Object
name|value
init|=
name|unionOI
operator|.
name|getField
argument_list|(
name|union
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|childOIs
operator|.
name|size
argument_list|()
argument_list|)
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
name|childOIs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|tag
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|convert
argument_list|(
name|value
argument_list|,
name|childOIs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

