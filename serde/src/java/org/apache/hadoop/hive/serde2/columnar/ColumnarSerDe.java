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
name|Properties
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
name|conf
operator|.
name|Configuration
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
name|ColumnProjectionUtils
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
name|SerDe
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
name|SerDeUtils
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
name|LazySimpleSerDe
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
name|LazySimpleSerDe
operator|.
name|SerDeParameters
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
name|ObjectInspector
operator|.
name|Category
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|StructTypeInfo
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
name|TypeInfoUtils
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * ColumnarSerDe is used for columnar based storage supported by RCFile.  * ColumnarSerDe differentiate from LazySimpleSerDe in:<br>  * (1) ColumnarSerDe uses a ColumnarStruct as its lazy Object<br>  * (2) ColumnarSerDe initialize ColumnarStruct's field directly. But under the  * field level, it works like LazySimpleSerDe<br>  */
end_comment

begin_class
specifier|public
class|class
name|ColumnarSerDe
extends|extends
name|ColumnarSerDeBase
block|{
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"["
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
name|serdeParams
operator|.
name|getSeparators
argument_list|()
argument_list|)
operator|+
literal|":"
operator|+
operator|(
operator|(
name|StructTypeInfo
operator|)
name|serdeParams
operator|.
name|getRowTypeInfo
argument_list|()
operator|)
operator|.
name|getAllStructFieldNames
argument_list|()
operator|+
literal|":"
operator|+
operator|(
operator|(
name|StructTypeInfo
operator|)
name|serdeParams
operator|.
name|getRowTypeInfo
argument_list|()
operator|)
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
operator|+
literal|"]"
return|;
block|}
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ColumnarSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|ColumnarSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{   }
name|SerDeParameters
name|serdeParams
init|=
literal|null
decl_stmt|;
comment|/**    * Initialize the SerDe given the parameters.    *    * @see SerDe#initialize(Configuration, Properties)    */
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|serdeParams
operator|=
name|LazySimpleSerDe
operator|.
name|initSerdeParams
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create the ObjectInspectors for the fields. Note: Currently
comment|// ColumnarObject uses same ObjectInpector as LazyStruct
name|cachedObjectInspector
operator|=
name|LazyFactory
operator|.
name|createColumnarStructInspector
argument_list|(
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getColumnTypes
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getSeparators
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|isEscaped
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getEscapeChar
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|serdeParams
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|notSkipIDs
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
operator|||
name|ColumnProjectionUtils
operator|.
name|isReadAllColumns
argument_list|(
name|conf
argument_list|)
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|notSkipIDs
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|notSkipIDs
operator|=
name|ColumnProjectionUtils
operator|.
name|getReadColumnIDs
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|cachedLazyStruct
operator|=
operator|new
name|ColumnarStruct
argument_list|(
name|cachedObjectInspector
argument_list|,
name|notSkipIDs
argument_list|,
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|initialize
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ColumnarSerDe initialized with: columnNames="
operator|+
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
operator|+
literal|" columnTypes="
operator|+
name|serdeParams
operator|.
name|getColumnTypes
argument_list|()
operator|+
literal|" separator="
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
name|serdeParams
operator|.
name|getSeparators
argument_list|()
argument_list|)
operator|+
literal|" nullstring="
operator|+
name|serdeParams
operator|.
name|getNullString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Serialize a row of data.    *    * @param obj    *          The row object    * @param objInspector    *          The ObjectInspector for the row object    * @return The serialized Writable object    * @see SerDe#serialize(Object, ObjectInspector)    */
annotation|@
name|Override
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|!=
name|Category
operator|.
name|STRUCT
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" can only serialize struct types, but we got: "
operator|+
name|objInspector
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
comment|// Prepare the field ObjectInspectors
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|objInspector
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|declaredFields
init|=
operator|(
name|serdeParams
operator|.
name|getRowTypeInfo
argument_list|()
operator|!=
literal|null
operator|&&
operator|(
operator|(
name|StructTypeInfo
operator|)
name|serdeParams
operator|.
name|getRowTypeInfo
argument_list|()
operator|)
operator|.
name|getAllStructFieldNames
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
condition|?
operator|(
operator|(
name|StructObjectInspector
operator|)
name|getObjectInspector
argument_list|()
operator|)
operator|.
name|getAllStructFieldRefs
argument_list|()
else|:
literal|null
decl_stmt|;
try|try
block|{
comment|// used for avoid extra byte copy
name|serializeStream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|serializedSize
operator|=
literal|0
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
comment|// Serialize each field
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
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
comment|// Get the field objectInspector and the field object.
name|ObjectInspector
name|foi
init|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|f
init|=
operator|(
name|list
operator|==
literal|null
condition|?
literal|null
else|:
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|)
decl_stmt|;
if|if
condition|(
name|declaredFields
operator|!=
literal|null
operator|&&
name|i
operator|>=
name|declaredFields
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error: expecting "
operator|+
name|declaredFields
operator|.
name|size
argument_list|()
operator|+
literal|" but asking for field "
operator|+
name|i
operator|+
literal|"\n"
operator|+
literal|"data="
operator|+
name|obj
operator|+
literal|"\n"
operator|+
literal|"tableType="
operator|+
name|serdeParams
operator|.
name|getRowTypeInfo
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"\n"
operator|+
literal|"dataType="
operator|+
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|objInspector
argument_list|)
argument_list|)
throw|;
block|}
comment|// If the field that is passed in is NOT a primitive, and either the
comment|// field is not declared (no schema was given at initialization), or
comment|// the field is declared as a primitive in initialization, serialize
comment|// the data to JSON string. Otherwise serialize the data in the
comment|// delimited way.
if|if
condition|(
operator|!
name|foi
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|PRIMITIVE
argument_list|)
operator|&&
operator|(
name|declaredFields
operator|==
literal|null
operator|||
name|declaredFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|PRIMITIVE
argument_list|)
operator|)
condition|)
block|{
name|LazySimpleSerDe
operator|.
name|serialize
argument_list|(
name|serializeStream
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|f
argument_list|,
name|foi
argument_list|)
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|serdeParams
operator|.
name|getSeparators
argument_list|()
argument_list|,
literal|1
argument_list|,
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|isEscaped
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getEscapeChar
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getNeedsEscape
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LazySimpleSerDe
operator|.
name|serialize
argument_list|(
name|serializeStream
argument_list|,
name|f
argument_list|,
name|foi
argument_list|,
name|serdeParams
operator|.
name|getSeparators
argument_list|()
argument_list|,
literal|1
argument_list|,
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|isEscaped
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getEscapeChar
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getNeedsEscape
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|field
index|[
name|i
index|]
operator|.
name|set
argument_list|(
name|serializeStream
operator|.
name|getData
argument_list|()
argument_list|,
name|count
argument_list|,
name|serializeStream
operator|.
name|getCount
argument_list|()
operator|-
name|count
argument_list|)
expr_stmt|;
name|count
operator|=
name|serializeStream
operator|.
name|getCount
argument_list|()
expr_stmt|;
block|}
name|serializedSize
operator|=
name|serializeStream
operator|.
name|getCount
argument_list|()
expr_stmt|;
name|lastOperationSerialize
operator|=
literal|true
expr_stmt|;
name|lastOperationDeserialize
operator|=
literal|false
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
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|serializeCache
return|;
block|}
block|}
end_class

end_unit

