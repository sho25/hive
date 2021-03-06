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
name|exec
operator|.
name|spark
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
name|FileSystem
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
name|exec
operator|.
name|Utilities
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
name|reexec
operator|.
name|ReExecDriver
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
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
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
name|mapreduce
operator|.
name|MRJobConfig
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
name|spark
operator|.
name|client
operator|.
name|JobContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
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
name|Test
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
name|nio
operator|.
name|file
operator|.
name|Paths
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
name|when
import|;
end_import

begin_class
specifier|public
class|class
name|TestHiveSparkClient
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSetJobGroupAndDescription
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|confDir
init|=
literal|"../data/conf/spark/local/hive-site.xml"
decl_stmt|;
name|HiveConf
operator|.
name|setHiveSiteLocation
argument_list|(
operator|new
name|File
argument_list|(
name|confDir
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
argument_list|)
expr_stmt|;
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
comment|// Set to false because we don't launch a job using LocalHiveSparkClient so the
comment|// hive-kryo-registrator jar is never added to the classpath
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_OPTIMIZE_SHUFFLE_SERDE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"spark.local.dir"
argument_list|,
name|Paths
operator|.
name|get
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
argument_list|,
literal|"TestHiveSparkClient-local-dir"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|tmpDir
init|=
operator|new
name|Path
argument_list|(
literal|"TestHiveSparkClient-tmp"
argument_list|)
decl_stmt|;
name|IDriver
name|driver
init|=
literal|null
decl_stmt|;
name|JavaSparkContext
name|sc
init|=
literal|null
decl_stmt|;
try|try
block|{
name|driver
operator|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table test (col int)"
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"select * from test order by col"
decl_stmt|;
operator|(
operator|(
name|ReExecDriver
operator|)
name|driver
operator|)
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|SparkTask
argument_list|>
name|sparkTasks
init|=
name|Utilities
operator|.
name|getSparkTasks
argument_list|(
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getRootTasks
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sparkTasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|SparkTask
name|sparkTask
init|=
name|sparkTasks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|MRJobConfig
operator|.
name|JOB_NAME
argument_list|,
name|query
argument_list|)
expr_stmt|;
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|SparkConf
name|sparkConf
init|=
operator|new
name|SparkConf
argument_list|()
decl_stmt|;
name|sparkConf
operator|.
name|setMaster
argument_list|(
literal|"local"
argument_list|)
expr_stmt|;
name|sparkConf
operator|.
name|setAppName
argument_list|(
literal|"TestHiveSparkClient-app"
argument_list|)
expr_stmt|;
name|sc
operator|=
operator|new
name|JavaSparkContext
argument_list|(
name|sparkConf
argument_list|)
expr_stmt|;
name|byte
index|[]
name|jobConfBytes
init|=
name|KryoSerializer
operator|.
name|serializeJobConf
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|byte
index|[]
name|scratchDirBytes
init|=
name|KryoSerializer
operator|.
name|serialize
argument_list|(
name|tmpDir
argument_list|)
decl_stmt|;
name|byte
index|[]
name|sparkWorkBytes
init|=
name|KryoSerializer
operator|.
name|serialize
argument_list|(
name|sparkTask
operator|.
name|getWork
argument_list|()
argument_list|)
decl_stmt|;
name|RemoteHiveSparkClient
operator|.
name|JobStatusJob
name|job
init|=
operator|new
name|RemoteHiveSparkClient
operator|.
name|JobStatusJob
argument_list|(
name|jobConfBytes
argument_list|,
name|scratchDirBytes
argument_list|,
name|sparkWorkBytes
argument_list|)
decl_stmt|;
name|JobContext
name|mockJobContext
init|=
name|mock
argument_list|(
name|JobContext
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockJobContext
operator|.
name|sc
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|sc
argument_list|)
expr_stmt|;
name|job
operator|.
name|call
argument_list|(
name|mockJobContext
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|sc
operator|.
name|getLocalProperty
argument_list|(
literal|"spark.job.description"
argument_list|)
operator|.
name|contains
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|sc
operator|.
name|getLocalProperty
argument_list|(
literal|"spark.jobGroup.id"
argument_list|)
operator|.
name|contains
argument_list|(
name|sparkTask
operator|.
name|getWork
argument_list|()
operator|.
name|getQueryId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|driver
operator|!=
literal|null
condition|)
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table if exists test"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|sc
operator|!=
literal|null
condition|)
block|{
name|sc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|tmpDir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|tmpDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

