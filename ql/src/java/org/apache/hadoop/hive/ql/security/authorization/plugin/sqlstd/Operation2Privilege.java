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
operator|.
name|sqlstd
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveOperationType
import|;
end_import

begin_comment
comment|/**  * Mapping of operation to its required input and output privileges  */
end_comment

begin_class
specifier|public
class|class
name|Operation2Privilege
block|{
specifier|private
specifier|static
class|class
name|InOutPrivs
block|{
specifier|private
specifier|final
name|SQLPrivTypeGrant
index|[]
name|inputPrivs
decl_stmt|;
specifier|private
specifier|final
name|SQLPrivTypeGrant
index|[]
name|outputPrivs
decl_stmt|;
name|InOutPrivs
parameter_list|(
name|SQLPrivTypeGrant
index|[]
name|inputPrivs
parameter_list|,
name|SQLPrivTypeGrant
index|[]
name|outputPrivs
parameter_list|)
block|{
name|this
operator|.
name|inputPrivs
operator|=
name|inputPrivs
expr_stmt|;
name|this
operator|.
name|outputPrivs
operator|=
name|outputPrivs
expr_stmt|;
block|}
specifier|private
name|SQLPrivTypeGrant
index|[]
name|getInputPrivs
parameter_list|()
block|{
return|return
name|inputPrivs
return|;
block|}
specifier|private
name|SQLPrivTypeGrant
index|[]
name|getOutputPrivs
parameter_list|()
block|{
return|return
name|outputPrivs
return|;
block|}
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|HiveOperationType
argument_list|,
name|InOutPrivs
argument_list|>
name|op2Priv
decl_stmt|;
specifier|private
specifier|static
name|SQLPrivTypeGrant
index|[]
name|OWNER_PRIV_AR
init|=
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|OWNER_PRIV
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|SQLPrivTypeGrant
index|[]
name|SEL_NOGRANT_AR
init|=
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|SELECT_NOGRANT
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|SQLPrivTypeGrant
index|[]
name|SEL_GRANT_AR
init|=
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|SELECT_WGRANT
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|SQLPrivTypeGrant
index|[]
name|ADMIN_PRIV_AR
init|=
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|ADMIN_PRIV
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|SQLPrivTypeGrant
index|[]
name|INS_NOGRANT_AR
init|=
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|INSERT_NOGRANT
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|SQLPrivTypeGrant
index|[]
name|DEL_NOGRANT_AR
init|=
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|DELETE_NOGRANT
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|SQLPrivTypeGrant
index|[]
name|OWNER_INS_SEL_DEL_NOGRANT_AR
init|=
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|OWNER_PRIV
argument_list|,
name|SQLPrivTypeGrant
operator|.
name|INSERT_NOGRANT
argument_list|,
name|SQLPrivTypeGrant
operator|.
name|DELETE_NOGRANT
argument_list|,
name|SQLPrivTypeGrant
operator|.
name|SELECT_NOGRANT
argument_list|)
decl_stmt|;
static|static
block|{
name|op2Priv
operator|=
operator|new
name|HashMap
argument_list|<
name|HiveOperationType
argument_list|,
name|InOutPrivs
argument_list|>
argument_list|()
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|EXPLAIN
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_NOGRANT_AR
argument_list|,
name|SEL_NOGRANT_AR
argument_list|)
argument_list|)
expr_stmt|;
comment|//??
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|CREATEDATABASE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|ADMIN_PRIV_AR
argument_list|,
name|OWNER_INS_SEL_DEL_NOGRANT_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DROPDATABASE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
comment|// this should be database usage privilege once it is supported
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SWITCHDATABASE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// lock operations not controlled for now
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|LOCKDB
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|UNLOCKDB
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DROPTABLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DESCTABLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_NOGRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DESCFUNCTION
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// meta store check command - require admin priv
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|MSCK
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|ADMIN_PRIV_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|//alter table commands require table ownership
comment|// There should not be output object, but just in case the table is incorrectly added
comment|// to output instead of input, adding owner requirement on output will catch that as well
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_ADDCOLS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_REPLACECOLS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_RENAMECOL
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_RENAMEPART
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_RENAME
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_TOUCH
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_ARCHIVE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_UNARCHIVE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_PROPERTIES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_SERIALIZER
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_PARTCOLTYPE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERPARTITION_SERIALIZER
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_SERDEPROPERTIES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERPARTITION_SERDEPROPERTIES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_CLUSTER_SORT
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_BUCKETNUM
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERPARTITION_BUCKETNUM
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_PROTECTMODE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERPARTITION_PROTECTMODE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_FILEFORMAT
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERPARTITION_FILEFORMAT
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_LOCATION
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_INS_SEL_DEL_NOGRANT_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERPARTITION_LOCATION
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_INS_SEL_DEL_NOGRANT_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_MERGEFILES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERPARTITION_MERGEFILES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_SKEWED
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTBLPART_SKEWED_LOCATION
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_INS_SEL_DEL_NOGRANT_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|TRUNCATETABLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
comment|//table ownership for create/drop/alter index
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|CREATEINDEX
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_INS_SEL_DEL_NOGRANT_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DROPINDEX
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERINDEX_REBUILD
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERINDEX_PROPS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
comment|// require view ownership for alter/drop view
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERVIEW_PROPERTIES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DROPVIEW_PROPERTIES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERVIEW_RENAME
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DROPVIEW
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_PRIV_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ANALYZE_TABLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|SELECT_NOGRANT
argument_list|,
name|SQLPrivTypeGrant
operator|.
name|INSERT_NOGRANT
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOWDATABASES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOWTABLES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// operations that require insert/delete privileges
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_DROPPARTS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|DEL_NOGRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// in alter-table-add-partition, the table is output, and location is input
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERTABLE_ADDPARTS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_INS_SEL_DEL_NOGRANT_AR
argument_list|,
name|INS_NOGRANT_AR
argument_list|)
argument_list|)
expr_stmt|;
comment|// select with grant for exporting contents
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|EXPORT
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_GRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|IMPORT
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|INS_NOGRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// operations require select priv
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOWCOLUMNS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_NOGRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOW_TABLESTATUS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_NOGRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOW_TBLPROPERTIES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_NOGRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|CREATETABLE_AS_SELECT
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_NOGRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// QUERY,LOAD op can contain an insert& ovewrite, so require insert+delete privileges on output
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|QUERY
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_NOGRANT_AR
argument_list|,
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|INSERT_NOGRANT
argument_list|,
name|SQLPrivTypeGrant
operator|.
name|DELETE_NOGRANT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|LOAD
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_INS_SEL_DEL_NOGRANT_AR
argument_list|,
name|arr
argument_list|(
name|SQLPrivTypeGrant
operator|.
name|INSERT_NOGRANT
argument_list|,
name|SQLPrivTypeGrant
operator|.
name|DELETE_NOGRANT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// show create table is more sensitive information, includes table properties etc
comment|// for now require select WITH GRANT
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOW_CREATETABLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_GRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// for now allow only create-view with 'select with grant'
comment|// the owner will also have select with grant privileges on new view
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|CREATEVIEW
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|SEL_GRANT_AR
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOWFUNCTIONS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOWINDEXES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOWPARTITIONS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOWLOCKS
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|CREATEFUNCTION
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DROPFUNCTION
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|CREATEMACRO
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DROPMACRO
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|LOCKTABLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|UNLOCKTABLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// require db ownership, if there is a file require SELECT , INSERT, and DELETE
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|CREATETABLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
name|OWNER_INS_SEL_DEL_NOGRANT_AR
argument_list|,
name|OWNER_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERDATABASE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
name|ADMIN_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|ALTERDATABASE_OWNER
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
name|ADMIN_PRIV_AR
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DESCDATABASE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// The following actions are authorized through SQLStdHiveAccessController,
comment|// and it is not using this privilege mapping, but it might make sense to move it here
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|CREATEROLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|DROPROLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|GRANT_PRIVILEGE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|REVOKE_PRIVILEGE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOW_GRANT
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|GRANT_ROLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|REVOKE_ROLE
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOW_ROLES
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|op2Priv
operator|.
name|put
argument_list|(
name|HiveOperationType
operator|.
name|SHOW_ROLE_GRANT
argument_list|,
operator|new
name|InOutPrivs
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Convenience method so that creation of this array in InOutPrivs constructor    * is not too verbose    *    * @param grantList    * @return grantList    */
specifier|private
specifier|static
name|SQLPrivTypeGrant
index|[]
name|arr
parameter_list|(
name|SQLPrivTypeGrant
modifier|...
name|grantList
parameter_list|)
block|{
return|return
name|grantList
return|;
block|}
specifier|public
specifier|static
name|SQLPrivTypeGrant
index|[]
name|getInputPrivs
parameter_list|(
name|HiveOperationType
name|opType
parameter_list|)
block|{
return|return
name|op2Priv
operator|.
name|get
argument_list|(
name|opType
argument_list|)
operator|.
name|getInputPrivs
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|SQLPrivTypeGrant
index|[]
name|getOutputPrivs
parameter_list|(
name|HiveOperationType
name|opType
parameter_list|)
block|{
return|return
name|op2Priv
operator|.
name|get
argument_list|(
name|opType
argument_list|)
operator|.
name|getOutputPrivs
argument_list|()
return|;
block|}
comment|// for unit tests
specifier|public
specifier|static
name|Set
argument_list|<
name|HiveOperationType
argument_list|>
name|getOperationTypes
parameter_list|()
block|{
return|return
name|op2Priv
operator|.
name|keySet
argument_list|()
return|;
block|}
block|}
end_class

end_unit

