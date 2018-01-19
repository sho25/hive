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
name|common
operator|.
name|type
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|code
operator|.
name|tempusfugit
operator|.
name|concurrency
operator|.
name|ConcurrentRule
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|code
operator|.
name|tempusfugit
operator|.
name|concurrency
operator|.
name|RepeatingRule
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|code
operator|.
name|tempusfugit
operator|.
name|concurrency
operator|.
name|annotations
operator|.
name|Concurrent
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|code
operator|.
name|tempusfugit
operator|.
name|concurrency
operator|.
name|annotations
operator|.
name|Repeating
import|;
end_import

begin_class
specifier|public
class|class
name|TestHiveVarchar
block|{
annotation|@
name|Rule
specifier|public
name|ConcurrentRule
name|concurrentRule
init|=
operator|new
name|ConcurrentRule
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|RepeatingRule
name|repeatingRule
init|=
operator|new
name|RepeatingRule
argument_list|()
decl_stmt|;
specifier|public
name|TestHiveVarchar
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|static
name|Random
name|rnd
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|int
name|getRandomSupplementaryChar
parameter_list|()
block|{
name|int
name|lowSurrogate
init|=
literal|0xDC00
operator|+
name|rnd
operator|.
name|nextInt
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
comment|//return 0xD8000000 + lowSurrogate;
name|int
name|highSurrogate
init|=
literal|0xD800
decl_stmt|;
return|return
name|Character
operator|.
name|toCodePoint
argument_list|(
operator|(
name|char
operator|)
name|highSurrogate
argument_list|,
operator|(
name|char
operator|)
name|lowSurrogate
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|getRandomCodePoint
parameter_list|()
block|{
name|int
name|codePoint
decl_stmt|;
if|if
condition|(
name|rnd
operator|.
name|nextDouble
argument_list|()
operator|<
literal|0.50
condition|)
block|{
name|codePoint
operator|=
literal|32
operator|+
name|rnd
operator|.
name|nextInt
argument_list|(
literal|90
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|codePoint
operator|=
name|getRandomSupplementaryChar
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|Character
operator|.
name|isValidCodePoint
argument_list|(
name|codePoint
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Integer
operator|.
name|toHexString
argument_list|(
name|codePoint
argument_list|)
operator|+
literal|" is not a valid code point"
argument_list|)
expr_stmt|;
block|}
return|return
name|codePoint
return|;
block|}
specifier|public
specifier|static
name|int
name|getRandomCodePoint
parameter_list|(
name|int
name|excludeChar
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|codePoint
init|=
name|getRandomCodePoint
argument_list|()
decl_stmt|;
if|if
condition|(
name|codePoint
operator|!=
name|excludeChar
condition|)
block|{
return|return
name|codePoint
return|;
block|}
block|}
block|}
annotation|@
name|Test
annotation|@
name|Concurrent
argument_list|(
name|count
operator|=
literal|4
argument_list|)
annotation|@
name|Repeating
argument_list|(
name|repetition
operator|=
literal|100
argument_list|)
specifier|public
name|void
name|testStringLength
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|strLen
init|=
literal|20
decl_stmt|;
name|int
index|[]
name|lengths
init|=
block|{
literal|15
block|,
literal|20
block|,
literal|25
block|}
decl_stmt|;
comment|// Try with supplementary characters
for|for
control|(
name|int
name|idx1
init|=
literal|0
init|;
name|idx1
operator|<
name|lengths
operator|.
name|length
condition|;
operator|++
name|idx1
control|)
block|{
comment|// Create random test string
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|curLen
init|=
name|lengths
index|[
name|idx1
index|]
decl_stmt|;
for|for
control|(
name|int
name|idx2
init|=
literal|0
init|;
name|idx2
operator|<
name|curLen
condition|;
operator|++
name|idx2
control|)
block|{
name|sb
operator|.
name|appendCodePoint
argument_list|(
name|getRandomCodePoint
argument_list|(
literal|' '
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|testString
init|=
name|sb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|curLen
argument_list|,
name|testString
operator|.
name|codePointCount
argument_list|(
literal|0
argument_list|,
name|testString
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|enforcedString
init|=
name|HiveBaseChar
operator|.
name|enforceMaxLength
argument_list|(
name|testString
argument_list|,
name|strLen
argument_list|)
decl_stmt|;
if|if
condition|(
name|curLen
operator|<=
name|strLen
condition|)
block|{
comment|// No truncation needed
name|assertEquals
argument_list|(
name|testString
argument_list|,
name|enforcedString
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// String should have been truncated.
name|assertEquals
argument_list|(
name|strLen
argument_list|,
name|enforcedString
operator|.
name|codePointCount
argument_list|(
literal|0
argument_list|,
name|enforcedString
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Try with ascii chars
name|String
index|[]
name|testStrings
init|=
block|{
literal|"abcdefg"
block|,
literal|"abcdefghijklmnopqrst"
block|,
literal|"abcdefghijklmnopqrstuvwxyz"
block|}
decl_stmt|;
for|for
control|(
name|String
name|testString
range|:
name|testStrings
control|)
block|{
name|int
name|curLen
init|=
name|testString
operator|.
name|length
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|curLen
argument_list|,
name|testString
operator|.
name|codePointCount
argument_list|(
literal|0
argument_list|,
name|testString
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|enforcedString
init|=
name|HiveBaseChar
operator|.
name|enforceMaxLength
argument_list|(
name|testString
argument_list|,
name|strLen
argument_list|)
decl_stmt|;
if|if
condition|(
name|curLen
operator|<=
name|strLen
condition|)
block|{
comment|// No truncation needed
name|assertEquals
argument_list|(
name|testString
argument_list|,
name|enforcedString
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// String should have been truncated.
name|assertEquals
argument_list|(
name|strLen
argument_list|,
name|enforcedString
operator|.
name|codePointCount
argument_list|(
literal|0
argument_list|,
name|enforcedString
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|HiveVarchar
name|vc1
init|=
operator|new
name|HiveVarchar
argument_list|(
literal|"0123456789"
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|vc1
operator|.
name|getCharacterLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// Changing string value; getCharacterLength() should update accordingly
name|vc1
operator|.
name|setValue
argument_list|(
literal|"012345678901234"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|15
argument_list|,
name|vc1
operator|.
name|getCharacterLength
argument_list|()
argument_list|)
expr_stmt|;
name|vc1
operator|.
name|setValue
argument_list|(
literal|"01234"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|vc1
operator|.
name|getCharacterLength
argument_list|()
argument_list|)
expr_stmt|;
name|vc1
operator|.
name|setValue
argument_list|(
operator|new
name|HiveVarchar
argument_list|(
literal|"0123456789"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|vc1
operator|.
name|getCharacterLength
argument_list|()
argument_list|)
expr_stmt|;
name|vc1
operator|.
name|setValue
argument_list|(
operator|new
name|HiveVarchar
argument_list|(
literal|"01234"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|vc1
operator|.
name|getCharacterLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Concurrent
argument_list|(
name|count
operator|=
literal|4
argument_list|)
annotation|@
name|Repeating
argument_list|(
name|repetition
operator|=
literal|100
argument_list|)
specifier|public
name|void
name|testComparison
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveVarchar
name|hc1
init|=
operator|new
name|HiveVarchar
argument_list|(
literal|"abcd"
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|HiveVarchar
name|hc2
init|=
operator|new
name|HiveVarchar
argument_list|(
literal|"abcd"
argument_list|,
literal|20
argument_list|)
decl_stmt|;
comment|// Identical strings should be equal
name|assertTrue
argument_list|(
name|hc1
operator|.
name|equals
argument_list|(
name|hc2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hc2
operator|.
name|equals
argument_list|(
name|hc1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hc1
operator|.
name|compareTo
argument_list|(
name|hc2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|hc2
operator|.
name|compareTo
argument_list|(
name|hc1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Unequal strings
name|hc2
operator|=
operator|new
name|HiveVarchar
argument_list|(
literal|"abcde"
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hc1
operator|.
name|equals
argument_list|(
name|hc2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hc2
operator|.
name|equals
argument_list|(
name|hc1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|0
operator|==
name|hc1
operator|.
name|compareTo
argument_list|(
name|hc2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|0
operator|==
name|hc2
operator|.
name|compareTo
argument_list|(
name|hc1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Trailing spaces are significant
name|hc2
operator|=
operator|new
name|HiveVarchar
argument_list|(
literal|"abcd  "
argument_list|,
literal|30
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hc1
operator|.
name|equals
argument_list|(
name|hc2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hc2
operator|.
name|equals
argument_list|(
name|hc1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|0
operator|==
name|hc1
operator|.
name|compareTo
argument_list|(
name|hc2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|0
operator|==
name|hc2
operator|.
name|compareTo
argument_list|(
name|hc1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Leading spaces are significant
name|hc2
operator|=
operator|new
name|HiveVarchar
argument_list|(
literal|"  abcd"
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hc1
operator|.
name|equals
argument_list|(
name|hc2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hc2
operator|.
name|equals
argument_list|(
name|hc1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|0
operator|==
name|hc1
operator|.
name|compareTo
argument_list|(
name|hc2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|0
operator|==
name|hc2
operator|.
name|compareTo
argument_list|(
name|hc1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

