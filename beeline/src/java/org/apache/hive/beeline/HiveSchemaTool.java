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
name|hive
operator|.
name|beeline
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedWriter
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
name|java
operator|.
name|io
operator|.
name|PrintStream
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
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Statement
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLineParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|GnuParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|HelpFormatter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Option
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|OptionBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|OptionGroup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Options
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|ParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|output
operator|.
name|NullOutputStream
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
name|HiveMetaException
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
name|MetaStoreSchemaInfo
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
name|shims
operator|.
name|ShimLoader
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
name|beeline
operator|.
name|HiveSchemaHelper
operator|.
name|NestedScriptParser
import|;
end_import

begin_class
specifier|public
class|class
name|HiveSchemaTool
block|{
specifier|private
name|String
name|userName
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|passWord
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|dryRun
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|verbose
init|=
literal|false
decl_stmt|;
specifier|private
name|String
name|dbOpts
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbType
decl_stmt|;
specifier|private
specifier|final
name|MetaStoreSchemaInfo
name|metaStoreSchemaInfo
decl_stmt|;
specifier|public
name|HiveSchemaTool
parameter_list|(
name|String
name|dbType
parameter_list|)
throws|throws
name|HiveMetaException
block|{
name|this
argument_list|(
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_HOME"
argument_list|)
argument_list|,
operator|new
name|HiveConf
argument_list|(
name|HiveSchemaTool
operator|.
name|class
argument_list|)
argument_list|,
name|dbType
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveSchemaTool
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
if|if
condition|(
name|hiveHome
operator|==
literal|null
operator|||
name|hiveHome
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"No Hive home directory provided"
argument_list|)
throw|;
block|}
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|dbType
operator|=
name|dbType
expr_stmt|;
name|this
operator|.
name|metaStoreSchemaInfo
operator|=
operator|new
name|MetaStoreSchemaInfo
argument_list|(
name|hiveHome
argument_list|,
name|hiveConf
argument_list|,
name|dbType
argument_list|)
expr_stmt|;
name|userName
operator|=
name|hiveConf
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|METASTORE_CONNECTION_USER_NAME
operator|.
name|varname
argument_list|)
expr_stmt|;
try|try
block|{
name|passWord
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getPassword
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|err
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Error getting metastore password"
argument_list|,
name|err
argument_list|)
throw|;
block|}
block|}
specifier|public
name|HiveConf
name|getHiveConf
parameter_list|()
block|{
return|return
name|hiveConf
return|;
block|}
specifier|public
name|void
name|setUserName
parameter_list|(
name|String
name|userName
parameter_list|)
block|{
name|this
operator|.
name|userName
operator|=
name|userName
expr_stmt|;
block|}
specifier|public
name|void
name|setPassWord
parameter_list|(
name|String
name|passWord
parameter_list|)
block|{
name|this
operator|.
name|passWord
operator|=
name|passWord
expr_stmt|;
block|}
specifier|public
name|void
name|setDryRun
parameter_list|(
name|boolean
name|dryRun
parameter_list|)
block|{
name|this
operator|.
name|dryRun
operator|=
name|dryRun
expr_stmt|;
block|}
specifier|public
name|void
name|setVerbose
parameter_list|(
name|boolean
name|verbose
parameter_list|)
block|{
name|this
operator|.
name|verbose
operator|=
name|verbose
expr_stmt|;
block|}
specifier|public
name|void
name|setDbOpts
parameter_list|(
name|String
name|dbOpts
parameter_list|)
block|{
name|this
operator|.
name|dbOpts
operator|=
name|dbOpts
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|printAndExit
parameter_list|(
name|Options
name|cmdLineOptions
parameter_list|)
block|{
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
literal|"schemaTool"
argument_list|,
name|cmdLineOptions
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Connection
name|getConnectionToMetastore
parameter_list|(
name|boolean
name|printInfo
parameter_list|)
throws|throws
name|HiveMetaException
block|{
return|return
name|HiveSchemaHelper
operator|.
name|getConnectionToMetastore
argument_list|(
name|userName
argument_list|,
name|passWord
argument_list|,
name|printInfo
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
specifier|private
name|NestedScriptParser
name|getDbCommandParser
parameter_list|(
name|String
name|dbType
parameter_list|)
block|{
return|return
name|HiveSchemaHelper
operator|.
name|getDbCommandParser
argument_list|(
name|dbType
argument_list|,
name|dbOpts
argument_list|,
name|userName
argument_list|,
name|passWord
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
comment|/***    * Print Hive version and schema version    * @throws MetaException    */
specifier|public
name|void
name|showInfo
parameter_list|()
throws|throws
name|HiveMetaException
block|{
name|Connection
name|metastoreConn
init|=
name|getConnectionToMetastore
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Hive distribution version:\t "
operator|+
name|MetaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Metastore schema version:\t "
operator|+
name|getMetaStoreSchemaVersion
argument_list|(
name|metastoreConn
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// read schema version from metastore
specifier|private
name|String
name|getMetaStoreSchemaVersion
parameter_list|(
name|Connection
name|metastoreConn
parameter_list|)
throws|throws
name|HiveMetaException
block|{
name|String
name|versionQuery
decl_stmt|;
if|if
condition|(
name|getDbCommandParser
argument_list|(
name|dbType
argument_list|)
operator|.
name|needsQuotedIdentifier
argument_list|()
condition|)
block|{
name|versionQuery
operator|=
literal|"select t.\"SCHEMA_VERSION\" from \"VERSION\" t"
expr_stmt|;
block|}
else|else
block|{
name|versionQuery
operator|=
literal|"select t.SCHEMA_VERSION from VERSION t"
expr_stmt|;
block|}
try|try
block|{
name|Statement
name|stmt
init|=
name|metastoreConn
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|ResultSet
name|res
init|=
name|stmt
operator|.
name|executeQuery
argument_list|(
name|versionQuery
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|res
operator|.
name|next
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Didn't find version data in metastore"
argument_list|)
throw|;
block|}
name|String
name|currentSchemaVersion
init|=
name|res
operator|.
name|getString
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|metastoreConn
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|currentSchemaVersion
return|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Failed to get schema version."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// test the connection metastore using the config property
specifier|private
name|void
name|testConnectionToMetastore
parameter_list|()
throws|throws
name|HiveMetaException
block|{
name|Connection
name|conn
init|=
name|getConnectionToMetastore
argument_list|(
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Failed to close metastore connection"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * check if the current schema version in metastore matches the Hive version    * @throws MetaException    */
specifier|public
name|void
name|verifySchemaVersion
parameter_list|()
throws|throws
name|HiveMetaException
block|{
comment|// don't check version if its a dry run
if|if
condition|(
name|dryRun
condition|)
block|{
return|return;
block|}
name|String
name|newSchemaVersion
init|=
name|getMetaStoreSchemaVersion
argument_list|(
name|getConnectionToMetastore
argument_list|(
literal|false
argument_list|)
argument_list|)
decl_stmt|;
comment|// verify that the new version is added to schema
if|if
condition|(
operator|!
name|MetaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|newSchemaVersion
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Expected schema version "
operator|+
name|MetaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
operator|+
literal|", found version "
operator|+
name|newSchemaVersion
argument_list|)
throw|;
block|}
block|}
comment|/**    * Perform metastore schema upgrade. extract the current schema version from metastore    * @throws MetaException    */
specifier|public
name|void
name|doUpgrade
parameter_list|()
throws|throws
name|HiveMetaException
block|{
name|String
name|fromVersion
init|=
name|getMetaStoreSchemaVersion
argument_list|(
name|getConnectionToMetastore
argument_list|(
literal|false
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fromVersion
operator|==
literal|null
operator|||
name|fromVersion
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Schema version not stored in the metastore. "
operator|+
literal|"Metastore schema is too old or corrupt. Try specifying the version manually"
argument_list|)
throw|;
block|}
name|doUpgrade
argument_list|(
name|fromVersion
argument_list|)
expr_stmt|;
block|}
comment|/**    * Perform metastore schema upgrade    *    * @param fromSchemaVer    *          Existing version of the metastore. If null, then read from the metastore    * @throws MetaException    */
specifier|public
name|void
name|doUpgrade
parameter_list|(
name|String
name|fromSchemaVer
parameter_list|)
throws|throws
name|HiveMetaException
block|{
if|if
condition|(
name|MetaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
operator|.
name|equals
argument_list|(
name|fromSchemaVer
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"No schema upgrade required from version "
operator|+
name|fromSchemaVer
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Find the list of scripts to execute for this upgrade
name|List
argument_list|<
name|String
argument_list|>
name|upgradeScripts
init|=
name|metaStoreSchemaInfo
operator|.
name|getUpgradeScripts
argument_list|(
name|fromSchemaVer
argument_list|)
decl_stmt|;
name|testConnectionToMetastore
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting upgrade metastore schema from version "
operator|+
name|fromSchemaVer
operator|+
literal|" to "
operator|+
name|MetaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|scriptDir
init|=
name|metaStoreSchemaInfo
operator|.
name|getMetaStoreScriptDir
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|String
name|scriptFile
range|:
name|upgradeScripts
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Upgrade script "
operator|+
name|scriptFile
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|dryRun
condition|)
block|{
name|runPreUpgrade
argument_list|(
name|scriptDir
argument_list|,
name|scriptFile
argument_list|)
expr_stmt|;
name|runBeeLine
argument_list|(
name|scriptDir
argument_list|,
name|scriptFile
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Completed "
operator|+
name|scriptFile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|eIO
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Upgrade FAILED! Metastore state would be inconsistent !!"
argument_list|,
name|eIO
argument_list|)
throw|;
block|}
comment|// Revalidated the new version after upgrade
name|verifySchemaVersion
argument_list|()
expr_stmt|;
block|}
comment|/**    * Initialize the metastore schema to current version    *    * @throws MetaException    */
specifier|public
name|void
name|doInit
parameter_list|()
throws|throws
name|HiveMetaException
block|{
name|doInit
argument_list|(
name|MetaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
argument_list|)
expr_stmt|;
comment|// Revalidated the new version after upgrade
name|verifySchemaVersion
argument_list|()
expr_stmt|;
block|}
comment|/**    * Initialize the metastore schema    *    * @param toVersion    *          If null then current hive version is used    * @throws MetaException    */
specifier|public
name|void
name|doInit
parameter_list|(
name|String
name|toVersion
parameter_list|)
throws|throws
name|HiveMetaException
block|{
name|testConnectionToMetastore
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting metastore schema initialization to "
operator|+
name|toVersion
argument_list|)
expr_stmt|;
name|String
name|initScriptDir
init|=
name|metaStoreSchemaInfo
operator|.
name|getMetaStoreScriptDir
argument_list|()
decl_stmt|;
name|String
name|initScriptFile
init|=
name|metaStoreSchemaInfo
operator|.
name|generateInitFileName
argument_list|(
name|toVersion
argument_list|)
decl_stmt|;
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Initialization script "
operator|+
name|initScriptFile
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|dryRun
condition|)
block|{
name|runBeeLine
argument_list|(
name|initScriptDir
argument_list|,
name|initScriptFile
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Initialization script completed"
argument_list|)
expr_stmt|;
block|}
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
literal|"Schema initialization FAILED!"
operator|+
literal|" Metastore state would be inconsistent !!"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    *  Run pre-upgrade scripts corresponding to a given upgrade script,    *  if any exist. The errors from pre-upgrade are ignored.    *  Pre-upgrade scripts typically contain setup statements which    *  may fail on some database versions and failure is ignorable.    *    *  @param scriptDir upgrade script directory name    *  @param scriptFile upgrade script file name    */
specifier|private
name|void
name|runPreUpgrade
parameter_list|(
name|String
name|scriptDir
parameter_list|,
name|String
name|scriptFile
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
condition|;
name|i
operator|++
control|)
block|{
name|String
name|preUpgradeScript
init|=
name|MetaStoreSchemaInfo
operator|.
name|getPreUpgradeScriptName
argument_list|(
name|i
argument_list|,
name|scriptFile
argument_list|)
decl_stmt|;
name|File
name|preUpgradeScriptFile
init|=
operator|new
name|File
argument_list|(
name|scriptDir
argument_list|,
name|preUpgradeScript
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|preUpgradeScriptFile
operator|.
name|isFile
argument_list|()
condition|)
block|{
break|break;
block|}
try|try
block|{
name|runBeeLine
argument_list|(
name|scriptDir
argument_list|,
name|preUpgradeScript
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Completed "
operator|+
name|preUpgradeScript
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Ignore the pre-upgrade script errors
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Warning in pre-upgrade script "
operator|+
name|preUpgradeScript
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/***    * Run beeline with the given metastore script. Flatten the nested scripts    * into single file.    */
specifier|private
name|void
name|runBeeLine
parameter_list|(
name|String
name|scriptDir
parameter_list|,
name|String
name|scriptFile
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveMetaException
block|{
name|NestedScriptParser
name|dbCommandParser
init|=
name|getDbCommandParser
argument_list|(
name|dbType
argument_list|)
decl_stmt|;
comment|// expand the nested script
name|String
name|sqlCommands
init|=
name|dbCommandParser
operator|.
name|buildCommand
argument_list|(
name|scriptDir
argument_list|,
name|scriptFile
argument_list|)
decl_stmt|;
name|File
name|tmpFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"schematool"
argument_list|,
literal|".sql"
argument_list|)
decl_stmt|;
name|tmpFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
comment|// write out the buffer into a file. Add beeline commands for autocommit and close
name|FileWriter
name|fstream
init|=
operator|new
name|FileWriter
argument_list|(
name|tmpFile
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|BufferedWriter
name|out
init|=
operator|new
name|BufferedWriter
argument_list|(
name|fstream
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"!autocommit on"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|sqlCommands
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"!closeall"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|runBeeLine
argument_list|(
name|tmpFile
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Generate the beeline args per hive conf and execute the given script
specifier|public
name|void
name|runBeeLine
parameter_list|(
name|String
name|sqlScriptFile
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|argList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"-u"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|HiveSchemaHelper
operator|.
name|getValidConfVar
argument_list|(
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
argument_list|,
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"-d"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|HiveSchemaHelper
operator|.
name|getValidConfVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_CONNECTION_DRIVER
argument_list|,
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"-n"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|userName
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"-p"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|passWord
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
literal|"-f"
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|sqlScriptFile
argument_list|)
expr_stmt|;
comment|// run the script using Beeline
name|BeeLine
name|beeLine
init|=
operator|new
name|BeeLine
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|verbose
condition|)
block|{
name|beeLine
operator|.
name|setOutputStream
argument_list|(
operator|new
name|PrintStream
argument_list|(
operator|new
name|NullOutputStream
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|setSilent
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|setAllowMultiLineCommand
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|setIsolation
argument_list|(
literal|"TRANSACTION_READ_COMMITTED"
argument_list|)
expr_stmt|;
comment|// We can be pretty sure that an entire line can be processed as a single command since
comment|// we always add a line separator at the end while calling dbCommandParser.buildCommand.
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|setEntireLineAsCommand
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|beeLine
operator|.
name|begin
argument_list|(
name|argList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Schema script failed, errorcode "
operator|+
name|status
argument_list|)
throw|;
block|}
block|}
comment|// Create the required command line options
annotation|@
name|SuppressWarnings
argument_list|(
literal|"static-access"
argument_list|)
specifier|private
specifier|static
name|void
name|initOptions
parameter_list|(
name|Options
name|cmdLineOptions
parameter_list|)
block|{
name|Option
name|help
init|=
operator|new
name|Option
argument_list|(
literal|"help"
argument_list|,
literal|"print this message"
argument_list|)
decl_stmt|;
name|Option
name|upgradeOpt
init|=
operator|new
name|Option
argument_list|(
literal|"upgradeSchema"
argument_list|,
literal|"Schema upgrade"
argument_list|)
decl_stmt|;
name|Option
name|upgradeFromOpt
init|=
name|OptionBuilder
operator|.
name|withArgName
argument_list|(
literal|"upgradeFrom"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|withDescription
argument_list|(
literal|"Schema upgrade from a version"
argument_list|)
operator|.
name|create
argument_list|(
literal|"upgradeSchemaFrom"
argument_list|)
decl_stmt|;
name|Option
name|initOpt
init|=
operator|new
name|Option
argument_list|(
literal|"initSchema"
argument_list|,
literal|"Schema initialization"
argument_list|)
decl_stmt|;
name|Option
name|initToOpt
init|=
name|OptionBuilder
operator|.
name|withArgName
argument_list|(
literal|"initTo"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|withDescription
argument_list|(
literal|"Schema initialization to a version"
argument_list|)
operator|.
name|create
argument_list|(
literal|"initSchemaTo"
argument_list|)
decl_stmt|;
name|Option
name|infoOpt
init|=
operator|new
name|Option
argument_list|(
literal|"info"
argument_list|,
literal|"Show config and schema details"
argument_list|)
decl_stmt|;
name|OptionGroup
name|optGroup
init|=
operator|new
name|OptionGroup
argument_list|()
decl_stmt|;
name|optGroup
operator|.
name|addOption
argument_list|(
name|upgradeOpt
argument_list|)
operator|.
name|addOption
argument_list|(
name|initOpt
argument_list|)
operator|.
name|addOption
argument_list|(
name|help
argument_list|)
operator|.
name|addOption
argument_list|(
name|upgradeFromOpt
argument_list|)
operator|.
name|addOption
argument_list|(
name|initToOpt
argument_list|)
operator|.
name|addOption
argument_list|(
name|infoOpt
argument_list|)
expr_stmt|;
name|optGroup
operator|.
name|setRequired
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Option
name|userNameOpt
init|=
name|OptionBuilder
operator|.
name|withArgName
argument_list|(
literal|"user"
argument_list|)
operator|.
name|hasArgs
argument_list|()
operator|.
name|withDescription
argument_list|(
literal|"Override config file user name"
argument_list|)
operator|.
name|create
argument_list|(
literal|"userName"
argument_list|)
decl_stmt|;
name|Option
name|passwdOpt
init|=
name|OptionBuilder
operator|.
name|withArgName
argument_list|(
literal|"password"
argument_list|)
operator|.
name|hasArgs
argument_list|()
operator|.
name|withDescription
argument_list|(
literal|"Override config file password"
argument_list|)
operator|.
name|create
argument_list|(
literal|"passWord"
argument_list|)
decl_stmt|;
name|Option
name|dbTypeOpt
init|=
name|OptionBuilder
operator|.
name|withArgName
argument_list|(
literal|"databaseType"
argument_list|)
operator|.
name|hasArgs
argument_list|()
operator|.
name|withDescription
argument_list|(
literal|"Metastore database type"
argument_list|)
operator|.
name|create
argument_list|(
literal|"dbType"
argument_list|)
decl_stmt|;
name|Option
name|dbOpts
init|=
name|OptionBuilder
operator|.
name|withArgName
argument_list|(
literal|"databaseOpts"
argument_list|)
operator|.
name|hasArgs
argument_list|()
operator|.
name|withDescription
argument_list|(
literal|"Backend DB specific options"
argument_list|)
operator|.
name|create
argument_list|(
literal|"dbOpts"
argument_list|)
decl_stmt|;
name|Option
name|dryRunOpt
init|=
operator|new
name|Option
argument_list|(
literal|"dryRun"
argument_list|,
literal|"list SQL scripts (no execute)"
argument_list|)
decl_stmt|;
name|Option
name|verboseOpt
init|=
operator|new
name|Option
argument_list|(
literal|"verbose"
argument_list|,
literal|"only print SQL statements"
argument_list|)
decl_stmt|;
name|cmdLineOptions
operator|.
name|addOption
argument_list|(
name|help
argument_list|)
expr_stmt|;
name|cmdLineOptions
operator|.
name|addOption
argument_list|(
name|dryRunOpt
argument_list|)
expr_stmt|;
name|cmdLineOptions
operator|.
name|addOption
argument_list|(
name|userNameOpt
argument_list|)
expr_stmt|;
name|cmdLineOptions
operator|.
name|addOption
argument_list|(
name|passwdOpt
argument_list|)
expr_stmt|;
name|cmdLineOptions
operator|.
name|addOption
argument_list|(
name|dbTypeOpt
argument_list|)
expr_stmt|;
name|cmdLineOptions
operator|.
name|addOption
argument_list|(
name|verboseOpt
argument_list|)
expr_stmt|;
name|cmdLineOptions
operator|.
name|addOption
argument_list|(
name|dbOpts
argument_list|)
expr_stmt|;
name|cmdLineOptions
operator|.
name|addOptionGroup
argument_list|(
name|optGroup
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|CommandLineParser
name|parser
init|=
operator|new
name|GnuParser
argument_list|()
decl_stmt|;
name|CommandLine
name|line
init|=
literal|null
decl_stmt|;
name|String
name|dbType
init|=
literal|null
decl_stmt|;
name|String
name|schemaVer
init|=
literal|null
decl_stmt|;
name|Options
name|cmdLineOptions
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
comment|// Argument handling
name|initOptions
argument_list|(
name|cmdLineOptions
argument_list|)
expr_stmt|;
try|try
block|{
name|line
operator|=
name|parser
operator|.
name|parse
argument_list|(
name|cmdLineOptions
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"HiveSchemaTool:Parsing failed.  Reason: "
operator|+
name|e
operator|.
name|getLocalizedMessage
argument_list|()
argument_list|)
expr_stmt|;
name|printAndExit
argument_list|(
name|cmdLineOptions
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"help"
argument_list|)
condition|)
block|{
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
literal|"schemaTool"
argument_list|,
name|cmdLineOptions
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"dbType"
argument_list|)
condition|)
block|{
name|dbType
operator|=
name|line
operator|.
name|getOptionValue
argument_list|(
literal|"dbType"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
operator|!
name|dbType
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveSchemaHelper
operator|.
name|DB_DERBY
argument_list|)
operator|&&
operator|!
name|dbType
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveSchemaHelper
operator|.
name|DB_MSSQL
argument_list|)
operator|&&
operator|!
name|dbType
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveSchemaHelper
operator|.
name|DB_MYSQL
argument_list|)
operator|&&
operator|!
name|dbType
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveSchemaHelper
operator|.
name|DB_POSTGRACE
argument_list|)
operator|&&
operator|!
name|dbType
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveSchemaHelper
operator|.
name|DB_ORACLE
argument_list|)
operator|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unsupported dbType "
operator|+
name|dbType
argument_list|)
expr_stmt|;
name|printAndExit
argument_list|(
name|cmdLineOptions
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"no dbType supplied"
argument_list|)
expr_stmt|;
name|printAndExit
argument_list|(
name|cmdLineOptions
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
try|try
block|{
name|HiveSchemaTool
name|schemaTool
init|=
operator|new
name|HiveSchemaTool
argument_list|(
name|dbType
argument_list|)
decl_stmt|;
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"userName"
argument_list|)
condition|)
block|{
name|schemaTool
operator|.
name|setUserName
argument_list|(
name|line
operator|.
name|getOptionValue
argument_list|(
literal|"userName"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"passWord"
argument_list|)
condition|)
block|{
name|schemaTool
operator|.
name|setPassWord
argument_list|(
name|line
operator|.
name|getOptionValue
argument_list|(
literal|"passWord"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"dryRun"
argument_list|)
condition|)
block|{
name|schemaTool
operator|.
name|setDryRun
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"verbose"
argument_list|)
condition|)
block|{
name|schemaTool
operator|.
name|setVerbose
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"dbOpts"
argument_list|)
condition|)
block|{
name|schemaTool
operator|.
name|setDbOpts
argument_list|(
name|line
operator|.
name|getOptionValue
argument_list|(
literal|"dbOpts"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"info"
argument_list|)
condition|)
block|{
name|schemaTool
operator|.
name|showInfo
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"upgradeSchema"
argument_list|)
condition|)
block|{
name|schemaTool
operator|.
name|doUpgrade
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"upgradeSchemaFrom"
argument_list|)
condition|)
block|{
name|schemaVer
operator|=
name|line
operator|.
name|getOptionValue
argument_list|(
literal|"upgradeSchemaFrom"
argument_list|)
expr_stmt|;
name|schemaTool
operator|.
name|doUpgrade
argument_list|(
name|schemaVer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"initSchema"
argument_list|)
condition|)
block|{
name|schemaTool
operator|.
name|doInit
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"initSchemaTo"
argument_list|)
condition|)
block|{
name|schemaVer
operator|=
name|line
operator|.
name|getOptionValue
argument_list|(
literal|"initSchemaTo"
argument_list|)
expr_stmt|;
name|schemaTool
operator|.
name|doInit
argument_list|(
name|schemaVer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"no valid option supplied"
argument_list|)
expr_stmt|;
name|printAndExit
argument_list|(
name|cmdLineOptions
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveMetaException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|line
operator|.
name|hasOption
argument_list|(
literal|"verbose"
argument_list|)
condition|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"*** schemaTool failed ***"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"schemaTool completed"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

