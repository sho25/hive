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
name|security
operator|.
name|authorization
operator|.
name|plugin
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
operator|.
name|LimitedPrivate
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
operator|.
name|Evolving
import|;
end_import

begin_comment
comment|/**  * List of hive operations types.  */
end_comment

begin_enum
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|"Apache Argus (incubating)"
block|}
argument_list|)
annotation|@
name|Evolving
specifier|public
enum|enum
name|HiveOperationType
block|{
name|EXPLAIN
block|,
name|LOAD
block|,
name|EXPORT
block|,
name|IMPORT
block|,
name|CREATEDATABASE
block|,
name|DROPDATABASE
block|,
name|SWITCHDATABASE
block|,
name|LOCKDB
block|,
name|UNLOCKDB
block|,
name|DROPTABLE
block|,
name|DESCTABLE
block|,
name|DESCFUNCTION
block|,
name|MSCK
block|,
name|ALTERTABLE_ADDCOLS
block|,
name|ALTERTABLE_REPLACECOLS
block|,
name|ALTERTABLE_RENAMECOL
block|,
name|ALTERTABLE_RENAMEPART
block|,
name|ALTERTABLE_RENAME
block|,
name|ALTERTABLE_DROPPARTS
block|,
name|ALTERTABLE_ADDPARTS
block|,
name|ALTERTABLE_TOUCH
block|,
name|ALTERTABLE_ARCHIVE
block|,
name|ALTERTABLE_UNARCHIVE
block|,
name|ALTERTABLE_PROPERTIES
block|,
name|ALTERTABLE_SERIALIZER
block|,
name|ALTERTABLE_PARTCOLTYPE
block|,
name|ALTERPARTITION_SERIALIZER
block|,
name|ALTERTABLE_SERDEPROPERTIES
block|,
name|ALTERPARTITION_SERDEPROPERTIES
block|,
name|ALTERTABLE_CLUSTER_SORT
block|,
name|ANALYZE_TABLE
block|,
name|ALTERTABLE_BUCKETNUM
block|,
name|ALTERPARTITION_BUCKETNUM
block|,
name|ALTERTABLE_UPDATETABLESTATS
block|,
name|ALTERTABLE_UPDATEPARTSTATS
block|,
name|SHOWDATABASES
block|,
name|SHOWTABLES
block|,
name|SHOWCOLUMNS
block|,
name|SHOW_TABLESTATUS
block|,
name|SHOW_TBLPROPERTIES
block|,
name|SHOW_CREATEDATABASE
block|,
name|SHOW_CREATETABLE
block|,
name|SHOWFUNCTIONS
block|,
name|SHOWINDEXES
block|,
name|SHOWPARTITIONS
block|,
name|SHOWLOCKS
block|,
name|SHOWCONF
block|,
name|CREATEFUNCTION
block|,
name|DROPFUNCTION
block|,
name|CREATEMACRO
block|,
name|DROPMACRO
block|,
name|CREATEVIEW
block|,
name|DROPVIEW
block|,
name|CREATEINDEX
block|,
name|DROPINDEX
block|,
name|ALTERINDEX_REBUILD
block|,
name|ALTERVIEW_PROPERTIES
block|,
name|DROPVIEW_PROPERTIES
block|,
name|LOCKTABLE
block|,
name|UNLOCKTABLE
block|,
name|CREATEROLE
block|,
name|DROPROLE
block|,
name|GRANT_PRIVILEGE
block|,
name|REVOKE_PRIVILEGE
block|,
name|SHOW_GRANT
block|,
name|GRANT_ROLE
block|,
name|REVOKE_ROLE
block|,
name|SHOW_ROLES
block|,
name|SHOW_ROLE_GRANT
block|,
name|SHOW_ROLE_PRINCIPALS
block|,
name|ALTERTABLE_PROTECTMODE
block|,
name|ALTERPARTITION_PROTECTMODE
block|,
name|ALTERTABLE_FILEFORMAT
block|,
name|ALTERPARTITION_FILEFORMAT
block|,
name|ALTERTABLE_LOCATION
block|,
name|ALTERPARTITION_LOCATION
block|,
name|CREATETABLE
block|,
name|TRUNCATETABLE
block|,
name|CREATETABLE_AS_SELECT
block|,
name|QUERY
block|,
name|ALTERINDEX_PROPS
block|,
name|ALTERDATABASE
block|,
name|ALTERDATABASE_OWNER
block|,
name|DESCDATABASE
block|,
name|ALTERTABLE_MERGEFILES
block|,
name|ALTERPARTITION_MERGEFILES
block|,
name|ALTERTABLE_SKEWED
block|,
name|ALTERTBLPART_SKEWED_LOCATION
block|,
name|ALTERVIEW_RENAME
block|,
name|ALTERVIEW_AS
block|,
name|ALTERTABLE_COMPACT
block|,
name|SHOW_COMPACTIONS
block|,
name|SHOW_TRANSACTIONS
block|,
comment|// ==== Hive command operation types starts here ==== //
name|SET
block|,
name|RESET
block|,
name|DFS
block|,
name|ADD
block|,
name|DELETE
block|,
name|COMPILE
block|,
name|START_TRANSACTION
block|,
name|COMMIT
block|,
name|ROLLBACK
block|,
name|SET_AUTOCOMMIT
block|,
name|ALTERTABLE_EXCHANGEPARTITION
block|,
comment|// ==== Hive command operations ends here ==== //
comment|// ==== HiveServer2 metadata api types start here ==== //
comment|// these corresponds to various java.sql.DatabaseMetaData calls.
name|GET_CATALOGS
block|,
comment|// DatabaseMetaData.getCatalogs()  catalogs are actually not supported in
comment|// hive, so this is a no-op
name|GET_COLUMNS
block|,
comment|// getColumns(String catalog, String schemaPattern, String
comment|// tableNamePattern, String columnNamePattern)
name|GET_FUNCTIONS
block|,
comment|// getFunctions(String catalog, String schemaPattern, String functionNamePattern)
name|GET_SCHEMAS
block|,
comment|// getSchemas()
name|GET_TABLES
block|,
comment|// getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
name|GET_TABLETYPES
block|,
comment|// getTableTypes()
name|GET_TYPEINFO
comment|// getTypeInfo()
comment|// ==== HiveServer2 metadata api types ends here ==== //
block|}
end_enum

end_unit

