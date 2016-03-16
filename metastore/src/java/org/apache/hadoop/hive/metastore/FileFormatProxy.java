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
name|Metastore
operator|.
name|SplitInfos
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
name|sarg
operator|.
name|SearchArgument
import|;
end_import

begin_comment
comment|/**  * Same as PartitionExpressionProxy, but for file format specific methods for metadata cache.  */
end_comment

begin_interface
specifier|public
interface|interface
name|FileFormatProxy
block|{
comment|/**    * Applies SARG to file metadata, and produces some result for this file.    * @param sarg SARG    * @param fileMetadata File metadata from metastore cache.    * @return The result to return to client for this file, or null if file is eliminated.    */
name|SplitInfos
name|applySargToMetadata
parameter_list|(
name|SearchArgument
name|sarg
parameter_list|,
name|ByteBuffer
name|fileMetadata
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param fs The filesystem of the file.    * @param path The file path.    * @param addedVals Output parameter; additional column values for columns returned by    *                  getAddedColumnsToCache to cache in MS.    * @return The ORC file metadata for a given file.    */
name|ByteBuffer
name|getMetadataToCache
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|ByteBuffer
index|[]
name|addedVals
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return Additional column names to cache in MS for this format.    */
name|ByteBuffer
index|[]
name|getAddedColumnsToCache
parameter_list|()
function_decl|;
comment|/**    * @param metadata File metadatas.    * @return Additional values for columns returned by getAddedColumnsToCache to cache in MS    *         for respective metadatas.    */
name|ByteBuffer
index|[]
index|[]
name|getAddedValuesToCache
parameter_list|(
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|metadata
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

