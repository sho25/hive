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
name|hooks
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
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
name|Partition
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

begin_comment
comment|/**  * This class encapsulates an object that is being written to by the query. This  * object may be a table, partition, dfs directory or a local directory.  */
end_comment

begin_class
specifier|public
class|class
name|WriteEntity
block|{
comment|/**    * The type of the write entity.    */
specifier|public
specifier|static
enum|enum
name|Type
block|{
name|TABLE
block|,
name|PARTITION
block|,
name|DFS_DIR
block|,
name|LOCAL_DIR
block|}
empty_stmt|;
comment|/**    * The type.    */
specifier|private
name|Type
name|typ
decl_stmt|;
comment|/**    * The table. This is null if this is a directory.    */
specifier|private
specifier|final
name|Table
name|t
decl_stmt|;
comment|/**    * The partition.This is null if this object is not a partition.    */
specifier|private
specifier|final
name|Partition
name|p
decl_stmt|;
comment|/**    * The directory if this is a directory.    */
specifier|private
specifier|final
name|String
name|d
decl_stmt|;
comment|/**    * Constructor for a table.    *     * @param t    *          Table that is written to.    */
specifier|public
name|WriteEntity
parameter_list|(
name|Table
name|t
parameter_list|)
block|{
name|d
operator|=
literal|null
expr_stmt|;
name|p
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|t
operator|=
name|t
expr_stmt|;
name|typ
operator|=
name|Type
operator|.
name|TABLE
expr_stmt|;
block|}
comment|/**    * Constructor for a partition.    *     * @param p    *          Partition that is written to.    */
specifier|public
name|WriteEntity
parameter_list|(
name|Partition
name|p
parameter_list|)
block|{
name|d
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
name|t
operator|=
name|p
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|typ
operator|=
name|Type
operator|.
name|PARTITION
expr_stmt|;
block|}
comment|/**    * Constructor for a file.    *     * @param d    *          The name of the directory that is being written to.    * @param islocal    *          Flag to decide whether this directory is local or in dfs.    */
specifier|public
name|WriteEntity
parameter_list|(
name|String
name|d
parameter_list|,
name|boolean
name|islocal
parameter_list|)
block|{
name|this
operator|.
name|d
operator|=
name|d
expr_stmt|;
name|p
operator|=
literal|null
expr_stmt|;
name|t
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|islocal
condition|)
block|{
name|typ
operator|=
name|Type
operator|.
name|LOCAL_DIR
expr_stmt|;
block|}
else|else
block|{
name|typ
operator|=
name|Type
operator|.
name|DFS_DIR
expr_stmt|;
block|}
block|}
comment|/**    * Get the type of the entity.    */
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|typ
return|;
block|}
comment|/**    * Get the location of the entity.    */
specifier|public
name|URI
name|getLocation
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|TABLE
condition|)
block|{
return|return
name|t
operator|.
name|getDataLocation
argument_list|()
return|;
block|}
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|PARTITION
condition|)
block|{
return|return
name|p
operator|.
name|getDataLocation
argument_list|()
return|;
block|}
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|DFS_DIR
operator|||
name|typ
operator|==
name|Type
operator|.
name|LOCAL_DIR
condition|)
block|{
return|return
operator|new
name|URI
argument_list|(
name|d
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Get the partition associated with the entity.    */
specifier|public
name|Partition
name|getPartition
parameter_list|()
block|{
return|return
name|p
return|;
block|}
comment|/**    * Get the table associated with the entity.    */
specifier|public
name|Table
name|getTable
parameter_list|()
block|{
return|return
name|t
return|;
block|}
comment|/**    * toString function.    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
switch|switch
condition|(
name|typ
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|t
operator|.
name|getDbName
argument_list|()
operator|+
literal|"@"
operator|+
name|t
operator|.
name|getName
argument_list|()
return|;
case|case
name|PARTITION
case|:
return|return
name|t
operator|.
name|getDbName
argument_list|()
operator|+
literal|"@"
operator|+
name|t
operator|.
name|getName
argument_list|()
operator|+
literal|"@"
operator|+
name|p
operator|.
name|getName
argument_list|()
return|;
default|default:
return|return
name|d
return|;
block|}
block|}
comment|/**    * Equals function.    */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|WriteEntity
condition|)
block|{
name|WriteEntity
name|ore
init|=
operator|(
name|WriteEntity
operator|)
name|o
decl_stmt|;
return|return
operator|(
name|toString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|ore
operator|.
name|toString
argument_list|()
argument_list|)
operator|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
comment|/**    * Hashcode function.    */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|toString
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

