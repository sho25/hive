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
comment|/**  * Join conditions Descriptor implementation.  *   */
end_comment

begin_class
specifier|public
class|class
name|joinCond
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
specifier|private
name|int
name|left
decl_stmt|;
specifier|private
name|int
name|right
decl_stmt|;
specifier|private
name|int
name|type
decl_stmt|;
specifier|private
name|boolean
name|preserved
decl_stmt|;
specifier|public
name|joinCond
parameter_list|()
block|{   }
specifier|public
name|joinCond
parameter_list|(
name|int
name|left
parameter_list|,
name|int
name|right
parameter_list|,
name|int
name|type
parameter_list|)
block|{
name|this
operator|.
name|left
operator|=
name|left
expr_stmt|;
name|this
operator|.
name|right
operator|=
name|right
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|joinCond
parameter_list|(
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
name|joinCond
name|condn
parameter_list|)
block|{
name|left
operator|=
name|condn
operator|.
name|getLeft
argument_list|()
expr_stmt|;
name|right
operator|=
name|condn
operator|.
name|getRight
argument_list|()
expr_stmt|;
name|preserved
operator|=
name|condn
operator|.
name|getPreserved
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|condn
operator|.
name|getJoinType
argument_list|()
condition|)
block|{
case|case
name|INNER
case|:
name|type
operator|=
name|joinDesc
operator|.
name|INNER_JOIN
expr_stmt|;
break|break;
case|case
name|LEFTOUTER
case|:
name|type
operator|=
name|joinDesc
operator|.
name|LEFT_OUTER_JOIN
expr_stmt|;
break|break;
case|case
name|RIGHTOUTER
case|:
name|type
operator|=
name|joinDesc
operator|.
name|RIGHT_OUTER_JOIN
expr_stmt|;
break|break;
case|case
name|FULLOUTER
case|:
name|type
operator|=
name|joinDesc
operator|.
name|FULL_OUTER_JOIN
expr_stmt|;
break|break;
case|case
name|UNIQUE
case|:
name|type
operator|=
name|joinDesc
operator|.
name|UNIQUE_JOIN
expr_stmt|;
break|break;
case|case
name|LEFTSEMI
case|:
name|type
operator|=
name|joinDesc
operator|.
name|LEFT_SEMI_JOIN
expr_stmt|;
break|break;
default|default:
assert|assert
literal|false
assert|;
block|}
block|}
comment|/**    * @return true if table is preserved, false otherwise    */
specifier|public
name|boolean
name|getPreserved
parameter_list|()
block|{
return|return
name|preserved
return|;
block|}
comment|/**    * @param preserved    *          if table is preserved, false otherwise    */
specifier|public
name|void
name|setPreserved
parameter_list|(
specifier|final
name|boolean
name|preserved
parameter_list|)
block|{
name|this
operator|.
name|preserved
operator|=
name|preserved
expr_stmt|;
block|}
specifier|public
name|int
name|getLeft
parameter_list|()
block|{
return|return
name|left
return|;
block|}
specifier|public
name|void
name|setLeft
parameter_list|(
specifier|final
name|int
name|left
parameter_list|)
block|{
name|this
operator|.
name|left
operator|=
name|left
expr_stmt|;
block|}
specifier|public
name|int
name|getRight
parameter_list|()
block|{
return|return
name|right
return|;
block|}
specifier|public
name|void
name|setRight
parameter_list|(
specifier|final
name|int
name|right
parameter_list|)
block|{
name|this
operator|.
name|right
operator|=
name|right
expr_stmt|;
block|}
specifier|public
name|int
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|void
name|setType
parameter_list|(
specifier|final
name|int
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
annotation|@
name|explain
specifier|public
name|String
name|getJoinCondString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|joinDesc
operator|.
name|INNER_JOIN
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"Inner Join "
argument_list|)
expr_stmt|;
break|break;
case|case
name|joinDesc
operator|.
name|FULL_OUTER_JOIN
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"Outer Join "
argument_list|)
expr_stmt|;
break|break;
case|case
name|joinDesc
operator|.
name|LEFT_OUTER_JOIN
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"Left Outer Join"
argument_list|)
expr_stmt|;
break|break;
case|case
name|joinDesc
operator|.
name|RIGHT_OUTER_JOIN
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"Right Outer Join"
argument_list|)
expr_stmt|;
break|break;
case|case
name|joinDesc
operator|.
name|UNIQUE_JOIN
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"Unique Join"
argument_list|)
expr_stmt|;
break|break;
case|case
name|joinDesc
operator|.
name|LEFT_SEMI_JOIN
case|:
name|sb
operator|.
name|append
argument_list|(
literal|"Left Semi Join "
argument_list|)
expr_stmt|;
break|break;
default|default:
name|sb
operator|.
name|append
argument_list|(
literal|"Unknow Join "
argument_list|)
expr_stmt|;
break|break;
block|}
name|sb
operator|.
name|append
argument_list|(
name|left
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" to "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|right
argument_list|)
expr_stmt|;
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

