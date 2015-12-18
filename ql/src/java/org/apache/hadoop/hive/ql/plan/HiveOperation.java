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
name|ql
operator|.
name|plan
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
name|hive
operator|.
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|Privilege
import|;
end_import

begin_enum
specifier|public
enum|enum
name|HiveOperation
block|{
name|EXPLAIN
argument_list|(
literal|"EXPLAIN"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|LOAD
argument_list|(
literal|"LOAD"
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|)
block|,
name|EXPORT
argument_list|(
literal|"EXPORT"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|IMPORT
argument_list|(
literal|"IMPORT"
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|,
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|)
block|,
name|CREATEDATABASE
argument_list|(
literal|"CREATEDATABASE"
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|CREATE
block|}
argument_list|)
block|,
name|DROPDATABASE
argument_list|(
literal|"DROPDATABASE"
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|DROP
block|}
argument_list|)
block|,
name|SWITCHDATABASE
argument_list|(
literal|"SWITCHDATABASE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|LOCKDB
argument_list|(
literal|"LOCKDATABASE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|LOCK
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|UNLOCKDB
argument_list|(
literal|"UNLOCKDATABASE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|LOCK
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|DROPTABLE
argument_list|(
literal|"DROPTABLE"
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|DROP
block|}
argument_list|)
block|,
name|DESCTABLE
argument_list|(
literal|"DESCTABLE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|DESCFUNCTION
argument_list|(
literal|"DESCFUNCTION"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|MSCK
argument_list|(
literal|"MSCK"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_ADDCOLS
argument_list|(
literal|"ALTERTABLE_ADDCOLS"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_REPLACECOLS
argument_list|(
literal|"ALTERTABLE_REPLACECOLS"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_RENAMECOL
argument_list|(
literal|"ALTERTABLE_RENAMECOL"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_RENAMEPART
argument_list|(
literal|"ALTERTABLE_RENAMEPART"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|DROP
block|}
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|CREATE
block|}
argument_list|)
block|,
name|ALTERTABLE_UPDATEPARTSTATS
argument_list|(
literal|"ALTERTABLE_UPDATEPARTSTATS"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_UPDATETABLESTATS
argument_list|(
literal|"ALTERTABLE_UPDATETABLESTATS"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_RENAME
argument_list|(
literal|"ALTERTABLE_RENAME"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_DROPPARTS
argument_list|(
literal|"ALTERTABLE_DROPPARTS"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|DROP
block|}
argument_list|,
literal|null
argument_list|)
block|,
comment|// The location is input and table is output for alter-table add partitions
name|ALTERTABLE_ADDPARTS
argument_list|(
literal|"ALTERTABLE_ADDPARTS"
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|CREATE
block|}
argument_list|)
block|,
name|ALTERTABLE_TOUCH
argument_list|(
literal|"ALTERTABLE_TOUCH"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_ARCHIVE
argument_list|(
literal|"ALTERTABLE_ARCHIVE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_UNARCHIVE
argument_list|(
literal|"ALTERTABLE_UNARCHIVE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_PROPERTIES
argument_list|(
literal|"ALTERTABLE_PROPERTIES"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_SERIALIZER
argument_list|(
literal|"ALTERTABLE_SERIALIZER"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERPARTITION_SERIALIZER
argument_list|(
literal|"ALTERPARTITION_SERIALIZER"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_SERDEPROPERTIES
argument_list|(
literal|"ALTERTABLE_SERDEPROPERTIES"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERPARTITION_SERDEPROPERTIES
argument_list|(
literal|"ALTERPARTITION_SERDEPROPERTIES"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_CLUSTER_SORT
argument_list|(
literal|"ALTERTABLE_CLUSTER_SORT"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ANALYZE_TABLE
argument_list|(
literal|"ANALYZE_TABLE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|CACHE_METADATA
argument_list|(
literal|"CACHE_METADATA"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_BUCKETNUM
argument_list|(
literal|"ALTERTABLE_BUCKETNUM"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERPARTITION_BUCKETNUM
argument_list|(
literal|"ALTERPARTITION_BUCKETNUM"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|SHOWDATABASES
argument_list|(
literal|"SHOWDATABASES"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SHOW_DATABASE
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|SHOWTABLES
argument_list|(
literal|"SHOWTABLES"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOWCOLUMNS
argument_list|(
literal|"SHOWCOLUMNS"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOW_TABLESTATUS
argument_list|(
literal|"SHOW_TABLESTATUS"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOW_TBLPROPERTIES
argument_list|(
literal|"SHOW_TBLPROPERTIES"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOW_CREATEDATABASE
argument_list|(
literal|"SHOW_CREATEDATABASE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|SHOW_CREATETABLE
argument_list|(
literal|"SHOW_CREATETABLE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|SHOWFUNCTIONS
argument_list|(
literal|"SHOWFUNCTIONS"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOWINDEXES
argument_list|(
literal|"SHOWINDEXES"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOWPARTITIONS
argument_list|(
literal|"SHOWPARTITIONS"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOWLOCKS
argument_list|(
literal|"SHOWLOCKS"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOWCONF
argument_list|(
literal|"SHOWCONF"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|CREATEFUNCTION
argument_list|(
literal|"CREATEFUNCTION"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|DROPFUNCTION
argument_list|(
literal|"DROPFUNCTION"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|CREATEMACRO
argument_list|(
literal|"CREATEMACRO"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|DROPMACRO
argument_list|(
literal|"DROPMACRO"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|CREATEVIEW
argument_list|(
literal|"CREATEVIEW"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|CREATE
block|}
argument_list|)
block|,
name|DROPVIEW
argument_list|(
literal|"DROPVIEW"
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|DROP
block|}
argument_list|)
block|,
name|CREATEINDEX
argument_list|(
literal|"CREATEINDEX"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|DROPINDEX
argument_list|(
literal|"DROPINDEX"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERINDEX_REBUILD
argument_list|(
literal|"ALTERINDEX_REBUILD"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERVIEW_PROPERTIES
argument_list|(
literal|"ALTERVIEW_PROPERTIES"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|DROPVIEW_PROPERTIES
argument_list|(
literal|"DROPVIEW_PROPERTIES"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|LOCKTABLE
argument_list|(
literal|"LOCKTABLE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|LOCK
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|UNLOCKTABLE
argument_list|(
literal|"UNLOCKTABLE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|LOCK
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|CREATEROLE
argument_list|(
literal|"CREATEROLE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|DROPROLE
argument_list|(
literal|"DROPROLE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|GRANT_PRIVILEGE
argument_list|(
literal|"GRANT_PRIVILEGE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|REVOKE_PRIVILEGE
argument_list|(
literal|"REVOKE_PRIVILEGE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOW_GRANT
argument_list|(
literal|"SHOW_GRANT"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|GRANT_ROLE
argument_list|(
literal|"GRANT_ROLE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|REVOKE_ROLE
argument_list|(
literal|"REVOKE_ROLE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOW_ROLES
argument_list|(
literal|"SHOW_ROLES"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOW_ROLE_PRINCIPALS
argument_list|(
literal|"SHOW_ROLE_PRINCIPALS"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOW_ROLE_GRANT
argument_list|(
literal|"SHOW_ROLE_GRANT"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_FILEFORMAT
argument_list|(
literal|"ALTERTABLE_FILEFORMAT"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERPARTITION_FILEFORMAT
argument_list|(
literal|"ALTERPARTITION_FILEFORMAT"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_LOCATION
argument_list|(
literal|"ALTERTABLE_LOCATION"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERPARTITION_LOCATION
argument_list|(
literal|"ALTERPARTITION_LOCATION"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|CREATETABLE
argument_list|(
literal|"CREATETABLE"
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|CREATE
block|}
argument_list|)
block|,
name|TRUNCATETABLE
argument_list|(
literal|"TRUNCATETABLE"
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|DROP
block|}
argument_list|)
block|,
name|CREATETABLE_AS_SELECT
argument_list|(
literal|"CREATETABLE_AS_SELECT"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|CREATE
block|}
argument_list|)
block|,
name|QUERY
argument_list|(
literal|"QUERY"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|,
name|Privilege
operator|.
name|CREATE
block|}
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
block|,
name|ALTERINDEX_PROPS
argument_list|(
literal|"ALTERINDEX_PROPS"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERDATABASE
argument_list|(
literal|"ALTERDATABASE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERDATABASE_OWNER
argument_list|(
literal|"ALTERDATABASE_OWNER"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|DESCDATABASE
argument_list|(
literal|"DESCDATABASE"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_MERGEFILES
argument_list|(
literal|"ALTER_TABLE_MERGE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|)
block|,
name|ALTERPARTITION_MERGEFILES
argument_list|(
literal|"ALTER_PARTITION_MERGE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|)
block|,
name|ALTERTABLE_SKEWED
argument_list|(
literal|"ALTERTABLE_SKEWED"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTBLPART_SKEWED_LOCATION
argument_list|(
literal|"ALTERTBLPART_SKEWED_LOCATION"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_PARTCOLTYPE
argument_list|(
literal|"ALTERTABLE_PARTCOLTYPE"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|)
block|,
name|ALTERTABLE_EXCHANGEPARTITION
argument_list|(
literal|"ALTERTABLE_EXCHANGEPARTITION"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERVIEW_RENAME
argument_list|(
literal|"ALTERVIEW_RENAME"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERVIEW_AS
argument_list|(
literal|"ALTERVIEW_AS"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|,
literal|null
argument_list|)
block|,
name|ALTERTABLE_COMPACT
argument_list|(
literal|"ALTERTABLE_COMPACT"
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|SELECT
block|}
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_DATA
block|}
argument_list|)
block|,
name|SHOW_COMPACTIONS
argument_list|(
literal|"SHOW COMPACTIONS"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|SHOW_TRANSACTIONS
argument_list|(
literal|"SHOW TRANSACTIONS"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|,
name|START_TRANSACTION
argument_list|(
literal|"START TRANSACTION"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
block|,
name|COMMIT
argument_list|(
literal|"COMMIT"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
block|,
name|ROLLBACK
argument_list|(
literal|"ROLLBACK"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
block|,
name|SET_AUTOCOMMIT
argument_list|(
literal|"SET AUTOCOMMIT"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
block|;   ;
specifier|private
name|String
name|operationName
decl_stmt|;
specifier|private
name|Privilege
index|[]
name|inputRequiredPrivileges
decl_stmt|;
specifier|private
name|Privilege
index|[]
name|outputRequiredPrivileges
decl_stmt|;
comment|/**    * Only a small set of operations is allowed inside an open transactions, e.g. DML    */
specifier|private
specifier|final
name|boolean
name|allowedInTransaction
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|requiresOpenTransaction
decl_stmt|;
specifier|public
name|Privilege
index|[]
name|getInputRequiredPrivileges
parameter_list|()
block|{
return|return
name|inputRequiredPrivileges
return|;
block|}
specifier|public
name|Privilege
index|[]
name|getOutputRequiredPrivileges
parameter_list|()
block|{
return|return
name|outputRequiredPrivileges
return|;
block|}
specifier|public
name|String
name|getOperationName
parameter_list|()
block|{
return|return
name|operationName
return|;
block|}
specifier|public
name|boolean
name|isAllowedInTransaction
parameter_list|()
block|{
return|return
name|allowedInTransaction
return|;
block|}
specifier|public
name|boolean
name|isRequiresOpenTransaction
parameter_list|()
block|{
return|return
name|requiresOpenTransaction
return|;
block|}
specifier|private
name|HiveOperation
parameter_list|(
name|String
name|operationName
parameter_list|,
name|Privilege
index|[]
name|inputRequiredPrivileges
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPrivileges
parameter_list|)
block|{
name|this
argument_list|(
name|operationName
argument_list|,
name|inputRequiredPrivileges
argument_list|,
name|outputRequiredPrivileges
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HiveOperation
parameter_list|(
name|String
name|operationName
parameter_list|,
name|Privilege
index|[]
name|inputRequiredPrivileges
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPrivileges
parameter_list|,
name|boolean
name|allowedInTransaction
parameter_list|,
name|boolean
name|requiresOpenTransaction
parameter_list|)
block|{
name|this
operator|.
name|operationName
operator|=
name|operationName
expr_stmt|;
name|this
operator|.
name|inputRequiredPrivileges
operator|=
name|inputRequiredPrivileges
expr_stmt|;
name|this
operator|.
name|outputRequiredPrivileges
operator|=
name|outputRequiredPrivileges
expr_stmt|;
name|this
operator|.
name|requiresOpenTransaction
operator|=
name|requiresOpenTransaction
expr_stmt|;
if|if
condition|(
name|requiresOpenTransaction
condition|)
block|{
name|allowedInTransaction
operator|=
literal|true
expr_stmt|;
block|}
name|this
operator|.
name|allowedInTransaction
operator|=
name|allowedInTransaction
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|PrivilegeAgreement
block|{
specifier|private
name|Privilege
index|[]
name|inputUserLevelRequiredPriv
decl_stmt|;
specifier|private
name|Privilege
index|[]
name|inputDBLevelRequiredPriv
decl_stmt|;
specifier|private
name|Privilege
index|[]
name|inputTableLevelRequiredPriv
decl_stmt|;
specifier|private
name|Privilege
index|[]
name|inputColumnLevelRequiredPriv
decl_stmt|;
specifier|private
name|Privilege
index|[]
name|outputUserLevelRequiredPriv
decl_stmt|;
specifier|private
name|Privilege
index|[]
name|outputDBLevelRequiredPriv
decl_stmt|;
specifier|private
name|Privilege
index|[]
name|outputTableLevelRequiredPriv
decl_stmt|;
specifier|private
name|Privilege
index|[]
name|outputColumnLevelRequiredPriv
decl_stmt|;
specifier|public
name|PrivilegeAgreement
name|putUserLevelRequiredPriv
parameter_list|(
name|Privilege
index|[]
name|inputUserLevelRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputUserLevelRequiredPriv
parameter_list|)
block|{
name|this
operator|.
name|inputUserLevelRequiredPriv
operator|=
name|inputUserLevelRequiredPriv
expr_stmt|;
name|this
operator|.
name|outputUserLevelRequiredPriv
operator|=
name|outputUserLevelRequiredPriv
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|PrivilegeAgreement
name|putDBLevelRequiredPriv
parameter_list|(
name|Privilege
index|[]
name|inputDBLevelRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputDBLevelRequiredPriv
parameter_list|)
block|{
name|this
operator|.
name|inputDBLevelRequiredPriv
operator|=
name|inputDBLevelRequiredPriv
expr_stmt|;
name|this
operator|.
name|outputDBLevelRequiredPriv
operator|=
name|outputDBLevelRequiredPriv
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|PrivilegeAgreement
name|putTableLevelRequiredPriv
parameter_list|(
name|Privilege
index|[]
name|inputTableLevelRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputTableLevelRequiredPriv
parameter_list|)
block|{
name|this
operator|.
name|inputTableLevelRequiredPriv
operator|=
name|inputTableLevelRequiredPriv
expr_stmt|;
name|this
operator|.
name|outputTableLevelRequiredPriv
operator|=
name|outputTableLevelRequiredPriv
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|PrivilegeAgreement
name|putColumnLevelRequiredPriv
parameter_list|(
name|Privilege
index|[]
name|inputColumnLevelPriv
parameter_list|,
name|Privilege
index|[]
name|outputColumnLevelPriv
parameter_list|)
block|{
name|this
operator|.
name|inputColumnLevelRequiredPriv
operator|=
name|inputColumnLevelPriv
expr_stmt|;
name|this
operator|.
name|outputColumnLevelRequiredPriv
operator|=
name|outputColumnLevelPriv
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Privilege
index|[]
name|getInputUserLevelRequiredPriv
parameter_list|()
block|{
return|return
name|inputUserLevelRequiredPriv
return|;
block|}
specifier|public
name|Privilege
index|[]
name|getInputDBLevelRequiredPriv
parameter_list|()
block|{
return|return
name|inputDBLevelRequiredPriv
return|;
block|}
specifier|public
name|Privilege
index|[]
name|getInputTableLevelRequiredPriv
parameter_list|()
block|{
return|return
name|inputTableLevelRequiredPriv
return|;
block|}
specifier|public
name|Privilege
index|[]
name|getInputColumnLevelRequiredPriv
parameter_list|()
block|{
return|return
name|inputColumnLevelRequiredPriv
return|;
block|}
specifier|public
name|Privilege
index|[]
name|getOutputUserLevelRequiredPriv
parameter_list|()
block|{
return|return
name|outputUserLevelRequiredPriv
return|;
block|}
specifier|public
name|Privilege
index|[]
name|getOutputDBLevelRequiredPriv
parameter_list|()
block|{
return|return
name|outputDBLevelRequiredPriv
return|;
block|}
specifier|public
name|Privilege
index|[]
name|getOutputTableLevelRequiredPriv
parameter_list|()
block|{
return|return
name|outputTableLevelRequiredPriv
return|;
block|}
specifier|public
name|Privilege
index|[]
name|getOutputColumnLevelRequiredPriv
parameter_list|()
block|{
return|return
name|outputColumnLevelRequiredPriv
return|;
block|}
block|}
block|}
end_enum

end_unit

