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
name|metastore
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
name|MetaException
import|;
end_import

begin_comment
comment|/**  * The proxy interface that metastore uses to manipulate and apply  * serialized filter expressions coming from client.  */
end_comment

begin_interface
specifier|public
interface|interface
name|PartitionExpressionProxy
block|{
comment|/**    * Converts serialized Hive expression into filter in the format suitable for Filter.g.    * @param expr Serialized expression.    * @return The filter string.    */
specifier|public
name|String
name|convertExprToFilter
parameter_list|(
name|byte
index|[]
name|expr
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Filters the partition names via serialized Hive expression.    * @param columnNames Partition column names in the underlying table.    * @param expr Serialized expression.    * @param defaultPartitionName Default partition name from job or server configuration.    * @param partitionNames Partition names; the list is modified in place.    * @return Whether there were any unknown partitions preserved in the name list.    */
specifier|public
name|boolean
name|filterPartitionsByExpr
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|byte
index|[]
name|expr
parameter_list|,
name|String
name|defaultPartitionName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partitionNames
parameter_list|)
throws|throws
name|MetaException
function_decl|;
block|}
end_interface

end_unit

