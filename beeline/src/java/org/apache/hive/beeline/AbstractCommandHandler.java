begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * See SQLLine notice in LICENSE  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|console
operator|.
name|completer
operator|.
name|Completer
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|console
operator|.
name|completer
operator|.
name|NullCompleter
import|;
end_import

begin_comment
comment|/**  * An abstract implementation of CommandHandler.  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractCommandHandler
implements|implements
name|CommandHandler
block|{
specifier|private
specifier|final
name|BeeLine
name|beeLine
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|names
decl_stmt|;
specifier|private
specifier|final
name|String
name|helpText
decl_stmt|;
specifier|private
name|Completer
index|[]
name|parameterCompleters
init|=
operator|new
name|Completer
index|[
literal|0
index|]
decl_stmt|;
specifier|protected
specifier|transient
name|Throwable
name|lastException
decl_stmt|;
specifier|public
name|AbstractCommandHandler
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|,
name|String
index|[]
name|names
parameter_list|,
name|String
name|helpText
parameter_list|,
name|Completer
index|[]
name|completors
parameter_list|)
block|{
name|this
operator|.
name|beeLine
operator|=
name|beeLine
expr_stmt|;
name|name
operator|=
name|names
index|[
literal|0
index|]
expr_stmt|;
name|this
operator|.
name|names
operator|=
name|names
expr_stmt|;
name|this
operator|.
name|helpText
operator|=
name|helpText
expr_stmt|;
if|if
condition|(
name|completors
operator|==
literal|null
operator|||
name|completors
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|parameterCompleters
operator|=
operator|new
name|Completer
index|[]
block|{
operator|new
name|NullCompleter
argument_list|()
block|}
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|Completer
argument_list|>
name|c
init|=
operator|new
name|LinkedList
argument_list|<
name|Completer
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|completors
argument_list|)
argument_list|)
decl_stmt|;
name|c
operator|.
name|add
argument_list|(
operator|new
name|NullCompleter
argument_list|()
argument_list|)
expr_stmt|;
name|parameterCompleters
operator|=
name|c
operator|.
name|toArray
argument_list|(
operator|new
name|Completer
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getHelpText
parameter_list|()
block|{
return|return
name|helpText
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getNames
parameter_list|()
block|{
return|return
name|names
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|matches
parameter_list|(
name|String
name|line
parameter_list|)
block|{
if|if
condition|(
name|line
operator|==
literal|null
operator|||
name|line
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
index|[]
name|parts
init|=
name|beeLine
operator|.
name|split
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|==
literal|null
operator|||
name|parts
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|String
name|name2
range|:
name|names
control|)
block|{
if|if
condition|(
name|name2
operator|.
name|startsWith
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
return|return
name|name2
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|setParameterCompleters
parameter_list|(
name|Completer
index|[]
name|parameterCompleters
parameter_list|)
block|{
name|this
operator|.
name|parameterCompleters
operator|=
name|parameterCompleters
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Completer
index|[]
name|getParameterCompleters
parameter_list|()
block|{
return|return
name|parameterCompleters
return|;
block|}
annotation|@
name|Override
specifier|public
name|Throwable
name|getLastException
parameter_list|()
block|{
return|return
name|lastException
return|;
block|}
block|}
end_class

end_unit

