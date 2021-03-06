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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
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
name|hive
operator|.
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

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
name|TableTypeMapping
block|{
comment|/**    * Map client's table type name to hive's table type    * @param clientTypeName    * @return    */
specifier|public
name|String
index|[]
name|mapToHiveType
parameter_list|(
name|String
name|clientTypeName
parameter_list|)
function_decl|;
comment|/**    * Map hive's table type name to client's table type    * @param hiveTypeName    * @return    */
specifier|public
name|String
name|mapToClientType
parameter_list|(
name|String
name|hiveTypeName
parameter_list|)
function_decl|;
comment|/**    * Get all the table types of this mapping    * @return    */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getTableTypeNames
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

