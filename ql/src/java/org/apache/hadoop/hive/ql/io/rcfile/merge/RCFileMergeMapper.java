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
operator|.
name|rcfile
operator|.
name|merge
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
name|io
operator|.
name|RCFile
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
name|metadata
operator|.
name|HiveException
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
name|DynamicPartitionCtx
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
operator|.
name|LogHelper
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
name|CombineHiveKey
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
name|compress
operator|.
name|CompressionCodec
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
name|MapReduceBase
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
name|Mapper
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
name|OutputCollector
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

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|RCFileMergeMapper
extends|extends
name|MapReduceBase
implements|implements
name|Mapper
argument_list|<
name|Object
argument_list|,
name|RCFileValueBufferWrapper
argument_list|,
name|Object
argument_list|,
name|Object
argument_list|>
block|{
specifier|private
name|JobConf
name|jc
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|outputClass
decl_stmt|;
name|RCFile
operator|.
name|Writer
name|outWriter
decl_stmt|;
name|Path
name|finalPath
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
name|boolean
name|exception
init|=
literal|false
decl_stmt|;
name|boolean
name|autoDelete
init|=
literal|false
decl_stmt|;
name|Path
name|outPath
decl_stmt|;
name|CompressionCodec
name|codec
init|=
literal|null
decl_stmt|;
name|int
name|columnNumber
init|=
literal|0
decl_stmt|;
name|boolean
name|hasDynamicPartitions
init|=
literal|false
decl_stmt|;
name|boolean
name|isListBucketingDML
init|=
literal|false
decl_stmt|;
name|boolean
name|isListBucketingAlterTableConcatenate
init|=
literal|false
decl_stmt|;
name|int
name|listBucketingDepth
decl_stmt|;
comment|// used as depth for dir-calculation and if it is list bucketing case.
name|boolean
name|tmpPathFixedConcatenate
init|=
literal|false
decl_stmt|;
name|boolean
name|tmpPathFixed
init|=
literal|false
decl_stmt|;
name|Path
name|tmpPath
decl_stmt|;
name|Path
name|taskTmpPath
decl_stmt|;
name|Path
name|dpPath
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"RCFileMergeMapper"
argument_list|)
decl_stmt|;
specifier|public
name|RCFileMergeMapper
parameter_list|()
block|{   }
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
name|jc
operator|=
name|job
expr_stmt|;
name|hasDynamicPartitions
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGECURRENTJOBHASDYNAMICPARTITIONS
argument_list|)
expr_stmt|;
name|isListBucketingAlterTableConcatenate
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGECURRENTJOBCONCATENATELISTBUCKETING
argument_list|)
expr_stmt|;
name|listBucketingDepth
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGECURRENTJOBCONCATENATELISTBUCKETINGDEPTH
argument_list|)
expr_stmt|;
name|String
name|specPath
init|=
name|RCFileBlockMergeOutputFormat
operator|.
name|getMergeOutputPath
argument_list|(
name|job
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Path
name|tmpPath
init|=
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
name|Path
name|taskTmpPath
init|=
name|Utilities
operator|.
name|toTaskTempPath
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
name|updatePaths
argument_list|(
name|tmpPath
argument_list|,
name|taskTmpPath
argument_list|)
expr_stmt|;
try|try
block|{
name|fs
operator|=
operator|(
operator|new
name|Path
argument_list|(
name|specPath
argument_list|)
operator|)
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|autoDelete
operator|=
name|fs
operator|.
name|deleteOnExit
argument_list|(
name|outPath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|this
operator|.
name|exception
operator|=
literal|true
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|updatePaths
parameter_list|(
name|Path
name|tmpPath
parameter_list|,
name|Path
name|taskTmpPath
parameter_list|)
block|{
name|String
name|taskId
init|=
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|jc
argument_list|)
decl_stmt|;
name|this
operator|.
name|tmpPath
operator|=
name|tmpPath
expr_stmt|;
name|this
operator|.
name|taskTmpPath
operator|=
name|taskTmpPath
expr_stmt|;
name|finalPath
operator|=
operator|new
name|Path
argument_list|(
name|tmpPath
argument_list|,
name|taskId
argument_list|)
expr_stmt|;
name|outPath
operator|=
operator|new
name|Path
argument_list|(
name|taskTmpPath
argument_list|,
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|taskId
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|Object
name|k
parameter_list|,
name|RCFileValueBufferWrapper
name|value
parameter_list|,
name|OutputCollector
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|RCFileKeyBufferWrapper
name|key
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|k
operator|instanceof
name|CombineHiveKey
condition|)
block|{
name|key
operator|=
call|(
name|RCFileKeyBufferWrapper
call|)
argument_list|(
operator|(
name|CombineHiveKey
operator|)
name|k
argument_list|)
operator|.
name|getKey
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|key
operator|=
operator|(
name|RCFileKeyBufferWrapper
operator|)
name|k
expr_stmt|;
block|}
comment|/**        * 1. boolean isListBucketingAlterTableConcatenate will be true only if it is alter table ...        * concatenate on stored-as-dir so it will handle list bucketing alter table merge in the if        * cause with the help of fixTmpPathConcatenate        * 2. If it is DML, isListBucketingAlterTableConcatenate will be false so that it will be        * handled by else cause. In this else cause, we have another if check.        * 2.1 the if check will make sure DP or LB, we will fix path with the help of fixTmpPath(..).        * Since both has sub-directories. it includes SP + LB.        * 2.2 only SP without LB, we dont fix path.        */
comment|// Fix temp path for alter table ... concatenate
if|if
condition|(
name|isListBucketingAlterTableConcatenate
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|tmpPathFixedConcatenate
condition|)
block|{
name|checkPartitionsMatch
argument_list|(
name|key
operator|.
name|inputPath
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fixTmpPathConcatenate
argument_list|(
name|key
operator|.
name|inputPath
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
name|tmpPathFixedConcatenate
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|hasDynamicPartitions
operator|||
operator|(
name|listBucketingDepth
operator|>
literal|0
operator|)
condition|)
block|{
if|if
condition|(
name|tmpPathFixed
condition|)
block|{
name|checkPartitionsMatch
argument_list|(
name|key
operator|.
name|inputPath
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We haven't fixed the TMP path for this mapper yet
name|fixTmpPath
argument_list|(
name|key
operator|.
name|inputPath
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
name|tmpPathFixed
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|outWriter
operator|==
literal|null
condition|)
block|{
name|codec
operator|=
name|key
operator|.
name|codec
expr_stmt|;
name|columnNumber
operator|=
name|key
operator|.
name|keyBuffer
operator|.
name|getColumnNumber
argument_list|()
expr_stmt|;
name|jc
operator|.
name|setInt
argument_list|(
name|RCFile
operator|.
name|COLUMN_NUMBER_CONF_STR
argument_list|,
name|columnNumber
argument_list|)
expr_stmt|;
name|outWriter
operator|=
operator|new
name|RCFile
operator|.
name|Writer
argument_list|(
name|fs
argument_list|,
name|jc
argument_list|,
name|outPath
argument_list|,
literal|null
argument_list|,
name|codec
argument_list|)
expr_stmt|;
block|}
name|boolean
name|sameCodec
init|=
operator|(
operator|(
name|codec
operator|==
name|key
operator|.
name|codec
operator|)
operator|||
name|codec
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|key
operator|.
name|codec
operator|.
name|getClass
argument_list|()
argument_list|)
operator|)
decl_stmt|;
if|if
condition|(
operator|(
name|key
operator|.
name|keyBuffer
operator|.
name|getColumnNumber
argument_list|()
operator|!=
name|columnNumber
operator|)
operator|||
operator|(
operator|!
name|sameCodec
operator|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"RCFileMerge failed because the input files use different CompressionCodec or have different column number setting."
argument_list|)
throw|;
block|}
name|outWriter
operator|.
name|flushBlock
argument_list|(
name|key
operator|.
name|keyBuffer
argument_list|,
name|value
operator|.
name|valueBuffer
argument_list|,
name|key
operator|.
name|recordLength
argument_list|,
name|key
operator|.
name|keyLength
argument_list|,
name|key
operator|.
name|compressedKeyLength
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|this
operator|.
name|exception
operator|=
literal|true
expr_stmt|;
name|close
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Validates that each input path belongs to the same partition    * since each mapper merges the input to a single output directory    *    * @param inputPath    * @throws HiveException    */
specifier|private
name|void
name|checkPartitionsMatch
parameter_list|(
name|Path
name|inputPath
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|dpPath
operator|.
name|equals
argument_list|(
name|inputPath
argument_list|)
condition|)
block|{
comment|// Temp partition input path does not match exist temp path
name|String
name|msg
init|=
literal|"Multiple partitions for one block merge mapper: "
operator|+
name|dpPath
operator|+
literal|" NOT EQUAL TO "
operator|+
name|inputPath
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
comment|/**    * Fixes tmpPath to point to the correct partition.    * Before this is called, tmpPath will default to the root tmp table dir    * fixTmpPath(..) works for DP + LB + multiple skewed values + merge. reason:    * 1. fixTmpPath(..) compares inputPath and tmpDepth, find out path difference and put it into    * newPath. Then add newpath to existing this.tmpPath and this.taskTmpPath.    * 2. The path difference between inputPath and tmpDepth can be DP or DP+LB. It will automatically    * handle it.    * 3. For example,    * if inputpath is<prefix>/-ext-10002/hr=a1/HIVE_DEFAULT_LIST_BUCKETING_DIR_NAME/    * HIVE_DEFAULT_LIST_BUCKETING_DIR_NAME    * tmppath is<prefix>/_tmp.-ext-10000    * newpath will be hr=a1/HIVE_DEFAULT_LIST_BUCKETING_DIR_NAME/HIVE_DEFAULT_LIST_BUCKETING_DIR_NAME    * Then, this.tmpPath and this.taskTmpPath will be update correctly.    * We have list_bucket_dml_6.q cover this case: DP + LP + multiple skewed values + merge.    * @param inputPath    * @throws HiveException    * @throws IOException    */
specifier|private
name|void
name|fixTmpPath
parameter_list|(
name|Path
name|inputPath
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|dpPath
operator|=
name|inputPath
expr_stmt|;
name|Path
name|newPath
init|=
operator|new
name|Path
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
name|int
name|inputDepth
init|=
name|inputPath
operator|.
name|depth
argument_list|()
decl_stmt|;
name|int
name|tmpDepth
init|=
name|tmpPath
operator|.
name|depth
argument_list|()
decl_stmt|;
comment|// Build the path from bottom up
while|while
condition|(
name|inputPath
operator|!=
literal|null
operator|&&
name|inputPath
operator|.
name|depth
argument_list|()
operator|>
name|tmpDepth
condition|)
block|{
name|newPath
operator|=
operator|new
name|Path
argument_list|(
name|inputPath
operator|.
name|getName
argument_list|()
argument_list|,
name|newPath
argument_list|)
expr_stmt|;
name|inputDepth
operator|--
expr_stmt|;
name|inputPath
operator|=
name|inputPath
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
name|Path
name|newTmpPath
init|=
operator|new
name|Path
argument_list|(
name|tmpPath
argument_list|,
name|newPath
argument_list|)
decl_stmt|;
name|Path
name|newTaskTmpPath
init|=
operator|new
name|Path
argument_list|(
name|taskTmpPath
argument_list|,
name|newPath
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|newTmpPath
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|newTmpPath
argument_list|)
expr_stmt|;
block|}
name|updatePaths
argument_list|(
name|newTmpPath
argument_list|,
name|newTaskTmpPath
argument_list|)
expr_stmt|;
block|}
comment|/**    * Fixes tmpPath to point to the correct list bucketing sub-directories.    * Before this is called, tmpPath will default to the root tmp table dir    * Reason to add a new method instead of changing fixTmpPath()    * Reason 1: logic has slightly difference    * fixTmpPath(..) needs 2 variables in order to decide path delta which is in variable newPath.    * 1. inputPath.depth()    * 2. tmpPath.depth()    * fixTmpPathConcatenate needs 2 variables too but one of them is different from fixTmpPath(..)    * 1. inputPath.depth()    * 2. listBucketingDepth    * Reason 2: less risks    * The existing logic is a little not trivial around map() and fixTmpPath(). In order to ensure    * minimum impact on existing flow, we try to avoid change on existing code/flow but add new code    * for new feature.    *    * @param inputPath    * @throws HiveException    * @throws IOException    */
specifier|private
name|void
name|fixTmpPathConcatenate
parameter_list|(
name|Path
name|inputPath
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|dpPath
operator|=
name|inputPath
expr_stmt|;
name|Path
name|newPath
init|=
operator|new
name|Path
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
name|int
name|depth
init|=
name|listBucketingDepth
decl_stmt|;
comment|// Build the path from bottom up. pick up list bucketing subdirectories
while|while
condition|(
operator|(
name|inputPath
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|depth
operator|>
literal|0
operator|)
condition|)
block|{
name|newPath
operator|=
operator|new
name|Path
argument_list|(
name|inputPath
operator|.
name|getName
argument_list|()
argument_list|,
name|newPath
argument_list|)
expr_stmt|;
name|inputPath
operator|=
name|inputPath
operator|.
name|getParent
argument_list|()
expr_stmt|;
name|depth
operator|--
expr_stmt|;
block|}
name|Path
name|newTmpPath
init|=
operator|new
name|Path
argument_list|(
name|tmpPath
argument_list|,
name|newPath
argument_list|)
decl_stmt|;
name|Path
name|newTaskTmpPath
init|=
operator|new
name|Path
argument_list|(
name|taskTmpPath
argument_list|,
name|newPath
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|newTmpPath
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|newTmpPath
argument_list|)
expr_stmt|;
block|}
name|updatePaths
argument_list|(
name|newTmpPath
argument_list|,
name|newTaskTmpPath
argument_list|)
expr_stmt|;
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
comment|// close writer
if|if
condition|(
name|outWriter
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|outWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|outWriter
operator|=
literal|null
expr_stmt|;
if|if
condition|(
operator|!
name|exception
condition|)
block|{
name|FileStatus
name|fss
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|outPath
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"renamed path "
operator|+
name|outPath
operator|+
literal|" to "
operator|+
name|finalPath
operator|+
literal|" . File size is "
operator|+
name|fss
operator|.
name|getLen
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|outPath
argument_list|,
name|finalPath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to rename output to "
operator|+
name|finalPath
argument_list|)
throw|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|autoDelete
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|outPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|String
name|BACKUP_PREFIX
init|=
literal|"_backup."
decl_stmt|;
specifier|public
specifier|static
name|Path
name|backupOutputPath
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|outpath
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|outpath
argument_list|)
condition|)
block|{
name|Path
name|backupPath
init|=
operator|new
name|Path
argument_list|(
name|outpath
operator|.
name|getParent
argument_list|()
argument_list|,
name|BACKUP_PREFIX
operator|+
name|outpath
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|rename
argument_list|(
name|fs
argument_list|,
name|outpath
argument_list|,
name|backupPath
argument_list|)
expr_stmt|;
return|return
name|backupPath
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|void
name|jobClose
parameter_list|(
name|String
name|outputPath
parameter_list|,
name|boolean
name|success
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|LogHelper
name|console
parameter_list|,
name|DynamicPartitionCtx
name|dynPartCtx
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|Path
name|outpath
init|=
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|outpath
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|Path
name|backupPath
init|=
name|backupOutputPath
argument_list|(
name|fs
argument_list|,
name|outpath
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|mvFileToFinalPath
argument_list|(
name|outputPath
argument_list|,
name|job
argument_list|,
name|success
argument_list|,
name|LOG
argument_list|,
name|dynPartCtx
argument_list|,
literal|null
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|backupPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

