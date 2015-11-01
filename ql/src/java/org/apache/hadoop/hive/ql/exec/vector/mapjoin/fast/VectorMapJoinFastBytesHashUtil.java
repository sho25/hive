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
name|WriteBuffers
import|;
end_import

begin_class
specifier|public
class|class
name|VectorMapJoinFastBytesHashUtil
block|{
specifier|public
specifier|static
name|String
name|displayBytes
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|start
operator|+
name|length
condition|;
name|i
operator|++
control|)
block|{
name|char
name|ch
init|=
operator|(
name|char
operator|)
name|bytes
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|ch
argument_list|<
literal|' '
operator|||
name|ch
argument_list|>
literal|'~'
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"\\%03d"
argument_list|,
call|(
name|int
call|)
argument_list|(
name|bytes
index|[
name|i
index|]
operator|&
literal|0xff
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|ch
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

