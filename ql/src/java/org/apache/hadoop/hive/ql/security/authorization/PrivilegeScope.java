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
name|security
operator|.
name|authorization
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
import|;
end_import

begin_comment
comment|/**  * PrivilegeScope describes a hive defined privilege's scope  * (global/database/table/column). For example some hive privileges are  * db-level only, some are global, and some are table only.  */
end_comment

begin_enum
specifier|public
enum|enum
name|PrivilegeScope
block|{
name|USER_LEVEL_SCOPE
argument_list|(
operator|(
name|short
operator|)
literal|0x01
argument_list|)
block|,
name|DB_LEVEL_SCOPE
argument_list|(
operator|(
name|short
operator|)
literal|0x02
argument_list|)
block|,
name|TABLE_LEVEL_SCOPE
argument_list|(
operator|(
name|short
operator|)
literal|0x04
argument_list|)
block|,
name|COLUMN_LEVEL_SCOPE
argument_list|(
operator|(
name|short
operator|)
literal|0x08
argument_list|)
block|;
specifier|private
name|short
name|mode
decl_stmt|;
specifier|private
name|PrivilegeScope
parameter_list|(
name|short
name|mode
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
block|}
specifier|public
name|short
name|getMode
parameter_list|()
block|{
return|return
name|mode
return|;
block|}
specifier|public
name|void
name|setMode
parameter_list|(
name|short
name|mode
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
block|}
specifier|public
specifier|static
name|EnumSet
argument_list|<
name|PrivilegeScope
argument_list|>
name|ALLSCOPE
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|PrivilegeScope
operator|.
name|USER_LEVEL_SCOPE
argument_list|,
name|PrivilegeScope
operator|.
name|DB_LEVEL_SCOPE
argument_list|,
name|PrivilegeScope
operator|.
name|TABLE_LEVEL_SCOPE
argument_list|,
name|PrivilegeScope
operator|.
name|COLUMN_LEVEL_SCOPE
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|EnumSet
argument_list|<
name|PrivilegeScope
argument_list|>
name|ALLSCOPE_EXCEPT_COLUMN
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|PrivilegeScope
operator|.
name|USER_LEVEL_SCOPE
argument_list|,
name|PrivilegeScope
operator|.
name|DB_LEVEL_SCOPE
argument_list|,
name|PrivilegeScope
operator|.
name|TABLE_LEVEL_SCOPE
argument_list|)
decl_stmt|;
block|}
end_enum

end_unit

