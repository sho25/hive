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
operator|.
name|txn
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
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertTrue
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
name|TestTxnHandlerNegative
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestTxnHandlerNegative
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * this intentionally sets a bad URL for connection to test error handling logic    * in TxnHandler    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testBadConnection
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECT_URL_KEY
argument_list|,
literal|"blah"
argument_list|)
expr_stmt|;
name|RuntimeException
name|e
init|=
literal|null
decl_stmt|;
try|try
block|{
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Expected error: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|e
operator|=
name|ex
expr_stmt|;
block|}
name|assertNotNull
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"No suitable driver found for blah"
argument_list|)
operator|||
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Failed to get driver instance for jdbcUrl=blah"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

