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
name|exec
operator|.
name|tez
operator|.
name|monitoring
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
name|llap
operator|.
name|counters
operator|.
name|LlapIOCounters
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
name|exec
operator|.
name|Utilities
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|counters
operator|.
name|TezCounters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|DAGClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|Progress
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|StatusGetOpts
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
name|text
operator|.
name|DecimalFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import static
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
name|exec
operator|.
name|tez
operator|.
name|monitoring
operator|.
name|Constants
operator|.
name|SEPARATOR
import|;
end_import

begin_import
import|import static
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
name|exec
operator|.
name|tez
operator|.
name|monitoring
operator|.
name|TezJobMonitor
operator|.
name|getCounterValueByGroupName
import|;
end_import

begin_class
specifier|public
class|class
name|LLAPioSummary
implements|implements
name|PrintSummary
block|{
specifier|private
specifier|static
specifier|final
name|String
name|LLAP_SUMMARY_HEADER_FORMAT
init|=
literal|"%10s %9s %9s %10s %9s %10s %9s"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LLAP_IO_SUMMARY_HEADER
init|=
literal|"LLAP IO Summary"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LLAP_SUMMARY_HEADER
init|=
name|String
operator|.
name|format
argument_list|(
name|LLAP_SUMMARY_HEADER_FORMAT
argument_list|,
literal|"VERTICES"
argument_list|,
literal|"ROWGROUPS"
argument_list|,
literal|"META_HIT"
argument_list|,
literal|"META_MISS"
argument_list|,
literal|"DATA_HIT"
argument_list|,
literal|"DATA_MISS"
argument_list|,
literal|"TOTAL_IO"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|DecimalFormat
name|secondsFormatter
init|=
operator|new
name|DecimalFormat
argument_list|(
literal|"#0.00"
argument_list|)
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|progressMap
decl_stmt|;
specifier|private
name|DAGClient
name|dagClient
decl_stmt|;
specifier|private
name|boolean
name|first
init|=
literal|false
decl_stmt|;
name|LLAPioSummary
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|progressMap
parameter_list|,
name|DAGClient
name|dagClient
parameter_list|)
block|{
name|this
operator|.
name|progressMap
operator|=
name|progressMap
expr_stmt|;
name|this
operator|.
name|dagClient
operator|=
name|dagClient
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|print
parameter_list|(
name|SessionState
operator|.
name|LogHelper
name|console
parameter_list|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|LLAP_IO_SUMMARY_HEADER
argument_list|)
expr_stmt|;
name|SortedSet
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|progressMap
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|StatusGetOpts
argument_list|>
name|statusOptions
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|statusOptions
operator|.
name|add
argument_list|(
name|StatusGetOpts
operator|.
name|GET_COUNTERS
argument_list|)
expr_stmt|;
name|String
name|counterGroup
init|=
name|LlapIOCounters
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|vertexName
range|:
name|keys
control|)
block|{
comment|// Reducers do not benefit from LLAP IO so no point in printing
if|if
condition|(
name|vertexName
operator|.
name|startsWith
argument_list|(
literal|"Reducer"
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|TezCounters
name|vertexCounters
init|=
name|vertexCounter
argument_list|(
name|statusOptions
argument_list|,
name|vertexName
argument_list|)
decl_stmt|;
if|if
condition|(
name|vertexCounters
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|LLAP_SUMMARY_HEADER
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
block|}
name|console
operator|.
name|printInfo
argument_list|(
name|vertexSummary
argument_list|(
name|vertexName
argument_list|,
name|counterGroup
argument_list|,
name|vertexCounters
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|console
operator|.
name|printInfo
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|vertexSummary
parameter_list|(
name|String
name|vertexName
parameter_list|,
name|String
name|counterGroup
parameter_list|,
name|TezCounters
name|vertexCounters
parameter_list|)
block|{
specifier|final
name|long
name|selectedRowgroups
init|=
name|getCounterValueByGroupName
argument_list|(
name|vertexCounters
argument_list|,
name|counterGroup
argument_list|,
name|LlapIOCounters
operator|.
name|SELECTED_ROWGROUPS
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|long
name|metadataCacheHit
init|=
name|getCounterValueByGroupName
argument_list|(
name|vertexCounters
argument_list|,
name|counterGroup
argument_list|,
name|LlapIOCounters
operator|.
name|METADATA_CACHE_HIT
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|long
name|metadataCacheMiss
init|=
name|getCounterValueByGroupName
argument_list|(
name|vertexCounters
argument_list|,
name|counterGroup
argument_list|,
name|LlapIOCounters
operator|.
name|METADATA_CACHE_MISS
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|long
name|cacheHitBytes
init|=
name|getCounterValueByGroupName
argument_list|(
name|vertexCounters
argument_list|,
name|counterGroup
argument_list|,
name|LlapIOCounters
operator|.
name|CACHE_HIT_BYTES
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|long
name|cacheMissBytes
init|=
name|getCounterValueByGroupName
argument_list|(
name|vertexCounters
argument_list|,
name|counterGroup
argument_list|,
name|LlapIOCounters
operator|.
name|CACHE_MISS_BYTES
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|long
name|totalIoTime
init|=
name|getCounterValueByGroupName
argument_list|(
name|vertexCounters
argument_list|,
name|counterGroup
argument_list|,
name|LlapIOCounters
operator|.
name|TOTAL_IO_TIME_NS
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|String
operator|.
name|format
argument_list|(
name|LLAP_SUMMARY_HEADER_FORMAT
argument_list|,
name|vertexName
argument_list|,
name|selectedRowgroups
argument_list|,
name|metadataCacheHit
argument_list|,
name|metadataCacheMiss
argument_list|,
name|Utilities
operator|.
name|humanReadableByteCount
argument_list|(
name|cacheHitBytes
argument_list|)
argument_list|,
name|Utilities
operator|.
name|humanReadableByteCount
argument_list|(
name|cacheMissBytes
argument_list|)
argument_list|,
name|secondsFormatter
operator|.
name|format
argument_list|(
name|totalIoTime
operator|/
literal|1000_000_000.0
argument_list|)
operator|+
literal|"s"
argument_list|)
return|;
block|}
specifier|private
name|TezCounters
name|vertexCounter
parameter_list|(
name|Set
argument_list|<
name|StatusGetOpts
argument_list|>
name|statusOptions
parameter_list|,
name|String
name|vertexName
parameter_list|)
block|{
try|try
block|{
return|return
name|dagClient
operator|.
name|getVertexStatus
argument_list|(
name|vertexName
argument_list|,
name|statusOptions
argument_list|)
operator|.
name|getVertexCounters
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|TezException
name|e
parameter_list|)
block|{
comment|// best attempt, shouldn't really kill DAG for this
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

