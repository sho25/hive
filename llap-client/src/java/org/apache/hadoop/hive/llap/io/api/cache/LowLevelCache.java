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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|cache
package|;
end_package

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
name|common
operator|.
name|DiskRange
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
name|DiskRangeList
import|;
end_import

begin_interface
specifier|public
interface|interface
name|LowLevelCache
block|{
specifier|public
enum|enum
name|Priority
block|{
name|NORMAL
block|,
name|HIGH
comment|// TODO: we could add more priorities, e.g. tiered-high, where we always evict it last.
block|}
comment|/**    * Gets file data for particular offsets. The range list is modified in place; it is then    * returned (since the list head could have changed). Ranges are replaced with cached ranges.    * In case of partial overlap with cached data, full cache blocks are always returned;    * there's no capacity for partial matches in return type. The rules are as follows:    * 1) If the requested range starts in the middle of a cached range, that cached range will not    *    be returned by default (e.g. if [100,200) and [200,300) are cached, the request for    *    [150,300) will only return [200,300) from cache). This may be configurable in impls.    *    This is because we assume well-known range start offsets are used (rg/stripe offsets), so    *    a request from the middle of the start doesn't make sense.    * 2) If the requested range ends in the middle of a cached range, that entire cached range will    *    be returned (e.g. if [100,200) and [200,300) are cached, the request for [100,250) will    *    return both ranges). It should really be same as #1, however currently ORC uses estimated    *    end offsets; we do in fact know in such cases that partially-matched cached block (rg)    *    can be thrown away, the reader will never touch it; but we need code in the reader to    *    handle such cases to avoid disk reads for these "tails" vs real unmatched ranges.    *    Some sort of InvalidCacheChunk could be placed to avoid them. TODO    * @param base base offset for the ranges (stripe/stream offset in case of ORC).    */
name|DiskRangeList
name|getFileData
parameter_list|(
name|long
name|fileId
parameter_list|,
name|DiskRangeList
name|range
parameter_list|,
name|long
name|baseOffset
parameter_list|)
function_decl|;
comment|/**    * Puts file data into cache.    * @return null if all data was put; bitmask indicating which chunks were not put otherwise;    *         the replacement chunks from cache are updated directly in the array.    */
name|long
index|[]
name|putFileData
parameter_list|(
name|long
name|fileId
parameter_list|,
name|DiskRange
index|[]
name|ranges
parameter_list|,
name|LlapMemoryBuffer
index|[]
name|chunks
parameter_list|,
name|long
name|base
parameter_list|,
name|Priority
name|priority
parameter_list|)
function_decl|;
comment|/**    * Releases the buffer returned by getFileData or allocateMultiple.    */
name|void
name|releaseBuffer
parameter_list|(
name|LlapMemoryBuffer
name|buffer
parameter_list|)
function_decl|;
comment|/**    * Allocate dest.length new blocks of size into dest.    */
name|void
name|allocateMultiple
parameter_list|(
name|LlapMemoryBuffer
index|[]
name|dest
parameter_list|,
name|int
name|size
parameter_list|)
function_decl|;
name|void
name|releaseBuffers
parameter_list|(
name|List
argument_list|<
name|LlapMemoryBuffer
argument_list|>
name|cacheBuffers
parameter_list|)
function_decl|;
name|LlapMemoryBuffer
name|createUnallocated
parameter_list|()
function_decl|;
name|void
name|notifyReused
parameter_list|(
name|LlapMemoryBuffer
name|buffer
parameter_list|)
function_decl|;
name|boolean
name|isDirectAlloc
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

