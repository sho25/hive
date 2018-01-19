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
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|ParsePosition
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
import|;
end_import

begin_comment
comment|/**  * Date parser class for Hive.  */
end_comment

begin_class
specifier|public
class|class
name|DateParser
block|{
specifier|private
specifier|final
name|SimpleDateFormat
name|formatter
decl_stmt|;
specifier|private
specifier|final
name|ParsePosition
name|pos
decl_stmt|;
specifier|public
name|DateParser
parameter_list|()
block|{
name|formatter
operator|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd"
argument_list|)
expr_stmt|;
comment|// TODO: ideally, we should set formatter.setLenient(false);
name|pos
operator|=
operator|new
name|ParsePosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Date
name|parseDate
parameter_list|(
name|String
name|strValue
parameter_list|)
block|{
name|Date
name|result
init|=
operator|new
name|Date
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|parseDate
argument_list|(
name|strValue
argument_list|,
name|result
argument_list|)
condition|)
block|{
return|return
name|result
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|boolean
name|parseDate
parameter_list|(
name|String
name|strValue
parameter_list|,
name|Date
name|result
parameter_list|)
block|{
name|pos
operator|.
name|setIndex
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|java
operator|.
name|util
operator|.
name|Date
name|parsedVal
init|=
name|formatter
operator|.
name|parse
argument_list|(
name|strValue
argument_list|,
name|pos
argument_list|)
decl_stmt|;
if|if
condition|(
name|parsedVal
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|result
operator|.
name|setTime
argument_list|(
name|parsedVal
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

