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
name|common
operator|.
name|type
operator|.
name|HiveVarchar
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
name|*
import|;
end_import

begin_comment
comment|/**  * Constant is represented as a vector with repeating values.  */
end_comment

begin_class
specifier|public
class|class
name|ConstantVectorExpression
extends|extends
name|VectorExpression
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
specifier|static
enum|enum
name|Type
block|{
name|LONG
block|,
name|DOUBLE
block|,
name|BYTES
block|,
name|DECIMAL
block|}
specifier|private
name|int
name|outputColumn
decl_stmt|;
specifier|protected
name|long
name|longValue
init|=
literal|0
decl_stmt|;
specifier|private
name|double
name|doubleValue
init|=
literal|0
decl_stmt|;
specifier|private
name|byte
index|[]
name|bytesValue
init|=
literal|null
decl_stmt|;
specifier|private
name|HiveDecimal
name|decimalValue
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|isNullValue
init|=
literal|false
decl_stmt|;
specifier|private
name|Type
name|type
decl_stmt|;
specifier|private
name|int
name|bytesValueLength
init|=
literal|0
decl_stmt|;
specifier|public
name|ConstantVectorExpression
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumn
parameter_list|,
name|String
name|typeString
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
name|setTypeString
argument_list|(
name|typeString
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumn
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|outputColumn
argument_list|,
literal|"long"
argument_list|)
expr_stmt|;
name|this
operator|.
name|longValue
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumn
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|outputColumn
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|this
operator|.
name|doubleValue
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumn
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|outputColumn
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|setBytesValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumn
parameter_list|,
name|HiveChar
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|outputColumn
argument_list|,
literal|"char"
argument_list|)
expr_stmt|;
name|setBytesValue
argument_list|(
name|value
operator|.
name|getStrippedValue
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumn
parameter_list|,
name|HiveVarchar
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|outputColumn
argument_list|,
literal|"varchar"
argument_list|)
expr_stmt|;
name|setBytesValue
argument_list|(
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumn
parameter_list|,
name|HiveDecimal
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|outputColumn
argument_list|,
literal|"decimal"
argument_list|)
expr_stmt|;
name|setDecimalValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
comment|/*    * Support for null constant object    */
specifier|public
name|ConstantVectorExpression
parameter_list|(
name|int
name|outputColumn
parameter_list|,
name|String
name|typeString
parameter_list|,
name|boolean
name|isNull
parameter_list|)
block|{
name|this
argument_list|(
name|outputColumn
argument_list|,
name|typeString
argument_list|)
expr_stmt|;
name|isNullValue
operator|=
name|isNull
expr_stmt|;
block|}
specifier|private
name|void
name|evaluateLong
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|LongColumnVector
name|cv
init|=
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
name|cv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|cv
operator|.
name|noNulls
operator|=
operator|!
name|isNullValue
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|cv
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|longValue
expr_stmt|;
block|}
else|else
block|{
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateDouble
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|DoubleColumnVector
name|cv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
name|cv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|cv
operator|.
name|noNulls
operator|=
operator|!
name|isNullValue
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|cv
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|doubleValue
expr_stmt|;
block|}
else|else
block|{
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateBytes
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|BytesColumnVector
name|cv
init|=
operator|(
name|BytesColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
name|cv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|cv
operator|.
name|noNulls
operator|=
operator|!
name|isNullValue
expr_stmt|;
name|cv
operator|.
name|initBuffer
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|cv
operator|.
name|setVal
argument_list|(
literal|0
argument_list|,
name|bytesValue
argument_list|,
literal|0
argument_list|,
name|bytesValueLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateDecimal
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|(
name|DecimalColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
name|dcv
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|dcv
operator|.
name|noNulls
operator|=
operator|!
name|isNullValue
expr_stmt|;
if|if
condition|(
operator|!
name|isNullValue
condition|)
block|{
name|dcv
operator|.
name|vector
index|[
literal|0
index|]
operator|.
name|set
argument_list|(
name|decimalValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dcv
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|LONG
case|:
name|evaluateLong
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|evaluateDouble
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTES
case|:
name|evaluateBytes
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|evaluateDecimal
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOutputColumn
parameter_list|()
block|{
return|return
name|outputColumn
return|;
block|}
specifier|public
name|long
name|getLongValue
parameter_list|()
block|{
return|return
name|longValue
return|;
block|}
specifier|public
name|void
name|setLongValue
parameter_list|(
name|long
name|longValue
parameter_list|)
block|{
name|this
operator|.
name|longValue
operator|=
name|longValue
expr_stmt|;
block|}
specifier|public
name|double
name|getDoubleValue
parameter_list|()
block|{
return|return
name|doubleValue
return|;
block|}
specifier|public
name|void
name|setDoubleValue
parameter_list|(
name|double
name|doubleValue
parameter_list|)
block|{
name|this
operator|.
name|doubleValue
operator|=
name|doubleValue
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getBytesValue
parameter_list|()
block|{
return|return
name|bytesValue
return|;
block|}
specifier|public
name|void
name|setBytesValue
parameter_list|(
name|byte
index|[]
name|bytesValue
parameter_list|)
block|{
name|this
operator|.
name|bytesValue
operator|=
name|bytesValue
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|bytesValueLength
operator|=
name|bytesValue
operator|.
name|length
expr_stmt|;
block|}
specifier|public
name|void
name|setDecimalValue
parameter_list|(
name|HiveDecimal
name|decimalValue
parameter_list|)
block|{
name|this
operator|.
name|decimalValue
operator|=
name|decimalValue
expr_stmt|;
block|}
specifier|public
name|String
name|getTypeString
parameter_list|()
block|{
return|return
name|getOutputType
argument_list|()
return|;
block|}
specifier|public
name|void
name|setTypeString
parameter_list|(
name|String
name|typeString
parameter_list|)
block|{
name|this
operator|.
name|outputType
operator|=
name|typeString
expr_stmt|;
if|if
condition|(
name|VectorizationContext
operator|.
name|isStringFamily
argument_list|(
name|typeString
argument_list|)
condition|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|BYTES
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|isFloatFamily
argument_list|(
name|typeString
argument_list|)
condition|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|DOUBLE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|isDecimalFamily
argument_list|(
name|typeString
argument_list|)
condition|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|DECIMAL
expr_stmt|;
block|}
else|else
block|{
comment|// everything else that does not belong to string, double, decimal is treated as long.
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|LONG
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setOutputColumn
parameter_list|(
name|int
name|outputColumn
parameter_list|)
block|{
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
block|}
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|void
name|setType
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOutputType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|setTypeString
argument_list|(
name|type
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
return|return
operator|(
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
operator|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

