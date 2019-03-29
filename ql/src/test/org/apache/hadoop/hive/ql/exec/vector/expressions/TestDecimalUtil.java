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
name|exec
operator|.
name|vector
operator|.
name|expressions
package|;
end_package

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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|exec
operator|.
name|vector
operator|.
name|DecimalColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|LongColumnVector
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

begin_comment
comment|/**  * Unit tests for DecimalUtil.  */
end_comment

begin_class
specifier|public
class|class
name|TestDecimalUtil
block|{
annotation|@
name|Test
specifier|public
name|void
name|testFloor
parameter_list|()
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|new
name|DecimalColumnVector
argument_list|(
literal|4
argument_list|,
literal|20
argument_list|,
literal|13
argument_list|)
decl_stmt|;
name|HiveDecimal
name|d1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.56778"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|floor
argument_list|(
literal|0
argument_list|,
name|d1
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected1
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// As of HIVE-8745, these decimal values should be trimmed of trailing zeros.
name|HiveDecimal
name|d2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"23.00000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|d2
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"23"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|floor
argument_list|(
literal|0
argument_list|,
name|d2
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected2
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.34567"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-26"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|floor
argument_list|(
literal|0
argument_list|,
name|d3
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected3
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d4
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-17.00000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|d4
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected4
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-17"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|floor
argument_list|(
literal|0
argument_list|,
name|d4
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected4
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d5
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-0.30000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|d5
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected5
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-1"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|floor
argument_list|(
literal|0
argument_list|,
name|d5
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected5
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d6
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0.30000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|d6
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected6
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|floor
argument_list|(
literal|0
argument_list|,
name|d6
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected6
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCeiling
parameter_list|()
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|new
name|DecimalColumnVector
argument_list|(
literal|4
argument_list|,
literal|20
argument_list|,
literal|13
argument_list|)
decl_stmt|;
name|HiveDecimal
name|d1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.56778"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"20"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|ceiling
argument_list|(
literal|0
argument_list|,
name|d1
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected1
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// As of HIVE-8745, these decimal values should be trimmed of trailing zeros.
name|HiveDecimal
name|d2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"23.00000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|d2
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"23"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|ceiling
argument_list|(
literal|0
argument_list|,
name|d2
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected2
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.34567"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|ceiling
argument_list|(
literal|0
argument_list|,
name|d3
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected3
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d4
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-17.00000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|d4
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected4
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-17"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|ceiling
argument_list|(
literal|0
argument_list|,
name|d4
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected4
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d5
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-0.30000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|d5
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected5
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|ceiling
argument_list|(
literal|0
argument_list|,
name|d5
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected5
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d6
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0.30000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|d6
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected6
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|ceiling
argument_list|(
literal|0
argument_list|,
name|d6
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected6
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAbs
parameter_list|()
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|new
name|DecimalColumnVector
argument_list|(
literal|4
argument_list|,
literal|20
argument_list|,
literal|13
argument_list|)
decl_stmt|;
name|HiveDecimal
name|d1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.56778"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|abs
argument_list|(
literal|0
argument_list|,
name|d1
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|d1
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.34567"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"25.34567"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|abs
argument_list|(
literal|0
argument_list|,
name|d2
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected2
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRound
parameter_list|()
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|new
name|DecimalColumnVector
argument_list|(
literal|4
argument_list|,
literal|20
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|HiveDecimal
name|d1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.56778"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"20"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d1
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected1
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// As of HIVE-8745, these decimal values should be trimmed of trailing zeros.
name|HiveDecimal
name|d2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"23.00000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|d2
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"23"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d2
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected2
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.34567"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d3
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected3
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d4
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-17.00000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|d4
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected4
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-17"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d4
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected4
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d5
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.36778"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected5
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d5
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected5
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d6
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.54567"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected6
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-26"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d6
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected6
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRoundWithDigits
parameter_list|()
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|new
name|DecimalColumnVector
argument_list|(
literal|4
argument_list|,
literal|20
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|HiveDecimal
name|d1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.56778"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.568"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d1
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected1
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// As of HIVE-8745, these decimal values should be trimmed of trailing zeros.
name|HiveDecimal
name|d2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"23.56700"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|d2
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"23.567"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d2
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected2
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.34567"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.346"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d3
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected3
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d4
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-17.23400"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|d4
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected4
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-17.234"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d4
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected4
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d5
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.36748"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected5
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.367"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d5
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected5
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d6
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.54537"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected6
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.545"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|round
argument_list|(
literal|0
argument_list|,
name|d6
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected6
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNegate
parameter_list|()
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|new
name|DecimalColumnVector
argument_list|(
literal|4
argument_list|,
literal|20
argument_list|,
literal|13
argument_list|)
decl_stmt|;
name|HiveDecimal
name|d1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.56778"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-19.56778"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|negate
argument_list|(
literal|0
argument_list|,
name|d1
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected1
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.34567"
argument_list|)
decl_stmt|;
name|HiveDecimal
name|expected2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"25.34567"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|negate
argument_list|(
literal|0
argument_list|,
name|d2
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected2
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// As of HIVE-8745, these decimal values should be trimmed of trailing zeros.
name|HiveDecimal
name|d3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0.00000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|d3
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|HiveDecimal
name|expected3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|negate
argument_list|(
literal|0
argument_list|,
name|d3
argument_list|,
name|dcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|expected3
operator|.
name|compareTo
argument_list|(
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSign
parameter_list|()
block|{
name|LongColumnVector
name|lcv
init|=
operator|new
name|LongColumnVector
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|HiveDecimal
name|d1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"19.56778"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|sign
argument_list|(
literal|0
argument_list|,
name|d1
argument_list|,
name|lcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|lcv
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d2
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"-25.34567"
argument_list|)
decl_stmt|;
name|DecimalUtil
operator|.
name|sign
argument_list|(
literal|0
argument_list|,
name|d2
argument_list|,
name|lcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|lcv
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|HiveDecimal
name|d3
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0.00000"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|d3
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|DecimalUtil
operator|.
name|sign
argument_list|(
literal|0
argument_list|,
name|d3
argument_list|,
name|lcv
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|lcv
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

