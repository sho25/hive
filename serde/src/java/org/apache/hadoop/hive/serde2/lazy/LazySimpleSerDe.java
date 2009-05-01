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
name|lazy
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
name|ByteStream
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

begin_comment
comment|/**  * LazySimpleSerDe can be used to read the same data format as   * MetadataTypedColumnsetSerDe and TCTLSeparatedProtocol.  *   * However, LazySimpleSerDe creates Objects in a lazy way, to   * provide better performance.  *   * Also LazySimpleSerDe outputs typed columns instead of treating  * all columns as String like MetadataTypedColumnsetSerDe.  */
end_comment

begin_class
specifier|public
class|class
name|LazySimpleSerDe
implements|implements
name|SerDe
block|{
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
name|LazySimpleSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
specifier|public
specifier|static
name|byte
index|[]
name|DefaultSeparators
init|=
block|{
operator|(
name|byte
operator|)
literal|1
block|,
operator|(
name|byte
operator|)
literal|2
block|,
operator|(
name|byte
operator|)
literal|3
block|}
decl_stmt|;
specifier|private
name|ObjectInspector
name|cachedObjectInspector
decl_stmt|;
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
name|separators
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
name|rowTypeInfo
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
name|rowTypeInfo
operator|)
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
operator|+
literal|"]"
return|;
block|}
specifier|public
name|LazySimpleSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{   }
comment|/**    * Return the byte value of the number string.    * @param altValue   The string containing a number.    * @param defaultVal If the altValue does not represent a number,     *                   return the defaultVal.    */
specifier|public
specifier|static
name|byte
name|getByte
parameter_list|(
name|String
name|altValue
parameter_list|,
name|byte
name|defaultVal
parameter_list|)
block|{
if|if
condition|(
name|altValue
operator|!=
literal|null
operator|&&
name|altValue
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
try|try
block|{
return|return
name|Byte
operator|.
name|valueOf
argument_list|(
name|altValue
argument_list|)
operator|.
name|byteValue
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
operator|(
name|byte
operator|)
name|altValue
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
return|return
name|defaultVal
return|;
block|}
specifier|public
specifier|static
class|class
name|SerDeParameters
block|{
name|byte
index|[]
name|separators
init|=
name|DefaultSeparators
decl_stmt|;
name|String
name|nullString
decl_stmt|;
name|Text
name|nullSequence
decl_stmt|;
name|TypeInfo
name|rowTypeInfo
decl_stmt|;
name|boolean
name|lastColumnTakesRest
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
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
name|byte
index|[]
name|getSeparators
parameter_list|()
block|{
return|return
name|separators
return|;
block|}
specifier|public
name|String
name|getNullString
parameter_list|()
block|{
return|return
name|nullString
return|;
block|}
specifier|public
name|Text
name|getNullSequence
parameter_list|()
block|{
return|return
name|nullSequence
return|;
block|}
specifier|public
name|TypeInfo
name|getRowTypeInfo
parameter_list|()
block|{
return|return
name|rowTypeInfo
return|;
block|}
specifier|public
name|boolean
name|isLastColumnTakesRest
parameter_list|()
block|{
return|return
name|lastColumnTakesRest
return|;
block|}
block|}
name|SerDeParameters
name|serdeParams
init|=
literal|null
decl_stmt|;
comment|/**    * Initialize the SerDe given the parameters.    * serialization.format: separator char or byte code (only supports     * byte-value up to 127)    * columns:  ","-separated column names     * columns.types:  ",", ":", or ";"-separated column types    * @see SerDe#initialize(Configuration, Properties)     */
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|job
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
name|job
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
name|cachedLazyStruct
operator|=
operator|(
name|LazyStruct
operator|)
name|LazyFactory
operator|.
name|createLazyObject
argument_list|(
name|serdeParams
operator|.
name|rowTypeInfo
argument_list|)
expr_stmt|;
comment|// Create the ObjectInspectors for the fields
name|cachedObjectInspector
operator|=
name|LazyFactory
operator|.
name|createLazyStructInspector
argument_list|(
name|serdeParams
operator|.
name|columnNames
argument_list|,
name|serdeParams
operator|.
name|columnTypes
argument_list|,
name|serdeParams
operator|.
name|separators
argument_list|,
name|serdeParams
operator|.
name|nullSequence
argument_list|,
name|serdeParams
operator|.
name|lastColumnTakesRest
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"LazySimpleSerDe initialized with: columnNames="
operator|+
name|serdeParams
operator|.
name|columnNames
operator|+
literal|" columnTypes="
operator|+
name|serdeParams
operator|.
name|columnTypes
operator|+
literal|" separator="
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
name|serdeParams
operator|.
name|separators
argument_list|)
operator|+
literal|" nullstring="
operator|+
name|serdeParams
operator|.
name|nullString
operator|+
literal|" lastColumnTakesRest="
operator|+
name|serdeParams
operator|.
name|lastColumnTakesRest
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|SerDeParameters
name|initSerdeParams
parameter_list|(
name|Configuration
name|job
parameter_list|,
name|Properties
name|tbl
parameter_list|,
name|String
name|serdeName
parameter_list|)
throws|throws
name|SerDeException
block|{
name|SerDeParameters
name|serdeParams
init|=
operator|new
name|SerDeParameters
argument_list|()
decl_stmt|;
comment|// Read the separators: We use 10 levels of separators by default, but we
comment|// should change this when we allow users to specify more than 10 levels
comment|// of separators through DDL.
name|serdeParams
operator|.
name|separators
operator|=
operator|new
name|byte
index|[
literal|10
index|]
expr_stmt|;
name|serdeParams
operator|.
name|separators
index|[
literal|0
index|]
operator|=
name|getByte
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|FIELD_DELIM
argument_list|,
name|tbl
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|)
argument_list|)
argument_list|,
name|DefaultSeparators
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|serdeParams
operator|.
name|separators
index|[
literal|1
index|]
operator|=
name|getByte
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|COLLECTION_DELIM
argument_list|)
argument_list|,
name|DefaultSeparators
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|serdeParams
operator|.
name|separators
index|[
literal|2
index|]
operator|=
name|getByte
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|MAPKEY_DELIM
argument_list|)
argument_list|,
name|DefaultSeparators
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|3
init|;
name|i
operator|<
name|serdeParams
operator|.
name|separators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|serdeParams
operator|.
name|separators
index|[
name|i
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|serdeParams
operator|.
name|nullString
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|,
literal|"\\N"
argument_list|)
expr_stmt|;
name|serdeParams
operator|.
name|nullSequence
operator|=
operator|new
name|Text
argument_list|(
name|serdeParams
operator|.
name|nullString
argument_list|)
expr_stmt|;
name|String
name|lastColumnTakesRestString
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
argument_list|)
decl_stmt|;
name|serdeParams
operator|.
name|lastColumnTakesRest
operator|=
operator|(
name|lastColumnTakesRestString
operator|!=
literal|null
operator|&&
name|lastColumnTakesRestString
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"true"
argument_list|)
operator|)
expr_stmt|;
comment|// Read the configuration parameters
name|String
name|columnNameProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
literal|"columns"
argument_list|)
decl_stmt|;
comment|// NOTE: if "columns.types" is missing, all columns will be of String type
name|String
name|columnTypeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
literal|"columns.types"
argument_list|)
decl_stmt|;
comment|// Parse the configuration parameters
if|if
condition|(
name|columnNameProperty
operator|!=
literal|null
operator|&&
name|columnNameProperty
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|serdeParams
operator|.
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
else|else
block|{
name|serdeParams
operator|.
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
if|if
condition|(
name|columnTypeProperty
operator|==
literal|null
condition|)
block|{
comment|// Default type: all string
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
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
name|serdeParams
operator|.
name|columnNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Constants
operator|.
name|STRING_TYPE_NAME
argument_list|)
expr_stmt|;
block|}
name|columnTypeProperty
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|serdeParams
operator|.
name|columnTypes
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
expr_stmt|;
if|if
condition|(
name|serdeParams
operator|.
name|columnNames
operator|.
name|size
argument_list|()
operator|!=
name|serdeParams
operator|.
name|columnTypes
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|serdeName
operator|+
literal|": columns has "
operator|+
name|serdeParams
operator|.
name|columnNames
operator|.
name|size
argument_list|()
operator|+
literal|" elements while columns.types has "
operator|+
name|serdeParams
operator|.
name|columnTypes
operator|.
name|size
argument_list|()
operator|+
literal|" elements!"
argument_list|)
throw|;
block|}
comment|// Create the LazyObject for storing the rows
name|serdeParams
operator|.
name|rowTypeInfo
operator|=
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|serdeParams
operator|.
name|columnNames
argument_list|,
name|serdeParams
operator|.
name|columnTypes
argument_list|)
expr_stmt|;
return|return
name|serdeParams
return|;
block|}
comment|// The object for storing row data
name|LazyStruct
name|cachedLazyStruct
decl_stmt|;
comment|// The wrapper for byte array
name|ByteArrayRef
name|byteArrayRef
decl_stmt|;
comment|/**    * Deserialize a row from the Writable to a LazyObject.    * @param field the Writable that contains the data    * @return  The deserialized row Object.    * @see SerDe#deserialize(Writable)    */
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|field
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|byteArrayRef
operator|==
literal|null
condition|)
block|{
name|byteArrayRef
operator|=
operator|new
name|ByteArrayRef
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|instanceof
name|BytesWritable
condition|)
block|{
name|BytesWritable
name|b
init|=
operator|(
name|BytesWritable
operator|)
name|field
decl_stmt|;
comment|// For backward-compatibility with hadoop 0.17
name|byteArrayRef
operator|.
name|setData
argument_list|(
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|cachedLazyStruct
operator|.
name|init
argument_list|(
name|byteArrayRef
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|field
operator|instanceof
name|Text
condition|)
block|{
name|Text
name|t
init|=
operator|(
name|Text
operator|)
name|field
decl_stmt|;
name|byteArrayRef
operator|.
name|setData
argument_list|(
name|t
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|cachedLazyStruct
operator|.
name|init
argument_list|(
name|byteArrayRef
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
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
literal|": expects either BytesWritable or Text object!"
argument_list|)
throw|;
block|}
return|return
name|cachedLazyStruct
return|;
block|}
comment|/**    * Returns the ObjectInspector for the row.    */
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
comment|/**    * Returns the Writable Class after serialization.    * @see SerDe#getSerializedClass()    */
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
name|Text
operator|.
name|class
return|;
block|}
name|Text
name|serializeCache
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|ByteStream
operator|.
name|Output
name|serializeStream
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
comment|/**    * Serialize a row of data.    * @param obj          The row object    * @param objInspector The ObjectInspector for the row object    * @return             The serialized Writable object    * @throws IOException     * @see SerDe#serialize(Object, ObjectInspector)      */
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
name|rowTypeInfo
operator|!=
literal|null
operator|&&
operator|(
operator|(
name|StructTypeInfo
operator|)
name|serdeParams
operator|.
name|rowTypeInfo
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
name|serializeStream
operator|.
name|reset
argument_list|()
expr_stmt|;
try|try
block|{
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
comment|// Append the separator if needed.
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|serializeStream
operator|.
name|write
argument_list|(
name|serdeParams
operator|.
name|separators
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
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
name|rowTypeInfo
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
comment|// the data to JSON string.  Otherwise serialize the data in the
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
name|separators
argument_list|,
literal|1
argument_list|,
name|serdeParams
operator|.
name|nullSequence
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|separators
argument_list|,
literal|1
argument_list|,
name|serdeParams
operator|.
name|nullSequence
argument_list|)
expr_stmt|;
block|}
block|}
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
comment|// TODO: The copy of data is unnecessary, but there is no work-around
comment|// since we cannot directly set the private byte[] field inside Text.
name|serializeCache
operator|.
name|set
argument_list|(
name|serializeStream
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|serializeStream
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|serializeCache
return|;
block|}
comment|/**    * Serialize the row into the StringBuilder.    * @param out  The StringBuilder to store the serialized data.    * @param obj The object for the current field.    * @param objInspector  The ObjectInspector for the current Object.    * @param separators    The separators array.    * @param level         The current level of separator.    * @param nullSequence    The byte sequence representing the NULL value.    * @throws IOException     */
specifier|public
specifier|static
name|void
name|serialize
parameter_list|(
name|ByteStream
operator|.
name|Output
name|out
parameter_list|,
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|,
name|byte
index|[]
name|separators
parameter_list|,
name|int
name|level
parameter_list|,
name|Text
name|nullSequence
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|nullSequence
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullSequence
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
switch|switch
condition|(
name|objInspector
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
block|{
name|LazyUtils
operator|.
name|writePrimitiveUTF8
argument_list|(
name|out
argument_list|,
name|obj
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|objInspector
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|LIST
case|:
block|{
name|char
name|separator
init|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
index|]
decl_stmt|;
name|ListObjectInspector
name|loi
init|=
operator|(
name|ListObjectInspector
operator|)
name|objInspector
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|list
init|=
name|loi
operator|.
name|getList
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|ObjectInspector
name|eoi
init|=
name|loi
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|nullSequence
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullSequence
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
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
name|list
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
name|serialize
argument_list|(
name|out
argument_list|,
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|eoi
argument_list|,
name|separators
argument_list|,
name|level
operator|+
literal|1
argument_list|,
name|nullSequence
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
block|}
case|case
name|MAP
case|:
block|{
name|char
name|separator
init|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
index|]
decl_stmt|;
name|char
name|keyValueSeparator
init|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
operator|+
literal|1
index|]
decl_stmt|;
name|MapObjectInspector
name|moi
init|=
operator|(
name|MapObjectInspector
operator|)
name|objInspector
decl_stmt|;
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
name|?
argument_list|,
name|?
argument_list|>
name|map
init|=
name|moi
operator|.
name|getMap
argument_list|(
name|obj
argument_list|)
decl_stmt|;
if|if
condition|(
name|map
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|nullSequence
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullSequence
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boolean
name|first
init|=
literal|true
decl_stmt|;
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
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|write
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
name|serialize
argument_list|(
name|out
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|koi
argument_list|,
name|separators
argument_list|,
name|level
operator|+
literal|2
argument_list|,
name|nullSequence
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|keyValueSeparator
argument_list|)
expr_stmt|;
name|serialize
argument_list|(
name|out
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|voi
argument_list|,
name|separators
argument_list|,
name|level
operator|+
literal|2
argument_list|,
name|nullSequence
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
block|}
case|case
name|STRUCT
case|:
block|{
name|char
name|separator
init|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
index|]
decl_stmt|;
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
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|nullSequence
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullSequence
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
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
name|list
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
name|serialize
argument_list|(
name|out
argument_list|,
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|separators
argument_list|,
name|level
operator|+
literal|1
argument_list|,
name|nullSequence
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown category type: "
operator|+
name|objInspector
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

