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
name|math
operator|.
name|BigDecimal
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
name|Formatter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IllegalFormatConversionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|serde
operator|.
name|serdeConstants
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
name|WritableHiveDecimalObjectInspector
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Generic UDF for printf function  *<code>printf(String format, Obj... args)</code>.  *  * @see org.apache.hadoop.hive.ql.udf.generic.GenericUDF  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"printf"
argument_list|,
name|value
operator|=
literal|"_FUNC_(String format, Obj... args) - "
operator|+
literal|"function that can format strings according to printf-style format strings"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(\"Hello World %d %s\", 100, \"days\")"
operator|+
literal|"FROM src LIMIT 1;\n"
operator|+
literal|"  \"Hello World 100 days\""
argument_list|)
specifier|public
class|class
name|GenericUDFPrintf
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|ObjectInspector
index|[]
name|argumentOIs
decl_stmt|;
specifier|protected
specifier|transient
name|Converter
name|converterFormat
decl_stmt|;
specifier|private
specifier|final
name|Text
name|resultText
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
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
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The function PRINTF(String format, Obj... args) needs at least one arguments."
argument_list|)
throw|;
block|}
name|WritableStringObjectInspector
name|resultOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
operator|)
decl_stmt|;
if|if
condition|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|STRING
operator|||
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|CHAR
operator|||
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|VARCHAR
operator|||
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|VOID
condition|)
block|{
name|converterFormat
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|0
index|]
argument_list|,
name|resultOI
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Argument 1"
operator|+
literal|" of function PRINTF must be \""
operator|+
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
operator|+
literal|"\", but \""
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" was found."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Argument 1"
operator|+
literal|" of function PRINTF must be \""
operator|+
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
operator|+
literal|"\", but \""
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" was found."
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|arguments
index|[
name|i
index|]
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|PRIMITIVE
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"Argument "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|" of function PRINTF must be \""
operator|+
name|Category
operator|.
name|PRIMITIVE
operator|+
literal|"\", but \""
operator|+
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" was found."
argument_list|)
throw|;
block|}
block|}
name|argumentOIs
operator|=
name|arguments
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
comment|// If the first argument is null, return null. (It's okay for other arguments to be null, in
comment|// which case, "null" will be printed.)
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|Formatter
name|formatter
init|=
operator|new
name|Formatter
argument_list|(
name|sb
argument_list|,
name|Locale
operator|.
name|US
argument_list|)
decl_stmt|;
name|Text
name|pattern
init|=
operator|(
name|Text
operator|)
name|converterFormat
operator|.
name|convert
argument_list|(
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|argumentList
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|argumentOIs
index|[
name|i
index|]
decl_stmt|;
switch|switch
condition|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
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
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
case|case
name|STRING
case|:
case|case
name|TIMESTAMP
case|:
name|argumentList
operator|.
name|add
argument_list|(
name|poi
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|arguments
index|[
name|i
index|]
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
comment|// Decimal classes cannot be converted by printf, so convert them to doubles.
name|Object
name|obj
init|=
name|poi
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|arguments
index|[
name|i
index|]
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|obj
operator|instanceof
name|HiveDecimal
condition|)
block|{
name|obj
operator|=
operator|(
operator|(
name|HiveDecimal
operator|)
name|obj
operator|)
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|BigDecimal
condition|)
block|{
name|obj
operator|=
operator|(
operator|(
name|BigDecimal
operator|)
name|obj
operator|)
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
name|argumentList
operator|.
name|add
argument_list|(
name|obj
argument_list|)
expr_stmt|;
break|break;
default|default:
name|argumentList
operator|.
name|add
argument_list|(
name|arguments
index|[
name|i
index|]
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|formatter
operator|.
name|format
argument_list|(
name|pattern
operator|.
name|toString
argument_list|()
argument_list|,
name|argumentList
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
name|resultText
operator|.
name|set
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|formatter
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|resultText
return|;
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
assert|assert
operator|(
name|children
operator|.
name|length
operator|>=
literal|2
operator|)
assert|;
return|return
name|getStandardDisplayString
argument_list|(
literal|"printf"
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

end_unit

