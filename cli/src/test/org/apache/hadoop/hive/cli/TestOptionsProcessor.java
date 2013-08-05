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
name|cli
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
name|assertEquals
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
name|assertNull
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
name|Test
import|;
end_import

begin_comment
comment|/**  *  test class OptionsProcessor  */
end_comment

begin_class
specifier|public
class|class
name|TestOptionsProcessor
block|{
comment|/**    * test pase parameters for Hive    */
annotation|@
name|Test
specifier|public
name|void
name|testOptionsProcessor
parameter_list|()
block|{
name|OptionsProcessor
name|processor
init|=
operator|new
name|OptionsProcessor
argument_list|()
decl_stmt|;
name|System
operator|.
name|clearProperty
argument_list|(
literal|"hiveconf"
argument_list|)
expr_stmt|;
name|System
operator|.
name|clearProperty
argument_list|(
literal|"define"
argument_list|)
expr_stmt|;
name|System
operator|.
name|clearProperty
argument_list|(
literal|"hivevar"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"_A"
argument_list|)
argument_list|)
expr_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-hiveconf"
block|,
literal|"_A=B"
block|,
literal|"-define"
block|,
literal|"C=D"
block|,
literal|"-hivevar"
block|,
literal|"X=Y"
block|,
literal|"-S"
block|,
literal|"true"
block|,
literal|"-database"
block|,
literal|"testDb"
block|,
literal|"-e"
block|,
literal|"execString"
block|,
literal|"-v"
block|,
literal|"true"
block|,
literal|"-h"
block|,
literal|"yahoo.host"
block|,
literal|"-p"
block|,
literal|"3000"
block|}
decl_stmt|;
comment|// stage 1
name|assertTrue
argument_list|(
name|processor
operator|.
name|process_stage1
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"B"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"_A"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"D"
argument_list|,
name|processor
operator|.
name|getHiveVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"C"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Y"
argument_list|,
name|processor
operator|.
name|getHiveVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"X"
argument_list|)
argument_list|)
expr_stmt|;
name|CliSessionState
name|sessionState
init|=
operator|new
name|CliSessionState
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
decl_stmt|;
comment|// stage 2
name|processor
operator|.
name|process_stage2
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"testDb"
argument_list|,
name|sessionState
operator|.
name|database
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"execString"
argument_list|,
name|sessionState
operator|.
name|execString
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"yahoo.host"
argument_list|,
name|sessionState
operator|.
name|host
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3000
argument_list|,
name|sessionState
operator|.
name|port
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|sessionState
operator|.
name|initFiles
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|sessionState
operator|.
name|getIsVerbose
argument_list|()
argument_list|)
expr_stmt|;
name|sessionState
operator|.
name|setConf
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|sessionState
operator|.
name|getIsSilent
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test set fileName    */
annotation|@
name|Test
specifier|public
name|void
name|testFiles
parameter_list|()
block|{
name|OptionsProcessor
name|processor
init|=
operator|new
name|OptionsProcessor
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-i"
block|,
literal|"f1"
block|,
literal|"-i"
block|,
literal|"f2"
block|,
literal|"-f"
block|,
literal|"fileName"
block|,}
decl_stmt|;
name|assertTrue
argument_list|(
name|processor
operator|.
name|process_stage1
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|CliSessionState
name|sessionState
init|=
operator|new
name|CliSessionState
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
decl_stmt|;
name|processor
operator|.
name|process_stage2
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"fileName"
argument_list|,
name|sessionState
operator|.
name|fileName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|sessionState
operator|.
name|initFiles
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"f1"
argument_list|,
name|sessionState
operator|.
name|initFiles
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"f2"
argument_list|,
name|sessionState
operator|.
name|initFiles
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

