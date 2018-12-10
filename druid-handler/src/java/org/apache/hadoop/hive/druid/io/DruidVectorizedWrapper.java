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
name|druid
operator|.
name|io
package|;
end_package

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
name|druid
operator|.
name|serde
operator|.
name|DruidQueryRecordReader
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
name|druid
operator|.
name|serde
operator|.
name|DruidSerDe
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
name|druid
operator|.
name|serde
operator|.
name|DruidWritable
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
name|VectorAssignRow
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
name|metadata
operator|.
name|HiveException
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
name|MapWork
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
name|SerDeException
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
name|StructObjectInspector
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
name|RecordReader
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
name|Properties
import|;
end_import

begin_comment
comment|/**  * A Wrapper class that consumes row-by-row from base Druid Record Reader and provides a Vectorized one.  * @param<T> type of the Druid query.  */
end_comment

begin_class
specifier|public
class|class
name|DruidVectorizedWrapper
parameter_list|<
name|T
extends|extends
name|Comparable
parameter_list|<
name|T
parameter_list|>
parameter_list|>
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
name|VectorAssignRow
name|vectorAssignRow
init|=
operator|new
name|VectorAssignRow
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|DruidQueryRecordReader
name|baseReader
decl_stmt|;
specifier|private
specifier|final
name|VectorizedRowBatchCtx
name|rbCtx
decl_stmt|;
specifier|private
specifier|final
name|DruidSerDe
name|serDe
decl_stmt|;
specifier|private
specifier|final
name|Object
index|[]
name|rowBoat
decl_stmt|;
comment|/**    * Actual projected columns needed by the query, this can be empty in case of query like: select count(*) from src.    */
specifier|private
specifier|final
name|int
index|[]
name|projectedColumns
decl_stmt|;
specifier|private
specifier|final
name|DruidWritable
name|druidWritable
decl_stmt|;
specifier|public
name|DruidVectorizedWrapper
parameter_list|(
name|DruidQueryRecordReader
name|reader
parameter_list|,
name|Configuration
name|jobConf
parameter_list|)
block|{
name|this
operator|.
name|rbCtx
operator|=
name|Utilities
operator|.
name|getVectorizedRowBatchCtx
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
if|if
condition|(
name|rbCtx
operator|.
name|getDataColumnNums
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|projectedColumns
operator|=
name|rbCtx
operator|.
name|getDataColumnNums
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// case all the columns are selected
name|projectedColumns
operator|=
operator|new
name|int
index|[
name|rbCtx
operator|.
name|getRowColumnTypeInfos
argument_list|()
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|projectedColumns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|projectedColumns
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
name|this
operator|.
name|serDe
operator|=
name|createAndInitializeSerde
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|baseReader
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|reader
argument_list|)
expr_stmt|;
comment|// row parser and row assigner initializing
try|try
block|{
name|vectorAssignRow
operator|.
name|init
argument_list|(
operator|(
name|StructObjectInspector
operator|)
name|serDe
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
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
name|druidWritable
operator|=
name|baseReader
operator|.
name|createValue
argument_list|()
expr_stmt|;
name|rowBoat
operator|=
operator|new
name|Object
index|[
name|rbCtx
operator|.
name|getDataColumnCount
argument_list|()
index|]
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|NullWritable
name|nullWritable
parameter_list|,
name|VectorizedRowBatch
name|vectorizedRowBatch
parameter_list|)
throws|throws
name|IOException
block|{
name|vectorizedRowBatch
operator|.
name|reset
argument_list|()
expr_stmt|;
name|int
name|rowsCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|rowsCount
operator|<
name|vectorizedRowBatch
operator|.
name|getMaxSize
argument_list|()
operator|&&
name|baseReader
operator|.
name|next
argument_list|(
name|nullWritable
argument_list|,
name|druidWritable
argument_list|)
condition|)
block|{
if|if
condition|(
name|projectedColumns
operator|.
name|length
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|serDe
operator|.
name|deserializeAsPrimitive
argument_list|(
name|druidWritable
argument_list|,
name|rowBoat
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
range|:
name|projectedColumns
control|)
block|{
name|vectorAssignRow
operator|.
name|assignRowColumn
argument_list|(
name|vectorizedRowBatch
argument_list|,
name|rowsCount
argument_list|,
name|i
argument_list|,
name|rowBoat
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|rowsCount
operator|++
expr_stmt|;
block|}
name|vectorizedRowBatch
operator|.
name|size
operator|=
name|rowsCount
expr_stmt|;
return|return
name|rowsCount
operator|>
literal|0
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
name|baseReader
operator|.
name|getPos
argument_list|()
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
name|baseReader
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
name|baseReader
operator|.
name|getProgress
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|DruidSerDe
name|createAndInitializeSerde
parameter_list|(
name|Configuration
name|jobConf
parameter_list|)
block|{
name|DruidSerDe
name|serDe
init|=
operator|new
name|DruidSerDe
argument_list|()
decl_stmt|;
name|MapWork
name|mapWork
init|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|jobConf
argument_list|)
argument_list|,
literal|"Map work is null"
argument_list|)
decl_stmt|;
name|Properties
name|properties
init|=
name|mapWork
operator|.
name|getPartitionDescs
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|partitionDesc
lambda|->
name|partitionDesc
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getProperties
argument_list|()
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|orElseThrow
argument_list|(
parameter_list|()
lambda|->
operator|new
name|RuntimeException
argument_list|(
literal|"Can not find table property at the map work"
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|serDe
operator|.
name|initialize
argument_list|(
name|jobConf
argument_list|,
name|properties
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can not initialized the serde"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|serDe
return|;
block|}
block|}
end_class

end_unit

