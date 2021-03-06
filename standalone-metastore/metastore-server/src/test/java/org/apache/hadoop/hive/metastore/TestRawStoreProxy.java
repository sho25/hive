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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|conf
operator|.
name|Configuration
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
name|api
operator|.
name|MetaException
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
name|Test
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
name|TestRawStoreProxy
block|{
specifier|static
class|class
name|TestStore
extends|extends
name|ObjectStore
block|{
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// noop
block|}
specifier|public
name|void
name|noopMethod
parameter_list|()
throws|throws
name|MetaException
block|{
name|Deadline
operator|.
name|checkTimeout
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|exceptions
parameter_list|()
throws|throws
name|IllegalStateException
throws|,
name|MetaException
block|{
name|Deadline
operator|.
name|checkTimeout
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"throwing an exception"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExceptionDispatch
parameter_list|()
throws|throws
name|Throwable
block|{
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setTimeVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CLIENT_SOCKET_TIMEOUT
argument_list|,
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|RawStoreProxy
name|rsp
init|=
operator|new
name|RawStoreProxy
argument_list|(
name|conf
argument_list|,
name|conf
argument_list|,
name|TestStore
operator|.
name|class
argument_list|,
literal|1
argument_list|)
decl_stmt|;
try|try
block|{
name|rsp
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|TestStore
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"exceptions"
argument_list|)
argument_list|,
operator|new
name|Object
index|[]
block|{}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"an exception is expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ise
parameter_list|)
block|{
comment|// expected
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|20
argument_list|)
expr_stmt|;
comment|// this shouldn't throw an exception
name|rsp
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|TestStore
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"noopMethod"
argument_list|)
argument_list|,
operator|new
name|Object
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

