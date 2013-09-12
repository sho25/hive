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
operator|.
name|objectinspector
operator|.
name|primitive
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
name|HashMap
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
name|PrimitiveObjectInspectorUtils
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
name|BaseTypeParams
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
name|PrimitiveTypeSpec
import|;
end_import

begin_comment
comment|/**  * LazyPrimitiveObjectInspectorFactory is the primary way to create new  * ObjectInspector instances.  *  * SerDe classes should call the static functions in this library to create an  * ObjectInspector to return to the caller of SerDe2.getObjectInspector().  *  * The reason of having caches here is that ObjectInspector is because  * ObjectInspectors do not have an internal state - so ObjectInspectors with the  * same construction parameters should result in exactly the same  * ObjectInspector.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|LazyPrimitiveObjectInspectorFactory
block|{
specifier|public
specifier|static
specifier|final
name|LazyBooleanObjectInspector
name|LAZY_BOOLEAN_OBJECT_INSPECTOR
init|=
operator|new
name|LazyBooleanObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyByteObjectInspector
name|LAZY_BYTE_OBJECT_INSPECTOR
init|=
operator|new
name|LazyByteObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyShortObjectInspector
name|LAZY_SHORT_OBJECT_INSPECTOR
init|=
operator|new
name|LazyShortObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyIntObjectInspector
name|LAZY_INT_OBJECT_INSPECTOR
init|=
operator|new
name|LazyIntObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyLongObjectInspector
name|LAZY_LONG_OBJECT_INSPECTOR
init|=
operator|new
name|LazyLongObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyFloatObjectInspector
name|LAZY_FLOAT_OBJECT_INSPECTOR
init|=
operator|new
name|LazyFloatObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyDoubleObjectInspector
name|LAZY_DOUBLE_OBJECT_INSPECTOR
init|=
operator|new
name|LazyDoubleObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyVoidObjectInspector
name|LAZY_VOID_OBJECT_INSPECTOR
init|=
operator|new
name|LazyVoidObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyDateObjectInspector
name|LAZY_DATE_OBJECT_INSPECTOR
init|=
operator|new
name|LazyDateObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyTimestampObjectInspector
name|LAZY_TIMESTAMP_OBJECT_INSPECTOR
init|=
operator|new
name|LazyTimestampObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyBinaryObjectInspector
name|LAZY_BINARY_OBJECT_INSPECTOR
init|=
operator|new
name|LazyBinaryObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|LazyHiveDecimalObjectInspector
name|LAZY_BIG_DECIMAL_OBJECT_INSPECTOR
init|=
operator|new
name|LazyHiveDecimalObjectInspector
argument_list|()
decl_stmt|;
specifier|static
name|HashMap
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazyStringObjectInspector
argument_list|>
name|cachedLazyStringObjectInspector
init|=
operator|new
name|HashMap
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazyStringObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|LazyStringObjectInspector
name|getLazyStringObjectInspector
parameter_list|(
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|signature
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|Boolean
operator|.
name|valueOf
argument_list|(
name|escaped
argument_list|)
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
name|escapeChar
argument_list|)
argument_list|)
expr_stmt|;
name|LazyStringObjectInspector
name|result
init|=
name|cachedLazyStringObjectInspector
operator|.
name|get
argument_list|(
name|signature
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|LazyStringObjectInspector
argument_list|(
name|escaped
argument_list|,
name|escapeChar
argument_list|)
expr_stmt|;
name|cachedLazyStringObjectInspector
operator|.
name|put
argument_list|(
name|signature
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|static
name|PrimitiveObjectInspectorUtils
operator|.
name|ParameterizedObjectInspectorMap
name|cachedParameterizedLazyObjectInspectors
init|=
operator|new
name|PrimitiveObjectInspectorUtils
operator|.
name|ParameterizedObjectInspectorMap
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|PrimitiveObjectInspector
name|getParameterizedObjectInspector
parameter_list|(
name|PrimitiveTypeSpec
name|typeSpec
parameter_list|)
block|{
name|PrimitiveCategory
name|primitiveCategory
init|=
name|typeSpec
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|BaseTypeParams
name|typeParams
init|=
name|typeSpec
operator|.
name|getTypeParams
argument_list|()
decl_stmt|;
name|PrimitiveObjectInspector
name|poi
init|=
name|cachedParameterizedLazyObjectInspectors
operator|.
name|getObjectInspector
argument_list|(
name|typeSpec
argument_list|)
decl_stmt|;
if|if
condition|(
name|poi
operator|==
literal|null
condition|)
block|{
comment|// Object inspector hasn't been cached for this type/params yet, create now
switch|switch
condition|(
name|primitiveCategory
condition|)
block|{
comment|// Get type entry for parameterized type, and create new object inspector for type
comment|// Currently no parameterized types
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Primitve type "
operator|+
name|primitiveCategory
operator|+
literal|" should not take parameters"
argument_list|)
throw|;
block|}
block|}
return|return
name|poi
return|;
block|}
specifier|public
specifier|static
name|AbstractPrimitiveLazyObjectInspector
argument_list|<
name|?
argument_list|>
name|getLazyObjectInspector
parameter_list|(
name|PrimitiveCategory
name|primitiveCategory
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
switch|switch
condition|(
name|primitiveCategory
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
name|LAZY_BOOLEAN_OBJECT_INSPECTOR
return|;
case|case
name|BYTE
case|:
return|return
name|LAZY_BYTE_OBJECT_INSPECTOR
return|;
case|case
name|SHORT
case|:
return|return
name|LAZY_SHORT_OBJECT_INSPECTOR
return|;
case|case
name|INT
case|:
return|return
name|LAZY_INT_OBJECT_INSPECTOR
return|;
case|case
name|LONG
case|:
return|return
name|LAZY_LONG_OBJECT_INSPECTOR
return|;
case|case
name|FLOAT
case|:
return|return
name|LAZY_FLOAT_OBJECT_INSPECTOR
return|;
case|case
name|DOUBLE
case|:
return|return
name|LAZY_DOUBLE_OBJECT_INSPECTOR
return|;
case|case
name|STRING
case|:
return|return
name|getLazyStringObjectInspector
argument_list|(
name|escaped
argument_list|,
name|escapeChar
argument_list|)
return|;
case|case
name|BINARY
case|:
return|return
name|LAZY_BINARY_OBJECT_INSPECTOR
return|;
case|case
name|VOID
case|:
return|return
name|LAZY_VOID_OBJECT_INSPECTOR
return|;
case|case
name|DATE
case|:
return|return
name|LAZY_DATE_OBJECT_INSPECTOR
return|;
case|case
name|TIMESTAMP
case|:
return|return
name|LAZY_TIMESTAMP_OBJECT_INSPECTOR
return|;
case|case
name|DECIMAL
case|:
return|return
name|LAZY_BIG_DECIMAL_OBJECT_INSPECTOR
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: Cannot find ObjectInspector "
operator|+
literal|" for "
operator|+
name|primitiveCategory
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|AbstractPrimitiveLazyObjectInspector
argument_list|<
name|?
argument_list|>
name|getLazyObjectInspector
parameter_list|(
name|PrimitiveTypeSpec
name|typeSpec
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
name|PrimitiveCategory
name|primitiveCategory
init|=
name|typeSpec
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|BaseTypeParams
name|typeParams
init|=
name|typeSpec
operator|.
name|getTypeParams
argument_list|()
decl_stmt|;
if|if
condition|(
name|typeParams
operator|==
literal|null
condition|)
block|{
return|return
name|getLazyObjectInspector
argument_list|(
name|primitiveCategory
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|)
return|;
block|}
else|else
block|{
switch|switch
condition|(
name|primitiveCategory
condition|)
block|{
comment|// call getParameterizedObjectInspector(). But no parameterized types yet
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Type "
operator|+
name|primitiveCategory
operator|+
literal|" does not take parameters"
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|LazyPrimitiveObjectInspectorFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

