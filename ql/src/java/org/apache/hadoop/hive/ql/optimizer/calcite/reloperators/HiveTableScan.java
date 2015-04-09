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
name|reloperators
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
name|LinkedList
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
name|RelOptCluster
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
name|RelOptCost
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
name|RelOptPlanner
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
name|RelTraitSet
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
name|RelFactories
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
name|TableScan
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
name|RelMetadataQuery
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
name|type
operator|.
name|RelDataType
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
name|type
operator|.
name|RelDataTypeField
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
name|RexBuilder
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
name|HiveCalciteUtil
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
name|calcite
operator|.
name|TraitsUtil
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
name|collect
operator|.
name|ImmutableList
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
operator|.
name|Builder
import|;
end_import

begin_comment
comment|/**  * Relational expression representing a scan of a HiveDB collection.  *  *<p>  * Additional operations might be applied, using the "find" or "aggregate"  * methods.  *</p>  */
end_comment

begin_class
specifier|public
class|class
name|HiveTableScan
extends|extends
name|TableScan
implements|implements
name|HiveRelNode
block|{
specifier|private
specifier|final
name|RelDataType
name|hiveTableScanRowType
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|Integer
argument_list|>
name|neededColIndxsFrmReloptHT
decl_stmt|;
specifier|private
specifier|final
name|String
name|tblAlias
decl_stmt|;
specifier|public
name|String
name|getTableAlias
parameter_list|()
block|{
return|return
name|tblAlias
return|;
block|}
comment|/**    * Creates a HiveTableScan.    *    * @param cluster    *          Cluster    * @param traitSet    *          Traits    * @param table    *          Table    * @param table    *          HiveDB table    */
specifier|public
name|HiveTableScan
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelOptHiveTable
name|table
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|this
argument_list|(
name|cluster
argument_list|,
name|traitSet
argument_list|,
name|table
argument_list|,
name|alias
argument_list|,
name|table
operator|.
name|getRowType
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HiveTableScan
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelOptHiveTable
name|table
parameter_list|,
name|String
name|alias
parameter_list|,
name|RelDataType
name|newRowtype
parameter_list|)
block|{
name|super
argument_list|(
name|cluster
argument_list|,
name|TraitsUtil
operator|.
name|getDefaultTraitSet
argument_list|(
name|cluster
argument_list|)
argument_list|,
name|table
argument_list|)
expr_stmt|;
assert|assert
name|getConvention
argument_list|()
operator|==
name|HiveRelNode
operator|.
name|CONVENTION
assert|;
name|this
operator|.
name|tblAlias
operator|=
name|alias
expr_stmt|;
name|this
operator|.
name|hiveTableScanRowType
operator|=
name|newRowtype
expr_stmt|;
name|this
operator|.
name|neededColIndxsFrmReloptHT
operator|=
name|buildNeededColIndxsFrmReloptHT
argument_list|(
name|table
operator|.
name|getRowType
argument_list|()
argument_list|,
name|newRowtype
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RelNode
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|List
argument_list|<
name|RelNode
argument_list|>
name|inputs
parameter_list|)
block|{
assert|assert
name|inputs
operator|.
name|isEmpty
argument_list|()
assert|;
return|return
name|this
return|;
block|}
comment|/**    * Copy TableScan operator with a new Row Schema. The new Row Schema can only    * be a subset of this TS schema.    *    * @param newRowtype    * @return    */
specifier|public
name|HiveTableScan
name|copy
parameter_list|(
name|RelDataType
name|newRowtype
parameter_list|)
block|{
return|return
operator|new
name|HiveTableScan
argument_list|(
name|getCluster
argument_list|()
argument_list|,
name|getTraitSet
argument_list|()
argument_list|,
operator|(
operator|(
name|RelOptHiveTable
operator|)
name|table
operator|)
argument_list|,
name|this
operator|.
name|tblAlias
argument_list|,
name|newRowtype
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelOptCost
name|computeSelfCost
parameter_list|(
name|RelOptPlanner
name|planner
parameter_list|)
block|{
return|return
name|RelMetadataQuery
operator|.
name|getNonCumulativeCost
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|register
parameter_list|(
name|RelOptPlanner
name|planner
parameter_list|)
block|{    }
annotation|@
name|Override
specifier|public
name|void
name|implement
parameter_list|(
name|Implementor
name|implementor
parameter_list|)
block|{    }
annotation|@
name|Override
specifier|public
name|double
name|getRows
parameter_list|()
block|{
return|return
operator|(
operator|(
name|RelOptHiveTable
operator|)
name|table
operator|)
operator|.
name|getRowCount
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|getColStat
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|projIndxLst
parameter_list|)
block|{
return|return
operator|(
operator|(
name|RelOptHiveTable
operator|)
name|table
operator|)
operator|.
name|getColStat
argument_list|(
name|projIndxLst
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelNode
name|project
parameter_list|(
name|ImmutableBitSet
name|fieldsUsed
parameter_list|,
name|Set
argument_list|<
name|RelDataTypeField
argument_list|>
name|extraFields
parameter_list|,
name|RelFactories
operator|.
name|ProjectFactory
name|projectFactory
parameter_list|)
block|{
comment|// 1. If the schema is the same then bail out
specifier|final
name|int
name|fieldCount
init|=
name|getRowType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldsUsed
operator|.
name|equals
argument_list|(
name|ImmutableBitSet
operator|.
name|range
argument_list|(
name|fieldCount
argument_list|)
argument_list|)
operator|&&
name|extraFields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
comment|// 2. Make sure there is no dynamic addition of virtual cols
if|if
condition|(
name|extraFields
operator|!=
literal|null
operator|&&
operator|!
name|extraFields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive TS does not support adding virtual columns dynamically"
argument_list|)
throw|;
block|}
comment|// 3. Create new TS schema that is a subset of original
specifier|final
name|List
argument_list|<
name|RelDataTypeField
argument_list|>
name|fields
init|=
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RelDataType
argument_list|>
name|fieldTypes
init|=
operator|new
name|LinkedList
argument_list|<
name|RelDataType
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|exprList
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|RexBuilder
name|rexBuilder
init|=
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
range|:
name|fieldsUsed
control|)
block|{
name|RelDataTypeField
name|field
init|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|fieldTypes
operator|.
name|add
argument_list|(
name|field
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|fieldNames
operator|.
name|add
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|exprList
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|this
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// 4. Build new TS
name|HiveTableScan
name|newHT
init|=
name|copy
argument_list|(
name|getCluster
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
operator|.
name|createStructType
argument_list|(
name|fieldTypes
argument_list|,
name|fieldNames
argument_list|)
argument_list|)
decl_stmt|;
comment|// 5. Add Proj on top of TS
return|return
name|projectFactory
operator|.
name|createProject
argument_list|(
name|newHT
argument_list|,
name|exprList
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|fieldNames
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getNeededColIndxsFrmReloptHT
parameter_list|()
block|{
return|return
name|neededColIndxsFrmReloptHT
return|;
block|}
specifier|public
name|RelDataType
name|getPrunedRowType
parameter_list|()
block|{
return|return
name|hiveTableScanRowType
return|;
block|}
specifier|private
specifier|static
name|ImmutableList
argument_list|<
name|Integer
argument_list|>
name|buildNeededColIndxsFrmReloptHT
parameter_list|(
name|RelDataType
name|htRowtype
parameter_list|,
name|RelDataType
name|scanRowType
parameter_list|)
block|{
name|Builder
argument_list|<
name|Integer
argument_list|>
name|neededColIndxsFrmReloptHTBldr
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|colNameToPosInReloptHT
init|=
name|HiveCalciteUtil
operator|.
name|getRowColNameIndxMap
argument_list|(
name|htRowtype
operator|.
name|getFieldList
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colNamesInScanRowType
init|=
name|scanRowType
operator|.
name|getFieldNames
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
name|colNamesInScanRowType
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|neededColIndxsFrmReloptHTBldr
operator|.
name|add
argument_list|(
name|colNameToPosInReloptHT
operator|.
name|get
argument_list|(
name|colNamesInScanRowType
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|neededColIndxsFrmReloptHTBldr
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

