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
name|VectorExpressionDescriptor
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
name|udf
operator|.
name|UDFRound
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
name|io
operator|.
name|DoubleWritable
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
name|IntWritable
import|;
end_import

begin_comment
comment|// Vectorized implementation of ROUND(Col, N) function
end_comment

begin_class
specifier|public
class|class
name|RoundWithNumDigitsDoubleToDouble
extends|extends
name|MathFuncDoubleToDouble
implements|implements
name|ISetLongArg
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|IntWritable
name|decimalPlaces
decl_stmt|;
specifier|private
specifier|transient
name|UDFRound
name|roundFunc
decl_stmt|;
specifier|private
specifier|transient
name|DoubleWritable
name|dw
decl_stmt|;
specifier|public
name|RoundWithNumDigitsDoubleToDouble
parameter_list|(
name|int
name|colNum
parameter_list|,
name|long
name|scalarVal
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|super
argument_list|(
name|colNum
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
name|this
operator|.
name|decimalPlaces
operator|=
operator|new
name|IntWritable
argument_list|()
expr_stmt|;
name|roundFunc
operator|=
operator|new
name|UDFRound
argument_list|()
expr_stmt|;
name|dw
operator|=
operator|new
name|DoubleWritable
argument_list|()
expr_stmt|;
name|decimalPlaces
operator|.
name|set
argument_list|(
operator|(
name|int
operator|)
name|scalarVal
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RoundWithNumDigitsDoubleToDouble
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|dw
operator|=
operator|new
name|DoubleWritable
argument_list|()
expr_stmt|;
name|roundFunc
operator|=
operator|new
name|UDFRound
argument_list|()
expr_stmt|;
block|}
comment|// Round to the specified number of decimal places using the standard Hive round function.
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
name|dw
operator|.
name|set
argument_list|(
name|d
argument_list|)
expr_stmt|;
return|return
name|roundFunc
operator|.
name|evaluate
argument_list|(
name|dw
argument_list|,
name|decimalPlaces
argument_list|)
operator|.
name|get
argument_list|()
return|;
block|}
name|void
name|setDecimalPlaces
parameter_list|(
name|IntWritable
name|decimalPlaces
parameter_list|)
block|{
name|this
operator|.
name|decimalPlaces
operator|=
name|decimalPlaces
expr_stmt|;
block|}
specifier|public
name|IntWritable
name|getDecimalPlaces
parameter_list|()
block|{
return|return
name|this
operator|.
name|decimalPlaces
return|;
block|}
name|void
name|setRoundFunc
parameter_list|(
name|UDFRound
name|roundFunc
parameter_list|)
block|{
name|this
operator|.
name|roundFunc
operator|=
name|roundFunc
expr_stmt|;
block|}
name|UDFRound
name|getRoundFunc
parameter_list|()
block|{
return|return
name|this
operator|.
name|roundFunc
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setArg
parameter_list|(
name|long
name|l
parameter_list|)
block|{
name|this
operator|.
name|decimalPlaces
operator|.
name|set
argument_list|(
operator|(
name|int
operator|)
name|l
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|VectorExpressionDescriptor
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
name|VectorExpressionDescriptor
operator|.
name|Builder
name|b
init|=
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|b
operator|.
name|setMode
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|Mode
operator|.
name|PROJECTION
argument_list|)
operator|.
name|setNumArguments
argument_list|(
literal|2
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|DOUBLE
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|LONG
argument_list|)
operator|.
name|setInputExpressionTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|COLUMN
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|SCALAR
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

