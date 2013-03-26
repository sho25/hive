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
name|hcatalog
operator|.
name|cli
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
name|cli
operator|.
name|CliSessionState
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
name|Warehouse
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
name|MetaException
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
name|Hive
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
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|MiniCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|cli
operator|.
name|SemanticAnalysis
operator|.
name|HCatSemanticAnalyzer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_class
specifier|public
class|class
name|TestEximSemanticAnalysis
extends|extends
name|TestCase
block|{
specifier|private
specifier|final
name|MiniCluster
name|cluster
init|=
name|MiniCluster
operator|.
name|buildCluster
argument_list|()
decl_stmt|;
specifier|private
name|HiveConf
name|hcatConf
decl_stmt|;
specifier|private
name|HCatDriver
name|hcatDriver
decl_stmt|;
specifier|private
name|Warehouse
name|wh
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestEximSemanticAnalysis
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|hcatConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hcatConf
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
name|hcatConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|SEMANTIC_ANALYZER_HOOK
operator|.
name|varname
argument_list|,
name|HCatSemanticAnalyzer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
literal|"fs.pfile.impl"
argument_list|,
literal|"org.apache.hadoop.fs.ProxyLocalFileSystem"
argument_list|)
expr_stmt|;
name|URI
name|fsuri
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|Path
name|whPath
init|=
operator|new
name|Path
argument_list|(
name|fsuri
operator|.
name|getScheme
argument_list|()
argument_list|,
name|fsuri
operator|.
name|getAuthority
argument_list|()
argument_list|,
literal|"/user/hive/warehouse"
argument_list|)
decl_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPFS
operator|.
name|varname
argument_list|,
name|fsuri
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
name|whPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|wh
operator|=
operator|new
name|Warehouse
argument_list|(
name|hcatConf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hcatConf
argument_list|)
argument_list|)
expr_stmt|;
name|hcatDriver
operator|=
operator|new
name|HCatDriver
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{   }
specifier|public
name|void
name|testExportPerms
parameter_list|()
throws|throws
name|IOException
throws|,
name|MetaException
throws|,
name|HiveException
block|{
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_sem_analysis"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|response
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create table junit_sem_analysis (a int) partitioned by (b string) stored as RCFILE"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|whPath
init|=
name|wh
operator|.
name|getTablePath
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|hcatConf
argument_list|)
operator|.
name|getDatabase
argument_list|(
literal|"default"
argument_list|)
argument_list|,
literal|"junit_sem_analysis"
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|setPermission
argument_list|(
name|whPath
argument_list|,
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"-rwxrwx-wx"
argument_list|)
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|setOwner
argument_list|(
name|whPath
argument_list|,
literal|"nosuchuser"
argument_list|,
literal|"nosuchgroup"
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
literal|"rm -rf /tmp/hcat"
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"export table junit_sem_analysis to 'pfile://local:9080/tmp/hcat/exports/junit_sem_analysis'"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Permission denied expected : "
operator|+
name|response
operator|.
name|getErrorMessage
argument_list|()
argument_list|,
name|response
operator|.
name|getErrorMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"FAILED: Error in semantic analysis: org.apache.hcatalog.common.HCatException : 3000 : Permission denied"
argument_list|)
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
literal|"rm -rf /tmp/hcat"
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_sem_analysis"
argument_list|)
expr_stmt|;
if|if
condition|(
name|response
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|response
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Drop table failed"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testImportPerms
parameter_list|()
throws|throws
name|IOException
throws|,
name|MetaException
throws|,
name|HiveException
block|{
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_sem_analysis"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|response
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create table junit_sem_analysis (a int) partitioned by (b string) stored as RCFILE"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
literal|"rm -rf /tmp/hcat"
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"export table junit_sem_analysis to 'pfile://local:9080/tmp/hcat/exports/junit_sem_analysis'"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_sem_analysis"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create table junit_sem_analysis (a int) partitioned by (b string) stored as RCFILE"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|whPath
init|=
name|wh
operator|.
name|getTablePath
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|hcatConf
argument_list|)
operator|.
name|getDatabase
argument_list|(
literal|"default"
argument_list|)
argument_list|,
literal|"junit_sem_analysis"
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|setPermission
argument_list|(
name|whPath
argument_list|,
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"-rwxrwxr-x"
argument_list|)
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|setOwner
argument_list|(
name|whPath
argument_list|,
literal|"nosuchuser"
argument_list|,
literal|"nosuchgroup"
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"import table junit_sem_analysis from 'pfile://local:9080/tmp/hcat/exports/junit_sem_analysis'"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Permission denied expected: "
operator|+
name|response
operator|.
name|getErrorMessage
argument_list|()
argument_list|,
name|response
operator|.
name|getErrorMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"FAILED: Error in semantic analysis: org.apache.hcatalog.common.HCatException : 3000 : Permission denied"
argument_list|)
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
literal|"rm -rf /tmp/hcat"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|setPermission
argument_list|(
name|whPath
argument_list|,
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"-rwxrwxrwx"
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_sem_analysis"
argument_list|)
expr_stmt|;
if|if
condition|(
name|response
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|response
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Drop table failed"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testImportSetPermsGroup
parameter_list|()
throws|throws
name|IOException
throws|,
name|MetaException
throws|,
name|HiveException
block|{
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_sem_analysis"
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_sem_analysis_imported"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|response
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create table junit_sem_analysis (a int) partitioned by (b string) stored as RCFILE"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
literal|"rm -rf /tmp/hcat"
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"export table junit_sem_analysis to 'pfile://local:9080/tmp/hcat/exports/junit_sem_analysis'"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_sem_analysis"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PERMS
argument_list|,
literal|"-rwxrw-r--"
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_GROUP
argument_list|,
literal|"nosuchgroup"
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"import table junit_sem_analysis_imported from 'pfile://local:9080/tmp/hcat/exports/junit_sem_analysis'"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|whPath
init|=
name|wh
operator|.
name|getTablePath
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|hcatConf
argument_list|)
operator|.
name|getDatabase
argument_list|(
literal|"default"
argument_list|)
argument_list|,
literal|"junit_sem_analysis_imported"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"-rwxrw-r--"
argument_list|)
argument_list|,
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getFileStatus
argument_list|(
name|whPath
argument_list|)
operator|.
name|getPermission
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"nosuchgroup"
argument_list|,
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getFileStatus
argument_list|(
name|whPath
argument_list|)
operator|.
name|getGroup
argument_list|()
argument_list|)
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
literal|"rm -rf /tmp/hcat"
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_sem_analysis_imported"
argument_list|)
expr_stmt|;
if|if
condition|(
name|response
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|response
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Drop table failed"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

