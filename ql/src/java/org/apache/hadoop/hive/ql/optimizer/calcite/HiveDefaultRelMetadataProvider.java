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
name|calcite
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
name|rel
operator|.
name|metadata
operator|.
name|ChainedRelMetadataProvider
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
name|metadata
operator|.
name|DefaultRelMetadataProvider
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
name|metadata
operator|.
name|RelMetadataProvider
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
name|calcite
operator|.
name|cost
operator|.
name|HiveCostModel
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
name|cost
operator|.
name|HiveDefaultCostModel
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
name|cost
operator|.
name|HiveOnTezCostModel
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
name|cost
operator|.
name|HiveRelMdCost
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
name|stats
operator|.
name|HiveRelMdCollation
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
name|stats
operator|.
name|HiveRelMdDistinctRowCount
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
name|stats
operator|.
name|HiveRelMdDistribution
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
name|stats
operator|.
name|HiveRelMdMemory
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
name|stats
operator|.
name|HiveRelMdParallelism
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
name|stats
operator|.
name|HiveRelMdPredicates
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
name|stats
operator|.
name|HiveRelMdRowCount
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
name|stats
operator|.
name|HiveRelMdSelectivity
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
name|stats
operator|.
name|HiveRelMdSize
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
name|stats
operator|.
name|HiveRelMdUniqueKeys
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

begin_class
specifier|public
class|class
name|HiveDefaultRelMetadataProvider
block|{
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|public
name|HiveDefaultRelMetadataProvider
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
block|}
specifier|public
name|RelMetadataProvider
name|getMetadataProvider
parameter_list|()
block|{
comment|// Create cost metadata provider
specifier|final
name|HiveCostModel
name|cm
decl_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|this
operator|.
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|this
operator|.
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CBO_EXTENDED_COST_MODEL
argument_list|)
condition|)
block|{
name|cm
operator|=
name|HiveOnTezCostModel
operator|.
name|getCostModel
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cm
operator|=
name|HiveDefaultCostModel
operator|.
name|getCostModel
argument_list|()
expr_stmt|;
block|}
comment|// Get max split size for HiveRelMdParallelism
specifier|final
name|Double
name|maxSplitSize
init|=
operator|(
name|double
operator|)
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|this
operator|.
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMAXSPLITSIZE
argument_list|)
decl_stmt|;
comment|// Return MD provider
return|return
name|ChainedRelMetadataProvider
operator|.
name|of
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|HiveRelMdDistinctRowCount
operator|.
name|SOURCE
argument_list|,
operator|new
name|HiveRelMdCost
argument_list|(
name|cm
argument_list|)
operator|.
name|getMetadataProvider
argument_list|()
argument_list|,
name|HiveRelMdSelectivity
operator|.
name|SOURCE
argument_list|,
name|HiveRelMdRowCount
operator|.
name|SOURCE
argument_list|,
name|HiveRelMdUniqueKeys
operator|.
name|SOURCE
argument_list|,
name|HiveRelMdSize
operator|.
name|SOURCE
argument_list|,
name|HiveRelMdMemory
operator|.
name|SOURCE
argument_list|,
operator|new
name|HiveRelMdParallelism
argument_list|(
name|maxSplitSize
argument_list|)
operator|.
name|getMetadataProvider
argument_list|()
argument_list|,
name|HiveRelMdDistribution
operator|.
name|SOURCE
argument_list|,
name|HiveRelMdCollation
operator|.
name|SOURCE
argument_list|,
name|HiveRelMdPredicates
operator|.
name|SOURCE
argument_list|,
name|DefaultRelMetadataProvider
operator|.
name|INSTANCE
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

