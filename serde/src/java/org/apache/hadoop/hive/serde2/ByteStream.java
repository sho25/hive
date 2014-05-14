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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|io
operator|.
name|NonSyncByteArrayInputStream
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
name|io
operator|.
name|NonSyncByteArrayOutputStream
import|;
end_import

begin_comment
comment|/**  * Extensions to bytearrayinput/output streams.  */
end_comment

begin_class
specifier|public
class|class
name|ByteStream
block|{
comment|/**    * Input.    *    */
specifier|public
specifier|static
class|class
name|Input
extends|extends
name|NonSyncByteArrayInputStream
block|{
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
name|buf
return|;
block|}
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|count
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|(
name|byte
index|[]
name|argBuf
parameter_list|,
name|int
name|argCount
parameter_list|)
block|{
name|buf
operator|=
name|argBuf
expr_stmt|;
name|mark
operator|=
name|pos
operator|=
literal|0
expr_stmt|;
name|count
operator|=
name|argCount
expr_stmt|;
block|}
specifier|public
name|Input
parameter_list|()
block|{
name|super
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Input
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
block|{
name|super
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Input
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Output.    *    */
specifier|public
specifier|static
specifier|final
class|class
name|Output
extends|extends
name|NonSyncByteArrayOutputStream
implements|implements
name|RandomAccessOutput
block|{
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
name|buf
return|;
block|}
specifier|public
name|Output
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Output
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeInt
parameter_list|(
name|long
name|offset
parameter_list|,
name|int
name|value
parameter_list|)
block|{
name|int
name|offset2
init|=
operator|(
name|int
operator|)
name|offset
decl_stmt|;
name|getData
argument_list|()
index|[
name|offset2
operator|++
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|value
operator|>>
literal|24
argument_list|)
expr_stmt|;
name|getData
argument_list|()
index|[
name|offset2
operator|++
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|value
operator|>>
literal|16
argument_list|)
expr_stmt|;
name|getData
argument_list|()
index|[
name|offset2
operator|++
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|value
operator|>>
literal|8
argument_list|)
expr_stmt|;
name|getData
argument_list|()
index|[
name|offset2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reserve
parameter_list|(
name|int
name|byteCount
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|byteCount
condition|;
operator|++
name|i
control|)
block|{
name|write
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
interface|interface
name|RandomAccessOutput
block|{
specifier|public
name|void
name|writeInt
parameter_list|(
name|long
name|offset
parameter_list|,
name|int
name|value
parameter_list|)
function_decl|;
specifier|public
name|void
name|reserve
parameter_list|(
name|int
name|byteCount
parameter_list|)
function_decl|;
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
function_decl|;
specifier|public
name|void
name|write
parameter_list|(
name|byte
name|b
index|[]
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
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
function_decl|;
specifier|public
name|int
name|getLength
parameter_list|()
function_decl|;
block|}
block|}
end_class

end_unit

