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
name|exec
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|conf
operator|.
name|Configuration
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
name|hive
operator|.
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_comment
comment|/**  * Read from a binary stream and treat each 1000 bytes (configurable via  * hive.binary.record.max.length) as a record.  The last record before the  * end of stream can have less than 1000 bytes.  */
end_comment

begin_class
specifier|public
class|class
name|BinaryRecordReader
implements|implements
name|RecordReader
block|{
specifier|private
name|InputStream
name|in
decl_stmt|;
specifier|private
name|BytesWritable
name|bytes
decl_stmt|;
specifier|private
name|int
name|maxRecordLength
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
name|maxRecordLength
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEBINARYRECORDMAX
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Writable
name|createRow
parameter_list|()
throws|throws
name|IOException
block|{
name|bytes
operator|=
operator|new
name|BytesWritable
argument_list|()
expr_stmt|;
name|bytes
operator|.
name|setCapacity
argument_list|(
name|maxRecordLength
argument_list|)
expr_stmt|;
return|return
name|bytes
return|;
block|}
specifier|public
name|int
name|next
parameter_list|(
name|Writable
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|recordLength
init|=
name|in
operator|.
name|read
argument_list|(
name|bytes
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|maxRecordLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|recordLength
operator|>=
literal|0
condition|)
block|{
name|bytes
operator|.
name|setSize
argument_list|(
name|recordLength
argument_list|)
expr_stmt|;
block|}
return|return
name|recordLength
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

