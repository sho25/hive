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
name|ql
operator|.
name|lib
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|parse
operator|.
name|SemanticException
import|;
end_import

begin_comment
comment|/**  * Interface for operator graph walker.  */
end_comment

begin_interface
specifier|public
interface|interface
name|SemanticGraphWalker
extends|extends
name|GraphWalker
block|{
comment|/**    * starting point for walking.    *     * @param startNodes    *          list of starting operators    * @param nodeOutput    *          If this parameter is not null, the call to the function returns    *          the map from node to objects returned by the processors.    * @throws SemanticException    */
annotation|@
name|Override
name|void
name|startWalking
parameter_list|(
name|Collection
argument_list|<
name|Node
argument_list|>
name|startNodes
parameter_list|,
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|nodeOutput
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
block|}
end_interface

end_unit

