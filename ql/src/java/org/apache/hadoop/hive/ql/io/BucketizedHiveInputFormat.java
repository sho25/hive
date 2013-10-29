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
name|List
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
name|common
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
name|Writable
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
name|WritableComparable
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
name|InvalidInputException
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

begin_comment
comment|/**  * BucketizedHiveInputFormat serves the similar function as hiveInputFormat but  * its getSplits() always group splits from one input file into one wrapper  * split. It is useful for the applications that requires input files to fit in  * one mapper.  */
end_comment

begin_class
specifier|public
class|class
name|BucketizedHiveInputFormat
parameter_list|<
name|K
extends|extends
name|WritableComparable
parameter_list|,
name|V
extends|extends
name|Writable
parameter_list|>
extends|extends
name|HiveInputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"org.apache.hadoop.hive.ql.io.BucketizedHiveInputFormat"
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|RecordReader
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
name|BucketizedHiveInputSplit
name|hsplit
init|=
operator|(
name|BucketizedHiveInputSplit
operator|)
name|split
decl_stmt|;
name|String
name|inputFormatClassName
init|=
literal|null
decl_stmt|;
name|Class
name|inputFormatClass
init|=
literal|null
decl_stmt|;
try|try
block|{
name|inputFormatClassName
operator|=
name|hsplit
operator|.
name|inputFormatClassName
argument_list|()
expr_stmt|;
name|inputFormatClass
operator|=
name|job
operator|.
name|getClassByName
argument_list|(
name|inputFormatClassName
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
literal|"cannot find class "
operator|+
name|inputFormatClassName
argument_list|)
throw|;
block|}
name|pushProjectionsAndFilters
argument_list|(
name|job
argument_list|,
name|inputFormatClass
argument_list|,
name|hsplit
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|hsplit
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|InputFormat
name|inputFormat
init|=
name|getInputFormatFromCache
argument_list|(
name|inputFormatClass
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|BucketizedHiveRecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|rr
init|=
operator|new
name|BucketizedHiveRecordReader
argument_list|(
name|inputFormat
argument_list|,
name|hsplit
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
name|rr
operator|.
name|initIOContext
argument_list|(
name|hsplit
argument_list|,
name|job
argument_list|,
name|inputFormatClass
argument_list|)
expr_stmt|;
return|return
name|rr
return|;
block|}
comment|/**    * Recursively lists status for all files starting from the directory dir    * @param job    * @param dir    * @return    * @throws IOException    */
specifier|protected
name|FileStatus
index|[]
name|listStatus
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|IOException
argument_list|>
name|errors
init|=
operator|new
name|ArrayList
argument_list|<
name|IOException
argument_list|>
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dir
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|matches
init|=
name|fs
operator|.
name|globStatus
argument_list|(
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|matches
operator|==
literal|null
condition|)
block|{
name|errors
operator|.
name|add
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Input path does not exist: "
operator|+
name|dir
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|matches
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|errors
operator|.
name|add
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Input Pattern "
operator|+
name|dir
operator|+
literal|" matches 0 files"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|FileStatus
name|globStat
range|:
name|matches
control|)
block|{
name|FileUtils
operator|.
name|listStatusRecursively
argument_list|(
name|fs
argument_list|,
name|globStat
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|errors
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InvalidInputException
argument_list|(
name|errors
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Total input paths to process : "
operator|+
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|toArray
argument_list|(
operator|new
name|FileStatus
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
name|init
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|Path
index|[]
name|dirs
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
name|dirs
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
literal|"No input paths specified in job"
argument_list|)
throw|;
block|}
name|JobConf
name|newjob
init|=
operator|new
name|JobConf
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|ArrayList
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
name|int
name|numOrigSplits
init|=
literal|0
decl_stmt|;
comment|// for each dir, get all files under the dir, do getSplits to each
comment|// individual file,
comment|// and then create a BucketizedHiveInputSplit on it
for|for
control|(
name|Path
name|dir
range|:
name|dirs
control|)
block|{
name|PartitionDesc
name|part
init|=
name|getPartitionDescFromPath
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|dir
argument_list|)
decl_stmt|;
comment|// create a new InputFormat instance if this is the first time to see this
comment|// class
name|Class
name|inputFormatClass
init|=
name|part
operator|.
name|getInputFileFormatClass
argument_list|()
decl_stmt|;
name|InputFormat
name|inputFormat
init|=
name|getInputFormatFromCache
argument_list|(
name|inputFormatClass
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|newjob
operator|.
name|setInputFormat
argument_list|(
name|inputFormat
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|listStatus
init|=
name|listStatus
argument_list|(
name|newjob
argument_list|,
name|dir
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|listStatus
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"block size: "
operator|+
name|status
operator|.
name|getBlockSize
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"file length: "
operator|+
name|status
operator|.
name|getLen
argument_list|()
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|newjob
argument_list|,
name|status
operator|.
name|getPath
argument_list|()
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
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|iss
operator|!=
literal|null
operator|&&
name|iss
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|numOrigSplits
operator|+=
name|iss
operator|.
name|length
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
operator|new
name|BucketizedHiveInputSplit
argument_list|(
name|iss
argument_list|,
name|inputFormatClass
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
name|result
operator|.
name|size
argument_list|()
operator|+
literal|" bucketized splits generated from "
operator|+
name|numOrigSplits
operator|+
literal|" original splits."
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|toArray
argument_list|(
operator|new
name|BucketizedHiveInputSplit
index|[
name|result
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
block|}
end_class

end_unit

