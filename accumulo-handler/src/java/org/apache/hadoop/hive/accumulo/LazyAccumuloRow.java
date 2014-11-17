begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|accumulo
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
name|hive
operator|.
name|accumulo
operator|.
name|columns
operator|.
name|ColumnEncoding
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
name|accumulo
operator|.
name|columns
operator|.
name|ColumnMapping
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
name|accumulo
operator|.
name|columns
operator|.
name|HiveAccumuloColumnMapping
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
name|accumulo
operator|.
name|columns
operator|.
name|HiveAccumuloMapColumnMapping
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
name|accumulo
operator|.
name|columns
operator|.
name|HiveAccumuloRowIdColumnMapping
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
name|accumulo
operator|.
name|serde
operator|.
name|AccumuloRowIdFactory
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
name|LazyObjectBase
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
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  *  * Parses column tuples in each AccumuloHiveRow and creates Lazy objects for each field.  *  */
end_comment

begin_class
specifier|public
class|class
name|LazyAccumuloRow
extends|extends
name|LazyStruct
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|log
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|LazyAccumuloRow
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|AccumuloHiveRow
name|row
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|columnMappings
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|cachedList
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|AccumuloRowIdFactory
name|rowIdFactory
decl_stmt|;
specifier|public
name|LazyAccumuloRow
parameter_list|(
name|LazySimpleStructObjectInspector
name|inspector
parameter_list|)
block|{
name|super
argument_list|(
name|inspector
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|AccumuloHiveRow
name|hiveRow
parameter_list|,
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|columnMappings
parameter_list|,
name|AccumuloRowIdFactory
name|rowIdFactory
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|hiveRow
expr_stmt|;
name|this
operator|.
name|columnMappings
operator|=
name|columnMappings
expr_stmt|;
name|this
operator|.
name|rowIdFactory
operator|=
name|rowIdFactory
expr_stmt|;
name|setParsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
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
comment|// Will properly set string or binary serialization via createLazyField(...)
name|initLazyFields
argument_list|(
name|oi
operator|.
name|getAllStructFieldRefs
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|getParsed
argument_list|()
condition|)
block|{
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
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getField
parameter_list|(
name|int
name|id
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
name|id
argument_list|)
return|;
block|}
comment|/*    * split pairs by delimiter.    */
specifier|private
name|Object
name|uncheckedGetField
parameter_list|(
name|int
name|id
parameter_list|)
block|{
if|if
condition|(
operator|!
name|getFieldInited
argument_list|()
index|[
name|id
index|]
condition|)
block|{
name|ByteArrayRef
name|ref
decl_stmt|;
name|ColumnMapping
name|columnMapping
init|=
name|columnMappings
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnMapping
operator|instanceof
name|HiveAccumuloMapColumnMapping
condition|)
block|{
name|HiveAccumuloMapColumnMapping
name|mapColumnMapping
init|=
operator|(
name|HiveAccumuloMapColumnMapping
operator|)
name|columnMapping
decl_stmt|;
name|LazyAccumuloMap
name|map
init|=
operator|(
name|LazyAccumuloMap
operator|)
name|getFields
argument_list|()
index|[
name|id
index|]
decl_stmt|;
name|map
operator|.
name|init
argument_list|(
name|row
argument_list|,
name|mapColumnMapping
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|columnMapping
operator|instanceof
name|HiveAccumuloRowIdColumnMapping
condition|)
block|{
comment|// Use the rowID directly
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
name|row
operator|.
name|getRowId
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|columnMapping
operator|instanceof
name|HiveAccumuloColumnMapping
condition|)
block|{
name|HiveAccumuloColumnMapping
name|accumuloColumnMapping
init|=
operator|(
name|HiveAccumuloColumnMapping
operator|)
name|columnMapping
decl_stmt|;
comment|// Use the colfam and colqual to get the value
name|byte
index|[]
name|val
init|=
name|row
operator|.
name|getValue
argument_list|(
operator|new
name|Text
argument_list|(
name|accumuloColumnMapping
operator|.
name|getColumnFamily
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
name|accumuloColumnMapping
operator|.
name|getColumnQualifier
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
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
name|val
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|log
operator|.
name|error
argument_list|(
literal|"Could not process ColumnMapping of type "
operator|+
name|columnMapping
operator|.
name|getClass
argument_list|()
operator|+
literal|" at offset "
operator|+
name|id
operator|+
literal|" in column mapping: "
operator|+
name|columnMapping
operator|.
name|getMappingSpec
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot process ColumnMapping of type "
operator|+
name|columnMapping
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
name|getFields
argument_list|()
index|[
name|id
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
comment|// HIVE-3179 only init the field when it isn't null
name|getFieldInited
argument_list|()
index|[
name|id
index|]
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|getFields
argument_list|()
index|[
name|id
index|]
operator|.
name|getObject
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|cachedList
operator|.
name|clear
argument_list|()
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
specifier|final
name|ColumnMapping
name|columnMapping
init|=
name|columnMappings
operator|.
name|get
argument_list|(
name|fieldID
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnMapping
operator|instanceof
name|HiveAccumuloRowIdColumnMapping
condition|)
block|{
return|return
name|rowIdFactory
operator|.
name|createRowId
argument_list|(
name|fieldRef
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|columnMapping
operator|instanceof
name|HiveAccumuloMapColumnMapping
condition|)
block|{
return|return
operator|new
name|LazyAccumuloMap
argument_list|(
operator|(
name|LazyMapObjectInspector
operator|)
name|fieldRef
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
return|;
block|}
else|else
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
argument_list|,
name|ColumnEncoding
operator|.
name|BINARY
operator|==
name|columnMapping
operator|.
name|getEncoding
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

