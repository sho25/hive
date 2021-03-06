begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|mapreduce
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
name|fs
operator|.
name|FileUtil
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
name|fs
operator|.
name|Path
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
name|cli
operator|.
name|CliSessionState
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
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|DriverFactory
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
name|IDriver
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
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|ExecType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|PigServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|backend
operator|.
name|executionengine
operator|.
name|ExecException
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
name|BeforeClass
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
name|Properties
import|;
end_import

begin_comment
comment|/**  * Simplify writing HCatalog tests that require a HiveMetaStore.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HCatBaseTest
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HCatBaseTest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEST_DATA_DIR
init|=
name|HCatUtil
operator|.
name|makePathASafeFileName
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|)
operator|+
literal|"/build/test/data/"
operator|+
name|HCatBaseTest
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|TEST_WAREHOUSE_DIR
init|=
name|TEST_DATA_DIR
operator|+
literal|"/warehouse"
decl_stmt|;
specifier|protected
name|HiveConf
name|hiveConf
init|=
literal|null
decl_stmt|;
specifier|protected
name|IDriver
name|driver
init|=
literal|null
decl_stmt|;
specifier|protected
name|HiveMetaStoreClient
name|client
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpTestDataDir
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Using warehouse directory "
operator|+
name|TEST_WAREHOUSE_DIR
argument_list|)
expr_stmt|;
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
argument_list|)
operator|.
name|mkdirs
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|driver
operator|==
literal|null
condition|)
block|{
name|setUpHiveConf
argument_list|()
expr_stmt|;
name|driver
operator|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|client
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Create a new HiveConf and set properties necessary for unit tests.    */
specifier|protected
name|void
name|setUpHiveConf
parameter_list|()
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|workDir
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"target"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"test"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"mapred.local.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"local"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"mapred.system.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"system"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"mapreduce.jobtracker.staging.root.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"staging"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"mapred.temp.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"temp"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
argument_list|,
name|TEST_WAREHOUSE_DIR
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPREDMODE
argument_list|,
literal|"nonstrict"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTIMIZEMETADATAQUERIES
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|logAndRegister
parameter_list|(
name|PigServer
name|server
parameter_list|,
name|String
name|query
parameter_list|)
throws|throws
name|IOException
block|{
name|logAndRegister
argument_list|(
name|server
argument_list|,
name|query
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|logAndRegister
parameter_list|(
name|PigServer
name|server
parameter_list|,
name|String
name|query
parameter_list|,
name|int
name|lineNumber
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|lineNumber
operator|>
literal|0
operator|:
literal|"(lineNumber> 0) is false"
assert|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Registering pig query: "
operator|+
name|query
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
name|query
argument_list|,
name|lineNumber
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|PigServer
name|createPigServer
parameter_list|(
name|boolean
name|stopOnFailure
parameter_list|)
throws|throws
name|ExecException
block|{
return|return
name|createPigServer
argument_list|(
name|stopOnFailure
argument_list|,
operator|new
name|Properties
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * creates PigServer in LOCAL mode.    * http://pig.apache.org/docs/r0.12.0/perf.html#error-handling    * @param stopOnFailure equivalent of "-stop_on_failure" command line arg, setting to 'true' makes    *                      debugging easier    */
specifier|public
specifier|static
name|PigServer
name|createPigServer
parameter_list|(
name|boolean
name|stopOnFailure
parameter_list|,
name|Properties
name|p
parameter_list|)
throws|throws
name|ExecException
block|{
name|Path
name|workDir
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"target"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"test"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|testId
init|=
literal|"HCatBaseTest_"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|p
operator|.
name|put
argument_list|(
literal|"mapred.local.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|testId
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"local"
argument_list|)
expr_stmt|;
name|p
operator|.
name|put
argument_list|(
literal|"mapred.system.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|testId
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"system"
argument_list|)
expr_stmt|;
name|p
operator|.
name|put
argument_list|(
literal|"mapreduce.jobtracker.staging.root.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|testId
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"staging"
argument_list|)
expr_stmt|;
name|p
operator|.
name|put
argument_list|(
literal|"mapred.temp.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|testId
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"temp"
argument_list|)
expr_stmt|;
name|p
operator|.
name|put
argument_list|(
literal|"pig.temp.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|testId
operator|+
name|File
operator|.
name|separator
operator|+
literal|"pig"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"temp"
argument_list|)
expr_stmt|;
if|if
condition|(
name|stopOnFailure
condition|)
block|{
name|p
operator|.
name|put
argument_list|(
literal|"stop.on.failure"
argument_list|,
name|Boolean
operator|.
name|TRUE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|,
name|p
argument_list|)
return|;
block|}
return|return
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|,
name|p
argument_list|)
return|;
block|}
block|}
end_class

end_unit

