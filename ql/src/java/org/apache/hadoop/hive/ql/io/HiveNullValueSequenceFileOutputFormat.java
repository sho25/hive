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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|FileSinkOperator
operator|.
name|RecordWriter
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
name|ql
operator|.
name|exec
operator|.
name|Utilities
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
name|SequenceFileOutputFormat
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
comment|/**  * A {@link HiveOutputFormat} that writes {@link SequenceFile}s with the  * content saved in the keys, and null in the values.  */
end_comment

begin_class
specifier|public
class|class
name|HiveNullValueSequenceFileOutputFormat
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
name|SequenceFileOutputFormat
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
specifier|private
specifier|static
specifier|final
name|Writable
name|NULL_WRITABLE
init|=
name|NullWritable
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|private
name|HiveKey
name|keyWritable
decl_stmt|;
specifier|private
name|boolean
name|keyIsText
decl_stmt|;
annotation|@
name|Override
specifier|public
name|RecordWriter
name|getHiveRecordWriter
parameter_list|(
name|JobConf
name|jc
parameter_list|,
name|Path
name|finalOutPath
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
name|finalOutPath
operator|.
name|getFileSystem
argument_list|(
name|jc
argument_list|)
decl_stmt|;
specifier|final
name|SequenceFile
operator|.
name|Writer
name|outStream
init|=
name|Utilities
operator|.
name|createSequenceWriter
argument_list|(
name|jc
argument_list|,
name|fs
argument_list|,
name|finalOutPath
argument_list|,
name|HiveKey
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|,
name|isCompressed
argument_list|,
name|progress
argument_list|)
decl_stmt|;
name|keyWritable
operator|=
operator|new
name|HiveKey
argument_list|()
expr_stmt|;
name|keyIsText
operator|=
name|valueClass
operator|.
name|equals
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
operator|new
name|RecordWriter
argument_list|()
block|{
annotation|@
name|Override
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
name|keyIsText
condition|)
block|{
name|Text
name|text
init|=
operator|(
name|Text
operator|)
name|r
decl_stmt|;
name|keyWritable
operator|.
name|set
argument_list|(
name|text
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|text
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|BytesWritable
name|bw
init|=
operator|(
name|BytesWritable
operator|)
name|r
decl_stmt|;
comment|// Once we drop support for old Hadoop versions, change these
comment|// to getBytes() and getLength() to fix the deprecation warnings.
comment|// Not worth a shim.
name|keyWritable
operator|.
name|set
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
name|keyWritable
operator|.
name|setHashCode
argument_list|(
name|r
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|append
argument_list|(
name|keyWritable
argument_list|,
name|NULL_WRITABLE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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

