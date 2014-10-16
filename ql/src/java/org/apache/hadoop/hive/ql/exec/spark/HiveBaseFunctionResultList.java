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
name|exec
operator|.
name|spark
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
name|Serializable
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
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|HiveKey
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
name|mapred
operator|.
name|OutputCollector
import|;
end_import

begin_import
import|import
name|scala
operator|.
name|Tuple2
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Base class for  *   - collecting Map/Reduce function output and  *   - providing an Iterable interface for fetching output records. Input records  *     are processed in lazy fashion i.e when output records are requested  *     through Iterator interface.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|public
specifier|abstract
class|class
name|HiveBaseFunctionResultList
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Iterable
implements|,
name|OutputCollector
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
implements|,
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
specifier|final
name|Iterator
argument_list|<
name|T
argument_list|>
name|inputIterator
decl_stmt|;
specifier|private
name|boolean
name|isClosed
init|=
literal|false
decl_stmt|;
comment|// Contains results from last processed input record.
specifier|private
specifier|final
name|HiveKVResultCache
name|lastRecordOutput
decl_stmt|;
specifier|private
name|boolean
name|iteratorAlreadyCreated
init|=
literal|false
decl_stmt|;
specifier|public
name|HiveBaseFunctionResultList
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Iterator
argument_list|<
name|T
argument_list|>
name|inputIterator
parameter_list|)
block|{
name|this
operator|.
name|inputIterator
operator|=
name|inputIterator
expr_stmt|;
name|this
operator|.
name|lastRecordOutput
operator|=
operator|new
name|HiveKVResultCache
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
name|iterator
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|!
name|iteratorAlreadyCreated
argument_list|,
literal|"Iterator can only be created once."
argument_list|)
expr_stmt|;
name|iteratorAlreadyCreated
operator|=
literal|true
expr_stmt|;
return|return
operator|new
name|ResultIterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|HiveKey
name|key
parameter_list|,
name|BytesWritable
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|lastRecordOutput
operator|.
name|add
argument_list|(
name|copyHiveKey
argument_list|(
name|key
argument_list|)
argument_list|,
name|copyBytesWritable
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|HiveKey
name|copyHiveKey
parameter_list|(
name|HiveKey
name|key
parameter_list|)
block|{
name|HiveKey
name|copy
init|=
operator|new
name|HiveKey
argument_list|()
decl_stmt|;
name|copy
operator|.
name|setDistKeyLength
argument_list|(
name|key
operator|.
name|getDistKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|copy
operator|.
name|setHashCode
argument_list|(
name|key
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|copy
operator|.
name|set
argument_list|(
name|key
argument_list|)
expr_stmt|;
return|return
name|copy
return|;
block|}
specifier|private
specifier|static
name|BytesWritable
name|copyBytesWritable
parameter_list|(
name|BytesWritable
name|bw
parameter_list|)
block|{
name|BytesWritable
name|copy
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|copy
operator|.
name|set
argument_list|(
name|bw
argument_list|)
expr_stmt|;
return|return
name|copy
return|;
block|}
comment|/** Process the given record. */
specifier|protected
specifier|abstract
name|void
name|processNextRecord
parameter_list|(
name|T
name|inputRecord
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** Is the current state of the record processor done? */
specifier|protected
specifier|abstract
name|boolean
name|processingDone
parameter_list|()
function_decl|;
comment|/** Close the record processor */
specifier|protected
specifier|abstract
name|void
name|closeRecordProcessor
parameter_list|()
function_decl|;
comment|/** Implement Iterator interface */
specifier|public
class|class
name|ResultIterator
implements|implements
name|Iterator
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
comment|// Return remaining records (if any) from last processed input record.
if|if
condition|(
name|lastRecordOutput
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|lastRecordOutput
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Process the records in the input iterator until
comment|//  - new output records are available for serving downstream operator,
comment|//  - input records are exhausted or
comment|//  - processing is completed.
while|while
condition|(
name|inputIterator
operator|.
name|hasNext
argument_list|()
operator|&&
operator|!
name|processingDone
argument_list|()
condition|)
block|{
try|try
block|{
name|processNextRecord
argument_list|(
name|inputIterator
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastRecordOutput
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// TODO: better handling of exception.
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error while processing input."
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
comment|// At this point we are done processing the input. Close the record processor
if|if
condition|(
operator|!
name|isClosed
condition|)
block|{
name|closeRecordProcessor
argument_list|()
expr_stmt|;
name|isClosed
operator|=
literal|true
expr_stmt|;
block|}
comment|// It is possible that some operators add records after closing the processor, so make sure
comment|// to check the lastRecordOutput
if|if
condition|(
name|lastRecordOutput
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|lastRecordOutput
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|next
parameter_list|()
block|{
if|if
condition|(
name|hasNext
argument_list|()
condition|)
block|{
return|return
name|lastRecordOutput
operator|.
name|next
argument_list|()
return|;
block|}
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
literal|"There are no more elements"
argument_list|)
throw|;
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
argument_list|(
literal|"Iterator.remove() is not supported"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

