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
name|udf
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
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|Description
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
name|UDAF
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
name|UDAFEvaluator
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

begin_comment
comment|/**  * UDAF for calculating the percentile values.  * There are several definitions of percentile, and we take the method recommended by  * NIST.  * @see http://en.wikipedia.org/wiki/Percentile#Alternative_methods  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"percentile"
argument_list|,
name|value
operator|=
literal|"_FUNC_(expr, pc) - Returns the percentile(s) of expr at pc (range: [0,1])."
operator|+
literal|"pc can be a double or double array"
argument_list|)
specifier|public
class|class
name|UDAFPercentile
extends|extends
name|UDAF
block|{
comment|/**    * A state class to store intermediate aggregation results.    */
specifier|public
specifier|static
class|class
name|State
block|{
specifier|private
name|Map
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
name|counts
decl_stmt|;
specifier|private
name|List
argument_list|<
name|DoubleWritable
argument_list|>
name|percentiles
decl_stmt|;
block|}
comment|/**    * A comparator to sort the entries in order.    */
specifier|public
specifier|static
class|class
name|MyComparator
implements|implements
name|Comparator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
name|o1
parameter_list|,
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getKey
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * Increment the State object with o as the key, and i as the count.    */
specifier|private
specifier|static
name|void
name|increment
parameter_list|(
name|State
name|s
parameter_list|,
name|LongWritable
name|o
parameter_list|,
name|long
name|i
parameter_list|)
block|{
if|if
condition|(
name|s
operator|.
name|counts
operator|==
literal|null
condition|)
block|{
name|s
operator|.
name|counts
operator|=
operator|new
name|HashMap
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|LongWritable
name|count
init|=
name|s
operator|.
name|counts
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
if|if
condition|(
name|count
operator|==
literal|null
condition|)
block|{
comment|// We have to create a new object, because the object o belongs
comment|// to the code that creates it and may get its value changed.
name|LongWritable
name|key
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
name|key
operator|.
name|set
argument_list|(
name|o
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|s
operator|.
name|counts
operator|.
name|put
argument_list|(
name|key
argument_list|,
operator|new
name|LongWritable
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|count
operator|.
name|set
argument_list|(
name|count
operator|.
name|get
argument_list|()
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the percentile value.    */
specifier|private
specifier|static
name|double
name|getPercentile
parameter_list|(
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
argument_list|>
name|entriesList
parameter_list|,
name|double
name|position
parameter_list|)
block|{
comment|// We may need to do linear interpolation to get the exact percentile
name|long
name|lower
init|=
operator|(
name|long
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|position
argument_list|)
decl_stmt|;
name|long
name|higher
init|=
operator|(
name|long
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|position
argument_list|)
decl_stmt|;
comment|// Linear search since this won't take much time from the total execution anyway
comment|// lower has the range of [0 .. total-1]
comment|// The first entry with accumulated count (lower+1) corresponds to the lower position.
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|entriesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
operator|<
name|lower
operator|+
literal|1
condition|)
block|{
name|i
operator|++
expr_stmt|;
block|}
name|long
name|lowerKey
init|=
name|entriesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getKey
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|higher
operator|==
name|lower
condition|)
block|{
comment|// no interpolation needed because position does not have a fraction
return|return
name|lowerKey
return|;
block|}
if|if
condition|(
name|entriesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
operator|<
name|higher
operator|+
literal|1
condition|)
block|{
name|i
operator|++
expr_stmt|;
block|}
name|long
name|higherKey
init|=
name|entriesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getKey
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|higherKey
operator|==
name|lowerKey
condition|)
block|{
comment|// no interpolation needed because lower position and higher position has the same key
return|return
name|lowerKey
return|;
block|}
comment|// Linear interpolation to get the exact percentile
return|return
operator|(
name|higher
operator|-
name|position
operator|)
operator|*
name|lowerKey
operator|+
operator|(
name|position
operator|-
name|lower
operator|)
operator|*
name|higherKey
return|;
block|}
comment|/**    * The evaluator for percentile computation based on long.    */
specifier|public
specifier|static
class|class
name|PercentileLongEvaluator
implements|implements
name|UDAFEvaluator
block|{
specifier|private
specifier|final
name|State
name|state
decl_stmt|;
specifier|public
name|PercentileLongEvaluator
parameter_list|()
block|{
name|state
operator|=
operator|new
name|State
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
if|if
condition|(
name|state
operator|.
name|counts
operator|!=
literal|null
condition|)
block|{
name|state
operator|.
name|counts
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|iterate
parameter_list|(
name|LongWritable
name|o
parameter_list|,
name|double
name|percentile
parameter_list|)
block|{
if|if
condition|(
name|state
operator|.
name|percentiles
operator|==
literal|null
condition|)
block|{
name|state
operator|.
name|percentiles
operator|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|state
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
block|}
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
name|increment
argument_list|(
name|state
argument_list|,
name|o
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|State
name|terminatePartial
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|public
name|boolean
name|merge
parameter_list|(
name|State
name|other
parameter_list|)
block|{
if|if
condition|(
name|state
operator|.
name|percentiles
operator|==
literal|null
condition|)
block|{
name|state
operator|.
name|percentiles
operator|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|(
name|other
operator|.
name|percentiles
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|counts
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
name|e
range|:
name|other
operator|.
name|counts
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|increment
argument_list|(
name|state
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|DoubleWritable
name|result
decl_stmt|;
specifier|public
name|DoubleWritable
name|terminate
parameter_list|()
block|{
comment|// No input data.
if|if
condition|(
name|state
operator|.
name|counts
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Get all items into an array and sort them.
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
argument_list|>
name|entries
init|=
name|state
operator|.
name|counts
operator|.
name|entrySet
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
argument_list|>
name|entriesList
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
argument_list|>
argument_list|(
name|entries
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|entriesList
argument_list|,
operator|new
name|MyComparator
argument_list|()
argument_list|)
expr_stmt|;
comment|// Accumulate the counts.
name|long
name|total
init|=
literal|0
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
name|entriesList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|LongWritable
name|count
init|=
name|entriesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|total
operator|+=
name|count
operator|.
name|get
argument_list|()
expr_stmt|;
name|count
operator|.
name|set
argument_list|(
name|total
argument_list|)
expr_stmt|;
block|}
comment|// Initialize the result.
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|DoubleWritable
argument_list|()
expr_stmt|;
block|}
comment|// maxPosition is the 1.0 percentile
name|long
name|maxPosition
init|=
name|total
operator|-
literal|1
decl_stmt|;
name|double
name|position
init|=
name|maxPosition
operator|*
name|state
operator|.
name|percentiles
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|result
operator|.
name|set
argument_list|(
name|getPercentile
argument_list|(
name|entriesList
argument_list|,
name|position
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
comment|/**    * The evaluator for percentile computation based on long for an array of percentiles.    */
specifier|public
specifier|static
class|class
name|PercentileLongArrayEvaluator
implements|implements
name|UDAFEvaluator
block|{
specifier|private
specifier|final
name|State
name|state
decl_stmt|;
specifier|public
name|PercentileLongArrayEvaluator
parameter_list|()
block|{
name|state
operator|=
operator|new
name|State
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
if|if
condition|(
name|state
operator|.
name|counts
operator|!=
literal|null
condition|)
block|{
name|state
operator|.
name|counts
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|iterate
parameter_list|(
name|LongWritable
name|o
parameter_list|,
name|List
argument_list|<
name|DoubleWritable
argument_list|>
name|percentiles
parameter_list|)
block|{
if|if
condition|(
name|state
operator|.
name|percentiles
operator|==
literal|null
condition|)
block|{
name|state
operator|.
name|percentiles
operator|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|(
name|percentiles
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
name|increment
argument_list|(
name|state
argument_list|,
name|o
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|State
name|terminatePartial
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|public
name|boolean
name|merge
parameter_list|(
name|State
name|other
parameter_list|)
block|{
if|if
condition|(
name|state
operator|.
name|percentiles
operator|==
literal|null
condition|)
block|{
name|state
operator|.
name|percentiles
operator|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|(
name|other
operator|.
name|percentiles
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|counts
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
name|e
range|:
name|other
operator|.
name|counts
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|increment
argument_list|(
name|state
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|List
argument_list|<
name|DoubleWritable
argument_list|>
name|results
decl_stmt|;
specifier|public
name|List
argument_list|<
name|DoubleWritable
argument_list|>
name|terminate
parameter_list|()
block|{
comment|// No input data
if|if
condition|(
name|state
operator|.
name|counts
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Get all items into an array and sort them
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
argument_list|>
name|entries
init|=
name|state
operator|.
name|counts
operator|.
name|entrySet
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
argument_list|>
name|entriesList
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|LongWritable
argument_list|,
name|LongWritable
argument_list|>
argument_list|>
argument_list|(
name|entries
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|entriesList
argument_list|,
operator|new
name|MyComparator
argument_list|()
argument_list|)
expr_stmt|;
comment|// accumulate the counts
name|long
name|total
init|=
literal|0
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
name|entriesList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|LongWritable
name|count
init|=
name|entriesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|total
operator|+=
name|count
operator|.
name|get
argument_list|()
expr_stmt|;
name|count
operator|.
name|set
argument_list|(
name|total
argument_list|)
expr_stmt|;
block|}
comment|// maxPosition is the 1.0 percentile
name|long
name|maxPosition
init|=
name|total
operator|-
literal|1
decl_stmt|;
comment|// Initialize the results
if|if
condition|(
name|results
operator|==
literal|null
condition|)
block|{
name|results
operator|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|()
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
name|state
operator|.
name|percentiles
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|results
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Set the results
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|state
operator|.
name|percentiles
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|double
name|position
init|=
name|maxPosition
operator|*
name|state
operator|.
name|percentiles
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|set
argument_list|(
name|getPercentile
argument_list|(
name|entriesList
argument_list|,
name|position
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
block|}
block|}
end_class

end_unit

