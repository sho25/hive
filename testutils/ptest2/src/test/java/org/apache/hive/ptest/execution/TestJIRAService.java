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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|HashSet
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
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|JIRAService
operator|.
name|BuildInfo
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
name|Context
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
name|TestConfiguration
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
name|junit
operator|.
name|Test
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
annotation|@
name|UseReporter
argument_list|(
name|JunitReporter
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestJIRAService
extends|extends
name|AbstractTestPhase
block|{
name|TestConfiguration
name|conf
decl_stmt|;
name|JIRAService
name|jiraService
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
name|conf
operator|=
name|TestConfiguration
operator|.
name|withContext
argument_list|(
name|Context
operator|.
name|fromInputStream
argument_list|(
name|Resources
operator|.
name|getResource
argument_list|(
literal|"test-configuration.properties"
argument_list|)
operator|.
name|openStream
argument_list|()
argument_list|)
argument_list|,
name|logger
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setPatch
argument_list|(
literal|"https://HIVE-10000.patch"
argument_list|)
expr_stmt|;
name|jiraService
operator|=
operator|new
name|JIRAService
argument_list|(
name|logger
argument_list|,
name|conf
argument_list|,
literal|"tag-10"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFormatBuildTagPositive
parameter_list|()
throws|throws
name|Throwable
block|{
name|BuildInfo
name|buildInfo
init|=
name|JIRAService
operator|.
name|formatBuildTag
argument_list|(
literal|"abc-123"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"abc/123"
argument_list|,
name|buildInfo
operator|.
name|getFormattedBuildTag
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"abc"
argument_list|,
name|buildInfo
operator|.
name|getBuildName
argument_list|()
argument_list|)
expr_stmt|;
name|buildInfo
operator|=
name|JIRAService
operator|.
name|formatBuildTag
argument_list|(
literal|"PreCommit-HIVE-TRUNK-Build-1115"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"PreCommit-HIVE-TRUNK-Build/1115"
argument_list|,
name|buildInfo
operator|.
name|getFormattedBuildTag
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"PreCommit-HIVE-TRUNK-Build"
argument_list|,
name|buildInfo
operator|.
name|getBuildName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testFormatBuildTagNoDashSlash
parameter_list|()
throws|throws
name|Throwable
block|{
name|JIRAService
operator|.
name|formatBuildTag
argument_list|(
literal|"abc/123"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testFormatBuildTagNoDashSpace
parameter_list|()
throws|throws
name|Throwable
block|{
name|JIRAService
operator|.
name|formatBuildTag
argument_list|(
literal|"abc 123"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testFormatBuildTagNoDashNone
parameter_list|()
throws|throws
name|Throwable
block|{
name|JIRAService
operator|.
name|formatBuildTag
argument_list|(
literal|"abc123"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrimMesssagesBoundry
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|messages
argument_list|,
name|JIRAService
operator|.
name|trimMessages
argument_list|(
name|messages
argument_list|)
argument_list|)
expr_stmt|;
name|messages
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|JIRAService
operator|.
name|MAX_MESSAGES
condition|;
name|i
operator|++
control|)
block|{
name|messages
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|messages
argument_list|,
name|JIRAService
operator|.
name|trimMessages
argument_list|(
name|messages
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrimMesssagesNotTrimmed
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|messages
argument_list|,
name|JIRAService
operator|.
name|trimMessages
argument_list|(
name|messages
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrimMesssagesTrimmed
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
name|Lists
operator|.
name|newArrayList
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
name|JIRAService
operator|.
name|MAX_MESSAGES
operator|+
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|messages
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|messages
argument_list|)
decl_stmt|;
name|expected
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|JIRAService
operator|.
name|TRIMMED_MESSAGE
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|JIRAService
operator|.
name|trimMessages
argument_list|(
name|messages
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testErrorWithMessages
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|messages
operator|.
name|add
argument_list|(
literal|"Error message 1"
argument_list|)
expr_stmt|;
name|messages
operator|.
name|add
argument_list|(
literal|"Error message 2"
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|addedTests
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|jiraService
operator|.
name|generateComments
argument_list|(
literal|true
argument_list|,
literal|0
argument_list|,
name|failedTests
argument_list|,
name|messages
argument_list|,
name|addedTests
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testErrorWithoutMessages
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|addedTests
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|jiraService
operator|.
name|generateComments
argument_list|(
literal|true
argument_list|,
literal|0
argument_list|,
name|failedTests
argument_list|,
name|messages
argument_list|,
name|addedTests
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailNoAdd
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|failedTests
operator|.
name|add
argument_list|(
literal|"FailedTest1"
argument_list|)
expr_stmt|;
name|failedTests
operator|.
name|add
argument_list|(
literal|"FailedTest2"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|addedTests
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|jiraService
operator|.
name|generateComments
argument_list|(
literal|false
argument_list|,
literal|5
argument_list|,
name|failedTests
argument_list|,
name|messages
argument_list|,
name|addedTests
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailAdd
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|failedTests
operator|.
name|add
argument_list|(
literal|"FailedTest1"
argument_list|)
expr_stmt|;
name|failedTests
operator|.
name|add
argument_list|(
literal|"FailedTest2"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|addedTests
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|addedTests
operator|.
name|add
argument_list|(
literal|"AddedTest1"
argument_list|)
expr_stmt|;
name|addedTests
operator|.
name|add
argument_list|(
literal|"AddedTest2"
argument_list|)
expr_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|jiraService
operator|.
name|generateComments
argument_list|(
literal|false
argument_list|,
literal|5
argument_list|,
name|failedTests
argument_list|,
name|messages
argument_list|,
name|addedTests
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSuccessNoAdd
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|addedTests
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|jiraService
operator|.
name|generateComments
argument_list|(
literal|false
argument_list|,
literal|5
argument_list|,
name|failedTests
argument_list|,
name|messages
argument_list|,
name|addedTests
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSuccessAdd
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|addedTests
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|addedTests
operator|.
name|add
argument_list|(
literal|"AddedTest1"
argument_list|)
expr_stmt|;
name|addedTests
operator|.
name|add
argument_list|(
literal|"AddedTest2"
argument_list|)
expr_stmt|;
name|Approvals
operator|.
name|verify
argument_list|(
name|jiraService
operator|.
name|generateComments
argument_list|(
literal|false
argument_list|,
literal|5
argument_list|,
name|failedTests
argument_list|,
name|messages
argument_list|,
name|addedTests
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

