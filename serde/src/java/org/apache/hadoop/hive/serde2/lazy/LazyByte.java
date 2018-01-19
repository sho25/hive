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
name|serde2
operator|.
name|lazy
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyByteObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyObject for storing a value of Byte.  *   *<p>  * Part of the code is adapted from Apache Harmony Project.  *   * As with the specification, this implementation relied on code laid out in<a  * href="http://www.hackersdelight.org/">Henry S. Warren, Jr.'s Hacker's  * Delight, (Addison Wesley, 2002)</a> as well as<a  * href="http://aggregate.org/MAGIC/">The Aggregate's Magic Algorithms</a>.  *</p>  *   */
end_comment

begin_class
specifier|public
class|class
name|LazyByte
extends|extends
name|LazyPrimitive
argument_list|<
name|LazyByteObjectInspector
argument_list|,
name|ByteWritable
argument_list|>
block|{
specifier|public
name|LazyByte
parameter_list|(
name|LazyByteObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|ByteWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|LazyByte
parameter_list|(
name|LazyByte
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|ByteWritable
argument_list|(
name|copy
operator|.
name|data
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
operator|!
name|LazyUtils
operator|.
name|isNumberMaybe
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
condition|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
return|return;
block|}
try|try
block|{
name|data
operator|.
name|set
argument_list|(
name|parseByte
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
name|logExceptionMessage
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|"TINYINT"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Parses the string argument as if it was a byte value and returns the    * result. Throws NumberFormatException if the string does not represent a    * single byte quantity.    *     * @param bytes    * @param start    * @param length    *          a UTF-8 encoded string representation of a single byte quantity.    * @return byte the value represented by the argument    * @throws NumberFormatException    *           if the argument could not be parsed as a byte quantity.    */
specifier|public
specifier|static
name|byte
name|parseByte
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
block|{
return|return
name|parseByte
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
comment|/**    * Parses the string argument as if it was a byte value and returns the    * result. Throws NumberFormatException if the string does not represent a    * single byte quantity. The second argument specifies the radix to use when    * parsing the value.    *     * @param bytes    * @param start    * @param length    *          a UTF-8 encoded string representation of a single byte quantity.    * @param radix    *          the radix to use when parsing.    * @return byte the value represented by the argument    * @throws NumberFormatException    *           if the argument could not be parsed as a byte quantity.    */
specifier|public
specifier|static
name|byte
name|parseByte
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
block|{
return|return
name|parseByte
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
name|radix
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Parses the string argument as if it was a byte value and returns the    * result. Throws NumberFormatException if the string does not represent a    * single byte quantity. The second argument specifies the radix to use when    * parsing the value.    *    * @param bytes    * @param start    * @param length    *          a UTF-8 encoded string representation of a single byte quantity.    * @param radix    *          the radix to use when parsing.    * @param trim    *          whether to trim leading/trailing whitespace    * @return byte the value represented by the argument    * @throws NumberFormatException    *           if the argument could not be parsed as a byte quantity.    */
specifier|public
specifier|static
name|byte
name|parseByte
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
parameter_list|,
name|boolean
name|trim
parameter_list|)
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
argument_list|,
name|trim
argument_list|)
decl_stmt|;
name|byte
name|result
init|=
operator|(
name|byte
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

