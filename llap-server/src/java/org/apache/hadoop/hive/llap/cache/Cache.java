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
name|llap
operator|.
name|cache
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
name|common
operator|.
name|io
operator|.
name|storage_api
operator|.
name|EncodedColumnBatch
operator|.
name|ColumnStreamData
import|;
end_import

begin_comment
comment|/** Dummy interface for now, might be different. */
end_comment

begin_interface
specifier|public
interface|interface
name|Cache
parameter_list|<
name|CacheKey
parameter_list|>
block|{
specifier|public
name|ColumnStreamData
index|[]
name|cacheOrGet
parameter_list|(
name|CacheKey
name|key
parameter_list|,
name|ColumnStreamData
index|[]
name|value
parameter_list|)
function_decl|;
specifier|public
name|ColumnStreamData
index|[]
name|get
parameter_list|(
name|CacheKey
name|key
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

