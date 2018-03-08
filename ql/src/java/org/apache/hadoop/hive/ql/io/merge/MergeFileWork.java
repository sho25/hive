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
name|io
operator|.
name|merge
package|;
end_package

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
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
name|HiveStatsUtils
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
name|io
operator|.
name|CombineHiveInputFormat
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
name|RCFileInputFormat
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
name|orc
operator|.
name|OrcFileStripeMergeInputFormat
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
name|orc
operator|.
name|OrcInputFormat
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
name|rcfile
operator|.
name|merge
operator|.
name|RCFileBlockMergeInputFormat
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
name|Explain
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
name|ListBucketingCtx
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
name|MapWork
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
name|plan
operator|.
name|Explain
operator|.
name|Level
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

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Merge File Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|MergeFileWork
extends|extends
name|MapWork
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MergeFileWork
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|inputPaths
decl_stmt|;
specifier|private
name|Path
name|outputDir
decl_stmt|;
specifier|private
name|boolean
name|hasDynamicPartitions
decl_stmt|;
specifier|private
name|boolean
name|isListBucketingAlterTableConcatenate
decl_stmt|;
specifier|private
name|ListBucketingCtx
name|listBucketingCtx
decl_stmt|;
comment|// source table input format
specifier|private
name|String
name|srcTblInputFormat
decl_stmt|;
comment|// internal input format used by CombineHiveInputFormat
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|internalInputFormat
decl_stmt|;
specifier|public
name|MergeFileWork
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|inputPaths
parameter_list|,
name|Path
name|outputDir
parameter_list|,
name|String
name|srcTblInputFormat
parameter_list|,
name|TableDesc
name|tbl
parameter_list|)
block|{
name|this
argument_list|(
name|inputPaths
argument_list|,
name|outputDir
argument_list|,
literal|false
argument_list|,
name|srcTblInputFormat
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MergeFileWork
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|inputPaths
parameter_list|,
name|Path
name|outputDir
parameter_list|,
name|boolean
name|hasDynamicPartitions
parameter_list|,
name|String
name|srcTblInputFormat
parameter_list|,
name|TableDesc
name|tbl
parameter_list|)
block|{
name|this
operator|.
name|inputPaths
operator|=
name|inputPaths
expr_stmt|;
name|this
operator|.
name|outputDir
operator|=
name|outputDir
expr_stmt|;
name|this
operator|.
name|hasDynamicPartitions
operator|=
name|hasDynamicPartitions
expr_stmt|;
name|this
operator|.
name|srcTblInputFormat
operator|=
name|srcTblInputFormat
expr_stmt|;
name|PartitionDesc
name|partDesc
init|=
operator|new
name|PartitionDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|srcTblInputFormat
operator|.
name|equals
argument_list|(
name|OrcInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|this
operator|.
name|internalInputFormat
operator|=
name|OrcFileStripeMergeInputFormat
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|srcTblInputFormat
operator|.
name|equals
argument_list|(
name|RCFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|this
operator|.
name|internalInputFormat
operator|=
name|RCFileBlockMergeInputFormat
operator|.
name|class
expr_stmt|;
block|}
name|partDesc
operator|.
name|setInputFileFormatClass
argument_list|(
name|internalInputFormat
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setTableDesc
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|this
operator|.
name|inputPaths
control|)
block|{
name|this
operator|.
name|addPathToPartitionInfo
argument_list|(
name|path
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|isListBucketingAlterTableConcatenate
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|getInputPaths
parameter_list|()
block|{
return|return
name|inputPaths
return|;
block|}
specifier|public
name|void
name|setInputPaths
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|inputPaths
parameter_list|)
block|{
name|this
operator|.
name|inputPaths
operator|=
name|inputPaths
expr_stmt|;
block|}
specifier|public
name|Path
name|getOutputDir
parameter_list|()
block|{
return|return
name|outputDir
return|;
block|}
specifier|public
name|void
name|setOutputDir
parameter_list|(
name|Path
name|outputDir
parameter_list|)
block|{
name|this
operator|.
name|outputDir
operator|=
name|outputDir
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|getMinSplitSize
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getInputformat
parameter_list|()
block|{
return|return
name|getInputformatClass
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|getInputformatClass
parameter_list|()
block|{
return|return
name|CombineHiveInputFormat
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isGatheringStats
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|hasDynamicPartitions
parameter_list|()
block|{
return|return
name|this
operator|.
name|hasDynamicPartitions
return|;
block|}
specifier|public
name|void
name|setHasDynamicPartitions
parameter_list|(
name|boolean
name|hasDynamicPartitions
parameter_list|)
block|{
name|this
operator|.
name|hasDynamicPartitions
operator|=
name|hasDynamicPartitions
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resolveDynamicPartitionStoredAsSubDirsMerge
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Path
name|path
parameter_list|,
name|TableDesc
name|tblDesc
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
parameter_list|,
name|PartitionDesc
name|partDesc
parameter_list|)
block|{
name|super
operator|.
name|resolveDynamicPartitionStoredAsSubDirsMerge
argument_list|(
name|conf
argument_list|,
name|path
argument_list|,
name|tblDesc
argument_list|,
name|aliases
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
comment|// set internal input format for all partition descriptors
name|partDesc
operator|.
name|setInputFileFormatClass
argument_list|(
name|internalInputFormat
argument_list|)
expr_stmt|;
comment|// Add the DP path to the list of input paths
name|inputPaths
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
comment|/**    * alter table ... concatenate    *<p/>    * If it is skewed table, use subdirectories in inputpaths.    */
specifier|public
name|void
name|resolveConcatenateMerge
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|isListBucketingAlterTableConcatenate
operator|=
operator|(
operator|(
name|listBucketingCtx
operator|==
literal|null
operator|)
condition|?
literal|false
else|:
name|listBucketingCtx
operator|.
name|isSkewedStoredAsDir
argument_list|()
operator|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"isListBucketingAlterTableConcatenate : "
operator|+
name|isListBucketingAlterTableConcatenate
argument_list|)
expr_stmt|;
if|if
condition|(
name|isListBucketingAlterTableConcatenate
condition|)
block|{
comment|// use sub-dir as inputpath.
assert|assert
operator|(
operator|(
name|this
operator|.
name|inputPaths
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|this
operator|.
name|inputPaths
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
operator|)
operator|:
literal|"alter table ... concatenate should only have one"
operator|+
literal|" directory inside inputpaths"
assert|;
name|Path
name|dirPath
init|=
name|inputPaths
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
try|try
block|{
name|FileSystem
name|inpFs
init|=
name|dirPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|status
init|=
name|HiveStatsUtils
operator|.
name|getFileStatusRecurse
argument_list|(
name|dirPath
argument_list|,
name|listBucketingCtx
operator|.
name|getSkewedColNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|inpFs
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|newInputPath
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|succeed
init|=
literal|true
decl_stmt|;
for|for
control|(
name|FileStatus
name|s
range|:
name|status
control|)
block|{
if|if
condition|(
name|s
operator|.
name|isDir
argument_list|()
condition|)
block|{
comment|// Add the lb path to the list of input paths
name|newInputPath
operator|.
name|add
argument_list|(
name|s
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// find file instead of dir. dont change inputpath
name|succeed
operator|=
literal|false
expr_stmt|;
block|}
block|}
assert|assert
operator|(
name|succeed
operator|||
operator|(
operator|(
operator|!
name|succeed
operator|)
operator|&&
name|newInputPath
operator|.
name|isEmpty
argument_list|()
operator|)
operator|)
operator|:
literal|"This partition has "
operator|+
literal|" inconsistent file structure: "
operator|+
literal|"it is stored-as-subdir and expected all files in the same depth"
operator|+
literal|" of subdirectories."
assert|;
if|if
condition|(
name|succeed
condition|)
block|{
name|inputPaths
operator|.
name|clear
argument_list|()
expr_stmt|;
name|inputPaths
operator|.
name|addAll
argument_list|(
name|newInputPath
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Fail to get filesystem for directory name : "
operator|+
name|dirPath
operator|.
name|toUri
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * @return the listBucketingCtx    */
specifier|public
name|ListBucketingCtx
name|getListBucketingCtx
parameter_list|()
block|{
return|return
name|listBucketingCtx
return|;
block|}
comment|/**    * @param listBucketingCtx the listBucketingCtx to set    */
specifier|public
name|void
name|setListBucketingCtx
parameter_list|(
name|ListBucketingCtx
name|listBucketingCtx
parameter_list|)
block|{
name|this
operator|.
name|listBucketingCtx
operator|=
name|listBucketingCtx
expr_stmt|;
block|}
comment|/**    * @return the isListBucketingAlterTableConcatenate    */
specifier|public
name|boolean
name|isListBucketingAlterTableConcatenate
parameter_list|()
block|{
return|return
name|isListBucketingAlterTableConcatenate
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"input format"
argument_list|)
specifier|public
name|String
name|getSourceTableInputFormat
parameter_list|()
block|{
return|return
name|srcTblInputFormat
return|;
block|}
specifier|public
name|void
name|setSourceTableInputFormat
parameter_list|(
name|String
name|srcTblInputFormat
parameter_list|)
block|{
name|this
operator|.
name|srcTblInputFormat
operator|=
name|srcTblInputFormat
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"merge level"
argument_list|)
specifier|public
name|String
name|getMergeLevel
parameter_list|()
block|{
if|if
condition|(
name|srcTblInputFormat
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|srcTblInputFormat
operator|.
name|equals
argument_list|(
name|OrcInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|"stripe"
return|;
block|}
elseif|else
if|if
condition|(
name|srcTblInputFormat
operator|.
name|equals
argument_list|(
name|RCFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|"block"
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

