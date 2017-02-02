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
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
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
comment|// Set the HiveDecimalWritable from bytes without converting to String first for
comment|// better performance.
name|data
operator|.
name|setFromBytes
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
if|if
condition|(
operator|!
name|data
operator|.
name|isSet
argument_list|()
condition|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|isNull
operator|=
operator|!
name|data
operator|.
name|mutateEnforcePrecisionScale
argument_list|(
name|precision
argument_list|,
name|scale
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isNull
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Data not in the HiveDecimal data type range so converted to null. Given data is :"
operator|+
operator|new
name|String
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
parameter_list|,
name|int
name|scale
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
name|byte
index|[]
name|scratchBuffer
init|=
operator|new
name|byte
index|[
name|HiveDecimal
operator|.
name|SCRATCH_BUFFER_LEN_TO_BYTES
index|]
decl_stmt|;
name|int
name|index
init|=
name|hiveDecimal
operator|.
name|toFormatBytes
argument_list|(
name|scale
argument_list|,
name|scratchBuffer
argument_list|)
decl_stmt|;
name|outputStream
operator|.
name|write
argument_list|(
name|scratchBuffer
argument_list|,
name|index
argument_list|,
name|scratchBuffer
operator|.
name|length
operator|-
name|index
argument_list|)
expr_stmt|;
block|}
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
parameter_list|,
name|int
name|scale
parameter_list|,
name|byte
index|[]
name|scratchBuffer
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
name|int
name|index
init|=
name|hiveDecimal
operator|.
name|toFormatBytes
argument_list|(
name|scale
argument_list|,
name|scratchBuffer
argument_list|)
decl_stmt|;
name|outputStream
operator|.
name|write
argument_list|(
name|scratchBuffer
argument_list|,
name|index
argument_list|,
name|scratchBuffer
operator|.
name|length
operator|-
name|index
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Writes HiveDecimalWritable object to output stream as string    * @param outputStream    * @param hiveDecimal    * @throws IOException    */
specifier|public
specifier|static
name|void
name|writeUTF8
parameter_list|(
name|OutputStream
name|outputStream
parameter_list|,
name|HiveDecimalWritable
name|hiveDecimalWritable
parameter_list|,
name|int
name|scale
parameter_list|,
name|byte
index|[]
name|scratchBuffer
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|hiveDecimalWritable
operator|==
literal|null
operator|||
operator|!
name|hiveDecimalWritable
operator|.
name|isSet
argument_list|()
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
name|int
name|index
init|=
name|hiveDecimalWritable
operator|.
name|toFormatBytes
argument_list|(
name|scale
argument_list|,
name|scratchBuffer
argument_list|)
decl_stmt|;
name|outputStream
operator|.
name|write
argument_list|(
name|scratchBuffer
argument_list|,
name|index
argument_list|,
name|scratchBuffer
operator|.
name|length
operator|-
name|index
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

