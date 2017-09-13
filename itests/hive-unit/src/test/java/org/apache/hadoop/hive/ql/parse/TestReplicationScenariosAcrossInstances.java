begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   Licensed to the Apache Software Foundation (ASF) under one   or more contributor license agreements.  See the NOTICE file   distributed with this work for additional information   regarding copyright ownership.  The ASF licenses this file   to you under the Apache License, Version 2.0 (the   "License"); you may not use this file except in compliance   with the License.  You may obtain a copy of the License at<p>   http://www.apache.org/licenses/LICENSE-2.0<p>   Unless required by applicable law or agreed to in writing, software   distributed under the License is distributed on an "AS IS" BASIS,   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the License for the specific language governing permissions and   limitations under the License.  */
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
name|lang3
operator|.
name|StringUtils
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
name|fs
operator|.
name|FileStatus
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
name|ql
operator|.
name|parse
operator|.
name|repl
operator|.
name|PathBuilder
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
name|util
operator|.
name|DependencyResolver
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsInAnyOrder
import|;
end_import

begin_class
specifier|public
class|class
name|TestReplicationScenariosAcrossInstances
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
specifier|private
specifier|static
name|WarehouseInstance
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
name|primary
operator|=
operator|new
name|WarehouseInstance
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
argument_list|)
expr_stmt|;
name|replica
operator|=
operator|new
name|WarehouseInstance
argument_list|(
name|LOG
argument_list|,
name|miniDFSCluster
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateFunctionIncrementalReplication
parameter_list|()
throws|throws
name|Throwable
block|{
name|WarehouseInstance
operator|.
name|Tuple
name|bootStrapDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootStrapDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
name|primary
operator|.
name|run
argument_list|(
literal|"CREATE FUNCTION "
operator|+
name|primaryDbName
operator|+
literal|".testFunction as 'hivemall.tools.string.StopwordUDF' "
operator|+
literal|"using jar  'ivy://io.github.myui:hivemall:0.4.0-2'"
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incrementalDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
operator|.
name|run
argument_list|(
literal|"SHOW FUNCTIONS LIKE '"
operator|+
name|replicatedDbName
operator|+
literal|"*'"
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|replicatedDbName
operator|+
literal|".testFunction"
argument_list|)
expr_stmt|;
comment|// Test the idempotent behavior of CREATE FUNCTION
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
operator|.
name|run
argument_list|(
literal|"SHOW FUNCTIONS LIKE '"
operator|+
name|replicatedDbName
operator|+
literal|"*'"
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|replicatedDbName
operator|+
literal|".testFunction"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDropFunctionIncrementalReplication
parameter_list|()
throws|throws
name|Throwable
block|{
name|primary
operator|.
name|run
argument_list|(
literal|"CREATE FUNCTION "
operator|+
name|primaryDbName
operator|+
literal|".testFunction as 'hivemall.tools.string.StopwordUDF' "
operator|+
literal|"using jar  'ivy://io.github.myui:hivemall:0.4.0-2'"
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|bootStrapDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootStrapDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
name|primary
operator|.
name|run
argument_list|(
literal|"Drop FUNCTION "
operator|+
name|primaryDbName
operator|+
literal|".testFunction "
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incrementalDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootStrapDump
operator|.
name|lastReplicationId
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
operator|.
name|run
argument_list|(
literal|"SHOW FUNCTIONS LIKE '*testfunction*'"
argument_list|)
operator|.
name|verifyResult
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// Test the idempotent behavior of DROP FUNCTION
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"REPL STATUS "
operator|+
name|replicatedDbName
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
operator|.
name|run
argument_list|(
literal|"SHOW FUNCTIONS LIKE '*testfunction*'"
argument_list|)
operator|.
name|verifyResult
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBootstrapFunctionReplication
parameter_list|()
throws|throws
name|Throwable
block|{
name|primary
operator|.
name|run
argument_list|(
literal|"CREATE FUNCTION "
operator|+
name|primaryDbName
operator|+
literal|".testFunction as 'hivemall.tools.string.StopwordUDF' "
operator|+
literal|"using jar  'ivy://io.github.myui:hivemall:0.4.0-2'"
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|bootStrapDump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootStrapDump
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"SHOW FUNCTIONS LIKE '"
operator|+
name|replicatedDbName
operator|+
literal|"*'"
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|replicatedDbName
operator|+
literal|".testFunction"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateFunctionWithFunctionBinaryJarsOnHDFS
parameter_list|()
throws|throws
name|Throwable
block|{
name|Dependencies
name|dependencies
init|=
name|dependencies
argument_list|(
literal|"ivy://io.github.myui:hivemall:0.4.0-2"
argument_list|,
name|primary
argument_list|)
decl_stmt|;
name|String
name|jarSubString
init|=
name|dependencies
operator|.
name|toJarSubSql
argument_list|()
decl_stmt|;
name|primary
operator|.
name|run
argument_list|(
literal|"CREATE FUNCTION "
operator|+
name|primaryDbName
operator|+
literal|".anotherFunction as 'hivemall.tools.string.StopwordUDF' "
operator|+
literal|"using "
operator|+
name|jarSubString
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|tuple
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|tuple
operator|.
name|dumpLocation
argument_list|)
operator|.
name|run
argument_list|(
literal|"SHOW FUNCTIONS LIKE '"
operator|+
name|replicatedDbName
operator|+
literal|"*'"
argument_list|)
operator|.
name|verifyResult
argument_list|(
name|replicatedDbName
operator|+
literal|".anotherFunction"
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|fileStatuses
init|=
name|replica
operator|.
name|miniDFSCluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|globStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|replica
operator|.
name|functionsRoot
operator|+
literal|"/"
operator|+
name|replicatedDbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"/anotherfunction/*/*"
argument_list|)
argument_list|,
name|path
lambda|->
name|path
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"jar"
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|expectedDependenciesNames
init|=
name|dependencies
operator|.
name|jarNames
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|fileStatuses
operator|.
name|length
argument_list|,
name|is
argument_list|(
name|equalTo
argument_list|(
name|expectedDependenciesNames
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|jars
init|=
name|Arrays
operator|.
name|stream
argument_list|(
name|fileStatuses
argument_list|)
operator|.
name|map
argument_list|(
name|f
lambda|->
block|{
name|String
index|[]
name|splits
init|=
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
return|return
name|splits
index|[
name|splits
operator|.
name|length
operator|-
literal|1
index|]
return|;
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|jars
argument_list|,
name|containsInAnyOrder
argument_list|(
name|expectedDependenciesNames
operator|.
name|toArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|Dependencies
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|fullQualifiedJarPaths
decl_stmt|;
name|Dependencies
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|fullQualifiedJarPaths
parameter_list|)
block|{
name|this
operator|.
name|fullQualifiedJarPaths
operator|=
name|fullQualifiedJarPaths
expr_stmt|;
block|}
specifier|private
name|String
name|toJarSubSql
parameter_list|()
block|{
return|return
name|StringUtils
operator|.
name|join
argument_list|(
name|fullQualifiedJarPaths
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|p
lambda|->
literal|"jar '"
operator|+
name|p
operator|+
literal|"'"
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|,
literal|","
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|jarNames
parameter_list|()
block|{
return|return
name|fullQualifiedJarPaths
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|p
lambda|->
block|{
name|String
index|[]
name|splits
init|=
name|p
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
return|return
name|splits
index|[
name|splits
operator|.
name|length
operator|-
literal|1
index|]
return|;
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|private
name|Dependencies
name|dependencies
parameter_list|(
name|String
name|ivyPath
parameter_list|,
name|WarehouseInstance
name|onWarehouse
parameter_list|)
throws|throws
name|IOException
throws|,
name|URISyntaxException
throws|,
name|SemanticException
block|{
name|List
argument_list|<
name|URI
argument_list|>
name|localUris
init|=
operator|new
name|DependencyResolver
argument_list|()
operator|.
name|downloadDependencies
argument_list|(
operator|new
name|URI
argument_list|(
name|ivyPath
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|remotePaths
init|=
name|onWarehouse
operator|.
name|copyToHDFS
argument_list|(
name|localUris
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|collect
init|=
name|remotePaths
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
block|{
try|try
block|{
return|return
name|PathBuilder
operator|.
name|fullyQualifiedHDFSUri
argument_list|(
name|r
argument_list|,
name|onWarehouse
operator|.
name|miniDFSCluster
operator|.
name|getFileSystem
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|Dependencies
argument_list|(
name|collect
argument_list|)
return|;
block|}
comment|/*   From the hive logs(hive.log) we can also check for the info statement   fgrep "Total Tasks" [location of hive.log]   each line indicates one run of loadTask.    */
annotation|@
name|Test
specifier|public
name|void
name|testMultipleStagesOfReplicationLoadTask
parameter_list|()
throws|throws
name|Throwable
block|{
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
literal|"create table t1 (id int)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t2 (place string) partitioned by (country string)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table t2 partition(country='india') values ('bangalore')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table t2 partition(country='us') values ('austin')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table t2 partition(country='france') values ('paris')"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t3 (rank int)"
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// each table creation itself takes more than one task, give we are giving a max of 1, we should hit multiple runs.
name|replica
operator|.
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_APPROX_MAX_LOAD_TASKS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|tuple
operator|.
name|dumpLocation
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
literal|"show tables"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"t1"
block|,
literal|"t2"
block|,
literal|"t3"
block|}
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
literal|"select country from t2 order by country"
argument_list|)
operator|.
name|verifyResults
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"france"
block|,
literal|"india"
block|,
literal|"us"
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

