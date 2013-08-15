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
operator|.
name|orc
package|;
end_package

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
name|nio
operator|.
name|ByteBuffer
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
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_class
specifier|public
class|class
name|TestRunLengthIntegerReader
block|{
specifier|public
name|void
name|runSeekTest
parameter_list|(
name|CompressionCodec
name|codec
parameter_list|)
throws|throws
name|Exception
block|{
name|TestInStream
operator|.
name|OutputCollector
name|collect
init|=
operator|new
name|TestInStream
operator|.
name|OutputCollector
argument_list|()
decl_stmt|;
name|RunLengthIntegerWriter
name|out
init|=
operator|new
name|RunLengthIntegerWriter
argument_list|(
operator|new
name|OutStream
argument_list|(
literal|"test"
argument_list|,
literal|1000
argument_list|,
name|codec
argument_list|,
name|collect
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|TestInStream
operator|.
name|PositionCollector
index|[]
name|positions
init|=
operator|new
name|TestInStream
operator|.
name|PositionCollector
index|[
literal|4096
index|]
decl_stmt|;
name|Random
name|random
init|=
operator|new
name|Random
argument_list|(
literal|99
argument_list|)
decl_stmt|;
name|int
index|[]
name|junk
init|=
operator|new
name|int
index|[
literal|2048
index|]
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
name|junk
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|junk
index|[
name|i
index|]
operator|=
name|random
operator|.
name|nextInt
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|4096
condition|;
operator|++
name|i
control|)
block|{
name|positions
index|[
name|i
index|]
operator|=
operator|new
name|TestInStream
operator|.
name|PositionCollector
argument_list|()
expr_stmt|;
name|out
operator|.
name|getPosition
argument_list|(
name|positions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
comment|// test runs, incrementing runs, non-runs
if|if
condition|(
name|i
operator|<
literal|1024
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|i
operator|/
literal|4
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|i
operator|<
literal|2048
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|2
operator|*
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|write
argument_list|(
name|junk
index|[
name|i
operator|-
literal|2048
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|ByteBuffer
name|inBuf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|collect
operator|.
name|buffer
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|collect
operator|.
name|buffer
operator|.
name|setByteBuffer
argument_list|(
name|inBuf
argument_list|,
literal|0
argument_list|,
name|collect
operator|.
name|buffer
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|inBuf
operator|.
name|flip
argument_list|()
expr_stmt|;
name|RunLengthIntegerReader
name|in
init|=
operator|new
name|RunLengthIntegerReader
argument_list|(
name|InStream
operator|.
name|create
argument_list|(
literal|"test"
argument_list|,
operator|new
name|ByteBuffer
index|[]
block|{
name|inBuf
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
literal|0
block|}
argument_list|,
name|inBuf
operator|.
name|remaining
argument_list|()
argument_list|,
name|codec
argument_list|,
literal|1000
argument_list|)
argument_list|,
literal|true
argument_list|)
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
literal|2048
condition|;
operator|++
name|i
control|)
block|{
name|int
name|x
init|=
operator|(
name|int
operator|)
name|in
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|<
literal|1024
condition|)
block|{
name|assertEquals
argument_list|(
name|i
operator|/
literal|4
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|i
operator|<
literal|2048
condition|)
block|{
name|assertEquals
argument_list|(
literal|2
operator|*
name|i
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|junk
index|[
name|i
operator|-
literal|2048
index|]
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|2047
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|in
operator|.
name|seek
argument_list|(
name|positions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|int
name|x
init|=
operator|(
name|int
operator|)
name|in
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|<
literal|1024
condition|)
block|{
name|assertEquals
argument_list|(
name|i
operator|/
literal|4
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|i
operator|<
literal|2048
condition|)
block|{
name|assertEquals
argument_list|(
literal|2
operator|*
name|i
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|junk
index|[
name|i
operator|-
literal|2048
index|]
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUncompressedSeek
parameter_list|()
throws|throws
name|Exception
block|{
name|runSeekTest
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompressedSeek
parameter_list|()
throws|throws
name|Exception
block|{
name|runSeekTest
argument_list|(
operator|new
name|ZlibCodec
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSkips
parameter_list|()
throws|throws
name|Exception
block|{
name|TestInStream
operator|.
name|OutputCollector
name|collect
init|=
operator|new
name|TestInStream
operator|.
name|OutputCollector
argument_list|()
decl_stmt|;
name|RunLengthIntegerWriter
name|out
init|=
operator|new
name|RunLengthIntegerWriter
argument_list|(
operator|new
name|OutStream
argument_list|(
literal|"test"
argument_list|,
literal|100
argument_list|,
literal|null
argument_list|,
name|collect
argument_list|)
argument_list|,
literal|true
argument_list|)
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
literal|2048
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|<
literal|1024
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|write
argument_list|(
literal|256
operator|*
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|ByteBuffer
name|inBuf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|collect
operator|.
name|buffer
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|collect
operator|.
name|buffer
operator|.
name|setByteBuffer
argument_list|(
name|inBuf
argument_list|,
literal|0
argument_list|,
name|collect
operator|.
name|buffer
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|inBuf
operator|.
name|flip
argument_list|()
expr_stmt|;
name|RunLengthIntegerReader
name|in
init|=
operator|new
name|RunLengthIntegerReader
argument_list|(
name|InStream
operator|.
name|create
argument_list|(
literal|"test"
argument_list|,
operator|new
name|ByteBuffer
index|[]
block|{
name|inBuf
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
literal|0
block|}
argument_list|,
name|inBuf
operator|.
name|remaining
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|100
argument_list|)
argument_list|,
literal|true
argument_list|)
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
literal|2048
condition|;
name|i
operator|+=
literal|10
control|)
block|{
name|int
name|x
init|=
operator|(
name|int
operator|)
name|in
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|<
literal|1024
condition|)
block|{
name|assertEquals
argument_list|(
name|i
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|256
operator|*
name|i
argument_list|,
name|x
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|<
literal|2038
condition|)
block|{
name|in
operator|.
name|skip
argument_list|(
literal|9
argument_list|)
expr_stmt|;
block|}
name|in
operator|.
name|skip
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

