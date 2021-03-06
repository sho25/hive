begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|log
package|;
end_package

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
name|nio
operator|.
name|file
operator|.
name|DirectoryStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|FileAlreadyExistsException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|FileSystems
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|hooks
operator|.
name|LineageLogger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|LogManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|Appender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|config
operator|.
name|LoggerConfig
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
name|assertNotNull
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

begin_comment
comment|/**  * Test configuration and use of SlidingFilenameRolloverStrategy  * @see SlidingFilenameRolloverStrategy  */
end_comment

begin_class
specifier|public
class|class
name|TestSlidingFilenameRolloverStrategy
block|{
comment|// properties file used to configure log4j2
specifier|private
specifier|static
specifier|final
name|String
name|PROPERTIES_FILE
init|=
literal|"log4j2_test_sliding_rollover.properties"
decl_stmt|;
comment|//  file pattern that is set in PROPERTIES_FILE
specifier|private
specifier|static
specifier|final
name|String
name|FILE_PATTERN
init|=
literal|"./target/tmp/log/slidingTest.log"
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"log4j.configurationFile"
argument_list|,
name|PROPERTIES_FILE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
block|{
name|System
operator|.
name|clearProperty
argument_list|(
literal|"log4j.configurationFile"
argument_list|)
expr_stmt|;
name|LogManager
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSlidingLogFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
literal|"bad props file"
argument_list|,
name|PROPERTIES_FILE
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"log4j.configurationFile"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Where the log files wll be written
name|Path
name|logTemplate
init|=
name|FileSystems
operator|.
name|getDefault
argument_list|()
operator|.
name|getPath
argument_list|(
name|FILE_PATTERN
argument_list|)
decl_stmt|;
name|String
name|fileName
init|=
name|logTemplate
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Path
name|parent
init|=
name|logTemplate
operator|.
name|getParent
argument_list|()
decl_stmt|;
try|try
block|{
name|Files
operator|.
name|createDirectory
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileAlreadyExistsException
name|e
parameter_list|)
block|{
comment|// OK, fall through.
block|}
comment|// Delete any stale log files left around from previous failed tests
name|deleteLogFiles
argument_list|(
name|parent
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
name|Logger
name|logger
init|=
name|LogManager
operator|.
name|getLogger
argument_list|(
name|LineageLogger
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Does the logger config look correct?
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|Logger
name|coreLogger
init|=
operator|(
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|Logger
operator|)
name|logger
decl_stmt|;
name|LoggerConfig
name|loggerConfig
init|=
name|coreLogger
operator|.
name|get
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Appender
argument_list|>
name|appenders
init|=
name|loggerConfig
operator|.
name|getAppenders
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"sliding appender is missing"
argument_list|,
name|appenders
operator|.
name|get
argument_list|(
literal|"sliding"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Do some logging and force log rollover
name|int
name|NUM_LOGS
init|=
literal|7
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Debug Message Logged !!!"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Info Message Logged !!!"
argument_list|)
expr_stmt|;
name|String
name|errorString
init|=
literal|"Error Message Logged "
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
name|NUM_LOGS
condition|;
name|i
operator|++
control|)
block|{
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
comment|// log an exception - this produces enough text to force a new logfile
comment|// (as appender.sliding.policies.size.size=1KB)
name|logger
operator|.
name|error
argument_list|(
name|errorString
operator|+
name|i
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"part of a test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Check log files look OK
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|parent
argument_list|,
name|fileName
operator|+
literal|".*"
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|stream
control|)
block|{
name|count
operator|++
expr_stmt|;
name|String
name|contents
init|=
operator|new
name|String
argument_list|(
name|Files
operator|.
name|readAllBytes
argument_list|(
name|path
argument_list|)
argument_list|,
literal|"UTF-8"
argument_list|)
decl_stmt|;
comment|// There should be one exception message per file
name|assertTrue
argument_list|(
literal|"File "
operator|+
name|path
operator|+
literal|" did not have expected content"
argument_list|,
name|contents
operator|.
name|contains
argument_list|(
name|errorString
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|suffix
init|=
name|StringUtils
operator|.
name|substringAfterLast
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
literal|"."
argument_list|)
decl_stmt|;
comment|// suffix should be a timestamp
try|try
block|{
name|long
name|timestamp
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|suffix
argument_list|)
decl_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Suffix "
operator|+
name|suffix
operator|+
literal|" is not a long"
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"bad count of log files"
argument_list|,
name|NUM_LOGS
argument_list|,
name|count
argument_list|)
expr_stmt|;
comment|// Check there is no log file without the suffix
name|assertFalse
argument_list|(
literal|"file should not exist:"
operator|+
name|logTemplate
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|logTemplate
argument_list|)
argument_list|)
expr_stmt|;
comment|// Clean up
name|deleteLogFiles
argument_list|(
name|parent
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|deleteLogFiles
parameter_list|(
name|Path
name|parent
parameter_list|,
name|String
name|fileName
parameter_list|)
throws|throws
name|IOException
block|{
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|parent
argument_list|,
name|fileName
operator|+
literal|".*"
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|stream
control|)
block|{
name|Files
operator|.
name|delete
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

