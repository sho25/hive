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
name|fileformat
operator|.
name|base64
package|;
end_package

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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|binary
operator|.
name|Base64
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
name|LongWritable
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
name|Text
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
name|FileSplit
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
name|InputFormat
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
name|InputSplit
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
name|hadoop
operator|.
name|mapred
operator|.
name|JobConfigurable
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
name|LineRecordReader
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
name|RecordReader
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
name|Reporter
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
name|TextInputFormat
import|;
end_import

begin_comment
comment|/**  * FileInputFormat for base64 encoded text files.  *   * Each line is a base64-encoded record. The key is a LongWritable which is the  * offset. The value is a BytesWritable containing the base64-decoded bytes.  *   * This class accepts a configurable parameter:  * "base64.text.input.format.signature"  *   * The UTF-8 encoded signature will be compared with the beginning of each  * decoded bytes. If they don't match, the record is discarded. If they match,  * the signature is stripped off the data.  */
end_comment

begin_class
specifier|public
class|class
name|Base64TextInputFormat
implements|implements
name|InputFormat
argument_list|<
name|LongWritable
argument_list|,
name|BytesWritable
argument_list|>
implements|,
name|JobConfigurable
block|{
comment|/**    * Base64LineRecordReader.    *    */
specifier|public
specifier|static
class|class
name|Base64LineRecordReader
implements|implements
name|RecordReader
argument_list|<
name|LongWritable
argument_list|,
name|BytesWritable
argument_list|>
implements|,
name|JobConfigurable
block|{
name|LineRecordReader
name|reader
decl_stmt|;
name|Text
name|text
decl_stmt|;
specifier|public
name|Base64LineRecordReader
parameter_list|(
name|LineRecordReader
name|reader
parameter_list|)
block|{
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|text
operator|=
name|reader
operator|.
name|createValue
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|LongWritable
name|createKey
parameter_list|()
block|{
return|return
name|reader
operator|.
name|createKey
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|BytesWritable
name|createValue
parameter_list|()
block|{
return|return
operator|new
name|BytesWritable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|reader
operator|.
name|getPos
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|reader
operator|.
name|getProgress
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|LongWritable
name|key
parameter_list|,
name|BytesWritable
name|value
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|text
argument_list|)
condition|)
block|{
comment|// text -> byte[] -> value
name|byte
index|[]
name|textBytes
init|=
name|text
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|text
operator|.
name|getLength
argument_list|()
decl_stmt|;
comment|// Trim additional bytes
if|if
condition|(
name|length
operator|!=
name|textBytes
operator|.
name|length
condition|)
block|{
name|textBytes
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|textBytes
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|binaryData
init|=
name|base64
operator|.
name|decode
argument_list|(
name|textBytes
argument_list|)
decl_stmt|;
comment|// compare data header with signature
name|int
name|i
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|binaryData
operator|.
name|length
operator|&&
name|i
operator|<
name|signature
operator|.
name|length
operator|&&
name|binaryData
index|[
name|i
index|]
operator|==
name|signature
index|[
name|i
index|]
condition|;
operator|++
name|i
control|)
block|{
empty_stmt|;
block|}
comment|// return the row only if it's not corrupted
if|if
condition|(
name|i
operator|==
name|signature
operator|.
name|length
condition|)
block|{
name|value
operator|.
name|set
argument_list|(
name|binaryData
argument_list|,
name|signature
operator|.
name|length
argument_list|,
name|binaryData
operator|.
name|length
operator|-
name|signature
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
comment|// no more data
return|return
literal|false
return|;
block|}
specifier|private
name|byte
index|[]
name|signature
decl_stmt|;
specifier|private
specifier|final
name|Base64
name|base64
init|=
name|createBase64
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|String
name|signatureString
init|=
name|job
operator|.
name|get
argument_list|(
literal|"base64.text.input.format.signature"
argument_list|)
decl_stmt|;
if|if
condition|(
name|signatureString
operator|!=
literal|null
condition|)
block|{
name|signature
operator|=
name|signatureString
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|signature
operator|=
operator|new
name|byte
index|[
literal|0
index|]
expr_stmt|;
block|}
block|}
block|}
name|TextInputFormat
name|format
decl_stmt|;
name|JobConf
name|job
decl_stmt|;
specifier|public
name|Base64TextInputFormat
parameter_list|()
block|{
name|format
operator|=
operator|new
name|TextInputFormat
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|this
operator|.
name|job
operator|=
name|job
expr_stmt|;
name|format
operator|.
name|configure
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RecordReader
argument_list|<
name|LongWritable
argument_list|,
name|BytesWritable
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|genericSplit
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|reporter
operator|.
name|setStatus
argument_list|(
name|genericSplit
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Base64LineRecordReader
name|reader
init|=
operator|new
name|Base64LineRecordReader
argument_list|(
operator|new
name|LineRecordReader
argument_list|(
name|job
argument_list|,
operator|(
name|FileSplit
operator|)
name|genericSplit
argument_list|)
argument_list|)
decl_stmt|;
name|reader
operator|.
name|configure
argument_list|(
name|job
argument_list|)
expr_stmt|;
return|return
name|reader
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|format
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
name|numSplits
argument_list|)
return|;
block|}
comment|/**    * Workaround an incompatible change from commons-codec 1.3 to 1.4.    * Since Hadoop has this jar on its classpath, we have no way of knowing    * which version we are running against.    */
specifier|static
name|Base64
name|createBase64
parameter_list|()
block|{
try|try
block|{
comment|// This constructor appeared in 1.4 and specifies that we do not want to
comment|// line-wrap or use any newline separator
name|Constructor
argument_list|<
name|Base64
argument_list|>
name|ctor
init|=
name|Base64
operator|.
name|class
operator|.
name|getConstructor
argument_list|(
name|int
operator|.
name|class
argument_list|,
name|byte
index|[]
operator|.
expr|class
argument_list|)
decl_stmt|;
return|return
name|ctor
operator|.
name|newInstance
argument_list|(
literal|0
argument_list|,
literal|null
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
comment|// ie we are running 1.3
comment|// In 1.3, this constructor has the same behavior, but in 1.4 the default
comment|// was changed to add wrapping and newlines.
return|return
operator|new
name|Base64
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
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
catch|catch
parameter_list|(
name|IllegalAccessException
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
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

