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
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
import|;
end_import

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
name|NonSyncByteArrayOutputStream
import|;
end_import

begin_comment
comment|/**  * A thread-not-safe version of Hadoop's DataOutputBuffer, which removes all  * synchronized modifiers.  */
end_comment

begin_class
specifier|public
class|class
name|NonSyncDataOutputBuffer
extends|extends
name|DataOutputStream
block|{
specifier|private
specifier|final
name|NonSyncByteArrayOutputStream
name|buffer
decl_stmt|;
comment|/** Constructs a new empty buffer. */
specifier|public
name|NonSyncDataOutputBuffer
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|NonSyncByteArrayOutputStream
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|NonSyncDataOutputBuffer
parameter_list|(
name|NonSyncByteArrayOutputStream
name|buffer
parameter_list|)
block|{
name|super
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|this
operator|.
name|buffer
operator|=
name|buffer
expr_stmt|;
block|}
comment|/**    * Returns the current contents of the buffer. Data is only valid to    * {@link #getLength()}.    */
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
name|buffer
operator|.
name|getData
argument_list|()
return|;
block|}
comment|/** Returns the length of the valid data currently in the buffer. */
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|buffer
operator|.
name|getLength
argument_list|()
return|;
block|}
comment|/** Resets the buffer to empty. */
specifier|public
name|NonSyncDataOutputBuffer
name|reset
parameter_list|()
block|{
name|written
operator|=
literal|0
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Writes bytes from a DataInput directly into the buffer. */
specifier|public
name|void
name|write
parameter_list|(
name|DataInput
name|in
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|buffer
operator|.
name|write
argument_list|(
name|in
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|buffer
operator|.
name|write
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|incCount
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
throws|throws
name|IOException
block|{
name|buffer
operator|.
name|write
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|incCount
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|incCount
parameter_list|(
name|int
name|value
parameter_list|)
block|{
if|if
condition|(
name|written
operator|+
name|value
operator|<
literal|0
condition|)
block|{
name|written
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
else|else
block|{
name|written
operator|+=
name|value
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

