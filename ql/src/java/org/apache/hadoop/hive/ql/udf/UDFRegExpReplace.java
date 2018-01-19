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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * UDFRegExpReplace.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"regexp_replace"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str, regexp, rep) - replace all substrings of str that "
operator|+
literal|"match regexp with rep"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('100-200', '(\\d+)', 'num') FROM src LIMIT 1;\n"
operator|+
literal|"  'num-num'"
argument_list|)
specifier|public
class|class
name|UDFRegExpReplace
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|Text
name|lastRegex
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|private
name|Pattern
name|p
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Text
name|lastReplacement
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|private
name|String
name|replacementString
init|=
literal|""
decl_stmt|;
specifier|private
name|Text
name|result
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|public
name|UDFRegExpReplace
parameter_list|()
block|{   }
specifier|public
name|Text
name|evaluate
parameter_list|(
name|Text
name|s
parameter_list|,
name|Text
name|regex
parameter_list|,
name|Text
name|replacement
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
operator|||
name|replacement
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// If the regex is changed, make sure we compile the regex again.
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
operator|.
name|set
argument_list|(
name|regex
argument_list|)
expr_stmt|;
name|p
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|regex
operator|.
name|toString
argument_list|()
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
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// If the replacement is changed, make sure we redo toString again.
if|if
condition|(
operator|!
name|replacement
operator|.
name|equals
argument_list|(
name|lastReplacement
argument_list|)
condition|)
block|{
name|lastReplacement
operator|.
name|set
argument_list|(
name|replacement
argument_list|)
expr_stmt|;
name|replacementString
operator|=
name|replacement
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
while|while
condition|(
name|m
operator|.
name|find
argument_list|()
condition|)
block|{
name|m
operator|.
name|appendReplacement
argument_list|(
name|sb
argument_list|,
name|replacementString
argument_list|)
expr_stmt|;
block|}
name|m
operator|.
name|appendTail
argument_list|(
name|sb
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

