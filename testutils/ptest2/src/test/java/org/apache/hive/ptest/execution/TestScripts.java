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
name|execution
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
name|StringWriter
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
name|velocity
operator|.
name|VelocityContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|velocity
operator|.
name|app
operator|.
name|Velocity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|approvaltests
operator|.
name|Approvals
import|;
end_import

begin_import
import|import
name|org
operator|.
name|approvaltests
operator|.
name|reporters
operator|.
name|JunitReporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|approvaltests
operator|.
name|reporters
operator|.
name|UseReporter
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
name|Before
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
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Charsets
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
name|collect
operator|.
name|Maps
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
name|io
operator|.
name|Files
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
name|io
operator|.
name|Resources
import|;
end_import

begin_class
annotation|@
name|UseReporter
argument_list|(
name|JunitReporter
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestScripts
block|{
specifier|private
name|File
name|baseDir
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|baseDir
operator|=
name|Files
operator|.
name|createTempDir
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|baseDir
operator|!=
literal|null
condition|)
block|{
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|baseDir
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBatch
parameter_list|()
throws|throws
name|Throwable
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateVariables
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repository"
argument_list|,
literal|"git:///repo1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repositoryName"
argument_list|,
literal|"apache"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"branch"
argument_list|,
literal|"branch-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"localDir"
argument_list|,
literal|"/some/local/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"workingDir"
argument_list|,
literal|"/some/working/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antArgs"
argument_list|,
literal|"-Dant=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"buildTag"
argument_list|,
literal|"build-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"logDir"
argument_list|,
literal|"/some/log/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"instanceName"
argument_list|,
literal|"instance-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"batchName"
argument_list|,
literal|"batch-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"numOfFailedTests"
argument_list|,
literal|"20"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"maxSourceDirs"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"testArguments"
argument_list|,
literal|"-Dtest=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"clearLibraryCache"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"javaHome"
argument_list|,
literal|"/usr/java/jdk1.7"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antEnvOpts"
argument_list|,
literal|"-Dhttp.proxyHost=somehost -Dhttp.proxyPort=3128"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antTestArgs"
argument_list|,
literal|"-DgrammarBuild.notRequired=true -Dskip.javadoc=true"
argument_list|)
expr_stmt|;
name|String
name|template
init|=
name|readResource
argument_list|(
literal|"batch-exec.vm"
argument_list|)
decl_stmt|;
name|String
name|actual
init|=
name|getTemplateResult
argument_list|(
name|template
argument_list|,
name|templateVariables
argument_list|)
decl_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlternativeTestJVM
parameter_list|()
throws|throws
name|Throwable
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateVariables
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repository"
argument_list|,
literal|"git:///repo1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repositoryName"
argument_list|,
literal|"apache"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"branch"
argument_list|,
literal|"branch-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"localDir"
argument_list|,
literal|"/some/local/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"workingDir"
argument_list|,
literal|"/some/working/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antArgs"
argument_list|,
literal|"-Dant=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"buildTag"
argument_list|,
literal|"build-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"logDir"
argument_list|,
literal|"/some/log/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"instanceName"
argument_list|,
literal|"instance-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"batchName"
argument_list|,
literal|"batch-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"numOfFailedTests"
argument_list|,
literal|"20"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"maxSourceDirs"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"testArguments"
argument_list|,
literal|"-Dtest=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"clearLibraryCache"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"javaHome"
argument_list|,
literal|"/usr/java/jdk1.7"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"javaHomeForTests"
argument_list|,
literal|"/usr/java/jdk1.7-other"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antEnvOpts"
argument_list|,
literal|"-Dhttp.proxyHost=somehost -Dhttp.proxyPort=3128"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antTestArgs"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|String
name|template
init|=
name|readResource
argument_list|(
literal|"batch-exec.vm"
argument_list|)
decl_stmt|;
name|String
name|actual
init|=
name|getTemplateResult
argument_list|(
name|template
argument_list|,
name|templateVariables
argument_list|)
decl_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPrepNone
parameter_list|()
throws|throws
name|Throwable
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateVariables
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repository"
argument_list|,
literal|"git:///repo1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repositoryName"
argument_list|,
literal|"apache"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"branch"
argument_list|,
literal|"branch-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"localDir"
argument_list|,
literal|"/some/local/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"workingDir"
argument_list|,
literal|"/some/working/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antArgs"
argument_list|,
literal|"-Dant=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"buildTag"
argument_list|,
literal|"build-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"logDir"
argument_list|,
literal|"/some/log/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"testArguments"
argument_list|,
literal|"-Dtest=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"clearLibraryCache"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"javaHome"
argument_list|,
literal|"/usr/java/jdk1.7"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antEnvOpts"
argument_list|,
literal|"-Dhttp.proxyHost=somehost -Dhttp.proxyPort=3128"
argument_list|)
expr_stmt|;
name|String
name|template
init|=
name|readResource
argument_list|(
literal|"source-prep.vm"
argument_list|)
decl_stmt|;
name|String
name|actual
init|=
name|getTemplateResult
argument_list|(
name|template
argument_list|,
name|templateVariables
argument_list|)
decl_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPrepGit
parameter_list|()
throws|throws
name|Throwable
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateVariables
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repository"
argument_list|,
literal|"git:///repo1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repositoryName"
argument_list|,
literal|"apache"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"branch"
argument_list|,
literal|"branch-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"localDir"
argument_list|,
literal|"/some/local/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"workingDir"
argument_list|,
literal|"/some/working/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antArgs"
argument_list|,
literal|"-Dant=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"buildTag"
argument_list|,
literal|"build-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"logDir"
argument_list|,
literal|"/some/log/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"testArguments"
argument_list|,
literal|"-Dtest=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"clearLibraryCache"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"javaHome"
argument_list|,
literal|"/usr/java/jdk1.7"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antEnvOpts"
argument_list|,
literal|"-Dhttp.proxyHost=somehost -Dhttp.proxyPort=3128"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repositoryType"
argument_list|,
literal|"git"
argument_list|)
expr_stmt|;
name|String
name|template
init|=
name|readResource
argument_list|(
literal|"source-prep.vm"
argument_list|)
decl_stmt|;
name|String
name|actual
init|=
name|getTemplateResult
argument_list|(
name|template
argument_list|,
name|templateVariables
argument_list|)
decl_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPrepSvn
parameter_list|()
throws|throws
name|Throwable
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateVariables
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repository"
argument_list|,
literal|"https://svn.apache.org/repos/asf/hive/trunk"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repositoryName"
argument_list|,
literal|"apache"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"branch"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"localDir"
argument_list|,
literal|"/some/local/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"workingDir"
argument_list|,
literal|"/some/working/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antArgs"
argument_list|,
literal|"-Dant=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"buildTag"
argument_list|,
literal|"build-1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"logDir"
argument_list|,
literal|"/some/log/dir"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"testArguments"
argument_list|,
literal|"-Dtest=arg1"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"clearLibraryCache"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"javaHome"
argument_list|,
literal|"/usr/java/jdk1.7"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"antEnvOpts"
argument_list|,
literal|"-Dhttp.proxyHost=somehost -Dhttp.proxyPort=3128"
argument_list|)
expr_stmt|;
name|templateVariables
operator|.
name|put
argument_list|(
literal|"repositoryType"
argument_list|,
literal|"svn"
argument_list|)
expr_stmt|;
name|String
name|template
init|=
name|readResource
argument_list|(
literal|"source-prep.vm"
argument_list|)
decl_stmt|;
name|String
name|actual
init|=
name|getTemplateResult
argument_list|(
name|template
argument_list|,
name|templateVariables
argument_list|)
decl_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|actual
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|String
name|readResource
parameter_list|(
name|String
name|resource
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Resources
operator|.
name|toString
argument_list|(
name|Resources
operator|.
name|getResource
argument_list|(
name|resource
argument_list|)
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|String
name|getTemplateResult
parameter_list|(
name|String
name|command
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|keyValues
parameter_list|)
throws|throws
name|IOException
block|{
name|VelocityContext
name|context
init|=
operator|new
name|VelocityContext
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|keyValues
operator|.
name|keySet
argument_list|()
control|)
block|{
name|context
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|keyValues
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Velocity
operator|.
name|evaluate
argument_list|(
name|context
argument_list|,
name|writer
argument_list|,
name|command
argument_list|,
name|command
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to render "
operator|+
name|command
operator|+
literal|" with "
operator|+
name|keyValues
argument_list|)
throw|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|writer
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

