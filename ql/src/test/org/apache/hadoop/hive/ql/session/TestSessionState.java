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
name|ql
operator|.
name|session
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|assertNull
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|Collection
import|;
end_import

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
name|commons
operator|.
name|io
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|MetaStoreUtils
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
name|common
operator|.
name|util
operator|.
name|HiveTestUtils
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
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
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
name|Files
import|;
end_import

begin_comment
comment|/**  * Test SessionState  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|value
operator|=
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSessionState
block|{
specifier|private
specifier|final
name|boolean
name|prewarm
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|clazzDistFileName
init|=
literal|"RefreshedJarClass.jar.V1"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|clazzV2FileName
init|=
literal|"RefreshedJarClass.jar.V2"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|reloadClazzFileName
init|=
literal|"RefreshedJarClass.jar"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|versionMethodName
init|=
literal|"version"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|RELOADED_CLAZZ_PREFIX_NAME
init|=
literal|"RefreshedJarClass"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|JAVA_FILE_EXT
init|=
literal|".java"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|CLAZZ_FILE_EXT
init|=
literal|".class"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|JAR_FILE_EXT
init|=
literal|".jar"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|TXT_FILE_EXT
init|=
literal|".txt"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|V1
init|=
literal|"V1"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|V2
init|=
literal|"V2"
decl_stmt|;
specifier|private
specifier|final
name|String
name|clazzFile
init|=
name|RELOADED_CLAZZ_PREFIX_NAME
operator|+
name|CLAZZ_FILE_EXT
decl_stmt|;
specifier|private
specifier|final
name|String
name|jarFile
init|=
name|RELOADED_CLAZZ_PREFIX_NAME
operator|+
name|JAR_FILE_EXT
decl_stmt|;
specifier|private
specifier|final
name|String
name|javaFile
init|=
name|RELOADED_CLAZZ_PREFIX_NAME
operator|+
name|JAVA_FILE_EXT
decl_stmt|;
specifier|private
specifier|static
name|String
name|hiveReloadPath
decl_stmt|;
specifier|private
name|File
name|reloadFolder
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestSessionState
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|TestSessionState
parameter_list|(
name|Boolean
name|mode
parameter_list|)
block|{
name|this
operator|.
name|prewarm
operator|=
name|mode
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Boolean
index|[]
argument_list|>
name|data
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Boolean
index|[]
index|[]
block|{
block|{
literal|false
block|}
block|,
block|{
literal|true
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|String
name|tmp
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
decl_stmt|;
name|File
name|tmpDir
init|=
operator|new
name|File
argument_list|(
name|tmp
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tmpDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|tmpDir
operator|.
name|mkdir
argument_list|()
expr_stmt|;
block|}
name|hiveReloadPath
operator|=
name|Files
operator|.
name|createTempDir
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
expr_stmt|;
comment|// create the reloading folder to place jar files if not exist
name|reloadFolder
operator|=
operator|new
name|File
argument_list|(
name|hiveReloadPath
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|reloadFolder
operator|.
name|exists
argument_list|()
condition|)
block|{
name|reloadFolder
operator|.
name|mkdir
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|generateRefreshJarFiles
argument_list|(
name|V2
argument_list|)
expr_stmt|;
name|generateRefreshJarFiles
argument_list|(
name|V1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"fail to generate refresh jar file due to the error "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|prewarm
condition|)
block|{
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_PREWARM_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_PREWARM_NUM_CONTAINERS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|reloadFolder
argument_list|)
expr_stmt|;
block|}
comment|/**    * test set and get db    */
annotation|@
name|Test
specifier|public
name|void
name|testgetDbName
parameter_list|()
throws|throws
name|Exception
block|{
comment|//check that we start with default db
name|assertEquals
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|newdb
init|=
literal|"DB_2"
decl_stmt|;
comment|//set new db and verify get
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|setCurrentDatabase
argument_list|(
name|newdb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newdb
argument_list|,
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
argument_list|)
expr_stmt|;
comment|//verify that a new sessionstate has default db
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClose
parameter_list|()
throws|throws
name|Exception
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|ss
operator|.
name|getTezSession
argument_list|()
argument_list|)
expr_stmt|;
name|ss
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|ss
operator|.
name|getTezSession
argument_list|()
argument_list|)
expr_stmt|;
block|}
class|class
name|RegisterJarRunnable
implements|implements
name|Runnable
block|{
name|String
name|jar
decl_stmt|;
name|ClassLoader
name|loader
decl_stmt|;
name|SessionState
name|ss
decl_stmt|;
specifier|public
name|RegisterJarRunnable
parameter_list|(
name|String
name|jar
parameter_list|,
name|SessionState
name|ss
parameter_list|)
block|{
name|this
operator|.
name|jar
operator|=
name|jar
expr_stmt|;
name|this
operator|.
name|ss
operator|=
name|ss
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|registerJars
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|jar
argument_list|)
argument_list|)
expr_stmt|;
name|loader
operator|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClassLoaderEquality
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|final
name|SessionState
name|ss1
init|=
operator|new
name|SessionState
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|RegisterJarRunnable
name|otherThread
init|=
operator|new
name|RegisterJarRunnable
argument_list|(
literal|"./build/contrib/test/test-udfs.jar"
argument_list|,
name|ss1
argument_list|)
decl_stmt|;
name|Thread
name|th1
init|=
operator|new
name|Thread
argument_list|(
name|otherThread
argument_list|)
decl_stmt|;
name|th1
operator|.
name|start
argument_list|()
expr_stmt|;
name|th1
operator|.
name|join
argument_list|()
expr_stmt|;
comment|// set state in current thread
name|SessionState
operator|.
name|start
argument_list|(
name|ss1
argument_list|)
expr_stmt|;
name|SessionState
name|ss2
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|ClassLoader
name|loader2
init|=
name|ss2
operator|.
name|conf
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Loader1:(Set in other thread) "
operator|+
name|otherThread
operator|.
name|loader
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Loader2:(Set in SessionState.conf) "
operator|+
name|loader2
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Loader3:(CurrentThread.getContextClassLoader()) "
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Other thread loader and session state loader"
argument_list|,
name|otherThread
operator|.
name|loader
argument_list|,
name|loader2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Other thread loader and current thread loader"
argument_list|,
name|otherThread
operator|.
name|loader
argument_list|,
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getReloadedClazzVersion
parameter_list|(
name|ClassLoader
name|cl
parameter_list|)
throws|throws
name|Exception
block|{
name|Class
name|addedClazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|RELOADED_CLAZZ_PREFIX_NAME
argument_list|,
literal|true
argument_list|,
name|cl
argument_list|)
decl_stmt|;
name|Method
name|versionMethod
init|=
name|addedClazz
operator|.
name|getMethod
argument_list|(
name|versionMethodName
argument_list|)
decl_stmt|;
return|return
operator|(
name|String
operator|)
name|versionMethod
operator|.
name|invoke
argument_list|(
name|addedClazz
operator|.
name|newInstance
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|void
name|generateRefreshJarFiles
parameter_list|(
name|String
name|version
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|u
init|=
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
name|RELOADED_CLAZZ_PREFIX_NAME
operator|+
name|version
operator|+
name|TXT_FILE_EXT
argument_list|)
decl_stmt|;
name|File
name|dir
init|=
operator|new
name|File
argument_list|(
name|u
argument_list|)
decl_stmt|;
name|File
name|parentDir
init|=
name|dir
operator|.
name|getParentFile
argument_list|()
decl_stmt|;
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|parentDir
argument_list|,
name|javaFile
argument_list|)
decl_stmt|;
name|Files
operator|.
name|copy
argument_list|(
name|dir
argument_list|,
name|f
argument_list|)
expr_stmt|;
name|executeCmd
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"javac"
block|,
name|javaFile
block|}
argument_list|,
name|parentDir
argument_list|)
expr_stmt|;
name|executeCmd
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"jar"
block|,
literal|"cf"
block|,
name|jarFile
block|,
name|clazzFile
block|}
argument_list|,
name|parentDir
argument_list|)
expr_stmt|;
name|Files
operator|.
name|move
argument_list|(
operator|new
name|File
argument_list|(
name|parentDir
argument_list|,
name|jarFile
argument_list|)
argument_list|,
operator|new
name|File
argument_list|(
name|parentDir
argument_list|,
name|jarFile
operator|+
literal|"."
operator|+
name|version
argument_list|)
argument_list|)
expr_stmt|;
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
operator|new
name|File
argument_list|(
name|parentDir
argument_list|,
name|clazzFile
argument_list|)
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|executeCmd
parameter_list|(
name|String
index|[]
name|cmdArr
parameter_list|,
name|File
name|dir
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|Process
name|p1
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
name|cmdArr
argument_list|,
literal|null
argument_list|,
name|dir
argument_list|)
decl_stmt|;
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|BufferedReader
name|input
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|p1
operator|.
name|getErrorStream
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|line
decl_stmt|;
try|try
block|{
while|while
condition|(
operator|(
name|line
operator|=
name|input
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to execute the command due the exception "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|p1
operator|.
name|waitFor
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReloadAuxJars2
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVERELOADABLEJARS
argument_list|,
name|hiveReloadPath
argument_list|)
expr_stmt|;
name|SessionState
name|ss
init|=
operator|new
name|SessionState
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|ss
operator|=
name|SessionState
operator|.
name|get
argument_list|()
expr_stmt|;
name|File
name|dist
init|=
literal|null
decl_stmt|;
try|try
block|{
name|dist
operator|=
operator|new
name|File
argument_list|(
name|reloadFolder
operator|.
name|getAbsolutePath
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
name|reloadClazzFileName
argument_list|)
expr_stmt|;
name|Files
operator|.
name|copy
argument_list|(
operator|new
name|File
argument_list|(
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
name|clazzDistFileName
argument_list|)
argument_list|)
argument_list|,
name|dist
argument_list|)
expr_stmt|;
name|ss
operator|.
name|reloadAuxJars
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"version1"
argument_list|,
name|getReloadedClazzVersion
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Reload auxiliary jar test fail with message: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|dist
argument_list|)
expr_stmt|;
try|try
block|{
name|ss
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioException
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
name|ioException
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Fail to close the created session: "
argument_list|,
name|ioException
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReloadExistingAuxJars2
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVERELOADABLEJARS
argument_list|,
name|hiveReloadPath
argument_list|)
expr_stmt|;
name|SessionState
name|ss
init|=
operator|new
name|SessionState
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|File
name|dist
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ss
operator|=
name|SessionState
operator|.
name|get
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"copy jar file 1"
argument_list|)
expr_stmt|;
name|dist
operator|=
operator|new
name|File
argument_list|(
name|reloadFolder
operator|.
name|getAbsolutePath
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
name|reloadClazzFileName
argument_list|)
expr_stmt|;
name|Files
operator|.
name|copy
argument_list|(
operator|new
name|File
argument_list|(
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
name|clazzDistFileName
argument_list|)
argument_list|)
argument_list|,
name|dist
argument_list|)
expr_stmt|;
name|ss
operator|.
name|reloadAuxJars
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"version1"
argument_list|,
name|getReloadedClazzVersion
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"copy jar file 2"
argument_list|)
expr_stmt|;
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|dist
argument_list|)
expr_stmt|;
name|Files
operator|.
name|copy
argument_list|(
operator|new
name|File
argument_list|(
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
name|clazzV2FileName
argument_list|)
argument_list|)
argument_list|,
name|dist
argument_list|)
expr_stmt|;
name|ss
operator|.
name|reloadAuxJars
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"version2"
argument_list|,
name|getReloadedClazzVersion
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|dist
argument_list|)
expr_stmt|;
name|ss
operator|.
name|reloadAuxJars
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"refresh existing jar file case failed with message: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|dist
argument_list|)
expr_stmt|;
try|try
block|{
name|ss
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioException
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
name|ioException
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Fail to close the created session: "
argument_list|,
name|ioException
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

