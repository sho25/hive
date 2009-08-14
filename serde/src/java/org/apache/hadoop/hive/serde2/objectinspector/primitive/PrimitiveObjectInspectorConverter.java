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
operator|.
name|primitive
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
name|serde2
operator|.
name|ByteStream
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
name|LazyInteger
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
name|LazyLong
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
name|io
operator|.
name|Text
import|;
end_import

begin_class
specifier|public
class|class
name|PrimitiveObjectInspectorConverter
block|{
comment|/**    * A converter for the byte type.    */
specifier|public
specifier|static
class|class
name|BooleanConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableBooleanObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|BooleanConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableBooleanObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
literal|false
argument_list|)
expr_stmt|;
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
try|try
block|{
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getBoolean
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the byte type.    */
specifier|public
specifier|static
class|class
name|ByteConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableByteObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|ByteConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableByteObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
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
try|try
block|{
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getByte
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the short type.    */
specifier|public
specifier|static
class|class
name|ShortConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableShortObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|ShortConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableShortObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|(
name|short
operator|)
literal|0
argument_list|)
expr_stmt|;
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
try|try
block|{
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getShort
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the int type.    */
specifier|public
specifier|static
class|class
name|IntConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableIntObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|IntConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableIntObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|(
name|int
operator|)
literal|0
argument_list|)
expr_stmt|;
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
try|try
block|{
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the long type.    */
specifier|public
specifier|static
class|class
name|LongConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableLongObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|LongConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableLongObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|(
name|long
operator|)
literal|0
argument_list|)
expr_stmt|;
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
try|try
block|{
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getLong
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the float type.    */
specifier|public
specifier|static
class|class
name|FloatConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableFloatObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|FloatConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableFloatObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|(
name|float
operator|)
literal|0
argument_list|)
expr_stmt|;
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
try|try
block|{
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getFloat
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the double type.    */
specifier|public
specifier|static
class|class
name|DoubleConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableDoubleObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|DoubleConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableDoubleObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|(
name|double
operator|)
literal|0
argument_list|)
expr_stmt|;
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
try|try
block|{
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A helper class to convert any primitive to Text.     */
specifier|public
specifier|static
class|class
name|TextConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|Text
name|t
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|ByteStream
operator|.
name|Output
name|out
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
specifier|static
name|byte
index|[]
name|trueBytes
init|=
block|{
literal|'T'
block|,
literal|'R'
block|,
literal|'U'
block|,
literal|'E'
block|}
decl_stmt|;
specifier|static
name|byte
index|[]
name|falseBytes
init|=
block|{
literal|'F'
block|,
literal|'A'
block|,
literal|'L'
block|,
literal|'S'
block|,
literal|'E'
block|}
decl_stmt|;
specifier|public
name|TextConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|)
block|{
comment|// The output ObjectInspector is writableStringObjectInspector.
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
block|}
specifier|public
name|Text
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
switch|switch
condition|(
name|inputOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|VOID
case|:
block|{
return|return
literal|null
return|;
block|}
case|case
name|BOOLEAN
case|:
block|{
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
condition|?
name|trueBytes
else|:
name|falseBytes
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
case|case
name|BYTE
case|:
block|{
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|LazyInteger
operator|.
name|writeUTF8NoException
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|ByteObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|set
argument_list|(
name|out
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|out
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
case|case
name|SHORT
case|:
block|{
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|LazyInteger
operator|.
name|writeUTF8NoException
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|ShortObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|set
argument_list|(
name|out
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|out
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
case|case
name|INT
case|:
block|{
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|LazyInteger
operator|.
name|writeUTF8NoException
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|IntObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|set
argument_list|(
name|out
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|out
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
case|case
name|LONG
case|:
block|{
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|LazyLong
operator|.
name|writeUTF8NoException
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|LongObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|set
argument_list|(
name|out
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|out
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
case|case
name|FLOAT
case|:
block|{
name|t
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|FloatObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
case|case
name|DOUBLE
case|:
block|{
name|t
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
case|case
name|STRING
case|:
block|{
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|StringObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
default|default:
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive 2 Internal error: type = "
operator|+
name|inputOI
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
comment|/**    * A helper class to convert any primitive to String.     */
specifier|public
specifier|static
class|class
name|StringConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|public
name|StringConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|)
block|{
comment|// The output ObjectInspector is writableStringObjectInspector.
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
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
return|return
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

