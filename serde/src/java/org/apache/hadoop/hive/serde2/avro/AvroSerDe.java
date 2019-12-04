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
name|common
operator|.
name|StringInternUtils
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

begin_comment
comment|/**  * Read or write Avro data from Hive.  */
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
name|AvroSerDe
operator|.
name|LIST_COLUMN_COMMENTS
block|,
name|AvroSerDe
operator|.
name|TABLE_NAME
block|,
name|AvroSerDe
operator|.
name|TABLE_COMMENT
block|,
name|AvroSerdeUtils
operator|.
name|SCHEMA_LITERAL
block|,
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
block|,
name|AvroSerdeUtils
operator|.
name|SCHEMA_NAMESPACE
block|,
name|AvroSerdeUtils
operator|.
name|SCHEMA_NAME
block|,
name|AvroSerdeUtils
operator|.
name|SCHEMA_DOC
block|}
argument_list|)
specifier|public
class|class
name|AvroSerDe
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
name|AvroSerDe
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TABLE_COMMENT
init|=
literal|"comment"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LIST_COLUMN_COMMENTS
init|=
literal|"columns.comments"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DECIMAL_TYPE_NAME
init|=
literal|"decimal"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CHAR_TYPE_NAME
init|=
literal|"char"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|VARCHAR_TYPE_NAME
init|=
literal|"varchar"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DATE_TYPE_NAME
init|=
literal|"date"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TIMESTAMP_TYPE_NAME
init|=
literal|"timestamp-millis"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WRITER_TIME_ZONE
init|=
literal|"writer.time.zone"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WRITER_PROLEPTIC
init|=
literal|"writer.proleptic"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_PROP_LOGICAL_TYPE
init|=
literal|"logicalType"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_PROP_PRECISION
init|=
literal|"precision"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_PROP_SCALE
init|=
literal|"scale"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_PROP_MAX_LENGTH
init|=
literal|"maxLength"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_STRING_TYPE_NAME
init|=
literal|"string"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_INT_TYPE_NAME
init|=
literal|"int"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_LONG_TYPE_NAME
init|=
literal|"long"
decl_stmt|;
specifier|private
name|ObjectInspector
name|oi
decl_stmt|;
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
name|Schema
name|schema
decl_stmt|;
specifier|private
name|AvroDeserializer
name|avroDeserializer
init|=
literal|null
decl_stmt|;
specifier|private
name|AvroSerializer
name|avroSerializer
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|badSchema
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Properties
name|tableProperties
parameter_list|,
name|Properties
name|partitionProperties
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Avro should always use the table properties for initialization (see HIVE-6835).
name|initialize
argument_list|(
name|configuration
argument_list|,
name|tableProperties
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Properties
name|properties
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Reset member variables so we don't get in a half-constructed state
if|if
condition|(
name|schema
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Resetting already initialized AvroSerDe"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"AvroSerde::initialize(): Preset value of avro.schema.literal == "
operator|+
name|properties
operator|.
name|get
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|schema
operator|=
literal|null
expr_stmt|;
name|oi
operator|=
literal|null
expr_stmt|;
name|columnNames
operator|=
literal|null
expr_stmt|;
name|columnTypes
operator|=
literal|null
expr_stmt|;
specifier|final
name|String
name|columnNameProperty
init|=
name|properties
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
name|properties
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
name|columnCommentProperty
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|LIST_COLUMN_COMMENTS
argument_list|,
literal|""
argument_list|)
decl_stmt|;
specifier|final
name|String
name|columnNameDelimiter
init|=
name|properties
operator|.
name|containsKey
argument_list|(
name|serdeConstants
operator|.
name|COLUMN_NAME_DELIMITER
argument_list|)
condition|?
name|properties
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
name|boolean
name|gotColTypesFromColProps
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|hasExternalSchema
argument_list|(
name|properties
argument_list|)
operator|||
name|columnNameProperty
operator|==
literal|null
operator|||
name|columnNameProperty
operator|.
name|isEmpty
argument_list|()
operator|||
name|columnTypeProperty
operator|==
literal|null
operator|||
name|columnTypeProperty
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|schema
operator|=
name|determineSchemaOrReturnErrorSchema
argument_list|(
name|configuration
argument_list|,
name|properties
argument_list|)
expr_stmt|;
name|gotColTypesFromColProps
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
comment|// Get column names and sort order
name|columnNames
operator|=
name|StringInternUtils
operator|.
name|internStringsInList
argument_list|(
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
argument_list|)
expr_stmt|;
name|columnTypes
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
expr_stmt|;
name|schema
operator|=
name|getSchemaFromCols
argument_list|(
name|properties
argument_list|,
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|columnCommentProperty
argument_list|)
expr_stmt|;
block|}
name|properties
operator|.
name|setProperty
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|,
name|schema
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Avro schema is "
operator|+
name|schema
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|configuration
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Configuration null, not inserting schema"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|configuration
operator|.
name|set
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|AVRO_SERDE_SCHEMA
operator|.
name|getPropName
argument_list|()
argument_list|,
name|schema
operator|.
name|toString
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|badSchema
operator|=
name|schema
operator|.
name|equals
argument_list|(
name|SchemaResolutionProblem
operator|.
name|SIGNAL_BAD_SCHEMA
argument_list|)
expr_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|this
operator|.
name|columnNames
operator|=
name|StringInternUtils
operator|.
name|internStringsInList
argument_list|(
name|aoig
operator|.
name|getColumnNames
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnTypes
operator|=
name|aoig
operator|.
name|getColumnTypes
argument_list|()
expr_stmt|;
name|this
operator|.
name|oi
operator|=
name|aoig
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
comment|// HIVE-22595: Update the column/type properties to reflect the  current, since the
comment|// these properties may be used
if|if
condition|(
operator|!
name|gotColTypesFromColProps
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Updating column name/type properties based on current schema"
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|String
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|columnNames
argument_list|)
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|String
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|TypeInfoUtils
operator|.
name|getTypeStringsFromTypeInfo
argument_list|(
name|columnTypes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|badSchema
condition|)
block|{
name|this
operator|.
name|avroSerializer
operator|=
operator|new
name|AvroSerializer
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|this
operator|.
name|avroDeserializer
operator|=
operator|new
name|AvroDeserializer
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|hasExternalSchema
parameter_list|(
name|Properties
name|properties
parameter_list|)
block|{
return|return
name|properties
operator|.
name|getProperty
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|)
operator|!=
literal|null
operator|||
name|properties
operator|.
name|getProperty
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|)
operator|!=
literal|null
return|;
block|}
specifier|private
name|boolean
name|hasExternalSchema
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableParams
parameter_list|)
block|{
return|return
name|tableParams
operator|.
name|containsKey
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|)
operator|||
name|tableParams
operator|.
name|containsKey
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Schema
name|getSchemaFromCols
parameter_list|(
name|Properties
name|properties
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
parameter_list|,
name|String
name|columnCommentProperty
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|columnComments
decl_stmt|;
if|if
condition|(
name|columnCommentProperty
operator|==
literal|null
operator|||
name|columnCommentProperty
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|columnComments
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
comment|//Comments are separated by "\0" in columnCommentProperty, see method getSchema
comment|//in MetaStoreUtils where this string columns.comments is generated
name|columnComments
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnCommentProperty
operator|.
name|split
argument_list|(
literal|"\0"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"columnComments is "
operator|+
name|columnCommentProperty
argument_list|)
expr_stmt|;
block|}
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
literal|"AvroSerde initialization failed. Number of column "
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
specifier|final
name|String
name|tableName
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
specifier|final
name|String
name|tableComment
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|TABLE_COMMENT
argument_list|)
decl_stmt|;
name|TypeInfoToSchema
name|typeInfoToSchema
init|=
operator|new
name|TypeInfoToSchema
argument_list|()
decl_stmt|;
return|return
name|typeInfoToSchema
operator|.
name|convert
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|columnComments
argument_list|,
name|properties
operator|.
name|getProperty
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_NAMESPACE
operator|.
name|getPropName
argument_list|()
argument_list|)
argument_list|,
name|properties
operator|.
name|getProperty
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_NAME
operator|.
name|getPropName
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|,
name|properties
operator|.
name|getProperty
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_DOC
operator|.
name|getPropName
argument_list|()
argument_list|,
name|tableComment
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Attempt to determine the schema via the usual means, but do not throw    * an exception if we fail.  Instead, signal failure via a special    * schema.  This is used because Hive calls init on the serde during    * any call, including calls to update the serde properties, meaning    * if the serde is in a bad state, there is no way to update that state.    */
specifier|public
name|Schema
name|determineSchemaOrReturnErrorSchema
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|props
parameter_list|)
block|{
try|try
block|{
name|configErrors
operator|=
literal|""
expr_stmt|;
return|return
name|AvroSerdeUtils
operator|.
name|determineSchemaOrThrowException
argument_list|(
name|conf
argument_list|,
name|props
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|AvroSerdeException
name|he
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Encountered AvroSerdeException determining schema. Returning "
operator|+
literal|"signal schema to indicate problem"
argument_list|,
name|he
argument_list|)
expr_stmt|;
name|configErrors
operator|=
operator|new
name|String
argument_list|(
literal|"Encountered AvroSerdeException determining schema. Returning "
operator|+
literal|"signal schema to indicate problem: "
operator|+
name|he
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|schema
operator|=
name|SchemaResolutionProblem
operator|.
name|SIGNAL_BAD_SCHEMA
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Encountered exception determining schema. Returning signal "
operator|+
literal|"schema to indicate problem"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|configErrors
operator|=
operator|new
name|String
argument_list|(
literal|"Encountered exception determining schema. Returning signal "
operator|+
literal|"schema to indicate problem: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|SchemaResolutionProblem
operator|.
name|SIGNAL_BAD_SCHEMA
return|;
block|}
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
name|AvroGenericRecordWritable
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
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|badSchema
condition|)
block|{
throw|throw
operator|new
name|BadSchemaException
argument_list|()
throw|;
block|}
return|return
name|avroSerializer
operator|.
name|serialize
argument_list|(
name|o
argument_list|,
name|objectInspector
argument_list|,
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|schema
argument_list|)
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
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|badSchema
condition|)
block|{
throw|throw
operator|new
name|BadSchemaException
argument_list|()
throw|;
block|}
return|return
name|avroDeserializer
operator|.
name|deserialize
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|writable
argument_list|,
name|schema
argument_list|)
return|;
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
name|oi
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
annotation|@
name|Override
specifier|public
name|boolean
name|shouldStoreFieldsInMetastore
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableParams
parameter_list|)
block|{
return|return
operator|!
name|hasExternalSchema
argument_list|(
name|tableParams
argument_list|)
return|;
block|}
block|}
end_class

end_unit

