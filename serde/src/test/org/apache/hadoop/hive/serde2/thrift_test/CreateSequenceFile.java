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
name|serde2
operator|.
name|thrift_test
package|;
end_package

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
name|HashMap
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|serde2
operator|.
name|ByteStream
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
name|serde2
operator|.
name|thrift
operator|.
name|test
operator|.
name|Complex
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
name|serde2
operator|.
name|thrift
operator|.
name|test
operator|.
name|IntString
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
name|BytesWritable
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
name|SequenceFile
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
name|Writable
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TIOStreamTransport
import|;
end_import

begin_comment
comment|/**  * CreateSequenceFile.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|CreateSequenceFile
block|{
specifier|private
name|CreateSequenceFile
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|public
specifier|static
name|void
name|usage
parameter_list|()
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage: CreateSequenceFile<output_sequencefile>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * ThriftSerializer.    *    */
specifier|public
specifier|static
class|class
name|ThriftSerializer
block|{
specifier|private
name|ByteStream
operator|.
name|Output
name|bos
decl_stmt|;
specifier|private
name|TProtocol
name|outProtocol
decl_stmt|;
specifier|public
name|ThriftSerializer
parameter_list|()
block|{
name|bos
operator|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
expr_stmt|;
name|TIOStreamTransport
name|outTransport
init|=
operator|new
name|TIOStreamTransport
argument_list|(
name|bos
argument_list|)
decl_stmt|;
name|TProtocolFactory
name|outFactory
init|=
operator|new
name|TBinaryProtocol
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|outProtocol
operator|=
name|outFactory
operator|.
name|getProtocol
argument_list|(
name|outTransport
argument_list|)
expr_stmt|;
block|}
specifier|private
name|BytesWritable
name|bw
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
specifier|public
name|BytesWritable
name|serialize
parameter_list|(
name|TBase
name|base
parameter_list|)
throws|throws
name|TException
block|{
name|bos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|base
operator|.
name|write
argument_list|(
name|outProtocol
argument_list|)
expr_stmt|;
name|bw
operator|.
name|set
argument_list|(
name|bos
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bos
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|bw
return|;
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Read parameters
name|int
name|lines
init|=
literal|10
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|extraArgs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|ai
init|=
literal|0
init|;
name|ai
operator|<
name|args
operator|.
name|length
condition|;
name|ai
operator|++
control|)
block|{
if|if
condition|(
name|args
index|[
name|ai
index|]
operator|.
name|equals
argument_list|(
literal|"-line"
argument_list|)
operator|&&
name|ai
operator|+
literal|1
operator|<
name|args
operator|.
name|length
condition|)
block|{
name|lines
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
name|ai
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
name|ai
operator|++
expr_stmt|;
block|}
else|else
block|{
name|extraArgs
operator|.
name|add
argument_list|(
name|args
index|[
name|ai
index|]
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|extraArgs
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|usage
argument_list|()
expr_stmt|;
block|}
name|JobConf
name|conf
init|=
operator|new
name|JobConf
argument_list|(
name|CreateSequenceFile
operator|.
name|class
argument_list|)
decl_stmt|;
name|ThriftSerializer
name|serializer
init|=
operator|new
name|ThriftSerializer
argument_list|()
decl_stmt|;
comment|// Open files
name|SequenceFile
operator|.
name|Writer
name|writer
init|=
operator|new
name|SequenceFile
operator|.
name|Writer
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|,
name|conf
argument_list|,
operator|new
name|Path
argument_list|(
name|extraArgs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
name|BytesWritable
operator|.
name|class
argument_list|,
name|BytesWritable
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// write to file
name|BytesWritable
name|key
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|20081215
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
name|lines
condition|;
name|i
operator|++
control|)
block|{
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|alist
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|alist
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|alist
operator|.
name|add
argument_list|(
name|i
operator|*
literal|2
argument_list|)
expr_stmt|;
name|alist
operator|.
name|add
argument_list|(
name|i
operator|*
literal|3
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|slist
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|slist
operator|.
name|add
argument_list|(
literal|""
operator|+
name|i
operator|*
literal|10
argument_list|)
expr_stmt|;
name|slist
operator|.
name|add
argument_list|(
literal|""
operator|+
name|i
operator|*
literal|100
argument_list|)
expr_stmt|;
name|slist
operator|.
name|add
argument_list|(
literal|""
operator|+
name|i
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|IntString
argument_list|>
name|islist
init|=
operator|new
name|ArrayList
argument_list|<
name|IntString
argument_list|>
argument_list|()
decl_stmt|;
name|islist
operator|.
name|add
argument_list|(
operator|new
name|IntString
argument_list|(
name|i
operator|*
name|i
argument_list|,
literal|""
operator|+
name|i
operator|*
name|i
operator|*
name|i
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hash
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|hash
operator|.
name|put
argument_list|(
literal|"key_"
operator|+
name|i
argument_list|,
literal|"value_"
operator|+
name|i
argument_list|)
expr_stmt|;
name|Complex
name|complex
init|=
operator|new
name|Complex
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|,
literal|"record_"
operator|+
operator|(
operator|new
name|Integer
argument_list|(
name|i
argument_list|)
operator|)
operator|.
name|toString
argument_list|()
argument_list|,
name|alist
argument_list|,
name|slist
argument_list|,
name|islist
argument_list|,
name|hash
argument_list|)
decl_stmt|;
name|Writable
name|value
init|=
name|serializer
operator|.
name|serialize
argument_list|(
name|complex
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|// Add an all-null record
name|Complex
name|complex
init|=
operator|new
name|Complex
argument_list|(
literal|0
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Writable
name|value
init|=
name|serializer
operator|.
name|serialize
argument_list|(
name|complex
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// Close files
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

