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

begin_comment
comment|/**   * Math expression evaluation helper functions.  * Some of these are referenced from ColumnUnaryFunc.txt.  */
end_comment

begin_class
specifier|public
class|class
name|MathExpr
block|{
comment|// Round using the "half-up" method used in Hive.
specifier|public
specifier|static
name|double
name|round
parameter_list|(
name|double
name|d
parameter_list|)
block|{
if|if
condition|(
name|d
operator|>
literal|0.0
condition|)
block|{
return|return
call|(
name|double
call|)
argument_list|(
call|(
name|long
call|)
argument_list|(
name|d
operator|+
literal|0.5d
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
call|(
name|double
call|)
argument_list|(
call|(
name|long
call|)
argument_list|(
name|d
operator|-
literal|0.5d
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|double
name|log2
parameter_list|(
name|double
name|d
parameter_list|)
block|{
return|return
name|Math
operator|.
name|log
argument_list|(
name|d
argument_list|)
operator|/
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|abs
parameter_list|(
name|long
name|v
parameter_list|)
block|{
return|return
name|v
operator|>=
literal|0
condition|?
name|v
else|:
operator|-
name|v
return|;
block|}
specifier|public
specifier|static
name|double
name|sign
parameter_list|(
name|double
name|v
parameter_list|)
block|{
return|return
name|v
operator|>=
literal|0
condition|?
literal|1.0
else|:
operator|-
literal|1.0
return|;
block|}
specifier|public
specifier|static
name|double
name|sign
parameter_list|(
name|long
name|v
parameter_list|)
block|{
return|return
name|v
operator|>=
literal|0
condition|?
literal|1.0
else|:
operator|-
literal|1.0
return|;
block|}
block|}
end_class

end_unit

