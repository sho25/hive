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
name|NullWritable
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
name|FileInputFormat
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

begin_comment
comment|/**  * https://cwiki.apache.org/confluence/display/Hive/TeradataBinarySerde.  * FileInputFormat for Teradata binary files.  *  * In the Teradata Binary File, each record constructs as below:  * The first 2 bytes represents the length of the bytes next for this record.  * Then the null bitmap whose length is depended on the number of fields is followed.  * Then each field of the record is serialized into bytes - the serialization strategy is decided by the type of field.  * At last, there is one byte (0x0a) in the end of the record.  *  * This InputFormat currently doesn't support the split of the file.  * Teradata binary files are using little endian.  */
end_comment

begin_class
specifier|public
class|class
name|TeradataBinaryFileInputFormat
extends|extends
name|FileInputFormat
argument_list|<
name|NullWritable
argument_list|,
name|BytesWritable
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|BytesWritable
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
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
name|split
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|TeradataBinaryRecordReader
argument_list|(
name|job
argument_list|,
operator|(
name|FileSplit
operator|)
name|split
argument_list|)
return|;
block|}
comment|/**    * the<code>TeradataBinaryFileInputFormat</code> is not splittable right now.    * Override the<code>isSplitable</code> function.    *    * @param fs the file system that the file is on    * @param filename the file name to check    * @return is this file splitable?    */
annotation|@
name|Override
specifier|protected
name|boolean
name|isSplitable
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|filename
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

