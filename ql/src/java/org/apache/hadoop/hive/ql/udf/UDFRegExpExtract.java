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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|MatchResult
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|ql
operator|.
name|exec
operator|.
name|UDF
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
name|exec
operator|.
name|description
import|;
end_import

begin_comment
comment|/**  * UDF to extract a specific group identified by a java regex.  * Note that if a regexp has a backslash ('\'), then need to specify '\\'  * For example, regexp_extract('100-200', '(\\d+)-(\\d+)', 1) will return '100'  */
end_comment

begin_class
annotation|@
name|description
argument_list|(
name|name
operator|=
literal|"regexp_extract"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str, regexp[, idx]) - extracts a group that matches regexp"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('100-200', '(\\d+)-(\\d+)', 1) FROM src LIMIT 1;\n"
operator|+
literal|"  '100'"
argument_list|)
specifier|public
class|class
name|UDFRegExpExtract
extends|extends
name|UDF
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|UDFRegExpExtract
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|String
name|lastRegex
init|=
literal|null
decl_stmt|;
specifier|private
name|Pattern
name|p
init|=
literal|null
decl_stmt|;
specifier|public
name|UDFRegExpExtract
parameter_list|()
block|{   }
specifier|public
name|String
name|evaluate
parameter_list|(
name|String
name|s
parameter_list|,
name|String
name|regex
parameter_list|,
name|Integer
name|extractIndex
parameter_list|)
block|{
if|if
condition|(
name|s
operator|==
literal|null
operator|||
name|regex
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
operator|!
name|regex
operator|.
name|equals
argument_list|(
name|lastRegex
argument_list|)
operator|||
name|p
operator|==
literal|null
condition|)
block|{
name|lastRegex
operator|=
name|regex
expr_stmt|;
name|p
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|regex
argument_list|)
expr_stmt|;
block|}
name|Matcher
name|m
init|=
name|p
operator|.
name|matcher
argument_list|(
name|s
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|find
argument_list|()
condition|)
block|{
name|MatchResult
name|mr
init|=
name|m
operator|.
name|toMatchResult
argument_list|()
decl_stmt|;
return|return
name|mr
operator|.
name|group
argument_list|(
name|extractIndex
argument_list|)
return|;
block|}
return|return
literal|""
return|;
block|}
specifier|public
name|String
name|evaluate
parameter_list|(
name|String
name|s
parameter_list|,
name|String
name|regex
parameter_list|)
block|{
return|return
name|this
operator|.
name|evaluate
argument_list|(
name|s
argument_list|,
name|regex
argument_list|,
literal|1
argument_list|)
return|;
block|}
block|}
end_class

end_unit

