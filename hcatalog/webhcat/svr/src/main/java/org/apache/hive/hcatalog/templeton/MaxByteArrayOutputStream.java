begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_comment
comment|/**  * An output stream that will only accept the first N bytes of data.  */
end_comment

begin_class
specifier|public
class|class
name|MaxByteArrayOutputStream
extends|extends
name|ByteArrayOutputStream
block|{
comment|/**      * The max number of bytes stored.      */
specifier|private
name|int
name|maxBytes
decl_stmt|;
comment|/**      * The number of bytes currently stored.      */
specifier|private
name|int
name|nBytes
decl_stmt|;
comment|/**      * Create.      */
specifier|public
name|MaxByteArrayOutputStream
parameter_list|(
name|int
name|maxBytes
parameter_list|)
block|{
name|this
operator|.
name|maxBytes
operator|=
name|maxBytes
expr_stmt|;
name|nBytes
operator|=
literal|0
expr_stmt|;
block|}
comment|/**      * Writes the specified byte to this byte array output stream.      * Any bytes after the first maxBytes will be ignored.      *      * @param   b   the byte to be written.      */
specifier|public
specifier|synchronized
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
block|{
if|if
condition|(
name|nBytes
operator|<
name|maxBytes
condition|)
block|{
operator|++
name|nBytes
expr_stmt|;
name|super
operator|.
name|write
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Writes<code>len</code> bytes from the specified byte array      * starting at offset<code>off</code> to this byte array output stream.      * Any bytes after the first maxBytes will be ignored.      *      * @param   b     the data.      * @param   off   the start offset in the data.      * @param   len   the number of bytes to write.      */
specifier|public
specifier|synchronized
name|void
name|write
parameter_list|(
name|byte
name|b
index|[]
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|int
name|storable
init|=
name|Math
operator|.
name|min
argument_list|(
name|maxBytes
operator|-
name|nBytes
argument_list|,
name|len
argument_list|)
decl_stmt|;
if|if
condition|(
name|storable
operator|>
literal|0
condition|)
block|{
name|nBytes
operator|+=
name|storable
expr_stmt|;
name|super
operator|.
name|write
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|storable
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

