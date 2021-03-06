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
operator|.
name|tools
operator|.
name|metatool
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
name|ObjectStore
import|;
end_import

begin_class
specifier|abstract
class|class
name|MetaToolTask
block|{
specifier|private
name|ObjectStore
name|objectStore
decl_stmt|;
specifier|private
name|HiveMetaToolCommandLine
name|cl
decl_stmt|;
specifier|abstract
name|void
name|execute
parameter_list|()
function_decl|;
name|void
name|setObjectStore
parameter_list|(
name|ObjectStore
name|objectStore
parameter_list|)
block|{
name|this
operator|.
name|objectStore
operator|=
name|objectStore
expr_stmt|;
block|}
specifier|protected
name|ObjectStore
name|getObjectStore
parameter_list|()
block|{
return|return
name|objectStore
return|;
block|}
name|void
name|setCommandLine
parameter_list|(
name|HiveMetaToolCommandLine
name|cl
parameter_list|)
block|{
name|this
operator|.
name|cl
operator|=
name|cl
expr_stmt|;
block|}
specifier|protected
name|HiveMetaToolCommandLine
name|getCl
parameter_list|()
block|{
return|return
name|cl
return|;
block|}
block|}
end_class

end_unit

