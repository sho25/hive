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
name|merge
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
name|annotation
operator|.
name|MetastoreUnitTest
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
name|Decimal
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
name|utils
operator|.
name|DecimalUtils
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
name|columnstats
operator|.
name|cache
operator|.
name|DecimalColumnStatsDataInspector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|DecimalColumnStatsMergerTest
block|{
specifier|private
specifier|static
specifier|final
name|Decimal
name|DECIMAL_3
init|=
name|DecimalUtils
operator|.
name|getDecimal
argument_list|(
literal|3
argument_list|,
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Decimal
name|DECIMAL_5
init|=
name|DecimalUtils
operator|.
name|getDecimal
argument_list|(
literal|5
argument_list|,
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Decimal
name|DECIMAL_20
init|=
name|DecimalUtils
operator|.
name|getDecimal
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|DecimalColumnStatsDataInspector
name|DATA_3
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|DecimalColumnStatsDataInspector
name|DATA_5
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|DecimalColumnStatsDataInspector
name|DATA_20
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
static|static
block|{
name|DATA_3
operator|.
name|setLowValue
argument_list|(
name|DECIMAL_3
argument_list|)
expr_stmt|;
name|DATA_3
operator|.
name|setHighValue
argument_list|(
name|DECIMAL_3
argument_list|)
expr_stmt|;
name|DATA_5
operator|.
name|setLowValue
argument_list|(
name|DECIMAL_5
argument_list|)
expr_stmt|;
name|DATA_5
operator|.
name|setHighValue
argument_list|(
name|DECIMAL_5
argument_list|)
expr_stmt|;
name|DATA_20
operator|.
name|setLowValue
argument_list|(
name|DECIMAL_20
argument_list|)
expr_stmt|;
name|DATA_20
operator|.
name|setHighValue
argument_list|(
name|DECIMAL_20
argument_list|)
expr_stmt|;
block|}
specifier|private
name|DecimalColumnStatsMerger
name|merger
init|=
operator|new
name|DecimalColumnStatsMerger
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testMergeNullMinMaxValues
parameter_list|()
block|{
name|ColumnStatisticsObj
name|objNulls
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|objNulls
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|merger
operator|.
name|merge
argument_list|(
name|objNulls
argument_list|,
name|objNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|objNulls
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|objNulls
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeNonNullAndNullLowerValuesOldIsNull
parameter_list|()
block|{
name|ColumnStatisticsObj
name|oldObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|oldObj
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|newObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|newObj
argument_list|,
name|DECIMAL_3
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|merger
operator|.
name|merge
argument_list|(
name|oldObj
argument_list|,
name|newObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|oldObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeNonNullAndNullLowerValuesNewIsNull
parameter_list|()
block|{
name|ColumnStatisticsObj
name|oldObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|oldObj
argument_list|,
name|DECIMAL_3
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|newObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|newObj
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|merger
operator|.
name|merge
argument_list|(
name|oldObj
argument_list|,
name|newObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|oldObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeNonNullAndNullHigherValuesOldIsNull
parameter_list|()
block|{
name|ColumnStatisticsObj
name|oldObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|oldObj
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|newObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|newObj
argument_list|,
literal|null
argument_list|,
name|DECIMAL_3
argument_list|)
expr_stmt|;
name|merger
operator|.
name|merge
argument_list|(
name|oldObj
argument_list|,
name|newObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|oldObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeNonNullAndNullHigherValuesNewIsNull
parameter_list|()
block|{
name|ColumnStatisticsObj
name|oldObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|oldObj
argument_list|,
literal|null
argument_list|,
name|DECIMAL_3
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|newObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|newObj
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|merger
operator|.
name|merge
argument_list|(
name|oldObj
argument_list|,
name|newObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|oldObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeLowValuesFirstWins
parameter_list|()
block|{
name|ColumnStatisticsObj
name|oldObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|oldObj
argument_list|,
name|DECIMAL_3
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|newObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|newObj
argument_list|,
name|DECIMAL_5
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|merger
operator|.
name|merge
argument_list|(
name|oldObj
argument_list|,
name|newObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|oldObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeLowValuesSecondWins
parameter_list|()
block|{
name|ColumnStatisticsObj
name|oldObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|oldObj
argument_list|,
name|DECIMAL_5
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|newObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|newObj
argument_list|,
name|DECIMAL_3
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|merger
operator|.
name|merge
argument_list|(
name|oldObj
argument_list|,
name|newObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|oldObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeHighValuesFirstWins
parameter_list|()
block|{
name|ColumnStatisticsObj
name|oldObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|oldObj
argument_list|,
literal|null
argument_list|,
name|DECIMAL_5
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|newObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|newObj
argument_list|,
literal|null
argument_list|,
name|DECIMAL_3
argument_list|)
expr_stmt|;
name|merger
operator|.
name|merge
argument_list|(
name|oldObj
argument_list|,
name|newObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_5
argument_list|,
name|oldObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMergeHighValuesSecondWins
parameter_list|()
block|{
name|ColumnStatisticsObj
name|oldObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|oldObj
argument_list|,
literal|null
argument_list|,
name|DECIMAL_3
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|newObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|createData
argument_list|(
name|newObj
argument_list|,
literal|null
argument_list|,
name|DECIMAL_5
argument_list|)
expr_stmt|;
name|merger
operator|.
name|merge
argument_list|(
name|oldObj
argument_list|,
name|newObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_5
argument_list|,
name|oldObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDecimalStats
argument_list|()
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalCompareEqual
parameter_list|()
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|DECIMAL_3
operator|.
name|equals
argument_list|(
name|DECIMAL_3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalCompareDoesntEqual
parameter_list|()
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|!
name|DECIMAL_3
operator|.
name|equals
argument_list|(
name|DECIMAL_5
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareSimple
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_3
argument_list|)
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_5
argument_list|)
decl_stmt|;
name|merger
operator|.
name|setHighValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_5
argument_list|,
name|data1
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareSimpleFlipped
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_5
argument_list|)
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_3
argument_list|)
decl_stmt|;
name|merger
operator|.
name|setHighValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_5
argument_list|,
name|data1
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareSimpleReversed
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_3
argument_list|)
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_5
argument_list|)
decl_stmt|;
name|merger
operator|.
name|setLowValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|data1
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareSimpleFlippedReversed
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_5
argument_list|)
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_3
argument_list|)
decl_stmt|;
name|merger
operator|.
name|setLowValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|data1
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareUnscaledValue
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_3
argument_list|)
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_20
argument_list|)
decl_stmt|;
name|merger
operator|.
name|setHighValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_20
argument_list|,
name|data1
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareNullsMin
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
name|merger
operator|.
name|setLowValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|data1
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareNullsMax
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
name|merger
operator|.
name|setHighValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|data1
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareFirstNullMin
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_3
argument_list|)
decl_stmt|;
name|merger
operator|.
name|setLowValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|data1
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareSecondNullMin
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_3
argument_list|)
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
name|merger
operator|.
name|setLowValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|data1
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareFirstNullMax
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_3
argument_list|)
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
name|merger
operator|.
name|setHighValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|data1
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareSecondNullMax
parameter_list|()
block|{
name|DecimalColumnStatsDataInspector
name|data1
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data2
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|(
name|DATA_3
argument_list|)
decl_stmt|;
name|merger
operator|.
name|setHighValue
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DECIMAL_3
argument_list|,
name|data1
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|DecimalColumnStatsDataInspector
name|createData
parameter_list|(
name|ColumnStatisticsObj
name|objNulls
parameter_list|,
name|Decimal
name|lowValue
parameter_list|,
name|Decimal
name|highValue
parameter_list|)
block|{
name|ColumnStatisticsData
name|statisticsData
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
name|DecimalColumnStatsDataInspector
name|data
init|=
operator|new
name|DecimalColumnStatsDataInspector
argument_list|()
decl_stmt|;
name|statisticsData
operator|.
name|setDecimalStats
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|objNulls
operator|.
name|setStatsData
argument_list|(
name|statisticsData
argument_list|)
expr_stmt|;
name|data
operator|.
name|setLowValue
argument_list|(
name|lowValue
argument_list|)
expr_stmt|;
name|data
operator|.
name|setHighValue
argument_list|(
name|highValue
argument_list|)
expr_stmt|;
return|return
name|data
return|;
block|}
block|}
end_class

end_unit

