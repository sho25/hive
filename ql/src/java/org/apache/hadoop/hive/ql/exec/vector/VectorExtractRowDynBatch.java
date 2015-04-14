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
name|vector
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * This class extracts specified VectorizedRowBatch row columns into a Writable row Object[].  *  * The caller provides the hive type names and target column numbers in the order desired to  * extract from the Writable row Object[].  *  * This class is for use when the batch being assigned is always the same.  */
end_comment

begin_class
specifier|public
class|class
name|VectorExtractRowDynBatch
extends|extends
name|VectorExtractRow
block|{
specifier|public
name|void
name|setBatchOnEntry
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
name|setBatch
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|forgetBatchOnExit
parameter_list|()
block|{
name|forgetBatch
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

