begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|LinkedHashMap
import|;
end_import

begin_comment
comment|/**  * Metadata related to Msck.  */
end_comment

begin_class
specifier|public
class|class
name|MsckInfo
block|{
specifier|private
name|String
name|catalogName
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partSpecs
decl_stmt|;
specifier|private
name|String
name|resFile
decl_stmt|;
specifier|private
name|boolean
name|repairPartitions
decl_stmt|;
specifier|private
name|boolean
name|addPartitions
decl_stmt|;
specifier|private
name|boolean
name|dropPartitions
decl_stmt|;
specifier|private
name|long
name|partitionExpirySeconds
decl_stmt|;
specifier|public
name|MsckInfo
parameter_list|(
specifier|final
name|String
name|catalogName
parameter_list|,
specifier|final
name|String
name|dbName
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|ArrayList
argument_list|<
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partSpecs
parameter_list|,
specifier|final
name|String
name|resFile
parameter_list|,
specifier|final
name|boolean
name|repairPartitions
parameter_list|,
specifier|final
name|boolean
name|addPartitions
parameter_list|,
specifier|final
name|boolean
name|dropPartitions
parameter_list|,
specifier|final
name|long
name|partitionExpirySeconds
parameter_list|)
block|{
name|this
operator|.
name|catalogName
operator|=
name|catalogName
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partSpecs
operator|=
name|partSpecs
expr_stmt|;
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
name|this
operator|.
name|repairPartitions
operator|=
name|repairPartitions
expr_stmt|;
name|this
operator|.
name|addPartitions
operator|=
name|addPartitions
expr_stmt|;
name|this
operator|.
name|dropPartitions
operator|=
name|dropPartitions
expr_stmt|;
name|this
operator|.
name|partitionExpirySeconds
operator|=
name|partitionExpirySeconds
expr_stmt|;
block|}
specifier|public
name|String
name|getCatalogName
parameter_list|()
block|{
return|return
name|catalogName
return|;
block|}
specifier|public
name|void
name|setCatalogName
parameter_list|(
specifier|final
name|String
name|catalogName
parameter_list|)
block|{
name|this
operator|.
name|catalogName
operator|=
name|catalogName
expr_stmt|;
block|}
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
specifier|public
name|void
name|setDbName
parameter_list|(
specifier|final
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
block|}
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|void
name|setTableName
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|getPartSpecs
parameter_list|()
block|{
return|return
name|partSpecs
return|;
block|}
specifier|public
name|void
name|setPartSpecs
parameter_list|(
specifier|final
name|ArrayList
argument_list|<
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partSpecs
parameter_list|)
block|{
name|this
operator|.
name|partSpecs
operator|=
name|partSpecs
expr_stmt|;
block|}
specifier|public
name|String
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
block|}
specifier|public
name|void
name|setResFile
parameter_list|(
specifier|final
name|String
name|resFile
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
block|}
specifier|public
name|boolean
name|isRepairPartitions
parameter_list|()
block|{
return|return
name|repairPartitions
return|;
block|}
specifier|public
name|void
name|setRepairPartitions
parameter_list|(
specifier|final
name|boolean
name|repairPartitions
parameter_list|)
block|{
name|this
operator|.
name|repairPartitions
operator|=
name|repairPartitions
expr_stmt|;
block|}
specifier|public
name|boolean
name|isAddPartitions
parameter_list|()
block|{
return|return
name|addPartitions
return|;
block|}
specifier|public
name|void
name|setAddPartitions
parameter_list|(
specifier|final
name|boolean
name|addPartitions
parameter_list|)
block|{
name|this
operator|.
name|addPartitions
operator|=
name|addPartitions
expr_stmt|;
block|}
specifier|public
name|boolean
name|isDropPartitions
parameter_list|()
block|{
return|return
name|dropPartitions
return|;
block|}
specifier|public
name|void
name|setDropPartitions
parameter_list|(
specifier|final
name|boolean
name|dropPartitions
parameter_list|)
block|{
name|this
operator|.
name|dropPartitions
operator|=
name|dropPartitions
expr_stmt|;
block|}
specifier|public
name|long
name|getPartitionExpirySeconds
parameter_list|()
block|{
return|return
name|partitionExpirySeconds
return|;
block|}
specifier|public
name|void
name|setPartitionExpirySeconds
parameter_list|(
specifier|final
name|long
name|partitionExpirySeconds
parameter_list|)
block|{
name|this
operator|.
name|partitionExpirySeconds
operator|=
name|partitionExpirySeconds
expr_stmt|;
block|}
block|}
end_class

end_unit

