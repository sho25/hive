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
name|ArrayList
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
name|serde2
operator|.
name|ColumnProjectionUtils
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
comment|/**  * A MapReduce/Hive input format for ORC files.  */
end_comment

begin_class
specifier|public
class|class
name|OrcInputFormat
extends|extends
name|FileInputFormat
argument_list|<
name|NullWritable
argument_list|,
name|OrcStruct
argument_list|>
implements|implements
name|InputFormatChecker
implements|,
name|VectorizedInputFormatInterface
block|{
name|VectorizedOrcInputFormat
name|voif
init|=
operator|new
name|VectorizedOrcInputFormat
argument_list|()
decl_stmt|;
specifier|private
specifier|static
class|class
name|OrcRecordReader
implements|implements
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|OrcStruct
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
specifier|final
name|int
name|numColumns
decl_stmt|;
specifier|private
name|float
name|progress
init|=
literal|0.0f
decl_stmt|;
name|OrcRecordReader
parameter_list|(
name|Reader
name|file
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|long
name|offset
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|reader
operator|=
name|file
operator|.
name|rows
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|findIncludedColumns
argument_list|(
name|file
operator|.
name|getTypes
argument_list|()
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
init|=
name|file
operator|.
name|getTypes
argument_list|()
decl_stmt|;
if|if
condition|(
name|types
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|numColumns
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|numColumns
operator|=
name|types
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSubtypesCount
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
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
name|OrcStruct
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|reader
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|reader
operator|.
name|next
argument_list|(
name|value
argument_list|)
expr_stmt|;
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
else|else
block|{
return|return
literal|false
return|;
block|}
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
name|OrcStruct
name|createValue
parameter_list|()
block|{
return|return
operator|new
name|OrcStruct
argument_list|(
name|numColumns
argument_list|)
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
name|OrcInputFormat
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
comment|/**    * Recurse down into a type subtree turning on all of the sub-columns.    * @param types the types of the file    * @param result the global view of columns that should be included    * @param typeId the root of tree to enable    */
specifier|private
specifier|static
name|void
name|includeColumnRecursive
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
parameter_list|,
name|boolean
index|[]
name|result
parameter_list|,
name|int
name|typeId
parameter_list|)
block|{
name|result
index|[
name|typeId
index|]
operator|=
literal|true
expr_stmt|;
name|OrcProto
operator|.
name|Type
name|type
init|=
name|types
operator|.
name|get
argument_list|(
name|typeId
argument_list|)
decl_stmt|;
name|int
name|children
init|=
name|type
operator|.
name|getSubtypesCount
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
name|children
condition|;
operator|++
name|i
control|)
block|{
name|includeColumnRecursive
argument_list|(
name|types
argument_list|,
name|result
argument_list|,
name|type
operator|.
name|getSubtypes
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Take the configuration and figure out which columns we need to include.    * @param types the types of the file    * @param conf the configuration    * @return true for each column that should be included    */
specifier|private
specifier|static
name|boolean
index|[]
name|findIncludedColumns
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|includedStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|ColumnProjectionUtils
operator|.
name|READ_COLUMN_IDS_CONF_STR
argument_list|)
decl_stmt|;
if|if
condition|(
name|includedStr
operator|==
literal|null
operator|||
name|includedStr
operator|.
name|trim
argument_list|()
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|int
name|numColumns
init|=
name|types
operator|.
name|size
argument_list|()
decl_stmt|;
name|boolean
index|[]
name|result
init|=
operator|new
name|boolean
index|[
name|numColumns
index|]
decl_stmt|;
name|result
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|OrcProto
operator|.
name|Type
name|root
init|=
name|types
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|included
init|=
name|ColumnProjectionUtils
operator|.
name|getReadColumnIDs
argument_list|(
name|conf
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
name|root
operator|.
name|getSubtypesCount
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|included
operator|.
name|contains
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|includeColumnRecursive
argument_list|(
name|types
argument_list|,
name|result
argument_list|,
name|root
operator|.
name|getSubtypes
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// if we are filtering at least one column, return the boolean array
for|for
control|(
name|boolean
name|include
range|:
name|result
control|)
block|{
if|if
condition|(
operator|!
name|include
condition|)
block|{
return|return
name|result
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|OrcStruct
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
name|boolean
name|vectorPath
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_ENABLED
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|vectorPath
condition|)
block|{
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|vorr
init|=
name|voif
operator|.
name|getRecordReader
argument_list|(
name|inputSplit
argument_list|,
name|conf
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
return|return
operator|(
name|RecordReader
operator|)
name|vorr
return|;
block|}
name|FileSplit
name|fileSplit
init|=
operator|(
name|FileSplit
operator|)
name|inputSplit
decl_stmt|;
name|Path
name|path
init|=
name|fileSplit
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|reporter
operator|.
name|setStatus
argument_list|(
name|fileSplit
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|OrcRecordReader
argument_list|(
name|OrcFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
argument_list|,
name|conf
argument_list|,
name|fileSplit
operator|.
name|getStart
argument_list|()
argument_list|,
name|fileSplit
operator|.
name|getLength
argument_list|()
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
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|vectorPath
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_ENABLED
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|vectorPath
condition|)
block|{
return|return
name|voif
operator|.
name|validateInput
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|files
argument_list|)
return|;
block|}
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
name|fs
argument_list|,
name|file
operator|.
name|getPath
argument_list|()
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

