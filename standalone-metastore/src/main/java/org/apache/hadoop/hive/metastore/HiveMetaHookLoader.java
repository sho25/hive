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
name|Table
import|;
end_import

begin_comment
comment|/**  * HiveMetaHookLoader is responsible for loading a {@link HiveMetaHook}  * for a given table.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveMetaHookLoader
block|{
comment|/**    * Loads a hook for the specified table.    *    * @param tbl table of interest    *    * @return hook, or null if none registered    */
specifier|public
name|HiveMetaHook
name|getHook
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|MetaException
function_decl|;
block|}
end_interface

begin_comment
comment|// End HiveMetaHookLoader.java
end_comment

end_unit

