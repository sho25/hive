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
name|vector
operator|.
name|VectorizedExpressions
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
name|StringLength
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
name|lazy
operator|.
name|LazyBinary
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
name|typeinfo
operator|.
name|CharTypeInfo
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
name|BytesWritable
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

begin_comment
comment|/**  * GenericUDFLength.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"length"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str | binary) - Returns the length of str or number of bytes in binary data"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('Facebook') FROM src LIMIT 1;\n"
operator|+
literal|"  8"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|StringLength
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFLength
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|final
name|IntWritable
name|result
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|argumentOI
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspectorConverter
operator|.
name|StringConverter
name|stringConverter
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspectorConverter
operator|.
name|BinaryConverter
name|binaryConverter
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|isInputString
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|isInputFixedLength
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
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"LENGTH requires 1 argument, got "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"LENGTH only takes primitive types, got "
operator|+
name|argumentOI
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
name|argumentOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
name|inputType
init|=
name|argumentOI
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|ObjectInspector
name|outputOI
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|inputType
condition|)
block|{
case|case
name|CHAR
case|:
name|isInputFixedLength
operator|=
literal|true
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
operator|(
operator|(
name|CharTypeInfo
operator|)
name|argumentOI
operator|.
name|getTypeInfo
argument_list|()
operator|)
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
case|case
name|VARCHAR
case|:
case|case
name|STRING
case|:
name|isInputString
operator|=
literal|true
expr_stmt|;
name|stringConverter
operator|=
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|StringConverter
argument_list|(
name|argumentOI
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|isInputString
operator|=
literal|false
expr_stmt|;
name|binaryConverter
operator|=
operator|new
name|PrimitiveObjectInspectorConverter
operator|.
name|BinaryConverter
argument_list|(
name|argumentOI
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|" LENGTH() only takes STRING/CHAR/VARCHAR/BINARY types as first argument, got "
operator|+
name|inputType
argument_list|)
throw|;
block|}
name|outputOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
expr_stmt|;
return|return
name|outputOI
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
name|byte
index|[]
name|data
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|isInputString
condition|)
block|{
name|String
name|val
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|!=
literal|null
condition|)
block|{
name|val
operator|=
operator|(
name|String
operator|)
name|stringConverter
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
expr_stmt|;
block|}
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
comment|// For char, we do not need to explore the data
if|if
condition|(
name|isInputFixedLength
condition|)
block|{
return|return
name|result
return|;
block|}
name|data
operator|=
name|val
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|int
name|len
init|=
literal|0
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
name|data
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|GenericUDFUtils
operator|.
name|isUtfStartByte
argument_list|(
name|data
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|len
operator|++
expr_stmt|;
block|}
block|}
name|result
operator|.
name|set
argument_list|(
name|len
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
else|else
block|{
name|BytesWritable
name|val
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|!=
literal|null
condition|)
block|{
name|val
operator|=
operator|(
name|BytesWritable
operator|)
name|binaryConverter
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
expr_stmt|;
block|}
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
name|result
operator|.
name|set
argument_list|(
name|val
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
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
return|return
name|getStandardDisplayString
argument_list|(
literal|"length"
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

end_unit

