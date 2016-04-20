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
package|;
end_package

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
name|nio
operator|.
name|ByteBuffer
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
name|io
operator|.
name|DiskRangeList
import|;
end_import

begin_comment
comment|/** An abstract data reader that IO formats can use to read bytes from underlying storage. */
end_comment

begin_interface
specifier|public
interface|interface
name|DataReader
extends|extends
name|Closeable
block|{
comment|/** Opens the DataReader, making it ready to use. */
name|void
name|open
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/** Reads the data.    *    * Note that for the cases such as zero-copy read, caller must release the disk ranges    * produced after being done with them. Call isTrackingDiskRanges to find out if this is needed.    * @param range List if disk ranges to read. Ranges with data will be ignored.    * @param baseOffset Base offset from the start of the file of the ranges in disk range list.    * @param doForceDirect Whether the data should be read into direct buffers.    * @return New or modified list of DiskRange-s, where all the ranges are filled with data.    */
name|DiskRangeList
name|readFileData
parameter_list|(
name|DiskRangeList
name|range
parameter_list|,
name|long
name|baseOffset
parameter_list|,
name|boolean
name|doForceDirect
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Whether the user should release buffers created by readFileData. See readFileData javadoc.    */
name|boolean
name|isTrackingDiskRanges
parameter_list|()
function_decl|;
comment|/**    * Releases buffers created by readFileData. See readFileData javadoc.    * @param toRelease The buffer to release.    */
name|void
name|releaseBuffer
parameter_list|(
name|ByteBuffer
name|toRelease
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

