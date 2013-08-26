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

begin_comment
comment|/**  * A streamFactory that writes a sequence of integers. A control byte is written before  * each run with positive values 0 to 127 meaning 3 to 130 repetitions, each  * repetition is offset by a delta. If the control byte is -1 to -128, 1 to 128  * literal vint values follow.  */
end_comment

begin_class
class|class
name|RunLengthIntegerWriter
implements|implements
name|IntegerWriter
block|{
specifier|static
specifier|final
name|int
name|MIN_REPEAT_SIZE
init|=
literal|3
decl_stmt|;
specifier|static
specifier|final
name|int
name|MAX_DELTA
init|=
literal|127
decl_stmt|;
specifier|static
specifier|final
name|int
name|MIN_DELTA
init|=
operator|-
literal|128
decl_stmt|;
specifier|static
specifier|final
name|int
name|MAX_LITERAL_SIZE
init|=
literal|128
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_REPEAT_SIZE
init|=
literal|127
operator|+
name|MIN_REPEAT_SIZE
decl_stmt|;
specifier|private
specifier|final
name|PositionedOutputStream
name|output
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|signed
decl_stmt|;
specifier|private
specifier|final
name|long
index|[]
name|literals
init|=
operator|new
name|long
index|[
name|MAX_LITERAL_SIZE
index|]
decl_stmt|;
specifier|private
name|int
name|numLiterals
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|delta
init|=
literal|0
decl_stmt|;
specifier|private
name|boolean
name|repeat
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|tailRunLength
init|=
literal|0
decl_stmt|;
name|RunLengthIntegerWriter
parameter_list|(
name|PositionedOutputStream
name|output
parameter_list|,
name|boolean
name|signed
parameter_list|)
block|{
name|this
operator|.
name|output
operator|=
name|output
expr_stmt|;
name|this
operator|.
name|signed
operator|=
name|signed
expr_stmt|;
block|}
specifier|private
name|void
name|writeValues
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|numLiterals
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|repeat
condition|)
block|{
name|output
operator|.
name|write
argument_list|(
name|numLiterals
operator|-
name|MIN_REPEAT_SIZE
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
name|delta
argument_list|)
expr_stmt|;
if|if
condition|(
name|signed
condition|)
block|{
name|SerializationUtils
operator|.
name|writeVslong
argument_list|(
name|output
argument_list|,
name|literals
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|SerializationUtils
operator|.
name|writeVulong
argument_list|(
name|output
argument_list|,
name|literals
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|output
operator|.
name|write
argument_list|(
operator|-
name|numLiterals
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numLiterals
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|signed
condition|)
block|{
name|SerializationUtils
operator|.
name|writeVslong
argument_list|(
name|output
argument_list|,
name|literals
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|SerializationUtils
operator|.
name|writeVulong
argument_list|(
name|output
argument_list|,
name|literals
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|repeat
operator|=
literal|false
expr_stmt|;
name|numLiterals
operator|=
literal|0
expr_stmt|;
name|tailRunLength
operator|=
literal|0
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
name|writeValues
argument_list|()
expr_stmt|;
name|output
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|long
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|numLiterals
operator|==
literal|0
condition|)
block|{
name|literals
index|[
name|numLiterals
operator|++
index|]
operator|=
name|value
expr_stmt|;
name|tailRunLength
operator|=
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|repeat
condition|)
block|{
if|if
condition|(
name|value
operator|==
name|literals
index|[
literal|0
index|]
operator|+
name|delta
operator|*
name|numLiterals
condition|)
block|{
name|numLiterals
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|numLiterals
operator|==
name|MAX_REPEAT_SIZE
condition|)
block|{
name|writeValues
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|writeValues
argument_list|()
expr_stmt|;
name|literals
index|[
name|numLiterals
operator|++
index|]
operator|=
name|value
expr_stmt|;
name|tailRunLength
operator|=
literal|1
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|tailRunLength
operator|==
literal|1
condition|)
block|{
name|delta
operator|=
name|value
operator|-
name|literals
index|[
name|numLiterals
operator|-
literal|1
index|]
expr_stmt|;
if|if
condition|(
name|delta
argument_list|<
name|MIN_DELTA
operator|||
name|delta
argument_list|>
name|MAX_DELTA
condition|)
block|{
name|tailRunLength
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|tailRunLength
operator|=
literal|2
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|value
operator|==
name|literals
index|[
name|numLiterals
operator|-
literal|1
index|]
operator|+
name|delta
condition|)
block|{
name|tailRunLength
operator|+=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|delta
operator|=
name|value
operator|-
name|literals
index|[
name|numLiterals
operator|-
literal|1
index|]
expr_stmt|;
if|if
condition|(
name|delta
argument_list|<
name|MIN_DELTA
operator|||
name|delta
argument_list|>
name|MAX_DELTA
condition|)
block|{
name|tailRunLength
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|tailRunLength
operator|=
literal|2
expr_stmt|;
block|}
block|}
if|if
condition|(
name|tailRunLength
operator|==
name|MIN_REPEAT_SIZE
condition|)
block|{
if|if
condition|(
name|numLiterals
operator|+
literal|1
operator|==
name|MIN_REPEAT_SIZE
condition|)
block|{
name|repeat
operator|=
literal|true
expr_stmt|;
name|numLiterals
operator|+=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|numLiterals
operator|-=
name|MIN_REPEAT_SIZE
operator|-
literal|1
expr_stmt|;
name|long
name|base
init|=
name|literals
index|[
name|numLiterals
index|]
decl_stmt|;
name|writeValues
argument_list|()
expr_stmt|;
name|literals
index|[
literal|0
index|]
operator|=
name|base
expr_stmt|;
name|repeat
operator|=
literal|true
expr_stmt|;
name|numLiterals
operator|=
name|MIN_REPEAT_SIZE
expr_stmt|;
block|}
block|}
else|else
block|{
name|literals
index|[
name|numLiterals
operator|++
index|]
operator|=
name|value
expr_stmt|;
if|if
condition|(
name|numLiterals
operator|==
name|MAX_LITERAL_SIZE
condition|)
block|{
name|writeValues
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
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
name|numLiterals
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

