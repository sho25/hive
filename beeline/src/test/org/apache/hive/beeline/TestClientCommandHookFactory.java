begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
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
name|mockito
operator|.
name|Mockito
operator|.
name|verify
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
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|ArgumentCaptor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
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
name|TestClientCommandHookFactory
block|{
specifier|public
name|BeeLine
name|setupMockData
parameter_list|(
name|boolean
name|isBeeLine
parameter_list|,
name|boolean
name|showDbInPrompt
parameter_list|)
block|{
name|BeeLine
name|mockBeeLine
init|=
name|mock
argument_list|(
name|BeeLine
operator|.
name|class
argument_list|)
decl_stmt|;
name|DatabaseConnection
name|mockDatabaseConnection
init|=
name|mock
argument_list|(
name|DatabaseConnection
operator|.
name|class
argument_list|)
decl_stmt|;
name|Connection
name|mockConnection
init|=
name|mock
argument_list|(
name|Connection
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
name|when
argument_list|(
name|mockConnection
operator|.
name|getSchema
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"newDatabase"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockDatabaseConnection
operator|.
name|getConnection
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockConnection
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|sqlException
parameter_list|)
block|{
comment|// We do mnot test this
block|}
name|when
argument_list|(
name|mockBeeLine
operator|.
name|getDatabaseConnection
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockDatabaseConnection
argument_list|)
expr_stmt|;
name|BeeLineOpts
name|mockBeeLineOpts
init|=
name|mock
argument_list|(
name|BeeLineOpts
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockBeeLineOpts
operator|.
name|getShowDbInPrompt
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|showDbInPrompt
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockBeeLine
operator|.
name|getOpts
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockBeeLineOpts
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockBeeLine
operator|.
name|isBeeLine
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|isBeeLine
argument_list|)
expr_stmt|;
return|return
name|mockBeeLine
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetHookCli
parameter_list|()
block|{
name|BeeLine
name|beeLine
init|=
name|setupMockData
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"set a;"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"set a=b;"
argument_list|)
operator|instanceof
name|ClientCommandHookFactory
operator|.
name|SetCommandHook
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"USE a.b"
argument_list|)
operator|instanceof
name|ClientCommandHookFactory
operator|.
name|UseCommandHook
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"coNNect a.b"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"gO 1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"g"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetHookBeeLineWithShowDbInPrompt
parameter_list|()
block|{
name|BeeLine
name|beeLine
init|=
name|setupMockData
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"set a;"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"set a=b;"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"USE a.b"
argument_list|)
operator|instanceof
name|ClientCommandHookFactory
operator|.
name|UseCommandHook
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"coNNect a.b"
argument_list|)
operator|instanceof
name|ClientCommandHookFactory
operator|.
name|ConnectCommandHook
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"gO 1"
argument_list|)
operator|instanceof
name|ClientCommandHookFactory
operator|.
name|GoCommandHook
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"g"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetHookBeeLineWithoutShowDbInPrompt
parameter_list|()
block|{
name|BeeLine
name|beeLine
init|=
name|setupMockData
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"set a;"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"set a=b;"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"USE a.b"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"coNNect a.b"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"gO 1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"g"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUseHook
parameter_list|()
block|{
name|BeeLine
name|beeLine
init|=
name|setupMockData
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ClientHook
name|hook
init|=
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"USE newDatabase1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hook
operator|instanceof
name|ClientCommandHookFactory
operator|.
name|UseCommandHook
argument_list|)
expr_stmt|;
name|hook
operator|.
name|postHook
argument_list|(
name|beeLine
argument_list|)
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|String
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|beeLine
argument_list|)
operator|.
name|setCurrentDatabase
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"newDatabase1"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConnectHook
parameter_list|()
block|{
name|BeeLine
name|beeLine
init|=
name|setupMockData
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ClientHook
name|hook
init|=
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"coNNect jdbc:hive2://localhost:10000/newDatabase2 a a"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hook
operator|instanceof
name|ClientCommandHookFactory
operator|.
name|ConnectCommandHook
argument_list|)
expr_stmt|;
name|hook
operator|.
name|postHook
argument_list|(
name|beeLine
argument_list|)
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|String
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|beeLine
argument_list|)
operator|.
name|setCurrentDatabase
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"newDatabase2"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGoHook
parameter_list|()
block|{
name|BeeLine
name|beeLine
init|=
name|setupMockData
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ClientHook
name|hook
init|=
name|ClientCommandHookFactory
operator|.
name|get
argument_list|()
operator|.
name|getHook
argument_list|(
name|beeLine
argument_list|,
literal|"go 1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hook
operator|instanceof
name|ClientCommandHookFactory
operator|.
name|GoCommandHook
argument_list|)
expr_stmt|;
name|hook
operator|.
name|postHook
argument_list|(
name|beeLine
argument_list|)
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|String
argument_list|>
name|argument
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|beeLine
argument_list|)
operator|.
name|setCurrentDatabase
argument_list|(
name|argument
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"newDatabase"
argument_list|,
name|argument
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

