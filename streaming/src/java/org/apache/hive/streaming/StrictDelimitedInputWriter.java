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
name|streaming
package|;
end_package

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
name|io
operator|.
name|BytesWritable
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
name|Joiner
import|;
end_import

begin_comment
comment|/**  * Streaming Writer handles delimited input (eg. CSV).  * Delimited input is parsed to extract partition values, bucketing info and is forwarded to record updater.  * Uses Lazy Simple SerDe to process delimited input.  *  * NOTE: This record writer is NOT thread-safe. Use one record writer per streaming connection.  */
end_comment

begin_class
specifier|public
class|class
name|StrictDelimitedInputWriter
extends|extends
name|AbstractRecordWriter
block|{
specifier|private
name|char
name|fieldDelimiter
decl_stmt|;
specifier|private
name|char
name|collectionDelimiter
decl_stmt|;
specifier|private
name|char
name|mapKeyDelimiter
decl_stmt|;
specifier|private
name|LazySimpleSerDe
name|serde
decl_stmt|;
specifier|private
name|StrictDelimitedInputWriter
parameter_list|(
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|fieldDelimiter
operator|=
name|builder
operator|.
name|fieldDelimiter
expr_stmt|;
name|this
operator|.
name|collectionDelimiter
operator|=
name|builder
operator|.
name|collectionDelimiter
expr_stmt|;
name|this
operator|.
name|mapKeyDelimiter
operator|=
name|builder
operator|.
name|mapKeyDelimiter
expr_stmt|;
block|}
specifier|public
specifier|static
name|Builder
name|newBuilder
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
name|char
name|fieldDelimiter
init|=
operator|(
name|char
operator|)
name|LazySerDeParameters
operator|.
name|DefaultSeparators
index|[
literal|0
index|]
decl_stmt|;
specifier|private
name|char
name|collectionDelimiter
init|=
operator|(
name|char
operator|)
name|LazySerDeParameters
operator|.
name|DefaultSeparators
index|[
literal|1
index|]
decl_stmt|;
specifier|private
name|char
name|mapKeyDelimiter
init|=
operator|(
name|char
operator|)
name|LazySerDeParameters
operator|.
name|DefaultSeparators
index|[
literal|2
index|]
decl_stmt|;
specifier|public
name|Builder
name|withFieldDelimiter
parameter_list|(
specifier|final
name|char
name|fieldDelimiter
parameter_list|)
block|{
name|this
operator|.
name|fieldDelimiter
operator|=
name|fieldDelimiter
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withCollectionDelimiter
parameter_list|(
specifier|final
name|char
name|collectionDelimiter
parameter_list|)
block|{
name|this
operator|.
name|collectionDelimiter
operator|=
name|collectionDelimiter
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|withMapKeyDelimiter
parameter_list|(
specifier|final
name|char
name|mapKeyDelimiter
parameter_list|)
block|{
name|this
operator|.
name|mapKeyDelimiter
operator|=
name|mapKeyDelimiter
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StrictDelimitedInputWriter
name|build
parameter_list|()
block|{
return|return
operator|new
name|StrictDelimitedInputWriter
argument_list|(
name|this
argument_list|)
return|;
block|}
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
annotation|@
name|Override
specifier|public
name|LazySimpleSerDe
name|createSerde
parameter_list|()
throws|throws
name|SerializationError
block|{
try|try
block|{
name|Properties
name|tableProps
init|=
name|table
operator|.
name|getMetadata
argument_list|()
decl_stmt|;
name|tableProps
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|join
argument_list|(
name|inputColumns
argument_list|)
argument_list|)
expr_stmt|;
name|tableProps
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|Joiner
operator|.
name|on
argument_list|(
literal|":"
argument_list|)
operator|.
name|join
argument_list|(
name|inputTypes
argument_list|)
argument_list|)
expr_stmt|;
name|tableProps
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|FIELD_DELIM
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|fieldDelimiter
argument_list|)
argument_list|)
expr_stmt|;
name|tableProps
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|COLLECTION_DELIM
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|collectionDelimiter
argument_list|)
argument_list|)
expr_stmt|;
name|tableProps
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|MAPKEY_DELIM
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|mapKeyDelimiter
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
name|this
operator|.
name|serde
operator|=
name|serde
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
block|}
end_class

end_unit

