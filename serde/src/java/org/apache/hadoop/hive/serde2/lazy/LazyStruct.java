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
name|Arrays
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
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|SerDeException
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
name|serde2
operator|.
name|SerDeStatsStruct
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
name|StructObject
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
name|LazySimpleStructObjectInspector
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
name|StructField
import|;
end_import

begin_comment
comment|/**  * LazyObject for storing a struct. The field of a struct can be primitive or  * non-primitive.  *  * LazyStruct does not deal with the case of a NULL struct. That is handled by  * the parent LazyObject.  */
end_comment

begin_class
specifier|public
class|class
name|LazyStruct
extends|extends
name|LazyNonPrimitive
argument_list|<
name|LazySimpleStructObjectInspector
argument_list|>
implements|implements
name|StructObject
implements|,
name|SerDeStatsStruct
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
name|LazyStruct
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Whether the data is already parsed or not.    */
name|boolean
name|parsed
decl_stmt|;
comment|/**    * Size of serialized data    */
name|long
name|serializedSize
decl_stmt|;
comment|/**    * The start positions of struct fields. Only valid when the data is parsed.    * Note that startPosition[arrayLength] = begin + length + 1; that makes sure    * we can use the same formula to compute the length of each element of the    * array.    */
name|int
index|[]
name|startPosition
decl_stmt|;
comment|/**    * The fields of the struct.    */
name|LazyObjectBase
index|[]
name|fields
decl_stmt|;
comment|/**    * Whether init() has been called on the field or not.    */
name|boolean
index|[]
name|fieldInited
decl_stmt|;
comment|/**    * Construct a LazyStruct object with the ObjectInspector.    */
specifier|public
name|LazyStruct
parameter_list|(
name|LazySimpleStructObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the row data for this LazyStruct.    *    * @see LazyObject#init(ByteArrayRef, int, int)    */
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
name|serializedSize
operator|=
name|length
expr_stmt|;
block|}
name|boolean
name|missingFieldWarned
init|=
literal|false
decl_stmt|;
name|boolean
name|extraFieldWarned
init|=
literal|false
decl_stmt|;
comment|/**    * Parse the byte[] and fill each field.    */
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
name|lastColumnTakesRest
init|=
name|oi
operator|.
name|getLastColumnTakesRest
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
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|initLazyFields
argument_list|(
name|oi
operator|.
name|getAllStructFieldRefs
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|structByteEnd
init|=
name|start
operator|+
name|length
decl_stmt|;
name|int
name|fieldId
init|=
literal|0
decl_stmt|;
name|int
name|fieldByteBegin
init|=
name|start
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
operator|<=
name|structByteEnd
condition|)
block|{
if|if
condition|(
name|fieldByteEnd
operator|==
name|structByteEnd
operator|||
name|bytes
index|[
name|fieldByteEnd
index|]
operator|==
name|separator
condition|)
block|{
comment|// Reached the end of a field?
if|if
condition|(
name|lastColumnTakesRest
operator|&&
name|fieldId
operator|==
name|fields
operator|.
name|length
operator|-
literal|1
condition|)
block|{
name|fieldByteEnd
operator|=
name|structByteEnd
expr_stmt|;
block|}
name|startPosition
index|[
name|fieldId
index|]
operator|=
name|fieldByteBegin
expr_stmt|;
name|fieldId
operator|++
expr_stmt|;
if|if
condition|(
name|fieldId
operator|==
name|fields
operator|.
name|length
operator|||
name|fieldByteEnd
operator|==
name|structByteEnd
condition|)
block|{
comment|// All fields have been parsed, or bytes have been parsed.
comment|// We need to set the startPosition of fields.length to ensure we
comment|// can use the same formula to calculate the length of each field.
comment|// For missing fields, their starting positions will all be the same,
comment|// which will make their lengths to be -1 and uncheckedGetField will
comment|// return these fields as NULLs.
for|for
control|(
name|int
name|i
init|=
name|fieldId
init|;
name|i
operator|<=
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|startPosition
index|[
name|i
index|]
operator|=
name|fieldByteEnd
operator|+
literal|1
expr_stmt|;
block|}
break|break;
block|}
name|fieldByteBegin
operator|=
name|fieldByteEnd
operator|+
literal|1
expr_stmt|;
name|fieldByteEnd
operator|++
expr_stmt|;
block|}
else|else
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
name|structByteEnd
condition|)
block|{
comment|// ignore the char after escape_char
name|fieldByteEnd
operator|+=
literal|2
expr_stmt|;
block|}
else|else
block|{
name|fieldByteEnd
operator|++
expr_stmt|;
block|}
block|}
block|}
comment|// Extra bytes at the end?
if|if
condition|(
operator|!
name|extraFieldWarned
operator|&&
name|fieldByteEnd
operator|<
name|structByteEnd
condition|)
block|{
name|extraFieldWarned
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Extra bytes detected at the end of the row! Ignoring similar "
operator|+
literal|"problems."
argument_list|)
expr_stmt|;
block|}
comment|// Missing fields?
if|if
condition|(
operator|!
name|missingFieldWarned
operator|&&
name|fieldId
operator|<
name|fields
operator|.
name|length
condition|)
block|{
name|missingFieldWarned
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Missing fields! Expected "
operator|+
name|fields
operator|.
name|length
operator|+
literal|" fields but "
operator|+
literal|"only got "
operator|+
name|fieldId
operator|+
literal|"! Ignoring similar problems."
argument_list|)
expr_stmt|;
block|}
name|Arrays
operator|.
name|fill
argument_list|(
name|fieldInited
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|parsed
operator|=
literal|true
expr_stmt|;
block|}
specifier|protected
specifier|final
name|void
name|initLazyFields
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
parameter_list|)
block|{
name|fields
operator|=
operator|new
name|LazyObjectBase
index|[
name|fieldRefs
operator|.
name|size
argument_list|()
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
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|fields
index|[
name|i
index|]
operator|=
name|createLazyField
argument_list|(
name|i
argument_list|,
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|fieldInited
operator|=
operator|new
name|boolean
index|[
name|fields
operator|.
name|length
index|]
expr_stmt|;
comment|// Extra element to make sure we have the same formula to compute the
comment|// length of each element of the array.
name|startPosition
operator|=
operator|new
name|int
index|[
name|fields
operator|.
name|length
operator|+
literal|1
index|]
expr_stmt|;
block|}
specifier|protected
name|LazyObjectBase
name|createLazyField
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|StructField
name|fieldRef
parameter_list|)
throws|throws
name|SerDeException
block|{
return|return
name|LazyFactory
operator|.
name|createLazyObject
argument_list|(
name|fieldRef
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get one field out of the struct.    *    * If the field is a primitive field, return the actual object. Otherwise    * return the LazyObject. This is because PrimitiveObjectInspector does not    * have control over the object used by the user - the user simply directly    * use the Object instead of going through Object    * PrimitiveObjectInspector.get(Object).    *    * @param fieldID    *          The field ID    * @return The field as a LazyObject    */
specifier|public
name|Object
name|getField
parameter_list|(
name|int
name|fieldID
parameter_list|)
block|{
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
argument_list|(
name|fieldID
argument_list|)
return|;
block|}
comment|/**    * Get the field out of the row without checking parsed. This is called by    * both getField and getFieldsAsList.    *    * @param fieldID    *          The id of the field starting from 0.    * @param nullSequence    *          The sequence representing NULL value.    * @return The value of the field    */
specifier|private
name|Object
name|uncheckedGetField
parameter_list|(
name|int
name|fieldID
parameter_list|)
block|{
if|if
condition|(
name|fieldInited
index|[
name|fieldID
index|]
condition|)
block|{
return|return
name|fields
index|[
name|fieldID
index|]
operator|.
name|getObject
argument_list|()
return|;
block|}
name|fieldInited
index|[
name|fieldID
index|]
operator|=
literal|true
expr_stmt|;
name|int
name|fieldByteBegin
init|=
name|startPosition
index|[
name|fieldID
index|]
decl_stmt|;
name|int
name|fieldLength
init|=
name|startPosition
index|[
name|fieldID
operator|+
literal|1
index|]
operator|-
name|startPosition
index|[
name|fieldID
index|]
operator|-
literal|1
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
name|fieldByteBegin
argument_list|,
name|fieldLength
argument_list|)
condition|)
block|{
name|fields
index|[
name|fieldID
index|]
operator|.
name|setNull
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|fields
index|[
name|fieldID
index|]
operator|.
name|init
argument_list|(
name|bytes
argument_list|,
name|fieldByteBegin
argument_list|,
name|fieldLength
argument_list|)
expr_stmt|;
block|}
return|return
name|fields
index|[
name|fieldID
index|]
operator|.
name|getObject
argument_list|()
return|;
block|}
specifier|private
specifier|transient
name|List
argument_list|<
name|Object
argument_list|>
name|cachedList
decl_stmt|;
comment|/**    * Get the values of the fields as an ArrayList.    *    * @return The values of the fields as an ArrayList.    */
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getFieldsAsList
parameter_list|()
block|{
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
if|if
condition|(
name|cachedList
operator|==
literal|null
condition|)
block|{
name|cachedList
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|cachedList
operator|.
name|clear
argument_list|()
expr_stmt|;
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
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|cachedList
operator|.
name|add
argument_list|(
name|uncheckedGetField
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|cachedList
return|;
block|}
specifier|protected
name|boolean
name|getParsed
parameter_list|()
block|{
return|return
name|parsed
return|;
block|}
specifier|protected
name|void
name|setParsed
parameter_list|(
name|boolean
name|parsed
parameter_list|)
block|{
name|this
operator|.
name|parsed
operator|=
name|parsed
expr_stmt|;
block|}
specifier|protected
name|LazyObjectBase
index|[]
name|getFields
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
specifier|protected
name|void
name|setFields
parameter_list|(
name|LazyObject
index|[]
name|fields
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
block|}
specifier|protected
name|boolean
index|[]
name|getFieldInited
parameter_list|()
block|{
return|return
name|fieldInited
return|;
block|}
specifier|protected
name|void
name|setFieldInited
parameter_list|(
name|boolean
index|[]
name|fieldInited
parameter_list|)
block|{
name|this
operator|.
name|fieldInited
operator|=
name|fieldInited
expr_stmt|;
block|}
specifier|public
name|long
name|getRawDataSerializedSize
parameter_list|()
block|{
return|return
name|serializedSize
return|;
block|}
comment|/**    *  Parses rawRow using multi-char delimiter.    *    * @param rawRow row to be parsed, delimited by fieldDelimit    * @param fieldDelimit pattern of multi-char delimiter    * @param replacementDelim delimiter with which fieldDelimit has been replaced in rawRow    */
specifier|public
name|void
name|parseMultiDelimit
parameter_list|(
specifier|final
name|String
name|rawRow
parameter_list|,
specifier|final
name|Pattern
name|fieldDelimit
parameter_list|,
specifier|final
name|String
name|replacementDelim
parameter_list|)
block|{
if|if
condition|(
name|rawRow
operator|==
literal|null
operator|||
name|fieldDelimit
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|oi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|fields
operator|=
operator|new
name|LazyObject
index|[
name|fieldRefs
operator|.
name|size
argument_list|()
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
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|fields
index|[
name|i
index|]
operator|=
name|LazyFactory
operator|.
name|createLazyObject
argument_list|(
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fieldInited
operator|=
operator|new
name|boolean
index|[
name|fields
operator|.
name|length
index|]
expr_stmt|;
name|startPosition
operator|=
operator|new
name|int
index|[
name|fields
operator|.
name|length
operator|+
literal|1
index|]
expr_stmt|;
block|}
specifier|final
name|int
name|delimiterLength
init|=
name|fieldDelimit
operator|.
name|toString
argument_list|()
operator|.
name|length
argument_list|()
decl_stmt|;
specifier|final
name|int
name|extraBytesInDelim
init|=
name|delimiterLength
operator|-
name|replacementDelim
operator|.
name|length
argument_list|()
decl_stmt|;
comment|// first field always starts from 0, even when missing
name|startPosition
index|[
literal|0
index|]
operator|=
literal|0
expr_stmt|;
name|Matcher
name|delimiterMatcher
init|=
name|fieldDelimit
operator|.
name|matcher
argument_list|(
name|rawRow
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|delimiterMatcher
operator|.
name|find
argument_list|()
condition|)
block|{
comment|// MultiDelimitSerDe replaces actual multi-char delimiter by replacementDelim("\1") which reduces the length
comment|// however here we are getting rawRow with original multi-char delimiter
comment|// due to this we have to subtract those extra chars to match length of LazyNonPrimitive#bytes which are used
comment|// while reading data, see uncheckedGetField()
name|startPosition
index|[
name|i
index|]
operator|=
name|delimiterMatcher
operator|.
name|start
argument_list|()
operator|+
name|delimiterLength
operator|-
name|i
operator|*
name|extraBytesInDelim
expr_stmt|;
block|}
else|else
block|{
name|startPosition
index|[
name|i
index|]
operator|=
name|length
operator|+
literal|1
expr_stmt|;
block|}
block|}
name|Arrays
operator|.
name|fill
argument_list|(
name|fieldInited
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|parsed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Return the data in bytes corresponding to this given struct. This is useful specifically in    * cases where the data is stored in serialized formats like protobufs or thrift and would need    * custom deserializers to be deserialized.    * */
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|()
block|{
return|return
name|bytes
operator|.
name|getData
argument_list|()
return|;
block|}
block|}
end_class

end_unit

