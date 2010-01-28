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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|Map
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
name|hive
operator|.
name|shims
operator|.
name|HadoopShims
operator|.
name|CombineFileInputFormatShim
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
operator|.
name|InputSplitShim
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

begin_comment
comment|/**  * CombineHiveInputFormat is a parameterized InputFormat which looks at the path  * name and determine the correct InputFormat for that path name from  * mapredPlan.pathToPartitionInfo(). It can be used to read files with different  * input format in the same map-reduce job.  */
end_comment

begin_class
specifier|public
class|class
name|CombineHiveInputFormat
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
literal|"org.apache.hadoop.hive.ql.io.CombineHiveInputFormat"
argument_list|)
decl_stmt|;
comment|/**    * CombineHiveInputSplit encapsulates an InputSplit with its corresponding    * inputFormatClassName. A CombineHiveInputSplit comprises of multiple chunks    * from different files. Since, they belong to a single directory, there is a    * single inputformat for all the chunks.    */
specifier|public
specifier|static
class|class
name|CombineHiveInputSplit
implements|implements
name|InputSplitShim
block|{
name|String
name|inputFormatClassName
decl_stmt|;
name|InputSplitShim
name|inputSplitShim
decl_stmt|;
specifier|public
name|CombineHiveInputSplit
parameter_list|()
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getCombineFileInputFormat
argument_list|()
operator|.
name|getInputSplitShim
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CombineHiveInputSplit
parameter_list|(
name|InputSplitShim
name|inputSplitShim
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|inputSplitShim
operator|.
name|getJob
argument_list|()
argument_list|,
name|inputSplitShim
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CombineHiveInputSplit
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|InputSplitShim
name|inputSplitShim
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|inputSplitShim
operator|=
name|inputSplitShim
expr_stmt|;
if|if
condition|(
name|job
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
init|=
name|Utilities
operator|.
name|getMapRedWork
argument_list|(
name|job
argument_list|)
operator|.
name|getPathToPartitionInfo
argument_list|()
decl_stmt|;
comment|// extract all the inputFormatClass names for each chunk in the
comment|// CombinedSplit.
name|Path
index|[]
name|ipaths
init|=
name|inputSplitShim
operator|.
name|getPaths
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
name|ipaths
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|PartitionDesc
name|part
init|=
literal|null
decl_stmt|;
try|try
block|{
name|part
operator|=
name|getPartitionDescFromPath
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|ipaths
index|[
name|i
index|]
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// The file path may be present in case of sampling - so ignore that
name|part
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|part
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|part
operator|=
name|getPartitionDescFromPath
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|ipaths
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"CombineHiveInputSplit unable to find table description for "
operator|+
name|ipaths
index|[
name|i
index|]
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
comment|// create a new InputFormat instance if this is the first time to see
comment|// this class
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|inputFormatClassName
operator|=
name|part
operator|.
name|getInputFileFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|inputFormatClassName
operator|.
name|equals
argument_list|(
name|part
operator|.
name|getInputFileFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
assert|;
block|}
block|}
block|}
block|}
specifier|public
name|InputSplitShim
name|getInputSplitShim
parameter_list|()
block|{
return|return
name|inputSplitShim
return|;
block|}
comment|/**      * Returns the inputFormat class name for the i-th chunk      */
specifier|public
name|String
name|inputFormatClassName
parameter_list|()
block|{
return|return
name|inputFormatClassName
return|;
block|}
specifier|public
name|void
name|setInputFormatClassName
parameter_list|(
name|String
name|inputFormatClassName
parameter_list|)
block|{
name|this
operator|.
name|inputFormatClassName
operator|=
name|inputFormatClassName
expr_stmt|;
block|}
specifier|public
name|JobConf
name|getJob
parameter_list|()
block|{
return|return
name|inputSplitShim
operator|.
name|getJob
argument_list|()
return|;
block|}
specifier|public
name|long
name|getLength
parameter_list|()
block|{
return|return
name|inputSplitShim
operator|.
name|getLength
argument_list|()
return|;
block|}
comment|/** Returns an array containing the startoffsets of the files in the split */
specifier|public
name|long
index|[]
name|getStartOffsets
parameter_list|()
block|{
return|return
name|inputSplitShim
operator|.
name|getStartOffsets
argument_list|()
return|;
block|}
comment|/** Returns an array containing the lengths of the files in the split */
specifier|public
name|long
index|[]
name|getLengths
parameter_list|()
block|{
return|return
name|inputSplitShim
operator|.
name|getLengths
argument_list|()
return|;
block|}
comment|/** Returns the start offset of the i<sup>th</sup> Path */
specifier|public
name|long
name|getOffset
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|inputSplitShim
operator|.
name|getOffset
argument_list|(
name|i
argument_list|)
return|;
block|}
comment|/** Returns the length of the i<sup>th</sup> Path */
specifier|public
name|long
name|getLength
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|inputSplitShim
operator|.
name|getLength
argument_list|(
name|i
argument_list|)
return|;
block|}
comment|/** Returns the number of Paths in the split */
specifier|public
name|int
name|getNumPaths
parameter_list|()
block|{
return|return
name|inputSplitShim
operator|.
name|getNumPaths
argument_list|()
return|;
block|}
comment|/** Returns the i<sup>th</sup> Path */
specifier|public
name|Path
name|getPath
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|inputSplitShim
operator|.
name|getPath
argument_list|(
name|i
argument_list|)
return|;
block|}
comment|/** Returns all the Paths in the split */
specifier|public
name|Path
index|[]
name|getPaths
parameter_list|()
block|{
return|return
name|inputSplitShim
operator|.
name|getPaths
argument_list|()
return|;
block|}
comment|/** Returns all the Paths where this input-split resides */
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|inputSplitShim
operator|.
name|getLocations
argument_list|()
return|;
block|}
comment|/**      * Prints this obejct as a string.      */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|inputSplitShim
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"InputFormatClass: "
operator|+
name|inputFormatClassName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * Writable interface      */
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
name|inputSplitShim
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|inputFormatClassName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
comment|/**      * Writable interface      */
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
name|inputSplitShim
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|inputFormatClassName
operator|==
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
init|=
name|Utilities
operator|.
name|getMapRedWork
argument_list|(
name|getJob
argument_list|()
argument_list|)
operator|.
name|getPathToPartitionInfo
argument_list|()
decl_stmt|;
comment|// extract all the inputFormatClass names for each chunk in the
comment|// CombinedSplit.
name|PartitionDesc
name|part
init|=
literal|null
decl_stmt|;
try|try
block|{
name|part
operator|=
name|getPartitionDescFromPath
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|inputSplitShim
operator|.
name|getPath
argument_list|(
literal|0
argument_list|)
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// The file path may be present in case of sampling - so ignore that
name|part
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|part
operator|==
literal|null
condition|)
block|{
name|part
operator|=
name|getPartitionDescFromPath
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|inputSplitShim
operator|.
name|getPath
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// create a new InputFormat instance if this is the first time to see
comment|// this class
name|inputFormatClassName
operator|=
name|part
operator|.
name|getInputFileFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|out
operator|.
name|writeUTF
argument_list|(
name|inputFormatClassName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Create Hive splits based on CombineFileSplit    */
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
name|CombineFileInputFormatShim
name|combine
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getCombineFileInputFormat
argument_list|()
decl_stmt|;
if|if
condition|(
name|combine
operator|.
name|getInputPathsShim
argument_list|(
name|job
argument_list|)
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
comment|// combine splits only from same tables. Do not combine splits from multiple
comment|// tables.
name|Path
index|[]
name|paths
init|=
name|combine
operator|.
name|getInputPathsShim
argument_list|(
name|job
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
name|LOG
operator|.
name|info
argument_list|(
literal|"CombineHiveInputSplit creating pool for "
operator|+
name|path
argument_list|)
expr_stmt|;
name|combine
operator|.
name|createPool
argument_list|(
name|job
argument_list|,
operator|new
name|CombineFilter
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|InputSplitShim
index|[]
name|iss
init|=
name|combine
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|InputSplitShim
name|is
range|:
name|iss
control|)
block|{
name|CombineHiveInputSplit
name|csplit
init|=
operator|new
name|CombineHiveInputSplit
argument_list|(
name|job
argument_list|,
name|is
argument_list|)
decl_stmt|;
name|result
operator|.
name|add
argument_list|(
name|csplit
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"number of splits "
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
name|CombineHiveInputSplit
index|[
name|result
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|/**    * Create a generic Hive RecordReader than can iterate over all chunks in a    * CombinedFileSplit    */
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
name|CombineHiveInputSplit
name|hsplit
init|=
operator|(
name|CombineHiveInputSplit
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
name|initColumnsNeeded
argument_list|(
name|job
argument_list|,
name|inputFormatClass
argument_list|,
name|hsplit
operator|.
name|getPath
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|hsplit
operator|.
name|getPath
argument_list|(
literal|0
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getCombineFileInputFormat
argument_list|()
operator|.
name|getRecordReader
argument_list|(
name|job
argument_list|,
operator|(
operator|(
name|CombineHiveInputSplit
operator|)
name|split
operator|)
operator|.
name|getInputSplitShim
argument_list|()
argument_list|,
name|reporter
argument_list|,
name|CombineHiveRecordReader
operator|.
name|class
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|PartitionDesc
name|getPartitionDescFromPath
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|,
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
comment|// The format of the keys in pathToPartitionInfo sometimes contains a port
comment|// and sometimes doesn't, so we just compare paths.
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|entry
range|:
name|pathToPartitionInfo
operator|.
name|entrySet
argument_list|()
control|)
block|{
try|try
block|{
if|if
condition|(
operator|new
name|URI
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|getPath
argument_list|()
operator|.
name|equals
argument_list|(
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|entry
operator|.
name|getValue
argument_list|()
return|;
block|}
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e2
parameter_list|)
block|{       }
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"cannot find dir = "
operator|+
name|dir
operator|.
name|toString
argument_list|()
operator|+
literal|" in partToPartitionInfo!"
argument_list|)
throw|;
block|}
specifier|static
class|class
name|CombineFilter
implements|implements
name|PathFilter
block|{
specifier|private
specifier|final
name|String
name|pString
decl_stmt|;
comment|// store a path prefix in this TestFilter
specifier|public
name|CombineFilter
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|pString
operator|=
name|p
operator|.
name|toString
argument_list|()
operator|+
name|File
operator|.
name|separator
expr_stmt|;
block|}
comment|// returns true if the specified path matches the prefix stored
comment|// in this TestFilter.
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|.
name|toString
argument_list|()
operator|.
name|indexOf
argument_list|(
name|pString
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"PathFilter:"
operator|+
name|pString
return|;
block|}
block|}
block|}
end_class

end_unit

