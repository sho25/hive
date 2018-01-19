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
name|common
operator|.
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_comment
comment|/**  * A thread-not-safe version of ByteArrayInputStream, which removes all  * synchronized modifiers.  */
end_comment

begin_class
specifier|public
class|class
name|NonSyncByteArrayInputStream
extends|extends
name|ByteArrayInputStream
block|{
specifier|public
name|NonSyncByteArrayInputStream
parameter_list|()
block|{
name|super
argument_list|(
operator|new
name|byte
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|NonSyncByteArrayInputStream
parameter_list|(
name|byte
index|[]
name|bs
parameter_list|)
block|{
name|super
argument_list|(
name|bs
argument_list|)
expr_stmt|;
block|}
specifier|public
name|NonSyncByteArrayInputStream
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
specifier|public
name|void
name|reset
parameter_list|(
name|byte
index|[]
name|input
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|buf
operator|=
name|input
expr_stmt|;
name|count
operator|=
name|start
operator|+
name|length
expr_stmt|;
name|mark
operator|=
name|start
expr_stmt|;
name|pos
operator|=
name|start
expr_stmt|;
block|}
specifier|public
name|int
name|getPosition
parameter_list|()
block|{
return|return
name|pos
return|;
block|}
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|count
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
block|{
return|return
operator|(
name|pos
operator|<
name|count
operator|)
condition|?
operator|(
name|buf
index|[
name|pos
operator|++
index|]
operator|&
literal|0xff
operator|)
else|:
operator|-
literal|1
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|int
name|read
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
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
block|}
elseif|else
if|if
condition|(
name|off
operator|<
literal|0
operator|||
name|len
argument_list|<
literal|0
operator|||
name|len
argument_list|>
name|b
operator|.
name|length
operator|-
name|off
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|()
throw|;
block|}
if|if
condition|(
name|pos
operator|>=
name|count
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|pos
operator|+
name|len
operator|>
name|count
condition|)
block|{
name|len
operator|=
name|count
operator|-
name|pos
expr_stmt|;
block|}
if|if
condition|(
name|len
operator|<=
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|buf
argument_list|,
name|pos
argument_list|,
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|len
expr_stmt|;
return|return
name|len
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|long
name|skip
parameter_list|(
name|long
name|n
parameter_list|)
block|{
if|if
condition|(
name|pos
operator|+
name|n
operator|>
name|count
condition|)
block|{
name|n
operator|=
name|count
operator|-
name|pos
expr_stmt|;
block|}
if|if
condition|(
name|n
operator|<
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
name|pos
operator|+=
name|n
expr_stmt|;
return|return
name|n
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|int
name|available
parameter_list|()
block|{
return|return
name|count
operator|-
name|pos
return|;
block|}
block|}
end_class

end_unit

