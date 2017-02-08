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
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|net
operator|.
name|URLDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|StringTokenizer
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
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|UriBuilder
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
name|common
operator|.
name|LogUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|FileStatus
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
name|security
operator|.
name|UserGroupInformation
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
name|Shell
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
name|StringUtils
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
name|UgiFactory
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
name|BadParam
import|;
end_import

begin_comment
comment|/**  * General utility methods.  */
end_comment

begin_class
specifier|public
class|class
name|TempletonUtils
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TempletonUtils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Is the object non-empty?    */
specifier|public
specifier|static
name|boolean
name|isset
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
operator|(
name|s
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|s
operator|.
name|length
argument_list|()
operator|>
literal|0
operator|)
return|;
block|}
comment|/**    * Is the object non-empty?    */
specifier|public
specifier|static
name|boolean
name|isset
parameter_list|(
name|char
name|ch
parameter_list|)
block|{
return|return
operator|(
name|ch
operator|!=
literal|0
operator|)
return|;
block|}
comment|/**    * Is the object non-empty?    */
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|boolean
name|isset
parameter_list|(
name|T
index|[]
name|a
parameter_list|)
block|{
return|return
operator|(
name|a
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|a
operator|.
name|length
operator|>
literal|0
operator|)
return|;
block|}
comment|/**    * Is the object non-empty?    */
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|boolean
name|isset
parameter_list|(
name|Collection
argument_list|<
name|T
argument_list|>
name|col
parameter_list|)
block|{
return|return
operator|(
name|col
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|col
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
comment|/**    * Is the object non-empty?    */
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|boolean
name|isset
parameter_list|(
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|col
parameter_list|)
block|{
return|return
operator|(
name|col
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|col
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
comment|//looking for map 100% reduce 100%
specifier|public
specifier|static
specifier|final
name|Pattern
name|JAR_COMPLETE
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|" map \\d+%\\s+reduce \\d+%$"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Pattern
name|PIG_COMPLETE
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|" \\d+% complete$"
argument_list|)
decl_stmt|;
comment|//looking for map = 100%,  reduce = 100%
specifier|public
specifier|static
specifier|final
name|Pattern
name|HIVE_COMPLETE
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|" map = (\\d+%),\\s+reduce = (\\d+%).*$"
argument_list|)
decl_stmt|;
comment|/**    * Hive on Tez produces progress report that looks like this    * Map 1: -/-	Reducer 2: 0/1    * Map 1: -/-	Reducer 2: 0(+1)/1    * Map 1: -/-	Reducer 2: 1/1    *    * -/- means there are no tasks (yet)    * 0/1 means 1 total tasks, 0 completed    * 1(+2)/3 means 3 total, 1 completed and 2 running    *    * HIVE-8495, in particular https://issues.apache.org/jira/secure/attachment/12675504/Screen%20Shot%202014-10-16%20at%209.35.26%20PM.png    * has more examples.    * To report progress, we'll assume all tasks are equal size and compute "completed" as percent of "total"    * "(Map|Reducer) (\\d+:) ((-/-)|(\\d+(\\(\\+\\d+\\))?/\\d+))" is the complete pattern but we'll drop "-/-" to exclude    * groups that don't add information such as "Map 1: -/-"    */
specifier|public
specifier|static
specifier|final
name|Pattern
name|HIVE_TEZ_COMPLETE
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"(Map|Reducer) (\\d+:) (\\d+(\\(\\+\\d+\\))?/\\d+)"
argument_list|)
decl_stmt|;
comment|/**    * Pig on Tez produces progress report that looks like this    * DAG Status: status=RUNNING, progress=TotalTasks: 3 Succeeded: 0 Running: 0 Failed: 0 Killed: 0    *    * Use Succeeded/TotalTasks to report progress    * There is a hole as Pig might launch more than one DAGs. If this happens, user might    * see progress rewind since the percentage is for the new DAG. To fix this, We need to fix    * Pig print total number of DAGs on console, and track complete DAGs in WebHCat.    */
specifier|public
specifier|static
specifier|final
name|Pattern
name|PIG_TEZ_COMPLETE
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"progress=TotalTasks: (\\d+) Succeeded: (\\d+)"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Pattern
name|TEZ_COUNTERS
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\d+"
argument_list|)
decl_stmt|;
comment|/**    * Extract the percent complete line from Pig or Jar jobs.    */
specifier|public
specifier|static
name|String
name|extractPercentComplete
parameter_list|(
name|String
name|line
parameter_list|)
block|{
name|Matcher
name|jar
init|=
name|JAR_COMPLETE
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|jar
operator|.
name|find
argument_list|()
condition|)
return|return
name|jar
operator|.
name|group
argument_list|()
operator|.
name|trim
argument_list|()
return|;
name|Matcher
name|pig
init|=
name|PIG_COMPLETE
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|pig
operator|.
name|find
argument_list|()
condition|)
return|return
name|pig
operator|.
name|group
argument_list|()
operator|.
name|trim
argument_list|()
return|;
name|Matcher
name|hive
init|=
name|HIVE_COMPLETE
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|hive
operator|.
name|find
argument_list|()
condition|)
block|{
return|return
literal|"map "
operator|+
name|hive
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|+
literal|" reduce "
operator|+
name|hive
operator|.
name|group
argument_list|(
literal|2
argument_list|)
return|;
block|}
name|Matcher
name|hiveTez
init|=
name|HIVE_TEZ_COMPLETE
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveTez
operator|.
name|find
argument_list|()
condition|)
block|{
name|int
name|totalTasks
init|=
literal|0
decl_stmt|;
name|int
name|completedTasks
init|=
literal|0
decl_stmt|;
do|do
block|{
comment|//here each group looks something like "Map 2: 2/4" "Reducer 3: 1(+2)/4"
comment|//just parse the numbers and ignore one from "Map 2" and from "(+2)" if it's there
name|Matcher
name|counts
init|=
name|TEZ_COUNTERS
operator|.
name|matcher
argument_list|(
name|hiveTez
operator|.
name|group
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|items
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|4
argument_list|)
decl_stmt|;
while|while
condition|(
name|counts
operator|.
name|find
argument_list|()
condition|)
block|{
name|items
operator|.
name|add
argument_list|(
name|counts
operator|.
name|group
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|completedTasks
operator|+=
name|Integer
operator|.
name|parseInt
argument_list|(
name|items
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|items
operator|.
name|size
argument_list|()
operator|==
literal|3
condition|)
block|{
name|totalTasks
operator|+=
name|Integer
operator|.
name|parseInt
argument_list|(
name|items
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|totalTasks
operator|+=
name|Integer
operator|.
name|parseInt
argument_list|(
name|items
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
do|while
condition|(
name|hiveTez
operator|.
name|find
argument_list|()
condition|)
do|;
if|if
condition|(
name|totalTasks
operator|==
literal|0
condition|)
block|{
return|return
literal|"0% complete (0 total tasks)"
return|;
block|}
return|return
name|completedTasks
operator|*
literal|100
operator|/
name|totalTasks
operator|+
literal|"% complete"
return|;
block|}
name|Matcher
name|pigTez
init|=
name|PIG_TEZ_COMPLETE
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|pigTez
operator|.
name|find
argument_list|()
condition|)
block|{
name|int
name|totalTasks
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|pigTez
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|completedTasks
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|pigTez
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|totalTasks
operator|==
literal|0
condition|)
block|{
return|return
literal|"0% complete (0 total tasks)"
return|;
block|}
return|return
name|completedTasks
operator|*
literal|100
operator|/
name|totalTasks
operator|+
literal|"% complete"
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
specifier|final
name|Pattern
name|JAR_ID
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|" Running job: (\\S+)$"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Pattern
name|PIG_ID
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|" HadoopJobId: (\\S+)$"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Pattern
index|[]
name|ID_PATTERNS
init|=
block|{
name|JAR_ID
block|,
name|PIG_ID
block|}
decl_stmt|;
comment|/**    * Extract the job id from jar jobs.    */
specifier|public
specifier|static
name|String
name|extractChildJobId
parameter_list|(
name|String
name|line
parameter_list|)
block|{
for|for
control|(
name|Pattern
name|p
range|:
name|ID_PATTERNS
control|)
block|{
name|Matcher
name|m
init|=
name|p
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|find
argument_list|()
condition|)
return|return
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Take an array of strings and encode it into one string.    */
specifier|public
specifier|static
name|String
name|encodeArray
parameter_list|(
name|String
index|[]
name|plain
parameter_list|)
block|{
if|if
condition|(
name|plain
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|String
index|[]
name|escaped
init|=
operator|new
name|String
index|[
name|plain
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
name|plain
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|plain
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
name|plain
index|[
name|i
index|]
operator|=
literal|""
expr_stmt|;
block|}
name|escaped
index|[
name|i
index|]
operator|=
name|StringUtils
operator|.
name|escapeString
argument_list|(
name|plain
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|StringUtils
operator|.
name|arrayToString
argument_list|(
name|escaped
argument_list|)
return|;
block|}
comment|/**    * Encode a List into a string.    */
specifier|public
specifier|static
name|String
name|encodeArray
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|list
parameter_list|)
block|{
if|if
condition|(
name|list
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|String
index|[]
name|array
init|=
operator|new
name|String
index|[
name|list
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
return|return
name|encodeArray
argument_list|(
name|list
operator|.
name|toArray
argument_list|(
name|array
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Take an encode strings and decode it into an array of strings.    */
specifier|public
specifier|static
name|String
index|[]
name|decodeArray
parameter_list|(
name|String
name|s
parameter_list|)
block|{
if|if
condition|(
name|s
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|String
index|[]
name|escaped
init|=
name|StringUtils
operator|.
name|split
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|String
index|[]
name|plain
init|=
operator|new
name|String
index|[
name|escaped
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
name|escaped
operator|.
name|length
condition|;
operator|++
name|i
control|)
name|plain
index|[
name|i
index|]
operator|=
name|StringUtils
operator|.
name|unEscapeString
argument_list|(
name|escaped
index|[
name|i
index|]
argument_list|)
expr_stmt|;
return|return
name|plain
return|;
block|}
specifier|public
specifier|static
name|String
index|[]
name|hadoopFsListAsArray
parameter_list|(
name|String
name|files
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|FileNotFoundException
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|files
operator|==
literal|null
operator|||
name|conf
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
index|[]
name|dirty
init|=
name|files
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|String
index|[]
name|clean
init|=
operator|new
name|String
index|[
name|dirty
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
name|dirty
operator|.
name|length
condition|;
operator|++
name|i
control|)
name|clean
index|[
name|i
index|]
operator|=
name|hadoopFsFilename
argument_list|(
name|dirty
index|[
name|i
index|]
argument_list|,
name|conf
argument_list|,
name|user
argument_list|)
expr_stmt|;
return|return
name|clean
return|;
block|}
specifier|public
specifier|static
name|String
name|hadoopFsListAsString
parameter_list|(
name|String
name|files
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|FileNotFoundException
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|files
operator|==
literal|null
operator|||
name|conf
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|StringUtils
operator|.
name|arrayToString
argument_list|(
name|hadoopFsListAsArray
argument_list|(
name|files
argument_list|,
name|conf
argument_list|,
name|user
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|hadoopFsFilename
parameter_list|(
name|String
name|fname
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|FileNotFoundException
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
name|Path
name|p
init|=
name|hadoopFsPath
argument_list|(
name|fname
argument_list|,
name|conf
argument_list|,
name|user
argument_list|)
decl_stmt|;
if|if
condition|(
name|p
operator|==
literal|null
condition|)
return|return
literal|null
return|;
else|else
return|return
name|p
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Returns all files (non-recursive) in {@code dirName}    */
specifier|public
specifier|static
name|List
argument_list|<
name|Path
argument_list|>
name|hadoopFsListChildren
parameter_list|(
name|String
name|dirName
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
name|Path
name|p
init|=
name|hadoopFsPath
argument_list|(
name|dirName
argument_list|,
name|conf
argument_list|,
name|user
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
name|FileStatus
index|[]
name|children
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isset
argument_list|(
name|children
argument_list|)
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
name|List
argument_list|<
name|Path
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|stat
range|:
name|children
control|)
block|{
name|files
operator|.
name|add
argument_list|(
name|stat
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|files
return|;
block|}
comment|/**    * @return true iff we are sure the file is not there.    */
specifier|public
specifier|static
name|boolean
name|hadoopFsIsMissing
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|p
parameter_list|)
block|{
try|try
block|{
return|return
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// Got an error, might be there anyway due to a
comment|// permissions problem.
return|return
literal|false
return|;
block|}
block|}
specifier|public
specifier|static
name|String
name|addUserHomeDirectoryIfApplicable
parameter_list|(
name|String
name|origPathStr
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|IOException
throws|,
name|URISyntaxException
block|{
name|URI
name|uri
init|=
operator|new
name|URI
argument_list|(
name|origPathStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|uri
operator|.
name|getPath
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|newPath
init|=
literal|"/user/"
operator|+
name|user
decl_stmt|;
name|uri
operator|=
name|UriBuilder
operator|.
name|fromUri
argument_list|(
name|uri
argument_list|)
operator|.
name|replacePath
argument_list|(
name|newPath
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|new
name|Path
argument_list|(
name|uri
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|isAbsolute
argument_list|()
condition|)
block|{
name|String
name|newPath
init|=
literal|"/user/"
operator|+
name|user
operator|+
literal|"/"
operator|+
name|uri
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|uri
operator|=
name|UriBuilder
operator|.
name|fromUri
argument_list|(
name|uri
argument_list|)
operator|.
name|replacePath
argument_list|(
name|newPath
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
comment|// no work needed for absolute paths
return|return
name|uri
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Path
name|hadoopFsPath
parameter_list|(
name|String
name|fname
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|fname
operator|==
literal|null
operator|||
name|conf
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|UserGroupInformation
name|ugi
decl_stmt|;
if|if
condition|(
name|user
operator|!=
literal|null
condition|)
block|{
name|ugi
operator|=
name|UgiFactory
operator|.
name|getUgi
argument_list|(
name|user
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ugi
operator|=
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
expr_stmt|;
block|}
specifier|final
name|String
name|finalFName
init|=
operator|new
name|String
argument_list|(
name|fname
argument_list|)
decl_stmt|;
specifier|final
name|FileSystem
name|defaultFs
init|=
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|FileSystem
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|FileSystem
name|run
parameter_list|()
throws|throws
name|URISyntaxException
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|FileSystem
operator|.
name|get
argument_list|(
operator|new
name|URI
argument_list|(
name|finalFName
argument_list|)
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|fname
operator|=
name|addUserHomeDirectoryIfApplicable
argument_list|(
name|fname
argument_list|,
name|user
argument_list|)
expr_stmt|;
name|URI
name|u
init|=
operator|new
name|URI
argument_list|(
name|fname
argument_list|)
decl_stmt|;
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|u
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|defaultFs
argument_list|)
decl_stmt|;
if|if
condition|(
name|hadoopFsIsMissing
argument_list|(
name|defaultFs
argument_list|,
name|p
argument_list|)
condition|)
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"File "
operator|+
name|fname
operator|+
literal|" does not exist."
argument_list|)
throw|;
return|return
name|p
return|;
block|}
comment|/**    * GET the given url.  Returns the number of bytes received.    */
specifier|public
specifier|static
name|int
name|fetchUrl
parameter_list|(
name|URL
name|url
parameter_list|)
throws|throws
name|IOException
block|{
name|URLConnection
name|cnx
init|=
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|InputStream
name|in
init|=
name|cnx
operator|.
name|getInputStream
argument_list|()
decl_stmt|;
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
literal|8192
index|]
decl_stmt|;
name|int
name|total
init|=
literal|0
decl_stmt|;
name|int
name|len
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
name|len
operator|=
name|in
operator|.
name|read
argument_list|(
name|buf
argument_list|)
operator|)
operator|>=
literal|0
condition|)
name|total
operator|+=
name|len
expr_stmt|;
return|return
name|total
return|;
block|}
comment|/**    * Set the environment variables to specify the hadoop user.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hadoopUserEnv
parameter_list|(
name|String
name|user
parameter_list|,
name|String
name|overrideClasspath
parameter_list|)
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|env
operator|.
name|put
argument_list|(
literal|"HADOOP_USER_NAME"
argument_list|,
name|user
argument_list|)
expr_stmt|;
if|if
condition|(
name|overrideClasspath
operator|!=
literal|null
condition|)
block|{
name|env
operator|.
name|put
argument_list|(
literal|"HADOOP_USER_CLASSPATH_FIRST"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|String
name|cur
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HADOOP_CLASSPATH"
argument_list|)
decl_stmt|;
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|cur
argument_list|)
condition|)
name|overrideClasspath
operator|=
name|overrideClasspath
operator|+
literal|":"
operator|+
name|cur
expr_stmt|;
name|env
operator|.
name|put
argument_list|(
literal|"HADOOP_CLASSPATH"
argument_list|,
name|overrideClasspath
argument_list|)
expr_stmt|;
block|}
return|return
name|env
return|;
block|}
comment|// Add double quotes around the given input parameter if it is not already
comment|// quoted. Quotes are not allowed in the middle of the parameter, and
comment|// BadParam exception is thrown if this is the case.
comment|//
comment|// This method should be used to escape parameters before they get passed to
comment|// Windows cmd scripts (specifically, special characters like a comma or an
comment|// equal sign might be lost as part of the cmd script processing if not
comment|// under quotes).
specifier|public
specifier|static
name|String
name|quoteForWindows
parameter_list|(
name|String
name|param
parameter_list|)
throws|throws
name|BadParam
block|{
if|if
condition|(
name|Shell
operator|.
name|WINDOWS
condition|)
block|{
if|if
condition|(
name|param
operator|!=
literal|null
operator|&&
name|param
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|String
name|nonQuotedPart
init|=
name|param
decl_stmt|;
name|boolean
name|addQuotes
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|param
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'\"'
operator|&&
name|param
operator|.
name|charAt
argument_list|(
name|param
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
operator|==
literal|'\"'
condition|)
block|{
if|if
condition|(
name|param
operator|.
name|length
argument_list|()
operator|<
literal|2
condition|)
throw|throw
operator|new
name|BadParam
argument_list|(
literal|"Passed in parameter is incorrectly quoted: "
operator|+
name|param
argument_list|)
throw|;
name|addQuotes
operator|=
literal|false
expr_stmt|;
name|nonQuotedPart
operator|=
name|param
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|param
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// If we have any quotes other then the outside quotes, throw
if|if
condition|(
name|nonQuotedPart
operator|.
name|contains
argument_list|(
literal|"\""
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|BadParam
argument_list|(
literal|"Passed in parameter is incorrectly quoted: "
operator|+
name|param
argument_list|)
throw|;
block|}
if|if
condition|(
name|addQuotes
condition|)
block|{
name|param
operator|=
literal|'\"'
operator|+
name|param
operator|+
literal|'\"'
expr_stmt|;
block|}
block|}
block|}
return|return
name|param
return|;
block|}
specifier|public
specifier|static
name|void
name|addCmdForWindows
parameter_list|(
name|ArrayList
argument_list|<
name|String
argument_list|>
name|args
parameter_list|)
block|{
if|if
condition|(
name|Shell
operator|.
name|WINDOWS
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"cmd"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"/c"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"call"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * replaces all occurrences of "\," with ","; returns {@code s} if no modifications needed    */
specifier|public
specifier|static
name|String
name|unEscapeString
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
name|s
operator|!=
literal|null
operator|&&
name|s
operator|.
name|contains
argument_list|(
literal|"\\,"
argument_list|)
condition|?
name|StringUtils
operator|.
name|unEscapeString
argument_list|(
name|s
argument_list|)
else|:
name|s
return|;
block|}
comment|/**    * Find a jar that contains a class of the same name and which    * file name matches the given pattern.    *    * @param clazz the class to find.    * @param fileNamePattern regex pattern that must match the jar full path    * @return a jar file that contains the class, or null    */
specifier|public
specifier|static
name|String
name|findContainingJar
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|String
name|fileNamePattern
parameter_list|)
block|{
name|ClassLoader
name|loader
init|=
name|clazz
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|String
name|classFile
init|=
name|clazz
operator|.
name|getName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"\\."
argument_list|,
literal|"/"
argument_list|)
operator|+
literal|".class"
decl_stmt|;
try|try
block|{
for|for
control|(
specifier|final
name|Enumeration
argument_list|<
name|URL
argument_list|>
name|itr
init|=
name|loader
operator|.
name|getResources
argument_list|(
name|classFile
argument_list|)
init|;
name|itr
operator|.
name|hasMoreElements
argument_list|()
condition|;
control|)
block|{
specifier|final
name|URL
name|url
init|=
name|itr
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"jar"
operator|.
name|equals
argument_list|(
name|url
operator|.
name|getProtocol
argument_list|()
argument_list|)
condition|)
block|{
name|String
name|toReturn
init|=
name|url
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileNamePattern
operator|==
literal|null
operator|||
name|toReturn
operator|.
name|matches
argument_list|(
name|fileNamePattern
argument_list|)
condition|)
block|{
name|toReturn
operator|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|toReturn
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
return|return
name|toReturn
operator|.
name|replaceAll
argument_list|(
literal|"!.*$"
argument_list|,
literal|""
argument_list|)
return|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|StringBuilder
name|dumpPropMap
parameter_list|(
name|String
name|header
parameter_list|,
name|Properties
name|props
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|ent
range|:
name|props
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|ent
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|ent
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|ent
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|dumpPropMap
argument_list|(
name|header
argument_list|,
name|map
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|StringBuilder
name|dumpPropMap
parameter_list|(
name|String
name|header
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"START"
argument_list|)
operator|.
name|append
argument_list|(
name|header
argument_list|)
operator|.
name|append
argument_list|(
literal|":\n"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|propKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|map
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|propKeys
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|propKey
range|:
name|propKeys
control|)
block|{
if|if
condition|(
name|propKey
operator|.
name|toLowerCase
argument_list|()
operator|.
name|contains
argument_list|(
literal|"path"
argument_list|)
condition|)
block|{
name|StringTokenizer
name|st
init|=
operator|new
name|StringTokenizer
argument_list|(
name|map
operator|.
name|get
argument_list|(
name|propKey
argument_list|)
argument_list|,
name|File
operator|.
name|pathSeparator
argument_list|)
decl_stmt|;
if|if
condition|(
name|st
operator|.
name|countTokens
argument_list|()
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|propKey
argument_list|)
operator|.
name|append
argument_list|(
literal|"=\n"
argument_list|)
expr_stmt|;
while|while
condition|(
name|st
operator|.
name|hasMoreTokens
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"    "
argument_list|)
operator|.
name|append
argument_list|(
name|st
operator|.
name|nextToken
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
name|File
operator|.
name|pathSeparator
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|propKey
argument_list|)
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
operator|.
name|append
argument_list|(
name|map
operator|.
name|get
argument_list|(
name|propKey
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|propKey
argument_list|)
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
operator|.
name|append
argument_list|(
name|LogUtils
operator|.
name|maskIfPassword
argument_list|(
name|propKey
argument_list|,
name|map
operator|.
name|get
argument_list|(
name|propKey
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|append
argument_list|(
literal|"END"
argument_list|)
operator|.
name|append
argument_list|(
name|header
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
return|;
block|}
block|}
end_class

end_unit

