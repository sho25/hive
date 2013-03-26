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
name|hcatalog
operator|.
name|rcfile
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|MetaStoreUtils
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|Constants
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
name|columnar
operator|.
name|ColumnarSerDe
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
name|hadoop
operator|.
name|io
operator|.
name|WritableComparable
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|OutputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
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
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|FileOutputStorageDriver
import|;
end_import

begin_comment
comment|/**  * The storage driver for writing RCFile data through HCatOutputFormat.  */
end_comment

begin_class
specifier|public
class|class
name|RCFileOutputDriver
extends|extends
name|FileOutputStorageDriver
block|{
comment|/** The serde for serializing the HCatRecord to bytes writable */
specifier|private
name|SerDe
name|serde
decl_stmt|;
comment|/** The object inspector for the given schema */
specifier|private
name|StructObjectInspector
name|objectInspector
decl_stmt|;
comment|/** The schema for the output data */
specifier|private
name|HCatSchema
name|outputSchema
decl_stmt|;
comment|/** The cached RCFile output format instance */
specifier|private
name|OutputFormat
name|outputFormat
init|=
literal|null
decl_stmt|;
comment|/* (non-Javadoc)    * @see org.apache.hcatalog.mapreduce.HCatOutputStorageDriver#convertValue(org.apache.hcatalog.data.HCatRecord)    */
annotation|@
name|Override
specifier|public
name|Writable
name|convertValue
parameter_list|(
name|HCatRecord
name|value
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|serde
operator|.
name|serialize
argument_list|(
name|value
operator|.
name|getAll
argument_list|()
argument_list|,
name|objectInspector
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hcatalog.mapreduce.HCatOutputStorageDriver#generateKey(org.apache.hcatalog.data.HCatRecord)    */
annotation|@
name|Override
specifier|public
name|WritableComparable
argument_list|<
name|?
argument_list|>
name|generateKey
parameter_list|(
name|HCatRecord
name|value
parameter_list|)
throws|throws
name|IOException
block|{
comment|//key is not used for RCFile output
return|return
literal|null
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hcatalog.mapreduce.HCatOutputStorageDriver#getOutputFormat(java.util.Properties)    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
name|OutputFormat
argument_list|<
name|?
extends|extends
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
extends|extends
name|Writable
argument_list|>
name|getOutputFormat
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|outputFormat
operator|==
literal|null
condition|)
block|{
name|outputFormat
operator|=
operator|new
name|RCFileMapReduceOutputFormat
argument_list|()
expr_stmt|;
block|}
return|return
name|outputFormat
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hcatalog.mapreduce.HCatOutputStorageDriver#setOutputPath(org.apache.hadoop.mapreduce.JobContext, java.lang.String)    */
annotation|@
name|Override
specifier|public
name|void
name|setOutputPath
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|IOException
block|{
comment|//Not calling FileOutputFormat.setOutputPath since that requires a Job instead of JobContext
name|jobContext
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"mapred.output.dir"
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hcatalog.mapreduce.HCatOutputStorageDriver#setPartitionValues(org.apache.hadoop.mapreduce.JobContext, java.util.Map)    */
annotation|@
name|Override
specifier|public
name|void
name|setPartitionValues
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
parameter_list|)
throws|throws
name|IOException
block|{
comment|//default implementation of HCatOutputStorageDriver.getPartitionLocation will use the partition
comment|//values to generate the data location, so partition values not used here
block|}
comment|/* (non-Javadoc)    * @see org.apache.hcatalog.mapreduce.HCatOutputStorageDriver#setSchema(org.apache.hadoop.mapreduce.JobContext, org.apache.hadoop.hive.metastore.api.Schema)    */
annotation|@
name|Override
specifier|public
name|void
name|setSchema
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|HCatSchema
name|schema
parameter_list|)
throws|throws
name|IOException
block|{
name|outputSchema
operator|=
name|schema
expr_stmt|;
name|RCFileMapReduceOutputFormat
operator|.
name|setColumnNumber
argument_list|(
name|jobContext
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|schema
operator|.
name|getFields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|Properties
name|hcatProperties
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|initialize
argument_list|(
name|context
argument_list|,
name|hcatProperties
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
name|HCatUtil
operator|.
name|getFieldSchemaList
argument_list|(
name|outputSchema
operator|.
name|getFields
argument_list|()
argument_list|)
decl_stmt|;
name|hcatProperties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|LIST_COLUMNS
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|fields
argument_list|)
argument_list|)
expr_stmt|;
name|hcatProperties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnTypesFromFieldSchema
argument_list|(
name|fields
argument_list|)
argument_list|)
expr_stmt|;
comment|// setting these props to match LazySimpleSerde
name|hcatProperties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|,
literal|"\\N"
argument_list|)
expr_stmt|;
name|hcatProperties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
try|try
block|{
name|serde
operator|=
operator|new
name|ColumnarSerDe
argument_list|()
expr_stmt|;
name|serde
operator|.
name|initialize
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|hcatProperties
argument_list|)
expr_stmt|;
name|objectInspector
operator|=
name|createStructObjectInspector
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|StructObjectInspector
name|createStructObjectInspector
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|outputSchema
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid output schema specified"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HCatFieldSchema
name|hcatFieldSchema
range|:
name|outputSchema
operator|.
name|getFields
argument_list|()
control|)
block|{
name|TypeInfo
name|type
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|hcatFieldSchema
operator|.
name|getTypeString
argument_list|()
argument_list|)
decl_stmt|;
name|fieldNames
operator|.
name|add
argument_list|(
name|hcatFieldSchema
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|fieldInspectors
operator|.
name|add
argument_list|(
name|getObjectInspector
argument_list|(
name|type
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|structInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldInspectors
argument_list|)
decl_stmt|;
return|return
name|structInspector
return|;
block|}
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|(
name|TypeInfo
name|type
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|type
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
name|PrimitiveTypeInfo
name|primitiveType
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|type
decl_stmt|;
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|primitiveType
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
return|;
case|case
name|MAP
case|:
name|MapTypeInfo
name|mapType
init|=
operator|(
name|MapTypeInfo
operator|)
name|type
decl_stmt|;
name|MapObjectInspector
name|mapInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardMapObjectInspector
argument_list|(
name|getObjectInspector
argument_list|(
name|mapType
operator|.
name|getMapKeyTypeInfo
argument_list|()
argument_list|)
argument_list|,
name|getObjectInspector
argument_list|(
name|mapType
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|mapInspector
return|;
case|case
name|LIST
case|:
name|ListTypeInfo
name|listType
init|=
operator|(
name|ListTypeInfo
operator|)
name|type
decl_stmt|;
name|ListObjectInspector
name|listInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|getObjectInspector
argument_list|(
name|listType
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|listInspector
return|;
case|case
name|STRUCT
case|:
name|StructTypeInfo
name|structType
init|=
operator|(
name|StructTypeInfo
operator|)
name|type
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypes
init|=
name|structType
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|TypeInfo
name|fieldType
range|:
name|fieldTypes
control|)
block|{
name|fieldInspectors
operator|.
name|add
argument_list|(
name|getObjectInspector
argument_list|(
name|fieldType
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|structInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|structType
operator|.
name|getAllStructFieldNames
argument_list|()
argument_list|,
name|fieldInspectors
argument_list|)
decl_stmt|;
return|return
name|structInspector
return|;
default|default :
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown field schema type"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

