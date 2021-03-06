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
name|metastore
operator|.
name|api
package|;
end_package

begin_class
specifier|public
class|class
name|InitializeTableWriteIdsRequest
block|{
specifier|private
specifier|final
name|String
name|dbName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tblName
decl_stmt|;
specifier|private
specifier|final
name|long
name|seeWriteId
decl_stmt|;
specifier|public
name|InitializeTableWriteIdsRequest
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|long
name|seeWriteId
parameter_list|)
block|{
assert|assert
name|dbName
operator|!=
literal|null
assert|;
assert|assert
name|tblName
operator|!=
literal|null
assert|;
assert|assert
name|seeWriteId
operator|>
literal|1
assert|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tblName
operator|=
name|tblName
expr_stmt|;
name|this
operator|.
name|seeWriteId
operator|=
name|seeWriteId
expr_stmt|;
block|}
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
specifier|public
name|String
name|getTblName
parameter_list|()
block|{
return|return
name|tblName
return|;
block|}
specifier|public
name|long
name|getSeeWriteId
parameter_list|()
block|{
return|return
name|seeWriteId
return|;
block|}
block|}
end_class

end_unit

