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
name|serde2
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharacterCodingException
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
name|objectinspector
operator|.
name|MetadataListStructObjectInspector
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
comment|/**  * MetadataTypedColumnsetSerDe.  *  */
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
name|SERIALIZATION_FORMAT
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_FORMAT
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_LIB
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
block|}
argument_list|)
specifier|public
class|class
name|MetadataTypedColumnsetSerDe
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
name|MetadataTypedColumnsetSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DefaultSeparator
init|=
literal|"\001"
decl_stmt|;
specifier|private
name|String
name|separator
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|defaultNullString
init|=
literal|"\\N"
decl_stmt|;
specifier|private
name|String
name|nullString
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|private
name|ObjectInspector
name|cachedObjectInspector
decl_stmt|;
specifier|private
name|boolean
name|lastColumnTakesRest
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|splitLimit
init|=
operator|-
literal|1
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"MetaDataTypedColumnsetSerDe["
operator|+
name|separator
operator|+
literal|","
operator|+
name|columnNames
operator|+
literal|"]"
return|;
block|}
specifier|public
name|MetadataTypedColumnsetSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{
name|separator
operator|=
name|DefaultSeparator
expr_stmt|;
block|}
specifier|private
name|String
name|getByteValue
parameter_list|(
name|String
name|altValue
parameter_list|,
name|String
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
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
literal|1
index|]
decl_stmt|;
name|b
index|[
literal|0
index|]
operator|=
name|Byte
operator|.
name|parseByte
argument_list|(
name|altValue
argument_list|)
expr_stmt|;
return|return
operator|new
name|String
argument_list|(
name|b
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
name|altValue
return|;
block|}
block|}
return|return
name|defaultVal
return|;
block|}
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
name|String
name|altSep
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|)
decl_stmt|;
name|separator
operator|=
name|getByteValue
argument_list|(
name|altSep
argument_list|,
name|DefaultSeparator
argument_list|)
expr_stmt|;
name|String
name|altNull
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|)
decl_stmt|;
name|nullString
operator|=
name|getByteValue
argument_list|(
name|altNull
argument_list|,
name|defaultNullString
argument_list|)
expr_stmt|;
name|String
name|columnProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
literal|"columns"
argument_list|)
decl_stmt|;
name|String
name|serdeName
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_LIB
argument_list|)
decl_stmt|;
comment|// tables that were serialized with columnsetSerDe doesn't have metadata
comment|// so this hack applies to all such tables
name|boolean
name|columnsetSerDe
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|(
name|serdeName
operator|!=
literal|null
operator|)
operator|&&
name|serdeName
operator|.
name|equals
argument_list|(
literal|"org.apache.hadoop.hive.serde.thrift.columnsetSerDe"
argument_list|)
condition|)
block|{
name|columnsetSerDe
operator|=
literal|true
expr_stmt|;
block|}
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
if|if
condition|(
name|columnProperty
operator|==
literal|null
operator|||
name|columnProperty
operator|.
name|length
argument_list|()
operator|==
literal|0
operator|||
name|columnsetSerDe
condition|)
block|{
comment|// Hack for tables with no columns
comment|// Treat it as a table with a single column called "col"
name|cachedObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|ColumnSet
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
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
name|columnProperty
operator|.
name|split
argument_list|(
name|columnNameDelimiter
argument_list|)
argument_list|)
expr_stmt|;
name|cachedObjectInspector
operator|=
name|MetadataListStructObjectInspector
operator|.
name|getInstance
argument_list|(
name|columnNames
argument_list|)
expr_stmt|;
block|}
name|String
name|lastColumnTakesRestString
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
argument_list|)
decl_stmt|;
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
name|splitLimit
operator|=
operator|(
name|lastColumnTakesRest
operator|&&
name|columnNames
operator|!=
literal|null
operator|)
condition|?
name|columnNames
operator|.
name|size
argument_list|()
else|:
operator|-
literal|1
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
literal|": initialized with columnNames: "
operator|+
name|columnNames
operator|+
literal|" and separator code="
operator|+
operator|(
name|int
operator|)
name|separator
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|+
literal|" lastColumnTakesRest="
operator|+
name|lastColumnTakesRest
operator|+
literal|" splitLimit="
operator|+
name|splitLimit
argument_list|)
expr_stmt|;
block|}
comment|/**    * Split the row into columns.    *    * @param limit    *          up to limit columns will be produced (the last column takes all    *          the rest), -1 for unlimited.    * @return The ColumnSet object    * @throws Exception    */
specifier|public
specifier|static
name|Object
name|deserialize
parameter_list|(
name|ColumnSet
name|c
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|sep
parameter_list|,
name|String
name|nullString
parameter_list|,
name|int
name|limit
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|c
operator|.
name|col
operator|==
literal|null
condition|)
block|{
name|c
operator|.
name|col
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
name|c
operator|.
name|col
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|String
index|[]
name|l1
init|=
name|row
operator|.
name|split
argument_list|(
name|sep
argument_list|,
name|limit
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|l1
control|)
block|{
if|if
condition|(
name|s
operator|.
name|equals
argument_list|(
name|nullString
argument_list|)
condition|)
block|{
name|c
operator|.
name|col
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|col
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|c
operator|)
return|;
block|}
name|ColumnSet
name|deserializeCache
init|=
operator|new
name|ColumnSet
argument_list|()
decl_stmt|;
annotation|@
name|Override
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
name|String
name|row
init|=
literal|null
decl_stmt|;
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
try|try
block|{
name|row
operator|=
name|Text
operator|.
name|decode
argument_list|(
name|b
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
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
elseif|else
if|if
condition|(
name|field
operator|instanceof
name|Text
condition|)
block|{
name|row
operator|=
name|field
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|deserialize
argument_list|(
name|deserializeCache
argument_list|,
name|row
argument_list|,
name|separator
argument_list|,
name|nullString
argument_list|,
name|splitLimit
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnNames
operator|!=
literal|null
condition|)
block|{
assert|assert
operator|(
name|columnNames
operator|.
name|size
argument_list|()
operator|==
name|deserializeCache
operator|.
name|col
operator|.
name|size
argument_list|()
operator|)
assert|;
block|}
return|return
name|deserializeCache
return|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" expects Text or BytesWritable"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
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
name|fields
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
name|sb
operator|.
name|append
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
name|Object
name|column
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|obj
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fields
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
operator|==
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
comment|// For primitive object, serialize to plain string
name|sb
operator|.
name|append
argument_list|(
name|column
operator|==
literal|null
condition|?
name|nullString
else|:
name|column
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// For complex object, serialize to JSON format
name|sb
operator|.
name|append
argument_list|(
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|column
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
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|serializeCache
operator|.
name|set
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|serializeCache
return|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// no support for statistics
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

