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
name|pig
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
name|fs
operator|.
name|FileStatus
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
name|PathFilter
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
name|UnknownTableException
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
name|HCatCli
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
name|pig
operator|.
name|HCatStorer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|ExecType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|PigServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|impl
operator|.
name|util
operator|.
name|UDFContext
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

begin_class
specifier|public
class|class
name|TestPermsInheritance
extends|extends
name|TestCase
block|{
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
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
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|msc
operator|.
name|dropTable
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
literal|"testNoPartTbl"
argument_list|,
literal|true
argument_list|,
literal|true
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
name|msc
operator|.
name|dropTable
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
literal|"testPartTbl"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|pig
operator|=
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|,
name|conf
operator|.
name|getAllProperties
argument_list|()
argument_list|)
expr_stmt|;
name|UDFContext
operator|.
name|getUDFContext
argument_list|()
operator|.
name|setClientSystemProps
argument_list|()
expr_stmt|;
block|}
specifier|private
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|private
name|SecurityManager
name|securityManager
decl_stmt|;
specifier|private
name|PigServer
name|pig
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
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
name|securityManager
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
name|HiveConf
name|conf
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
specifier|public
name|void
name|testNoPartTbl
parameter_list|()
throws|throws
name|IOException
throws|,
name|MetaException
throws|,
name|UnknownTableException
throws|,
name|TException
throws|,
name|NoSuchObjectException
throws|,
name|HiveException
block|{
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
literal|"create table testNoPartTbl (line string) stored as RCFILE"
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
name|Warehouse
name|wh
init|=
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|dfsPath
init|=
name|wh
operator|.
name|getTablePath
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getDatabase
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|)
argument_list|,
literal|"testNoPartTbl"
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dfsPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dfsPath
argument_list|)
operator|.
name|getPermission
argument_list|()
argument_list|,
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"drwx-wx---"
argument_list|)
argument_list|)
expr_stmt|;
name|pig
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|pig
operator|.
name|registerQuery
argument_list|(
literal|"A  = load 'build.xml' as (line:chararray);"
argument_list|)
expr_stmt|;
name|pig
operator|.
name|registerQuery
argument_list|(
literal|"store A into 'testNoPartTbl' using "
operator|+
name|HCatStorer
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"();"
argument_list|)
expr_stmt|;
name|pig
operator|.
name|executeBatch
argument_list|()
expr_stmt|;
name|FileStatus
index|[]
name|status
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dfsPath
argument_list|,
name|hiddenFileFilter
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|status
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"drwx-wx---"
argument_list|)
argument_list|,
name|status
index|[
literal|0
index|]
operator|.
name|getPermission
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"create table testPartTbl (line string)  partitioned by (a string) stored as RCFILE"
block|,
literal|"-p"
block|,
literal|"rwx-wx--x"
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
name|wh
operator|.
name|getTablePath
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getDatabase
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|)
argument_list|,
literal|"testPartTbl"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dfsPath
argument_list|)
operator|.
name|getPermission
argument_list|()
argument_list|,
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"drwx-wx--x"
argument_list|)
argument_list|)
expr_stmt|;
name|pig
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|pig
operator|.
name|registerQuery
argument_list|(
literal|"A  = load 'build.xml' as (line:chararray);"
argument_list|)
expr_stmt|;
name|pig
operator|.
name|registerQuery
argument_list|(
literal|"store A into 'testPartTbl' using "
operator|+
name|HCatStorer
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"('a=part');"
argument_list|)
expr_stmt|;
name|pig
operator|.
name|executeBatch
argument_list|()
expr_stmt|;
name|Path
name|partPath
init|=
operator|new
name|Path
argument_list|(
name|dfsPath
argument_list|,
literal|"a=part"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"drwx-wx--x"
argument_list|)
argument_list|,
name|fs
operator|.
name|getFileStatus
argument_list|(
name|partPath
argument_list|)
operator|.
name|getPermission
argument_list|()
argument_list|)
expr_stmt|;
name|status
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|partPath
argument_list|,
name|hiddenFileFilter
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|status
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"drwx-wx--x"
argument_list|)
argument_list|,
name|status
index|[
literal|0
index|]
operator|.
name|getPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|PathFilter
name|hiddenFileFilter
init|=
operator|new
name|PathFilter
argument_list|()
block|{
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|String
name|name
init|=
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
return|;
block|}
block|}
decl_stmt|;
block|}
end_class

end_unit

