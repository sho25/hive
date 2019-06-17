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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|ddl
operator|.
name|table
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_comment
comment|/**  * Enumeration of alter table command types.  */
end_comment

begin_enum
specifier|public
enum|enum
name|AlterTableType
block|{
comment|// column
name|ADDCOLS
argument_list|(
literal|"add columns"
argument_list|)
block|,
name|REPLACE_COLUMNS
argument_list|(
literal|"replace columns"
argument_list|)
block|,
name|RENAME_COLUMN
argument_list|(
literal|"rename column"
argument_list|)
block|,
name|UPDATE_COLUMNS
argument_list|(
literal|"update columns"
argument_list|)
block|,
comment|// partition
name|ADDPARTITION
argument_list|(
literal|"add partition"
argument_list|)
block|,
name|DROPPARTITION
argument_list|(
literal|"drop partition"
argument_list|)
block|,
name|RENAMEPARTITION
argument_list|(
literal|"rename partition"
argument_list|)
block|,
comment|// Note: used in RenamePartitionDesc, not here.
name|ALTERPARTITION
argument_list|(
literal|"alter partition"
argument_list|)
block|,
comment|// Note: this is never used in AlterTableDesc.
comment|// constraint
name|ADD_CONSTRAINT
argument_list|(
literal|"add constraint"
argument_list|)
block|,
name|DROP_CONSTRAINT
argument_list|(
literal|"drop constraint"
argument_list|)
block|,
comment|// storage
name|SET_SERDE
argument_list|(
literal|"set serde"
argument_list|)
block|,
name|SET_SERDE_PROPS
argument_list|(
literal|"set serde props"
argument_list|)
block|,
name|SET_FILE_FORMAT
argument_list|(
literal|"add fileformat"
argument_list|)
block|,
name|CLUSTERED_BY
argument_list|(
literal|"clustered by"
argument_list|)
block|,
name|NOT_SORTED
argument_list|(
literal|"not sorted"
argument_list|)
block|,
name|NOT_CLUSTERED
argument_list|(
literal|"not clustered"
argument_list|)
block|,
name|ALTERLOCATION
argument_list|(
literal|"set location"
argument_list|)
block|,
name|SKEWED_BY
argument_list|(
literal|"skewed by"
argument_list|)
block|,
name|NOT_SKEWED
argument_list|(
literal|"not skewed"
argument_list|)
block|,
name|SET_SKEWED_LOCATION
argument_list|(
literal|"alter skew location"
argument_list|)
block|,
name|INTO_BUCKETS
argument_list|(
literal|"alter bucket number"
argument_list|)
block|,
comment|// misc
name|ADDPROPS
argument_list|(
literal|"set properties"
argument_list|)
block|,
name|DROPPROPS
argument_list|(
literal|"unset properties"
argument_list|)
block|,
name|TOUCH
argument_list|(
literal|"touch"
argument_list|)
block|,
name|RENAME
argument_list|(
literal|"rename"
argument_list|)
block|,
name|OWNER
argument_list|(
literal|"set owner"
argument_list|)
block|,
name|ARCHIVE
argument_list|(
literal|"archieve"
argument_list|)
block|,
name|UNARCHIVE
argument_list|(
literal|"unarchieve"
argument_list|)
block|,
name|COMPACT
argument_list|(
literal|"compact"
argument_list|)
block|,
name|TRUNCATE
argument_list|(
literal|"truncate"
argument_list|)
block|,
name|MERGEFILES
argument_list|(
literal|"merge files"
argument_list|)
block|,
name|UPDATESTATS
argument_list|(
literal|"update stats"
argument_list|)
block|;
comment|// Note: used in ColumnStatsUpdateWork, not here.
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
name|AlterTableType
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
specifier|static
specifier|final
name|List
argument_list|<
name|AlterTableType
argument_list|>
name|NON_NATIVE_TABLE_ALLOWED
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
name|ADDPROPS
argument_list|,
name|DROPPROPS
argument_list|,
name|ADDCOLS
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|AlterTableType
argument_list|>
name|SUPPORT_PARTIAL_PARTITION_SPEC
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
name|ADDCOLS
argument_list|,
name|REPLACE_COLUMNS
argument_list|,
name|RENAME_COLUMN
argument_list|,
name|ADDPROPS
argument_list|,
name|DROPPROPS
argument_list|,
name|SET_SERDE
argument_list|,
name|SET_SERDE_PROPS
argument_list|,
name|SET_FILE_FORMAT
argument_list|)
decl_stmt|;
block|}
end_enum

end_unit

