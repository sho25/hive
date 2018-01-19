begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
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
name|java
operator|.
name|io
operator|.
name|FileWriter
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
comment|/**  * Utility methods for tests  */
end_comment

begin_class
specifier|public
class|class
name|HcatTestUtils
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
name|HcatTestUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|FsPermission
name|perm007
init|=
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0007
argument_list|)
decl_stmt|;
comment|// -------rwx
specifier|public
specifier|static
name|FsPermission
name|perm070
init|=
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0070
argument_list|)
decl_stmt|;
comment|// ----rwx---
specifier|public
specifier|static
name|FsPermission
name|perm700
init|=
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0700
argument_list|)
decl_stmt|;
comment|// -rwx------
specifier|public
specifier|static
name|FsPermission
name|perm755
init|=
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0755
argument_list|)
decl_stmt|;
comment|// -rwxr-xr-x
specifier|public
specifier|static
name|FsPermission
name|perm777
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
comment|// -rwxrwxrwx
specifier|public
specifier|static
name|FsPermission
name|perm300
init|=
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0300
argument_list|)
decl_stmt|;
comment|// --wx------
specifier|public
specifier|static
name|FsPermission
name|perm500
init|=
name|FsPermission
operator|.
name|createImmutable
argument_list|(
operator|(
name|short
operator|)
literal|0500
argument_list|)
decl_stmt|;
comment|// -r-x------
specifier|public
specifier|static
name|FsPermission
name|perm555
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
comment|// -r-xr-xr-x
comment|/**    * Returns the database path.    */
specifier|public
specifier|static
name|Path
name|getDbPath
parameter_list|(
name|Hive
name|hive
parameter_list|,
name|Warehouse
name|wh
parameter_list|,
name|String
name|dbName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|HiveException
block|{
return|return
name|wh
operator|.
name|getDatabasePath
argument_list|(
name|hive
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Removes all databases and tables from the metastore    */
specifier|public
specifier|static
name|void
name|cleanupHMS
parameter_list|(
name|Hive
name|hive
parameter_list|,
name|Warehouse
name|wh
parameter_list|,
name|FsPermission
name|defaultPerm
parameter_list|)
throws|throws
name|HiveException
throws|,
name|MetaException
throws|,
name|NoSuchObjectException
block|{
for|for
control|(
name|String
name|dbName
range|:
name|hive
operator|.
name|getAllDatabases
argument_list|()
control|)
block|{
if|if
condition|(
name|dbName
operator|.
name|equals
argument_list|(
literal|"default"
argument_list|)
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|Path
name|path
init|=
name|getDbPath
argument_list|(
name|hive
argument_list|,
name|wh
argument_list|,
name|dbName
argument_list|)
decl_stmt|;
name|FileSystem
name|whFs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|hive
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|whFs
operator|.
name|setPermission
argument_list|(
name|path
argument_list|,
name|defaultPerm
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|//ignore
block|}
name|hive
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|//clean tables in default db
for|for
control|(
name|String
name|tablename
range|:
name|hive
operator|.
name|getAllTables
argument_list|(
literal|"default"
argument_list|)
control|)
block|{
name|hive
operator|.
name|dropTable
argument_list|(
literal|"default"
argument_list|,
name|tablename
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|createTestDataFile
parameter_list|(
name|String
name|filename
parameter_list|,
name|String
index|[]
name|lines
parameter_list|)
throws|throws
name|IOException
block|{
name|FileWriter
name|writer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|filename
argument_list|)
decl_stmt|;
name|file
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|writer
operator|=
operator|new
name|FileWriter
argument_list|(
name|file
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|line
range|:
name|lines
control|)
block|{
name|writer
operator|.
name|write
argument_list|(
name|line
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

