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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
specifier|public
class|class
name|VectorizedSupport
block|{
specifier|public
enum|enum
name|Support
block|{
name|DECIMAL_64
block|;
specifier|final
name|String
name|lowerCaseName
decl_stmt|;
name|Support
parameter_list|()
block|{
name|this
operator|.
name|lowerCaseName
operator|=
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Support
argument_list|>
name|nameToSupportMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Support
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
for|for
control|(
name|Support
name|support
range|:
name|values
argument_list|()
control|)
block|{
name|nameToSupportMap
operator|.
name|put
argument_list|(
name|support
operator|.
name|lowerCaseName
argument_list|,
name|support
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

