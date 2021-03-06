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
name|common
operator|.
name|type
package|;
end_package

begin_comment
comment|/**  *  * HiveVarChar.  * String wrapper to support SQL VARCHAR features.  * Max string length is enforced.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveVarchar
extends|extends
name|HiveBaseChar
implements|implements
name|Comparable
argument_list|<
name|HiveVarchar
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|int
name|MAX_VARCHAR_LENGTH
init|=
literal|65535
decl_stmt|;
specifier|public
name|HiveVarchar
parameter_list|()
block|{   }
specifier|public
name|HiveVarchar
parameter_list|(
name|String
name|val
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|setValue
argument_list|(
name|val
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveVarchar
parameter_list|(
name|HiveVarchar
name|hc
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|setValue
argument_list|(
name|hc
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the new value    */
specifier|public
name|void
name|setValue
parameter_list|(
name|String
name|val
parameter_list|)
block|{
name|super
operator|.
name|setValue
argument_list|(
name|val
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setValue
parameter_list|(
name|HiveVarchar
name|hc
parameter_list|)
block|{
name|super
operator|.
name|setValue
argument_list|(
name|hc
operator|.
name|getValue
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|HiveVarchar
name|rhs
parameter_list|)
block|{
if|if
condition|(
name|rhs
operator|==
name|this
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|this
operator|.
name|getValue
argument_list|()
operator|.
name|compareTo
argument_list|(
name|rhs
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|rhs
parameter_list|)
block|{
if|if
condition|(
name|rhs
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|this
operator|.
name|getValue
argument_list|()
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|HiveVarchar
operator|)
name|rhs
operator|)
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

