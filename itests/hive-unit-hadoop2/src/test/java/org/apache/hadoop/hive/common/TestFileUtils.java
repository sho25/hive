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
name|common
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
name|io
operator|.
name|OutputStream
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
name|shims
operator|.
name|HadoopShims
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
name|ShimLoader
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
name|FileSystem
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

begin_comment
comment|/**  * Integration tests for {{@link FileUtils}. Tests run against a {@link HadoopShims.MiniDFSShim}.  */
end_comment

begin_class
specifier|public
class|class
name|TestFileUtils
block|{
specifier|private
specifier|static
specifier|final
name|Path
name|basePath
init|=
operator|new
name|Path
argument_list|(
literal|"/tmp/"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|HadoopShims
operator|.
name|MiniDFSShim
name|dfs
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|TestFileUtils
operator|.
name|class
argument_list|)
expr_stmt|;
name|dfs
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getMiniDfs
argument_list|(
name|conf
argument_list|,
literal|4
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fs
operator|=
name|dfs
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCopySingleEmptyFile
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|file1Name
init|=
literal|"file1.txt"
decl_stmt|;
name|Path
name|copySrc
init|=
operator|new
name|Path
argument_list|(
name|basePath
argument_list|,
literal|"copySrc"
argument_list|)
decl_stmt|;
name|Path
name|copyDst
init|=
operator|new
name|Path
argument_list|(
name|basePath
argument_list|,
literal|"copyDst"
argument_list|)
decl_stmt|;
try|try
block|{
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|basePath
argument_list|,
operator|new
name|Path
argument_list|(
name|copySrc
argument_list|,
name|file1Name
argument_list|)
argument_list|)
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"FileUtils.copy failed to copy data"
argument_list|,
name|FileUtils
operator|.
name|copy
argument_list|(
name|fs
argument_list|,
name|copySrc
argument_list|,
name|fs
argument_list|,
name|copyDst
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|dstFileName1
init|=
operator|new
name|Path
argument_list|(
name|copyDst
argument_list|,
name|file1Name
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|copyDst
argument_list|,
name|file1Name
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dstFileName1
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|copySrc
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|copyDst
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Do nothing
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCopyWithDistcp
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|file1Name
init|=
literal|"file1.txt"
decl_stmt|;
name|String
name|file2Name
init|=
literal|"file2.txt"
decl_stmt|;
name|Path
name|copySrc
init|=
operator|new
name|Path
argument_list|(
name|basePath
argument_list|,
literal|"copySrc"
argument_list|)
decl_stmt|;
name|Path
name|copyDst
init|=
operator|new
name|Path
argument_list|(
name|basePath
argument_list|,
literal|"copyDst"
argument_list|)
decl_stmt|;
name|Path
name|srcFile1
init|=
operator|new
name|Path
argument_list|(
name|basePath
argument_list|,
operator|new
name|Path
argument_list|(
name|copySrc
argument_list|,
name|file1Name
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|srcFile2
init|=
operator|new
name|Path
argument_list|(
name|basePath
argument_list|,
operator|new
name|Path
argument_list|(
name|copySrc
argument_list|,
name|file2Name
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|OutputStream
name|os1
init|=
name|fs
operator|.
name|create
argument_list|(
name|srcFile1
argument_list|)
decl_stmt|;
name|os1
operator|.
name|write
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|)
expr_stmt|;
name|os1
operator|.
name|close
argument_list|()
expr_stmt|;
name|OutputStream
name|os2
init|=
name|fs
operator|.
name|create
argument_list|(
name|srcFile2
argument_list|)
decl_stmt|;
name|os2
operator|.
name|write
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|)
expr_stmt|;
name|os2
operator|.
name|close
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
name|HIVE_EXEC_COPYFILE_MAXNUMFILES
operator|.
name|varname
argument_list|,
literal|"1"
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
name|HIVE_EXEC_COPYFILE_MAXSIZE
operator|.
name|varname
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"FileUtils.copy failed to copy data"
argument_list|,
name|FileUtils
operator|.
name|copy
argument_list|(
name|fs
argument_list|,
name|copySrc
argument_list|,
name|fs
argument_list|,
name|copyDst
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|dstFileName1
init|=
operator|new
name|Path
argument_list|(
name|copyDst
argument_list|,
name|file1Name
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|copyDst
argument_list|,
name|file1Name
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dstFileName1
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|Path
name|dstFileName2
init|=
operator|new
name|Path
argument_list|(
name|copyDst
argument_list|,
name|file2Name
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|copyDst
argument_list|,
name|file2Name
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dstFileName2
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|copySrc
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|copyDst
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Do nothing
block|}
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
name|dfs
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

