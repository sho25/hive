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
name|calcite
operator|.
name|translator
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
name|parse
operator|.
name|JoinType
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
name|TypeCheckCtx
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_comment
comment|/**  * JoinTypeCheckCtx is used by Calcite planner(CBO) to generate Join Conditions from Join Condition AST.  * Reasons for sub class:  * 1. Join Conditions can not handle:  *    a. Stateful Functions  *    b. Distinct  *    c. '*' expr  *    d. '.*' expr  *    e. Windowing expr  *    f. Complex type member access  *    g. Array Index Access  *    h. Sub query  *    i. GB expr elimination  * 2. Join Condn expr has two input RR as opposed to one.  */
end_comment

begin_comment
comment|/**  * TODO:<br>  * 1. Could we use combined RR instead of list of RR ?<br>  * 2. Why not use GB expr ?  */
end_comment

begin_class
specifier|public
class|class
name|JoinTypeCheckCtx
extends|extends
name|TypeCheckCtx
block|{
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|RowResolver
argument_list|>
name|inputRRLst
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|outerJoin
decl_stmt|;
specifier|public
name|JoinTypeCheckCtx
parameter_list|(
name|RowResolver
name|leftRR
parameter_list|,
name|RowResolver
name|rightRR
parameter_list|,
name|JoinType
name|hiveJoinType
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|RowResolver
operator|.
name|getCombinedRR
argument_list|(
name|leftRR
argument_list|,
name|rightRR
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|inputRRLst
operator|=
name|ImmutableList
operator|.
name|of
argument_list|(
name|leftRR
argument_list|,
name|rightRR
argument_list|)
expr_stmt|;
name|this
operator|.
name|outerJoin
operator|=
operator|(
name|hiveJoinType
operator|==
name|JoinType
operator|.
name|LEFTOUTER
operator|)
operator|||
operator|(
name|hiveJoinType
operator|==
name|JoinType
operator|.
name|RIGHTOUTER
operator|)
operator|||
operator|(
name|hiveJoinType
operator|==
name|JoinType
operator|.
name|FULLOUTER
operator|)
expr_stmt|;
block|}
comment|/**    * @return the inputRR List    */
specifier|public
name|List
argument_list|<
name|RowResolver
argument_list|>
name|getInputRRList
parameter_list|()
block|{
return|return
name|inputRRLst
return|;
block|}
specifier|public
name|boolean
name|isOuterJoin
parameter_list|()
block|{
return|return
name|outerJoin
return|;
block|}
block|}
end_class

end_unit

