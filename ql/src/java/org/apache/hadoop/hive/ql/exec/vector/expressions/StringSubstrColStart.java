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
name|exec
operator|.
name|vector
operator|.
name|expressions
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
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
name|BytesColumnVector
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
name|VectorizedRowBatch
import|;
end_import

begin_comment
comment|/**  * This class provides the implementation of vectorized substring, with a single start index  * parameter. If the start index is invalid (outside of the string boundaries) then an empty  * string will be in the output.  */
end_comment

begin_class
specifier|public
class|class
name|StringSubstrColStart
extends|extends
name|VectorExpression
block|{
specifier|private
specifier|final
name|int
name|startIdx
decl_stmt|;
specifier|private
specifier|final
name|int
name|colNum
decl_stmt|;
specifier|private
specifier|final
name|int
name|outputColumn
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|EMPTYSTRING
decl_stmt|;
comment|// Populating the Empty string bytes. Putting it as static since it should be immutable and can
comment|// be shared.
static|static
block|{
try|try
block|{
name|EMPTYSTRING
operator|=
literal|""
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|StringSubstrColStart
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|startIdx
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
name|this
operator|.
name|startIdx
operator|=
name|startIdx
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
block|}
comment|/**    * Given the substring start index param it finds the starting offset of the passed in utf8    * string byte array that matches the index.    * @param utf8String byte array that holds the utf8 string    * @param start start offset of the byte array the string starts at    * @param len length of the bytes the string holds in the byte array    * @param substrStart the Start index for the substring operation    */
specifier|static
name|int
name|getSubstrStartOffset
parameter_list|(
name|byte
index|[]
name|utf8String
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|,
name|int
name|substrStart
parameter_list|)
block|{
name|int
name|curIdx
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|substrStart
operator|<
literal|0
condition|)
block|{
name|int
name|length
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|!=
name|len
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|(
name|utf8String
index|[
name|i
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
operator|++
name|length
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|-
name|length
operator|>
name|substrStart
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|substrStart
operator|=
name|length
operator|+
name|substrStart
expr_stmt|;
block|}
name|int
name|end
init|=
name|start
operator|+
name|len
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|!=
name|end
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|(
name|utf8String
index|[
name|i
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
operator|++
name|curIdx
expr_stmt|;
if|if
condition|(
name|curIdx
operator|==
name|substrStart
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
block|{
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
name|super
operator|.
name|evaluateChildren
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
name|BytesColumnVector
name|inV
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colNum
index|]
decl_stmt|;
name|BytesColumnVector
name|outV
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|byte
index|[]
index|[]
name|vector
init|=
name|inV
operator|.
name|vector
decl_stmt|;
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|int
index|[]
name|len
init|=
name|inV
operator|.
name|length
decl_stmt|;
name|int
index|[]
name|start
init|=
name|inV
operator|.
name|start
decl_stmt|;
if|if
condition|(
name|inV
operator|.
name|isRepeating
condition|)
block|{
name|outV
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|!
name|inV
operator|.
name|noNulls
operator|&&
name|inV
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|outV
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outV
operator|.
name|setRef
argument_list|(
literal|0
argument_list|,
name|EMPTYSTRING
argument_list|,
literal|0
argument_list|,
name|EMPTYSTRING
operator|.
name|length
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|int
name|offset
init|=
name|getSubstrStartOffset
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|,
name|sel
index|[
literal|0
index|]
argument_list|,
name|len
index|[
literal|0
index|]
argument_list|,
name|startIdx
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|!=
operator|-
literal|1
condition|)
block|{
name|outV
operator|.
name|setRef
argument_list|(
literal|0
argument_list|,
name|vector
index|[
literal|0
index|]
argument_list|,
name|offset
argument_list|,
name|len
index|[
literal|0
index|]
operator|-
name|offset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outV
operator|.
name|setRef
argument_list|(
literal|0
argument_list|,
name|EMPTYSTRING
argument_list|,
literal|0
argument_list|,
name|EMPTYSTRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
if|if
condition|(
operator|!
name|inV
operator|.
name|noNulls
condition|)
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
operator|++
name|i
control|)
block|{
name|int
name|selected
init|=
name|sel
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|inV
operator|.
name|isNull
index|[
name|selected
index|]
condition|)
block|{
name|int
name|offset
init|=
name|getSubstrStartOffset
argument_list|(
name|vector
index|[
name|selected
index|]
argument_list|,
name|start
index|[
name|selected
index|]
argument_list|,
name|len
index|[
name|selected
index|]
argument_list|,
name|startIdx
argument_list|)
decl_stmt|;
name|outV
operator|.
name|isNull
index|[
name|selected
index|]
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|offset
operator|!=
operator|-
literal|1
condition|)
block|{
name|outV
operator|.
name|setRef
argument_list|(
name|selected
argument_list|,
name|vector
index|[
name|selected
index|]
argument_list|,
name|offset
argument_list|,
name|len
index|[
name|selected
index|]
operator|-
name|offset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outV
operator|.
name|setRef
argument_list|(
name|selected
argument_list|,
name|EMPTYSTRING
argument_list|,
literal|0
argument_list|,
name|EMPTYSTRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|outV
operator|.
name|isNull
index|[
name|selected
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
operator|++
name|i
control|)
block|{
name|int
name|selected
init|=
name|sel
index|[
name|i
index|]
decl_stmt|;
name|int
name|offset
init|=
name|getSubstrStartOffset
argument_list|(
name|vector
index|[
name|selected
index|]
argument_list|,
name|start
index|[
name|selected
index|]
argument_list|,
name|len
index|[
name|selected
index|]
argument_list|,
name|startIdx
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|!=
operator|-
literal|1
condition|)
block|{
name|outV
operator|.
name|setRef
argument_list|(
name|selected
argument_list|,
name|vector
index|[
name|selected
index|]
argument_list|,
name|offset
argument_list|,
name|len
index|[
name|selected
index|]
operator|-
name|offset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outV
operator|.
name|setRef
argument_list|(
name|selected
argument_list|,
name|EMPTYSTRING
argument_list|,
literal|0
argument_list|,
name|EMPTYSTRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|inV
operator|.
name|noNulls
condition|)
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|inV
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|outV
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
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
operator|!=
name|n
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|inV
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|int
name|offset
init|=
name|getSubstrStartOffset
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|len
index|[
name|i
index|]
argument_list|,
name|startIdx
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|!=
operator|-
literal|1
condition|)
block|{
name|outV
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|vector
index|[
name|i
index|]
argument_list|,
name|offset
argument_list|,
name|len
index|[
name|i
index|]
operator|-
name|offset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outV
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|EMPTYSTRING
argument_list|,
literal|0
argument_list|,
name|EMPTYSTRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
operator|++
name|i
control|)
block|{
name|int
name|offset
init|=
name|getSubstrStartOffset
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|len
index|[
name|i
index|]
argument_list|,
name|startIdx
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|!=
operator|-
literal|1
condition|)
block|{
name|outV
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|vector
index|[
name|i
index|]
argument_list|,
name|offset
argument_list|,
name|len
index|[
name|i
index|]
operator|-
name|offset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outV
operator|.
name|setRef
argument_list|(
name|i
argument_list|,
name|EMPTYSTRING
argument_list|,
literal|0
argument_list|,
name|EMPTYSTRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOutputColumn
parameter_list|()
block|{
return|return
name|outputColumn
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getOutputType
parameter_list|()
block|{
return|return
literal|"string"
return|;
block|}
block|}
end_class

end_unit

