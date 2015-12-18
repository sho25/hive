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
name|io
operator|.
name|DiskRange
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Disk range information class containing disk ranges and total length.  */
end_comment

begin_class
specifier|public
class|class
name|DiskRangeInfo
block|{
name|List
argument_list|<
name|DiskRange
argument_list|>
name|diskRanges
decl_stmt|;
comment|// TODO: use DiskRangeList instead
name|long
name|totalLength
decl_stmt|;
specifier|public
name|DiskRangeInfo
parameter_list|(
name|int
name|indexBaseOffset
parameter_list|)
block|{
name|this
operator|.
name|diskRanges
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
comment|// Some data is missing from the stream for PPD uncompressed read (because index offset is
comment|// relative to the entire stream and we only read part of stream if RGs are filtered; unlike
comment|// with compressed data where PPD only filters CBs, so we always get full CB, and index offset
comment|// is relative to CB). To take care of the case when UncompressedStream goes seeking around by
comment|// its incorrect (relative to partial stream) index offset, we will increase the length by our
comment|// offset-relative-to-the-stream, and also account for it in buffers (see createDiskRangeInfo).
comment|// So, index offset now works; as long as noone seeks into this data before the RG (why would
comment|// they), everything works. This is hacky... Stream shouldn't depend on having all the data.
name|this
operator|.
name|totalLength
operator|=
name|indexBaseOffset
expr_stmt|;
block|}
specifier|public
name|void
name|addDiskRange
parameter_list|(
name|DiskRange
name|diskRange
parameter_list|)
block|{
name|diskRanges
operator|.
name|add
argument_list|(
name|diskRange
argument_list|)
expr_stmt|;
name|totalLength
operator|+=
name|diskRange
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|DiskRange
argument_list|>
name|getDiskRanges
parameter_list|()
block|{
return|return
name|diskRanges
return|;
block|}
specifier|public
name|long
name|getTotalLength
parameter_list|()
block|{
return|return
name|totalLength
return|;
block|}
block|}
end_class

end_unit

