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
name|hive
operator|.
name|service
operator|.
name|cli
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Layout
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|PatternLayout
import|;
end_import

begin_comment
comment|/**  * CLIServiceUtils.  *  */
end_comment

begin_class
specifier|public
class|class
name|CLIServiceUtils
block|{
specifier|private
specifier|static
specifier|final
name|char
name|SEARCH_STRING_ESCAPE
init|=
literal|'\\'
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Layout
name|verboseLayout
init|=
operator|new
name|PatternLayout
argument_list|(
literal|"%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Layout
name|nonVerboseLayout
init|=
operator|new
name|PatternLayout
argument_list|(
literal|"%-5p : %m%n"
argument_list|)
decl_stmt|;
comment|/**    * Convert a SQL search pattern into an equivalent Java Regex.    *    * @param pattern input which may contain '%' or '_' wildcard characters, or    * these characters escaped using {@link #getSearchStringEscape()}.    * @return replace %/_ with regex search characters, also handle escaped    * characters.    */
specifier|public
specifier|static
name|String
name|patternToRegex
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
if|if
condition|(
name|pattern
operator|==
literal|null
condition|)
block|{
return|return
literal|".*"
return|;
block|}
else|else
block|{
name|StringBuilder
name|result
init|=
operator|new
name|StringBuilder
argument_list|(
name|pattern
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|escaped
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|len
init|=
name|pattern
operator|.
name|length
argument_list|()
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|++
control|)
block|{
name|char
name|c
init|=
name|pattern
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|escaped
condition|)
block|{
if|if
condition|(
name|c
operator|!=
name|SEARCH_STRING_ESCAPE
condition|)
block|{
name|escaped
operator|=
literal|false
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|c
operator|==
name|SEARCH_STRING_ESCAPE
condition|)
block|{
name|escaped
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
elseif|else
if|if
condition|(
name|c
operator|==
literal|'%'
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|".*"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|c
operator|==
literal|'_'
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|append
argument_list|(
name|Character
operator|.
name|toLowerCase
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

