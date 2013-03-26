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
name|ql
operator|.
name|CommandNeedRetryException
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
name|Driver
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
name|cli
operator|.
name|SemanticAnalysis
operator|.
name|HCatSemanticAnalyzer
import|;
end_import

begin_comment
comment|/* Unit test for GitHub Howl issue #3 */
end_comment

begin_class
specifier|public
class|class
name|TestUseDatabase
extends|extends
name|TestCase
block|{
specifier|private
name|Driver
name|hcatDriver
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
name|HiveConf
name|hcatConf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
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
name|hcatDriver
operator|=
operator|new
name|Driver
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
block|}
name|String
name|query
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbName
init|=
literal|"testUseDatabase_db"
decl_stmt|;
specifier|private
specifier|final
name|String
name|tblName
init|=
literal|"testUseDatabase_tbl"
decl_stmt|;
specifier|public
name|void
name|testAlterTablePass
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create table "
operator|+
name|tblName
operator|+
literal|" (a int) partitioned by (b string) stored as RCFILE"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|response
decl_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"alter table "
operator|+
name|tblName
operator|+
literal|" add partition (b='2') location '/tmp'"
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
name|assertNull
argument_list|(
name|response
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"alter table "
operator|+
name|tblName
operator|+
literal|" set fileformat INPUTFORMAT 'org.apache.hadoop.hive.ql.io.RCFileInputFormat' OUTPUTFORMAT "
operator|+
literal|"'org.apache.hadoop.hive.ql.io.RCFileOutputFormat' inputdriver 'mydriver' outputdriver 'yourdriver'"
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
name|assertNull
argument_list|(
name|response
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table "
operator|+
name|tblName
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop database "
operator|+
name|dbName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

