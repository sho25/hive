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
name|serde2
operator|.
name|lazy
package|;
end_package

begin_comment
comment|/**  * LazyObject for storing a value of Short.  *   *<p>  * Part of the code is adapted from Apache Harmony Project.  *   * As with the specification, this implementation relied on code laid out in<a  * href="http://www.hackersdelight.org/">Henry S. Warren, Jr.'s Hacker's  * Delight, (Addison Wesley, 2002)</a> as well as<a  * href="http://aggregate.org/MAGIC/">The Aggregate's Magic Algorithms</a>.  *</p>  *   */
end_comment

begin_class
specifier|public
class|class
name|LazyShort
extends|extends
name|LazyPrimitive
argument_list|<
name|Short
argument_list|>
block|{
specifier|public
name|LazyShort
parameter_list|()
block|{
name|super
argument_list|(
name|Short
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Short
name|getPrimitiveObject
parameter_list|()
block|{
try|try
block|{
comment|// Slower method: convert to String and then convert to Integer
comment|// return Short.valueOf(LazyUtils.convertToString(bytes, start, length));
return|return
name|Short
operator|.
name|valueOf
argument_list|(
name|parseShort
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Parses the string argument as if it was a short value and returns the    * result. Throws NumberFormatException if the string does not represent an    * short quantity.    *     * @param bytes    * @param start    * @param length    *            a UTF-8 encoded string representation of a short quantity.    * @return short the value represented by the argument    * @exception NumberFormatException    *                if the argument could not be parsed as a short quantity.    */
specifier|public
specifier|static
name|short
name|parseShort
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|NumberFormatException
block|{
return|return
name|parseShort
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|10
argument_list|)
return|;
block|}
comment|/**    * Parses the string argument as if it was a short value and returns the    * result. Throws NumberFormatException if the string does not represent a    * single short quantity. The second argument specifies the radix to use    * when parsing the value.    *     * @param bytes    * @param start    * @param length    *            a UTF-8 encoded string representation of a short quantity.    * @param radix    *            the radix to use when parsing.    * @return short the value represented by the argument    * @exception NumberFormatException    *                if the argument could not be parsed as a short quantity.    */
specifier|public
specifier|static
name|short
name|parseShort
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|radix
parameter_list|)
throws|throws
name|NumberFormatException
block|{
name|int
name|intValue
init|=
name|LazyInteger
operator|.
name|parseInt
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
name|radix
argument_list|)
decl_stmt|;
name|short
name|result
init|=
operator|(
name|short
operator|)
name|intValue
decl_stmt|;
if|if
condition|(
name|result
operator|==
name|intValue
condition|)
block|{
return|return
name|result
return|;
block|}
throw|throw
operator|new
name|NumberFormatException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

