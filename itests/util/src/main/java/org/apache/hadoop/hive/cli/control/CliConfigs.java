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
name|cli
operator|.
name|control
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
name|net
operator|.
name|MalformedURLException
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
name|QTestUtil
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
name|QTestUtil
operator|.
name|MiniClusterType
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
name|CoreParseNegative
import|;
end_import

begin_class
specifier|public
class|class
name|CliConfigs
block|{
specifier|private
specifier|static
name|URL
name|testConfigProps
init|=
name|getTestPropsURL
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|URL
name|getTestPropsURL
parameter_list|()
block|{
try|try
block|{
return|return
operator|new
name|File
argument_list|(
name|AbstractCliConfig
operator|.
name|HIVE_ROOT
operator|+
literal|"/itests/src/test/resources/testconfiguration.properties"
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
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
specifier|public
specifier|static
class|class
name|CliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|CliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minillap.query.files"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minillaplocal.query.files"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minimr.query.files"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minitez.query.files"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"encrypted.query.files"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"spark.only.query.files"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"disabled.query.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientpositive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|ParseNegativeConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|ParseNegativeConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreParseNegative
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/negative"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/compiler/errors"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/negative"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"data/conf/perf-reg/"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|MinimrCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|MinimrCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minimr.query.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientpositive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_for_minimr.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|mr
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|MiniTezCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|MiniTezCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minitez.query.files"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minitez.query.files.shared"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minillap.query.files"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minillap.shared.query.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive/tez"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientpositive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_tez.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_tez.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"data/conf/tez"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|tez
argument_list|)
expr_stmt|;
name|setMetastoreType
argument_list|(
name|MetastoreType
operator|.
name|sql
argument_list|)
expr_stmt|;
name|setFsType
argument_list|(
name|QTestUtil
operator|.
name|FsType
operator|.
name|hdfs
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|MiniLlapCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|MiniLlapCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minillap.query.files"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minillap.shared.query.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive/llap"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientpositive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"data/conf/llap"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|llap
argument_list|)
expr_stmt|;
name|setMetastoreType
argument_list|(
name|MetastoreType
operator|.
name|sql
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|MiniLlapLocalCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|MiniLlapLocalCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minillaplocal.query.files"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minillaplocal.shared.query.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive/llap"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientpositive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"data/conf/llap"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|llap_local
argument_list|)
expr_stmt|;
name|setMetastoreType
argument_list|(
name|MetastoreType
operator|.
name|sql
argument_list|)
expr_stmt|;
name|setFsType
argument_list|(
name|QTestUtil
operator|.
name|FsType
operator|.
name|local
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|EncryptedHDFSCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|EncryptedHDFSCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"encrypted.query.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive/encrypted"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientpositive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_src.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_src.sql"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|mr
argument_list|)
expr_stmt|;
name|setFsType
argument_list|(
name|QTestUtil
operator|.
name|FsType
operator|.
name|encrypted_hdfs
argument_list|)
expr_stmt|;
if|if
condition|(
name|getClusterType
argument_list|()
operator|==
name|MiniClusterType
operator|.
name|tez
condition|)
block|{
name|setHiveConfDir
argument_list|(
literal|"data/conf/tez"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setHiveConfDir
argument_list|(
literal|"data/conf"
argument_list|)
expr_stmt|;
block|}
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|ContribCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|ContribCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"contrib/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"contrib/src/test/results/clientpositive"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/contribclientpositive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_contrib.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_contrib.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|PerfCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|PerfCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CorePerfCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive/perf"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minimr.query.files"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minitez.query.files"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"encrypted.query.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive/perf/"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientpositive/"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_perf_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_perf_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"data/conf/perf-reg/"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|tez
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|CompareCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|CompareCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCompareCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientcompare"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientcompare"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientcompare"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_compare.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_compare.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|NegativeCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|NegativeCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreNegativeCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientnegative"
argument_list|)
expr_stmt|;
name|excludesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minimr.query.negative.files"
argument_list|)
expr_stmt|;
name|excludeQuery
argument_list|(
literal|"authorization_uri_import.q"
argument_list|)
expr_stmt|;
name|excludeQuery
argument_list|(
literal|"spark_job_max_tasks.q"
argument_list|)
expr_stmt|;
name|excludeQuery
argument_list|(
literal|"spark_stage_max_tasks.q"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientnegative"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientnegative"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|NegativeMinimrCli
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|NegativeMinimrCli
parameter_list|()
block|{
name|super
argument_list|(
name|CoreNegativeCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientnegative"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"minimr.query.negative.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientnegative"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientnegative"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_src.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_src.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|mr
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|HBaseCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|HBaseCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreHBaseCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"hbase-handler/src/test/queries/positive"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"hbase-handler/src/test/results/positive"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/hbase-handler/positive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_src_with_stats.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_src.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|DummyConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|DummyConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreDummy
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientcompare"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientcompare"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/clientcompare"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_compare.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_compare.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|HBaseNegativeCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|HBaseNegativeCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreHBaseNegativeCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"hbase-handler/src/test/queries/negative"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"hbase-handler/src/test/results/negative"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/hbase-handler/negative"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_src.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_src.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|ContribNegativeCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|ContribNegativeCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreNegativeCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"contrib/src/test/queries/clientnegative"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"contrib/src/test/results/clientnegative"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/contribclientnegative"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|BeeLineConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|BeeLineConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreBeeLineDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"beeline.positive.include"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive/beeline"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/beelinepositive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_src.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_src.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|AccumuloCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|AccumuloCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreAccumuloCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"accumulo-handler/src/test/queries/positive"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"accumulo-handler/src/test/results/positive"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest/target/qfile-results/accumulo-handler/positive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init_src_with_stats.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup_src.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|SparkCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|SparkCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"spark.query.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive/spark"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest-spark/target/qfile-results/clientpositive/spark"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"data/conf/spark/standalone"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|spark
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|SparkOnYarnCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|SparkOnYarnCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"miniSparkOnYarn.query.files"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"spark.only.query.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientpositive/spark"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest-spark/target/qfile-results/clientpositive/spark"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"data/conf/spark/yarn-client"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|miniSparkOnYarn
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|SparkNegativeCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|SparkNegativeCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreNegativeCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"ql/src/test/queries/clientnegative"
argument_list|)
expr_stmt|;
name|includesFrom
argument_list|(
name|testConfigProps
argument_list|,
literal|"spark.query.negative.files"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"ql/src/test/results/clientnegative/spark"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/qtest-spark/target/qfile-results/clientnegative/spark"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"q_test_init.sql"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"q_test_cleanup.sql"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"data/conf/spark/standalone"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|spark
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|BlobstoreCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|BlobstoreCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreBlobstoreCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"itests/hive-blobstore/src/test/queries/clientpositive"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"itests/hive-blobstore/src/test/results/clientpositive"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/hive-blobstore/target/qfile-results/clientpositive"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"blobstore_test_init.q"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"blobstore_test_cleanup.q"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"itests/hive-blobstore/src/test/resources"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|BlobstoreNegativeCliConfig
extends|extends
name|AbstractCliConfig
block|{
specifier|public
name|BlobstoreNegativeCliConfig
parameter_list|()
block|{
name|super
argument_list|(
name|CoreBlobstoreNegativeCliDriver
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|setQueryDir
argument_list|(
literal|"itests/hive-blobstore/src/test/queries/clientnegative"
argument_list|)
expr_stmt|;
name|setResultsDir
argument_list|(
literal|"itests/hive-blobstore/src/test/results/clientnegative"
argument_list|)
expr_stmt|;
name|setLogDir
argument_list|(
literal|"itests/hive-blobstore/target/qfile-results/clientnegative"
argument_list|)
expr_stmt|;
name|setInitScript
argument_list|(
literal|"blobstore_test_init.q"
argument_list|)
expr_stmt|;
name|setCleanupScript
argument_list|(
literal|"blobstore_test_cleanup.q"
argument_list|)
expr_stmt|;
name|setHiveConfDir
argument_list|(
literal|"itests/hive-blobstore/src/test/resources"
argument_list|)
expr_stmt|;
name|setClusterType
argument_list|(
name|MiniClusterType
operator|.
name|none
argument_list|)
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
literal|"can't construct cliconfig"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

