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
name|metastore
operator|.
name|model
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
specifier|public
class|class
name|MPartition
block|{
specifier|private
name|String
name|partitionName
decl_stmt|;
comment|// partitionname ==>  (key=value/)*(key=value)
specifier|private
name|MTable
name|table
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|values
decl_stmt|;
specifier|private
name|int
name|createTime
decl_stmt|;
specifier|private
name|int
name|lastAccessTime
decl_stmt|;
specifier|private
name|MStorageDescriptor
name|sd
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
decl_stmt|;
specifier|private
name|long
name|txnId
decl_stmt|;
specifier|private
name|String
name|writeIdList
decl_stmt|;
specifier|public
name|MPartition
parameter_list|()
block|{}
comment|/**    * @param partitionName    * @param table    * @param values    * @param createTime    * @param lastAccessTime    * @param sd    * @param parameters    */
specifier|public
name|MPartition
parameter_list|(
name|String
name|partitionName
parameter_list|,
name|MTable
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|,
name|int
name|createTime
parameter_list|,
name|int
name|lastAccessTime
parameter_list|,
name|MStorageDescriptor
name|sd
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
block|{
name|this
operator|.
name|partitionName
operator|=
name|partitionName
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|createTime
operator|=
name|createTime
expr_stmt|;
name|this
operator|.
name|lastAccessTime
operator|=
name|lastAccessTime
expr_stmt|;
name|this
operator|.
name|sd
operator|=
name|sd
expr_stmt|;
name|this
operator|.
name|parameters
operator|=
name|parameters
expr_stmt|;
block|}
comment|/**    * @return the lastAccessTime    */
specifier|public
name|int
name|getLastAccessTime
parameter_list|()
block|{
return|return
name|lastAccessTime
return|;
block|}
comment|/**    * @param lastAccessTime the lastAccessTime to set    */
specifier|public
name|void
name|setLastAccessTime
parameter_list|(
name|int
name|lastAccessTime
parameter_list|)
block|{
name|this
operator|.
name|lastAccessTime
operator|=
name|lastAccessTime
expr_stmt|;
block|}
comment|/**    * @return the values    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getValues
parameter_list|()
block|{
return|return
name|values
return|;
block|}
comment|/**    * @param values the values to set    */
specifier|public
name|void
name|setValues
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
comment|/**    * @return the table    */
specifier|public
name|MTable
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
comment|/**    * @param table the table to set    */
specifier|public
name|void
name|setTable
parameter_list|(
name|MTable
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
comment|/**    * @return the sd    */
specifier|public
name|MStorageDescriptor
name|getSd
parameter_list|()
block|{
return|return
name|sd
return|;
block|}
comment|/**    * @param sd the sd to set    */
specifier|public
name|void
name|setSd
parameter_list|(
name|MStorageDescriptor
name|sd
parameter_list|)
block|{
name|this
operator|.
name|sd
operator|=
name|sd
expr_stmt|;
block|}
comment|/**    * @return the parameters    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getParameters
parameter_list|()
block|{
return|return
name|parameters
return|;
block|}
comment|/**    * @param parameters the parameters to set    */
specifier|public
name|void
name|setParameters
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
block|{
name|this
operator|.
name|parameters
operator|=
name|parameters
expr_stmt|;
block|}
comment|/**    * @return the partitionName    */
specifier|public
name|String
name|getPartitionName
parameter_list|()
block|{
return|return
name|partitionName
return|;
block|}
comment|/**    * @param partitionName the partitionName to set    */
specifier|public
name|void
name|setPartitionName
parameter_list|(
name|String
name|partitionName
parameter_list|)
block|{
name|this
operator|.
name|partitionName
operator|=
name|partitionName
expr_stmt|;
block|}
comment|/**    * @return the createTime    */
specifier|public
name|int
name|getCreateTime
parameter_list|()
block|{
return|return
name|createTime
return|;
block|}
comment|/**    * @param createTime the createTime to set    */
specifier|public
name|void
name|setCreateTime
parameter_list|(
name|int
name|createTime
parameter_list|)
block|{
name|this
operator|.
name|createTime
operator|=
name|createTime
expr_stmt|;
block|}
specifier|public
name|long
name|getTxnId
parameter_list|()
block|{
return|return
name|txnId
return|;
block|}
specifier|public
name|void
name|setTxnId
parameter_list|(
name|long
name|txnId
parameter_list|)
block|{
name|this
operator|.
name|txnId
operator|=
name|txnId
expr_stmt|;
block|}
specifier|public
name|String
name|getWriteIdList
parameter_list|()
block|{
return|return
name|writeIdList
return|;
block|}
specifier|public
name|void
name|setWriteIdList
parameter_list|(
name|String
name|writeIdList
parameter_list|)
block|{
name|this
operator|.
name|writeIdList
operator|=
name|writeIdList
expr_stmt|;
block|}
block|}
end_class

end_unit

