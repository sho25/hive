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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|ColumnVector
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
name|DecimalColumnVector
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
operator|.
name|ArgumentType
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
name|HiveDecimalWritable
import|;
end_import

begin_comment
comment|/**  * Vectorized implementation of trunc(number, scale) function for decimal input  */
end_comment

begin_class
specifier|public
class|class
name|TruncDecimal
extends|extends
name|TruncFloat
block|{
comment|/**    *     */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|TruncDecimal
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TruncDecimal
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|scale
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|colNum
argument_list|,
name|scale
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|trunc
parameter_list|(
name|ColumnVector
name|inputColVector
parameter_list|,
name|ColumnVector
name|outputColVector
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|HiveDecimal
name|input
init|=
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|inputColVector
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|getHiveDecimal
argument_list|()
decl_stmt|;
name|HiveDecimal
name|output
init|=
name|trunc
argument_list|(
name|input
argument_list|)
decl_stmt|;
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|outputColVector
operator|)
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|HiveDecimal
name|trunc
parameter_list|(
name|HiveDecimal
name|input
parameter_list|)
block|{
name|HiveDecimal
name|pow
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
name|Math
operator|.
name|pow
argument_list|(
literal|10
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|scale
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|scale
operator|>=
literal|0
condition|)
block|{
if|if
condition|(
name|scale
operator|!=
literal|0
condition|)
block|{
name|long
name|longValue
init|=
name|input
operator|.
name|multiply
argument_list|(
name|pow
argument_list|)
operator|.
name|longValue
argument_list|()
decl_stmt|;
return|return
name|HiveDecimal
operator|.
name|create
argument_list|(
name|longValue
argument_list|)
operator|.
name|divide
argument_list|(
name|pow
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|HiveDecimal
operator|.
name|create
argument_list|(
name|input
operator|.
name|longValue
argument_list|()
argument_list|)
return|;
block|}
block|}
else|else
block|{
name|long
name|longValue2
init|=
name|input
operator|.
name|divide
argument_list|(
name|pow
argument_list|)
operator|.
name|longValue
argument_list|()
decl_stmt|;
return|return
name|HiveDecimal
operator|.
name|create
argument_list|(
name|longValue2
argument_list|)
operator|.
name|multiply
argument_list|(
name|pow
argument_list|)
return|;
block|}
block|}
specifier|protected
name|ArgumentType
name|getInputColumnType
parameter_list|()
block|{
return|return
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|DECIMAL
return|;
block|}
block|}
end_class

end_unit

