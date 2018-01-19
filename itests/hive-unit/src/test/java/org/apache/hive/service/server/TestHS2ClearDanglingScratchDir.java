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
name|hive
operator|.
name|service
operator|.
name|server
package|;
end_package

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
name|FileStatus
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
name|hive
operator|.
name|ql
operator|.
name|session
operator|.
name|SessionState
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
name|shims
operator|.
name|Utils
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
name|Assert
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

begin_class
specifier|public
class|class
name|TestHS2ClearDanglingScratchDir
block|{
annotation|@
name|Test
specifier|public
name|void
name|testScratchDirCleared
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniDFSCluster
name|m_dfs
init|=
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
decl_stmt|;
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
name|m_dfs
operator|.
name|getConfiguration
argument_list|(
literal|0
argument_list|)
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
name|HIVE_SERVER2_CLEAR_DANGLING_SCRATCH_DIR
operator|.
name|toString
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|Path
name|scratchDir
init|=
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
decl_stmt|;
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
comment|// Fake two live session
name|SessionState
operator|.
name|start
argument_list|(
name|conf
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
name|HIVESESSIONID
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Fake dead session
name|Path
name|fakeSessionPath
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|scratchDir
argument_list|,
name|Utils
operator|.
name|getUGI
argument_list|()
operator|.
name|getShortUserName
argument_list|()
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
decl_stmt|;
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
name|fakeSessionPath
argument_list|)
expr_stmt|;
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|fakeSessionPath
argument_list|,
literal|"inuse.lck"
argument_list|)
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|FileStatus
index|[]
name|scratchDirs
init|=
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|scratchDir
argument_list|,
name|Utils
operator|.
name|getUGI
argument_list|()
operator|.
name|getShortUserName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|scratchDirs
operator|.
name|length
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|HiveServer2
operator|.
name|scheduleClearDanglingScratchDir
argument_list|(
name|conf
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Check dead session get cleared
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|end
decl_stmt|;
do|do
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|end
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
if|if
condition|(
name|end
operator|-
name|start
operator|>
literal|5000
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"timeout, scratch dir has not been cleared"
argument_list|)
expr_stmt|;
block|}
name|scratchDirs
operator|=
name|m_dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|scratchDir
argument_list|,
name|Utils
operator|.
name|getUGI
argument_list|()
operator|.
name|getShortUserName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|scratchDirs
operator|.
name|length
operator|!=
literal|2
condition|)
do|;
block|}
block|}
end_class

end_unit

