begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|api
operator|.
name|server
package|;
end_package

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
name|text
operator|.
name|SimpleDateFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|helpers
operator|.
name|FormattingTuple
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|helpers
operator|.
name|MarkerIgnoringBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|helpers
operator|.
name|MessageFormatter
import|;
end_import

begin_comment
comment|/**  * Simple logger which allows each test to have it's own log file.  */
end_comment

begin_class
specifier|public
class|class
name|TestLogger
extends|extends
name|MarkerIgnoringBase
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|1711679924980202258L
decl_stmt|;
specifier|private
specifier|final
name|LEVEL
name|mLevel
decl_stmt|;
specifier|private
specifier|final
name|PrintStream
name|mLog
decl_stmt|;
specifier|private
name|SimpleDateFormat
name|mDateFormatter
decl_stmt|;
specifier|public
name|TestLogger
parameter_list|(
name|PrintStream
name|logFile
parameter_list|,
name|LEVEL
name|level
parameter_list|)
block|{
name|mLog
operator|=
name|logFile
expr_stmt|;
name|mLevel
operator|=
name|level
expr_stmt|;
name|mDateFormatter
operator|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss,SSS"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
enum|enum
name|LEVEL
block|{
name|TRACE
argument_list|(
literal|1
argument_list|)
block|,
name|DEBUG
argument_list|(
literal|2
argument_list|)
block|,
name|INFO
argument_list|(
literal|3
argument_list|)
block|,
name|WARN
argument_list|(
literal|4
argument_list|)
block|,
name|ERROR
argument_list|(
literal|5
argument_list|)
block|;
specifier|private
name|int
name|index
decl_stmt|;
specifier|private
name|LEVEL
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isTraceEnabled
parameter_list|()
block|{
return|return
name|mLevel
operator|.
name|index
operator|>=
name|LEVEL
operator|.
name|TRACE
operator|.
name|index
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|TRACE
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|TRACE
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg1
parameter_list|,
name|Object
name|arg2
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg1
argument_list|,
name|arg2
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|TRACE
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
index|[]
name|argArray
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|arrayFormat
argument_list|(
name|format
argument_list|,
name|argArray
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|TRACE
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|TRACE
argument_list|,
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDebugEnabled
parameter_list|()
block|{
return|return
name|mLevel
operator|.
name|index
operator|>=
name|LEVEL
operator|.
name|DEBUG
operator|.
name|index
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|DEBUG
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|DEBUG
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg1
parameter_list|,
name|Object
name|arg2
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg1
argument_list|,
name|arg2
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|DEBUG
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
index|[]
name|argArray
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|arrayFormat
argument_list|(
name|format
argument_list|,
name|argArray
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|DEBUG
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|DEBUG
argument_list|,
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isInfoEnabled
parameter_list|()
block|{
return|return
name|mLevel
operator|.
name|index
operator|>=
name|LEVEL
operator|.
name|INFO
operator|.
name|index
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|info
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|INFO
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|info
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|INFO
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|info
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg1
parameter_list|,
name|Object
name|arg2
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg1
argument_list|,
name|arg2
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|INFO
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|info
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
index|[]
name|argArray
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|arrayFormat
argument_list|(
name|format
argument_list|,
name|argArray
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|INFO
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|info
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|INFO
argument_list|,
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isWarnEnabled
parameter_list|()
block|{
return|return
name|mLevel
operator|.
name|index
operator|>=
name|LEVEL
operator|.
name|WARN
operator|.
name|index
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|WARN
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|WARN
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg1
parameter_list|,
name|Object
name|arg2
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg1
argument_list|,
name|arg2
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|WARN
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
index|[]
name|argArray
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|arrayFormat
argument_list|(
name|format
argument_list|,
name|argArray
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|WARN
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|WARN
argument_list|,
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isErrorEnabled
parameter_list|()
block|{
return|return
name|mLevel
operator|.
name|index
operator|>=
name|LEVEL
operator|.
name|ERROR
operator|.
name|index
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|error
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|ERROR
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|error
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|ERROR
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|error
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg1
parameter_list|,
name|Object
name|arg2
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|arg1
argument_list|,
name|arg2
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|ERROR
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|error
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
index|[]
name|argArray
parameter_list|)
block|{
name|FormattingTuple
name|ft
init|=
name|MessageFormatter
operator|.
name|arrayFormat
argument_list|(
name|format
argument_list|,
name|argArray
argument_list|)
decl_stmt|;
name|log
argument_list|(
name|LEVEL
operator|.
name|ERROR
argument_list|,
name|ft
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ft
operator|.
name|getThrowable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|error
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|log
argument_list|(
name|LEVEL
operator|.
name|ERROR
argument_list|,
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getCaller
parameter_list|()
block|{
name|StackTraceElement
index|[]
name|stack
init|=
operator|new
name|Exception
argument_list|()
operator|.
name|getStackTrace
argument_list|()
decl_stmt|;
if|if
condition|(
name|stack
operator|.
name|length
operator|>
literal|3
condition|)
block|{
return|return
name|getCallerShortName
argument_list|(
name|stack
index|[
literal|3
index|]
argument_list|)
return|;
block|}
return|return
literal|"<unknown>"
return|;
block|}
specifier|private
name|String
name|getThreadName
parameter_list|()
block|{
return|return
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
specifier|private
name|String
name|getCallerShortName
parameter_list|(
name|StackTraceElement
name|frame
parameter_list|)
block|{
name|String
name|className
init|=
name|frame
operator|.
name|getClassName
argument_list|()
decl_stmt|;
name|String
name|methodName
init|=
name|frame
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|int
name|lineNumber
init|=
name|frame
operator|.
name|getLineNumber
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
name|className
operator|.
name|lastIndexOf
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
if|if
condition|(
name|pos
operator|>
literal|0
condition|)
block|{
name|className
operator|=
name|className
operator|.
name|substring
argument_list|(
name|pos
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s.%s:%d"
argument_list|,
name|className
argument_list|,
name|methodName
argument_list|,
name|lineNumber
argument_list|)
return|;
block|}
specifier|private
specifier|synchronized
name|void
name|log
parameter_list|(
name|LEVEL
name|level
parameter_list|,
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|level
operator|.
name|index
operator|>=
name|mLevel
operator|.
name|index
condition|)
block|{
name|mLog
operator|.
name|print
argument_list|(
name|mDateFormatter
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%5s"
argument_list|,
name|level
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
name|getThreadName
argument_list|()
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
name|getCaller
argument_list|()
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|print
argument_list|(
name|msg
argument_list|)
expr_stmt|;
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
block|{
name|mLog
operator|.
name|print
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|t
operator|.
name|printStackTrace
argument_list|(
name|mLog
argument_list|)
expr_stmt|;
block|}
name|mLog
operator|.
name|print
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|mLog
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

