begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|messaging
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

begin_comment
comment|/**  * The HCat message sent when partition(s) are added to a table.  * @deprecated Use/modify {@link org.apache.hive.hcatalog.messaging.AddPartitionMessage} instead  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AddPartitionMessage
extends|extends
name|HCatEventMessage
block|{
specifier|protected
name|AddPartitionMessage
parameter_list|()
block|{
name|super
argument_list|(
name|EventType
operator|.
name|ADD_PARTITION
argument_list|)
expr_stmt|;
block|}
comment|/**      * Getter for name of table (where partitions are added).      * @return Table-name (String).      */
specifier|public
specifier|abstract
name|String
name|getTable
parameter_list|()
function_decl|;
comment|/**      * Getter for list of partitions added.      * @return List of maps, where each map identifies values for each partition-key, for every added partition.      */
specifier|public
specifier|abstract
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|getPartitions
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|HCatEventMessage
name|checkValid
parameter_list|()
block|{
if|if
condition|(
name|getTable
argument_list|()
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Table name unset."
argument_list|)
throw|;
if|if
condition|(
name|getPartitions
argument_list|()
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Partition-list unset."
argument_list|)
throw|;
return|return
name|super
operator|.
name|checkValid
argument_list|()
return|;
block|}
block|}
end_class

end_unit

