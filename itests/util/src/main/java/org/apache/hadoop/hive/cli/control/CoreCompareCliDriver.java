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
name|ql
operator|.
name|QFileVersionHandler
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
name|QTestMiniClusters
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
name|ql
operator|.
name|processors
operator|.
name|CommandProcessorException
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

begin_class
specifier|public
class|class
name|CoreCompareCliDriver
extends|extends
name|CliAdapter
block|{
specifier|private
specifier|static
name|QTestUtil
name|qt
decl_stmt|;
specifier|private
name|QFileVersionHandler
name|qvh
init|=
operator|new
name|QFileVersionHandler
argument_list|()
decl_stmt|;
specifier|public
name|CoreCompareCliDriver
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
annotation|@
name|Override
specifier|protected
name|QTestUtil
name|getQt
parameter_list|()
block|{
return|return
name|qt
return|;
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
block|{
specifier|final
name|String
name|queryDirectory
init|=
name|cliConfig
operator|.
name|getQueryDirectory
argument_list|()
decl_stmt|;
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
comment|// TODO: versions could also be picked at build time.
name|List
argument_list|<
name|String
argument_list|>
name|versionFiles
init|=
name|qvh
operator|.
name|getVersionFiles
argument_list|(
name|queryDirectory
argument_list|,
name|tname
argument_list|)
decl_stmt|;
if|if
condition|(
name|versionFiles
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
name|fail
argument_list|(
literal|"Cannot run "
operator|+
name|tname
operator|+
literal|" with only "
operator|+
name|versionFiles
operator|.
name|size
argument_list|()
operator|+
literal|" versions"
argument_list|)
expr_stmt|;
block|}
name|qt
operator|.
name|addFile
argument_list|(
name|fpath
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|versionFile
range|:
name|versionFiles
control|)
block|{
name|qt
operator|.
name|addFile
argument_list|(
operator|new
name|File
argument_list|(
name|queryDirectory
argument_list|,
name|versionFile
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
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
name|List
argument_list|<
name|String
argument_list|>
name|outputs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|versionFiles
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|versionFile
range|:
name|versionFiles
control|)
block|{
comment|// 1 for "_" after tname; 3 for ".qv" at the end. Version is in between.
name|String
name|versionStr
init|=
name|versionFile
operator|.
name|substring
argument_list|(
name|tname
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|,
name|versionFile
operator|.
name|length
argument_list|()
operator|-
literal|3
argument_list|)
decl_stmt|;
name|outputs
operator|.
name|add
argument_list|(
name|qt
operator|.
name|cliInit
argument_list|(
operator|new
name|File
argument_list|(
name|queryDirectory
argument_list|,
name|versionFile
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// TODO: will this work?
try|try
block|{
name|qt
operator|.
name|executeClient
argument_list|(
name|versionFile
argument_list|,
name|fname
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|qt
operator|.
name|failedQuery
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|,
name|fname
argument_list|,
name|QTestUtil
operator|.
name|DEBUG_HINT
argument_list|)
expr_stmt|;
block|}
block|}
name|QTestProcessExecResult
name|result
init|=
name|qt
operator|.
name|checkCompareCliDriverResults
argument_list|(
name|fname
argument_list|,
name|outputs
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
name|QTestUtil
operator|.
name|DEBUG_HINT
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
name|failedWithException
argument_list|(
name|e
argument_list|,
name|fname
argument_list|,
name|QTestUtil
operator|.
name|DEBUG_HINT
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
block|}
block|}
end_class

end_unit

