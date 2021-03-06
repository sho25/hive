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
name|parse
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FileUtils
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
name|apache
operator|.
name|hadoop
operator|.
name|hdfs
operator|.
name|DFSTestUtil
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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|shims
operator|.
name|Utils
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
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
name|junit
operator|.
name|Ignore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|rules
operator|.
name|TestName
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
name|HashMap
import|;
end_import

begin_import
import|import static
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
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_ENABLED
import|;
end_import

begin_import
import|import static
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
name|ReplChangeManager
operator|.
name|SOURCE_OF_REPLICATION
import|;
end_import

begin_class
specifier|public
class|class
name|TestReplicationOnHDFSEncryptedZones
block|{
specifier|private
specifier|static
name|String
name|jksFile
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
literal|"/test.jks"
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
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
name|TestReplicationScenarios
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|WarehouseInstance
name|primary
decl_stmt|;
specifier|private
specifier|static
name|String
name|primaryDbName
decl_stmt|,
name|replicatedDbName
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|MiniDFSCluster
name|miniDFSCluster
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClassSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.client.use.datanode.hostname"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hadoop.proxyuser."
operator|+
name|Utils
operator|.
name|getUGI
argument_list|()
operator|.
name|getShortUserName
argument_list|()
operator|+
literal|".hosts"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hadoop.security.key.provider.path"
argument_list|,
literal|"jceks://file"
operator|+
name|jksFile
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"dfs.namenode.delegation.token.always-use"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXEC_COPYFILE_MAXSIZE
operator|.
name|varname
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXEC_COPYFILE_MAXNUMFILES
operator|.
name|varname
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|METASTORE_AGGREGATE_STATS_CACHE_ENABLED
operator|.
name|varname
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|miniDFSCluster
operator|=
operator|new
name|MiniDFSCluster
operator|.
name|Builder
argument_list|(
name|conf
argument_list|)
operator|.
name|numDataNodes
argument_list|(
literal|1
argument_list|)
operator|.
name|format
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|DFSTestUtil
operator|.
name|createKey
argument_list|(
literal|"test_key"
argument_list|,
name|miniDFSCluster
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|primary
operator|=
operator|new
name|WarehouseInstance
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_INCLUDE_EXTERNAL_TABLES
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
literal|"test_key"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|classLevelTearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|primary
operator|.
name|close
argument_list|()
expr_stmt|;
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
operator|new
name|File
argument_list|(
name|jksFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Throwable
block|{
name|primaryDbName
operator|=
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"_"
operator|+
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|replicatedDbName
operator|=
literal|"replicated_"
operator|+
name|primaryDbName
expr_stmt|;
name|primary
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|primaryDbName
operator|+
literal|" WITH DBPROPERTIES ( '"
operator|+
name|SOURCE_OF_REPLICATION
operator|+
literal|"' = '1,2,3')"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|targetAndSourceHaveDifferentEncryptionZoneKeys
parameter_list|()
throws|throws
name|Throwable
block|{
name|DFSTestUtil
operator|.
name|createKey
argument_list|(
literal|"test_key123"
argument_list|,
name|miniDFSCluster
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|WarehouseInstance
name|replica
init|=
operator|new
name|WarehouseInstance
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DISTCP_DOAS_USER
operator|.
name|varname
argument_list|,
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
name|put
parameter_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPLDIR
operator|.
name|varname
parameter_list|,
name|primary
operator|.
name|repldDir
parameter_list|)
constructor_decl|;
block|}
block|}
argument_list|,
literal|"test_key123"
argument_list|)
decl_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|tuple
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table encrypted_table (id int, value string)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table encrypted_table values (1,'value1')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table encrypted_table values (2,'value2')"
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|)
decl_stmt|;
name|replica
operator|.
name|run
argument_list|(
literal|"repl load "
operator|+
name|primaryDbName
operator|+
literal|" into "
operator|+
name|replicatedDbName
operator|+
literal|" with('hive.repl.add.raw.reserved.namespace'='true', "
operator|+
literal|"'hive.repl.replica.external.table.base.dir'='"
operator|+
name|replica
operator|.
name|externalTableWarehouseRoot
operator|+
literal|"', "
operator|+
literal|"'distcp.options.pugpbx'='', 'distcp.options.skipcrccheck'='')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"repl status "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|tuple
operator|.
name|lastReplicationId
argument_list|)
operator|.
name|run
argument_list|(
literal|"select value from encrypted_table"
argument_list|)
operator|.
name|verifyFailure
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"value1"
block|,
literal|"value2"
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"this is ignored as minidfs cluster as of writing this test looked like did not copy the "
operator|+
literal|"files correctly"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|targetAndSourceHaveSameEncryptionZoneKeys
parameter_list|()
throws|throws
name|Throwable
block|{
name|WarehouseInstance
name|replica
init|=
operator|new
name|WarehouseInstance
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DISTCP_DOAS_USER
operator|.
name|varname
argument_list|,
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
literal|"test_key"
argument_list|)
decl_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|tuple
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table encrypted_table (id int, value string)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table encrypted_table values (1,'value1')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table encrypted_table values (2,'value2')"
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|)
decl_stmt|;
name|replica
operator|.
name|run
argument_list|(
literal|"repl load "
operator|+
name|primaryDbName
operator|+
literal|" into "
operator|+
name|replicatedDbName
operator|+
literal|" with('hive.repl.add.raw.reserved.namespace'='true')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"repl status "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|tuple
operator|.
name|lastReplicationId
argument_list|)
operator|.
name|run
argument_list|(
literal|"select value from encrypted_table"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"value1"
block|,
literal|"value2"
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

