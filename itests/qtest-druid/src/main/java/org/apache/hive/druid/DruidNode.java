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
name|druid
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|DruidNode
implements|implements
name|Closeable
block|{
specifier|private
specifier|final
name|String
name|nodeType
decl_stmt|;
specifier|public
name|DruidNode
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
name|this
operator|.
name|nodeType
operator|=
name|nodeId
expr_stmt|;
block|}
specifier|final
specifier|public
name|String
name|getNodeType
parameter_list|()
block|{
return|return
name|nodeType
return|;
block|}
comment|/**    * starts the druid node    */
specifier|public
specifier|abstract
name|void
name|start
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return true if the process is working    */
specifier|public
specifier|abstract
name|boolean
name|isAlive
parameter_list|()
function_decl|;
block|}
end_class

end_unit

