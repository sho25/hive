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
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|List
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
name|*
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

begin_class
specifier|public
class|class
name|TestTimestampParser
block|{
specifier|public
specifier|static
class|class
name|ValidTimestampCase
block|{
name|String
name|strValue
decl_stmt|;
name|Timestamp
name|expectedValue
decl_stmt|;
specifier|public
name|ValidTimestampCase
parameter_list|(
name|String
name|strValue
parameter_list|,
name|Timestamp
name|expectedValue
parameter_list|)
block|{
name|this
operator|.
name|strValue
operator|=
name|strValue
expr_stmt|;
name|this
operator|.
name|expectedValue
operator|=
name|expectedValue
expr_stmt|;
block|}
block|}
specifier|static
name|void
name|testValidCases
parameter_list|(
name|TimestampParser
name|tp
parameter_list|,
name|ValidTimestampCase
index|[]
name|validCases
parameter_list|)
block|{
for|for
control|(
name|ValidTimestampCase
name|validCase
range|:
name|validCases
control|)
block|{
name|Timestamp
name|ts
init|=
name|tp
operator|.
name|parseTimestamp
argument_list|(
name|validCase
operator|.
name|strValue
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Parsing "
operator|+
name|validCase
operator|.
name|strValue
argument_list|,
name|validCase
operator|.
name|expectedValue
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|void
name|testInvalidCases
parameter_list|(
name|TimestampParser
name|tp
parameter_list|,
name|String
index|[]
name|invalidCases
parameter_list|)
block|{
for|for
control|(
name|String
name|invalidString
range|:
name|invalidCases
control|)
block|{
try|try
block|{
name|Timestamp
name|ts
init|=
name|tp
operator|.
name|parseTimestamp
argument_list|(
name|invalidString
argument_list|)
decl_stmt|;
name|fail
argument_list|(
literal|"Expected exception parsing "
operator|+
name|invalidString
operator|+
literal|", but parsed value to "
operator|+
name|ts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|err
parameter_list|)
block|{
comment|// Exception expected
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefault
parameter_list|()
block|{
comment|// No timestamp patterns, should default to normal timestamp format
name|TimestampParser
name|tp
init|=
operator|new
name|TimestampParser
argument_list|()
decl_stmt|;
name|ValidTimestampCase
index|[]
name|validCases
init|=
block|{
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1945-12-31 23:59:59.0"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1945-12-31 23:59:59.0"
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1945-12-31 23:59:59.1234"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1945-12-31 23:59:59.1234"
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1970-01-01 00:00:00"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1970-01-01 00:00:00"
argument_list|)
argument_list|)
block|,     }
decl_stmt|;
name|String
index|[]
name|invalidCases
init|=
block|{
literal|"1945-12-31T23:59:59"
block|,
literal|"12345"
block|,     }
decl_stmt|;
name|testValidCases
argument_list|(
name|tp
argument_list|,
name|validCases
argument_list|)
expr_stmt|;
name|testInvalidCases
argument_list|(
name|tp
argument_list|,
name|invalidCases
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPattern1
parameter_list|()
block|{
comment|// Joda pattern matching expects fractional seconds length to match
comment|// the number of 'S' in the pattern. So if you want to match .1, .12, .123,
comment|// you need 3 different patterns with .S, .SS, .SSS
name|String
index|[]
name|patterns
init|=
block|{
comment|// ISO-8601 timestamps
literal|"yyyy-MM-dd'T'HH:mm:ss"
block|,
literal|"yyyy-MM-dd'T'HH:mm:ss.S"
block|,
literal|"yyyy-MM-dd'T'HH:mm:ss.SS"
block|,
literal|"yyyy-MM-dd'T'HH:mm:ss.SSS"
block|,
literal|"yyyy-MM-dd'T'HH:mm:ss.SSSS"
block|,     }
decl_stmt|;
name|TimestampParser
name|tp
init|=
operator|new
name|TimestampParser
argument_list|(
name|patterns
argument_list|)
decl_stmt|;
name|ValidTimestampCase
index|[]
name|validCases
init|=
block|{
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1945-12-31T23:59:59.0"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1945-12-31 23:59:59.0"
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"2001-01-01 00:00:00.100"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"2001-01-01 00:00:00.100"
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"2001-01-01 00:00:00.001"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"2001-01-01 00:00:00.001"
argument_list|)
argument_list|)
block|,
comment|// Joda parsing only supports up to millisecond precision
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1945-12-31T23:59:59.1234"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1945-12-31 23:59:59.123"
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1970-01-01T00:00:00"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1970-01-01 00:00:00"
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1970-4-5T6:7:8"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1970-04-05 06:07:08"
argument_list|)
argument_list|)
block|,
comment|// Default timestamp format still works?
operator|new
name|ValidTimestampCase
argument_list|(
literal|"2001-01-01 00:00:00"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"2001-01-01 00:00:00"
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1945-12-31 23:59:59.1234"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1945-12-31 23:59:59.1234"
argument_list|)
argument_list|)
block|,     }
decl_stmt|;
name|String
index|[]
name|invalidCases
init|=
block|{
literal|"1945-12-31-23:59:59"
block|,
literal|"1945-12-31T23:59:59.12345"
block|,
comment|// our pattern didn't specify 5 decimal places
literal|"12345"
block|,     }
decl_stmt|;
name|testValidCases
argument_list|(
name|tp
argument_list|,
name|validCases
argument_list|)
expr_stmt|;
name|testInvalidCases
argument_list|(
name|tp
argument_list|,
name|invalidCases
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMillisParser
parameter_list|()
block|{
name|String
index|[]
name|patterns
init|=
block|{
literal|"millis"
block|,
comment|// Also try other patterns
literal|"yyyy-MM-dd'T'HH:mm:ss"
block|,     }
decl_stmt|;
name|TimestampParser
name|tp
init|=
operator|new
name|TimestampParser
argument_list|(
name|patterns
argument_list|)
decl_stmt|;
name|ValidTimestampCase
index|[]
name|validCases
init|=
block|{
operator|new
name|ValidTimestampCase
argument_list|(
literal|"0"
argument_list|,
operator|new
name|Timestamp
argument_list|(
literal|0
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"-1000000"
argument_list|,
operator|new
name|Timestamp
argument_list|(
operator|-
literal|1000000
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1420509274123"
argument_list|,
operator|new
name|Timestamp
argument_list|(
literal|1420509274123L
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1420509274123.456789"
argument_list|,
operator|new
name|Timestamp
argument_list|(
literal|1420509274123L
argument_list|)
argument_list|)
block|,
comment|// Other format pattern should also work
operator|new
name|ValidTimestampCase
argument_list|(
literal|"1945-12-31T23:59:59"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1945-12-31 23:59:59"
argument_list|)
argument_list|)
block|,     }
decl_stmt|;
name|String
index|[]
name|invalidCases
init|=
block|{
literal|"1945-12-31-23:59:59"
block|,
literal|"1945-12-31T23:59:59.12345"
block|,
comment|// our pattern didn't specify 5 decimal places
literal|"1420509274123-"
block|,     }
decl_stmt|;
name|testValidCases
argument_list|(
name|tp
argument_list|,
name|validCases
argument_list|)
expr_stmt|;
name|testInvalidCases
argument_list|(
name|tp
argument_list|,
name|invalidCases
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPattern2
parameter_list|()
block|{
comment|// Pattern does not contain all date fields
name|String
index|[]
name|patterns
init|=
block|{
literal|"HH:mm"
block|,
literal|"MM:dd:ss"
block|,     }
decl_stmt|;
name|TimestampParser
name|tp
init|=
operator|new
name|TimestampParser
argument_list|(
name|patterns
argument_list|)
decl_stmt|;
name|ValidTimestampCase
index|[]
name|validCases
init|=
block|{
operator|new
name|ValidTimestampCase
argument_list|(
literal|"05:06"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1970-01-01 05:06:00"
argument_list|)
argument_list|)
block|,
operator|new
name|ValidTimestampCase
argument_list|(
literal|"05:06:07"
argument_list|,
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"1970-05-06 00:00:07"
argument_list|)
argument_list|)
block|,     }
decl_stmt|;
name|String
index|[]
name|invalidCases
init|=
block|{
literal|"1945-12-31T23:59:59"
block|,
literal|"1945:12:31-"
block|,
literal|"12345"
block|,     }
decl_stmt|;
name|testValidCases
argument_list|(
name|tp
argument_list|,
name|validCases
argument_list|)
expr_stmt|;
name|testInvalidCases
argument_list|(
name|tp
argument_list|,
name|invalidCases
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

