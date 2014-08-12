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
name|optimizer
operator|.
name|optiq
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
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|optiq
operator|.
name|RelOptHiveTable
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
name|optiq
operator|.
name|reloperators
operator|.
name|HiveFilterRel
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
name|optiq
operator|.
name|reloperators
operator|.
name|HiveTableScanRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|FilterRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptRuleCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|util
operator|.
name|Pair
import|;
end_import

begin_class
specifier|public
class|class
name|HivePartitionPrunerRule
extends|extends
name|RelOptRule
block|{
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|HivePartitionPrunerRule
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveFilterRel
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveTableScanRel
operator|.
name|class
argument_list|,
name|none
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
name|HiveFilterRel
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HiveTableScanRel
name|tScan
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|perform
argument_list|(
name|call
argument_list|,
name|filter
argument_list|,
name|tScan
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|perform
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|,
name|FilterRelBase
name|filter
parameter_list|,
name|HiveTableScanRel
name|tScan
parameter_list|)
block|{
name|RelOptHiveTable
name|hiveTable
init|=
operator|(
name|RelOptHiveTable
operator|)
name|tScan
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|RexNode
name|predicate
init|=
name|filter
operator|.
name|getCondition
argument_list|()
decl_stmt|;
name|Pair
argument_list|<
name|RexNode
argument_list|,
name|RexNode
argument_list|>
name|predicates
init|=
name|PartitionPruner
operator|.
name|extractPartitionPredicates
argument_list|(
name|filter
operator|.
name|getCluster
argument_list|()
argument_list|,
name|hiveTable
argument_list|,
name|predicate
argument_list|)
decl_stmt|;
name|RexNode
name|partColExpr
init|=
name|predicates
operator|.
name|left
decl_stmt|;
name|RexNode
name|remainingExpr
init|=
name|predicates
operator|.
name|right
decl_stmt|;
name|remainingExpr
operator|=
name|remainingExpr
operator|==
literal|null
condition|?
name|filter
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeLiteral
argument_list|(
literal|true
argument_list|)
else|:
name|remainingExpr
expr_stmt|;
name|hiveTable
operator|.
name|computePartitionList
argument_list|(
name|conf
argument_list|,
name|partColExpr
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

