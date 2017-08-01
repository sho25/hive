begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|utils
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Interner
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Interners
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
specifier|public
class|class
name|StringUtils
block|{
specifier|private
specifier|static
specifier|final
name|Interner
argument_list|<
name|String
argument_list|>
name|STRING_INTERNER
init|=
name|Interners
operator|.
name|newWeakInterner
argument_list|()
decl_stmt|;
comment|/**    * Return the internalized string, or null if the given string is null.    * @param str The string to intern    * @return The identical string cached in the string pool.    */
specifier|public
specifier|static
name|String
name|intern
parameter_list|(
name|String
name|str
parameter_list|)
block|{
if|if
condition|(
name|str
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|STRING_INTERNER
operator|.
name|intern
argument_list|(
name|str
argument_list|)
return|;
block|}
comment|/**    * Return an interned list with identical contents as the given list.    * @param list The list whose strings will be interned    * @return An identical list with its strings interned.    */
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|intern
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|list
parameter_list|)
block|{
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|newList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|str
range|:
name|list
control|)
block|{
name|newList
operator|.
name|add
argument_list|(
name|intern
argument_list|(
name|str
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|newList
return|;
block|}
comment|/**    * Return an interned map with identical contents as the given map.    * @param map The map whose strings will be interned    * @return An identical map with its strings interned.    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|intern
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
parameter_list|)
block|{
if|if
condition|(
name|map
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|map
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// nothing to intern
return|return
name|map
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|map
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|newMap
operator|.
name|put
argument_list|(
name|intern
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|intern
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|newMap
return|;
block|}
specifier|public
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|asSet
parameter_list|(
name|String
modifier|...
name|elements
parameter_list|)
block|{
if|if
condition|(
name|elements
operator|==
literal|null
condition|)
return|return
operator|new
name|HashSet
argument_list|<>
argument_list|()
return|;
name|Set
argument_list|<
name|String
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|elements
operator|.
name|length
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|set
argument_list|,
name|elements
argument_list|)
expr_stmt|;
return|return
name|set
return|;
block|}
comment|/**    * Normalize all identifiers to make equality comparisons easier.    * @param identifier identifier    * @return normalized version, with white space removed and all lower case.    */
specifier|public
specifier|static
name|String
name|normalizeIdentifier
parameter_list|(
name|String
name|identifier
parameter_list|)
block|{
return|return
name|identifier
operator|.
name|trim
argument_list|()
operator|.
name|toLowerCase
argument_list|()
return|;
block|}
comment|/**    * Make a string representation of the exception.    * @param e The exception to stringify    * @return A string with exception name and call stack.    */
specifier|public
specifier|static
name|String
name|stringifyException
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|StringWriter
name|stm
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|PrintWriter
name|wrt
init|=
operator|new
name|PrintWriter
argument_list|(
name|stm
argument_list|)
decl_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|(
name|wrt
argument_list|)
expr_stmt|;
name|wrt
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|stm
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

