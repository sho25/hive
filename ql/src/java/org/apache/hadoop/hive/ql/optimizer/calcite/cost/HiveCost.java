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
name|optimizer
operator|.
name|calcite
operator|.
name|cost
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptCost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptCostFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptUtil
import|;
end_import

begin_comment
comment|// TODO: This should inherit from VolcanoCost and should just override isLE method.
end_comment

begin_class
specifier|public
class|class
name|HiveCost
implements|implements
name|RelOptCost
block|{
comment|// ~ Static fields/initializers ---------------------------------------------
specifier|public
specifier|static
specifier|final
name|HiveCost
name|INFINITY
init|=
operator|new
name|HiveCost
argument_list|(
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"{inf}"
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveCost
name|HUGE
init|=
operator|new
name|HiveCost
argument_list|(
name|Double
operator|.
name|MAX_VALUE
argument_list|,
name|Double
operator|.
name|MAX_VALUE
argument_list|,
name|Double
operator|.
name|MAX_VALUE
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"{huge}"
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveCost
name|ZERO
init|=
operator|new
name|HiveCost
argument_list|(
literal|0.0
argument_list|,
literal|0.0
argument_list|,
literal|0.0
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"{0}"
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveCost
name|TINY
init|=
operator|new
name|HiveCost
argument_list|(
literal|1.0
argument_list|,
literal|1.0
argument_list|,
literal|0.0
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"{tiny}"
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|RelOptCostFactory
name|FACTORY
init|=
operator|new
name|Factory
argument_list|()
decl_stmt|;
comment|// ~ Instance fields --------------------------------------------------------
specifier|final
name|double
name|cpu
decl_stmt|;
specifier|final
name|double
name|io
decl_stmt|;
specifier|final
name|double
name|rowCount
decl_stmt|;
comment|// ~ Constructors -----------------------------------------------------------
name|HiveCost
parameter_list|(
name|double
name|rowCount
parameter_list|,
name|double
name|cpu
parameter_list|,
name|double
name|io
parameter_list|)
block|{
assert|assert
name|rowCount
operator|>=
literal|0d
assert|;
assert|assert
name|cpu
operator|>=
literal|0d
assert|;
assert|assert
name|io
operator|>=
literal|0d
assert|;
name|this
operator|.
name|rowCount
operator|=
name|rowCount
expr_stmt|;
name|this
operator|.
name|cpu
operator|=
name|cpu
expr_stmt|;
name|this
operator|.
name|io
operator|=
name|io
expr_stmt|;
block|}
comment|// ~ Methods ----------------------------------------------------------------
specifier|public
name|double
name|getCpu
parameter_list|()
block|{
return|return
name|cpu
return|;
block|}
specifier|public
name|boolean
name|isInfinite
parameter_list|()
block|{
return|return
operator|(
name|this
operator|==
name|INFINITY
operator|)
operator|||
operator|(
name|this
operator|.
name|rowCount
operator|==
name|Double
operator|.
name|POSITIVE_INFINITY
operator|)
operator|||
operator|(
name|this
operator|.
name|cpu
operator|==
name|Double
operator|.
name|POSITIVE_INFINITY
operator|)
operator|||
operator|(
name|this
operator|.
name|io
operator|==
name|Double
operator|.
name|POSITIVE_INFINITY
operator|)
return|;
block|}
specifier|public
name|double
name|getIo
parameter_list|()
block|{
return|return
name|io
return|;
block|}
comment|// TODO: If two cost is equal, could we do any better than comparing
comment|// cardinality (may be some other heuristics to break the tie)
specifier|public
name|boolean
name|isLe
parameter_list|(
name|RelOptCost
name|other
parameter_list|)
block|{
return|return
name|this
operator|==
name|other
operator|||
name|this
operator|.
name|rowCount
operator|<=
name|other
operator|.
name|getRows
argument_list|()
return|;
comment|/*      * if (((this.dCpu + this.dIo)< (other.getCpu() + other.getIo())) ||      * ((this.dCpu + this.dIo) == (other.getCpu() + other.getIo())&& this.dRows      *<= other.getRows())) { return true; } else { return false; }      */
block|}
specifier|public
name|boolean
name|isLt
parameter_list|(
name|RelOptCost
name|other
parameter_list|)
block|{
return|return
name|this
operator|.
name|rowCount
operator|<
name|other
operator|.
name|getRows
argument_list|()
return|;
comment|/*      * return isLe(other)&& !equals(other);      */
block|}
specifier|public
name|double
name|getRows
parameter_list|()
block|{
return|return
name|rowCount
return|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|RelOptCost
name|other
parameter_list|)
block|{
return|return
operator|(
name|this
operator|==
name|other
operator|)
operator|||
operator|(
operator|(
name|this
operator|.
name|rowCount
operator|)
operator|==
operator|(
name|other
operator|.
name|getRows
argument_list|()
operator|)
operator|)
return|;
comment|/*      * //TODO: should we consider cardinality as well? return (this == other) ||      * ((this.dCpu + this.dIo) == (other.getCpu() + other.getIo()));      */
block|}
specifier|public
name|boolean
name|isEqWithEpsilon
parameter_list|(
name|RelOptCost
name|other
parameter_list|)
block|{
return|return
operator|(
name|this
operator|==
name|other
operator|)
operator|||
operator|(
name|Math
operator|.
name|abs
argument_list|(
operator|(
name|this
operator|.
name|rowCount
operator|)
operator|-
operator|(
name|other
operator|.
name|getRows
argument_list|()
operator|)
argument_list|)
operator|<
name|RelOptUtil
operator|.
name|EPSILON
operator|)
return|;
comment|// Turn this one once we do the Algorithm selection in CBO
comment|/*      * return (this == other) || (Math.abs((this.dCpu + this.dIo) -      * (other.getCpu() + other.getIo()))< RelOptUtil.EPSILON);      */
block|}
specifier|public
name|RelOptCost
name|minus
parameter_list|(
name|RelOptCost
name|other
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|INFINITY
condition|)
block|{
return|return
name|this
return|;
block|}
return|return
operator|new
name|HiveCost
argument_list|(
name|this
operator|.
name|rowCount
operator|-
name|other
operator|.
name|getRows
argument_list|()
argument_list|,
name|this
operator|.
name|cpu
operator|-
name|other
operator|.
name|getCpu
argument_list|()
argument_list|,
name|this
operator|.
name|io
operator|-
name|other
operator|.
name|getIo
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|RelOptCost
name|multiplyBy
parameter_list|(
name|double
name|factor
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|INFINITY
condition|)
block|{
return|return
name|this
return|;
block|}
return|return
operator|new
name|HiveCost
argument_list|(
name|rowCount
operator|*
name|factor
argument_list|,
name|cpu
operator|*
name|factor
argument_list|,
name|io
operator|*
name|factor
argument_list|)
return|;
block|}
specifier|public
name|double
name|divideBy
parameter_list|(
name|RelOptCost
name|cost
parameter_list|)
block|{
comment|// Compute the geometric average of the ratios of all of the factors
comment|// which are non-zero and finite.
name|double
name|d
init|=
literal|1
decl_stmt|;
name|double
name|n
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|(
name|this
operator|.
name|rowCount
operator|!=
literal|0
operator|)
operator|&&
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|this
operator|.
name|rowCount
argument_list|)
operator|&&
operator|(
name|cost
operator|.
name|getRows
argument_list|()
operator|!=
literal|0
operator|)
operator|&&
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|cost
operator|.
name|getRows
argument_list|()
argument_list|)
condition|)
block|{
name|d
operator|*=
name|this
operator|.
name|rowCount
operator|/
name|cost
operator|.
name|getRows
argument_list|()
expr_stmt|;
operator|++
name|n
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|this
operator|.
name|cpu
operator|!=
literal|0
operator|)
operator|&&
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|this
operator|.
name|cpu
argument_list|)
operator|&&
operator|(
name|cost
operator|.
name|getCpu
argument_list|()
operator|!=
literal|0
operator|)
operator|&&
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|cost
operator|.
name|getCpu
argument_list|()
argument_list|)
condition|)
block|{
name|d
operator|*=
name|this
operator|.
name|cpu
operator|/
name|cost
operator|.
name|getCpu
argument_list|()
expr_stmt|;
operator|++
name|n
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|this
operator|.
name|io
operator|!=
literal|0
operator|)
operator|&&
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|this
operator|.
name|io
argument_list|)
operator|&&
operator|(
name|cost
operator|.
name|getIo
argument_list|()
operator|!=
literal|0
operator|)
operator|&&
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|cost
operator|.
name|getIo
argument_list|()
argument_list|)
condition|)
block|{
name|d
operator|*=
name|this
operator|.
name|io
operator|/
name|cost
operator|.
name|getIo
argument_list|()
expr_stmt|;
operator|++
name|n
expr_stmt|;
block|}
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
return|return
literal|1.0
return|;
block|}
return|return
name|Math
operator|.
name|pow
argument_list|(
name|d
argument_list|,
literal|1
operator|/
name|n
argument_list|)
return|;
block|}
specifier|public
name|RelOptCost
name|plus
parameter_list|(
name|RelOptCost
name|other
parameter_list|)
block|{
if|if
condition|(
operator|(
name|this
operator|==
name|INFINITY
operator|)
operator|||
operator|(
name|other
operator|.
name|isInfinite
argument_list|()
operator|)
condition|)
block|{
return|return
name|INFINITY
return|;
block|}
return|return
operator|new
name|HiveCost
argument_list|(
name|this
operator|.
name|rowCount
operator|+
name|other
operator|.
name|getRows
argument_list|()
argument_list|,
name|this
operator|.
name|cpu
operator|+
name|other
operator|.
name|getCpu
argument_list|()
argument_list|,
name|this
operator|.
name|io
operator|+
name|other
operator|.
name|getIo
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"{"
operator|+
name|rowCount
operator|+
literal|" rows, "
operator|+
name|cpu
operator|+
literal|" cpu, "
operator|+
name|io
operator|+
literal|" io}"
return|;
block|}
specifier|private
specifier|static
class|class
name|Factory
implements|implements
name|RelOptCostFactory
block|{
specifier|private
name|Factory
parameter_list|()
block|{     }
specifier|public
name|RelOptCost
name|makeCost
parameter_list|(
name|double
name|rowCount
parameter_list|,
name|double
name|cpu
parameter_list|,
name|double
name|io
parameter_list|)
block|{
return|return
operator|new
name|HiveCost
argument_list|(
name|rowCount
argument_list|,
name|cpu
argument_list|,
name|io
argument_list|)
return|;
block|}
specifier|public
name|RelOptCost
name|makeHugeCost
parameter_list|()
block|{
return|return
name|HUGE
return|;
block|}
specifier|public
name|HiveCost
name|makeInfiniteCost
parameter_list|()
block|{
return|return
name|INFINITY
return|;
block|}
specifier|public
name|HiveCost
name|makeTinyCost
parameter_list|()
block|{
return|return
name|TINY
return|;
block|}
specifier|public
name|HiveCost
name|makeZeroCost
parameter_list|()
block|{
return|return
name|ZERO
return|;
block|}
block|}
block|}
end_class

end_unit

