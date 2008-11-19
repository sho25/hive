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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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

begin_class
specifier|public
class|class
name|UDFToBoolean
implements|implements
name|UDF
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|UDFToBoolean
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|UDFToBoolean
parameter_list|()
block|{   }
comment|/**    * Convert from a byte to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i The byte value to convert    * @return Boolean    */
specifier|public
name|Boolean
name|evaluate
parameter_list|(
name|Byte
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
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|i
operator|.
name|byteValue
argument_list|()
operator|==
literal|0
argument_list|)
return|;
block|}
block|}
comment|/**    * Convert from a short to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i The short value to convert    * @return Boolean    */
specifier|public
name|Boolean
name|evaluate
parameter_list|(
name|Short
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
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|i
operator|.
name|shortValue
argument_list|()
operator|==
literal|0
argument_list|)
return|;
block|}
block|}
comment|/**    * Convert from a integer to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i The integer value to convert    * @return Boolean    */
specifier|public
name|Boolean
name|evaluate
parameter_list|(
name|Integer
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
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|i
operator|.
name|intValue
argument_list|()
operator|==
literal|0
argument_list|)
return|;
block|}
block|}
comment|/**    * Convert from a long to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i The long value to convert    * @return Boolean    */
specifier|public
name|Boolean
name|evaluate
parameter_list|(
name|Long
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
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|i
operator|.
name|longValue
argument_list|()
operator|==
literal|0
argument_list|)
return|;
block|}
block|}
comment|/**    * Convert from a float to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i The float value to convert    * @return Boolean    */
specifier|public
name|Boolean
name|evaluate
parameter_list|(
name|Float
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
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|i
operator|.
name|floatValue
argument_list|()
operator|==
literal|0
argument_list|)
return|;
block|}
block|}
comment|/**    * Convert from a double to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i The double value to convert    * @return Boolean    */
specifier|public
name|Boolean
name|evaluate
parameter_list|(
name|Double
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
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|i
operator|.
name|doubleValue
argument_list|()
operator|==
literal|0
argument_list|)
return|;
block|}
block|}
comment|/**    * Convert from a string to boolean. This is called for CAST(... AS BOOLEAN)    *    * @param i The string value to convert    * @return Boolean    */
specifier|public
name|Boolean
name|evaluate
parameter_list|(
name|String
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
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|i
operator|.
name|length
argument_list|()
operator|==
literal|0
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

