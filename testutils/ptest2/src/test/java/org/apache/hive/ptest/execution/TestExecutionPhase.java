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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|atomic
operator|.
name|AtomicInteger
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
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|QFileTestBatch
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
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|TestBatch
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
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|UnitTestBatch
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
name|Assert
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
name|base
operator|.
name|Suppliers
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
name|Sets
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
name|TestExecutionPhase
extends|extends
name|AbstractTestPhase
block|{
specifier|private
specifier|static
specifier|final
name|String
name|DRIVER
init|=
literal|"driver"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|QFILENAME
init|=
literal|"sometest"
decl_stmt|;
specifier|private
name|ExecutionPhase
name|phase
decl_stmt|;
specifier|private
name|File
name|testDir
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|executedTests
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|failedTests
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TestBatch
argument_list|>
name|testBatches
decl_stmt|;
specifier|private
name|TestBatch
name|testBatch
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
name|initialize
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|executedTests
operator|=
name|Sets
operator|.
name|newHashSet
argument_list|()
expr_stmt|;
name|failedTests
operator|=
name|Sets
operator|.
name|newHashSet
argument_list|()
expr_stmt|;
block|}
specifier|private
name|ExecutionPhase
name|getPhase
parameter_list|()
throws|throws
name|IOException
block|{
name|createHostExecutor
argument_list|()
expr_stmt|;
name|phase
operator|=
operator|new
name|ExecutionPhase
argument_list|(
name|hostExecutors
argument_list|,
name|executionContext
argument_list|,
name|hostExecutorBuilder
argument_list|,
name|localCommandFactory
argument_list|,
name|templateDefaults
argument_list|,
name|succeededLogDir
argument_list|,
name|failedLogDir
argument_list|,
name|Suppliers
operator|.
name|ofInstance
argument_list|(
name|testBatches
argument_list|)
argument_list|,
name|executedTests
argument_list|,
name|failedTests
argument_list|,
name|logger
argument_list|)
expr_stmt|;
return|return
name|phase
return|;
block|}
specifier|private
name|void
name|setupQFile
parameter_list|(
name|boolean
name|isParallel
parameter_list|)
throws|throws
name|Exception
block|{
name|testDir
operator|=
name|Dirs
operator|.
name|create
argument_list|(
operator|new
name|File
argument_list|(
name|baseDir
argument_list|,
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|new
name|File
argument_list|(
name|testDir
argument_list|,
name|QFILENAME
argument_list|)
operator|.
name|createNewFile
argument_list|()
argument_list|)
expr_stmt|;
name|testBatch
operator|=
operator|new
name|QFileTestBatch
argument_list|(
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|"testcase"
argument_list|,
name|DRIVER
argument_list|,
literal|"qfile"
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
name|QFILENAME
argument_list|)
argument_list|,
name|isParallel
argument_list|,
literal|"testModule"
argument_list|)
expr_stmt|;
name|testBatches
operator|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|testBatch
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupUnitTest
parameter_list|()
throws|throws
name|Exception
block|{
name|testBatch
operator|=
operator|new
name|UnitTestBatch
argument_list|(
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|"testcase"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|DRIVER
argument_list|)
argument_list|,
literal|"fakemodule"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testBatches
operator|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|testBatch
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupUnitTest
parameter_list|(
name|int
name|nTests
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|testList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|nTests
condition|;
name|i
operator|++
control|)
block|{
name|testList
operator|.
name|add
argument_list|(
literal|"TestClass-"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|testBatch
operator|=
operator|new
name|UnitTestBatch
argument_list|(
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|"testcase"
argument_list|,
name|testList
argument_list|,
literal|"fakemodule"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testBatches
operator|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|testBatch
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|copyTestOutput
parameter_list|(
name|String
name|resource
parameter_list|,
name|File
name|directory
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|junitOutput
init|=
name|Templates
operator|.
name|readResource
argument_list|(
name|resource
argument_list|)
decl_stmt|;
name|File
name|junitOutputFile
init|=
operator|new
name|File
argument_list|(
name|Dirs
operator|.
name|create
argument_list|(
operator|new
name|File
argument_list|(
name|directory
argument_list|,
name|name
argument_list|)
argument_list|)
argument_list|,
literal|"TEST-SomeTest.xml"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|junitOutput
operator|.
name|getBytes
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|,
name|junitOutputFile
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|copyTestOutput
parameter_list|(
name|String
name|resource
parameter_list|,
name|File
name|directory
parameter_list|,
name|String
name|batchName
parameter_list|,
name|String
name|outputName
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|junitOutput
init|=
name|Templates
operator|.
name|readResource
argument_list|(
name|resource
argument_list|)
decl_stmt|;
name|File
name|junitOutputFile
init|=
operator|new
name|File
argument_list|(
name|Dirs
operator|.
name|create
argument_list|(
operator|new
name|File
argument_list|(
name|directory
argument_list|,
name|batchName
argument_list|)
argument_list|)
argument_list|,
name|outputName
argument_list|)
decl_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|junitOutput
operator|.
name|getBytes
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|,
name|junitOutputFile
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
block|{
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|baseDir
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPassingQFileTest
parameter_list|()
throws|throws
name|Throwable
block|{
name|setupQFile
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|copyTestOutput
argument_list|(
literal|"SomeTest-success.xml"
argument_list|,
name|succeededLogDir
argument_list|,
name|testBatch
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|getPhase
argument_list|()
operator|.
name|execute
argument_list|()
expr_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|getExecutedCommands
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"SomeTest."
operator|+
name|QFILENAME
argument_list|)
argument_list|,
name|executedTests
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|()
argument_list|,
name|failedTests
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailingQFile
parameter_list|()
throws|throws
name|Throwable
block|{
name|setupQFile
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|sshCommandExecutor
operator|.
name|putFailure
argument_list|(
literal|"bash "
operator|+
name|LOCAL_DIR
operator|+
literal|"/"
operator|+
name|HOST
operator|+
literal|"-"
operator|+
name|USER
operator|+
literal|"-0/scratch/hiveptest-"
operator|+
literal|"1-"
operator|+
name|DRIVER
operator|+
literal|"-"
operator|+
name|QFILENAME
operator|+
literal|".sh"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|copyTestOutput
argument_list|(
literal|"SomeTest-failure.xml"
argument_list|,
name|failedLogDir
argument_list|,
name|testBatch
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|getPhase
argument_list|()
operator|.
name|execute
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sshCommandExecutor
operator|.
name|getMatchCount
argument_list|()
argument_list|)
expr_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|getExecutedCommands
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"SomeTest."
operator|+
name|QFILENAME
argument_list|)
argument_list|,
name|executedTests
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"SomeTest."
operator|+
name|QFILENAME
operator|+
literal|" (batchId=1)"
argument_list|)
argument_list|,
name|failedTests
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPassingUnitTest
parameter_list|()
throws|throws
name|Throwable
block|{
name|setupUnitTest
argument_list|()
expr_stmt|;
name|copyTestOutput
argument_list|(
literal|"SomeTest-success.xml"
argument_list|,
name|succeededLogDir
argument_list|,
name|testBatch
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|getPhase
argument_list|()
operator|.
name|execute
argument_list|()
expr_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|getExecutedCommands
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"SomeTest."
operator|+
name|QFILENAME
argument_list|)
argument_list|,
name|executedTests
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|()
argument_list|,
name|failedTests
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailingUnitTest
parameter_list|()
throws|throws
name|Throwable
block|{
name|setupUnitTest
argument_list|()
expr_stmt|;
name|sshCommandExecutor
operator|.
name|putFailure
argument_list|(
literal|"bash "
operator|+
name|LOCAL_DIR
operator|+
literal|"/"
operator|+
name|HOST
operator|+
literal|"-"
operator|+
name|USER
operator|+
literal|"-0/scratch/hiveptest-"
operator|+
name|testBatch
operator|.
name|getBatchId
argument_list|()
operator|+
literal|"_"
operator|+
name|DRIVER
operator|+
literal|".sh"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|copyTestOutput
argument_list|(
literal|"SomeTest-failure.xml"
argument_list|,
name|failedLogDir
argument_list|,
name|testBatch
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|getPhase
argument_list|()
operator|.
name|execute
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sshCommandExecutor
operator|.
name|getMatchCount
argument_list|()
argument_list|)
expr_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|getExecutedCommands
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"SomeTest."
operator|+
name|QFILENAME
argument_list|)
argument_list|,
name|executedTests
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"SomeTest."
operator|+
name|QFILENAME
operator|+
literal|" (batchId=1)"
argument_list|)
argument_list|,
name|failedTests
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPerfMetrics
parameter_list|()
throws|throws
name|Throwable
block|{
comment|//when test is successful
name|setupUnitTest
argument_list|()
expr_stmt|;
name|copyTestOutput
argument_list|(
literal|"SomeTest-success.xml"
argument_list|,
name|succeededLogDir
argument_list|,
name|testBatch
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Phase
name|phase
init|=
name|getPhase
argument_list|()
decl_stmt|;
name|phase
operator|.
name|execute
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"Perf metrics should have been initialized"
argument_list|,
name|phase
operator|.
name|getPerfMetrics
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|ExecutionPhase
operator|.
name|TOTAL_RSYNC_TIME
operator|+
literal|" should have been initialized"
argument_list|,
name|phase
operator|.
name|getPerfMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|ExecutionPhase
operator|.
name|TOTAL_RSYNC_TIME
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Total Rsync Elapsed time should have been greater than 0"
argument_list|,
name|phase
operator|.
name|getPerfMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|ExecutionPhase
operator|.
name|TOTAL_RSYNC_TIME
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|//when test fails
name|setupUnitTest
argument_list|()
expr_stmt|;
name|sshCommandExecutor
operator|.
name|putFailure
argument_list|(
literal|"bash "
operator|+
name|LOCAL_DIR
operator|+
literal|"/"
operator|+
name|HOST
operator|+
literal|"-"
operator|+
name|USER
operator|+
literal|"-0/scratch/hiveptest-"
operator|+
name|testBatch
operator|.
name|getBatchId
argument_list|()
operator|+
literal|"_"
operator|+
name|DRIVER
operator|+
literal|".sh"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|copyTestOutput
argument_list|(
literal|"SomeTest-failure.xml"
argument_list|,
name|failedLogDir
argument_list|,
name|testBatch
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|phase
operator|=
name|getPhase
argument_list|()
expr_stmt|;
name|phase
operator|.
name|execute
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"Perf metrics should have been initialized"
argument_list|,
name|phase
operator|.
name|getPerfMetrics
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|ExecutionPhase
operator|.
name|TOTAL_RSYNC_TIME
operator|+
literal|" should have been initialized"
argument_list|,
name|phase
operator|.
name|getPerfMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|ExecutionPhase
operator|.
name|TOTAL_RSYNC_TIME
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Total Rsync Elapsed time should have been greater than 0"
argument_list|,
name|phase
operator|.
name|getPerfMetrics
argument_list|()
operator|.
name|get
argument_list|(
name|ExecutionPhase
operator|.
name|TOTAL_RSYNC_TIME
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|20000
argument_list|)
specifier|public
name|void
name|testTimedOutUnitTest
parameter_list|()
throws|throws
name|Throwable
block|{
name|setupUnitTest
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|copyTestOutput
argument_list|(
literal|"SomeTest-success.xml"
argument_list|,
name|succeededLogDir
argument_list|,
name|testBatch
operator|.
name|getName
argument_list|()
argument_list|,
literal|"TEST-TestClass-0.xml"
argument_list|)
expr_stmt|;
name|copyTestOutput
argument_list|(
literal|"SomeTest-success.xml"
argument_list|,
name|succeededLogDir
argument_list|,
name|testBatch
operator|.
name|getName
argument_list|()
argument_list|,
literal|"TEST-TestClass-1.xml"
argument_list|)
expr_stmt|;
name|getPhase
argument_list|()
operator|.
name|execute
argument_list|()
expr_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|getExecutedCommands
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|failedTests
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"TestClass-2 - did not produce a TEST-*.xml file (likely timed out) (batchId=1)"
argument_list|,
name|failedTests
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

