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
name|RelWriter
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
name|tools
operator|.
name|RelBuilder
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
name|commons
operator|.
name|lang3
operator|.
name|tuple
operator|.
name|Triple
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
name|ImmutableSet
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
specifier|private
specifier|final
name|String
name|concatQbIDAlias
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|useQBIdInDigest
decl_stmt|;
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|Integer
argument_list|>
name|virtualOrPartColIndxsInTS
decl_stmt|;
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|Integer
argument_list|>
name|virtualColIndxsInTS
decl_stmt|;
comment|// insiderView will tell this TableScan is inside a view or not.
specifier|private
specifier|final
name|boolean
name|insideView
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
specifier|public
name|String
name|getConcatQbIDAlias
parameter_list|()
block|{
return|return
name|concatQbIDAlias
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
parameter_list|,
name|String
name|concatQbIDAlias
parameter_list|,
name|boolean
name|useQBIdInDigest
parameter_list|,
name|boolean
name|insideView
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
name|concatQbIDAlias
argument_list|,
name|table
operator|.
name|getRowType
argument_list|()
argument_list|,
name|useQBIdInDigest
argument_list|,
name|insideView
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
name|String
name|concatQbIDAlias
parameter_list|,
name|RelDataType
name|newRowtype
parameter_list|,
name|boolean
name|useQBIdInDigest
parameter_list|,
name|boolean
name|insideView
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
name|concatQbIDAlias
operator|=
name|concatQbIDAlias
expr_stmt|;
name|this
operator|.
name|hiveTableScanRowType
operator|=
name|newRowtype
expr_stmt|;
name|Triple
argument_list|<
name|ImmutableList
argument_list|<
name|Integer
argument_list|>
argument_list|,
name|ImmutableSet
argument_list|<
name|Integer
argument_list|>
argument_list|,
name|ImmutableSet
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|colIndxPair
init|=
name|buildColIndxsFrmReloptHT
argument_list|(
name|table
argument_list|,
name|newRowtype
argument_list|)
decl_stmt|;
name|this
operator|.
name|neededColIndxsFrmReloptHT
operator|=
name|colIndxPair
operator|.
name|getLeft
argument_list|()
expr_stmt|;
name|this
operator|.
name|virtualOrPartColIndxsInTS
operator|=
name|colIndxPair
operator|.
name|getMiddle
argument_list|()
expr_stmt|;
name|this
operator|.
name|virtualColIndxsInTS
operator|=
name|colIndxPair
operator|.
name|getRight
argument_list|()
expr_stmt|;
name|this
operator|.
name|useQBIdInDigest
operator|=
name|useQBIdInDigest
expr_stmt|;
name|this
operator|.
name|insideView
operator|=
name|insideView
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
name|this
operator|.
name|concatQbIDAlias
argument_list|,
name|newRowtype
argument_list|,
name|this
operator|.
name|useQBIdInDigest
argument_list|,
name|this
operator|.
name|insideView
argument_list|)
return|;
block|}
comment|/**    * Copy TableScan operator with a new Row Schema. The new Row Schema can only    * be a subset of this TS schema. Copies underlying RelOptHiveTable object    * too.    */
specifier|public
name|HiveTableScan
name|copyIncludingTable
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
operator|.
name|copy
argument_list|(
name|newRowtype
argument_list|)
argument_list|,
name|this
operator|.
name|tblAlias
argument_list|,
name|this
operator|.
name|concatQbIDAlias
argument_list|,
name|newRowtype
argument_list|,
name|this
operator|.
name|useQBIdInDigest
argument_list|,
name|this
operator|.
name|insideView
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelWriter
name|explainTerms
parameter_list|(
name|RelWriter
name|pw
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|useQBIdInDigest
condition|)
block|{
comment|// TODO: Only the qualified name should be left here
return|return
name|super
operator|.
name|explainTerms
argument_list|(
name|pw
argument_list|)
operator|.
name|item
argument_list|(
literal|"qbid:alias"
argument_list|,
name|concatQbIDAlias
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|super
operator|.
name|explainTerms
argument_list|(
name|pw
argument_list|)
operator|.
name|item
argument_list|(
literal|"table:alias"
argument_list|,
name|tblAlias
argument_list|)
return|;
block|}
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
name|estimateRowCount
parameter_list|(
name|RelMetadataQuery
name|mq
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
name|RelBuilder
name|relBuilder
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
name|HiveProject
name|hp
init|=
operator|(
name|HiveProject
operator|)
name|relBuilder
operator|.
name|push
argument_list|(
name|newHT
argument_list|)
operator|.
name|project
argument_list|(
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
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// 6. Set synthetic flag, so that we would push filter below this one
name|hp
operator|.
name|setSynthetic
argument_list|()
expr_stmt|;
return|return
name|hp
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
specifier|public
name|Set
argument_list|<
name|Integer
argument_list|>
name|getPartOrVirtualCols
parameter_list|()
block|{
return|return
name|virtualOrPartColIndxsInTS
return|;
block|}
specifier|public
name|Set
argument_list|<
name|Integer
argument_list|>
name|getVirtualCols
parameter_list|()
block|{
return|return
name|virtualColIndxsInTS
return|;
block|}
specifier|private
specifier|static
name|Triple
argument_list|<
name|ImmutableList
argument_list|<
name|Integer
argument_list|>
argument_list|,
name|ImmutableSet
argument_list|<
name|Integer
argument_list|>
argument_list|,
name|ImmutableSet
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|buildColIndxsFrmReloptHT
parameter_list|(
name|RelOptHiveTable
name|relOptHTable
parameter_list|,
name|RelDataType
name|scanRowType
parameter_list|)
block|{
name|RelDataType
name|relOptHtRowtype
init|=
name|relOptHTable
operator|.
name|getRowType
argument_list|()
decl_stmt|;
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
name|ImmutableSet
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
name|virtualOrPartColIndxsInTSBldr
init|=
operator|new
name|ImmutableSet
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableSet
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
name|virtualColIndxsInTSBldr
init|=
operator|new
name|ImmutableSet
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
name|relOptHtRowtype
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
name|int
name|partColStartPosInrelOptHtRowtype
init|=
name|relOptHTable
operator|.
name|getNonPartColumns
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|virtualColStartPosInrelOptHtRowtype
init|=
name|relOptHTable
operator|.
name|getNonPartColumns
argument_list|()
operator|.
name|size
argument_list|()
operator|+
name|relOptHTable
operator|.
name|getPartColumns
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|tmp
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
name|tmp
operator|=
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
expr_stmt|;
name|neededColIndxsFrmReloptHTBldr
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmp
operator|>=
name|partColStartPosInrelOptHtRowtype
condition|)
block|{
comment|// Part or virtual
name|virtualOrPartColIndxsInTSBldr
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmp
operator|>=
name|virtualColStartPosInrelOptHtRowtype
condition|)
block|{
comment|// Virtual
name|virtualColIndxsInTSBldr
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|Triple
operator|.
name|of
argument_list|(
name|neededColIndxsFrmReloptHTBldr
operator|.
name|build
argument_list|()
argument_list|,
name|virtualOrPartColIndxsInTSBldr
operator|.
name|build
argument_list|()
argument_list|,
name|virtualColIndxsInTSBldr
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isInsideView
parameter_list|()
block|{
return|return
name|insideView
return|;
block|}
comment|// We need to include isInsideView inside digest to differentiate direct
comment|// tables and tables inside view. Otherwise, Calcite will treat them as the same.
comment|// Also include partition list key to trigger cost evaluation even if an
comment|// expression was already generated.
specifier|public
name|String
name|computeDigest
parameter_list|()
block|{
name|String
name|digest
init|=
name|super
operator|.
name|computeDigest
argument_list|()
operator|+
literal|"["
operator|+
name|this
operator|.
name|neededColIndxsFrmReloptHT
operator|+
literal|"]"
operator|+
literal|"["
operator|+
name|this
operator|.
name|isInsideView
argument_list|()
operator|+
literal|"]"
decl_stmt|;
name|String
name|partitionListKey
init|=
operator|(
operator|(
name|RelOptHiveTable
operator|)
name|table
operator|)
operator|.
name|getPartitionListKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitionListKey
operator|!=
literal|null
condition|)
block|{
return|return
name|digest
operator|+
literal|"["
operator|+
name|partitionListKey
operator|+
literal|"]"
return|;
block|}
return|return
name|digest
return|;
block|}
block|}
end_class

end_unit

