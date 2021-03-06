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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|CharBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharacterCodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharsetEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CodingErrorAction
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
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
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
name|ConstantObjectInspector
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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"encode"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str, str) - Encode the first argument using the second argument character set"
argument_list|,
name|extended
operator|=
literal|"Possible options for the character set are 'US-ASCII', 'ISO-8859-1',\n"
operator|+
literal|"'UTF-8', 'UTF-16BE', 'UTF-16LE', and 'UTF-16'. If either argument\n"
operator|+
literal|"is null, the result will also be null"
argument_list|)
specifier|public
class|class
name|GenericUDFEncode
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|CharsetEncoder
name|encoder
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|stringOI
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|charsetOI
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|BytesWritable
name|result
init|=
operator|new
name|BytesWritable
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
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"Encode() requires exactly two arguments"
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
name|Category
operator|.
name|PRIMITIVE
operator|||
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
operator|!=
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"The first argument to Encode() must be a string/varchar"
argument_list|)
throw|;
block|}
name|stringOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
name|arguments
index|[
literal|1
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|Category
operator|.
name|PRIMITIVE
operator|||
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
operator|!=
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"The second argument to Encode() must be a string/varchar"
argument_list|)
throw|;
block|}
name|charsetOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
expr_stmt|;
comment|// If the character set for encoding is constant, we can optimize that
if|if
condition|(
name|charsetOI
operator|instanceof
name|ConstantObjectInspector
condition|)
block|{
name|String
name|charSetName
init|=
operator|(
operator|(
name|ConstantObjectInspector
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
name|toString
argument_list|()
decl_stmt|;
name|encoder
operator|=
name|Charset
operator|.
name|forName
argument_list|(
name|charSetName
argument_list|)
operator|.
name|newEncoder
argument_list|()
operator|.
name|onMalformedInput
argument_list|(
name|CodingErrorAction
operator|.
name|REPORT
argument_list|)
operator|.
name|onUnmappableCharacter
argument_list|(
name|CodingErrorAction
operator|.
name|REPORT
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
operator|new
name|BytesWritable
argument_list|()
expr_stmt|;
return|return
operator|(
name|ObjectInspector
operator|)
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
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
name|String
name|value
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|stringOI
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ByteBuffer
name|encoded
decl_stmt|;
if|if
condition|(
name|encoder
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|encoded
operator|=
name|encoder
operator|.
name|encode
argument_list|(
name|CharBuffer
operator|.
name|wrap
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|encoded
operator|=
name|Charset
operator|.
name|forName
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|charsetOI
argument_list|)
argument_list|)
operator|.
name|encode
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|setSize
argument_list|(
name|encoded
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
name|encoded
operator|.
name|get
argument_list|(
name|result
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|encoded
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
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
operator|==
literal|2
operator|)
assert|;
return|return
name|getStandardDisplayString
argument_list|(
literal|"encode"
argument_list|,
name|children
argument_list|,
literal|","
argument_list|)
return|;
block|}
block|}
end_class

end_unit

