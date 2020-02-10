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
name|llap
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
name|NullWritable
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
name|mapred
operator|.
name|InputFormat
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
name|mapred
operator|.
name|InputSplit
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|RecordReader
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
name|mapred
operator|.
name|Reporter
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
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|memory
operator|.
name|BufferAllocator
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
name|arrow
operator|.
name|RootAllocatorFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
import|;
end_import

begin_comment
comment|/*  * Adapts an Arrow batch reader to a row reader  * Only used for testing  */
end_comment

begin_class
specifier|public
class|class
name|LlapArrowRowInputFormat
implements|implements
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|Row
argument_list|>
block|{
specifier|private
name|LlapBaseInputFormat
name|baseInputFormat
decl_stmt|;
specifier|public
name|LlapArrowRowInputFormat
parameter_list|(
name|long
name|arrowAllocatorLimit
parameter_list|)
block|{
name|BufferAllocator
name|allocator
init|=
name|RootAllocatorFactory
operator|.
name|INSTANCE
operator|.
name|getOrCreateRootAllocator
argument_list|(
name|arrowAllocatorLimit
argument_list|)
operator|.
name|newChildAllocator
argument_list|(
comment|//allocator name, use UUID for testing
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
comment|//No use for reservation, allocators claim memory from the same pool,
comment|//but allocate/releases are tracked per-allocator
literal|0
argument_list|,
comment|//Limit passed in by client
name|arrowAllocatorLimit
argument_list|)
decl_stmt|;
name|baseInputFormat
operator|=
operator|new
name|LlapBaseInputFormat
argument_list|(
literal|true
argument_list|,
name|allocator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|baseInputFormat
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
name|numSplits
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|Row
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|LlapInputSplit
name|llapSplit
init|=
operator|(
name|LlapInputSplit
operator|)
name|split
decl_stmt|;
name|LlapArrowBatchRecordReader
name|reader
init|=
operator|(
name|LlapArrowBatchRecordReader
operator|)
name|baseInputFormat
operator|.
name|getRecordReader
argument_list|(
name|llapSplit
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
return|return
operator|new
name|LlapArrowRowRecordReader
argument_list|(
name|job
argument_list|,
name|reader
operator|.
name|getSchema
argument_list|()
argument_list|,
name|reader
argument_list|)
return|;
block|}
block|}
end_class

end_unit

