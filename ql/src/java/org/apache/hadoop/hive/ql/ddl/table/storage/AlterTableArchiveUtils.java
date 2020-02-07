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
operator|.
name|storage
package|;
end_package

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
name|hive_metastoreConstants
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
name|exec
operator|.
name|ArchiveUtils
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
name|Partition
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

begin_comment
comment|/**  * Utilities for archiving.  */
end_comment

begin_class
specifier|final
class|class
name|AlterTableArchiveUtils
block|{
specifier|private
name|AlterTableArchiveUtils
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"ArchiveUtils should not be instantiated"
argument_list|)
throw|;
block|}
specifier|static
specifier|final
name|String
name|ARCHIVE_NAME
init|=
literal|"data.har"
decl_stmt|;
comment|/**    * Returns original partition of archived partition, null for unarchived one.    */
specifier|static
name|String
name|getOriginalLocation
parameter_list|(
name|Partition
name|partition
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|partition
operator|.
name|getParameters
argument_list|()
decl_stmt|;
return|return
name|params
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|ORIGINAL_LOCATION
argument_list|)
return|;
block|}
comment|/**    * Sets original location of partition which is to be archived.    */
specifier|static
name|void
name|setOriginalLocation
parameter_list|(
name|Partition
name|partition
parameter_list|,
name|String
name|loc
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|partition
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|loc
operator|==
literal|null
condition|)
block|{
name|params
operator|.
name|remove
argument_list|(
name|hive_metastoreConstants
operator|.
name|ORIGINAL_LOCATION
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|params
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|ORIGINAL_LOCATION
argument_list|,
name|loc
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Checks in partition is in custom (not-standard) location.    * @param table - table in which partition is    * @param partition - partition    * @return true if partition location is custom, false if it is standard    */
specifier|static
name|boolean
name|partitionInCustomLocation
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|subdir
init|=
literal|null
decl_stmt|;
try|try
block|{
name|subdir
operator|=
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|table
operator|.
name|getPartCols
argument_list|()
argument_list|,
name|partition
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unable to get partition's directory"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|Path
name|tableDir
init|=
name|table
operator|.
name|getDataLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableDir
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Table has no location set"
argument_list|)
throw|;
block|}
name|String
name|standardLocation
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|subdir
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|ArchiveUtils
operator|.
name|isArchived
argument_list|(
name|partition
argument_list|)
condition|)
block|{
return|return
operator|!
name|getOriginalLocation
argument_list|(
name|partition
argument_list|)
operator|.
name|equals
argument_list|(
name|standardLocation
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|!
name|partition
operator|.
name|getLocation
argument_list|()
operator|.
name|equals
argument_list|(
name|standardLocation
argument_list|)
return|;
block|}
block|}
specifier|static
name|Path
name|getInterMediateDir
parameter_list|(
name|Path
name|dir
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|suffixConfig
parameter_list|)
block|{
name|String
name|intermediateDirSuffix
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|suffixConfig
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|dir
operator|.
name|getParent
argument_list|()
argument_list|,
name|dir
operator|.
name|getName
argument_list|()
operator|+
name|intermediateDirSuffix
argument_list|)
return|;
block|}
specifier|static
name|void
name|deleteDir
parameter_list|(
name|Path
name|dir
parameter_list|,
name|boolean
name|shouldEnableCm
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|Warehouse
name|wh
init|=
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|wh
operator|.
name|deleteDir
argument_list|(
name|dir
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|shouldEnableCm
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Sets archiving flag locally; it has to be pushed into metastore.    * @param partition partition to set flag    * @param state desired state of IS_ARCHIVED flag    * @param level desired level for state == true, anything for false    */
specifier|static
name|void
name|setIsArchived
parameter_list|(
name|Partition
name|partition
parameter_list|,
name|boolean
name|state
parameter_list|,
name|int
name|level
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|partition
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|state
condition|)
block|{
name|params
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|IS_ARCHIVED
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|params
operator|.
name|put
argument_list|(
name|ArchiveUtils
operator|.
name|ARCHIVING_LEVEL
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|level
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|params
operator|.
name|remove
argument_list|(
name|hive_metastoreConstants
operator|.
name|IS_ARCHIVED
argument_list|)
expr_stmt|;
name|params
operator|.
name|remove
argument_list|(
name|ArchiveUtils
operator|.
name|ARCHIVING_LEVEL
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

