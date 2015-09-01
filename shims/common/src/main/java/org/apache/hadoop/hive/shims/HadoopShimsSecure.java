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
name|EOFException
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
name|InputStream
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessControlException
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
name|Collections
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
name|Set
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
name|lang
operator|.
name|ArrayUtils
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
name|DefaultFileAccess
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
name|FsShell
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
name|fs
operator|.
name|permission
operator|.
name|FsAction
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
name|io
operator|.
name|HiveIOExceptionHandlerUtil
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
name|ClusterStatus
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
name|mapreduce
operator|.
name|Job
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
name|Credentials
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
name|Progressable
import|;
end_import

begin_comment
comment|/**  * Base implemention for shims against secure Hadoop 0.20.3/0.23.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HadoopShimsSecure
implements|implements
name|HadoopShims
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HadoopShimsSecure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
class|class
name|InputSplitShim
extends|extends
name|CombineFileSplit
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
name|JobConf
name|conf
parameter_list|,
name|Path
index|[]
name|paths
parameter_list|,
name|long
index|[]
name|startOffsets
parameter_list|,
name|long
index|[]
name|lengths
parameter_list|,
name|String
index|[]
name|locations
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|paths
argument_list|,
name|startOffsets
argument_list|,
name|lengths
argument_list|,
name|dedup
argument_list|(
name|locations
argument_list|)
argument_list|)
expr_stmt|;
name|_isShrinked
operator|=
literal|false
expr_stmt|;
block|}
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
annotation|@
name|Override
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
name|doNextWithExceptionHandler
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
annotation|@
name|Override
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
annotation|@
name|Override
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
annotation|@
name|Override
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
annotation|@
name|Override
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
annotation|@
name|Override
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
comment|/**      * do next and handle exception inside it.      * @param key      * @param value      * @return      * @throws IOException      */
specifier|private
name|boolean
name|doNextWithExceptionHandler
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
try|try
block|{
return|return
name|curReader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|HiveIOExceptionHandlerUtil
operator|.
name|handleRecordReaderNextException
argument_list|(
name|e
argument_list|,
name|jc
argument_list|)
return|;
block|}
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
comment|// if all chunks have been processed, nothing more to do.
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
name|curReader
operator|=
name|HiveIOExceptionHandlerUtil
operator|.
name|handleRecordReaderCreationException
argument_list|(
name|e
argument_list|,
name|jc
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
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
name|CombineFileSplit
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
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHadoopConfNames
argument_list|()
operator|.
name|get
argument_list|(
literal|"MAPREDMINSPLITSIZE"
argument_list|)
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
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHadoopConfNames
argument_list|()
operator|.
name|get
argument_list|(
literal|"MAPREDMINSPLITSIZEPERNODE"
argument_list|)
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
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHadoopConfNames
argument_list|()
operator|.
name|get
argument_list|(
literal|"MAPREDMINSPLITSIZEPERRACK"
argument_list|)
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
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHadoopConfNames
argument_list|()
operator|.
name|get
argument_list|(
literal|"MAPREDMAXSPLITSIZE"
argument_list|)
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
name|InputSplit
index|[]
name|splits
init|=
name|super
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
name|numSplits
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|InputSplitShim
argument_list|>
name|inputSplitShims
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSplitShim
argument_list|>
argument_list|()
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
name|CombineFileSplit
name|split
init|=
operator|(
name|CombineFileSplit
operator|)
name|splits
index|[
name|pos
index|]
decl_stmt|;
if|if
condition|(
name|split
operator|.
name|getPaths
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|inputSplitShims
operator|.
name|add
argument_list|(
operator|new
name|InputSplitShim
argument_list|(
name|job
argument_list|,
name|split
operator|.
name|getPaths
argument_list|()
argument_list|,
name|split
operator|.
name|getStartOffsets
argument_list|()
argument_list|,
name|split
operator|.
name|getLengths
argument_list|()
argument_list|,
name|split
operator|.
name|getLocations
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|inputSplitShims
operator|.
name|toArray
argument_list|(
operator|new
name|InputSplitShim
index|[
name|inputSplitShims
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
annotation|@
name|Override
specifier|public
name|RecordReader
name|getRecordReader
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
name|CombineFileSplit
name|cfSplit
init|=
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
annotation|@
name|Override
specifier|abstract
specifier|public
name|JobTrackerState
name|getJobTrackerState
parameter_list|(
name|ClusterStatus
name|clusterStatus
parameter_list|)
throws|throws
name|Exception
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptContext
name|newTaskAttemptContext
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Progressable
name|progressable
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobContext
name|newJobContext
parameter_list|(
name|Job
name|job
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|boolean
name|isLocalMode
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|void
name|setJobLauncherRpcAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|val
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|String
name|getJobLauncherHttpAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|String
name|getJobLauncherRpcAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|short
name|getDefaultReplication
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|long
name|getDefaultBlockSize
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|boolean
name|moveToAppropriateTrash
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|FileSystem
name|createProxyFileSystem
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|URI
name|uri
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|abstract
specifier|public
name|FileSystem
name|getNonCachedFileSystem
parameter_list|(
name|URI
name|uri
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
name|void
name|run
parameter_list|(
name|FsShell
name|shell
parameter_list|,
name|String
index|[]
name|command
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|ArrayUtils
operator|.
name|toString
argument_list|(
name|command
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|retval
init|=
name|shell
operator|.
name|run
argument_list|(
name|command
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Return value is :"
operator|+
name|retval
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
index|[]
name|dedup
parameter_list|(
name|String
index|[]
name|locations
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|dedup
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|dedup
argument_list|,
name|locations
argument_list|)
expr_stmt|;
return|return
name|dedup
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|dedup
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
name|checkFileAccess
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileStatus
name|stat
parameter_list|,
name|FsAction
name|action
parameter_list|)
throws|throws
name|IOException
throws|,
name|AccessControlException
throws|,
name|Exception
block|{
name|DefaultFileAccess
operator|.
name|checkFileAccess
argument_list|(
name|fs
argument_list|,
name|stat
argument_list|,
name|action
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|abstract
specifier|public
name|void
name|addDelegationTokens
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Credentials
name|cred
parameter_list|,
name|String
name|uname
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|private
specifier|final
class|class
name|BasicTextReaderShim
implements|implements
name|TextReaderShim
block|{
specifier|private
specifier|final
name|InputStream
name|in
decl_stmt|;
specifier|public
name|BasicTextReaderShim
parameter_list|(
name|InputStream
name|in
parameter_list|)
block|{
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|read
parameter_list|(
name|Text
name|txt
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|offset
init|=
literal|0
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
while|while
condition|(
name|len
operator|>
literal|0
condition|)
block|{
name|int
name|written
init|=
name|in
operator|.
name|read
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
decl_stmt|;
if|if
condition|(
name|written
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Can't finish read from "
operator|+
name|in
operator|+
literal|" read "
operator|+
operator|(
name|offset
operator|)
operator|+
literal|" bytes out of "
operator|+
name|bytes
operator|.
name|length
argument_list|)
throw|;
block|}
name|len
operator|-=
name|written
expr_stmt|;
name|offset
operator|+=
name|written
expr_stmt|;
block|}
name|txt
operator|.
name|set
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|TextReaderShim
name|getTextReaderShim
parameter_list|(
name|InputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|BasicTextReaderShim
argument_list|(
name|in
argument_list|)
return|;
block|}
block|}
end_class

end_unit

