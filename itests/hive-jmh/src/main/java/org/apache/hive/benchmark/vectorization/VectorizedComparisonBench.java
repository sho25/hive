begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|benchmark
operator|.
name|vectorization
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
operator|.
name|gen
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|annotations
operator|.
name|Scope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|annotations
operator|.
name|State
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|runner
operator|.
name|Runner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|runner
operator|.
name|RunnerException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|runner
operator|.
name|options
operator|.
name|Options
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|runner
operator|.
name|options
operator|.
name|OptionsBuilder
import|;
end_import

begin_comment
comment|/**  * This test measures the performance for vectorization.  *<p/>  * This test uses JMH framework for benchmarking.  * You may execute this benchmark tool using JMH command line in different ways:  *<p/>  * To use the settings shown in the main() function, use:  * $ java -cp target/benchmarks.jar org.apache.hive.benchmark.vectorization.VectorizedComparisonBench  *<p/>  * To use the default settings used by JMH, use:  * $ java -jar target/benchmarks.jar org.apache.hive.benchmark.vectorization.VectorizedComparisonBench  *<p/>  * To specify different parameters, use:  * - This command will use 10 warm-up iterations, 5 test iterations, and 2 forks. And it will  * display the Average Time (avgt) in Microseconds (us)  * - Benchmark mode. Available modes are:  * [Throughput/thrpt, AverageTime/avgt, SampleTime/sample, SingleShotTime/ss, All/all]  * - Output time unit. Available time units are: [m, s, ms, us, ns].  *<p/>  * $ java -jar target/benchmarks.jar org.apache.hive.benchmark.vectorization.VectorizedComparisonBench  * -wi 10 -i 5 -f 2 -bm avgt -tu us  */
end_comment

begin_class
annotation|@
name|State
argument_list|(
name|Scope
operator|.
name|Benchmark
argument_list|)
specifier|public
class|class
name|VectorizedComparisonBench
block|{
specifier|public
specifier|static
class|class
name|LongColEqualLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|2
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColEqualLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColGreaterEqualLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|2
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColGreaterEqualLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColGreaterLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|2
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColGreaterLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColLessEqualLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|2
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColLessEqualLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColLessLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|2
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColLessLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColNotEqualLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|2
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColNotEqualLongColumn
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColEqualLongScalarBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColEqualLongScalar
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColGreaterEqualLongScalarBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColGreaterEqualLongScalar
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColGreaterLongScalarBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColGreaterLongScalar
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColLessEqualLongScalarBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColLessEqualLongScalar
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColLessLongScalarBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColLessLongScalar
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongColNotEqualLongScalarBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongColNotEqualLongScalar
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongScalarEqualLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongScalarEqualLongColumn
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongScalarGreaterEqualLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongScalarGreaterEqualLongColumn
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongScalarGreaterLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongScalarGreaterLongColumn
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongScalarLessEqualLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongScalarLessEqualLongColumn
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongScalarLessLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongScalarLessLongColumn
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LongScalarNotEqualLongColumnBench
extends|extends
name|AbstractExpression
block|{
annotation|@
name|Override
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|rowBatch
operator|=
name|buildRowBatch
argument_list|(
operator|new
name|LongColumnVector
argument_list|()
argument_list|,
literal|1
argument_list|,
name|getLongColumnVector
argument_list|()
argument_list|)
expr_stmt|;
name|expression
operator|=
operator|new
name|LongScalarNotEqualLongColumn
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|RunnerException
block|{
name|Options
name|opt
init|=
operator|new
name|OptionsBuilder
argument_list|()
operator|.
name|include
argument_list|(
literal|".*"
operator|+
name|VectorizedComparisonBench
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".*"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
operator|new
name|Runner
argument_list|(
name|opt
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

