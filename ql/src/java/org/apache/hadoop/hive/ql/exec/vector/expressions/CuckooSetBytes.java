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
name|exec
operator|.
name|vector
operator|.
name|expressions
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

begin_comment
comment|/**  * A high-performance set implementation used to support fast set membership testing,  * using Cuckoo hashing. This is used to support fast tests of the form  *  *       column IN ( list-of-values )  *  * For details on the algorithm, see R. Pagh and F. F. Rodler, "Cuckoo Hashing,"  * Elsevier Science preprint, Dec. 2003. http://www.itu.dk/people/pagh/papers/cuckoo-jour.pdf.  */
end_comment

begin_class
specifier|public
class|class
name|CuckooSetBytes
block|{
specifier|private
name|byte
name|t1
index|[]
index|[]
decl_stmt|;
specifier|private
name|byte
name|t2
index|[]
index|[]
decl_stmt|;
specifier|private
name|byte
name|prev1
index|[]
index|[]
init|=
literal|null
decl_stmt|;
comment|// used for rehashing to get last set of values
specifier|private
name|byte
name|prev2
index|[]
index|[]
init|=
literal|null
decl_stmt|;
comment|// " "
specifier|private
name|int
name|n
decl_stmt|;
comment|// current array size
specifier|private
specifier|static
specifier|final
name|double
name|PADDING_FACTOR
init|=
literal|1.0
operator|/
literal|0.40
decl_stmt|;
comment|// have minimum 40% fill factor
specifier|private
name|int
name|salt
init|=
literal|0
decl_stmt|;
specifier|private
name|Random
name|gen
init|=
operator|new
name|Random
argument_list|(
literal|676983475
argument_list|)
decl_stmt|;
specifier|private
name|int
name|rehashCount
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|INT_MASK
init|=
literal|0x00000000ffffffffL
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|BYTE_MASK
init|=
literal|0x00000000000000ffL
decl_stmt|;
comment|/**    * Allocate a new set to hold expectedSize values. Re-allocation to expand    * the set is not implemented, so the expected size must be at least the    * size of the set to be inserted.    * @param expectedSize At least the size of the set of values that will be inserted.    */
specifier|public
name|CuckooSetBytes
parameter_list|(
name|int
name|expectedSize
parameter_list|)
block|{
comment|// Choose array size. We have two hash tables to hold entries, so the sum
comment|// of the two should have a bit more than twice as much space as the
comment|// minimum required.
name|n
operator|=
call|(
name|int
call|)
argument_list|(
name|expectedSize
operator|*
name|PADDING_FACTOR
operator|/
literal|2.0
argument_list|)
expr_stmt|;
comment|// some prime numbers spaced about at powers of 2 in magnitude
comment|// try to get prime number table size to have less dependence on good hash function
name|int
name|primes
index|[]
init|=
name|CuckooSetLong
operator|.
name|primes
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|primes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|n
operator|<=
name|primes
index|[
name|i
index|]
condition|)
block|{
name|n
operator|=
name|primes
index|[
name|i
index|]
expr_stmt|;
break|break;
block|}
block|}
name|t1
operator|=
operator|new
name|byte
index|[
name|n
index|]
index|[]
expr_stmt|;
name|t2
operator|=
operator|new
name|byte
index|[
name|n
index|]
index|[]
expr_stmt|;
name|updateHashSalt
argument_list|()
expr_stmt|;
block|}
comment|/**    * Return true if and only if the value in byte array b beginning at start    * and ending at start+len is present in the set.    */
specifier|public
name|boolean
name|lookup
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
return|return
name|entryEqual
argument_list|(
name|t1
argument_list|,
name|h1
argument_list|(
name|b
argument_list|,
name|start
argument_list|,
name|len
argument_list|)
argument_list|,
name|b
argument_list|,
name|start
argument_list|,
name|len
argument_list|)
operator|||
name|entryEqual
argument_list|(
name|t2
argument_list|,
name|h2
argument_list|(
name|b
argument_list|,
name|start
argument_list|,
name|len
argument_list|)
argument_list|,
name|b
argument_list|,
name|start
argument_list|,
name|len
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|entryEqual
parameter_list|(
name|byte
index|[]
index|[]
name|t
parameter_list|,
name|int
name|hash
parameter_list|,
name|byte
index|[]
name|b
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
return|return
name|t
index|[
name|hash
index|]
operator|!=
literal|null
operator|&&
name|StringExpr
operator|.
name|equal
argument_list|(
name|t
index|[
name|hash
index|]
argument_list|,
literal|0
argument_list|,
name|t
index|[
name|hash
index|]
operator|.
name|length
argument_list|,
name|b
argument_list|,
name|start
argument_list|,
name|len
argument_list|)
return|;
block|}
specifier|public
name|void
name|insert
parameter_list|(
name|byte
index|[]
name|x
parameter_list|)
block|{
name|byte
index|[]
name|temp
decl_stmt|;
if|if
condition|(
name|lookup
argument_list|(
name|x
argument_list|,
literal|0
argument_list|,
name|x
operator|.
name|length
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// Try to insert up to n times. Rehash if that fails.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
name|int
name|hash1
init|=
name|h1
argument_list|(
name|x
argument_list|,
literal|0
argument_list|,
name|x
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|t1
index|[
name|hash1
index|]
operator|==
literal|null
condition|)
block|{
name|t1
index|[
name|hash1
index|]
operator|=
name|x
expr_stmt|;
return|return;
block|}
comment|// swap x and t1[h1(x)]
name|temp
operator|=
name|t1
index|[
name|hash1
index|]
expr_stmt|;
name|t1
index|[
name|hash1
index|]
operator|=
name|x
expr_stmt|;
name|x
operator|=
name|temp
expr_stmt|;
name|int
name|hash2
init|=
name|h2
argument_list|(
name|x
argument_list|,
literal|0
argument_list|,
name|x
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|t2
index|[
name|hash2
index|]
operator|==
literal|null
condition|)
block|{
name|t2
index|[
name|hash2
index|]
operator|=
name|x
expr_stmt|;
return|return;
block|}
comment|// swap x and t2[h2(x)]
name|temp
operator|=
name|t2
index|[
name|hash2
index|]
expr_stmt|;
name|t2
index|[
name|hash2
index|]
operator|=
name|x
expr_stmt|;
name|x
operator|=
name|temp
expr_stmt|;
block|}
name|rehash
argument_list|()
expr_stmt|;
name|insert
argument_list|(
name|x
argument_list|)
expr_stmt|;
block|}
comment|/**    * Insert all values in the input array into the set.    */
specifier|public
name|void
name|load
parameter_list|(
name|byte
index|[]
index|[]
name|a
parameter_list|)
block|{
for|for
control|(
name|byte
index|[]
name|x
range|:
name|a
control|)
block|{
name|insert
argument_list|(
name|x
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Try to insert with up to n value's "poked out". Return the last value poked out.    * If the value is not blank then we assume there was a cycle.    * Don't try to insert the same value twice. This is for use in rehash only,    * so you won't see the same value twice.    */
specifier|private
name|byte
index|[]
name|tryInsert
parameter_list|(
name|byte
index|[]
name|x
parameter_list|)
block|{
name|byte
index|[]
name|temp
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
name|int
name|hash1
init|=
name|h1
argument_list|(
name|x
argument_list|,
literal|0
argument_list|,
name|x
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|t1
index|[
name|hash1
index|]
operator|==
literal|null
condition|)
block|{
name|t1
index|[
name|hash1
index|]
operator|=
name|x
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// swap x and t1[h1(x)]
name|temp
operator|=
name|t1
index|[
name|hash1
index|]
expr_stmt|;
name|t1
index|[
name|hash1
index|]
operator|=
name|x
expr_stmt|;
name|x
operator|=
name|temp
expr_stmt|;
name|int
name|hash2
init|=
name|h2
argument_list|(
name|x
argument_list|,
literal|0
argument_list|,
name|x
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|t2
index|[
name|hash2
index|]
operator|==
literal|null
condition|)
block|{
name|t2
index|[
name|hash2
index|]
operator|=
name|x
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// swap x and t2[h2(x)]
name|temp
operator|=
name|t2
index|[
name|hash2
index|]
expr_stmt|;
name|t2
index|[
name|hash2
index|]
operator|=
name|x
expr_stmt|;
name|x
operator|=
name|temp
expr_stmt|;
if|if
condition|(
name|x
operator|==
literal|null
condition|)
block|{
break|break;
block|}
block|}
return|return
name|x
return|;
block|}
comment|/**    * first hash function    */
specifier|private
name|int
name|h1
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
comment|// AND hash with mask to 0 out sign bit to make sure it's positive.
comment|// Then we know taking the result mod n is in the range (0..n-1).
return|return
operator|(
name|hash
argument_list|(
name|b
argument_list|,
name|start
argument_list|,
name|len
argument_list|,
literal|0
argument_list|)
operator|&
literal|0x7FFFFFFF
operator|)
operator|%
name|n
return|;
block|}
comment|/**    * second hash function    */
specifier|private
name|int
name|h2
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
comment|// AND hash with mask to 0 out sign bit to make sure it's positive.
comment|// Then we know taking the result mod n is in the range (0..n-1).
comment|// Include salt as argument so this hash function can be varied
comment|// if we need to rehash.
return|return
operator|(
name|hash
argument_list|(
name|b
argument_list|,
name|start
argument_list|,
name|len
argument_list|,
name|salt
argument_list|)
operator|&
literal|0x7FFFFFFF
operator|)
operator|%
name|n
return|;
block|}
comment|/**    * In case of rehash, hash function h2 is changed by updating the    * salt value passed in to the function hash().    */
specifier|private
name|void
name|updateHashSalt
parameter_list|()
block|{
name|salt
operator|=
name|gen
operator|.
name|nextInt
argument_list|(
literal|0x7FFFFFFF
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|rehash
parameter_list|()
block|{
name|rehashCount
operator|++
expr_stmt|;
if|if
condition|(
name|rehashCount
operator|>
literal|20
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Too many rehashes"
argument_list|)
throw|;
block|}
name|updateHashSalt
argument_list|()
expr_stmt|;
comment|// Save original values
if|if
condition|(
name|prev1
operator|==
literal|null
condition|)
block|{
name|prev1
operator|=
name|t1
expr_stmt|;
name|prev2
operator|=
name|t2
expr_stmt|;
block|}
name|t1
operator|=
operator|new
name|byte
index|[
name|n
index|]
index|[]
expr_stmt|;
name|t2
operator|=
operator|new
name|byte
index|[
name|n
index|]
index|[]
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|v
range|:
name|prev1
control|)
block|{
if|if
condition|(
name|v
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|x
init|=
name|tryInsert
argument_list|(
name|v
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|!=
literal|null
condition|)
block|{
name|rehash
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
block|}
for|for
control|(
name|byte
index|[]
name|v
range|:
name|prev2
control|)
block|{
if|if
condition|(
name|v
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|x
init|=
name|tryInsert
argument_list|(
name|v
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|!=
literal|null
condition|)
block|{
name|rehash
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
block|}
comment|// We succeeded in adding all the values, so
comment|// clear the previous values recorded.
name|prev1
operator|=
literal|null
expr_stmt|;
name|prev2
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * This is adapted from the org.apache.hadoop.util.hash.JenkinsHash package.    * The interface needed to be modified to suit the use here, by adding    * a start offset parameter to the hash function.    *    * In the future, folding this back into the original Hadoop package should    * be considered. This could could them import that package and use it.    * The original comments from the source are below.    *    * taken from  hashlittle() -- hash a variable-length key into a 32-bit value    *    * @param key the key (the unaligned variable-length array of bytes)    * @param nbytes number of bytes to include in hash    * @param initval can be any integer value    * @return a 32-bit value.  Every bit of the key affects every bit of the    * return value.  Two keys differing by one or two bits will have totally    * different hash values.    *    *<p>The best hash table sizes are powers of 2.  There is no need to do mod    * a prime (mod is sooo slow!).  If you need less than 32 bits, use a bitmask.    * For example, if you need only 10 bits, do    *<code>h = (h& hashmask(10));</code>    * In which case, the hash table should have hashsize(10) elements.    *    *<p>If you are hashing n strings byte[][] k, do it like this:    * for (int i = 0, h = 0; i< n; ++i) h = hash( k[i], h);    *    *<p>By Bob Jenkins, 2006.  bob_jenkins@burtleburtle.net.  You may use this    * code any way you wish, private, educational, or commercial.  It's free.    *    *<p>Use for hash table lookup, or anything where one collision in 2^^32 is    * acceptable.  Do NOT use for cryptographic purposes.   */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"fallthrough"
argument_list|)
specifier|private
name|int
name|hash
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|nbytes
parameter_list|,
name|int
name|initval
parameter_list|)
block|{
name|int
name|length
init|=
name|nbytes
decl_stmt|;
name|long
name|a
decl_stmt|,
name|b
decl_stmt|,
name|c
decl_stmt|;
comment|// We use longs because we don't have unsigned ints
name|a
operator|=
name|b
operator|=
name|c
operator|=
operator|(
literal|0x00000000deadbeefL
operator|+
name|length
operator|+
name|initval
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|int
name|offset
init|=
name|start
decl_stmt|;
for|for
control|(
init|;
name|length
operator|>
literal|12
condition|;
name|offset
operator|+=
literal|12
operator|,
name|length
operator|-=
literal|12
control|)
block|{
name|a
operator|=
operator|(
name|a
operator|+
operator|(
name|key
index|[
name|offset
operator|+
literal|0
index|]
operator|&
name|BYTE_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|a
operator|=
operator|(
name|a
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|1
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|8
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|a
operator|=
operator|(
name|a
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|2
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|16
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|a
operator|=
operator|(
name|a
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|3
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|24
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|+
operator|(
name|key
index|[
name|offset
operator|+
literal|4
index|]
operator|&
name|BYTE_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|5
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|8
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|6
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|16
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|7
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|24
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|+
operator|(
name|key
index|[
name|offset
operator|+
literal|8
index|]
operator|&
name|BYTE_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|9
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|8
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|10
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|16
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|11
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|24
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
comment|/*        * mix -- mix 3 32-bit values reversibly.        * This is reversible, so any information in (a,b,c) before mix() is        * still in (a,b,c) after mix().        *        * If four pairs of (a,b,c) inputs are run through mix(), or through        * mix() in reverse, there are at least 32 bits of the output that        * are sometimes the same for one pair and different for another pair.        *        * This was tested for:        * - pairs that differed by one bit, by two bits, in any combination        *   of top bits of (a,b,c), or in any combination of bottom bits of        *   (a,b,c).        * - "differ" is defined as +, -, ^, or ~^.  For + and -, I transformed        *   the output delta to a Gray code (a^(a>>1)) so a string of 1's (as        *    is commonly produced by subtraction) look like a single 1-bit        *    difference.        * - the base values were pseudorandom, all zero but one bit set, or        *   all zero plus a counter that starts at zero.        *        * Some k values for my "a-=c; a^=rot(c,k); c+=b;" arrangement that        * satisfy this are        *     4  6  8 16 19  4        *     9 15  3 18 27 15        *    14  9  3  7 17  3        * Well, "9 15 3 18 27 15" didn't quite get 32 bits diffing for        * "differ" defined as + with a one-bit base and a two-bit delta.  I        * used http://burtleburtle.net/bob/hash/avalanche.html to choose        * the operations, constants, and arrangements of the variables.        *        * This does not achieve avalanche.  There are input bits of (a,b,c)        * that fail to affect some output bits of (a,b,c), especially of a.        * The most thoroughly mixed value is c, but it doesn't really even        * achieve avalanche in c.        *        * This allows some parallelism.  Read-after-writes are good at doubling        * the number of bits affected, so the goal of mixing pulls in the        * opposite direction as the goal of parallelism.  I did what I could.        * Rotates seem to cost as much as shifts on every machine I could lay        * my hands on, and rotates are much kinder to the top and bottom bits,        * so I used rotates.        *        * #define mix(a,b,c) \        * { \        *   a -= c;  a ^= rot(c, 4);  c += b; \        *   b -= a;  b ^= rot(a, 6);  a += c; \        *   c -= b;  c ^= rot(b, 8);  b += a; \        *   a -= c;  a ^= rot(c,16);  c += b; \        *   b -= a;  b ^= rot(a,19);  a += c; \        *   c -= b;  c ^= rot(b, 4);  b += a; \        * }        *        * mix(a,b,c);        */
name|a
operator|=
operator|(
name|a
operator|-
name|c
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|a
operator|^=
name|rot
argument_list|(
name|c
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|+
name|b
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|-
name|a
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|^=
name|rot
argument_list|(
name|a
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|a
operator|=
operator|(
name|a
operator|+
name|c
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|-
name|b
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|^=
name|rot
argument_list|(
name|b
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|+
name|a
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|a
operator|=
operator|(
name|a
operator|-
name|c
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|a
operator|^=
name|rot
argument_list|(
name|c
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|+
name|b
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|-
name|a
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|^=
name|rot
argument_list|(
name|a
argument_list|,
literal|19
argument_list|)
expr_stmt|;
name|a
operator|=
operator|(
name|a
operator|+
name|c
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|-
name|b
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|^=
name|rot
argument_list|(
name|b
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|+
name|a
operator|)
operator|&
name|INT_MASK
expr_stmt|;
block|}
comment|//-------------------------------- last block: affect all 32 bits of (c)
switch|switch
condition|(
name|length
condition|)
block|{
comment|// all the case statements fall through
case|case
literal|12
case|:
name|c
operator|=
operator|(
name|c
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|11
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|24
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|11
case|:
name|c
operator|=
operator|(
name|c
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|10
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|16
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|10
case|:
name|c
operator|=
operator|(
name|c
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|9
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|8
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|9
case|:
name|c
operator|=
operator|(
name|c
operator|+
operator|(
name|key
index|[
name|offset
operator|+
literal|8
index|]
operator|&
name|BYTE_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|8
case|:
name|b
operator|=
operator|(
name|b
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|7
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|24
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|7
case|:
name|b
operator|=
operator|(
name|b
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|6
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|16
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|6
case|:
name|b
operator|=
operator|(
name|b
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|5
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|8
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|5
case|:
name|b
operator|=
operator|(
name|b
operator|+
operator|(
name|key
index|[
name|offset
operator|+
literal|4
index|]
operator|&
name|BYTE_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|4
case|:
name|a
operator|=
operator|(
name|a
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|3
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|24
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|3
case|:
name|a
operator|=
operator|(
name|a
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|2
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|16
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|2
case|:
name|a
operator|=
operator|(
name|a
operator|+
operator|(
operator|(
operator|(
name|key
index|[
name|offset
operator|+
literal|1
index|]
operator|&
name|BYTE_MASK
operator|)
operator|<<
literal|8
operator|)
operator|&
name|INT_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
case|case
literal|1
case|:
name|a
operator|=
operator|(
name|a
operator|+
operator|(
name|key
index|[
name|offset
operator|+
literal|0
index|]
operator|&
name|BYTE_MASK
operator|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
break|break;
case|case
literal|0
case|:
return|return
call|(
name|int
call|)
argument_list|(
name|c
operator|&
name|INT_MASK
argument_list|)
return|;
block|}
comment|/*      * final -- final mixing of 3 32-bit values (a,b,c) into c      *      * Pairs of (a,b,c) values differing in only a few bits will usually      * produce values of c that look totally different.  This was tested for      * - pairs that differed by one bit, by two bits, in any combination      *   of top bits of (a,b,c), or in any combination of bottom bits of      *   (a,b,c).      *      * - "differ" is defined as +, -, ^, or ~^.  For + and -, I transformed      *   the output delta to a Gray code (a^(a>>1)) so a string of 1's (as      *   is commonly produced by subtraction) look like a single 1-bit      *   difference.      *      * - the base values were pseudorandom, all zero but one bit set, or      *   all zero plus a counter that starts at zero.      *      * These constants passed:      *   14 11 25 16 4 14 24      *   12 14 25 16 4 14 24      * and these came close:      *    4  8 15 26 3 22 24      *   10  8 15 26 3 22 24      *   11  8 15 26 3 22 24      *      * #define final(a,b,c) \      * {      *   c ^= b; c -= rot(b,14); \      *   a ^= c; a -= rot(c,11); \      *   b ^= a; b -= rot(a,25); \      *   c ^= b; c -= rot(b,16); \      *   a ^= c; a -= rot(c,4);  \      *   b ^= a; b -= rot(a,14); \      *   c ^= b; c -= rot(b,24); \      * }      *      */
name|c
operator|^=
name|b
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|-
name|rot
argument_list|(
name|b
argument_list|,
literal|14
argument_list|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|a
operator|^=
name|c
expr_stmt|;
name|a
operator|=
operator|(
name|a
operator|-
name|rot
argument_list|(
name|c
argument_list|,
literal|11
argument_list|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|^=
name|a
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|-
name|rot
argument_list|(
name|a
argument_list|,
literal|25
argument_list|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|^=
name|b
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|-
name|rot
argument_list|(
name|b
argument_list|,
literal|16
argument_list|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|a
operator|^=
name|c
expr_stmt|;
name|a
operator|=
operator|(
name|a
operator|-
name|rot
argument_list|(
name|c
argument_list|,
literal|4
argument_list|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|b
operator|^=
name|a
expr_stmt|;
name|b
operator|=
operator|(
name|b
operator|-
name|rot
argument_list|(
name|a
argument_list|,
literal|14
argument_list|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
name|c
operator|^=
name|b
expr_stmt|;
name|c
operator|=
operator|(
name|c
operator|-
name|rot
argument_list|(
name|b
argument_list|,
literal|24
argument_list|)
operator|)
operator|&
name|INT_MASK
expr_stmt|;
return|return
call|(
name|int
call|)
argument_list|(
name|c
operator|&
name|INT_MASK
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|long
name|rot
parameter_list|(
name|long
name|val
parameter_list|,
name|int
name|pos
parameter_list|)
block|{
return|return
operator|(
operator|(
name|Integer
operator|.
name|rotateLeft
argument_list|(
call|(
name|int
call|)
argument_list|(
name|val
operator|&
name|INT_MASK
argument_list|)
argument_list|,
name|pos
argument_list|)
operator|)
operator|&
name|INT_MASK
operator|)
return|;
block|}
block|}
end_class

end_unit

