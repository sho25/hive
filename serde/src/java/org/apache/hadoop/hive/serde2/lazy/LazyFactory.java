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
name|serde2
operator|.
name|lazy
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
name|lazy
operator|.
name|objectinspector
operator|.
name|LazyListObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|LazyMapObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|LazyObjectInspectorFactory
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
name|lazy
operator|.
name|objectinspector
operator|.
name|LazySimpleStructObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|LazyUnionObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyBinaryObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyBooleanObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyByteObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyDateObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyDoubleObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyFloatObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyHiveDecimalObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyHiveVarcharObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyIntObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyLongObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyPrimitiveObjectInspectorFactory
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyShortObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyStringObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyTimestampObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyVoidObjectInspector
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
name|lazydio
operator|.
name|LazyDioBoolean
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
name|lazydio
operator|.
name|LazyDioByte
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
name|lazydio
operator|.
name|LazyDioDouble
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
name|lazydio
operator|.
name|LazyDioFloat
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
name|lazydio
operator|.
name|LazyDioInteger
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
name|lazydio
operator|.
name|LazyDioLong
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
name|lazydio
operator|.
name|LazyDioShort
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
name|PrimitiveObjectInspector
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|typeinfo
operator|.
name|ListTypeInfo
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
name|typeinfo
operator|.
name|MapTypeInfo
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|StructTypeInfo
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|UnionTypeInfo
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
name|Text
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

begin_comment
comment|/**  * LazyFactory.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|LazyFactory
block|{
comment|/**    * Create a lazy primitive object instance given a primitive object inspector based on it's    * type. It takes a boolean switch to decide whether to return a binary or standard variant    * of the lazy object.    *    * @param poi PrimitiveObjectInspector    * @param typeBinary a switch to return either a LazyPrimtive class or it's binary    *        companion    * @return LazyPrimitive<? extends ObjectInspector, ? extends Writable>    */
specifier|public
specifier|static
name|LazyPrimitive
argument_list|<
name|?
extends|extends
name|ObjectInspector
argument_list|,
name|?
extends|extends
name|Writable
argument_list|>
name|createLazyPrimitiveClass
parameter_list|(
name|PrimitiveObjectInspector
name|poi
parameter_list|,
name|boolean
name|typeBinary
parameter_list|)
block|{
if|if
condition|(
name|typeBinary
condition|)
block|{
return|return
name|createLazyPrimitiveBinaryClass
argument_list|(
name|poi
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|createLazyPrimitiveClass
argument_list|(
name|poi
argument_list|)
return|;
block|}
block|}
comment|/**    * Create a lazy primitive class given the type name.    */
specifier|public
specifier|static
name|LazyPrimitive
argument_list|<
name|?
extends|extends
name|ObjectInspector
argument_list|,
name|?
extends|extends
name|Writable
argument_list|>
name|createLazyPrimitiveClass
parameter_list|(
name|PrimitiveObjectInspector
name|oi
parameter_list|)
block|{
name|PrimitiveCategory
name|p
init|=
name|oi
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|p
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
operator|new
name|LazyBoolean
argument_list|(
operator|(
name|LazyBooleanObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|BYTE
case|:
return|return
operator|new
name|LazyByte
argument_list|(
operator|(
name|LazyByteObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
operator|new
name|LazyShort
argument_list|(
operator|(
name|LazyShortObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|INT
case|:
return|return
operator|new
name|LazyInteger
argument_list|(
operator|(
name|LazyIntObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
operator|new
name|LazyLong
argument_list|(
operator|(
name|LazyLongObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
operator|new
name|LazyFloat
argument_list|(
operator|(
name|LazyFloatObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|LazyDouble
argument_list|(
operator|(
name|LazyDoubleObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|STRING
case|:
return|return
operator|new
name|LazyString
argument_list|(
operator|(
name|LazyStringObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|VARCHAR
case|:
return|return
operator|new
name|LazyHiveVarchar
argument_list|(
operator|(
name|LazyHiveVarcharObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|DATE
case|:
return|return
operator|new
name|LazyDate
argument_list|(
operator|(
name|LazyDateObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
return|return
operator|new
name|LazyTimestamp
argument_list|(
operator|(
name|LazyTimestampObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|BINARY
case|:
return|return
operator|new
name|LazyBinary
argument_list|(
operator|(
name|LazyBinaryObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|DECIMAL
case|:
return|return
operator|new
name|LazyHiveDecimal
argument_list|(
operator|(
name|LazyHiveDecimalObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|VOID
case|:
return|return
operator|new
name|LazyVoid
argument_list|(
operator|(
name|LazyVoidObjectInspector
operator|)
name|oi
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: no LazyObject for "
operator|+
name|p
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|LazyPrimitive
argument_list|<
name|?
extends|extends
name|ObjectInspector
argument_list|,
name|?
extends|extends
name|Writable
argument_list|>
name|createLazyPrimitiveBinaryClass
parameter_list|(
name|PrimitiveObjectInspector
name|poi
parameter_list|)
block|{
name|PrimitiveCategory
name|pc
init|=
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|pc
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
operator|new
name|LazyDioBoolean
argument_list|(
operator|(
name|LazyBooleanObjectInspector
operator|)
name|poi
argument_list|)
return|;
case|case
name|BYTE
case|:
return|return
operator|new
name|LazyDioByte
argument_list|(
operator|(
name|LazyByteObjectInspector
operator|)
name|poi
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
operator|new
name|LazyDioShort
argument_list|(
operator|(
name|LazyShortObjectInspector
operator|)
name|poi
argument_list|)
return|;
case|case
name|INT
case|:
return|return
operator|new
name|LazyDioInteger
argument_list|(
operator|(
name|LazyIntObjectInspector
operator|)
name|poi
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
operator|new
name|LazyDioLong
argument_list|(
operator|(
name|LazyLongObjectInspector
operator|)
name|poi
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
operator|new
name|LazyDioFloat
argument_list|(
operator|(
name|LazyFloatObjectInspector
operator|)
name|poi
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|LazyDioDouble
argument_list|(
operator|(
name|LazyDoubleObjectInspector
operator|)
name|poi
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive Internal Error: no LazyObject for "
operator|+
name|poi
argument_list|)
throw|;
block|}
block|}
comment|/**    * Create a hierarchical LazyObject based on the given typeInfo.    */
specifier|public
specifier|static
name|LazyObject
argument_list|<
name|?
extends|extends
name|ObjectInspector
argument_list|>
name|createLazyObject
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|)
block|{
name|ObjectInspector
operator|.
name|Category
name|c
init|=
name|oi
operator|.
name|getCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
name|PRIMITIVE
case|:
return|return
name|createLazyPrimitiveClass
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
operator|new
name|LazyMap
argument_list|(
operator|(
name|LazyMapObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|LIST
case|:
return|return
operator|new
name|LazyArray
argument_list|(
operator|(
name|LazyListObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|STRUCT
case|:
return|return
operator|new
name|LazyStruct
argument_list|(
operator|(
name|LazySimpleStructObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|UNION
case|:
return|return
operator|new
name|LazyUnion
argument_list|(
operator|(
name|LazyUnionObjectInspector
operator|)
name|oi
argument_list|)
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive LazySerDe Internal error."
argument_list|)
throw|;
block|}
comment|/**    * Creates a LazyObject based on the LazyObjectInspector. Will create binary variants for    * primitive objects when the switch<code>typeBinary</code> is specified as true.    *    * @param oi ObjectInspector    * @param typeBinary Boolean value used as switch to return variants of LazyPrimitive    *                   objects which are initialized from a binary format for the data.    * @return LazyObject<? extends ObjectInspector>    */
specifier|public
specifier|static
name|LazyObject
argument_list|<
name|?
extends|extends
name|ObjectInspector
argument_list|>
name|createLazyObject
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|boolean
name|typeBinary
parameter_list|)
block|{
if|if
condition|(
name|oi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
return|return
name|createLazyPrimitiveClass
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|oi
argument_list|,
name|typeBinary
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|createLazyObject
argument_list|(
name|oi
argument_list|)
return|;
block|}
block|}
comment|/**    * Create a hierarchical ObjectInspector for LazyObject with the given    * typeInfo.    *    * @param typeInfo    *          The type information for the LazyObject    * @param separator    *          The array of separators for delimiting each level    * @param separatorIndex    *          The current level (for separators). List(array), struct uses 1    *          level of separator, and map uses 2 levels: the first one for    *          delimiting entries, the second one for delimiting key and values.    * @param nullSequence    *          The sequence of bytes representing NULL.    * @return The ObjectInspector    * @throws SerDeException    */
specifier|public
specifier|static
name|ObjectInspector
name|createLazyObjectInspector
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|byte
index|[]
name|separator
parameter_list|,
name|int
name|separatorIndex
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
throws|throws
name|SerDeException
block|{
name|ObjectInspector
operator|.
name|Category
name|c
init|=
name|typeInfo
operator|.
name|getCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
name|PRIMITIVE
case|:
return|return
name|LazyPrimitiveObjectInspectorFactory
operator|.
name|getLazyObjectInspector
argument_list|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
name|LazyObjectInspectorFactory
operator|.
name|getLazySimpleMapObjectInspector
argument_list|(
name|createLazyObjectInspector
argument_list|(
operator|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
argument_list|,
name|separator
argument_list|,
name|separatorIndex
operator|+
literal|2
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
argument_list|,
name|createLazyObjectInspector
argument_list|(
operator|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|,
name|separator
argument_list|,
name|separatorIndex
operator|+
literal|2
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
argument_list|,
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separator
argument_list|,
name|separatorIndex
argument_list|)
argument_list|,
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separator
argument_list|,
name|separatorIndex
operator|+
literal|1
argument_list|)
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
return|;
case|case
name|LIST
case|:
return|return
name|LazyObjectInspectorFactory
operator|.
name|getLazySimpleListObjectInspector
argument_list|(
name|createLazyObjectInspector
argument_list|(
operator|(
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|,
name|separator
argument_list|,
name|separatorIndex
operator|+
literal|1
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
argument_list|,
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separator
argument_list|,
name|separatorIndex
argument_list|)
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
return|;
case|case
name|STRUCT
case|:
name|StructTypeInfo
name|structTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypeInfos
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|fieldTypeInfos
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
name|fieldTypeInfos
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldObjectInspectors
operator|.
name|add
argument_list|(
name|createLazyObjectInspector
argument_list|(
name|fieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|separator
argument_list|,
name|separatorIndex
operator|+
literal|1
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|LazyObjectInspectorFactory
operator|.
name|getLazySimpleStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldObjectInspectors
argument_list|,
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separator
argument_list|,
name|separatorIndex
argument_list|)
argument_list|,
name|nullSequence
argument_list|,
literal|false
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
return|;
case|case
name|UNION
case|:
name|UnionTypeInfo
name|unionTypeInfo
init|=
operator|(
name|UnionTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|lazyOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|TypeInfo
name|uti
range|:
name|unionTypeInfo
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
control|)
block|{
name|lazyOIs
operator|.
name|add
argument_list|(
name|createLazyObjectInspector
argument_list|(
name|uti
argument_list|,
name|separator
argument_list|,
name|separatorIndex
operator|+
literal|1
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|LazyObjectInspectorFactory
operator|.
name|getLazyUnionObjectInspector
argument_list|(
name|lazyOIs
argument_list|,
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separator
argument_list|,
name|separatorIndex
argument_list|)
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive LazySerDe Internal error."
argument_list|)
throw|;
block|}
comment|/**    * Create a hierarchical ObjectInspector for LazyStruct with the given    * columnNames and columnTypeInfos.    *    * @param lastColumnTakesRest    *          whether the last column of the struct should take the rest of the    *          row if there are extra fields.    * @throws SerDeException    * @see LazyFactory#createLazyObjectInspector(TypeInfo, byte[], int, Text,    *      boolean, byte)    */
specifier|public
specifier|static
name|ObjectInspector
name|createLazyStructInspector
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|typeInfos
parameter_list|,
name|byte
index|[]
name|separators
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|lastColumnTakesRest
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
throws|throws
name|SerDeException
block|{
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|columnObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|typeInfos
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
name|typeInfos
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|columnObjectInspectors
operator|.
name|add
argument_list|(
name|LazyFactory
operator|.
name|createLazyObjectInspector
argument_list|(
name|typeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|separators
argument_list|,
literal|1
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|LazyObjectInspectorFactory
operator|.
name|getLazySimpleStructObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|columnObjectInspectors
argument_list|,
name|separators
index|[
literal|0
index|]
argument_list|,
name|nullSequence
argument_list|,
name|lastColumnTakesRest
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
return|;
block|}
comment|/**    * Create a hierarchical ObjectInspector for ColumnarStruct with the given    * columnNames and columnTypeInfos.    * @throws SerDeException    *    * @see LazyFactory#createLazyObjectInspector(TypeInfo, byte[], int, Text,    *      boolean, byte)    */
specifier|public
specifier|static
name|ObjectInspector
name|createColumnarStructInspector
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
parameter_list|,
name|byte
index|[]
name|separators
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
throws|throws
name|SerDeException
block|{
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|columnObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|columnTypes
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
name|columnTypes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|columnObjectInspectors
operator|.
name|add
argument_list|(
name|LazyFactory
operator|.
name|createLazyObjectInspector
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|separators
argument_list|,
literal|1
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getColumnarStructObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|columnObjectInspectors
argument_list|)
return|;
block|}
specifier|private
name|LazyFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

