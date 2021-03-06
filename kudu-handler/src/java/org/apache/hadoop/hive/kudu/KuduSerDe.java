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
name|kudu
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
name|math
operator|.
name|BigDecimal
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
name|Properties
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|common
operator|.
name|type
operator|.
name|Timestamp
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
name|utils
operator|.
name|StringUtils
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
name|io
operator|.
name|ByteWritable
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
name|io
operator|.
name|DoubleWritable
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
name|io
operator|.
name|HiveDecimalWritable
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
name|io
operator|.
name|ShortWritable
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
name|io
operator|.
name|TimestampWritableV2
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
name|BinaryObjectInspector
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
name|BooleanObjectInspector
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
name|ByteObjectInspector
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
name|DoubleObjectInspector
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
name|FloatObjectInspector
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
name|HiveDecimalObjectInspector
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
name|IntObjectInspector
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
name|LongObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|ShortObjectInspector
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
name|StringObjectInspector
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
name|TimestampObjectInspector
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
name|io
operator|.
name|BooleanWritable
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
name|BytesWritable
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
name|FloatWritable
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
name|IntWritable
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
name|LongWritable
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
name|kudu
operator|.
name|ColumnSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
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
name|kudu
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|KuduClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|PartialRow
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|kudu
operator|.
name|KuduHiveUtils
operator|.
name|createOverlayedConf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|kudu
operator|.
name|KuduHiveUtils
operator|.
name|toHiveType
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|kudu
operator|.
name|KuduStorageHandler
operator|.
name|KUDU_TABLE_NAME_KEY
import|;
end_import

begin_comment
comment|/**  * A Kudu serializer and deserializer to support reading and writing Kudu data from Hive.  */
end_comment

begin_class
annotation|@
name|SerDeSpec
argument_list|(
name|schemaProps
operator|=
block|{
name|KuduStorageHandler
operator|.
name|KUDU_TABLE_NAME_KEY
block|}
argument_list|)
specifier|public
class|class
name|KuduSerDe
extends|extends
name|AbstractSerDe
block|{
specifier|private
name|ObjectInspector
name|objectInspector
decl_stmt|;
specifier|private
name|Schema
name|schema
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|public
name|KuduSerDe
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|sysConf
parameter_list|,
name|Properties
name|tblProps
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Configuration
name|conf
init|=
name|createOverlayedConf
argument_list|(
name|sysConf
argument_list|,
name|tblProps
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|conf
operator|.
name|get
argument_list|(
name|KuduStorageHandler
operator|.
name|KUDU_TABLE_NAME_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|KUDU_TABLE_NAME_KEY
operator|+
literal|" is not set."
argument_list|)
throw|;
block|}
try|try
init|(
name|KuduClient
name|client
init|=
name|KuduHiveUtils
operator|.
name|getKuduClient
argument_list|(
name|conf
argument_list|)
init|)
block|{
if|if
condition|(
operator|!
name|client
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Kudu table does not exist: "
operator|+
name|tableName
argument_list|)
throw|;
block|}
name|schema
operator|=
name|client
operator|.
name|openTable
argument_list|(
name|tableName
argument_list|)
operator|.
name|getSchema
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
name|this
operator|.
name|objectInspector
operator|=
name|createObjectInspector
argument_list|(
name|schema
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|ObjectInspector
name|createObjectInspector
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|schema
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|ArrayList
argument_list|<>
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
argument_list|<>
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
argument_list|<>
argument_list|()
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
name|schema
operator|.
name|getColumnCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ColumnSchema
name|col
init|=
name|schema
operator|.
name|getColumnByIndex
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|PrimitiveTypeInfo
name|typeInfo
init|=
name|toHiveType
argument_list|(
name|col
operator|.
name|getType
argument_list|()
argument_list|,
name|col
operator|.
name|getTypeAttributes
argument_list|()
argument_list|)
decl_stmt|;
name|fieldNames
operator|.
name|add
argument_list|(
name|col
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|fieldInspectors
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|typeInfo
argument_list|)
argument_list|)
expr_stmt|;
name|fieldComments
operator|.
name|add
argument_list|(
name|col
operator|.
name|getComment
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldInspectors
argument_list|,
name|fieldComments
argument_list|)
return|;
block|}
specifier|public
name|Schema
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
block|{
return|return
name|objectInspector
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
name|KuduWritable
operator|.
name|class
return|;
block|}
comment|/**    * Serialize an object by navigating inside the Object with the ObjectInspector.    */
annotation|@
name|Override
specifier|public
name|KuduWritable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|objectInspector
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|STRUCT
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|objectInspector
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|writableObj
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
name|fields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|PartialRow
name|row
init|=
name|schema
operator|.
name|newPartialRow
argument_list|()
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
name|schema
operator|.
name|getColumnCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|StructField
name|field
init|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Object
name|value
init|=
name|writableObj
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|row
operator|.
name|setNull
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Type
name|type
init|=
name|schema
operator|.
name|getColumnByIndex
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
name|ObjectInspector
name|inspector
init|=
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BOOL
case|:
name|boolean
name|boolVal
init|=
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addBoolean
argument_list|(
name|i
argument_list|,
name|boolVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT8
case|:
name|byte
name|byteVal
init|=
operator|(
operator|(
name|ByteObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addByte
argument_list|(
name|i
argument_list|,
name|byteVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT16
case|:
name|short
name|shortVal
init|=
operator|(
operator|(
name|ShortObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addShort
argument_list|(
name|i
argument_list|,
name|shortVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT32
case|:
name|int
name|intVal
init|=
operator|(
operator|(
name|IntObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addInt
argument_list|(
name|i
argument_list|,
name|intVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT64
case|:
name|long
name|longVal
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addLong
argument_list|(
name|i
argument_list|,
name|longVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|UNIXTIME_MICROS
case|:
comment|// Calling toSqlTimestamp and using the addTimestamp API ensures we properly
comment|// convert Hive localDateTime to UTC.
name|java
operator|.
name|sql
operator|.
name|Timestamp
name|timestampVal
init|=
operator|(
operator|(
name|TimestampObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|value
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
decl_stmt|;
name|row
operator|.
name|addTimestamp
argument_list|(
name|i
argument_list|,
name|timestampVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|HiveDecimal
name|decimalVal
init|=
operator|(
operator|(
name|HiveDecimalObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addDecimal
argument_list|(
name|i
argument_list|,
name|decimalVal
operator|.
name|bigDecimalValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|float
name|floatVal
init|=
operator|(
operator|(
name|FloatObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addFloat
argument_list|(
name|i
argument_list|,
name|floatVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|double
name|doubleVal
init|=
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addDouble
argument_list|(
name|i
argument_list|,
name|doubleVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|String
name|stringVal
init|=
operator|(
operator|(
name|StringObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addString
argument_list|(
name|i
argument_list|,
name|stringVal
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|byte
index|[]
name|bytesVal
init|=
operator|(
operator|(
name|BinaryObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|row
operator|.
name|addBinary
argument_list|(
name|i
argument_list|,
name|bytesVal
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Unsupported column type: "
operator|+
name|type
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
return|return
operator|new
name|KuduWritable
argument_list|(
name|row
argument_list|)
return|;
block|}
comment|/**    * Deserialize an object out of a Writable blob.    */
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|writable
parameter_list|)
throws|throws
name|SerDeException
block|{
name|KuduWritable
name|input
init|=
operator|(
name|KuduWritable
operator|)
name|writable
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|output
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|schema
operator|.
name|getColumnCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
comment|// If the column isn't set, skip it.
if|if
condition|(
operator|!
name|input
operator|.
name|isSet
argument_list|(
name|i
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|Object
name|javaObj
init|=
name|input
operator|.
name|getValueObject
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ColumnSchema
name|col
init|=
name|schema
operator|.
name|getColumnByIndex
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|PrimitiveTypeInfo
name|typeInfo
init|=
name|toHiveType
argument_list|(
name|col
operator|.
name|getType
argument_list|()
argument_list|,
name|col
operator|.
name|getTypeAttributes
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|javaObj
operator|==
literal|null
condition|)
block|{
name|output
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
switch|switch
condition|(
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|output
operator|.
name|add
argument_list|(
operator|new
name|BooleanWritable
argument_list|(
operator|(
name|boolean
operator|)
name|javaObj
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|output
operator|.
name|add
argument_list|(
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
name|javaObj
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|output
operator|.
name|add
argument_list|(
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
name|javaObj
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|output
operator|.
name|add
argument_list|(
operator|new
name|IntWritable
argument_list|(
operator|(
name|int
operator|)
name|javaObj
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|output
operator|.
name|add
argument_list|(
operator|new
name|LongWritable
argument_list|(
operator|(
name|long
operator|)
name|javaObj
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|java
operator|.
name|sql
operator|.
name|Timestamp
name|sqlTs
init|=
operator|(
name|java
operator|.
name|sql
operator|.
name|Timestamp
operator|)
name|javaObj
decl_stmt|;
name|Timestamp
name|hiveTs
init|=
name|Timestamp
operator|.
name|ofEpochMilli
argument_list|(
name|sqlTs
operator|.
name|getTime
argument_list|()
argument_list|,
name|sqlTs
operator|.
name|getNanos
argument_list|()
argument_list|)
decl_stmt|;
name|output
operator|.
name|add
argument_list|(
operator|new
name|TimestampWritableV2
argument_list|(
name|hiveTs
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|HiveDecimal
name|hiveDecimal
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|(
name|BigDecimal
operator|)
name|javaObj
argument_list|)
decl_stmt|;
name|output
operator|.
name|add
argument_list|(
operator|new
name|HiveDecimalWritable
argument_list|(
name|hiveDecimal
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|output
operator|.
name|add
argument_list|(
operator|new
name|FloatWritable
argument_list|(
operator|(
name|float
operator|)
name|javaObj
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|output
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
operator|(
name|double
operator|)
name|javaObj
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|output
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
operator|(
name|String
operator|)
name|javaObj
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|output
operator|.
name|add
argument_list|(
operator|new
name|BytesWritable
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|javaObj
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Unsupported type: "
operator|+
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
return|return
name|output
return|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// No support for statistics. That seems to be a popular answer.
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

