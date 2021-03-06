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
operator|.
name|jdbc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|adapter
operator|.
name|jdbc
operator|.
name|JdbcRules
operator|.
name|JdbcFilter
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
name|adapter
operator|.
name|jdbc
operator|.
name|JdbcRules
operator|.
name|JdbcFilterRule
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
name|Filter
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
name|RexNode
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
name|HiveFilter
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
name|jdbc
operator|.
name|HiveJdbcConverter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * JDBCExtractJoinFilterRule extracts out the  * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveFilter}  * from a {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveJoin} operator.  * if the HiveFilter could be replaced by two HiveFilter operators that one of them could be pushed down below the  * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.jdbc.HiveJdbcConverter}  */
end_comment

begin_class
specifier|public
class|class
name|JDBCFilterPushDownRule
extends|extends
name|RelOptRule
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JDBCFilterPushDownRule
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JDBCFilterPushDownRule
name|INSTANCE
init|=
operator|new
name|JDBCFilterPushDownRule
argument_list|()
decl_stmt|;
specifier|public
name|JDBCFilterPushDownRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveFilter
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveJdbcConverter
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
specifier|final
name|HiveFilter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|HiveJdbcConverter
name|converter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|RexNode
name|cond
init|=
name|filter
operator|.
name|getCondition
argument_list|()
decl_stmt|;
return|return
name|JDBCRexCallValidator
operator|.
name|isValidJdbcOperation
argument_list|(
name|cond
argument_list|,
name|converter
operator|.
name|getJdbcDialect
argument_list|()
argument_list|)
return|;
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"JDBCFilterPushDown has been called"
argument_list|)
expr_stmt|;
specifier|final
name|HiveFilter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|HiveJdbcConverter
name|converter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Filter
name|newHiveFilter
init|=
name|filter
operator|.
name|copy
argument_list|(
name|filter
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|converter
operator|.
name|getInput
argument_list|()
argument_list|,
name|filter
operator|.
name|getCondition
argument_list|()
argument_list|)
decl_stmt|;
name|JdbcFilter
name|newJdbcFilter
init|=
operator|(
name|JdbcFilter
operator|)
operator|new
name|JdbcFilterRule
argument_list|(
name|converter
operator|.
name|getJdbcConvention
argument_list|()
argument_list|)
operator|.
name|convert
argument_list|(
name|newHiveFilter
argument_list|)
decl_stmt|;
if|if
condition|(
name|newJdbcFilter
operator|!=
literal|null
condition|)
block|{
name|RelNode
name|converterRes
init|=
name|converter
operator|.
name|copy
argument_list|(
name|converter
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|newJdbcFilter
argument_list|)
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|converterRes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

begin_empty_stmt
empty_stmt|;
end_empty_stmt

end_unit

