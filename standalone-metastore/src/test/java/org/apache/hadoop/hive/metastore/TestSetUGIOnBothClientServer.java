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
name|metastore
operator|.
name|annotation
operator|.
name|MetastoreCheckinTest
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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreCheckinTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSetUGIOnBothClientServer
extends|extends
name|TestRemoteHiveMetaStore
block|{
specifier|public
name|TestSetUGIOnBothClientServer
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|isThriftClient
operator|=
literal|true
expr_stmt|;
comment|// This will turn on setugi on both client and server processes of the test.
name|System
operator|.
name|setProperty
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|EXECUTE_SET_UGI
operator|.
name|toString
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

