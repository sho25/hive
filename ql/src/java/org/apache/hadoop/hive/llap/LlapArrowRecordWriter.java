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
name|llap
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
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|VectorSchemaRoot
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|ipc
operator|.
name|ArrowStreamWriter
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
name|arrow
operator|.
name|ArrowWrapperWritable
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
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|WritableByteChannel
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
name|mapred
operator|.
name|Reporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Writes Arrow batches to an {@link org.apache.arrow.vector.ipc.ArrowStreamWriter}.  * The byte stream will be formatted according to the Arrow Streaming format.  * Because ArrowStreamWriter is bound to a {@link org.apache.arrow.vector.VectorSchemaRoot}  * when it is created,  * calls to the {@link #write(Writable, Writable)} method only serve as a signal that  * a new batch has been loaded to the associated VectorSchemaRoot.  * Payload data for writing is indirectly made available by reference:  * ArrowStreamWriter -> VectorSchemaRoot -> List<FieldVector>  * i.e. both they key and value are ignored once a reference to the VectorSchemaRoot  * is obtained.  */
end_comment

begin_class
specifier|public
class|class
name|LlapArrowRecordWriter
parameter_list|<
name|K
extends|extends
name|Writable
parameter_list|,
name|V
extends|extends
name|Writable
parameter_list|>
implements|implements
name|RecordWriter
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LlapArrowRecordWriter
operator|.
name|class
argument_list|)
decl_stmt|;
name|ArrowStreamWriter
name|arrowStreamWriter
decl_stmt|;
name|WritableByteChannel
name|out
decl_stmt|;
specifier|public
name|LlapArrowRecordWriter
parameter_list|(
name|WritableByteChannel
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|arrowStreamWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
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
name|ArrowWrapperWritable
name|arrowWrapperWritable
init|=
operator|(
name|ArrowWrapperWritable
operator|)
name|value
decl_stmt|;
if|if
condition|(
name|arrowStreamWriter
operator|==
literal|null
condition|)
block|{
name|VectorSchemaRoot
name|vectorSchemaRoot
init|=
name|arrowWrapperWritable
operator|.
name|getVectorSchemaRoot
argument_list|()
decl_stmt|;
name|arrowStreamWriter
operator|=
operator|new
name|ArrowStreamWriter
argument_list|(
name|vectorSchemaRoot
argument_list|,
literal|null
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
name|arrowStreamWriter
operator|.
name|writeBatch
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

