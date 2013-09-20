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
name|cli
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|AlreadyExistsException
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
name|Database
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
name|FieldSchema
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
name|InvalidObjectException
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
name|InvalidOperationException
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
name|metastore
operator|.
name|api
operator|.
name|NoSuchObjectException
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
name|SerDeInfo
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
name|StorageDescriptor
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
name|Table
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
name|Type
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
name|serde
operator|.
name|serdeConstants
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
name|shims
operator|.
name|ShimLoader
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
name|ExitException
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
name|NoExitSecurityManager
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
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

begin_comment
comment|/**  * @deprecated Use/modify {@link org.apache.hive.hcatalog.cli.TestPermsGrp} instead  */
end_comment

begin_class
specifier|public
class|class
name|TestPermsGrp
extends|extends
name|TestCase
block|{
specifier|private
name|boolean
name|isServerRunning
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|msPort
decl_stmt|;
specifier|private
name|HiveConf
name|hcatConf
decl_stmt|;
specifier|private
name|Warehouse
name|clientWH
decl_stmt|;
specifier|private
name|HiveMetaStoreClient
name|msc
decl_stmt|;
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
name|TestPermsGrp
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setSecurityManager
argument_list|(
name|securityManager
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|isServerRunning
condition|)
block|{
return|return;
block|}
name|msPort
operator|=
name|MetaStoreUtils
operator|.
name|findFreePort
argument_list|()
expr_stmt|;
name|MetaStoreUtils
operator|.
name|startMetaStore
argument_list|(
name|msPort
argument_list|,
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
argument_list|)
expr_stmt|;
name|isServerRunning
operator|=
literal|true
expr_stmt|;
name|securityManager
operator|=
name|System
operator|.
name|getSecurityManager
argument_list|()
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|NoExitSecurityManager
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"hive.metastore.local"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://127.0.0.1:"
operator|+
name|msPort
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTCONNECTIONRETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTFAILURERETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_SOCKET_TIMEOUT
argument_list|,
literal|120
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
name|clientWH
operator|=
operator|new
name|Warehouse
argument_list|(
name|hcatConf
argument_list|)
expr_stmt|;
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hcatConf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|" "
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|" "
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testCustomPerms
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
decl_stmt|;
name|String
name|tblName
init|=
literal|"simptbl"
decl_stmt|;
name|String
name|typeName
init|=
literal|"Person"
decl_stmt|;
try|try
block|{
comment|// Lets first test for default permissions, this is the case when user specified nothing.
name|Table
name|tbl
init|=
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|typeName
argument_list|)
decl_stmt|;
name|msc
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
name|Database
name|db
init|=
name|Hive
operator|.
name|get
argument_list|(
name|hcatConf
argument_list|)
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
name|Path
name|dfsPath
init|=
name|clientWH
operator|.
name|getTablePath
argument_list|(
name|db
argument_list|,
name|tblName
argument_list|)
decl_stmt|;
name|cleanupTbl
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|typeName
argument_list|)
expr_stmt|;
comment|// Next user did specify perms.
try|try
block|{
name|HCatCli
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"create table simptbl (name string) stored as RCFILE"
block|,
literal|"-p"
block|,
literal|"rwx-wx---"
block|}
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
operator|instanceof
name|ExitException
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|ExitException
operator|)
name|e
operator|)
operator|.
name|getStatus
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|dfsPath
operator|=
name|clientWH
operator|.
name|getTablePath
argument_list|(
name|db
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dfsPath
operator|.
name|getFileSystem
argument_list|(
name|hcatConf
argument_list|)
operator|.
name|getFileStatus
argument_list|(
name|dfsPath
argument_list|)
operator|.
name|getPermission
argument_list|()
operator|.
name|equals
argument_list|(
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"drwx-wx---"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cleanupTbl
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|typeName
argument_list|)
expr_stmt|;
comment|// User specified perms in invalid format.
name|hcatConf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PERMS
argument_list|,
literal|"rwx"
argument_list|)
expr_stmt|;
comment|// make sure create table fails.
try|try
block|{
name|HCatCli
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"create table simptbl (name string) stored as RCFILE"
block|,
literal|"-p"
block|,
literal|"rwx"
block|}
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|Exception
name|me
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|me
operator|instanceof
name|ExitException
argument_list|)
expr_stmt|;
block|}
comment|// No physical dir gets created.
name|dfsPath
operator|=
name|clientWH
operator|.
name|getTablePath
argument_list|(
name|db
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
try|try
block|{
name|dfsPath
operator|.
name|getFileSystem
argument_list|(
name|hcatConf
argument_list|)
operator|.
name|getFileStatus
argument_list|(
name|dfsPath
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|Exception
name|fnfe
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|fnfe
operator|instanceof
name|FileNotFoundException
argument_list|)
expr_stmt|;
block|}
comment|// And no metadata gets created.
try|try
block|{
name|msc
operator|.
name|getTable
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
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
operator|instanceof
name|NoSuchObjectException
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"default.simptbl table not found"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// test for invalid group name
name|hcatConf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PERMS
argument_list|,
literal|"drw-rw-rw-"
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
literal|"THIS_CANNOT_BE_A_VALID_GRP_NAME_EVER"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// create table must fail.
name|HCatCli
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"create table simptbl (name string) stored as RCFILE"
block|,
literal|"-p"
block|,
literal|"rw-rw-rw-"
block|,
literal|"-g"
block|,
literal|"THIS_CANNOT_BE_A_VALID_GRP_NAME_EVER"
block|}
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|Exception
name|me
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|me
operator|instanceof
name|SecurityException
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// no metadata should get created.
name|msc
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
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
operator|instanceof
name|NoSuchObjectException
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"default.simptbl table not found"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// neither dir should get created.
name|dfsPath
operator|.
name|getFileSystem
argument_list|(
name|hcatConf
argument_list|)
operator|.
name|getFileStatus
argument_list|(
name|dfsPath
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
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
operator|instanceof
name|FileNotFoundException
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
name|LOG
operator|.
name|error
argument_list|(
literal|"testCustomPerms failed."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
name|void
name|silentDropDatabase
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
try|try
block|{
for|for
control|(
name|String
name|tableName
range|:
name|msc
operator|.
name|getTables
argument_list|(
name|dbName
argument_list|,
literal|"*"
argument_list|)
control|)
block|{
name|msc
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{     }
block|}
specifier|private
name|void
name|cleanupTbl
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|String
name|typeName
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
throws|,
name|InvalidOperationException
block|{
name|msc
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|msc
operator|.
name|dropType
argument_list|(
name|typeName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Table
name|getTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|String
name|typeName
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
throws|,
name|AlreadyExistsException
throws|,
name|InvalidObjectException
block|{
name|msc
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|silentDropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|msc
operator|.
name|dropType
argument_list|(
name|typeName
argument_list|)
expr_stmt|;
name|Type
name|typ1
init|=
operator|new
name|Type
argument_list|()
decl_stmt|;
name|typ1
operator|.
name|setName
argument_list|(
name|typeName
argument_list|)
expr_stmt|;
name|typ1
operator|.
name|setFields
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|typ1
operator|.
name|getFields
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"name"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|msc
operator|.
name|createType
argument_list|(
name|typ1
argument_list|)
expr_stmt|;
name|Table
name|tbl
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|tbl
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableName
argument_list|(
name|tblName
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|tbl
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setCols
argument_list|(
name|typ1
operator|.
name|getFields
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSerdeInfo
argument_list|(
operator|new
name|SerDeInfo
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tbl
return|;
block|}
specifier|private
name|SecurityManager
name|securityManager
decl_stmt|;
block|}
end_class

end_unit

