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
name|hive
operator|.
name|beeline
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
name|hive
operator|.
name|ql
operator|.
name|QTestProcessExecResult
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
name|dataset
operator|.
name|QTestDatasetHandler
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
name|apache
operator|.
name|hive
operator|.
name|beeline
operator|.
name|ConvertedOutputFile
operator|.
name|Converter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|Arrays
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|Set
argument_list|<
name|String
argument_list|>
name|srcTables
init|=
name|QTestDatasetHandler
operator|.
name|getSrcTables
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEBUG_HINT
init|=
literal|"The following files can help you identifying the problem:%n"
operator|+
literal|" - Query file: %1s%n"
operator|+
literal|" - Raw output file: %2s%n"
operator|+
literal|" - Filtered output file: %3s%n"
operator|+
literal|" - Expected output file: %4s%n"
operator|+
literal|" - Client log file: %5s%n"
operator|+
literal|" - Client log files before the test: %6s%n"
operator|+
literal|" - Client log files after the test: %7s%n"
operator|+
literal|" - Hiveserver2 log file: %8s%n"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|USE_COMMAND_WARNING
init|=
literal|"The query file %1s contains \"%2s\" command.%n"
operator|+
literal|"The source table name rewrite is turned on, so this might cause problems when the used "
operator|+
literal|"database contains tables named any of the following: "
operator|+
name|srcTables
operator|+
literal|"%n"
operator|+
literal|"To turn off the table name rewrite use -Dtest.rewrite.source.tables=false%n"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|USE_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^\\s*use\\s.*"
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|ENTITYLIST_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"(((PREHOOK|POSTHOOK): (Input|Output): \\S+\n)+)"
argument_list|,
name|Pattern
operator|.
name|MULTILINE
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MASK_PATTERN
init|=
literal|"#### A masked pattern was here ####\n"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|COMMANDS_TO_REMOVE
init|=
block|{
literal|"EXPLAIN"
block|,
literal|"DESC(RIBE)?[\\s\\n]+EXTENDED"
block|,
literal|"DESC(RIBE)?[\\s\\n]+FORMATTED"
block|,
literal|"DESC(RIBE)?"
block|,
literal|"SHOW[\\s\\n]+TABLES"
block|,
literal|"SHOW[\\s\\n]+FORMATTED[\\s\\n]+INDEXES"
block|,
literal|"SHOW[\\s\\n]+DATABASES"
block|}
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|databaseName
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
name|expectedOutputFile
decl_stmt|;
specifier|private
name|File
name|logFile
decl_stmt|;
specifier|private
name|File
name|beforeExecuteLogFile
decl_stmt|;
specifier|private
name|File
name|afterExecuteLogFile
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
specifier|static
name|RegexFilterSet
name|portableFilterSet
init|=
name|getPortableFilterSet
argument_list|()
decl_stmt|;
specifier|private
name|RegexFilterSet
name|specificFilterSet
decl_stmt|;
specifier|private
name|boolean
name|useSharedDatabase
decl_stmt|;
specifier|private
name|Converter
name|converter
decl_stmt|;
specifier|private
name|boolean
name|comparePortable
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
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|databaseName
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
name|expectedOutputFile
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
name|getBeforeExecuteLogFile
parameter_list|()
block|{
return|return
name|beforeExecuteLogFile
return|;
block|}
specifier|public
name|File
name|getAfterExecuteLogFile
parameter_list|()
block|{
return|return
name|afterExecuteLogFile
return|;
block|}
specifier|public
name|Converter
name|getConverter
parameter_list|()
block|{
return|return
name|converter
return|;
block|}
specifier|public
name|boolean
name|isUseSharedDatabase
parameter_list|()
block|{
return|return
name|useSharedDatabase
return|;
block|}
specifier|public
name|String
name|getDebugHint
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
name|DEBUG_HINT
argument_list|,
name|inputFile
argument_list|,
name|rawOutputFile
argument_list|,
name|outputFile
argument_list|,
name|expectedOutputFile
argument_list|,
name|logFile
argument_list|,
name|beforeExecuteLogFile
argument_list|,
name|afterExecuteLogFile
argument_list|,
literal|"./itests/qtest/target/tmp/log/hive.log"
argument_list|)
return|;
block|}
comment|/**    * Filters the sql commands if necessary - eg. not using the shared database.    * @param commands The array of the sql commands before filtering    * @return The filtered array of the sql command strings    * @throws IOException File read error    */
specifier|public
name|String
index|[]
name|filterCommands
parameter_list|(
name|String
index|[]
name|commands
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|useSharedDatabase
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
name|commands
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|USE_PATTERN
operator|.
name|matcher
argument_list|(
name|commands
index|[
name|i
index|]
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|USE_COMMAND_WARNING
argument_list|,
name|inputFile
argument_list|,
name|commands
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|commands
index|[
name|i
index|]
operator|=
name|replaceTableNames
argument_list|(
name|commands
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|commands
return|;
block|}
comment|/**    * Replace the default src database TABLE_NAMEs in the queries with default.TABLE_NAME, like    * src->default.src, srcpart->default.srcpart, so the queries could be run even if the used    * database is query specific. This change is only a best effort, since we do not want to parse    * the queries, we could not be sure that we do not replace other strings which are not    * tablenames. Like 'select src from othertable;'. The q files containing these commands should    * be excluded. Only replace the tablenames, if rewriteSourceTables is set.    * @param source The original query string    * @return The query string where the tablenames are replaced    */
specifier|private
name|String
name|replaceTableNames
parameter_list|(
name|String
name|source
parameter_list|)
block|{
for|for
control|(
name|String
name|table
range|:
name|srcTables
control|)
block|{
name|source
operator|=
name|source
operator|.
name|replaceAll
argument_list|(
literal|"(?is)(\\s+)("
operator|+
name|table
operator|+
literal|")([\\s;\\n\\),])"
argument_list|,
literal|"$1default.$2$3"
argument_list|)
expr_stmt|;
block|}
return|return
name|source
return|;
block|}
comment|/**    * The result contains the original queries. To revert them to the original form remove the    * 'default' from every default.TABLE_NAME, like default.src->src, default.srcpart->srcpart.    * @param source The original query output    * @return The query output where the tablenames are replaced    */
specifier|private
name|String
name|revertReplaceTableNames
parameter_list|(
name|String
name|source
parameter_list|)
block|{
for|for
control|(
name|String
name|table
range|:
name|srcTables
control|)
block|{
name|source
operator|=
name|source
operator|.
name|replaceAll
argument_list|(
literal|"(?is)(?<!name:?|alias:?)(\\s+)default\\.("
operator|+
name|table
operator|+
literal|")([\\s;\\n\\),])"
argument_list|,
literal|"$1$2$3"
argument_list|)
expr_stmt|;
block|}
return|return
name|source
return|;
block|}
comment|/**    * The PREHOOK/POSTHOOK Input/Output lists should be sorted again after reverting the database    * name in those strings to match the original Cli output.    * @param source The original query output    * @return The query output where the input/output list are alphabetically ordered    */
specifier|private
name|String
name|sortInputOutput
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|Matcher
name|matcher
init|=
name|ENTITYLIST_PATTERN
operator|.
name|matcher
argument_list|(
name|source
argument_list|)
decl_stmt|;
while|while
condition|(
name|matcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|lines
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|lines
argument_list|)
expr_stmt|;
name|source
operator|=
name|source
operator|.
name|replaceAll
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|,
name|String
operator|.
name|join
argument_list|(
literal|"\n"
argument_list|,
name|lines
argument_list|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|source
return|;
block|}
comment|/**    * Filters the generated output file    * @throws IOException    */
specifier|public
name|void
name|filterOutput
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|output
init|=
name|FileUtils
operator|.
name|readFileToString
argument_list|(
name|rawOutputFile
argument_list|,
literal|"UTF-8"
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparePortable
condition|)
block|{
name|output
operator|=
name|portableFilterSet
operator|.
name|filter
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
name|output
operator|=
name|staticFilterSet
operator|.
name|filter
argument_list|(
name|specificFilterSet
operator|.
name|filter
argument_list|(
name|output
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|useSharedDatabase
condition|)
block|{
name|output
operator|=
name|sortInputOutput
argument_list|(
name|revertReplaceTableNames
argument_list|(
name|output
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|FileUtils
operator|.
name|writeStringToFile
argument_list|(
name|outputFile
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
comment|/**    * Compare the filtered file with the expected golden file    * @return The comparison data    * @throws IOException If there is a problem accessing the golden or generated file    * @throws InterruptedException If there is a problem running the diff command    */
specifier|public
name|QTestProcessExecResult
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
name|expectedOutputFile
operator|.
name|exists
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expected results file does not exist: "
operator|+
name|expectedOutputFile
argument_list|)
throw|;
block|}
return|return
name|executeDiff
argument_list|()
return|;
block|}
comment|/**    * Overwrite the golden file with the generated output    * @throws IOException If there is a problem accessing the golden or generated file    */
specifier|public
name|void
name|overwriteResults
parameter_list|()
throws|throws
name|IOException
block|{
name|FileUtils
operator|.
name|copyFile
argument_list|(
name|outputFile
argument_list|,
name|expectedOutputFile
argument_list|)
expr_stmt|;
block|}
specifier|private
name|QTestProcessExecResult
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
name|expectedOutputFile
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
name|lang3
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
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|out
init|=
operator|new
name|PrintStream
argument_list|(
name|bos
argument_list|,
literal|true
argument_list|,
literal|"UTF-8"
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
argument_list|,
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
name|QTestProcessExecResult
operator|.
name|create
argument_list|(
name|result
argument_list|,
operator|new
name|String
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
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
name|Filter
block|{
specifier|private
specifier|final
name|Pattern
name|pattern
decl_stmt|;
specifier|private
specifier|final
name|String
name|replacement
decl_stmt|;
specifier|public
name|Filter
parameter_list|(
name|Pattern
name|pattern
parameter_list|,
name|String
name|replacement
parameter_list|)
block|{
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
name|this
operator|.
name|replacement
operator|=
name|replacement
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|RegexFilterSet
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|Filter
argument_list|>
name|regexFilters
init|=
operator|new
name|ArrayList
argument_list|<
name|Filter
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
name|add
argument_list|(
operator|new
name|Filter
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
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegexFilterSet
name|addFilter
parameter_list|(
name|String
name|regex
parameter_list|,
name|int
name|flags
parameter_list|,
name|String
name|replacement
parameter_list|)
block|{
name|regexFilters
operator|.
name|add
argument_list|(
operator|new
name|Filter
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|regex
argument_list|,
name|flags
argument_list|)
argument_list|,
name|replacement
argument_list|)
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
name|Filter
name|filter
range|:
name|regexFilters
control|)
block|{
name|input
operator|=
name|filter
operator|.
name|pattern
operator|.
name|matcher
argument_list|(
name|input
argument_list|)
operator|.
name|replaceAll
argument_list|(
name|filter
operator|.
name|replacement
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
comment|// Pattern to remove the timestamp and other infrastructural info from the out file
return|return
operator|new
name|RegexFilterSet
argument_list|()
operator|.
name|addFilter
argument_list|(
literal|"Reading log file: .*\n"
argument_list|,
literal|""
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"INFO  : "
argument_list|,
literal|""
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*/tmp/.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*file:.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*file\\..*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*Location.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*LOCATION '.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*Output:.*/data/files/.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*CreateTime.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*last_modified_.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*transient_lastDdlTime.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*lastUpdateTime.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*lastAccessTime.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|".*[Oo]wner.*\n"
argument_list|,
name|MASK_PATTERN
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"(?s)("
operator|+
name|MASK_PATTERN
operator|+
literal|")+"
argument_list|,
name|MASK_PATTERN
argument_list|)
return|;
block|}
comment|/**    * If the test.beeline.compare.portable system property is true,    * the commands, listed in the COMMANDS_TO_REMOVE array will be removed    * from the out files before comparison.    * @return The regex filters to apply to remove the commands from the out files.    */
specifier|private
specifier|static
name|RegexFilterSet
name|getPortableFilterSet
parameter_list|()
block|{
name|RegexFilterSet
name|filterSet
init|=
operator|new
name|RegexFilterSet
argument_list|()
decl_stmt|;
name|String
name|regex
init|=
literal|"PREHOOK: query:\\s+%s[\\n\\s]+.*?(?=(PREHOOK: query:|$))"
decl_stmt|;
for|for
control|(
name|String
name|command
range|:
name|COMMANDS_TO_REMOVE
control|)
block|{
name|filterSet
operator|.
name|addFilter
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|regex
argument_list|,
name|command
argument_list|)
argument_list|,
name|Pattern
operator|.
name|DOTALL
operator||
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
name|filterSet
operator|.
name|addFilter
argument_list|(
literal|"(Warning: )(.* Join .*JOIN\\[\\d+\\].*)( is a cross product)"
argument_list|,
literal|"$1MASKED$3"
argument_list|)
expr_stmt|;
name|filterSet
operator|.
name|addFilter
argument_list|(
literal|"mapreduce.jobtracker.address=.*\n"
argument_list|,
literal|"mapreduce.jobtracker.address=MASKED\n"
argument_list|)
expr_stmt|;
return|return
name|filterSet
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
name|boolean
name|useSharedDatabase
decl_stmt|;
specifier|private
name|boolean
name|comparePortable
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
name|setUseSharedDatabase
parameter_list|(
name|boolean
name|useSharedDatabase
parameter_list|)
block|{
name|this
operator|.
name|useSharedDatabase
operator|=
name|useSharedDatabase
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileBuilder
name|setComparePortable
parameter_list|(
name|boolean
name|compareProtable
parameter_list|)
block|{
name|this
operator|.
name|comparePortable
operator|=
name|compareProtable
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
if|if
condition|(
operator|!
name|useSharedDatabase
condition|)
block|{
name|result
operator|.
name|databaseName
operator|=
literal|"test_db_"
operator|+
name|name
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
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
literal|"(PREHOOK|POSTHOOK): (Output|Input): database:"
operator|+
name|result
operator|.
name|databaseName
operator|+
literal|"\n"
argument_list|,
literal|"$1: $2: database:default\n"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"(PREHOOK|POSTHOOK): (Output|Input): "
operator|+
name|result
operator|.
name|databaseName
operator|+
literal|"@"
argument_list|,
literal|"$1: $2: default@"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"name(:?) "
operator|+
name|result
operator|.
name|databaseName
operator|+
literal|"\\.(.*)\n"
argument_list|,
literal|"name$1 default.$2\n"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"alias(:?) "
operator|+
name|result
operator|.
name|databaseName
operator|+
literal|"\\.(.*)\n"
argument_list|,
literal|"alias$1 default.$2\n"
argument_list|)
operator|.
name|addFilter
argument_list|(
literal|"/"
operator|+
name|result
operator|.
name|databaseName
operator|+
literal|".db/"
argument_list|,
literal|"/"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|databaseName
operator|=
literal|"default"
expr_stmt|;
name|result
operator|.
name|specificFilterSet
operator|=
operator|new
name|RegexFilterSet
argument_list|()
expr_stmt|;
block|}
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
name|beforeExecuteLogFile
operator|=
operator|new
name|File
argument_list|(
name|logDirectory
argument_list|,
name|name
operator|+
literal|".q.beforeExecute.log"
argument_list|)
expr_stmt|;
name|result
operator|.
name|afterExecuteLogFile
operator|=
operator|new
name|File
argument_list|(
name|logDirectory
argument_list|,
name|name
operator|+
literal|".q.afterExecute.log"
argument_list|)
expr_stmt|;
name|result
operator|.
name|useSharedDatabase
operator|=
name|useSharedDatabase
expr_stmt|;
name|result
operator|.
name|converter
operator|=
name|Converter
operator|.
name|NONE
expr_stmt|;
name|String
name|input
init|=
name|FileUtils
operator|.
name|readFileToString
argument_list|(
name|result
operator|.
name|inputFile
argument_list|,
literal|"UTF-8"
argument_list|)
decl_stmt|;
if|if
condition|(
name|input
operator|.
name|contains
argument_list|(
literal|"-- SORT_QUERY_RESULTS"
argument_list|)
condition|)
block|{
name|result
operator|.
name|converter
operator|=
name|Converter
operator|.
name|SORT_QUERY_RESULTS
expr_stmt|;
block|}
if|if
condition|(
name|input
operator|.
name|contains
argument_list|(
literal|"-- HASH_QUERY_RESULTS"
argument_list|)
condition|)
block|{
name|result
operator|.
name|converter
operator|=
name|Converter
operator|.
name|HASH_QUERY_RESULTS
expr_stmt|;
block|}
if|if
condition|(
name|input
operator|.
name|contains
argument_list|(
literal|"-- SORT_AND_HASH_QUERY_RESULTS"
argument_list|)
condition|)
block|{
name|result
operator|.
name|converter
operator|=
name|Converter
operator|.
name|SORT_AND_HASH_QUERY_RESULTS
expr_stmt|;
block|}
name|result
operator|.
name|comparePortable
operator|=
name|comparePortable
expr_stmt|;
name|result
operator|.
name|expectedOutputFile
operator|=
name|prepareExpectedOutputFile
argument_list|(
name|name
argument_list|,
name|comparePortable
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**      * Prepare the output file and apply the necessary filters on it.      * @param name      * @param comparePortable If this parameter is true, the commands, listed in the      * COMMANDS_TO_REMOVE array will be filtered out in the output file.      * @return The expected output file.      * @throws IOException      */
specifier|private
name|File
name|prepareExpectedOutputFile
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|comparePortable
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|comparePortable
condition|)
block|{
return|return
operator|new
name|File
argument_list|(
name|resultsDirectory
argument_list|,
name|name
operator|+
literal|".q.out"
argument_list|)
return|;
block|}
else|else
block|{
name|File
name|rawExpectedOutputFile
init|=
operator|new
name|File
argument_list|(
name|resultsDirectory
argument_list|,
name|name
operator|+
literal|".q.out"
argument_list|)
decl_stmt|;
name|String
name|rawOutput
init|=
name|FileUtils
operator|.
name|readFileToString
argument_list|(
name|rawExpectedOutputFile
argument_list|,
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|rawOutput
operator|=
name|portableFilterSet
operator|.
name|filter
argument_list|(
name|rawOutput
argument_list|)
expr_stmt|;
name|File
name|expectedOutputFile
init|=
operator|new
name|File
argument_list|(
name|logDirectory
argument_list|,
name|name
operator|+
literal|".q.out.portable"
argument_list|)
decl_stmt|;
name|FileUtils
operator|.
name|writeStringToFile
argument_list|(
name|expectedOutputFile
argument_list|,
name|rawOutput
argument_list|)
expr_stmt|;
return|return
name|expectedOutputFile
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

