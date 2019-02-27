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
name|hive
operator|.
name|beeline
operator|.
name|schematool
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
name|tools
operator|.
name|schematool
operator|.
name|HiveSchemaHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|powermock
operator|.
name|core
operator|.
name|classloader
operator|.
name|annotations
operator|.
name|PowerMockIgnore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|powermock
operator|.
name|core
operator|.
name|classloader
operator|.
name|annotations
operator|.
name|PrepareForTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|powermock
operator|.
name|modules
operator|.
name|junit4
operator|.
name|PowerMockRunner
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

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
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|eq
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|same
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|powermock
operator|.
name|api
operator|.
name|mockito
operator|.
name|PowerMockito
operator|.
name|mockStatic
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|powermock
operator|.
name|api
operator|.
name|mockito
operator|.
name|PowerMockito
operator|.
name|verifyStatic
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|PowerMockRunner
operator|.
name|class
argument_list|)
annotation|@
name|PowerMockIgnore
argument_list|(
literal|"javax.management.*"
argument_list|)
annotation|@
name|PrepareForTest
argument_list|(
block|{
name|HiveSchemaHelper
operator|.
name|class
block|,
name|HiveSchemaTool
operator|.
name|HiveSchemaToolCommandBuilder
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHiveSchemaTool
block|{
name|String
name|scriptFile
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
literal|"someScript.sql"
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|HiveSchemaTool
operator|.
name|HiveSchemaToolCommandBuilder
name|builder
decl_stmt|;
specifier|private
name|String
name|pasword
init|=
literal|"reallySimplePassword"
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|mockStatic
argument_list|(
name|HiveSchemaHelper
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|HiveSchemaHelper
operator|.
name|getValidConfVar
argument_list|(
name|eq
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECT_URL_KEY
argument_list|)
argument_list|,
name|same
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"someURL"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|HiveSchemaHelper
operator|.
name|getValidConfVar
argument_list|(
name|eq
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECTION_DRIVER
argument_list|)
argument_list|,
name|same
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"someDriver"
argument_list|)
expr_stmt|;
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|scriptFile
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|file
operator|.
name|exists
argument_list|()
condition|)
block|{
name|file
operator|.
name|createNewFile
argument_list|()
expr_stmt|;
block|}
name|builder
operator|=
operator|new
name|HiveSchemaTool
operator|.
name|HiveSchemaToolCommandBuilder
argument_list|(
name|hiveConf
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|"testUser"
argument_list|,
name|pasword
argument_list|,
name|scriptFile
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|globalAssert
parameter_list|()
throws|throws
name|IOException
block|{
name|verifyStatic
argument_list|()
expr_stmt|;
name|HiveSchemaHelper
operator|.
name|getValidConfVar
argument_list|(
name|eq
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECT_URL_KEY
argument_list|)
argument_list|,
name|same
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|HiveSchemaHelper
operator|.
name|getValidConfVar
argument_list|(
name|eq
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECTION_DRIVER
argument_list|)
argument_list|,
name|same
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
operator|new
name|File
argument_list|(
name|scriptFile
argument_list|)
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|shouldReturnStrippedPassword
parameter_list|()
throws|throws
name|IOException
block|{
name|assertFalse
argument_list|(
name|builder
operator|.
name|buildToLog
argument_list|()
operator|.
name|contains
argument_list|(
name|pasword
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|shouldReturnActualPassword
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|strings
init|=
name|builder
operator|.
name|buildToRun
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|strings
argument_list|)
operator|.
name|contains
argument_list|(
name|pasword
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

