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
name|ql
operator|.
name|io
operator|.
name|orc
package|;
end_package

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
name|orc
operator|.
name|OrcFile
operator|.
name|WriterVersion
import|;
end_import

begin_comment
comment|/**  * FileMetaInfo - represents file metadata stored in footer and postscript sections of the file  * that is useful for Reader implementation  *  */
end_comment

begin_class
specifier|public
class|class
name|FileMetaInfo
block|{
name|ByteBuffer
name|footerMetaAndPsBuffer
decl_stmt|;
specifier|final
name|String
name|compressionType
decl_stmt|;
specifier|final
name|int
name|bufferSize
decl_stmt|;
specifier|final
name|int
name|metadataSize
decl_stmt|;
specifier|final
name|ByteBuffer
name|footerBuffer
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|versionList
decl_stmt|;
specifier|final
name|OrcFile
operator|.
name|WriterVersion
name|writerVersion
decl_stmt|;
comment|/** Ctor used when reading splits - no version list or full footer buffer. */
name|FileMetaInfo
parameter_list|(
name|String
name|compressionType
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|int
name|metadataSize
parameter_list|,
name|ByteBuffer
name|footerBuffer
parameter_list|,
name|OrcFile
operator|.
name|WriterVersion
name|writerVersion
parameter_list|)
block|{
name|this
argument_list|(
name|compressionType
argument_list|,
name|bufferSize
argument_list|,
name|metadataSize
argument_list|,
name|footerBuffer
argument_list|,
literal|null
argument_list|,
name|writerVersion
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/** Ctor used when creating file info during init and when getting a new one. */
specifier|public
name|FileMetaInfo
parameter_list|(
name|String
name|compressionType
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|int
name|metadataSize
parameter_list|,
name|ByteBuffer
name|footerBuffer
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|versionList
parameter_list|,
name|WriterVersion
name|writerVersion
parameter_list|,
name|ByteBuffer
name|fullFooterBuffer
parameter_list|)
block|{
name|this
operator|.
name|compressionType
operator|=
name|compressionType
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
expr_stmt|;
name|this
operator|.
name|metadataSize
operator|=
name|metadataSize
expr_stmt|;
name|this
operator|.
name|footerBuffer
operator|=
name|footerBuffer
expr_stmt|;
name|this
operator|.
name|versionList
operator|=
name|versionList
expr_stmt|;
name|this
operator|.
name|writerVersion
operator|=
name|writerVersion
expr_stmt|;
name|this
operator|.
name|footerMetaAndPsBuffer
operator|=
name|fullFooterBuffer
expr_stmt|;
block|}
specifier|public
name|OrcFile
operator|.
name|WriterVersion
name|getWriterVersion
parameter_list|()
block|{
return|return
name|writerVersion
return|;
block|}
block|}
end_class

end_unit

