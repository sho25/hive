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
name|ql
operator|.
name|io
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
name|OutputStreamWriter
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
name|LinkedHashMap
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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|ContentSummary
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
name|Context
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
name|Driver
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
name|QueryPlan
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
name|exec
operator|.
name|Utilities
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
name|exec
operator|.
name|mr
operator|.
name|ExecDriver
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
name|exec
operator|.
name|mr
operator|.
name|MapRedTask
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
name|plan
operator|.
name|MapredWork
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
name|plan
operator|.
name|PartitionDesc
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
name|plan
operator|.
name|TableDesc
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
name|io
operator|.
name|LongWritable
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
name|io
operator|.
name|Text
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
name|mapred
operator|.
name|FileInputFormat
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
name|mapred
operator|.
name|InputSplit
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|RecordReader
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
name|mapred
operator|.
name|Reporter
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
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Unittest for SymlinkTextInputFormat.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|TestSymlinkTextInputFormat
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
name|Log
name|log
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestSymlinkTextInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|JobConf
name|job
decl_stmt|;
specifier|private
name|FileSystem
name|fileSystem
decl_stmt|;
specifier|private
name|Path
name|testDir
decl_stmt|;
name|Reporter
name|reporter
decl_stmt|;
specifier|private
name|Path
name|dataDir1
decl_stmt|;
specifier|private
name|Path
name|dataDir2
decl_stmt|;
specifier|private
name|Path
name|symlinkDir
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|job
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TableDesc
name|tblDesc
init|=
name|Utilities
operator|.
name|defaultTd
decl_stmt|;
name|PartitionDesc
name|partDesc
init|=
operator|new
name|PartitionDesc
argument_list|(
name|tblDesc
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pt
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
decl_stmt|;
name|pt
operator|.
name|put
argument_list|(
literal|"/tmp/testfolder"
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
name|MapredWork
name|mrwork
init|=
operator|new
name|MapredWork
argument_list|()
decl_stmt|;
name|mrwork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setPathToPartitionInfo
argument_list|(
name|pt
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapRedWork
argument_list|(
name|job
argument_list|,
name|mrwork
argument_list|,
literal|"/tmp/"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
operator|+
literal|"/hive"
argument_list|)
expr_stmt|;
name|fileSystem
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|testDir
operator|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|,
operator|new
name|File
argument_list|(
literal|"."
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|)
operator|+
literal|"/TestSymlinkTextInputFormat"
argument_list|)
expr_stmt|;
name|reporter
operator|=
name|Reporter
operator|.
name|NULL
expr_stmt|;
name|fileSystem
operator|.
name|delete
argument_list|(
name|testDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|dataDir1
operator|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
literal|"datadir1"
argument_list|)
expr_stmt|;
name|dataDir2
operator|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
literal|"datadir2"
argument_list|)
expr_stmt|;
name|symlinkDir
operator|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
literal|"symlinkdir"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|fileSystem
operator|.
name|delete
argument_list|(
name|testDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test combine symlink text input file. Two input dir, and each contails one    * file, and then create one symlink file containing these 2 files. Normally    * without combine, it will return at least 2 splits    */
specifier|public
name|void
name|testCombine
parameter_list|()
throws|throws
name|Exception
block|{
name|JobConf
name|newJob
init|=
operator|new
name|JobConf
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dataDir1
operator|.
name|getFileSystem
argument_list|(
name|newJob
argument_list|)
decl_stmt|;
name|int
name|symbolLinkedFileSize
init|=
literal|0
decl_stmt|;
name|Path
name|dir1_file1
init|=
operator|new
name|Path
argument_list|(
name|dataDir1
argument_list|,
literal|"combinefile1_1"
argument_list|)
decl_stmt|;
name|writeTextFile
argument_list|(
name|dir1_file1
argument_list|,
literal|"dir1_file1_line1\n"
operator|+
literal|"dir1_file1_line2\n"
argument_list|)
expr_stmt|;
name|symbolLinkedFileSize
operator|+=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dir1_file1
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|Path
name|dir2_file1
init|=
operator|new
name|Path
argument_list|(
name|dataDir2
argument_list|,
literal|"combinefile2_1"
argument_list|)
decl_stmt|;
name|writeTextFile
argument_list|(
name|dir2_file1
argument_list|,
literal|"dir2_file1_line1\n"
operator|+
literal|"dir2_file1_line2\n"
argument_list|)
expr_stmt|;
name|symbolLinkedFileSize
operator|+=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dir2_file1
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
comment|// A symlink file, contains first file from first dir and second file from
comment|// second dir.
name|writeSymlinkFile
argument_list|(
operator|new
name|Path
argument_list|(
name|symlinkDir
argument_list|,
literal|"symlink_file"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|dataDir1
argument_list|,
literal|"combinefile1_1"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|dataDir2
argument_list|,
literal|"combinefile2_1"
argument_list|)
argument_list|)
expr_stmt|;
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|TestSymlinkTextInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_REWORK_MAPREDWORK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Driver
name|drv
init|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|drv
operator|.
name|init
argument_list|()
expr_stmt|;
name|String
name|tblName
init|=
literal|"text_symlink_text"
decl_stmt|;
name|String
name|createSymlinkTableCmd
init|=
literal|"create table "
operator|+
name|tblName
operator|+
literal|" (key int) stored as "
operator|+
literal|" inputformat 'org.apache.hadoop.hive.ql.io.SymlinkTextInputFormat' "
operator|+
literal|" outputformat 'org.apache.hadoop.hive.ql.io.IgnoreKeyTextOutputFormat'"
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|boolean
name|tblCreated
init|=
literal|false
decl_stmt|;
try|try
block|{
name|int
name|ecode
init|=
literal|0
decl_stmt|;
name|ecode
operator|=
name|drv
operator|.
name|run
argument_list|(
name|createSymlinkTableCmd
argument_list|)
operator|.
name|getResponseCode
argument_list|()
expr_stmt|;
if|if
condition|(
name|ecode
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Create table command: "
operator|+
name|createSymlinkTableCmd
operator|+
literal|" failed with exit code= "
operator|+
name|ecode
argument_list|)
throw|;
block|}
name|tblCreated
operator|=
literal|true
expr_stmt|;
name|String
name|loadFileCommand
init|=
literal|"LOAD DATA LOCAL INPATH '"
operator|+
operator|new
name|Path
argument_list|(
name|symlinkDir
argument_list|,
literal|"symlink_file"
argument_list|)
operator|.
name|toString
argument_list|()
operator|+
literal|"' INTO TABLE "
operator|+
name|tblName
decl_stmt|;
name|ecode
operator|=
name|drv
operator|.
name|run
argument_list|(
name|loadFileCommand
argument_list|)
operator|.
name|getResponseCode
argument_list|()
expr_stmt|;
if|if
condition|(
name|ecode
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Load data command: "
operator|+
name|loadFileCommand
operator|+
literal|" failed with exit code= "
operator|+
name|ecode
argument_list|)
throw|;
block|}
name|String
name|cmd
init|=
literal|"select key from "
operator|+
name|tblName
decl_stmt|;
name|drv
operator|.
name|compile
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
comment|//create scratch dir
name|String
name|emptyScratchDirStr
decl_stmt|;
name|Path
name|emptyScratchDir
decl_stmt|;
name|Context
name|ctx
init|=
operator|new
name|Context
argument_list|(
name|newJob
argument_list|)
decl_stmt|;
name|emptyScratchDirStr
operator|=
name|ctx
operator|.
name|getMRTmpFileURI
argument_list|()
expr_stmt|;
name|emptyScratchDir
operator|=
operator|new
name|Path
argument_list|(
name|emptyScratchDirStr
argument_list|)
expr_stmt|;
name|FileSystem
name|fileSys
init|=
name|emptyScratchDir
operator|.
name|getFileSystem
argument_list|(
name|newJob
argument_list|)
decl_stmt|;
name|fileSys
operator|.
name|mkdirs
argument_list|(
name|emptyScratchDir
argument_list|)
expr_stmt|;
name|QueryPlan
name|plan
init|=
name|drv
operator|.
name|getPlan
argument_list|()
decl_stmt|;
name|MapRedTask
name|selectTask
init|=
operator|(
name|MapRedTask
operator|)
name|plan
operator|.
name|getRootTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|inputPaths
init|=
name|Utilities
operator|.
name|getInputPaths
argument_list|(
name|newJob
argument_list|,
name|selectTask
operator|.
name|getWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
argument_list|,
name|emptyScratchDir
operator|.
name|toString
argument_list|()
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|setInputPaths
argument_list|(
name|newJob
argument_list|,
name|inputPaths
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapRedWork
argument_list|(
name|newJob
argument_list|,
name|selectTask
operator|.
name|getWork
argument_list|()
argument_list|,
name|ctx
operator|.
name|getMRTmpFileURI
argument_list|()
argument_list|)
expr_stmt|;
name|CombineHiveInputFormat
name|combineInputFormat
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|CombineHiveInputFormat
operator|.
name|class
argument_list|,
name|newJob
argument_list|)
decl_stmt|;
name|InputSplit
index|[]
name|retSplits
init|=
name|combineInputFormat
operator|.
name|getSplits
argument_list|(
name|newJob
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|retSplits
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Caught exception "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|tblCreated
condition|)
block|{
name|drv
operator|.
name|run
argument_list|(
literal|"drop table text_symlink_text"
argument_list|)
operator|.
name|getResponseCode
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Test scenario: Two data directories, one symlink file that contains two    * paths each point to a file in one of data directories.    */
specifier|public
name|void
name|testAccuracy1
parameter_list|()
throws|throws
name|IOException
block|{
comment|// First data dir, contains 2 files.
name|FileSystem
name|fs
init|=
name|dataDir1
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|int
name|symbolLinkedFileSize
init|=
literal|0
decl_stmt|;
name|Path
name|dir1_file1
init|=
operator|new
name|Path
argument_list|(
name|dataDir1
argument_list|,
literal|"file1"
argument_list|)
decl_stmt|;
name|writeTextFile
argument_list|(
name|dir1_file1
argument_list|,
literal|"dir1_file1_line1\n"
operator|+
literal|"dir1_file1_line2\n"
argument_list|)
expr_stmt|;
name|symbolLinkedFileSize
operator|+=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dir1_file1
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|Path
name|dir1_file2
init|=
operator|new
name|Path
argument_list|(
name|dataDir1
argument_list|,
literal|"file2"
argument_list|)
decl_stmt|;
name|writeTextFile
argument_list|(
name|dir1_file2
argument_list|,
literal|"dir1_file2_line1\n"
operator|+
literal|"dir1_file2_line2\n"
argument_list|)
expr_stmt|;
comment|// Second data dir, contains 2 files.
name|Path
name|dir2_file1
init|=
operator|new
name|Path
argument_list|(
name|dataDir2
argument_list|,
literal|"file1"
argument_list|)
decl_stmt|;
name|writeTextFile
argument_list|(
name|dir2_file1
argument_list|,
literal|"dir2_file1_line1\n"
operator|+
literal|"dir2_file1_line2\n"
argument_list|)
expr_stmt|;
name|Path
name|dir2_file2
init|=
operator|new
name|Path
argument_list|(
name|dataDir2
argument_list|,
literal|"file2"
argument_list|)
decl_stmt|;
name|writeTextFile
argument_list|(
name|dir2_file2
argument_list|,
literal|"dir2_file2_line1\n"
operator|+
literal|"dir2_file2_line2\n"
argument_list|)
expr_stmt|;
name|symbolLinkedFileSize
operator|+=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dir2_file2
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
comment|// A symlink file, contains first file from first dir and second file from
comment|// second dir.
name|writeSymlinkFile
argument_list|(
operator|new
name|Path
argument_list|(
name|symlinkDir
argument_list|,
literal|"symlink_file"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|dataDir1
argument_list|,
literal|"file1"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|dataDir2
argument_list|,
literal|"file2"
argument_list|)
argument_list|)
expr_stmt|;
name|SymlinkTextInputFormat
name|inputFormat
init|=
operator|new
name|SymlinkTextInputFormat
argument_list|()
decl_stmt|;
comment|//test content summary
name|ContentSummary
name|cs
init|=
name|inputFormat
operator|.
name|getContentSummary
argument_list|(
name|symlinkDir
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|symbolLinkedFileSize
argument_list|,
name|cs
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cs
operator|.
name|getFileCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cs
operator|.
name|getDirectoryCount
argument_list|()
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|symlinkDir
argument_list|)
expr_stmt|;
name|InputSplit
index|[]
name|splits
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|log
operator|.
name|info
argument_list|(
literal|"Number of splits: "
operator|+
name|splits
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Read all values.
name|List
argument_list|<
name|String
argument_list|>
name|received
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|InputSplit
name|split
range|:
name|splits
control|)
block|{
name|RecordReader
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|>
name|reader
init|=
name|inputFormat
operator|.
name|getRecordReader
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
name|LongWritable
name|key
init|=
name|reader
operator|.
name|createKey
argument_list|()
decl_stmt|;
name|Text
name|value
init|=
name|reader
operator|.
name|createValue
argument_list|()
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|received
operator|.
name|add
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
literal|"dir1_file1_line1"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
literal|"dir1_file1_line2"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
literal|"dir2_file2_line1"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
literal|"dir2_file2_line2"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|received
argument_list|)
expr_stmt|;
block|}
comment|/**    * Scenario: Empty input directory, i.e. no symlink file.    *    * Expected: Should return empty result set without any exception.    */
specifier|public
name|void
name|testAccuracy2
parameter_list|()
throws|throws
name|IOException
block|{
name|fileSystem
operator|.
name|mkdirs
argument_list|(
name|symlinkDir
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|symlinkDir
argument_list|)
expr_stmt|;
name|SymlinkTextInputFormat
name|inputFormat
init|=
operator|new
name|SymlinkTextInputFormat
argument_list|()
decl_stmt|;
name|ContentSummary
name|cs
init|=
name|inputFormat
operator|.
name|getContentSummary
argument_list|(
name|symlinkDir
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cs
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cs
operator|.
name|getFileCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cs
operator|.
name|getDirectoryCount
argument_list|()
argument_list|)
expr_stmt|;
name|InputSplit
index|[]
name|splits
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|log
operator|.
name|info
argument_list|(
literal|"Number of splits: "
operator|+
name|splits
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Read all values.
name|List
argument_list|<
name|String
argument_list|>
name|received
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|InputSplit
name|split
range|:
name|splits
control|)
block|{
name|RecordReader
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|>
name|reader
init|=
name|inputFormat
operator|.
name|getRecordReader
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
name|LongWritable
name|key
init|=
name|reader
operator|.
name|createKey
argument_list|()
decl_stmt|;
name|Text
name|value
init|=
name|reader
operator|.
name|createValue
argument_list|()
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|received
operator|.
name|add
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|received
argument_list|)
expr_stmt|;
block|}
comment|/**    * Scenario: No job input paths.    * Expected: IOException with proper message.    */
specifier|public
name|void
name|testFailure
parameter_list|()
block|{
name|SymlinkTextInputFormat
name|inputFormat
init|=
operator|new
name|SymlinkTextInputFormat
argument_list|()
decl_stmt|;
try|try
block|{
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"IOException expected if no job input paths specified."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Incorrect exception message for no job input paths error."
argument_list|,
literal|"No input paths specified in job."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Writes the given string to the given file.    */
specifier|private
name|void
name|writeTextFile
parameter_list|(
name|Path
name|file
parameter_list|,
name|String
name|content
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputStreamWriter
name|writer
init|=
operator|new
name|OutputStreamWriter
argument_list|(
name|fileSystem
operator|.
name|create
argument_list|(
name|file
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|content
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Writes a symlink file that contains given list of paths.    *    * @param symlinkFile    * The symlink file to write.    *    * @param paths    * The list of paths to write to the symlink file.    */
specifier|private
name|void
name|writeSymlinkFile
parameter_list|(
name|Path
name|symlinkFile
parameter_list|,
name|Path
modifier|...
name|paths
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputStreamWriter
name|writer
init|=
operator|new
name|OutputStreamWriter
argument_list|(
name|fileSystem
operator|.
name|create
argument_list|(
name|symlinkFile
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|paths
control|)
block|{
name|writer
operator|.
name|write
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

