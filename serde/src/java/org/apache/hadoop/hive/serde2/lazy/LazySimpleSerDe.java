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
name|HashMap
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
name|lang
operator|.
name|ArrayUtils
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|AbstractEncodingAwareSerDe
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyObjectInspectorParameters
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
name|primitive
operator|.
name|LazyObjectInspectorParametersImpl
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
name|UnionObjectInspector
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
name|BinaryComparable
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
import|;
end_import

begin_comment
comment|/**  * LazySimpleSerDe can be used to read the same data format as  * MetadataTypedColumnsetSerDe and TCTLSeparatedProtocol.  *  * However, LazySimpleSerDe creates Objects in a lazy way, to provide better  * performance.  *  * Also LazySimpleSerDe outputs typed columns instead of treating all columns as  * String like MetadataTypedColumnsetSerDe.  */
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
block|,
name|serdeConstants
operator|.
name|FIELD_DELIM
block|,
name|serdeConstants
operator|.
name|COLLECTION_DELIM
block|,
name|serdeConstants
operator|.
name|MAPKEY_DELIM
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_FORMAT
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
block|,
name|serdeConstants
operator|.
name|ESCAPE_CHAR
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_ENCODING
block|,
name|LazySerDeParameters
operator|.
name|SERIALIZATION_EXTEND_NESTING_LEVELS
block|,
name|LazySerDeParameters
operator|.
name|SERIALIZATION_EXTEND_ADDITIONAL_NESTING_LEVELS
block|}
argument_list|)
specifier|public
class|class
name|LazySimpleSerDe
extends|extends
name|AbstractEncodingAwareSerDe
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
specifier|private
name|LazySerDeParameters
name|serdeParams
init|=
literal|null
decl_stmt|;
specifier|private
name|ObjectInspector
name|cachedObjectInspector
decl_stmt|;
specifier|private
name|long
name|serializedSize
decl_stmt|;
specifier|private
name|SerDeStats
name|stats
decl_stmt|;
specifier|private
name|boolean
name|lastOperationSerialize
decl_stmt|;
specifier|private
name|boolean
name|lastOperationDeserialize
decl_stmt|;
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
name|LazySimpleSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{   }
comment|/**    * Initialize the SerDe given the parameters. serialization.format: separator    * char or byte code (only supports byte-value up to 127) columns:    * ","-separated column names columns.types: ",", ":", or ";"-separated column    * types    *    * @see SerDe#initialize(Configuration, Properties)    */
annotation|@
name|Override
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
name|super
operator|.
name|initialize
argument_list|(
name|job
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
name|serdeParams
operator|=
operator|new
name|LazySerDeParameters
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
comment|// Create the ObjectInspectors for the fields
name|cachedObjectInspector
operator|=
name|LazyFactory
operator|.
name|createLazyStructInspector
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
name|cachedObjectInspector
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" initialized with: columnNames="
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
operator|+
literal|" lastColumnTakesRest="
operator|+
name|serdeParams
operator|.
name|isLastColumnTakesRest
argument_list|()
operator|+
literal|" timestampFormats="
operator|+
name|serdeParams
operator|.
name|getTimestampFormats
argument_list|()
argument_list|)
expr_stmt|;
name|serializedSize
operator|=
literal|0
expr_stmt|;
name|stats
operator|=
operator|new
name|SerDeStats
argument_list|()
expr_stmt|;
name|lastOperationSerialize
operator|=
literal|false
expr_stmt|;
name|lastOperationDeserialize
operator|=
literal|false
expr_stmt|;
block|}
comment|// The object for storing row data
name|LazyStruct
name|cachedLazyStruct
decl_stmt|;
comment|// The wrapper for byte array
name|ByteArrayRef
name|byteArrayRef
decl_stmt|;
comment|/**    * Deserialize a row from the Writable to a LazyObject.    *    * @param field    *          the Writable that contains the data    * @return The deserialized row Object.    * @see SerDe#deserialize(Writable)    */
annotation|@
name|Override
specifier|public
name|Object
name|doDeserialize
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
name|BinaryComparable
name|b
init|=
operator|(
name|BinaryComparable
operator|)
name|field
decl_stmt|;
name|byteArrayRef
operator|.
name|setData
argument_list|(
name|b
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
name|b
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|lastOperationSerialize
operator|=
literal|false
expr_stmt|;
name|lastOperationDeserialize
operator|=
literal|true
expr_stmt|;
return|return
name|cachedLazyStruct
return|;
block|}
comment|/**    * Returns the ObjectInspector for the row.    */
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
comment|/**    * Returns the Writable Class after serialization.    *    * @see SerDe#getSerializedClass()    */
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
comment|/**    * Serialize a row of data.    *    * @param obj    *          The row object    * @param objInspector    *          The ObjectInspector for the row object    * @return The serialized Writable object    * @throws IOException    * @see SerDe#serialize(Object, ObjectInspector)    */
annotation|@
name|Override
specifier|public
name|Writable
name|doSerialize
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
name|serializeStream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|serializedSize
operator|=
literal|0
expr_stmt|;
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
name|getSeparators
argument_list|()
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
name|serializeField
argument_list|(
name|serializeStream
argument_list|,
name|f
argument_list|,
name|foi
argument_list|,
name|serdeParams
argument_list|)
expr_stmt|;
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
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|serializedSize
operator|=
name|serializeStream
operator|.
name|getLength
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
return|return
name|serializeCache
return|;
block|}
specifier|protected
name|void
name|serializeField
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
name|LazySerDeParameters
name|serdeParams
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
name|serialize
argument_list|(
name|out
argument_list|,
name|obj
argument_list|,
name|objInspector
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
block|}
comment|/**    * Serialize the row into the StringBuilder.    *    * @param out    *          The StringBuilder to store the serialized data.    * @param obj    *          The object for the current field.    * @param objInspector    *          The ObjectInspector for the current Object.    * @param separators    *          The separators array.    * @param level    *          The current level of separator.    * @param nullSequence    *          The byte sequence representing the NULL value.    * @param escaped    *          Whether we need to escape the data when writing out    * @param escapeChar    *          Which char to use as the escape char, e.g. '\\'    * @param needsEscape    *          Which byte needs to be escaped for 256 bytes.     * @throws IOException    * @throws SerDeException    */
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
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|,
name|boolean
index|[]
name|needsEscape
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
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
name|char
name|separator
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|list
decl_stmt|;
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
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
return|return;
case|case
name|LIST
case|:
name|separator
operator|=
operator|(
name|char
operator|)
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separators
argument_list|,
name|level
argument_list|)
expr_stmt|;
name|ListObjectInspector
name|loi
init|=
operator|(
name|ListObjectInspector
operator|)
name|objInspector
decl_stmt|;
name|list
operator|=
name|loi
operator|.
name|getList
argument_list|(
name|obj
argument_list|)
expr_stmt|;
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
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
case|case
name|MAP
case|:
name|separator
operator|=
operator|(
name|char
operator|)
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separators
argument_list|,
name|level
argument_list|)
expr_stmt|;
name|char
name|keyValueSeparator
init|=
operator|(
name|char
operator|)
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separators
argument_list|,
name|level
operator|+
literal|1
argument_list|)
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
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
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
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
case|case
name|STRUCT
case|:
name|separator
operator|=
operator|(
name|char
operator|)
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separators
argument_list|,
name|level
argument_list|)
expr_stmt|;
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
name|list
operator|=
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|obj
argument_list|)
expr_stmt|;
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
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
case|case
name|UNION
case|:
name|separator
operator|=
operator|(
name|char
operator|)
name|LazyUtils
operator|.
name|getSeparator
argument_list|(
name|separators
argument_list|,
name|level
argument_list|)
expr_stmt|;
name|UnionObjectInspector
name|uoi
init|=
operator|(
name|UnionObjectInspector
operator|)
name|objInspector
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|ObjectInspector
argument_list|>
name|ois
init|=
name|uoi
operator|.
name|getObjectInspectors
argument_list|()
decl_stmt|;
if|if
condition|(
name|ois
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
name|LazyUtils
operator|.
name|writePrimitiveUTF8
argument_list|(
name|out
argument_list|,
operator|new
name|Byte
argument_list|(
name|uoi
operator|.
name|getTag
argument_list|(
name|obj
argument_list|)
argument_list|)
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaByteObjectInspector
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|separator
argument_list|)
expr_stmt|;
name|serialize
argument_list|(
name|out
argument_list|,
name|uoi
operator|.
name|getField
argument_list|(
name|obj
argument_list|)
argument_list|,
name|ois
operator|.
name|get
argument_list|(
name|uoi
operator|.
name|getTag
argument_list|(
name|obj
argument_list|)
argument_list|)
argument_list|,
name|separators
argument_list|,
name|level
operator|+
literal|1
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
block|}
return|return;
default|default:
break|break;
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
comment|/**    * Returns the statistics after (de)serialization)    */
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
name|lastOperationSerialize
operator|!=
name|lastOperationDeserialize
operator|)
assert|;
if|if
condition|(
name|lastOperationSerialize
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
name|cachedLazyStruct
operator|.
name|getRawDataSerializedSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|stats
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Writable
name|transformFromUTF8
parameter_list|(
name|Writable
name|blob
parameter_list|)
block|{
name|Text
name|text
init|=
operator|(
name|Text
operator|)
name|blob
decl_stmt|;
return|return
name|SerDeUtils
operator|.
name|transformTextFromUTF8
argument_list|(
name|text
argument_list|,
name|this
operator|.
name|charset
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Writable
name|transformToUTF8
parameter_list|(
name|Writable
name|blob
parameter_list|)
block|{
name|Text
name|text
init|=
operator|(
name|Text
operator|)
name|blob
decl_stmt|;
return|return
name|SerDeUtils
operator|.
name|transformTextToUTF8
argument_list|(
name|text
argument_list|,
name|this
operator|.
name|charset
argument_list|)
return|;
block|}
block|}
end_class

end_unit

