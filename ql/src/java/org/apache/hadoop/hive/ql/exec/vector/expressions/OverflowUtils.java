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
name|DoubleColumnVector
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
name|LongColumnVector
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
name|serde
operator|.
name|serdeConstants
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Utility methods to handle integer overflow/underflows in a ColumnVector.  */
end_comment

begin_class
specifier|public
class|class
name|OverflowUtils
block|{
specifier|private
name|OverflowUtils
parameter_list|()
block|{
comment|//prevent instantiation
block|}
specifier|public
specifier|static
name|void
name|accountForOverflowLong
parameter_list|(
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|LongColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|outputTypeInfo
operator|==
literal|null
condition|)
block|{
comment|//can't do much if outputTypeInfo is not set
return|return;
block|}
switch|switch
condition|(
name|outputTypeInfo
operator|.
name|getTypeName
argument_list|()
condition|)
block|{
case|case
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
case|:
comment|//byte
if|if
condition|(
name|v
operator|.
name|isRepeating
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|v
operator|.
name|vector
index|[
literal|0
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|selectedInUse
condition|)
block|{
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
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|byte
operator|)
name|v
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
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
name|i
operator|++
control|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|byte
operator|)
name|v
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
break|break;
case|case
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
case|:
comment|//short
if|if
condition|(
name|v
operator|.
name|isRepeating
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
operator|(
name|short
operator|)
name|v
operator|.
name|vector
index|[
literal|0
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|selectedInUse
condition|)
block|{
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
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|short
operator|)
name|v
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
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
name|i
operator|++
control|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|short
operator|)
name|v
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
break|break;
case|case
name|serdeConstants
operator|.
name|INT_TYPE_NAME
case|:
comment|//int
if|if
condition|(
name|v
operator|.
name|isRepeating
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
operator|(
name|int
operator|)
name|v
operator|.
name|vector
index|[
literal|0
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|selectedInUse
condition|)
block|{
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
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|int
operator|)
name|v
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
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
name|i
operator|++
control|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|int
operator|)
name|v
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
break|break;
default|default:
comment|//nothing to be done
block|}
block|}
specifier|public
specifier|static
name|void
name|accountForOverflowDouble
parameter_list|(
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|DoubleColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|outputTypeInfo
operator|==
literal|null
condition|)
block|{
comment|//can't do much if outputTypeInfo is not set
return|return;
block|}
switch|switch
condition|(
name|outputTypeInfo
operator|.
name|getTypeName
argument_list|()
condition|)
block|{
case|case
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
case|:
comment|//float
if|if
condition|(
name|v
operator|.
name|isRepeating
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
operator|(
name|float
operator|)
name|v
operator|.
name|vector
index|[
literal|0
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|selectedInUse
condition|)
block|{
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
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|float
operator|)
name|v
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
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
name|i
operator|++
control|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|float
operator|)
name|v
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
break|break;
default|default:
comment|//nothing to be done
block|}
block|}
block|}
end_class

end_unit

