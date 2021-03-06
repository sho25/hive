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
name|ql
operator|.
name|processors
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|hive
operator|.
name|ql
operator|.
name|session
operator|.
name|SessionState
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
name|verify
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

begin_class
specifier|public
class|class
name|TestResetProcessor
block|{
annotation|@
name|Test
specifier|public
name|void
name|testResetClosesSparkSession
parameter_list|()
throws|throws
name|Exception
block|{
name|SessionState
name|mockSessionState
init|=
name|createMockSparkSessionState
argument_list|()
decl_stmt|;
operator|new
name|ResetProcessor
argument_list|()
operator|.
name|run
argument_list|(
name|mockSessionState
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockSessionState
argument_list|)
operator|.
name|closeSparkSession
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testResetExecutionEngineClosesSparkSession
parameter_list|()
throws|throws
name|Exception
block|{
name|SessionState
name|mockSessionState
init|=
name|createMockSparkSessionState
argument_list|()
decl_stmt|;
operator|new
name|ResetProcessor
argument_list|()
operator|.
name|run
argument_list|(
name|mockSessionState
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
operator|.
name|varname
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockSessionState
argument_list|)
operator|.
name|closeSparkSession
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|SessionState
name|createMockSparkSessionState
parameter_list|()
block|{
name|SessionState
name|mockSessionState
init|=
name|mock
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overriddenConfigurations
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|overriddenConfigurations
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
operator|.
name|varname
argument_list|,
literal|"spark"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockSessionState
operator|.
name|getOverriddenConfigurations
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|overriddenConfigurations
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockSessionState
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|mockSessionState
return|;
block|}
block|}
end_class

end_unit

