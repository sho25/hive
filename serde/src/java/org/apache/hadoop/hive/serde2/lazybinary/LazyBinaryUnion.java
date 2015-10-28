begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**    * Licensed to the Apache Software Foundation (ASF) under one    * or more contributor license agreements.  See the NOTICE file    * distributed with this work for additional information    * regarding copyright ownership.  The ASF licenses this file    * to you under the Apache License, Version 2.0 (the    * "License"); you may not use this file except in compliance    * with the License.  You may obtain a copy of the License at    *    *     http://www.apache.org/licenses/LICENSE-2.0    *    * Unless required by applicable law or agreed to in writing, software    * distributed under the License is distributed on an "AS IS" BASIS,    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    * See the License for the specific language governing permissions and    * limitations under the License.    */
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
name|objectinspector
operator|.
name|LazyBinaryUnionObjectInspector
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
name|*
import|;
end_import

begin_comment
comment|/**  * LazyBinaryUnion is serialized as follows: start TAG FIELD end bytes[] ->  * |-----|---------|--- ... ---|-----|---------|  *  * Section TAG is one byte, corresponding to tag of set union field  * FIELD is a LazyBinaryObject corresponding to set union field value.  *  */
end_comment

begin_class
specifier|public
class|class
name|LazyBinaryUnion
extends|extends
name|LazyBinaryNonPrimitive
argument_list|<
name|LazyBinaryUnionObjectInspector
argument_list|>
implements|implements
name|SerDeStatsStruct
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LazyBinaryUnion
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**      * Whether the data is already parsed or not.      */
name|boolean
name|parsed
decl_stmt|;
comment|/**      * Size of serialized data      */
name|long
name|serializedSize
decl_stmt|;
comment|/**      * The field of the union which contains the value.      */
name|LazyBinaryObject
name|field
decl_stmt|;
name|boolean
name|fieldInited
decl_stmt|;
comment|/**      * The start positions and lengths of union fields. Only valid when the data      * is parsed.      */
name|int
name|fieldStart
decl_stmt|;
name|int
name|fieldLength
decl_stmt|;
name|byte
name|tag
decl_stmt|;
specifier|final
name|LazyBinaryUtils
operator|.
name|VInt
name|vInt
init|=
operator|new
name|LazyBinaryUtils
operator|.
name|VInt
argument_list|()
decl_stmt|;
comment|/**      * Construct a LazyBinaryUnion object with an ObjectInspector.      */
specifier|protected
name|LazyBinaryUnion
parameter_list|(
name|LazyBinaryUnionObjectInspector
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
name|fieldInited
operator|=
literal|false
expr_stmt|;
name|field
operator|=
literal|null
expr_stmt|;
name|cachedObject
operator|=
literal|null
expr_stmt|;
block|}
name|LazyBinaryUtils
operator|.
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
comment|/**      * Parse the byte[] and fill fieldStart, fieldLength, fieldInited and      * fieldIsNull.      */
specifier|private
name|void
name|parse
parameter_list|()
block|{
name|LazyBinaryUnionObjectInspector
name|uoi
init|=
operator|(
name|LazyBinaryUnionObjectInspector
operator|)
name|oi
decl_stmt|;
comment|/**        * Please note that tag is followed by field        */
name|int
name|unionByteEnd
init|=
name|start
operator|+
name|length
decl_stmt|;
name|byte
index|[]
name|byteArr
init|=
name|this
operator|.
name|bytes
operator|.
name|getData
argument_list|()
decl_stmt|;
comment|//Tag of union field is the first byte to be parsed
specifier|final
name|int
name|tagEnd
init|=
name|start
operator|+
literal|1
decl_stmt|;
name|tag
operator|=
name|byteArr
index|[
name|start
index|]
expr_stmt|;
name|field
operator|=
name|LazyBinaryFactory
operator|.
name|createLazyBinaryObject
argument_list|(
name|uoi
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
comment|//Check the union field's length and offset
name|LazyBinaryUtils
operator|.
name|checkObjectByteInfo
argument_list|(
name|uoi
operator|.
name|getObjectInspectors
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|,
name|byteArr
argument_list|,
name|tagEnd
argument_list|,
name|recordInfo
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
name|fieldStart
operator|=
name|tagEnd
operator|+
name|recordInfo
operator|.
name|elementOffset
expr_stmt|;
comment|// Add 1 for tag
name|fieldLength
operator|=
name|recordInfo
operator|.
name|elementSize
expr_stmt|;
comment|// Extra bytes at the end?
if|if
condition|(
operator|!
name|extraFieldWarned
operator|&&
operator|(
name|fieldStart
operator|+
name|fieldLength
operator|)
operator|<
name|unionByteEnd
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
operator|(
name|fieldStart
operator|+
name|fieldLength
operator|)
operator|>
name|unionByteEnd
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
literal|"Missing fields! Expected 1 fields but "
operator|+
literal|"only got "
operator|+
name|field
operator|+
literal|"! Ignoring similar problems."
argument_list|)
expr_stmt|;
block|}
name|parsed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**      * Get the set field out of the union.      *      * If the field is a primitive field, return the actual object. Otherwise      * return the LazyObject. This is because PrimitiveObjectInspector does not      * have control over the object used by the user - the user simply directly      * use the Object instead of going through Object      * PrimitiveObjectInspector.get(Object).      * @return The field as a LazyObject      */
specifier|public
name|Object
name|getField
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
name|cachedObject
operator|==
literal|null
condition|)
block|{
return|return
name|uncheckedGetField
argument_list|()
return|;
block|}
return|return
name|cachedObject
return|;
block|}
comment|/**      * Get the field out of the row without checking parsed. This is called by      * both getField and getFieldsAsList.      *      * @param fieldID      *          The id of the field starting from 0.      * @return The value of the field      */
specifier|private
name|Object
name|uncheckedGetField
parameter_list|()
block|{
comment|// Test the length first so in most cases we avoid doing a byte[]
comment|// comparison.
if|if
condition|(
operator|!
name|fieldInited
condition|)
block|{
name|fieldInited
operator|=
literal|true
expr_stmt|;
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
name|cachedObject
operator|=
name|field
operator|.
name|getObject
argument_list|()
expr_stmt|;
return|return
name|field
operator|.
name|getObject
argument_list|()
return|;
block|}
name|Object
name|cachedObject
decl_stmt|;
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
comment|/**    * Get the set field's tag    *    *    * @return The tag of the field set in the union    */
specifier|public
name|byte
name|getTag
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
return|return
name|tag
return|;
block|}
block|}
end_class

end_unit

