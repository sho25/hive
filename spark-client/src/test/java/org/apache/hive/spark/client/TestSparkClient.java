begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
package|;
end_package

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
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|ZipEntry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|JarOutputStream
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Strings
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|ByteStreams
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
name|FutureAction
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
name|SparkFiles
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
name|JavaFutureAction
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
name|JavaRDD
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
name|function
operator|.
name|Function
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
name|function
operator|.
name|VoidFunction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestSparkClient
block|{
comment|// Timeouts are bad... mmmkay.
specifier|private
specifier|static
specifier|final
name|long
name|TIMEOUT
init|=
literal|10
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|createConf
parameter_list|(
name|boolean
name|local
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|local
condition|)
block|{
name|conf
operator|.
name|put
argument_list|(
name|ClientUtils
operator|.
name|CONF_KEY_IN_PROCESS
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|put
argument_list|(
literal|"spark.master"
argument_list|,
literal|"local"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|put
argument_list|(
literal|"spark.app.name"
argument_list|,
literal|"SparkClientSuite Local App"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|classpath
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.class.path"
argument_list|)
decl_stmt|;
name|conf
operator|.
name|put
argument_list|(
literal|"spark.master"
argument_list|,
literal|"local"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|put
argument_list|(
literal|"spark.app.name"
argument_list|,
literal|"SparkClientSuite Remote App"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|put
argument_list|(
literal|"spark.driver.extraClassPath"
argument_list|,
name|classpath
argument_list|)
expr_stmt|;
name|conf
operator|.
name|put
argument_list|(
literal|"spark.executor.extraClassPath"
argument_list|,
name|classpath
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"spark.home"
argument_list|)
argument_list|)
condition|)
block|{
name|conf
operator|.
name|put
argument_list|(
literal|"spark.home"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"spark.home"
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|conf
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJobSubmission
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|true
argument_list|,
operator|new
name|TestFunction
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|SparkClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|JobHandle
argument_list|<
name|String
argument_list|>
name|handle
init|=
name|client
operator|.
name|submit
argument_list|(
operator|new
name|SimpleJob
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"hello"
argument_list|,
name|handle
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleSparkJob
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|true
argument_list|,
operator|new
name|TestFunction
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|SparkClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|JobHandle
argument_list|<
name|Long
argument_list|>
name|handle
init|=
name|client
operator|.
name|submit
argument_list|(
operator|new
name|SparkJob
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|5L
argument_list|)
argument_list|,
name|handle
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testErrorJob
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|true
argument_list|,
operator|new
name|TestFunction
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|SparkClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|JobHandle
argument_list|<
name|String
argument_list|>
name|handle
init|=
name|client
operator|.
name|submit
argument_list|(
operator|new
name|SimpleJob
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|handle
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|ee
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|ee
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IllegalStateException
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoteClient
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|false
argument_list|,
operator|new
name|TestFunction
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|SparkClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|JobHandle
argument_list|<
name|Long
argument_list|>
name|handle
init|=
name|client
operator|.
name|submit
argument_list|(
operator|new
name|SparkJob
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|5L
argument_list|)
argument_list|,
name|handle
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetricsCollection
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|true
argument_list|,
operator|new
name|TestFunction
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|SparkClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|JobHandle
argument_list|<
name|Integer
argument_list|>
name|future
init|=
name|client
operator|.
name|submit
argument_list|(
operator|new
name|AsyncSparkJob
argument_list|()
argument_list|)
decl_stmt|;
name|future
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|MetricsCollection
name|metrics
init|=
name|future
operator|.
name|getMetrics
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|metrics
operator|.
name|getJobIds
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|metrics
operator|.
name|getAllMetrics
argument_list|()
operator|.
name|executorRunTime
operator|>
literal|0L
argument_list|)
expr_stmt|;
name|JobHandle
argument_list|<
name|Integer
argument_list|>
name|future2
init|=
name|client
operator|.
name|submit
argument_list|(
operator|new
name|AsyncSparkJob
argument_list|()
argument_list|)
decl_stmt|;
name|future2
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|MetricsCollection
name|metrics2
init|=
name|future2
operator|.
name|getMetrics
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|metrics2
operator|.
name|getJobIds
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Objects
operator|.
name|equal
argument_list|(
name|metrics
operator|.
name|getJobIds
argument_list|()
argument_list|,
name|metrics2
operator|.
name|getJobIds
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|metrics2
operator|.
name|getAllMetrics
argument_list|()
operator|.
name|executorRunTime
operator|>
literal|0L
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddJarsAndFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|true
argument_list|,
operator|new
name|TestFunction
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|SparkClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|File
name|jar
init|=
literal|null
decl_stmt|;
name|File
name|file
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Test that adding a jar to the remote context makes it show up in the classpath.
name|jar
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"test"
argument_list|,
literal|".jar"
argument_list|)
expr_stmt|;
name|JarOutputStream
name|jarFile
init|=
operator|new
name|JarOutputStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|jar
argument_list|)
argument_list|)
decl_stmt|;
name|jarFile
operator|.
name|putNextEntry
argument_list|(
operator|new
name|ZipEntry
argument_list|(
literal|"test.resource"
argument_list|)
argument_list|)
expr_stmt|;
name|jarFile
operator|.
name|write
argument_list|(
literal|"test resource"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
name|jarFile
operator|.
name|closeEntry
argument_list|()
expr_stmt|;
name|jarFile
operator|.
name|close
argument_list|()
expr_stmt|;
name|client
operator|.
name|addJar
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file:"
operator|+
name|jar
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
comment|// Need to run a Spark job to make sure the jar is added to the class loader. Monitoring
comment|// SparkContext#addJar() doesn't mean much, we can only be sure jars have been distributed
comment|// when we run a task after the jar has been added.
name|String
name|result
init|=
name|client
operator|.
name|submit
argument_list|(
operator|new
name|JarJob
argument_list|()
argument_list|)
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"test resource"
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// Test that adding a file to the remote context makes it available to executors.
name|file
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"test"
argument_list|,
literal|".file"
argument_list|)
expr_stmt|;
name|FileOutputStream
name|fileStream
init|=
operator|new
name|FileOutputStream
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|fileStream
operator|.
name|write
argument_list|(
literal|"test file"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
name|fileStream
operator|.
name|close
argument_list|()
expr_stmt|;
name|client
operator|.
name|addJar
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file:"
operator|+
name|file
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
comment|// The same applies to files added with "addFile". They're only guaranteed to be available
comment|// to tasks started after the addFile() call completes.
name|result
operator|=
name|client
operator|.
name|submit
argument_list|(
operator|new
name|FileJob
argument_list|(
name|file
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test file"
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|jar
operator|!=
literal|null
condition|)
block|{
name|jar
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|file
operator|!=
literal|null
condition|)
block|{
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testKryoSerializer
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|true
argument_list|,
operator|new
name|TestFunction
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|SparkClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|JobHandle
argument_list|<
name|Long
argument_list|>
name|handle
init|=
name|client
operator|.
name|submit
argument_list|(
operator|new
name|SparkJob
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|5L
argument_list|)
argument_list|,
name|handle
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|config
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|put
argument_list|(
name|ClientUtils
operator|.
name|CONF_KEY_SERIALIZER
argument_list|,
literal|"kryo"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runTest
parameter_list|(
name|boolean
name|local
parameter_list|,
name|TestFunction
name|test
parameter_list|)
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
init|=
name|createConf
argument_list|(
name|local
argument_list|)
decl_stmt|;
name|SparkClientFactory
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|SparkClient
name|client
init|=
literal|null
decl_stmt|;
try|try
block|{
name|test
operator|.
name|config
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|client
operator|=
name|SparkClientFactory
operator|.
name|createClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|test
operator|.
name|call
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
name|SparkClientFactory
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|SimpleJob
implements|implements
name|Job
argument_list|<
name|String
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|String
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
block|{
return|return
literal|"hello"
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|SparkJob
implements|implements
name|Job
argument_list|<
name|Long
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|Long
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
block|{
name|JavaRDD
argument_list|<
name|Integer
argument_list|>
name|rdd
init|=
name|jc
operator|.
name|sc
argument_list|()
operator|.
name|parallelize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|rdd
operator|.
name|count
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|AsyncSparkJob
implements|implements
name|Job
argument_list|<
name|Integer
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
throws|throws
name|Exception
block|{
name|JavaRDD
argument_list|<
name|Integer
argument_list|>
name|rdd
init|=
name|jc
operator|.
name|sc
argument_list|()
operator|.
name|parallelize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|JavaFutureAction
argument_list|<
name|?
argument_list|>
name|future
init|=
name|jc
operator|.
name|monitor
argument_list|(
name|rdd
operator|.
name|foreachAsync
argument_list|(
operator|new
name|VoidFunction
argument_list|<
name|Integer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|Integer
name|l
parameter_list|)
throws|throws
name|Exception
block|{          }
block|}
argument_list|)
argument_list|)
decl_stmt|;
name|future
operator|.
name|get
argument_list|(
name|TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|ErrorJob
implements|implements
name|Job
argument_list|<
name|String
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|String
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"This job does not work."
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
class|class
name|JarJob
implements|implements
name|Job
argument_list|<
name|String
argument_list|>
implements|,
name|Function
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|String
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
block|{
return|return
name|jc
operator|.
name|sc
argument_list|()
operator|.
name|parallelize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|this
argument_list|)
operator|.
name|collect
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|call
parameter_list|(
name|Integer
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|ClassLoader
name|ccl
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
name|InputStream
name|in
init|=
name|ccl
operator|.
name|getResourceAsStream
argument_list|(
literal|"test.resource"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|ByteStreams
operator|.
name|toByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|FileJob
implements|implements
name|Job
argument_list|<
name|String
argument_list|>
implements|,
name|Function
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
block|{
specifier|private
specifier|final
name|String
name|fileName
decl_stmt|;
name|FileJob
parameter_list|(
name|String
name|fileName
parameter_list|)
block|{
name|this
operator|.
name|fileName
operator|=
name|fileName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
block|{
return|return
name|jc
operator|.
name|sc
argument_list|()
operator|.
name|parallelize
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|this
argument_list|)
operator|.
name|collect
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|call
parameter_list|(
name|Integer
name|i
parameter_list|)
throws|throws
name|Exception
block|{
name|InputStream
name|in
init|=
operator|new
name|FileInputStream
argument_list|(
name|SparkFiles
operator|.
name|get
argument_list|(
name|fileName
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|ByteStreams
operator|.
name|toByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
specifier|abstract
class|class
name|TestFunction
block|{
specifier|abstract
name|void
name|call
parameter_list|(
name|SparkClient
name|client
parameter_list|)
throws|throws
name|Exception
function_decl|;
name|void
name|config
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
parameter_list|)
block|{ }
block|}
block|}
end_class

end_unit

