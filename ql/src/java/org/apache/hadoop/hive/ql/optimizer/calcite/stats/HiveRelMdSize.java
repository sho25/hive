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
name|stats
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
name|SemiJoin
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
name|RelMdSize
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
name|ImmutableNullableList
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
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_class
specifier|public
class|class
name|HiveRelMdSize
extends|extends
name|RelMdSize
block|{
specifier|private
specifier|static
specifier|final
name|HiveRelMdSize
name|INSTANCE
init|=
operator|new
name|HiveRelMdSize
argument_list|()
decl_stmt|;
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
name|INSTANCE
argument_list|,
name|BuiltInMethod
operator|.
name|AVERAGE_COLUMN_SIZES
operator|.
name|method
argument_list|,
name|BuiltInMethod
operator|.
name|AVERAGE_ROW_SIZE
operator|.
name|method
argument_list|)
decl_stmt|;
comment|//~ Constructors -----------------------------------------------------------
specifier|private
name|HiveRelMdSize
parameter_list|()
block|{}
comment|//~ Methods ----------------------------------------------------------------
specifier|public
name|List
argument_list|<
name|Double
argument_list|>
name|averageColumnSizes
parameter_list|(
name|HiveTableScan
name|scan
parameter_list|,
name|RelMetadataQuery
name|mq
parameter_list|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|neededcolsLst
init|=
name|scan
operator|.
name|getNeededColIndxsFrmReloptHT
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|columnStatistics
init|=
operator|(
operator|(
name|RelOptHiveTable
operator|)
name|scan
operator|.
name|getTable
argument_list|()
operator|)
operator|.
name|getColStat
argument_list|(
name|neededcolsLst
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// Obtain list of col stats, or use default if they are not available
specifier|final
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Double
argument_list|>
name|list
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
name|int
name|indxRqdCol
init|=
literal|0
decl_stmt|;
name|int
name|nFields
init|=
name|scan
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldCount
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
name|nFields
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|neededcolsLst
operator|.
name|contains
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|ColStatistics
name|columnStatistic
init|=
name|columnStatistics
operator|.
name|get
argument_list|(
name|indxRqdCol
argument_list|)
decl_stmt|;
name|indxRqdCol
operator|++
expr_stmt|;
if|if
condition|(
name|columnStatistic
operator|==
literal|null
condition|)
block|{
name|RelDataTypeField
name|field
init|=
name|scan
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|averageTypeValueSize
argument_list|(
name|field
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|list
operator|.
name|add
argument_list|(
name|columnStatistic
operator|.
name|getAvgColLen
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|list
operator|.
name|add
argument_list|(
operator|new
name|Double
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|list
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Double
argument_list|>
name|averageColumnSizes
parameter_list|(
name|SemiJoin
name|rel
parameter_list|,
name|RelMetadataQuery
name|mq
parameter_list|)
block|{
specifier|final
name|RelNode
name|left
init|=
name|rel
operator|.
name|getLeft
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Double
argument_list|>
name|lefts
init|=
name|mq
operator|.
name|getAverageColumnSizes
argument_list|(
name|left
argument_list|)
decl_stmt|;
if|if
condition|(
name|lefts
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|int
name|fieldCount
init|=
name|rel
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
decl_stmt|;
name|Double
index|[]
name|sizes
init|=
operator|new
name|Double
index|[
name|fieldCount
index|]
decl_stmt|;
if|if
condition|(
name|lefts
operator|!=
literal|null
condition|)
block|{
name|lefts
operator|.
name|toArray
argument_list|(
name|sizes
argument_list|)
expr_stmt|;
block|}
return|return
name|ImmutableNullableList
operator|.
name|copyOf
argument_list|(
name|sizes
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|Double
argument_list|>
name|averageColumnSizes
parameter_list|(
name|HiveJoin
name|rel
parameter_list|,
name|RelMetadataQuery
name|mq
parameter_list|)
block|{
specifier|final
name|RelNode
name|left
init|=
name|rel
operator|.
name|getLeft
argument_list|()
decl_stmt|;
specifier|final
name|RelNode
name|right
init|=
name|rel
operator|.
name|getRight
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Double
argument_list|>
name|lefts
init|=
name|mq
operator|.
name|getAverageColumnSizes
argument_list|(
name|left
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Double
argument_list|>
name|rights
init|=
name|mq
operator|.
name|getAverageColumnSizes
argument_list|(
name|right
argument_list|)
decl_stmt|;
if|if
condition|(
name|lefts
operator|==
literal|null
operator|&&
name|rights
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|int
name|fieldCount
init|=
name|rel
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
decl_stmt|;
name|Double
index|[]
name|sizes
init|=
operator|new
name|Double
index|[
name|fieldCount
index|]
decl_stmt|;
if|if
condition|(
name|lefts
operator|!=
literal|null
condition|)
block|{
name|lefts
operator|.
name|toArray
argument_list|(
name|sizes
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rights
operator|!=
literal|null
condition|)
block|{
specifier|final
name|int
name|leftCount
init|=
name|left
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldCount
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
name|rights
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|sizes
index|[
name|leftCount
operator|+
name|i
index|]
operator|=
name|rights
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ImmutableNullableList
operator|.
name|copyOf
argument_list|(
name|sizes
argument_list|)
return|;
block|}
comment|// TODO: remove when averageTypeValueSize method RelMdSize
comment|//       supports all types
annotation|@
name|Override
specifier|public
name|Double
name|averageTypeValueSize
parameter_list|(
name|RelDataType
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
operator|.
name|getSqlTypeName
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|TINYINT
case|:
return|return
literal|1d
return|;
case|case
name|SMALLINT
case|:
return|return
literal|2d
return|;
case|case
name|INTEGER
case|:
case|case
name|FLOAT
case|:
case|case
name|REAL
case|:
case|case
name|DECIMAL
case|:
case|case
name|DATE
case|:
case|case
name|TIME
case|:
return|return
literal|4d
return|;
case|case
name|BIGINT
case|:
case|case
name|DOUBLE
case|:
case|case
name|TIMESTAMP
case|:
case|case
name|INTERVAL_DAY
case|:
case|case
name|INTERVAL_DAY_HOUR
case|:
case|case
name|INTERVAL_DAY_MINUTE
case|:
case|case
name|INTERVAL_DAY_SECOND
case|:
case|case
name|INTERVAL_HOUR
case|:
case|case
name|INTERVAL_HOUR_MINUTE
case|:
case|case
name|INTERVAL_HOUR_SECOND
case|:
case|case
name|INTERVAL_MINUTE
case|:
case|case
name|INTERVAL_MINUTE_SECOND
case|:
case|case
name|INTERVAL_MONTH
case|:
case|case
name|INTERVAL_SECOND
case|:
case|case
name|INTERVAL_YEAR
case|:
case|case
name|INTERVAL_YEAR_MONTH
case|:
return|return
literal|8d
return|;
case|case
name|BINARY
case|:
return|return
operator|(
name|double
operator|)
name|type
operator|.
name|getPrecision
argument_list|()
return|;
case|case
name|VARBINARY
case|:
return|return
name|Math
operator|.
name|min
argument_list|(
name|type
operator|.
name|getPrecision
argument_list|()
argument_list|,
literal|100d
argument_list|)
return|;
case|case
name|CHAR
case|:
return|return
operator|(
name|double
operator|)
name|type
operator|.
name|getPrecision
argument_list|()
operator|*
name|BYTES_PER_CHARACTER
return|;
case|case
name|VARCHAR
case|:
comment|// Even in large (say VARCHAR(2000)) columns most strings are small
return|return
name|Math
operator|.
name|min
argument_list|(
operator|(
name|double
operator|)
name|type
operator|.
name|getPrecision
argument_list|()
operator|*
name|BYTES_PER_CHARACTER
argument_list|,
literal|100d
argument_list|)
return|;
case|case
name|ROW
case|:
name|Double
name|average
init|=
literal|0.0
decl_stmt|;
for|for
control|(
name|RelDataTypeField
name|field
range|:
name|type
operator|.
name|getFieldList
argument_list|()
control|)
block|{
name|average
operator|+=
name|averageTypeValueSize
argument_list|(
name|field
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|average
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

