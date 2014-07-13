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
name|conf
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
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|common
operator|.
name|LogUtils
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
name|common
operator|.
name|util
operator|.
name|HiveTestUtils
import|;
end_import

begin_comment
comment|/**  * TestHiveLogging  *  * Test cases for HiveLogging, which is initialized in HiveConf.  * Loads configuration files located in common/src/test/resources.  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveLogging
extends|extends
name|TestCase
block|{
specifier|private
specifier|final
name|Runtime
name|runTime
decl_stmt|;
specifier|private
name|Process
name|process
decl_stmt|;
specifier|public
name|TestHiveLogging
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|runTime
operator|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
expr_stmt|;
name|process
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
name|void
name|configLog
parameter_list|(
name|String
name|hiveLog4jTest
parameter_list|,
name|String
name|hiveExecLog4jTest
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|expectedLog4jTestPath
init|=
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
name|hiveLog4jTest
argument_list|)
decl_stmt|;
name|String
name|expectedLog4jExecPath
init|=
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
name|hiveExecLog4jTest
argument_list|)
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|HIVE_LOG4J_FILE
operator|.
name|varname
argument_list|,
name|expectedLog4jTestPath
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|HIVE_EXEC_LOG4J_FILE
operator|.
name|varname
argument_list|,
name|expectedLog4jExecPath
argument_list|)
expr_stmt|;
name|LogUtils
operator|.
name|initHiveLog4j
argument_list|()
expr_stmt|;
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedLog4jTestPath
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_LOG4J_FILE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedLog4jExecPath
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_EXEC_LOG4J_FILE
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runCmd
parameter_list|(
name|String
name|cmd
parameter_list|)
throws|throws
name|Exception
block|{
name|process
operator|=
name|runTime
operator|.
name|exec
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
name|process
operator|.
name|waitFor
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|getCmdOutput
parameter_list|(
name|String
name|logFile
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|logCreated
init|=
literal|false
decl_stmt|;
name|BufferedReader
name|buf
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|process
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|line
init|=
literal|""
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|buf
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|line
operator|=
name|line
operator|.
name|replace
argument_list|(
literal|"//"
argument_list|,
literal|"/"
argument_list|)
expr_stmt|;
if|if
condition|(
name|line
operator|.
name|equals
argument_list|(
name|logFile
argument_list|)
condition|)
block|{
name|logCreated
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|logCreated
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|RunTest
parameter_list|(
name|String
name|cleanCmd
parameter_list|,
name|String
name|findCmd
parameter_list|,
name|String
name|logFile
parameter_list|,
name|String
name|hiveLog4jProperty
parameter_list|,
name|String
name|hiveExecLog4jProperty
parameter_list|)
throws|throws
name|Exception
block|{
comment|// clean test space
name|runCmd
argument_list|(
name|cleanCmd
argument_list|)
expr_stmt|;
comment|// config log4j with customized files
comment|// check whether HiveConf initialize log4j correctly
name|configLog
argument_list|(
name|hiveLog4jProperty
argument_list|,
name|hiveExecLog4jProperty
argument_list|)
expr_stmt|;
comment|// check whether log file is created on test running
name|runCmd
argument_list|(
name|findCmd
argument_list|)
expr_stmt|;
name|getCmdOutput
argument_list|(
name|logFile
argument_list|)
expr_stmt|;
comment|// clean test space
name|runCmd
argument_list|(
name|cleanCmd
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testHiveLogging
parameter_list|()
throws|throws
name|Exception
block|{
comment|// customized log4j config log file to be: /tmp/TestHiveLogging/hiveLog4jTest.log
name|String
name|customLogPath
init|=
literal|"/tmp/"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
operator|+
literal|"-TestHiveLogging/"
decl_stmt|;
name|String
name|customLogName
init|=
literal|"hiveLog4jTest.log"
decl_stmt|;
name|String
name|customLogFile
init|=
name|customLogPath
operator|+
name|customLogName
decl_stmt|;
name|String
name|customCleanCmd
init|=
literal|"rm -rf "
operator|+
name|customLogFile
decl_stmt|;
name|String
name|customFindCmd
init|=
literal|"find "
operator|+
name|customLogPath
operator|+
literal|" -name "
operator|+
name|customLogName
decl_stmt|;
name|RunTest
argument_list|(
name|customCleanCmd
argument_list|,
name|customFindCmd
argument_list|,
name|customLogFile
argument_list|,
literal|"hive-log4j-test.properties"
argument_list|,
literal|"hive-exec-log4j-test.properties"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

