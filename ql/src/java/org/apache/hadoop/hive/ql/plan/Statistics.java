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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|Maps
import|;
end_import

begin_comment
comment|/**  * Statistics. Describes the output of an operator in terms of size, rows, etc  * based on estimates.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
class|class
name|Statistics
implements|implements
name|Serializable
block|{
specifier|public
enum|enum
name|State
block|{
name|COMPLETE
block|,
name|PARTIAL
block|,
name|NONE
block|}
specifier|private
name|long
name|numRows
decl_stmt|;
specifier|private
name|long
name|dataSize
decl_stmt|;
specifier|private
name|State
name|basicStatsState
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|ColStatistics
argument_list|>
name|columnStats
decl_stmt|;
specifier|private
name|State
name|columnStatsState
decl_stmt|;
specifier|public
name|Statistics
parameter_list|()
block|{
name|this
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Statistics
parameter_list|(
name|long
name|nr
parameter_list|,
name|long
name|ds
parameter_list|)
block|{
name|this
operator|.
name|setNumRows
argument_list|(
name|nr
argument_list|)
expr_stmt|;
name|this
operator|.
name|setDataSize
argument_list|(
name|ds
argument_list|)
expr_stmt|;
name|this
operator|.
name|basicStatsState
operator|=
name|State
operator|.
name|NONE
expr_stmt|;
name|this
operator|.
name|columnStats
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|columnStatsState
operator|=
name|State
operator|.
name|NONE
expr_stmt|;
block|}
specifier|public
name|long
name|getNumRows
parameter_list|()
block|{
return|return
name|numRows
return|;
block|}
specifier|public
name|void
name|setNumRows
parameter_list|(
name|long
name|numRows
parameter_list|)
block|{
name|this
operator|.
name|numRows
operator|=
name|numRows
expr_stmt|;
name|updateBasicStatsState
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|getDataSize
parameter_list|()
block|{
return|return
name|dataSize
return|;
block|}
specifier|public
name|void
name|setDataSize
parameter_list|(
name|long
name|dataSize
parameter_list|)
block|{
name|this
operator|.
name|dataSize
operator|=
name|dataSize
expr_stmt|;
name|updateBasicStatsState
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|updateBasicStatsState
parameter_list|()
block|{
if|if
condition|(
name|numRows
operator|<=
literal|0
operator|&&
name|dataSize
operator|<=
literal|0
condition|)
block|{
name|this
operator|.
name|basicStatsState
operator|=
name|State
operator|.
name|NONE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|numRows
operator|<=
literal|0
operator|||
name|dataSize
operator|<=
literal|0
condition|)
block|{
name|this
operator|.
name|basicStatsState
operator|=
name|State
operator|.
name|PARTIAL
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|basicStatsState
operator|=
name|State
operator|.
name|COMPLETE
expr_stmt|;
block|}
block|}
specifier|public
name|State
name|getBasicStatsState
parameter_list|()
block|{
return|return
name|basicStatsState
return|;
block|}
specifier|public
name|void
name|setBasicStatsState
parameter_list|(
name|State
name|basicStatsState
parameter_list|)
block|{
name|this
operator|.
name|basicStatsState
operator|=
name|basicStatsState
expr_stmt|;
block|}
specifier|public
name|State
name|getColumnStatsState
parameter_list|()
block|{
return|return
name|columnStatsState
return|;
block|}
specifier|public
name|void
name|setColumnStatsState
parameter_list|(
name|State
name|columnStatsState
parameter_list|)
block|{
name|this
operator|.
name|columnStatsState
operator|=
name|columnStatsState
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|""
argument_list|)
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" numRows: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|numRows
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" dataSize: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|dataSize
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" basicStatsState: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|basicStatsState
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" colStatsState: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|columnStatsState
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Statistics
name|clone
parameter_list|()
throws|throws
name|CloneNotSupportedException
block|{
name|Statistics
name|clone
init|=
operator|new
name|Statistics
argument_list|(
name|numRows
argument_list|,
name|dataSize
argument_list|)
decl_stmt|;
name|clone
operator|.
name|setBasicStatsState
argument_list|(
name|basicStatsState
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setColumnStatsState
argument_list|(
name|columnStatsState
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnStats
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ColStatistics
argument_list|>
name|cloneColStats
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ColStatistics
argument_list|>
name|entry
range|:
name|columnStats
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|cloneColStats
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|clone
operator|.
name|setColumnStats
argument_list|(
name|cloneColStats
argument_list|)
expr_stmt|;
block|}
return|return
name|clone
return|;
block|}
specifier|public
name|void
name|addToNumRows
parameter_list|(
name|long
name|nr
parameter_list|)
block|{
name|numRows
operator|+=
name|nr
expr_stmt|;
name|updateBasicStatsState
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|addToDataSize
parameter_list|(
name|long
name|rds
parameter_list|)
block|{
name|dataSize
operator|+=
name|rds
expr_stmt|;
name|updateBasicStatsState
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setColumnStats
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|ColStatistics
argument_list|>
name|colStats
parameter_list|)
block|{
name|this
operator|.
name|columnStats
operator|=
name|colStats
expr_stmt|;
block|}
specifier|public
name|void
name|setColumnStats
parameter_list|(
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|colStats
parameter_list|)
block|{
name|columnStats
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
name|addToColumnStats
argument_list|(
name|colStats
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addToColumnStats
parameter_list|(
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|colStats
parameter_list|)
block|{
if|if
condition|(
name|columnStats
operator|==
literal|null
condition|)
block|{
name|columnStats
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|colStats
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ColStatistics
name|cs
range|:
name|colStats
control|)
block|{
name|ColStatistics
name|updatedCS
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|cs
operator|!=
literal|null
condition|)
block|{
name|String
name|key
init|=
name|cs
operator|.
name|getFullyQualifiedColName
argument_list|()
decl_stmt|;
comment|// if column statistics for a column is already found then merge the statistics
if|if
condition|(
name|columnStats
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
operator|&&
name|columnStats
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|updatedCS
operator|=
name|columnStats
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|updatedCS
operator|.
name|setAvgColLen
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|updatedCS
operator|.
name|getAvgColLen
argument_list|()
argument_list|,
name|cs
operator|.
name|getAvgColLen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|updatedCS
operator|.
name|setNumNulls
argument_list|(
name|updatedCS
operator|.
name|getNumNulls
argument_list|()
operator|+
name|cs
operator|.
name|getNumNulls
argument_list|()
argument_list|)
expr_stmt|;
name|updatedCS
operator|.
name|setCountDistint
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|updatedCS
operator|.
name|getCountDistint
argument_list|()
argument_list|,
name|cs
operator|.
name|getCountDistint
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|columnStats
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|updatedCS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|columnStats
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|cs
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|//                  newState
comment|//                  -----------------------------------------
comment|// columnStatsState | COMPLETE          PARTIAL      NONE    |
comment|//                  |________________________________________|
comment|//         COMPLETE | COMPLETE          PARTIAL      PARTIAL |
comment|//          PARTIAL | PARTIAL           PARTIAL      PARTIAL |
comment|//             NONE | COMPLETE          PARTIAL      NONE    |
comment|//                  -----------------------------------------
specifier|public
name|void
name|updateColumnStatsState
parameter_list|(
name|State
name|newState
parameter_list|)
block|{
if|if
condition|(
name|newState
operator|.
name|equals
argument_list|(
name|State
operator|.
name|PARTIAL
argument_list|)
condition|)
block|{
name|columnStatsState
operator|=
name|State
operator|.
name|PARTIAL
expr_stmt|;
block|}
if|if
condition|(
name|newState
operator|.
name|equals
argument_list|(
name|State
operator|.
name|NONE
argument_list|)
condition|)
block|{
if|if
condition|(
name|columnStatsState
operator|.
name|equals
argument_list|(
name|State
operator|.
name|NONE
argument_list|)
condition|)
block|{
name|columnStatsState
operator|=
name|State
operator|.
name|NONE
expr_stmt|;
block|}
else|else
block|{
name|columnStatsState
operator|=
name|State
operator|.
name|PARTIAL
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newState
operator|.
name|equals
argument_list|(
name|State
operator|.
name|COMPLETE
argument_list|)
condition|)
block|{
if|if
condition|(
name|columnStatsState
operator|.
name|equals
argument_list|(
name|State
operator|.
name|PARTIAL
argument_list|)
condition|)
block|{
name|columnStatsState
operator|=
name|State
operator|.
name|PARTIAL
expr_stmt|;
block|}
else|else
block|{
name|columnStatsState
operator|=
name|State
operator|.
name|COMPLETE
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|long
name|getAvgRowSize
parameter_list|()
block|{
if|if
condition|(
name|numRows
operator|!=
literal|0
condition|)
block|{
return|return
name|dataSize
operator|/
name|numRows
return|;
block|}
return|return
name|dataSize
return|;
block|}
specifier|public
name|ColStatistics
name|getColumnStatisticsFromFQColName
parameter_list|(
name|String
name|fqColName
parameter_list|)
block|{
return|return
name|columnStats
operator|.
name|get
argument_list|(
name|fqColName
argument_list|)
return|;
block|}
specifier|public
name|ColStatistics
name|getColumnStatisticsFromColName
parameter_list|(
name|String
name|colName
parameter_list|)
block|{
for|for
control|(
name|ColStatistics
name|cs
range|:
name|columnStats
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|cs
operator|.
name|getColumnName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|colName
argument_list|)
condition|)
block|{
return|return
name|cs
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|ColStatistics
name|getColumnStatisticsForColumn
parameter_list|(
name|String
name|tabAlias
parameter_list|,
name|String
name|colName
parameter_list|)
block|{
name|String
name|fqColName
init|=
name|StatsUtils
operator|.
name|getFullyQualifiedColumnName
argument_list|(
name|tabAlias
argument_list|,
name|colName
argument_list|)
decl_stmt|;
return|return
name|getColumnStatisticsFromFQColName
argument_list|(
name|fqColName
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|getColumnStats
parameter_list|()
block|{
if|if
condition|(
name|columnStats
operator|!=
literal|null
condition|)
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|columnStats
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

