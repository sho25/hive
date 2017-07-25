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
operator|.
name|util
operator|.
name|batchgen
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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

begin_class
specifier|public
class|class
name|VectorBatchGenerateStream
block|{
specifier|private
specifier|final
name|long
name|randomSeed
decl_stmt|;
specifier|private
specifier|final
name|VectorBatchGenerator
name|generator
decl_stmt|;
specifier|private
specifier|final
name|int
name|rowCount
decl_stmt|;
comment|// Stream variables.
specifier|private
name|Random
name|random
decl_stmt|;
specifier|private
name|int
name|sizeCountDown
decl_stmt|;
specifier|public
name|VectorBatchGenerateStream
parameter_list|(
name|long
name|randomSeed
parameter_list|,
name|VectorBatchGenerator
name|generator
parameter_list|,
name|int
name|rowCount
parameter_list|)
block|{
name|this
operator|.
name|randomSeed
operator|=
name|randomSeed
expr_stmt|;
name|this
operator|.
name|generator
operator|=
name|generator
expr_stmt|;
name|this
operator|.
name|rowCount
operator|=
name|rowCount
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|getRowCount
parameter_list|()
block|{
return|return
name|rowCount
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
name|randomSeed
argument_list|)
expr_stmt|;
name|sizeCountDown
operator|=
name|rowCount
expr_stmt|;
block|}
specifier|public
name|boolean
name|isNext
parameter_list|()
block|{
return|return
operator|(
name|sizeCountDown
operator|>
literal|0
operator|)
return|;
block|}
specifier|public
name|void
name|fillNext
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
block|{
specifier|final
name|int
name|size
init|=
name|Math
operator|.
name|min
argument_list|(
name|sizeCountDown
argument_list|,
name|batch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
name|batch
operator|.
name|reset
argument_list|()
expr_stmt|;
name|generator
operator|.
name|generateBatch
argument_list|(
name|batch
argument_list|,
name|random
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|sizeCountDown
operator|-=
name|size
expr_stmt|;
block|}
block|}
end_class

end_unit

