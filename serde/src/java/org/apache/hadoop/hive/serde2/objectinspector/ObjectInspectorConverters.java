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
name|objectinspector
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
name|Map
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
name|JavaStringObjectInspector
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
name|PrimitiveObjectInspectorConverter
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
name|PrimitiveObjectInspectorFactory
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
name|SettableBinaryObjectInspector
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
name|SettableBooleanObjectInspector
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
name|SettableByteObjectInspector
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
name|SettableDateObjectInspector
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
name|SettableDoubleObjectInspector
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
name|SettableFloatObjectInspector
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
name|SettableHiveDecimalObjectInspector
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
name|SettableIntObjectInspector
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
name|SettableLongObjectInspector
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
name|SettableShortObjectInspector
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
name|SettableTimestampObjectInspector
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
name|VoidObjectInspector
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

begin_comment
comment|/**  * ObjectInspectorConverters.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ObjectInspectorConverters
block|{
comment|/**    * A converter which will convert objects with one ObjectInspector to another.    */
specifier|public
specifier|static
interface|interface
name|Converter
block|{
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
function_decl|;
block|}
comment|/**    * IdentityConverter.    *    */
specifier|public
specifier|static
class|class
name|IdentityConverter
implements|implements
name|Converter
block|{
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
return|return
name|input
return|;
block|}
block|}
specifier|private
specifier|static
name|Converter
name|getConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|PrimitiveObjectInspector
name|outputOI
parameter_list|)
block|{
switch|switch
condition|(
name|outputOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|BooleanConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableBooleanObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|BYTE
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|ByteConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableByteObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|ShortConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableShortObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|INT
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|IntConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableIntObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|LongConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableLongObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|FloatConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableFloatObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|DoubleConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableDoubleObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|STRING
case|:
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableStringObjectInspector
condition|)
block|{
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|TextConverter
argument_list|(
name|inputOI
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|JavaStringObjectInspector
condition|)
block|{
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|StringConverter
argument_list|(
name|inputOI
argument_list|)
return|;
block|}
case|case
name|DATE
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|DateConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableDateObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|TimestampConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableTimestampObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|BINARY
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|BinaryConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableBinaryObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|DECIMAL
case|:
return|return
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|HiveDecimalConverter
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|inputOI
argument_list|,
operator|(
name|SettableHiveDecimalObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive internal error: conversion of "
operator|+
name|inputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" to "
operator|+
name|outputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" not supported yet."
argument_list|)
throw|;
block|}
block|}
comment|/**    * Returns a converter that converts objects from one OI to another OI. The    * returned (converted) object belongs to this converter, so that it can be    * reused across different calls.    */
specifier|public
specifier|static
name|Converter
name|getConverter
parameter_list|(
name|ObjectInspector
name|inputOI
parameter_list|,
name|ObjectInspector
name|outputOI
parameter_list|)
block|{
comment|// If the inputOI is the same as the outputOI, just return an
comment|// IdentityConverter.
if|if
condition|(
name|inputOI
operator|.
name|equals
argument_list|(
name|outputOI
argument_list|)
condition|)
block|{
return|return
operator|new
name|IdentityConverter
argument_list|()
return|;
block|}
comment|// TODO: Add support for UNION once SettableUnionObjectInspector is implemented.
switch|switch
condition|(
name|outputOI
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
return|return
name|getConverter
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|inputOI
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|STRUCT
case|:
return|return
operator|new
name|StructConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableStructObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|LIST
case|:
return|return
operator|new
name|ListConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableListObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
operator|new
name|MapConverter
argument_list|(
name|inputOI
argument_list|,
operator|(
name|SettableMapObjectInspector
operator|)
name|outputOI
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive internal error: conversion of "
operator|+
name|inputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" to "
operator|+
name|outputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" not supported yet."
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|ObjectInspector
name|getConvertedOI
parameter_list|(
name|ObjectInspector
name|inputOI
parameter_list|,
name|ObjectInspector
name|outputOI
parameter_list|,
name|boolean
name|equalsCheck
parameter_list|)
block|{
comment|// If the inputOI is the same as the outputOI, just return it
if|if
condition|(
name|equalsCheck
operator|&&
name|inputOI
operator|.
name|equals
argument_list|(
name|outputOI
argument_list|)
condition|)
block|{
return|return
name|outputOI
return|;
block|}
comment|// Return the settable equivalent object inspector for primitive categories
comment|// For eg: for table T containing partitions p1 and p2 (possibly different
comment|// from the table T), return the settable inspector for T. The inspector for
comment|// T is settable recursively i.e all the nested fields are also settable.
comment|// TODO: Add support for UNION once SettableUnionObjectInspector is implemented.
switch|switch
condition|(
name|outputOI
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
name|PrimitiveObjectInspector
name|primInputOI
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|inputOI
decl_stmt|;
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|primInputOI
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
return|;
case|case
name|STRUCT
case|:
name|StructObjectInspector
name|structOutputOI
init|=
operator|(
name|StructObjectInspector
operator|)
name|outputOI
decl_stmt|;
if|if
condition|(
name|structOutputOI
operator|.
name|isSettable
argument_list|()
condition|)
block|{
return|return
name|outputOI
return|;
block|}
else|else
block|{
comment|// create a standard settable struct object inspector
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|listFields
init|=
name|structOutputOI
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|structFieldNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|listFields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|listFields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|StructField
name|listField
range|:
name|listFields
control|)
block|{
name|structFieldNames
operator|.
name|add
argument_list|(
name|listField
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|structFieldObjectInspectors
operator|.
name|add
argument_list|(
name|getConvertedOI
argument_list|(
name|listField
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|listField
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|)
return|;
block|}
case|case
name|LIST
case|:
name|ListObjectInspector
name|listOutputOI
init|=
operator|(
name|ListObjectInspector
operator|)
name|outputOI
decl_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|listOutputOI
operator|.
name|getListElementObjectInspector
argument_list|()
argument_list|)
return|;
case|case
name|MAP
case|:
name|MapObjectInspector
name|mapOutputOI
init|=
operator|(
name|MapObjectInspector
operator|)
name|outputOI
decl_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardMapObjectInspector
argument_list|(
name|mapOutputOI
operator|.
name|getMapKeyObjectInspector
argument_list|()
argument_list|,
name|mapOutputOI
operator|.
name|getMapValueObjectInspector
argument_list|()
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive internal error: conversion of "
operator|+
name|inputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" to "
operator|+
name|outputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" not supported yet."
argument_list|)
throw|;
block|}
block|}
comment|/**    * A converter class for List.    */
specifier|public
specifier|static
class|class
name|ListConverter
implements|implements
name|Converter
block|{
name|ListObjectInspector
name|inputOI
decl_stmt|;
name|SettableListObjectInspector
name|outputOI
decl_stmt|;
name|ObjectInspector
name|inputElementOI
decl_stmt|;
name|ObjectInspector
name|outputElementOI
decl_stmt|;
name|ArrayList
argument_list|<
name|Converter
argument_list|>
name|elementConverters
decl_stmt|;
name|Object
name|output
decl_stmt|;
specifier|public
name|ListConverter
parameter_list|(
name|ObjectInspector
name|inputOI
parameter_list|,
name|SettableListObjectInspector
name|outputOI
parameter_list|)
block|{
if|if
condition|(
name|inputOI
operator|instanceof
name|ListObjectInspector
condition|)
block|{
name|this
operator|.
name|inputOI
operator|=
operator|(
name|ListObjectInspector
operator|)
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|inputElementOI
operator|=
name|this
operator|.
name|inputOI
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
name|outputElementOI
operator|=
name|outputOI
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
name|output
operator|=
name|outputOI
operator|.
name|create
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|elementConverters
operator|=
operator|new
name|ArrayList
argument_list|<
name|Converter
argument_list|>
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|inputOI
operator|instanceof
name|VoidObjectInspector
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive internal error: conversion of "
operator|+
name|inputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" to "
operator|+
name|outputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"not supported yet."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Create enough elementConverters
comment|// NOTE: we have to have a separate elementConverter for each element,
comment|// because the elementConverters can reuse the internal object.
comment|// So it's not safe to use the same elementConverter to convert multiple
comment|// elements.
name|int
name|size
init|=
name|inputOI
operator|.
name|getListLength
argument_list|(
name|input
argument_list|)
decl_stmt|;
while|while
condition|(
name|elementConverters
operator|.
name|size
argument_list|()
operator|<
name|size
condition|)
block|{
name|elementConverters
operator|.
name|add
argument_list|(
name|getConverter
argument_list|(
name|inputElementOI
argument_list|,
name|outputElementOI
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Convert the elements
name|outputOI
operator|.
name|resize
argument_list|(
name|output
argument_list|,
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|size
condition|;
name|index
operator|++
control|)
block|{
name|Object
name|inputElement
init|=
name|inputOI
operator|.
name|getListElement
argument_list|(
name|input
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|Object
name|outputElement
init|=
name|elementConverters
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|convert
argument_list|(
name|inputElement
argument_list|)
decl_stmt|;
name|outputOI
operator|.
name|set
argument_list|(
name|output
argument_list|,
name|index
argument_list|,
name|outputElement
argument_list|)
expr_stmt|;
block|}
return|return
name|output
return|;
block|}
block|}
comment|/**    * A converter class for Struct.    */
specifier|public
specifier|static
class|class
name|StructConverter
implements|implements
name|Converter
block|{
name|StructObjectInspector
name|inputOI
decl_stmt|;
name|SettableStructObjectInspector
name|outputOI
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|inputFields
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|outputFields
decl_stmt|;
name|ArrayList
argument_list|<
name|Converter
argument_list|>
name|fieldConverters
decl_stmt|;
name|Object
name|output
decl_stmt|;
specifier|public
name|StructConverter
parameter_list|(
name|ObjectInspector
name|inputOI
parameter_list|,
name|SettableStructObjectInspector
name|outputOI
parameter_list|)
block|{
if|if
condition|(
name|inputOI
operator|instanceof
name|StructObjectInspector
condition|)
block|{
name|this
operator|.
name|inputOI
operator|=
operator|(
name|StructObjectInspector
operator|)
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|inputFields
operator|=
name|this
operator|.
name|inputOI
operator|.
name|getAllStructFieldRefs
argument_list|()
expr_stmt|;
name|outputFields
operator|=
name|outputOI
operator|.
name|getAllStructFieldRefs
argument_list|()
expr_stmt|;
comment|// If the output has some extra fields, set them to NULL.
name|int
name|minFields
init|=
name|Math
operator|.
name|min
argument_list|(
name|inputFields
operator|.
name|size
argument_list|()
argument_list|,
name|outputFields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|fieldConverters
operator|=
operator|new
name|ArrayList
argument_list|<
name|Converter
argument_list|>
argument_list|(
name|minFields
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
name|minFields
condition|;
name|f
operator|++
control|)
block|{
name|fieldConverters
operator|.
name|add
argument_list|(
name|getConverter
argument_list|(
name|inputFields
operator|.
name|get
argument_list|(
name|f
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|outputFields
operator|.
name|get
argument_list|(
name|f
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|output
operator|=
name|outputOI
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|inputOI
operator|instanceof
name|VoidObjectInspector
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive internal error: conversion of "
operator|+
name|inputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" to "
operator|+
name|outputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"not supported yet."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|minFields
init|=
name|Math
operator|.
name|min
argument_list|(
name|inputFields
operator|.
name|size
argument_list|()
argument_list|,
name|outputFields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// Convert the fields
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
name|minFields
condition|;
name|f
operator|++
control|)
block|{
name|Object
name|inputFieldValue
init|=
name|inputOI
operator|.
name|getStructFieldData
argument_list|(
name|input
argument_list|,
name|inputFields
operator|.
name|get
argument_list|(
name|f
argument_list|)
argument_list|)
decl_stmt|;
name|Object
name|outputFieldValue
init|=
name|fieldConverters
operator|.
name|get
argument_list|(
name|f
argument_list|)
operator|.
name|convert
argument_list|(
name|inputFieldValue
argument_list|)
decl_stmt|;
name|outputOI
operator|.
name|setStructFieldData
argument_list|(
name|output
argument_list|,
name|outputFields
operator|.
name|get
argument_list|(
name|f
argument_list|)
argument_list|,
name|outputFieldValue
argument_list|)
expr_stmt|;
block|}
comment|// set the extra fields to null
for|for
control|(
name|int
name|f
init|=
name|minFields
init|;
name|f
operator|<
name|outputFields
operator|.
name|size
argument_list|()
condition|;
name|f
operator|++
control|)
block|{
name|outputOI
operator|.
name|setStructFieldData
argument_list|(
name|output
argument_list|,
name|outputFields
operator|.
name|get
argument_list|(
name|f
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|output
return|;
block|}
block|}
comment|/**    * A converter class for Map.    */
specifier|public
specifier|static
class|class
name|MapConverter
implements|implements
name|Converter
block|{
name|MapObjectInspector
name|inputOI
decl_stmt|;
name|SettableMapObjectInspector
name|outputOI
decl_stmt|;
name|ObjectInspector
name|inputKeyOI
decl_stmt|;
name|ObjectInspector
name|outputKeyOI
decl_stmt|;
name|ObjectInspector
name|inputValueOI
decl_stmt|;
name|ObjectInspector
name|outputValueOI
decl_stmt|;
name|ArrayList
argument_list|<
name|Converter
argument_list|>
name|keyConverters
decl_stmt|;
name|ArrayList
argument_list|<
name|Converter
argument_list|>
name|valueConverters
decl_stmt|;
name|Object
name|output
decl_stmt|;
specifier|public
name|MapConverter
parameter_list|(
name|ObjectInspector
name|inputOI
parameter_list|,
name|SettableMapObjectInspector
name|outputOI
parameter_list|)
block|{
if|if
condition|(
name|inputOI
operator|instanceof
name|MapObjectInspector
condition|)
block|{
name|this
operator|.
name|inputOI
operator|=
operator|(
name|MapObjectInspector
operator|)
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|inputKeyOI
operator|=
name|this
operator|.
name|inputOI
operator|.
name|getMapKeyObjectInspector
argument_list|()
expr_stmt|;
name|outputKeyOI
operator|=
name|outputOI
operator|.
name|getMapKeyObjectInspector
argument_list|()
expr_stmt|;
name|inputValueOI
operator|=
name|this
operator|.
name|inputOI
operator|.
name|getMapValueObjectInspector
argument_list|()
expr_stmt|;
name|outputValueOI
operator|=
name|outputOI
operator|.
name|getMapValueObjectInspector
argument_list|()
expr_stmt|;
name|keyConverters
operator|=
operator|new
name|ArrayList
argument_list|<
name|Converter
argument_list|>
argument_list|()
expr_stmt|;
name|valueConverters
operator|=
operator|new
name|ArrayList
argument_list|<
name|Converter
argument_list|>
argument_list|()
expr_stmt|;
name|output
operator|=
name|outputOI
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|inputOI
operator|instanceof
name|VoidObjectInspector
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive internal error: conversion of "
operator|+
name|inputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" to "
operator|+
name|outputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"not supported yet."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Create enough keyConverters/valueConverters
comment|// NOTE: we have to have a separate key/valueConverter for each key/value,
comment|// because the key/valueConverters can reuse the internal object.
comment|// So it's not safe to use the same key/valueConverter to convert multiple
comment|// key/values.
comment|// NOTE: This code tries to get all key-value pairs out of the map.
comment|// It's not very efficient. The more efficient way should be to let MapOI
comment|// return an Iterator. This is currently not supported by MapOI yet.
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|map
init|=
name|inputOI
operator|.
name|getMap
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|map
operator|.
name|size
argument_list|()
decl_stmt|;
while|while
condition|(
name|keyConverters
operator|.
name|size
argument_list|()
operator|<
name|size
condition|)
block|{
name|keyConverters
operator|.
name|add
argument_list|(
name|getConverter
argument_list|(
name|inputKeyOI
argument_list|,
name|outputKeyOI
argument_list|)
argument_list|)
expr_stmt|;
name|valueConverters
operator|.
name|add
argument_list|(
name|getConverter
argument_list|(
name|inputValueOI
argument_list|,
name|outputValueOI
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// CLear the output
name|outputOI
operator|.
name|clear
argument_list|(
name|output
argument_list|)
expr_stmt|;
comment|// Convert the key/value pairs
name|int
name|entryID
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Object
name|inputKey
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Object
name|inputValue
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Object
name|outputKey
init|=
name|keyConverters
operator|.
name|get
argument_list|(
name|entryID
argument_list|)
operator|.
name|convert
argument_list|(
name|inputKey
argument_list|)
decl_stmt|;
name|Object
name|outputValue
init|=
name|valueConverters
operator|.
name|get
argument_list|(
name|entryID
argument_list|)
operator|.
name|convert
argument_list|(
name|inputValue
argument_list|)
decl_stmt|;
name|entryID
operator|++
expr_stmt|;
name|outputOI
operator|.
name|put
argument_list|(
name|output
argument_list|,
name|outputKey
argument_list|,
name|outputValue
argument_list|)
expr_stmt|;
block|}
return|return
name|output
return|;
block|}
block|}
specifier|private
name|ObjectInspectorConverters
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

