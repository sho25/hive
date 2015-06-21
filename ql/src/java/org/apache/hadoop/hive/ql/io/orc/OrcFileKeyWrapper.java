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
name|io
operator|.
name|WritableComparable
import|;
end_import

begin_comment
comment|/**  * Key for OrcFileMergeMapper task. Contains orc file related information that  * should match before merging two orc files.  */
end_comment

begin_class
specifier|public
class|class
name|OrcFileKeyWrapper
implements|implements
name|WritableComparable
argument_list|<
name|OrcFileKeyWrapper
argument_list|>
block|{
specifier|private
name|Path
name|inputPath
decl_stmt|;
specifier|private
name|CompressionKind
name|compression
decl_stmt|;
specifier|private
name|long
name|compressBufferSize
decl_stmt|;
specifier|private
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
decl_stmt|;
specifier|private
name|int
name|rowIndexStride
decl_stmt|;
specifier|private
name|OrcFile
operator|.
name|Version
name|version
decl_stmt|;
specifier|private
name|boolean
name|isIncompatFile
decl_stmt|;
specifier|public
name|boolean
name|isIncompatFile
parameter_list|()
block|{
return|return
name|isIncompatFile
return|;
block|}
specifier|public
name|void
name|setIsIncompatFile
parameter_list|(
name|boolean
name|isIncompatFile
parameter_list|)
block|{
name|this
operator|.
name|isIncompatFile
operator|=
name|isIncompatFile
expr_stmt|;
block|}
specifier|public
name|OrcFile
operator|.
name|Version
name|getVersion
parameter_list|()
block|{
return|return
name|version
return|;
block|}
specifier|public
name|void
name|setVersion
parameter_list|(
name|OrcFile
operator|.
name|Version
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
specifier|public
name|int
name|getRowIndexStride
parameter_list|()
block|{
return|return
name|rowIndexStride
return|;
block|}
specifier|public
name|void
name|setRowIndexStride
parameter_list|(
name|int
name|rowIndexStride
parameter_list|)
block|{
name|this
operator|.
name|rowIndexStride
operator|=
name|rowIndexStride
expr_stmt|;
block|}
specifier|public
name|long
name|getCompressBufferSize
parameter_list|()
block|{
return|return
name|compressBufferSize
return|;
block|}
specifier|public
name|void
name|setCompressBufferSize
parameter_list|(
name|long
name|compressBufferSize
parameter_list|)
block|{
name|this
operator|.
name|compressBufferSize
operator|=
name|compressBufferSize
expr_stmt|;
block|}
specifier|public
name|CompressionKind
name|getCompression
parameter_list|()
block|{
return|return
name|compression
return|;
block|}
specifier|public
name|void
name|setCompression
parameter_list|(
name|CompressionKind
name|compression
parameter_list|)
block|{
name|this
operator|.
name|compression
operator|=
name|compression
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|getTypes
parameter_list|()
block|{
return|return
name|types
return|;
block|}
specifier|public
name|void
name|setTypes
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
parameter_list|)
block|{
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
block|}
specifier|public
name|Path
name|getInputPath
parameter_list|()
block|{
return|return
name|inputPath
return|;
block|}
specifier|public
name|void
name|setInputPath
parameter_list|(
name|Path
name|inputPath
parameter_list|)
block|{
name|this
operator|.
name|inputPath
operator|=
name|inputPath
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not supported."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not supported."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|OrcFileKeyWrapper
name|o
parameter_list|)
block|{
return|return
name|inputPath
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|inputPath
argument_list|)
return|;
block|}
block|}
end_class

end_unit

