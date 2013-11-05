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
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|Mapper
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Block level merge"
argument_list|)
specifier|public
class|class
name|MergeWork
extends|extends
name|MapWork
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|inputPaths
decl_stmt|;
specifier|private
name|String
name|outputDir
decl_stmt|;
specifier|private
name|boolean
name|hasDynamicPartitions
decl_stmt|;
specifier|private
name|DynamicPartitionCtx
name|dynPartCtx
decl_stmt|;
specifier|private
name|boolean
name|isListBucketingAlterTableConcatenate
decl_stmt|;
specifier|private
name|ListBucketingCtx
name|listBucketingCtx
decl_stmt|;
specifier|public
name|MergeWork
parameter_list|()
block|{   }
specifier|public
name|MergeWork
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|inputPaths
parameter_list|,
name|String
name|outputDir
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
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MergeWork
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|inputPaths
parameter_list|,
name|String
name|outputDir
parameter_list|,
name|boolean
name|hasDynamicPartitions
parameter_list|,
name|DynamicPartitionCtx
name|dynPartCtx
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
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
name|dynPartCtx
operator|=
name|dynPartCtx
expr_stmt|;
name|PartitionDesc
name|partDesc
init|=
operator|new
name|PartitionDesc
argument_list|()
decl_stmt|;
name|partDesc
operator|.
name|setInputFileFormatClass
argument_list|(
name|RCFileBlockMergeInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|setPathToPartitionInfo
argument_list|(
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|path
range|:
name|this
operator|.
name|inputPaths
control|)
block|{
name|this
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|String
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
name|String
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
name|String
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
name|String
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
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Mapper
argument_list|>
name|getMapperClass
parameter_list|()
block|{
return|return
name|RCFileMergeMapper
operator|.
name|class
return|;
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
name|CombineHiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|String
name|inputFormatClass
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGEINPUTFORMATBLOCKLEVEL
argument_list|)
decl_stmt|;
try|try
block|{
name|partDesc
operator|.
name|setInputFileFormatClass
argument_list|(
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|inputFormatClass
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Merge input format class not found"
decl_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
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
comment|// Add the DP path to the list of input paths
name|inputPaths
operator|.
name|add
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * alter table ... concatenate    *    * If it is skewed table, use subdirectories in inputpaths.    */
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
literal|"alter table ... concatenate should only have one directory inside inputpaths"
assert|;
name|String
name|dirName
init|=
name|inputPaths
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Path
name|dirPath
init|=
operator|new
name|Path
argument_list|(
name|dirName
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
name|FileStatus
index|[]
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
name|String
argument_list|>
name|newInputPath
init|=
operator|new
name|ArrayList
argument_list|<
name|String
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
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|status
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|status
index|[
name|i
index|]
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
name|status
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|toString
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
literal|"it is stored-as-subdir and expected all files in the same depth of subdirectories."
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
name|dirName
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
specifier|public
name|DynamicPartitionCtx
name|getDynPartCtx
parameter_list|()
block|{
return|return
name|dynPartCtx
return|;
block|}
specifier|public
name|void
name|setDynPartCtx
parameter_list|(
name|DynamicPartitionCtx
name|dynPartCtx
parameter_list|)
block|{
name|this
operator|.
name|dynPartCtx
operator|=
name|dynPartCtx
expr_stmt|;
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
block|}
end_class

end_unit

