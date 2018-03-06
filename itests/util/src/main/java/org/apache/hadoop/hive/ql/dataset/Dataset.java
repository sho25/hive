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
name|dataset
package|;
end_package

begin_comment
comment|/**  * Dataset: simple class representation of a dataset  */
end_comment

begin_class
specifier|public
class|class
name|Dataset
block|{
specifier|public
specifier|static
specifier|final
name|String
name|INIT_FILE_NAME
init|=
literal|"load.hive.sql"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CLEANUP_FILE_NAME
init|=
literal|"cleanup.hive.sql"
decl_stmt|;
specifier|private
name|String
name|table
decl_stmt|;
specifier|public
name|Dataset
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
specifier|public
name|String
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
block|}
end_class

end_unit

