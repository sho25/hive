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
name|plan
operator|.
name|ptf
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
name|parse
operator|.
name|WindowingSpec
operator|.
name|BoundarySpec
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
name|WindowingSpec
operator|.
name|Direction
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|BoundaryDef
block|{
name|Direction
name|direction
decl_stmt|;
specifier|public
name|Direction
name|getDirection
parameter_list|()
block|{
return|return
name|direction
return|;
block|}
comment|/**    * Returns if the bound is PRECEDING.    * @return if the bound is PRECEDING    */
specifier|public
name|boolean
name|isPreceding
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Returns if the bound is FOLLOWING.    * @return if the bound is FOLLOWING    */
specifier|public
name|boolean
name|isFollowing
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Returns if the bound is CURRENT ROW.    * @return if the bound is CURRENT ROW    */
specifier|public
name|boolean
name|isCurrentRow
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Returns offset from XX PRECEDING/FOLLOWING.    *    * @return offset from XX PRECEDING/FOLLOWING    */
specifier|public
specifier|abstract
name|int
name|getAmt
parameter_list|()
function_decl|;
comment|/**    * Returns signed offset from XX PRECEDING/FOLLOWING. Nagative for preceding.    *    * @return signed offset from XX PRECEDING/FOLLOWING    */
specifier|public
specifier|abstract
name|int
name|getRelativeOffset
parameter_list|()
function_decl|;
specifier|public
name|boolean
name|isUnbounded
parameter_list|()
block|{
return|return
name|this
operator|.
name|getAmt
argument_list|()
operator|==
name|BoundarySpec
operator|.
name|UNBOUNDED_AMOUNT
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|direction
operator|==
literal|null
condition|?
literal|""
else|:
name|direction
operator|+
literal|"("
operator|+
operator|(
name|getAmt
argument_list|()
operator|==
name|Integer
operator|.
name|MAX_VALUE
condition|?
literal|"MAX"
else|:
name|getAmt
argument_list|()
operator|)
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

