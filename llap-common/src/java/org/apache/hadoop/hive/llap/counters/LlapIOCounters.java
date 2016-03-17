begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|counters
package|;
end_package

begin_comment
comment|/**  * LLAP IO related counters.  */
end_comment

begin_enum
specifier|public
enum|enum
name|LlapIOCounters
block|{
name|NUM_VECTOR_BATCHES
block|,
name|NUM_DECODED_BATCHES
block|,
name|SELECTED_ROWGROUPS
block|,
name|NUM_ERRORS
block|,
name|ROWS_EMITTED
block|,
name|METADATA_CACHE_HIT
block|,
name|METADATA_CACHE_MISS
block|,
name|CACHE_HIT_BYTES
block|,
name|CACHE_MISS_BYTES
block|,
name|ALLOCATED_BYTES
block|,
name|ALLOCATED_USED_BYTES
block|,
name|TOTAL_IO_TIME_NS
block|,
name|DECODE_TIME_NS
block|,
name|HDFS_TIME_NS
block|,
name|CONSUMER_TIME_NS
block|}
end_enum

end_unit

