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
name|exec
operator|.
name|vector
operator|.
name|expressions
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
name|metadata
operator|.
name|HiveException
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
name|udf
operator|.
name|UDFLike
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
name|List
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

begin_comment
comment|/**  * Evaluate LIKE filter on a batch for a vector of strings.  */
end_comment

begin_class
specifier|public
class|class
name|FilterStringColLikeStringScalar
extends|extends
name|AbstractFilterStringColLikeStringScalar
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
specifier|transient
specifier|final
specifier|static
name|List
argument_list|<
name|CheckerFactory
argument_list|>
name|checkerFactories
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|BeginCheckerFactory
argument_list|()
argument_list|,
operator|new
name|EndCheckerFactory
argument_list|()
argument_list|,
operator|new
name|MiddleCheckerFactory
argument_list|()
argument_list|,
operator|new
name|NoneCheckerFactory
argument_list|()
argument_list|,
operator|new
name|ChainedCheckerFactory
argument_list|()
argument_list|,
operator|new
name|ComplexCheckerFactory
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|FilterStringColLikeStringScalar
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|FilterStringColLikeStringScalar
parameter_list|(
name|int
name|colNum
parameter_list|,
name|byte
index|[]
name|likePattern
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
argument_list|(
name|colNum
argument_list|,
literal|null
argument_list|)
expr_stmt|;
try|try
block|{
name|super
operator|.
name|setPattern
argument_list|(
operator|new
name|String
argument_list|(
name|likePattern
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|CheckerFactory
argument_list|>
name|getCheckerFactories
parameter_list|()
block|{
return|return
name|checkerFactories
return|;
block|}
comment|/**    * Accepts simple LIKE patterns like "abc%" and creates corresponding checkers.    */
specifier|private
specifier|static
class|class
name|BeginCheckerFactory
implements|implements
name|CheckerFactory
block|{
specifier|private
specifier|static
specifier|final
name|Pattern
name|BEGIN_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"([^_%]+)%"
argument_list|)
decl_stmt|;
specifier|public
name|Checker
name|tryCreate
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|Matcher
name|matcher
init|=
name|BEGIN_PATTERN
operator|.
name|matcher
argument_list|(
name|pattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
operator|new
name|BeginChecker
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Accepts simple LIKE patterns like "%abc" and creates a corresponding checkers.    */
specifier|private
specifier|static
class|class
name|EndCheckerFactory
implements|implements
name|CheckerFactory
block|{
specifier|private
specifier|static
specifier|final
name|Pattern
name|END_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"%([^_%]+)"
argument_list|)
decl_stmt|;
specifier|public
name|Checker
name|tryCreate
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|Matcher
name|matcher
init|=
name|END_PATTERN
operator|.
name|matcher
argument_list|(
name|pattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
operator|new
name|EndChecker
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Accepts simple LIKE patterns like "%abc%" and creates a corresponding checkers.    */
specifier|private
specifier|static
class|class
name|MiddleCheckerFactory
implements|implements
name|CheckerFactory
block|{
specifier|private
specifier|static
specifier|final
name|Pattern
name|MIDDLE_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"%([^_%]+)%"
argument_list|)
decl_stmt|;
specifier|public
name|Checker
name|tryCreate
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|Matcher
name|matcher
init|=
name|MIDDLE_PATTERN
operator|.
name|matcher
argument_list|(
name|pattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
operator|new
name|MiddleChecker
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Accepts simple LIKE patterns like "abc" and creates corresponding checkers.    */
specifier|private
specifier|static
class|class
name|NoneCheckerFactory
implements|implements
name|CheckerFactory
block|{
specifier|private
specifier|static
specifier|final
name|Pattern
name|NONE_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[^%_]+"
argument_list|)
decl_stmt|;
specifier|public
name|Checker
name|tryCreate
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|Matcher
name|matcher
init|=
name|NONE_PATTERN
operator|.
name|matcher
argument_list|(
name|pattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
operator|new
name|NoneChecker
argument_list|(
name|pattern
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Accepts chained LIKE patterns without escaping like "abc%def%ghi%" and creates corresponding    * checkers.    *    */
specifier|private
specifier|static
class|class
name|ChainedCheckerFactory
implements|implements
name|CheckerFactory
block|{
specifier|private
specifier|static
specifier|final
name|Pattern
name|CHAIN_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"(%?[^%_\\\\]+%?)+"
argument_list|)
decl_stmt|;
specifier|public
name|Checker
name|tryCreate
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|Matcher
name|matcher
init|=
name|CHAIN_PATTERN
operator|.
name|matcher
argument_list|(
name|pattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
operator|new
name|ChainedChecker
argument_list|(
name|pattern
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Accepts any LIKE patterns and creates corresponding checkers.    */
specifier|private
specifier|static
class|class
name|ComplexCheckerFactory
implements|implements
name|CheckerFactory
block|{
specifier|public
name|Checker
name|tryCreate
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
comment|// anchor the pattern to the start:end of the whole string.
return|return
operator|new
name|ComplexChecker
argument_list|(
literal|"^"
operator|+
name|UDFLike
operator|.
name|likePatternToRegExp
argument_list|(
name|pattern
argument_list|)
operator|+
literal|"$"
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

