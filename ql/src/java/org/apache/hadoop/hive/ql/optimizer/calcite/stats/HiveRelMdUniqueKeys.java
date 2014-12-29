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
operator|.
name|stats
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|BitSet
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
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|RelOptUtil
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
name|rel
operator|.
name|metadata
operator|.
name|BuiltInMetadata
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
name|Metadata
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
name|ReflectiveRelMetadataProvider
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
name|RelMdUniqueKeys
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
name|calcite
operator|.
name|rex
operator|.
name|RexInputRef
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
name|calcite
operator|.
name|util
operator|.
name|BitSets
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
name|util
operator|.
name|BuiltInMethod
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
name|util
operator|.
name|ImmutableBitSet
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
name|plan
operator|.
name|ColStatistics
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
name|Function
import|;
end_import

begin_class
specifier|public
class|class
name|HiveRelMdUniqueKeys
block|{
specifier|public
specifier|static
specifier|final
name|RelMetadataProvider
name|SOURCE
init|=
name|ReflectiveRelMetadataProvider
operator|.
name|reflectiveSource
argument_list|(
name|BuiltInMethod
operator|.
name|UNIQUE_KEYS
operator|.
name|method
argument_list|,
operator|new
name|HiveRelMdUniqueKeys
argument_list|()
argument_list|)
decl_stmt|;
comment|/*    * Infer Uniquenes if: - rowCount(col) = ndv(col) - TBD for numerics: max(col)    * - min(col) = rowCount(col)    *     * Why are we intercepting Project and not TableScan? Because if we    * have a method for TableScan, it will not know which columns to check for.    * Inferring Uniqueness for all columns is very expensive right now. The flip    * side of doing this is, it only works post Field Trimming.    */
specifier|public
name|Set
argument_list|<
name|ImmutableBitSet
argument_list|>
name|getUniqueKeys
parameter_list|(
name|Project
name|rel
parameter_list|,
name|boolean
name|ignoreNulls
parameter_list|)
block|{
name|HiveTableScan
name|tScan
init|=
name|getTableScan
argument_list|(
name|rel
operator|.
name|getInput
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|tScan
operator|==
literal|null
condition|)
block|{
name|Function
argument_list|<
name|RelNode
argument_list|,
name|Metadata
argument_list|>
name|fn
init|=
name|RelMdUniqueKeys
operator|.
name|SOURCE
operator|.
name|apply
argument_list|(
name|rel
operator|.
name|getClass
argument_list|()
argument_list|,
name|BuiltInMetadata
operator|.
name|UniqueKeys
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
operator|(
operator|(
name|BuiltInMetadata
operator|.
name|UniqueKeys
operator|)
name|fn
operator|.
name|apply
argument_list|(
name|rel
argument_list|)
operator|)
operator|.
name|getUniqueKeys
argument_list|(
name|ignoreNulls
argument_list|)
return|;
block|}
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|posMap
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|projectPos
init|=
literal|0
decl_stmt|;
name|int
name|colStatsPos
init|=
literal|0
decl_stmt|;
name|BitSet
name|projectedCols
init|=
operator|new
name|BitSet
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|r
range|:
name|rel
operator|.
name|getProjects
argument_list|()
control|)
block|{
if|if
condition|(
name|r
operator|instanceof
name|RexInputRef
condition|)
block|{
name|projectedCols
operator|.
name|set
argument_list|(
operator|(
operator|(
name|RexInputRef
operator|)
name|r
operator|)
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|posMap
operator|.
name|put
argument_list|(
name|colStatsPos
argument_list|,
name|projectPos
argument_list|)
expr_stmt|;
name|colStatsPos
operator|++
expr_stmt|;
block|}
name|projectPos
operator|++
expr_stmt|;
block|}
name|double
name|numRows
init|=
name|tScan
operator|.
name|getRows
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|colStats
init|=
name|tScan
operator|.
name|getColStat
argument_list|(
name|BitSets
operator|.
name|toList
argument_list|(
name|projectedCols
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ImmutableBitSet
argument_list|>
name|keys
init|=
operator|new
name|HashSet
argument_list|<
name|ImmutableBitSet
argument_list|>
argument_list|()
decl_stmt|;
name|colStatsPos
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|ColStatistics
name|cStat
range|:
name|colStats
control|)
block|{
name|boolean
name|isKey
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|cStat
operator|.
name|getCountDistint
argument_list|()
operator|>=
name|numRows
condition|)
block|{
name|isKey
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|isKey
operator|&&
name|cStat
operator|.
name|getRange
argument_list|()
operator|!=
literal|null
operator|&&
name|cStat
operator|.
name|getRange
argument_list|()
operator|.
name|maxValue
operator|!=
literal|null
operator|&&
name|cStat
operator|.
name|getRange
argument_list|()
operator|.
name|minValue
operator|!=
literal|null
condition|)
block|{
name|double
name|r
init|=
name|cStat
operator|.
name|getRange
argument_list|()
operator|.
name|maxValue
operator|.
name|doubleValue
argument_list|()
operator|-
name|cStat
operator|.
name|getRange
argument_list|()
operator|.
name|minValue
operator|.
name|doubleValue
argument_list|()
operator|+
literal|1
decl_stmt|;
name|isKey
operator|=
operator|(
name|Math
operator|.
name|abs
argument_list|(
name|numRows
operator|-
name|r
argument_list|)
operator|<
name|RelOptUtil
operator|.
name|EPSILON
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|isKey
condition|)
block|{
name|ImmutableBitSet
name|key
init|=
name|ImmutableBitSet
operator|.
name|of
argument_list|(
name|posMap
operator|.
name|get
argument_list|(
name|colStatsPos
argument_list|)
argument_list|)
decl_stmt|;
name|keys
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|colStatsPos
operator|++
expr_stmt|;
block|}
return|return
name|keys
return|;
block|}
comment|/*    * traverse a path of Filter, Projects to get to the TableScan.    * In case of Unique keys, stop if you reach a Project, it will be handled    * by the invocation on the Project.    * In case of getting the base rowCount of a Path, keep going past a Project.    */
specifier|static
name|HiveTableScan
name|getTableScan
parameter_list|(
name|RelNode
name|r
parameter_list|,
name|boolean
name|traverseProject
parameter_list|)
block|{
while|while
condition|(
name|r
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|r
operator|instanceof
name|HiveTableScan
operator|)
condition|)
block|{
if|if
condition|(
name|r
operator|instanceof
name|HepRelVertex
condition|)
block|{
name|r
operator|=
operator|(
operator|(
name|HepRelVertex
operator|)
name|r
operator|)
operator|.
name|getCurrentRel
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|r
operator|instanceof
name|Filter
condition|)
block|{
name|r
operator|=
operator|(
operator|(
name|Filter
operator|)
name|r
operator|)
operator|.
name|getInput
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|traverseProject
operator|&&
name|r
operator|instanceof
name|Project
condition|)
block|{
name|r
operator|=
operator|(
operator|(
name|Project
operator|)
name|r
operator|)
operator|.
name|getInput
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|r
operator|=
literal|null
expr_stmt|;
block|}
block|}
return|return
name|r
operator|==
literal|null
condition|?
literal|null
else|:
operator|(
name|HiveTableScan
operator|)
name|r
return|;
block|}
block|}
end_class

end_unit

