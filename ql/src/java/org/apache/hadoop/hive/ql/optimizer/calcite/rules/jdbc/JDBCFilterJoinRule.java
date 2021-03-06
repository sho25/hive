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
name|rel
operator|.
name|core
operator|.
name|Join
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
name|HiveRelFactories
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
name|HiveJoin
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
name|rules
operator|.
name|HiveFilterJoinRule
import|;
end_import

begin_comment
comment|/**  * Rule that tries to push filter expressions into a join condition and into  * the inputs of the join.  */
end_comment

begin_class
specifier|public
class|class
name|JDBCFilterJoinRule
extends|extends
name|HiveFilterJoinRule
block|{
specifier|public
specifier|static
specifier|final
name|JDBCFilterJoinRule
name|INSTANCE
init|=
operator|new
name|JDBCFilterJoinRule
argument_list|()
decl_stmt|;
specifier|public
name|JDBCFilterJoinRule
parameter_list|()
block|{
name|super
argument_list|(
name|RelOptRule
operator|.
name|operand
argument_list|(
name|HiveFilter
operator|.
name|class
argument_list|,
name|RelOptRule
operator|.
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|RelOptRule
operator|.
name|operand
argument_list|(
name|HiveJdbcConverter
operator|.
name|class
argument_list|,
name|RelOptRule
operator|.
name|any
argument_list|()
argument_list|)
argument_list|,
name|RelOptRule
operator|.
name|operand
argument_list|(
name|HiveJdbcConverter
operator|.
name|class
argument_list|,
name|RelOptRule
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|,
literal|"JDBCFilterJoinRule"
argument_list|,
literal|true
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
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
name|Filter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Join
name|join
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|HiveJdbcConverter
name|conv1
init|=
name|call
operator|.
name|rel
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|HiveJdbcConverter
name|conv2
init|=
name|call
operator|.
name|rel
argument_list|(
literal|3
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|conv1
operator|.
name|getJdbcDialect
argument_list|()
operator|.
name|equals
argument_list|(
name|conv2
operator|.
name|getJdbcDialect
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|boolean
name|visitorRes
init|=
name|JDBCRexCallValidator
operator|.
name|isValidJdbcOperation
argument_list|(
name|filter
operator|.
name|getCondition
argument_list|()
argument_list|,
name|conv1
operator|.
name|getJdbcDialect
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|visitorRes
condition|)
block|{
return|return
name|JDBCRexCallValidator
operator|.
name|isValidJdbcOperation
argument_list|(
name|join
operator|.
name|getCondition
argument_list|()
argument_list|,
name|conv1
operator|.
name|getJdbcDialect
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|false
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
name|Filter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Join
name|join
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|super
operator|.
name|perform
argument_list|(
name|call
argument_list|,
name|filter
argument_list|,
name|join
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

