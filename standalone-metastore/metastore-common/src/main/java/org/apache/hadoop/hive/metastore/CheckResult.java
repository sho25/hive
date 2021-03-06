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
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_comment
comment|/**  * Result class used by the HiveMetaStoreChecker.  */
end_comment

begin_class
specifier|public
class|class
name|CheckResult
block|{
comment|// tree sets to preserve ordering in qfile tests
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|tablesNotOnFs
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|tablesNotInMs
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|PartitionResult
argument_list|>
name|partitionsNotOnFs
init|=
operator|new
name|TreeSet
argument_list|<
name|PartitionResult
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|PartitionResult
argument_list|>
name|partitionsNotInMs
init|=
operator|new
name|TreeSet
argument_list|<
name|PartitionResult
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|PartitionResult
argument_list|>
name|expiredPartitions
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * @return a list of tables not found on the filesystem.    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getTablesNotOnFs
parameter_list|()
block|{
return|return
name|tablesNotOnFs
return|;
block|}
comment|/**    * @param tablesNotOnFs    *          a list of tables not found on the filesystem.    */
specifier|public
name|void
name|setTablesNotOnFs
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|tablesNotOnFs
parameter_list|)
block|{
name|this
operator|.
name|tablesNotOnFs
operator|=
name|tablesNotOnFs
expr_stmt|;
block|}
comment|/**    * @return a list of tables not found in the metastore.    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getTablesNotInMs
parameter_list|()
block|{
return|return
name|tablesNotInMs
return|;
block|}
comment|/**    * @param tablesNotInMs    *          a list of tables not found in the metastore.    */
specifier|public
name|void
name|setTablesNotInMs
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|tablesNotInMs
parameter_list|)
block|{
name|this
operator|.
name|tablesNotInMs
operator|=
name|tablesNotInMs
expr_stmt|;
block|}
comment|/**    * @return a list of partitions not found on the fs    */
specifier|public
name|Set
argument_list|<
name|PartitionResult
argument_list|>
name|getPartitionsNotOnFs
parameter_list|()
block|{
return|return
name|partitionsNotOnFs
return|;
block|}
comment|/**    * @param partitionsNotOnFs    *          a list of partitions not found on the fs    */
specifier|public
name|void
name|setPartitionsNotOnFs
parameter_list|(
name|Set
argument_list|<
name|PartitionResult
argument_list|>
name|partitionsNotOnFs
parameter_list|)
block|{
name|this
operator|.
name|partitionsNotOnFs
operator|=
name|partitionsNotOnFs
expr_stmt|;
block|}
comment|/**    * @return a list of partitions not found in the metastore    */
specifier|public
name|Set
argument_list|<
name|PartitionResult
argument_list|>
name|getPartitionsNotInMs
parameter_list|()
block|{
return|return
name|partitionsNotInMs
return|;
block|}
comment|/**    * @param partitionsNotInMs    *          a list of partitions not found in the metastore    */
specifier|public
name|void
name|setPartitionsNotInMs
parameter_list|(
name|Set
argument_list|<
name|PartitionResult
argument_list|>
name|partitionsNotInMs
parameter_list|)
block|{
name|this
operator|.
name|partitionsNotInMs
operator|=
name|partitionsNotInMs
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|PartitionResult
argument_list|>
name|getExpiredPartitions
parameter_list|()
block|{
return|return
name|expiredPartitions
return|;
block|}
specifier|public
name|void
name|setExpiredPartitions
parameter_list|(
specifier|final
name|Set
argument_list|<
name|PartitionResult
argument_list|>
name|expiredPartitions
parameter_list|)
block|{
name|this
operator|.
name|expiredPartitions
operator|=
name|expiredPartitions
expr_stmt|;
block|}
comment|/**    * A basic description of a partition that is missing from either the fs or    * the ms.    */
specifier|public
specifier|static
class|class
name|PartitionResult
implements|implements
name|Comparable
argument_list|<
name|PartitionResult
argument_list|>
block|{
specifier|private
name|String
name|partitionName
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
comment|/**      * @return name of partition      */
specifier|public
name|String
name|getPartitionName
parameter_list|()
block|{
return|return
name|partitionName
return|;
block|}
comment|/**      * @param partitionName      *          name of partition      */
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
comment|/**      * @return table name      */
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**      * @param tableName      *          table name      */
specifier|public
name|void
name|setTableName
parameter_list|(
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
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|tableName
operator|+
literal|":"
operator|+
name|partitionName
return|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|PartitionResult
name|o
parameter_list|)
block|{
name|int
name|ret
init|=
name|tableName
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|tableName
argument_list|)
decl_stmt|;
return|return
name|ret
operator|!=
literal|0
condition|?
name|ret
else|:
name|partitionName
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|partitionName
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

