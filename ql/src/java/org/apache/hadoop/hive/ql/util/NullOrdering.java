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
name|util
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
name|parse
operator|.
name|HiveParser
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
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|NullValueOption
import|;
end_import

begin_comment
comment|/**  * Enum for converting different Null ordering description types.  */
end_comment

begin_enum
specifier|public
enum|enum
name|NullOrdering
block|{
name|NULLS_FIRST
argument_list|(
literal|1
argument_list|,
name|HiveParser
operator|.
name|TOK_NULLS_FIRST
argument_list|,
name|NullValueOption
operator|.
name|MAXVALUE
argument_list|)
block|,
name|NULLS_LAST
argument_list|(
literal|0
argument_list|,
name|HiveParser
operator|.
name|TOK_NULLS_LAST
argument_list|,
name|NullValueOption
operator|.
name|MINVALUE
argument_list|)
block|;
name|NullOrdering
parameter_list|(
name|int
name|code
parameter_list|,
name|int
name|token
parameter_list|,
name|NullValueOption
name|nullValueOption
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|token
operator|=
name|token
expr_stmt|;
name|this
operator|.
name|nullValueOption
operator|=
name|nullValueOption
expr_stmt|;
block|}
specifier|private
specifier|final
name|int
name|code
decl_stmt|;
specifier|private
specifier|final
name|int
name|token
decl_stmt|;
specifier|private
specifier|final
name|NullValueOption
name|nullValueOption
decl_stmt|;
specifier|public
specifier|static
name|NullOrdering
name|fromToken
parameter_list|(
name|int
name|token
parameter_list|)
block|{
for|for
control|(
name|NullOrdering
name|nullOrdering
range|:
name|NullOrdering
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|nullOrdering
operator|.
name|token
operator|==
name|token
condition|)
block|{
return|return
name|nullOrdering
return|;
block|}
block|}
throw|throw
operator|new
name|EnumConstantNotPresentException
argument_list|(
name|NullOrdering
operator|.
name|class
argument_list|,
literal|"No enum constant present with token "
operator|+
name|token
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|NullOrdering
name|fromCode
parameter_list|(
name|int
name|code
parameter_list|)
block|{
for|for
control|(
name|NullOrdering
name|nullOrdering
range|:
name|NullOrdering
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|nullOrdering
operator|.
name|code
operator|==
name|code
condition|)
block|{
return|return
name|nullOrdering
return|;
block|}
block|}
throw|throw
operator|new
name|EnumConstantNotPresentException
argument_list|(
name|NullOrdering
operator|.
name|class
argument_list|,
literal|"No enum constant present with code "
operator|+
name|code
argument_list|)
throw|;
block|}
specifier|public
name|int
name|getCode
parameter_list|()
block|{
return|return
name|code
return|;
block|}
specifier|public
name|int
name|getToken
parameter_list|()
block|{
return|return
name|token
return|;
block|}
specifier|public
name|NullValueOption
name|getNullValueOption
parameter_list|()
block|{
return|return
name|nullValueOption
return|;
block|}
block|}
end_enum

end_unit

