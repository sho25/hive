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
name|cli
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
name|conf
operator|.
name|Configuration
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
name|ql
operator|.
name|CommandNeedRetryException
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
name|Driver
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
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
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_class
specifier|public
class|class
name|HCatDriver
extends|extends
name|Driver
block|{
annotation|@
name|Override
specifier|public
name|CommandProcessorResponse
name|run
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|CommandProcessorResponse
name|cpr
init|=
literal|null
decl_stmt|;
try|try
block|{
name|cpr
operator|=
name|super
operator|.
name|run
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandNeedRetryException
name|e
parameter_list|)
block|{
return|return
operator|new
name|CommandProcessorResponse
argument_list|(
operator|-
literal|1
argument_list|,
name|e
operator|.
name|toString
argument_list|()
argument_list|,
literal|""
argument_list|)
return|;
block|}
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Only attempt to do this, if cmd was successful.
name|int
name|rc
init|=
name|setFSPermsNGrp
argument_list|(
name|ss
argument_list|)
decl_stmt|;
name|cpr
operator|=
operator|new
name|CommandProcessorResponse
argument_list|(
name|rc
argument_list|)
expr_stmt|;
block|}
comment|// reset conf vars
name|ss
operator|.
name|getConf
argument_list|()
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DB_NAME
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|ss
operator|.
name|getConf
argument_list|()
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_TBL_NAME
argument_list|,
literal|""
argument_list|)
expr_stmt|;
return|return
name|cpr
return|;
block|}
specifier|private
name|int
name|setFSPermsNGrp
parameter_list|(
name|SessionState
name|ss
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|ss
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|String
name|tblName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_TBL_NAME
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|tblName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tblName
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"import.destination.table"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"import.destination.table"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
name|String
name|dbName
init|=
name|conf
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DB_NAME
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|String
name|grp
init|=
name|conf
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_GROUP
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|String
name|permsStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PERMS
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|tblName
operator|.
name|isEmpty
argument_list|()
operator|&&
name|dbName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// it wasn't create db/table
return|return
literal|0
return|;
block|}
if|if
condition|(
literal|null
operator|==
name|grp
operator|&&
literal|null
operator|==
name|permsStr
condition|)
block|{
comment|// there were no grp and perms to begin with.
return|return
literal|0
return|;
block|}
name|FsPermission
name|perms
init|=
name|FsPermission
operator|.
name|valueOf
argument_list|(
name|permsStr
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tblName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Hive
name|db
init|=
literal|null
decl_stmt|;
try|try
block|{
name|db
operator|=
name|Hive
operator|.
name|get
argument_list|()
expr_stmt|;
name|Table
name|tbl
init|=
name|db
operator|.
name|getTable
argument_list|(
name|tblName
argument_list|)
decl_stmt|;
name|Path
name|tblPath
init|=
name|tbl
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|tblPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|perms
condition|)
block|{
name|fs
operator|.
name|setPermission
argument_list|(
name|tblPath
argument_list|,
name|perms
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
literal|null
operator|!=
name|grp
condition|)
block|{
name|fs
operator|.
name|setOwner
argument_list|(
name|tblPath
argument_list|,
literal|null
argument_list|,
name|grp
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|ss
operator|.
name|err
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Failed to set permissions/groups on TABLE:<%s> %s"
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
comment|// We need to drop the table.
if|if
condition|(
literal|null
operator|!=
name|db
condition|)
block|{
name|db
operator|.
name|dropTable
argument_list|(
name|tblName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|he
parameter_list|)
block|{
name|ss
operator|.
name|err
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Failed to drop TABLE<%s> after failing to set permissions/groups on it. %s"
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|1
return|;
block|}
block|}
else|else
block|{
comment|// looks like a db operation
if|if
condition|(
name|dbName
operator|.
name|isEmpty
argument_list|()
operator|||
name|dbName
operator|.
name|equals
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|)
condition|)
block|{
comment|// We dont set perms or groups for default dir.
return|return
literal|0
return|;
block|}
else|else
block|{
try|try
block|{
name|Hive
name|db
init|=
name|Hive
operator|.
name|get
argument_list|()
decl_stmt|;
name|Path
name|dbPath
init|=
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
operator|.
name|getDatabasePath
argument_list|(
name|db
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dbPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|perms
operator|!=
literal|null
condition|)
block|{
name|fs
operator|.
name|setPermission
argument_list|(
name|dbPath
argument_list|,
name|perms
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
literal|null
operator|!=
name|grp
condition|)
block|{
name|fs
operator|.
name|setOwner
argument_list|(
name|dbPath
argument_list|,
literal|null
argument_list|,
name|grp
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|ss
operator|.
name|err
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Failed to set permissions and/or group on DB:<%s> %s"
argument_list|,
name|dbName
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|ss
operator|.
name|err
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Failed to drop DB<%s> after failing to set permissions/group on it. %s"
argument_list|,
name|dbName
argument_list|,
name|e1
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|1
return|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

