begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
package|;
end_package

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Permission
import|;
end_import

begin_comment
comment|/**  * @deprecated Use/modify {@link org.apache.hive.hcatalog.NoExitSecurityManager} instead  */
end_comment

begin_class
specifier|public
class|class
name|NoExitSecurityManager
extends|extends
name|SecurityManager
block|{
annotation|@
name|Override
specifier|public
name|void
name|checkPermission
parameter_list|(
name|Permission
name|perm
parameter_list|)
block|{
comment|// allow anything.
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkPermission
parameter_list|(
name|Permission
name|perm
parameter_list|,
name|Object
name|context
parameter_list|)
block|{
comment|// allow anything.
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkExit
parameter_list|(
name|int
name|status
parameter_list|)
block|{
name|super
operator|.
name|checkExit
argument_list|(
name|status
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ExitException
argument_list|(
name|status
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

