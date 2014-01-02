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
name|truncate
package|;
end_package

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
name|BucketizedHiveInputFormat
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
literal|"Column Truncate"
argument_list|)
specifier|public
class|class
name|ColumnTruncateWork
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
specifier|transient
name|Path
name|inputDir
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
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|droppedColumns
decl_stmt|;
specifier|public
name|ColumnTruncateWork
parameter_list|()
block|{   }
specifier|public
name|ColumnTruncateWork
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|droppedColumns
parameter_list|,
name|Path
name|inputDir
parameter_list|,
name|Path
name|outputDir
parameter_list|)
block|{
name|this
argument_list|(
name|droppedColumns
argument_list|,
name|inputDir
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
name|ColumnTruncateWork
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|droppedColumns
parameter_list|,
name|Path
name|inputDir
parameter_list|,
name|Path
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
name|droppedColumns
operator|=
name|droppedColumns
expr_stmt|;
name|this
operator|.
name|inputDir
operator|=
name|inputDir
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
name|this
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|inputDir
operator|.
name|toString
argument_list|()
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Path
name|getInputDir
parameter_list|()
block|{
return|return
name|inputDir
return|;
block|}
specifier|public
name|void
name|setInputPaths
parameter_list|(
name|Path
name|inputDir
parameter_list|)
block|{
name|this
operator|.
name|inputDir
operator|=
name|inputDir
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
name|ColumnTruncateMapper
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
name|BucketizedHiveInputFormat
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
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getDroppedColumns
parameter_list|()
block|{
return|return
name|droppedColumns
return|;
block|}
specifier|public
name|void
name|setDroppedColumns
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|droppedColumns
parameter_list|)
block|{
name|this
operator|.
name|droppedColumns
operator|=
name|droppedColumns
expr_stmt|;
block|}
block|}
end_class

end_unit

