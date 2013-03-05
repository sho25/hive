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
name|io
operator|.
name|orc
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

begin_class
class|class
name|BitFieldWriter
block|{
specifier|private
name|RunLengthByteWriter
name|output
decl_stmt|;
specifier|private
specifier|final
name|int
name|bitSize
decl_stmt|;
specifier|private
name|byte
name|current
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|bitsLeft
init|=
literal|8
decl_stmt|;
name|BitFieldWriter
parameter_list|(
name|PositionedOutputStream
name|output
parameter_list|,
name|int
name|bitSize
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|output
operator|=
operator|new
name|RunLengthByteWriter
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|this
operator|.
name|bitSize
operator|=
name|bitSize
expr_stmt|;
block|}
specifier|private
name|void
name|writeByte
parameter_list|()
throws|throws
name|IOException
block|{
name|output
operator|.
name|write
argument_list|(
name|current
argument_list|)
expr_stmt|;
name|current
operator|=
literal|0
expr_stmt|;
name|bitsLeft
operator|=
literal|8
expr_stmt|;
block|}
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|bitsLeft
operator|!=
literal|8
condition|)
block|{
name|writeByte
argument_list|()
expr_stmt|;
block|}
name|output
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
name|void
name|write
parameter_list|(
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|bitsToWrite
init|=
name|bitSize
decl_stmt|;
while|while
condition|(
name|bitsToWrite
operator|>
name|bitsLeft
condition|)
block|{
comment|// add the bits to the bottom of the current word
name|current
operator||=
name|value
operator|>>>
operator|(
name|bitsToWrite
operator|-
name|bitsLeft
operator|)
expr_stmt|;
comment|// subtract out the bits we just added
name|bitsToWrite
operator|-=
name|bitsLeft
expr_stmt|;
comment|// zero out the bits above bitsToWrite
name|value
operator|&=
operator|(
literal|1
operator|<<
name|bitsToWrite
operator|)
operator|-
literal|1
expr_stmt|;
name|writeByte
argument_list|()
expr_stmt|;
block|}
name|bitsLeft
operator|-=
name|bitsToWrite
expr_stmt|;
name|current
operator||=
name|value
operator|<<
name|bitsLeft
expr_stmt|;
if|if
condition|(
name|bitsLeft
operator|==
literal|0
condition|)
block|{
name|writeByte
argument_list|()
expr_stmt|;
block|}
block|}
name|void
name|getPosition
parameter_list|(
name|PositionRecorder
name|recorder
parameter_list|)
throws|throws
name|IOException
block|{
name|output
operator|.
name|getPosition
argument_list|(
name|recorder
argument_list|)
expr_stmt|;
name|recorder
operator|.
name|addPosition
argument_list|(
literal|8
operator|-
name|bitsLeft
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

