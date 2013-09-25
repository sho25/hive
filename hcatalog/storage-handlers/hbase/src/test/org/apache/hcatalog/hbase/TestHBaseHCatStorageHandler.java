begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|snapshot
operator|.
name|RevisionManager
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
name|snapshot
operator|.
name|RevisionManagerConfiguration
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
operator|.
name|NoNodeException
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
name|TestHBaseHCatStorageHandler
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
specifier|private
specifier|static
name|Warehouse
name|wh
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
name|testTableCreateDrop
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
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
name|RevisionManager
name|rm
init|=
name|HBaseRevisionManagerUtil
operator|.
name|getOpenedRevisionManager
argument_list|(
name|hcatConf
argument_list|)
decl_stmt|;
name|rm
operator|.
name|open
argument_list|()
expr_stmt|;
comment|//Should be able to successfully query revision manager
name|rm
operator|.
name|getAbortedWriteTransactions
argument_list|(
literal|"test_table"
argument_list|,
literal|"cf1"
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table test_table"
argument_list|)
expr_stmt|;
name|doesTableExist
operator|=
name|hAdmin
operator|.
name|tableExists
argument_list|(
literal|"test_table"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
operator|==
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|rm
operator|.
name|getAbortedWriteTransactions
argument_list|(
literal|"test_table"
argument_list|,
literal|"cf1"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NoNodeException
argument_list|)
expr_stmt|;
block|}
name|rm
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableCreateDropDifferentCase
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table test_Table"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|response
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create table test_Table(key int, value string) STORED BY "
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
comment|//HBase table gets created with lower case unless specified as a table property.
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
name|RevisionManager
name|rm
init|=
name|HBaseRevisionManagerUtil
operator|.
name|getOpenedRevisionManager
argument_list|(
name|hcatConf
argument_list|)
decl_stmt|;
name|rm
operator|.
name|open
argument_list|()
expr_stmt|;
comment|//Should be able to successfully query revision manager
name|rm
operator|.
name|getAbortedWriteTransactions
argument_list|(
literal|"test_table"
argument_list|,
literal|"cf1"
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table test_table"
argument_list|)
expr_stmt|;
name|doesTableExist
operator|=
name|hAdmin
operator|.
name|tableExists
argument_list|(
literal|"test_table"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
operator|==
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|rm
operator|.
name|getAbortedWriteTransactions
argument_list|(
literal|"test_table"
argument_list|,
literal|"cf1"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NoNodeException
argument_list|)
expr_stmt|;
block|}
name|rm
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableCreateDropCaseSensitive
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table test_Table"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|response
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create table test_Table(key int, value string) STORED BY "
operator|+
literal|"'org.apache.hcatalog.hbase.HBaseHCatStorageHandler'"
operator|+
literal|"TBLPROPERTIES ('hbase.columns.mapping'=':key,cf1:val',"
operator|+
literal|" 'hbase.table.name'='CaseSensitiveTable')"
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
literal|"CaseSensitiveTable"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
argument_list|)
expr_stmt|;
name|RevisionManager
name|rm
init|=
name|HBaseRevisionManagerUtil
operator|.
name|getOpenedRevisionManager
argument_list|(
name|hcatConf
argument_list|)
decl_stmt|;
name|rm
operator|.
name|open
argument_list|()
expr_stmt|;
comment|//Should be able to successfully query revision manager
name|rm
operator|.
name|getAbortedWriteTransactions
argument_list|(
literal|"CaseSensitiveTable"
argument_list|,
literal|"cf1"
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table test_table"
argument_list|)
expr_stmt|;
name|doesTableExist
operator|=
name|hAdmin
operator|.
name|tableExists
argument_list|(
literal|"CaseSensitiveTable"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
operator|==
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|rm
operator|.
name|getAbortedWriteTransactions
argument_list|(
literal|"CaseSensitiveTable"
argument_list|,
literal|"cf1"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NoNodeException
argument_list|)
expr_stmt|;
block|}
name|rm
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableDropNonExistent
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table mytable"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|response
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create table mytable(key int, value string) STORED BY "
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
literal|"mytable"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
argument_list|)
expr_stmt|;
comment|//Now delete the table from hbase
if|if
condition|(
name|hAdmin
operator|.
name|isTableEnabled
argument_list|(
literal|"mytable"
argument_list|)
condition|)
block|{
name|hAdmin
operator|.
name|disableTable
argument_list|(
literal|"mytable"
argument_list|)
expr_stmt|;
block|}
name|hAdmin
operator|.
name|deleteTable
argument_list|(
literal|"mytable"
argument_list|)
expr_stmt|;
name|doesTableExist
operator|=
name|hAdmin
operator|.
name|tableExists
argument_list|(
literal|"mytable"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
operator|==
literal|false
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|responseTwo
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table mytable"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|responseTwo
operator|.
name|getResponseCode
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableCreateExternal
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
literal|"testTable"
decl_stmt|;
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
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|tableDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"key"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tableDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"familyone"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tableDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"familytwo"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|hAdmin
operator|.
name|createTable
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
name|boolean
name|doesTableExist
init|=
name|hAdmin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table mytabletwo"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|response
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"create external table mytabletwo(key int, valueone string, valuetwo string) STORED BY "
operator|+
literal|"'org.apache.hcatalog.hbase.HBaseHCatStorageHandler'"
operator|+
literal|"TBLPROPERTIES ('hbase.columns.mapping'=':key,familyone:val,familytwo:val',"
operator|+
literal|"'hbase.table.name'='testTable')"
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
block|}
block|}
end_class

end_unit

