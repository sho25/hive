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
literal|""
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
block|,   }
end_enum

end_unit

