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
name|List
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
name|ColumnInfo
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
name|GroupByOperator
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
name|NodeProcessor
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
name|plan
operator|.
name|ColStatistics
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
name|GroupByDesc
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
name|GroupByDesc
operator|.
name|Mode
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
name|Statistics
operator|.
name|State
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
name|stats
operator|.
name|StatsUtils
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
comment|/**  * SetHashGroupByMinReduction determines the min reduction to perform  * a hash aggregation for a group by.  */
end_comment

begin_class
specifier|public
class|class
name|SetHashGroupByMinReduction
implements|implements
name|NodeProcessor
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
name|SetHashGroupByMinReduction
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procContext
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GroupByOperator
name|groupByOperator
init|=
operator|(
name|GroupByOperator
operator|)
name|nd
decl_stmt|;
name|GroupByDesc
name|desc
init|=
name|groupByOperator
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getMode
argument_list|()
operator|!=
name|Mode
operator|.
name|HASH
operator|||
name|groupByOperator
operator|.
name|getStatistics
argument_list|()
operator|.
name|getBasicStatsState
argument_list|()
operator|!=
name|State
operator|.
name|COMPLETE
operator|||
name|groupByOperator
operator|.
name|getStatistics
argument_list|()
operator|.
name|getColumnStatsState
argument_list|()
operator|!=
name|State
operator|.
name|COMPLETE
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// compute product of distinct values of grouping columns
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|colStats
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|desc
operator|.
name|getKeys
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ColumnInfo
name|ci
init|=
name|groupByOperator
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|colStats
operator|.
name|add
argument_list|(
name|groupByOperator
operator|.
name|getStatistics
argument_list|()
operator|.
name|getColumnStatisticsFromColName
argument_list|(
name|ci
operator|.
name|getInternalName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|ndvProduct
init|=
name|StatsUtils
operator|.
name|computeNDVGroupingColumns
argument_list|(
name|colStats
argument_list|,
name|groupByOperator
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStatistics
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// if ndvProduct is 0 then column stats state must be partial and we are missing
if|if
condition|(
name|ndvProduct
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|long
name|numRows
init|=
name|groupByOperator
operator|.
name|getStatistics
argument_list|()
operator|.
name|getNumRows
argument_list|()
decl_stmt|;
if|if
condition|(
name|ndvProduct
operator|>
name|numRows
condition|)
block|{
name|ndvProduct
operator|=
name|numRows
expr_stmt|;
block|}
comment|// change the min reduction for hash group by
name|float
name|defaultMinReductionHashAggrFactor
init|=
name|desc
operator|.
name|getMinReductionHashAggr
argument_list|()
decl_stmt|;
name|float
name|minReductionHashAggrFactor
init|=
literal|1f
operator|-
operator|(
operator|(
name|float
operator|)
name|ndvProduct
operator|/
name|numRows
operator|)
decl_stmt|;
if|if
condition|(
name|minReductionHashAggrFactor
operator|<
name|defaultMinReductionHashAggrFactor
condition|)
block|{
name|desc
operator|.
name|setMinReductionHashAggr
argument_list|(
name|minReductionHashAggrFactor
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Minimum reduction for hash group by operator {} set to {}"
argument_list|,
name|groupByOperator
argument_list|,
name|minReductionHashAggrFactor
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

