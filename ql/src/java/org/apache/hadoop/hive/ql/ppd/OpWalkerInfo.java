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
name|ppd
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|Node
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
name|parse
operator|.
name|OpParseContext
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
name|RowResolver
import|;
end_import

begin_comment
comment|/**  * Context class for operator walker of predicate pushdown.  */
end_comment

begin_class
specifier|public
class|class
name|OpWalkerInfo
implements|implements
name|NodeProcessorCtx
block|{
comment|/**    * Operator to Pushdown Predicates Map. This keeps track of the final pushdown predicates    * for each operator as you walk the Op Graph from child to parent    */
specifier|private
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|ExprWalkerInfo
argument_list|>
name|opToPushdownPredMap
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|opToParseCtxMap
decl_stmt|;
specifier|public
name|OpWalkerInfo
parameter_list|(
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|opToParseCtxMap
parameter_list|)
block|{
name|this
operator|.
name|opToParseCtxMap
operator|=
name|opToParseCtxMap
expr_stmt|;
name|this
operator|.
name|opToPushdownPredMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|ExprWalkerInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ExprWalkerInfo
name|getPrunedPreds
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|)
block|{
return|return
name|opToPushdownPredMap
operator|.
name|get
argument_list|(
name|op
argument_list|)
return|;
block|}
specifier|public
name|ExprWalkerInfo
name|putPrunedPreds
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|,
name|ExprWalkerInfo
name|value
parameter_list|)
block|{
return|return
name|opToPushdownPredMap
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|value
argument_list|)
return|;
block|}
specifier|public
name|RowResolver
name|getRowResolver
parameter_list|(
name|Node
name|op
parameter_list|)
block|{
return|return
name|opToParseCtxMap
operator|.
name|get
argument_list|(
name|op
argument_list|)
operator|.
name|getRR
argument_list|()
return|;
block|}
specifier|public
name|OpParseContext
name|put
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|key
parameter_list|,
name|OpParseContext
name|value
parameter_list|)
block|{
return|return
name|opToParseCtxMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
block|}
end_class

end_unit

