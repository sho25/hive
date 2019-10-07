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
name|registry
operator|.
name|impl
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * ZookeeperUtils test suite.  */
end_comment

begin_class
specifier|public
class|class
name|TestZookeeperUtils
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|UserGroupInformation
name|ugi
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|ugi
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|UserGroupInformation
operator|.
name|class
argument_list|)
expr_stmt|;
name|UserGroupInformation
operator|.
name|setLoginUser
argument_list|(
name|ugi
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHadoopAuthKerberosAndZookeeperUseKerberos
parameter_list|()
block|{
name|Mockito
operator|.
name|when
argument_list|(
name|ugi
operator|.
name|isFromKeytab
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_USE_KERBEROS
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ZookeeperUtils
operator|.
name|isKerberosEnabled
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHadoopAuthKerberosAndZookeeperNoKerberos
parameter_list|()
block|{
name|Mockito
operator|.
name|when
argument_list|(
name|ugi
operator|.
name|isFromKeytab
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_USE_KERBEROS
operator|.
name|varname
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ZookeeperUtils
operator|.
name|isKerberosEnabled
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHadoopAuthSimpleAndZookeeperKerberos
parameter_list|()
block|{
name|Mockito
operator|.
name|when
argument_list|(
name|ugi
operator|.
name|isFromKeytab
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_USE_KERBEROS
operator|.
name|varname
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ZookeeperUtils
operator|.
name|isKerberosEnabled
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

