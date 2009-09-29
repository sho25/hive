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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_comment
comment|/**  * This class encapsulates the information on the partition and  * tables that are read by the query.  */
end_comment

begin_class
specifier|public
class|class
name|ReadEntity
block|{
comment|/**    * The partition. This is null for a non partitioned table.    */
specifier|private
name|Partition
name|p
decl_stmt|;
comment|/**    * The table.    */
specifier|private
name|Table
name|t
decl_stmt|;
comment|/**    * Constructor.    *    * @param t The Table that the query reads from.    */
specifier|public
name|ReadEntity
parameter_list|(
name|Table
name|t
parameter_list|)
block|{
name|this
operator|.
name|t
operator|=
name|t
expr_stmt|;
name|this
operator|.
name|p
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Constructor given a partiton.    *    * @param p The partition that the query reads from.    */
specifier|public
name|ReadEntity
parameter_list|(
name|Partition
name|p
parameter_list|)
block|{
name|this
operator|.
name|t
operator|=
name|p
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
block|}
comment|/**    * Enum that tells what time of a read entity this is.    */
specifier|public
specifier|static
enum|enum
name|Type
block|{
name|TABLE
block|,
name|PARTITION
block|}
empty_stmt|;
comment|/**    * Get the type.    */
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|p
operator|==
literal|null
condition|?
name|Type
operator|.
name|TABLE
else|:
name|Type
operator|.
name|PARTITION
return|;
block|}
comment|/**    * Get the parameter map of the Entity.    */
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
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
return|return
name|p
operator|.
name|getTPartition
argument_list|()
operator|.
name|getParameters
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|t
operator|.
name|getTTable
argument_list|()
operator|.
name|getParameters
argument_list|()
return|;
block|}
block|}
comment|/**    * Get the location of the entity.    */
specifier|public
name|URI
name|getLocation
parameter_list|()
block|{
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
return|return
name|p
operator|.
name|getDataLocation
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|t
operator|.
name|getDataLocation
argument_list|()
return|;
block|}
block|}
comment|/**    * Get partition entity.    */
specifier|public
name|Partition
name|getPartition
parameter_list|()
block|{
return|return
name|p
return|;
block|}
comment|/**    * Get table entity.    */
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
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
return|return
name|p
operator|.
name|getTable
argument_list|()
operator|.
name|getDbName
argument_list|()
operator|+
literal|"@"
operator|+
name|p
operator|.
name|getTable
argument_list|()
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
block|}
else|else
block|{
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
return|return
literal|false
return|;
if|if
condition|(
name|o
operator|instanceof
name|ReadEntity
condition|)
block|{
name|ReadEntity
name|ore
init|=
operator|(
name|ReadEntity
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
return|return
literal|false
return|;
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

