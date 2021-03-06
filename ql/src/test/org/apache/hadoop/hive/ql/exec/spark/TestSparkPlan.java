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
name|io
operator|.
name|HiveKey
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
name|io
operator|.
name|BytesWritable
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
name|spark
operator|.
name|Dependency
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
name|apache
operator|.
name|spark
operator|.
name|rdd
operator|.
name|HadoopRDD
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
name|rdd
operator|.
name|MapPartitionsRDD
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
name|rdd
operator|.
name|RDD
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
name|rdd
operator|.
name|ShuffledRDD
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
name|scala
operator|.
name|Tuple2
import|;
end_import

begin_import
import|import
name|scala
operator|.
name|collection
operator|.
name|JavaConversions
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

begin_class
specifier|public
class|class
name|TestSparkPlan
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSetRDDCallSite
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
literal|"TestSparkPlan-local-dir"
argument_list|)
operator|.
name|toString
argument_list|()
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
literal|"TestSparkPlan-tmp"
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
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
operator|(
operator|(
name|ReExecDriver
operator|)
name|driver
operator|)
operator|.
name|compile
argument_list|(
literal|"select * from test order by col"
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
literal|"TestSparkPlan-app"
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
name|SparkPlanGenerator
name|sparkPlanGenerator
init|=
operator|new
name|SparkPlanGenerator
argument_list|(
name|sc
argument_list|,
literal|null
argument_list|,
name|jobConf
argument_list|,
name|tmpDir
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|SparkPlan
name|sparkPlan
init|=
name|sparkPlanGenerator
operator|.
name|generate
argument_list|(
name|sparkTask
operator|.
name|getWork
argument_list|()
argument_list|)
decl_stmt|;
name|RDD
argument_list|<
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|reducerRdd
init|=
name|sparkPlan
operator|.
name|generateGraph
argument_list|()
operator|.
name|rdd
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|reducerRdd
operator|.
name|name
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Reducer 2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|reducerRdd
operator|instanceof
name|MapPartitionsRDD
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|reducerRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|shortForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Reducer 2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|reducerRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|longForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Explain Plan"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|reducerRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|longForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Reducer 2"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|rdds
init|=
name|JavaConversions
operator|.
name|seqAsJavaList
argument_list|(
name|reducerRdd
operator|.
name|dependencies
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rdds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|RDD
name|shuffledRdd
init|=
name|rdds
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|rdd
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|shuffledRdd
operator|.
name|name
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Reducer 2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|shuffledRdd
operator|.
name|name
argument_list|()
operator|.
name|contains
argument_list|(
literal|"SORT"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|shuffledRdd
operator|instanceof
name|ShuffledRDD
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|shuffledRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|shortForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Reducer 2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|shuffledRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|longForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Explain Plan"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|shuffledRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|longForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Reducer 2"
argument_list|)
argument_list|)
expr_stmt|;
name|rdds
operator|=
name|JavaConversions
operator|.
name|seqAsJavaList
argument_list|(
name|shuffledRdd
operator|.
name|dependencies
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rdds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|RDD
name|mapRdd
init|=
name|rdds
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|rdd
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mapRdd
operator|.
name|name
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Map 1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mapRdd
operator|instanceof
name|MapPartitionsRDD
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mapRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|shortForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Map 1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mapRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|longForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Explain Plan"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mapRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|longForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Map 1"
argument_list|)
argument_list|)
expr_stmt|;
name|rdds
operator|=
name|JavaConversions
operator|.
name|seqAsJavaList
argument_list|(
name|mapRdd
operator|.
name|dependencies
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rdds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|RDD
name|hadoopRdd
init|=
name|rdds
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|rdd
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hadoopRdd
operator|.
name|name
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Map 1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hadoopRdd
operator|.
name|name
argument_list|()
operator|.
name|contains
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hadoopRdd
operator|instanceof
name|HadoopRDD
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hadoopRdd
operator|.
name|creationSite
argument_list|()
operator|.
name|shortForm
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Map 1"
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

