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
name|CastDecimalToLong
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
name|CastDoubleToLong
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
name|CastTimestampToLong
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
name|hive
operator|.
name|serde2
operator|.
name|lazy
operator|.
name|LazyLong
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
comment|/**  * UDFToLong.  *  */
end_comment

begin_class
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|CastTimestampToLong
operator|.
name|class
block|,
name|CastDoubleToLong
operator|.
name|class
block|,
name|CastDecimalToLong
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|UDFToLong
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|LongWritable
name|longWritable
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFToLong
parameter_list|()
block|{   }
comment|/**    * Convert from void to a long. This is called for CAST(... AS BIGINT)    *    * @param i    *          The void value to convert    * @return LongWritable    */
specifier|public
name|LongWritable
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
comment|/**    * Convert from boolean to a long. This is called for CAST(... AS BIGINT)    *    * @param i    *          The boolean value to convert    * @return LongWritable    */
specifier|public
name|LongWritable
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
name|longWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
condition|?
operator|(
name|long
operator|)
literal|1
else|:
operator|(
name|long
operator|)
literal|0
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
block|}
block|}
comment|/**    * Convert from byte to a long. This is called for CAST(... AS BIGINT)    *    * @param i    *          The byte value to convert    * @return LongWritable    */
specifier|public
name|LongWritable
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
name|longWritable
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
name|longWritable
return|;
block|}
block|}
comment|/**    * Convert from short to a long. This is called for CAST(... AS BIGINT)    *    * @param i    *          The short value to convert    * @return LongWritable    */
specifier|public
name|LongWritable
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
name|longWritable
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
name|longWritable
return|;
block|}
block|}
comment|/**    * Convert from integer to a long. This is called for CAST(... AS BIGINT)    *    * @param i    *          The integer value to convert    * @return LongWritable    */
specifier|public
name|LongWritable
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
name|longWritable
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
name|longWritable
return|;
block|}
block|}
comment|/**    * Convert from long to a long. This is called for CAST(... AS BIGINT)    *    * @param i    *          The long value to convert    * @return LongWritable    */
specifier|public
name|LongWritable
name|evaluate
parameter_list|(
name|LongWritable
name|i
parameter_list|)
block|{
return|return
name|i
return|;
block|}
comment|/**    * Convert from float to a long. This is called for CAST(... AS BIGINT)    *    * @param i    *          The float value to convert    * @return LongWritable    */
specifier|public
name|LongWritable
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
name|longWritable
operator|.
name|set
argument_list|(
operator|(
name|long
operator|)
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
block|}
block|}
comment|/**    * Convert from double to a long. This is called for CAST(... AS BIGINT)    *    * @param i    *          The double value to convert    * @return LongWritable    */
specifier|public
name|LongWritable
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
name|longWritable
operator|.
name|set
argument_list|(
operator|(
name|long
operator|)
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
block|}
block|}
comment|/**    * Convert from string to a long. This is called for CAST(... AS BIGINT)    *    * @param i    *          The string value to convert    * @return LongWritable    */
specifier|public
name|LongWritable
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
name|longWritable
operator|.
name|set
argument_list|(
name|LazyLong
operator|.
name|parseLong
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
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
comment|// MySQL returns 0 if the string is not a well-formed numeric value.
comment|// return LongWritable.valueOf(0);
comment|// But we decided to return NULL instead, which is more conservative.
return|return
literal|null
return|;
block|}
block|}
block|}
specifier|public
name|LongWritable
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
name|longWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|getSeconds
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
block|}
block|}
specifier|public
name|LongWritable
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
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|longWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
block|}
block|}
block|}
end_class

end_unit

