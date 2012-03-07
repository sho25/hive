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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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

begin_comment
comment|/**  * A generic, re-usable histogram class that supports partial aggregations.  * The algorithm is a heuristic adapted from the following paper:  * Yael Ben-Haim and Elad Tom-Tov, "A streaming parallel decision tree algorithm",  * J. Machine Learning Research 11 (2010), pp. 849--872. Although there are no approximation  * guarantees, it appears to work well with adequate data and a large (e.g., 20-80) number  * of histogram bins.  */
end_comment

begin_class
specifier|public
class|class
name|NumericHistogram
block|{
comment|/**    * The Coord class defines a histogram bin, which is just an (x,y) pair.    */
specifier|static
class|class
name|Coord
implements|implements
name|Comparable
block|{
name|double
name|x
decl_stmt|;
name|double
name|y
decl_stmt|;
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
name|Coord
name|o
init|=
operator|(
name|Coord
operator|)
name|other
decl_stmt|;
if|if
condition|(
name|x
operator|<
name|o
operator|.
name|x
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|x
operator|>
name|o
operator|.
name|x
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
block|}
empty_stmt|;
comment|// Class variables
specifier|private
name|int
name|nbins
decl_stmt|;
specifier|private
name|int
name|nusedbins
decl_stmt|;
specifier|private
name|Coord
index|[]
name|bins
decl_stmt|;
specifier|private
name|Random
name|prng
decl_stmt|;
comment|/**    * Creates a new histogram object. Note that the allocate() or merge()    * method must be called before the histogram can be used.    */
specifier|public
name|NumericHistogram
parameter_list|()
block|{
name|nbins
operator|=
literal|0
expr_stmt|;
name|nusedbins
operator|=
literal|0
expr_stmt|;
name|bins
operator|=
literal|null
expr_stmt|;
comment|// init the RNG for breaking ties in histogram merging. A fixed seed is specified here
comment|// to aid testing, but can be eliminated to use a time-based seed (which would
comment|// make the algorithm non-deterministic).
name|prng
operator|=
operator|new
name|Random
argument_list|(
literal|31183
argument_list|)
expr_stmt|;
block|}
comment|/**    * Resets a histogram object to its initial state. allocate() or merge() must be    * called again before use.    */
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|bins
operator|=
literal|null
expr_stmt|;
name|nbins
operator|=
name|nusedbins
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Returns the number of bins currently being used by the histogram.    */
specifier|public
name|int
name|getUsedBins
parameter_list|()
block|{
return|return
name|nusedbins
return|;
block|}
comment|/**    * Returns true if this histogram object has been initialized by calling merge()    * or allocate().    */
specifier|public
name|boolean
name|isReady
parameter_list|()
block|{
return|return
name|nbins
operator|!=
literal|0
return|;
block|}
comment|/**    * Returns a particular histogram bin.    */
specifier|public
name|Coord
name|getBin
parameter_list|(
name|int
name|b
parameter_list|)
block|{
return|return
name|bins
index|[
name|b
index|]
return|;
block|}
comment|/**    * Sets the number of histogram bins to use for approximating data.    *    * @param num_bins Number of non-uniform-width histogram bins to use    */
specifier|public
name|void
name|allocate
parameter_list|(
name|int
name|num_bins
parameter_list|)
block|{
name|nbins
operator|=
name|num_bins
expr_stmt|;
name|bins
operator|=
operator|new
name|Coord
index|[
name|nbins
operator|+
literal|1
index|]
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
name|nbins
operator|+
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|bins
index|[
name|i
index|]
operator|=
operator|new
name|Coord
argument_list|()
expr_stmt|;
block|}
name|nusedbins
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Takes a serialized histogram created by the serialize() method and merges    * it with the current histogram object.    *    * @param other A serialized histogram created by the serialize() method    * @see #merge    */
specifier|public
name|void
name|merge
parameter_list|(
name|List
argument_list|<
name|DoubleWritable
argument_list|>
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|nbins
operator|==
literal|0
operator|||
name|nusedbins
operator|==
literal|0
condition|)
block|{
comment|// Our aggregation buffer has nothing in it, so just copy over 'other'
comment|// by deserializing the ArrayList of (x,y) pairs into an array of Coord objects
name|nbins
operator|=
operator|(
name|int
operator|)
name|other
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|nusedbins
operator|=
operator|(
name|other
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|)
operator|/
literal|2
expr_stmt|;
name|bins
operator|=
operator|new
name|Coord
index|[
name|nbins
operator|+
literal|1
index|]
expr_stmt|;
comment|// +1 to hold a temporary bin for insert()
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|other
operator|.
name|size
argument_list|()
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|bins
index|[
operator|(
name|i
operator|-
literal|1
operator|)
operator|/
literal|2
index|]
operator|=
operator|new
name|Coord
argument_list|()
expr_stmt|;
name|bins
index|[
operator|(
name|i
operator|-
literal|1
operator|)
operator|/
literal|2
index|]
operator|.
name|x
operator|=
name|other
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|bins
index|[
operator|(
name|i
operator|-
literal|1
operator|)
operator|/
literal|2
index|]
operator|.
name|y
operator|=
name|other
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// The aggregation buffer already contains a partial histogram. Therefore, we need
comment|// to merge histograms using Algorithm #2 from the Ben-Haim and Tom-Tov paper.
name|Coord
index|[]
name|tmp_bins
init|=
operator|new
name|Coord
index|[
name|nusedbins
operator|+
operator|(
name|other
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|)
operator|/
literal|2
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|tmp_bins
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|tmp_bins
index|[
name|j
index|]
operator|=
operator|new
name|Coord
argument_list|()
expr_stmt|;
block|}
comment|// Copy all the histogram bins from us and 'other' into an overstuffed histogram
name|int
name|i
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|nusedbins
condition|;
name|i
operator|++
control|)
block|{
name|tmp_bins
index|[
name|i
index|]
operator|.
name|x
operator|=
name|bins
index|[
name|i
index|]
operator|.
name|x
expr_stmt|;
name|tmp_bins
index|[
name|i
index|]
operator|.
name|y
operator|=
name|bins
index|[
name|i
index|]
operator|.
name|y
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<
name|other
operator|.
name|size
argument_list|()
condition|;
name|j
operator|+=
literal|2
operator|,
name|i
operator|++
control|)
block|{
name|tmp_bins
index|[
name|i
index|]
operator|.
name|x
operator|=
name|other
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|tmp_bins
index|[
name|i
index|]
operator|.
name|y
operator|=
name|other
operator|.
name|get
argument_list|(
name|j
operator|+
literal|1
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|Arrays
operator|.
name|sort
argument_list|(
name|tmp_bins
argument_list|)
expr_stmt|;
comment|// Now trim the overstuffed histogram down to the correct number of bins
name|bins
operator|=
name|tmp_bins
expr_stmt|;
name|nusedbins
operator|+=
operator|(
name|other
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|)
operator|/
literal|2
expr_stmt|;
name|trim
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Adds a new data point to the histogram approximation. Make sure you have    * called either allocate() or merge() first. This method implements Algorithm #1    * from Ben-Haim and Tom-Tov, "A Streaming Parallel Decision Tree Algorithm", JMLR 2010.    *    * @param v The data point to add to the histogram approximation.    */
specifier|public
name|void
name|add
parameter_list|(
name|double
name|v
parameter_list|)
block|{
comment|// Binary search to find the closest bucket that v should go into.
comment|// 'bin' should be interpreted as the bin to shift right in order to accomodate
comment|// v. As a result, bin is in the range [0,N], where N means that the value v is
comment|// greater than all the N bins currently in the histogram. It is also possible that
comment|// a bucket centered at 'v' already exists, so this must be checked in the next step.
name|int
name|bin
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|l
init|=
literal|0
init|,
name|r
init|=
name|nusedbins
init|;
name|l
operator|<
name|r
condition|;
control|)
block|{
name|bin
operator|=
operator|(
name|l
operator|+
name|r
operator|)
operator|/
literal|2
expr_stmt|;
if|if
condition|(
name|bins
index|[
name|bin
index|]
operator|.
name|x
operator|>
name|v
condition|)
block|{
name|r
operator|=
name|bin
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|bins
index|[
name|bin
index|]
operator|.
name|x
operator|<
name|v
condition|)
block|{
name|l
operator|=
operator|++
name|bin
expr_stmt|;
block|}
else|else
block|{
break|break;
comment|// break loop on equal comparator
block|}
block|}
block|}
comment|// If we found an exact bin match for value v, then just increment that bin's count.
comment|// Otherwise, we need to insert a new bin and trim the resulting histogram back to size.
comment|// A possible optimization here might be to set some threshold under which 'v' is just
comment|// assumed to be equal to the closest bin -- if fabs(v-bins[bin].x)< THRESHOLD, then
comment|// just increment 'bin'. This is not done now because we don't want to make any
comment|// assumptions about the range of numeric data being analyzed.
if|if
condition|(
name|bin
operator|<
name|nusedbins
operator|&&
name|bins
index|[
name|bin
index|]
operator|.
name|x
operator|==
name|v
condition|)
block|{
name|bins
index|[
name|bin
index|]
operator|.
name|y
operator|++
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
name|nusedbins
init|;
name|i
operator|>
name|bin
condition|;
name|i
operator|--
control|)
block|{
name|bins
index|[
name|i
index|]
operator|.
name|x
operator|=
name|bins
index|[
name|i
operator|-
literal|1
index|]
operator|.
name|x
expr_stmt|;
name|bins
index|[
name|i
index|]
operator|.
name|y
operator|=
name|bins
index|[
name|i
operator|-
literal|1
index|]
operator|.
name|y
expr_stmt|;
block|}
name|bins
index|[
name|bin
index|]
operator|.
name|x
operator|=
name|v
expr_stmt|;
comment|// new bins bin for value 'v'
name|bins
index|[
name|bin
index|]
operator|.
name|y
operator|=
literal|1
expr_stmt|;
comment|// of height 1 unit
comment|// Trim the bins down to the correct number of bins.
if|if
condition|(
operator|++
name|nusedbins
operator|>
name|nbins
condition|)
block|{
name|trim
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Trims a histogram down to 'nbins' bins by iteratively merging the closest bins.    * If two pairs of bins are equally close to each other, decide uniformly at random which    * pair to merge, based on a PRNG.    */
specifier|private
name|void
name|trim
parameter_list|()
block|{
while|while
condition|(
name|nusedbins
operator|>
name|nbins
condition|)
block|{
comment|// Find the closest pair of bins in terms of x coordinates. Break ties randomly.
name|double
name|smallestdiff
init|=
name|bins
index|[
literal|1
index|]
operator|.
name|x
operator|-
name|bins
index|[
literal|0
index|]
operator|.
name|x
decl_stmt|;
name|int
name|smallestdiffloc
init|=
literal|0
decl_stmt|,
name|smallestdiffcount
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|nusedbins
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|double
name|diff
init|=
name|bins
index|[
name|i
operator|+
literal|1
index|]
operator|.
name|x
operator|-
name|bins
index|[
name|i
index|]
operator|.
name|x
decl_stmt|;
if|if
condition|(
name|diff
operator|<
name|smallestdiff
condition|)
block|{
name|smallestdiff
operator|=
name|diff
expr_stmt|;
name|smallestdiffloc
operator|=
name|i
expr_stmt|;
name|smallestdiffcount
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|diff
operator|==
name|smallestdiff
operator|&&
name|prng
operator|.
name|nextDouble
argument_list|()
operator|<=
operator|(
literal|1.0
operator|/
operator|++
name|smallestdiffcount
operator|)
condition|)
block|{
name|smallestdiffloc
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
comment|// Merge the two closest bins into their average x location, weighted by their heights.
comment|// The height of the new bin is the sum of the heights of the old bins.
name|double
name|d
init|=
name|bins
index|[
name|smallestdiffloc
index|]
operator|.
name|y
operator|+
name|bins
index|[
name|smallestdiffloc
operator|+
literal|1
index|]
operator|.
name|y
decl_stmt|;
name|bins
index|[
name|smallestdiffloc
index|]
operator|.
name|x
operator|*=
name|bins
index|[
name|smallestdiffloc
index|]
operator|.
name|y
operator|/
name|d
expr_stmt|;
name|bins
index|[
name|smallestdiffloc
index|]
operator|.
name|x
operator|+=
name|bins
index|[
name|smallestdiffloc
operator|+
literal|1
index|]
operator|.
name|x
operator|/
name|d
operator|*
name|bins
index|[
name|smallestdiffloc
operator|+
literal|1
index|]
operator|.
name|y
expr_stmt|;
name|bins
index|[
name|smallestdiffloc
index|]
operator|.
name|y
operator|=
name|d
expr_stmt|;
comment|// Shift the remaining bins left one position
for|for
control|(
name|int
name|i
init|=
name|smallestdiffloc
operator|+
literal|1
init|;
name|i
operator|<
name|nusedbins
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|bins
index|[
name|i
index|]
operator|.
name|x
operator|=
name|bins
index|[
name|i
operator|+
literal|1
index|]
operator|.
name|x
expr_stmt|;
name|bins
index|[
name|i
index|]
operator|.
name|y
operator|=
name|bins
index|[
name|i
operator|+
literal|1
index|]
operator|.
name|y
expr_stmt|;
block|}
name|nusedbins
operator|--
expr_stmt|;
block|}
block|}
comment|/**    * Gets an approximate quantile value from the current histogram. Some popular    * quantiles are 0.5 (median), 0.95, and 0.98.    *    * @param q The requested quantile, must be strictly within the range (0,1).    * @return The quantile value.    */
specifier|public
name|double
name|quantile
parameter_list|(
name|double
name|q
parameter_list|)
block|{
assert|assert
operator|(
name|bins
operator|!=
literal|null
operator|&&
name|nusedbins
operator|>
literal|0
operator|&&
name|nbins
operator|>
literal|0
operator|)
assert|;
name|double
name|sum
init|=
literal|0
decl_stmt|,
name|csum
init|=
literal|0
decl_stmt|;
name|int
name|b
decl_stmt|;
for|for
control|(
name|b
operator|=
literal|0
init|;
name|b
operator|<
name|nusedbins
condition|;
name|b
operator|++
control|)
block|{
name|sum
operator|+=
name|bins
index|[
name|b
index|]
operator|.
name|y
expr_stmt|;
block|}
for|for
control|(
name|b
operator|=
literal|0
init|;
name|b
operator|<
name|nusedbins
condition|;
name|b
operator|++
control|)
block|{
name|csum
operator|+=
name|bins
index|[
name|b
index|]
operator|.
name|y
expr_stmt|;
if|if
condition|(
name|csum
operator|/
name|sum
operator|>=
name|q
condition|)
block|{
if|if
condition|(
name|b
operator|==
literal|0
condition|)
block|{
return|return
name|bins
index|[
name|b
index|]
operator|.
name|x
return|;
block|}
name|csum
operator|-=
name|bins
index|[
name|b
index|]
operator|.
name|y
expr_stmt|;
name|double
name|r
init|=
name|bins
index|[
name|b
operator|-
literal|1
index|]
operator|.
name|x
operator|+
operator|(
name|q
operator|*
name|sum
operator|-
name|csum
operator|)
operator|*
operator|(
name|bins
index|[
name|b
index|]
operator|.
name|x
operator|-
name|bins
index|[
name|b
operator|-
literal|1
index|]
operator|.
name|x
operator|)
operator|/
operator|(
name|bins
index|[
name|b
index|]
operator|.
name|y
operator|)
decl_stmt|;
return|return
name|r
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
comment|// for Xlint, code will never reach here
block|}
comment|/**    * In preparation for a Hive merge() call, serializes the current histogram object into an    * ArrayList of DoubleWritable objects. This list is deserialized and merged by the    * merge method.    *    * @return An ArrayList of Hadoop DoubleWritable objects that represents the current    * histogram.    * @see #merge    */
specifier|public
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
name|serialize
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|()
decl_stmt|;
comment|// Return a single ArrayList where the first element is the number of bins bins,
comment|// and subsequent elements represent bins (x,y) pairs.
name|result
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|nbins
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|bins
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nusedbins
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|bins
index|[
name|i
index|]
operator|.
name|x
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|bins
index|[
name|i
index|]
operator|.
name|y
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

