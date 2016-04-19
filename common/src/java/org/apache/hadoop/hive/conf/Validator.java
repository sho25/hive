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
name|conf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
comment|/**  * validate value for a ConfVar, return non-null string for fail message  */
end_comment

begin_interface
specifier|public
interface|interface
name|Validator
block|{
name|String
name|validate
parameter_list|(
name|String
name|value
parameter_list|)
function_decl|;
name|String
name|toDescription
parameter_list|()
function_decl|;
class|class
name|StringSet
implements|implements
name|Validator
block|{
specifier|private
specifier|final
name|boolean
name|caseSensitive
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|expected
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|StringSet
parameter_list|(
name|String
modifier|...
name|values
parameter_list|)
block|{
name|this
argument_list|(
literal|false
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
specifier|public
name|StringSet
parameter_list|(
name|boolean
name|caseSensitive
parameter_list|,
name|String
modifier|...
name|values
parameter_list|)
block|{
name|this
operator|.
name|caseSensitive
operator|=
name|caseSensitive
expr_stmt|;
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|caseSensitive
condition|?
name|value
else|:
name|value
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getExpected
parameter_list|()
block|{
return|return
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|expected
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|validate
parameter_list|(
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
operator|||
operator|!
name|expected
operator|.
name|contains
argument_list|(
name|caseSensitive
condition|?
name|value
else|:
name|value
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|"Invalid value.. expects one of "
operator|+
name|expected
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toDescription
parameter_list|()
block|{
return|return
literal|"Expects one of "
operator|+
name|expected
return|;
block|}
block|}
enum|enum
name|TYPE
block|{
name|INT
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|inRange
parameter_list|(
name|String
name|value
parameter_list|,
name|Object
name|lower
parameter_list|,
name|Object
name|upper
parameter_list|)
block|{
name|int
name|ivalue
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|lower
operator|!=
literal|null
operator|&&
name|ivalue
operator|<
operator|(
name|Integer
operator|)
name|lower
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|upper
operator|!=
literal|null
operator|&&
name|ivalue
operator|>
operator|(
name|Integer
operator|)
name|upper
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
block|,
name|LONG
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|inRange
parameter_list|(
name|String
name|value
parameter_list|,
name|Object
name|lower
parameter_list|,
name|Object
name|upper
parameter_list|)
block|{
name|long
name|lvalue
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|lower
operator|!=
literal|null
operator|&&
name|lvalue
operator|<
operator|(
name|Long
operator|)
name|lower
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|upper
operator|!=
literal|null
operator|&&
name|lvalue
operator|>
operator|(
name|Long
operator|)
name|upper
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
block|,
name|FLOAT
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|inRange
parameter_list|(
name|String
name|value
parameter_list|,
name|Object
name|lower
parameter_list|,
name|Object
name|upper
parameter_list|)
block|{
name|float
name|fvalue
init|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|lower
operator|!=
literal|null
operator|&&
name|fvalue
operator|<
operator|(
name|Float
operator|)
name|lower
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|upper
operator|!=
literal|null
operator|&&
name|fvalue
operator|>
operator|(
name|Float
operator|)
name|upper
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
block|;
specifier|public
specifier|static
name|TYPE
name|valueOf
parameter_list|(
name|Object
name|lower
parameter_list|,
name|Object
name|upper
parameter_list|)
block|{
if|if
condition|(
name|lower
operator|instanceof
name|Integer
operator|||
name|upper
operator|instanceof
name|Integer
condition|)
block|{
return|return
name|INT
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|instanceof
name|Long
operator|||
name|upper
operator|instanceof
name|Long
condition|)
block|{
return|return
name|LONG
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|instanceof
name|Float
operator|||
name|upper
operator|instanceof
name|Float
condition|)
block|{
return|return
name|FLOAT
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid range from "
operator|+
name|lower
operator|+
literal|" to "
operator|+
name|upper
argument_list|)
throw|;
block|}
specifier|protected
specifier|abstract
name|boolean
name|inRange
parameter_list|(
name|String
name|value
parameter_list|,
name|Object
name|lower
parameter_list|,
name|Object
name|upper
parameter_list|)
function_decl|;
block|}
class|class
name|RangeValidator
implements|implements
name|Validator
block|{
specifier|private
specifier|final
name|TYPE
name|type
decl_stmt|;
specifier|private
specifier|final
name|Object
name|lower
decl_stmt|,
name|upper
decl_stmt|;
specifier|public
name|RangeValidator
parameter_list|(
name|Object
name|lower
parameter_list|,
name|Object
name|upper
parameter_list|)
block|{
name|this
operator|.
name|lower
operator|=
name|lower
expr_stmt|;
name|this
operator|.
name|upper
operator|=
name|upper
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|TYPE
operator|.
name|valueOf
argument_list|(
name|lower
argument_list|,
name|upper
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|validate
parameter_list|(
name|String
name|value
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|"Value cannot be null"
return|;
block|}
if|if
condition|(
operator|!
name|type
operator|.
name|inRange
argument_list|(
name|value
operator|.
name|trim
argument_list|()
argument_list|,
name|lower
argument_list|,
name|upper
argument_list|)
condition|)
block|{
return|return
literal|"Invalid value  "
operator|+
name|value
operator|+
literal|", which should be in between "
operator|+
name|lower
operator|+
literal|" and "
operator|+
name|upper
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|e
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toDescription
parameter_list|()
block|{
if|if
condition|(
name|lower
operator|==
literal|null
operator|&&
name|upper
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|lower
operator|!=
literal|null
operator|&&
name|upper
operator|!=
literal|null
condition|)
block|{
return|return
literal|"Expects value between "
operator|+
name|lower
operator|+
literal|" and "
operator|+
name|upper
return|;
block|}
if|if
condition|(
name|lower
operator|!=
literal|null
condition|)
block|{
return|return
literal|"Expects value bigger than "
operator|+
name|lower
return|;
block|}
return|return
literal|"Expects value smaller than "
operator|+
name|upper
return|;
block|}
block|}
class|class
name|PatternSet
implements|implements
name|Validator
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|Pattern
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|Pattern
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|PatternSet
parameter_list|(
name|String
modifier|...
name|values
parameter_list|)
block|{
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
name|expected
operator|.
name|add
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|validate
parameter_list|(
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|"Invalid value.. expects one of patterns "
operator|+
name|expected
return|;
block|}
for|for
control|(
name|Pattern
name|pattern
range|:
name|expected
control|)
block|{
if|if
condition|(
name|pattern
operator|.
name|matcher
argument_list|(
name|value
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
return|return
literal|"Invalid value.. expects one of patterns "
operator|+
name|expected
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toDescription
parameter_list|()
block|{
return|return
literal|"Expects one of the pattern in "
operator|+
name|expected
return|;
block|}
block|}
class|class
name|RatioValidator
implements|implements
name|Validator
block|{
annotation|@
name|Override
specifier|public
name|String
name|validate
parameter_list|(
name|String
name|value
parameter_list|)
block|{
try|try
block|{
name|float
name|fvalue
init|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|fvalue
argument_list|<
literal|0
operator|||
name|fvalue
argument_list|>
literal|1
condition|)
block|{
return|return
literal|"Invalid ratio "
operator|+
name|value
operator|+
literal|", which should be in between 0 to 1"
return|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
name|e
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toDescription
parameter_list|()
block|{
return|return
literal|"Expects value between 0.0f and 1.0f"
return|;
block|}
block|}
class|class
name|TimeValidator
implements|implements
name|Validator
block|{
specifier|private
specifier|final
name|TimeUnit
name|timeUnit
decl_stmt|;
specifier|private
specifier|final
name|Long
name|min
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|minInclusive
decl_stmt|;
specifier|private
specifier|final
name|Long
name|max
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|maxInclusive
decl_stmt|;
specifier|public
name|TimeValidator
parameter_list|(
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
name|this
argument_list|(
name|timeUnit
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TimeValidator
parameter_list|(
name|TimeUnit
name|timeUnit
parameter_list|,
name|Long
name|min
parameter_list|,
name|boolean
name|minInclusive
parameter_list|,
name|Long
name|max
parameter_list|,
name|boolean
name|maxInclusive
parameter_list|)
block|{
name|this
operator|.
name|timeUnit
operator|=
name|timeUnit
expr_stmt|;
name|this
operator|.
name|min
operator|=
name|min
expr_stmt|;
name|this
operator|.
name|minInclusive
operator|=
name|minInclusive
expr_stmt|;
name|this
operator|.
name|max
operator|=
name|max
expr_stmt|;
name|this
operator|.
name|maxInclusive
operator|=
name|maxInclusive
expr_stmt|;
block|}
specifier|public
name|TimeUnit
name|getTimeUnit
parameter_list|()
block|{
return|return
name|timeUnit
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|validate
parameter_list|(
name|String
name|value
parameter_list|)
block|{
try|try
block|{
name|long
name|time
init|=
name|HiveConf
operator|.
name|toTime
argument_list|(
name|value
argument_list|,
name|timeUnit
argument_list|,
name|timeUnit
argument_list|)
decl_stmt|;
if|if
condition|(
name|min
operator|!=
literal|null
operator|&&
operator|(
name|minInclusive
condition|?
name|time
operator|<
name|min
else|:
name|time
operator|<=
name|min
operator|)
condition|)
block|{
return|return
name|value
operator|+
literal|" is smaller than "
operator|+
name|timeString
argument_list|(
name|min
argument_list|)
return|;
block|}
if|if
condition|(
name|max
operator|!=
literal|null
operator|&&
operator|(
name|maxInclusive
condition|?
name|time
operator|>
name|max
else|:
name|time
operator|>=
name|max
operator|)
condition|)
block|{
return|return
name|value
operator|+
literal|" is bigger than "
operator|+
name|timeString
argument_list|(
name|max
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|e
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|toDescription
parameter_list|()
block|{
name|String
name|description
init|=
literal|"Expects a time value with unit "
operator|+
literal|"(d/day, h/hour, m/min, s/sec, ms/msec, us/usec, ns/nsec)"
operator|+
literal|", which is "
operator|+
name|HiveConf
operator|.
name|stringFor
argument_list|(
name|timeUnit
argument_list|)
operator|+
literal|" if not specified"
decl_stmt|;
if|if
condition|(
name|min
operator|!=
literal|null
operator|&&
name|max
operator|!=
literal|null
condition|)
block|{
name|description
operator|+=
literal|".\nThe time should be in between "
operator|+
name|timeString
argument_list|(
name|min
argument_list|)
operator|+
operator|(
name|minInclusive
condition|?
literal|" (inclusive)"
else|:
literal|" (exclusive)"
operator|)
operator|+
literal|" and "
operator|+
name|timeString
argument_list|(
name|max
argument_list|)
operator|+
operator|(
name|maxInclusive
condition|?
literal|" (inclusive)"
else|:
literal|" (exclusive)"
operator|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|min
operator|!=
literal|null
condition|)
block|{
name|description
operator|+=
literal|".\nThe time should be bigger than "
operator|+
operator|(
name|minInclusive
condition|?
literal|"or equal to "
else|:
literal|""
operator|)
operator|+
name|timeString
argument_list|(
name|min
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|max
operator|!=
literal|null
condition|)
block|{
name|description
operator|+=
literal|".\nThe time should be smaller than "
operator|+
operator|(
name|maxInclusive
condition|?
literal|"or equal to "
else|:
literal|""
operator|)
operator|+
name|timeString
argument_list|(
name|max
argument_list|)
expr_stmt|;
block|}
return|return
name|description
return|;
block|}
specifier|private
name|String
name|timeString
parameter_list|(
name|long
name|time
parameter_list|)
block|{
return|return
name|time
operator|+
literal|" "
operator|+
name|HiveConf
operator|.
name|stringFor
argument_list|(
name|timeUnit
argument_list|)
return|;
block|}
block|}
class|class
name|SizeValidator
implements|implements
name|Validator
block|{
specifier|private
specifier|final
name|Long
name|min
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|minInclusive
decl_stmt|;
specifier|private
specifier|final
name|Long
name|max
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|maxInclusive
decl_stmt|;
specifier|public
name|SizeValidator
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SizeValidator
parameter_list|(
name|Long
name|min
parameter_list|,
name|boolean
name|minInclusive
parameter_list|,
name|Long
name|max
parameter_list|,
name|boolean
name|maxInclusive
parameter_list|)
block|{
name|this
operator|.
name|min
operator|=
name|min
expr_stmt|;
name|this
operator|.
name|minInclusive
operator|=
name|minInclusive
expr_stmt|;
name|this
operator|.
name|max
operator|=
name|max
expr_stmt|;
name|this
operator|.
name|maxInclusive
operator|=
name|maxInclusive
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|validate
parameter_list|(
name|String
name|value
parameter_list|)
block|{
try|try
block|{
name|long
name|size
init|=
name|HiveConf
operator|.
name|toSizeBytes
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|min
operator|!=
literal|null
operator|&&
operator|(
name|minInclusive
condition|?
name|size
operator|<
name|min
else|:
name|size
operator|<=
name|min
operator|)
condition|)
block|{
return|return
name|value
operator|+
literal|" is smaller than "
operator|+
name|sizeString
argument_list|(
name|min
argument_list|)
return|;
block|}
if|if
condition|(
name|max
operator|!=
literal|null
operator|&&
operator|(
name|maxInclusive
condition|?
name|size
operator|>
name|max
else|:
name|size
operator|>=
name|max
operator|)
condition|)
block|{
return|return
name|value
operator|+
literal|" is bigger than "
operator|+
name|sizeString
argument_list|(
name|max
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|e
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|toDescription
parameter_list|()
block|{
name|String
name|description
init|=
literal|"Expects a byte size value with unit (blank for bytes, kb, mb, gb, tb, pb)"
decl_stmt|;
if|if
condition|(
name|min
operator|!=
literal|null
operator|&&
name|max
operator|!=
literal|null
condition|)
block|{
name|description
operator|+=
literal|".\nThe size should be in between "
operator|+
name|sizeString
argument_list|(
name|min
argument_list|)
operator|+
operator|(
name|minInclusive
condition|?
literal|" (inclusive)"
else|:
literal|" (exclusive)"
operator|)
operator|+
literal|" and "
operator|+
name|sizeString
argument_list|(
name|max
argument_list|)
operator|+
operator|(
name|maxInclusive
condition|?
literal|" (inclusive)"
else|:
literal|" (exclusive)"
operator|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|min
operator|!=
literal|null
condition|)
block|{
name|description
operator|+=
literal|".\nThe time should be bigger than "
operator|+
operator|(
name|minInclusive
condition|?
literal|"or equal to "
else|:
literal|""
operator|)
operator|+
name|sizeString
argument_list|(
name|min
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|max
operator|!=
literal|null
condition|)
block|{
name|description
operator|+=
literal|".\nThe size should be smaller than "
operator|+
operator|(
name|maxInclusive
condition|?
literal|"or equal to "
else|:
literal|""
operator|)
operator|+
name|sizeString
argument_list|(
name|max
argument_list|)
expr_stmt|;
block|}
return|return
name|description
return|;
block|}
specifier|private
name|String
name|sizeString
parameter_list|(
name|long
name|size
parameter_list|)
block|{
specifier|final
name|String
index|[]
name|units
init|=
block|{
literal|" bytes"
block|,
literal|"Kb"
block|,
literal|"Mb"
block|,
literal|"Gb"
block|,
literal|"Tb"
block|}
decl_stmt|;
name|long
name|current
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
argument_list|<
name|units
operator|.
name|length
operator|&&
name|current
argument_list|>
literal|0
condition|;
operator|++
name|i
control|)
block|{
name|long
name|next
init|=
name|current
operator|<<
literal|10
decl_stmt|;
if|if
condition|(
operator|(
name|size
operator|&
operator|(
name|next
operator|-
literal|1
operator|)
operator|)
operator|!=
literal|0
condition|)
return|return
call|(
name|long
call|)
argument_list|(
name|size
operator|/
name|current
argument_list|)
operator|+
name|units
index|[
name|i
index|]
return|;
name|current
operator|=
name|next
expr_stmt|;
block|}
return|return
name|current
operator|>
literal|0
condition|?
operator|(
call|(
name|long
call|)
argument_list|(
name|size
operator|/
name|current
argument_list|)
operator|+
literal|"Pb"
operator|)
else|:
operator|(
name|size
operator|+
name|units
index|[
literal|0
index|]
operator|)
return|;
block|}
block|}
block|}
end_interface

end_unit

