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
name|metadata
operator|.
name|HiveException
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|sqlstd
operator|.
name|SQLStdHiveAuthorizerFactory
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
name|io
operator|.
name|IOException
import|;
end_import

begin_class
specifier|public
class|class
name|TestSparkInvalidFileFormat
block|{
annotation|@
name|Test
specifier|public
name|void
name|readTextFileAsParquet
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandProcessorException
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
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
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
name|SQLStdHiveAuthorizerFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
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
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|,
literal|"spark"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"spark.master"
argument_list|,
literal|"local"
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
literal|"TestSparkInvalidFileFormat-tmp"
argument_list|)
decl_stmt|;
name|File
name|testFile
init|=
operator|new
name|File
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
argument_list|,
literal|"kv1.txt"
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
literal|"CREATE TABLE test_table (key STRING, value STRING)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"LOAD DATA LOCAL INPATH '"
operator|+
name|testFile
operator|+
literal|"' INTO TABLE test_table"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"ALTER TABLE test_table SET FILEFORMAT parquet"
argument_list|)
expr_stmt|;
try|try
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"SELECT * FROM test_table ORDER BY key LIMIT 10"
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|HiveException
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Spark job failed due to task failures"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"kv1.txt is not a Parquet file. expected "
operator|+
literal|"magic number at tail [80, 65, 82, 49] but found [95, 57, 55, 10]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
literal|"DROP TABLE IF EXISTS test_table"
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

