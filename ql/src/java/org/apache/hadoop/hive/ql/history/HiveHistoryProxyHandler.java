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
name|history
package|;
end_package

begin_comment
comment|/**  * Proxy handler for HiveHistory to do nothing  * Used when HiveHistory is disabled.  */
end_comment

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationHandler
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Proxy
import|;
end_import

begin_class
specifier|public
class|class
name|HiveHistoryProxyHandler
implements|implements
name|InvocationHandler
block|{
specifier|public
specifier|static
name|HiveHistory
name|getNoOpHiveHistoryProxy
parameter_list|()
block|{
return|return
operator|(
name|HiveHistory
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|HiveHistory
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|HiveHistory
operator|.
name|class
block|}
operator|,
operator|new
name|HiveHistoryProxyHandler
argument_list|()
block|)
function|;
block|}
end_class

begin_function
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|arg0
parameter_list|,
specifier|final
name|Method
name|method
parameter_list|,
specifier|final
name|Object
index|[]
name|args
parameter_list|)
block|{
comment|//do nothing
return|return
literal|null
return|;
block|}
end_function

unit|}
end_unit

