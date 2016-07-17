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
name|plan
package|;
end_package

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
name|ql
operator|.
name|io
operator|.
name|AcidUtils
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
name|Table
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

begin_comment
comment|/**  * FileSinkDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"File Output Operator"
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
name|FileSinkDesc
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
enum|enum
name|DPSortState
block|{
name|NONE
block|,
name|PARTITION_SORTED
block|,
name|PARTITION_BUCKET_SORTED
block|}
specifier|private
name|DPSortState
name|dpSortState
decl_stmt|;
specifier|private
name|Path
name|dirName
decl_stmt|;
comment|// normally statsKeyPref will be the same as dirName, but the latter
comment|// could be changed in local execution optimization
specifier|private
name|String
name|statsKeyPref
decl_stmt|;
specifier|private
name|TableDesc
name|tableInfo
decl_stmt|;
specifier|private
name|boolean
name|compressed
decl_stmt|;
specifier|private
name|int
name|destTableId
decl_stmt|;
specifier|private
name|String
name|compressCodec
decl_stmt|;
specifier|private
name|String
name|compressType
decl_stmt|;
specifier|private
name|boolean
name|multiFileSpray
decl_stmt|;
specifier|private
name|boolean
name|temporary
decl_stmt|;
specifier|private
name|boolean
name|materialization
decl_stmt|;
comment|// Whether the files output by this FileSink can be merged, e.g. if they are to be put into a
comment|// bucketed or sorted table/partition they cannot be merged.
specifier|private
name|boolean
name|canBeMerged
decl_stmt|;
specifier|private
name|int
name|totalFiles
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
decl_stmt|;
specifier|private
name|int
name|numFiles
decl_stmt|;
specifier|private
name|DynamicPartitionCtx
name|dpCtx
decl_stmt|;
specifier|private
name|String
name|staticSpec
decl_stmt|;
comment|// static partition spec ends with a '/'
specifier|private
name|boolean
name|gatherStats
decl_stmt|;
specifier|private
name|int
name|indexInTezUnion
init|=
operator|-
literal|1
decl_stmt|;
comment|// Consider a query like:
comment|// insert overwrite table T3 select ... from T1 join T2 on T1.key = T2.key;
comment|// where T1, T2 and T3 are sorted and bucketed by key into the same number of buckets,
comment|// We dont need a reducer to enforce bucketing and sorting for T3.
comment|// The field below captures the fact that the reducer introduced to enforce sorting/
comment|// bucketing of T3 has been removed.
comment|// In this case, a sort-merge join is needed, and so the sort-merge join between T1 and T2
comment|// cannot be performed as a map-only job
specifier|private
specifier|transient
name|boolean
name|removedReduceSinkBucketSort
decl_stmt|;
comment|// This file descriptor is linked to other file descriptors.
comment|// One use case is that, a union->select (star)->file sink, is broken down.
comment|// For eg: consider a query like:
comment|// select * from (subq1 union all subq2)x;
comment|// where subq1 or subq2 involves a map-reduce job.
comment|// It is broken into two independent queries involving subq1 and subq2 directly, and
comment|// the sub-queries write to sub-directories of a common directory. So, the file sink
comment|// descriptors for subq1 and subq2 are linked.
specifier|private
name|boolean
name|linkedFileSink
init|=
literal|false
decl_stmt|;
specifier|private
name|Path
name|parentDir
decl_stmt|;
specifier|transient
specifier|private
name|List
argument_list|<
name|FileSinkDesc
argument_list|>
name|linkedFileSinkDesc
decl_stmt|;
specifier|private
name|boolean
name|statsReliable
decl_stmt|;
specifier|private
name|ListBucketingCtx
name|lbCtx
decl_stmt|;
specifier|private
name|String
name|statsTmpDir
decl_stmt|;
comment|// Record what type of write this is.  Default is non-ACID (ie old style).
specifier|private
name|AcidUtils
operator|.
name|Operation
name|writeType
init|=
name|AcidUtils
operator|.
name|Operation
operator|.
name|NOT_ACID
decl_stmt|;
specifier|private
name|long
name|txnId
init|=
literal|0
decl_stmt|;
comment|// transaction id for this operation
specifier|private
name|int
name|statementId
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|transient
name|Table
name|table
decl_stmt|;
specifier|private
name|Path
name|destPath
decl_stmt|;
specifier|private
name|boolean
name|isHiveServerQuery
decl_stmt|;
specifier|public
name|FileSinkDesc
parameter_list|()
block|{   }
comment|/**    * @param destPath - the final destination for data    */
specifier|public
name|FileSinkDesc
parameter_list|(
specifier|final
name|Path
name|dirName
parameter_list|,
specifier|final
name|TableDesc
name|tableInfo
parameter_list|,
specifier|final
name|boolean
name|compressed
parameter_list|,
specifier|final
name|int
name|destTableId
parameter_list|,
specifier|final
name|boolean
name|multiFileSpray
parameter_list|,
specifier|final
name|boolean
name|canBeMerged
parameter_list|,
specifier|final
name|int
name|numFiles
parameter_list|,
specifier|final
name|int
name|totalFiles
parameter_list|,
specifier|final
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
parameter_list|,
specifier|final
name|DynamicPartitionCtx
name|dpCtx
parameter_list|,
name|Path
name|destPath
parameter_list|)
block|{
name|this
operator|.
name|dirName
operator|=
name|dirName
expr_stmt|;
name|this
operator|.
name|tableInfo
operator|=
name|tableInfo
expr_stmt|;
name|this
operator|.
name|compressed
operator|=
name|compressed
expr_stmt|;
name|this
operator|.
name|destTableId
operator|=
name|destTableId
expr_stmt|;
name|this
operator|.
name|multiFileSpray
operator|=
name|multiFileSpray
expr_stmt|;
name|this
operator|.
name|canBeMerged
operator|=
name|canBeMerged
expr_stmt|;
name|this
operator|.
name|numFiles
operator|=
name|numFiles
expr_stmt|;
name|this
operator|.
name|totalFiles
operator|=
name|totalFiles
expr_stmt|;
name|this
operator|.
name|partitionCols
operator|=
name|partitionCols
expr_stmt|;
name|this
operator|.
name|dpCtx
operator|=
name|dpCtx
expr_stmt|;
name|this
operator|.
name|dpSortState
operator|=
name|DPSortState
operator|.
name|NONE
expr_stmt|;
name|this
operator|.
name|destPath
operator|=
name|destPath
expr_stmt|;
block|}
specifier|public
name|FileSinkDesc
parameter_list|(
specifier|final
name|Path
name|dirName
parameter_list|,
specifier|final
name|TableDesc
name|tableInfo
parameter_list|,
specifier|final
name|boolean
name|compressed
parameter_list|)
block|{
name|this
operator|.
name|dirName
operator|=
name|dirName
expr_stmt|;
name|this
operator|.
name|tableInfo
operator|=
name|tableInfo
expr_stmt|;
name|this
operator|.
name|compressed
operator|=
name|compressed
expr_stmt|;
name|destTableId
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|multiFileSpray
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|canBeMerged
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|numFiles
operator|=
literal|1
expr_stmt|;
name|this
operator|.
name|totalFiles
operator|=
literal|1
expr_stmt|;
name|this
operator|.
name|partitionCols
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|dpSortState
operator|=
name|DPSortState
operator|.
name|NONE
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
throws|throws
name|CloneNotSupportedException
block|{
name|FileSinkDesc
name|ret
init|=
operator|new
name|FileSinkDesc
argument_list|(
name|dirName
argument_list|,
name|tableInfo
argument_list|,
name|compressed
argument_list|,
name|destTableId
argument_list|,
name|multiFileSpray
argument_list|,
name|canBeMerged
argument_list|,
name|numFiles
argument_list|,
name|totalFiles
argument_list|,
name|partitionCols
argument_list|,
name|dpCtx
argument_list|,
name|destPath
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setCompressCodec
argument_list|(
name|compressCodec
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setCompressType
argument_list|(
name|compressType
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setGatherStats
argument_list|(
name|gatherStats
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setStaticSpec
argument_list|(
name|staticSpec
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setStatsAggPrefix
argument_list|(
name|statsKeyPref
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setLinkedFileSink
argument_list|(
name|linkedFileSink
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setParentDir
argument_list|(
name|parentDir
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setLinkedFileSinkDesc
argument_list|(
name|linkedFileSinkDesc
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setStatsReliable
argument_list|(
name|statsReliable
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setDpSortState
argument_list|(
name|dpSortState
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setWriteType
argument_list|(
name|writeType
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setTransactionId
argument_list|(
name|txnId
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setStatsTmpDir
argument_list|(
name|statsTmpDir
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
name|boolean
name|isHiveServerQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|isHiveServerQuery
return|;
block|}
specifier|public
name|void
name|setHiveServerQuery
parameter_list|(
name|boolean
name|isHiveServerQuery
parameter_list|)
block|{
name|this
operator|.
name|isHiveServerQuery
operator|=
name|isHiveServerQuery
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"directory"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|Path
name|getDirName
parameter_list|()
block|{
return|return
name|dirName
return|;
block|}
specifier|public
name|void
name|setDirName
parameter_list|(
specifier|final
name|Path
name|dirName
parameter_list|)
block|{
name|this
operator|.
name|dirName
operator|=
name|dirName
expr_stmt|;
block|}
specifier|public
name|Path
name|getFinalDirName
parameter_list|()
block|{
return|return
name|linkedFileSink
condition|?
name|parentDir
else|:
name|dirName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table"
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
name|TableDesc
name|getTableInfo
parameter_list|()
block|{
return|return
name|tableInfo
return|;
block|}
specifier|public
name|void
name|setTableInfo
parameter_list|(
specifier|final
name|TableDesc
name|tableInfo
parameter_list|)
block|{
name|this
operator|.
name|tableInfo
operator|=
name|tableInfo
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"compressed"
argument_list|)
specifier|public
name|boolean
name|getCompressed
parameter_list|()
block|{
return|return
name|compressed
return|;
block|}
specifier|public
name|void
name|setCompressed
parameter_list|(
name|boolean
name|compressed
parameter_list|)
block|{
name|this
operator|.
name|compressed
operator|=
name|compressed
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"GlobalTableId"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|int
name|getDestTableId
parameter_list|()
block|{
return|return
name|destTableId
return|;
block|}
specifier|public
name|void
name|setDestTableId
parameter_list|(
name|int
name|destTableId
parameter_list|)
block|{
name|this
operator|.
name|destTableId
operator|=
name|destTableId
expr_stmt|;
block|}
specifier|public
name|String
name|getCompressCodec
parameter_list|()
block|{
return|return
name|compressCodec
return|;
block|}
specifier|public
name|void
name|setCompressCodec
parameter_list|(
name|String
name|intermediateCompressorCodec
parameter_list|)
block|{
name|compressCodec
operator|=
name|intermediateCompressorCodec
expr_stmt|;
block|}
specifier|public
name|String
name|getCompressType
parameter_list|()
block|{
return|return
name|compressType
return|;
block|}
specifier|public
name|void
name|setCompressType
parameter_list|(
name|String
name|intermediateCompressType
parameter_list|)
block|{
name|compressType
operator|=
name|intermediateCompressType
expr_stmt|;
block|}
comment|/**    * @return the multiFileSpray    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"MultiFileSpray"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|boolean
name|isMultiFileSpray
parameter_list|()
block|{
return|return
name|multiFileSpray
return|;
block|}
comment|/**    * @param multiFileSpray the multiFileSpray to set    */
specifier|public
name|void
name|setMultiFileSpray
parameter_list|(
name|boolean
name|multiFileSpray
parameter_list|)
block|{
name|this
operator|.
name|multiFileSpray
operator|=
name|multiFileSpray
expr_stmt|;
block|}
comment|/**    * @return destination is temporary    */
specifier|public
name|boolean
name|isTemporary
parameter_list|()
block|{
return|return
name|temporary
return|;
block|}
specifier|public
name|void
name|setTemporary
parameter_list|(
name|boolean
name|temporary
parameter_list|)
block|{
name|this
operator|.
name|temporary
operator|=
name|temporary
expr_stmt|;
block|}
specifier|public
name|boolean
name|isMaterialization
parameter_list|()
block|{
return|return
name|materialization
return|;
block|}
specifier|public
name|void
name|setMaterialization
parameter_list|(
name|boolean
name|materialization
parameter_list|)
block|{
name|this
operator|.
name|materialization
operator|=
name|materialization
expr_stmt|;
block|}
specifier|public
name|boolean
name|canBeMerged
parameter_list|()
block|{
return|return
name|canBeMerged
return|;
block|}
specifier|public
name|void
name|setCanBeMerged
parameter_list|(
name|boolean
name|canBeMerged
parameter_list|)
block|{
name|this
operator|.
name|canBeMerged
operator|=
name|canBeMerged
expr_stmt|;
block|}
comment|/**    * @return the totalFiles    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"TotalFiles"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|int
name|getTotalFiles
parameter_list|()
block|{
return|return
name|totalFiles
return|;
block|}
comment|/**    * @param totalFiles the totalFiles to set    */
specifier|public
name|void
name|setTotalFiles
parameter_list|(
name|int
name|totalFiles
parameter_list|)
block|{
name|this
operator|.
name|totalFiles
operator|=
name|totalFiles
expr_stmt|;
block|}
comment|/**    * @return the partitionCols    */
specifier|public
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getPartitionCols
parameter_list|()
block|{
return|return
name|partitionCols
return|;
block|}
comment|/**    * @param partitionCols the partitionCols to set    */
specifier|public
name|void
name|setPartitionCols
parameter_list|(
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
parameter_list|)
block|{
name|this
operator|.
name|partitionCols
operator|=
name|partitionCols
expr_stmt|;
block|}
comment|/**    * @return the numFiles    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"NumFilesPerFileSink"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|int
name|getNumFiles
parameter_list|()
block|{
return|return
name|numFiles
return|;
block|}
comment|/**    * @param numFiles the numFiles to set    */
specifier|public
name|void
name|setNumFiles
parameter_list|(
name|int
name|numFiles
parameter_list|)
block|{
name|this
operator|.
name|numFiles
operator|=
name|numFiles
expr_stmt|;
block|}
specifier|public
name|void
name|setDynPartCtx
parameter_list|(
name|DynamicPartitionCtx
name|dpc
parameter_list|)
block|{
name|this
operator|.
name|dpCtx
operator|=
name|dpc
expr_stmt|;
block|}
specifier|public
name|DynamicPartitionCtx
name|getDynPartCtx
parameter_list|()
block|{
return|return
name|this
operator|.
name|dpCtx
return|;
block|}
specifier|public
name|void
name|setStaticSpec
parameter_list|(
name|String
name|staticSpec
parameter_list|)
block|{
name|this
operator|.
name|staticSpec
operator|=
name|staticSpec
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Static Partition Specification"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getStaticSpec
parameter_list|()
block|{
return|return
name|staticSpec
return|;
block|}
specifier|public
name|void
name|setGatherStats
parameter_list|(
name|boolean
name|gatherStats
parameter_list|)
block|{
name|this
operator|.
name|gatherStats
operator|=
name|gatherStats
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"GatherStats"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|boolean
name|isGatherStats
parameter_list|()
block|{
return|return
name|gatherStats
return|;
block|}
comment|/**    * Construct the key prefix used as (intermediate) statistics publishing    * and aggregation. During stats publishing phase, this key prefix will be    * appended with the optional dynamic partition spec and the task ID. The    * whole key uniquely identifies the output of a task for this job. In the    * stats aggregation phase, all rows with the same prefix plus dynamic partition    * specs (obtained at run-time after MR job finishes) will be serving as the    * prefix: all rows with the same prefix (output of all tasks for this job)    * will be aggregated.    * @return key prefix used for stats publishing and aggregation.    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Stats Publishing Key Prefix"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getStatsAggPrefix
parameter_list|()
block|{
comment|// dirName uniquely identifies destination directory of a FileSinkOperator.
comment|// If more than one FileSinkOperator write to the same partition, this dirName
comment|// should be different.
return|return
name|statsKeyPref
return|;
block|}
comment|/**    * Set the stats aggregation key. If the input string is not terminated by Path.SEPARATOR    * aggregation key will add one to make it as a directory name.    * @param k input directory name.    */
specifier|public
name|void
name|setStatsAggPrefix
parameter_list|(
name|String
name|k
parameter_list|)
block|{
if|if
condition|(
name|k
operator|.
name|endsWith
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
condition|)
block|{
name|statsKeyPref
operator|=
name|k
expr_stmt|;
block|}
else|else
block|{
name|statsKeyPref
operator|=
name|k
operator|+
name|Path
operator|.
name|SEPARATOR
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|isLinkedFileSink
parameter_list|()
block|{
return|return
name|linkedFileSink
return|;
block|}
specifier|public
name|void
name|setLinkedFileSink
parameter_list|(
name|boolean
name|linkedFileSink
parameter_list|)
block|{
name|this
operator|.
name|linkedFileSink
operator|=
name|linkedFileSink
expr_stmt|;
block|}
specifier|public
name|Path
name|getParentDir
parameter_list|()
block|{
return|return
name|parentDir
return|;
block|}
specifier|public
name|void
name|setParentDir
parameter_list|(
name|Path
name|parentDir
parameter_list|)
block|{
name|this
operator|.
name|parentDir
operator|=
name|parentDir
expr_stmt|;
block|}
specifier|public
name|boolean
name|isStatsReliable
parameter_list|()
block|{
return|return
name|statsReliable
return|;
block|}
specifier|public
name|void
name|setStatsReliable
parameter_list|(
name|boolean
name|statsReliable
parameter_list|)
block|{
name|this
operator|.
name|statsReliable
operator|=
name|statsReliable
expr_stmt|;
block|}
comment|/**    * @return the lbCtx    */
specifier|public
name|ListBucketingCtx
name|getLbCtx
parameter_list|()
block|{
return|return
name|lbCtx
return|;
block|}
comment|/**    * @param lbCtx the lbCtx to set    */
specifier|public
name|void
name|setLbCtx
parameter_list|(
name|ListBucketingCtx
name|lbCtx
parameter_list|)
block|{
name|this
operator|.
name|lbCtx
operator|=
name|lbCtx
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|FileSinkDesc
argument_list|>
name|getLinkedFileSinkDesc
parameter_list|()
block|{
return|return
name|linkedFileSinkDesc
return|;
block|}
specifier|public
name|void
name|setLinkedFileSinkDesc
parameter_list|(
name|List
argument_list|<
name|FileSinkDesc
argument_list|>
name|linkedFileSinkDesc
parameter_list|)
block|{
name|this
operator|.
name|linkedFileSinkDesc
operator|=
name|linkedFileSinkDesc
expr_stmt|;
block|}
specifier|public
name|boolean
name|isRemovedReduceSinkBucketSort
parameter_list|()
block|{
return|return
name|removedReduceSinkBucketSort
return|;
block|}
specifier|public
name|void
name|setRemovedReduceSinkBucketSort
parameter_list|(
name|boolean
name|removedReduceSinkBucketSort
parameter_list|)
block|{
name|this
operator|.
name|removedReduceSinkBucketSort
operator|=
name|removedReduceSinkBucketSort
expr_stmt|;
block|}
specifier|public
name|DPSortState
name|getDpSortState
parameter_list|()
block|{
return|return
name|dpSortState
return|;
block|}
specifier|public
name|void
name|setDpSortState
parameter_list|(
name|DPSortState
name|dpSortState
parameter_list|)
block|{
name|this
operator|.
name|dpSortState
operator|=
name|dpSortState
expr_stmt|;
block|}
specifier|public
name|void
name|setWriteType
parameter_list|(
name|AcidUtils
operator|.
name|Operation
name|type
parameter_list|)
block|{
name|writeType
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|AcidUtils
operator|.
name|Operation
name|getWriteType
parameter_list|()
block|{
return|return
name|writeType
return|;
block|}
specifier|public
name|void
name|setTransactionId
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|txnId
operator|=
name|id
expr_stmt|;
block|}
specifier|public
name|long
name|getTransactionId
parameter_list|()
block|{
return|return
name|txnId
return|;
block|}
specifier|public
name|void
name|setStatementId
parameter_list|(
name|int
name|id
parameter_list|)
block|{
name|statementId
operator|=
name|id
expr_stmt|;
block|}
comment|/**    * See {@link org.apache.hadoop.hive.ql.io.AcidOutputFormat.Options#statementId(int)}    */
specifier|public
name|int
name|getStatementId
parameter_list|()
block|{
return|return
name|statementId
return|;
block|}
specifier|public
name|Path
name|getDestPath
parameter_list|()
block|{
return|return
name|destPath
return|;
block|}
specifier|public
name|Table
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|void
name|setTable
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
specifier|public
name|String
name|getStatsTmpDir
parameter_list|()
block|{
return|return
name|statsTmpDir
return|;
block|}
specifier|public
name|void
name|setStatsTmpDir
parameter_list|(
name|String
name|statsCollectionTempDir
parameter_list|)
block|{
name|this
operator|.
name|statsTmpDir
operator|=
name|statsCollectionTempDir
expr_stmt|;
block|}
specifier|public
name|int
name|getIndexInTezUnion
parameter_list|()
block|{
return|return
name|indexInTezUnion
return|;
block|}
specifier|public
name|void
name|setIndexInTezUnion
parameter_list|(
name|int
name|indexInTezUnion
parameter_list|)
block|{
name|this
operator|.
name|indexInTezUnion
operator|=
name|indexInTezUnion
expr_stmt|;
block|}
block|}
end_class

end_unit

