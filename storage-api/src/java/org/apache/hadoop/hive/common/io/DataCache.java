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
name|common
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
name|hive
operator|.
name|common
operator|.
name|io
operator|.
name|encoded
operator|.
name|MemoryBuffer
import|;
end_import

begin_comment
comment|/** An abstract data cache that IO formats can use to retrieve and cache data. */
end_comment

begin_interface
specifier|public
interface|interface
name|DataCache
block|{
specifier|public
specifier|static
specifier|final
class|class
name|BooleanRef
block|{
specifier|public
name|boolean
name|value
decl_stmt|;
block|}
comment|/** Disk range factory used during cache retrieval. */
specifier|public
interface|interface
name|DiskRangeListFactory
block|{
name|DiskRangeList
name|createCacheChunk
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|,
name|long
name|startOffset
parameter_list|,
name|long
name|endOffset
parameter_list|)
function_decl|;
block|}
comment|/**    * Gets file data for particular offsets. The range list is modified in place; it is then    * returned (since the list head could have changed). Ranges are replaced with cached ranges.    *    * Any such buffer is locked in cache to prevent eviction, and must therefore be released    * back to cache via a corresponding call (releaseBuffer) when the caller is done with it.    *    * In case of partial overlap with cached data, full cache blocks are always returned;    * there's no capacity for partial matches in return type. The rules are as follows:    * 1) If the requested range starts in the middle of a cached range, that cached range will not    *    be returned by default (e.g. if [100,200) and [200,300) are cached, the request for    *    [150,300) will only return [200,300) from cache). This may be configurable in impls.    *    This is because we assume well-known range start offsets are used (rg/stripe offsets), so    *    a request from the middle of the start doesn't make sense.    * 2) If the requested range ends in the middle of a cached range, that entire cached range will    *    be returned (e.g. if [100,200) and [200,300) are cached, the request for [100,250) will    *    return both ranges). It should really be same as #1, however currently ORC uses estimated    *    end offsets; if we don't return the end block, the caller may read it from disk needlessly.    *    * @param fileKey Unique ID of the target file on the file system.    * @param range A set of DiskRange-s (linked list) that is to be retrieved. May be modified.    * @param baseOffset base offset for the ranges (stripe/stream offset in case of ORC).    * @param factory A factory to produce DiskRangeList-s out of cached MemoryBuffer-s.    * @param gotAllData An out param - whether all the requested data was found in cache.    * @return The new or modified list of DiskRange-s, where some ranges may contain cached data.    */
name|DiskRangeList
name|getFileData
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|DiskRangeList
name|range
parameter_list|,
name|long
name|baseOffset
parameter_list|,
name|DiskRangeListFactory
name|factory
parameter_list|,
name|BooleanRef
name|gotAllData
parameter_list|)
function_decl|;
comment|/**    * Puts file data into cache, or gets older data in case of collisions.    *    * The memory buffers provided MUST be allocated via an allocator returned by getAllocator    * method, to allow cache implementations that evict and then de-allocate the buffer.    *    * It is assumed that the caller will use the data immediately, therefore any buffers provided    * to putFileData (or returned due to cache collision) are locked in cache to prevent eviction,    * and must therefore be released back to cache via a corresponding call (releaseBuffer) when the    * caller is done with it. Buffers rejected due to conflict will neither be locked, nor    * automatically deallocated. The caller must take care to discard these buffers.    *    * @param fileKey Unique ID of the target file on the file system.    * @param ranges The ranges for which the data is being cached. These objects will not be stored.    * @param data The data for the corresponding ranges.    * @param baseOffset base offset for the ranges (stripe/stream offset in case of ORC).    * @return null if all data was put; bitmask indicating which chunks were not put otherwise;    *         the replacement chunks from cache are updated directly in the array.    */
name|long
index|[]
name|putFileData
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|DiskRange
index|[]
name|ranges
parameter_list|,
name|MemoryBuffer
index|[]
name|data
parameter_list|,
name|long
name|baseOffset
parameter_list|)
function_decl|;
comment|/**    * Releases the buffer returned by getFileData/provided to putFileData back to cache.    * See respective javadocs for details.    * @param buffer the buffer to release    */
name|void
name|releaseBuffer
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
function_decl|;
comment|/**    * Notifies the cache that the buffer returned from getFileData/provided to putFileData will    * be used by another consumer and therefore released multiple times (one more time per call).    * @param buffer the buffer to reuse    */
name|void
name|reuseBuffer
parameter_list|(
name|MemoryBuffer
name|buffer
parameter_list|)
function_decl|;
comment|/**    * Gets the allocator associated with this DataCache.    * @return the allocator    */
name|Allocator
name|getAllocator
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

