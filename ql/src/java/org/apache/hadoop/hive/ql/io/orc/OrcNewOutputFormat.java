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
name|ArrayList
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
name|io
operator|.
name|orc
operator|.
name|OrcSerde
operator|.
name|OrcSerdeRow
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
name|objectinspector
operator|.
name|ObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspectorFactory
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
name|shims
operator|.
name|ShimLoader
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
name|mapreduce
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|FileOutputFormat
import|;
end_import

begin_comment
comment|/** An OutputFormat that writes ORC files. */
end_comment

begin_class
specifier|public
class|class
name|OrcNewOutputFormat
extends|extends
name|FileOutputFormat
argument_list|<
name|NullWritable
argument_list|,
name|OrcSerdeRow
argument_list|>
block|{
specifier|private
specifier|static
class|class
name|OrcRecordWriter
extends|extends
name|RecordWriter
argument_list|<
name|NullWritable
argument_list|,
name|OrcSerdeRow
argument_list|>
block|{
specifier|private
name|Writer
name|writer
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Path
name|path
decl_stmt|;
specifier|private
specifier|final
name|OrcFile
operator|.
name|WriterOptions
name|options
decl_stmt|;
name|OrcRecordWriter
parameter_list|(
name|Path
name|path
parameter_list|,
name|OrcFile
operator|.
name|WriterOptions
name|options
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|options
operator|=
name|options
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|NullWritable
name|key
parameter_list|,
name|OrcSerdeRow
name|row
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
name|options
operator|.
name|inspector
argument_list|(
name|row
operator|.
name|getInspector
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|addRow
argument_list|(
name|row
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
comment|// a row with no columns
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|options
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
expr_stmt|;
name|writer
operator|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RecordWriter
name|getRecordWriter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Path
name|file
init|=
name|getDefaultWorkFile
argument_list|(
name|context
argument_list|,
literal|""
argument_list|)
decl_stmt|;
return|return
operator|new
name|OrcRecordWriter
argument_list|(
name|file
argument_list|,
name|OrcFile
operator|.
name|writerOptions
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getConfiguration
argument_list|(
name|context
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

