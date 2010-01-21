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
name|ReduceSinkOperator
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
name|DefaultGraphWalker
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
name|Dispatcher
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
name|Node
import|;
end_import

begin_comment
comment|/**  * Walks the operator tree in pre order fashion  */
end_comment

begin_class
specifier|public
class|class
name|GenMapRedWalker
extends|extends
name|DefaultGraphWalker
block|{
comment|/**    * constructor of the walker - the dispatcher is passed    *     * @param disp    *          the dispatcher to be called for each node visited    */
specifier|public
name|GenMapRedWalker
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
comment|/**    * Walk the given operator    *     * @param nd    *          operator being walked    */
annotation|@
name|Override
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
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|children
init|=
name|nd
operator|.
name|getChildren
argument_list|()
decl_stmt|;
comment|// maintain the stack of operators encountered
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
comment|// kids of reduce sink operator need not be traversed again
if|if
condition|(
operator|(
name|children
operator|==
literal|null
operator|)
operator|||
operator|(
operator|(
name|nd
operator|instanceof
name|ReduceSinkOperator
operator|)
operator|&&
operator|(
name|getDispatchedList
argument_list|()
operator|.
name|containsAll
argument_list|(
name|children
argument_list|)
operator|)
operator|)
condition|)
block|{
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// move all the children to the front of queue
for|for
control|(
name|Node
name|ch
range|:
name|children
control|)
block|{
name|walk
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
comment|// done with this operator
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

