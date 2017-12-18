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
name|List
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
name|fs
operator|.
name|FileStatus
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
name|conf
operator|.
name|HiveConf
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
operator|.
name|ConfVars
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|VectorizedInputFormatInterface
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
name|vector
operator|.
name|VectorizedRowBatch
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
name|vector
operator|.
name|VectorizedRowBatchCtx
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
name|InputFormatChecker
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
name|SelfDescribingInputFormatInterface
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|OrcProto
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
name|OrcUtils
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
name|TypeDescription
import|;
end_import

begin_comment
comment|/**  * A MapReduce/Hive input format for ORC files.  */
end_comment

begin_class
specifier|public
class|class
name|VectorizedOrcInputFormat
extends|extends
name|FileInputFormat
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
implements|implements
name|InputFormatChecker
implements|,
name|VectorizedInputFormatInterface
implements|,
name|SelfDescribingInputFormatInterface
block|{
specifier|static
class|class
name|VectorizedOrcRecordReader
implements|implements
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
block|{
specifier|private
specifier|final
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
name|RecordReader
name|reader
decl_stmt|;
specifier|private
specifier|final
name|long
name|offset
decl_stmt|;
specifier|private
specifier|final
name|long
name|length
decl_stmt|;
specifier|private
name|float
name|progress
init|=
literal|0.0f
decl_stmt|;
specifier|private
name|VectorizedRowBatchCtx
name|rbCtx
decl_stmt|;
specifier|private
specifier|final
name|Object
index|[]
name|partitionValues
decl_stmt|;
specifier|private
name|boolean
name|addPartitionCols
init|=
literal|true
decl_stmt|;
name|VectorizedOrcRecordReader
parameter_list|(
name|Reader
name|file
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSplit
name|fileSplit
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|isAcidRead
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_ACID_TABLE_SCAN
argument_list|)
decl_stmt|;
if|if
condition|(
name|isAcidRead
condition|)
block|{
name|OrcInputFormat
operator|.
name|raiseAcidTablesMustBeReadWithAcidReaderException
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|rbCtx
operator|=
name|Utilities
operator|.
name|getVectorizedRowBatchCtx
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|/**        * Do we have schema on read in the configuration variables?        */
name|int
name|dataColumns
init|=
name|rbCtx
operator|.
name|getDataColumnCount
argument_list|()
decl_stmt|;
name|TypeDescription
name|schema
init|=
name|OrcInputFormat
operator|.
name|getDesiredRowTypeDescr
argument_list|(
name|conf
argument_list|,
literal|false
argument_list|,
name|dataColumns
argument_list|)
decl_stmt|;
if|if
condition|(
name|schema
operator|==
literal|null
condition|)
block|{
name|schema
operator|=
name|file
operator|.
name|getSchema
argument_list|()
expr_stmt|;
comment|// Even if the user isn't doing schema evolution, cut the schema
comment|// to the desired size.
if|if
condition|(
name|schema
operator|.
name|getCategory
argument_list|()
operator|==
name|TypeDescription
operator|.
name|Category
operator|.
name|STRUCT
operator|&&
name|schema
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
operator|>
name|dataColumns
condition|)
block|{
name|schema
operator|=
name|schema
operator|.
name|clone
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|children
init|=
name|schema
operator|.
name|getChildren
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
name|children
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|c
operator|>=
name|dataColumns
condition|;
operator|--
name|c
control|)
block|{
name|children
operator|.
name|remove
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
init|=
name|OrcUtils
operator|.
name|getOrcTypes
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|Reader
operator|.
name|Options
name|options
init|=
operator|new
name|Reader
operator|.
name|Options
argument_list|()
operator|.
name|schema
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|this
operator|.
name|offset
operator|=
name|fileSplit
operator|.
name|getStart
argument_list|()
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|fileSplit
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|options
operator|.
name|range
argument_list|(
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|options
operator|.
name|include
argument_list|(
name|OrcInputFormat
operator|.
name|genIncludedColumns
argument_list|(
name|schema
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|OrcInputFormat
operator|.
name|setSearchArgument
argument_list|(
name|options
argument_list|,
name|types
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|file
operator|.
name|rowsOptions
argument_list|(
name|options
argument_list|)
expr_stmt|;
name|int
name|partitionColumnCount
init|=
name|rbCtx
operator|.
name|getPartitionColumnCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitionColumnCount
operator|>
literal|0
condition|)
block|{
name|partitionValues
operator|=
operator|new
name|Object
index|[
name|partitionColumnCount
index|]
expr_stmt|;
name|rbCtx
operator|.
name|getPartitionValues
argument_list|(
name|rbCtx
argument_list|,
name|conf
argument_list|,
name|fileSplit
argument_list|,
name|partitionValues
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|partitionValues
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|NullWritable
name|key
parameter_list|,
name|VectorizedRowBatch
name|value
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
comment|// Check and update partition cols if necessary. Ideally, this should be done
comment|// in CreateValue as the partition is constant per split. But since Hive uses
comment|// CombineHiveRecordReader and
comment|// as this does not call CreateValue for each new RecordReader it creates, this check is
comment|// required in next()
if|if
condition|(
name|addPartitionCols
condition|)
block|{
if|if
condition|(
name|partitionValues
operator|!=
literal|null
condition|)
block|{
name|rbCtx
operator|.
name|addPartitionColsToBatch
argument_list|(
name|value
argument_list|,
name|partitionValues
argument_list|)
expr_stmt|;
block|}
name|addPartitionCols
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|reader
operator|.
name|nextBatch
argument_list|(
name|value
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
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
name|progress
operator|=
name|reader
operator|.
name|getProgress
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|createKey
parameter_list|()
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizedRowBatch
name|createValue
parameter_list|()
block|{
return|return
name|rbCtx
operator|.
name|createVectorizedRowBatch
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
name|offset
operator|+
call|(
name|long
call|)
argument_list|(
name|progress
operator|*
name|length
argument_list|)
return|;
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
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|progress
return|;
block|}
block|}
specifier|public
name|VectorizedOrcInputFormat
parameter_list|()
block|{
comment|// just set a really small lower bound
name|setMinSplitSize
argument_list|(
literal|16
operator|*
literal|1024
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|inputSplit
parameter_list|,
name|JobConf
name|conf
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSplit
name|fSplit
init|=
operator|(
name|FileSplit
operator|)
name|inputSplit
decl_stmt|;
name|reporter
operator|.
name|setStatus
argument_list|(
name|fSplit
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
name|fSplit
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|OrcFile
operator|.
name|ReaderOptions
name|opts
init|=
name|OrcFile
operator|.
name|readerOptions
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|fSplit
operator|instanceof
name|OrcSplit
condition|)
block|{
name|OrcSplit
name|orcSplit
init|=
operator|(
name|OrcSplit
operator|)
name|fSplit
decl_stmt|;
if|if
condition|(
name|orcSplit
operator|.
name|hasFooter
argument_list|()
condition|)
block|{
name|opts
operator|.
name|orcTail
argument_list|(
name|orcSplit
operator|.
name|getOrcTail
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|opts
operator|.
name|maxLength
argument_list|(
name|orcSplit
operator|.
name|getFileLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Reader
name|reader
init|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|path
argument_list|,
name|opts
argument_list|)
decl_stmt|;
return|return
operator|new
name|VectorizedOrcRecordReader
argument_list|(
name|reader
argument_list|,
name|conf
argument_list|,
name|fSplit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|validateInput
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|files
operator|.
name|size
argument_list|()
operator|<=
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
try|try
block|{
name|OrcFile
operator|.
name|createReader
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
name|OrcFile
operator|.
name|readerOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|filesystem
argument_list|(
name|fs
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

