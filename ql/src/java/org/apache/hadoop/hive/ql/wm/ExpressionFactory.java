begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|wm
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|conf
operator|.
name|Validator
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
name|parse
operator|.
name|ASTNode
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
name|parse
operator|.
name|HiveParser
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
name|parse
operator|.
name|ParseDriver
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
name|parse
operator|.
name|ParseException
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
name|plan
operator|.
name|PlanUtils
import|;
end_import

begin_comment
comment|/**  * Factory to create expressions  */
end_comment

begin_class
specifier|public
class|class
name|ExpressionFactory
block|{
specifier|public
specifier|static
name|Expression
name|fromString
parameter_list|(
specifier|final
name|String
name|expression
parameter_list|)
block|{
if|if
condition|(
name|expression
operator|==
literal|null
operator|||
name|expression
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ParseDriver
name|driver
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|ASTNode
name|node
init|=
literal|null
decl_stmt|;
try|try
block|{
name|node
operator|=
name|driver
operator|.
name|parseTriggerExpression
argument_list|(
name|expression
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid expression: "
operator|+
name|expression
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
operator|&&
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|EOF
condition|)
block|{
name|node
operator|=
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|node
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_TRIGGER_EXPRESSION
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected trigger expression, got: "
operator|+
name|node
operator|.
name|toStringTree
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|!=
literal|3
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Only single> condition supported: "
operator|+
name|expression
argument_list|)
throw|;
block|}
comment|// Only ">" predicate is supported right now, this has to be extended to support
comment|// expression tree when multiple conditions are required. HIVE-17622
if|if
condition|(
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|GREATERTHAN
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid predicate in expression"
argument_list|)
throw|;
block|}
specifier|final
name|String
name|counterName
init|=
name|node
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
decl_stmt|;
specifier|final
name|String
name|counterValueStr
init|=
name|PlanUtils
operator|.
name|stripQuotes
argument_list|(
name|node
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getText
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|counterName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Counter name cannot be empty!"
argument_list|)
throw|;
block|}
comment|// look for matches in file system counters
name|long
name|counterValue
decl_stmt|;
for|for
control|(
name|FileSystemCounterLimit
operator|.
name|FSCounter
name|fsCounter
range|:
name|FileSystemCounterLimit
operator|.
name|FSCounter
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|counterName
operator|.
name|toUpperCase
argument_list|()
operator|.
name|endsWith
argument_list|(
name|fsCounter
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|counterValue
operator|=
name|getCounterValue
argument_list|(
name|counterValueStr
argument_list|,
operator|new
name|Validator
operator|.
name|SizeValidator
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|counterValue
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal value for counter limit. Expected a positive long value."
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid counter value: "
operator|+
name|counterValueStr
argument_list|)
throw|;
block|}
comment|// this is file system counter, valid and create counter
name|FileSystemCounterLimit
name|fsCounterLimit
init|=
name|FileSystemCounterLimit
operator|.
name|fromName
argument_list|(
name|counterName
argument_list|,
name|counterValue
argument_list|)
decl_stmt|;
return|return
name|createExpression
argument_list|(
name|fsCounterLimit
argument_list|)
return|;
block|}
block|}
comment|// look for matches in time based counters
for|for
control|(
name|TimeCounterLimit
operator|.
name|TimeCounter
name|timeCounter
range|:
name|TimeCounterLimit
operator|.
name|TimeCounter
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|counterName
operator|.
name|equalsIgnoreCase
argument_list|(
name|timeCounter
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|counterValue
operator|=
name|getCounterValue
argument_list|(
name|counterValueStr
argument_list|,
operator|new
name|Validator
operator|.
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|counterValue
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal value for counter limit. Expected a positive long value."
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid counter value: "
operator|+
name|counterValueStr
argument_list|)
throw|;
block|}
name|TimeCounterLimit
name|timeCounterLimit
init|=
operator|new
name|TimeCounterLimit
argument_list|(
name|TimeCounterLimit
operator|.
name|TimeCounter
operator|.
name|valueOf
argument_list|(
name|counterName
operator|.
name|toUpperCase
argument_list|()
argument_list|)
argument_list|,
name|counterValue
argument_list|)
decl_stmt|;
return|return
name|createExpression
argument_list|(
name|timeCounterLimit
argument_list|)
return|;
block|}
block|}
comment|// look for matches in vertex specific counters
for|for
control|(
name|VertexCounterLimit
operator|.
name|VertexCounter
name|vertexCounter
range|:
name|VertexCounterLimit
operator|.
name|VertexCounter
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|counterName
operator|.
name|equalsIgnoreCase
argument_list|(
name|vertexCounter
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|counterValue
operator|=
name|getCounterValue
argument_list|(
name|counterValueStr
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|counterValue
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal value for counter limit. Expected a positive long value."
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid counter value: "
operator|+
name|counterValueStr
argument_list|)
throw|;
block|}
name|VertexCounterLimit
name|vertexCounterLimit
init|=
operator|new
name|VertexCounterLimit
argument_list|(
name|VertexCounterLimit
operator|.
name|VertexCounter
operator|.
name|valueOf
argument_list|(
name|counterName
operator|.
name|toUpperCase
argument_list|()
argument_list|)
argument_list|,
name|counterValue
argument_list|)
decl_stmt|;
return|return
name|createExpression
argument_list|(
name|vertexCounterLimit
argument_list|)
return|;
block|}
block|}
comment|// if nothing matches, try creating a custom counter
try|try
block|{
name|counterValue
operator|=
name|getCounterValue
argument_list|(
name|counterValueStr
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|counterValue
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal value for counter limit. Expected a positive long value."
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid counter value: "
operator|+
name|counterValueStr
argument_list|)
throw|;
block|}
name|CustomCounterLimit
name|customCounterLimit
init|=
operator|new
name|CustomCounterLimit
argument_list|(
name|counterName
argument_list|,
name|counterValue
argument_list|)
decl_stmt|;
return|return
name|createExpression
argument_list|(
name|customCounterLimit
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|long
name|getCounterValue
parameter_list|(
specifier|final
name|String
name|counterValueStr
parameter_list|,
specifier|final
name|Validator
name|validator
parameter_list|)
throws|throws
name|NumberFormatException
block|{
name|long
name|counter
decl_stmt|;
try|try
block|{
name|counter
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|counterValueStr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
if|if
condition|(
name|validator
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|validator
operator|instanceof
name|Validator
operator|.
name|SizeValidator
condition|)
block|{
return|return
name|HiveConf
operator|.
name|toSizeBytes
argument_list|(
name|counterValueStr
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|validator
operator|instanceof
name|Validator
operator|.
name|TimeValidator
condition|)
block|{
return|return
name|HiveConf
operator|.
name|toTime
argument_list|(
name|counterValueStr
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
block|}
throw|throw
name|e
throw|;
block|}
return|return
name|counter
return|;
block|}
specifier|static
name|Expression
name|createExpression
parameter_list|(
name|CounterLimit
name|counterLimit
parameter_list|)
block|{
return|return
operator|new
name|TriggerExpression
argument_list|(
name|counterLimit
argument_list|,
name|Expression
operator|.
name|Predicate
operator|.
name|GREATER_THAN
argument_list|)
return|;
block|}
block|}
end_class

end_unit

