begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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

begin_class
specifier|public
class|class
name|TestReportParser
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestReportParser
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|File
name|baseDir
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|baseDir
operator|=
name|Files
operator|.
name|createTempDir
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
block|{
if|if
condition|(
name|baseDir
operator|!=
literal|null
condition|)
block|{
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|baseDir
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|reportDir
init|=
operator|new
name|File
argument_list|(
literal|"src/test/resources/test-outputs"
argument_list|)
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|reportDir
operator|.
name|listFiles
argument_list|()
control|)
block|{
if|if
condition|(
name|file
operator|.
name|isFile
argument_list|()
condition|)
block|{
if|if
condition|(
name|file
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|".xml"
argument_list|)
condition|)
block|{
name|Files
operator|.
name|copy
argument_list|(
name|file
argument_list|,
operator|new
name|File
argument_list|(
name|baseDir
argument_list|,
literal|"TEST-"
operator|+
name|file
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Files
operator|.
name|copy
argument_list|(
name|file
argument_list|,
operator|new
name|File
argument_list|(
name|baseDir
argument_list|,
name|file
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|JUnitReportParser
name|parser
init|=
operator|new
name|JUnitReportParser
argument_list|(
name|LOG
argument_list|,
name|baseDir
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|parser
operator|.
name|getAllFailedTests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_skewjoin_union_remove_1"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_union_remove_9"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_skewjoin"
argument_list|)
argument_list|,
name|parser
operator|.
name|getAllFailedTests
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_shutdown"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_binary_constant"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_skewjoin_union_remove_1"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_udf_regexp_extract"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_index_auth"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_auto_join17"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_authorization_2"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_load_dyn_part3"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_index_bitmap2"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_groupby_rollup1"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_bucketcontext_3"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_ppd_join"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_rcfile_lazydecompress"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_notable_alias1"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_union_remove_9"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_skewjoin"
argument_list|,
literal|"org.apache.hadoop.hive.cli.TestCliDriver.testCliDriver_multi_insert_gby"
argument_list|)
argument_list|,
name|parser
operator|.
name|getAllExecutedTests
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

