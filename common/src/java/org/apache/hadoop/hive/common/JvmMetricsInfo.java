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
name|common
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Objects
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
name|metrics2
operator|.
name|MetricsInfo
import|;
end_import

begin_comment
comment|/**  * JVM and logging related metrics info instances. Ported from Hadoop JvmMetricsInfo.  */
end_comment

begin_enum
specifier|public
enum|enum
name|JvmMetricsInfo
implements|implements
name|MetricsInfo
block|{
name|JvmMetrics
argument_list|(
literal|"JVM related metrics etc."
argument_list|)
block|,
comment|// record info
comment|// metrics
name|MemNonHeapUsedM
argument_list|(
literal|"Non-heap memory used in MB"
argument_list|)
block|,
name|MemNonHeapCommittedM
argument_list|(
literal|"Non-heap memory committed in MB"
argument_list|)
block|,
name|MemNonHeapMaxM
argument_list|(
literal|"Non-heap memory max in MB"
argument_list|)
block|,
name|MemHeapUsedM
argument_list|(
literal|"Heap memory used in MB"
argument_list|)
block|,
name|MemHeapCommittedM
argument_list|(
literal|"Heap memory committed in MB"
argument_list|)
block|,
name|MemHeapMaxM
argument_list|(
literal|"Heap memory max in MB"
argument_list|)
block|,
name|MemMaxM
argument_list|(
literal|"Max memory size in MB"
argument_list|)
block|,
name|GcCount
argument_list|(
literal|"Total GC count"
argument_list|)
block|,
name|GcTimeMillis
argument_list|(
literal|"Total GC time in milliseconds"
argument_list|)
block|,
name|ThreadsNew
argument_list|(
literal|"Number of new threads"
argument_list|)
block|,
name|ThreadsRunnable
argument_list|(
literal|"Number of runnable threads"
argument_list|)
block|,
name|ThreadsBlocked
argument_list|(
literal|"Number of blocked threads"
argument_list|)
block|,
name|ThreadsWaiting
argument_list|(
literal|"Number of waiting threads"
argument_list|)
block|,
name|ThreadsTimedWaiting
argument_list|(
literal|"Number of timed waiting threads"
argument_list|)
block|,
name|ThreadsTerminated
argument_list|(
literal|"Number of terminated threads"
argument_list|)
block|,
name|LogFatal
argument_list|(
literal|"Total number of fatal log events"
argument_list|)
block|,
name|LogError
argument_list|(
literal|"Total number of error log events"
argument_list|)
block|,
name|LogWarn
argument_list|(
literal|"Total number of warning log events"
argument_list|)
block|,
name|LogInfo
argument_list|(
literal|"Total number of info log events"
argument_list|)
block|,
name|GcNumWarnThresholdExceeded
argument_list|(
literal|"Number of times that the GC warn threshold is exceeded"
argument_list|)
block|,
name|GcNumInfoThresholdExceeded
argument_list|(
literal|"Number of times that the GC info threshold is exceeded"
argument_list|)
block|,
name|GcTotalExtraSleepTime
argument_list|(
literal|"Total GC extra sleep time in milliseconds"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|desc
decl_stmt|;
name|JvmMetricsInfo
parameter_list|(
name|String
name|desc
parameter_list|)
block|{
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
name|desc
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|toStringHelper
argument_list|(
name|this
argument_list|)
operator|.
name|add
argument_list|(
literal|"name"
argument_list|,
name|name
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"description"
argument_list|,
name|desc
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_enum

end_unit

