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
name|io
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
name|assertArrayEquals
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Test
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_comment
comment|/**  * TestHiveInputOutputBuffer.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveInputOutputBuffer
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|int
name|numCases
init|=
literal|14
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|asciiLine1
init|=
literal|"Foo 12345 moo"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|asciiLine2
init|=
literal|"Line two"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|asciiString
init|=
name|asciiLine1
operator|+
literal|"\n"
operator|+
name|asciiLine2
operator|+
literal|"\r\n"
decl_stmt|;
specifier|public
name|void
name|testReadAndWrite
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|testString
init|=
literal|"test_hive_input_output_number_0"
decl_stmt|;
name|byte
index|[]
name|string_bytes
init|=
name|testString
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|NonSyncDataInputBuffer
name|inBuffer
init|=
operator|new
name|NonSyncDataInputBuffer
argument_list|()
decl_stmt|;
name|NonSyncDataOutputBuffer
name|outBuffer
init|=
operator|new
name|NonSyncDataOutputBuffer
argument_list|()
decl_stmt|;
try|try
block|{
name|outBuffer
operator|.
name|write
argument_list|(
name|string_bytes
argument_list|)
expr_stmt|;
name|inBuffer
operator|.
name|reset
argument_list|(
name|outBuffer
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|outBuffer
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|readBytes
init|=
operator|new
name|byte
index|[
name|string_bytes
operator|.
name|length
index|]
decl_stmt|;
name|inBuffer
operator|.
name|read
argument_list|(
name|readBytes
argument_list|)
expr_stmt|;
name|String
name|readString
init|=
operator|new
name|String
argument_list|(
name|readBytes
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Field testReadAndWrite()"
argument_list|,
name|readString
argument_list|,
name|testString
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|inBuffer
operator|.
name|close
argument_list|()
expr_stmt|;
name|outBuffer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|private
specifier|static
name|void
name|readJunk
parameter_list|(
name|NonSyncDataInputBuffer
name|in
parameter_list|,
name|Random
name|r
parameter_list|,
name|long
name|seed
parameter_list|,
name|int
name|iter
parameter_list|)
throws|throws
name|IOException
block|{
name|r
operator|.
name|setSeed
argument_list|(
name|seed
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
name|iter
condition|;
operator|++
name|i
control|)
block|{
switch|switch
condition|(
name|r
operator|.
name|nextInt
argument_list|(
name|numCases
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|assertEquals
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
operator|&
literal|0xFF
argument_list|)
argument_list|,
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|assertEquals
argument_list|(
call|(
name|short
call|)
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
operator|&
literal|0xFFFF
argument_list|)
argument_list|,
name|in
operator|.
name|readShort
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|assertEquals
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
argument_list|,
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|assertEquals
argument_list|(
name|r
operator|.
name|nextLong
argument_list|()
argument_list|,
name|in
operator|.
name|readLong
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|assertEquals
argument_list|(
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|r
operator|.
name|nextDouble
argument_list|()
argument_list|)
argument_list|,
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|in
operator|.
name|readDouble
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|assertEquals
argument_list|(
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|r
operator|.
name|nextFloat
argument_list|()
argument_list|)
argument_list|,
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|in
operator|.
name|readFloat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|6
case|:
name|int
name|len
init|=
name|r
operator|.
name|nextInt
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
comment|// 1 (test #readFully(3)):
specifier|final
name|byte
index|[]
name|vb
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|r
operator|.
name|nextBytes
argument_list|(
name|vb
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|vb
argument_list|,
name|b
argument_list|)
expr_stmt|;
comment|// 2 (test #read(3)):
name|r
operator|.
name|nextBytes
argument_list|(
name|vb
argument_list|)
expr_stmt|;
name|in
operator|.
name|read
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|vb
argument_list|,
name|b
argument_list|)
expr_stmt|;
comment|// 3 (test #readFully(1)):
name|r
operator|.
name|nextBytes
argument_list|(
name|vb
argument_list|)
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|vb
argument_list|,
name|b
argument_list|)
expr_stmt|;
break|break;
case|case
literal|7
case|:
name|assertEquals
argument_list|(
name|r
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|8
case|:
name|assertEquals
argument_list|(
operator|(
name|char
operator|)
name|r
operator|.
name|nextInt
argument_list|()
argument_list|,
name|in
operator|.
name|readChar
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|9
case|:
name|int
name|actualUB
init|=
name|in
operator|.
name|readUnsignedByte
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|actualUB
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|actualUB
operator|<=
literal|255
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
operator|&
literal|0xFF
argument_list|,
name|actualUB
argument_list|)
expr_stmt|;
break|break;
case|case
literal|10
case|:
name|int
name|actualUS
init|=
name|in
operator|.
name|readUnsignedShort
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|actualUS
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|actualUS
operator|<=
literal|0xFFFF
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
operator|&
literal|0xFFFF
argument_list|,
name|actualUS
argument_list|)
expr_stmt|;
break|break;
case|case
literal|11
case|:
name|String
name|expectedString1
init|=
name|composeString
argument_list|(
literal|1024
argument_list|,
name|r
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedString1
argument_list|,
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|expectedString2
init|=
name|composeString
argument_list|(
literal|1024
argument_list|,
name|r
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedString2
argument_list|,
name|NonSyncDataInputBuffer
operator|.
name|readUTF
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|12
case|:
name|assertEquals
argument_list|(
name|asciiLine1
argument_list|,
name|in
operator|.
name|readLine
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|asciiLine2
argument_list|,
name|in
operator|.
name|readLine
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|13
case|:
name|in
operator|.
name|skipBytes
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|r
operator|.
name|nextLong
argument_list|()
expr_stmt|;
comment|// ignore
name|assertEquals
argument_list|(
name|r
operator|.
name|nextLong
argument_list|()
argument_list|,
name|in
operator|.
name|readLong
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|writeJunk
parameter_list|(
name|DataOutput
name|out
parameter_list|,
name|Random
name|r
parameter_list|,
name|long
name|seed
parameter_list|,
name|int
name|iter
parameter_list|)
throws|throws
name|IOException
block|{
name|r
operator|.
name|setSeed
argument_list|(
name|seed
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
name|iter
condition|;
operator|++
name|i
control|)
block|{
switch|switch
condition|(
name|r
operator|.
name|nextInt
argument_list|(
name|numCases
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|out
operator|.
name|writeByte
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|out
operator|.
name|writeShort
argument_list|(
call|(
name|short
call|)
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
operator|&
literal|0xFFFF
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|out
operator|.
name|writeInt
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|out
operator|.
name|writeLong
argument_list|(
name|r
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|out
operator|.
name|writeDouble
argument_list|(
name|r
operator|.
name|nextDouble
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|out
operator|.
name|writeFloat
argument_list|(
name|r
operator|.
name|nextFloat
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|6
case|:
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
name|r
operator|.
name|nextInt
argument_list|(
literal|1024
argument_list|)
index|]
decl_stmt|;
comment|// 1:
name|r
operator|.
name|nextBytes
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|b
argument_list|)
expr_stmt|;
comment|// 2:
name|r
operator|.
name|nextBytes
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|b
argument_list|)
expr_stmt|;
comment|// 3:
name|r
operator|.
name|nextBytes
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|b
argument_list|)
expr_stmt|;
break|break;
case|case
literal|7
case|:
name|out
operator|.
name|writeBoolean
argument_list|(
name|r
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|8
case|:
name|out
operator|.
name|writeChar
argument_list|(
operator|(
name|char
operator|)
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|9
case|:
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|10
case|:
name|out
operator|.
name|writeShort
argument_list|(
operator|(
name|short
operator|)
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|11
case|:
name|String
name|string
init|=
name|composeString
argument_list|(
literal|1024
argument_list|,
name|r
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|string
argument_list|)
expr_stmt|;
name|String
name|string2
init|=
name|composeString
argument_list|(
literal|1024
argument_list|,
name|r
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|string2
argument_list|)
expr_stmt|;
break|break;
case|case
literal|12
case|:
name|byte
index|[]
name|bb
init|=
name|asciiString
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|bb
argument_list|)
expr_stmt|;
break|break;
case|case
literal|13
case|:
name|out
operator|.
name|writeLong
argument_list|(
name|r
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|r
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
specifier|private
specifier|static
name|String
name|composeString
parameter_list|(
name|int
name|len
parameter_list|,
name|Random
name|r
parameter_list|)
block|{
name|char
index|[]
name|cc
init|=
operator|new
name|char
index|[
name|len
index|]
decl_stmt|;
name|char
name|ch
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
name|len
condition|;
name|i
operator|++
control|)
block|{
do|do
block|{
name|ch
operator|=
operator|(
name|char
operator|)
name|r
operator|.
name|nextInt
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|Character
operator|.
name|isDefined
argument_list|(
name|ch
argument_list|)
operator|||
name|Character
operator|.
name|isHighSurrogate
argument_list|(
name|ch
argument_list|)
operator|||
name|Character
operator|.
name|isLowSurrogate
argument_list|(
name|ch
argument_list|)
condition|)
do|;
name|cc
index|[
name|i
index|]
operator|=
name|ch
expr_stmt|;
block|}
return|return
operator|new
name|String
argument_list|(
name|cc
argument_list|)
return|;
block|}
comment|/**    * Tests methods of {@link NonSyncDataInputBuffer}.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testBaseBuffers
parameter_list|()
throws|throws
name|IOException
block|{
name|NonSyncDataOutputBuffer
name|dob
init|=
operator|new
name|NonSyncDataOutputBuffer
argument_list|()
decl_stmt|;
specifier|final
name|Random
name|r
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|final
name|long
name|seed
init|=
literal|0x0123456789ABCDEFL
decl_stmt|;
comment|// hardcoded for reproducibility.
name|r
operator|.
name|setSeed
argument_list|(
name|seed
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"SEED: "
operator|+
name|seed
argument_list|)
expr_stmt|;
name|writeJunk
argument_list|(
name|dob
argument_list|,
name|r
argument_list|,
name|seed
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|NonSyncDataInputBuffer
name|dib
init|=
operator|new
name|NonSyncDataInputBuffer
argument_list|()
decl_stmt|;
name|dib
operator|.
name|reset
argument_list|(
name|dob
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|dob
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|dib
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|dob
operator|.
name|getLength
argument_list|()
argument_list|,
name|dib
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|readJunk
argument_list|(
name|dib
argument_list|,
name|r
argument_list|,
name|seed
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|dob
operator|.
name|reset
argument_list|()
expr_stmt|;
name|writeJunk
argument_list|(
name|dob
argument_list|,
name|r
argument_list|,
name|seed
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|dib
operator|.
name|reset
argument_list|(
name|dob
operator|.
name|getData
argument_list|()
argument_list|,
name|dob
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|dib
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|dob
operator|.
name|getLength
argument_list|()
argument_list|,
name|dib
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|readJunk
argument_list|(
name|dib
argument_list|,
name|r
argument_list|,
name|seed
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

