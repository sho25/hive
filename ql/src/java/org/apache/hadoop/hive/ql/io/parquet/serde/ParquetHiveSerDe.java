begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|serde
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
name|Map
operator|.
name|Entry
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
name|ql
operator|.
name|io
operator|.
name|IOConstants
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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|writable
operator|.
name|BinaryWritable
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
name|typeinfo
operator|.
name|DecimalTypeInfo
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
name|ArrayWritable
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
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|Binary
import|;
end_import

begin_comment
comment|/**  *  * A ParquetHiveSerDe for Hive (with the deprecated package mapred)  *  */
end_comment

begin_class
specifier|public
class|class
name|ParquetHiveSerDe
extends|extends
name|AbstractSerDe
block|{
specifier|public
specifier|static
specifier|final
name|Text
name|MAP_KEY
init|=
operator|new
name|Text
argument_list|(
literal|"key"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Text
name|MAP_VALUE
init|=
operator|new
name|Text
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Text
name|MAP
init|=
operator|new
name|Text
argument_list|(
literal|"map"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Text
name|ARRAY
init|=
operator|new
name|Text
argument_list|(
literal|"bag"
argument_list|)
decl_stmt|;
comment|// Map precision to the number bytes needed for binary conversion.
specifier|public
specifier|static
specifier|final
name|int
name|PRECISION_TO_BYTE_COUNT
index|[]
init|=
operator|new
name|int
index|[
literal|38
index|]
decl_stmt|;
static|static
block|{
for|for
control|(
name|int
name|prec
init|=
literal|1
init|;
name|prec
operator|<=
literal|38
condition|;
name|prec
operator|++
control|)
block|{
comment|// Estimated number of bytes needed.
name|PRECISION_TO_BYTE_COUNT
index|[
name|prec
operator|-
literal|1
index|]
operator|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|Math
operator|.
name|log
argument_list|(
name|Math
operator|.
name|pow
argument_list|(
literal|10
argument_list|,
name|prec
argument_list|)
operator|-
literal|1
argument_list|)
operator|/
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
operator|+
literal|1
operator|)
operator|/
literal|8
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|SerDeStats
name|stats
decl_stmt|;
specifier|private
name|ObjectInspector
name|objInspector
decl_stmt|;
specifier|private
enum|enum
name|LAST_OPERATION
block|{
name|SERIALIZE
block|,
name|DESERIALIZE
block|,
name|UNKNOWN
block|}
specifier|private
name|LAST_OPERATION
name|status
decl_stmt|;
specifier|private
name|long
name|serializedSize
decl_stmt|;
specifier|private
name|long
name|deserializedSize
decl_stmt|;
annotation|@
name|Override
specifier|public
specifier|final
name|void
name|initialize
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|TypeInfo
name|rowTypeInfo
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
comment|// Get column names and sort order
specifier|final
name|String
name|columnNameProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|IOConstants
operator|.
name|COLUMNS
argument_list|)
decl_stmt|;
specifier|final
name|String
name|columnTypeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|IOConstants
operator|.
name|COLUMNS_TYPES
argument_list|)
decl_stmt|;
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
literal|","
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|columnNames
operator|.
name|size
argument_list|()
operator|!=
name|columnTypes
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"ParquetHiveSerde initialization failed. Number of column "
operator|+
literal|"name and column type differs. columnNames = "
operator|+
name|columnNames
operator|+
literal|", columnTypes = "
operator|+
name|columnTypes
argument_list|)
throw|;
block|}
comment|// Create row related objects
name|rowTypeInfo
operator|=
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
expr_stmt|;
name|this
operator|.
name|objInspector
operator|=
operator|new
name|ArrayWritableObjectInspector
argument_list|(
operator|(
name|StructTypeInfo
operator|)
name|rowTypeInfo
argument_list|)
expr_stmt|;
comment|// Stats part
name|stats
operator|=
operator|new
name|SerDeStats
argument_list|()
expr_stmt|;
name|serializedSize
operator|=
literal|0
expr_stmt|;
name|deserializedSize
operator|=
literal|0
expr_stmt|;
name|status
operator|=
name|LAST_OPERATION
operator|.
name|UNKNOWN
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
specifier|final
name|Writable
name|blob
parameter_list|)
throws|throws
name|SerDeException
block|{
name|status
operator|=
name|LAST_OPERATION
operator|.
name|DESERIALIZE
expr_stmt|;
name|deserializedSize
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|blob
operator|instanceof
name|ArrayWritable
condition|)
block|{
name|deserializedSize
operator|=
operator|(
operator|(
name|ArrayWritable
operator|)
name|blob
operator|)
operator|.
name|get
argument_list|()
operator|.
name|length
expr_stmt|;
return|return
name|blob
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
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
name|objInspector
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
name|ArrayWritable
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|serialize
parameter_list|(
specifier|final
name|Object
name|obj
parameter_list|,
specifier|final
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
operator|!
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|STRUCT
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Cannot serialize "
operator|+
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|+
literal|". Can only serialize a struct"
argument_list|)
throw|;
block|}
specifier|final
name|ArrayWritable
name|serializeData
init|=
name|createStruct
argument_list|(
name|obj
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|objInspector
argument_list|)
decl_stmt|;
name|serializedSize
operator|=
name|serializeData
operator|.
name|get
argument_list|()
operator|.
name|length
expr_stmt|;
name|status
operator|=
name|LAST_OPERATION
operator|.
name|SERIALIZE
expr_stmt|;
return|return
name|serializeData
return|;
block|}
specifier|private
name|ArrayWritable
name|createStruct
parameter_list|(
specifier|final
name|Object
name|obj
parameter_list|,
specifier|final
name|StructObjectInspector
name|inspector
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|inspector
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
specifier|final
name|Writable
index|[]
name|arr
init|=
operator|new
name|Writable
index|[
name|fields
operator|.
name|size
argument_list|()
index|]
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
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
specifier|final
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
specifier|final
name|Object
name|subObj
init|=
name|inspector
operator|.
name|getStructFieldData
argument_list|(
name|obj
argument_list|,
name|field
argument_list|)
decl_stmt|;
specifier|final
name|ObjectInspector
name|subInspector
init|=
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|arr
index|[
name|i
index|]
operator|=
name|createObject
argument_list|(
name|subObj
argument_list|,
name|subInspector
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|arr
argument_list|)
return|;
block|}
specifier|private
name|Writable
name|createMap
parameter_list|(
specifier|final
name|Object
name|obj
parameter_list|,
specifier|final
name|MapObjectInspector
name|inspector
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|sourceMap
init|=
name|inspector
operator|.
name|getMap
argument_list|(
name|obj
argument_list|)
decl_stmt|;
specifier|final
name|ObjectInspector
name|keyInspector
init|=
name|inspector
operator|.
name|getMapKeyObjectInspector
argument_list|()
decl_stmt|;
specifier|final
name|ObjectInspector
name|valueInspector
init|=
name|inspector
operator|.
name|getMapValueObjectInspector
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|ArrayWritable
argument_list|>
name|array
init|=
operator|new
name|ArrayList
argument_list|<
name|ArrayWritable
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|sourceMap
operator|!=
literal|null
condition|)
block|{
for|for
control|(
specifier|final
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|keyValue
range|:
name|sourceMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|Writable
name|key
init|=
name|createObject
argument_list|(
name|keyValue
operator|.
name|getKey
argument_list|()
argument_list|,
name|keyInspector
argument_list|)
decl_stmt|;
specifier|final
name|Writable
name|value
init|=
name|createObject
argument_list|(
name|keyValue
operator|.
name|getValue
argument_list|()
argument_list|,
name|valueInspector
argument_list|)
decl_stmt|;
if|if
condition|(
name|key
operator|!=
literal|null
condition|)
block|{
name|Writable
index|[]
name|arr
init|=
operator|new
name|Writable
index|[
literal|2
index|]
decl_stmt|;
name|arr
index|[
literal|0
index|]
operator|=
name|key
expr_stmt|;
name|arr
index|[
literal|1
index|]
operator|=
name|value
expr_stmt|;
name|array
operator|.
name|add
argument_list|(
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|arr
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|array
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
specifier|final
name|ArrayWritable
name|subArray
init|=
operator|new
name|ArrayWritable
argument_list|(
name|ArrayWritable
operator|.
name|class
argument_list|,
name|array
operator|.
name|toArray
argument_list|(
operator|new
name|ArrayWritable
index|[
name|array
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
operator|new
name|Writable
index|[]
block|{
name|subArray
block|}
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|ArrayWritable
name|createArray
parameter_list|(
specifier|final
name|Object
name|obj
parameter_list|,
specifier|final
name|ListObjectInspector
name|inspector
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|List
argument_list|<
name|?
argument_list|>
name|sourceArray
init|=
name|inspector
operator|.
name|getList
argument_list|(
name|obj
argument_list|)
decl_stmt|;
specifier|final
name|ObjectInspector
name|subInspector
init|=
name|inspector
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Writable
argument_list|>
name|array
init|=
operator|new
name|ArrayList
argument_list|<
name|Writable
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|sourceArray
operator|!=
literal|null
condition|)
block|{
for|for
control|(
specifier|final
name|Object
name|curObj
range|:
name|sourceArray
control|)
block|{
specifier|final
name|Writable
name|newObj
init|=
name|createObject
argument_list|(
name|curObj
argument_list|,
name|subInspector
argument_list|)
decl_stmt|;
if|if
condition|(
name|newObj
operator|!=
literal|null
condition|)
block|{
name|array
operator|.
name|add
argument_list|(
name|newObj
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|array
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
specifier|final
name|ArrayWritable
name|subArray
init|=
operator|new
name|ArrayWritable
argument_list|(
name|array
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|,
name|array
operator|.
name|toArray
argument_list|(
operator|new
name|Writable
index|[
name|array
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
operator|new
name|Writable
index|[]
block|{
name|subArray
block|}
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|Writable
name|createPrimitive
parameter_list|(
specifier|final
name|Object
name|obj
parameter_list|,
specifier|final
name|PrimitiveObjectInspector
name|inspector
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|inspector
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|VOID
case|:
return|return
literal|null
return|;
case|case
name|BOOLEAN
case|:
return|return
operator|new
name|BooleanWritable
argument_list|(
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|obj
argument_list|)
condition|?
name|Boolean
operator|.
name|TRUE
else|:
name|Boolean
operator|.
name|FALSE
argument_list|)
return|;
case|case
name|BYTE
case|:
return|return
operator|new
name|ByteWritable
argument_list|(
call|(
name|byte
call|)
argument_list|(
operator|(
name|ByteObjectInspector
operator|)
name|inspector
argument_list|)
operator|.
name|get
argument_list|(
name|obj
argument_list|)
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|DoubleWritable
argument_list|(
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|obj
argument_list|)
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
operator|new
name|FloatWritable
argument_list|(
operator|(
operator|(
name|FloatObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|obj
argument_list|)
argument_list|)
return|;
case|case
name|INT
case|:
return|return
operator|new
name|IntWritable
argument_list|(
operator|(
operator|(
name|IntObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|obj
argument_list|)
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
operator|new
name|LongWritable
argument_list|(
operator|(
operator|(
name|LongObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|get
argument_list|(
name|obj
argument_list|)
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
operator|new
name|ShortWritable
argument_list|(
call|(
name|short
call|)
argument_list|(
operator|(
name|ShortObjectInspector
operator|)
name|inspector
argument_list|)
operator|.
name|get
argument_list|(
name|obj
argument_list|)
argument_list|)
return|;
case|case
name|STRING
case|:
return|return
operator|new
name|BinaryWritable
argument_list|(
name|Binary
operator|.
name|fromString
argument_list|(
operator|(
operator|(
name|StringObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|obj
argument_list|)
argument_list|)
argument_list|)
return|;
case|case
name|DECIMAL
case|:
name|HiveDecimal
name|hd
init|=
operator|(
name|HiveDecimal
operator|)
name|inspector
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|DecimalTypeInfo
name|decTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|inspector
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|int
name|prec
init|=
name|decTypeInfo
operator|.
name|precision
argument_list|()
decl_stmt|;
name|int
name|scale
init|=
name|decTypeInfo
operator|.
name|scale
argument_list|()
decl_stmt|;
name|byte
index|[]
name|src
init|=
name|hd
operator|.
name|setScale
argument_list|(
name|scale
argument_list|)
operator|.
name|unscaledValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
comment|// Estimated number of bytes needed.
name|int
name|bytes
init|=
name|PRECISION_TO_BYTE_COUNT
index|[
name|prec
operator|-
literal|1
index|]
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
name|src
operator|.
name|length
condition|)
block|{
comment|// No padding needed.
return|return
operator|new
name|BinaryWritable
argument_list|(
name|Binary
operator|.
name|fromByteArray
argument_list|(
name|src
argument_list|)
argument_list|)
return|;
block|}
name|byte
index|[]
name|tgt
init|=
operator|new
name|byte
index|[
name|bytes
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|src
argument_list|,
literal|0
argument_list|,
name|tgt
argument_list|,
name|bytes
operator|-
name|src
operator|.
name|length
argument_list|,
name|src
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Padding leading zeroes.
return|return
operator|new
name|BinaryWritable
argument_list|(
name|Binary
operator|.
name|fromByteArray
argument_list|(
name|tgt
argument_list|)
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Unknown primitive : "
operator|+
name|inspector
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Writable
name|createObject
parameter_list|(
specifier|final
name|Object
name|obj
parameter_list|,
specifier|final
name|ObjectInspector
name|inspector
parameter_list|)
throws|throws
name|SerDeException
block|{
switch|switch
condition|(
name|inspector
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|STRUCT
case|:
return|return
name|createStruct
argument_list|(
name|obj
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|inspector
argument_list|)
return|;
case|case
name|LIST
case|:
return|return
name|createArray
argument_list|(
name|obj
argument_list|,
operator|(
name|ListObjectInspector
operator|)
name|inspector
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
name|createMap
argument_list|(
name|obj
argument_list|,
operator|(
name|MapObjectInspector
operator|)
name|inspector
argument_list|)
return|;
case|case
name|PRIMITIVE
case|:
return|return
name|createPrimitive
argument_list|(
name|obj
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|inspector
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Unknown data type"
operator|+
name|inspector
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// must be different
assert|assert
operator|(
name|status
operator|!=
name|LAST_OPERATION
operator|.
name|UNKNOWN
operator|)
assert|;
if|if
condition|(
name|status
operator|==
name|LAST_OPERATION
operator|.
name|SERIALIZE
condition|)
block|{
name|stats
operator|.
name|setRawDataSize
argument_list|(
name|serializedSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stats
operator|.
name|setRawDataSize
argument_list|(
name|deserializedSize
argument_list|)
expr_stmt|;
block|}
return|return
name|stats
return|;
block|}
block|}
end_class

end_unit

