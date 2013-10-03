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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
import|;
end_import

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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|CharBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharsetDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CodingErrorAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * An abstract class for LIKE and REGEXP expressions. LIKE and REGEXP expression share similar  * functions, but they have different grammars. AbstractFilterStringColLikeStringScalar class  * provides shared classes and methods. Each subclass handles its grammar.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractFilterStringColLikeStringScalar
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
name|int
name|colNum
decl_stmt|;
specifier|private
name|String
name|pattern
decl_stmt|;
specifier|transient
name|Checker
name|checker
decl_stmt|;
specifier|public
name|AbstractFilterStringColLikeStringScalar
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|AbstractFilterStringColLikeStringScalar
parameter_list|(
name|int
name|colNum
parameter_list|,
name|String
name|pattern
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
name|pattern
operator|=
name|pattern
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|List
argument_list|<
name|CheckerFactory
argument_list|>
name|getCheckerFactories
parameter_list|()
function_decl|;
comment|/**    * Selects an optimized checker for a given string.    * @param pattern    * @return    */
specifier|private
name|Checker
name|createChecker
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
for|for
control|(
name|CheckerFactory
name|checkerFactory
range|:
name|getCheckerFactories
argument_list|()
control|)
block|{
name|Checker
name|checker
init|=
name|checkerFactory
operator|.
name|tryCreate
argument_list|(
name|pattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|checker
operator|!=
literal|null
condition|)
block|{
return|return
name|checker
return|;
block|}
block|}
return|return
literal|null
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
name|this
operator|.
name|checker
operator|=
name|createChecker
argument_list|(
name|pattern
argument_list|)
expr_stmt|;
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
name|inputColVector
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
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|boolean
index|[]
name|nullPos
init|=
name|inputColVector
operator|.
name|isNull
decl_stmt|;
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
name|byte
index|[]
index|[]
name|vector
init|=
name|inputColVector
operator|.
name|vector
decl_stmt|;
name|int
index|[]
name|length
init|=
name|inputColVector
operator|.
name|length
decl_stmt|;
name|int
index|[]
name|start
init|=
name|inputColVector
operator|.
name|start
decl_stmt|;
comment|// return immediately if batch is empty
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|inputColVector
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
comment|// All must be selected otherwise size would be zero Repeating property will not change.
if|if
condition|(
operator|!
name|checker
operator|.
name|check
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
name|length
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
comment|// Entire batch is filtered out.
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
name|checker
operator|.
name|check
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
name|length
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
block|}
else|else
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
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
name|i
operator|++
control|)
block|{
if|if
condition|(
name|checker
operator|.
name|check
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
name|length
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newSize
operator|<
name|n
condition|)
block|{
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
comment|//All must be selected otherwise size would be zero. Repeating property will not change.
if|if
condition|(
operator|!
name|nullPos
index|[
literal|0
index|]
condition|)
block|{
if|if
condition|(
operator|!
name|checker
operator|.
name|check
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
name|length
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
comment|//Entire batch is filtered out.
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
block|}
else|else
block|{
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|nullPos
index|[
name|i
index|]
condition|)
block|{
if|if
condition|(
name|checker
operator|.
name|check
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
name|length
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
comment|//Change the selected vector
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
block|}
else|else
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
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
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|nullPos
index|[
name|i
index|]
condition|)
block|{
if|if
condition|(
name|checker
operator|.
name|check
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
name|length
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|newSize
operator|<
name|n
condition|)
block|{
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
block|}
comment|/* If every row qualified (newSize==n), then we can ignore the sel vector to streamline          * future operations. So selectedInUse will remain false.          */
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
operator|-
literal|1
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
literal|"boolean"
return|;
block|}
comment|/**    * A Checker contains a pattern and checks whether a given string matches or not.    */
specifier|public
interface|interface
name|Checker
block|{
comment|/**      * Checks whether the given string matches with its pattern.      * @param byteS The byte array that contains the string      * @param start The start position of the string      * @param len The length of the string      * @return Whether it matches or not.      */
name|boolean
name|check
parameter_list|(
name|byte
index|[]
name|byteS
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
function_decl|;
block|}
comment|/**    * A CheckerFactory creates checkers of its kind.    */
specifier|protected
interface|interface
name|CheckerFactory
block|{
comment|/**      * If the given pattern is acceptable for its checker class, it creates and returns a checker.      * Otherwise, it returns<code>null</code>.      * @param pattern      * @return If the pattern is acceptable, a<code>Checker</code> object. Otherwise      *<code>null</code>.      */
name|Checker
name|tryCreate
parameter_list|(
name|String
name|pattern
parameter_list|)
function_decl|;
block|}
comment|/**    * Matches the whole string to its pattern.    */
specifier|protected
specifier|static
class|class
name|NoneChecker
implements|implements
name|Checker
block|{
name|byte
index|[]
name|byteSub
decl_stmt|;
name|NoneChecker
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
try|try
block|{
name|byteSub
operator|=
name|pattern
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|check
parameter_list|(
name|byte
index|[]
name|byteS
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|int
name|lenSub
init|=
name|byteSub
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|len
operator|!=
name|lenSub
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|i
init|=
name|start
init|,
name|j
init|=
literal|0
init|;
name|j
operator|<
name|len
condition|;
name|i
operator|++
operator|,
name|j
operator|++
control|)
block|{
if|if
condition|(
name|byteS
index|[
name|i
index|]
operator|!=
name|byteSub
index|[
name|j
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Matches the beginning of each string to a pattern.    */
specifier|protected
specifier|static
class|class
name|BeginChecker
implements|implements
name|Checker
block|{
name|byte
index|[]
name|byteSub
decl_stmt|;
name|BeginChecker
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
try|try
block|{
name|byteSub
operator|=
name|pattern
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|check
parameter_list|(
name|byte
index|[]
name|byteS
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
if|if
condition|(
name|len
operator|<
name|byteSub
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|i
init|=
name|start
init|,
name|j
init|=
literal|0
init|;
name|j
operator|<
name|byteSub
operator|.
name|length
condition|;
name|i
operator|++
operator|,
name|j
operator|++
control|)
block|{
if|if
condition|(
name|byteS
index|[
name|i
index|]
operator|!=
name|byteSub
index|[
name|j
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Matches the ending of each string to its pattern.    */
specifier|protected
specifier|static
class|class
name|EndChecker
implements|implements
name|Checker
block|{
name|byte
index|[]
name|byteSub
decl_stmt|;
name|EndChecker
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
try|try
block|{
name|byteSub
operator|=
name|pattern
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|check
parameter_list|(
name|byte
index|[]
name|byteS
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|int
name|lenSub
init|=
name|byteSub
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|len
operator|<
name|lenSub
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|i
init|=
name|start
operator|+
name|len
operator|-
name|lenSub
init|,
name|j
init|=
literal|0
init|;
name|j
operator|<
name|lenSub
condition|;
name|i
operator|++
operator|,
name|j
operator|++
control|)
block|{
if|if
condition|(
name|byteS
index|[
name|i
index|]
operator|!=
name|byteSub
index|[
name|j
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
comment|/**    * Matches the middle of each string to its pattern.    */
specifier|protected
specifier|static
class|class
name|MiddleChecker
implements|implements
name|Checker
block|{
name|byte
index|[]
name|byteSub
decl_stmt|;
name|int
name|lenSub
decl_stmt|;
name|MiddleChecker
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
try|try
block|{
name|byteSub
operator|=
name|pattern
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
expr_stmt|;
name|lenSub
operator|=
name|byteSub
operator|.
name|length
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|check
parameter_list|(
name|byte
index|[]
name|byteS
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
if|if
condition|(
name|len
operator|<
name|lenSub
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|end
init|=
name|start
operator|+
name|len
operator|-
name|lenSub
operator|+
literal|1
decl_stmt|;
name|boolean
name|match
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
name|match
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|lenSub
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|byteS
index|[
name|i
operator|+
name|j
index|]
operator|!=
name|byteSub
index|[
name|j
index|]
condition|)
block|{
name|match
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|match
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
name|match
return|;
block|}
block|}
comment|/**    * Matches each string to a pattern with Java regular expression package.    */
specifier|protected
specifier|static
class|class
name|ComplexChecker
implements|implements
name|Checker
block|{
name|Pattern
name|compiledPattern
decl_stmt|;
name|Matcher
name|matcher
decl_stmt|;
name|FastUTF8Decoder
name|decoder
decl_stmt|;
name|ComplexChecker
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|compiledPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|pattern
argument_list|)
expr_stmt|;
name|matcher
operator|=
name|compiledPattern
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|decoder
operator|=
operator|new
name|FastUTF8Decoder
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|check
parameter_list|(
name|byte
index|[]
name|byteS
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
comment|// Match the given bytes with the like pattern
name|matcher
operator|.
name|reset
argument_list|(
name|decoder
operator|.
name|decodeUnsafely
argument_list|(
name|byteS
argument_list|,
name|start
argument_list|,
name|len
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|matcher
operator|.
name|matches
argument_list|()
return|;
block|}
block|}
comment|/**    * A fast UTF-8 decoder that caches necessary objects for decoding.    */
specifier|private
specifier|static
class|class
name|FastUTF8Decoder
block|{
name|CharsetDecoder
name|decoder
decl_stmt|;
name|ByteBuffer
name|byteBuffer
decl_stmt|;
name|CharBuffer
name|charBuffer
decl_stmt|;
specifier|public
name|FastUTF8Decoder
parameter_list|()
block|{
name|decoder
operator|=
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|newDecoder
argument_list|()
operator|.
name|onMalformedInput
argument_list|(
name|CodingErrorAction
operator|.
name|REPLACE
argument_list|)
operator|.
name|onUnmappableCharacter
argument_list|(
name|CodingErrorAction
operator|.
name|REPLACE
argument_list|)
expr_stmt|;
name|byteBuffer
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|charBuffer
operator|=
name|CharBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CharBuffer
name|decodeUnsafely
parameter_list|(
name|byte
index|[]
name|byteS
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
comment|// Prepare buffers
if|if
condition|(
name|byteBuffer
operator|.
name|capacity
argument_list|()
operator|<
name|len
condition|)
block|{
name|byteBuffer
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|len
operator|*
literal|2
argument_list|)
expr_stmt|;
block|}
name|byteBuffer
operator|.
name|clear
argument_list|()
expr_stmt|;
name|byteBuffer
operator|.
name|put
argument_list|(
name|byteS
argument_list|,
name|start
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|byteBuffer
operator|.
name|flip
argument_list|()
expr_stmt|;
name|int
name|maxChars
init|=
call|(
name|int
call|)
argument_list|(
name|byteBuffer
operator|.
name|capacity
argument_list|()
operator|*
name|decoder
operator|.
name|maxCharsPerByte
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|charBuffer
operator|.
name|capacity
argument_list|()
operator|<
name|maxChars
condition|)
block|{
name|charBuffer
operator|=
name|CharBuffer
operator|.
name|allocate
argument_list|(
name|maxChars
argument_list|)
expr_stmt|;
block|}
name|charBuffer
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Decode UTF-8
name|decoder
operator|.
name|reset
argument_list|()
expr_stmt|;
name|decoder
operator|.
name|decode
argument_list|(
name|byteBuffer
argument_list|,
name|charBuffer
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|decoder
operator|.
name|flush
argument_list|(
name|charBuffer
argument_list|)
expr_stmt|;
name|charBuffer
operator|.
name|flip
argument_list|()
expr_stmt|;
return|return
name|charBuffer
return|;
block|}
block|}
specifier|public
name|int
name|getColNum
parameter_list|()
block|{
return|return
name|colNum
return|;
block|}
specifier|public
name|void
name|setColNum
parameter_list|(
name|int
name|colNum
parameter_list|)
block|{
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
block|}
specifier|public
name|String
name|getPattern
parameter_list|()
block|{
return|return
name|pattern
return|;
block|}
specifier|public
name|void
name|setPattern
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
block|}
block|}
end_class

end_unit

