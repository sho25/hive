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
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
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
name|tools
operator|.
name|HiveSchemaHelper
import|;
end_import

begin_comment
comment|/**  * Defines the method which must be implemented to be used using schema tool to support metastore  * schema upgrades. The configuration hive.metastore.schema.info.class is used to create instances  * of this type by SchemaTool.  *  * Instances of this interface should be created using MetaStoreSchemaInfoFactory class which uses  * two Strings argument constructor to instantiate the implementations of this interface  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|IMetaStoreSchemaInfo
block|{
name|String
name|SQL_FILE_EXTENSION
init|=
literal|".sql"
decl_stmt|;
comment|/***    * Get the list of sql scripts required to upgrade from the give version to current.    *    * @param fromVersion    * @return    * @throws HiveMetaException    */
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
function_decl|;
comment|/***    * Get the name of the script to initialize the schema for given version    *    * @param toVersion Target version. If it's null, then the current server version is used    * @return    * @throws HiveMetaException    */
name|String
name|generateInitFileName
parameter_list|(
name|String
name|toVersion
parameter_list|)
throws|throws
name|HiveMetaException
function_decl|;
comment|/**    * Find the directory of metastore scripts    *    * @return the path of directory where the sql scripts are    */
name|String
name|getMetaStoreScriptDir
parameter_list|()
function_decl|;
comment|/**    * Get the pre-upgrade script for a given script name. Schema tool runs the pre-upgrade scripts    * returned by this method before running any upgrade scripts. These scripts could contain setup    * statements may fail on some database versions and failure is ignorable.    *    * @param index - index number of the file. The preupgrade script name is derived using the given    *          index    * @param scriptName - upgrade script name    * @return name of the pre-upgrade script to be run before running upgrade script    */
name|String
name|getPreUpgradeScriptName
parameter_list|(
name|int
name|index
parameter_list|,
name|String
name|scriptName
parameter_list|)
function_decl|;
comment|/**    * Get hive distribution schema version. Schematool uses this version to identify    * the Hive version. It compares this version with the version found in metastore database    * to determine the upgrade or initialization scripts    * @return Hive schema version    */
name|String
name|getHiveSchemaVersion
parameter_list|()
function_decl|;
comment|/**    * Get the schema version from the backend database. This version is used by SchemaTool to to    * compare the version returned by getHiveSchemaVersion and determine the upgrade order and    * scripts needed to upgrade the metastore schema    *     * @param metastoreDbConnectionInfo Connection information needed to connect to the backend    *          database    * @return    * @throws HiveMetaException when unable to fetch the schema version    */
name|String
name|getMetaStoreSchemaVersion
parameter_list|(
name|HiveSchemaHelper
operator|.
name|MetaStoreConnectionInfo
name|metastoreDbConnectionInfo
parameter_list|)
throws|throws
name|HiveMetaException
function_decl|;
comment|/**    * A dbVersion is compatible with hive version if it is greater or equal to the hive version. This    * is result of the db schema upgrade design principles followed in hive project. The state where    * db schema version is ahead of hive software version is often seen when a 'rolling upgrade' or    * 'rolling downgrade' is happening. This is a state where hive is functional and returning non    * zero status for it is misleading.    *    * @param hiveVersion version of hive software    * @param dbVersion version of metastore rdbms schema    * @return true if versions are compatible    */
name|boolean
name|isVersionCompatible
parameter_list|(
name|String
name|productVersion
parameter_list|,
name|String
name|dbVersion
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

