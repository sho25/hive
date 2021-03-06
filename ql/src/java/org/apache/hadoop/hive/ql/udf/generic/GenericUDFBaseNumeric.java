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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|NoMatchingMethodException
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
name|ql
operator|.
name|session
operator|.
name|SessionState
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
name|ByteWritable
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
name|DoubleWritable
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
name|HiveDecimalWritable
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
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
operator|.
name|PrimitiveGrouping
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
name|HiveDecimalUtils
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
name|FloatWritable
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
name|IntWritable
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
name|LongWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|HiveCompat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|HiveCompat
operator|.
name|CompatLevel
import|;
end_import

begin_comment
comment|/**  * GenericUDF Base Class for operations.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"op"
argument_list|,
name|value
operator|=
literal|"a op b - Returns the result of operation"
argument_list|)
specifier|public
specifier|abstract
class|class
name|GenericUDFBaseNumeric
extends|extends
name|GenericUDFBaseBinary
block|{
specifier|protected
specifier|transient
name|PrimitiveObjectInspector
name|leftOI
decl_stmt|;
specifier|protected
specifier|transient
name|PrimitiveObjectInspector
name|rightOI
decl_stmt|;
specifier|protected
specifier|transient
name|PrimitiveObjectInspector
name|resultOI
decl_stmt|;
specifier|protected
specifier|transient
name|Converter
name|converterLeft
decl_stmt|;
specifier|protected
specifier|transient
name|Converter
name|converterRight
decl_stmt|;
specifier|protected
name|ByteWritable
name|byteWritable
init|=
operator|new
name|ByteWritable
argument_list|()
decl_stmt|;
specifier|protected
name|ShortWritable
name|shortWritable
init|=
operator|new
name|ShortWritable
argument_list|()
decl_stmt|;
specifier|protected
name|IntWritable
name|intWritable
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|protected
name|LongWritable
name|longWritable
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|protected
name|FloatWritable
name|floatWritable
init|=
operator|new
name|FloatWritable
argument_list|()
decl_stmt|;
specifier|protected
name|DoubleWritable
name|doubleWritable
init|=
operator|new
name|DoubleWritable
argument_list|()
decl_stmt|;
specifier|protected
name|HiveDecimalWritable
name|decimalWritable
init|=
operator|new
name|HiveDecimalWritable
argument_list|()
decl_stmt|;
specifier|protected
name|boolean
name|confLookupNeeded
init|=
literal|true
decl_stmt|;
specifier|protected
name|boolean
name|ansiSqlArithmetic
init|=
literal|false
decl_stmt|;
specifier|public
name|GenericUDFBaseNumeric
parameter_list|()
block|{   }
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
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|opName
operator|+
literal|" requires two arguments."
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|Category
name|category
init|=
name|arguments
index|[
name|i
index|]
operator|.
name|getCategory
argument_list|()
decl_stmt|;
if|if
condition|(
name|category
operator|!=
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"The "
operator|+
name|GenericUDFUtils
operator|.
name|getOrdinal
argument_list|(
name|i
operator|+
literal|1
argument_list|)
operator|+
literal|" argument of "
operator|+
name|opName
operator|+
literal|"  is expected to a "
operator|+
name|Category
operator|.
name|PRIMITIVE
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|" type, but "
operator|+
name|category
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|" is found"
argument_list|)
throw|;
block|}
block|}
comment|// During map/reduce tasks, there may not be a valid HiveConf from the SessionState.
comment|// So lookup and save any needed conf information during query compilation in the Hive conf
comment|// (where there should be valid HiveConf from SessionState).  Plan serialization will ensure
comment|// we have access to these values in the map/reduce tasks.
if|if
condition|(
name|confLookupNeeded
condition|)
block|{
name|CompatLevel
name|compatLevel
init|=
name|HiveCompat
operator|.
name|getCompatLevel
argument_list|(
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|ansiSqlArithmetic
operator|=
name|compatLevel
operator|.
name|ordinal
argument_list|()
operator|>
name|CompatLevel
operator|.
name|HIVE_0_12
operator|.
name|ordinal
argument_list|()
expr_stmt|;
name|confLookupNeeded
operator|=
literal|false
expr_stmt|;
block|}
name|leftOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
name|rightOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
expr_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|deriveResultTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|converterLeft
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|leftOI
argument_list|,
name|resultOI
argument_list|)
expr_stmt|;
name|converterRight
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|rightOI
argument_list|,
name|resultOI
argument_list|)
expr_stmt|;
return|return
name|resultOI
return|;
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
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|==
literal|null
operator|||
name|arguments
index|[
literal|1
index|]
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Object
name|left
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
name|Object
name|right
init|=
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|left
operator|==
literal|null
operator|&&
name|right
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Handle decimal separately.
if|if
condition|(
name|resultOI
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveCategory
operator|.
name|DECIMAL
condition|)
block|{
name|HiveDecimal
name|hdLeft
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveDecimal
argument_list|(
name|left
argument_list|,
name|leftOI
argument_list|)
decl_stmt|;
name|HiveDecimal
name|hdRight
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveDecimal
argument_list|(
name|right
argument_list|,
name|rightOI
argument_list|)
decl_stmt|;
if|if
condition|(
name|hdLeft
operator|==
literal|null
operator|||
name|hdRight
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HiveDecimalWritable
name|result
init|=
name|evaluate
argument_list|(
name|hdLeft
argument_list|,
name|hdRight
argument_list|)
decl_stmt|;
return|return
name|resultOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|result
argument_list|)
return|;
block|}
name|left
operator|=
name|converterLeft
operator|.
name|convert
argument_list|(
name|left
argument_list|)
expr_stmt|;
if|if
condition|(
name|left
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|right
operator|=
name|converterRight
operator|.
name|convert
argument_list|(
name|right
argument_list|)
expr_stmt|;
if|if
condition|(
name|right
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
name|resultOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BYTE
case|:
return|return
name|evaluate
argument_list|(
operator|(
name|ByteWritable
operator|)
name|left
argument_list|,
operator|(
name|ByteWritable
operator|)
name|right
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
name|evaluate
argument_list|(
operator|(
name|ShortWritable
operator|)
name|left
argument_list|,
operator|(
name|ShortWritable
operator|)
name|right
argument_list|)
return|;
case|case
name|INT
case|:
return|return
name|evaluate
argument_list|(
operator|(
name|IntWritable
operator|)
name|left
argument_list|,
operator|(
name|IntWritable
operator|)
name|right
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
name|evaluate
argument_list|(
operator|(
name|LongWritable
operator|)
name|left
argument_list|,
operator|(
name|LongWritable
operator|)
name|right
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
name|evaluate
argument_list|(
operator|(
name|FloatWritable
operator|)
name|left
argument_list|,
operator|(
name|FloatWritable
operator|)
name|right
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
name|evaluate
argument_list|(
operator|(
name|DoubleWritable
operator|)
name|left
argument_list|,
operator|(
name|DoubleWritable
operator|)
name|right
argument_list|)
return|;
default|default:
comment|// Should never happen.
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected type in evaluating "
operator|+
name|opName
operator|+
literal|": "
operator|+
name|resultOI
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|ByteWritable
name|evaluate
parameter_list|(
name|ByteWritable
name|left
parameter_list|,
name|ByteWritable
name|right
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|protected
name|ShortWritable
name|evaluate
parameter_list|(
name|ShortWritable
name|left
parameter_list|,
name|ShortWritable
name|right
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|protected
name|IntWritable
name|evaluate
parameter_list|(
name|IntWritable
name|left
parameter_list|,
name|IntWritable
name|right
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|protected
name|LongWritable
name|evaluate
parameter_list|(
name|LongWritable
name|left
parameter_list|,
name|LongWritable
name|right
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|protected
name|FloatWritable
name|evaluate
parameter_list|(
name|FloatWritable
name|left
parameter_list|,
name|FloatWritable
name|right
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|protected
name|DoubleWritable
name|evaluate
parameter_list|(
name|DoubleWritable
name|left
parameter_list|,
name|DoubleWritable
name|right
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|protected
name|HiveDecimalWritable
name|evaluate
parameter_list|(
name|HiveDecimal
name|left
parameter_list|,
name|HiveDecimal
name|right
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Default implementation for deriving typeinfo instance for the operator result.    *    * @param leftOI TypeInfo instance of the left operand    * @param rightOI TypeInfo instance of the right operand    * @return    * @throws UDFArgumentException    */
specifier|private
name|PrimitiveTypeInfo
name|deriveResultTypeInfo
parameter_list|()
throws|throws
name|UDFArgumentException
block|{
name|PrimitiveTypeInfo
name|left
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|leftOI
argument_list|)
decl_stmt|;
name|PrimitiveTypeInfo
name|right
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|rightOI
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isNumericType
argument_list|(
name|left
argument_list|)
operator|||
operator|!
name|FunctionRegistry
operator|.
name|isNumericType
argument_list|(
name|right
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|argTypeInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|argTypeInfos
operator|.
name|add
argument_list|(
name|left
argument_list|)
expr_stmt|;
name|argTypeInfos
operator|.
name|add
argument_list|(
name|right
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NoMatchingMethodException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
name|argTypeInfos
argument_list|,
literal|null
argument_list|)
throw|;
block|}
comment|// If any of the type isn't exact, double is chosen.
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isExactNumericType
argument_list|(
name|left
argument_list|)
operator|||
operator|!
name|FunctionRegistry
operator|.
name|isExactNumericType
argument_list|(
name|right
argument_list|)
condition|)
block|{
return|return
name|deriveResultApproxTypeInfo
argument_list|()
return|;
block|}
return|return
name|deriveResultExactTypeInfo
argument_list|()
return|;
block|}
comment|/**    * Default implementation for getting the approximate type info for the operator result.    * Divide operator overrides this.    * @return    */
specifier|protected
name|PrimitiveTypeInfo
name|deriveResultApproxTypeInfo
parameter_list|()
block|{
name|PrimitiveTypeInfo
name|left
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|leftOI
argument_list|)
decl_stmt|;
name|PrimitiveTypeInfo
name|right
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|rightOI
argument_list|)
decl_stmt|;
comment|// string types get converted to double
if|if
condition|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|left
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
operator|==
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
condition|)
block|{
name|left
operator|=
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
expr_stmt|;
block|}
if|if
condition|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|right
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
operator|==
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
condition|)
block|{
name|right
operator|=
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
expr_stmt|;
block|}
comment|// Use type promotion
name|PrimitiveCategory
name|commonCat
init|=
name|FunctionRegistry
operator|.
name|getPrimitiveCommonCategory
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
decl_stmt|;
if|if
condition|(
name|commonCat
operator|==
name|PrimitiveCategory
operator|.
name|DECIMAL
condition|)
block|{
comment|// Hive 0.12 behavior where double * decimal -> decimal is gone.
return|return
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
return|;
block|}
elseif|else
if|if
condition|(
name|commonCat
operator|==
literal|null
condition|)
block|{
return|return
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
return|;
block|}
else|else
block|{
return|return
name|left
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|commonCat
condition|?
name|left
else|:
name|right
return|;
block|}
block|}
comment|/**    * Default implementation for getting the exact type info for the operator result. It worked for all    * but divide operator.    *    * @return    */
specifier|protected
name|PrimitiveTypeInfo
name|deriveResultExactTypeInfo
parameter_list|()
block|{
name|PrimitiveTypeInfo
name|left
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|leftOI
argument_list|)
decl_stmt|;
name|PrimitiveTypeInfo
name|right
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|rightOI
argument_list|)
decl_stmt|;
comment|// Now we are handling exact types. Base implementation handles type promotion.
name|PrimitiveCategory
name|commonCat
init|=
name|FunctionRegistry
operator|.
name|getPrimitiveCommonCategory
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
decl_stmt|;
if|if
condition|(
name|commonCat
operator|==
name|PrimitiveCategory
operator|.
name|DECIMAL
condition|)
block|{
return|return
name|deriveResultDecimalTypeInfo
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|left
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|commonCat
condition|?
name|left
else|:
name|right
return|;
block|}
block|}
comment|/**    * Derive the object inspector instance for the decimal result of the operator.    */
specifier|protected
name|DecimalTypeInfo
name|deriveResultDecimalTypeInfo
parameter_list|()
block|{
name|int
name|prec1
init|=
name|leftOI
operator|.
name|precision
argument_list|()
decl_stmt|;
name|int
name|prec2
init|=
name|rightOI
operator|.
name|precision
argument_list|()
decl_stmt|;
name|int
name|scale1
init|=
name|leftOI
operator|.
name|scale
argument_list|()
decl_stmt|;
name|int
name|scale2
init|=
name|rightOI
operator|.
name|scale
argument_list|()
decl_stmt|;
return|return
name|deriveResultDecimalTypeInfo
argument_list|(
name|prec1
argument_list|,
name|scale1
argument_list|,
name|prec2
argument_list|,
name|scale2
argument_list|)
return|;
block|}
specifier|protected
specifier|abstract
name|DecimalTypeInfo
name|deriveResultDecimalTypeInfo
parameter_list|(
name|int
name|prec1
parameter_list|,
name|int
name|scale1
parameter_list|,
name|int
name|prec2
parameter_list|,
name|int
name|scale2
parameter_list|)
function_decl|;
specifier|public
specifier|static
specifier|final
name|int
name|MINIMUM_ADJUSTED_SCALE
init|=
literal|6
decl_stmt|;
comment|/**    * Create DecimalTypeInfo from input precision/scale, adjusting if necessary to fit max precision    * @param precision precision value before adjustment    * @param scale scale value before adjustment    * @return    */
specifier|protected
name|DecimalTypeInfo
name|adjustPrecScale
parameter_list|(
name|int
name|precision
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
comment|// Assumptions:
comment|// precision>= scale
comment|// scale>= 0
if|if
condition|(
name|precision
operator|<=
name|HiveDecimal
operator|.
name|MAX_PRECISION
condition|)
block|{
comment|// Adjustment only needed when we exceed max precision
return|return
operator|new
name|DecimalTypeInfo
argument_list|(
name|precision
argument_list|,
name|scale
argument_list|)
return|;
block|}
comment|// Precision/scale exceed maximum precision. Result must be adjusted to HiveDecimal.MAX_PRECISION.
comment|// See https://blogs.msdn.microsoft.com/sqlprogrammability/2006/03/29/multiplication-and-division-with-numerics/
name|int
name|intDigits
init|=
name|precision
operator|-
name|scale
decl_stmt|;
comment|// If original scale less than 6, use original scale value; otherwise preserve at least 6 fractional digits
name|int
name|minScaleValue
init|=
name|Math
operator|.
name|min
argument_list|(
name|scale
argument_list|,
name|MINIMUM_ADJUSTED_SCALE
argument_list|)
decl_stmt|;
name|int
name|adjustedScale
init|=
name|HiveDecimal
operator|.
name|MAX_PRECISION
operator|-
name|intDigits
decl_stmt|;
name|adjustedScale
operator|=
name|Math
operator|.
name|max
argument_list|(
name|adjustedScale
argument_list|,
name|minScaleValue
argument_list|)
expr_stmt|;
return|return
operator|new
name|DecimalTypeInfo
argument_list|(
name|HiveDecimal
operator|.
name|MAX_PRECISION
argument_list|,
name|adjustedScale
argument_list|)
return|;
block|}
specifier|public
name|void
name|copyToNewInstance
parameter_list|(
name|Object
name|newInstance
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|super
operator|.
name|copyToNewInstance
argument_list|(
name|newInstance
argument_list|)
expr_stmt|;
name|GenericUDFBaseNumeric
name|other
init|=
operator|(
name|GenericUDFBaseNumeric
operator|)
name|newInstance
decl_stmt|;
name|other
operator|.
name|confLookupNeeded
operator|=
name|this
operator|.
name|confLookupNeeded
expr_stmt|;
name|other
operator|.
name|ansiSqlArithmetic
operator|=
name|this
operator|.
name|ansiSqlArithmetic
expr_stmt|;
block|}
specifier|public
name|boolean
name|isConfLookupNeeded
parameter_list|()
block|{
return|return
name|confLookupNeeded
return|;
block|}
specifier|public
name|void
name|setConfLookupNeeded
parameter_list|(
name|boolean
name|confLookupNeeded
parameter_list|)
block|{
name|this
operator|.
name|confLookupNeeded
operator|=
name|confLookupNeeded
expr_stmt|;
block|}
specifier|public
name|boolean
name|isAnsiSqlArithmetic
parameter_list|()
block|{
return|return
name|ansiSqlArithmetic
return|;
block|}
specifier|public
name|void
name|setAnsiSqlArithmetic
parameter_list|(
name|boolean
name|ansiSqlArithmetic
parameter_list|)
block|{
name|this
operator|.
name|ansiSqlArithmetic
operator|=
name|ansiSqlArithmetic
expr_stmt|;
block|}
block|}
end_class

end_unit

