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
name|util
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
name|commons
operator|.
name|lang
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|BeeLine
import|;
end_import

begin_comment
comment|/**  * QTestClient.  *  */
end_comment

begin_class
specifier|public
class|class
name|QFileClient
block|{
specifier|private
name|String
name|username
decl_stmt|;
specifier|private
name|String
name|password
decl_stmt|;
specifier|private
name|String
name|jdbcUrl
decl_stmt|;
specifier|private
name|String
name|jdbcDriver
decl_stmt|;
specifier|private
specifier|final
name|File
name|hiveRootDirectory
decl_stmt|;
specifier|private
name|File
name|qFileDirectory
decl_stmt|;
specifier|private
name|File
name|outputDirectory
decl_stmt|;
specifier|private
name|File
name|expectedDirectory
decl_stmt|;
specifier|private
specifier|final
name|File
name|scratchDirectory
decl_stmt|;
specifier|private
specifier|final
name|File
name|warehouseDirectory
decl_stmt|;
specifier|private
specifier|final
name|File
name|initScript
decl_stmt|;
specifier|private
specifier|final
name|File
name|cleanupScript
decl_stmt|;
specifier|private
name|File
name|testDataDirectory
decl_stmt|;
specifier|private
name|File
name|testScriptDirectory
decl_stmt|;
specifier|private
name|String
name|qFileName
decl_stmt|;
specifier|private
name|String
name|testname
decl_stmt|;
specifier|private
name|File
name|qFile
decl_stmt|;
specifier|private
name|File
name|outputFile
decl_stmt|;
specifier|private
name|File
name|expectedFile
decl_stmt|;
specifier|private
name|PrintStream
name|beelineOutputStream
decl_stmt|;
specifier|private
name|BeeLine
name|beeLine
decl_stmt|;
specifier|private
name|RegexFilterSet
name|filterSet
decl_stmt|;
specifier|private
name|boolean
name|hasErrors
init|=
literal|false
decl_stmt|;
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
name|QFileClient
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|QFileClient
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|hiveRootDirectory
parameter_list|,
name|String
name|qFileDirectory
parameter_list|,
name|String
name|outputDirectory
parameter_list|,
name|String
name|expectedDirectory
parameter_list|,
name|String
name|initScript
parameter_list|,
name|String
name|cleanupScript
parameter_list|)
block|{
name|this
operator|.
name|hiveRootDirectory
operator|=
operator|new
name|File
argument_list|(
name|hiveRootDirectory
argument_list|)
expr_stmt|;
name|this
operator|.
name|qFileDirectory
operator|=
operator|new
name|File
argument_list|(
name|qFileDirectory
argument_list|)
expr_stmt|;
name|this
operator|.
name|outputDirectory
operator|=
operator|new
name|File
argument_list|(
name|outputDirectory
argument_list|)
expr_stmt|;
name|this
operator|.
name|expectedDirectory
operator|=
operator|new
name|File
argument_list|(
name|expectedDirectory
argument_list|)
expr_stmt|;
name|this
operator|.
name|initScript
operator|=
operator|new
name|File
argument_list|(
name|initScript
argument_list|)
expr_stmt|;
name|this
operator|.
name|cleanupScript
operator|=
operator|new
name|File
argument_list|(
name|cleanupScript
argument_list|)
expr_stmt|;
name|this
operator|.
name|scratchDirectory
operator|=
operator|new
name|File
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|SCRATCHDIR
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|warehouseDirectory
operator|=
operator|new
name|File
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
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
name|void
name|initFilterSet
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
name|unixTimePattern
init|=
literal|"\\D"
operator|+
name|currentTimePrefix
operator|+
literal|"\\d{6}\\D"
decl_stmt|;
name|String
name|unixTimeMillisPattern
init|=
literal|"\\D"
operator|+
name|currentTimePrefix
operator|+
literal|"\\d{9}\\D"
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
name|filterSet
operator|=
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
literal|"Getting log thread is interrupted, since query is done!\n"
argument_list|,
literal|""
argument_list|)
operator|.
name|addFilter
argument_list|(
name|scratchDirectory
operator|.
name|toString
argument_list|()
operator|+
literal|"[\\w\\-/]+"
argument_list|,
literal|"!!{hive.exec.scratchdir}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|warehouseDirectory
operator|.
name|toString
argument_list|()
argument_list|,
literal|"!!{hive.metastore.warehouse.dir}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|expectedDirectory
operator|.
name|toString
argument_list|()
argument_list|,
literal|"!!{expectedDirectory}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|outputDirectory
operator|.
name|toString
argument_list|()
argument_list|,
literal|"!!{outputDirectory}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|qFileDirectory
operator|.
name|toString
argument_list|()
argument_list|,
literal|"!!{qFileDirectory}!!"
argument_list|)
operator|.
name|addFilter
argument_list|(
name|hiveRootDirectory
operator|.
name|toString
argument_list|()
argument_list|,
literal|"!!{hive.root}!!"
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
expr_stmt|;
block|}
empty_stmt|;
specifier|public
name|QFileClient
name|setUsername
parameter_list|(
name|String
name|username
parameter_list|)
block|{
name|this
operator|.
name|username
operator|=
name|username
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClient
name|setPassword
parameter_list|(
name|String
name|password
parameter_list|)
block|{
name|this
operator|.
name|password
operator|=
name|password
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClient
name|setJdbcUrl
parameter_list|(
name|String
name|jdbcUrl
parameter_list|)
block|{
name|this
operator|.
name|jdbcUrl
operator|=
name|jdbcUrl
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClient
name|setJdbcDriver
parameter_list|(
name|String
name|jdbcDriver
parameter_list|)
block|{
name|this
operator|.
name|jdbcDriver
operator|=
name|jdbcDriver
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClient
name|setQFileName
parameter_list|(
name|String
name|qFileName
parameter_list|)
block|{
name|this
operator|.
name|qFileName
operator|=
name|qFileName
expr_stmt|;
name|this
operator|.
name|qFile
operator|=
operator|new
name|File
argument_list|(
name|qFileDirectory
argument_list|,
name|qFileName
argument_list|)
expr_stmt|;
name|this
operator|.
name|testname
operator|=
name|StringUtils
operator|.
name|substringBefore
argument_list|(
name|qFileName
argument_list|,
literal|"."
argument_list|)
expr_stmt|;
name|expectedFile
operator|=
operator|new
name|File
argument_list|(
name|expectedDirectory
argument_list|,
name|qFileName
operator|+
literal|".out"
argument_list|)
expr_stmt|;
name|outputFile
operator|=
operator|new
name|File
argument_list|(
name|outputDirectory
argument_list|,
name|qFileName
operator|+
literal|".out"
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClient
name|setQFileDirectory
parameter_list|(
name|String
name|qFileDirectory
parameter_list|)
block|{
name|this
operator|.
name|qFileDirectory
operator|=
operator|new
name|File
argument_list|(
name|qFileDirectory
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClient
name|setOutputDirectory
parameter_list|(
name|String
name|outputDirectory
parameter_list|)
block|{
name|this
operator|.
name|outputDirectory
operator|=
operator|new
name|File
argument_list|(
name|outputDirectory
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClient
name|setExpectedDirectory
parameter_list|(
name|String
name|expectedDirectory
parameter_list|)
block|{
name|this
operator|.
name|expectedDirectory
operator|=
operator|new
name|File
argument_list|(
name|expectedDirectory
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClient
name|setTestDataDirectory
parameter_list|(
name|String
name|testDataDirectory
parameter_list|)
block|{
name|this
operator|.
name|testDataDirectory
operator|=
operator|new
name|File
argument_list|(
name|testDataDirectory
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClient
name|setTestScriptDirectory
parameter_list|(
name|String
name|testScriptDirectory
parameter_list|)
block|{
name|this
operator|.
name|testScriptDirectory
operator|=
operator|new
name|File
argument_list|(
name|testScriptDirectory
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|boolean
name|hasErrors
parameter_list|()
block|{
return|return
name|hasErrors
return|;
block|}
specifier|private
name|void
name|initBeeLine
parameter_list|()
throws|throws
name|Exception
block|{
name|beeLine
operator|=
operator|new
name|BeeLine
argument_list|()
expr_stmt|;
name|beelineOutputStream
operator|=
operator|new
name|PrintStream
argument_list|(
operator|new
name|File
argument_list|(
name|outputDirectory
argument_list|,
name|qFileName
operator|+
literal|".beeline"
argument_list|)
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|setOutputStream
argument_list|(
name|beelineOutputStream
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|setErrorStream
argument_list|(
name|beelineOutputStream
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!set verbose true"
block|,
literal|"!set shownestederrs true"
block|,
literal|"!set showwarnings true"
block|,
literal|"!set showelapsedtime false"
block|,
literal|"!set maxwidth -1"
block|,
literal|"!connect "
operator|+
name|jdbcUrl
operator|+
literal|" "
operator|+
name|username
operator|+
literal|" "
operator|+
name|password
operator|+
literal|" "
operator|+
name|jdbcDriver
block|,     }
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setUp
parameter_list|()
block|{
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"USE default;"
block|,
literal|"SHOW TABLES;"
block|,
literal|"DROP DATABASE IF EXISTS `"
operator|+
name|testname
operator|+
literal|"` CASCADE;"
block|,
literal|"CREATE DATABASE `"
operator|+
name|testname
operator|+
literal|"`;"
block|,
literal|"USE `"
operator|+
name|testname
operator|+
literal|"`;"
block|,
literal|"set test.data.dir="
operator|+
name|testDataDirectory
operator|+
literal|";"
block|,
literal|"set test.script.dir="
operator|+
name|testScriptDirectory
operator|+
literal|";"
block|,
literal|"!run "
operator|+
name|testScriptDirectory
operator|+
literal|"/"
operator|+
name|initScript
block|,     }
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tearDown
parameter_list|()
block|{
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!set outputformat table"
block|,
literal|"USE default;"
block|,
literal|"DROP DATABASE IF EXISTS `"
operator|+
name|testname
operator|+
literal|"` CASCADE;"
block|,
literal|"!run "
operator|+
name|testScriptDirectory
operator|+
literal|"/"
operator|+
name|cleanupScript
block|,     }
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runQFileTest
parameter_list|()
throws|throws
name|Exception
block|{
name|hasErrors
operator|=
literal|false
expr_stmt|;
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!set outputformat csv"
block|,
literal|"!record "
operator|+
name|outputDirectory
operator|+
literal|"/"
operator|+
name|qFileName
operator|+
literal|".raw"
block|,       }
argument_list|)
expr_stmt|;
if|if
condition|(
literal|1
operator|!=
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!run "
operator|+
name|qFileDirectory
operator|+
literal|"/"
operator|+
name|qFileName
block|}
block|)
block|)
block|{
name|hasErrors
operator|=
literal|true
expr_stmt|;
block|}
end_class

begin_expr_stmt
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!record"
block|}
argument_list|)
expr_stmt|;
end_expr_stmt

begin_function
unit|}     private
name|void
name|filterResults
parameter_list|()
throws|throws
name|IOException
block|{
name|initFilterSet
argument_list|()
expr_stmt|;
name|String
name|rawOutput
init|=
name|FileUtils
operator|.
name|readFileToString
argument_list|(
operator|new
name|File
argument_list|(
name|outputDirectory
argument_list|,
name|qFileName
operator|+
literal|".raw"
argument_list|)
argument_list|)
decl_stmt|;
name|FileUtils
operator|.
name|writeStringToFile
argument_list|(
name|outputFile
argument_list|,
name|filterSet
operator|.
name|filter
argument_list|(
name|rawOutput
argument_list|)
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
specifier|public
name|void
name|cleanup
parameter_list|()
block|{
if|if
condition|(
name|beeLine
operator|!=
literal|null
condition|)
block|{
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!quit"
block|}
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|beelineOutputStream
operator|!=
literal|null
condition|)
block|{
name|beelineOutputStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|hasErrors
condition|)
block|{
name|String
name|oldFileName
init|=
name|outputDirectory
operator|+
literal|"/"
operator|+
name|qFileName
operator|+
literal|".raw"
decl_stmt|;
name|String
name|newFileName
init|=
name|oldFileName
operator|+
literal|".error"
decl_stmt|;
try|try
block|{
name|FileUtils
operator|.
name|moveFile
argument_list|(
operator|new
name|File
argument_list|(
name|oldFileName
argument_list|)
argument_list|,
operator|new
name|File
argument_list|(
name|newFileName
argument_list|)
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
name|out
operator|.
name|println
argument_list|(
literal|"Failed to move '"
operator|+
name|oldFileName
operator|+
literal|"' to '"
operator|+
name|newFileName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_function

begin_function
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|initBeeLine
argument_list|()
expr_stmt|;
name|setUp
argument_list|()
expr_stmt|;
name|runQFileTest
argument_list|()
expr_stmt|;
name|tearDown
argument_list|()
expr_stmt|;
name|filterResults
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|cleanup
argument_list|()
expr_stmt|;
block|}
block|}
end_function

begin_comment
comment|/**    * Does the test have a file with expected results to compare the log against.    * False probably indicates that this is a new test and the caller should    * copy the log to the expected results directory.    * @return    */
end_comment

begin_function
specifier|public
name|boolean
name|hasExpectedResults
parameter_list|()
block|{
return|return
name|expectedFile
operator|.
name|exists
argument_list|()
return|;
block|}
end_function

begin_function
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
name|expectedFile
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
name|expectedFile
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
end_function

begin_function
specifier|private
name|boolean
name|executeDiff
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|ArrayList
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
name|expectedFile
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
end_function

begin_function
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
end_function

begin_function
specifier|public
name|void
name|overwriteResults
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|expectedFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtils
operator|.
name|forceDelete
argument_list|(
name|expectedFile
argument_list|)
expr_stmt|;
block|}
name|FileUtils
operator|.
name|copyFileToDirectory
argument_list|(
name|outputFile
argument_list|,
name|expectedDirectory
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to overwrite results!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
end_function

unit|}
end_unit

