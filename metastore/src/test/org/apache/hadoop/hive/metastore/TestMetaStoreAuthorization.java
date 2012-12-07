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
name|metastore
package|;
end_package

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
name|FileSystem
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
name|shims
operator|.
name|ShimLoader
import|;
end_import

begin_class
specifier|public
class|class
name|TestMetaStoreAuthorization
extends|extends
name|TestCase
block|{
specifier|protected
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|port
init|=
literal|10000
decl_stmt|;
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AUTHORIZATION_STORAGE_AUTH_CHECKS
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|conf
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
name|conf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_CLIENT_CONNECT_RETRY_DELAY
argument_list|,
literal|60
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testIsWritable
parameter_list|()
throws|throws
name|Exception
block|{
name|setup
argument_list|()
expr_stmt|;
name|conf
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
name|String
name|testDir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.warehouse.dir"
argument_list|,
literal|"/tmp"
argument_list|)
decl_stmt|;
name|Path
name|testDirPath
init|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|testDirPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|top
init|=
operator|new
name|Path
argument_list|(
name|testDirPath
argument_list|,
literal|"_foobarbaz12_"
argument_list|)
decl_stmt|;
try|try
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|top
argument_list|)
expr_stmt|;
name|Warehouse
name|wh
init|=
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FsPermission
name|writePerm
init|=
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0777
argument_list|)
decl_stmt|;
name|FsPermission
name|noWritePerm
init|=
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0555
argument_list|)
decl_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|top
argument_list|,
name|writePerm
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected "
operator|+
name|top
operator|+
literal|" to be writable"
argument_list|,
name|wh
operator|.
name|isWritable
argument_list|(
name|top
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|top
argument_list|,
name|noWritePerm
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected "
operator|+
name|top
operator|+
literal|" to be not writable"
argument_list|,
operator|!
name|wh
operator|.
name|isWritable
argument_list|(
name|top
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|fs
operator|.
name|delete
argument_list|(
name|top
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testMetaStoreAuthorization
parameter_list|()
throws|throws
name|Exception
block|{
name|setup
argument_list|()
expr_stmt|;
name|MetaStoreUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
argument_list|)
expr_stmt|;
name|HiveMetaStoreClient
name|client
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
name|String
name|dbName
init|=
literal|"simpdb"
decl_stmt|;
name|Database
name|db1
init|=
literal|null
decl_stmt|;
name|Path
name|p
init|=
literal|null
decl_stmt|;
try|try
block|{
try|try
block|{
name|db1
operator|=
name|client
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|noe
parameter_list|)
block|{}
if|if
condition|(
name|db1
operator|!=
literal|null
condition|)
block|{
name|p
operator|=
operator|new
name|Path
argument_list|(
name|db1
operator|.
name|getLocationUri
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|=
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|db1
operator|=
operator|new
name|Database
argument_list|()
expr_stmt|;
name|db1
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|client
operator|.
name|createDatabase
argument_list|(
name|db1
argument_list|)
expr_stmt|;
name|Database
name|db
init|=
name|client
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Databases do not match"
argument_list|,
name|db1
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|db
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Path
argument_list|(
name|db
operator|.
name|getLocationUri
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|==
literal|null
condition|)
block|{
name|fs
operator|=
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|fs
operator|.
name|setPermission
argument_list|(
name|p
operator|.
name|getParent
argument_list|()
argument_list|,
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0555
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Expected dropDatabase call to fail"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|me
parameter_list|)
block|{       }
name|fs
operator|.
name|setPermission
argument_list|(
name|p
operator|.
name|getParent
argument_list|()
argument_list|,
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0755
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

