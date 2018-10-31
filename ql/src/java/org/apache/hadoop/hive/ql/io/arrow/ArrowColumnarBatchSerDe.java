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
name|ql
operator|.
name|io
operator|.
name|arrow
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|memory
operator|.
name|BufferAllocator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|complex
operator|.
name|impl
operator|.
name|UnionListWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|complex
operator|.
name|writer
operator|.
name|BaseWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|types
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|types
operator|.
name|Types
operator|.
name|MinorType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|types
operator|.
name|pojo
operator|.
name|ArrowType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|types
operator|.
name|pojo
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|types
operator|.
name|pojo
operator|.
name|FieldType
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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|ColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|ListColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|MapColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|StructColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|VectorAssignRow
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
name|TimestampLocalTZTypeInfo
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
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|UnionTypeInfo
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
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
import|import static
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
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
import|;
end_import

begin_comment
comment|/**  * ArrowColumnarBatchSerDe converts Apache Hive rows to Apache Arrow columns. Its serialized  * class is {@link ArrowWrapperWritable}, which doesn't support {@link  * Writable#readFields(DataInput)} and {@link Writable#write(DataOutput)}.  *  * Followings are known issues of current implementation.  *  * A list column cannot have a decimal column. {@link UnionListWriter} doesn't have an  * implementation for {@link BaseWriter.ListWriter#decimal()}.  *  * A union column can have only one of string, char, varchar fields at a same time. Apache Arrow  * doesn't have string and char, so {@link ArrowColumnarBatchSerDe} uses varchar to simulate  * string and char. They will be considered as a same data type in  * {@link org.apache.arrow.vector.complex.UnionVector}.  *  * Timestamp with local timezone is not supported. {@link VectorAssignRow} doesn't support it.  */
end_comment

begin_class
specifier|public
class|class
name|ArrowColumnarBatchSerDe
extends|extends
name|AbstractSerDe
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ArrowColumnarBatchSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_ARROW_FIELD_NAME
init|=
literal|"[DEFAULT]"
decl_stmt|;
specifier|static
specifier|final
name|int
name|MILLIS_PER_SECOND
init|=
literal|1_000
decl_stmt|;
specifier|static
specifier|final
name|int
name|MICROS_PER_SECOND
init|=
literal|1_000_000
decl_stmt|;
specifier|static
specifier|final
name|int
name|NS_PER_SECOND
init|=
literal|1_000_000_000
decl_stmt|;
specifier|static
specifier|final
name|int
name|NS_PER_MILLIS
init|=
name|NS_PER_SECOND
operator|/
name|MILLIS_PER_SECOND
decl_stmt|;
specifier|static
specifier|final
name|int
name|NS_PER_MICROS
init|=
name|NS_PER_SECOND
operator|/
name|MICROS_PER_SECOND
decl_stmt|;
specifier|static
specifier|final
name|int
name|MICROS_PER_MILLIS
init|=
name|MICROS_PER_SECOND
operator|/
name|MILLIS_PER_SECOND
decl_stmt|;
specifier|static
specifier|final
name|int
name|SECOND_PER_DAY
init|=
literal|24
operator|*
literal|60
operator|*
literal|60
decl_stmt|;
name|BufferAllocator
name|rootAllocator
decl_stmt|;
name|StructTypeInfo
name|rowTypeInfo
decl_stmt|;
name|StructObjectInspector
name|rowObjectInspector
decl_stmt|;
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|Serializer
name|serializer
decl_stmt|;
specifier|private
name|Deserializer
name|deserializer
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
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
specifier|final
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
specifier|final
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
comment|// Create an object inspector
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
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
argument_list|<>
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
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
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
argument_list|<>
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
name|rowObjectInspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|rowTypeInfo
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|Field
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|size
init|=
name|columnNames
operator|.
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|toField
argument_list|(
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
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
block|}
specifier|private
specifier|static
name|Field
name|toField
parameter_list|(
name|String
name|name
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
switch|switch
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
specifier|final
name|PrimitiveTypeInfo
name|primitiveTypeInfo
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
decl_stmt|;
switch|switch
condition|(
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|BIT
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|BYTE
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|TINYINT
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|SMALLINT
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|INT
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|INT
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|BIGINT
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|FLOAT4
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|FLOAT8
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|STRING
case|:
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|VARCHAR
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|DATE
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|DATEDAY
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|TIMESTAMPMILLI
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|TIMESTAMPLOCALTZ
case|:
specifier|final
name|TimestampLocalTZTypeInfo
name|timestampLocalTZTypeInfo
init|=
operator|(
name|TimestampLocalTZTypeInfo
operator|)
name|typeInfo
decl_stmt|;
specifier|final
name|String
name|timeZone
init|=
name|timestampLocalTZTypeInfo
operator|.
name|getTimeZone
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
operator|new
name|ArrowType
operator|.
name|Timestamp
argument_list|(
name|TimeUnit
operator|.
name|MILLISECOND
argument_list|,
name|timeZone
argument_list|)
argument_list|)
return|;
case|case
name|BINARY
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|VARBINARY
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|DECIMAL
case|:
specifier|final
name|DecimalTypeInfo
name|decimalTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
decl_stmt|;
specifier|final
name|int
name|precision
init|=
name|decimalTypeInfo
operator|.
name|precision
argument_list|()
decl_stmt|;
specifier|final
name|int
name|scale
init|=
name|decimalTypeInfo
operator|.
name|scale
argument_list|()
decl_stmt|;
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
operator|new
name|ArrowType
operator|.
name|Decimal
argument_list|(
name|precision
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
case|case
name|INTERVAL_YEAR_MONTH
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|INTERVALYEAR
operator|.
name|getType
argument_list|()
argument_list|)
return|;
case|case
name|INTERVAL_DAY_TIME
case|:
return|return
name|Field
operator|.
name|nullable
argument_list|(
name|name
argument_list|,
name|MinorType
operator|.
name|INTERVALDAY
operator|.
name|getType
argument_list|()
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
case|case
name|LIST
case|:
specifier|final
name|ListTypeInfo
name|listTypeInfo
init|=
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
decl_stmt|;
specifier|final
name|TypeInfo
name|elementTypeInfo
init|=
name|listTypeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
decl_stmt|;
return|return
operator|new
name|Field
argument_list|(
name|name
argument_list|,
name|FieldType
operator|.
name|nullable
argument_list|(
name|MinorType
operator|.
name|LIST
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|toField
argument_list|(
name|DEFAULT_ARROW_FIELD_NAME
argument_list|,
name|elementTypeInfo
argument_list|)
argument_list|)
argument_list|)
return|;
case|case
name|STRUCT
case|:
specifier|final
name|StructTypeInfo
name|structTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypeInfos
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Field
argument_list|>
name|structFields
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|final
name|int
name|structSize
init|=
name|fieldNames
operator|.
name|size
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
name|structSize
condition|;
name|i
operator|++
control|)
block|{
name|structFields
operator|.
name|add
argument_list|(
name|toField
argument_list|(
name|fieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|fieldTypeInfos
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
operator|new
name|Field
argument_list|(
name|name
argument_list|,
name|FieldType
operator|.
name|nullable
argument_list|(
name|MinorType
operator|.
name|STRUCT
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|structFields
argument_list|)
return|;
case|case
name|UNION
case|:
specifier|final
name|UnionTypeInfo
name|unionTypeInfo
init|=
operator|(
name|UnionTypeInfo
operator|)
name|typeInfo
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|objectTypeInfos
init|=
name|unionTypeInfo
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Field
argument_list|>
name|unionFields
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|final
name|int
name|unionSize
init|=
name|unionFields
operator|.
name|size
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
name|unionSize
condition|;
name|i
operator|++
control|)
block|{
name|unionFields
operator|.
name|add
argument_list|(
name|toField
argument_list|(
name|DEFAULT_ARROW_FIELD_NAME
argument_list|,
name|objectTypeInfos
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
operator|new
name|Field
argument_list|(
name|name
argument_list|,
name|FieldType
operator|.
name|nullable
argument_list|(
name|MinorType
operator|.
name|UNION
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|unionFields
argument_list|)
return|;
case|case
name|MAP
case|:
specifier|final
name|MapTypeInfo
name|mapTypeInfo
init|=
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
decl_stmt|;
specifier|final
name|TypeInfo
name|keyTypeInfo
init|=
name|mapTypeInfo
operator|.
name|getMapKeyTypeInfo
argument_list|()
decl_stmt|;
specifier|final
name|TypeInfo
name|valueTypeInfo
init|=
name|mapTypeInfo
operator|.
name|getMapValueTypeInfo
argument_list|()
decl_stmt|;
specifier|final
name|StructTypeInfo
name|mapStructTypeInfo
init|=
operator|new
name|StructTypeInfo
argument_list|()
decl_stmt|;
name|mapStructTypeInfo
operator|.
name|setAllStructFieldNames
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"keys"
argument_list|,
literal|"values"
argument_list|)
argument_list|)
expr_stmt|;
name|mapStructTypeInfo
operator|.
name|setAllStructFieldTypeInfos
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|keyTypeInfo
argument_list|,
name|valueTypeInfo
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ListTypeInfo
name|mapListStructTypeInfo
init|=
operator|new
name|ListTypeInfo
argument_list|()
decl_stmt|;
name|mapListStructTypeInfo
operator|.
name|setListElementTypeInfo
argument_list|(
name|mapStructTypeInfo
argument_list|)
expr_stmt|;
return|return
name|toField
argument_list|(
name|name
argument_list|,
name|mapListStructTypeInfo
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
block|}
specifier|static
name|ListTypeInfo
name|toStructListTypeInfo
parameter_list|(
name|MapTypeInfo
name|mapTypeInfo
parameter_list|)
block|{
specifier|final
name|StructTypeInfo
name|structTypeInfo
init|=
operator|new
name|StructTypeInfo
argument_list|()
decl_stmt|;
name|structTypeInfo
operator|.
name|setAllStructFieldNames
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"keys"
argument_list|,
literal|"values"
argument_list|)
argument_list|)
expr_stmt|;
name|structTypeInfo
operator|.
name|setAllStructFieldTypeInfos
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|mapTypeInfo
operator|.
name|getMapKeyTypeInfo
argument_list|()
argument_list|,
name|mapTypeInfo
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ListTypeInfo
name|structListTypeInfo
init|=
operator|new
name|ListTypeInfo
argument_list|()
decl_stmt|;
name|structListTypeInfo
operator|.
name|setListElementTypeInfo
argument_list|(
name|structTypeInfo
argument_list|)
expr_stmt|;
return|return
name|structListTypeInfo
return|;
block|}
specifier|static
name|ListColumnVector
name|toStructListVector
parameter_list|(
name|MapColumnVector
name|mapVector
parameter_list|)
block|{
specifier|final
name|StructColumnVector
name|structVector
decl_stmt|;
specifier|final
name|ListColumnVector
name|structListVector
decl_stmt|;
name|structVector
operator|=
operator|new
name|StructColumnVector
argument_list|()
expr_stmt|;
name|structVector
operator|.
name|fields
operator|=
operator|new
name|ColumnVector
index|[]
block|{
name|mapVector
operator|.
name|keys
block|,
name|mapVector
operator|.
name|values
block|}
expr_stmt|;
name|structListVector
operator|=
operator|new
name|ListColumnVector
argument_list|()
expr_stmt|;
name|structListVector
operator|.
name|child
operator|=
name|structVector
expr_stmt|;
name|structListVector
operator|.
name|childCount
operator|=
name|mapVector
operator|.
name|childCount
expr_stmt|;
name|structListVector
operator|.
name|isRepeating
operator|=
name|mapVector
operator|.
name|isRepeating
expr_stmt|;
name|structListVector
operator|.
name|noNulls
operator|=
name|mapVector
operator|.
name|noNulls
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|mapVector
operator|.
name|offsets
argument_list|,
literal|0
argument_list|,
name|structListVector
operator|.
name|offsets
argument_list|,
literal|0
argument_list|,
name|mapVector
operator|.
name|childCount
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|mapVector
operator|.
name|lengths
argument_list|,
literal|0
argument_list|,
name|structListVector
operator|.
name|lengths
argument_list|,
literal|0
argument_list|,
name|mapVector
operator|.
name|childCount
argument_list|)
expr_stmt|;
return|return
name|structListVector
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
name|ArrowWrapperWritable
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|ArrowWrapperWritable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
block|{
if|if
condition|(
name|serializer
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|rootAllocator
operator|=
name|RootAllocatorFactory
operator|.
name|INSTANCE
operator|.
name|getRootAllocator
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|serializer
operator|=
operator|new
name|Serializer
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to initialize serializer for ArrowColumnarBatchSerDe"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|serializer
operator|.
name|serialize
argument_list|(
name|obj
argument_list|,
name|objInspector
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|writable
parameter_list|)
block|{
if|if
condition|(
name|deserializer
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|rootAllocator
operator|=
name|RootAllocatorFactory
operator|.
name|INSTANCE
operator|.
name|getRootAllocator
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|deserializer
operator|=
operator|new
name|Deserializer
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
block|}
return|return
name|deserializer
operator|.
name|deserialize
argument_list|(
name|writable
argument_list|)
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
name|rowObjectInspector
return|;
block|}
block|}
end_class

end_unit

