begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|columnstats
operator|.
name|aggr
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
name|MetaException
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
name|utils
operator|.
name|MetaStoreServerUtils
operator|.
name|ColStatsObjWithSourceInfo
import|;
end_import

begin_class
specifier|public
class|class
name|BooleanColumnStatsAggregator
extends|extends
name|ColumnStatsAggregator
block|{
annotation|@
name|Override
specifier|public
name|ColumnStatisticsObj
name|aggregate
parameter_list|(
name|List
argument_list|<
name|ColStatsObjWithSourceInfo
argument_list|>
name|colStatsWithSourceInfo
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|,
name|boolean
name|areAllPartsFound
parameter_list|)
throws|throws
name|MetaException
block|{
name|ColumnStatisticsObj
name|statsObj
init|=
literal|null
decl_stmt|;
name|String
name|colType
init|=
literal|null
decl_stmt|;
name|String
name|colName
init|=
literal|null
decl_stmt|;
name|BooleanColumnStatsData
name|aggregateData
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ColStatsObjWithSourceInfo
name|csp
range|:
name|colStatsWithSourceInfo
control|)
block|{
name|ColumnStatisticsObj
name|cso
init|=
name|csp
operator|.
name|getColStatsObj
argument_list|()
decl_stmt|;
if|if
condition|(
name|statsObj
operator|==
literal|null
condition|)
block|{
name|colName
operator|=
name|cso
operator|.
name|getColName
argument_list|()
expr_stmt|;
name|colType
operator|=
name|cso
operator|.
name|getColType
argument_list|()
expr_stmt|;
name|statsObj
operator|=
name|ColumnStatsAggregatorFactory
operator|.
name|newColumnStaticsObj
argument_list|(
name|colName
argument_list|,
name|colType
argument_list|,
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getSetField
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|BooleanColumnStatsData
name|newData
init|=
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getBooleanStats
argument_list|()
decl_stmt|;
if|if
condition|(
name|aggregateData
operator|==
literal|null
condition|)
block|{
name|aggregateData
operator|=
name|newData
operator|.
name|deepCopy
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|aggregateData
operator|.
name|setNumTrues
argument_list|(
name|aggregateData
operator|.
name|getNumTrues
argument_list|()
operator|+
name|newData
operator|.
name|getNumTrues
argument_list|()
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setNumFalses
argument_list|(
name|aggregateData
operator|.
name|getNumFalses
argument_list|()
operator|+
name|newData
operator|.
name|getNumFalses
argument_list|()
argument_list|)
expr_stmt|;
name|aggregateData
operator|.
name|setNumNulls
argument_list|(
name|aggregateData
operator|.
name|getNumNulls
argument_list|()
operator|+
name|newData
operator|.
name|getNumNulls
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|ColumnStatisticsData
name|columnStatisticsData
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
name|columnStatisticsData
operator|.
name|setBooleanStats
argument_list|(
name|aggregateData
argument_list|)
expr_stmt|;
name|statsObj
operator|.
name|setStatsData
argument_list|(
name|columnStatisticsData
argument_list|)
expr_stmt|;
return|return
name|statsObj
return|;
block|}
block|}
end_class

end_unit

