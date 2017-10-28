begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Custom counters with limits (this will only work if the execution engine exposes this counter)  */
end_comment

begin_class
specifier|public
class|class
name|CustomCounterLimit
implements|implements
name|CounterLimit
block|{
specifier|private
name|String
name|counterName
decl_stmt|;
specifier|private
name|long
name|limit
decl_stmt|;
specifier|public
name|CustomCounterLimit
parameter_list|(
specifier|final
name|String
name|counterName
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|)
block|{
name|this
operator|.
name|counterName
operator|=
name|counterName
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|counterName
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLimit
parameter_list|()
block|{
return|return
name|limit
return|;
block|}
annotation|@
name|Override
specifier|public
name|CounterLimit
name|clone
parameter_list|()
block|{
return|return
operator|new
name|CustomCounterLimit
argument_list|(
name|counterName
argument_list|,
name|limit
argument_list|)
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
literal|"counter: "
operator|+
name|counterName
operator|+
literal|" limit: "
operator|+
name|limit
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hash
init|=
literal|31
operator|*
name|counterName
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|hash
operator|+=
literal|31
operator|*
name|limit
expr_stmt|;
return|return
literal|31
operator|*
name|hash
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
operator|||
operator|!
operator|(
name|other
operator|instanceof
name|CustomCounterLimit
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|other
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
name|CustomCounterLimit
name|otherVcl
init|=
operator|(
name|CustomCounterLimit
operator|)
name|other
decl_stmt|;
return|return
name|counterName
operator|.
name|equalsIgnoreCase
argument_list|(
name|otherVcl
operator|.
name|counterName
argument_list|)
operator|&&
name|limit
operator|==
name|otherVcl
operator|.
name|limit
return|;
block|}
block|}
end_class

end_unit

