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
name|metastore
package|;
end_package

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|shims
operator|.
name|ShimLoader
import|;
end_import

begin_comment
comment|/**  * Test for unwrapping InvocationTargetException, which is thrown from  * constructor of listener class  */
end_comment

begin_class
specifier|public
class|class
name|TestMetaStoreListenersError
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testInitListenerException
parameter_list|()
throws|throws
name|Throwable
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.init.hooks"
argument_list|,
name|ErrorInitListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|port
init|=
name|MetaStoreUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
try|try
block|{
name|HiveMetaStore
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|MetaException
operator|.
name|class
argument_list|,
name|throwable
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Failed to instantiate listener named: "
operator|+
literal|"org.apache.hadoop.hive.metastore.TestMetaStoreListenersError$ErrorInitListener, "
operator|+
literal|"reason: java.lang.IllegalArgumentException: exception on constructor"
argument_list|,
name|throwable
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testEventListenerException
parameter_list|()
throws|throws
name|Throwable
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.init.hooks"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.event.listeners"
argument_list|,
name|ErrorEventListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|port
init|=
name|MetaStoreUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
try|try
block|{
name|HiveMetaStore
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|MetaException
operator|.
name|class
argument_list|,
name|throwable
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Failed to instantiate listener named: "
operator|+
literal|"org.apache.hadoop.hive.metastore.TestMetaStoreListenersError$ErrorEventListener, "
operator|+
literal|"reason: java.lang.IllegalArgumentException: exception on constructor"
argument_list|,
name|throwable
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|ErrorInitListener
extends|extends
name|MetaStoreInitListener
block|{
specifier|public
name|ErrorInitListener
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|super
argument_list|(
name|config
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"exception on constructor"
argument_list|)
throw|;
block|}
specifier|public
name|void
name|onInit
parameter_list|(
name|MetaStoreInitContext
name|context
parameter_list|)
throws|throws
name|MetaException
block|{     }
block|}
specifier|public
specifier|static
class|class
name|ErrorEventListener
extends|extends
name|MetaStoreEventListener
block|{
specifier|public
name|ErrorEventListener
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|super
argument_list|(
name|config
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"exception on constructor"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

