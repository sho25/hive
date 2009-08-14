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
name|contrib
operator|.
name|udf
operator|.
name|example
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
name|UDF
import|;
end_import

begin_class
specifier|public
class|class
name|UDFExampleAdd
extends|extends
name|UDF
block|{
specifier|public
name|Integer
name|evaluate
parameter_list|(
name|Integer
modifier|...
name|a
parameter_list|)
block|{
name|int
name|total
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
operator|<
name|a
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|a
index|[
name|i
index|]
operator|!=
literal|null
condition|)
name|total
operator|+=
name|a
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|total
return|;
block|}
specifier|public
name|Double
name|evaluate
parameter_list|(
name|Double
modifier|...
name|a
parameter_list|)
block|{
name|double
name|total
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
operator|<
name|a
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|a
index|[
name|i
index|]
operator|!=
literal|null
condition|)
name|total
operator|+=
name|a
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|total
return|;
block|}
block|}
end_class

end_unit

