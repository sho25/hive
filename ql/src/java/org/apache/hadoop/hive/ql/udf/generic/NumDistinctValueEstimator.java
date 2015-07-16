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
name|Random
import|;
end_import

begin_import
import|import
name|javolution
operator|.
name|util
operator|.
name|FastBitSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|util
operator|.
name|JavaDataModel
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
name|Text
import|;
end_import

begin_class
specifier|public
class|class
name|NumDistinctValueEstimator
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|NumDistinctValueEstimator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/* We want a,b,x to come from a finite field of size 0 to k, where k is a prime number.    * 2^p - 1 is prime for p = 31. Hence bitvectorSize has to be 31. Pick k to be 2^p -1.    * If a,b,x didn't come from a finite field ax1 + b mod k and ax2 + b mod k will not be pair wise    * independent. As a consequence, the hash values will not distribute uniformly from 0 to 2^p-1    * thus introducing errors in the estimates.    */
specifier|private
specifier|static
specifier|final
name|int
name|BIT_VECTOR_SIZE
init|=
literal|31
decl_stmt|;
specifier|private
specifier|final
name|int
name|numBitVectors
decl_stmt|;
comment|// Refer to Flajolet-Martin'86 for the value of phi
specifier|private
specifier|static
specifier|final
name|double
name|PHI
init|=
literal|0.77351
decl_stmt|;
specifier|private
specifier|final
name|int
index|[]
name|a
decl_stmt|;
specifier|private
specifier|final
name|int
index|[]
name|b
decl_stmt|;
specifier|private
specifier|final
name|FastBitSet
index|[]
name|bitVector
decl_stmt|;
specifier|private
specifier|final
name|Random
name|aValue
decl_stmt|;
specifier|private
specifier|final
name|Random
name|bValue
decl_stmt|;
comment|/* Create a new distinctValueEstimator    */
specifier|public
name|NumDistinctValueEstimator
parameter_list|(
name|int
name|numBitVectors
parameter_list|)
block|{
name|this
operator|.
name|numBitVectors
operator|=
name|numBitVectors
expr_stmt|;
name|bitVector
operator|=
operator|new
name|FastBitSet
index|[
name|numBitVectors
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
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|bitVector
index|[
name|i
index|]
operator|=
operator|new
name|FastBitSet
argument_list|(
name|BIT_VECTOR_SIZE
argument_list|)
expr_stmt|;
block|}
name|a
operator|=
operator|new
name|int
index|[
name|numBitVectors
index|]
expr_stmt|;
name|b
operator|=
operator|new
name|int
index|[
name|numBitVectors
index|]
expr_stmt|;
comment|/* Use a large prime number as a seed to the random number generator.      * Java's random number generator uses the Linear Congruential Generator to generate random      * numbers using the following recurrence relation,      *      * X(n+1) = (a X(n) + c ) mod m      *      *  where X0 is the seed. Java implementation uses m = 2^48. This is problematic because 2^48      *  is not a prime number and hence the set of numbers from 0 to m don't form a finite field.      *  If these numbers don't come from a finite field any give X(n) and X(n+1) may not be pair      *  wise independent.      *      *  However, empirically passing in prime numbers as seeds seems to work better than when passing      *  composite numbers as seeds. Ideally Java's Random should pick m such that m is prime.      *      */
name|aValue
operator|=
operator|new
name|Random
argument_list|(
literal|99397
argument_list|)
expr_stmt|;
name|bValue
operator|=
operator|new
name|Random
argument_list|(
literal|9876413
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
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|int
name|randVal
decl_stmt|;
comment|/* a and b shouldn't be even; If a and b are even, then none of the values        * will set bit 0 thus introducing errors in the estimate. Both a and b can be even        * 25% of the times and as a result 25% of the bit vectors could be inaccurate. To avoid this        * always pick odd values for a and b.        */
do|do
block|{
name|randVal
operator|=
name|aValue
operator|.
name|nextInt
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|randVal
operator|%
literal|2
operator|==
literal|0
condition|)
do|;
name|a
index|[
name|i
index|]
operator|=
name|randVal
expr_stmt|;
do|do
block|{
name|randVal
operator|=
name|bValue
operator|.
name|nextInt
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|randVal
operator|%
literal|2
operator|==
literal|0
condition|)
do|;
name|b
index|[
name|i
index|]
operator|=
name|randVal
expr_stmt|;
if|if
condition|(
name|a
index|[
name|i
index|]
operator|<
literal|0
condition|)
block|{
name|a
index|[
name|i
index|]
operator|=
name|a
index|[
name|i
index|]
operator|+
operator|(
literal|1
operator|<<
name|BIT_VECTOR_SIZE
operator|-
literal|1
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|b
index|[
name|i
index|]
operator|<
literal|0
condition|)
block|{
name|b
index|[
name|i
index|]
operator|=
name|b
index|[
name|i
index|]
operator|+
operator|(
literal|1
operator|<<
name|BIT_VECTOR_SIZE
operator|-
literal|1
operator|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|NumDistinctValueEstimator
parameter_list|(
name|String
name|s
parameter_list|,
name|int
name|numBitVectors
parameter_list|)
block|{
name|this
operator|.
name|numBitVectors
operator|=
name|numBitVectors
expr_stmt|;
name|FastBitSet
name|bitVectorDeser
index|[]
init|=
name|deserialize
argument_list|(
name|s
argument_list|,
name|numBitVectors
argument_list|)
decl_stmt|;
name|bitVector
operator|=
operator|new
name|FastBitSet
index|[
name|numBitVectors
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
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|bitVector
index|[
name|i
index|]
operator|=
operator|new
name|FastBitSet
argument_list|(
name|BIT_VECTOR_SIZE
argument_list|)
expr_stmt|;
name|bitVector
index|[
name|i
index|]
operator|.
name|clear
argument_list|()
expr_stmt|;
name|bitVector
index|[
name|i
index|]
operator|.
name|or
argument_list|(
name|bitVectorDeser
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|a
operator|=
literal|null
expr_stmt|;
name|b
operator|=
literal|null
expr_stmt|;
name|aValue
operator|=
literal|null
expr_stmt|;
name|bValue
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Resets a distinctValueEstimator object to its original state.    */
specifier|public
name|void
name|reset
parameter_list|()
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
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|bitVector
index|[
name|i
index|]
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|FastBitSet
name|getBitVector
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|bitVector
index|[
name|index
index|]
return|;
block|}
specifier|public
name|int
name|getnumBitVectors
parameter_list|()
block|{
return|return
name|numBitVectors
return|;
block|}
specifier|public
name|int
name|getBitVectorSize
parameter_list|()
block|{
return|return
name|BIT_VECTOR_SIZE
return|;
block|}
specifier|public
name|void
name|printNumDistinctValueEstimator
parameter_list|()
block|{
name|String
name|t
init|=
operator|new
name|String
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"NumDistinctValueEstimator"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Number of Vectors:"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|numBitVectors
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Vector Size: "
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|BIT_VECTOR_SIZE
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
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|t
operator|=
name|t
operator|+
name|bitVector
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Serialized Vectors: "
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
comment|/* Serializes a distinctValueEstimator object to Text for transport.    *    */
specifier|public
name|Text
name|serialize
parameter_list|()
block|{
name|String
name|s
init|=
operator|new
name|String
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
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|s
operator|=
name|s
operator|+
operator|(
name|bitVector
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
operator|)
expr_stmt|;
block|}
return|return
operator|new
name|Text
argument_list|(
name|s
argument_list|)
return|;
block|}
comment|/* Deserializes from string to FastBitSet; Creates a NumDistinctValueEstimator object and    * returns it.    */
specifier|private
name|FastBitSet
index|[]
name|deserialize
parameter_list|(
name|String
name|s
parameter_list|,
name|int
name|numBitVectors
parameter_list|)
block|{
name|FastBitSet
index|[]
name|b
init|=
operator|new
name|FastBitSet
index|[
name|numBitVectors
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
name|numBitVectors
condition|;
name|j
operator|++
control|)
block|{
name|b
index|[
name|j
index|]
operator|=
operator|new
name|FastBitSet
argument_list|(
name|BIT_VECTOR_SIZE
argument_list|)
expr_stmt|;
name|b
index|[
name|j
index|]
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|int
name|vectorIndex
init|=
literal|0
decl_stmt|;
comment|/* Parse input string to obtain the indexes that are set in the bitvector.      * When a toString() is called on a FastBitSet object to serialize it, the serialization      * adds { and } to the beginning and end of the return String.      * Skip "{", "}", ",", " " in the input string.      */
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|s
operator|.
name|length
argument_list|()
operator|-
literal|1
condition|;
control|)
block|{
name|char
name|c
init|=
name|s
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|i
operator|=
name|i
operator|+
literal|1
expr_stmt|;
comment|// Move on to the next bit vector
if|if
condition|(
name|c
operator|==
literal|'}'
condition|)
block|{
name|vectorIndex
operator|=
name|vectorIndex
operator|+
literal|1
expr_stmt|;
block|}
comment|// Encountered a numeric value; Extract out the entire number
if|if
condition|(
name|c
operator|>=
literal|'0'
operator|&&
name|c
operator|<=
literal|'9'
condition|)
block|{
name|String
name|t
init|=
operator|new
name|String
argument_list|()
decl_stmt|;
name|t
operator|=
name|t
operator|+
name|c
expr_stmt|;
name|c
operator|=
name|s
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|i
operator|=
name|i
operator|+
literal|1
expr_stmt|;
while|while
condition|(
name|c
operator|!=
literal|','
operator|&&
name|c
operator|!=
literal|'}'
condition|)
block|{
name|t
operator|=
name|t
operator|+
name|c
expr_stmt|;
name|c
operator|=
name|s
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|i
operator|=
name|i
operator|+
literal|1
expr_stmt|;
block|}
name|int
name|bitIndex
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|t
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|bitIndex
operator|>=
literal|0
operator|)
assert|;
assert|assert
operator|(
name|vectorIndex
operator|<
name|numBitVectors
operator|)
assert|;
name|b
index|[
name|vectorIndex
index|]
operator|.
name|set
argument_list|(
name|bitIndex
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|==
literal|'}'
condition|)
block|{
name|vectorIndex
operator|=
name|vectorIndex
operator|+
literal|1
expr_stmt|;
block|}
block|}
block|}
return|return
name|b
return|;
block|}
specifier|private
name|int
name|generateHash
parameter_list|(
name|long
name|v
parameter_list|,
name|int
name|hashNum
parameter_list|)
block|{
name|int
name|mod
init|=
operator|(
literal|1
operator|<<
name|BIT_VECTOR_SIZE
operator|)
operator|-
literal|1
decl_stmt|;
name|long
name|tempHash
init|=
name|a
index|[
name|hashNum
index|]
operator|*
name|v
operator|+
name|b
index|[
name|hashNum
index|]
decl_stmt|;
name|tempHash
operator|%=
name|mod
expr_stmt|;
name|int
name|hash
init|=
operator|(
name|int
operator|)
name|tempHash
decl_stmt|;
comment|/* Hash function should map the long value to 0...2^L-1.      * Hence hash value has to be non-negative.      */
if|if
condition|(
name|hash
operator|<
literal|0
condition|)
block|{
name|hash
operator|=
name|hash
operator|+
name|mod
expr_stmt|;
block|}
return|return
name|hash
return|;
block|}
specifier|private
name|int
name|generateHashForPCSA
parameter_list|(
name|long
name|v
parameter_list|)
block|{
name|int
name|mod
init|=
literal|1
operator|<<
operator|(
name|BIT_VECTOR_SIZE
operator|-
literal|1
operator|)
operator|-
literal|1
decl_stmt|;
name|long
name|tempHash
init|=
name|a
index|[
literal|0
index|]
operator|*
name|v
operator|+
name|b
index|[
literal|0
index|]
decl_stmt|;
name|tempHash
operator|%=
name|mod
expr_stmt|;
name|int
name|hash
init|=
operator|(
name|int
operator|)
name|tempHash
decl_stmt|;
comment|/* Hash function should map the long value to 0...2^L-1.      * Hence hash value has to be non-negative.      */
if|if
condition|(
name|hash
operator|<
literal|0
condition|)
block|{
name|hash
operator|=
name|hash
operator|+
name|mod
operator|+
literal|1
expr_stmt|;
block|}
return|return
name|hash
return|;
block|}
specifier|public
name|void
name|addToEstimator
parameter_list|(
name|long
name|v
parameter_list|)
block|{
comment|/* Update summary bitVector :      * Generate hash value of the long value and mod it by 2^bitVectorSize-1.      * In this implementation bitVectorSize is 31.      */
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|int
name|hash
init|=
name|generateHash
argument_list|(
name|v
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|int
name|index
decl_stmt|;
comment|// Find the index of the least significant bit that is 1
for|for
control|(
name|index
operator|=
literal|0
init|;
name|index
operator|<
name|BIT_VECTOR_SIZE
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
name|hash
operator|%
literal|2
operator|!=
literal|0
condition|)
block|{
break|break;
block|}
name|hash
operator|=
name|hash
operator|>>
literal|1
expr_stmt|;
block|}
comment|// Set bitvector[index] := 1
name|bitVector
index|[
name|i
index|]
operator|.
name|set
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addToEstimatorPCSA
parameter_list|(
name|long
name|v
parameter_list|)
block|{
name|int
name|hash
init|=
name|generateHashForPCSA
argument_list|(
name|v
argument_list|)
decl_stmt|;
name|int
name|rho
init|=
name|hash
operator|/
name|numBitVectors
decl_stmt|;
name|int
name|index
decl_stmt|;
comment|// Find the index of the least significant bit that is 1
for|for
control|(
name|index
operator|=
literal|0
init|;
name|index
operator|<
name|BIT_VECTOR_SIZE
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
name|rho
operator|%
literal|2
operator|!=
literal|0
condition|)
block|{
break|break;
block|}
name|rho
operator|=
name|rho
operator|>>
literal|1
expr_stmt|;
block|}
comment|// Set bitvector[index] := 1
name|bitVector
index|[
name|hash
operator|%
name|numBitVectors
index|]
operator|.
name|set
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addToEstimator
parameter_list|(
name|double
name|d
parameter_list|)
block|{
name|int
name|v
init|=
operator|new
name|Double
argument_list|(
name|d
argument_list|)
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|addToEstimator
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addToEstimatorPCSA
parameter_list|(
name|double
name|d
parameter_list|)
block|{
name|int
name|v
init|=
operator|new
name|Double
argument_list|(
name|d
argument_list|)
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|addToEstimatorPCSA
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addToEstimator
parameter_list|(
name|HiveDecimal
name|decimal
parameter_list|)
block|{
name|int
name|v
init|=
name|decimal
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|addToEstimator
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addToEstimatorPCSA
parameter_list|(
name|HiveDecimal
name|decimal
parameter_list|)
block|{
name|int
name|v
init|=
name|decimal
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|addToEstimatorPCSA
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|mergeEstimators
parameter_list|(
name|NumDistinctValueEstimator
name|o
parameter_list|)
block|{
comment|// Bitwise OR the bitvector with the bitvector in the agg buffer
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|bitVector
index|[
name|i
index|]
operator|.
name|or
argument_list|(
name|o
operator|.
name|getBitVector
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|long
name|estimateNumDistinctValuesPCSA
parameter_list|()
block|{
name|double
name|numDistinctValues
init|=
literal|0.0
decl_stmt|;
name|long
name|S
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
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|int
name|index
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|bitVector
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|&&
name|index
operator|<
name|BIT_VECTOR_SIZE
condition|)
block|{
name|index
operator|=
name|index
operator|+
literal|1
expr_stmt|;
block|}
name|S
operator|=
name|S
operator|+
name|index
expr_stmt|;
block|}
name|numDistinctValues
operator|=
operator|(
operator|(
name|numBitVectors
operator|/
name|PHI
operator|)
operator|*
name|Math
operator|.
name|pow
argument_list|(
literal|2.0
argument_list|,
name|S
operator|/
name|numBitVectors
argument_list|)
operator|)
expr_stmt|;
return|return
operator|(
operator|(
name|long
operator|)
name|numDistinctValues
operator|)
return|;
block|}
comment|/* We use the Flajolet-Martin estimator to estimate the number of distinct values.FM uses the    * location of the least significant zero as an estimate of log2(phi*ndvs).    */
specifier|public
name|long
name|estimateNumDistinctValues
parameter_list|()
block|{
name|int
name|sumLeastSigZero
init|=
literal|0
decl_stmt|;
name|double
name|avgLeastSigZero
decl_stmt|;
name|double
name|numDistinctValues
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
name|numBitVectors
condition|;
name|i
operator|++
control|)
block|{
name|int
name|leastSigZero
init|=
name|bitVector
index|[
name|i
index|]
operator|.
name|nextClearBit
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|sumLeastSigZero
operator|+=
name|leastSigZero
expr_stmt|;
block|}
name|avgLeastSigZero
operator|=
call|(
name|double
call|)
argument_list|(
name|sumLeastSigZero
operator|/
operator|(
name|numBitVectors
operator|*
literal|1.0
operator|)
argument_list|)
operator|-
operator|(
name|Math
operator|.
name|log
argument_list|(
name|PHI
argument_list|)
operator|/
name|Math
operator|.
name|log
argument_list|(
literal|2.0
argument_list|)
operator|)
expr_stmt|;
name|numDistinctValues
operator|=
name|Math
operator|.
name|pow
argument_list|(
literal|2.0
argument_list|,
name|avgLeastSigZero
argument_list|)
expr_stmt|;
return|return
operator|(
call|(
name|long
call|)
argument_list|(
name|numDistinctValues
argument_list|)
operator|)
return|;
block|}
specifier|public
name|int
name|lengthFor
parameter_list|(
name|JavaDataModel
name|model
parameter_list|)
block|{
name|int
name|length
init|=
name|model
operator|.
name|object
argument_list|()
decl_stmt|;
name|length
operator|+=
name|model
operator|.
name|primitive1
argument_list|()
operator|*
literal|2
expr_stmt|;
comment|// two int
name|length
operator|+=
name|model
operator|.
name|primitive2
argument_list|()
expr_stmt|;
comment|// one double
name|length
operator|+=
name|model
operator|.
name|lengthForRandom
argument_list|()
operator|*
literal|2
expr_stmt|;
comment|// two Random
name|int
name|numVector
init|=
name|getnumBitVectors
argument_list|()
decl_stmt|;
if|if
condition|(
name|numVector
operator|>
literal|0
condition|)
block|{
name|length
operator|+=
name|model
operator|.
name|array
argument_list|()
operator|*
literal|3
expr_stmt|;
comment|// three array
name|length
operator|+=
name|model
operator|.
name|primitive1
argument_list|()
operator|*
name|numVector
operator|*
literal|2
expr_stmt|;
comment|// two int array
name|length
operator|+=
operator|(
name|model
operator|.
name|object
argument_list|()
operator|+
name|model
operator|.
name|array
argument_list|()
operator|+
name|model
operator|.
name|primitive1
argument_list|()
operator|+
name|model
operator|.
name|primitive2
argument_list|()
operator|)
operator|*
name|numVector
expr_stmt|;
comment|// bitset array
block|}
return|return
name|length
return|;
block|}
block|}
end_class

end_unit

