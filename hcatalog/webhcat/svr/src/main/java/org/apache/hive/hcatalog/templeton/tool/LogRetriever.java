begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|tool
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStreamWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
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
name|net
operator|.
name|URLConnection
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
name|regex
operator|.
name|Matcher
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|JobClient
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
name|JobID
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
name|JobStatus
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
name|RunningJob
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
name|LauncherDelegator
operator|.
name|JobType
import|;
end_import

begin_comment
comment|/*  * This class provides support to collect mapreduce stderr/stdout/syslogs  * from jobtracker, and stored into a hdfs location. The log directory layout is:  *<ul compact>  *<li>logs/$job_id (directory for $job_id)  *<li>logs/$job_id/job.xml.html  *<li>logs/$job_id/$attempt_id (directory for $attempt_id)  *<li>logs/$job_id/$attempt_id/stderr  *<li>logs/$job_id/$attempt_id/stdout  *<li>logs/$job_id/$attempt_id/syslog   * Since there is no API to retrieve mapreduce log from jobtracker, the code retrieve  * it from jobtracker ui and parse the html file. The current parser only works with  * Hadoop 1, for Hadoop 2, we would need a different parser  */
end_comment

begin_class
specifier|public
class|class
name|LogRetriever
block|{
name|String
name|statusDir
decl_stmt|;
name|JobType
name|jobType
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|attemptDetailPatternInString
init|=
literal|"<a href=\"(taskdetails.jsp\\?.*?)\">"
decl_stmt|;
specifier|private
specifier|static
name|Pattern
name|attemptDetailPattern
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|attemptLogPatternInString
init|=
literal|"Last 8KB</a><br/><a href=\"(.*?tasklog\\?attemptid=.*?)\">All</a>"
decl_stmt|;
specifier|private
specifier|static
name|Pattern
name|attemptLogPattern
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|attemptIDPatternInString
init|=
literal|"attemptid=(.*)?&"
decl_stmt|;
specifier|private
specifier|static
name|Pattern
name|attemptIDPattern
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|attemptStartTimePatternInString
init|=
literal|"<td>(\\d{1,2}-[A-Za-z]{3}-\\d{4} \\d{2}:\\d{2}:\\d{2})(<br/>)?</td>"
decl_stmt|;
specifier|private
specifier|static
name|Pattern
name|attemptStartTimePattern
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|attemptEndTimePatternInString
init|=
literal|"<td>(\\d{1,2}-[A-Za-z]{3}-\\d{4} \\d{2}:\\d{2}:\\d{2}) \\(.*\\)(<br/>)?</td>"
decl_stmt|;
specifier|private
specifier|static
name|Pattern
name|attemptEndTimePattern
init|=
literal|null
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|JobClient
name|jobClient
init|=
literal|null
decl_stmt|;
specifier|private
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
comment|// Class to store necessary information for an attempt to log
specifier|static
class|class
name|AttemptInfo
block|{
specifier|public
name|String
name|id
decl_stmt|;
specifier|public
name|URL
name|baseUrl
decl_stmt|;
specifier|public
enum|enum
name|AttemptStatus
block|{
name|COMPLETED
block|,
name|FAILED
block|}
empty_stmt|;
name|AttemptStatus
name|status
decl_stmt|;
specifier|public
name|String
name|startTime
decl_stmt|;
specifier|public
name|String
name|endTime
decl_stmt|;
specifier|public
name|String
name|type
init|=
literal|"unknown"
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|id
operator|+
literal|"\t"
operator|+
name|baseUrl
operator|.
name|toString
argument_list|()
operator|+
literal|"\t"
operator|+
name|status
operator|.
name|toString
argument_list|()
operator|+
literal|"\t"
operator|+
name|type
operator|+
literal|"\t"
operator|+
name|startTime
operator|+
literal|"\t"
operator|+
name|endTime
operator|+
literal|"\n"
return|;
block|}
block|}
comment|/*    * @param statusDir directory of statusDir defined for the webhcat job. It is supposed    *                  to contain stdout/stderr/syslog for the webhcat controller job    * @param jobType   Currently we support pig/hive/stream/generic mapreduce. The specific    *                  parser will parse the log of the controller job and retrieve job_id    *                  of all mapreduce jobs it launches. The generic mapreduce parser works    *                  when the program use JobClient.runJob to submit the job, but if the program    *                  use other API, generic mapreduce parser is not guaranteed to find the job_id    * @param conf      Configuration for webhcat    */
specifier|public
name|LogRetriever
parameter_list|(
name|String
name|statusDir
parameter_list|,
name|JobType
name|jobType
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|statusDir
operator|=
name|statusDir
expr_stmt|;
name|this
operator|.
name|jobType
operator|=
name|jobType
expr_stmt|;
name|attemptDetailPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|attemptDetailPatternInString
argument_list|)
expr_stmt|;
name|attemptLogPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|attemptLogPatternInString
argument_list|)
expr_stmt|;
name|attemptIDPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|attemptIDPatternInString
argument_list|)
expr_stmt|;
name|attemptStartTimePattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|attemptStartTimePatternInString
argument_list|)
expr_stmt|;
name|attemptEndTimePattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|attemptEndTimePatternInString
argument_list|)
expr_stmt|;
name|Path
name|statusPath
init|=
operator|new
name|Path
argument_list|(
name|statusDir
argument_list|)
decl_stmt|;
name|fs
operator|=
name|statusPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|jobClient
operator|=
operator|new
name|JobClient
argument_list|(
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|logDir
init|=
name|statusDir
operator|+
literal|"/logs"
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
name|logDir
argument_list|)
argument_list|)
expr_stmt|;
comment|// Get jobids from job status dir
name|JobIDParser
name|jobIDParser
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|jobType
condition|)
block|{
case|case
name|PIG
case|:
name|jobIDParser
operator|=
operator|new
name|PigJobIDParser
argument_list|(
name|statusDir
argument_list|,
name|conf
argument_list|)
expr_stmt|;
break|break;
case|case
name|HIVE
case|:
name|jobIDParser
operator|=
operator|new
name|HiveJobIDParser
argument_list|(
name|statusDir
argument_list|,
name|conf
argument_list|)
expr_stmt|;
break|break;
case|case
name|SQOOP
case|:
case|case
name|JAR
case|:
case|case
name|STREAMING
case|:
name|jobIDParser
operator|=
operator|new
name|JarJobIDParser
argument_list|(
name|statusDir
argument_list|,
name|conf
argument_list|)
expr_stmt|;
break|break;
default|default:
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unknown job type:"
operator|+
name|jobType
operator|!=
literal|null
condition|?
name|jobType
operator|.
name|toString
argument_list|()
else|:
literal|"null"
operator|+
literal|", only pig/hive/jar/streaming/sqoop are supported, skip logs"
argument_list|)
expr_stmt|;
return|return;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|jobs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|jobs
operator|=
name|jobIDParser
operator|.
name|parseJobID
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Cannot retrieve jobid from log file"
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
comment|// Logger jobs
name|PrintWriter
name|listWriter
init|=
literal|null
decl_stmt|;
try|try
block|{
name|listWriter
operator|=
operator|new
name|PrintWriter
argument_list|(
operator|new
name|OutputStreamWriter
argument_list|(
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|logDir
argument_list|,
literal|"list.txt"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|job
range|:
name|jobs
control|)
block|{
try|try
block|{
name|logJob
argument_list|(
name|logDir
argument_list|,
name|job
argument_list|,
name|listWriter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Cannot retrieve log for "
operator|+
name|job
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|listWriter
operator|!=
literal|null
condition|)
block|{
name|listWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|logJob
parameter_list|(
name|String
name|logDir
parameter_list|,
name|String
name|jobID
parameter_list|,
name|PrintWriter
name|listWriter
parameter_list|)
throws|throws
name|IOException
block|{
name|RunningJob
name|rj
init|=
name|jobClient
operator|.
name|getJob
argument_list|(
name|JobID
operator|.
name|forName
argument_list|(
name|jobID
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|jobURLString
init|=
name|rj
operator|.
name|getTrackingURL
argument_list|()
decl_stmt|;
name|Path
name|jobDir
init|=
operator|new
name|Path
argument_list|(
name|logDir
argument_list|,
name|jobID
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|jobDir
argument_list|)
expr_stmt|;
comment|// Logger jobconf
try|try
block|{
name|logJobConf
argument_list|(
name|jobID
argument_list|,
name|jobURLString
argument_list|,
name|jobDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Cannot retrieve job.xml.html for "
operator|+
name|jobID
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|listWriter
operator|.
name|println
argument_list|(
literal|"job: "
operator|+
name|jobID
operator|+
literal|"("
operator|+
literal|"name="
operator|+
name|rj
operator|.
name|getJobName
argument_list|()
operator|+
literal|","
operator|+
literal|"status="
operator|+
name|JobStatus
operator|.
name|getJobRunState
argument_list|(
name|rj
operator|.
name|getJobState
argument_list|()
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
comment|// Get completed attempts
name|List
argument_list|<
name|AttemptInfo
argument_list|>
name|attempts
init|=
operator|new
name|ArrayList
argument_list|<
name|AttemptInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|type
range|:
operator|new
name|String
index|[]
block|{
literal|"map"
block|,
literal|"reduce"
block|,
literal|"setup"
block|,
literal|"cleanup"
block|}
control|)
block|{
try|try
block|{
name|List
argument_list|<
name|AttemptInfo
argument_list|>
name|successAttempts
init|=
name|getCompletedAttempts
argument_list|(
name|jobID
argument_list|,
name|jobURLString
argument_list|,
name|type
argument_list|)
decl_stmt|;
name|attempts
operator|.
name|addAll
argument_list|(
name|successAttempts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Cannot retrieve "
operator|+
name|type
operator|+
literal|" tasks for "
operator|+
name|jobID
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Get failed attempts
try|try
block|{
name|List
argument_list|<
name|AttemptInfo
argument_list|>
name|failedAttempts
init|=
name|getFailedAttempts
argument_list|(
name|jobID
argument_list|,
name|jobURLString
argument_list|)
decl_stmt|;
name|attempts
operator|.
name|addAll
argument_list|(
name|failedAttempts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Cannot retrieve failed attempts for "
operator|+
name|jobID
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
comment|// Logger attempts
for|for
control|(
name|AttemptInfo
name|attempt
range|:
name|attempts
control|)
block|{
try|try
block|{
name|logAttempt
argument_list|(
name|jobID
argument_list|,
name|attempt
argument_list|,
name|jobDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|listWriter
operator|.
name|println
argument_list|(
literal|"  attempt:"
operator|+
name|attempt
operator|.
name|id
operator|+
literal|"("
operator|+
literal|"type="
operator|+
name|attempt
operator|.
name|type
operator|+
literal|","
operator|+
literal|"status="
operator|+
name|attempt
operator|.
name|status
operator|+
literal|","
operator|+
literal|"starttime="
operator|+
name|attempt
operator|.
name|startTime
operator|+
literal|","
operator|+
literal|"endtime="
operator|+
name|attempt
operator|.
name|endTime
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Cannot log attempt "
operator|+
name|attempt
operator|.
name|id
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
name|listWriter
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
comment|// Utility to get patterns from a url, every array element is match for one
comment|// pattern
specifier|private
name|List
argument_list|<
name|String
argument_list|>
index|[]
name|getMatches
parameter_list|(
name|URL
name|url
parameter_list|,
name|Pattern
index|[]
name|pattern
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
index|[]
name|results
init|=
operator|new
name|ArrayList
index|[
name|pattern
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|pattern
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|URLConnection
name|urlConnection
init|=
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|urlConnection
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|pattern
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Matcher
name|matcher
init|=
name|pattern
index|[
name|i
index|]
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|results
index|[
name|i
index|]
operator|.
name|add
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|results
return|;
block|}
comment|// Retrieve job conf into logDir
specifier|private
name|void
name|logJobConf
parameter_list|(
name|String
name|job
parameter_list|,
name|String
name|jobURLInString
parameter_list|,
name|String
name|jobDir
parameter_list|)
throws|throws
name|IOException
block|{
name|URL
name|jobURL
init|=
operator|new
name|URL
argument_list|(
name|jobURLInString
argument_list|)
decl_stmt|;
name|String
name|fileInURL
init|=
literal|"/jobconf.jsp?jobid="
operator|+
name|job
decl_stmt|;
name|URL
name|jobTasksURL
init|=
operator|new
name|URL
argument_list|(
name|jobURL
operator|.
name|getProtocol
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getHost
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getPort
argument_list|()
argument_list|,
name|fileInURL
argument_list|)
decl_stmt|;
name|URLConnection
name|urlConnection
init|=
name|jobTasksURL
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|BufferedReader
name|reader
init|=
literal|null
decl_stmt|;
name|PrintWriter
name|writer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|reader
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|urlConnection
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|=
operator|new
name|PrintWriter
argument_list|(
operator|new
name|OutputStreamWriter
argument_list|(
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|jobDir
argument_list|,
literal|"job.xml.html"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Copy conf file
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|println
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|// Get completed attempts from jobtasks.jsp
specifier|private
name|List
argument_list|<
name|AttemptInfo
argument_list|>
name|getCompletedAttempts
parameter_list|(
name|String
name|job
parameter_list|,
name|String
name|jobURLInString
parameter_list|,
name|String
name|type
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Get task detail link from the jobtask page
name|String
name|fileInURL
init|=
literal|"/jobtasks.jsp?jobid="
operator|+
name|job
operator|+
literal|"&type="
operator|+
name|type
operator|+
literal|"&pagenum=1&state=completed"
decl_stmt|;
name|URL
name|jobURL
init|=
operator|new
name|URL
argument_list|(
name|jobURLInString
argument_list|)
decl_stmt|;
name|URL
name|jobTasksURL
init|=
operator|new
name|URL
argument_list|(
name|jobURL
operator|.
name|getProtocol
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getHost
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getPort
argument_list|()
argument_list|,
name|fileInURL
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
index|[]
name|taskAttemptURLAndTimestamp
init|=
name|getMatches
argument_list|(
name|jobTasksURL
argument_list|,
operator|new
name|Pattern
index|[]
block|{
name|attemptDetailPattern
block|,
name|attemptStartTimePattern
block|,
name|attemptEndTimePattern
block|}
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|AttemptInfo
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|AttemptInfo
argument_list|>
argument_list|()
decl_stmt|;
comment|// Go to task details, fetch task tracker url
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|taskAttemptURLAndTimestamp
index|[
literal|0
index|]
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|taskString
init|=
name|taskAttemptURLAndTimestamp
index|[
literal|0
index|]
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|URL
name|taskDetailsURL
init|=
operator|new
name|URL
argument_list|(
name|jobURL
operator|.
name|getProtocol
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getHost
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getPort
argument_list|()
argument_list|,
literal|"/"
operator|+
name|taskString
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
index|[]
name|attemptLogStrings
init|=
name|getMatches
argument_list|(
name|taskDetailsURL
argument_list|,
operator|new
name|Pattern
index|[]
block|{
name|attemptLogPattern
block|}
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|attemptLogString
range|:
name|attemptLogStrings
index|[
literal|0
index|]
control|)
block|{
name|AttemptInfo
name|attempt
init|=
operator|new
name|AttemptInfo
argument_list|()
decl_stmt|;
name|attempt
operator|.
name|baseUrl
operator|=
operator|new
name|URL
argument_list|(
name|attemptLogString
argument_list|)
expr_stmt|;
name|attempt
operator|.
name|startTime
operator|=
name|taskAttemptURLAndTimestamp
index|[
literal|1
index|]
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|attempt
operator|.
name|endTime
operator|=
name|taskAttemptURLAndTimestamp
index|[
literal|2
index|]
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|attempt
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|Matcher
name|matcher
init|=
name|attemptIDPattern
operator|.
name|matcher
argument_list|(
name|attemptLogString
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|attempt
operator|.
name|id
operator|=
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|attempt
operator|.
name|status
operator|=
name|AttemptInfo
operator|.
name|AttemptStatus
operator|.
name|COMPLETED
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
name|attempt
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|results
return|;
block|}
comment|// Get failed attempts from jobfailures.jsp
specifier|private
name|List
argument_list|<
name|AttemptInfo
argument_list|>
name|getFailedAttempts
parameter_list|(
name|String
name|job
parameter_list|,
name|String
name|jobURLInString
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|fileInURL
init|=
literal|"/jobfailures.jsp?jobid="
operator|+
name|job
operator|+
literal|"&kind=all&cause=failed"
decl_stmt|;
name|URL
name|jobURL
init|=
operator|new
name|URL
argument_list|(
name|jobURLInString
argument_list|)
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|jobURL
operator|.
name|getProtocol
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getHost
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getPort
argument_list|()
argument_list|,
name|fileInURL
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
index|[]
name|attemptLogStrings
init|=
name|getMatches
argument_list|(
name|url
argument_list|,
operator|new
name|Pattern
index|[]
block|{
name|attemptDetailPattern
block|}
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|failedTaskStrings
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
name|attempt
range|:
name|attemptLogStrings
index|[
literal|0
index|]
control|)
block|{
if|if
condition|(
operator|!
name|failedTaskStrings
operator|.
name|contains
argument_list|(
name|attempt
argument_list|)
condition|)
block|{
name|failedTaskStrings
operator|.
name|add
argument_list|(
name|attempt
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|AttemptInfo
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|AttemptInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|taskString
range|:
name|failedTaskStrings
control|)
block|{
name|URL
name|taskDetailsURL
init|=
operator|new
name|URL
argument_list|(
name|jobURL
operator|.
name|getProtocol
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getHost
argument_list|()
argument_list|,
name|jobURL
operator|.
name|getPort
argument_list|()
argument_list|,
literal|"/"
operator|+
name|taskString
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
index|[]
name|taskAttemptURLAndTimestamp
init|=
name|getMatches
argument_list|(
name|taskDetailsURL
argument_list|,
operator|new
name|Pattern
index|[]
block|{
name|attemptLogPattern
block|,
name|attemptStartTimePattern
block|,
name|attemptEndTimePattern
block|}
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|taskAttemptURLAndTimestamp
index|[
literal|0
index|]
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|attemptLogString
init|=
name|taskAttemptURLAndTimestamp
index|[
literal|0
index|]
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|AttemptInfo
name|attempt
init|=
operator|new
name|AttemptInfo
argument_list|()
decl_stmt|;
name|attempt
operator|.
name|baseUrl
operator|=
operator|new
name|URL
argument_list|(
name|attemptLogString
argument_list|)
expr_stmt|;
name|attempt
operator|.
name|startTime
operator|=
name|taskAttemptURLAndTimestamp
index|[
literal|1
index|]
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|attempt
operator|.
name|endTime
operator|=
name|taskAttemptURLAndTimestamp
index|[
literal|2
index|]
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|Matcher
name|matcher
init|=
name|attemptIDPattern
operator|.
name|matcher
argument_list|(
name|attemptLogString
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|attempt
operator|.
name|id
operator|=
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|attempt
operator|.
name|id
operator|.
name|contains
argument_list|(
literal|"_r_"
argument_list|)
condition|)
block|{
name|attempt
operator|.
name|type
operator|=
literal|"reduce"
expr_stmt|;
block|}
name|attempt
operator|.
name|status
operator|=
name|AttemptInfo
operator|.
name|AttemptStatus
operator|.
name|COMPLETED
operator|.
name|FAILED
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
name|attempt
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|results
return|;
block|}
comment|// Retrieve attempt log into logDir
specifier|private
name|void
name|logAttempt
parameter_list|(
name|String
name|job
parameter_list|,
name|AttemptInfo
name|attemptInfo
parameter_list|,
name|String
name|logDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|attemptDir
init|=
operator|new
name|Path
argument_list|(
name|logDir
argument_list|,
name|attemptInfo
operator|.
name|id
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|attemptDir
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|type
range|:
operator|new
name|String
index|[]
block|{
literal|"stderr"
block|,
literal|"stdout"
block|,
literal|"syslog"
block|}
control|)
block|{
comment|// Retrieve log from task tracker
name|String
name|fileInURL
init|=
literal|"tasklog?attemptid="
operator|+
name|attemptInfo
operator|.
name|id
operator|+
literal|"&plaintext=true&filter="
operator|+
name|type
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|attemptInfo
operator|.
name|baseUrl
operator|.
name|getProtocol
argument_list|()
argument_list|,
name|attemptInfo
operator|.
name|baseUrl
operator|.
name|getHost
argument_list|()
argument_list|,
name|attemptInfo
operator|.
name|baseUrl
operator|.
name|getPort
argument_list|()
argument_list|,
literal|"/"
operator|+
name|fileInURL
argument_list|)
decl_stmt|;
name|URLConnection
name|urlConnection
init|=
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|BufferedReader
name|reader
init|=
literal|null
decl_stmt|;
name|PrintWriter
name|writer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|reader
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|urlConnection
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|=
operator|new
name|PrintWriter
argument_list|(
operator|new
name|OutputStreamWriter
argument_list|(
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|attemptDir
argument_list|,
name|type
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Copy log file
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|println
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

