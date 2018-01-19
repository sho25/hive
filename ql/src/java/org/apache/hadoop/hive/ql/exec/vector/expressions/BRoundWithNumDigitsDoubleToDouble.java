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
name|udf
operator|.
name|generic
operator|.
name|RoundUtils
import|;
end_import

begin_comment
comment|// Vectorized implementation of BROUND(Col, N) function
end_comment

begin_class
specifier|public
class|class
name|BRoundWithNumDigitsDoubleToDouble
extends|extends
name|RoundWithNumDigitsDoubleToDouble
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|18493485928L
decl_stmt|;
specifier|public
name|BRoundWithNumDigitsDoubleToDouble
parameter_list|(
name|int
name|colNum
parameter_list|,
name|long
name|scalarVal
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|colNum
argument_list|,
name|scalarVal
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
specifier|public
name|BRoundWithNumDigitsDoubleToDouble
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|// Round to the specified number of decimal places using half-even round function.
annotation|@
name|Override
specifier|public
name|double
name|func
parameter_list|(
name|double
name|d
parameter_list|)
block|{
return|return
name|RoundUtils
operator|.
name|bround
argument_list|(
name|d
argument_list|,
name|getDecimalPlaces
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

