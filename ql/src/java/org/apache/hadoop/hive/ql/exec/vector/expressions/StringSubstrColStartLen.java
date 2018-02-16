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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|VectorExpressionDescriptor
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
comment|/**  * This class provides the implementation of vectorized substring, with a start index and length  * parameters. If the start index is invalid (outside of the string boundaries) then an empty  * string will be in the output.  * If the length provided is longer then the string boundary, then it will replace it with the  * ending index.  */
end_comment

begin_class
specifier|public
class|class
name|StringSubstrColStartLen
extends|extends
name|VectorExpression
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|final
name|int
name|colNum
decl_stmt|;
specifier|private
specifier|final
name|int
name|startIdx
decl_stmt|;
specifier|private
specifier|final
name|int
name|length
decl_stmt|;
specifier|private
specifier|final
name|int
index|[]
name|offsetArray
decl_stmt|;
specifier|private
specifier|transient
specifier|static
name|byte
index|[]
name|EMPTY_STRING
decl_stmt|;
comment|// Populating the Empty string bytes. Putting it as static since it should be immutable and can be
comment|// shared
static|static
block|{
try|try
block|{
name|EMPTY_STRING
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
name|StringSubstrColStartLen
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|startIdx
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|outputColumnNum
argument_list|)
expr_stmt|;
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
name|offsetArray
operator|=
operator|new
name|int
index|[
literal|2
index|]
expr_stmt|;
comment|/* Switch from a 1-based start offset (the Hive end user convention) to a 0-based start offset      * (the internal convention).      */
if|if
condition|(
name|startIdx
operator|>=
literal|1
condition|)
block|{
name|this
operator|.
name|startIdx
operator|=
name|startIdx
operator|-
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|startIdx
operator|==
literal|0
condition|)
block|{
comment|// If start index is 0 in query, that is equivalent to using 1 in query.
comment|// So internal offset is 0.
name|this
operator|.
name|startIdx
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
comment|// start index of -n means give the last n characters of the string
name|this
operator|.
name|startIdx
operator|=
name|startIdx
expr_stmt|;
block|}
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
specifier|public
name|StringSubstrColStartLen
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|colNum
operator|=
operator|-
literal|1
expr_stmt|;
name|startIdx
operator|=
operator|-
literal|1
expr_stmt|;
name|length
operator|=
literal|0
expr_stmt|;
name|offsetArray
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Populates the substring start and end offsets based on the substring start and length params.    *    * @param utf8String byte array that holds the utf8 string    * @param start start offset of the byte array the string starts at    * @param len length of the bytes the string holds in the byte array    * @param substrStart the Start index for the substring operation    * @param substrLen the length of the substring    * @param offsetArray the array that indexes are populated to. Assume its length>= 2.    */
specifier|static
name|void
name|populateSubstrOffsets
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
parameter_list|,
name|int
name|substrLength
parameter_list|,
name|int
index|[]
name|offsetArray
parameter_list|)
block|{
name|int
name|curIdx
init|=
operator|-
literal|1
decl_stmt|;
name|offsetArray
index|[
literal|0
index|]
operator|=
operator|-
literal|1
expr_stmt|;
name|offsetArray
index|[
literal|1
index|]
operator|=
operator|-
literal|1
expr_stmt|;
name|int
name|end
init|=
name|start
operator|+
name|len
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
name|length
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|-
name|substrStart
operator|>
name|length
condition|)
block|{
return|return;
block|}
name|substrStart
operator|=
name|length
operator|+
name|substrStart
expr_stmt|;
block|}
if|if
condition|(
name|substrLength
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|int
name|endIdx
init|=
name|substrStart
operator|+
name|substrLength
operator|-
literal|1
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
name|offsetArray
index|[
literal|0
index|]
operator|=
name|i
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|curIdx
operator|-
literal|1
operator|==
name|endIdx
condition|)
block|{
name|offsetArray
index|[
literal|1
index|]
operator|=
name|i
operator|-
name|offsetArray
index|[
literal|0
index|]
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|offsetArray
index|[
literal|1
index|]
operator|==
operator|-
literal|1
condition|)
block|{
name|offsetArray
index|[
literal|1
index|]
operator|=
name|end
operator|-
name|offsetArray
index|[
literal|0
index|]
expr_stmt|;
block|}
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
name|outputColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumnNum
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
name|outputColVector
operator|.
name|initBuffer
argument_list|()
expr_stmt|;
name|boolean
index|[]
name|outputIsNull
init|=
name|outputColVector
operator|.
name|isNull
decl_stmt|;
comment|// We do not need to do a column reset since we are carefully changing the output.
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|inV
operator|.
name|isRepeating
condition|)
block|{
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
name|outputIsNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|setVal
argument_list|(
literal|0
argument_list|,
name|EMPTY_STRING
argument_list|,
literal|0
argument_list|,
name|EMPTY_STRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputIsNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|populateSubstrOffsets
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|,
name|start
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
argument_list|,
name|length
argument_list|,
name|offsetArray
argument_list|)
expr_stmt|;
if|if
condition|(
name|offsetArray
index|[
literal|0
index|]
operator|!=
operator|-
literal|1
condition|)
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
literal|0
argument_list|,
name|vector
index|[
literal|0
index|]
argument_list|,
name|offsetArray
index|[
literal|0
index|]
argument_list|,
name|offsetArray
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
literal|0
argument_list|,
name|EMPTY_STRING
argument_list|,
literal|0
argument_list|,
name|EMPTY_STRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
return|return;
block|}
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
comment|/* there are nulls in the inputColVector */
block|{
comment|// Carefully handle NULLs...
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
name|outputIsNull
index|[
name|selected
index|]
operator|=
literal|false
expr_stmt|;
name|populateSubstrOffsets
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
argument_list|,
name|length
argument_list|,
name|offsetArray
argument_list|)
expr_stmt|;
if|if
condition|(
name|offsetArray
index|[
literal|0
index|]
operator|!=
operator|-
literal|1
condition|)
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
name|selected
argument_list|,
name|vector
index|[
name|selected
index|]
argument_list|,
name|offsetArray
index|[
literal|0
index|]
argument_list|,
name|offsetArray
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
name|selected
argument_list|,
name|EMPTY_STRING
argument_list|,
literal|0
argument_list|,
name|EMPTY_STRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|outputIsNull
index|[
name|selected
index|]
operator|=
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
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
name|outputColVector
operator|.
name|isNull
index|[
name|selected
index|]
operator|=
literal|false
expr_stmt|;
name|populateSubstrOffsets
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
argument_list|,
name|length
argument_list|,
name|offsetArray
argument_list|)
expr_stmt|;
if|if
condition|(
name|offsetArray
index|[
literal|0
index|]
operator|!=
operator|-
literal|1
condition|)
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
name|selected
argument_list|,
name|vector
index|[
name|selected
index|]
argument_list|,
name|offsetArray
index|[
literal|0
index|]
argument_list|,
name|offsetArray
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
name|selected
argument_list|,
name|EMPTY_STRING
argument_list|,
literal|0
argument_list|,
name|EMPTY_STRING
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
comment|/* there are nulls in the inputColVector */
block|{
comment|// Carefully handle NULLs...
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
name|outputIsNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|populateSubstrOffsets
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
argument_list|,
name|length
argument_list|,
name|offsetArray
argument_list|)
expr_stmt|;
if|if
condition|(
name|offsetArray
index|[
literal|0
index|]
operator|!=
operator|-
literal|1
condition|)
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|vector
index|[
name|i
index|]
argument_list|,
name|offsetArray
index|[
literal|0
index|]
argument_list|,
name|offsetArray
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|EMPTY_STRING
argument_list|,
literal|0
argument_list|,
name|EMPTY_STRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|outputIsNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|outputColVector
operator|.
name|noNulls
condition|)
block|{
comment|// Assume it is almost always a performance win to fill all of isNull so we can
comment|// safely reset noNulls.
name|Arrays
operator|.
name|fill
argument_list|(
name|outputIsNull
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
block|}
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
name|populateSubstrOffsets
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
argument_list|,
name|length
argument_list|,
name|offsetArray
argument_list|)
expr_stmt|;
if|if
condition|(
name|offsetArray
index|[
literal|0
index|]
operator|!=
operator|-
literal|1
condition|)
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|vector
index|[
name|i
index|]
argument_list|,
name|offsetArray
index|[
literal|0
index|]
argument_list|,
name|offsetArray
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputColVector
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|EMPTY_STRING
argument_list|,
literal|0
argument_list|,
name|EMPTY_STRING
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
return|return
name|getColumnParamString
argument_list|(
literal|0
argument_list|,
name|colNum
argument_list|)
operator|+
literal|", start "
operator|+
name|startIdx
operator|+
literal|", length "
operator|+
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorExpressionDescriptor
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
name|VectorExpressionDescriptor
operator|.
name|Builder
name|b
init|=
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|b
operator|.
name|setMode
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|Mode
operator|.
name|PROJECTION
argument_list|)
operator|.
name|setNumArguments
argument_list|(
literal|3
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|STRING_FAMILY
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|INT_FAMILY
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|INT_FAMILY
argument_list|)
operator|.
name|setInputExpressionTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|COLUMN
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|SCALAR
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|SCALAR
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

