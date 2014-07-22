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
name|fs
operator|.
name|FSDataInputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|mapred
operator|.
name|JobConf
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
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|Buffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
comment|/**  * Utilities useful only to the AvroSerde itself.  Not mean to be used by  * end-users but public for interop to the ql package.  */
end_comment

begin_class
specifier|public
class|class
name|AvroSerdeUtils
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AvroSerdeUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SCHEMA_LITERAL
init|=
literal|"avro.schema.literal"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SCHEMA_URL
init|=
literal|"avro.schema.url"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SCHEMA_NONE
init|=
literal|"none"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXCEPTION_MESSAGE
init|=
literal|"Neither "
operator|+
name|SCHEMA_LITERAL
operator|+
literal|" nor "
operator|+
name|SCHEMA_URL
operator|+
literal|" specified, can't determine table schema"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_SERDE_SCHEMA
init|=
literal|"avro.serde.schema"
decl_stmt|;
comment|/**    * Determine the schema to that's been provided for Avro serde work.    * @param properties containing a key pointing to the schema, one way or another    * @return schema to use while serdeing the avro file    * @throws IOException if error while trying to read the schema from another location    * @throws AvroSerdeException if unable to find a schema or pointer to it in the properties    */
specifier|public
specifier|static
name|Schema
name|determineSchemaOrThrowException
parameter_list|(
name|Properties
name|properties
parameter_list|)
throws|throws
name|IOException
throws|,
name|AvroSerdeException
block|{
name|String
name|schemaString
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|SCHEMA_LITERAL
argument_list|)
decl_stmt|;
if|if
condition|(
name|schemaString
operator|!=
literal|null
operator|&&
operator|!
name|schemaString
operator|.
name|equals
argument_list|(
name|SCHEMA_NONE
argument_list|)
condition|)
return|return
name|Schema
operator|.
name|parse
argument_list|(
name|schemaString
argument_list|)
return|;
comment|// Try pulling directly from URL
name|schemaString
operator|=
name|properties
operator|.
name|getProperty
argument_list|(
name|SCHEMA_URL
argument_list|)
expr_stmt|;
if|if
condition|(
name|schemaString
operator|==
literal|null
operator|||
name|schemaString
operator|.
name|equals
argument_list|(
name|SCHEMA_NONE
argument_list|)
condition|)
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
name|EXCEPTION_MESSAGE
argument_list|)
throw|;
try|try
block|{
name|Schema
name|s
init|=
name|getSchemaFromFS
argument_list|(
name|schemaString
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
comment|//in case schema is not a file system
return|return
name|Schema
operator|.
name|parse
argument_list|(
operator|new
name|URL
argument_list|(
name|schemaString
argument_list|)
operator|.
name|openStream
argument_list|()
argument_list|)
return|;
block|}
return|return
name|s
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Unable to read schema from given path: "
operator|+
name|schemaString
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|urie
parameter_list|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Unable to read schema from given path: "
operator|+
name|schemaString
argument_list|,
name|urie
argument_list|)
throw|;
block|}
block|}
comment|/**    * Attempt to determine the schema via the usual means, but do not throw    * an exception if we fail.  Instead, signal failure via a special    * schema.  This is used because Hive calls init on the serde during    * any call, including calls to update the serde properties, meaning    * if the serde is in a bad state, there is no way to update that state.    */
specifier|public
specifier|static
name|Schema
name|determineSchemaOrReturnErrorSchema
parameter_list|(
name|Properties
name|props
parameter_list|)
block|{
try|try
block|{
return|return
name|determineSchemaOrThrowException
argument_list|(
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
return|return
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
return|return
name|SchemaResolutionProblem
operator|.
name|SIGNAL_BAD_SCHEMA
return|;
block|}
block|}
comment|// Protected for testing and so we can pass in a conf for testing.
specifier|protected
specifier|static
name|Schema
name|getSchemaFromFS
parameter_list|(
name|String
name|schemaFSUrl
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|URISyntaxException
block|{
name|FSDataInputStream
name|in
init|=
literal|null
decl_stmt|;
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
operator|new
name|URI
argument_list|(
name|schemaFSUrl
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|//return null only if the file system in schema is not recognized
name|String
name|msg
init|=
literal|"Failed to open file system for uri "
operator|+
name|schemaFSUrl
operator|+
literal|" assuming it is not a FileSystem url"
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
try|try
block|{
name|in
operator|=
name|fs
operator|.
name|open
argument_list|(
operator|new
name|Path
argument_list|(
name|schemaFSUrl
argument_list|)
argument_list|)
expr_stmt|;
name|Schema
name|s
init|=
name|Schema
operator|.
name|parse
argument_list|(
name|in
argument_list|)
decl_stmt|;
return|return
name|s
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Determine if an Avro schema is of type Union[T, NULL].  Avro supports nullable    * types via a union of type T and null.  This is a very common use case.    * As such, we want to silently convert it to just T and allow the value to be null.    *    * @return true if type represents Union[T, Null], false otherwise    */
specifier|public
specifier|static
name|boolean
name|isNullableType
parameter_list|(
name|Schema
name|schema
parameter_list|)
block|{
return|return
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
name|UNION
argument_list|)
operator|&&
name|schema
operator|.
name|getTypes
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|2
operator|&&
operator|(
name|schema
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
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
name|NULL
argument_list|)
operator|||
name|schema
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
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
name|NULL
argument_list|)
operator|)
return|;
comment|// [null, null] not allowed, so this check is ok.
block|}
comment|/**    * In a nullable type, get the schema for the non-nullable type.  This method    * does no checking that the provides Schema is nullable.    */
specifier|public
specifier|static
name|Schema
name|getOtherTypeFromNullableType
parameter_list|(
name|Schema
name|schema
parameter_list|)
block|{
name|List
argument_list|<
name|Schema
argument_list|>
name|types
init|=
name|schema
operator|.
name|getTypes
argument_list|()
decl_stmt|;
return|return
name|types
operator|.
name|get
argument_list|(
literal|0
argument_list|)
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
name|NULL
argument_list|)
condition|?
name|types
operator|.
name|get
argument_list|(
literal|1
argument_list|)
else|:
name|types
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
comment|/**    * Determine if we're being executed from within an MR job or as part    * of a select * statement.  The signals for this varies between Hive versions.    * @param job that contains things that are or are not set in a job    * @return Are we in a job or not?    */
specifier|public
specifier|static
name|boolean
name|insideMRJob
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
return|return
name|job
operator|!=
literal|null
operator|&&
operator|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PLAN
argument_list|)
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|HiveConf
operator|.
name|getVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PLAN
argument_list|)
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
specifier|public
specifier|static
name|Buffer
name|getBufferFromBytes
parameter_list|(
name|byte
index|[]
name|input
parameter_list|)
block|{
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|input
argument_list|)
decl_stmt|;
return|return
name|bb
operator|.
name|rewind
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Buffer
name|getBufferFromDecimal
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
if|if
condition|(
name|dec
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|dec
operator|=
name|dec
operator|.
name|setScale
argument_list|(
name|scale
argument_list|)
expr_stmt|;
return|return
name|AvroSerdeUtils
operator|.
name|getBufferFromBytes
argument_list|(
name|dec
operator|.
name|unscaledValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|byte
index|[]
name|getBytesFromByteBuffer
parameter_list|(
name|ByteBuffer
name|byteBuffer
parameter_list|)
block|{
name|byteBuffer
operator|.
name|rewind
argument_list|()
expr_stmt|;
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|byteBuffer
operator|.
name|limit
argument_list|()
index|]
decl_stmt|;
name|byteBuffer
operator|.
name|get
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|getHiveDecimalFromByteBuffer
parameter_list|(
name|ByteBuffer
name|byteBuffer
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|byte
index|[]
name|result
init|=
name|getBytesFromByteBuffer
argument_list|(
name|byteBuffer
argument_list|)
decl_stmt|;
name|HiveDecimal
name|dec
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigInteger
argument_list|(
name|result
argument_list|)
argument_list|,
name|scale
argument_list|)
decl_stmt|;
return|return
name|dec
return|;
block|}
block|}
end_class

end_unit

