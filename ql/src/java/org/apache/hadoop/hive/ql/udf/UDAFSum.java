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
name|udf
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
name|UDAF
import|;
end_import

begin_class
specifier|public
class|class
name|UDAFSum
extends|extends
name|UDAF
block|{
specifier|private
name|double
name|mSum
decl_stmt|;
specifier|private
name|boolean
name|mEmpty
decl_stmt|;
specifier|public
name|UDAFSum
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|mSum
operator|=
literal|0
expr_stmt|;
name|mEmpty
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|aggregate
parameter_list|(
name|Double
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
name|mSum
operator|+=
name|o
expr_stmt|;
name|mEmpty
operator|=
literal|false
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|Double
name|evaluatePartial
parameter_list|()
block|{
comment|// This is SQL standard - sum of zero items should be null.
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|Double
argument_list|(
name|mSum
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|aggregatePartial
parameter_list|(
name|Double
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
name|mSum
operator|+=
name|o
expr_stmt|;
name|mEmpty
operator|=
literal|false
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|Double
name|evaluate
parameter_list|()
block|{
comment|// This is SQL standard - sum of zero items should be null.
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|Double
argument_list|(
name|mSum
argument_list|)
return|;
block|}
block|}
end_class

end_unit

