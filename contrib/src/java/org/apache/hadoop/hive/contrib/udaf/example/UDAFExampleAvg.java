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
name|contrib
operator|.
name|udaf
operator|.
name|example
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

begin_comment
comment|/**  * This is a simple UDAF that calculates average.  *   * It should be very easy to follow and can be used as an example  * for writing new UDAFs.  *    * Note that Hive internally uses a different mechanism (called  * GenericUDAF) to implement built-in aggregation functions, which  * are harder to program but more efficient.  *   */
end_comment

begin_class
specifier|public
class|class
name|UDAFExampleAvg
extends|extends
name|UDAF
block|{
comment|/**    * The internal state of an aggregation for average.    *     * Note that this is only needed if the internal state cannot be    * represented by a primitive.    *     * The internal state can also contains fields with types like    * ArrayList<String> and HashMap<String,Double> if needed.     */
specifier|public
specifier|static
class|class
name|UDAFAvgState
block|{
specifier|private
name|long
name|mCount
decl_stmt|;
specifier|private
name|double
name|mSum
decl_stmt|;
block|}
comment|/**    * The actual class for doing the aggregation.    * Hive will automatically look for all internal classes of the UDAF    * that implements UDAFEvaluator.      */
specifier|public
specifier|static
class|class
name|UDAFExampleAvgEvaluator
implements|implements
name|UDAFEvaluator
block|{
name|UDAFAvgState
name|state
decl_stmt|;
specifier|public
name|UDAFExampleAvgEvaluator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|state
operator|=
operator|new
name|UDAFAvgState
argument_list|()
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
comment|/**      * Reset the state of the aggregation.      */
specifier|public
name|void
name|init
parameter_list|()
block|{
name|state
operator|.
name|mSum
operator|=
literal|0
expr_stmt|;
name|state
operator|.
name|mCount
operator|=
literal|0
expr_stmt|;
block|}
comment|/**      * Iterate through one row of original data.      *       * The number and type of arguments need to the same as we call      * this UDAF from Hive command line.      *       * This function should always return true.      */
specifier|public
name|boolean
name|iterate
parameter_list|(
name|Double
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
name|state
operator|.
name|mSum
operator|+=
name|o
expr_stmt|;
name|state
operator|.
name|mCount
operator|++
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Terminate a partial aggregation and return the state.      * If the state is a primitive, just return primitive Java classes      * like Integer or String.      */
specifier|public
name|UDAFAvgState
name|terminatePartial
parameter_list|()
block|{
comment|// This is SQL standard - average of zero items should be null.
return|return
name|state
operator|.
name|mCount
operator|==
literal|0
condition|?
literal|null
else|:
name|state
return|;
block|}
comment|/**      * Merge with a partial aggregation.      *       * This function should always have a single argument which has      * the same type as the return value of terminatePartial().        */
specifier|public
name|boolean
name|merge
parameter_list|(
name|UDAFAvgState
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
name|state
operator|.
name|mSum
operator|+=
name|o
operator|.
name|mSum
expr_stmt|;
name|state
operator|.
name|mCount
operator|+=
name|o
operator|.
name|mCount
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Terminates the aggregation and return the final result.      */
specifier|public
name|Double
name|terminate
parameter_list|()
block|{
comment|// This is SQL standard - average of zero items should be null.
return|return
name|state
operator|.
name|mCount
operator|==
literal|0
condition|?
literal|null
else|:
name|Double
operator|.
name|valueOf
argument_list|(
name|state
operator|.
name|mSum
operator|/
name|state
operator|.
name|mCount
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

