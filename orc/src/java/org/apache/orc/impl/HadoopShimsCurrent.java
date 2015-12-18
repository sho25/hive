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
name|orc
operator|.
name|impl
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
name|io
operator|.
name|compress
operator|.
name|snappy
operator|.
name|SnappyDecompressor
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
name|compress
operator|.
name|zlib
operator|.
name|ZlibDecompressor
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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_comment
comment|/**  * Shims for recent versions of Hadoop  */
end_comment

begin_class
specifier|public
class|class
name|HadoopShimsCurrent
implements|implements
name|HadoopShims
block|{
specifier|private
specifier|static
class|class
name|DirectDecompressWrapper
implements|implements
name|DirectDecompressor
block|{
specifier|private
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|compress
operator|.
name|DirectDecompressor
name|root
decl_stmt|;
name|DirectDecompressWrapper
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|compress
operator|.
name|DirectDecompressor
name|root
parameter_list|)
block|{
name|this
operator|.
name|root
operator|=
name|root
expr_stmt|;
block|}
specifier|public
name|void
name|decompress
parameter_list|(
name|ByteBuffer
name|input
parameter_list|,
name|ByteBuffer
name|output
parameter_list|)
throws|throws
name|IOException
block|{
name|root
operator|.
name|decompress
argument_list|(
name|input
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|DirectDecompressor
name|getDirectDecompressor
parameter_list|(
name|DirectCompressionType
name|codec
parameter_list|)
block|{
switch|switch
condition|(
name|codec
condition|)
block|{
case|case
name|ZLIB
case|:
return|return
operator|new
name|DirectDecompressWrapper
argument_list|(
operator|new
name|ZlibDecompressor
operator|.
name|ZlibDirectDecompressor
argument_list|()
argument_list|)
return|;
case|case
name|ZLIB_NOHEADER
case|:
return|return
operator|new
name|DirectDecompressWrapper
argument_list|(
operator|new
name|ZlibDecompressor
operator|.
name|ZlibDirectDecompressor
argument_list|(
name|ZlibDecompressor
operator|.
name|CompressionHeader
operator|.
name|NO_HEADER
argument_list|,
literal|0
argument_list|)
argument_list|)
return|;
case|case
name|SNAPPY
case|:
return|return
operator|new
name|DirectDecompressWrapper
argument_list|(
operator|new
name|SnappyDecompressor
operator|.
name|SnappyDirectDecompressor
argument_list|()
argument_list|)
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

