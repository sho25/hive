begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|serde2
operator|.
name|io
package|;
end_package

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
name|Repeating
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
name|TimestampTZ
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
name|WritableComparator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadLocalRandom
import|;
end_import

begin_class
specifier|public
class|class
name|TestTimestampTZWritable
block|{
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
annotation|@
name|Test
annotation|@
name|Repeating
argument_list|(
name|repetition
operator|=
literal|10
argument_list|)
specifier|public
name|void
name|testSeconds
parameter_list|()
block|{
comment|// just 1 VInt
name|long
name|seconds
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|TimestampTZ
name|tstz
init|=
operator|new
name|TimestampTZ
argument_list|(
name|seconds
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|verifyConversion
argument_list|(
name|tstz
argument_list|)
expr_stmt|;
comment|// 2 VInt
name|seconds
operator|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|+
name|Integer
operator|.
name|MAX_VALUE
operator|+
literal|1
expr_stmt|;
if|if
condition|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|seconds
operator|=
operator|-
name|seconds
expr_stmt|;
block|}
name|tstz
operator|.
name|set
argument_list|(
name|seconds
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|verifyConversion
argument_list|(
name|tstz
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Repeating
argument_list|(
name|repetition
operator|=
literal|10
argument_list|)
specifier|public
name|void
name|testSecondsWithNanos
parameter_list|()
block|{
name|long
name|seconds
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
literal|31556889864403199L
argument_list|)
decl_stmt|;
if|if
condition|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|seconds
operator|=
operator|-
name|seconds
expr_stmt|;
block|}
name|int
name|nanos
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|999999999
argument_list|)
operator|+
literal|1
decl_stmt|;
name|TimestampTZ
name|tstz
init|=
operator|new
name|TimestampTZ
argument_list|(
name|seconds
argument_list|,
name|nanos
argument_list|)
decl_stmt|;
name|verifyConversion
argument_list|(
name|tstz
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testComparison
parameter_list|()
block|{
name|String
name|s1
init|=
literal|"2017-04-14 18:00:00 Asia/Shanghai"
decl_stmt|;
name|String
name|s2
init|=
literal|"2017-04-14 10:00:00.00 GMT"
decl_stmt|;
name|String
name|s3
init|=
literal|"2017-04-14 18:00:00 UTC+08:00"
decl_stmt|;
name|String
name|s4
init|=
literal|"2017-04-14 18:00:00 Europe/London"
decl_stmt|;
name|TimestampTZWritable
name|writable1
init|=
operator|new
name|TimestampTZWritable
argument_list|(
name|TimestampTZ
operator|.
name|parse
argument_list|(
name|s1
argument_list|)
argument_list|)
decl_stmt|;
name|TimestampTZWritable
name|writable2
init|=
operator|new
name|TimestampTZWritable
argument_list|(
name|TimestampTZ
operator|.
name|parse
argument_list|(
name|s2
argument_list|)
argument_list|)
decl_stmt|;
name|TimestampTZWritable
name|writable3
init|=
operator|new
name|TimestampTZWritable
argument_list|(
name|TimestampTZ
operator|.
name|parse
argument_list|(
name|s3
argument_list|)
argument_list|)
decl_stmt|;
name|TimestampTZWritable
name|writable4
init|=
operator|new
name|TimestampTZWritable
argument_list|(
name|TimestampTZ
operator|.
name|parse
argument_list|(
name|s4
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|writable1
argument_list|,
name|writable2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|writable1
argument_list|,
name|writable3
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|writable1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|writable2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|writable1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|writable3
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|writable1
operator|.
name|compareTo
argument_list|(
name|writable4
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bs1
init|=
name|writable1
operator|.
name|toBinarySortable
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bs2
init|=
name|writable2
operator|.
name|toBinarySortable
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bs3
init|=
name|writable3
operator|.
name|toBinarySortable
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bs4
init|=
name|writable4
operator|.
name|toBinarySortable
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|WritableComparator
operator|.
name|compareBytes
argument_list|(
name|bs1
argument_list|,
literal|0
argument_list|,
name|bs1
operator|.
name|length
argument_list|,
name|bs2
argument_list|,
literal|0
argument_list|,
name|bs2
operator|.
name|length
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|WritableComparator
operator|.
name|compareBytes
argument_list|(
name|bs1
argument_list|,
literal|0
argument_list|,
name|bs1
operator|.
name|length
argument_list|,
name|bs3
argument_list|,
literal|0
argument_list|,
name|bs3
operator|.
name|length
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|WritableComparator
operator|.
name|compareBytes
argument_list|(
name|bs1
argument_list|,
literal|0
argument_list|,
name|bs1
operator|.
name|length
argument_list|,
name|bs4
argument_list|,
literal|0
argument_list|,
name|bs4
operator|.
name|length
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|verifyConversion
parameter_list|(
name|TimestampTZ
name|srcTstz
parameter_list|)
block|{
name|TimestampTZWritable
name|src
init|=
operator|new
name|TimestampTZWritable
argument_list|(
name|srcTstz
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|src
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|TimestampTZWritable
name|dest
init|=
operator|new
name|TimestampTZWritable
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|TimestampTZ
name|destTstz
init|=
name|dest
operator|.
name|getTimestampTZ
argument_list|()
decl_stmt|;
name|String
name|errMsg
init|=
literal|"Src tstz with seconds "
operator|+
name|srcTstz
operator|.
name|getEpochSecond
argument_list|()
operator|+
literal|", nanos "
operator|+
name|srcTstz
operator|.
name|getNanos
argument_list|()
operator|+
literal|". Dest tstz with seconds "
operator|+
name|destTstz
operator|.
name|getEpochSecond
argument_list|()
operator|+
literal|", nanos "
operator|+
name|destTstz
operator|.
name|getNanos
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|errMsg
argument_list|,
name|srcTstz
argument_list|,
name|destTstz
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

