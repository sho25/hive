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
name|parse
operator|.
name|repl
package|;
end_package

begin_enum
specifier|public
enum|enum
name|DumpType
block|{
name|BOOTSTRAP
argument_list|(
literal|"BOOTSTRAP"
argument_list|)
block|,
name|INCREMENTAL
argument_list|(
literal|"INCREMENTAL"
argument_list|)
block|,
name|EVENT_CREATE_TABLE
argument_list|(
literal|"EVENT_CREATE_TABLE"
argument_list|)
block|,
name|EVENT_ADD_PARTITION
argument_list|(
literal|"EVENT_ADD_PARTITION"
argument_list|)
block|,
name|EVENT_DROP_TABLE
argument_list|(
literal|"EVENT_DROP_TABLE"
argument_list|)
block|,
name|EVENT_DROP_PARTITION
argument_list|(
literal|"EVENT_DROP_PARTITION"
argument_list|)
block|,
name|EVENT_ALTER_TABLE
argument_list|(
literal|"EVENT_ALTER_TABLE"
argument_list|)
block|,
name|EVENT_RENAME_TABLE
argument_list|(
literal|"EVENT_RENAME_TABLE"
argument_list|)
block|,
name|EVENT_TRUNCATE_TABLE
argument_list|(
literal|"EVENT_TRUNCATE_TABLE"
argument_list|)
block|,
name|EVENT_ALTER_PARTITION
argument_list|(
literal|"EVENT_ALTER_PARTITION"
argument_list|)
block|,
name|EVENT_RENAME_PARTITION
argument_list|(
literal|"EVENT_RENAME_PARTITION"
argument_list|)
block|,
name|EVENT_TRUNCATE_PARTITION
argument_list|(
literal|"EVENT_TRUNCATE_PARTITION"
argument_list|)
block|,
name|EVENT_INSERT
argument_list|(
literal|"EVENT_INSERT"
argument_list|)
block|,
name|EVENT_UNKNOWN
argument_list|(
literal|"EVENT_UNKNOWN"
argument_list|)
block|;
name|String
name|type
init|=
literal|null
decl_stmt|;
name|DumpType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|type
return|;
block|}
block|}
end_enum

end_unit

