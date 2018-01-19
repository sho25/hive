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
name|rules
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptRuleCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Aggregate
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Intersect
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Project
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexUtil
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveIntersect
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveProject
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
name|base
operator|.
name|Predicate
import|;
end_import

begin_comment
comment|/**  * HiveProjectOverIntersectRemoveRule removes a HiveProject over another  * HiveIntersect, provided the projects aren't projecting identical sets of  * input references.  */
end_comment

begin_class
specifier|public
class|class
name|HiveProjectOverIntersectRemoveRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveProjectOverIntersectRemoveRule
name|INSTANCE
init|=
operator|new
name|HiveProjectOverIntersectRemoveRule
argument_list|()
decl_stmt|;
comment|// ~ Constructors -----------------------------------------------------------
comment|/** Creates a HiveProjectOverIntersectRemoveRule. */
specifier|private
name|HiveProjectOverIntersectRemoveRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveIntersect
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// ~ Methods ----------------------------------------------------------------
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
name|Project
name|project
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Intersect
name|intersect
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
return|return
name|isTrivial
argument_list|(
name|project
argument_list|,
name|intersect
argument_list|)
return|;
block|}
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
name|call
operator|.
name|transformTo
argument_list|(
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|boolean
name|isTrivial
parameter_list|(
name|Project
name|project
parameter_list|,
name|Intersect
name|intersect
parameter_list|)
block|{
return|return
name|RexUtil
operator|.
name|isIdentity
argument_list|(
name|project
operator|.
name|getProjects
argument_list|()
argument_list|,
name|intersect
operator|.
name|getRowType
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

begin_comment
comment|// End HiveProjectOverIntersectRemoveRule.java
end_comment

end_unit

