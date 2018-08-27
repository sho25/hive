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
name|metastore
operator|.
name|tools
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|math3
operator|.
name|stat
operator|.
name|descriptive
operator|.
name|DescriptiveStatistics
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jetbrains
operator|.
name|annotations
operator|.
name|NotNull
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jetbrains
operator|.
name|annotations
operator|.
name|Nullable
import|;
end_import

begin_comment
comment|/**  * Micro-benchmark some piece of code.<p>  *  * Every benchmark has three parts:  *<ul>  *<li>Optional pre-test</li>  *<li>Mandatory test</lI>  *<li>Optional post-test</li>  *</ul>  * Measurement consists of the warm-up phase and measurement phase.  * Consumer can specify number of times the warmup and measurement is repeated.<p>  * All time is measured in nanoseconds.  */
end_comment

begin_class
class|class
name|MicroBenchmark
block|{
comment|// Specify defaults
specifier|private
specifier|static
specifier|final
name|int
name|WARMUP_DEFAULT
init|=
literal|15
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ITERATIONS_DEFAULT
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SCALE_DEFAULT
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|int
name|warmup
decl_stmt|;
specifier|private
specifier|final
name|int
name|iterations
decl_stmt|;
specifier|private
specifier|final
name|int
name|scaleFactor
decl_stmt|;
comment|/**    * Create default micro benchmark measurer    */
specifier|public
name|MicroBenchmark
parameter_list|()
block|{
name|this
argument_list|(
name|WARMUP_DEFAULT
argument_list|,
name|ITERATIONS_DEFAULT
argument_list|,
name|SCALE_DEFAULT
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create micro benchmark measurer.    * @param warmup number of test calls for warmup    * @param iterations number of test calls for measurement    */
name|MicroBenchmark
parameter_list|(
name|int
name|warmup
parameter_list|,
name|int
name|iterations
parameter_list|)
block|{
name|this
argument_list|(
name|warmup
argument_list|,
name|iterations
argument_list|,
name|SCALE_DEFAULT
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create micro benchmark measurer.    *    * @param warmup number of test calls for warmup    * @param iterations number of test calls for measurement    * @param scaleFactor Every delta is divided by scale factor    */
specifier|private
name|MicroBenchmark
parameter_list|(
name|int
name|warmup
parameter_list|,
name|int
name|iterations
parameter_list|,
name|int
name|scaleFactor
parameter_list|)
block|{
name|this
operator|.
name|warmup
operator|=
name|warmup
expr_stmt|;
name|this
operator|.
name|iterations
operator|=
name|iterations
expr_stmt|;
name|this
operator|.
name|scaleFactor
operator|=
name|scaleFactor
expr_stmt|;
block|}
comment|/**    * Run the benchmark and measure run-time statistics in nanoseconds.<p>    * Before the run the warm-up phase is executed.    * @param pre Optional pre-test setup    * @param test Mandatory test    * @param post Optional post-test cleanup    * @return Statistics describing the results. All times are in nanoseconds.    */
specifier|public
name|DescriptiveStatistics
name|measure
parameter_list|(
annotation|@
name|Nullable
name|Runnable
name|pre
parameter_list|,
annotation|@
name|NotNull
name|Runnable
name|test
parameter_list|,
annotation|@
name|Nullable
name|Runnable
name|post
parameter_list|)
block|{
comment|// Warmup phase
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|warmup
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|pre
operator|!=
literal|null
condition|)
block|{
name|pre
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
name|test
operator|.
name|run
argument_list|()
expr_stmt|;
if|if
condition|(
name|post
operator|!=
literal|null
condition|)
block|{
name|post
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Run the benchmark
name|DescriptiveStatistics
name|stats
init|=
operator|new
name|DescriptiveStatistics
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
name|iterations
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|pre
operator|!=
literal|null
condition|)
block|{
name|pre
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|test
operator|.
name|run
argument_list|()
expr_stmt|;
name|long
name|end
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|stats
operator|.
name|addValue
argument_list|(
call|(
name|double
call|)
argument_list|(
name|end
operator|-
name|start
argument_list|)
operator|/
name|scaleFactor
argument_list|)
expr_stmt|;
if|if
condition|(
name|post
operator|!=
literal|null
condition|)
block|{
name|post
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|stats
return|;
block|}
comment|/**    * Run the benchmark and measure run-time statistics in nanoseconds.<p>    * Before the run the warm-up phase is executed. No pre or post operations are executed.    * @param test test to measure    * @return Statistics describing the results. All times are in nanoseconds.    */
specifier|public
name|DescriptiveStatistics
name|measure
parameter_list|(
annotation|@
name|NotNull
name|Runnable
name|test
parameter_list|)
block|{
return|return
name|measure
argument_list|(
literal|null
argument_list|,
name|test
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

