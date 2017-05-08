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
name|mapjoin
operator|.
name|fast
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_class
specifier|public
class|class
name|CommonFastHashTable
block|{
specifier|protected
specifier|static
specifier|final
name|float
name|LOAD_FACTOR
init|=
literal|0.75f
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|CAPACITY
init|=
literal|8
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|WB_SIZE
init|=
literal|128
decl_stmt|;
comment|// Make sure we cross some buffer boundaries...
specifier|protected
specifier|static
specifier|final
name|int
name|MODERATE_WB_SIZE
init|=
literal|8
operator|*
literal|1024
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|MODERATE_CAPACITY
init|=
literal|512
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|LARGE_WB_SIZE
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|LARGE_CAPACITY
init|=
literal|8388608
decl_stmt|;
specifier|protected
specifier|static
name|Random
name|random
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|MAX_KEY_LENGTH
init|=
literal|100
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|MAX_VALUE_LENGTH
init|=
literal|1000
decl_stmt|;
specifier|public
specifier|static
name|int
name|generateLargeCount
parameter_list|()
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
operator|!=
literal|0
condition|)
block|{
switch|switch
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|count
operator|=
literal|1
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|count
operator|=
literal|2
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|count
operator|=
literal|3
expr_stmt|;
case|case
literal|3
case|:
name|count
operator|=
literal|4
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|7
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|count
operator|=
literal|10
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|90
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|Error
argument_list|(
literal|"Missing case"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
switch|switch
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|count
operator|=
literal|100
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|900
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|count
operator|=
literal|1000
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|9000
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|count
operator|=
literal|10000
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|90000
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
return|return
name|count
return|;
block|}
block|}
end_class

end_unit

