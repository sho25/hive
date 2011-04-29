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
name|shims
package|;
end_package

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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|fs
operator|.
name|PathFilter
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
name|JobContext
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
name|JobStatus
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
name|OutputCommitter
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
name|RunningJob
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
name|TaskAttemptContext
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
name|TaskCompletionEvent
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
name|TaskID
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
name|lib
operator|.
name|CombineFileInputFormat
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
name|lib
operator|.
name|CombineFileSplit
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
name|lib
operator|.
name|NullOutputFormat
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
name|security
operator|.
name|UnixUserGroupInformation
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
name|security
operator|.
name|UserGroupInformation
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
name|tools
operator|.
name|HadoopArchives
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
name|ToolRunner
import|;
end_import

begin_comment
comment|/**  * Implemention of shims against Hadoop 0.20.0.  */
end_comment

begin_class
specifier|public
class|class
name|Hadoop20Shims
implements|implements
name|HadoopShims
block|{
specifier|public
name|boolean
name|usesJobShell
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|fileSystemDeleteOnExit
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|deleteOnExit
argument_list|(
name|path
argument_list|)
return|;
block|}
specifier|public
name|void
name|inputFormatValidateInput
parameter_list|(
name|InputFormat
name|fmt
parameter_list|,
name|JobConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// gone in 0.18+
block|}
specifier|public
name|boolean
name|isJobPreparing
parameter_list|(
name|RunningJob
name|job
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|job
operator|.
name|getJobState
argument_list|()
operator|==
name|JobStatus
operator|.
name|PREP
return|;
block|}
comment|/**    * Workaround for hadoop-17 - jobclient only looks at commandlineconfig.    */
specifier|public
name|void
name|setTmpFiles
parameter_list|(
name|String
name|prop
parameter_list|,
name|String
name|files
parameter_list|)
block|{
comment|// gone in 20+
block|}
specifier|public
name|HadoopShims
operator|.
name|MiniDFSShim
name|getMiniDfs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|numDataNodes
parameter_list|,
name|boolean
name|format
parameter_list|,
name|String
index|[]
name|racks
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MiniDFSShim
argument_list|(
operator|new
name|MiniDFSCluster
argument_list|(
name|conf
argument_list|,
name|numDataNodes
argument_list|,
name|format
argument_list|,
name|racks
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * MiniDFSShim.    *    */
specifier|public
class|class
name|MiniDFSShim
implements|implements
name|HadoopShims
operator|.
name|MiniDFSShim
block|{
specifier|private
specifier|final
name|MiniDFSCluster
name|cluster
decl_stmt|;
specifier|public
name|MiniDFSShim
parameter_list|(
name|MiniDFSCluster
name|cluster
parameter_list|)
block|{
name|this
operator|.
name|cluster
operator|=
name|cluster
expr_stmt|;
block|}
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|cluster
operator|.
name|getFileSystem
argument_list|()
return|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * We define this function here to make the code compatible between    * hadoop 0.17 and hadoop 0.20.    *    * Hive binary that compiled Text.compareTo(Text) with hadoop 0.20 won't    * work with hadoop 0.17 because in hadoop 0.20, Text.compareTo(Text) is    * implemented in org.apache.hadoop.io.BinaryComparable, and Java compiler    * references that class, which is not available in hadoop 0.17.    */
specifier|public
name|int
name|compareText
parameter_list|(
name|Text
name|a
parameter_list|,
name|Text
name|b
parameter_list|)
block|{
return|return
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getAccessTime
parameter_list|(
name|FileStatus
name|file
parameter_list|)
block|{
return|return
name|file
operator|.
name|getAccessTime
argument_list|()
return|;
block|}
specifier|public
name|HadoopShims
operator|.
name|CombineFileInputFormatShim
name|getCombineFileInputFormat
parameter_list|()
block|{
return|return
operator|new
name|CombineFileInputFormatShim
argument_list|()
block|{
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
throw|throw
operator|new
name|IOException
argument_list|(
literal|"CombineFileInputFormat.getRecordReader not needed."
argument_list|)
throw|;
block|}
block|}
return|;
block|}
specifier|public
specifier|static
class|class
name|InputSplitShim
extends|extends
name|CombineFileSplit
implements|implements
name|HadoopShims
operator|.
name|InputSplitShim
block|{
name|long
name|shrinkedLength
decl_stmt|;
name|boolean
name|_isShrinked
decl_stmt|;
specifier|public
name|InputSplitShim
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|_isShrinked
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|InputSplitShim
parameter_list|(
name|CombineFileSplit
name|old
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|old
argument_list|)
expr_stmt|;
name|_isShrinked
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shrinkSplit
parameter_list|(
name|long
name|length
parameter_list|)
block|{
name|_isShrinked
operator|=
literal|true
expr_stmt|;
name|shrinkedLength
operator|=
name|length
expr_stmt|;
block|}
specifier|public
name|boolean
name|isShrinked
parameter_list|()
block|{
return|return
name|_isShrinked
return|;
block|}
specifier|public
name|long
name|getShrinkedLength
parameter_list|()
block|{
return|return
name|shrinkedLength
return|;
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
name|_isShrinked
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
if|if
condition|(
name|_isShrinked
condition|)
block|{
name|shrinkedLength
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
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
name|out
operator|.
name|writeBoolean
argument_list|(
name|_isShrinked
argument_list|)
expr_stmt|;
if|if
condition|(
name|_isShrinked
condition|)
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|shrinkedLength
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/* This class should be replaced with org.apache.hadoop.mapred.lib.CombineFileRecordReader class, once    * https://issues.apache.org/jira/browse/MAPREDUCE-955 is fixed. This code should be removed - it is a copy    * of org.apache.hadoop.mapred.lib.CombineFileRecordReader    */
specifier|public
specifier|static
class|class
name|CombineFileRecordReader
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|static
specifier|final
name|Class
index|[]
name|constructorSignature
init|=
operator|new
name|Class
index|[]
block|{
name|InputSplit
operator|.
name|class
block|,
name|Configuration
operator|.
name|class
block|,
name|Reporter
operator|.
name|class
block|,
name|Integer
operator|.
name|class
block|}
decl_stmt|;
specifier|protected
name|CombineFileSplit
name|split
decl_stmt|;
specifier|protected
name|JobConf
name|jc
decl_stmt|;
specifier|protected
name|Reporter
name|reporter
decl_stmt|;
specifier|protected
name|Class
argument_list|<
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|rrClass
decl_stmt|;
specifier|protected
name|Constructor
argument_list|<
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|rrConstructor
decl_stmt|;
specifier|protected
name|FileSystem
name|fs
decl_stmt|;
specifier|protected
name|int
name|idx
decl_stmt|;
specifier|protected
name|long
name|progress
decl_stmt|;
specifier|protected
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|curReader
decl_stmt|;
specifier|protected
name|boolean
name|isShrinked
decl_stmt|;
specifier|protected
name|long
name|shrinkedLength
decl_stmt|;
specifier|public
name|boolean
name|next
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
operator|(
name|curReader
operator|==
literal|null
operator|)
operator|||
operator|!
name|curReader
operator|.
name|next
argument_list|(
call|(
name|K
call|)
argument_list|(
operator|(
name|CombineHiveKey
operator|)
name|key
argument_list|)
operator|.
name|getKey
argument_list|()
argument_list|,
name|value
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|initNextRecordReader
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|K
name|createKey
parameter_list|()
block|{
name|K
name|newKey
init|=
name|curReader
operator|.
name|createKey
argument_list|()
decl_stmt|;
return|return
call|(
name|K
call|)
argument_list|(
operator|new
name|CombineHiveKey
argument_list|(
name|newKey
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|V
name|createValue
parameter_list|()
block|{
return|return
name|curReader
operator|.
name|createValue
argument_list|()
return|;
block|}
comment|/**      * Return the amount of data processed.      */
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|progress
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|curReader
operator|!=
literal|null
condition|)
block|{
name|curReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|curReader
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**      * Return progress based on the amount of data processed so far.      */
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|Math
operator|.
name|min
argument_list|(
literal|1.0f
argument_list|,
name|progress
operator|/
call|(
name|float
call|)
argument_list|(
name|split
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * A generic RecordReader that can hand out different recordReaders      * for each chunk in the CombineFileSplit.      */
specifier|public
name|CombineFileRecordReader
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|CombineFileSplit
name|split
parameter_list|,
name|Reporter
name|reporter
parameter_list|,
name|Class
argument_list|<
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|rrClass
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
name|this
operator|.
name|jc
operator|=
name|job
expr_stmt|;
name|this
operator|.
name|rrClass
operator|=
name|rrClass
expr_stmt|;
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
name|this
operator|.
name|idx
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|curReader
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|progress
operator|=
literal|0
expr_stmt|;
name|isShrinked
operator|=
literal|false
expr_stmt|;
assert|assert
operator|(
name|split
operator|instanceof
name|InputSplitShim
operator|)
assert|;
if|if
condition|(
operator|(
operator|(
name|InputSplitShim
operator|)
name|split
operator|)
operator|.
name|isShrinked
argument_list|()
condition|)
block|{
name|isShrinked
operator|=
literal|true
expr_stmt|;
name|shrinkedLength
operator|=
operator|(
operator|(
name|InputSplitShim
operator|)
name|split
operator|)
operator|.
name|getShrinkedLength
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|rrConstructor
operator|=
name|rrClass
operator|.
name|getDeclaredConstructor
argument_list|(
name|constructorSignature
argument_list|)
expr_stmt|;
name|rrConstructor
operator|.
name|setAccessible
argument_list|(
literal|true
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
name|RuntimeException
argument_list|(
name|rrClass
operator|.
name|getName
argument_list|()
operator|+
literal|" does not have valid constructor"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|initNextRecordReader
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * Get the record reader for the next chunk in this CombineFileSplit.      */
specifier|protected
name|boolean
name|initNextRecordReader
parameter_list|(
name|K
name|key
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|curReader
operator|!=
literal|null
condition|)
block|{
name|curReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|curReader
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|idx
operator|>
literal|0
condition|)
block|{
name|progress
operator|+=
name|split
operator|.
name|getLength
argument_list|(
name|idx
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// done processing so far
block|}
block|}
comment|// if all chunks have been processed or reached the length, nothing more to do.
if|if
condition|(
name|idx
operator|==
name|split
operator|.
name|getNumPaths
argument_list|()
operator|||
operator|(
name|isShrinked
operator|&&
name|progress
operator|>
name|shrinkedLength
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// get a record reader for the idx-th chunk
try|try
block|{
name|curReader
operator|=
name|rrConstructor
operator|.
name|newInstance
argument_list|(
operator|new
name|Object
index|[]
block|{
name|split
block|,
name|jc
block|,
name|reporter
block|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|idx
argument_list|)
block|}
argument_list|)
expr_stmt|;
comment|// change the key if need be
if|if
condition|(
name|key
operator|!=
literal|null
condition|)
block|{
name|K
name|newKey
init|=
name|curReader
operator|.
name|createKey
argument_list|()
decl_stmt|;
operator|(
operator|(
name|CombineHiveKey
operator|)
name|key
operator|)
operator|.
name|setKey
argument_list|(
name|newKey
argument_list|)
expr_stmt|;
block|}
comment|// setup some helper config variables.
name|jc
operator|.
name|set
argument_list|(
literal|"map.input.file"
argument_list|,
name|split
operator|.
name|getPath
argument_list|(
name|idx
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|jc
operator|.
name|setLong
argument_list|(
literal|"map.input.start"
argument_list|,
name|split
operator|.
name|getOffset
argument_list|(
name|idx
argument_list|)
argument_list|)
expr_stmt|;
name|jc
operator|.
name|setLong
argument_list|(
literal|"map.input.length"
argument_list|,
name|split
operator|.
name|getLength
argument_list|(
name|idx
argument_list|)
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|idx
operator|++
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
specifier|public
specifier|abstract
specifier|static
class|class
name|CombineFileInputFormatShim
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
name|CombineFileInputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
implements|implements
name|HadoopShims
operator|.
name|CombineFileInputFormatShim
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|public
name|Path
index|[]
name|getInputPathsShim
parameter_list|(
name|JobConf
name|conf
parameter_list|)
block|{
try|try
block|{
return|return
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|conf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|createPool
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|PathFilter
modifier|...
name|filters
parameter_list|)
block|{
name|super
operator|.
name|createPool
argument_list|(
name|conf
argument_list|,
name|filters
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|InputSplitShim
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
name|long
name|minSize
init|=
name|job
operator|.
name|getLong
argument_list|(
literal|"mapred.min.split.size"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|// For backward compatibility, let the above parameter be used
if|if
condition|(
name|job
operator|.
name|getLong
argument_list|(
literal|"mapred.min.split.size.per.node"
argument_list|,
literal|0
argument_list|)
operator|==
literal|0
condition|)
block|{
name|super
operator|.
name|setMinSplitSizeNode
argument_list|(
name|minSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|job
operator|.
name|getLong
argument_list|(
literal|"mapred.min.split.size.per.rack"
argument_list|,
literal|0
argument_list|)
operator|==
literal|0
condition|)
block|{
name|super
operator|.
name|setMinSplitSizeRack
argument_list|(
name|minSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|job
operator|.
name|getLong
argument_list|(
literal|"mapred.max.split.size"
argument_list|,
literal|0
argument_list|)
operator|==
literal|0
condition|)
block|{
name|super
operator|.
name|setMaxSplitSize
argument_list|(
name|minSize
argument_list|)
expr_stmt|;
block|}
name|CombineFileSplit
index|[]
name|splits
init|=
operator|(
name|CombineFileSplit
index|[]
operator|)
name|super
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
name|numSplits
argument_list|)
decl_stmt|;
name|InputSplitShim
index|[]
name|isplits
init|=
operator|new
name|InputSplitShim
index|[
name|splits
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|splits
operator|.
name|length
condition|;
name|pos
operator|++
control|)
block|{
name|isplits
index|[
name|pos
index|]
operator|=
operator|new
name|InputSplitShim
argument_list|(
name|splits
index|[
name|pos
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|isplits
return|;
block|}
specifier|public
name|InputSplitShim
name|getInputSplitShim
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|InputSplitShim
argument_list|()
return|;
block|}
specifier|public
name|RecordReader
name|getRecordReader
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|HadoopShims
operator|.
name|InputSplitShim
name|split
parameter_list|,
name|Reporter
name|reporter
parameter_list|,
name|Class
argument_list|<
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|rrClass
parameter_list|)
throws|throws
name|IOException
block|{
name|CombineFileSplit
name|cfSplit
init|=
operator|(
name|CombineFileSplit
operator|)
name|split
decl_stmt|;
return|return
operator|new
name|CombineFileRecordReader
argument_list|(
name|job
argument_list|,
name|cfSplit
argument_list|,
name|reporter
argument_list|,
name|rrClass
argument_list|)
return|;
block|}
block|}
specifier|public
name|String
name|getInputFormatClassName
parameter_list|()
block|{
return|return
literal|"org.apache.hadoop.hive.ql.io.CombineHiveInputFormat"
return|;
block|}
name|String
index|[]
name|ret
init|=
operator|new
name|String
index|[
literal|2
index|]
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getTaskJobIDs
parameter_list|(
name|TaskCompletionEvent
name|t
parameter_list|)
block|{
name|TaskID
name|tid
init|=
name|t
operator|.
name|getTaskAttemptId
argument_list|()
operator|.
name|getTaskID
argument_list|()
decl_stmt|;
name|ret
index|[
literal|0
index|]
operator|=
name|tid
operator|.
name|toString
argument_list|()
expr_stmt|;
name|ret
index|[
literal|1
index|]
operator|=
name|tid
operator|.
name|getJobID
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
name|void
name|setFloatConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|varName
parameter_list|,
name|float
name|val
parameter_list|)
block|{
name|conf
operator|.
name|setFloat
argument_list|(
name|varName
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|createHadoopArchive
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
name|sourceDir
parameter_list|,
name|Path
name|destDir
parameter_list|,
name|String
name|archiveName
parameter_list|)
throws|throws
name|Exception
block|{
name|HadoopArchives
name|har
init|=
operator|new
name|HadoopArchives
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
literal|"hive.archive.har.parentdir.settable"
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"hive.archive.har.parentdir.settable is not set"
argument_list|)
throw|;
block|}
name|boolean
name|parentSettable
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hive.archive.har.parentdir.settable"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentSettable
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-archiveName"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|archiveName
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-p"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|sourceDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|destDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-archiveName"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|archiveName
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|sourceDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|destDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ToolRunner
operator|.
name|run
argument_list|(
name|har
argument_list|,
name|args
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|NullOutputCommitter
extends|extends
name|OutputCommitter
block|{
annotation|@
name|Override
specifier|public
name|void
name|setupJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|cleanupJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|setupTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|boolean
name|needsTaskCommit
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|abortTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
block|{ }
block|}
specifier|public
name|void
name|setNullOutputFormat
parameter_list|(
name|JobConf
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setOutputFormat
argument_list|(
name|NullOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setOutputCommitter
argument_list|(
name|Hadoop20Shims
operator|.
name|NullOutputCommitter
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// option to bypass job setup and cleanup was introduced in hadoop-21 (MAPREDUCE-463)
comment|// but can be backported. So we disable setup/cleanup in all versions>= 0.19
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"mapred.committer.job.setup.cleanup.needed"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// option to bypass task cleanup task was introduced in hadoop-23 (MAPREDUCE-2206)
comment|// but can be backported. So we disable setup/cleanup in all versions>= 0.19
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"mapreduce.job.committer.task.cleanup.needed"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|UserGroupInformation
name|getUGIForConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|LoginException
block|{
name|UserGroupInformation
name|ugi
init|=
name|UnixUserGroupInformation
operator|.
name|readFromConf
argument_list|(
name|conf
argument_list|,
name|UnixUserGroupInformation
operator|.
name|UGI_PROPERTY_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|ugi
operator|==
literal|null
condition|)
block|{
name|ugi
operator|=
name|UserGroupInformation
operator|.
name|login
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|ugi
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSecureShimImpl
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getShortUserName
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|)
block|{
return|return
name|ugi
operator|.
name|getUserName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTokenStrForm
parameter_list|(
name|String
name|tokenSignature
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Tokens are not supported in current hadoop version"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

