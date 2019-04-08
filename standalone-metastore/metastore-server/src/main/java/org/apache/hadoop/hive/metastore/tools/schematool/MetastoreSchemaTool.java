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
name|metastore
operator|.
name|tools
operator|.
name|schematool
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|IMetaStoreSchemaInfo
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
name|MetaStoreSchemaInfoFactory
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
name|conf
operator|.
name|MetastoreConf
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
name|conf
operator|.
name|MetastoreConf
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
name|tools
operator|.
name|schematool
operator|.
name|HiveSchemaHelper
operator|.
name|MetaStoreConnectionInfo
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
name|schematool
operator|.
name|HiveSchemaHelper
operator|.
name|NestedScriptParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|sqlline
operator|.
name|SqlLine
import|;
end_import

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
name|ByteArrayOutputStream
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
name|io
operator|.
name|OutputStream
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
name|net
operator|.
name|URI
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
name|SQLException
import|;
end_import

begin_class
specifier|public
class|class
name|MetastoreSchemaTool
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MetastoreSchemaTool
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PASSWD_MASK
init|=
literal|"[passwd stripped]"
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|String
name|dbOpts
init|=
literal|null
decl_stmt|;
specifier|protected
name|String
name|dbType
decl_stmt|;
specifier|protected
name|String
name|driver
init|=
literal|null
decl_stmt|;
specifier|protected
name|boolean
name|dryRun
init|=
literal|false
decl_stmt|;
specifier|protected
name|String
name|hiveDb
decl_stmt|;
comment|// Hive database, for use when creating the user, not for connecting
specifier|protected
name|String
name|hivePasswd
decl_stmt|;
comment|// Hive password, for use when creating the user, not for connecting
specifier|protected
name|String
name|hiveUser
decl_stmt|;
comment|// Hive username, for use when creating the user, not for connecting
specifier|protected
name|String
name|metaDbType
decl_stmt|;
specifier|protected
name|IMetaStoreSchemaInfo
name|metaStoreSchemaInfo
decl_stmt|;
specifier|protected
name|boolean
name|needsQuotedIdentifier
decl_stmt|;
specifier|protected
name|String
name|quoteCharacter
decl_stmt|;
specifier|protected
name|String
name|passWord
init|=
literal|null
decl_stmt|;
specifier|protected
name|String
name|url
init|=
literal|null
decl_stmt|;
specifier|protected
name|String
name|userName
init|=
literal|null
decl_stmt|;
specifier|protected
name|URI
index|[]
name|validationServers
init|=
literal|null
decl_stmt|;
comment|// The list of servers the database/partition/table can locate on
specifier|protected
name|boolean
name|verbose
init|=
literal|false
decl_stmt|;
specifier|protected
name|SchemaToolCommandLine
name|cmdLine
decl_stmt|;
specifier|private
specifier|static
name|String
name|homeDir
decl_stmt|;
specifier|private
specifier|static
name|String
name|findHomeDir
parameter_list|()
block|{
comment|// If METASTORE_HOME is set, use it, else use HIVE_HOME for backwards compatibility.
name|homeDir
operator|=
name|homeDir
operator|==
literal|null
condition|?
name|System
operator|.
name|getenv
argument_list|(
literal|"METASTORE_HOME"
argument_list|)
else|:
name|homeDir
expr_stmt|;
return|return
name|homeDir
operator|==
literal|null
condition|?
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_HOME"
argument_list|)
else|:
name|homeDir
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|void
name|setHomeDirForTesting
parameter_list|()
block|{
name|homeDir
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"target/tmp"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|MetastoreSchemaTool
parameter_list|()
block|{    }
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|init
parameter_list|(
name|String
name|metastoreHome
parameter_list|,
name|String
index|[]
name|args
parameter_list|,
name|OptionGroup
name|additionalOptions
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveMetaException
block|{
try|try
block|{
name|cmdLine
operator|=
operator|new
name|SchemaToolCommandLine
argument_list|(
name|args
argument_list|,
name|additionalOptions
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
literal|"Failed to parse command line. "
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveMetaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|metastoreHome
operator|==
literal|null
operator|||
name|metastoreHome
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"No Metastore home directory provided"
argument_list|)
throw|;
block|}
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|dbType
operator|=
name|cmdLine
operator|.
name|getDbType
argument_list|()
expr_stmt|;
name|this
operator|.
name|metaDbType
operator|=
name|cmdLine
operator|.
name|getMetaDbType
argument_list|()
expr_stmt|;
name|NestedScriptParser
name|parser
init|=
name|getDbCommandParser
argument_list|(
name|dbType
argument_list|,
name|metaDbType
argument_list|)
decl_stmt|;
name|this
operator|.
name|needsQuotedIdentifier
operator|=
name|parser
operator|.
name|needsQuotedIdentifier
argument_list|()
expr_stmt|;
name|this
operator|.
name|quoteCharacter
operator|=
name|parser
operator|.
name|getQuoteCharacter
argument_list|()
expr_stmt|;
name|this
operator|.
name|metaStoreSchemaInfo
operator|=
name|MetaStoreSchemaInfoFactory
operator|.
name|get
argument_list|(
name|conf
argument_list|,
name|metastoreHome
argument_list|,
name|dbType
argument_list|)
expr_stmt|;
comment|// If the dbType is "hive", this is setting up the information schema in Hive.
comment|// We will set the default jdbc url and driver.
comment|// It is overridden by command line options if passed (-url and -driver)
if|if
condition|(
name|dbType
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveSchemaHelper
operator|.
name|DB_HIVE
argument_list|)
condition|)
block|{
name|this
operator|.
name|url
operator|=
name|HiveSchemaHelper
operator|.
name|EMBEDDED_HS2_URL
expr_stmt|;
name|this
operator|.
name|driver
operator|=
name|HiveSchemaHelper
operator|.
name|HIVE_JDBC_DRIVER
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"userName"
argument_list|)
condition|)
block|{
name|setUserName
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
literal|"userName"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setUserName
argument_list|(
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECTION_USER_NAME
operator|.
name|getVarname
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"passWord"
argument_list|)
condition|)
block|{
name|setPassWord
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
literal|"passWord"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|setPassWord
argument_list|(
name|MetastoreConf
operator|.
name|getPassword
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|ConfVars
operator|.
name|PWD
argument_list|)
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
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"url"
argument_list|)
condition|)
block|{
name|setUrl
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
literal|"url"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"driver"
argument_list|)
condition|)
block|{
name|setDriver
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
literal|"driver"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"dryRun"
argument_list|)
condition|)
block|{
name|setDryRun
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"verbose"
argument_list|)
condition|)
block|{
name|setVerbose
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"dbOpts"
argument_list|)
condition|)
block|{
name|setDbOpts
argument_list|(
name|cmdLine
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
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"validate"
argument_list|)
operator|&&
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"servers"
argument_list|)
condition|)
block|{
name|setValidationServers
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
literal|"servers"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"hiveUser"
argument_list|)
condition|)
block|{
name|setHiveUser
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
literal|"hiveUser"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"hivePassword"
argument_list|)
condition|)
block|{
name|setHivePasswd
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
literal|"hivePassword"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"hiveDb"
argument_list|)
condition|)
block|{
name|setHiveDb
argument_list|(
name|cmdLine
operator|.
name|getOptionValue
argument_list|(
literal|"hiveDb"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|protected
name|String
name|getDbType
parameter_list|()
block|{
return|return
name|dbType
return|;
block|}
specifier|protected
name|void
name|setUrl
parameter_list|(
name|String
name|url
parameter_list|)
block|{
name|this
operator|.
name|url
operator|=
name|url
expr_stmt|;
block|}
specifier|protected
name|void
name|setDriver
parameter_list|(
name|String
name|driver
parameter_list|)
block|{
name|this
operator|.
name|driver
operator|=
name|driver
expr_stmt|;
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
specifier|protected
name|boolean
name|isDryRun
parameter_list|()
block|{
return|return
name|dryRun
return|;
block|}
specifier|protected
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
specifier|protected
name|boolean
name|isVerbose
parameter_list|()
block|{
return|return
name|verbose
return|;
block|}
specifier|protected
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
specifier|protected
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
specifier|protected
name|URI
index|[]
name|getValidationServers
parameter_list|()
block|{
return|return
name|validationServers
return|;
block|}
specifier|protected
name|void
name|setValidationServers
parameter_list|(
name|String
name|servers
parameter_list|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|servers
argument_list|)
condition|)
block|{
name|String
index|[]
name|strServers
init|=
name|servers
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|this
operator|.
name|validationServers
operator|=
operator|new
name|URI
index|[
name|strServers
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|validationServers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|validationServers
index|[
name|i
index|]
operator|=
operator|new
name|Path
argument_list|(
name|strServers
index|[
name|i
index|]
argument_list|)
operator|.
name|toUri
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|String
name|getHiveUser
parameter_list|()
block|{
return|return
name|hiveUser
return|;
block|}
specifier|protected
name|void
name|setHiveUser
parameter_list|(
name|String
name|hiveUser
parameter_list|)
block|{
name|this
operator|.
name|hiveUser
operator|=
name|hiveUser
expr_stmt|;
block|}
specifier|protected
name|String
name|getHivePasswd
parameter_list|()
block|{
return|return
name|hivePasswd
return|;
block|}
specifier|protected
name|void
name|setHivePasswd
parameter_list|(
name|String
name|hivePasswd
parameter_list|)
block|{
name|this
operator|.
name|hivePasswd
operator|=
name|hivePasswd
expr_stmt|;
block|}
specifier|protected
name|String
name|getHiveDb
parameter_list|()
block|{
return|return
name|hiveDb
return|;
block|}
specifier|protected
name|void
name|setHiveDb
parameter_list|(
name|String
name|hiveDb
parameter_list|)
block|{
name|this
operator|.
name|hiveDb
operator|=
name|hiveDb
expr_stmt|;
block|}
specifier|protected
name|SchemaToolCommandLine
name|getCmdLine
parameter_list|()
block|{
return|return
name|cmdLine
return|;
block|}
specifier|public
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
name|url
argument_list|,
name|driver
argument_list|,
name|printInfo
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|protected
name|NestedScriptParser
name|getDbCommandParser
parameter_list|(
name|String
name|dbType
parameter_list|,
name|String
name|metaDbType
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
name|conf
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|protected
name|MetaStoreConnectionInfo
name|getConnectionInfo
parameter_list|(
name|boolean
name|printInfo
parameter_list|)
block|{
return|return
operator|new
name|MetaStoreConnectionInfo
argument_list|(
name|userName
argument_list|,
name|passWord
argument_list|,
name|url
argument_list|,
name|driver
argument_list|,
name|printInfo
argument_list|,
name|conf
argument_list|,
name|dbType
argument_list|,
name|hiveDb
argument_list|)
return|;
block|}
specifier|protected
name|IMetaStoreSchemaInfo
name|getMetaStoreSchemaInfo
parameter_list|()
block|{
return|return
name|metaStoreSchemaInfo
return|;
block|}
comment|/**    * check if the current schema version in metastore matches the Hive version    */
annotation|@
name|VisibleForTesting
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
name|metaStoreSchemaInfo
operator|.
name|getMetaStoreSchemaVersion
argument_list|(
name|getConnectionInfo
argument_list|(
literal|false
argument_list|)
argument_list|)
decl_stmt|;
comment|// verify that the new version is added to schema
name|assertCompatibleVersion
argument_list|(
name|metaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
argument_list|,
name|newSchemaVersion
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|assertCompatibleVersion
parameter_list|(
name|String
name|hiveSchemaVersion
parameter_list|,
name|String
name|dbSchemaVersion
parameter_list|)
throws|throws
name|HiveMetaException
block|{
if|if
condition|(
operator|!
name|metaStoreSchemaInfo
operator|.
name|isVersionCompatible
argument_list|(
name|hiveSchemaVersion
argument_list|,
name|dbSchemaVersion
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Metastore schema version is not compatible. Hive Version: "
operator|+
name|hiveSchemaVersion
operator|+
literal|", Database Schema Version: "
operator|+
name|dbSchemaVersion
argument_list|)
throw|;
block|}
block|}
comment|/***    * Execute a given metastore script. This default version uses sqlline to execute the files,    * which requires only running one file.  Subclasses can use other executors.    * @param scriptDir directory script is in    * @param scriptFile file in the directory to run    * @throws IOException if it cannot read the file or directory    * @throws HiveMetaException default implementation never throws this    */
specifier|protected
name|void
name|execSql
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
name|execSql
argument_list|(
name|scriptDir
operator|+
name|File
operator|.
name|separatorChar
operator|+
name|scriptFile
argument_list|)
expr_stmt|;
block|}
comment|// Generate the beeline args per hive conf and execute the given script
specifier|protected
name|void
name|execSql
parameter_list|(
name|String
name|sqlScriptFile
parameter_list|)
throws|throws
name|IOException
block|{
name|CommandBuilder
name|builder
init|=
operator|new
name|CommandBuilder
argument_list|(
name|conf
argument_list|,
name|url
argument_list|,
name|driver
argument_list|,
name|userName
argument_list|,
name|passWord
argument_list|,
name|sqlScriptFile
argument_list|)
decl_stmt|;
comment|// run the script using SqlLine
name|SqlLine
name|sqlLine
init|=
operator|new
name|SqlLine
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|outputForLog
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|verbose
condition|)
block|{
name|OutputStream
name|out
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|out
operator|=
name|outputForLog
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|out
operator|=
operator|new
name|NullOutputStream
argument_list|()
expr_stmt|;
block|}
name|sqlLine
operator|.
name|setOutputStream
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"sqlline.silent"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to run command<"
operator|+
name|builder
operator|.
name|buildToLog
argument_list|()
operator|+
literal|">"
argument_list|)
expr_stmt|;
name|SqlLine
operator|.
name|Status
name|status
init|=
name|sqlLine
operator|.
name|begin
argument_list|(
name|builder
operator|.
name|buildToRun
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
operator|&&
name|outputForLog
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Received following output from Sqlline:"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|outputForLog
operator|.
name|toString
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|status
operator|!=
name|SqlLine
operator|.
name|Status
operator|.
name|OK
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
comment|// test the connection metastore using the config property
specifier|protected
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
comment|// Quote if the database requires it
specifier|protected
name|String
name|quote
parameter_list|(
name|String
name|stmt
parameter_list|)
block|{
name|stmt
operator|=
name|stmt
operator|.
name|replace
argument_list|(
literal|"<q>"
argument_list|,
name|needsQuotedIdentifier
condition|?
name|quoteCharacter
else|:
literal|""
argument_list|)
expr_stmt|;
name|stmt
operator|=
name|stmt
operator|.
name|replace
argument_list|(
literal|"<qa>"
argument_list|,
name|quoteCharacter
argument_list|)
expr_stmt|;
return|return
name|stmt
return|;
block|}
specifier|protected
specifier|static
class|class
name|CommandBuilder
block|{
specifier|protected
specifier|final
name|String
name|userName
decl_stmt|;
specifier|protected
specifier|final
name|String
name|password
decl_stmt|;
specifier|protected
specifier|final
name|String
name|sqlScriptFile
decl_stmt|;
specifier|protected
specifier|final
name|String
name|driver
decl_stmt|;
specifier|protected
specifier|final
name|String
name|url
decl_stmt|;
specifier|protected
name|CommandBuilder
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|url
parameter_list|,
name|String
name|driver
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|password
parameter_list|,
name|String
name|sqlScriptFile
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|userName
operator|=
name|userName
expr_stmt|;
name|this
operator|.
name|password
operator|=
name|password
expr_stmt|;
name|this
operator|.
name|url
operator|=
name|url
operator|==
literal|null
condition|?
name|HiveSchemaHelper
operator|.
name|getValidConfVar
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECT_URL_KEY
argument_list|,
name|conf
argument_list|)
else|:
name|url
expr_stmt|;
name|this
operator|.
name|driver
operator|=
name|driver
operator|==
literal|null
condition|?
name|HiveSchemaHelper
operator|.
name|getValidConfVar
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECTION_DRIVER
argument_list|,
name|conf
argument_list|)
else|:
name|driver
expr_stmt|;
name|this
operator|.
name|sqlScriptFile
operator|=
name|sqlScriptFile
expr_stmt|;
block|}
specifier|public
name|String
index|[]
name|buildToRun
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|argsWith
argument_list|(
name|password
argument_list|)
return|;
block|}
specifier|public
name|String
name|buildToLog
parameter_list|()
throws|throws
name|IOException
block|{
name|logScript
argument_list|()
expr_stmt|;
return|return
name|StringUtils
operator|.
name|join
argument_list|(
name|argsWith
argument_list|(
name|PASSWD_MASK
argument_list|)
argument_list|,
literal|" "
argument_list|)
return|;
block|}
specifier|protected
name|String
index|[]
name|argsWith
parameter_list|(
name|String
name|password
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
name|url
block|,
literal|"-d"
block|,
name|driver
block|,
literal|"-n"
block|,
name|userName
block|,
literal|"-p"
block|,
name|password
block|,
literal|"--isolation=TRANSACTION_READ_COMMITTED"
block|,
literal|"-f"
block|,
name|sqlScriptFile
block|}
return|;
block|}
specifier|private
name|void
name|logScript
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Going to invoke file that contains:"
argument_list|)
expr_stmt|;
try|try
init|(
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|FileReader
argument_list|(
name|sqlScriptFile
argument_list|)
argument_list|)
init|)
block|{
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"script: "
operator|+
name|line
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|// Create the required command line options
specifier|private
specifier|static
name|void
name|logAndPrintToError
parameter_list|(
name|String
name|errmsg
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|errmsg
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|errmsg
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
name|MetastoreSchemaTool
name|tool
init|=
operator|new
name|MetastoreSchemaTool
argument_list|()
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|tool
operator|.
name|run
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
return|return
name|run
argument_list|(
name|findHomeDir
argument_list|()
argument_list|,
name|args
argument_list|,
literal|null
argument_list|,
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|int
name|run
parameter_list|(
name|String
name|metastoreHome
parameter_list|,
name|String
index|[]
name|args
parameter_list|,
name|OptionGroup
name|additionalOptions
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
try|try
block|{
name|init
argument_list|(
name|metastoreHome
argument_list|,
name|args
argument_list|,
name|additionalOptions
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|SchemaToolTask
name|task
decl_stmt|;
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"info"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskInfo
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"upgradeSchema"
argument_list|)
operator|||
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"upgradeSchemaFrom"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskUpgrade
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"initSchema"
argument_list|)
operator|||
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"initSchemaTo"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskInit
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"initOrUpgradeSchema"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskInitOrUpgrade
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"validate"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskValidate
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"createCatalog"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskCreateCatalog
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"alterCatalog"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskAlterCatalog
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"moveDatabase"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskMoveDatabase
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"moveTable"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskMoveTable
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"createUser"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskCreateUser
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
literal|"dropAllDatabases"
argument_list|)
condition|)
block|{
name|task
operator|=
operator|new
name|SchemaToolTaskDrop
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"No task defined!"
argument_list|)
throw|;
block|}
name|task
operator|.
name|setHiveSchemaTool
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|task
operator|.
name|setCommandLineArguments
argument_list|(
name|cmdLine
argument_list|)
expr_stmt|;
name|task
operator|.
name|execute
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
catch|catch
parameter_list|(
name|HiveMetaException
name|e
parameter_list|)
block|{
name|logAndPrintToError
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Throwable
name|t
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|logAndPrintToError
argument_list|(
literal|"Underlying cause: "
operator|+
name|t
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" : "
operator|+
name|t
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|SQLException
condition|)
block|{
name|logAndPrintToError
argument_list|(
literal|"SQL Error code: "
operator|+
operator|(
operator|(
name|SQLException
operator|)
name|t
operator|)
operator|.
name|getErrorCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|cmdLine
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|cmdLine
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
else|else
block|{
name|logAndPrintToError
argument_list|(
literal|"Use --verbose for detailed stacktrace."
argument_list|)
expr_stmt|;
block|}
block|}
name|logAndPrintToError
argument_list|(
literal|"*** schemaTool failed ***"
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
block|}
end_class

end_unit

