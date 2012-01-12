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
name|lazybinary
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
name|lazy
operator|.
name|ByteArrayRef
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
name|lazybinary
operator|.
name|LazyBinaryUtils
operator|.
name|RecordInfo
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
name|lazybinary
operator|.
name|objectinspector
operator|.
name|LazyBinaryStructObjectInspector
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
name|StructField
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
name|StructObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyBinaryStruct is serialized as follows: start A B A B A B end bytes[] ->  * |-----|---------|--- ... ---|-----|---------|  *  * Section A is one null-byte, corresponding to eight struct fields in Section  * B. Each bit indicates whether the corresponding field is null (0) or not null  * (1). Each field is a LazyBinaryObject.  *  * Following B, there is another section A and B. This pattern repeats until the  * all struct fields are serialized.  */
end_comment

begin_class
specifier|public
class|class
name|LazyBinaryStruct
extends|extends
name|LazyBinaryNonPrimitive
argument_list|<
name|LazyBinaryStructObjectInspector
argument_list|>
implements|implements
name|SerDeStatsStruct
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|LazyBinaryStruct
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
comment|/**    * The fields of the struct.    */
name|LazyBinaryObject
index|[]
name|fields
decl_stmt|;
comment|/**    * Whether a field is initialized or not.    */
name|boolean
index|[]
name|fieldInited
decl_stmt|;
comment|/**    * Whether a field is null or not. Because length is 0 does not means the    * field is null. In particular, a 0-length string is not null.    */
name|boolean
index|[]
name|fieldIsNull
decl_stmt|;
comment|/**    * The start positions and lengths of struct fields. Only valid when the data    * is parsed.    */
name|int
index|[]
name|fieldStart
decl_stmt|;
name|int
index|[]
name|fieldLength
decl_stmt|;
comment|/**    * Construct a LazyBinaryStruct object with an ObjectInspector.    */
specifier|protected
name|LazyBinaryStruct
parameter_list|(
name|LazyBinaryStructObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
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
name|RecordInfo
name|recordInfo
init|=
operator|new
name|LazyBinaryUtils
operator|.
name|RecordInfo
argument_list|()
decl_stmt|;
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
comment|/**    * Parse the byte[] and fill fieldStart, fieldLength, fieldInited and    * fieldIsNull.    */
specifier|private
name|void
name|parse
parameter_list|()
block|{
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
operator|(
operator|(
name|StructObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
operator|new
name|LazyBinaryObject
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
name|ObjectInspector
name|insp
init|=
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|fields
index|[
name|i
index|]
operator|=
name|insp
operator|==
literal|null
condition|?
literal|null
else|:
name|LazyBinaryFactory
operator|.
name|createLazyBinaryObject
argument_list|(
name|insp
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
name|fieldIsNull
operator|=
operator|new
name|boolean
index|[
name|fields
operator|.
name|length
index|]
expr_stmt|;
name|fieldStart
operator|=
operator|new
name|int
index|[
name|fields
operator|.
name|length
index|]
expr_stmt|;
name|fieldLength
operator|=
operator|new
name|int
index|[
name|fields
operator|.
name|length
index|]
expr_stmt|;
block|}
comment|/**      * Please note that one null byte is followed by eight fields, then more      * null byte and fields.      */
name|int
name|fieldId
init|=
literal|0
decl_stmt|;
name|int
name|structByteEnd
init|=
name|start
operator|+
name|length
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
name|byte
name|nullByte
init|=
name|bytes
index|[
name|start
index|]
decl_stmt|;
name|int
name|lastFieldByteEnd
init|=
name|start
operator|+
literal|1
decl_stmt|;
comment|// Go through all bytes in the byte[]
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
name|fieldIsNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|(
name|nullByte
operator|&
operator|(
literal|1
operator|<<
operator|(
name|i
operator|%
literal|8
operator|)
operator|)
operator|)
operator|!=
literal|0
condition|)
block|{
name|fieldIsNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|LazyBinaryUtils
operator|.
name|checkObjectByteInfo
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
argument_list|,
name|bytes
argument_list|,
name|lastFieldByteEnd
argument_list|,
name|recordInfo
argument_list|)
expr_stmt|;
name|fieldStart
index|[
name|i
index|]
operator|=
name|lastFieldByteEnd
operator|+
name|recordInfo
operator|.
name|elementOffset
expr_stmt|;
name|fieldLength
index|[
name|i
index|]
operator|=
name|recordInfo
operator|.
name|elementSize
expr_stmt|;
name|lastFieldByteEnd
operator|=
name|fieldStart
index|[
name|i
index|]
operator|+
name|fieldLength
index|[
name|i
index|]
expr_stmt|;
block|}
comment|// count how many fields are there
if|if
condition|(
name|lastFieldByteEnd
operator|<=
name|structByteEnd
condition|)
block|{
name|fieldId
operator|++
expr_stmt|;
block|}
comment|// next byte is a null byte if there are more bytes to go
if|if
condition|(
literal|7
operator|==
operator|(
name|i
operator|%
literal|8
operator|)
condition|)
block|{
if|if
condition|(
name|lastFieldByteEnd
operator|<
name|structByteEnd
condition|)
block|{
name|nullByte
operator|=
name|bytes
index|[
name|lastFieldByteEnd
index|]
expr_stmt|;
name|lastFieldByteEnd
operator|++
expr_stmt|;
block|}
else|else
block|{
comment|// otherwise all null afterwards
name|nullByte
operator|=
literal|0
expr_stmt|;
name|lastFieldByteEnd
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
name|lastFieldByteEnd
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
name|lastFieldByteEnd
operator|>
name|structByteEnd
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
comment|/**    * Get the field out of the row without checking parsed. This is called by    * both getField and getFieldsAsList.    *    * @param fieldID    *          The id of the field starting from 0.    * @return The value of the field    */
specifier|private
name|Object
name|uncheckedGetField
parameter_list|(
name|int
name|fieldID
parameter_list|)
block|{
comment|// Test the length first so in most cases we avoid doing a byte[]
comment|// comparison.
if|if
condition|(
name|fieldIsNull
index|[
name|fieldID
index|]
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|fieldInited
index|[
name|fieldID
index|]
condition|)
block|{
name|fieldInited
index|[
name|fieldID
index|]
operator|=
literal|true
expr_stmt|;
name|fields
index|[
name|fieldID
index|]
operator|.
name|init
argument_list|(
name|bytes
argument_list|,
name|fieldStart
index|[
name|fieldID
index|]
argument_list|,
name|fieldLength
index|[
name|fieldID
index|]
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
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|cachedList
decl_stmt|;
comment|/**    * Get the values of the fields as an ArrayList.    *    * @return The values of the fields as an ArrayList.    */
specifier|public
name|ArrayList
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
annotation|@
name|Override
specifier|public
name|Object
name|getObject
parameter_list|()
block|{
return|return
name|this
return|;
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
block|}
end_class

end_unit

