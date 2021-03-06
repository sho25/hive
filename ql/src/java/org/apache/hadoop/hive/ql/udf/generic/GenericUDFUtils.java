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
name|lang
operator|.
name|reflect
operator|.
name|Array
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|ParameterizedType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Type
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
name|FunctionRegistry
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
name|exec
operator|.
name|UDFArgumentLengthException
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
name|UDFArgumentTypeException
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
name|plan
operator|.
name|ExprNodeDesc
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
name|HiveCharWritable
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
name|HiveVarcharWritable
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
name|ObjectInspectorConverters
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
name|ObjectInspectorConverters
operator|.
name|Converter
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
name|ObjectInspectorConverters
operator|.
name|IdentityConverter
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
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
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
name|typeinfo
operator|.
name|BaseCharTypeInfo
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
name|DecimalTypeInfo
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
name|TypeInfoFactory
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
name|TypeInfoUtils
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

begin_comment
comment|/**  * Util functions for GenericUDF classes.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|GenericUDFUtils
block|{
comment|/**    * Checks if b is the first byte of a UTF-8 character.    *    */
specifier|public
specifier|static
name|boolean
name|isUtfStartByte
parameter_list|(
name|byte
name|b
parameter_list|)
block|{
return|return
operator|(
name|b
operator|&
literal|0xC0
operator|)
operator|!=
literal|0x80
return|;
block|}
comment|/**    * This class helps to find the return ObjectInspector for a GenericUDF.    *    * In many cases like CASE and IF, the GenericUDF is returning a value out of    * several possibilities. However these possibilities may not always have the    * same ObjectInspector.    *    * This class will help detect whether all possibilities have exactly the same    * ObjectInspector. If not, then we need to convert the Objects to the same    * ObjectInspector.    *    * A special case is when some values are constant NULL. In this case we can    * use the same ObjectInspector.    */
specifier|public
specifier|static
class|class
name|ReturnObjectInspectorResolver
block|{
specifier|public
enum|enum
name|ConversionType
block|{
name|COMMON
block|,
name|UNION
block|,
name|COMPARISON
block|}
name|boolean
name|allowTypeConversion
decl_stmt|;
name|ObjectInspector
name|returnObjectInspector
decl_stmt|;
comment|// We create converters beforehand, so that the converters can reuse the
comment|// same object for returning conversion results.
name|HashMap
argument_list|<
name|ObjectInspector
argument_list|,
name|Converter
argument_list|>
name|converters
decl_stmt|;
specifier|public
name|ReturnObjectInspectorResolver
parameter_list|()
block|{
name|this
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReturnObjectInspectorResolver
parameter_list|(
name|boolean
name|allowTypeConversion
parameter_list|)
block|{
name|this
operator|.
name|allowTypeConversion
operator|=
name|allowTypeConversion
expr_stmt|;
block|}
comment|/**      * Update returnObjectInspector and valueInspectorsAreTheSame based on the      * ObjectInspector seen.      *      * @return false if there is a type mismatch      */
specifier|public
name|boolean
name|update
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
return|return
name|update
argument_list|(
name|oi
argument_list|,
name|ConversionType
operator|.
name|COMMON
argument_list|)
return|;
block|}
comment|/**      * Update returnObjectInspector and valueInspectorsAreTheSame based on the      * ObjectInspector seen for UnionAll.      *      * @return false if there is a type mismatch      */
specifier|public
name|boolean
name|updateForUnionAll
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
return|return
name|update
argument_list|(
name|oi
argument_list|,
name|ConversionType
operator|.
name|UNION
argument_list|)
return|;
block|}
comment|/**      * Update returnObjectInspector and valueInspectorsAreTheSame based on the      * ObjectInspector seen for comparison (for example GenericUDFIn).      *      * @return false if there is a type mismatch      */
specifier|public
name|boolean
name|updateForComparison
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
return|return
name|update
argument_list|(
name|oi
argument_list|,
name|ConversionType
operator|.
name|COMPARISON
argument_list|)
return|;
block|}
comment|/**      * Update returnObjectInspector and valueInspectorsAreTheSame based on the      * ObjectInspector seen.      *      * @return false if there is a type mismatch      */
specifier|private
name|boolean
name|update
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|ConversionType
name|conversionType
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
if|if
condition|(
name|oi
operator|instanceof
name|VoidObjectInspector
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|returnObjectInspector
operator|==
literal|null
condition|)
block|{
comment|// The first argument, just set the return to be the standard
comment|// writable version of this OI.
name|returnObjectInspector
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|oi
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
name|returnObjectInspector
operator|==
name|oi
condition|)
block|{
comment|// The new ObjectInspector is the same as the old one, directly return
comment|// true
return|return
literal|true
return|;
block|}
name|TypeInfo
name|oiTypeInfo
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|oi
argument_list|)
decl_stmt|;
name|TypeInfo
name|rTypeInfo
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|returnObjectInspector
argument_list|)
decl_stmt|;
if|if
condition|(
name|oiTypeInfo
operator|==
name|rTypeInfo
condition|)
block|{
comment|// Convert everything to writable, if types of arguments are the same,
comment|// but ObjectInspectors are different.
name|returnObjectInspector
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|returnObjectInspector
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
name|allowTypeConversion
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Types are different, we need to check whether we can convert them to
comment|// a common base class or not.
name|TypeInfo
name|commonTypeInfo
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|conversionType
condition|)
block|{
case|case
name|COMMON
case|:
name|commonTypeInfo
operator|=
name|FunctionRegistry
operator|.
name|getCommonClass
argument_list|(
name|oiTypeInfo
argument_list|,
name|rTypeInfo
argument_list|)
expr_stmt|;
break|break;
case|case
name|UNION
case|:
name|commonTypeInfo
operator|=
name|FunctionRegistry
operator|.
name|getCommonClassForUnionAll
argument_list|(
name|rTypeInfo
argument_list|,
name|oiTypeInfo
argument_list|)
expr_stmt|;
break|break;
case|case
name|COMPARISON
case|:
name|commonTypeInfo
operator|=
name|FunctionRegistry
operator|.
name|getCommonClassForComparison
argument_list|(
name|rTypeInfo
argument_list|,
name|oiTypeInfo
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|commonTypeInfo
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|returnObjectInspector
operator|=
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|commonTypeInfo
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/**      * Returns the ObjectInspector of the return value.      */
specifier|public
name|ObjectInspector
name|get
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaVoidObjectInspector
argument_list|)
return|;
block|}
specifier|public
name|ObjectInspector
name|get
parameter_list|(
name|ObjectInspector
name|defaultOI
parameter_list|)
block|{
return|return
name|returnObjectInspector
operator|!=
literal|null
condition|?
name|returnObjectInspector
else|:
name|defaultOI
return|;
block|}
specifier|public
name|Object
name|convertIfNecessary
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|)
block|{
return|return
name|convertIfNecessary
argument_list|(
name|o
argument_list|,
name|oi
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**      * Convert the return Object if necessary (when the ObjectInspectors of      * different possibilities are not all the same). If reuse is true,      * the result Object will be the same object as the last invocation      * (as long as the oi is the same)      */
specifier|public
name|Object
name|convertIfNecessary
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|,
name|boolean
name|reuse
parameter_list|)
block|{
name|Object
name|converted
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|oi
operator|==
name|returnObjectInspector
condition|)
block|{
name|converted
operator|=
name|o
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Converter
name|converter
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|reuse
condition|)
block|{
if|if
condition|(
name|converters
operator|==
literal|null
condition|)
block|{
name|converters
operator|=
operator|new
name|HashMap
argument_list|<
name|ObjectInspector
argument_list|,
name|Converter
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|converter
operator|=
name|converters
operator|.
name|get
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|converter
operator|==
literal|null
condition|)
block|{
name|converter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|oi
argument_list|,
name|returnObjectInspector
argument_list|)
expr_stmt|;
if|if
condition|(
name|reuse
condition|)
block|{
name|converters
operator|.
name|put
argument_list|(
name|oi
argument_list|,
name|converter
argument_list|)
expr_stmt|;
block|}
block|}
name|converted
operator|=
name|converter
operator|.
name|convert
argument_list|(
name|o
argument_list|)
expr_stmt|;
block|}
return|return
name|converted
return|;
block|}
block|}
comment|// Based on update() above.
specifier|public
specifier|static
name|TypeInfo
name|deriveInType
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|)
block|{
name|TypeInfo
name|returnType
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|node
range|:
name|children
control|)
block|{
name|TypeInfo
name|ti
init|=
name|node
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|ti
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|PRIMITIVE
operator|&&
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|ti
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveCategory
operator|.
name|VOID
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|returnType
operator|==
literal|null
condition|)
block|{
name|returnType
operator|=
name|ti
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|returnType
operator|==
name|ti
condition|)
continue|continue;
name|TypeInfo
name|commonTypeInfo
init|=
name|FunctionRegistry
operator|.
name|getCommonClassForComparison
argument_list|(
name|returnType
argument_list|,
name|ti
argument_list|)
decl_stmt|;
if|if
condition|(
name|commonTypeInfo
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|returnType
operator|=
name|commonTypeInfo
expr_stmt|;
block|}
return|return
name|returnType
return|;
block|}
comment|/**    * Convert parameters for the method if needed.    */
specifier|public
specifier|static
class|class
name|ConversionHelper
block|{
specifier|private
specifier|final
name|ObjectInspector
index|[]
name|givenParameterOIs
decl_stmt|;
name|Type
index|[]
name|methodParameterTypes
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isVariableLengthArgument
decl_stmt|;
name|Type
name|lastParaElementType
decl_stmt|;
name|boolean
name|conversionNeeded
decl_stmt|;
name|Converter
index|[]
name|converters
decl_stmt|;
name|Object
index|[]
name|convertedParameters
decl_stmt|;
name|Object
index|[]
name|convertedParametersInArray
decl_stmt|;
specifier|private
specifier|static
name|Class
argument_list|<
name|?
argument_list|>
name|getClassFromType
parameter_list|(
name|Type
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|Class
argument_list|<
name|?
argument_list|>
condition|)
block|{
return|return
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|t
return|;
block|}
elseif|else
if|if
condition|(
name|t
operator|instanceof
name|ParameterizedType
condition|)
block|{
name|ParameterizedType
name|pt
init|=
operator|(
name|ParameterizedType
operator|)
name|t
decl_stmt|;
return|return
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|pt
operator|.
name|getRawType
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * Create a PrimitiveConversionHelper for Method m. The ObjectInspector's      * input parameters are specified in parameters.      */
specifier|public
name|ConversionHelper
parameter_list|(
name|Method
name|m
parameter_list|,
name|ObjectInspector
index|[]
name|parameterOIs
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|givenParameterOIs
operator|=
name|parameterOIs
expr_stmt|;
name|methodParameterTypes
operator|=
name|m
operator|.
name|getGenericParameterTypes
argument_list|()
expr_stmt|;
comment|// Whether the method takes an array like Object[],
comment|// or String[] etc in the last argument.
name|lastParaElementType
operator|=
name|TypeInfoUtils
operator|.
name|getArrayElementType
argument_list|(
name|methodParameterTypes
operator|.
name|length
operator|==
literal|0
condition|?
literal|null
else|:
name|methodParameterTypes
index|[
name|methodParameterTypes
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
name|isVariableLengthArgument
operator|=
operator|(
name|lastParaElementType
operator|!=
literal|null
operator|)
expr_stmt|;
comment|// Create the output OI array
name|ObjectInspector
index|[]
name|methodParameterOIs
init|=
operator|new
name|ObjectInspector
index|[
name|parameterOIs
operator|.
name|length
index|]
decl_stmt|;
if|if
condition|(
name|isVariableLengthArgument
condition|)
block|{
comment|// ConversionHelper can be called without method parameter length
comment|// checkings
comment|// for terminatePartial() and merge() calls.
if|if
condition|(
name|parameterOIs
operator|.
name|length
operator|<
name|methodParameterTypes
operator|.
name|length
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
name|m
operator|.
name|toString
argument_list|()
operator|+
literal|" requires at least "
operator|+
operator|(
name|methodParameterTypes
operator|.
name|length
operator|-
literal|1
operator|)
operator|+
literal|" arguments but only "
operator|+
name|parameterOIs
operator|.
name|length
operator|+
literal|" are passed in."
argument_list|)
throw|;
block|}
comment|// Copy the first methodParameterTypes.length - 1 entries
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|methodParameterTypes
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
comment|// This method takes Object, so it accepts whatever types that are
comment|// passed in.
if|if
condition|(
name|methodParameterTypes
index|[
name|i
index|]
operator|==
name|Object
operator|.
name|class
condition|)
block|{
name|methodParameterOIs
index|[
name|i
index|]
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|parameterOIs
index|[
name|i
index|]
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|methodParameterOIs
index|[
name|i
index|]
operator|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|methodParameterTypes
index|[
name|i
index|]
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Deal with the last entry
if|if
condition|(
name|lastParaElementType
operator|==
name|Object
operator|.
name|class
condition|)
block|{
comment|// This method takes Object[], so it accepts whatever types that are
comment|// passed in.
for|for
control|(
name|int
name|i
init|=
name|methodParameterTypes
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|<
name|parameterOIs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|methodParameterOIs
index|[
name|i
index|]
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|parameterOIs
index|[
name|i
index|]
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// This method takes something like String[], so it only accepts
comment|// something like String
name|ObjectInspector
name|oi
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|lastParaElementType
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|methodParameterTypes
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|<
name|parameterOIs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|methodParameterOIs
index|[
name|i
index|]
operator|=
name|oi
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// Normal case, the last parameter is a normal parameter.
comment|// ConversionHelper can be called without method parameter length
comment|// checkings
comment|// for terminatePartial() and merge() calls.
if|if
condition|(
name|methodParameterTypes
operator|.
name|length
operator|!=
name|parameterOIs
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
name|m
operator|.
name|toString
argument_list|()
operator|+
literal|" requires "
operator|+
name|methodParameterTypes
operator|.
name|length
operator|+
literal|" arguments but "
operator|+
name|parameterOIs
operator|.
name|length
operator|+
literal|" are passed in."
argument_list|)
throw|;
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
name|methodParameterTypes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// This method takes Object, so it accepts whatever types that are
comment|// passed in.
if|if
condition|(
name|methodParameterTypes
index|[
name|i
index|]
operator|==
name|Object
operator|.
name|class
condition|)
block|{
name|methodParameterOIs
index|[
name|i
index|]
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|parameterOIs
index|[
name|i
index|]
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|methodParameterOIs
index|[
name|i
index|]
operator|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|methodParameterTypes
index|[
name|i
index|]
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Create the converters
name|conversionNeeded
operator|=
literal|false
expr_stmt|;
name|converters
operator|=
operator|new
name|Converter
index|[
name|parameterOIs
operator|.
name|length
index|]
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
name|parameterOIs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Converter
name|pc
init|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|parameterOIs
index|[
name|i
index|]
argument_list|,
name|methodParameterOIs
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|converters
index|[
name|i
index|]
operator|=
name|pc
expr_stmt|;
comment|// Conversion is needed?
name|conversionNeeded
operator|=
name|conversionNeeded
operator|||
operator|(
operator|!
operator|(
name|pc
operator|instanceof
name|IdentityConverter
operator|)
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|isVariableLengthArgument
condition|)
block|{
name|convertedParameters
operator|=
operator|new
name|Object
index|[
name|methodParameterTypes
operator|.
name|length
index|]
expr_stmt|;
name|convertedParametersInArray
operator|=
operator|(
name|Object
index|[]
operator|)
name|Array
operator|.
name|newInstance
argument_list|(
name|getClassFromType
argument_list|(
name|lastParaElementType
argument_list|)
argument_list|,
name|parameterOIs
operator|.
name|length
operator|-
name|methodParameterTypes
operator|.
name|length
operator|+
literal|1
argument_list|)
expr_stmt|;
name|convertedParameters
index|[
name|convertedParameters
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|convertedParametersInArray
expr_stmt|;
block|}
else|else
block|{
name|convertedParameters
operator|=
operator|new
name|Object
index|[
name|parameterOIs
operator|.
name|length
index|]
expr_stmt|;
block|}
block|}
specifier|public
name|Object
index|[]
name|convertIfNecessary
parameter_list|(
name|Object
modifier|...
name|parameters
parameter_list|)
block|{
assert|assert
operator|(
name|parameters
operator|.
name|length
operator|==
name|givenParameterOIs
operator|.
name|length
operator|)
assert|;
if|if
condition|(
operator|!
name|conversionNeeded
operator|&&
operator|!
name|isVariableLengthArgument
condition|)
block|{
comment|// no conversion needed, and not variable-length argument:
comment|// just return what is passed in.
return|return
name|parameters
return|;
block|}
if|if
condition|(
name|isVariableLengthArgument
condition|)
block|{
comment|// convert the first methodParameterTypes.length - 1 entries
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|methodParameterTypes
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|convertedParameters
index|[
name|i
index|]
operator|=
name|converters
index|[
name|i
index|]
operator|.
name|convert
argument_list|(
name|parameters
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|// convert the rest and put into the last entry
for|for
control|(
name|int
name|i
init|=
name|methodParameterTypes
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|<
name|parameters
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|convertedParametersInArray
index|[
name|i
operator|+
literal|1
operator|-
name|methodParameterTypes
operator|.
name|length
index|]
operator|=
name|converters
index|[
name|i
index|]
operator|.
name|convert
argument_list|(
name|parameters
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// normal case, convert all parameters
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|methodParameterTypes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|convertedParameters
index|[
name|i
index|]
operator|=
name|converters
index|[
name|i
index|]
operator|.
name|convert
argument_list|(
name|parameters
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|convertedParameters
return|;
block|}
block|}
empty_stmt|;
comment|/**    * Helper class for UDFs returning string/varchar/char    */
specifier|public
specifier|static
class|class
name|StringHelper
block|{
specifier|protected
name|Object
name|returnValue
decl_stmt|;
specifier|protected
name|PrimitiveCategory
name|type
decl_stmt|;
specifier|public
name|StringHelper
parameter_list|(
name|PrimitiveCategory
name|type
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|STRING
case|:
name|returnValue
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
name|returnValue
operator|=
operator|new
name|HiveCharWritable
argument_list|()
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
name|returnValue
operator|=
operator|new
name|HiveVarcharWritable
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"Unexpected non-string type "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Object
name|setReturnValue
parameter_list|(
name|String
name|val
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|STRING
case|:
operator|(
operator|(
name|Text
operator|)
name|returnValue
operator|)
operator|.
name|set
argument_list|(
name|val
argument_list|)
expr_stmt|;
return|return
name|returnValue
return|;
case|case
name|CHAR
case|:
operator|(
operator|(
name|HiveCharWritable
operator|)
name|returnValue
operator|)
operator|.
name|set
argument_list|(
name|val
argument_list|)
expr_stmt|;
return|return
name|returnValue
return|;
case|case
name|VARCHAR
case|:
operator|(
operator|(
name|HiveVarcharWritable
operator|)
name|returnValue
operator|)
operator|.
name|set
argument_list|(
name|val
argument_list|)
expr_stmt|;
return|return
name|returnValue
return|;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"Bad return type "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
comment|/**      * Helper function to help GenericUDFs determine the return type      * character length for char/varchar.      * @param poi PrimitiveObjectInspector representing the type      * @return character length of the type      * @throws UDFArgumentException      */
specifier|public
specifier|static
name|int
name|getFixedStringSizeForType
parameter_list|(
name|PrimitiveObjectInspector
name|poi
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
comment|// TODO: we can support date, int, .. any types which would have a fixed length value
switch|switch
condition|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
name|BaseCharTypeInfo
name|typeInfo
init|=
operator|(
name|BaseCharTypeInfo
operator|)
name|poi
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
return|return
name|typeInfo
operator|.
name|getLength
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"No fixed size for type "
operator|+
name|poi
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Return an ordinal from an integer.    */
specifier|public
specifier|static
name|String
name|getOrdinal
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|int
name|unit
init|=
name|i
operator|%
literal|10
decl_stmt|;
return|return
operator|(
name|i
operator|<=
literal|0
operator|)
condition|?
literal|""
else|:
operator|(
name|i
operator|!=
literal|11
operator|&&
name|unit
operator|==
literal|1
operator|)
condition|?
name|i
operator|+
literal|"st"
else|:
operator|(
name|i
operator|!=
literal|12
operator|&&
name|unit
operator|==
literal|2
operator|)
condition|?
name|i
operator|+
literal|"nd"
else|:
operator|(
name|i
operator|!=
literal|13
operator|&&
name|unit
operator|==
literal|3
operator|)
condition|?
name|i
operator|+
literal|"rd"
else|:
name|i
operator|+
literal|"th"
return|;
block|}
comment|/**    * Finds any occurence of<code>subtext</code> from<code>text</code> in the    * backing buffer.    */
specifier|public
specifier|static
name|int
name|findText
parameter_list|(
name|Text
name|text
parameter_list|,
name|Text
name|subtext
parameter_list|,
name|int
name|start
parameter_list|)
block|{
comment|// src.position(start) can't accept negative numbers.
name|int
name|length
init|=
name|text
operator|.
name|getLength
argument_list|()
operator|-
name|start
decl_stmt|;
if|if
condition|(
name|start
operator|<
literal|0
operator|||
name|length
operator|<
literal|0
operator|||
name|length
operator|<
name|subtext
operator|.
name|getLength
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|subtext
operator|.
name|getLength
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|String
name|textString
init|=
name|text
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|subtextString
init|=
name|subtext
operator|.
name|toString
argument_list|()
decl_stmt|;
name|int
name|index
init|=
name|textString
operator|.
name|indexOf
argument_list|(
name|subtextString
argument_list|,
name|start
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|index
return|;
block|}
else|else
block|{
return|return
name|textString
operator|.
name|codePointCount
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
return|;
block|}
block|}
specifier|private
name|GenericUDFUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

