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
name|llap
operator|.
name|io
operator|.
name|decode
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
name|hive
operator|.
name|llap
operator|.
name|Consumer
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
name|llap
operator|.
name|ConsumerFeedback
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|EncodedColumnBatch
operator|.
name|StreamBuffer
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|impl
operator|.
name|ColumnVectorBatch
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|orc
operator|.
name|OrcBatchKey
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
name|RecordReaderImpl
import|;
end_import

begin_class
specifier|public
class|class
name|OrcEncodedDataConsumer
extends|extends
name|EncodedDataConsumer
argument_list|<
name|OrcBatchKey
argument_list|>
block|{
specifier|private
name|RecordReaderImpl
operator|.
name|TreeReader
index|[]
name|columnReaders
decl_stmt|;
specifier|public
name|OrcEncodedDataConsumer
parameter_list|(
name|ColumnVectorProducer
argument_list|<
name|OrcBatchKey
argument_list|>
name|cvp
parameter_list|,
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|consumer
parameter_list|,
name|int
name|colCount
parameter_list|)
block|{
name|super
argument_list|(
name|cvp
argument_list|,
name|consumer
argument_list|,
name|colCount
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setColumnReaders
parameter_list|(
name|RecordReaderImpl
operator|.
name|TreeReader
index|[]
name|columnReaders
parameter_list|)
block|{
name|this
operator|.
name|columnReaders
operator|=
name|columnReaders
expr_stmt|;
block|}
specifier|public
name|RecordReaderImpl
operator|.
name|TreeReader
index|[]
name|getColumnReaders
parameter_list|()
block|{
return|return
name|columnReaders
return|;
block|}
block|}
end_class

end_unit

