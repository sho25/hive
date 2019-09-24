begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2014 The Apache Software Foundation.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|JUnit4TestAdapter
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
name|CommonConfigurationKeysPublic
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
name|metastore
operator|.
name|MetaStoreTestUtils
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
name|api
operator|.
name|Database
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
name|mr
operator|.
name|ExecDriver
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
name|metadata
operator|.
name|*
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
name|processors
operator|.
name|CommandProcessorException
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
name|processors
operator|.
name|CommandProcessorResponse
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
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
name|assertTrue
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
name|After
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

begin_comment
comment|/**  * Tests DDL with remote metastore service and second namenode (HIVE-6374)  *  */
end_comment

begin_class
specifier|public
class|class
name|TestDDLWithRemoteMetastoreSecondNamenode
block|{
specifier|static
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|Database1Name
init|=
literal|"db1_nondefault_nn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|Database2Name
init|=
literal|"db2_nondefault_nn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|Table1Name
init|=
literal|"table1_nondefault_nn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|Table2Name
init|=
literal|"table2_nondefault_nn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|Table3Name
init|=
literal|"table3_nondefault_nn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|Table4Name
init|=
literal|"table4_nondefault_nn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|Table5Name
init|=
literal|"table5_nondefault_nn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|Table6Name
init|=
literal|"table6_nondefault_nn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|Table7Name
init|=
literal|"table7_nondefault_nn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|tmpdir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|tmpdirFs2
init|=
literal|"/"
operator|+
name|TestDDLWithRemoteMetastoreSecondNamenode
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Path
name|tmppath
init|=
operator|new
name|Path
argument_list|(
name|tmpdir
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Path
name|tmppathFs2
init|=
operator|new
name|Path
argument_list|(
name|tmpdirFs2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|fs2Uri
decl_stmt|;
specifier|private
specifier|static
name|MiniDFSCluster
name|miniDfs
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Hive
name|db
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|,
name|fs2
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|jobConf
decl_stmt|;
specifier|private
specifier|static
name|IDriver
name|driver
decl_stmt|;
specifier|private
specifier|static
name|int
name|tests
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
name|Boolean
name|isInitialized
init|=
literal|false
decl_stmt|;
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
name|tests
operator|>
literal|0
condition|)
block|{
return|return;
block|}
name|tests
operator|=
operator|new
name|JUnit4TestAdapter
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|countTestCases
argument_list|()
expr_stmt|;
try|try
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|ExecDriver
operator|.
name|class
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Test with remote metastore service
name|int
name|port
init|=
name|MetaStoreTestUtils
operator|.
name|startMetaStoreWithRetry
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTCONNECTIONRETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
argument_list|,
operator|new
name|URI
argument_list|(
name|tmppath
operator|+
literal|"/warehouse"
argument_list|)
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
comment|// Initialize second mocked filesystem (implement only necessary stuff)
comment|// Physical files are resides in local file system in the similar location
name|jobConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|miniDfs
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fs2
operator|=
name|miniDfs
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
try|try
block|{
name|fs2
operator|.
name|delete
argument_list|(
name|tmppathFs2
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{       }
name|fs2
operator|.
name|mkdirs
argument_list|(
name|tmppathFs2
argument_list|)
expr_stmt|;
name|fs2Uri
operator|=
name|fs2
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
name|CommonConfigurationKeysPublic
operator|.
name|FS_DEFAULT_NAME_KEY
argument_list|,
name|fs2Uri
argument_list|)
expr_stmt|;
name|driver
operator|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|tmppath
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|getFileStatus
argument_list|(
name|tmppath
argument_list|)
operator|.
name|isDir
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|tmpdir
operator|+
literal|" exists but is not a directory"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|tmppath
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|tmppath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not make scratch directory "
operator|+
name|tmpdir
argument_list|)
throw|;
block|}
block|}
name|db
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|cleanup
argument_list|()
expr_stmt|;
name|isInitialized
operator|=
literal|true
expr_stmt|;
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
literal|"Encountered exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
operator|(
name|e
operator|.
name|getCause
argument_list|()
operator|==
literal|null
condition|?
literal|""
else|:
literal|", caused by: "
operator|+
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|isInitialized
condition|)
block|{
name|shutdownMiniDfs
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|--
name|tests
operator|==
literal|0
condition|)
block|{
name|cleanup
argument_list|()
expr_stmt|;
name|shutdownMiniDfs
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|shutdownMiniDfs
parameter_list|()
block|{
if|if
condition|(
name|miniDfs
operator|!=
literal|null
condition|)
block|{
name|miniDfs
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|cleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|srctables
init|=
block|{
name|Table1Name
block|,
name|Table2Name
block|,
name|Database1Name
operator|+
literal|"."
operator|+
name|Table3Name
block|,
name|Database1Name
operator|+
literal|"."
operator|+
name|Table4Name
block|,
name|Table5Name
block|,
name|Table6Name
block|}
decl_stmt|;
for|for
control|(
name|String
name|src
range|:
name|srctables
control|)
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"DROP TABLE IF EXISTS "
operator|+
name|src
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|srcdatabases
init|=
block|{
name|Database1Name
block|,
name|Database2Name
block|}
decl_stmt|;
for|for
control|(
name|String
name|src
range|:
name|srcdatabases
control|)
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"DROP DATABASE IF EXISTS "
operator|+
name|src
operator|+
literal|" CASCADE"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|executeQuery
parameter_list|(
name|String
name|query
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|CommandProcessorResponse
name|result
init|=
name|driver
operator|.
name|run
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"driver.run() was expected to return result for query: "
operator|+
name|query
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Execution of ("
operator|+
name|query
operator|+
literal|") failed with exit status: "
operator|+
name|e
operator|.
name|getResponseCode
argument_list|()
operator|+
literal|", "
operator|+
name|e
operator|.
name|getErrorMessage
argument_list|()
operator|+
literal|", query: "
operator|+
name|query
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|buildLocationClause
parameter_list|(
name|String
name|location
parameter_list|)
block|{
return|return
name|location
operator|==
literal|null
condition|?
literal|""
else|:
operator|(
literal|" LOCATION '"
operator|+
name|location
operator|+
literal|"'"
operator|)
return|;
block|}
specifier|private
name|void
name|addPartitionAndCheck
parameter_list|(
name|Table
name|table
parameter_list|,
name|String
name|column
parameter_list|,
name|String
name|value
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|executeQuery
argument_list|(
literal|"ALTER TABLE "
operator|+
name|table
operator|.
name|getTableName
argument_list|()
operator|+
literal|" ADD PARTITION ("
operator|+
name|column
operator|+
literal|"='"
operator|+
name|value
operator|+
literal|"')"
operator|+
name|buildLocationClause
argument_list|(
name|location
argument_list|)
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionDef1
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
name|partitionDef1
operator|.
name|put
argument_list|(
name|column
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Partition
name|partition
init|=
name|db
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|partitionDef1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Partition object is expected for "
operator|+
name|Table1Name
argument_list|,
name|partition
argument_list|)
expr_stmt|;
name|String
name|locationActual
init|=
name|partition
operator|.
name|getLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|location
operator|==
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
literal|"Partition should be located in the second filesystem"
argument_list|,
name|fs2
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
operator|+
literal|"/p=p1"
argument_list|,
name|locationActual
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
literal|"Partition should be located in the first filesystem"
argument_list|,
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|locationActual
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|"Partition should be located in the second filesystem"
argument_list|,
name|fs2
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|locationActual
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|alterPartitionAndCheck
parameter_list|(
name|Table
name|table
parameter_list|,
name|String
name|column
parameter_list|,
name|String
name|value
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|assertNotNull
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|executeQuery
argument_list|(
literal|"ALTER TABLE "
operator|+
name|table
operator|.
name|getTableName
argument_list|()
operator|+
literal|" PARTITION ("
operator|+
name|column
operator|+
literal|"='"
operator|+
name|value
operator|+
literal|"')"
operator|+
literal|" SET LOCATION '"
operator|+
name|location
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitions
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
name|partitions
operator|.
name|put
argument_list|(
name|column
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Partition
name|partition
init|=
name|db
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|partitions
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Partition object is expected for "
operator|+
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partition
argument_list|)
expr_stmt|;
name|String
name|locationActual
init|=
name|partition
operator|.
name|getLocation
argument_list|()
decl_stmt|;
if|if
condition|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
literal|"Partition should be located in the first filesystem"
argument_list|,
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|locationActual
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|"Partition should be located in the second filesystem"
argument_list|,
name|fs2
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|locationActual
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Table
name|createTableAndCheck
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|tableLocation
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|createTableAndCheck
argument_list|(
literal|null
argument_list|,
name|tableName
argument_list|,
name|tableLocation
argument_list|)
return|;
block|}
specifier|private
name|Table
name|createTableAndCheck
parameter_list|(
name|Table
name|baseTable
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|tableLocation
parameter_list|)
throws|throws
name|Exception
block|{
name|executeQuery
argument_list|(
literal|"CREATE TABLE "
operator|+
name|tableName
operator|+
operator|(
name|baseTable
operator|==
literal|null
condition|?
literal|" (col1 string, col2 string) PARTITIONED BY (p string) "
else|:
literal|" LIKE "
operator|+
name|baseTable
operator|.
name|getTableName
argument_list|()
operator|)
operator|+
name|buildLocationClause
argument_list|(
name|tableLocation
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|db
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Table object is expected for "
operator|+
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|String
name|location
init|=
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableLocation
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
literal|"Table should be located in the second filesystem"
argument_list|,
name|fs2
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|tableLocation
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Since warehouse path is non-qualified the table should be located on second filesystem
name|assertEquals
argument_list|(
literal|"Table should be located in the second filesystem"
argument_list|,
name|fs2
operator|.
name|getUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
operator|new
name|URI
argument_list|(
name|location
argument_list|)
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|table
return|;
block|}
specifier|private
name|void
name|createDatabaseAndCheck
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|databaseLocation
parameter_list|)
throws|throws
name|Exception
block|{
name|executeQuery
argument_list|(
literal|"CREATE DATABASE "
operator|+
name|databaseName
operator|+
name|buildLocationClause
argument_list|(
name|databaseLocation
argument_list|)
argument_list|)
expr_stmt|;
name|Database
name|database
init|=
name|db
operator|.
name|getDatabase
argument_list|(
name|databaseName
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Database object is expected for "
operator|+
name|databaseName
argument_list|,
name|database
argument_list|)
expr_stmt|;
name|String
name|location
init|=
name|database
operator|.
name|getLocationUri
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|databaseLocation
operator|!=
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
literal|"Database should be located in the second filesystem"
argument_list|,
name|fs2
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|databaseLocation
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Since warehouse path is non-qualified the database should be located on second filesystem
name|assertEquals
argument_list|(
literal|"Database should be located in the second filesystem"
argument_list|,
name|fs2
operator|.
name|getUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
operator|new
name|URI
argument_list|(
name|location
argument_list|)
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAlterPartitionSetLocationNonDefaultNameNode
parameter_list|()
throws|throws
name|Exception
block|{
name|assertTrue
argument_list|(
literal|"Test suite should have been initialized"
argument_list|,
name|isInitialized
argument_list|)
expr_stmt|;
name|String
name|tableLocation
init|=
name|tmppathFs2
operator|+
literal|"/"
operator|+
literal|"test_set_part_loc"
decl_stmt|;
name|Table
name|table
init|=
name|createTableAndCheck
argument_list|(
name|Table7Name
argument_list|,
name|tableLocation
argument_list|)
decl_stmt|;
name|addPartitionAndCheck
argument_list|(
name|table
argument_list|,
literal|"p"
argument_list|,
literal|"p1"
argument_list|,
literal|"/tmp/test/1"
argument_list|)
expr_stmt|;
name|alterPartitionAndCheck
argument_list|(
name|table
argument_list|,
literal|"p"
argument_list|,
literal|"p1"
argument_list|,
literal|"/tmp/test/2"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateDatabaseWithTableNonDefaultNameNode
parameter_list|()
throws|throws
name|Exception
block|{
name|assertTrue
argument_list|(
literal|"Test suite should be initialied"
argument_list|,
name|isInitialized
argument_list|)
expr_stmt|;
specifier|final
name|String
name|tableLocation
init|=
name|tmppathFs2
operator|+
literal|"/"
operator|+
name|Table3Name
decl_stmt|;
specifier|final
name|String
name|databaseLocation
init|=
name|tmppathFs2
operator|+
literal|"/"
operator|+
name|Database1Name
decl_stmt|;
comment|// Create database in specific location (absolute non-qualified path)
name|createDatabaseAndCheck
argument_list|(
name|Database1Name
argument_list|,
name|databaseLocation
argument_list|)
expr_stmt|;
comment|// Create database without location clause
name|createDatabaseAndCheck
argument_list|(
name|Database2Name
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Create table in database in specific location
name|createTableAndCheck
argument_list|(
name|Database1Name
operator|+
literal|"."
operator|+
name|Table3Name
argument_list|,
name|tableLocation
argument_list|)
expr_stmt|;
comment|// Create table in database without location clause
name|createTableAndCheck
argument_list|(
name|Database1Name
operator|+
literal|"."
operator|+
name|Table4Name
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

