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
name|io
operator|.
name|orc
operator|.
name|encoded
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
name|*
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
name|ArrayList
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
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|impl
operator|.
name|BufferChunk
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
name|TestEncodedReaderImpl
block|{
annotation|@
name|Test
specifier|public
name|void
name|testReadLength
parameter_list|()
throws|throws
name|IOException
block|{
name|ByteBuffer
name|one
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
decl_stmt|,
name|two
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|)
decl_stmt|,
name|three
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|3
block|}
argument_list|)
decl_stmt|,
name|twoThree
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|,
literal|3
block|}
argument_list|)
decl_stmt|,
name|oneTwo
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|)
decl_stmt|;
name|BufferChunk
name|bc
init|=
operator|new
name|BufferChunk
argument_list|(
name|one
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|int
index|[]
name|result
init|=
operator|new
name|int
index|[
literal|3
index|]
decl_stmt|;
name|List
argument_list|<
name|IncompleteCb
argument_list|>
name|l
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|IoTrace
name|trace
init|=
operator|new
name|IoTrace
argument_list|(
literal|0
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|BufferChunk
name|rv
init|=
name|EncodedReaderImpl
operator|.
name|readLengthBytesFromSmallBuffers
argument_list|(
name|bc
argument_list|,
literal|0l
argument_list|,
name|result
argument_list|,
name|l
argument_list|,
literal|true
argument_list|,
name|trace
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|rv
argument_list|)
expr_stmt|;
name|one
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|bc
operator|.
name|insertAfter
argument_list|(
operator|new
name|BufferChunk
argument_list|(
name|two
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|result
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|rv
operator|=
name|EncodedReaderImpl
operator|.
name|readLengthBytesFromSmallBuffers
argument_list|(
name|bc
argument_list|,
literal|0l
argument_list|,
name|result
argument_list|,
name|l
argument_list|,
literal|true
argument_list|,
name|trace
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rv
argument_list|)
expr_stmt|;
name|one
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|two
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|bc
operator|.
name|insertAfter
argument_list|(
operator|new
name|BufferChunk
argument_list|(
name|two
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|insertAfter
argument_list|(
operator|new
name|BufferChunk
argument_list|(
name|three
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|result
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|rv
operator|=
name|EncodedReaderImpl
operator|.
name|readLengthBytesFromSmallBuffers
argument_list|(
name|bc
argument_list|,
literal|0l
argument_list|,
name|result
argument_list|,
name|l
argument_list|,
literal|true
argument_list|,
name|trace
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|rv
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
name|result
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|result
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|one
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|bc
operator|.
name|insertAfter
argument_list|(
operator|new
name|BufferChunk
argument_list|(
name|twoThree
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|result
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|rv
operator|=
name|EncodedReaderImpl
operator|.
name|readLengthBytesFromSmallBuffers
argument_list|(
name|bc
argument_list|,
literal|0l
argument_list|,
name|result
argument_list|,
name|l
argument_list|,
literal|true
argument_list|,
name|trace
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|rv
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
name|result
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|result
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|bc
operator|=
operator|new
name|BufferChunk
argument_list|(
name|oneTwo
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|result
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|rv
operator|=
name|EncodedReaderImpl
operator|.
name|readLengthBytesFromSmallBuffers
argument_list|(
name|bc
argument_list|,
literal|0l
argument_list|,
name|result
argument_list|,
name|l
argument_list|,
literal|true
argument_list|,
name|trace
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rv
argument_list|)
expr_stmt|;
name|three
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|bc
operator|.
name|insertAfter
argument_list|(
operator|new
name|BufferChunk
argument_list|(
name|three
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|result
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|rv
operator|=
name|EncodedReaderImpl
operator|.
name|readLengthBytesFromSmallBuffers
argument_list|(
name|bc
argument_list|,
literal|0l
argument_list|,
name|result
argument_list|,
name|l
argument_list|,
literal|true
argument_list|,
name|trace
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|rv
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
name|result
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|result
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

