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
name|hive
operator|.
name|beeline
operator|.
name|schematool
package|;
end_package

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
name|tools
operator|.
name|HiveSchemaHelper
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
name|HiveSchemaHelper
operator|.
name|NestedScriptParser
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
name|MetastoreSchemaTool
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
name|BeeLine
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

begin_class
specifier|public
class|class
name|HiveSchemaTool
extends|extends
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
name|HiveSchemaTool
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
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
name|metaDbType
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|metaDbType
argument_list|)
return|;
block|}
comment|/***    * Run beeline with the given metastore script. Flatten the nested scripts    * into single file.    */
annotation|@
name|Override
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
name|NestedScriptParser
name|dbCommandParser
init|=
name|getDbCommandParser
argument_list|(
name|dbType
argument_list|,
name|metaDbType
argument_list|)
decl_stmt|;
comment|// expand the nested script
comment|// If the metaDbType is set, this is setting up the information
comment|// schema in Hive. That specifically means that the sql commands need
comment|// to be adjusted for the underlying RDBMS (correct quotation
comment|// strings, etc).
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
argument_list|,
name|metaDbType
operator|!=
literal|null
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
name|execSql
argument_list|(
name|tmpFile
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|HiveSchemaToolCommandBuilder
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
comment|// run the script using Beeline
try|try
init|(
name|BeeLine
name|beeLine
init|=
operator|new
name|BeeLine
argument_list|()
init|)
block|{
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
name|LOG
operator|.
name|debug
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
name|int
name|status
init|=
name|beeLine
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
block|}
specifier|static
class|class
name|HiveSchemaToolCommandBuilder
extends|extends
name|MetastoreSchemaTool
operator|.
name|CommandBuilder
block|{
name|HiveSchemaToolCommandBuilder
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
name|super
argument_list|(
name|conf
argument_list|,
name|url
argument_list|,
name|driver
argument_list|,
name|userName
argument_list|,
name|password
argument_list|,
name|sqlScriptFile
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
index|[]
name|argsWith
parameter_list|(
name|String
name|password
parameter_list|)
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
literal|"-f"
block|,
name|sqlScriptFile
block|}
return|;
block|}
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
name|HiveSchemaTool
argument_list|()
decl_stmt|;
name|OptionGroup
name|additionalGroup
init|=
operator|new
name|OptionGroup
argument_list|()
decl_stmt|;
name|Option
name|metaDbTypeOpt
init|=
name|OptionBuilder
operator|.
name|withArgName
argument_list|(
literal|"metaDatabaseType"
argument_list|)
operator|.
name|hasArgs
argument_list|()
operator|.
name|withDescription
argument_list|(
literal|"Used only if upgrading the system catalog for hive"
argument_list|)
operator|.
name|create
argument_list|(
literal|"metaDbType"
argument_list|)
decl_stmt|;
name|additionalGroup
operator|.
name|addOption
argument_list|(
name|metaDbTypeOpt
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|SCHEMA_VERIFICATION
operator|.
name|getVarname
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|tool
operator|.
name|run
argument_list|(
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_HOME"
argument_list|)
argument_list|,
name|args
argument_list|,
name|additionalGroup
argument_list|,
operator|new
name|HiveConf
argument_list|(
name|HiveSchemaTool
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

