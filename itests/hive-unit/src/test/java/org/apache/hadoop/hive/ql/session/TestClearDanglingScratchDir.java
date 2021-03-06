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
name|ql
operator|.
name|session
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|CommonConfigurationKeysPublic
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
name|fs
operator|.
name|Path
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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|util
operator|.
name|Shell
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
name|Assert
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
name|LoggerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|TestClearDanglingScratchDir
block|{
specifier|private
specifier|static
name|MiniDFSCluster
name|m_dfs
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|static
name|Path
name|scratchDir
decl_stmt|;
specifier|private
name|ByteArrayOutputStream
name|stdout
decl_stmt|;
specifier|private
name|ByteArrayOutputStream
name|stderr
decl_stmt|;
specifier|private
name|PrintStream
name|origStdoutPs
decl_stmt|;
specifier|private
name|PrintStream
name|origStderrPs
decl_stmt|;
annotation|@
name|BeforeClass
specifier|static
specifier|public
name|void
name|oneTimeSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|m_dfs
operator|=
operator|new
name|MiniDFSCluster
operator|.
name|Builder
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
operator|.
name|numDataNodes
argument_list|(
literal|1
argument_list|)
operator|.
name|format
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SCRATCH_DIR_LOCK
operator|.
name|toString
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AUTO_CREATE_ALL
operator|.
name|toString
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"SessionState"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
argument_list|,
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
argument_list|,
literal|"warehouse"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CommonConfigurationKeysPublic
operator|.
name|FS_DEFAULT_NAME_KEY
argument_list|,
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|scratchDir
operator|=
operator|new
name|Path
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
argument_list|)
argument_list|)
expr_stmt|;
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
name|scratchDir
argument_list|)
expr_stmt|;
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|setPermission
argument_list|(
name|scratchDir
argument_list|,
operator|new
name|FsPermission
argument_list|(
literal|"777"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|static
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|Exception
block|{
name|m_dfs
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|redirectStdOutErr
parameter_list|()
block|{
name|stdout
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|PrintStream
name|psStdout
init|=
operator|new
name|PrintStream
argument_list|(
name|stdout
argument_list|)
decl_stmt|;
name|origStdoutPs
operator|=
name|System
operator|.
name|out
expr_stmt|;
name|System
operator|.
name|setOut
argument_list|(
name|psStdout
argument_list|)
expr_stmt|;
name|stderr
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|PrintStream
name|psStderr
init|=
operator|new
name|PrintStream
argument_list|(
name|stderr
argument_list|)
decl_stmt|;
name|origStderrPs
operator|=
name|System
operator|.
name|err
expr_stmt|;
name|System
operator|.
name|setErr
argument_list|(
name|psStderr
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|rollbackStdOutErr
parameter_list|()
block|{
name|System
operator|.
name|setOut
argument_list|(
name|origStdoutPs
argument_list|)
expr_stmt|;
name|System
operator|.
name|setErr
argument_list|(
name|origStderrPs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClearDanglingScratchDir
parameter_list|()
throws|throws
name|Exception
block|{
comment|// No scratch dir initially
name|redirectStdOutErr
argument_list|()
expr_stmt|;
name|ClearDanglingScratchDir
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-v"
block|,
literal|"-s"
block|,
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|scratchDir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|rollbackStdOutErr
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|stderr
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Cannot find any scratch directory to clear"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create scratch dir without lock files
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|scratchDir
argument_list|,
literal|"dummy"
argument_list|)
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|redirectStdOutErr
argument_list|()
expr_stmt|;
name|ClearDanglingScratchDir
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-v"
block|,
literal|"-s"
block|,
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|scratchDir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|rollbackStdOutErr
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|StringUtils
operator|.
name|countMatches
argument_list|(
name|stderr
operator|.
name|toString
argument_list|()
argument_list|,
literal|"since it does not contain "
operator|+
name|SessionState
operator|.
name|LOCK_FILE_NAME
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|stderr
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Cannot find any scratch directory to clear"
argument_list|)
argument_list|)
expr_stmt|;
comment|// One live session
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|redirectStdOutErr
argument_list|()
expr_stmt|;
name|ClearDanglingScratchDir
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-v"
block|,
literal|"-s"
block|,
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|scratchDir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|rollbackStdOutErr
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|StringUtils
operator|.
name|countMatches
argument_list|(
name|stderr
operator|.
name|toString
argument_list|()
argument_list|,
literal|"is being used by live process"
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// One dead session with dry-run
name|ss
operator|.
name|releaseSessionLockFile
argument_list|()
expr_stmt|;
name|redirectStdOutErr
argument_list|()
expr_stmt|;
name|ClearDanglingScratchDir
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-r"
block|,
literal|"-v"
block|,
literal|"-s"
block|,
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|scratchDir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|rollbackStdOutErr
argument_list|()
expr_stmt|;
comment|// Find one session dir to remove
name|Assert
operator|.
name|assertFalse
argument_list|(
name|stdout
operator|.
name|toString
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Remove the dead session dir
name|redirectStdOutErr
argument_list|()
expr_stmt|;
name|ClearDanglingScratchDir
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-v"
block|,
literal|"-s"
block|,
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|scratchDir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|rollbackStdOutErr
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|stderr
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Removing 1 scratch directories"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|StringUtils
operator|.
name|countMatches
argument_list|(
name|stderr
operator|.
name|toString
argument_list|()
argument_list|,
literal|"removed"
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|ss
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

