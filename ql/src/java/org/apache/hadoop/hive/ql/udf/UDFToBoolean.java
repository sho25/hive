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
name|udf
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
name|UDF
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
name|VectorizedExpressions
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
name|expressions
operator|.
name|CastDecimalToBoolean
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
name|expressions
operator|.
name|CastStringToLong
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
name|expressions
operator|.
name|gen
operator|.
name|CastDoubleToBooleanViaDoubleToLong
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
name|expressions
operator|.
name|gen
operator|.
name|CastLongToBooleanViaLongToLong
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
name|expressions
operator|.
name|gen
operator|.
name|CastDateToBooleanViaLongToLong
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
name|expressions
operator|.
name|CastTimestampToBoolean
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
name|ByteWritable
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
name|DateWritable
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
name|hive
operator|.
name|serde2
operator|.
name|io
operator|.
name|HiveDecimalWritable
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
name|ShortWritable
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
name|TimestampLocalTZWritable
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
name|TimestampWritable
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
name|BooleanWritable
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
name|FloatWritable
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
name|LongWritable
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
name|NullWritable
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
name|Text
import|;
end_import

begin_comment
comment|/**  * UDFToBoolean.  *  */
end_comment

begin_class
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|CastLongToBooleanViaLongToLong
operator|.
name|class
block|,
name|CastDateToBooleanViaLongToLong
operator|.
name|class
block|,
name|CastTimestampToBoolean
operator|.
name|class
block|,
name|CastDoubleToBooleanViaDoubleToLong
operator|.
name|class
block|,
name|CastDecimalToBoolean
operator|.
name|class
block|,
name|CastStringToLong
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|UDFToBoolean
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|BooleanWritable
name|booleanWritable
init|=
operator|new
name|BooleanWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFToBoolean
parameter_list|()
block|{   }
comment|/**    * Convert a void to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i    *          The value of a void type    * @return BooleanWritable    */
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|NullWritable
name|i
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Convert from a byte to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i    *          The byte value to convert    * @return BooleanWritable    */
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|ByteWritable
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|booleanWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
return|return
name|booleanWritable
return|;
block|}
block|}
comment|/**    * Convert from a short to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i    *          The short value to convert    * @return BooleanWritable    */
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|ShortWritable
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|booleanWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
return|return
name|booleanWritable
return|;
block|}
block|}
comment|/**    * Convert from a integer to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i    *          The integer value to convert    * @return BooleanWritable    */
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|IntWritable
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|booleanWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
return|return
name|booleanWritable
return|;
block|}
block|}
comment|/**    * Convert from a long to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i    *          The long value to convert    * @return BooleanWritable    */
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|LongWritable
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|booleanWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
return|return
name|booleanWritable
return|;
block|}
block|}
comment|/**    * Convert from a float to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i    *          The float value to convert    * @return BooleanWritable    */
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|FloatWritable
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|booleanWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
return|return
name|booleanWritable
return|;
block|}
block|}
comment|/**    * Convert from a double to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i    *          The double value to convert    * @return BooleanWritable    */
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|DoubleWritable
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|booleanWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
return|return
name|booleanWritable
return|;
block|}
block|}
comment|/**    * Convert from a string to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i    *          The string value to convert    * @return BooleanWritable    */
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|Text
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|booleanWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|getLength
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
return|return
name|booleanWritable
return|;
block|}
block|}
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|DateWritable
name|d
parameter_list|)
block|{
comment|// date value to boolean doesn't make any sense.
return|return
literal|null
return|;
block|}
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|TimestampWritable
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|booleanWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|getSeconds
argument_list|()
operator|!=
literal|0
operator|||
name|i
operator|.
name|getNanos
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
return|return
name|booleanWritable
return|;
block|}
block|}
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|HiveDecimalWritable
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
operator|||
operator|!
name|i
operator|.
name|isSet
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|booleanWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|compareTo
argument_list|(
name|HiveDecimal
operator|.
name|ZERO
argument_list|)
operator|!=
literal|0
argument_list|)
expr_stmt|;
return|return
name|booleanWritable
return|;
block|}
block|}
block|}
end_class

end_unit

