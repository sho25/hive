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
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|logging
operator|.
name|Log
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
name|io
operator|.
name|IOUtils
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

begin_comment
comment|/**  * Intelligence to make clients wait if the cluster is in a bad state.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|Throttle
block|{
comment|// The percentage of maximum allocated memory that triggers GC
comment|// on job tracker. This could be overridden thru the jobconf.
comment|// The default is such that there is no throttling.
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_MEMORY_GC_PERCENT
init|=
literal|100
decl_stmt|;
comment|// sleep this many seconds between each retry.
comment|// This could be overridden thru the jobconf.
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_RETRY_PERIOD
init|=
literal|60
decl_stmt|;
comment|/**    * Fetch http://tracker.om:/gc.jsp?threshold=period.    */
specifier|public
specifier|static
name|void
name|checkJobTracker
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|Log
name|LOG
parameter_list|)
block|{
try|try
block|{
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
name|int
name|threshold
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"mapred.throttle.threshold.percent"
argument_list|,
name|DEFAULT_MEMORY_GC_PERCENT
argument_list|)
decl_stmt|;
name|int
name|retry
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"mapred.throttle.retry.period"
argument_list|,
name|DEFAULT_RETRY_PERIOD
argument_list|)
decl_stmt|;
comment|// If the threshold is 100 percent, then there is no throttling
if|if
condition|(
name|threshold
operator|==
literal|100
condition|)
block|{
return|return;
block|}
comment|// This is the Job Tracker URL
name|String
name|tracker
init|=
name|JobTrackerURLResolver
operator|.
name|getURL
argument_list|(
name|conf
argument_list|)
operator|+
literal|"/gc.jsp?threshold="
operator|+
name|threshold
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
comment|// read in the first 1K characters from the URL
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|tracker
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Throttle: URL "
operator|+
name|tracker
argument_list|)
expr_stmt|;
name|InputStream
name|in
init|=
literal|null
decl_stmt|;
try|try
block|{
name|in
operator|=
name|url
operator|.
name|openStream
argument_list|()
expr_stmt|;
name|in
operator|.
name|read
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
name|in
operator|=
literal|null
expr_stmt|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|String
name|fetchString
init|=
operator|new
name|String
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
comment|// fetch the xml tag<dogc>xxx</dogc>
name|Pattern
name|dowait
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"<dogc>"
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
operator||
name|Pattern
operator|.
name|DOTALL
operator||
name|Pattern
operator|.
name|MULTILINE
argument_list|)
decl_stmt|;
name|String
index|[]
name|results
init|=
name|dowait
operator|.
name|split
argument_list|(
name|fetchString
argument_list|)
decl_stmt|;
if|if
condition|(
name|results
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Throttle: Unable to parse response of URL "
operator|+
name|url
operator|+
literal|". Get retuned "
operator|+
name|fetchString
argument_list|)
throw|;
block|}
name|dowait
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"</dogc>"
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
operator||
name|Pattern
operator|.
name|DOTALL
operator||
name|Pattern
operator|.
name|MULTILINE
argument_list|)
expr_stmt|;
name|results
operator|=
name|dowait
operator|.
name|split
argument_list|(
name|results
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|results
operator|.
name|length
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Throttle: Unable to parse response of URL "
operator|+
name|url
operator|+
literal|". Get retuned "
operator|+
name|fetchString
argument_list|)
throw|;
block|}
comment|// if the jobtracker signalled that the threshold is not exceeded,
comment|// then we return immediately.
if|if
condition|(
name|results
index|[
literal|0
index|]
operator|.
name|trim
argument_list|()
operator|.
name|compareToIgnoreCase
argument_list|(
literal|"false"
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return;
block|}
comment|// The JobTracker has exceeded its threshold and is doing a GC.
comment|// The client has to wait and retry.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Job is being throttled because of resource crunch on the "
operator|+
literal|"JobTracker. Will retry in "
operator|+
name|retry
operator|+
literal|" seconds.."
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|retry
operator|*
literal|1000L
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Job is not being throttled. "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Throttle
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

