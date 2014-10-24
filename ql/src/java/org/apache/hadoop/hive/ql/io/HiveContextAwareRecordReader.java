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
name|java
operator|.
name|util
operator|.
name|Map
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|io
operator|.
name|HiveIOExceptionHandlerUtil
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
name|FooterBuffer
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
name|spark
operator|.
name|SparkUtilities
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
name|IOContext
operator|.
name|Comparison
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
name|plan
operator|.
name|PartitionDesc
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
name|plan
operator|.
name|TableDesc
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPEqual
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPEqualOrGreaterThan
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPEqualOrLessThan
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPGreaterThan
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPLessThan
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

begin_comment
comment|/** This class prepares an IOContext, and provides the ability to perform a binary search on the   * data.  The binary search can be used by setting the value of inputFormatSorted in the   * MapreduceWork to true, but it should only be used if the data is going to a FilterOperator,   * which filters by comparing a value in the data with a constant, using one of the comparisons   * =,<,>,<=,>=.  If the RecordReader's underlying format is an RCFile, this object can perform   * a binary search to find the block to begin reading from, and stop reading once it can be   * determined no other entries will match the filter.   */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HiveContextAwareRecordReader
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveContextAwareRecordReader
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|initDone
init|=
literal|false
decl_stmt|;
specifier|private
name|long
name|rangeStart
decl_stmt|;
specifier|private
name|long
name|rangeEnd
decl_stmt|;
specifier|private
name|long
name|splitEnd
decl_stmt|;
specifier|private
name|long
name|previousPosition
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|boolean
name|wasUsingSortedSearch
init|=
literal|false
decl_stmt|;
specifier|private
name|String
name|genericUDFClassName
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Comparison
argument_list|>
name|stopComparisons
init|=
operator|new
name|ArrayList
argument_list|<
name|Comparison
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
name|RecordReader
name|recordReader
decl_stmt|;
specifier|protected
name|JobConf
name|jobConf
decl_stmt|;
specifier|protected
name|boolean
name|isSorted
init|=
literal|false
decl_stmt|;
specifier|public
name|HiveContextAwareRecordReader
parameter_list|(
name|JobConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveContextAwareRecordReader
parameter_list|(
name|RecordReader
name|recordReader
parameter_list|)
block|{
name|this
operator|.
name|recordReader
operator|=
name|recordReader
expr_stmt|;
block|}
specifier|public
name|HiveContextAwareRecordReader
parameter_list|(
name|RecordReader
name|recordReader
parameter_list|,
name|JobConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|recordReader
operator|=
name|recordReader
expr_stmt|;
name|this
operator|.
name|jobConf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|void
name|setRecordReader
parameter_list|(
name|RecordReader
name|recordReader
parameter_list|)
block|{
name|this
operator|.
name|recordReader
operator|=
name|recordReader
expr_stmt|;
block|}
comment|/**    * Close this {@link InputSplit} to future operations.    *    * @throws IOException    */
specifier|public
specifier|abstract
name|void
name|doClose
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|private
name|IOContext
name|ioCxtRef
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|doClose
argument_list|()
expr_stmt|;
name|initDone
operator|=
literal|false
expr_stmt|;
name|ioCxtRef
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|initDone
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Hive IOContext is not inited."
argument_list|)
throw|;
block|}
name|updateIOContext
argument_list|()
expr_stmt|;
try|try
block|{
name|boolean
name|retVal
init|=
name|doNext
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|retVal
condition|)
block|{
if|if
condition|(
name|key
operator|instanceof
name|RecordIdentifier
condition|)
block|{
comment|//supports AcidInputFormat which uses the KEY pass ROW__ID info
name|ioCxtRef
operator|.
name|ri
operator|=
operator|(
name|RecordIdentifier
operator|)
name|key
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|recordReader
operator|instanceof
name|AcidInputFormat
operator|.
name|AcidRecordReader
condition|)
block|{
comment|//supports AcidInputFormat which do not use the KEY pass ROW__ID info
name|ioCxtRef
operator|.
name|ri
operator|=
operator|(
operator|(
name|AcidInputFormat
operator|.
name|AcidRecordReader
operator|)
name|recordReader
operator|)
operator|.
name|getRecordIdentifier
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|retVal
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|ioCxtRef
operator|.
name|setIOExceptions
argument_list|(
literal|true
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|protected
name|void
name|updateIOContext
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|pointerPos
init|=
name|this
operator|.
name|getPos
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|ioCxtRef
operator|.
name|isBlockPointer
condition|)
block|{
name|ioCxtRef
operator|.
name|currentBlockStart
operator|=
name|pointerPos
expr_stmt|;
name|ioCxtRef
operator|.
name|currentRow
operator|=
literal|0
expr_stmt|;
return|return;
block|}
name|ioCxtRef
operator|.
name|currentRow
operator|++
expr_stmt|;
if|if
condition|(
name|ioCxtRef
operator|.
name|nextBlockStart
operator|==
operator|-
literal|1
condition|)
block|{
name|ioCxtRef
operator|.
name|nextBlockStart
operator|=
name|pointerPos
expr_stmt|;
name|ioCxtRef
operator|.
name|currentRow
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|pointerPos
operator|!=
name|ioCxtRef
operator|.
name|nextBlockStart
condition|)
block|{
comment|// the reader pointer has moved to the end of next block, or the end of
comment|// current record.
name|ioCxtRef
operator|.
name|currentRow
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|ioCxtRef
operator|.
name|currentBlockStart
operator|==
name|ioCxtRef
operator|.
name|nextBlockStart
condition|)
block|{
name|ioCxtRef
operator|.
name|currentRow
operator|=
literal|1
expr_stmt|;
block|}
name|ioCxtRef
operator|.
name|currentBlockStart
operator|=
name|ioCxtRef
operator|.
name|nextBlockStart
expr_stmt|;
name|ioCxtRef
operator|.
name|nextBlockStart
operator|=
name|pointerPos
expr_stmt|;
block|}
block|}
specifier|public
name|IOContext
name|getIOContext
parameter_list|()
block|{
return|return
name|IOContext
operator|.
name|get
argument_list|(
name|jobConf
argument_list|)
return|;
block|}
specifier|private
name|void
name|initIOContext
parameter_list|(
name|long
name|startPos
parameter_list|,
name|boolean
name|isBlockPointer
parameter_list|,
name|Path
name|inputPath
parameter_list|)
block|{
name|ioCxtRef
operator|=
name|this
operator|.
name|getIOContext
argument_list|()
expr_stmt|;
name|ioCxtRef
operator|.
name|currentBlockStart
operator|=
name|startPos
expr_stmt|;
name|ioCxtRef
operator|.
name|isBlockPointer
operator|=
name|isBlockPointer
expr_stmt|;
name|ioCxtRef
operator|.
name|inputPath
operator|=
name|inputPath
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing file "
operator|+
name|inputPath
argument_list|)
expr_stmt|;
comment|// In spark, in multi-insert an input HadoopRDD maybe be shared by multiple
comment|// mappers, and if we cache it, only the first thread will have its thread-local
comment|// IOContext initialized, while the rest will not.
comment|// To solve this issue, we need to save a copy of the initialized IOContext, so that
comment|// later it can be used for other threads.
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jobConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"spark"
argument_list|)
condition|)
block|{
name|IOContext
name|iocontext
init|=
operator|new
name|IOContext
argument_list|()
decl_stmt|;
name|IOContext
operator|.
name|copy
argument_list|(
name|iocontext
argument_list|,
name|ioCxtRef
argument_list|)
expr_stmt|;
name|IOContext
operator|.
name|getMap
argument_list|()
operator|.
name|put
argument_list|(
name|SparkUtilities
operator|.
name|MAP_IO_CONTEXT
argument_list|,
name|iocontext
argument_list|)
expr_stmt|;
block|}
name|initDone
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|initIOContext
parameter_list|(
name|FileSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Class
name|inputFormatClass
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|initIOContext
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|inputFormatClass
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initIOContext
parameter_list|(
name|FileSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Class
name|inputFormatClass
parameter_list|,
name|RecordReader
name|recordReader
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|blockPointer
init|=
literal|false
decl_stmt|;
name|long
name|blockStart
init|=
operator|-
literal|1
decl_stmt|;
name|FileSplit
name|fileSplit
init|=
name|split
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
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|inputFormatClass
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
literal|"SequenceFile"
argument_list|)
condition|)
block|{
name|SequenceFile
operator|.
name|Reader
name|in
init|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|blockPointer
operator|=
name|in
operator|.
name|isBlockCompressed
argument_list|()
expr_stmt|;
name|in
operator|.
name|sync
argument_list|(
name|fileSplit
operator|.
name|getStart
argument_list|()
argument_list|)
expr_stmt|;
name|blockStart
operator|=
name|in
operator|.
name|getPosition
argument_list|()
expr_stmt|;
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|recordReader
operator|instanceof
name|RCFileRecordReader
condition|)
block|{
name|blockPointer
operator|=
literal|true
expr_stmt|;
name|blockStart
operator|=
operator|(
operator|(
name|RCFileRecordReader
operator|)
name|recordReader
operator|)
operator|.
name|getStart
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|inputFormatClass
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
literal|"RCFile"
argument_list|)
condition|)
block|{
name|blockPointer
operator|=
literal|true
expr_stmt|;
name|RCFile
operator|.
name|Reader
name|in
init|=
operator|new
name|RCFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|in
operator|.
name|sync
argument_list|(
name|fileSplit
operator|.
name|getStart
argument_list|()
argument_list|)
expr_stmt|;
name|blockStart
operator|=
name|in
operator|.
name|getPosition
argument_list|()
expr_stmt|;
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|jobConf
operator|=
name|job
expr_stmt|;
name|this
operator|.
name|initIOContext
argument_list|(
name|blockStart
argument_list|,
name|blockPointer
argument_list|,
name|path
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|initIOContextSortedProps
argument_list|(
name|split
argument_list|,
name|recordReader
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initIOContextSortedProps
parameter_list|(
name|FileSplit
name|split
parameter_list|,
name|RecordReader
name|recordReader
parameter_list|,
name|JobConf
name|job
parameter_list|)
block|{
name|this
operator|.
name|jobConf
operator|=
name|job
expr_stmt|;
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|resetSortingValues
argument_list|()
expr_stmt|;
name|this
operator|.
name|isSorted
operator|=
name|jobConf
operator|.
name|getBoolean
argument_list|(
literal|"hive.input.format.sorted"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|rangeStart
operator|=
name|split
operator|.
name|getStart
argument_list|()
expr_stmt|;
name|this
operator|.
name|rangeEnd
operator|=
name|split
operator|.
name|getStart
argument_list|()
operator|+
name|split
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|splitEnd
operator|=
name|rangeEnd
expr_stmt|;
if|if
condition|(
name|recordReader
operator|instanceof
name|RCFileRecordReader
operator|&&
name|rangeEnd
operator|!=
literal|0
operator|&&
name|this
operator|.
name|isSorted
condition|)
block|{
comment|// Binary search only works if we know the size of the split, and the recordReader is an
comment|// RCFileRecordReader
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|setUseSorted
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|setIsBinarySearching
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|wasUsingSortedSearch
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// Use the defalut methods for next in the child class
name|this
operator|.
name|isSorted
operator|=
literal|false
expr_stmt|;
block|}
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
if|if
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|isBinarySearching
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|recordReader
operator|.
name|getProgress
argument_list|()
return|;
block|}
block|}
specifier|private
name|FooterBuffer
name|footerBuffer
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|headerCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|footerCount
init|=
literal|0
decl_stmt|;
specifier|public
name|boolean
name|doNext
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|isSorted
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|shouldEndBinarySearch
argument_list|()
operator|||
operator|(
operator|!
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|useSorted
argument_list|()
operator|&&
name|this
operator|.
name|wasUsingSortedSearch
operator|)
condition|)
block|{
name|beginLinearSearch
argument_list|()
expr_stmt|;
name|this
operator|.
name|wasUsingSortedSearch
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|setEndBinarySearch
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|useSorted
argument_list|()
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|genericUDFClassName
operator|==
literal|null
operator|&&
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|getGenericUDFClassName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|setGenericUDFClassName
argument_list|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|getGenericUDFClassName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|isBinarySearching
argument_list|()
condition|)
block|{
comment|// Proceed with a binary search
if|if
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|getComparison
argument_list|()
operator|!=
literal|null
condition|)
block|{
switch|switch
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|getComparison
argument_list|()
condition|)
block|{
case|case
name|GREATER
case|:
case|case
name|EQUAL
case|:
comment|// Indexes have only one entry per value, could go linear from here, if we want to
comment|// use this for any sorted table, we'll need to continue the search
name|rangeEnd
operator|=
name|previousPosition
expr_stmt|;
break|break;
case|case
name|LESS
case|:
name|rangeStart
operator|=
name|previousPosition
expr_stmt|;
break|break;
default|default:
break|break;
block|}
block|}
name|long
name|position
init|=
operator|(
name|rangeStart
operator|+
name|rangeEnd
operator|)
operator|/
literal|2
decl_stmt|;
name|sync
argument_list|(
name|position
argument_list|)
expr_stmt|;
name|long
name|newPosition
init|=
name|getSyncedPosition
argument_list|()
decl_stmt|;
comment|// If the newPosition is the same as the previousPosition, we've reached the end of the
comment|// binary search, if the new position at least as big as the size of the split, any
comment|// matching rows must be in the final block, so we can end the binary search.
if|if
condition|(
name|newPosition
operator|==
name|previousPosition
operator|||
name|newPosition
operator|>=
name|splitEnd
condition|)
block|{
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|setIsBinarySearching
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|sync
argument_list|(
name|rangeStart
argument_list|)
expr_stmt|;
block|}
name|previousPosition
operator|=
name|newPosition
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|foundAllTargets
argument_list|()
condition|)
block|{
comment|// Found all possible rows which will not be filtered
return|return
literal|false
return|;
block|}
block|}
block|}
try|try
block|{
comment|/**        * When start reading new file, check header, footer rows.        * If file contains header, skip header lines before reading the records.        * If file contains footer, used a FooterBuffer to remove footer lines        * at the end of the table file.        **/
if|if
condition|(
name|this
operator|.
name|ioCxtRef
operator|.
name|getCurrentBlockStart
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Check if the table file has header to skip.
name|Path
name|filePath
init|=
name|this
operator|.
name|ioCxtRef
operator|.
name|getInputPath
argument_list|()
decl_stmt|;
name|PartitionDesc
name|part
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
init|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|jobConf
argument_list|)
operator|.
name|getPathToPartitionInfo
argument_list|()
decl_stmt|;
name|part
operator|=
name|HiveFileFormatUtils
operator|.
name|getPartitionDescFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|filePath
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|getPartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|ae
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cannot get partition description from "
operator|+
name|this
operator|.
name|ioCxtRef
operator|.
name|getInputPath
argument_list|()
operator|+
literal|"because "
operator|+
name|ae
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|part
operator|=
literal|null
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cannot get partition description from "
operator|+
name|this
operator|.
name|ioCxtRef
operator|.
name|getInputPath
argument_list|()
operator|+
literal|"because "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|part
operator|=
literal|null
expr_stmt|;
block|}
name|TableDesc
name|table
init|=
operator|(
name|part
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|part
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|headerCount
operator|=
name|Utilities
operator|.
name|getHeaderCount
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|footerCount
operator|=
name|Utilities
operator|.
name|getFooterCount
argument_list|(
name|table
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
block|}
comment|// If input contains header, skip header.
if|if
condition|(
operator|!
name|Utilities
operator|.
name|skipHeader
argument_list|(
name|recordReader
argument_list|,
name|headerCount
argument_list|,
operator|(
name|WritableComparable
operator|)
name|key
argument_list|,
operator|(
name|Writable
operator|)
name|value
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|footerCount
operator|>
literal|0
condition|)
block|{
name|footerBuffer
operator|=
operator|new
name|FooterBuffer
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|footerBuffer
operator|.
name|initializeBuffer
argument_list|(
name|jobConf
argument_list|,
name|recordReader
argument_list|,
name|footerCount
argument_list|,
operator|(
name|WritableComparable
operator|)
name|key
argument_list|,
operator|(
name|Writable
operator|)
name|value
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
if|if
condition|(
name|footerBuffer
operator|==
literal|null
condition|)
block|{
comment|// Table files don't have footer rows.
return|return
name|recordReader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|footerBuffer
operator|.
name|updateBuffer
argument_list|(
name|jobConf
argument_list|,
name|recordReader
argument_list|,
operator|(
name|WritableComparable
operator|)
name|key
argument_list|,
operator|(
name|Writable
operator|)
name|value
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|HiveIOExceptionHandlerUtil
operator|.
name|handleRecordReaderNextException
argument_list|(
name|e
argument_list|,
name|jobConf
argument_list|)
return|;
block|}
block|}
specifier|private
name|void
name|sync
parameter_list|(
name|long
name|position
parameter_list|)
throws|throws
name|IOException
block|{
operator|(
operator|(
name|RCFileRecordReader
operator|)
name|recordReader
operator|)
operator|.
name|sync
argument_list|(
name|position
argument_list|)
expr_stmt|;
operator|(
operator|(
name|RCFileRecordReader
operator|)
name|recordReader
operator|)
operator|.
name|resetBuffer
argument_list|()
expr_stmt|;
block|}
specifier|private
name|long
name|getSyncedPosition
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|recordReader
operator|.
name|getPos
argument_list|()
return|;
block|}
comment|/**    * This uses the name of the generic UDF being used by the filter to determine whether we should    * perform a binary search, and what the comparisons we should use to signal the end of the    * linear scan are.    * @param genericUDFClassName    * @throws IOException    */
specifier|private
name|void
name|setGenericUDFClassName
parameter_list|(
name|String
name|genericUDFClassName
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|genericUDFClassName
operator|=
name|genericUDFClassName
expr_stmt|;
if|if
condition|(
name|genericUDFClassName
operator|.
name|equals
argument_list|(
name|GenericUDFOPEqual
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|stopComparisons
operator|.
name|add
argument_list|(
name|Comparison
operator|.
name|GREATER
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|genericUDFClassName
operator|.
name|equals
argument_list|(
name|GenericUDFOPLessThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|stopComparisons
operator|.
name|add
argument_list|(
name|Comparison
operator|.
name|EQUAL
argument_list|)
expr_stmt|;
name|stopComparisons
operator|.
name|add
argument_list|(
name|Comparison
operator|.
name|GREATER
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|isBinarySearching
argument_list|()
condition|)
block|{
name|beginLinearSearch
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|genericUDFClassName
operator|.
name|equals
argument_list|(
name|GenericUDFOPEqualOrLessThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|stopComparisons
operator|.
name|add
argument_list|(
name|Comparison
operator|.
name|GREATER
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|isBinarySearching
argument_list|()
condition|)
block|{
name|beginLinearSearch
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|genericUDFClassName
operator|.
name|equals
argument_list|(
name|GenericUDFOPGreaterThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
name|genericUDFClassName
operator|.
name|equals
argument_list|(
name|GenericUDFOPEqualOrGreaterThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
comment|// Do nothing
block|}
else|else
block|{
comment|// This is an unsupported operator
name|LOG
operator|.
name|debug
argument_list|(
name|genericUDFClassName
operator|+
literal|" is not the name of a supported class.  "
operator|+
literal|"Continuing linearly."
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|isBinarySearching
argument_list|()
condition|)
block|{
name|beginLinearSearch
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * This should be called after the binary search is finished and before the linear scan begins    * @throws IOException    */
specifier|private
name|void
name|beginLinearSearch
parameter_list|()
throws|throws
name|IOException
block|{
name|sync
argument_list|(
name|rangeStart
argument_list|)
expr_stmt|;
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|setIsBinarySearching
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|wasUsingSortedSearch
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Returns true if the current comparison is in the list of stop comparisons, i.e. we've found    * all records which won't be filtered    * @return true if the current comparison is found    */
specifier|public
name|boolean
name|foundAllTargets
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|getComparison
argument_list|()
operator|==
literal|null
operator|||
operator|!
name|stopComparisons
operator|.
name|contains
argument_list|(
name|this
operator|.
name|getIOContext
argument_list|()
operator|.
name|getComparison
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

