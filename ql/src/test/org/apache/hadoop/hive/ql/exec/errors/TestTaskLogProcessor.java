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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|errors
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
name|EOFException
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
name|FileOutputStream
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
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
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
name|LinkedList
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestTaskLogProcessor
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|File
argument_list|>
name|toBeDeletedList
init|=
operator|new
name|LinkedList
argument_list|<
name|File
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
block|{
for|for
control|(
name|File
name|f
range|:
name|toBeDeletedList
control|)
block|{
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
name|toBeDeletedList
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|private
name|File
name|writeTestLog
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|content
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Put the script content in a temp file
name|File
name|scriptFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"-"
operator|+
name|id
operator|+
literal|"-"
argument_list|,
literal|".log"
argument_list|)
decl_stmt|;
name|scriptFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|toBeDeletedList
operator|.
name|add
argument_list|(
name|scriptFile
argument_list|)
expr_stmt|;
name|PrintStream
name|os
init|=
operator|new
name|PrintStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|scriptFile
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|os
operator|.
name|print
argument_list|(
name|content
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|scriptFile
return|;
block|}
specifier|private
name|String
name|toString
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|StringWriter
name|sw
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|PrintWriter
name|pw
init|=
operator|new
name|PrintWriter
argument_list|(
name|sw
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|t
operator|.
name|printStackTrace
argument_list|(
name|pw
argument_list|)
expr_stmt|;
name|pw
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|sw
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/*    * returns number of lines in the printed throwable stack trace.    */
specifier|private
name|String
name|writeThrowableAsFile
parameter_list|(
name|String
name|before
parameter_list|,
name|Throwable
name|t
parameter_list|,
name|String
name|after
parameter_list|,
name|String
name|fileSuffix
parameter_list|,
name|TaskLogProcessor
name|taskLogProcessor
parameter_list|)
throws|throws
name|IOException
block|{
comment|// compose file text:
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|before
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|before
argument_list|)
expr_stmt|;
block|}
specifier|final
name|String
name|stackTraceStr
init|=
name|toString
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|stackTraceStr
argument_list|)
expr_stmt|;
if|if
condition|(
name|after
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|after
argument_list|)
expr_stmt|;
block|}
comment|// write it to file:
name|File
name|file
init|=
name|writeTestLog
argument_list|(
name|fileSuffix
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// add it to the log processor:
name|taskLogProcessor
operator|.
name|addTaskAttemptLogUrl
argument_list|(
name|file
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|stackTraceStr
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetStackTraces
parameter_list|()
throws|throws
name|Exception
block|{
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|HiveConf
operator|.
name|setQueryString
argument_list|(
name|jobConf
argument_list|,
literal|"select * from foo group by moo;"
argument_list|)
expr_stmt|;
specifier|final
name|TaskLogProcessor
name|taskLogProcessor
init|=
operator|new
name|TaskLogProcessor
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|Throwable
name|oome
init|=
operator|new
name|OutOfMemoryError
argument_list|(
literal|"java heap space"
argument_list|)
decl_stmt|;
name|String
name|oomeStr
init|=
name|writeThrowableAsFile
argument_list|(
literal|"Some line in the beginning\n"
argument_list|,
name|oome
argument_list|,
literal|null
argument_list|,
literal|"1"
argument_list|,
name|taskLogProcessor
argument_list|)
decl_stmt|;
name|Throwable
name|compositeException
init|=
operator|new
name|InvocationTargetException
argument_list|(
operator|new
name|IOException
argument_list|(
operator|new
name|NullPointerException
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|compositeStr
init|=
name|writeThrowableAsFile
argument_list|(
literal|null
argument_list|,
name|compositeException
argument_list|,
literal|"Some line in the end.\n"
argument_list|,
literal|"2"
argument_list|,
name|taskLogProcessor
argument_list|)
decl_stmt|;
name|Throwable
name|eofe
init|=
operator|new
name|EOFException
argument_list|()
decl_stmt|;
name|String
name|eofeStr
init|=
name|writeThrowableAsFile
argument_list|(
literal|"line a\nlineb\n"
argument_list|,
name|eofe
argument_list|,
literal|" line c\nlineD\n"
argument_list|,
literal|"3"
argument_list|,
name|taskLogProcessor
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|stackTraces
init|=
name|taskLogProcessor
operator|.
name|getStackTraces
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|stackTraces
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Assert the actual stack traces are exactly equal to the written ones,
comment|// and are contained in "stackTraces" list in the submission order:
name|checkException
argument_list|(
name|oomeStr
argument_list|,
name|stackTraces
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|checkException
argument_list|(
name|compositeStr
argument_list|,
name|stackTraces
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|checkException
argument_list|(
name|eofeStr
argument_list|,
name|stackTraces
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkException
parameter_list|(
name|String
name|writenText
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|actualTrace
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|expectedLines
init|=
name|getLines
argument_list|(
name|writenText
argument_list|)
decl_stmt|;
name|String
name|expected
decl_stmt|,
name|actual
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
name|expectedLines
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|=
name|expectedLines
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|actual
operator|=
name|actualTrace
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getLines
parameter_list|(
name|String
name|text
parameter_list|)
throws|throws
name|IOException
block|{
name|BufferedReader
name|br
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|text
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|48
argument_list|)
decl_stmt|;
name|String
name|string
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|string
operator|=
name|br
operator|.
name|readLine
argument_list|()
expr_stmt|;
if|if
condition|(
name|string
operator|==
literal|null
condition|)
block|{
break|break;
block|}
else|else
block|{
name|list
operator|.
name|add
argument_list|(
name|string
argument_list|)
expr_stmt|;
block|}
block|}
name|br
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|list
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScriptErrorHeuristic
parameter_list|()
throws|throws
name|Exception
block|{
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|HiveConf
operator|.
name|setQueryString
argument_list|(
name|jobConf
argument_list|,
literal|"select * from foo group by moo;"
argument_list|)
expr_stmt|;
specifier|final
name|TaskLogProcessor
name|taskLogProcessor
init|=
operator|new
name|TaskLogProcessor
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|String
name|errorCode
init|=
literal|"7874"
decl_stmt|;
comment|// example code
name|String
name|content
init|=
literal|"line a\nlineb\n"
operator|+
literal|"Script failed with code "
operator|+
name|errorCode
operator|+
literal|" line c\nlineD\n"
decl_stmt|;
name|File
name|log3File
init|=
name|writeTestLog
argument_list|(
literal|"1"
argument_list|,
name|content
argument_list|)
decl_stmt|;
name|taskLogProcessor
operator|.
name|addTaskAttemptLogUrl
argument_list|(
name|log3File
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ErrorAndSolution
argument_list|>
name|errList
init|=
name|taskLogProcessor
operator|.
name|getErrors
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|errList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|ErrorAndSolution
name|eas
init|=
name|errList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|error
init|=
name|eas
operator|.
name|getError
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|error
argument_list|)
expr_stmt|;
comment|// check that the error code is present in the error description:
name|assertTrue
argument_list|(
name|error
operator|.
name|indexOf
argument_list|(
name|errorCode
argument_list|)
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|String
name|solution
init|=
name|eas
operator|.
name|getSolution
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|solution
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|solution
operator|.
name|length
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDataCorruptErrorHeuristic
parameter_list|()
throws|throws
name|Exception
block|{
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|HiveConf
operator|.
name|setQueryString
argument_list|(
name|jobConf
argument_list|,
literal|"select * from foo group by moo;"
argument_list|)
expr_stmt|;
specifier|final
name|TaskLogProcessor
name|taskLogProcessor
init|=
operator|new
name|TaskLogProcessor
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|String
name|badFile1
init|=
literal|"hdfs://localhost/foo1/moo1/zoo1"
decl_stmt|;
name|String
name|badFile2
init|=
literal|"hdfs://localhost/foo2/moo2/zoo2"
decl_stmt|;
name|String
name|content
init|=
literal|"line a\nlineb\n"
operator|+
literal|"split: "
operator|+
name|badFile1
operator|+
literal|" is very bad.\n"
operator|+
literal|" line c\nlineD\n"
operator|+
literal|"split: "
operator|+
name|badFile2
operator|+
literal|" is also very bad.\n"
operator|+
literal|" java.io.EOFException: null \n"
operator|+
literal|"line E\n"
decl_stmt|;
name|File
name|log3File
init|=
name|writeTestLog
argument_list|(
literal|"1"
argument_list|,
name|content
argument_list|)
decl_stmt|;
name|taskLogProcessor
operator|.
name|addTaskAttemptLogUrl
argument_list|(
name|log3File
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ErrorAndSolution
argument_list|>
name|errList
init|=
name|taskLogProcessor
operator|.
name|getErrors
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|errList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|ErrorAndSolution
name|eas
init|=
name|errList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|error
init|=
name|eas
operator|.
name|getError
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|error
argument_list|)
expr_stmt|;
comment|// check that the error code is present in the error description:
name|assertTrue
argument_list|(
name|error
operator|.
name|contains
argument_list|(
name|badFile1
argument_list|)
operator|||
name|error
operator|.
name|contains
argument_list|(
name|badFile2
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|solution
init|=
name|eas
operator|.
name|getSolution
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|solution
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|solution
operator|.
name|length
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMapAggrMemErrorHeuristic
parameter_list|()
throws|throws
name|Exception
block|{
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|HiveConf
operator|.
name|setQueryString
argument_list|(
name|jobConf
argument_list|,
literal|"select * from foo group by moo;"
argument_list|)
expr_stmt|;
specifier|final
name|TaskLogProcessor
name|taskLogProcessor
init|=
operator|new
name|TaskLogProcessor
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|Throwable
name|oome
init|=
operator|new
name|OutOfMemoryError
argument_list|(
literal|"java heap space"
argument_list|)
decl_stmt|;
name|File
name|log1File
init|=
name|writeTestLog
argument_list|(
literal|"1"
argument_list|,
name|toString
argument_list|(
name|oome
argument_list|)
argument_list|)
decl_stmt|;
name|taskLogProcessor
operator|.
name|addTaskAttemptLogUrl
argument_list|(
name|log1File
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ErrorAndSolution
argument_list|>
name|errList
init|=
name|taskLogProcessor
operator|.
name|getErrors
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|errList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|ErrorAndSolution
name|eas
init|=
name|errList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|error
init|=
name|eas
operator|.
name|getError
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|error
argument_list|)
expr_stmt|;
comment|// check that the error code is present in the error description:
name|assertTrue
argument_list|(
name|error
operator|.
name|contains
argument_list|(
literal|"memory"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|solution
init|=
name|eas
operator|.
name|getSolution
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|solution
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|solution
operator|.
name|length
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|confName
init|=
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPAGGRHASHMEMORY
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|solution
operator|.
name|contains
argument_list|(
name|confName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

