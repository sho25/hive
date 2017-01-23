begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
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
name|Map
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
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|serde
operator|.
name|serdeConstants
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
name|AbstractSerDe
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
name|SerDeSpec
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
name|SerDeStats
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
name|objectinspector
operator|.
name|ListObjectInspector
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
name|MapObjectInspector
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
name|PrimitiveObjectInspector
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
name|TypeInfo
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
name|TypeInfoFactory
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
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

begin_comment
comment|/**  * SerDe class for serializing to and from HCatRecord  */
end_comment

begin_class
annotation|@
name|SerDeSpec
argument_list|(
name|schemaProps
operator|=
block|{
name|serdeConstants
operator|.
name|LIST_COLUMNS
block|,
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
block|}
argument_list|)
specifier|public
class|class
name|HCatRecordSerDe
extends|extends
name|AbstractSerDe
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
name|HCatRecordSerDe
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|HCatRecordSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{   }
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
specifier|private
name|StructTypeInfo
name|rowTypeInfo
decl_stmt|;
specifier|private
name|HCatRecordObjectInspector
name|cachedObjectInspector
decl_stmt|;
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Initializing HCatRecordSerDe"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"props to serde: {}"
argument_list|,
name|tbl
operator|.
name|entrySet
argument_list|()
argument_list|)
expr_stmt|;
comment|// Get column names and types
name|String
name|columnNameProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
name|String
name|columnTypeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
specifier|final
name|String
name|columnNameDelimiter
init|=
name|tbl
operator|.
name|containsKey
argument_list|(
name|serdeConstants
operator|.
name|COLUMN_NAME_DELIMITER
argument_list|)
condition|?
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|COLUMN_NAME_DELIMITER
argument_list|)
else|:
name|String
operator|.
name|valueOf
argument_list|(
name|SerDeUtils
operator|.
name|COMMA
argument_list|)
decl_stmt|;
comment|// all table column names
if|if
condition|(
name|columnNameProperty
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnNames
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnNameProperty
operator|.
name|split
argument_list|(
name|columnNameDelimiter
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// all column types
if|if
condition|(
name|columnTypeProperty
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnTypes
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnTypes
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"columns: {} {}"
argument_list|,
name|columnNameProperty
argument_list|,
name|columnNames
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"types: {} {}"
argument_list|,
name|columnTypeProperty
argument_list|,
name|columnTypes
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|columnNames
operator|.
name|size
argument_list|()
operator|==
name|columnTypes
operator|.
name|size
argument_list|()
operator|)
assert|;
name|rowTypeInfo
operator|=
operator|(
name|StructTypeInfo
operator|)
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
expr_stmt|;
name|cachedObjectInspector
operator|=
name|HCatRecordObjectInspectorFactory
operator|.
name|getHCatRecordObjectInspector
argument_list|(
name|rowTypeInfo
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|HCatSchema
name|hsch
parameter_list|)
throws|throws
name|SerDeException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Initializing HCatRecordSerDe through HCatSchema {}."
argument_list|,
name|hsch
argument_list|)
expr_stmt|;
name|rowTypeInfo
operator|=
operator|(
name|StructTypeInfo
operator|)
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|hsch
operator|.
name|getSchemaAsTypeString
argument_list|()
argument_list|)
expr_stmt|;
name|cachedObjectInspector
operator|=
name|HCatRecordObjectInspectorFactory
operator|.
name|getHCatRecordObjectInspector
argument_list|(
name|rowTypeInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * The purpose of a deserialize method is to turn a data blob    * which is a writable representation of the data into an    * object that can then be parsed using the appropriate    * ObjectInspector. In this case, since HCatRecord is directly    * already the Writable object, there's no extra work to be done    * here. Most of the logic resides in the ObjectInspector to be    * able to return values from within the HCatRecord to hive when    * it wants it.    */
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|data
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
operator|!
operator|(
name|data
operator|instanceof
name|HCatRecord
operator|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": expects HCatRecord!"
argument_list|)
throw|;
block|}
return|return
name|data
return|;
block|}
comment|/**    * The purpose of the serialize method is to turn an object-representation    * with a provided ObjectInspector into a Writable format, which    * the underlying layer can then use to write out.    *    * In this case, it means that Hive will call this method to convert    * an object with appropriate objectinspectors that it knows about,    * to write out a HCatRecord.    */
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
return|return
operator|new
name|DefaultHCatRecord
argument_list|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|serializeStruct
argument_list|(
name|obj
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|objInspector
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Return serialized HCatRecord from an underlying    * object-representation, and readable by an ObjectInspector    * @param obj : Underlying object-representation    * @param soi : StructObjectInspector    * @return HCatRecord    */
specifier|private
specifier|static
name|List
argument_list|<
name|?
argument_list|>
name|serializeStruct
parameter_list|(
name|Object
name|obj
parameter_list|,
name|StructObjectInspector
name|soi
parameter_list|)
throws|throws
name|SerDeException
block|{
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
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|l
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fields
operator|!=
literal|null
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
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Object
name|res
init|=
name|serializeField
argument_list|(
name|f
argument_list|,
name|foi
argument_list|)
decl_stmt|;
name|l
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|res
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|l
return|;
block|}
comment|/**    * Return underlying Java Object from an object-representation    * that is readable by a provided ObjectInspector.    */
specifier|public
specifier|static
name|Object
name|serializeField
parameter_list|(
name|Object
name|field
parameter_list|,
name|ObjectInspector
name|fieldObjectInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Object
name|res
decl_stmt|;
if|if
condition|(
name|fieldObjectInspector
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
name|res
operator|=
name|serializePrimitiveField
argument_list|(
name|field
argument_list|,
name|fieldObjectInspector
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldObjectInspector
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|STRUCT
condition|)
block|{
name|res
operator|=
name|serializeStruct
argument_list|(
name|field
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|fieldObjectInspector
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldObjectInspector
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|LIST
condition|)
block|{
name|res
operator|=
name|serializeList
argument_list|(
name|field
argument_list|,
operator|(
name|ListObjectInspector
operator|)
name|fieldObjectInspector
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldObjectInspector
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|MAP
condition|)
block|{
name|res
operator|=
name|serializeMap
argument_list|(
name|field
argument_list|,
operator|(
name|MapObjectInspector
operator|)
name|fieldObjectInspector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|HCatRecordSerDe
operator|.
name|class
operator|.
name|toString
argument_list|()
operator|+
literal|" does not know what to do with fields of unknown category: "
operator|+
name|fieldObjectInspector
operator|.
name|getCategory
argument_list|()
operator|+
literal|" , type: "
operator|+
name|fieldObjectInspector
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|res
return|;
block|}
comment|/**    * Helper method to return underlying Java Map from    * an object-representation that is readable by a provided    * MapObjectInspector    */
specifier|private
specifier|static
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|serializeMap
parameter_list|(
name|Object
name|f
parameter_list|,
name|MapObjectInspector
name|moi
parameter_list|)
throws|throws
name|SerDeException
block|{
name|ObjectInspector
name|koi
init|=
name|moi
operator|.
name|getMapKeyObjectInspector
argument_list|()
decl_stmt|;
name|ObjectInspector
name|voi
init|=
name|moi
operator|.
name|getMapValueObjectInspector
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|m
init|=
operator|new
name|HashMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|readMap
init|=
name|moi
operator|.
name|getMap
argument_list|(
name|f
argument_list|)
decl_stmt|;
if|if
condition|(
name|readMap
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
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|entry
range|:
name|readMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|m
operator|.
name|put
argument_list|(
name|serializeField
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|koi
argument_list|)
argument_list|,
name|serializeField
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|voi
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|m
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|?
argument_list|>
name|serializeList
parameter_list|(
name|Object
name|f
parameter_list|,
name|ListObjectInspector
name|loi
parameter_list|)
throws|throws
name|SerDeException
block|{
name|List
name|l
init|=
name|loi
operator|.
name|getList
argument_list|(
name|f
argument_list|)
decl_stmt|;
if|if
condition|(
name|l
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ObjectInspector
name|eloi
init|=
name|loi
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
if|if
condition|(
name|eloi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|l
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|l
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|eloi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|l
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
elseif|else
if|if
condition|(
name|eloi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|STRUCT
condition|)
block|{
name|List
argument_list|<
name|List
argument_list|<
name|?
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
name|l
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|l
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|serializeStruct
argument_list|(
name|l
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|eloi
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
elseif|else
if|if
condition|(
name|eloi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|LIST
condition|)
block|{
name|List
argument_list|<
name|List
argument_list|<
name|?
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
name|l
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|l
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|serializeList
argument_list|(
name|l
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
operator|(
name|ListObjectInspector
operator|)
name|eloi
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
elseif|else
if|if
condition|(
name|eloi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|MAP
condition|)
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
argument_list|>
argument_list|(
name|l
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|l
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|serializeMap
argument_list|(
name|l
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
operator|(
name|MapObjectInspector
operator|)
name|eloi
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|HCatRecordSerDe
operator|.
name|class
operator|.
name|toString
argument_list|()
operator|+
literal|" does not know what to do with fields of unknown category: "
operator|+
name|eloi
operator|.
name|getCategory
argument_list|()
operator|+
literal|" , type: "
operator|+
name|eloi
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|Object
name|serializePrimitiveField
parameter_list|(
name|Object
name|field
parameter_list|,
name|ObjectInspector
name|fieldObjectInspector
parameter_list|)
block|{
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Object
name|f
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|fieldObjectInspector
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|!=
literal|null
operator|&&
name|HCatContext
operator|.
name|INSTANCE
operator|.
name|getConf
argument_list|()
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|Configuration
name|conf
init|=
name|HCatContext
operator|.
name|INSTANCE
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|f
operator|instanceof
name|Boolean
operator|&&
name|conf
operator|.
name|getBoolean
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DATA_CONVERT_BOOLEAN_TO_INTEGER
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DATA_CONVERT_BOOLEAN_TO_INTEGER_DEFAULT
argument_list|)
condition|)
block|{
return|return
operator|(
operator|(
name|Boolean
operator|)
name|f
operator|)
condition|?
literal|1
else|:
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|f
operator|instanceof
name|Short
operator|&&
name|conf
operator|.
name|getBoolean
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DATA_TINY_SMALL_INT_PROMOTION
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DATA_TINY_SMALL_INT_PROMOTION_DEFAULT
argument_list|)
condition|)
block|{
return|return
operator|new
name|Integer
argument_list|(
operator|(
name|Short
operator|)
name|f
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|f
operator|instanceof
name|Byte
operator|&&
name|conf
operator|.
name|getBoolean
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DATA_TINY_SMALL_INT_PROMOTION
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DATA_TINY_SMALL_INT_PROMOTION_DEFAULT
argument_list|)
condition|)
block|{
return|return
operator|new
name|Integer
argument_list|(
operator|(
name|Byte
operator|)
name|f
argument_list|)
return|;
block|}
block|}
return|return
name|f
return|;
block|}
comment|/**    * Return an object inspector that can read through the object    * that we return from deserialize(). To wit, that means we need    * to return an ObjectInspector that can read HCatRecord, given    * the type info for it during initialize(). This also means    * that this method cannot and should not be called before initialize()    */
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|cachedObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|HCatRecord
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// no support for statistics yet
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

