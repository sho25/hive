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
name|serde2
operator|.
name|io
operator|.
name|HiveDecimalWritable
import|;
end_import

begin_comment
comment|/**  * Type cast decimal to long  */
end_comment

begin_class
specifier|public
class|class
name|CastDecimalToLong
extends|extends
name|FuncDecimalToLong
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
name|CastDecimalToLong
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CastDecimalToLong
parameter_list|(
name|int
name|inputColumn
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|inputColumn
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|func
parameter_list|(
name|LongColumnVector
name|outV
parameter_list|,
name|DecimalColumnVector
name|inV
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|HiveDecimalWritable
name|decWritable
init|=
name|inV
operator|.
name|vector
index|[
name|i
index|]
decl_stmt|;
comment|// Check based on the Hive integer type we need to test with isByte, isShort, isInt, isLong
comment|// so we do not use corrupted (truncated) values for the Hive integer type.
name|boolean
name|isInRange
decl_stmt|;
switch|switch
condition|(
name|integerPrimitiveCategory
condition|)
block|{
case|case
name|BYTE
case|:
name|isInRange
operator|=
name|decWritable
operator|.
name|isByte
argument_list|()
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|isInRange
operator|=
name|decWritable
operator|.
name|isShort
argument_list|()
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|isInRange
operator|=
name|decWritable
operator|.
name|isInt
argument_list|()
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|isInRange
operator|=
name|decWritable
operator|.
name|isLong
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected integer primitive category "
operator|+
name|integerPrimitiveCategory
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|isInRange
condition|)
block|{
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
return|return;
block|}
switch|switch
condition|(
name|integerPrimitiveCategory
condition|)
block|{
case|case
name|BYTE
case|:
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|decWritable
operator|.
name|byteValue
argument_list|()
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|decWritable
operator|.
name|shortValue
argument_list|()
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|decWritable
operator|.
name|intValue
argument_list|()
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|decWritable
operator|.
name|longValue
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected integer primitive category "
operator|+
name|integerPrimitiveCategory
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

