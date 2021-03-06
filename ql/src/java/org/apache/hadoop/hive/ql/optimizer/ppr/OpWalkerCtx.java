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
name|optimizer
operator|.
name|ppr
package|;
end_package

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
name|TableScanOperator
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
name|ExprNodeDesc
import|;
end_import

begin_comment
comment|/**  * Context class for operator tree walker for partition pruner.  * TODO: this class may be not useful.  */
end_comment

begin_class
specifier|public
class|class
name|OpWalkerCtx
implements|implements
name|NodeProcessorCtx
block|{
comment|/**    * Map from tablescan operator to partition pruning predicate that is    * initialized from the ParseContext.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|ExprNodeDesc
argument_list|>
name|opToPartPruner
decl_stmt|;
comment|/**    * Constructor.    */
specifier|public
name|OpWalkerCtx
parameter_list|(
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|ExprNodeDesc
argument_list|>
name|opToPartPruner
parameter_list|)
block|{
name|this
operator|.
name|opToPartPruner
operator|=
name|opToPartPruner
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|ExprNodeDesc
argument_list|>
name|getOpToPartPruner
parameter_list|()
block|{
return|return
name|opToPartPruner
return|;
block|}
block|}
end_class

end_unit

