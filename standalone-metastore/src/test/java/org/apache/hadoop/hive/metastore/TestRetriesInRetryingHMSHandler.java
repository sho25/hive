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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|InvocationTargetException
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
name|javax
operator|.
name|jdo
operator|.
name|JDOException
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
operator|.
name|ConfVars
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
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
name|TestRetriesInRetryingHMSHandler
block|{
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RETRY_ATTEMPTS
init|=
literal|3
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
expr_stmt|;
name|MetastoreConf
operator|.
name|setLongVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HMS_HANDLER_ATTEMPTS
argument_list|,
name|RETRY_ATTEMPTS
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HMS_HANDLER_INTERVAL
argument_list|,
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HMS_HANDLER_FORCE_RELOAD_CONF
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/*    * If the init method of HMSHandler throws exception for the first time    * while creating RetryingHMSHandler it should be retried    */
annotation|@
name|Test
specifier|public
name|void
name|testRetryInit
parameter_list|()
throws|throws
name|MetaException
block|{
name|IHMSHandler
name|mockBaseHandler
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|IHMSHandler
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockBaseHandler
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doThrow
argument_list|(
name|JDOException
operator|.
name|class
argument_list|)
operator|.
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|mockBaseHandler
argument_list|)
operator|.
name|init
argument_list|()
expr_stmt|;
name|RetryingHMSHandler
operator|.
name|getProxy
argument_list|(
name|conf
argument_list|,
name|mockBaseHandler
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|mockBaseHandler
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
comment|/*    * init method in HMSHandler should not be retried if there are no exceptions    */
annotation|@
name|Test
specifier|public
name|void
name|testNoRetryInit
parameter_list|()
throws|throws
name|MetaException
block|{
name|IHMSHandler
name|mockBaseHandler
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|IHMSHandler
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockBaseHandler
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|mockBaseHandler
argument_list|)
operator|.
name|init
argument_list|()
expr_stmt|;
name|RetryingHMSHandler
operator|.
name|getProxy
argument_list|(
name|conf
argument_list|,
name|mockBaseHandler
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|mockBaseHandler
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
comment|/*    * If the init method in HMSHandler throws exception all the times it should be retried until    * HiveConf.ConfVars.HMSHANDLERATTEMPTS is reached before giving up    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testRetriesLimit
parameter_list|()
throws|throws
name|MetaException
block|{
name|IHMSHandler
name|mockBaseHandler
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|IHMSHandler
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockBaseHandler
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doThrow
argument_list|(
name|JDOException
operator|.
name|class
argument_list|)
operator|.
name|when
argument_list|(
name|mockBaseHandler
argument_list|)
operator|.
name|init
argument_list|()
expr_stmt|;
name|RetryingHMSHandler
operator|.
name|getProxy
argument_list|(
name|conf
argument_list|,
name|mockBaseHandler
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|mockBaseHandler
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
name|RETRY_ATTEMPTS
argument_list|)
argument_list|)
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
comment|/*    * Test retries when InvocationException wrapped in MetaException wrapped in JDOException    * is thrown    */
annotation|@
name|Test
specifier|public
name|void
name|testWrappedMetaExceptionRetry
parameter_list|()
throws|throws
name|MetaException
block|{
name|IHMSHandler
name|mockBaseHandler
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|IHMSHandler
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockBaseHandler
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|//JDOException wrapped in MetaException wrapped in InvocationException
name|MetaException
name|me
init|=
operator|new
name|MetaException
argument_list|(
literal|"Dummy exception"
argument_list|)
decl_stmt|;
name|me
operator|.
name|initCause
argument_list|(
operator|new
name|JDOException
argument_list|()
argument_list|)
expr_stmt|;
name|InvocationTargetException
name|ex
init|=
operator|new
name|InvocationTargetException
argument_list|(
name|me
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doThrow
argument_list|(
name|me
argument_list|)
operator|.
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|mockBaseHandler
argument_list|)
operator|.
name|init
argument_list|()
expr_stmt|;
name|RetryingHMSHandler
operator|.
name|getProxy
argument_list|(
name|conf
argument_list|,
name|mockBaseHandler
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|mockBaseHandler
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

