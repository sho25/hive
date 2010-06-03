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
name|hbase
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
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|RowResult
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
name|LazyStruct
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
name|LazyMapObjectInspector
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
comment|/**  * LazyObject for storing an HBase row.  The field of an HBase row can be  * primitive or non-primitive.  */
end_comment

begin_class
specifier|public
class|class
name|LazyHBaseRow
extends|extends
name|LazyStruct
block|{
comment|/**    * The HBase columns mapping of the row.    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumns
decl_stmt|;
specifier|private
name|RowResult
name|rowResult
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|cachedList
decl_stmt|;
comment|/**    * Construct a LazyHBaseRow object with the ObjectInspector.    */
specifier|public
name|LazyHBaseRow
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
comment|/**    * Set the hbase row data(a RowResult writable) for this LazyStruct.    * @see LazyHBaseRow#init(RowResult)    */
specifier|public
name|void
name|init
parameter_list|(
name|RowResult
name|rr
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumns
parameter_list|)
block|{
name|this
operator|.
name|rowResult
operator|=
name|rr
expr_stmt|;
name|this
operator|.
name|hbaseColumns
operator|=
name|hbaseColumns
expr_stmt|;
name|setParsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Parse the RowResult and fill each field.    * @see LazyStruct#parse()    */
specifier|private
name|void
name|parse
parameter_list|()
block|{
if|if
condition|(
name|getFields
argument_list|()
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
operator|(
operator|(
name|StructObjectInspector
operator|)
name|getInspector
argument_list|()
operator|)
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|setFields
argument_list|(
operator|new
name|LazyObject
index|[
name|fieldRefs
operator|.
name|size
argument_list|()
index|]
argument_list|)
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
name|getFields
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|hbaseColumn
init|=
name|hbaseColumns
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|hbaseColumn
operator|.
name|endsWith
argument_list|(
literal|":"
argument_list|)
condition|)
block|{
comment|// a column family
name|getFields
argument_list|()
index|[
name|i
index|]
operator|=
operator|new
name|LazyHBaseCellMap
argument_list|(
operator|(
name|LazyMapObjectInspector
operator|)
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
continue|continue;
block|}
name|getFields
argument_list|()
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
name|setFieldInited
argument_list|(
operator|new
name|boolean
index|[
name|getFields
argument_list|()
operator|.
name|length
index|]
argument_list|)
expr_stmt|;
block|}
name|Arrays
operator|.
name|fill
argument_list|(
name|getFieldInited
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|setParsed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get one field out of the hbase row.    *     * If the field is a primitive field, return the actual object.    * Otherwise return the LazyObject.  This is because PrimitiveObjectInspector    * does not have control over the object used by the user - the user simply    * directly uses the Object instead of going through     * Object PrimitiveObjectInspector.get(Object).      *     * @param fieldID  The field ID    * @return         The field as a LazyObject    */
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
name|getParsed
argument_list|()
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
comment|/**    * Get the field out of the row without checking whether parsing is needed.    * This is called by both getField and getFieldsAsList.    * @param fieldID  The id of the field starting from 0.    * @param nullSequence  The sequence representing NULL value.    * @return  The value of the field    */
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
operator|!
name|getFieldInited
argument_list|()
index|[
name|fieldID
index|]
condition|)
block|{
name|getFieldInited
argument_list|()
index|[
name|fieldID
index|]
operator|=
literal|true
expr_stmt|;
name|ByteArrayRef
name|ref
init|=
literal|null
decl_stmt|;
name|String
name|columnName
init|=
name|hbaseColumns
operator|.
name|get
argument_list|(
name|fieldID
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnName
operator|.
name|equals
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_KEY_COL
argument_list|)
condition|)
block|{
name|ref
operator|=
operator|new
name|ByteArrayRef
argument_list|()
expr_stmt|;
name|ref
operator|.
name|setData
argument_list|(
name|rowResult
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|columnName
operator|.
name|endsWith
argument_list|(
literal|":"
argument_list|)
condition|)
block|{
comment|// it is a column family
operator|(
operator|(
name|LazyHBaseCellMap
operator|)
name|getFields
argument_list|()
index|[
name|fieldID
index|]
operator|)
operator|.
name|init
argument_list|(
name|rowResult
argument_list|,
name|columnName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// it is a column
if|if
condition|(
name|rowResult
operator|.
name|containsKey
argument_list|(
name|columnName
argument_list|)
condition|)
block|{
name|ref
operator|=
operator|new
name|ByteArrayRef
argument_list|()
expr_stmt|;
name|ref
operator|.
name|setData
argument_list|(
name|rowResult
operator|.
name|get
argument_list|(
name|columnName
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
if|if
condition|(
name|ref
operator|!=
literal|null
condition|)
block|{
name|getFields
argument_list|()
index|[
name|fieldID
index|]
operator|.
name|init
argument_list|(
name|ref
argument_list|,
literal|0
argument_list|,
name|ref
operator|.
name|getData
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|getFields
argument_list|()
index|[
name|fieldID
index|]
operator|.
name|getObject
argument_list|()
return|;
block|}
comment|/**    * Get the values of the fields as an ArrayList.    * @return The values of the fields as an ArrayList.    */
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
name|getParsed
argument_list|()
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
name|getFields
argument_list|()
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
block|}
end_class

end_unit

