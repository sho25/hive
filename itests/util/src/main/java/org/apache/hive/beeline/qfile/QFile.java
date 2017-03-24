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
name|hive
operator|.
name|beeline
operator|.
name|qfile
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FileUtils
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|StreamPrinter
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
name|LinkedHashMap
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
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * Class for representing a Query and the connected files. It provides accessors for the specific  * input and output files, and provides methods for filtering the output of the runs.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|QFile
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
name|QFile
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|File
name|inputFile
decl_stmt|;
specifier|private
name|File
name|rawOutputFile
decl_stmt|;
specifier|private
name|File
name|outputFile
decl_stmt|;
specifier|private
name|File
name|expcetedOutputFile
decl_stmt|;
specifier|private
name|File
name|logFile
decl_stmt|;
specifier|private
name|File
name|infraLogFile
decl_stmt|;
specifier|private
specifier|static
name|RegexFilterSet
name|staticFilterSet
init|=
name|getStaticFilterSet
argument_list|()
decl_stmt|;
specifier|private
name|RegexFilterSet
name|specificFilterSet
decl_stmt|;
specifier|private
name|QFile
parameter_list|()
block|{}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|File
name|getInputFile
parameter_list|()
block|{
return|return
name|inputFile
return|;
block|}
specifier|public
name|File
name|getRawOutputFile
parameter_list|()
block|{
return|return
name|rawOutputFile
return|;
block|}
specifier|public
name|File
name|getOutputFile
parameter_list|()
block|{
return|return
name|outputFile
return|;
block|}
specifier|public
name|File
name|getExpectedOutputFile
parameter_list|()
block|{
return|return
name|expcetedOutputFile
return|;
block|}
specifier|public
name|File
name|getLogFile
parameter_list|()
block|{
return|return
name|logFile
return|;
block|}
specifier|public
name|File
name|getInfraLogFile
parameter_list|()
block|{
return|return
name|infraLogFile
return|;
block|}
specifier|public
name|void
name|filterOutput
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|rawOutput
init|=
name|FileUtils
operator|.
name|readFileToString
argument_list|(
name|rawOutputFile
argument_list|)
decl_stmt|;
name|String
name|filteredOutput
init|=
name|staticFilterSet
operator|.
name|filter
argument_list|(
name|specificFilterSet
operator|.
name|filter
argument_list|(
name|rawOutput
argument_list|)
argument_list|)
decl_stmt|;
name|FileUtils
operator|.
name|writeStringToFile
argument_list|(
name|outputFile
argument_list|,
name|filteredOutput
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|compareResults
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
operator|!
name|expcetedOutputFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Expected results file does not exist: "
operator|+
name|expcetedOutputFile
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|executeDiff
argument_list|()
return|;
block|}
specifier|public
name|void
name|overwriteResults
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|expcetedOutputFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtils
operator|.
name|forceDelete
argument_list|(
name|expcetedOutputFile
argument_list|)
expr_stmt|;
block|}
name|FileUtils
operator|.
name|copyFile
argument_list|(
name|outputFile
argument_list|,
name|expcetedOutputFile
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|executeDiff
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|diffCommandArgs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|diffCommandArgs
operator|.
name|add
argument_list|(
literal|"diff"
argument_list|)
expr_stmt|;
comment|// Text file comparison
name|diffCommandArgs
operator|.
name|add
argument_list|(
literal|"-a"
argument_list|)
expr_stmt|;
if|if
condition|(
name|Shell
operator|.
name|WINDOWS
condition|)
block|{
comment|// Ignore changes in the amount of white space
name|diffCommandArgs
operator|.
name|add
argument_list|(
literal|"-b"
argument_list|)
expr_stmt|;
comment|// Files created on Windows machines have different line endings
comment|// than files created on Unix/Linux. Windows uses carriage return and line feed
comment|// ("\r\n") as a line ending, whereas Unix uses just line feed ("\n").
comment|// Also StringBuilder.toString(), Stream to String conversions adds extra
comment|// spaces at the end of the line.
name|diffCommandArgs
operator|.
name|add
argument_list|(
literal|"--strip-trailing-cr"
argument_list|)
expr_stmt|;
comment|// Strip trailing carriage return on input
name|diffCommandArgs
operator|.
name|add
argument_list|(
literal|"-B"
argument_list|)
expr_stmt|;
comment|// Ignore changes whose lines are all blank
block|}
comment|// Add files to compare to the arguments list
name|diffCommandArgs
operator|.
name|add
argument_list|(
name|getQuotedString
argument_list|(
name|expcetedOutputFile
argument_list|)
argument_list|)
expr_stmt|;
name|diffCommandArgs
operator|.
name|add
argument_list|(
name|getQuotedString
argument_list|(
name|outputFile
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Running: "
operator|+
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
operator|.
name|join
argument_list|(
name|diffCommandArgs
argument_list|,
literal|' '
argument_list|)
argument_list|)
expr_stmt|;
name|Process
name|executor
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
name|diffCommandArgs
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|diffCommandArgs
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|StreamPrinter
name|errPrinter
init|=
operator|new
name|StreamPrinter
argument_list|(
name|executor
operator|.
name|getErrorStream
argument_list|()
argument_list|,
literal|null
argument_list|,
name|System
operator|.
name|err
argument_list|)
decl_stmt|;
name|StreamPrinter
name|outPrinter
init|=
operator|new
name|StreamPrinter
argument_list|(
name|executor
operator|.
name|getInputStream
argument_list|()
argument_list|,
literal|null
argument_list|,
name|System
operator|.
name|out
argument_list|)
decl_stmt|;
name|outPrinter
operator|.
name|start
argument_list|()
expr_stmt|;
name|errPrinter
operator|.
name|start
argument_list|()
expr_stmt|;
name|int
name|result
init|=
name|executor
operator|.
name|waitFor
argument_list|()
decl_stmt|;
name|outPrinter
operator|.
name|join
argument_list|()
expr_stmt|;
name|errPrinter
operator|.
name|join
argument_list|()
expr_stmt|;
name|executor
operator|.
name|waitFor
argument_list|()
expr_stmt|;
return|return
operator|(
name|result
operator|==
literal|0
operator|)
return|;
block|}
specifier|private
specifier|static
name|String
name|getQuotedString
parameter_list|(
name|File
name|file
parameter_list|)
block|{
return|return
name|Shell
operator|.
name|WINDOWS
condition|?
name|String
operator|.
name|format
argument_list|(
literal|"\"%s\""
argument_list|,
name|file
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
else|:
name|file
operator|.
name|getAbsolutePath
argument_list|()
return|;
block|}
specifier|private
specifier|static
class|class
name|RegexFilterSet
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
name|regexFilters
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|RegexFilterSet
name|addFilter
parameter_list|(
name|String
name|regex
parameter_list|,
name|String
name|replacement
parameter_list|)
block|{
name|regexFilters
operator|.
name|put
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|regex
argument_list|)
argument_list|,
name|replacement
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|String
name|filter
parameter_list|(
name|String
name|input
parameter_list|)
block|{
for|for
control|(
name|Pattern
name|pattern
range|:
name|regexFilters
operator|.
name|keySet
argument_list|()
control|)
block|{
name|input
operator|=
name|pattern
operator|.
name|matcher
argument_list|(
name|input
argument_list|)
operator|.
name|replaceAll
argument_list|(
name|regexFilters
operator|.
name|get
argument_list|(
name|pattern
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|input
return|;
block|}
block|}
comment|// These are the filters which are common for every QTest.
comment|// Check specificFilterSet for QTest specific ones.
specifier|private
specifier|static
name|RegexFilterSet
name|getStaticFilterSet
parameter_list|()
block|{
comment|// Extract the leading four digits from the unix time value.
comment|// Use this as a prefix in order to increase the selectivity
comment|// of the unix time stamp replacement regex.
name|String
name|currentTimePrefix
init|=
name|Long
operator|.
name|toString
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|String
name|userName
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
decl_stmt|;
name|String
name|timePattern
init|=
literal|"(Mon|Tue|Wed|Thu|Fri|Sat|Sun) "
operator|+
literal|"(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) "
operator|+
literal|"\\d{2} \\d{2}:\\d{2}:\\d{2} \\w+ 20\\d{2}"
decl_stmt|;
comment|// Pattern to remove the timestamp and other infrastructural info from the out file
name|String
name|logPattern
init|=
literal|"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2},\\d*\\s+\\S+\\s+\\["
operator|+
literal|".*\\]\\s+\\S+:\\s+"
decl_stmt|;
name|String
name|operatorPattern
init|=
literal|"\"(CONDITION|COPY|DEPENDENCY_COLLECTION|DDL"
operator|+
literal|"|EXPLAIN|FETCH|FIL|FS|FUNCTION|GBY|HASHTABLEDUMMY|HASTTABLESINK|JOIN"
operator|+
literal|"|LATERALVIEWFORWARD|LIM|LVJ|MAP|MAPJOIN|MAPRED|MAPREDLOCAL|MOVE|OP|RS"
operator|+
literal|"|SCR|SEL|STATS|TS|UDTF|UNION)_\\d+\""
decl_stmt|;
return|return
operator|new
name|RegexFilterSet
argument_list|()
operator|.
name|addFilter
argument_list|(
name|logPattern
argument_list|,
literal|""
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"(?s)\nWaiting to acquire compile lock:.*?Acquired the compile lock.\n"
argument_list|,
literal|"\nAcquired the compile lock.\n"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"Getting log thread is interrupted, since query is done!\n"
argument_list|,
literal|""
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"going to print operations logs\n"
argument_list|,
literal|""
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"printed operations logs\n"
argument_list|,
literal|""
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"\\(queryId=[^\\)]*\\)"
argument_list|,
literal|"queryId=(!!{queryId}!!)"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"file:/\\w\\S+"
argument_list|,
literal|"file:/!!ELIDED!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"pfile:/\\w\\S+"
argument_list|,
literal|"pfile:/!!ELIDED!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"hdfs:/\\w\\S+"
argument_list|,
literal|"hdfs:/!!ELIDED!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"last_modified_by=\\w+"
argument_list|,
literal|"last_modified_by=!!ELIDED!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|timePattern
argument_list|,
literal|"!!TIMESTAMP!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"(\\D)"
operator|+
name|currentTimePrefix
operator|+
literal|"\\d{6}(\\D)"
argument_list|,
literal|"$1!!UNIXTIME!!$2"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"(\\D)"
operator|+
name|currentTimePrefix
operator|+
literal|"\\d{9}(\\D)"
argument_list|,
literal|"$1!!UNIXTIMEMILLIS!!$2"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|userName
argument_list|,
literal|"!!{user.name}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|operatorPattern
argument_list|,
literal|"\"$1_!!ELIDED!!\""
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"Time taken: [0-9\\.]* seconds"
argument_list|,
literal|"Time taken: !!ELIDED!! seconds"
argument_list|)
return|;
block|}
comment|/**    * Builder to generate QFile objects. After initializing the builder it is possible the    * generate the next QFile object using it's name only.    */
specifier|public
specifier|static
class|class
name|QFileBuilder
block|{
specifier|private
name|File
name|queryDirectory
decl_stmt|;
specifier|private
name|File
name|logDirectory
decl_stmt|;
specifier|private
name|File
name|resultsDirectory
decl_stmt|;
specifier|private
name|String
name|scratchDirectoryString
decl_stmt|;
specifier|private
name|String
name|warehouseDirectoryString
decl_stmt|;
specifier|private
name|File
name|hiveRootDirectory
decl_stmt|;
specifier|public
name|QFileBuilder
parameter_list|()
block|{     }
specifier|public
name|QFileBuilder
name|setQueryDirectory
parameter_list|(
name|File
name|queryDirectory
parameter_list|)
block|{
name|this
operator|.
name|queryDirectory
operator|=
name|queryDirectory
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileBuilder
name|setLogDirectory
parameter_list|(
name|File
name|logDirectory
parameter_list|)
block|{
name|this
operator|.
name|logDirectory
operator|=
name|logDirectory
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileBuilder
name|setResultsDirectory
parameter_list|(
name|File
name|resultsDirectory
parameter_list|)
block|{
name|this
operator|.
name|resultsDirectory
operator|=
name|resultsDirectory
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileBuilder
name|setScratchDirectoryString
parameter_list|(
name|String
name|scratchDirectoryString
parameter_list|)
block|{
name|this
operator|.
name|scratchDirectoryString
operator|=
name|scratchDirectoryString
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileBuilder
name|setWarehouseDirectoryString
parameter_list|(
name|String
name|warehouseDirectoryString
parameter_list|)
block|{
name|this
operator|.
name|warehouseDirectoryString
operator|=
name|warehouseDirectoryString
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileBuilder
name|setHiveRootDirectory
parameter_list|(
name|File
name|hiveRootDirectory
parameter_list|)
block|{
name|this
operator|.
name|hiveRootDirectory
operator|=
name|hiveRootDirectory
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFile
name|getQFile
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|QFile
name|result
init|=
operator|new
name|QFile
argument_list|()
decl_stmt|;
name|result
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|result
operator|.
name|inputFile
operator|=
operator|new
name|File
argument_list|(
name|queryDirectory
argument_list|,
name|name
operator|+
literal|".q"
argument_list|)
expr_stmt|;
name|result
operator|.
name|rawOutputFile
operator|=
operator|new
name|File
argument_list|(
name|logDirectory
argument_list|,
name|name
operator|+
literal|".q.out.raw"
argument_list|)
expr_stmt|;
name|result
operator|.
name|outputFile
operator|=
operator|new
name|File
argument_list|(
name|logDirectory
argument_list|,
name|name
operator|+
literal|".q.out"
argument_list|)
expr_stmt|;
name|result
operator|.
name|expcetedOutputFile
operator|=
operator|new
name|File
argument_list|(
name|resultsDirectory
argument_list|,
name|name
operator|+
literal|".q.out"
argument_list|)
expr_stmt|;
name|result
operator|.
name|logFile
operator|=
operator|new
name|File
argument_list|(
name|logDirectory
argument_list|,
name|name
operator|+
literal|".q.beeline"
argument_list|)
expr_stmt|;
name|result
operator|.
name|infraLogFile
operator|=
operator|new
name|File
argument_list|(
name|logDirectory
argument_list|,
name|name
operator|+
literal|".q.out.infra"
argument_list|)
expr_stmt|;
comment|// These are the filters which are specific for the given QTest.
comment|// Check staticFilterSet for common filters.
name|result
operator|.
name|specificFilterSet
operator|=
operator|new
name|RegexFilterSet
argument_list|()
operator|.
name|addFilter
argument_list|(
name|scratchDirectoryString
operator|+
literal|"[\\w\\-/]+"
argument_list|,
literal|"!!{hive.exec.scratchdir}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|warehouseDirectoryString
argument_list|,
literal|"!!{hive.metastore.warehouse.dir}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|resultsDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
literal|"!!{expectedDirectory}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|logDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
literal|"!!{outputDirectory}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|queryDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
literal|"!!{qFileDirectory}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|hiveRootDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
literal|"!!{hive.root}!!"
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

