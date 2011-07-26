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
name|lazybinary
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
name|lazybinary
operator|.
name|objectinspector
operator|.
name|LazyBinaryListObjectInspector
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
name|lazybinary
operator|.
name|objectinspector
operator|.
name|LazyBinaryMapObjectInspector
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
name|lazybinary
operator|.
name|objectinspector
operator|.
name|LazyBinaryStructObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|WritableBooleanObjectInspector
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
name|WritableByteObjectInspector
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
name|WritableDoubleObjectInspector
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
name|WritableFloatObjectInspector
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
name|WritableIntObjectInspector
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
name|WritableLongObjectInspector
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
name|WritableShortObjectInspector
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
name|WritableStringObjectInspector
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
name|WritableVoidObjectInspector
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

begin_comment
comment|/**  * LazyBinaryFactory.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|LazyBinaryFactory
block|{
comment|/**    * Create a lazy binary primitive class given the type name.    */
specifier|public
specifier|static
name|LazyBinaryPrimitive
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|createLazyBinaryPrimitiveClass
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
name|LazyBinaryBoolean
argument_list|(
operator|(
name|WritableBooleanObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|BYTE
case|:
return|return
operator|new
name|LazyBinaryByte
argument_list|(
operator|(
name|WritableByteObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
operator|new
name|LazyBinaryShort
argument_list|(
operator|(
name|WritableShortObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|INT
case|:
return|return
operator|new
name|LazyBinaryInteger
argument_list|(
operator|(
name|WritableIntObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
operator|new
name|LazyBinaryLong
argument_list|(
operator|(
name|WritableLongObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
operator|new
name|LazyBinaryFloat
argument_list|(
operator|(
name|WritableFloatObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|LazyBinaryDouble
argument_list|(
operator|(
name|WritableDoubleObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|STRING
case|:
return|return
operator|new
name|LazyBinaryString
argument_list|(
operator|(
name|WritableStringObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|VOID
case|:
comment|// for NULL
return|return
operator|new
name|LazyBinaryVoid
argument_list|(
operator|(
name|WritableVoidObjectInspector
operator|)
name|oi
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: no LazyBinaryObject for "
operator|+
name|p
argument_list|)
throw|;
block|}
block|}
comment|/**    * Create a hierarchical LazyBinaryObject based on the given typeInfo.    */
specifier|public
specifier|static
name|LazyBinaryObject
name|createLazyBinaryObject
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
name|createLazyBinaryPrimitiveClass
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
name|LazyBinaryMap
argument_list|(
operator|(
name|LazyBinaryMapObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|LIST
case|:
return|return
operator|new
name|LazyBinaryArray
argument_list|(
operator|(
name|LazyBinaryListObjectInspector
operator|)
name|oi
argument_list|)
return|;
case|case
name|STRUCT
case|:
return|return
operator|new
name|LazyBinaryStruct
argument_list|(
operator|(
name|LazyBinaryStructObjectInspector
operator|)
name|oi
argument_list|)
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive LazyBinarySerDe Internal error."
argument_list|)
throw|;
block|}
specifier|private
name|LazyBinaryFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
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
parameter_list|)
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
name|LazyBinaryUtils
operator|.
name|getLazyBinaryObjectInspectorFromTypeInfo
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
block|}
end_class

end_unit

