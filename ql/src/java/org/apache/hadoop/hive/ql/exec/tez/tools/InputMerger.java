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
name|tez
operator|.
name|tools
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
name|Comparator
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
name|PriorityQueue
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|tez
operator|.
name|ReduceRecordProcessor
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
name|BinaryComparable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|Input
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|library
operator|.
name|api
operator|.
name|KeyValuesReader
import|;
end_import

begin_comment
comment|/**  * A KeyValuesReader implementation that returns a sorted stream of key-values  * by doing a sorted merge of the key-value in LogicalInputs.  * Tags are in the last byte of the key, so no special handling for tags is required.  * Uses a priority queue to pick the KeyValuesReader of the input that is next in  * sort order.  */
end_comment

begin_class
specifier|public
class|class
name|InputMerger
implements|implements
name|KeyValuesReader
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ReduceRecordProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|PriorityQueue
argument_list|<
name|KeyValuesReader
argument_list|>
name|pQueue
init|=
literal|null
decl_stmt|;
specifier|private
name|KeyValuesReader
name|nextKVReader
init|=
literal|null
decl_stmt|;
specifier|public
name|InputMerger
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Input
argument_list|>
name|shuffleInputs
parameter_list|)
throws|throws
name|Exception
block|{
comment|//get KeyValuesReaders from the LogicalInput and add them to priority queue
name|int
name|initialCapacity
init|=
name|shuffleInputs
operator|.
name|size
argument_list|()
decl_stmt|;
name|pQueue
operator|=
operator|new
name|PriorityQueue
argument_list|<
name|KeyValuesReader
argument_list|>
argument_list|(
name|initialCapacity
argument_list|,
operator|new
name|KVReaderComparator
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Input
name|input
range|:
name|shuffleInputs
control|)
block|{
name|addToQueue
argument_list|(
operator|(
name|KeyValuesReader
operator|)
name|input
operator|.
name|getReader
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Add KeyValuesReader to queue if it has more key-values    * @param kvsReadr    * @throws IOException    */
specifier|private
name|void
name|addToQueue
parameter_list|(
name|KeyValuesReader
name|kvsReadr
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|kvsReadr
operator|.
name|next
argument_list|()
condition|)
block|{
name|pQueue
operator|.
name|add
argument_list|(
name|kvsReadr
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return true if there are more key-values and advances to next key-values    * @throws IOException    */
specifier|public
name|boolean
name|next
parameter_list|()
throws|throws
name|IOException
block|{
comment|//add the previous nextKVReader back to queue
if|if
condition|(
name|nextKVReader
operator|!=
literal|null
condition|)
block|{
name|addToQueue
argument_list|(
name|nextKVReader
argument_list|)
expr_stmt|;
block|}
comment|//get the new nextKVReader with lowest key
name|nextKVReader
operator|=
name|pQueue
operator|.
name|poll
argument_list|()
expr_stmt|;
return|return
name|nextKVReader
operator|!=
literal|null
return|;
block|}
specifier|public
name|Object
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|nextKVReader
operator|.
name|getCurrentKey
argument_list|()
return|;
block|}
specifier|public
name|Iterable
argument_list|<
name|Object
argument_list|>
name|getCurrentValues
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|nextKVReader
operator|.
name|getCurrentValues
argument_list|()
return|;
block|}
comment|/**    * Comparator that compares KeyValuesReader on their current key    */
class|class
name|KVReaderComparator
implements|implements
name|Comparator
argument_list|<
name|KeyValuesReader
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|KeyValuesReader
name|kvReadr1
parameter_list|,
name|KeyValuesReader
name|kvReadr2
parameter_list|)
block|{
try|try
block|{
name|BinaryComparable
name|key1
init|=
operator|(
name|BinaryComparable
operator|)
name|kvReadr1
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
name|BinaryComparable
name|key2
init|=
operator|(
name|BinaryComparable
operator|)
name|kvReadr2
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
return|return
name|key1
operator|.
name|compareTo
argument_list|(
name|key2
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
literal|"Caught exception while reading shuffle input"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|//die!
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

