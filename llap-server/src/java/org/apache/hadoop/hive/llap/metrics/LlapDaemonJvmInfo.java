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
name|metrics
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
name|metrics2
operator|.
name|MetricsInfo
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
name|base
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * Llap daemon JVM info. These are some additional metrics that are not exposed via  * {@link org.apache.hadoop.metrics.jvm.JvmMetrics}  *  * NOTE: These metrics are for sinks supported by hadoop-metrics2. There is already a /jmx endpoint  * that gives all these info.  */
end_comment

begin_enum
specifier|public
enum|enum
name|LlapDaemonJvmInfo
implements|implements
name|MetricsInfo
block|{
name|LlapDaemonJVMMetrics
argument_list|(
literal|"Llap daemon JVM related metrics"
argument_list|)
block|,
name|LlapDaemonDirectBufferCount
argument_list|(
literal|"Total number of direct byte buffers"
argument_list|)
block|,
name|LlapDaemonDirectBufferTotalCapacity
argument_list|(
literal|"Estimate of total capacity of all allocated direct byte buffers in bytes"
argument_list|)
block|,
name|LlapDaemonDirectBufferMemoryUsed
argument_list|(
literal|"Estimate of memory that JVM is using for the allocated buffers in bytes"
argument_list|)
block|,
name|LlapDaemonMappedBufferCount
argument_list|(
literal|"Total number of mapped byte buffers"
argument_list|)
block|,
name|LlapDaemonMappedBufferTotalCapacity
argument_list|(
literal|"Estimate of total capacity of all mapped byte buffers in bytes"
argument_list|)
block|,
name|LlapDaemonMappedBufferMemoryUsed
argument_list|(
literal|"Estimate of memory that JVM is using for mapped byte buffers in bytes"
argument_list|)
block|,
name|LlapDaemonOpenFileDescriptorCount
argument_list|(
literal|"Number of open file descriptors"
argument_list|)
block|,
name|LlapDaemonMaxFileDescriptorCount
argument_list|(
literal|"Maximum number of file descriptors"
argument_list|)
block|,   ;
specifier|private
specifier|final
name|String
name|desc
decl_stmt|;
name|LlapDaemonJvmInfo
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

