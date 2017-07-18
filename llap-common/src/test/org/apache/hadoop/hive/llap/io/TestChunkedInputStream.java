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
name|llap
operator|.
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilterInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilterOutputStream
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PipedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PipedOutputStream
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
name|RandomTypeUtil
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

begin_class
specifier|public
class|class
name|TestChunkedInputStream
block|{
specifier|static
name|int
name|bufferSize
init|=
literal|128
decl_stmt|;
specifier|static
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|static
name|String
name|alphabet
init|=
literal|"abcdefghijklmnopqrstuvwxyz"
decl_stmt|;
specifier|static
class|class
name|StreamTester
block|{
name|Exception
name|error
init|=
literal|null
decl_stmt|;
specifier|public
name|Exception
name|getError
parameter_list|()
block|{
return|return
name|error
return|;
block|}
specifier|public
name|void
name|setError
parameter_list|(
name|Exception
name|error
parameter_list|)
block|{
name|this
operator|.
name|error
operator|=
name|error
expr_stmt|;
block|}
block|}
comment|// Test class to write a series of values to the designated output stream
specifier|static
class|class
name|BasicUsageWriter
extends|extends
name|StreamTester
implements|implements
name|Runnable
block|{
name|TestStreams
name|streams
decl_stmt|;
name|boolean
name|flushCout
decl_stmt|;
name|boolean
name|closePoutEarly
decl_stmt|;
specifier|public
name|BasicUsageWriter
parameter_list|(
name|TestStreams
name|streams
parameter_list|,
name|boolean
name|flushCout
parameter_list|,
name|boolean
name|closePoutEarly
parameter_list|)
block|{
name|this
operator|.
name|streams
operator|=
name|streams
expr_stmt|;
name|this
operator|.
name|flushCout
operator|=
name|flushCout
expr_stmt|;
name|this
operator|.
name|closePoutEarly
operator|=
name|closePoutEarly
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
comment|// Write the items to the output stream.
for|for
control|(
name|byte
index|[]
name|value
range|:
name|streams
operator|.
name|values
control|)
block|{
name|streams
operator|.
name|out
operator|.
name|write
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|flushCout
condition|)
block|{
name|streams
operator|.
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|closePoutEarly
condition|)
block|{
comment|// Close the inner output stream before closing the outer output stream.
comment|// For chunked output this means we don't write end-of-data indicator.
name|streams
operator|.
name|pout
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// This will throw error if we close pout early.
name|streams
operator|.
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|err
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|this
operator|.
name|error
operator|=
name|err
expr_stmt|;
block|}
block|}
block|}
comment|// Test class to read a series of values to the designated input stream
specifier|static
class|class
name|BasicUsageReader
extends|extends
name|StreamTester
implements|implements
name|Runnable
block|{
name|TestStreams
name|streams
decl_stmt|;
name|boolean
name|allValuesRead
init|=
literal|false
decl_stmt|;
specifier|public
name|BasicUsageReader
parameter_list|(
name|TestStreams
name|streams
parameter_list|)
block|{
name|this
operator|.
name|streams
operator|=
name|streams
expr_stmt|;
block|}
comment|// Continue reading from the input stream until the desired number of byte has been read
name|void
name|readFully
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|byte
index|[]
name|readValue
parameter_list|,
name|int
name|numBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|bytesRead
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|bytesRead
operator|<
name|numBytes
condition|)
block|{
name|int
name|read
init|=
name|in
operator|.
name|read
argument_list|(
name|readValue
argument_list|,
name|bytesRead
argument_list|,
name|numBytes
operator|-
name|bytesRead
argument_list|)
decl_stmt|;
if|if
condition|(
name|read
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected read length "
operator|+
name|read
argument_list|)
throw|;
block|}
name|bytesRead
operator|+=
name|read
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
comment|// Read the items from the input stream and confirm they match
for|for
control|(
name|byte
index|[]
name|value
range|:
name|streams
operator|.
name|values
control|)
block|{
name|byte
index|[]
name|readValue
init|=
operator|new
name|byte
index|[
name|value
operator|.
name|length
index|]
decl_stmt|;
name|readFully
argument_list|(
name|streams
operator|.
name|in
argument_list|,
name|readValue
argument_list|,
name|readValue
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|value
argument_list|,
name|readValue
argument_list|)
expr_stmt|;
block|}
name|allValuesRead
operator|=
literal|true
expr_stmt|;
comment|// Check that the output is done
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|streams
operator|.
name|in
operator|.
name|read
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|err
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|this
operator|.
name|error
operator|=
name|err
expr_stmt|;
block|}
block|}
block|}
specifier|static
class|class
name|MyFilterInputStream
extends|extends
name|FilterInputStream
block|{
specifier|public
name|MyFilterInputStream
parameter_list|(
name|InputStream
name|in
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Helper class to set up a ChunkedInput/Output stream for testing
specifier|static
class|class
name|TestStreams
block|{
name|PipedOutputStream
name|pout
decl_stmt|;
name|OutputStream
name|out
decl_stmt|;
name|PipedInputStream
name|pin
decl_stmt|;
name|InputStream
name|in
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|values
decl_stmt|;
specifier|public
name|TestStreams
parameter_list|(
name|boolean
name|useChunkedStream
parameter_list|)
throws|throws
name|Exception
block|{
name|pout
operator|=
operator|new
name|PipedOutputStream
argument_list|()
expr_stmt|;
name|pin
operator|=
operator|new
name|PipedInputStream
argument_list|(
name|pout
argument_list|)
expr_stmt|;
if|if
condition|(
name|useChunkedStream
condition|)
block|{
name|out
operator|=
operator|new
name|ChunkedOutputStream
argument_list|(
name|pout
argument_list|,
name|bufferSize
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|in
operator|=
operator|new
name|ChunkedInputStream
argument_list|(
name|pin
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Test behavior with non-chunked streams
name|out
operator|=
operator|new
name|FilterOutputStream
argument_list|(
name|pout
argument_list|)
expr_stmt|;
name|in
operator|=
operator|new
name|MyFilterInputStream
argument_list|(
name|pin
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|pout
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
comment|// ignore
block|}
try|try
block|{
name|pin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
specifier|static
name|void
name|runTest
parameter_list|(
name|Runnable
name|writer
parameter_list|,
name|Runnable
name|reader
parameter_list|,
name|TestStreams
name|streams
parameter_list|)
throws|throws
name|Exception
block|{
name|Thread
name|writerThread
init|=
operator|new
name|Thread
argument_list|(
name|writer
argument_list|)
decl_stmt|;
name|Thread
name|readerThread
init|=
operator|new
name|Thread
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|writerThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|readerThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|writerThread
operator|.
name|join
argument_list|()
expr_stmt|;
name|readerThread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicUsage
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|values
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|2
block|}
argument_list|,
name|RandomTypeUtil
operator|.
name|getRandString
argument_list|(
name|rand
argument_list|,
name|alphabet
argument_list|,
literal|99
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|,
name|RandomTypeUtil
operator|.
name|getRandString
argument_list|(
name|rand
argument_list|,
name|alphabet
argument_list|,
literal|1024
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
comment|// Try the basic test with non-chunked stream
name|TestStreams
name|nonChunkedStreams
init|=
operator|new
name|TestStreams
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|nonChunkedStreams
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|BasicUsageWriter
name|writer1
init|=
operator|new
name|BasicUsageWriter
argument_list|(
name|nonChunkedStreams
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|BasicUsageReader
name|reader1
init|=
operator|new
name|BasicUsageReader
argument_list|(
name|nonChunkedStreams
argument_list|)
decl_stmt|;
name|runTest
argument_list|(
name|writer1
argument_list|,
name|reader1
argument_list|,
name|nonChunkedStreams
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|reader1
operator|.
name|allValuesRead
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|writer1
operator|.
name|getError
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|reader1
operator|.
name|getError
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try with chunked streams
name|TestStreams
name|chunkedStreams
init|=
operator|new
name|TestStreams
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|chunkedStreams
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|BasicUsageWriter
name|writer2
init|=
operator|new
name|BasicUsageWriter
argument_list|(
name|chunkedStreams
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|BasicUsageReader
name|reader2
init|=
operator|new
name|BasicUsageReader
argument_list|(
name|chunkedStreams
argument_list|)
decl_stmt|;
name|runTest
argument_list|(
name|writer2
argument_list|,
name|reader2
argument_list|,
name|chunkedStreams
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|reader2
operator|.
name|allValuesRead
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
operator|(
name|ChunkedInputStream
operator|)
name|chunkedStreams
operator|.
name|in
operator|)
operator|.
name|isEndOfData
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|writer2
operator|.
name|getError
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|reader2
operator|.
name|getError
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAbruptlyClosedOutput
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|values
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|1
block|}
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|2
block|}
argument_list|,
name|RandomTypeUtil
operator|.
name|getRandString
argument_list|(
name|rand
argument_list|,
name|alphabet
argument_list|,
literal|99
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|,
name|RandomTypeUtil
operator|.
name|getRandString
argument_list|(
name|rand
argument_list|,
name|alphabet
argument_list|,
literal|1024
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
comment|// Close the PipedOutputStream before we close the outermost OutputStream.
comment|// Try non-chunked stream. There should be no issues assuming we flushed the streams before closing.
name|TestStreams
name|nonChunkedStreams
init|=
operator|new
name|TestStreams
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|nonChunkedStreams
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|BasicUsageWriter
name|writer1
init|=
operator|new
name|BasicUsageWriter
argument_list|(
name|nonChunkedStreams
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|BasicUsageReader
name|reader1
init|=
operator|new
name|BasicUsageReader
argument_list|(
name|nonChunkedStreams
argument_list|)
decl_stmt|;
name|runTest
argument_list|(
name|writer1
argument_list|,
name|reader1
argument_list|,
name|nonChunkedStreams
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|reader1
operator|.
name|allValuesRead
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|writer1
operator|.
name|getError
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|reader1
operator|.
name|getError
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try with chunked stream. Here the chunked output didn't get a chance to write the end-of-data
comment|// indicator, so the chunked input does not know to stop reading.
name|TestStreams
name|chunkedStreams
init|=
operator|new
name|TestStreams
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|chunkedStreams
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|BasicUsageWriter
name|writer2
init|=
operator|new
name|BasicUsageWriter
argument_list|(
name|chunkedStreams
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|BasicUsageReader
name|reader2
init|=
operator|new
name|BasicUsageReader
argument_list|(
name|chunkedStreams
argument_list|)
decl_stmt|;
name|runTest
argument_list|(
name|writer2
argument_list|,
name|reader2
argument_list|,
name|chunkedStreams
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|reader2
operator|.
name|allValuesRead
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
operator|(
operator|(
name|ChunkedInputStream
operator|)
name|chunkedStreams
operator|.
name|in
operator|)
operator|.
name|isEndOfData
argument_list|()
argument_list|)
expr_stmt|;
comment|// Closing the chunked output stream early gives an error
name|assertNotNull
argument_list|(
name|writer2
operator|.
name|getError
argument_list|()
argument_list|)
expr_stmt|;
comment|// In this case we should expect the test to have failed at the very last read() check.
name|assertNotNull
argument_list|(
name|reader2
operator|.
name|getError
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

