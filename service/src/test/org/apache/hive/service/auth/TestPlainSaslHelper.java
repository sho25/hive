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
name|hive
operator|.
name|service
operator|.
name|auth
package|;
end_package

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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|CLIService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|ThriftCLIService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|ThriftBinaryCLIService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TProcessorFactory
import|;
end_import

begin_class
specifier|public
class|class
name|TestPlainSaslHelper
extends|extends
name|TestCase
block|{
comment|/**    * Test setting {@link HiveConf.ConfVars}} config parameter    *   HIVE_SERVER2_ENABLE_DOAS for unsecure mode    */
specifier|public
name|void
name|testDoAsSetting
parameter_list|()
block|{
name|HiveConf
name|hconf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"default value of hive server2 doAs should be true"
argument_list|,
name|hconf
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|)
argument_list|)
expr_stmt|;
name|CLIService
name|cliService
init|=
operator|new
name|CLIService
argument_list|()
decl_stmt|;
name|cliService
operator|.
name|init
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|ThriftCLIService
name|tcliService
init|=
operator|new
name|ThriftBinaryCLIService
argument_list|(
name|cliService
argument_list|)
decl_stmt|;
name|tcliService
operator|.
name|init
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|TProcessorFactory
name|procFactory
init|=
name|PlainSaslHelper
operator|.
name|getPlainProcessorFactory
argument_list|(
name|tcliService
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"doAs enabled processor for unsecure mode"
argument_list|,
name|procFactory
operator|.
name|getProcessor
argument_list|(
literal|null
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|,
name|TUGIContainingProcessor
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

