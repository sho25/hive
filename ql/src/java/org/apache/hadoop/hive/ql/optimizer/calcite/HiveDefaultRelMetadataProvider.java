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
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidQuery
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
name|JdbcAggregate
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
name|JdbcJoin
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
name|JdbcProject
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
name|JdbcSort
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
name|JdbcUnion
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
name|hep
operator|.
name|HepRelVertex
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
name|volcano
operator|.
name|AbstractConverter
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
name|volcano
operator|.
name|RelSubset
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
name|AbstractRelNode
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
name|convert
operator|.
name|ConverterImpl
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
name|JaninoRelMetadataProvider
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
name|reloperators
operator|.
name|HiveAggregate
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
name|HiveExcept
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
name|reloperators
operator|.
name|HiveMultiJoin
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
name|HiveRelNode
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
name|HiveSemiJoin
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
name|HiveSortExchange
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
name|HiveSortLimit
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
name|HiveTableFunctionScan
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
name|HiveTableScan
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
name|HiveUnion
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
name|jdbc
operator|.
name|JdbcHiveTableScan
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
name|HiveRelMdColumnUniqueness
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
name|HiveRelMdCumulativeCost
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
name|HiveRelMdRuntimeRowCount
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
comment|/**    * The default metadata provider can be instantiated statically since    * it does not need any parameter specified by user (hive conf).    */
specifier|private
specifier|static
specifier|final
name|JaninoRelMetadataProvider
name|DEFAULT
init|=
name|JaninoRelMetadataProvider
operator|.
name|of
argument_list|(
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
name|HiveRelMdCumulativeCost
operator|.
name|SOURCE
argument_list|,
operator|new
name|HiveRelMdCost
argument_list|(
name|HiveDefaultCostModel
operator|.
name|getCostModel
argument_list|()
argument_list|)
operator|.
name|getMetadataProvider
argument_list|()
argument_list|,
name|HiveRelMdSelectivity
operator|.
name|SOURCE
argument_list|,
name|HiveRelMdRuntimeRowCount
operator|.
name|SOURCE
argument_list|,
name|HiveRelMdUniqueKeys
operator|.
name|SOURCE
argument_list|,
name|HiveRelMdColumnUniqueness
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
name|JaninoRelMetadataProvider
operator|.
name|DEFAULT
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RelMetadataProvider
name|metadataProvider
decl_stmt|;
specifier|public
name|HiveDefaultRelMetadataProvider
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|RelNode
argument_list|>
argument_list|>
name|nodeClasses
parameter_list|)
block|{
name|this
operator|.
name|metadataProvider
operator|=
name|init
argument_list|(
name|hiveConf
argument_list|,
name|nodeClasses
argument_list|)
expr_stmt|;
block|}
specifier|private
name|RelMetadataProvider
name|init
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|RelNode
argument_list|>
argument_list|>
name|nodeClasses
parameter_list|)
block|{
comment|// Create cost metadata provider
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
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
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMAXSPLITSIZE
argument_list|)
decl_stmt|;
comment|// Create and return metadata provider
name|JaninoRelMetadataProvider
name|metadataProvider
init|=
name|JaninoRelMetadataProvider
operator|.
name|of
argument_list|(
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
name|HiveRelMdCumulativeCost
operator|.
name|SOURCE
argument_list|,
operator|new
name|HiveRelMdCost
argument_list|(
name|HiveOnTezCostModel
operator|.
name|getCostModel
argument_list|(
name|hiveConf
argument_list|)
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
name|HiveRelMdColumnUniqueness
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
name|JaninoRelMetadataProvider
operator|.
name|DEFAULT
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeClasses
operator|!=
literal|null
condition|)
block|{
comment|// If classes were passed, pre-register them
name|metadataProvider
operator|.
name|register
argument_list|(
name|nodeClasses
argument_list|)
expr_stmt|;
block|}
return|return
name|metadataProvider
return|;
block|}
return|return
name|DEFAULT
return|;
block|}
specifier|public
name|RelMetadataProvider
name|getMetadataProvider
parameter_list|()
block|{
return|return
name|metadataProvider
return|;
block|}
comment|/**    * This method can be called at startup time to pre-register all the    * additional Hive classes (compared to Calcite core classes) that may    * be visited during the planning phase.    */
specifier|public
specifier|static
name|void
name|initializeMetadataProviderClass
parameter_list|(
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|RelNode
argument_list|>
argument_list|>
name|nodeClasses
parameter_list|)
block|{
comment|// This will register the classes in the default Janino implementation
name|JaninoRelMetadataProvider
operator|.
name|DEFAULT
operator|.
name|register
argument_list|(
name|nodeClasses
argument_list|)
expr_stmt|;
comment|// This will register the classes in the default Hive implementation
name|DEFAULT
operator|.
name|register
argument_list|(
name|nodeClasses
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

