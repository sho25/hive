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
name|common
operator|.
name|type
operator|.
name|HiveChar
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
name|VectorExpressionDescriptor
import|;
end_import

begin_comment
comment|/**  * Compute IF(expr1, expr2, expr3) for 3 input column expressions.  * The first is always a boolean (LongColumnVector).  * The second is a string scalar.  * The third is a string scalar.  */
end_comment

begin_class
specifier|public
class|class
name|IfExprStringScalarCharScalar
extends|extends
name|IfExprStringScalarStringScalar
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|IfExprStringScalarCharScalar
parameter_list|(
name|int
name|arg1Column
parameter_list|,
name|byte
index|[]
name|arg2Scalar
parameter_list|,
name|HiveChar
name|arg3Scalar
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|arg1Column
argument_list|,
name|arg2Scalar
argument_list|,
name|arg3Scalar
operator|.
name|getValue
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IfExprStringScalarCharScalar
parameter_list|()
block|{
name|super
argument_list|()
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
return|return
operator|(
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
operator|)
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
literal|3
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|INT_FAMILY
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|STRING
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|CHAR
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
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|SCALAR
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

