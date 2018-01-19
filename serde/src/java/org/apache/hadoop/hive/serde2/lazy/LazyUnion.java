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
name|serde2
operator|.
name|lazy
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
name|lazy
operator|.
name|objectinspector
operator|.
name|LazyUnionObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyObject for storing a union. The field of a union can be primitive or  * non-primitive.  *  */
end_comment

begin_class
specifier|public
class|class
name|LazyUnion
extends|extends
name|LazyNonPrimitive
argument_list|<
name|LazyUnionObjectInspector
argument_list|>
block|{
comment|/**    * Whether the data is already parsed or not.    */
specifier|private
name|boolean
name|parsed
decl_stmt|;
comment|/**    * The start position of union field. Only valid when the data is parsed.    */
specifier|private
name|int
name|startPosition
decl_stmt|;
comment|/**    * The object of the union.    */
specifier|private
name|Object
name|field
decl_stmt|;
comment|/**    * Tag of the Union    */
specifier|private
name|byte
name|tag
decl_stmt|;
comment|/**    * Whether init() has been called on the field or not.    */
specifier|private
name|boolean
name|fieldInited
init|=
literal|false
decl_stmt|;
comment|/**    * Whether the field has been set or not    * */
specifier|private
name|boolean
name|fieldSet
init|=
literal|false
decl_stmt|;
comment|/**    * Construct a LazyUnion object with the ObjectInspector.    */
specifier|public
name|LazyUnion
parameter_list|(
name|LazyUnionObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
comment|// exceptional use case for avro
specifier|public
name|LazyUnion
parameter_list|(
name|LazyUnionObjectInspector
name|oi
parameter_list|,
name|byte
name|tag
parameter_list|,
name|Object
name|field
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
name|fieldSet
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Set the row data for this LazyUnion.    *    * @see LazyObject#init(ByteArrayRef, int, int)    */
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
name|super
operator|.
name|init
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|parsed
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Parse the byte[] and fill each field.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|void
name|parse
parameter_list|()
block|{
name|byte
name|separator
init|=
name|oi
operator|.
name|getSeparator
argument_list|()
decl_stmt|;
name|boolean
name|isEscaped
init|=
name|oi
operator|.
name|isEscaped
argument_list|()
decl_stmt|;
name|byte
name|escapeChar
init|=
name|oi
operator|.
name|getEscapeChar
argument_list|()
decl_stmt|;
name|boolean
name|tagStarted
init|=
literal|false
decl_stmt|;
name|boolean
name|tagParsed
init|=
literal|false
decl_stmt|;
name|int
name|tagStart
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|tagEnd
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|unionByteEnd
init|=
name|start
operator|+
name|length
decl_stmt|;
name|int
name|fieldByteEnd
init|=
name|start
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|this
operator|.
name|bytes
operator|.
name|getData
argument_list|()
decl_stmt|;
comment|// Go through all bytes in the byte[]
while|while
condition|(
name|fieldByteEnd
operator|<
name|unionByteEnd
condition|)
block|{
if|if
condition|(
name|bytes
index|[
name|fieldByteEnd
index|]
operator|!=
name|separator
condition|)
block|{
if|if
condition|(
name|isEscaped
operator|&&
name|bytes
index|[
name|fieldByteEnd
index|]
operator|==
name|escapeChar
operator|&&
name|fieldByteEnd
operator|+
literal|1
operator|<
name|unionByteEnd
condition|)
block|{
comment|// ignore the char after escape_char
name|fieldByteEnd
operator|+=
literal|1
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|tagStarted
condition|)
block|{
name|tagStart
operator|=
name|fieldByteEnd
expr_stmt|;
name|tagStarted
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// (bytes[fieldByteEnd] == separator)
if|if
condition|(
operator|!
name|tagParsed
condition|)
block|{
comment|// Reached the end of the tag
name|tagEnd
operator|=
name|fieldByteEnd
operator|-
literal|1
expr_stmt|;
name|startPosition
operator|=
name|fieldByteEnd
operator|+
literal|1
expr_stmt|;
name|tagParsed
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|fieldByteEnd
operator|++
expr_stmt|;
block|}
name|tag
operator|=
name|LazyByte
operator|.
name|parseByte
argument_list|(
name|bytes
argument_list|,
name|tagStart
argument_list|,
operator|(
name|tagEnd
operator|-
name|tagStart
operator|)
operator|+
literal|1
argument_list|)
expr_stmt|;
name|field
operator|=
name|LazyFactory
operator|.
name|createLazyObject
argument_list|(
name|oi
operator|.
name|getObjectInspectors
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|)
expr_stmt|;
name|fieldInited
operator|=
literal|false
expr_stmt|;
name|parsed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Get the field out of the row without checking parsed.    *    * @return The value of the field    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|private
name|Object
name|uncheckedGetField
parameter_list|()
block|{
name|LazyObject
name|field
init|=
operator|(
name|LazyObject
operator|)
name|this
operator|.
name|field
decl_stmt|;
if|if
condition|(
name|fieldInited
condition|)
block|{
return|return
name|field
operator|.
name|getObject
argument_list|()
return|;
block|}
name|fieldInited
operator|=
literal|true
expr_stmt|;
name|int
name|fieldStart
init|=
name|startPosition
decl_stmt|;
name|int
name|fieldLength
init|=
name|start
operator|+
name|length
operator|-
name|startPosition
decl_stmt|;
if|if
condition|(
name|isNull
argument_list|(
name|oi
operator|.
name|getNullSequence
argument_list|()
argument_list|,
name|bytes
argument_list|,
name|fieldStart
argument_list|,
name|fieldLength
argument_list|)
condition|)
block|{
name|field
operator|.
name|setNull
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|field
operator|.
name|init
argument_list|(
name|bytes
argument_list|,
name|fieldStart
argument_list|,
name|fieldLength
argument_list|)
expr_stmt|;
block|}
return|return
name|field
operator|.
name|getObject
argument_list|()
return|;
block|}
comment|/**    * Get the field out of the union.    *    * @return The field as a LazyObject    */
specifier|public
name|Object
name|getField
parameter_list|()
block|{
if|if
condition|(
name|fieldSet
condition|)
block|{
return|return
name|field
return|;
block|}
if|if
condition|(
operator|!
name|parsed
condition|)
block|{
name|parse
argument_list|()
expr_stmt|;
block|}
return|return
name|uncheckedGetField
argument_list|()
return|;
block|}
comment|/**    * Get the tag of the union    *    * @return The tag byte    */
specifier|public
name|byte
name|getTag
parameter_list|()
block|{
if|if
condition|(
name|fieldSet
condition|)
block|{
return|return
name|tag
return|;
block|}
if|if
condition|(
operator|!
name|parsed
condition|)
block|{
name|parse
argument_list|()
expr_stmt|;
block|}
return|return
name|tag
return|;
block|}
block|}
end_class

end_unit

