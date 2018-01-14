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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|expressions
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
name|exec
operator|.
name|vector
operator|.
name|LongColumnVector
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

begin_comment
comment|/**  * This vector expression implements a Checked variant of LongColModuloLongColumn  * If the outputTypeInfo is not long it casts the result column vector values to  * the set outputType so as to have similar result when compared to non-vectorized UDF  * execution.  */
end_comment

begin_class
specifier|public
class|class
name|LongColModuloLongColumnChecked
extends|extends
name|LongColModuloLongColumn
block|{
specifier|public
name|LongColModuloLongColumnChecked
parameter_list|(
name|int
name|colNum1
parameter_list|,
name|int
name|colNum2
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|colNum1
argument_list|,
name|colNum2
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LongColModuloLongColumnChecked
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
block|{
name|super
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|//checked for overflow based on the outputTypeInfo
name|OverflowUtils
operator|.
name|accountForOverflowLong
argument_list|(
name|outputTypeInfo
argument_list|,
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumnNum
index|]
argument_list|,
name|batch
operator|.
name|selectedInUse
argument_list|,
name|batch
operator|.
name|selected
argument_list|,
name|batch
operator|.
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportsCheckedExecution
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

