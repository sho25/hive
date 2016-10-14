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

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|FileMergeDesc
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
name|DynamicPartitionCtx
name|dpCtx
decl_stmt|;
specifier|private
name|Path
name|outputPath
decl_stmt|;
specifier|private
name|int
name|listBucketingDepth
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
name|Long
name|mmWriteId
decl_stmt|;
specifier|public
name|FileMergeDesc
parameter_list|(
name|DynamicPartitionCtx
name|dynPartCtx
parameter_list|,
name|Path
name|outputDir
parameter_list|)
block|{
name|this
operator|.
name|dpCtx
operator|=
name|dynPartCtx
expr_stmt|;
name|this
operator|.
name|outputPath
operator|=
name|outputDir
expr_stmt|;
block|}
specifier|public
name|DynamicPartitionCtx
name|getDpCtx
parameter_list|()
block|{
return|return
name|dpCtx
return|;
block|}
specifier|public
name|void
name|setDpCtx
parameter_list|(
name|DynamicPartitionCtx
name|dpCtx
parameter_list|)
block|{
name|this
operator|.
name|dpCtx
operator|=
name|dpCtx
expr_stmt|;
block|}
specifier|public
name|Path
name|getOutputPath
parameter_list|()
block|{
return|return
name|outputPath
return|;
block|}
specifier|public
name|void
name|setOutputPath
parameter_list|(
name|Path
name|outputPath
parameter_list|)
block|{
name|this
operator|.
name|outputPath
operator|=
name|outputPath
expr_stmt|;
block|}
specifier|public
name|int
name|getListBucketingDepth
parameter_list|()
block|{
return|return
name|listBucketingDepth
return|;
block|}
specifier|public
name|void
name|setListBucketingDepth
parameter_list|(
name|int
name|listBucketingDepth
parameter_list|)
block|{
name|this
operator|.
name|listBucketingDepth
operator|=
name|listBucketingDepth
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasDynamicPartitions
parameter_list|()
block|{
return|return
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
name|boolean
name|isListBucketingAlterTableConcatenate
parameter_list|()
block|{
return|return
name|isListBucketingAlterTableConcatenate
return|;
block|}
specifier|public
name|void
name|setListBucketingAlterTableConcatenate
parameter_list|(
name|boolean
name|isListBucketingAlterTableConcatenate
parameter_list|)
block|{
name|this
operator|.
name|isListBucketingAlterTableConcatenate
operator|=
name|isListBucketingAlterTableConcatenate
expr_stmt|;
block|}
specifier|public
name|Long
name|getMmWriteId
parameter_list|()
block|{
return|return
name|mmWriteId
return|;
block|}
specifier|public
name|void
name|setMmWriteId
parameter_list|(
name|Long
name|mmWriteId
parameter_list|)
block|{
name|this
operator|.
name|mmWriteId
operator|=
name|mmWriteId
expr_stmt|;
block|}
block|}
end_class

end_unit

