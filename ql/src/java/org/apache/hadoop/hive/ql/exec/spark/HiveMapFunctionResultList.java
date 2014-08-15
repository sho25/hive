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
name|mapred
operator|.
name|Reporter
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
name|Iterator
import|;
end_import

begin_class
specifier|public
class|class
name|HiveMapFunctionResultList
extends|extends
name|HiveBaseFunctionResultList
argument_list|<
name|Tuple2
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
block|{
specifier|private
specifier|final
name|SparkMapRecordHandler
name|recordHandler
decl_stmt|;
comment|/**    * Instantiate result set Iterable for Map function output.    *    * @param inputIterator Input record iterator.    * @param handler Initialized {@link SparkMapRecordHandler} instance.    */
specifier|public
name|HiveMapFunctionResultList
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Iterator
argument_list|<
name|Tuple2
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|inputIterator
parameter_list|,
name|SparkMapRecordHandler
name|handler
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|inputIterator
argument_list|)
expr_stmt|;
name|recordHandler
operator|=
name|handler
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processNextRecord
parameter_list|(
name|Tuple2
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
name|inputRecord
parameter_list|)
throws|throws
name|IOException
block|{
name|recordHandler
operator|.
name|processRow
argument_list|(
name|inputRecord
operator|.
name|_2
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|processingDone
parameter_list|()
block|{
return|return
name|recordHandler
operator|.
name|getDone
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|closeRecordProcessor
parameter_list|()
block|{
name|recordHandler
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

