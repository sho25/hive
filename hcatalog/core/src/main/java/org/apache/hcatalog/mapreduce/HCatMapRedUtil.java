begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
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
name|shims
operator|.
name|ShimLoader
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
name|util
operator|.
name|Progressable
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
name|conf
operator|.
name|Configuration
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
name|JobContext
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptContext
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
name|TaskAttemptID
import|;
end_import

begin_class
specifier|public
class|class
name|HCatMapRedUtil
block|{
specifier|public
specifier|static
name|TaskAttemptContext
name|createTaskAttemptContext
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptContext
name|context
parameter_list|)
block|{
return|return
name|createTaskAttemptContext
argument_list|(
operator|new
name|JobConf
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptID
operator|.
name|forName
argument_list|(
name|context
operator|.
name|getTaskAttemptID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptContext
name|createTaskAttemptContext
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptID
name|id
parameter_list|)
block|{
return|return
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptContext
argument_list|(
name|conf
argument_list|,
name|id
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TaskAttemptContext
name|createTaskAttemptContext
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|TaskAttemptID
name|id
parameter_list|,
name|Progressable
name|progressable
parameter_list|)
block|{
return|return
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptContext
argument_list|(
name|conf
argument_list|,
name|id
argument_list|,
operator|(
name|Reporter
operator|)
name|progressable
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobContext
name|createJobContext
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobContext
name|context
parameter_list|)
block|{
return|return
name|createJobContext
argument_list|(
operator|(
name|JobConf
operator|)
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|context
operator|.
name|getJobID
argument_list|()
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|JobContext
name|createJobContext
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobID
name|id
parameter_list|,
name|Progressable
name|progressable
parameter_list|)
block|{
return|return
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createJobContext
argument_list|(
name|conf
argument_list|,
name|id
argument_list|,
operator|(
name|Reporter
operator|)
name|progressable
argument_list|)
return|;
block|}
block|}
end_class

end_unit

