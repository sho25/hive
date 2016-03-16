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
name|metastore
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
name|metastore
operator|.
name|api
operator|.
name|FileMetadataExprType
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
name|hbase
operator|.
name|MetadataStore
import|;
end_import

begin_comment
comment|/**  * The base implementation of a file metadata handler for a specific file type.  * There are currently two classes for each file type (of 1), this one, which is very simple due  * to the fact that it just calls the proxy class for most calls; and the proxy class, that  * contains the actual implementation that depends on some stuff in QL (for ORC).  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|FileMetadataHandler
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FileMetadataHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|PartitionExpressionProxy
name|expressionProxy
decl_stmt|;
specifier|private
name|FileFormatProxy
name|fileFormatProxy
decl_stmt|;
specifier|private
name|MetadataStore
name|store
decl_stmt|;
comment|/**    * Same as RawStore.getFileMetadataByExpr.    */
specifier|public
specifier|abstract
name|void
name|getFileMetadataByExpr
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|fileIds
parameter_list|,
name|byte
index|[]
name|expr
parameter_list|,
name|ByteBuffer
index|[]
name|metadatas
parameter_list|,
name|ByteBuffer
index|[]
name|results
parameter_list|,
name|boolean
index|[]
name|eliminated
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|FileMetadataExprType
name|getType
parameter_list|()
function_decl|;
specifier|protected
name|PartitionExpressionProxy
name|getExpressionProxy
parameter_list|()
block|{
return|return
name|expressionProxy
return|;
block|}
specifier|protected
name|FileFormatProxy
name|getFileFormatProxy
parameter_list|()
block|{
return|return
name|fileFormatProxy
return|;
block|}
specifier|protected
name|MetadataStore
name|getStore
parameter_list|()
block|{
return|return
name|store
return|;
block|}
comment|/**    * Configures the handler. Called once before use.    * @param conf Config.    * @param expressionProxy Expression proxy to access ql stuff.    * @param store Storage interface to manipulate the metadata.    */
specifier|public
name|void
name|configure
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|PartitionExpressionProxy
name|expressionProxy
parameter_list|,
name|MetadataStore
name|store
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|expressionProxy
operator|=
name|expressionProxy
expr_stmt|;
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|fileFormatProxy
operator|=
name|expressionProxy
operator|.
name|getFileFormatProxy
argument_list|(
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Caches the file metadata for a particular file.    * @param fileId File id.    * @param fs The filesystem of the file.    * @param path Path to the file.    */
specifier|public
name|void
name|cacheFileMetadata
parameter_list|(
name|long
name|fileId
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// ORC is in ql, so we cannot do anything here. For now, all the logic is in the proxy.
name|ByteBuffer
index|[]
name|cols
init|=
name|fileFormatProxy
operator|.
name|getAddedColumnsToCache
argument_list|()
decl_stmt|;
name|ByteBuffer
index|[]
name|vals
init|=
operator|(
name|cols
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
operator|new
name|ByteBuffer
index|[
name|cols
operator|.
name|length
index|]
decl_stmt|;
name|ByteBuffer
name|metadata
init|=
name|fileFormatProxy
operator|.
name|getMetadataToCache
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|vals
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Caching file metadata for "
operator|+
name|path
operator|+
literal|", size "
operator|+
name|metadata
operator|.
name|remaining
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|storeFileMetadata
argument_list|(
name|fileId
argument_list|,
name|metadata
argument_list|,
name|cols
argument_list|,
name|vals
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the added column names to be cached in metastore with the metadata for this type.    */
specifier|public
name|ByteBuffer
index|[]
name|createAddedCols
parameter_list|()
block|{
return|return
name|fileFormatProxy
operator|.
name|getAddedColumnsToCache
argument_list|()
return|;
block|}
comment|/**    * @return the values for the added columns returned by createAddedCols for respective metadatas.    */
specifier|public
name|ByteBuffer
index|[]
index|[]
name|createAddedColVals
parameter_list|(
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|metadata
parameter_list|)
block|{
return|return
name|fileFormatProxy
operator|.
name|getAddedValuesToCache
argument_list|(
name|metadata
argument_list|)
return|;
block|}
block|}
end_class

end_unit

