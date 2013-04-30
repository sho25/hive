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
name|util
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
name|udf
operator|.
name|generic
operator|.
name|NumDistinctValueEstimator
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
name|NumericHistogram
import|;
end_import

begin_comment
comment|/**  * Estimation of memory footprint of object  */
end_comment

begin_enum
specifier|public
enum|enum
name|JavaDataModel
block|{
name|JAVA32
block|{
specifier|public
name|int
name|object
parameter_list|()
block|{
return|return
name|JAVA32_OBJECT
return|;
block|}
specifier|public
name|int
name|array
parameter_list|()
block|{
return|return
name|JAVA32_ARRAY
return|;
block|}
specifier|public
name|int
name|ref
parameter_list|()
block|{
return|return
name|JAVA32_REF
return|;
block|}
specifier|public
name|int
name|hashMap
parameter_list|(
name|int
name|entry
parameter_list|)
block|{
comment|// base  = JAVA32_OBJECT + PRIMITIVES1 * 4 + JAVA32_FIELDREF * 3 + JAVA32_ARRAY;
comment|// entry = JAVA32_OBJECT + JAVA32_FIELDREF + PRIMITIVES1
return|return
literal|64
operator|+
literal|24
operator|*
name|entry
return|;
block|}
specifier|public
name|int
name|hashSet
parameter_list|(
name|int
name|entry
parameter_list|)
block|{
comment|// hashMap += JAVA32_OBJECT
return|return
literal|80
operator|+
literal|24
operator|*
name|entry
return|;
block|}
specifier|public
name|int
name|linkedHashMap
parameter_list|(
name|int
name|entry
parameter_list|)
block|{
comment|// hashMap += JAVA32_FIELDREF + PRIMITIVES1
comment|// hashMap.entry += JAVA32_FIELDREF * 2
return|return
literal|72
operator|+
literal|32
operator|*
name|entry
return|;
block|}
specifier|public
name|int
name|linkedList
parameter_list|(
name|int
name|entry
parameter_list|)
block|{
comment|// base  = JAVA32_OBJECT + PRIMITIVES1 * 2 + JAVA32_FIELDREF;
comment|// entry = JAVA32_OBJECT + JAVA32_FIELDREF * 2
return|return
literal|28
operator|+
literal|24
operator|*
name|entry
return|;
block|}
specifier|public
name|int
name|arrayList
parameter_list|()
block|{
comment|// JAVA32_OBJECT + PRIMITIVES1 * 2 + JAVA32_ARRAY;
return|return
literal|44
return|;
block|}
block|}
block|,
name|JAVA64
block|{
specifier|public
name|int
name|object
parameter_list|()
block|{
return|return
name|JAVA64_OBJECT
return|;
block|}
specifier|public
name|int
name|array
parameter_list|()
block|{
return|return
name|JAVA64_ARRAY
return|;
block|}
specifier|public
name|int
name|ref
parameter_list|()
block|{
return|return
name|JAVA64_REF
return|;
block|}
specifier|public
name|int
name|hashMap
parameter_list|(
name|int
name|entry
parameter_list|)
block|{
comment|// base  = JAVA64_OBJECT + PRIMITIVES1 * 4 + JAVA64_FIELDREF * 3 + JAVA64_ARRAY;
comment|// entry = JAVA64_OBJECT + JAVA64_FIELDREF + PRIMITIVES1
return|return
literal|112
operator|+
literal|44
operator|*
name|entry
return|;
block|}
specifier|public
name|int
name|hashSet
parameter_list|(
name|int
name|entry
parameter_list|)
block|{
comment|// hashMap += JAVA64_OBJECT
return|return
literal|144
operator|+
literal|44
operator|*
name|entry
return|;
block|}
specifier|public
name|int
name|linkedHashMap
parameter_list|(
name|int
name|entry
parameter_list|)
block|{
comment|// hashMap += JAVA64_FIELDREF + PRIMITIVES1
comment|// hashMap.entry += JAVA64_FIELDREF * 2
return|return
literal|128
operator|+
literal|60
operator|*
name|entry
return|;
block|}
specifier|public
name|int
name|linkedList
parameter_list|(
name|int
name|entry
parameter_list|)
block|{
comment|// base  = JAVA64_OBJECT + PRIMITIVES1 * 2 + JAVA64_FIELDREF;
comment|// entry = JAVA64_OBJECT + JAVA64_FIELDREF * 2
return|return
literal|48
operator|+
literal|48
operator|*
name|entry
return|;
block|}
specifier|public
name|int
name|arrayList
parameter_list|()
block|{
comment|// JAVA64_OBJECT + PRIMITIVES1 * 2 + JAVA64_ARRAY;
return|return
literal|80
return|;
block|}
block|}
block|;
specifier|public
specifier|abstract
name|int
name|object
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|int
name|array
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|int
name|ref
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|int
name|hashMap
parameter_list|(
name|int
name|entry
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|int
name|hashSet
parameter_list|(
name|int
name|entry
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|int
name|linkedHashMap
parameter_list|(
name|int
name|entry
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|int
name|linkedList
parameter_list|(
name|int
name|entry
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|int
name|arrayList
parameter_list|()
function_decl|;
comment|// ascii string
specifier|public
name|int
name|lengthFor
parameter_list|(
name|String
name|string
parameter_list|)
block|{
return|return
name|object
argument_list|()
operator|+
name|primitive1
argument_list|()
operator|*
literal|3
operator|+
name|array
argument_list|()
operator|+
name|string
operator|.
name|length
argument_list|()
return|;
block|}
specifier|public
name|int
name|lengthFor
parameter_list|(
name|NumericHistogram
name|histogram
parameter_list|)
block|{
name|int
name|length
init|=
name|object
argument_list|()
decl_stmt|;
name|length
operator|+=
name|primitive1
argument_list|()
operator|*
literal|2
expr_stmt|;
comment|// two int
name|int
name|numBins
init|=
name|histogram
operator|.
name|getNumBins
argument_list|()
decl_stmt|;
if|if
condition|(
name|numBins
operator|>
literal|0
condition|)
block|{
name|length
operator|+=
name|arrayList
argument_list|()
expr_stmt|;
comment|// List<Coord>
name|length
operator|+=
name|numBins
operator|*
operator|(
name|object
argument_list|()
operator|+
name|primitive2
argument_list|()
operator|*
literal|2
operator|)
expr_stmt|;
comment|// Coord holds two doubles
block|}
name|length
operator|+=
name|lengthForRandom
argument_list|()
expr_stmt|;
comment|// Random
return|return
name|length
return|;
block|}
specifier|public
name|int
name|lengthFor
parameter_list|(
name|NumDistinctValueEstimator
name|estimator
parameter_list|)
block|{
name|int
name|length
init|=
name|object
argument_list|()
decl_stmt|;
name|length
operator|+=
name|primitive1
argument_list|()
operator|*
literal|2
expr_stmt|;
comment|// two int
name|length
operator|+=
name|primitive2
argument_list|()
expr_stmt|;
comment|// one double
name|length
operator|+=
name|lengthForRandom
argument_list|()
operator|*
literal|2
expr_stmt|;
comment|// two Random
name|int
name|numVector
init|=
name|estimator
operator|.
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
name|array
argument_list|()
operator|*
literal|3
expr_stmt|;
comment|// three array
name|length
operator|+=
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
name|object
argument_list|()
operator|+
name|array
argument_list|()
operator|+
name|primitive1
argument_list|()
operator|+
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
specifier|public
name|int
name|lengthForRandom
parameter_list|()
block|{
comment|// boolean + double + AtomicLong
return|return
name|object
argument_list|()
operator|+
name|primitive1
argument_list|()
operator|+
name|primitive2
argument_list|()
operator|+
name|object
argument_list|()
operator|+
name|primitive2
argument_list|()
return|;
block|}
specifier|public
name|int
name|primitive1
parameter_list|()
block|{
return|return
name|PRIMITIVES1
return|;
block|}
specifier|public
name|int
name|primitive2
parameter_list|()
block|{
return|return
name|PRIMITIVES2
return|;
block|}
specifier|public
specifier|static
specifier|final
name|int
name|JAVA32_META
init|=
literal|12
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|JAVA32_ARRAY_META
init|=
literal|16
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|JAVA32_REF
init|=
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|JAVA32_OBJECT
init|=
literal|16
decl_stmt|;
comment|// JAVA32_META + JAVA32_REF
specifier|public
specifier|static
specifier|final
name|int
name|JAVA32_ARRAY
init|=
literal|20
decl_stmt|;
comment|// JAVA32_ARRAY_META + JAVA32_REF
specifier|public
specifier|static
specifier|final
name|int
name|JAVA64_META
init|=
literal|24
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|JAVA64_ARRAY_META
init|=
literal|32
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|JAVA64_REF
init|=
literal|8
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|JAVA64_OBJECT
init|=
literal|32
decl_stmt|;
comment|// JAVA64_META + JAVA64_REF
specifier|public
specifier|static
specifier|final
name|int
name|JAVA64_ARRAY
init|=
literal|40
decl_stmt|;
comment|// JAVA64_ARRAY_META + JAVA64_REF
specifier|public
specifier|static
specifier|final
name|int
name|PRIMITIVES1
init|=
literal|4
decl_stmt|;
comment|// void, boolean, byte, short, int, float
specifier|public
specifier|static
specifier|final
name|int
name|PRIMITIVES2
init|=
literal|8
decl_stmt|;
comment|// long, double
specifier|private
specifier|static
name|JavaDataModel
name|current
decl_stmt|;
specifier|public
specifier|static
name|JavaDataModel
name|get
parameter_list|()
block|{
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
return|return
name|current
return|;
block|}
try|try
block|{
name|String
name|props
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"sun.arch.data.model"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"32"
operator|.
name|equals
argument_list|(
name|props
argument_list|)
condition|)
block|{
return|return
name|current
operator|=
name|JAVA32
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
return|return
name|current
operator|=
name|JAVA64
return|;
block|}
specifier|public
specifier|static
name|int
name|round
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|JavaDataModel
name|model
init|=
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|model
operator|==
name|JAVA32
operator|||
name|size
operator|%
literal|8
operator|==
literal|0
condition|)
block|{
return|return
name|size
return|;
block|}
return|return
operator|(
operator|(
name|size
operator|+
literal|8
operator|)
operator|>>
literal|3
operator|)
operator|<<
literal|3
return|;
block|}
block|}
end_enum

end_unit

