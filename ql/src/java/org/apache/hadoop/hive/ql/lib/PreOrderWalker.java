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
name|lib
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|Object
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
comment|/**  * base class for operator graph walker  * this class takes list of starting ops and walks them one by one.  */
end_comment

begin_class
specifier|public
class|class
name|PreOrderWalker
extends|extends
name|DefaultGraphWalker
block|{
comment|/*     * Since the operator tree is a DAG, nodes with mutliple parents will be visited more than once.    * This can be made configurable.    */
comment|/**    * Constructor    * @param disp dispatcher to call for each op encountered    */
specifier|public
name|PreOrderWalker
parameter_list|(
name|Dispatcher
name|disp
parameter_list|)
block|{
name|super
argument_list|(
name|disp
argument_list|)
expr_stmt|;
block|}
comment|/**    * walk the current operator and its descendants    * @param nd current operator in the graph    * @throws SemanticException    */
specifier|public
name|void
name|walk
parameter_list|(
name|Node
name|nd
parameter_list|)
throws|throws
name|SemanticException
block|{
name|opStack
operator|.
name|push
argument_list|(
name|nd
argument_list|)
expr_stmt|;
name|dispatch
argument_list|(
name|nd
argument_list|,
name|opStack
argument_list|)
expr_stmt|;
comment|// move all the children to the front of queue
if|if
condition|(
name|nd
operator|.
name|getChildren
argument_list|()
operator|!=
literal|null
condition|)
for|for
control|(
name|Node
name|n
range|:
name|nd
operator|.
name|getChildren
argument_list|()
control|)
name|walk
argument_list|(
name|n
argument_list|)
expr_stmt|;
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

