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
name|parse
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Operator
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|plan
operator|.
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * This class implements the context information that is used for typechecking  * phase in query compilation.  */
end_comment

begin_class
specifier|public
class|class
name|TableAccessCtx
implements|implements
name|NodeProcessorCtx
block|{
comment|/**    * Map of operator id to table and key names.    */
specifier|private
specifier|final
name|TableAccessInfo
name|tableAccessInfo
decl_stmt|;
comment|/**    * Constructor.    */
specifier|public
name|TableAccessCtx
parameter_list|()
block|{
name|tableAccessInfo
operator|=
operator|new
name|TableAccessInfo
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TableAccessInfo
name|getTableAccessInfo
parameter_list|()
block|{
return|return
name|tableAccessInfo
return|;
block|}
specifier|public
name|void
name|addOperatorTableAccess
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableToKeysMap
parameter_list|)
block|{
assert|assert
operator|(
name|tableToKeysMap
operator|!=
literal|null
operator|)
assert|;
assert|assert
operator|(
name|op
operator|!=
literal|null
operator|)
assert|;
name|tableAccessInfo
operator|.
name|add
argument_list|(
name|op
argument_list|,
name|tableToKeysMap
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

