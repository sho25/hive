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
name|contrib
operator|.
name|mr
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|InputStreamReader
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
name|OutputStreamWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Writer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_comment
comment|/**  * This class attempts to provide a simple framework for writing Hive map/reduce  * tasks in java.  *   * The main benefit is that it deals with grouping the keys together for reduce  * tasks.  *   * Additionally, it deals with all system io... and provides something closer to  * the hadoop m/r.  *   * As an example, here's the wordcount reduce:  *   * new GenericMR().reduce(System.in, System.out, new Reducer() { public void  * reduce(String key, Iterator&lt;String[]&gt; records, Output output) throws  * Exception { int count = 0;  *   * while (records.hasNext()) { count += Integer.parseInt(records.next()[1]); }  *   * output.collect(new String[] { key, String.valueOf(count) }); }});  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|GenericMR
block|{
specifier|public
name|void
name|map
parameter_list|(
specifier|final
name|InputStream
name|in
parameter_list|,
specifier|final
name|OutputStream
name|out
parameter_list|,
specifier|final
name|Mapper
name|mapper
parameter_list|)
throws|throws
name|Exception
block|{
name|map
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|in
argument_list|)
argument_list|,
operator|new
name|OutputStreamWriter
argument_list|(
name|out
argument_list|)
argument_list|,
name|mapper
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|map
parameter_list|(
specifier|final
name|Reader
name|in
parameter_list|,
specifier|final
name|Writer
name|out
parameter_list|,
specifier|final
name|Mapper
name|mapper
parameter_list|)
throws|throws
name|Exception
block|{
name|handle
argument_list|(
name|in
argument_list|,
name|out
argument_list|,
operator|new
name|RecordProcessor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|processNext
parameter_list|(
name|RecordReader
name|reader
parameter_list|,
name|Output
name|output
parameter_list|)
throws|throws
name|Exception
block|{
name|mapper
operator|.
name|map
argument_list|(
name|reader
operator|.
name|next
argument_list|()
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|reduce
parameter_list|(
specifier|final
name|InputStream
name|in
parameter_list|,
specifier|final
name|OutputStream
name|out
parameter_list|,
specifier|final
name|Reducer
name|reducer
parameter_list|)
throws|throws
name|Exception
block|{
name|reduce
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|in
argument_list|)
argument_list|,
operator|new
name|OutputStreamWriter
argument_list|(
name|out
argument_list|)
argument_list|,
name|reducer
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|reduce
parameter_list|(
specifier|final
name|Reader
name|in
parameter_list|,
specifier|final
name|Writer
name|out
parameter_list|,
specifier|final
name|Reducer
name|reducer
parameter_list|)
throws|throws
name|Exception
block|{
name|handle
argument_list|(
name|in
argument_list|,
name|out
argument_list|,
operator|new
name|RecordProcessor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|processNext
parameter_list|(
name|RecordReader
name|reader
parameter_list|,
name|Output
name|output
parameter_list|)
throws|throws
name|Exception
block|{
name|reducer
operator|.
name|reduce
argument_list|(
name|reader
operator|.
name|peek
argument_list|()
index|[
literal|0
index|]
argument_list|,
operator|new
name|KeyRecordIterator
argument_list|(
name|reader
operator|.
name|peek
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|reader
argument_list|)
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|handle
parameter_list|(
specifier|final
name|Reader
name|in
parameter_list|,
specifier|final
name|Writer
name|out
parameter_list|,
specifier|final
name|RecordProcessor
name|processor
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|RecordReader
name|reader
init|=
operator|new
name|RecordReader
argument_list|(
name|in
argument_list|)
decl_stmt|;
specifier|final
name|OutputStreamOutput
name|output
init|=
operator|new
name|OutputStreamOutput
argument_list|(
name|out
argument_list|)
decl_stmt|;
try|try
block|{
while|while
condition|(
name|reader
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|processor
operator|.
name|processNext
argument_list|(
name|reader
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
try|try
block|{
name|output
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
interface|interface
name|RecordProcessor
block|{
name|void
name|processNext
parameter_list|(
specifier|final
name|RecordReader
name|reader
parameter_list|,
specifier|final
name|Output
name|output
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|KeyRecordIterator
implements|implements
name|Iterator
argument_list|<
name|String
index|[]
argument_list|>
block|{
specifier|private
specifier|final
name|String
name|key
decl_stmt|;
specifier|private
specifier|final
name|RecordReader
name|reader
decl_stmt|;
specifier|private
name|KeyRecordIterator
parameter_list|(
specifier|final
name|String
name|key
parameter_list|,
specifier|final
name|RecordReader
name|reader
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
operator|(
name|reader
operator|.
name|hasNext
argument_list|()
operator|&&
name|key
operator|.
name|equals
argument_list|(
name|reader
operator|.
name|peek
argument_list|()
index|[
literal|0
index|]
argument_list|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
return|return
name|reader
operator|.
name|next
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|RecordReader
block|{
specifier|private
specifier|final
name|BufferedReader
name|reader
decl_stmt|;
specifier|private
name|String
index|[]
name|next
decl_stmt|;
specifier|private
name|RecordReader
parameter_list|(
specifier|final
name|InputStream
name|in
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|RecordReader
parameter_list|(
specifier|final
name|Reader
name|in
parameter_list|)
block|{
name|reader
operator|=
operator|new
name|BufferedReader
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|next
operator|=
name|readNext
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
index|[]
name|next
parameter_list|()
block|{
specifier|final
name|String
index|[]
name|ret
init|=
name|next
decl_stmt|;
name|next
operator|=
name|readNext
argument_list|()
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|private
name|String
index|[]
name|readNext
parameter_list|()
block|{
try|try
block|{
specifier|final
name|String
name|line
init|=
name|reader
operator|.
name|readLine
argument_list|()
decl_stmt|;
return|return
operator|(
name|line
operator|==
literal|null
condition|?
literal|null
else|:
name|line
operator|.
name|split
argument_list|(
literal|"\t"
argument_list|)
operator|)
return|;
block|}
catch|catch
parameter_list|(
specifier|final
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|next
operator|!=
literal|null
return|;
block|}
specifier|private
name|String
index|[]
name|peek
parameter_list|()
block|{
return|return
name|next
return|;
block|}
specifier|private
name|void
name|close
parameter_list|()
throws|throws
name|Exception
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|OutputStreamOutput
implements|implements
name|Output
block|{
specifier|private
specifier|final
name|PrintWriter
name|out
decl_stmt|;
specifier|private
name|OutputStreamOutput
parameter_list|(
specifier|final
name|OutputStream
name|out
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|OutputStreamWriter
argument_list|(
name|out
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|OutputStreamOutput
parameter_list|(
specifier|final
name|Writer
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
operator|new
name|PrintWriter
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|Exception
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|String
index|[]
name|record
parameter_list|)
throws|throws
name|Exception
block|{
name|out
operator|.
name|println
argument_list|(
name|_join
argument_list|(
name|record
argument_list|,
literal|"\t"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|_join
parameter_list|(
specifier|final
name|String
index|[]
name|record
parameter_list|,
specifier|final
name|String
name|separator
parameter_list|)
block|{
if|if
condition|(
name|record
operator|==
literal|null
operator|||
name|record
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|""
return|;
block|}
specifier|final
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|record
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|record
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

