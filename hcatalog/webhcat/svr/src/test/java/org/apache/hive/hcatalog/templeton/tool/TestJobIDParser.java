begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
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
name|util
operator|.
name|List
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
import|;
end_import

begin_class
specifier|public
class|class
name|TestJobIDParser
block|{
annotation|@
name|Test
specifier|public
name|void
name|testParsePig
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|errFileName
init|=
literal|"src/test/data/status/pig"
decl_stmt|;
name|PigJobIDParser
name|pigJobIDParser
init|=
operator|new
name|PigJobIDParser
argument_list|(
name|errFileName
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|jobs
init|=
name|pigJobIDParser
operator|.
name|parseJobID
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobs
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseHive
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|errFileName
init|=
literal|"src/test/data/status/hive"
decl_stmt|;
name|HiveJobIDParser
name|hiveJobIDParser
init|=
operator|new
name|HiveJobIDParser
argument_list|(
name|errFileName
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|jobs
init|=
name|hiveJobIDParser
operator|.
name|parseJobID
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobs
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseJar
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|errFileName
init|=
literal|"src/test/data/status/jar"
decl_stmt|;
name|JarJobIDParser
name|jarJobIDParser
init|=
operator|new
name|JarJobIDParser
argument_list|(
name|errFileName
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|jobs
init|=
name|jarJobIDParser
operator|.
name|parseJobID
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobs
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseStreaming
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|errFileName
init|=
literal|"src/test/data/status/streaming"
decl_stmt|;
name|JarJobIDParser
name|jarJobIDParser
init|=
operator|new
name|JarJobIDParser
argument_list|(
name|errFileName
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|jobs
init|=
name|jarJobIDParser
operator|.
name|parseJobID
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jobs
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

