begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * Copy of C++ STL pair container.  */
end_comment

begin_class
specifier|public
class|class
name|Pair
parameter_list|<
name|T
parameter_list|,
name|U
parameter_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|T
name|first
decl_stmt|;
specifier|public
name|U
name|second
decl_stmt|;
comment|/**    * @param f First element in pair.    * @param s Second element in pair.    */
specifier|public
name|Pair
parameter_list|(
name|T
name|f
parameter_list|,
name|U
name|s
parameter_list|)
block|{
name|first
operator|=
name|f
expr_stmt|;
name|second
operator|=
name|s
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see java.lang.Object#toString()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|first
operator|.
name|toString
argument_list|()
operator|+
literal|","
operator|+
name|second
operator|.
name|toString
argument_list|()
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|(
operator|(
operator|(
name|this
operator|.
name|first
operator|==
literal|null
condition|?
literal|1
else|:
name|this
operator|.
name|first
operator|.
name|hashCode
argument_list|()
operator|)
operator|*
literal|17
operator|)
operator|+
operator|(
name|this
operator|.
name|second
operator|==
literal|null
condition|?
literal|1
else|:
name|this
operator|.
name|second
operator|.
name|hashCode
argument_list|()
operator|)
operator|*
literal|19
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|Pair
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Pair
name|otherPair
init|=
operator|(
name|Pair
operator|)
name|other
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|first
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|otherPair
operator|.
name|first
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
return|return
literal|true
return|;
block|}
block|}
if|if
condition|(
name|this
operator|.
name|second
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|otherPair
operator|.
name|second
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
return|return
literal|true
return|;
block|}
block|}
if|if
condition|(
name|this
operator|.
name|first
operator|.
name|equals
argument_list|(
name|otherPair
operator|.
name|first
argument_list|)
operator|&&
name|this
operator|.
name|second
operator|.
name|equals
argument_list|(
name|otherPair
operator|.
name|second
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

