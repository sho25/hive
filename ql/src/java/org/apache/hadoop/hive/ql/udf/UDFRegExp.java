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
name|io
operator|.
name|BooleanWritable
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

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"rlike,regexp"
argument_list|,
name|value
operator|=
literal|"str _FUNC_ regexp - Returns true if str matches regexp and "
operator|+
literal|"false otherwise"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT 'fb' _FUNC_ '.*' FROM src LIMIT 1;\n"
operator|+
literal|"  true"
argument_list|)
specifier|public
class|class
name|UDFRegExp
extends|extends
name|UDF
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|UDFRegExp
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
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
name|boolean
name|warned
init|=
literal|false
decl_stmt|;
name|BooleanWritable
name|result
init|=
operator|new
name|BooleanWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFRegExp
parameter_list|()
block|{   }
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|Text
name|s
parameter_list|,
name|Text
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
name|regex
operator|.
name|getLength
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|warned
condition|)
block|{
name|warned
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" regex is empty. Additional "
operator|+
literal|"warnings for an empty regex will be suppressed."
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|result
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
name|result
operator|.
name|set
argument_list|(
name|m
operator|.
name|find
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

