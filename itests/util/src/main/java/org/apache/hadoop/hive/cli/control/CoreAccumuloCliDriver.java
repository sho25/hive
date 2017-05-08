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
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|accumulo
operator|.
name|AccumuloQTestUtil
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
name|accumulo
operator|.
name|AccumuloTestSetup
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
class|class
name|CoreAccumuloCliDriver
extends|extends
name|CliAdapter
block|{
specifier|private
name|AccumuloQTestUtil
name|qt
decl_stmt|;
specifier|private
specifier|static
name|AccumuloTestSetup
name|setup
decl_stmt|;
specifier|public
name|CoreAccumuloCliDriver
parameter_list|(
name|AbstractCliConfig
name|cliConfig
parameter_list|)
block|{
name|super
argument_list|(
name|cliConfig
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
name|setup
operator|=
operator|new
name|AccumuloTestSetup
argument_list|()
expr_stmt|;
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
name|setup
operator|.
name|tearDown
argument_list|()
expr_stmt|;
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
name|MiniClusterType
name|miniMR
init|=
name|cliConfig
operator|.
name|getClusterType
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
name|qt
operator|=
operator|new
name|AccumuloQTestUtil
argument_list|(
name|cliConfig
operator|.
name|getResultsDir
argument_list|()
argument_list|,
name|cliConfig
operator|.
name|getLogDir
argument_list|()
argument_list|,
name|miniMR
argument_list|,
name|setup
argument_list|,
name|initScript
argument_list|,
name|cleanupScript
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected exception in setUp"
argument_list|,
name|e
argument_list|)
throw|;
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
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected exception in tearDown"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
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
argument_list|)
expr_stmt|;
name|qt
operator|.
name|clearTestSideEffects
argument_list|()
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
literal|null
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
name|result
operator|.
name|getCapturedOutput
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|qt
operator|.
name|failed
argument_list|(
name|e
argument_list|,
name|fname
argument_list|,
literal|null
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

