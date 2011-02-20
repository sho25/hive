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
name|ql
operator|.
name|exec
package|;
end_package

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
name|hadoop
operator|.
name|mapred
operator|.
name|Counters
import|;
end_import

begin_comment
comment|/**  * TaskHandle.  *  */
end_comment

begin_class
specifier|public
class|class
name|TaskHandle
block|{
comment|// The eventual goal is to monitor the progress of all the tasks, not only the
comment|// map reduce task.
comment|// The execute() method of the tasks will return immediately, and return a
comment|// task specific handle to
comment|// monitor the progress of that task.
comment|// Right now, the behavior is kind of broken, ExecDriver's execute method
comment|// calls progress - instead it should
comment|// be invoked by Driver
specifier|public
name|Counters
name|getCounters
parameter_list|()
throws|throws
name|IOException
block|{
comment|// default implementation
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

