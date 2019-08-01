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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|metastore
operator|.
name|api
operator|.
name|Table
import|;
end_import

begin_comment
comment|/**  * This interface provides a way for users to implement a custom metadata transformer for tables/partitions.  * This transformer can match and manipulate the data to be returned to the data processor  * The classes implementing this interface should be in the HMS classpath, if this configuration is turned on.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|IMetaStoreMetadataTransformer
block|{
comment|/**   * @param table A Table object to be transformed   * @param processorCapabilities A array of String capabilities received from the data processor   * @param processorId String ID used for logging purpose.   * @return Map A Map of transformed objects keyed by Table and value is list of required capabilities   * @throws HiveMetaException   */
comment|// TODO HiveMetaException or MetaException
specifier|public
name|Map
argument_list|<
name|Table
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|transform
parameter_list|(
name|List
argument_list|<
name|Table
argument_list|>
name|tables
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|processorCapabilities
parameter_list|,
name|String
name|processorId
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**   * @param parts A list of Partition objects to be transformed   * @param processorCapabilities A array of String capabilities received from the data processor   * @param processorId String ID used for logging purpose.   * @return Map A Map of transformed objects keyed by Partition and value is list of required capabilities   * @throws HiveMetaException   */
comment|// TODO HiveMetaException or MetaException
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|transformPartitions
parameter_list|(
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
parameter_list|,
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|processorCapabilities
parameter_list|,
name|String
name|processorId
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**   * @param table A table object to be transformed prior to the creation of the table   * @param processorCapabilities A array of String capabilities received from the data processor   * @param processorId String ID used for logging purpose.   * @return Table An altered Table based on the processor capabilities   * @throws HiveMetaException   */
specifier|public
name|Table
name|transformCreateTable
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|processorCapabilities
parameter_list|,
name|String
name|processorId
parameter_list|)
throws|throws
name|MetaException
function_decl|;
block|}
end_interface

end_unit

