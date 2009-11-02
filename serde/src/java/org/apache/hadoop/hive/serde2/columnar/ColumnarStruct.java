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
name|columnar
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
name|lazy
operator|.
name|LazyFactory
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
name|LazyObject
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
name|LazyUtils
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

begin_comment
comment|/**  * ColumnarStruct is different from LazyStruct in that ColumnarStruct's field  * Object get parsed at its initialize time when call  * {@link #init(BytesRefArrayWritable cols)}, while LazyStruct parse fields in a  * lazy way.  *   */
end_comment

begin_class
specifier|public
class|class
name|ColumnarStruct
block|{
comment|/**    * The fields of the struct.    */
name|LazyObject
index|[]
name|fields
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ColumnarStruct
operator|.
name|class
argument_list|)
decl_stmt|;
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
comment|// init() function is called?
name|int
index|[]
name|prjColIDs
init|=
literal|null
decl_stmt|;
comment|// list of projected column IDs
comment|/**    * Construct a ColumnarStruct object with the TypeInfo. It creates the first    * level object at the first place    *     * @param oi    *          the ObjectInspector representing the type of this LazyStruct.    */
specifier|public
name|ColumnarStruct
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|)
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
name|int
name|num
init|=
name|fieldRefs
operator|.
name|size
argument_list|()
decl_stmt|;
name|fields
operator|=
operator|new
name|LazyObject
index|[
name|num
index|]
expr_stmt|;
name|cachedByteArrayRef
operator|=
operator|new
name|ByteArrayRef
index|[
name|num
index|]
expr_stmt|;
name|rawBytesField
operator|=
operator|new
name|BytesRefWritable
index|[
name|num
index|]
expr_stmt|;
name|fieldIsNull
operator|=
operator|new
name|boolean
index|[
name|num
index|]
expr_stmt|;
name|inited
operator|=
operator|new
name|boolean
index|[
name|num
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
name|num
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
name|cachedByteArrayRef
index|[
name|i
index|]
operator|=
operator|new
name|ByteArrayRef
argument_list|()
expr_stmt|;
name|fieldIsNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|inited
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/**    * Get one field out of the struct.    *     * If the field is a primitive field, return the actual object. Otherwise    * return the LazyObject. This is because PrimitiveObjectInspector does not    * have control over the object used by the user - the user simply directly    * use the Object instead of going through Object    * PrimitiveObjectInspector.get(Object).    *     * NOTE: separator and nullSequence has to be the same each time this method    * is called. These two parameters are used only once to parse each record.    *     * @param fieldID    *          The field ID    * @param nullSequence    *          The sequence for null value    * @return The field as a LazyObject    */
specifier|public
name|Object
name|getField
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|Text
name|nullSequence
parameter_list|)
block|{
return|return
name|uncheckedGetField
argument_list|(
name|fieldID
argument_list|,
name|nullSequence
argument_list|)
return|;
block|}
comment|/*    * use an array instead of only one object in case in future hive does not do    * the byte copy.    */
name|ByteArrayRef
index|[]
name|cachedByteArrayRef
init|=
literal|null
decl_stmt|;
name|BytesRefWritable
index|[]
name|rawBytesField
init|=
literal|null
decl_stmt|;
name|boolean
index|[]
name|inited
init|=
literal|null
decl_stmt|;
name|boolean
index|[]
name|fieldIsNull
init|=
literal|null
decl_stmt|;
comment|/**    * Get the field out of the row without checking parsed. This is called by    * both getField and getFieldsAsList.    *     * @param fieldID    *          The id of the field starting from 0.    * @param nullSequence    *          The sequence representing NULL value.    * @return The value of the field    */
specifier|protected
name|Object
name|uncheckedGetField
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|Text
name|nullSequence
parameter_list|)
block|{
if|if
condition|(
name|fieldIsNull
index|[
name|fieldID
index|]
condition|)
return|return
literal|null
return|;
if|if
condition|(
operator|!
name|inited
index|[
name|fieldID
index|]
condition|)
block|{
name|BytesRefWritable
name|passedInField
init|=
name|rawBytesField
index|[
name|fieldID
index|]
decl_stmt|;
try|try
block|{
name|cachedByteArrayRef
index|[
name|fieldID
index|]
operator|.
name|setData
argument_list|(
name|passedInField
operator|.
name|getData
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
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
name|fields
index|[
name|fieldID
index|]
operator|.
name|init
argument_list|(
name|cachedByteArrayRef
index|[
name|fieldID
index|]
argument_list|,
name|passedInField
operator|.
name|getStart
argument_list|()
argument_list|,
name|passedInField
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|inited
index|[
name|fieldID
index|]
operator|=
literal|true
expr_stmt|;
block|}
name|byte
index|[]
name|data
init|=
name|cachedByteArrayRef
index|[
name|fieldID
index|]
operator|.
name|getData
argument_list|()
decl_stmt|;
name|int
name|fieldLen
init|=
name|data
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|fieldLen
operator|==
name|nullSequence
operator|.
name|getLength
argument_list|()
operator|&&
name|LazyUtils
operator|.
name|compare
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|fieldLen
argument_list|,
name|nullSequence
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullSequence
operator|.
name|getLength
argument_list|()
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
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
comment|/*  ============================  [PERF] ===================================    *  This function is called for every row. Setting up the selected/projected     *  columns at the first call, and don't do that for the following calls.     *  Ideally this should be done in the constructor where we don't need to     *  branch in the function for each row.     *  =========================================================================    */
specifier|public
name|void
name|init
parameter_list|(
name|BytesRefArrayWritable
name|cols
parameter_list|)
block|{
if|if
condition|(
name|initialized
condition|)
block|{
comment|// short cut for non-first calls
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|prjColIDs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|int
name|fieldIndex
init|=
name|prjColIDs
index|[
name|i
index|]
decl_stmt|;
name|rawBytesField
index|[
name|fieldIndex
index|]
operator|=
name|cols
operator|.
name|unCheckedGet
argument_list|(
name|fieldIndex
argument_list|)
expr_stmt|;
name|inited
index|[
name|fieldIndex
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// first time call init()
name|int
name|fieldIndex
init|=
literal|0
decl_stmt|;
name|int
name|min
init|=
name|cols
operator|.
name|size
argument_list|()
operator|<
name|fields
operator|.
name|length
condition|?
name|cols
operator|.
name|size
argument_list|()
else|:
name|fields
operator|.
name|length
decl_stmt|;
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|tmp_sel_cols
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
init|;
name|fieldIndex
operator|<
name|min
condition|;
name|fieldIndex
operator|++
control|)
block|{
comment|// call the faster unCheckedGet()
comment|// alsert: min<= cols.size()
name|BytesRefWritable
name|passedInField
init|=
name|cols
operator|.
name|unCheckedGet
argument_list|(
name|fieldIndex
argument_list|)
decl_stmt|;
if|if
condition|(
name|passedInField
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// if (fields[fieldIndex] == null)
comment|// fields[fieldIndex] = LazyFactory.createLazyObject(fieldTypeInfos
comment|// .get(fieldIndex));
name|tmp_sel_cols
operator|.
name|add
argument_list|(
name|fieldIndex
argument_list|)
expr_stmt|;
name|rawBytesField
index|[
name|fieldIndex
index|]
operator|=
name|passedInField
expr_stmt|;
name|fieldIsNull
index|[
name|fieldIndex
index|]
operator|=
literal|false
expr_stmt|;
block|}
else|else
name|fieldIsNull
index|[
name|fieldIndex
index|]
operator|=
literal|true
expr_stmt|;
name|inited
index|[
name|fieldIndex
index|]
operator|=
literal|false
expr_stmt|;
block|}
for|for
control|(
init|;
name|fieldIndex
operator|<
name|fields
operator|.
name|length
condition|;
name|fieldIndex
operator|++
control|)
name|fieldIsNull
index|[
name|fieldIndex
index|]
operator|=
literal|true
expr_stmt|;
comment|// maintain a list of non-NULL column IDs
name|prjColIDs
operator|=
operator|new
name|int
index|[
name|tmp_sel_cols
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
name|prjColIDs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|prjColIDs
index|[
name|i
index|]
operator|=
name|tmp_sel_cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|cachedList
decl_stmt|;
comment|/**    * Get the values of the fields as an ArrayList.    *     * @param nullSequence    *          The sequence for the NULL value    * @return The values of the fields as an ArrayList.    */
specifier|public
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|getFieldsAsList
parameter_list|(
name|Text
name|nullSequence
parameter_list|)
block|{
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
argument_list|,
name|nullSequence
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|cachedList
return|;
block|}
block|}
end_class

end_unit

