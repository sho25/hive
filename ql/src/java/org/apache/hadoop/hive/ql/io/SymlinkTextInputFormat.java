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
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|InputStreamReader
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
name|HashMap
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
name|Map
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
name|FileSplit
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
name|InputFormat
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
name|JobConfigurable
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
name|mapred
operator|.
name|TextInputFormat
import|;
end_import

begin_comment
comment|/**  * Symlink file is a text file which contains a list of filename / dirname.  * This input method reads symlink files from specified job input paths and  * takes the files / directories specified in those symlink files as  * actual map-reduce input. The target input data should be in TextInputFormat.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|SymlinkTextInputFormat
extends|extends
name|SymbolicInputFormat
implements|implements
name|InputFormat
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|>
implements|,
name|JobConfigurable
implements|,
name|ContentSummaryInputFormat
implements|,
name|ReworkMapredInputFormat
block|{
comment|/**    * This input split wraps the FileSplit generated from    * TextInputFormat.getSplits(), while setting the original link file path    * as job input path. This is needed because MapOperator relies on the    * job input path to lookup correct child operators. The target data file    * is encapsulated in the wrapped FileSplit.    */
specifier|public
specifier|static
class|class
name|SymlinkTextInputSplit
extends|extends
name|FileSplit
block|{
specifier|private
specifier|final
name|FileSplit
name|split
decl_stmt|;
specifier|public
name|SymlinkTextInputSplit
parameter_list|()
block|{
name|super
argument_list|(
operator|(
name|Path
operator|)
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|split
operator|=
operator|new
name|FileSplit
argument_list|(
operator|(
name|Path
operator|)
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SymlinkTextInputSplit
parameter_list|(
name|Path
name|symlinkPath
parameter_list|,
name|FileSplit
name|split
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|symlinkPath
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|split
operator|.
name|getLocations
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
block|}
comment|/**      * Gets the target split, i.e. the split of target data.      */
specifier|public
name|FileSplit
name|getTargetSplit
parameter_list|()
block|{
return|return
name|split
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|split
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|split
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|InputSplit
name|targetSplit
init|=
operator|(
operator|(
name|SymlinkTextInputSplit
operator|)
name|split
operator|)
operator|.
name|getTargetSplit
argument_list|()
decl_stmt|;
comment|// The target data is in TextInputFormat.
name|TextInputFormat
name|inputFormat
init|=
operator|new
name|TextInputFormat
argument_list|()
decl_stmt|;
name|inputFormat
operator|.
name|configure
argument_list|(
name|job
argument_list|)
expr_stmt|;
return|return
name|inputFormat
operator|.
name|getRecordReader
argument_list|(
name|targetSplit
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
return|;
block|}
comment|/**    * Parses all target paths from job input directory which contains symlink    * files, and splits the target data using TextInputFormat.    */
annotation|@
name|Override
specifier|public
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
index|[]
name|symlinksDirs
init|=
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|symlinksDirs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No input paths specified in job."
argument_list|)
throw|;
block|}
comment|// Get all target paths first, because the number of total target paths
comment|// is used to determine number of splits of each target path.
name|List
argument_list|<
name|Path
argument_list|>
name|targetPaths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|symlinkPaths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|getTargetPathsFromSymlinksDirs
argument_list|(
name|job
argument_list|,
name|symlinksDirs
argument_list|,
name|targetPaths
argument_list|,
name|symlinkPaths
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error parsing symlinks from specified job input path."
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|targetPaths
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|InputSplit
index|[
literal|0
index|]
return|;
block|}
comment|// The input should be in TextInputFormat.
name|TextInputFormat
name|inputFormat
init|=
operator|new
name|TextInputFormat
argument_list|()
decl_stmt|;
name|JobConf
name|newjob
init|=
operator|new
name|JobConf
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|newjob
operator|.
name|setInputFormat
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|inputFormat
operator|.
name|configure
argument_list|(
name|newjob
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
argument_list|()
decl_stmt|;
comment|// ceil(numSplits / numPaths), so we can get at least numSplits splits.
name|int
name|numPaths
init|=
name|targetPaths
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|numSubSplits
init|=
operator|(
name|numSplits
operator|+
name|numPaths
operator|-
literal|1
operator|)
operator|/
name|numPaths
decl_stmt|;
comment|// For each path, do getSplits().
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numPaths
condition|;
operator|++
name|i
control|)
block|{
name|Path
name|targetPath
init|=
name|targetPaths
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Path
name|symlinkPath
init|=
name|symlinkPaths
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|newjob
argument_list|,
name|targetPath
argument_list|)
expr_stmt|;
name|InputSplit
index|[]
name|iss
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|newjob
argument_list|,
name|numSubSplits
argument_list|)
decl_stmt|;
for|for
control|(
name|InputSplit
name|is
range|:
name|iss
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|SymlinkTextInputSplit
argument_list|(
name|symlinkPath
argument_list|,
operator|(
name|FileSplit
operator|)
name|is
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
operator|.
name|toArray
argument_list|(
operator|new
name|InputSplit
index|[
name|result
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
comment|// empty
block|}
comment|/**    * Given list of directories containing symlink files, read all target    * paths from symlink files and return as targetPaths list. And for each    * targetPaths[i], symlinkPaths[i] will be the path to the symlink file    * containing the target path.    */
specifier|private
specifier|static
name|void
name|getTargetPathsFromSymlinksDirs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
index|[]
name|symlinksDirs
parameter_list|,
name|List
argument_list|<
name|Path
argument_list|>
name|targetPaths
parameter_list|,
name|List
argument_list|<
name|Path
argument_list|>
name|symlinkPaths
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Path
name|symlinkDir
range|:
name|symlinksDirs
control|)
block|{
name|FileSystem
name|fileSystem
init|=
name|symlinkDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|symlinks
init|=
name|fileSystem
operator|.
name|listStatus
argument_list|(
name|symlinkDir
argument_list|)
decl_stmt|;
comment|// Read paths from each symlink file.
for|for
control|(
name|FileStatus
name|symlink
range|:
name|symlinks
control|)
block|{
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|fileSystem
operator|.
name|open
argument_list|(
name|symlink
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|targetPaths
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
name|line
argument_list|)
argument_list|)
expr_stmt|;
name|symlinkPaths
operator|.
name|add
argument_list|(
name|symlink
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * For backward compatibility with hadoop 0.17.    */
specifier|public
name|void
name|validateInput
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|// do nothing
block|}
annotation|@
name|Override
specifier|public
name|ContentSummary
name|getContentSummary
parameter_list|(
name|Path
name|p
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|//length, file count, directory count
name|long
index|[]
name|summary
init|=
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|targetPaths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|symlinkPaths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|getTargetPathsFromSymlinksDirs
argument_list|(
name|job
argument_list|,
operator|new
name|Path
index|[]
block|{
name|p
block|}
argument_list|,
name|targetPaths
argument_list|,
name|symlinkPaths
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error parsing symlinks from specified job input path."
argument_list|,
name|e
argument_list|)
throw|;
block|}
for|for
control|(
name|Path
name|path
range|:
name|targetPaths
control|)
block|{
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|ContentSummary
name|cs
init|=
name|fs
operator|.
name|getContentSummary
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|summary
index|[
literal|0
index|]
operator|+=
name|cs
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|summary
index|[
literal|1
index|]
operator|+=
name|cs
operator|.
name|getFileCount
argument_list|()
expr_stmt|;
name|summary
index|[
literal|2
index|]
operator|+=
name|cs
operator|.
name|getDirectoryCount
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|ContentSummary
argument_list|(
name|summary
index|[
literal|0
index|]
argument_list|,
name|summary
index|[
literal|1
index|]
argument_list|,
name|summary
index|[
literal|2
index|]
argument_list|)
return|;
block|}
block|}
end_class

end_unit

