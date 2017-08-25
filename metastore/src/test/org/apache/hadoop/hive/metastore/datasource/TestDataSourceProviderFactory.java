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
operator|.
name|datasource
package|;
end_package

begin_import
import|import
name|com
operator|.
name|jolbox
operator|.
name|bonecp
operator|.
name|BoneCPDataSource
import|;
end_import

begin_import
import|import
name|com
operator|.
name|zaxxer
operator|.
name|hikari
operator|.
name|HikariDataSource
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
name|javax
operator|.
name|sql
operator|.
name|DataSource
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_class
specifier|public
class|class
name|TestDataSourceProviderFactory
block|{
specifier|private
name|HiveConf
name|conf
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|init
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_USER_NAME
argument_list|,
literal|"dummyUser"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
argument_list|,
literal|"dummyPass"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoDataSourceCreatedWithoutProps
parameter_list|()
throws|throws
name|SQLException
block|{
name|DataSourceProvider
name|dsp
init|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|,
name|BoneCPDataSourceProvider
operator|.
name|BONECP
argument_list|)
expr_stmt|;
name|dsp
operator|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateBoneCpDataSource
parameter_list|()
throws|throws
name|SQLException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|,
name|BoneCPDataSourceProvider
operator|.
name|BONECP
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|BoneCPDataSourceProvider
operator|.
name|BONECP
operator|+
literal|".firstProp"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|BoneCPDataSourceProvider
operator|.
name|BONECP
operator|+
literal|".secondProp"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|DataSourceProvider
name|dsp
init|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
name|DataSource
name|ds
init|=
name|dsp
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ds
operator|instanceof
name|BoneCPDataSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetBoneCpStringProperty
parameter_list|()
throws|throws
name|SQLException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|,
name|BoneCPDataSourceProvider
operator|.
name|BONECP
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|BoneCPDataSourceProvider
operator|.
name|BONECP
operator|+
literal|".initSQL"
argument_list|,
literal|"select 1 from dual"
argument_list|)
expr_stmt|;
name|DataSourceProvider
name|dsp
init|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
name|DataSource
name|ds
init|=
name|dsp
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ds
operator|instanceof
name|BoneCPDataSource
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"select 1 from dual"
argument_list|,
operator|(
operator|(
name|BoneCPDataSource
operator|)
name|ds
operator|)
operator|.
name|getInitSQL
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetBoneCpNumberProperty
parameter_list|()
throws|throws
name|SQLException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|,
name|BoneCPDataSourceProvider
operator|.
name|BONECP
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|BoneCPDataSourceProvider
operator|.
name|BONECP
operator|+
literal|".acquireRetryDelayInMs"
argument_list|,
literal|"599"
argument_list|)
expr_stmt|;
name|DataSourceProvider
name|dsp
init|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
name|DataSource
name|ds
init|=
name|dsp
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ds
operator|instanceof
name|BoneCPDataSource
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|599L
argument_list|,
operator|(
operator|(
name|BoneCPDataSource
operator|)
name|ds
operator|)
operator|.
name|getAcquireRetryDelayInMs
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetBoneCpBooleanProperty
parameter_list|()
throws|throws
name|SQLException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|,
name|BoneCPDataSourceProvider
operator|.
name|BONECP
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|BoneCPDataSourceProvider
operator|.
name|BONECP
operator|+
literal|".disableJMX"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|DataSourceProvider
name|dsp
init|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
name|DataSource
name|ds
init|=
name|dsp
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ds
operator|instanceof
name|BoneCPDataSource
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
operator|(
operator|(
name|BoneCPDataSource
operator|)
name|ds
operator|)
operator|.
name|isDisableJMX
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateHikariCpDataSource
parameter_list|()
throws|throws
name|SQLException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|,
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
argument_list|)
expr_stmt|;
comment|// This is needed to prevent the HikariDataSource from trying to connect to the DB
name|conf
operator|.
name|set
argument_list|(
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
operator|+
literal|".initializationFailTimeout"
argument_list|,
literal|"-1"
argument_list|)
expr_stmt|;
name|DataSourceProvider
name|dsp
init|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
name|DataSource
name|ds
init|=
name|dsp
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ds
operator|instanceof
name|HikariDataSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetHikariCpStringProperty
parameter_list|()
throws|throws
name|SQLException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|,
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
operator|+
literal|".connectionInitSql"
argument_list|,
literal|"select 1 from dual"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
operator|+
literal|".initializationFailTimeout"
argument_list|,
literal|"-1"
argument_list|)
expr_stmt|;
name|DataSourceProvider
name|dsp
init|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
name|DataSource
name|ds
init|=
name|dsp
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ds
operator|instanceof
name|HikariDataSource
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"select 1 from dual"
argument_list|,
operator|(
operator|(
name|HikariDataSource
operator|)
name|ds
operator|)
operator|.
name|getConnectionInitSql
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetHikariCpNumberProperty
parameter_list|()
throws|throws
name|SQLException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|,
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
operator|+
literal|".idleTimeout"
argument_list|,
literal|"59999"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
operator|+
literal|".initializationFailTimeout"
argument_list|,
literal|"-1"
argument_list|)
expr_stmt|;
name|DataSourceProvider
name|dsp
init|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
name|DataSource
name|ds
init|=
name|dsp
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ds
operator|instanceof
name|HikariDataSource
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|59999L
argument_list|,
operator|(
operator|(
name|HikariDataSource
operator|)
name|ds
operator|)
operator|.
name|getIdleTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetHikariCpBooleanProperty
parameter_list|()
throws|throws
name|SQLException
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|,
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
operator|+
literal|".allowPoolSuspension"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HikariCPDataSourceProvider
operator|.
name|HIKARI
operator|+
literal|".initializationFailTimeout"
argument_list|,
literal|"-1"
argument_list|)
expr_stmt|;
name|DataSourceProvider
name|dsp
init|=
name|DataSourceProviderFactory
operator|.
name|getDataSourceProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|dsp
argument_list|)
expr_stmt|;
name|DataSource
name|ds
init|=
name|dsp
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ds
operator|instanceof
name|HikariDataSource
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
operator|(
operator|(
name|HikariDataSource
operator|)
name|ds
operator|)
operator|.
name|isAllowPoolSuspension
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

