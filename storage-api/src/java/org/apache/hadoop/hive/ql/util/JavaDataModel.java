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
name|util
package|;
end_package

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
annotation|@
name|Override
specifier|public
name|int
name|object
parameter_list|()
block|{
return|return
name|JAVA32_OBJECT
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|array
parameter_list|()
block|{
return|return
name|JAVA32_ARRAY
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|ref
parameter_list|()
block|{
return|return
name|JAVA32_REF
return|;
block|}
annotation|@
name|Override
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
name|hashMapBase
argument_list|()
operator|+
name|hashMapEntry
argument_list|()
operator|*
name|entry
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashMapBase
parameter_list|()
block|{
return|return
literal|64
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashMapEntry
parameter_list|()
block|{
return|return
literal|24
return|;
block|}
annotation|@
name|Override
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
name|hashSetBase
argument_list|()
operator|+
name|hashSetEntry
argument_list|()
operator|*
name|entry
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashSetBase
parameter_list|()
block|{
return|return
literal|80
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashSetEntry
parameter_list|()
block|{
return|return
literal|24
return|;
block|}
annotation|@
name|Override
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
annotation|@
name|Override
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
name|linkedListBase
argument_list|()
operator|+
name|linkedListEntry
argument_list|()
operator|*
name|entry
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|linkedListBase
parameter_list|()
block|{
return|return
literal|28
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|linkedListEntry
parameter_list|()
block|{
return|return
literal|24
return|;
block|}
annotation|@
name|Override
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
annotation|@
name|Override
specifier|public
name|int
name|memoryAlign
parameter_list|()
block|{
return|return
literal|8
return|;
block|}
block|}
block|,
name|JAVA64
block|{
annotation|@
name|Override
specifier|public
name|int
name|object
parameter_list|()
block|{
return|return
name|JAVA64_OBJECT
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|array
parameter_list|()
block|{
return|return
name|JAVA64_ARRAY
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|ref
parameter_list|()
block|{
return|return
name|JAVA64_REF
return|;
block|}
annotation|@
name|Override
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
name|hashMapBase
argument_list|()
operator|+
name|hashMapEntry
argument_list|()
operator|*
name|entry
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashMapBase
parameter_list|()
block|{
return|return
literal|112
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashMapEntry
parameter_list|()
block|{
return|return
literal|44
return|;
block|}
annotation|@
name|Override
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
name|hashSetBase
argument_list|()
operator|+
name|hashSetEntry
argument_list|()
operator|*
name|entry
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashSetBase
parameter_list|()
block|{
return|return
literal|144
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashSetEntry
parameter_list|()
block|{
return|return
literal|44
return|;
block|}
annotation|@
name|Override
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
annotation|@
name|Override
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
name|linkedListBase
argument_list|()
operator|+
name|linkedListEntry
argument_list|()
operator|*
name|entry
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|linkedListBase
parameter_list|()
block|{
return|return
literal|48
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|linkedListEntry
parameter_list|()
block|{
return|return
literal|48
return|;
block|}
annotation|@
name|Override
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
annotation|@
name|Override
specifier|public
name|int
name|memoryAlign
parameter_list|()
block|{
return|return
literal|8
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
name|hashMapBase
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|int
name|hashMapEntry
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|int
name|hashSetBase
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|int
name|hashSetEntry
parameter_list|()
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
name|linkedListBase
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|int
name|linkedListEntry
parameter_list|()
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
specifier|public
specifier|abstract
name|int
name|memoryAlign
parameter_list|()
function_decl|;
comment|// ascii string
specifier|public
name|long
name|lengthFor
parameter_list|(
name|String
name|string
parameter_list|)
block|{
return|return
name|lengthForStringOfLength
argument_list|(
name|string
operator|.
name|length
argument_list|()
argument_list|)
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
name|long
name|alignUp
parameter_list|(
name|long
name|value
parameter_list|,
name|long
name|align
parameter_list|)
block|{
return|return
operator|(
name|value
operator|+
name|align
operator|-
literal|1L
operator|)
operator|&
operator|~
operator|(
name|align
operator|-
literal|1L
operator|)
return|;
block|}
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JavaDataModel
operator|.
name|class
argument_list|)
decl_stmt|;
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
specifier|public
specifier|static
specifier|final
name|int
name|PRIMITIVE_BYTE
init|=
literal|1
decl_stmt|;
comment|// byte
specifier|private
specifier|static
specifier|final
class|class
name|LazyHolder
block|{
specifier|private
specifier|static
specifier|final
name|JavaDataModel
name|MODEL_FOR_SYSTEM
init|=
name|getModelForSystem
argument_list|()
decl_stmt|;
block|}
comment|//@VisibleForTesting
specifier|static
name|JavaDataModel
name|getModelForSystem
parameter_list|()
block|{
name|String
name|props
init|=
literal|null
decl_stmt|;
try|try
block|{
name|props
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"sun.arch.data.model"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to determine java data model, defaulting to 64"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
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
name|JAVA32
return|;
block|}
comment|// TODO: separate model is needed for compressedOops, which can be guessed from memory size.
return|return
name|JAVA64
return|;
block|}
specifier|public
specifier|static
name|JavaDataModel
name|get
parameter_list|()
block|{
return|return
name|LazyHolder
operator|.
name|MODEL_FOR_SYSTEM
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
specifier|public
name|long
name|lengthForPrimitiveArrayOfSize
parameter_list|(
name|int
name|primitiveSize
parameter_list|,
name|long
name|length
parameter_list|)
block|{
return|return
name|alignUp
argument_list|(
name|array
argument_list|()
operator|+
name|primitiveSize
operator|*
name|length
argument_list|,
name|memoryAlign
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|long
name|lengthForByteArrayOfSize
parameter_list|(
name|long
name|length
parameter_list|)
block|{
return|return
name|lengthForPrimitiveArrayOfSize
argument_list|(
name|PRIMITIVE_BYTE
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|long
name|lengthForObjectArrayOfSize
parameter_list|(
name|long
name|length
parameter_list|)
block|{
return|return
name|lengthForPrimitiveArrayOfSize
argument_list|(
name|ref
argument_list|()
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|long
name|lengthForLongArrayOfSize
parameter_list|(
name|long
name|length
parameter_list|)
block|{
return|return
name|lengthForPrimitiveArrayOfSize
argument_list|(
name|primitive2
argument_list|()
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|long
name|lengthForDoubleArrayOfSize
parameter_list|(
name|long
name|length
parameter_list|)
block|{
return|return
name|lengthForPrimitiveArrayOfSize
argument_list|(
name|primitive2
argument_list|()
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|long
name|lengthForIntArrayOfSize
parameter_list|(
name|long
name|length
parameter_list|)
block|{
return|return
name|lengthForPrimitiveArrayOfSize
argument_list|(
name|primitive1
argument_list|()
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|long
name|lengthForBooleanArrayOfSize
parameter_list|(
name|long
name|length
parameter_list|)
block|{
return|return
name|lengthForPrimitiveArrayOfSize
argument_list|(
name|PRIMITIVE_BYTE
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|long
name|lengthForTimestampArrayOfSize
parameter_list|(
name|long
name|length
parameter_list|)
block|{
return|return
name|lengthForPrimitiveArrayOfSize
argument_list|(
name|lengthOfTimestamp
argument_list|()
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|long
name|lengthForDateArrayOfSize
parameter_list|(
name|long
name|length
parameter_list|)
block|{
return|return
name|lengthForPrimitiveArrayOfSize
argument_list|(
name|lengthOfDate
argument_list|()
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|long
name|lengthForDecimalArrayOfSize
parameter_list|(
name|long
name|length
parameter_list|)
block|{
return|return
name|lengthForPrimitiveArrayOfSize
argument_list|(
name|lengthOfDecimal
argument_list|()
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|int
name|lengthOfDecimal
parameter_list|()
block|{
comment|// object overhead + 8 bytes for intCompact + 4 bytes for precision
comment|// + 4 bytes for scale + size of BigInteger
return|return
name|object
argument_list|()
operator|+
literal|2
operator|*
name|primitive2
argument_list|()
operator|+
name|lengthOfBigInteger
argument_list|()
return|;
block|}
specifier|private
name|int
name|lengthOfBigInteger
parameter_list|()
block|{
comment|// object overhead + 4 bytes for bitCount + 4 bytes for bitLength
comment|// + 4 bytes for firstNonzeroByteNum + 4 bytes for firstNonzeroIntNum +
comment|// + 4 bytes for lowestSetBit + 5 bytes for size of magnitude (since max precision
comment|// is only 38 for HiveDecimal) + 7 bytes of padding (since java memory allocations
comment|// are 8 byte aligned)
return|return
name|object
argument_list|()
operator|+
literal|4
operator|*
name|primitive2
argument_list|()
return|;
block|}
specifier|public
name|int
name|lengthOfTimestamp
parameter_list|()
block|{
comment|// object overhead + 4 bytes for int (nanos) + 4 bytes of padding
return|return
name|object
argument_list|()
operator|+
name|primitive2
argument_list|()
return|;
block|}
specifier|public
name|int
name|lengthOfDate
parameter_list|()
block|{
comment|// object overhead + 8 bytes for long (fastTime) + 16 bytes for cdate
return|return
name|object
argument_list|()
operator|+
literal|3
operator|*
name|primitive2
argument_list|()
return|;
block|}
specifier|public
name|int
name|lengthForStringOfLength
parameter_list|(
name|int
name|strLen
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
name|strLen
return|;
block|}
block|}
end_enum

end_unit

