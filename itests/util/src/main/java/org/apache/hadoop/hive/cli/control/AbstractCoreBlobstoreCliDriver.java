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
name|Calendar
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
name|HiveVariableSource
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
name|VariableSubstitution
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

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractCoreBlobstoreCliDriver
extends|extends
name|CliAdapter
block|{
specifier|protected
specifier|static
name|QTestUtil
name|qt
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HCONF_TEST_BLOBSTORE_PATH
init|=
literal|"test.blobstore.path"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HCONF_TEST_BLOBSTORE_PATH_UNIQUE
init|=
name|HCONF_TEST_BLOBSTORE_PATH
operator|+
literal|".unique"
decl_stmt|;
specifier|private
specifier|static
name|String
name|testBlobstorePathUnique
decl_stmt|;
specifier|public
name|AbstractCoreBlobstoreCliDriver
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
argument_list|)
expr_stmt|;
if|if
condition|(
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|qt
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|HCONF_TEST_BLOBSTORE_PATH
argument_list|)
argument_list|)
condition|)
block|{
name|fail
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s must be set. Try setting in blobstore-conf.xml"
argument_list|,
name|HCONF_TEST_BLOBSTORE_PATH
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// do a one time initialization
name|setupUniqueTestPath
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
name|qt
operator|.
name|clearTestSideEffects
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
name|qt
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
name|System
operator|.
name|getenv
argument_list|(
name|QTestUtil
operator|.
name|QTEST_LEAVE_FILES
argument_list|)
operator|==
literal|null
condition|)
block|{
name|String
name|rmUniquePathCommand
init|=
name|String
operator|.
name|format
argument_list|(
literal|"dfs -rmdir ${hiveconf:%s};"
argument_list|,
name|HCONF_TEST_BLOBSTORE_PATH_UNIQUE
argument_list|)
decl_stmt|;
name|qt
operator|.
name|executeAdhocCommand
argument_list|(
name|rmUniquePathCommand
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
literal|"\nSee ./itests/hive-blobstore/target/tmp/log/hive.log, "
operator|+
literal|"or check ./itests/hive-blobstore/target/surefire-reports/ for specific test cases logs."
decl_stmt|;
specifier|protected
name|void
name|runTestHelper
parameter_list|(
name|String
name|tname
parameter_list|,
name|String
name|fname
parameter_list|,
name|String
name|fpath
parameter_list|,
name|boolean
name|expectSuccess
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
name|qt
operator|.
name|getConf
argument_list|()
operator|.
name|set
argument_list|(
name|HCONF_TEST_BLOBSTORE_PATH_UNIQUE
argument_list|,
name|testBlobstorePathUnique
argument_list|)
expr_stmt|;
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
operator|(
name|ecode
operator|==
literal|0
operator|)
operator|^
name|expectSuccess
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
comment|/**    * Generates a unique test path for this particular CliDriver in the following form:    *   ${test.blobstore.path}/CoreBlobstore[Negative]CliDriver/20160101.053046.332-{random number 000-999}    * 20160101.053046.332 represents the current datetime:    *   {year}{month}{day}.{hour}{minute}{second}.{millisecond}    * Random integer 000-999 included to avoid collisions when two test runs are started at the same millisecond with    *  the same ${test.blobstore.path} (possible if test runs are controlled by an automated system)    */
specifier|private
name|void
name|setupUniqueTestPath
parameter_list|()
block|{
name|String
name|testBlobstorePath
init|=
operator|new
name|VariableSubstitution
argument_list|(
operator|new
name|HiveVariableSource
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveVariable
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|substitute
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|,
name|qt
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|HCONF_TEST_BLOBSTORE_PATH
argument_list|)
argument_list|)
decl_stmt|;
name|testBlobstorePath
operator|=
name|QTestUtil
operator|.
name|ensurePathEndsInSlash
argument_list|(
name|testBlobstorePath
argument_list|)
expr_stmt|;
name|testBlobstorePath
operator|+=
name|QTestUtil
operator|.
name|ensurePathEndsInSlash
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
comment|// name of child class
name|String
name|uid
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyyMMdd.HHmmss.SSS"
argument_list|)
operator|.
name|format
argument_list|(
name|Calendar
operator|.
name|getInstance
argument_list|()
operator|.
name|getTime
argument_list|()
argument_list|)
operator|+
literal|"-"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%03d"
argument_list|,
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|999
argument_list|)
argument_list|)
decl_stmt|;
name|testBlobstorePathUnique
operator|=
name|testBlobstorePath
operator|+
name|uid
expr_stmt|;
name|qt
operator|.
name|addPatternWithMaskComment
argument_list|(
name|testBlobstorePathUnique
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"### %s ###"
argument_list|,
name|HCONF_TEST_BLOBSTORE_PATH
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

