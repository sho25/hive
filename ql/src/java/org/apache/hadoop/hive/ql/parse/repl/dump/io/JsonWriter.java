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
name|ql
operator|.
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|io
package|;
end_package

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
name|ql
operator|.
name|parse
operator|.
name|ReplicationSpec
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
name|parse
operator|.
name|SemanticException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonGenerator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|OutputStream
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
name|ql
operator|.
name|parse
operator|.
name|EximUtil
operator|.
name|METADATA_FORMAT_VERSION
import|;
end_import

begin_class
specifier|public
class|class
name|JsonWriter
implements|implements
name|Closeable
block|{
specifier|final
name|JsonGenerator
name|jsonGenerator
decl_stmt|;
specifier|public
name|JsonWriter
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|writePath
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|writePath
argument_list|)
decl_stmt|;
name|jsonGenerator
operator|=
operator|new
name|JsonFactory
argument_list|()
operator|.
name|createJsonGenerator
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|jsonGenerator
operator|.
name|writeStartObject
argument_list|()
expr_stmt|;
name|jsonGenerator
operator|.
name|writeStringField
argument_list|(
literal|"version"
argument_list|,
name|METADATA_FORMAT_VERSION
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|jsonGenerator
operator|.
name|writeEndObject
argument_list|()
expr_stmt|;
name|jsonGenerator
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
interface|interface
name|Serializer
block|{
name|String
name|UTF_8
init|=
literal|"UTF-8"
decl_stmt|;
name|void
name|writeTo
parameter_list|(
name|JsonWriter
name|writer
parameter_list|,
name|ReplicationSpec
name|additionalPropertiesProvider
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|IOException
function_decl|;
block|}
block|}
end_class

end_unit

