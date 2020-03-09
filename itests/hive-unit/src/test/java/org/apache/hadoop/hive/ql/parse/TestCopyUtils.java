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
name|shims
operator|.
name|HadoopShims
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
name|ShimLoader
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
name|junit
operator|.
name|rules
operator|.
name|TestRule
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|hadoop
operator|.
name|hive
operator|.
name|shims
operator|.
name|HadoopShims
operator|.
name|MiniMrShim
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
name|TestCopyUtils
block|{
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
annotation|@
name|Rule
specifier|public
name|TestRule
name|replV1BackwardCompat
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
specifier|static
class|class
name|WarehouseInstanceWithMR
extends|extends
name|WarehouseInstance
block|{
name|MiniMrShim
name|mrCluster
decl_stmt|;
name|WarehouseInstanceWithMR
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|MiniDFSCluster
name|cluster
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overridesForHiveConf
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|logger
argument_list|,
name|cluster
argument_list|,
name|overridesForHiveConf
argument_list|)
expr_stmt|;
name|HadoopShims
name|shims
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
decl_stmt|;
name|mrCluster
operator|=
name|shims
operator|.
name|getLocalMiniTezCluster
argument_list|(
name|hiveConf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|//      mrCluster = shims.getMiniMrCluster(hiveConf, 2,
comment|//          miniDFSCluster.getFileSystem().getUri().toString(), 1);
name|mrCluster
operator|.
name|setupConfiguration
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|mrCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|WarehouseInstanceWithMR
name|primary
decl_stmt|,
name|replica
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|classLevelSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.client.use.datanode.hostname"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|UserGroupInformation
name|ugi
init|=
name|Utils
operator|.
name|getUGI
argument_list|()
decl_stmt|;
specifier|final
name|String
name|currentUser
init|=
name|ugi
operator|.
name|getShortUserName
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hadoop.proxyuser."
operator|+
name|currentUser
operator|+
literal|".hosts"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|MiniDFSCluster
name|miniDFSCluster
init|=
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
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overridesForHiveConf
init|=
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
name|ConfVars
operator|.
name|HIVE_IN_TEST
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_EXEC_COPYFILE_MAXSIZE
operator|.
name|varname
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|put
argument_list|(
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
parameter_list|(
name|ConfVars
operator|.
name|HIVE_DISTCP_DOAS_USER
operator|.
name|varname
parameter_list|,
name|currentUser
parameter_list|)
constructor_decl|;
block|}
block|}
decl_stmt|;
name|primary
operator|=
operator|new
name|WarehouseInstanceWithMR
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|,
name|overridesForHiveConf
argument_list|)
expr_stmt|;
name|overridesForHiveConf
operator|.
name|put
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|REPLDIR
operator|.
name|getHiveName
argument_list|()
argument_list|,
name|primary
operator|.
name|repldDir
argument_list|)
expr_stmt|;
name|replica
operator|=
operator|new
name|WarehouseInstanceWithMR
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|,
name|overridesForHiveConf
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
name|replica
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|primaryDbName
decl_stmt|,
name|replicatedDbName
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Throwable
block|{
name|replV1BackwardCompat
operator|=
name|primary
operator|.
name|getReplivationV1CompatRule
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
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
comment|/**    * We need to have to separate insert statements as we want the table to have two different data files.    * This is required as one of the conditions for distcp to get invoked is to have more than 1 file.    */
annotation|@
name|Test
specifier|public
name|void
name|testPrivilegedDistCpWithSameUserAsCurrentDoesNotTryToImpersonate
parameter_list|()
throws|throws
name|Throwable
block|{
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
literal|"create table t1 (id int)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t1 values (1),(2),(3)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into t1 values (11),(12),(13)"
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
comment|/*       We have to do a comparision on the data of table t1 in replicated database because even though the file       copy will fail due to impersonation failure the driver will return a success code 0. May be something to look at later     */
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|primaryDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"select * from "
operator|+
name|replicatedDbName
operator|+
literal|".t1"
argument_list|)
operator|.
name|verifyResults
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|,
literal|"12"
argument_list|,
literal|"11"
argument_list|,
literal|"13"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

