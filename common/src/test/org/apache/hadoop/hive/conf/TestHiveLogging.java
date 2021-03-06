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
name|conf
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|assertFalse
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
name|assertTrue
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

begin_comment
comment|/**  * TestHiveLogging  *  * Test cases for HiveLogging, which is initialized in HiveConf.  * Loads configuration files located in common/src/test/resources.  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveLogging
block|{
specifier|public
name|TestHiveLogging
parameter_list|()
block|{
name|super
argument_list|()
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
specifier|public
name|void
name|cleanLog
parameter_list|(
name|File
name|logFile
parameter_list|)
block|{
if|if
condition|(
name|logFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|logFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
name|File
name|logFileDir
init|=
name|logFile
operator|.
name|getParentFile
argument_list|()
decl_stmt|;
if|if
condition|(
name|logFileDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|logFileDir
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|RunTest
parameter_list|(
name|File
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
name|cleanLog
argument_list|(
name|logFile
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|logFile
operator|+
literal|" should not exist"
argument_list|,
name|logFile
operator|.
name|exists
argument_list|()
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
name|assertTrue
argument_list|(
name|logFile
operator|+
literal|" should exist"
argument_list|,
name|logFile
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHiveLogging
parameter_list|()
throws|throws
name|Exception
block|{
comment|// customized log4j config log file to be: /${test.tmp.dir}/TestHiveLogging/hiveLog4jTest.log
name|File
name|customLogPath
init|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
argument_list|)
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
operator|+
literal|"-TestHiveLogging/"
argument_list|)
decl_stmt|;
name|String
name|customLogName
init|=
literal|"hiveLog4j2Test.log"
decl_stmt|;
name|File
name|customLogFile
init|=
operator|new
name|File
argument_list|(
name|customLogPath
argument_list|,
name|customLogName
argument_list|)
decl_stmt|;
name|RunTest
argument_list|(
name|customLogFile
argument_list|,
literal|"hive-log4j2-test.properties"
argument_list|,
literal|"hive-exec-log4j2-test.properties"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

