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
name|parse
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_comment
comment|/**  * The input signature of a function or operator. The signature basically  * consists of name, list of parameter types.  *   **/
end_comment

begin_class
specifier|public
class|class
name|InputSignature
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|typeArray
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|InputSignature
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|InputSignature
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|typeArray
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|InputSignature
parameter_list|(
name|String
name|name
parameter_list|,
name|TypeInfo
modifier|...
name|classList
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|classList
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
for|for
control|(
name|TypeInfo
name|cl
range|:
name|classList
control|)
block|{
name|typeArray
operator|.
name|add
argument_list|(
name|cl
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|InputSignature
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|classList
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|classList
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|cl
range|:
name|classList
control|)
block|{
name|typeArray
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfoFromPrimitiveWritable
argument_list|(
name|cl
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|add
parameter_list|(
name|TypeInfo
name|paramType
parameter_list|)
block|{
name|typeArray
operator|.
name|add
argument_list|(
name|paramType
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
operator|.
name|toUpperCase
argument_list|()
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|getTypeArray
parameter_list|()
block|{
return|return
name|typeArray
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|InputSignature
name|other
init|=
literal|null
decl_stmt|;
try|try
block|{
name|other
operator|=
operator|(
name|InputSignature
operator|)
name|obj
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|cce
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|name
operator|.
name|equalsIgnoreCase
argument_list|(
name|other
operator|.
name|getName
argument_list|()
argument_list|)
operator|&&
operator|(
name|other
operator|.
name|typeArray
operator|.
name|equals
argument_list|(
name|typeArray
argument_list|)
operator|)
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
name|toString
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
name|boolean
name|isfirst
init|=
literal|true
decl_stmt|;
for|for
control|(
name|TypeInfo
name|cls
range|:
name|getTypeArray
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|isfirst
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|cls
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|isfirst
operator|=
literal|false
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
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

