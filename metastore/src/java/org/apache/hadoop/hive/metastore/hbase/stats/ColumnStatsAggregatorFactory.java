begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|metastore
operator|.
name|hbase
operator|.
name|stats
package|;
end_package

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
name|metastore
operator|.
name|NumDistinctValueEstimator
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
name|metastore
operator|.
name|api
operator|.
name|BinaryColumnStatsData
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
name|metastore
operator|.
name|api
operator|.
name|BooleanColumnStatsData
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
name|metastore
operator|.
name|api
operator|.
name|ColumnStatisticsData
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
name|metastore
operator|.
name|api
operator|.
name|ColumnStatisticsData
operator|.
name|_Fields
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
name|metastore
operator|.
name|api
operator|.
name|ColumnStatisticsObj
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
name|metastore
operator|.
name|api
operator|.
name|DecimalColumnStatsData
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
name|metastore
operator|.
name|api
operator|.
name|DoubleColumnStatsData
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
name|metastore
operator|.
name|api
operator|.
name|LongColumnStatsData
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
name|metastore
operator|.
name|api
operator|.
name|StringColumnStatsData
import|;
end_import

begin_class
specifier|public
class|class
name|ColumnStatsAggregatorFactory
block|{
specifier|private
name|ColumnStatsAggregatorFactory
parameter_list|()
block|{   }
specifier|public
specifier|static
name|ColumnStatsAggregator
name|getColumnStatsAggregator
parameter_list|(
name|_Fields
name|type
parameter_list|,
name|int
name|numBitVectors
parameter_list|)
block|{
name|ColumnStatsAggregator
name|agg
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BOOLEAN_STATS
case|:
name|agg
operator|=
operator|new
name|BooleanColumnStatsAggregator
argument_list|()
expr_stmt|;
break|break;
case|case
name|LONG_STATS
case|:
name|agg
operator|=
operator|new
name|LongColumnStatsAggregator
argument_list|()
expr_stmt|;
break|break;
case|case
name|DOUBLE_STATS
case|:
name|agg
operator|=
operator|new
name|DoubleColumnStatsAggregator
argument_list|()
expr_stmt|;
break|break;
case|case
name|STRING_STATS
case|:
name|agg
operator|=
operator|new
name|StringColumnStatsAggregator
argument_list|()
expr_stmt|;
break|break;
case|case
name|BINARY_STATS
case|:
name|agg
operator|=
operator|new
name|BinaryColumnStatsAggregator
argument_list|()
expr_stmt|;
break|break;
case|case
name|DECIMAL_STATS
case|:
name|agg
operator|=
operator|new
name|DecimalColumnStatsAggregator
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Woh, bad.  Unknown stats type "
operator|+
name|type
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|numBitVectors
operator|>
literal|0
condition|)
block|{
name|agg
operator|.
name|ndvEstimator
operator|=
operator|new
name|NumDistinctValueEstimator
argument_list|(
name|numBitVectors
argument_list|)
expr_stmt|;
block|}
return|return
name|agg
return|;
block|}
specifier|public
specifier|static
name|ColumnStatisticsObj
name|newColumnStaticsObj
parameter_list|(
name|String
name|colName
parameter_list|,
name|String
name|colType
parameter_list|,
name|_Fields
name|type
parameter_list|)
block|{
name|ColumnStatisticsObj
name|cso
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|ColumnStatisticsData
name|csd
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
name|cso
operator|.
name|setColName
argument_list|(
name|colName
argument_list|)
expr_stmt|;
name|cso
operator|.
name|setColType
argument_list|(
name|colType
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BOOLEAN_STATS
case|:
name|csd
operator|.
name|setBooleanStats
argument_list|(
operator|new
name|BooleanColumnStatsData
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG_STATS
case|:
name|csd
operator|.
name|setLongStats
argument_list|(
operator|new
name|LongColumnStatsData
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE_STATS
case|:
name|csd
operator|.
name|setDoubleStats
argument_list|(
operator|new
name|DoubleColumnStatsData
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING_STATS
case|:
name|csd
operator|.
name|setStringStats
argument_list|(
operator|new
name|StringColumnStatsData
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY_STATS
case|:
name|csd
operator|.
name|setBinaryStats
argument_list|(
operator|new
name|BinaryColumnStatsData
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL_STATS
case|:
name|csd
operator|.
name|setDecimalStats
argument_list|(
operator|new
name|DecimalColumnStatsData
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Woh, bad.  Unknown stats type!"
argument_list|)
throw|;
block|}
name|cso
operator|.
name|setStatsData
argument_list|(
name|csd
argument_list|)
expr_stmt|;
return|return
name|cso
return|;
block|}
block|}
end_class

end_unit

