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
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|CastDecimalToFloat
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
name|CastStringToFloat
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
name|CastLongToFloatViaLongToDouble
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
name|CastTimestampToDouble
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
name|TimestampWritableV2
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
name|lazy
operator|.
name|LazyUtils
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
comment|/**  * UDFToFloat.  *  */
end_comment

begin_class
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|CastTimestampToDouble
operator|.
name|class
block|,
name|CastLongToFloatViaLongToDouble
operator|.
name|class
block|,
name|CastDecimalToFloat
operator|.
name|class
block|,
name|CastStringToFloat
operator|.
name|class
block|}
argument_list|)
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"float"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - converts it's parameter to _FUNC_"
argument_list|,
name|extended
operator|=
literal|"- x is NULL -> NULL\n"
operator|+
literal|"- byte, short, integer, long, float, double, decimal, timestamp:\n"
operator|+
literal|"  x fits into the type _FUNC_ -> x\n"
operator|+
literal|"  undefined otherwise\n"
operator|+
literal|"- boolean:\n"
operator|+
literal|"  true  -> 1.0\n"
operator|+
literal|"  false -> 0.0\n"
operator|+
literal|"- string:\n"
operator|+
literal|"  x is a valid _FUNC_ -> x\n"
operator|+
literal|"  NULL otherwise\n"
operator|+
literal|"Example:\n "
operator|+
literal|"> SELECT _FUNC_(true);\n"
operator|+
literal|"  1.0"
argument_list|)
specifier|public
class|class
name|UDFToFloat
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|FloatWritable
name|floatWritable
init|=
operator|new
name|FloatWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFToFloat
parameter_list|()
block|{   }
comment|/**    * Convert from void to a float. This is called for CAST(... AS FLOAT)    *    * @param i    *          The void value to convert    * @return FloatWritable    */
specifier|public
name|FloatWritable
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
comment|/**    * Convert from boolean to a float. This is called for CAST(... AS FLOAT)    *    * @param i    *          The boolean value to convert    * @return FloatWritable    */
specifier|public
name|FloatWritable
name|evaluate
parameter_list|(
name|BooleanWritable
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
name|floatWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
condition|?
operator|(
name|float
operator|)
literal|1.0
else|:
operator|(
name|float
operator|)
literal|0.0
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
block|}
comment|/**    * Convert from byte to a float. This is called for CAST(... AS FLOAT)    *    * @param i    *          The byte value to convert    * @return FloatWritable    */
specifier|public
name|FloatWritable
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
name|floatWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
block|}
comment|/**    * Convert from short to a float. This is called for CAST(... AS FLOAT)    *    * @param i    *          The short value to convert    * @return FloatWritable    */
specifier|public
name|FloatWritable
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
name|floatWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
block|}
comment|/**    * Convert from integer to a float. This is called for CAST(... AS FLOAT)    *    * @param i    *          The integer value to convert    * @return FloatWritable    */
specifier|public
name|FloatWritable
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
name|floatWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
block|}
comment|/**    * Convert from long to a float. This is called for CAST(... AS FLOAT)    *    * @param i    *          The long value to convert    * @return FloatWritable    */
specifier|public
name|FloatWritable
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
name|floatWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
block|}
comment|/**    * Convert from double to a float. This is called for CAST(... AS FLOAT)    *    * @param i    *          The double value to convert    * @return FloatWritable    */
specifier|public
name|FloatWritable
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
name|floatWritable
operator|.
name|set
argument_list|(
operator|(
name|float
operator|)
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
block|}
comment|/**    * Convert from string to a float. This is called for CAST(... AS FLOAT)    *    * @param i    *          The string value to convert    * @return FloatWritable    */
specifier|public
name|FloatWritable
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
if|if
condition|(
operator|!
name|LazyUtils
operator|.
name|isNumberMaybe
argument_list|(
name|i
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|i
operator|.
name|getLength
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
name|floatWritable
operator|.
name|set
argument_list|(
name|Float
operator|.
name|parseFloat
argument_list|(
name|i
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
comment|// MySQL returns 0 if the string is not a well-formed numeric value.
comment|// But we decided to return NULL instead, which is more conservative.
return|return
literal|null
return|;
block|}
block|}
block|}
specifier|public
name|FloatWritable
name|evaluate
parameter_list|(
name|TimestampWritableV2
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
try|try
block|{
name|floatWritable
operator|.
name|set
argument_list|(
operator|(
name|float
operator|)
name|i
operator|.
name|getDouble
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
comment|// MySQL returns 0 if the string is not a well-formed numeric value.
comment|// But we decided to return NULL instead, which is more conservative.
return|return
literal|null
return|;
block|}
block|}
block|}
specifier|public
name|FloatWritable
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
name|floatWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
block|}
block|}
end_class

end_unit

