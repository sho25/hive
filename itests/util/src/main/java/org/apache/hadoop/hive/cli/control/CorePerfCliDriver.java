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
name|io
operator|.
name|File
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
name|MetaStoreDumpUtility
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
name|QTestArguments
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

begin_comment
comment|/**  This is the TestPerformance Cli Driver for integrating performance regression tests  as part of the Hive Unit tests.  Currently this includes support for :  1. Running explain plans for TPCDS workload (non-partitioned dataset)  on 30TB scaleset.  TODO :  1. Support for partitioned data set  2. Use HBase Metastore instead of Derby  This suite differs from TestCliDriver w.r.t the fact that we modify the underlying metastore database to reflect the dataset before running the queries. */
end_comment

begin_class
specifier|public
class|class
name|CorePerfCliDriver
extends|extends
name|CliAdapter
block|{
specifier|private
specifier|static
name|QTestUtil
name|qt
decl_stmt|;
specifier|public
name|CorePerfCliDriver
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
specifier|public
name|void
name|beforeClass
parameter_list|()
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"datanucleus.schema.autoCreateAll"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.schema.verification"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|MiniClusterType
name|miniMR
init|=
name|cliConfig
operator|.
name|getClusterType
argument_list|()
decl_stmt|;
name|String
name|hiveConfDir
init|=
name|cliConfig
operator|.
name|getHiveConfDir
argument_list|()
decl_stmt|;
name|String
name|initScript
init|=
name|cliConfig
operator|.
name|getInitScript
argument_list|()
decl_stmt|;
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
name|QTestUtil
argument_list|(
name|QTestArguments
operator|.
name|QTestArgumentsBuilder
operator|.
name|instance
argument_list|()
operator|.
name|withOutDir
argument_list|(
name|cliConfig
operator|.
name|getResultsDir
argument_list|()
argument_list|)
operator|.
name|withLogDir
argument_list|(
name|cliConfig
operator|.
name|getLogDir
argument_list|()
argument_list|)
operator|.
name|withClusterType
argument_list|(
name|miniMR
argument_list|)
operator|.
name|withConfDir
argument_list|(
name|hiveConfDir
argument_list|)
operator|.
name|withHadoopVer
argument_list|(
name|hadoopVer
argument_list|)
operator|.
name|withInitScript
argument_list|(
name|initScript
argument_list|)
operator|.
name|withCleanupScript
argument_list|(
name|cleanupScript
argument_list|)
operator|.
name|withLlapIo
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// do a one time initialization
name|qt
operator|.
name|newSession
argument_list|()
expr_stmt|;
name|qt
operator|.
name|cleanUp
argument_list|()
expr_stmt|;
name|qt
operator|.
name|createSources
argument_list|()
expr_stmt|;
comment|// Manually modify the underlying metastore db to reflect statistics corresponding to
comment|// the 30TB TPCDS scale set. This way the optimizer will generate plans for a 30 TB set.
name|MetaStoreDumpUtility
operator|.
name|setupMetaStoreTableColumnStatsFor30TBTPCDSWorkload
argument_list|(
name|qt
operator|.
name|getConf
argument_list|()
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
name|QTestUtil
operator|.
name|TEST_TMP_DIR_PROPERTY
argument_list|)
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
literal|"Unexpected exception in static initialization: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
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
name|qt
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
block|{
try|try
block|{
name|qt
operator|.
name|newSession
argument_list|()
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
literal|"Unexpected exception"
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
name|qt
operator|.
name|clearPostTestEffects
argument_list|()
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
name|name
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
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
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
name|qt
operator|.
name|cliInit
argument_list|(
operator|new
name|File
argument_list|(
name|fpath
argument_list|)
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
name|long
name|elapsedTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Done query: "
operator|+
name|fname
operator|+
literal|" elapsedTime="
operator|+
name|elapsedTime
operator|/
literal|1000
operator|+
literal|"s"
argument_list|)
expr_stmt|;
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

