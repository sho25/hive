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
name|io
operator|.
name|orc
operator|.
name|encoded
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
name|function
operator|.
name|Supplier
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
name|io
operator|.
name|orc
operator|.
name|OrcFile
operator|.
name|ReaderOptions
import|;
end_import

begin_comment
comment|/**  * Factory for encoded ORC readers and options.  */
end_comment

begin_class
specifier|public
class|class
name|EncodedOrcFile
block|{
comment|/**    * Extends ReaderOptions to accept a file system supplier    * instead of a fully initialized fs object.    */
specifier|public
specifier|static
class|class
name|EncodedReaderOptions
extends|extends
name|ReaderOptions
block|{
specifier|private
name|Supplier
argument_list|<
name|FileSystem
argument_list|>
name|fileSystemSupplier
decl_stmt|;
specifier|public
name|EncodedReaderOptions
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|super
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
specifier|public
name|EncodedReaderOptions
name|filesystem
parameter_list|(
name|Supplier
argument_list|<
name|FileSystem
argument_list|>
name|fsSupplier
parameter_list|)
block|{
name|this
operator|.
name|fileSystemSupplier
operator|=
name|fsSupplier
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|EncodedReaderOptions
name|filesystem
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
block|{
name|this
operator|.
name|fileSystemSupplier
operator|=
parameter_list|()
lambda|->
name|fs
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileSystem
name|getFilesystem
parameter_list|()
block|{
return|return
name|fileSystemSupplier
operator|!=
literal|null
condition|?
name|fileSystemSupplier
operator|.
name|get
argument_list|()
else|:
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|Reader
name|createReader
parameter_list|(
name|Path
name|path
parameter_list|,
name|ReaderOptions
name|options
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ReaderImpl
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|EncodedReaderOptions
name|readerOptions
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|EncodedReaderOptions
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

