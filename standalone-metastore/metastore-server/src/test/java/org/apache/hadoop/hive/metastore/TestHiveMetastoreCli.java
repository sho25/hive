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
name|MetastoreUnitTest
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
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHiveMetastoreCli
block|{
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|CLI_ARGUMENTS
init|=
block|{
literal|"9999"
block|}
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testDefaultCliPortValue
parameter_list|()
block|{
name|Configuration
name|configuration
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|HiveMetaStore
operator|.
name|HiveMetastoreCli
name|cli
init|=
operator|new
name|HiveMetaStore
operator|.
name|HiveMetastoreCli
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|cli
operator|.
name|getPort
argument_list|()
operator|==
name|MetastoreConf
operator|.
name|getIntVar
argument_list|(
name|configuration
argument_list|,
name|ConfVars
operator|.
name|SERVER_PORT
argument_list|)
operator|)
assert|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOverriddenCliPortValue
parameter_list|()
block|{
name|Configuration
name|configuration
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|HiveMetaStore
operator|.
name|HiveMetastoreCli
name|cli
init|=
operator|new
name|HiveMetaStore
operator|.
name|HiveMetastoreCli
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|cli
operator|.
name|parse
argument_list|(
name|TestHiveMetastoreCli
operator|.
name|CLI_ARGUMENTS
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|cli
operator|.
name|getPort
argument_list|()
operator|==
literal|9999
operator|)
assert|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOverriddenMetastoreServerPortValue
parameter_list|()
block|{
name|Configuration
name|configuration
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setLongVar
argument_list|(
name|configuration
argument_list|,
name|ConfVars
operator|.
name|SERVER_PORT
argument_list|,
literal|12345
argument_list|)
expr_stmt|;
name|HiveMetaStore
operator|.
name|HiveMetastoreCli
name|cli
init|=
operator|new
name|HiveMetaStore
operator|.
name|HiveMetastoreCli
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|cli
operator|.
name|getPort
argument_list|()
operator|==
literal|12345
operator|)
assert|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCliOverridesConfiguration
parameter_list|()
block|{
name|Configuration
name|configuration
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setLongVar
argument_list|(
name|configuration
argument_list|,
name|ConfVars
operator|.
name|SERVER_PORT
argument_list|,
literal|12345
argument_list|)
expr_stmt|;
name|HiveMetaStore
operator|.
name|HiveMetastoreCli
name|cli
init|=
operator|new
name|HiveMetaStore
operator|.
name|HiveMetastoreCli
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|cli
operator|.
name|parse
argument_list|(
name|CLI_ARGUMENTS
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|cli
operator|.
name|getPort
argument_list|()
operator|==
literal|9999
operator|)
assert|;
block|}
block|}
end_class

end_unit

