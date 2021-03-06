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
name|udaf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|metadata
operator|.
name|HiveException
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
name|parse
operator|.
name|WindowingSpec
operator|.
name|BoundarySpec
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
name|udaf
operator|.
name|TestStreamingSum
operator|.
name|TypeHandler
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFAverage
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
name|serde2
operator|.
name|io
operator|.
name|DoubleWritable
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
name|serde2
operator|.
name|io
operator|.
name|HiveDecimalWritable
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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

begin_class
specifier|public
class|class
name|TestStreamingAvg
block|{
specifier|public
name|void
name|avgDouble
parameter_list|(
name|Iterator
argument_list|<
name|Double
argument_list|>
name|inVals
parameter_list|,
name|int
name|inSz
parameter_list|,
name|int
name|numPreceding
parameter_list|,
name|int
name|numFollowing
parameter_list|,
name|Iterator
argument_list|<
name|Double
argument_list|>
name|outVals
parameter_list|)
throws|throws
name|HiveException
block|{
name|GenericUDAFAverage
name|fnR
init|=
operator|new
name|GenericUDAFAverage
argument_list|()
decl_stmt|;
name|TypeInfo
index|[]
name|inputTypes
init|=
block|{
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
block|}
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
block|}
decl_stmt|;
name|DoubleWritable
index|[]
name|in
init|=
operator|new
name|DoubleWritable
index|[
literal|1
index|]
decl_stmt|;
name|in
index|[
literal|0
index|]
operator|=
operator|new
name|DoubleWritable
argument_list|()
expr_stmt|;
name|TestStreamingSum
operator|.
name|_agg
argument_list|(
name|fnR
argument_list|,
name|inputTypes
argument_list|,
name|inVals
argument_list|,
name|TypeHandler
operator|.
name|DoubleHandler
argument_list|,
name|in
argument_list|,
name|inputOIs
argument_list|,
name|inSz
argument_list|,
name|numPreceding
argument_list|,
name|numFollowing
argument_list|,
name|outVals
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|avgHiveDecimal
parameter_list|(
name|Iterator
argument_list|<
name|HiveDecimal
argument_list|>
name|inVals
parameter_list|,
name|int
name|inSz
parameter_list|,
name|int
name|numPreceding
parameter_list|,
name|int
name|numFollowing
parameter_list|,
name|Iterator
argument_list|<
name|HiveDecimal
argument_list|>
name|outVals
parameter_list|)
throws|throws
name|HiveException
block|{
name|GenericUDAFAverage
name|fnR
init|=
operator|new
name|GenericUDAFAverage
argument_list|()
decl_stmt|;
name|TypeInfo
index|[]
name|inputTypes
init|=
block|{
name|TypeInfoFactory
operator|.
name|decimalTypeInfo
block|}
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|writableHiveDecimalObjectInspector
block|}
decl_stmt|;
name|HiveDecimalWritable
index|[]
name|in
init|=
operator|new
name|HiveDecimalWritable
index|[
literal|1
index|]
decl_stmt|;
name|in
index|[
literal|0
index|]
operator|=
operator|new
name|HiveDecimalWritable
argument_list|()
expr_stmt|;
name|TestStreamingSum
operator|.
name|_agg
argument_list|(
name|fnR
argument_list|,
name|inputTypes
argument_list|,
name|inVals
argument_list|,
name|TypeHandler
operator|.
name|HiveDecimalHandler
argument_list|,
name|in
argument_list|,
name|inputOIs
argument_list|,
name|inSz
argument_list|,
name|numPreceding
argument_list|,
name|numFollowing
argument_list|,
name|outVals
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDouble_3_4
parameter_list|()
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|Double
argument_list|>
name|inVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1.0
argument_list|,
literal|2.0
argument_list|,
literal|3.0
argument_list|,
literal|4.0
argument_list|,
literal|5.0
argument_list|,
literal|6.0
argument_list|,
literal|7.0
argument_list|,
literal|8.0
argument_list|,
literal|9.0
argument_list|,
literal|10.0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Double
argument_list|>
name|outVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|15.0
operator|/
literal|5
argument_list|,
literal|21.0
operator|/
literal|6
argument_list|,
literal|28.0
operator|/
literal|7
argument_list|,
literal|36.0
operator|/
literal|8
argument_list|,
literal|44.0
operator|/
literal|8
argument_list|,
literal|52.0
operator|/
literal|8
argument_list|,
literal|49.0
operator|/
literal|7
argument_list|,
literal|45.0
operator|/
literal|6
argument_list|,
literal|40.0
operator|/
literal|5
argument_list|,
literal|34.0
operator|/
literal|4
argument_list|)
decl_stmt|;
name|avgDouble
argument_list|(
name|inVals
operator|.
name|iterator
argument_list|()
argument_list|,
literal|10
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
name|outVals
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHiveDecimal_3_4
parameter_list|()
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|HiveDecimal
argument_list|>
name|inVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|1L
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|2L
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|3L
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|4L
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|5L
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|6L
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|7L
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|8L
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|9L
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|10L
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HiveDecimal
argument_list|>
name|outVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|15.0
operator|/
literal|5
argument_list|)
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|21.0
operator|/
literal|6
argument_list|)
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|28.0
operator|/
literal|7
argument_list|)
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|36.0
operator|/
literal|8
argument_list|)
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|44.0
operator|/
literal|8
argument_list|)
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|52.0
operator|/
literal|8
argument_list|)
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|49.0
operator|/
literal|7
argument_list|)
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|45.0
operator|/
literal|6
argument_list|)
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|40.0
operator|/
literal|5
argument_list|)
argument_list|)
argument_list|,
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|34.0
operator|/
literal|4
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|avgHiveDecimal
argument_list|(
name|inVals
operator|.
name|iterator
argument_list|()
argument_list|,
literal|10
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
name|outVals
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDouble_3_0
parameter_list|()
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|Double
argument_list|>
name|inVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1.0
argument_list|,
literal|2.0
argument_list|,
literal|3.0
argument_list|,
literal|4.0
argument_list|,
literal|5.0
argument_list|,
literal|6.0
argument_list|,
literal|7.0
argument_list|,
literal|8.0
argument_list|,
literal|9.0
argument_list|,
literal|10.0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Double
argument_list|>
name|outVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1.0
operator|/
literal|1
argument_list|,
literal|3.0
operator|/
literal|2
argument_list|,
literal|6.0
operator|/
literal|3
argument_list|,
literal|10.0
operator|/
literal|4
argument_list|,
literal|14.0
operator|/
literal|4
argument_list|,
literal|18.0
operator|/
literal|4
argument_list|,
literal|22.0
operator|/
literal|4
argument_list|,
literal|26.0
operator|/
literal|4
argument_list|,
literal|30.0
operator|/
literal|4
argument_list|,
literal|34.0
operator|/
literal|4
argument_list|)
decl_stmt|;
name|avgDouble
argument_list|(
name|inVals
operator|.
name|iterator
argument_list|()
argument_list|,
literal|10
argument_list|,
literal|3
argument_list|,
literal|0
argument_list|,
name|outVals
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDouble_unb_0
parameter_list|()
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|Double
argument_list|>
name|inVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1.0
argument_list|,
literal|2.0
argument_list|,
literal|3.0
argument_list|,
literal|4.0
argument_list|,
literal|5.0
argument_list|,
literal|6.0
argument_list|,
literal|7.0
argument_list|,
literal|8.0
argument_list|,
literal|9.0
argument_list|,
literal|10.0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Double
argument_list|>
name|outVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1.0
operator|/
literal|1
argument_list|,
literal|3.0
operator|/
literal|2
argument_list|,
literal|6.0
operator|/
literal|3
argument_list|,
literal|10.0
operator|/
literal|4
argument_list|,
literal|15.0
operator|/
literal|5
argument_list|,
literal|21.0
operator|/
literal|6
argument_list|,
literal|28.0
operator|/
literal|7
argument_list|,
literal|36.0
operator|/
literal|8
argument_list|,
literal|45.0
operator|/
literal|9
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|)
decl_stmt|;
name|avgDouble
argument_list|(
name|inVals
operator|.
name|iterator
argument_list|()
argument_list|,
literal|10
argument_list|,
name|BoundarySpec
operator|.
name|UNBOUNDED_AMOUNT
argument_list|,
literal|0
argument_list|,
name|outVals
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDouble_0_5
parameter_list|()
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|Double
argument_list|>
name|inVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1.0
argument_list|,
literal|2.0
argument_list|,
literal|3.0
argument_list|,
literal|4.0
argument_list|,
literal|5.0
argument_list|,
literal|6.0
argument_list|,
literal|7.0
argument_list|,
literal|8.0
argument_list|,
literal|9.0
argument_list|,
literal|10.0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Double
argument_list|>
name|outVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|21.0
operator|/
literal|6
argument_list|,
literal|27.0
operator|/
literal|6
argument_list|,
literal|33.0
operator|/
literal|6
argument_list|,
literal|39.0
operator|/
literal|6
argument_list|,
literal|45.0
operator|/
literal|6
argument_list|,
literal|40.0
operator|/
literal|5
argument_list|,
literal|34.0
operator|/
literal|4
argument_list|,
literal|27.0
operator|/
literal|3
argument_list|,
literal|19.0
operator|/
literal|2
argument_list|,
literal|10.0
operator|/
literal|1
argument_list|)
decl_stmt|;
name|avgDouble
argument_list|(
name|inVals
operator|.
name|iterator
argument_list|()
argument_list|,
literal|10
argument_list|,
literal|0
argument_list|,
literal|5
argument_list|,
name|outVals
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDouble_unb_5
parameter_list|()
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|Double
argument_list|>
name|inVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1.0
argument_list|,
literal|2.0
argument_list|,
literal|3.0
argument_list|,
literal|4.0
argument_list|,
literal|5.0
argument_list|,
literal|6.0
argument_list|,
literal|7.0
argument_list|,
literal|8.0
argument_list|,
literal|9.0
argument_list|,
literal|10.0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Double
argument_list|>
name|outVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|21.0
operator|/
literal|6
argument_list|,
literal|28.0
operator|/
literal|7
argument_list|,
literal|36.0
operator|/
literal|8
argument_list|,
literal|45.0
operator|/
literal|9
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|)
decl_stmt|;
name|avgDouble
argument_list|(
name|inVals
operator|.
name|iterator
argument_list|()
argument_list|,
literal|10
argument_list|,
name|BoundarySpec
operator|.
name|UNBOUNDED_AMOUNT
argument_list|,
literal|5
argument_list|,
name|outVals
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDouble_7_2
parameter_list|()
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|Double
argument_list|>
name|inVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1.0
argument_list|,
literal|2.0
argument_list|,
literal|3.0
argument_list|,
literal|4.0
argument_list|,
literal|5.0
argument_list|,
literal|6.0
argument_list|,
literal|7.0
argument_list|,
literal|8.0
argument_list|,
literal|9.0
argument_list|,
literal|10.0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Double
argument_list|>
name|outVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|6.0
operator|/
literal|3
argument_list|,
literal|10.0
operator|/
literal|4
argument_list|,
literal|15.0
operator|/
literal|5
argument_list|,
literal|21.0
operator|/
literal|6
argument_list|,
literal|28.0
operator|/
literal|7
argument_list|,
literal|36.0
operator|/
literal|8
argument_list|,
literal|45.0
operator|/
literal|9
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|54.0
operator|/
literal|9
argument_list|,
literal|52.0
operator|/
literal|8
argument_list|)
decl_stmt|;
name|avgDouble
argument_list|(
name|inVals
operator|.
name|iterator
argument_list|()
argument_list|,
literal|10
argument_list|,
literal|7
argument_list|,
literal|2
argument_list|,
name|outVals
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDouble_15_15
parameter_list|()
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|Double
argument_list|>
name|inVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|1.0
argument_list|,
literal|2.0
argument_list|,
literal|3.0
argument_list|,
literal|4.0
argument_list|,
literal|5.0
argument_list|,
literal|6.0
argument_list|,
literal|7.0
argument_list|,
literal|8.0
argument_list|,
literal|9.0
argument_list|,
literal|10.0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Double
argument_list|>
name|outVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|,
literal|55.0
operator|/
literal|10
argument_list|)
decl_stmt|;
name|avgDouble
argument_list|(
name|inVals
operator|.
name|iterator
argument_list|()
argument_list|,
literal|10
argument_list|,
literal|15
argument_list|,
literal|15
argument_list|,
name|outVals
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

