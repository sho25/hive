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
name|cli
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|conf
operator|.
name|HiveConf
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_class
specifier|public
class|class
name|CliSessionState
extends|extends
name|SessionState
block|{
comment|/**    * -e option if any that the session has been invoked with    */
specifier|public
name|String
name|execString
decl_stmt|;
comment|/**    * -f option if any that the session has been invoked with    */
specifier|public
name|String
name|fileName
decl_stmt|;
comment|/**    * properties set from -hiveconf via cmdline    */
specifier|public
name|Properties
name|cmdProperties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
specifier|public
name|CliSessionState
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CliSessionState
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

