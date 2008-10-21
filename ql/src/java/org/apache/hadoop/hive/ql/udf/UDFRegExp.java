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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_class
specifier|public
class|class
name|UDFRegExp
implements|implements
name|UDF
block|{
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
name|UDFRegExp
parameter_list|()
block|{   }
specifier|public
name|Boolean
name|evaluate
parameter_list|(
name|String
name|s
parameter_list|,
name|String
name|regex
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
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|m
operator|.
name|matches
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

