begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|avro
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
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
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
name|ObjectInspectorFactory
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
name|ListTypeInfo
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
name|MapTypeInfo
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
name|PrimitiveTypeInfo
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
name|UnionTypeInfo
import|;
end_import

begin_comment
comment|/**  * An AvroObjectInspectorGenerator takes an Avro schema and creates the three  * data structures Hive needs to work with Avro-encoded data:  *   * A list of the schema field names  *   * A list of those fields equivalent types in Hive  *   * An ObjectInspector capable of working with an instance of that datum.  */
end_comment

begin_class
specifier|public
class|class
name|AvroObjectInspectorGenerator
block|{
specifier|final
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|final
specifier|private
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
specifier|final
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnComments
decl_stmt|;
specifier|final
specifier|private
name|ObjectInspector
name|oi
decl_stmt|;
specifier|public
name|AvroObjectInspectorGenerator
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|SerDeException
block|{
name|verifySchemaIsARecord
argument_list|(
name|schema
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnNames
operator|=
name|AvroObjectInspectorGenerator
operator|.
name|generateColumnNames
argument_list|(
name|schema
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnTypes
operator|=
name|SchemaToTypeInfo
operator|.
name|generateColumnTypes
argument_list|(
name|schema
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnComments
operator|=
name|AvroObjectInspectorGenerator
operator|.
name|generateColumnComments
argument_list|(
name|schema
argument_list|)
expr_stmt|;
assert|assert
name|columnNames
operator|.
name|size
argument_list|()
operator|==
name|columnTypes
operator|.
name|size
argument_list|()
assert|;
name|this
operator|.
name|oi
operator|=
name|createObjectInspector
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|verifySchemaIsARecord
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
operator|!
name|schema
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|RECORD
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Schema for table must be of type RECORD. "
operator|+
literal|"Received type: "
operator|+
name|schema
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColumnNames
parameter_list|()
block|{
return|return
name|columnNames
return|;
block|}
specifier|public
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|getColumnTypes
parameter_list|()
block|{
return|return
name|columnTypes
return|;
block|}
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
block|{
return|return
name|oi
return|;
block|}
specifier|private
name|ObjectInspector
name|createObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|columnOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|columnNames
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// At this point we've verified the types are correct.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|columnNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|columnOIs
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|createObjectInspectorWorker
argument_list|(
name|columnTypes
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
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|columnOIs
argument_list|,
name|columnComments
argument_list|)
return|;
block|}
specifier|private
name|ObjectInspector
name|createObjectInspectorWorker
parameter_list|(
name|TypeInfo
name|ti
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// We don't need to do the check for U[T,Null] here because we'll give the real type
comment|// at deserialization and the object inspector will never see the actual union.
if|if
condition|(
operator|!
name|supportedCategories
argument_list|(
name|ti
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Don't yet support this type: "
operator|+
name|ti
argument_list|)
throw|;
block|}
name|ObjectInspector
name|result
decl_stmt|;
switch|switch
condition|(
name|ti
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
name|PrimitiveTypeInfo
name|pti
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|ti
decl_stmt|;
name|result
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|pti
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRUCT
case|:
name|StructTypeInfo
name|sti
init|=
operator|(
name|StructTypeInfo
operator|)
name|ti
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|sti
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TypeInfo
name|typeInfo
range|:
name|sti
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
control|)
block|{
name|ois
operator|.
name|add
argument_list|(
name|createObjectInspectorWorker
argument_list|(
name|typeInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|sti
operator|.
name|getAllStructFieldNames
argument_list|()
argument_list|,
name|ois
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP
case|:
name|MapTypeInfo
name|mti
init|=
operator|(
name|MapTypeInfo
operator|)
name|ti
decl_stmt|;
name|result
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardMapObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|STRING
argument_list|)
argument_list|,
name|createObjectInspectorWorker
argument_list|(
name|mti
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
name|ListTypeInfo
name|ati
init|=
operator|(
name|ListTypeInfo
operator|)
name|ti
decl_stmt|;
name|result
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|createObjectInspectorWorker
argument_list|(
name|ati
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|UNION
case|:
name|UnionTypeInfo
name|uti
init|=
operator|(
name|UnionTypeInfo
operator|)
name|ti
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|allUnionObjectTypeInfos
init|=
name|uti
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|unionObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|allUnionObjectTypeInfos
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TypeInfo
name|typeInfo
range|:
name|allUnionObjectTypeInfos
control|)
block|{
name|unionObjectInspectors
operator|.
name|add
argument_list|(
name|createObjectInspectorWorker
argument_list|(
name|typeInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardUnionObjectInspector
argument_list|(
name|unionObjectInspectors
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"No Hive categories matched: "
operator|+
name|ti
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|boolean
name|supportedCategories
parameter_list|(
name|TypeInfo
name|ti
parameter_list|)
block|{
specifier|final
name|ObjectInspector
operator|.
name|Category
name|c
init|=
name|ti
operator|.
name|getCategory
argument_list|()
decl_stmt|;
return|return
name|c
operator|.
name|equals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
argument_list|)
operator|||
name|c
operator|.
name|equals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|MAP
argument_list|)
operator|||
name|c
operator|.
name|equals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|LIST
argument_list|)
operator|||
name|c
operator|.
name|equals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|STRUCT
argument_list|)
operator|||
name|c
operator|.
name|equals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|UNION
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|generateColumnNames
parameter_list|(
name|Schema
name|schema
parameter_list|)
block|{
name|List
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
name|fields
init|=
name|schema
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldsList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Schema
operator|.
name|Field
name|field
range|:
name|fields
control|)
block|{
name|fieldsList
operator|.
name|add
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|fieldsList
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|generateColumnComments
parameter_list|(
name|Schema
name|schema
parameter_list|)
block|{
name|List
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
name|fields
init|=
name|schema
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldComments
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Schema
operator|.
name|Field
name|field
range|:
name|fields
control|)
block|{
name|String
name|fieldComment
init|=
name|field
operator|.
name|doc
argument_list|()
operator|==
literal|null
condition|?
literal|""
else|:
name|field
operator|.
name|doc
argument_list|()
decl_stmt|;
name|fieldComments
operator|.
name|add
argument_list|(
name|fieldComment
argument_list|)
expr_stmt|;
block|}
return|return
name|fieldComments
return|;
block|}
block|}
end_class

end_unit

