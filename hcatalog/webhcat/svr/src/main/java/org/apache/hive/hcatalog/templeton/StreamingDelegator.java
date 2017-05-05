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
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|exec
operator|.
name|ExecuteException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|TempletonUtils
import|;
end_import

begin_comment
comment|/**  * Submit a streaming job to the MapReduce queue.  Really just a front  end to the JarDelegator.  *  * This is the backend of the mapreduce/streaming web service.  */
end_comment

begin_class
specifier|public
class|class
name|StreamingDelegator
extends|extends
name|LauncherDelegator
block|{
specifier|public
name|StreamingDelegator
parameter_list|(
name|AppConfig
name|appConf
parameter_list|)
block|{
name|super
argument_list|(
name|appConf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|EnqueueBean
name|run
parameter_list|(
name|String
name|user
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|userArgs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|inputs
parameter_list|,
name|String
name|inputreader
parameter_list|,
name|String
name|output
parameter_list|,
name|String
name|mapper
parameter_list|,
name|String
name|reducer
parameter_list|,
name|String
name|combiner
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fileList
parameter_list|,
name|String
name|files
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|defines
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|cmdenvs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|jarArgs
parameter_list|,
name|String
name|statusdir
parameter_list|,
name|String
name|callback
parameter_list|,
name|String
name|completedUrl
parameter_list|,
name|boolean
name|enableLog
parameter_list|,
name|Boolean
name|enableJobReconnect
parameter_list|,
name|JobType
name|jobType
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|BadParam
throws|,
name|BusyException
throws|,
name|QueueException
throws|,
name|ExecuteException
throws|,
name|IOException
throws|,
name|InterruptedException
throws|,
name|TooManyRequestsException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
name|makeArgs
argument_list|(
name|inputs
argument_list|,
name|inputreader
argument_list|,
name|output
argument_list|,
name|mapper
argument_list|,
name|reducer
argument_list|,
name|combiner
argument_list|,
name|fileList
argument_list|,
name|cmdenvs
argument_list|,
name|jarArgs
argument_list|)
decl_stmt|;
name|JarDelegator
name|d
init|=
operator|new
name|JarDelegator
argument_list|(
name|appConf
argument_list|)
decl_stmt|;
return|return
name|d
operator|.
name|run
argument_list|(
name|user
argument_list|,
name|userArgs
argument_list|,
name|appConf
operator|.
name|streamingJar
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|files
argument_list|,
name|args
argument_list|,
name|defines
argument_list|,
name|statusdir
argument_list|,
name|callback
argument_list|,
literal|false
argument_list|,
name|completedUrl
argument_list|,
name|enableLog
argument_list|,
name|enableJobReconnect
argument_list|,
name|jobType
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|makeArgs
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|inputs
parameter_list|,
name|String
name|inputreader
parameter_list|,
name|String
name|output
parameter_list|,
name|String
name|mapper
parameter_list|,
name|String
name|reducer
parameter_list|,
name|String
name|combiner
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fileList
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|cmdenvs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|jarArgs
parameter_list|)
throws|throws
name|BadParam
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|args
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|input
range|:
name|inputs
control|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-input"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|input
argument_list|)
expr_stmt|;
block|}
name|args
operator|.
name|add
argument_list|(
literal|"-output"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-mapper"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-reducer"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|reducer
argument_list|)
expr_stmt|;
if|if
condition|(
name|inputreader
operator|!=
literal|null
operator|&&
operator|!
name|inputreader
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-inputreader"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|inputreader
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|combiner
argument_list|)
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-combiner"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|combiner
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|f
range|:
name|fileList
control|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-file"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|e
range|:
name|cmdenvs
control|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-cmdenv"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|arg
range|:
name|jarArgs
control|)
block|{
name|args
operator|.
name|add
argument_list|(
name|arg
argument_list|)
expr_stmt|;
block|}
return|return
name|args
return|;
block|}
block|}
end_class

end_unit

