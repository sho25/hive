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
name|kudu
package|;
end_package

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
name|QTestMiniClusters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|ColumnSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|Schema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|CreateTableOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|KuduClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|KuduException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|test
operator|.
name|cluster
operator|.
name|MiniKuduCluster
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
name|ImmutableList
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * Start and stop a Kudu MiniCluster for testing purposes.  */
end_comment

begin_class
specifier|public
class|class
name|KuduTestSetup
extends|extends
name|QTestMiniClusters
operator|.
name|QTestSetup
block|{
specifier|public
specifier|static
specifier|final
name|String
name|KV_TABLE_NAME
init|=
literal|"default.kudu_kv"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ALL_TYPES_TABLE_NAME
init|=
literal|"default.kudu_all_types"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Schema
name|ALL_TYPES_SCHEMA
init|=
name|KuduTestUtils
operator|.
name|getAllTypesSchema
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Schema
name|KV_SCHEMA
init|=
operator|new
name|Schema
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|ColumnSchema
operator|.
name|ColumnSchemaBuilder
argument_list|(
literal|"key"
argument_list|,
name|Type
operator|.
name|INT32
argument_list|)
operator|.
name|key
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|ColumnSchema
operator|.
name|ColumnSchemaBuilder
argument_list|(
literal|"value"
argument_list|,
name|Type
operator|.
name|STRING
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
name|MiniKuduCluster
name|miniCluster
decl_stmt|;
specifier|public
name|KuduTestSetup
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|preTest
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|super
operator|.
name|preTest
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|setupWithHiveConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|createKuduTables
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postTest
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|dropKuduTables
argument_list|()
expr_stmt|;
name|super
operator|.
name|postTest
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
literal|null
operator|!=
name|miniCluster
condition|)
block|{
name|miniCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|miniCluster
operator|=
literal|null
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|setupWithHiveConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
literal|null
operator|==
name|miniCluster
condition|)
block|{
name|String
name|testTmpDir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
decl_stmt|;
name|File
name|tmpDir
init|=
operator|new
name|File
argument_list|(
name|testTmpDir
argument_list|,
literal|"kudu"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmpDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
name|tmpDir
argument_list|)
expr_stmt|;
block|}
name|miniCluster
operator|=
operator|new
name|MiniKuduCluster
operator|.
name|MiniKuduClusterBuilder
argument_list|()
operator|.
name|numMasterServers
argument_list|(
literal|3
argument_list|)
operator|.
name|numTabletServers
argument_list|(
literal|3
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|updateConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update hiveConf with the Kudu specific parameters.    * @param conf The hiveconf to update    */
specifier|private
name|void
name|updateConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
if|if
condition|(
name|miniCluster
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_KUDU_MASTER_ADDRESSES_DEFAULT
argument_list|,
name|miniCluster
operator|.
name|getMasterAddressesAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|createKuduTables
parameter_list|()
throws|throws
name|KuduException
block|{
if|if
condition|(
literal|null
operator|!=
name|miniCluster
condition|)
block|{
name|String
name|masterAddresses
init|=
name|miniCluster
operator|.
name|getMasterAddressesAsString
argument_list|()
decl_stmt|;
try|try
init|(
name|KuduClient
name|client
init|=
operator|new
name|KuduClient
operator|.
name|KuduClientBuilder
argument_list|(
name|masterAddresses
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
name|createKVTable
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|createAllTypesTable
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|dropKuduTables
parameter_list|()
throws|throws
name|KuduException
block|{
if|if
condition|(
literal|null
operator|!=
name|miniCluster
condition|)
block|{
name|String
name|masterAddresses
init|=
name|miniCluster
operator|.
name|getMasterAddressesAsString
argument_list|()
decl_stmt|;
try|try
init|(
name|KuduClient
name|client
init|=
operator|new
name|KuduClient
operator|.
name|KuduClientBuilder
argument_list|(
name|masterAddresses
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
name|dropKVTable
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|dropAllTypesTable
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|createKVTable
parameter_list|(
name|KuduClient
name|client
parameter_list|)
throws|throws
name|KuduException
block|{
name|dropKVTable
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|CreateTableOptions
name|options
init|=
operator|new
name|CreateTableOptions
argument_list|()
operator|.
name|setRangePartitionColumns
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"key"
argument_list|)
argument_list|)
decl_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|KV_TABLE_NAME
argument_list|,
name|KV_SCHEMA
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|dropKVTable
parameter_list|(
name|KuduClient
name|client
parameter_list|)
throws|throws
name|KuduException
block|{
if|if
condition|(
name|client
operator|.
name|tableExists
argument_list|(
name|KV_TABLE_NAME
argument_list|)
condition|)
block|{
name|client
operator|.
name|deleteTable
argument_list|(
name|KV_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|createAllTypesTable
parameter_list|(
name|KuduClient
name|client
parameter_list|)
throws|throws
name|KuduException
block|{
name|dropAllTypesTable
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|CreateTableOptions
name|options
init|=
operator|new
name|CreateTableOptions
argument_list|()
operator|.
name|addHashPartitions
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"key"
argument_list|)
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|ALL_TYPES_TABLE_NAME
argument_list|,
name|ALL_TYPES_SCHEMA
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|dropAllTypesTable
parameter_list|(
name|KuduClient
name|client
parameter_list|)
throws|throws
name|KuduException
block|{
if|if
condition|(
name|client
operator|.
name|tableExists
argument_list|(
name|ALL_TYPES_TABLE_NAME
argument_list|)
condition|)
block|{
name|client
operator|.
name|deleteTable
argument_list|(
name|ALL_TYPES_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

