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
name|udf
operator|.
name|generic
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
name|GenericUDAFPercentileCont
operator|.
name|PercentileContDoubleEvaluator
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
name|GenericUDAFPercentileCont
operator|.
name|PercentileContEvaluator
operator|.
name|PercentileAgg
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
name|GenericUDAFPercentileCont
operator|.
name|PercentileContLongCalculator
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
name|GenericUDAFPercentileCont
operator|.
name|PercentileContLongEvaluator
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
name|io
operator|.
name|LongWritable
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

begin_comment
comment|/**  * Test class for GenericUDAFPercentileCont.  */
end_comment

begin_class
specifier|public
class|class
name|TestGenericUDAFPercentileCont
block|{
name|PercentileContLongCalculator
name|calc
init|=
operator|new
name|PercentileContLongCalculator
argument_list|()
decl_stmt|;
comment|// Long type tests
annotation|@
name|Test
specifier|public
name|void
name|testNoInterpolation
parameter_list|()
throws|throws
name|Exception
block|{
name|Long
index|[]
name|items
init|=
operator|new
name|Long
index|[]
block|{
literal|1L
block|,
literal|2L
block|,
literal|3L
block|,
literal|4L
block|,
literal|5L
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.5
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInterpolateLower
parameter_list|()
throws|throws
name|Exception
block|{
name|Long
index|[]
name|items
init|=
operator|new
name|Long
index|[]
block|{
literal|1L
block|,
literal|2L
block|,
literal|3L
block|,
literal|4L
block|,
literal|5L
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.49
argument_list|,
literal|2.96
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInterpolateHigher
parameter_list|()
throws|throws
name|Exception
block|{
name|Long
index|[]
name|items
init|=
operator|new
name|Long
index|[]
block|{
literal|1L
block|,
literal|2L
block|,
literal|3L
block|,
literal|4L
block|,
literal|5L
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.51
argument_list|,
literal|3.04
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleItem50
parameter_list|()
throws|throws
name|Exception
block|{
name|Long
index|[]
name|items
init|=
operator|new
name|Long
index|[]
block|{
literal|1L
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.5
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleItem100
parameter_list|()
throws|throws
name|Exception
block|{
name|Long
index|[]
name|items
init|=
operator|new
name|Long
index|[]
block|{
literal|1L
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/*    * POSTGRES check: WITH vals (k) AS (VALUES (54), (35), (15), (15), (76), (87), (78)) SELECT *    * INTO table percentile_src FROM vals; select percentile_cont(.50) within group (order by k) as    * perc from percentile_src;    */
annotation|@
name|Test
specifier|public
name|void
name|testPostgresRefExample
parameter_list|()
throws|throws
name|Exception
block|{
name|Long
index|[]
name|items
init|=
operator|new
name|Long
index|[]
block|{
literal|54L
block|,
literal|35L
block|,
literal|15L
block|,
literal|15L
block|,
literal|76L
block|,
literal|87L
block|,
literal|78L
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.5
argument_list|,
literal|54
argument_list|)
expr_stmt|;
block|}
comment|/*    * POSTGRES check: WITH vals (k) AS (VALUES (54), (35), (15), (15), (76), (87), (78)) SELECT *    * INTO table percentile_src FROM vals; select percentile_cont(.72) within group (order by k) as    * perc from percentile_src;    */
annotation|@
name|Test
specifier|public
name|void
name|testPostgresRefExample2
parameter_list|()
throws|throws
name|Exception
block|{
name|Long
index|[]
name|items
init|=
operator|new
name|Long
index|[]
block|{
literal|54L
block|,
literal|35L
block|,
literal|15L
block|,
literal|15L
block|,
literal|76L
block|,
literal|87L
block|,
literal|78L
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.72
argument_list|,
literal|76.64
argument_list|)
expr_stmt|;
block|}
comment|// Double type tests
annotation|@
name|Test
specifier|public
name|void
name|testDoubleNoInterpolation
parameter_list|()
throws|throws
name|Exception
block|{
name|Double
index|[]
name|items
init|=
operator|new
name|Double
index|[]
block|{
literal|1.0
block|,
literal|2.0
block|,
literal|3.0
block|,
literal|4.0
block|,
literal|5.0
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.5
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoubleInterpolateLower
parameter_list|()
throws|throws
name|Exception
block|{
name|Double
index|[]
name|items
init|=
operator|new
name|Double
index|[]
block|{
literal|1.0
block|,
literal|2.0
block|,
literal|3.0
block|,
literal|4.0
block|,
literal|5.0
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.49
argument_list|,
literal|2.96
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoubleInterpolateHigher
parameter_list|()
throws|throws
name|Exception
block|{
name|Double
index|[]
name|items
init|=
operator|new
name|Double
index|[]
block|{
literal|1.0
block|,
literal|2.0
block|,
literal|3.0
block|,
literal|4.0
block|,
literal|5.0
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.51
argument_list|,
literal|3.04
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoubleSingleItem50
parameter_list|()
throws|throws
name|Exception
block|{
name|Double
index|[]
name|items
init|=
operator|new
name|Double
index|[]
block|{
literal|1.0
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.5
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoubleSingleItem100
parameter_list|()
throws|throws
name|Exception
block|{
name|Double
index|[]
name|items
init|=
operator|new
name|Double
index|[]
block|{
literal|1.0
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/*    * POSTGRES check: WITH vals (k) AS (VALUES (54.0), (35.0), (15.0), (15.0), (76.0), (87.0),    * (78.0)) SELECT * INTO table percentile_src FROM vals; select percentile_cont(.50) within group    * (order by k) as perc from percentile_src;    */
annotation|@
name|Test
specifier|public
name|void
name|testDoublePostgresRefExample
parameter_list|()
throws|throws
name|Exception
block|{
name|Double
index|[]
name|items
init|=
operator|new
name|Double
index|[]
block|{
literal|54.0
block|,
literal|35.0
block|,
literal|15.0
block|,
literal|15.0
block|,
literal|76.0
block|,
literal|87.0
block|,
literal|78.0
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.5
argument_list|,
literal|54
argument_list|)
expr_stmt|;
block|}
comment|/*    * POSTGRES check: WITH vals (k) AS (VALUES (54.5), (35.3), (15.7), (15.7), (76.8), (87.34),    * (78.0)) SELECT * INTO table percentile_src FROM vals; select percentile_cont(.72) within group    * (order by k) as perc from percentile_src;    */
annotation|@
name|Test
specifier|public
name|void
name|testDoublePostgresRefExample2
parameter_list|()
throws|throws
name|Exception
block|{
name|Double
index|[]
name|items
init|=
operator|new
name|Double
index|[]
block|{
literal|54.5
block|,
literal|35.3
block|,
literal|15.7
block|,
literal|15.7
block|,
literal|76.8
block|,
literal|87.34
block|,
literal|78.0
block|}
decl_stmt|;
name|checkPercentile
argument_list|(
name|items
argument_list|,
literal|0.72
argument_list|,
literal|77.184
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"resource"
block|}
argument_list|)
specifier|private
name|void
name|checkPercentile
parameter_list|(
name|Long
index|[]
name|items
parameter_list|,
name|double
name|percentile
parameter_list|,
name|double
name|expected
parameter_list|)
throws|throws
name|Exception
block|{
name|PercentileContLongEvaluator
name|eval
init|=
operator|new
name|GenericUDAFPercentileCont
operator|.
name|PercentileContLongEvaluator
argument_list|()
decl_stmt|;
name|PercentileAgg
name|agg
init|=
operator|new
name|PercentileContLongEvaluator
argument_list|()
operator|.
operator|new
name|PercentileAgg
argument_list|()
decl_stmt|;
name|agg
operator|.
name|percentiles
operator|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|()
expr_stmt|;
name|agg
operator|.
name|percentiles
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|percentile
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|items
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|eval
operator|.
name|increment
argument_list|(
name|agg
argument_list|,
operator|new
name|LongWritable
argument_list|(
name|items
index|[
name|i
index|]
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|DoubleWritable
name|result
init|=
operator|(
name|DoubleWritable
operator|)
name|eval
operator|.
name|terminate
argument_list|(
name|agg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|result
operator|.
name|get
argument_list|()
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|eval
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"resource"
block|}
argument_list|)
specifier|private
name|void
name|checkPercentile
parameter_list|(
name|Double
index|[]
name|items
parameter_list|,
name|double
name|percentile
parameter_list|,
name|double
name|expected
parameter_list|)
throws|throws
name|Exception
block|{
name|PercentileContDoubleEvaluator
name|eval
init|=
operator|new
name|GenericUDAFPercentileCont
operator|.
name|PercentileContDoubleEvaluator
argument_list|()
decl_stmt|;
name|PercentileAgg
name|agg
init|=
operator|new
name|PercentileContLongEvaluator
argument_list|()
operator|.
operator|new
name|PercentileAgg
argument_list|()
decl_stmt|;
name|agg
operator|.
name|percentiles
operator|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|()
expr_stmt|;
name|agg
operator|.
name|percentiles
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|percentile
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|items
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|eval
operator|.
name|increment
argument_list|(
name|agg
argument_list|,
operator|new
name|DoubleWritable
argument_list|(
name|items
index|[
name|i
index|]
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|DoubleWritable
name|result
init|=
operator|(
name|DoubleWritable
operator|)
name|eval
operator|.
name|terminate
argument_list|(
name|agg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|result
operator|.
name|get
argument_list|()
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|eval
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

