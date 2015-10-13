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
name|client
operator|.
name|HBaseAdmin
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
name|HConnection
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|java
operator|.
name|util
operator|.
name|List
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
name|HConnection
name|conn
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
name|initScript
argument_list|,
name|cleanupScript
argument_list|)
expr_stmt|;
name|setup
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
comment|/** return true when HBase table snapshot exists, false otherwise. */
specifier|private
specifier|static
name|boolean
name|hbaseTableSnapshotExists
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|String
name|snapshotName
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|HBaseProtos
operator|.
name|SnapshotDescription
argument_list|>
name|snapshots
init|=
name|admin
operator|.
name|listSnapshots
argument_list|(
literal|".*"
operator|+
name|snapshotName
operator|+
literal|".*"
argument_list|)
decl_stmt|;
for|for
control|(
name|HBaseProtos
operator|.
name|SnapshotDescription
name|sn
range|:
name|snapshots
control|)
block|{
if|if
condition|(
name|sn
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|HBASE_SRC_SNAPSHOT_NAME
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
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
literal|"  TBLPROPERTIES ('hbase.table.name' = '"
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
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
try|try
block|{
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
name|HBASE_SRC_SNAPSHOT_NAME
argument_list|,
name|HBASE_SRC_NAME
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
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|HBASE_SRC_NAME
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
literal|null
decl_stmt|;
try|try
block|{
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|hbaseTableSnapshotExists
argument_list|(
name|admin
argument_list|,
name|HBASE_SRC_SNAPSHOT_NAME
argument_list|)
condition|)
block|{
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|HBASE_SRC_SNAPSHOT_NAME
argument_list|)
expr_stmt|;
block|}
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
block|}
end_class

end_unit

