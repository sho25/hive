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
name|accumulo
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|lexicoder
operator|.
name|BigIntegerLexicoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|lexicoder
operator|.
name|DoubleLexicoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|lexicoder
operator|.
name|IntegerLexicoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|lexicoder
operator|.
name|LongLexicoder
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
name|serde
operator|.
name|serdeConstants
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
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
operator|.
name|UTF_8
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
name|assertArrayEquals
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
name|assertEquals
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|TestAccumuloIndexLexicoder
block|{
annotation|@
name|Test
specifier|public
name|void
name|testBooleanString
parameter_list|()
block|{
name|byte
index|[]
name|value
init|=
name|Boolean
operator|.
name|TRUE
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
argument_list|,
literal|true
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBooleanBinary
parameter_list|()
block|{
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|}
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
argument_list|,
literal|false
argument_list|)
argument_list|,
name|Boolean
operator|.
name|TRUE
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIntString
parameter_list|()
block|{
name|byte
index|[]
name|value
init|=
literal|"10"
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|byte
index|[]
name|encoded
init|=
operator|new
name|IntegerLexicoder
argument_list|()
operator|.
name|encode
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|byte
index|[]
name|lex
init|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
name|lex
operator|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
name|lex
operator|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIntBinary
parameter_list|()
block|{
name|byte
index|[]
name|value
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
operator|.
name|putInt
argument_list|(
literal|10
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|byte
index|[]
name|encoded
init|=
operator|new
name|IntegerLexicoder
argument_list|()
operator|.
name|encode
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|byte
index|[]
name|lex
init|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
name|value
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|2
argument_list|)
operator|.
name|putShort
argument_list|(
operator|(
name|short
operator|)
literal|10
argument_list|)
operator|.
name|array
argument_list|()
expr_stmt|;
name|lex
operator|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
name|value
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|1
argument_list|)
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
literal|10
argument_list|)
operator|.
name|array
argument_list|()
expr_stmt|;
name|lex
operator|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFloatBinary
parameter_list|()
block|{
name|byte
index|[]
name|value
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
operator|.
name|putFloat
argument_list|(
literal|10.55f
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|byte
index|[]
name|encoded
init|=
operator|new
name|DoubleLexicoder
argument_list|()
operator|.
name|encode
argument_list|(
operator|(
name|double
operator|)
literal|10.55f
argument_list|)
decl_stmt|;
name|String
name|val
init|=
operator|new
name|String
argument_list|(
name|encoded
argument_list|)
decl_stmt|;
name|byte
index|[]
name|lex
init|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
name|value
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|8
argument_list|)
operator|.
name|putDouble
argument_list|(
literal|10.55
argument_list|)
operator|.
name|array
argument_list|()
expr_stmt|;
name|encoded
operator|=
operator|new
name|DoubleLexicoder
argument_list|()
operator|.
name|encode
argument_list|(
literal|10.55
argument_list|)
expr_stmt|;
name|lex
operator|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFloatString
parameter_list|()
block|{
name|byte
index|[]
name|value
init|=
literal|"10.55"
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|byte
index|[]
name|encoded
init|=
operator|new
name|DoubleLexicoder
argument_list|()
operator|.
name|encode
argument_list|(
literal|10.55
argument_list|)
decl_stmt|;
name|byte
index|[]
name|lex
init|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
name|lex
operator|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBigIntBinary
parameter_list|()
block|{
name|byte
index|[]
name|value
init|=
operator|new
name|String
argument_list|(
literal|"1232322323"
argument_list|)
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|byte
index|[]
name|encoded
init|=
operator|new
name|BigIntegerLexicoder
argument_list|()
operator|.
name|encode
argument_list|(
operator|new
name|BigInteger
argument_list|(
literal|"1232322323"
argument_list|,
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|lex
init|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
name|value
operator|=
operator|new
name|BigInteger
argument_list|(
literal|"1232322323"
argument_list|,
literal|10
argument_list|)
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
name|encoded
operator|=
operator|new
name|BigIntegerLexicoder
argument_list|()
operator|.
name|encode
argument_list|(
operator|new
name|BigInteger
argument_list|(
literal|"1232322323"
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|lex
operator|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalString
parameter_list|()
block|{
name|String
name|strVal
init|=
literal|"12323232233434"
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|strVal
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|byte
index|[]
name|encoded
init|=
name|strVal
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|byte
index|[]
name|lex
init|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|DECIMAL_TYPE_NAME
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
name|lex
operator|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
literal|"DECIMAL (10,3)"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalBinary
parameter_list|()
block|{
name|byte
index|[]
name|value
init|=
operator|new
name|BigInteger
argument_list|(
literal|"12323232233434"
argument_list|,
literal|10
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|byte
index|[]
name|encoded
init|=
operator|new
name|String
argument_list|(
name|value
argument_list|)
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|byte
index|[]
name|lex
init|=
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|DECIMAL_TYPE_NAME
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|lex
argument_list|,
name|encoded
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDateString
parameter_list|()
block|{
name|String
name|date
init|=
literal|"2016-02-22"
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|date
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|DATE_TYPE_NAME
argument_list|,
literal|true
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDateTimeString
parameter_list|()
block|{
name|String
name|timestamp
init|=
literal|"2016-02-22 12:12:06.000000005"
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|timestamp
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|TIMESTAMP_TYPE_NAME
argument_list|,
literal|true
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testString
parameter_list|()
block|{
name|String
name|strVal
init|=
literal|"The quick brown fox"
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|strVal
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|true
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
literal|"varChar(20)"
argument_list|,
literal|true
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|AccumuloIndexLexicoder
operator|.
name|encodeValue
argument_list|(
name|value
argument_list|,
literal|"CHAR (20)"
argument_list|,
literal|true
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

