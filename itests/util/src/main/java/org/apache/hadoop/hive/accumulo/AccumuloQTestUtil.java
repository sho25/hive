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
name|accumulo
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
name|ql
operator|.
name|QTestUtil
import|;
end_import

begin_comment
comment|/**  * AccumuloQTestUtil initializes Accumulo-specific test fixtures.  */
end_comment

begin_class
specifier|public
class|class
name|AccumuloQTestUtil
extends|extends
name|QTestUtil
block|{
specifier|public
name|AccumuloQTestUtil
parameter_list|(
name|String
name|outDir
parameter_list|,
name|String
name|logDir
parameter_list|,
name|MiniClusterType
name|miniMr
parameter_list|,
name|AccumuloTestSetup
name|setup
parameter_list|,
name|String
name|initScript
parameter_list|,
name|String
name|cleanupScript
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|outDir
argument_list|,
name|logDir
argument_list|,
name|miniMr
argument_list|,
literal|null
argument_list|,
name|initScript
argument_list|,
name|cleanupScript
argument_list|)
expr_stmt|;
name|setup
operator|.
name|setupWithHiveConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|super
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|Exception
block|{
comment|// defer
block|}
block|}
end_class

end_unit

