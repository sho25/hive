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
name|cli
operator|.
name|control
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Stopwatch
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Strings
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
name|cli
operator|.
name|control
operator|.
name|AbstractCliConfig
operator|.
name|MetastoreType
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
name|QTestUtil
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
name|QTestUtil
operator|.
name|MiniClusterType
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
name|util
operator|.
name|ElapsedTimeLoggingWrapper
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
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_class
specifier|public
class|class
name|CoreCliDriver
extends|extends
name|CliAdapter
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
name|CoreCliDriver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|QTestUtil
name|qt
decl_stmt|;
specifier|public
name|CoreCliDriver
parameter_list|(
name|AbstractCliConfig
name|testCliConfig
parameter_list|)
block|{
name|super
argument_list|(
name|testCliConfig
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|BeforeClass
specifier|public
name|void
name|beforeClass
parameter_list|()
block|{
name|String
name|message
init|=
literal|"Starting "
operator|+
name|CoreCliDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|" run at "
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|message
argument_list|)
expr_stmt|;
specifier|final
name|MiniClusterType
name|miniMR
init|=
name|cliConfig
operator|.
name|getClusterType
argument_list|()
decl_stmt|;
specifier|final
name|String
name|hiveConfDir
init|=
name|cliConfig
operator|.
name|getHiveConfDir
argument_list|()
decl_stmt|;
specifier|final
name|String
name|initScript
init|=
name|cliConfig
operator|.
name|getInitScript
argument_list|()
decl_stmt|;
specifier|final
name|String
name|cleanupScript
init|=
name|cliConfig
operator|.
name|getCleanupScript
argument_list|()
decl_stmt|;
try|try
block|{
specifier|final
name|String
name|hadoopVer
init|=
name|cliConfig
operator|.
name|getHadoopVersion
argument_list|()
decl_stmt|;
name|qt
operator|=
operator|new
name|ElapsedTimeLoggingWrapper
argument_list|<
name|QTestUtil
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|QTestUtil
name|invokeInternal
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|new
name|QTestUtil
argument_list|(
operator|(
name|cliConfig
operator|.
name|getResultsDir
argument_list|()
operator|)
argument_list|,
operator|(
name|cliConfig
operator|.
name|getLogDir
argument_list|()
operator|)
argument_list|,
name|miniMR
argument_list|,
name|hiveConfDir
argument_list|,
name|hadoopVer
argument_list|,
name|initScript
argument_list|,
name|cleanupScript
argument_list|,
literal|true
argument_list|,
name|cliConfig
operator|.
name|getFsType
argument_list|()
argument_list|)
return|;
block|}
block|}
operator|.
name|invoke
argument_list|(
literal|"QtestUtil instance created"
argument_list|,
name|LOG
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// do a one time initialization
operator|new
name|ElapsedTimeLoggingWrapper
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|invokeInternal
parameter_list|()
throws|throws
name|Exception
block|{
name|qt
operator|.
name|cleanUp
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
operator|.
name|invoke
argument_list|(
literal|"Initialization cleanup done."
argument_list|,
name|LOG
argument_list|,
literal|true
argument_list|)
expr_stmt|;
operator|new
name|ElapsedTimeLoggingWrapper
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|invokeInternal
parameter_list|()
throws|throws
name|Exception
block|{
name|qt
operator|.
name|createSources
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
operator|.
name|invoke
argument_list|(
literal|"Initialization createSources done."
argument_list|,
name|LOG
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|flush
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected exception in static initialization"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
try|try
block|{
operator|new
name|ElapsedTimeLoggingWrapper
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|invokeInternal
parameter_list|()
throws|throws
name|Exception
block|{
name|qt
operator|.
name|clearTestSideEffects
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
operator|.
name|invoke
argument_list|(
literal|"PerTestSetup done."
argument_list|,
name|LOG
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|flush
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Unexpected exception in setup"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
try|try
block|{
operator|new
name|ElapsedTimeLoggingWrapper
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|invokeInternal
parameter_list|()
throws|throws
name|Exception
block|{
name|qt
operator|.
name|clearPostTestEffects
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
operator|.
name|invoke
argument_list|(
literal|"PerTestTearDown done."
argument_list|,
name|LOG
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|flush
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Unexpected exception in tearDown"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
annotation|@
name|AfterClass
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
operator|new
name|ElapsedTimeLoggingWrapper
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|invokeInternal
parameter_list|()
throws|throws
name|Exception
block|{
name|qt
operator|.
name|shutdown
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
operator|.
name|invoke
argument_list|(
literal|"Teardown done."
argument_list|,
name|LOG
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|flush
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Unexpected exception in shutdown"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|String
name|debugHint
init|=
literal|"\nSee ./ql/target/tmp/log/hive.log or ./itests/qtest/target/tmp/log/hive.log, "
operator|+
literal|"or check ./ql/target/surefire-reports or ./itests/qtest/target/surefire-reports/ for specific test cases logs."
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|runTest
parameter_list|(
name|String
name|tname
parameter_list|,
name|String
name|fname
parameter_list|,
name|String
name|fpath
parameter_list|)
throws|throws
name|Exception
block|{
name|Stopwatch
name|sw
init|=
name|Stopwatch
operator|.
name|createStarted
argument_list|()
decl_stmt|;
name|boolean
name|skipped
init|=
literal|false
decl_stmt|;
name|boolean
name|failed
init|=
literal|false
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Begin query: "
operator|+
name|fname
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Begin query: "
operator|+
name|fname
argument_list|)
expr_stmt|;
name|qt
operator|.
name|addFile
argument_list|(
name|fpath
argument_list|)
expr_stmt|;
if|if
condition|(
name|qt
operator|.
name|shouldBeSkipped
argument_list|(
name|fname
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Test "
operator|+
name|fname
operator|+
literal|" skipped"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Test "
operator|+
name|fname
operator|+
literal|" skipped"
argument_list|)
expr_stmt|;
name|skipped
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|qt
operator|.
name|cliInit
argument_list|(
name|fname
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|int
name|ecode
init|=
name|qt
operator|.
name|executeClient
argument_list|(
name|fname
argument_list|)
decl_stmt|;
if|if
condition|(
name|ecode
operator|!=
literal|0
condition|)
block|{
name|failed
operator|=
literal|true
expr_stmt|;
name|qt
operator|.
name|failed
argument_list|(
name|ecode
argument_list|,
name|fname
argument_list|,
name|debugHint
argument_list|)
expr_stmt|;
block|}
name|QTestProcessExecResult
name|result
init|=
name|qt
operator|.
name|checkCliDriverResults
argument_list|(
name|fname
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|getReturnCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|failed
operator|=
literal|true
expr_stmt|;
name|String
name|message
init|=
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|result
operator|.
name|getCapturedOutput
argument_list|()
argument_list|)
condition|?
name|debugHint
else|:
literal|"\r\n"
operator|+
name|result
operator|.
name|getCapturedOutput
argument_list|()
decl_stmt|;
name|qt
operator|.
name|failedDiff
argument_list|(
name|result
operator|.
name|getReturnCode
argument_list|()
argument_list|,
name|fname
argument_list|,
name|message
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
name|failed
operator|=
literal|true
expr_stmt|;
name|qt
operator|.
name|failed
argument_list|(
name|e
argument_list|,
name|fname
argument_list|,
name|debugHint
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|String
name|message
init|=
literal|"Done query"
operator|+
name|fname
operator|+
literal|". succeeded="
operator|+
operator|!
name|failed
operator|+
literal|", skipped="
operator|+
name|skipped
operator|+
literal|". ElapsedTime(ms)="
operator|+
name|sw
operator|.
name|stop
argument_list|()
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Test passed"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

