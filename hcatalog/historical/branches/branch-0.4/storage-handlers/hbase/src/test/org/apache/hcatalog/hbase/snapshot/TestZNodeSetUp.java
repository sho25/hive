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
name|hbase
operator|.
name|snapshot
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
name|assertTrue
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
name|Map
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
name|hbase
operator|.
name|HBaseConfiguration
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
name|HCatDriver
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
name|hbase
operator|.
name|SkeletonHBaseTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
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

begin_class
specifier|public
class|class
name|TestZNodeSetUp
extends|extends
name|SkeletonHBaseTest
block|{
specifier|private
specifier|static
name|HiveConf
name|hcatConf
decl_stmt|;
specifier|private
specifier|static
name|HCatDriver
name|hcatDriver
decl_stmt|;
specifier|public
name|void
name|Initialize
parameter_list|()
throws|throws
name|Exception
block|{
name|hcatConf
operator|=
name|getHiveConf
argument_list|()
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
name|URI
name|fsuri
init|=
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
name|getTestDir
argument_list|()
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
comment|//Add hbase properties
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|el
range|:
name|getHbaseConf
argument_list|()
control|)
block|{
if|if
condition|(
name|el
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"hbase."
argument_list|)
condition|)
block|{
name|hcatConf
operator|.
name|set
argument_list|(
name|el
operator|.
name|getKey
argument_list|()
argument_list|,
name|el
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|HBaseConfiguration
operator|.
name|merge
argument_list|(
name|hcatConf
argument_list|,
name|RevisionManagerConfiguration
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|RMConstants
operator|.
name|ZOOKEEPER_DATADIR
argument_list|,
literal|"/rm_base"
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
name|Test
specifier|public
name|void
name|testBasicZNodeCreation
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|int
name|port
init|=
name|getHbaseConf
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.zookeeper.property.clientPort"
argument_list|,
literal|2181
argument_list|)
decl_stmt|;
name|String
name|servers
init|=
name|getHbaseConf
argument_list|()
operator|.
name|get
argument_list|(
literal|"hbase.zookeeper.quorum"
argument_list|)
decl_stmt|;
name|String
index|[]
name|splits
init|=
name|servers
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|split
range|:
name|splits
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|split
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|port
argument_list|)
expr_stmt|;
block|}
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table test_table"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|response
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create table test_table(key int, value string) STORED BY "
operator|+
literal|"'org.apache.hcatalog.hbase.HBaseHCatStorageHandler'"
operator|+
literal|"TBLPROPERTIES ('hbase.columns.mapping'=':key,cf1:val')"
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
name|HBaseAdmin
name|hAdmin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|getHbaseConf
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|doesTableExist
init|=
name|hAdmin
operator|.
name|tableExists
argument_list|(
literal|"test_table"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
argument_list|)
expr_stmt|;
name|ZKUtil
name|zkutil
init|=
operator|new
name|ZKUtil
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|,
literal|"/rm_base"
argument_list|)
decl_stmt|;
name|ZooKeeper
name|zk
init|=
name|zkutil
operator|.
name|getSession
argument_list|()
decl_stmt|;
name|String
name|tablePath
init|=
name|PathUtil
operator|.
name|getTxnDataPath
argument_list|(
literal|"/rm_base"
argument_list|,
literal|"test_table"
argument_list|)
decl_stmt|;
name|Stat
name|tempTwo
init|=
name|zk
operator|.
name|exists
argument_list|(
name|tablePath
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|tempTwo
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|String
name|cfPath
init|=
name|PathUtil
operator|.
name|getTxnDataPath
argument_list|(
literal|"/rm_base"
argument_list|,
literal|"test_table"
argument_list|)
operator|+
literal|"/cf1"
decl_stmt|;
name|Stat
name|tempThree
init|=
name|zk
operator|.
name|exists
argument_list|(
name|cfPath
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|tempThree
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table test_table"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Table path : "
operator|+
name|tablePath
argument_list|)
expr_stmt|;
name|Stat
name|tempFour
init|=
name|zk
operator|.
name|exists
argument_list|(
name|tablePath
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|tempFour
operator|==
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

