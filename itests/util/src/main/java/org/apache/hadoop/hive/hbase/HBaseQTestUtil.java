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
name|hbase
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
name|hbase
operator|.
name|TableName
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
name|hbase
operator|.
name|client
operator|.
name|Admin
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
name|hbase
operator|.
name|client
operator|.
name|Connection
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
name|ql
operator|.
name|QTestUtil
import|;
end_import

begin_comment
comment|/**  * HBaseQTestUtil initializes HBase-specific test fixtures.  */
end_comment

begin_class
specifier|public
class|class
name|HBaseQTestUtil
extends|extends
name|QTestUtil
block|{
comment|/** Name of the HBase table, in both Hive and HBase. */
specifier|public
specifier|static
name|String
name|HBASE_SRC_NAME
init|=
literal|"src_hbase"
decl_stmt|;
comment|/** Name of the table snapshot. */
specifier|public
specifier|static
name|String
name|HBASE_SRC_SNAPSHOT_NAME
init|=
literal|"src_hbase_snapshot"
decl_stmt|;
comment|/** A handle to this harness's cluster */
specifier|private
specifier|final
name|Connection
name|conn
decl_stmt|;
specifier|private
name|HBaseTestSetup
name|hbaseSetup
init|=
literal|null
decl_stmt|;
specifier|public
name|HBaseQTestUtil
parameter_list|(
name|String
name|outDir
parameter_list|,
name|String
name|logDir
parameter_list|,
name|MiniClusterType
name|miniMr
parameter_list|,
name|HBaseTestSetup
name|setup
parameter_list|,
name|String
name|initScript
parameter_list|,
name|String
name|cleanupScript
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|outDir
argument_list|,
name|logDir
argument_list|,
name|miniMr
argument_list|,
literal|null
argument_list|,
literal|"0.20"
argument_list|,
name|initScript
argument_list|,
name|cleanupScript
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hbaseSetup
operator|=
name|setup
expr_stmt|;
name|hbaseSetup
operator|.
name|preTest
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|conn
operator|=
name|setup
operator|.
name|getConnection
argument_list|()
expr_stmt|;
name|super
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|Exception
block|{
comment|// defer
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initConfFromSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|initConfFromSetup
argument_list|()
expr_stmt|;
name|hbaseSetup
operator|.
name|preTest
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|createSources
parameter_list|(
name|String
name|tname
parameter_list|)
throws|throws
name|Exception
block|{
name|super
operator|.
name|createSources
argument_list|(
name|tname
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hive.test.init.phase"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// create and load the input data into the hbase table
name|runCreateTableCmd
argument_list|(
literal|"CREATE TABLE "
operator|+
name|HBASE_SRC_NAME
operator|+
literal|"(key INT, value STRING)"
operator|+
literal|"  STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'"
operator|+
literal|"  WITH SERDEPROPERTIES ('hbase.columns.mapping' = ':key,cf:val')"
operator|+
literal|"  TBLPROPERTIES ('hbase.mapreduce.hfileoutputformat.table.name' = '"
operator|+
name|HBASE_SRC_NAME
operator|+
literal|"')"
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"INSERT OVERWRITE TABLE "
operator|+
name|HBASE_SRC_NAME
operator|+
literal|" SELECT * FROM src"
argument_list|)
expr_stmt|;
comment|// create a snapshot
name|Admin
name|admin
init|=
literal|null
decl_stmt|;
try|try
block|{
name|admin
operator|=
name|conn
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|HBASE_SRC_SNAPSHOT_NAME
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|HBASE_SRC_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hive.test.init.phase"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanUp
parameter_list|(
name|String
name|tname
parameter_list|)
throws|throws
name|Exception
block|{
name|super
operator|.
name|cleanUp
argument_list|(
name|tname
argument_list|)
expr_stmt|;
comment|// drop in case leftover from unsuccessful run
name|db
operator|.
name|dropTable
argument_list|(
name|Warehouse
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|HBASE_SRC_NAME
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
literal|null
decl_stmt|;
try|try
block|{
name|admin
operator|=
name|conn
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|admin
operator|.
name|deleteSnapshots
argument_list|(
name|HBASE_SRC_SNAPSHOT_NAME
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearTestSideEffects
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|clearTestSideEffects
argument_list|()
expr_stmt|;
name|hbaseSetup
operator|.
name|preTest
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

