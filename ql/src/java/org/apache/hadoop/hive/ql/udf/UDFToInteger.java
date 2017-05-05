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
name|ql
operator|.
name|io
operator|.
name|RecordIdentifier
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
name|LazyInteger
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
comment|/**  * UDFToInteger.  *  */
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
block|,
name|CastStringToLong
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|UDFToInteger
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|IntWritable
name|intWritable
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFToInteger
parameter_list|()
block|{   }
comment|/**    * Convert from void to an integer. This is called for CAST(... AS INT)    *    * @param i    *          The void value to convert    * @return Integer    */
specifier|public
name|IntWritable
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
comment|/**    * Convert from boolean to an integer. This is called for CAST(... AS INT)    *    * @param i    *          The boolean value to convert    * @return IntWritable    */
specifier|public
name|IntWritable
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
name|intWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|get
argument_list|()
condition|?
literal|1
else|:
literal|0
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
block|}
comment|/**    * Convert from byte to an integer. This is called for CAST(... AS INT)    *    * @param i    *          The byte value to convert    * @return IntWritable    */
specifier|public
name|IntWritable
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
name|intWritable
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
name|intWritable
return|;
block|}
block|}
comment|/**    * Convert from short to an integer. This is called for CAST(... AS INT)    *    * @param i    *          The short value to convert    * @return IntWritable    */
specifier|public
name|IntWritable
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
name|intWritable
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
name|intWritable
return|;
block|}
block|}
comment|/**    * Convert from long to an integer. This is called for CAST(... AS INT)    *    * @param i    *          The long value to convert    * @return IntWritable    */
specifier|public
name|IntWritable
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
name|intWritable
operator|.
name|set
argument_list|(
operator|(
name|int
operator|)
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
block|}
comment|/**    * Convert from float to an integer. This is called for CAST(... AS INT)    *    * @param i    *          The float value to convert    * @return IntWritable    */
specifier|public
name|IntWritable
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
name|intWritable
operator|.
name|set
argument_list|(
operator|(
name|int
operator|)
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
block|}
comment|/**    * Convert from double to an integer. This is called for CAST(... AS INT)    *    * @param i    *          The double value to convert    * @return IntWritable    */
specifier|public
name|IntWritable
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
name|intWritable
operator|.
name|set
argument_list|(
operator|(
name|int
operator|)
name|i
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
block|}
comment|/**    * Convert from string to an integer. This is called for CAST(... AS INT)    *    * @param i    *          The string value to convert    * @return IntWritable    */
specifier|public
name|IntWritable
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
name|intWritable
operator|.
name|set
argument_list|(
name|LazyInteger
operator|.
name|parseInt
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
name|intWritable
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
comment|// MySQL returns 0 if the string is not a well-formed numeric value.
comment|// return IntWritable.valueOf(0);
comment|// But we decided to return NULL instead, which is more conservative.
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * Convert from Timestamp to an integer. This is called for CAST(... AS INT)    *    * @param i    *          The Timestamp value to convert    * @return IntWritable    */
specifier|public
name|IntWritable
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
name|intWritable
operator|.
name|set
argument_list|(
operator|(
name|int
operator|)
name|i
operator|.
name|getSeconds
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
block|}
specifier|public
name|IntWritable
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
operator|||
operator|!
name|i
operator|.
name|isInt
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|intWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
block|}
comment|/**    * Convert a RecordIdentifier.  This is done so that we can use the RecordIdentifier in place    * of the bucketing column.    * @param i RecordIdentifier to convert    * @return value of the bucket identifier    */
specifier|public
name|IntWritable
name|evaluate
parameter_list|(
name|RecordIdentifier
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
name|intWritable
operator|.
name|set
argument_list|(
name|i
operator|.
name|getBucketId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
block|}
block|}
end_class

end_unit

