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
name|api
operator|.
name|DataOperationType
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
name|metastore
operator|.
name|api
operator|.
name|LockComponent
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
name|metastore
operator|.
name|api
operator|.
name|LockLevel
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
name|metastore
operator|.
name|api
operator|.
name|LockType
import|;
end_import

begin_comment
comment|/**  * A builder for {@link LockComponent}s  */
end_comment

begin_class
specifier|public
class|class
name|LockComponentBuilder
block|{
specifier|private
name|LockComponent
name|component
decl_stmt|;
specifier|private
name|boolean
name|tableNameSet
decl_stmt|;
specifier|private
name|boolean
name|partNameSet
decl_stmt|;
specifier|public
name|LockComponentBuilder
parameter_list|()
block|{
name|component
operator|=
operator|new
name|LockComponent
argument_list|()
expr_stmt|;
name|tableNameSet
operator|=
name|partNameSet
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Set the lock to be exclusive.    * @return reference to this builder    */
specifier|public
name|LockComponentBuilder
name|setExclusive
parameter_list|()
block|{
name|component
operator|.
name|setType
argument_list|(
name|LockType
operator|.
name|EXCLUSIVE
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set the lock to be semi-shared.    * @return reference to this builder    */
specifier|public
name|LockComponentBuilder
name|setSemiShared
parameter_list|()
block|{
name|component
operator|.
name|setType
argument_list|(
name|LockType
operator|.
name|SHARED_WRITE
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set the lock to be shared.    * @return reference to this builder    */
specifier|public
name|LockComponentBuilder
name|setShared
parameter_list|()
block|{
name|component
operator|.
name|setType
argument_list|(
name|LockType
operator|.
name|SHARED_READ
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set the database name.    * @param dbName database name    * @return reference to this builder    */
specifier|public
name|LockComponentBuilder
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|component
operator|.
name|setDbname
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LockComponentBuilder
name|setOperationType
parameter_list|(
name|DataOperationType
name|dop
parameter_list|)
block|{
name|component
operator|.
name|setOperationType
argument_list|(
name|dop
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LockComponentBuilder
name|setIsTransactional
parameter_list|(
name|boolean
name|t
parameter_list|)
block|{
name|component
operator|.
name|setIsTransactional
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set the table name.    * @param tableName table name    * @return reference to this builder    */
specifier|public
name|LockComponentBuilder
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|component
operator|.
name|setTablename
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|tableNameSet
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set the partition name.    * @param partitionName partition name    * @return reference to this builder    */
specifier|public
name|LockComponentBuilder
name|setPartitionName
parameter_list|(
name|String
name|partitionName
parameter_list|)
block|{
name|component
operator|.
name|setPartitionname
argument_list|(
name|partitionName
argument_list|)
expr_stmt|;
name|partNameSet
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LockComponentBuilder
name|setIsDynamicPartitionWrite
parameter_list|(
name|boolean
name|t
parameter_list|)
block|{
name|component
operator|.
name|setIsDynamicPartitionWrite
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get the constructed lock component.    * @return lock component.    */
specifier|public
name|LockComponent
name|build
parameter_list|()
block|{
name|LockLevel
name|level
init|=
name|LockLevel
operator|.
name|DB
decl_stmt|;
if|if
condition|(
name|tableNameSet
condition|)
name|level
operator|=
name|LockLevel
operator|.
name|TABLE
expr_stmt|;
if|if
condition|(
name|partNameSet
condition|)
name|level
operator|=
name|LockLevel
operator|.
name|PARTITION
expr_stmt|;
name|component
operator|.
name|setLevel
argument_list|(
name|level
argument_list|)
expr_stmt|;
return|return
name|component
return|;
block|}
block|}
end_class

end_unit

