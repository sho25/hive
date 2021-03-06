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
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
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
name|annotations
operator|.
name|VisibleForTesting
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
name|MetaStoreUtils
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
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|LazySerDeParameters
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
name|LazySimpleSerDe
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
name|LazySimpleStructObjectInspector
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
name|io
operator|.
name|BytesWritable
import|;
end_import

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
name|io
operator|.
name|UnsupportedEncodingException
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

begin_comment
comment|/**  * Streaming Writer handles delimited input (eg. CSV).  * Delimited input is parsed&amp; reordered to match column order in table  * Uses Lazy Simple Serde to process delimited input  * @deprecated as of Hive 3.0.0, replaced by org.apache.hive.streaming.StrictDelimitedInputWriter  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
class|class
name|DelimitedInputWriter
extends|extends
name|AbstractRecordWriter
block|{
specifier|private
specifier|final
name|boolean
name|reorderingNeeded
decl_stmt|;
specifier|private
name|String
name|delimiter
decl_stmt|;
specifier|private
name|char
name|serdeSeparator
decl_stmt|;
specifier|private
name|int
index|[]
name|fieldToColMapping
decl_stmt|;
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|String
argument_list|>
name|tableColumns
decl_stmt|;
specifier|private
name|LazySimpleSerDe
name|serde
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|LazySimpleStructObjectInspector
name|recordObjInspector
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspector
index|[]
name|bucketObjInspectors
decl_stmt|;
specifier|private
specifier|final
name|StructField
index|[]
name|bucketStructFields
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|DelimitedInputWriter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/** Constructor. Uses default separator of the LazySimpleSerde    * @param colNamesForFields Column name assignment for input fields. nulls or empty    *                          strings in the array indicates the fields to be skipped    * @param delimiter input field delimiter    * @param endPoint Hive endpoint    * @throws ConnectionError Problem talking to Hive    * @throws ClassNotFoundException Serde class not found    * @throws SerializationError Serde initialization/interaction failed    * @throws StreamingException Problem acquiring file system path for partition    * @throws InvalidColumn any element in colNamesForFields refers to a non existing column    */
specifier|public
name|DelimitedInputWriter
parameter_list|(
name|String
index|[]
name|colNamesForFields
parameter_list|,
name|String
name|delimiter
parameter_list|,
name|HiveEndPoint
name|endPoint
parameter_list|,
name|StreamingConnection
name|conn
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|ConnectionError
throws|,
name|SerializationError
throws|,
name|InvalidColumn
throws|,
name|StreamingException
block|{
name|this
argument_list|(
name|colNamesForFields
argument_list|,
name|delimiter
argument_list|,
name|endPoint
argument_list|,
literal|null
argument_list|,
name|conn
argument_list|)
expr_stmt|;
block|}
comment|/** Constructor. Uses default separator of the LazySimpleSerde   * @param colNamesForFields Column name assignment for input fields. nulls or empty   *                          strings in the array indicates the fields to be skipped   * @param delimiter input field delimiter   * @param endPoint Hive endpoint   * @param conf a Hive conf object. Can be null if not using advanced hive settings.   * @throws ConnectionError Problem talking to Hive   * @throws ClassNotFoundException Serde class not found   * @throws SerializationError Serde initialization/interaction failed   * @throws StreamingException Problem acquiring file system path for partition   * @throws InvalidColumn any element in colNamesForFields refers to a non existing column   */
specifier|public
name|DelimitedInputWriter
parameter_list|(
name|String
index|[]
name|colNamesForFields
parameter_list|,
name|String
name|delimiter
parameter_list|,
name|HiveEndPoint
name|endPoint
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|StreamingConnection
name|conn
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|ConnectionError
throws|,
name|SerializationError
throws|,
name|InvalidColumn
throws|,
name|StreamingException
block|{
name|this
argument_list|(
name|colNamesForFields
argument_list|,
name|delimiter
argument_list|,
name|endPoint
argument_list|,
name|conf
argument_list|,
operator|(
name|char
operator|)
name|LazySerDeParameters
operator|.
name|DefaultSeparators
index|[
literal|0
index|]
argument_list|,
name|conn
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor. Allows overriding separator of the LazySimpleSerde    * @param colNamesForFields Column name assignment for input fields    * @param delimiter input field delimiter    * @param endPoint Hive endpoint    * @param conf a Hive conf object. Set to null if not using advanced hive settings.    * @param serdeSeparator separator used when encoding data that is fed into the    *                             LazySimpleSerde. Ensure this separator does not occur    *                             in the field data    * @param conn connection this Writer is to be used with    * @throws ConnectionError Problem talking to Hive    * @throws ClassNotFoundException Serde class not found    * @throws SerializationError Serde initialization/interaction failed    * @throws StreamingException Problem acquiring file system path for partition    * @throws InvalidColumn any element in colNamesForFields refers to a non existing column    */
specifier|public
name|DelimitedInputWriter
parameter_list|(
name|String
index|[]
name|colNamesForFields
parameter_list|,
name|String
name|delimiter
parameter_list|,
name|HiveEndPoint
name|endPoint
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|char
name|serdeSeparator
parameter_list|,
name|StreamingConnection
name|conn
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|ConnectionError
throws|,
name|SerializationError
throws|,
name|InvalidColumn
throws|,
name|StreamingException
block|{
name|super
argument_list|(
name|endPoint
argument_list|,
name|conf
argument_list|,
name|conn
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableColumns
operator|=
name|getCols
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
name|this
operator|.
name|serdeSeparator
operator|=
name|serdeSeparator
expr_stmt|;
name|this
operator|.
name|delimiter
operator|=
name|delimiter
expr_stmt|;
name|this
operator|.
name|fieldToColMapping
operator|=
name|getFieldReordering
argument_list|(
name|colNamesForFields
argument_list|,
name|getTableColumns
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|reorderingNeeded
operator|=
name|isReorderingNeeded
argument_list|(
name|delimiter
argument_list|,
name|getTableColumns
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Field reordering needed = "
operator|+
name|this
operator|.
name|reorderingNeeded
operator|+
literal|", for endpoint "
operator|+
name|endPoint
argument_list|)
expr_stmt|;
name|this
operator|.
name|serdeSeparator
operator|=
name|serdeSeparator
expr_stmt|;
name|this
operator|.
name|serde
operator|=
name|createSerde
argument_list|(
name|tbl
argument_list|,
name|conf
argument_list|,
name|serdeSeparator
argument_list|)
expr_stmt|;
comment|// get ObjInspectors for entire record and bucketed cols
try|try
block|{
name|this
operator|.
name|recordObjInspector
operator|=
operator|(
name|LazySimpleStructObjectInspector
operator|)
name|serde
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
name|this
operator|.
name|bucketObjInspectors
operator|=
name|getObjectInspectorsForBucketedCols
argument_list|(
name|bucketIds
argument_list|,
name|recordObjInspector
argument_list|)
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
name|SerializationError
argument_list|(
literal|"Unable to get ObjectInspector for bucket columns"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// get StructFields for bucketed cols
name|bucketStructFields
operator|=
operator|new
name|StructField
index|[
name|bucketIds
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|allFields
init|=
name|recordObjInspector
operator|.
name|getAllStructFieldRefs
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
name|bucketIds
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|bucketStructFields
index|[
name|i
index|]
operator|=
name|allFields
operator|.
name|get
argument_list|(
name|bucketIds
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @deprecated As of release 1.3/2.1.  Replaced by {@link #DelimitedInputWriter(String[], String, HiveEndPoint, StreamingConnection)}    */
specifier|public
name|DelimitedInputWriter
parameter_list|(
name|String
index|[]
name|colNamesForFields
parameter_list|,
name|String
name|delimiter
parameter_list|,
name|HiveEndPoint
name|endPoint
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|ConnectionError
throws|,
name|SerializationError
throws|,
name|InvalidColumn
throws|,
name|StreamingException
block|{
name|this
argument_list|(
name|colNamesForFields
argument_list|,
name|delimiter
argument_list|,
name|endPoint
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated As of release 1.3/2.1.  Replaced by {@link #DelimitedInputWriter(String[], String, HiveEndPoint, HiveConf, StreamingConnection)}    */
specifier|public
name|DelimitedInputWriter
parameter_list|(
name|String
index|[]
name|colNamesForFields
parameter_list|,
name|String
name|delimiter
parameter_list|,
name|HiveEndPoint
name|endPoint
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|ConnectionError
throws|,
name|SerializationError
throws|,
name|InvalidColumn
throws|,
name|StreamingException
block|{
name|this
argument_list|(
name|colNamesForFields
argument_list|,
name|delimiter
argument_list|,
name|endPoint
argument_list|,
name|conf
argument_list|,
operator|(
name|char
operator|)
name|LazySerDeParameters
operator|.
name|DefaultSeparators
index|[
literal|0
index|]
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated As of release 1.3/2.1.  Replaced by {@link #DelimitedInputWriter(String[], String, HiveEndPoint, HiveConf, char, StreamingConnection)}    */
specifier|public
name|DelimitedInputWriter
parameter_list|(
name|String
index|[]
name|colNamesForFields
parameter_list|,
name|String
name|delimiter
parameter_list|,
name|HiveEndPoint
name|endPoint
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|char
name|serdeSeparator
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|StreamingException
block|{
name|this
argument_list|(
name|colNamesForFields
argument_list|,
name|delimiter
argument_list|,
name|endPoint
argument_list|,
name|conf
argument_list|,
name|serdeSeparator
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isReorderingNeeded
parameter_list|(
name|String
name|delimiter
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|tableColumns
parameter_list|)
block|{
return|return
operator|!
operator|(
name|delimiter
operator|.
name|equals
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|getSerdeSeparator
argument_list|()
argument_list|)
argument_list|)
operator|&&
name|areFieldsInColOrder
argument_list|(
name|fieldToColMapping
argument_list|)
operator|&&
name|tableColumns
operator|.
name|size
argument_list|()
operator|>=
name|fieldToColMapping
operator|.
name|length
operator|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|areFieldsInColOrder
parameter_list|(
name|int
index|[]
name|fieldToColMapping
parameter_list|)
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
name|fieldToColMapping
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|fieldToColMapping
index|[
name|i
index|]
operator|!=
name|i
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|int
index|[]
name|getFieldReordering
parameter_list|(
name|String
index|[]
name|colNamesForFields
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tableColNames
parameter_list|)
throws|throws
name|InvalidColumn
block|{
name|int
index|[]
name|result
init|=
operator|new
name|int
index|[
name|colNamesForFields
operator|.
name|length
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
name|colNamesForFields
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|int
name|i
init|=
operator|-
literal|1
decl_stmt|,
name|fieldLabelCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|col
range|:
name|colNamesForFields
control|)
block|{
operator|++
name|i
expr_stmt|;
if|if
condition|(
name|col
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|col
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
operator|++
name|fieldLabelCount
expr_stmt|;
name|int
name|loc
init|=
name|tableColNames
operator|.
name|indexOf
argument_list|(
name|col
argument_list|)
decl_stmt|;
if|if
condition|(
name|loc
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|InvalidColumn
argument_list|(
literal|"Column '"
operator|+
name|col
operator|+
literal|"' not found in table for input field "
operator|+
name|i
operator|+
literal|1
argument_list|)
throw|;
block|}
name|result
index|[
name|i
index|]
operator|=
name|loc
expr_stmt|;
block|}
if|if
condition|(
name|fieldLabelCount
operator|>
name|tableColNames
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InvalidColumn
argument_list|(
literal|"Number of field names exceeds the number of columns in table"
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|// Reorder fields in record based on the order of columns in the table
specifier|protected
name|byte
index|[]
name|reorderFields
parameter_list|(
name|byte
index|[]
name|record
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
if|if
condition|(
operator|!
name|reorderingNeeded
condition|)
block|{
return|return
name|record
return|;
block|}
name|String
index|[]
name|reorderedFields
init|=
operator|new
name|String
index|[
name|getTableColumns
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|String
name|decoded
init|=
operator|new
name|String
argument_list|(
name|record
argument_list|)
decl_stmt|;
name|String
index|[]
name|fields
init|=
name|decoded
operator|.
name|split
argument_list|(
name|delimiter
argument_list|,
operator|-
literal|1
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
name|fieldToColMapping
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|int
name|newIndex
init|=
name|fieldToColMapping
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|newIndex
operator|!=
operator|-
literal|1
condition|)
block|{
name|reorderedFields
index|[
name|newIndex
index|]
operator|=
name|fields
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
return|return
name|join
argument_list|(
name|reorderedFields
argument_list|,
name|getSerdeSeparator
argument_list|()
argument_list|)
return|;
block|}
comment|// handles nulls in items[]
comment|// TODO: perhaps can be made more efficient by creating a byte[] directly
specifier|private
specifier|static
name|byte
index|[]
name|join
parameter_list|(
name|String
index|[]
name|items
parameter_list|,
name|char
name|separator
parameter_list|)
block|{
name|StringBuilder
name|buff
init|=
operator|new
name|StringBuilder
argument_list|(
literal|100
argument_list|)
decl_stmt|;
if|if
condition|(
name|items
operator|.
name|length
operator|==
literal|0
condition|)
return|return
literal|""
operator|.
name|getBytes
argument_list|()
return|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
name|i
operator|<
name|items
operator|.
name|length
operator|-
literal|1
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|items
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|buff
operator|.
name|append
argument_list|(
name|items
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|buff
operator|.
name|append
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|items
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|buff
operator|.
name|append
argument_list|(
name|items
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|buff
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
return|;
block|}
specifier|protected
name|ArrayList
argument_list|<
name|String
argument_list|>
name|getTableColumns
parameter_list|()
block|{
return|return
name|tableColumns
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|long
name|writeId
parameter_list|,
name|byte
index|[]
name|record
parameter_list|)
throws|throws
name|SerializationError
throws|,
name|StreamingIOFailure
block|{
try|try
block|{
name|byte
index|[]
name|orderedFields
init|=
name|reorderFields
argument_list|(
name|record
argument_list|)
decl_stmt|;
name|Object
name|encodedRow
init|=
name|encode
argument_list|(
name|orderedFields
argument_list|)
decl_stmt|;
name|int
name|bucket
init|=
name|getBucket
argument_list|(
name|encodedRow
argument_list|)
decl_stmt|;
name|getRecordUpdater
argument_list|(
name|bucket
argument_list|)
operator|.
name|insert
argument_list|(
name|writeId
argument_list|,
name|encodedRow
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
name|StreamingIOFailure
argument_list|(
literal|"Error writing record in transaction write id ("
operator|+
name|writeId
operator|+
literal|")"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AbstractSerDe
name|getSerde
parameter_list|()
block|{
return|return
name|serde
return|;
block|}
specifier|protected
name|LazySimpleStructObjectInspector
name|getRecordObjectInspector
parameter_list|()
block|{
return|return
name|recordObjInspector
return|;
block|}
annotation|@
name|Override
specifier|protected
name|StructField
index|[]
name|getBucketStructFields
parameter_list|()
block|{
return|return
name|bucketStructFields
return|;
block|}
specifier|protected
name|ObjectInspector
index|[]
name|getBucketObjectInspectors
parameter_list|()
block|{
return|return
name|bucketObjInspectors
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|encode
parameter_list|(
name|byte
index|[]
name|record
parameter_list|)
throws|throws
name|SerializationError
block|{
try|try
block|{
name|BytesWritable
name|blob
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|blob
operator|.
name|set
argument_list|(
name|record
argument_list|,
literal|0
argument_list|,
name|record
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|serde
operator|.
name|deserialize
argument_list|(
name|blob
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
name|SerializationError
argument_list|(
literal|"Unable to convert byte[] record into Object"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Creates LazySimpleSerde    * @return    * @throws SerializationError if serde could not be initialized    * @param tbl    */
specifier|protected
specifier|static
name|LazySimpleSerDe
name|createSerde
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|char
name|serdeSeparator
parameter_list|)
throws|throws
name|SerializationError
block|{
try|try
block|{
name|Properties
name|tableProps
init|=
name|MetaStoreUtils
operator|.
name|getTableMetadata
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
name|tableProps
operator|.
name|setProperty
argument_list|(
literal|"field.delim"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|serdeSeparator
argument_list|)
argument_list|)
expr_stmt|;
name|LazySimpleSerDe
name|serde
init|=
operator|new
name|LazySimpleSerDe
argument_list|()
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
name|conf
argument_list|,
name|tableProps
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|serde
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
name|SerializationError
argument_list|(
literal|"Error initializing serde"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|ArrayList
argument_list|<
name|String
argument_list|>
name|getCols
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|colNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|cols
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FieldSchema
name|col
range|:
name|cols
control|)
block|{
name|colNames
operator|.
name|add
argument_list|(
name|col
operator|.
name|getName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|colNames
return|;
block|}
specifier|public
name|char
name|getSerdeSeparator
parameter_list|()
block|{
return|return
name|serdeSeparator
return|;
block|}
block|}
end_class

end_unit

