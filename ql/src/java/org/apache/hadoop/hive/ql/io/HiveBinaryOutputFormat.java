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
name|OutputStream
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
name|io
operator|.
name|WritableComparable
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
name|TextOutputFormat
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
name|util
operator|.
name|Progressable
import|;
end_import

begin_comment
comment|/**  * HiveBinaryOutputFormat writes out the values consecutively without any  * separators.  It can be used to create a binary data file.  */
end_comment

begin_class
specifier|public
class|class
name|HiveBinaryOutputFormat
parameter_list|<
name|K
extends|extends
name|WritableComparable
parameter_list|,
name|V
extends|extends
name|Writable
parameter_list|>
extends|extends
name|TextOutputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
implements|implements
name|HiveOutputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
comment|/**    * create the final out file, and output row by row. After one row is    * appended, a configured row separator is appended    *    * @param jc    *          the job configuration file    * @param outPath    *          the final output file to be created    * @param valueClass    *          the value class used for create    * @param isCompressed    *          ignored. Currently we don't support compression.    * @param tableProperties    *          the tableProperties of this file's corresponding table    * @param progress    *          progress used for status report    * @return the RecordWriter    */
annotation|@
name|Override
specifier|public
name|FSRecordWriter
name|getHiveRecordWriter
parameter_list|(
name|JobConf
name|jc
parameter_list|,
name|Path
name|outPath
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|valueClass
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|Properties
name|tableProperties
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|outPath
operator|.
name|getFileSystem
argument_list|(
name|jc
argument_list|)
decl_stmt|;
specifier|final
name|OutputStream
name|outStream
init|=
name|fs
operator|.
name|create
argument_list|(
name|outPath
argument_list|)
decl_stmt|;
return|return
operator|new
name|FSRecordWriter
argument_list|()
block|{
specifier|public
name|void
name|write
parameter_list|(
name|Writable
name|r
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|r
operator|instanceof
name|Text
condition|)
block|{
name|Text
name|tr
init|=
operator|(
name|Text
operator|)
name|r
decl_stmt|;
name|outStream
operator|.
name|write
argument_list|(
name|tr
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|tr
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// DynamicSerDe always writes out BytesWritable
name|BytesWritable
name|bw
init|=
operator|(
name|BytesWritable
operator|)
name|r
decl_stmt|;
name|outStream
operator|.
name|write
argument_list|(
name|bw
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bw
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|IOException
block|{
name|outStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

