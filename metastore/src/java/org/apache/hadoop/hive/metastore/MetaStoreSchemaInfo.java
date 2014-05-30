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
name|java
operator|.
name|io
operator|.
name|BufferedReader
import|;
end_import

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
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileReader
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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveVersionInfo
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
name|ImmutableMap
import|;
end_import

begin_class
specifier|public
class|class
name|MetaStoreSchemaInfo
block|{
specifier|private
specifier|static
name|String
name|SQL_FILE_EXTENSION
init|=
literal|".sql"
decl_stmt|;
specifier|private
specifier|static
name|String
name|UPGRADE_FILE_PREFIX
init|=
literal|"upgrade-"
decl_stmt|;
specifier|private
specifier|static
name|String
name|INIT_FILE_PREFIX
init|=
literal|"hive-schema-"
decl_stmt|;
specifier|private
specifier|static
name|String
name|VERSION_UPGRADE_LIST
init|=
literal|"upgrade.order"
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbType
decl_stmt|;
specifier|private
specifier|final
name|String
name|hiveSchemaVersions
index|[]
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|final
name|String
name|hiveHome
decl_stmt|;
comment|// Minor version upgrades often don't change schema. So they are equivalent to a version
comment|// that has a corresponding schema. eg "0.13.1" is equivalent to "0.13.0"
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|EQUIVALENT_VERSIONS
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"0.13.1"
argument_list|,
literal|"0.13.0"
argument_list|)
decl_stmt|;
specifier|public
name|MetaStoreSchemaInfo
parameter_list|(
name|String
name|hiveHome
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|dbType
parameter_list|)
throws|throws
name|HiveMetaException
block|{
name|this
operator|.
name|hiveHome
operator|=
name|hiveHome
expr_stmt|;
name|this
operator|.
name|dbType
operator|=
name|dbType
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
comment|// load upgrade order for the given dbType
name|List
argument_list|<
name|String
argument_list|>
name|upgradeOrderList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|upgradeListFile
init|=
name|getMetaStoreScriptDir
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
name|VERSION_UPGRADE_LIST
operator|+
literal|"."
operator|+
name|dbType
decl_stmt|;
try|try
block|{
name|BufferedReader
name|bfReader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|FileReader
argument_list|(
name|upgradeListFile
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|currSchemaVersion
decl_stmt|;
while|while
condition|(
operator|(
name|currSchemaVersion
operator|=
name|bfReader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|upgradeOrderList
operator|.
name|add
argument_list|(
name|currSchemaVersion
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"File "
operator|+
name|upgradeListFile
operator|+
literal|"not found "
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Error reading "
operator|+
name|upgradeListFile
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|hiveSchemaVersions
operator|=
name|upgradeOrderList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/***    * Get the list of sql scripts required to upgrade from the give version to current    * @param fromVersion    * @return    * @throws HiveMetaException    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getUpgradeScripts
parameter_list|(
name|String
name|fromVersion
parameter_list|)
throws|throws
name|HiveMetaException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|upgradeScriptList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// check if we are already at current schema level
if|if
condition|(
name|getHiveSchemaVersion
argument_list|()
operator|.
name|equals
argument_list|(
name|fromVersion
argument_list|)
condition|)
block|{
return|return
name|upgradeScriptList
return|;
block|}
comment|// Find the list of scripts to execute for this upgrade
name|int
name|firstScript
init|=
name|hiveSchemaVersions
operator|.
name|length
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|hiveSchemaVersions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|hiveSchemaVersions
index|[
name|i
index|]
operator|.
name|startsWith
argument_list|(
name|fromVersion
argument_list|)
condition|)
block|{
name|firstScript
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|firstScript
operator|==
name|hiveSchemaVersions
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Unknown version specified for upgrade "
operator|+
name|fromVersion
operator|+
literal|" Metastore schema may be too old or newer"
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
name|firstScript
init|;
name|i
operator|<
name|hiveSchemaVersions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|scriptFile
init|=
name|generateUpgradeFileName
argument_list|(
name|hiveSchemaVersions
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|upgradeScriptList
operator|.
name|add
argument_list|(
name|scriptFile
argument_list|)
expr_stmt|;
block|}
return|return
name|upgradeScriptList
return|;
block|}
comment|/***    * Get the name of the script to initialize the schema for given version    * @param toVersion Target version. If it's null, then the current server version is used    * @return    * @throws HiveMetaException    */
specifier|public
name|String
name|generateInitFileName
parameter_list|(
name|String
name|toVersion
parameter_list|)
throws|throws
name|HiveMetaException
block|{
if|if
condition|(
name|toVersion
operator|==
literal|null
condition|)
block|{
name|toVersion
operator|=
name|getHiveSchemaVersion
argument_list|()
expr_stmt|;
block|}
name|String
name|initScriptName
init|=
name|INIT_FILE_PREFIX
operator|+
name|toVersion
operator|+
literal|"."
operator|+
name|dbType
operator|+
name|SQL_FILE_EXTENSION
decl_stmt|;
comment|// check if the file exists
if|if
condition|(
operator|!
operator|(
operator|new
name|File
argument_list|(
name|getMetaStoreScriptDir
argument_list|()
operator|+
name|File
operator|.
name|separatorChar
operator|+
name|initScriptName
argument_list|)
operator|.
name|exists
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Unknown version specified for initialization: "
operator|+
name|toVersion
argument_list|)
throw|;
block|}
return|return
name|initScriptName
return|;
block|}
comment|/**    * Find the directory of metastore scripts    * @return    */
specifier|public
name|String
name|getMetaStoreScriptDir
parameter_list|()
block|{
return|return
name|hiveHome
operator|+
name|File
operator|.
name|separatorChar
operator|+
literal|"scripts"
operator|+
name|File
operator|.
name|separatorChar
operator|+
literal|"metastore"
operator|+
name|File
operator|.
name|separatorChar
operator|+
literal|"upgrade"
operator|+
name|File
operator|.
name|separatorChar
operator|+
name|dbType
return|;
block|}
comment|// format the upgrade script name eg upgrade-x-y-dbType.sql
specifier|private
name|String
name|generateUpgradeFileName
parameter_list|(
name|String
name|fileVersion
parameter_list|)
block|{
return|return
name|UPGRADE_FILE_PREFIX
operator|+
name|fileVersion
operator|+
literal|"."
operator|+
name|dbType
operator|+
name|SQL_FILE_EXTENSION
return|;
block|}
specifier|public
specifier|static
name|String
name|getHiveSchemaVersion
parameter_list|()
block|{
name|String
name|hiveVersion
init|=
name|HiveVersionInfo
operator|.
name|getShortVersion
argument_list|()
decl_stmt|;
comment|// if there is an equivalent version, return that, else return this version
name|String
name|equivalentVersion
init|=
name|EQUIVALENT_VERSIONS
operator|.
name|get
argument_list|(
name|hiveVersion
argument_list|)
decl_stmt|;
if|if
condition|(
name|equivalentVersion
operator|!=
literal|null
condition|)
block|{
return|return
name|equivalentVersion
return|;
block|}
else|else
block|{
return|return
name|hiveVersion
return|;
block|}
block|}
block|}
end_class

end_unit

