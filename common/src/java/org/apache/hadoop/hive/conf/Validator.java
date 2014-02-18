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
specifier|static
class|class
name|StringSet
implements|implements
name|Validator
block|{
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
name|value
operator|.
name|toLowerCase
argument_list|()
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
operator|||
operator|!
name|expected
operator|.
name|contains
argument_list|(
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
block|}
specifier|static
enum|enum
name|RANGE_TYPE
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
return|return
operator|(
name|Integer
operator|)
name|lower
operator|<=
name|ivalue
operator|&&
name|ivalue
operator|<=
operator|(
name|Integer
operator|)
name|upper
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
return|return
operator|(
name|Long
operator|)
name|lower
operator|<=
name|lvalue
operator|&&
name|lvalue
operator|<=
operator|(
name|Long
operator|)
name|upper
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
return|return
operator|(
name|Float
operator|)
name|lower
operator|<=
name|fvalue
operator|&&
name|fvalue
operator|<=
operator|(
name|Float
operator|)
name|upper
return|;
block|}
block|}
block|;
specifier|public
specifier|static
name|RANGE_TYPE
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
operator|&&
name|upper
operator|instanceof
name|Integer
condition|)
block|{
assert|assert
operator|(
name|Integer
operator|)
name|lower
operator|<
operator|(
name|Integer
operator|)
name|upper
assert|;
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
operator|&&
name|upper
operator|instanceof
name|Long
condition|)
block|{
assert|assert
operator|(
name|Long
operator|)
name|lower
operator|<
operator|(
name|Long
operator|)
name|upper
assert|;
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
operator|&&
name|upper
operator|instanceof
name|Float
condition|)
block|{
assert|assert
operator|(
name|Float
operator|)
name|lower
operator|<
operator|(
name|Float
operator|)
name|upper
assert|;
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
specifier|static
class|class
name|RangeValidator
implements|implements
name|Validator
block|{
specifier|private
specifier|final
name|RANGE_TYPE
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
name|RANGE_TYPE
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
block|}
specifier|static
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
block|}
specifier|static
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
name|valueOf
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|fvalue
operator|<=
literal|0
operator|||
name|fvalue
operator|>=
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
block|}
block|}
end_interface

end_unit

