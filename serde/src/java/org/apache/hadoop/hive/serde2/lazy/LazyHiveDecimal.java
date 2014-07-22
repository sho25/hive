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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

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
name|charset
operator|.
name|CharacterCodingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|io
operator|.
name|Text
import|;
end_import

begin_class
specifier|public
class|class
name|LazyHiveDecimal
extends|extends
name|LazyPrimitive
argument_list|<
name|LazyHiveDecimalObjectInspector
argument_list|,
name|HiveDecimalWritable
argument_list|>
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|LazyHiveDecimal
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|precision
decl_stmt|;
specifier|private
specifier|final
name|int
name|scale
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|nullBytes
init|=
operator|new
name|byte
index|[]
block|{
literal|0x0
block|,
literal|0x0
block|,
literal|0x0
block|,
literal|0x0
block|}
decl_stmt|;
specifier|public
name|LazyHiveDecimal
parameter_list|(
name|LazyHiveDecimalObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|DecimalTypeInfo
name|typeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|oi
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|typeInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Decimal type used without type params"
argument_list|)
throw|;
block|}
name|precision
operator|=
name|typeInfo
operator|.
name|precision
argument_list|()
expr_stmt|;
name|scale
operator|=
name|typeInfo
operator|.
name|scale
argument_list|()
expr_stmt|;
name|data
operator|=
operator|new
name|HiveDecimalWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|LazyHiveDecimal
parameter_list|(
name|LazyHiveDecimal
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
name|precision
operator|=
name|copy
operator|.
name|precision
expr_stmt|;
name|scale
operator|=
name|copy
operator|.
name|scale
expr_stmt|;
name|data
operator|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|copy
operator|.
name|data
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initilizes LazyHiveDecimal object by interpreting the input bytes    * as a numeric string    *    * @param bytes    * @param start    * @param length    */
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|String
name|byteData
init|=
literal|null
decl_stmt|;
try|try
block|{
name|byteData
operator|=
name|Text
operator|.
name|decode
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
name|e
parameter_list|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Data not in the HiveDecimal data type range so converted to null."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|HiveDecimal
name|dec
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
name|byteData
argument_list|)
decl_stmt|;
name|dec
operator|=
name|enforcePrecisionScale
argument_list|(
name|dec
argument_list|)
expr_stmt|;
if|if
condition|(
name|dec
operator|!=
literal|null
condition|)
block|{
name|data
operator|.
name|set
argument_list|(
name|dec
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Data not in the HiveDecimal data type range so converted to null. Given data is :"
operator|+
name|byteData
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|private
name|HiveDecimal
name|enforcePrecisionScale
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|)
block|{
return|return
name|HiveDecimalUtils
operator|.
name|enforcePrecisionScale
argument_list|(
name|dec
argument_list|,
name|precision
argument_list|,
name|scale
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveDecimalWritable
name|getWritableObject
parameter_list|()
block|{
return|return
name|data
return|;
block|}
comment|/**    * Writes HiveDecimal object to output stream as string    * @param outputStream    * @param hiveDecimal    * @throws IOException    */
specifier|public
specifier|static
name|void
name|writeUTF8
parameter_list|(
name|OutputStream
name|outputStream
parameter_list|,
name|HiveDecimal
name|hiveDecimal
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|hiveDecimal
operator|==
literal|null
condition|)
block|{
name|outputStream
operator|.
name|write
argument_list|(
name|nullBytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ByteBuffer
name|b
init|=
name|Text
operator|.
name|encode
argument_list|(
name|hiveDecimal
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|outputStream
operator|.
name|write
argument_list|(
name|b
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

