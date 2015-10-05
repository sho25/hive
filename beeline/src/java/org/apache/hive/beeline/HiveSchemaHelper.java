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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|DriverManager
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
name|util
operator|.
name|IllegalFormatException
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

begin_class
specifier|public
class|class
name|HiveSchemaHelper
block|{
specifier|public
specifier|static
specifier|final
name|String
name|DB_DERBY
init|=
literal|"derby"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DB_MSSQL
init|=
literal|"mssql"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DB_MYSQL
init|=
literal|"mysql"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DB_POSTGRACE
init|=
literal|"postgres"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DB_ORACLE
init|=
literal|"oracle"
decl_stmt|;
comment|/***    * Get JDBC connection to metastore db    *    * @param userName metastore connection username    * @param password metastore connection password    * @param printInfo print connection parameters    * @param hiveConf hive config object    * @return metastore connection object    * @throws org.apache.hadoop.hive.metastore.api.MetaException    */
specifier|public
specifier|static
name|Connection
name|getConnectionToMetastore
parameter_list|(
name|String
name|userName
parameter_list|,
name|String
name|password
parameter_list|,
name|boolean
name|printInfo
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|HiveMetaException
block|{
try|try
block|{
name|String
name|connectionURL
init|=
name|getValidConfVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
argument_list|,
name|hiveConf
argument_list|)
decl_stmt|;
name|String
name|driver
init|=
name|getValidConfVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_DRIVER
argument_list|,
name|hiveConf
argument_list|)
decl_stmt|;
if|if
condition|(
name|printInfo
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Metastore connection URL:\t "
operator|+
name|connectionURL
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Metastore Connection Driver :\t "
operator|+
name|driver
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Metastore connection User:\t "
operator|+
name|userName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|userName
operator|==
literal|null
operator|)
operator|||
name|userName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"UserName empty "
argument_list|)
throw|;
block|}
comment|// load required JDBC driver
name|Class
operator|.
name|forName
argument_list|(
name|driver
argument_list|)
expr_stmt|;
comment|// Connect using the JDBC URL and user/pass from conf
return|return
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|connectionURL
argument_list|,
name|userName
argument_list|,
name|password
argument_list|)
return|;
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
literal|"Failed to get schema version."
argument_list|,
name|e
argument_list|)
throw|;
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
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Failed to load driver"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|String
name|getValidConfVar
parameter_list|(
name|HiveConf
operator|.
name|ConfVars
name|confVar
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|confVarStr
init|=
name|hiveConf
operator|.
name|get
argument_list|(
name|confVar
operator|.
name|varname
argument_list|)
decl_stmt|;
if|if
condition|(
name|confVarStr
operator|==
literal|null
operator|||
name|confVarStr
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Empty "
operator|+
name|confVar
operator|.
name|varname
argument_list|)
throw|;
block|}
return|return
name|confVarStr
return|;
block|}
specifier|public
interface|interface
name|NestedScriptParser
block|{
specifier|public
enum|enum
name|CommandType
block|{
name|PARTIAL_STATEMENT
block|,
name|TERMINATED_STATEMENT
block|,
name|COMMENT
block|}
specifier|static
specifier|final
name|String
name|DEFAUTL_DELIMITER
init|=
literal|";"
decl_stmt|;
comment|/**      * Find the type of given command      *      * @param dbCommand      * @return      */
specifier|public
name|boolean
name|isPartialCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
throws|throws
name|IllegalArgumentException
function_decl|;
comment|/**      * Parse the DB specific nesting format and extract the inner script name if any      *      * @param dbCommand command from parent script      * @return      * @throws IllegalFormatException      */
specifier|public
name|String
name|getScriptName
parameter_list|(
name|String
name|dbCommand
parameter_list|)
throws|throws
name|IllegalArgumentException
function_decl|;
comment|/**      * Find if the given command is a nested script execution      *      * @param dbCommand      * @return      */
specifier|public
name|boolean
name|isNestedScript
parameter_list|(
name|String
name|dbCommand
parameter_list|)
function_decl|;
comment|/**      * Find if the given command should not be passed to DB      *      * @param dbCommand      * @return      */
specifier|public
name|boolean
name|isNonExecCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
function_decl|;
comment|/**      * Get the SQL statement delimiter      *      * @return      */
specifier|public
name|String
name|getDelimiter
parameter_list|()
function_decl|;
comment|/**      * Clear any client specific tags      *      * @return      */
specifier|public
name|String
name|cleanseCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
function_decl|;
comment|/**      * Does the DB required table/column names quoted      *      * @return      */
specifier|public
name|boolean
name|needsQuotedIdentifier
parameter_list|()
function_decl|;
comment|/**      * Flatten the nested upgrade script into a buffer      *      * @param scriptDir  upgrade script directory      * @param scriptFile upgrade script file      * @return string of sql commands      */
specifier|public
name|String
name|buildCommand
parameter_list|(
name|String
name|scriptDir
parameter_list|,
name|String
name|scriptFile
parameter_list|)
throws|throws
name|IllegalFormatException
throws|,
name|IOException
function_decl|;
block|}
comment|/***    * Base implementation of NestedScriptParser    * abstractCommandParser.    *    */
specifier|private
specifier|static
specifier|abstract
class|class
name|AbstractCommandParser
implements|implements
name|NestedScriptParser
block|{
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|dbOpts
decl_stmt|;
specifier|private
name|String
name|msUsername
decl_stmt|;
specifier|private
name|String
name|msPassword
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|public
name|AbstractCommandParser
parameter_list|(
name|String
name|dbOpts
parameter_list|,
name|String
name|msUsername
parameter_list|,
name|String
name|msPassword
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|setDbOpts
argument_list|(
name|dbOpts
argument_list|)
expr_stmt|;
name|this
operator|.
name|msUsername
operator|=
name|msUsername
expr_stmt|;
name|this
operator|.
name|msPassword
operator|=
name|msPassword
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isPartialCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|dbCommand
operator|==
literal|null
operator|||
name|dbCommand
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid command line "
operator|+
name|dbCommand
argument_list|)
throw|;
block|}
name|dbCommand
operator|=
name|dbCommand
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|dbCommand
operator|.
name|endsWith
argument_list|(
name|getDelimiter
argument_list|()
argument_list|)
operator|||
name|isNonExecCommand
argument_list|(
name|dbCommand
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNonExecCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
return|return
operator|(
name|dbCommand
operator|.
name|startsWith
argument_list|(
literal|"--"
argument_list|)
operator|||
name|dbCommand
operator|.
name|startsWith
argument_list|(
literal|"#"
argument_list|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDelimiter
parameter_list|()
block|{
return|return
name|DEFAUTL_DELIMITER
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|cleanseCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
comment|// strip off the delimiter
if|if
condition|(
name|dbCommand
operator|.
name|endsWith
argument_list|(
name|getDelimiter
argument_list|()
argument_list|)
condition|)
block|{
name|dbCommand
operator|=
name|dbCommand
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|dbCommand
operator|.
name|length
argument_list|()
operator|-
name|getDelimiter
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|dbCommand
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsQuotedIdentifier
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|buildCommand
parameter_list|(
name|String
name|scriptDir
parameter_list|,
name|String
name|scriptFile
parameter_list|)
throws|throws
name|IllegalFormatException
throws|,
name|IOException
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
name|scriptDir
operator|+
name|File
operator|.
name|separatorChar
operator|+
name|scriptFile
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|currLine
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|String
name|currentCommand
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|currLine
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
name|currLine
operator|=
name|currLine
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|currLine
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
comment|// skip empty lines
block|}
if|if
condition|(
name|currentCommand
operator|==
literal|null
condition|)
block|{
name|currentCommand
operator|=
name|currLine
expr_stmt|;
block|}
else|else
block|{
name|currentCommand
operator|=
name|currentCommand
operator|+
literal|" "
operator|+
name|currLine
expr_stmt|;
block|}
if|if
condition|(
name|isPartialCommand
argument_list|(
name|currLine
argument_list|)
condition|)
block|{
comment|// if its a partial line, continue collecting the pieces
continue|continue;
block|}
comment|// if this is a valid executable command then add it to the buffer
if|if
condition|(
operator|!
name|isNonExecCommand
argument_list|(
name|currentCommand
argument_list|)
condition|)
block|{
name|currentCommand
operator|=
name|cleanseCommand
argument_list|(
name|currentCommand
argument_list|)
expr_stmt|;
if|if
condition|(
name|isNestedScript
argument_list|(
name|currentCommand
argument_list|)
condition|)
block|{
comment|// if this is a nested sql script then flatten it
name|String
name|currScript
init|=
name|getScriptName
argument_list|(
name|currentCommand
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|buildCommand
argument_list|(
name|scriptDir
argument_list|,
name|currScript
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Now we have a complete statement, process it
comment|// write the line to buffer
name|sb
operator|.
name|append
argument_list|(
name|currentCommand
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|currentCommand
operator|=
literal|null
expr_stmt|;
block|}
name|bfReader
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|setDbOpts
parameter_list|(
name|String
name|dbOpts
parameter_list|)
block|{
if|if
condition|(
name|dbOpts
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|dbOpts
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|dbOpts
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|dbOpts
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|getDbOpts
parameter_list|()
block|{
return|return
name|dbOpts
return|;
block|}
specifier|protected
name|String
name|getMsUsername
parameter_list|()
block|{
return|return
name|msUsername
return|;
block|}
specifier|protected
name|String
name|getMsPassword
parameter_list|()
block|{
return|return
name|msPassword
return|;
block|}
specifier|protected
name|HiveConf
name|getHiveConf
parameter_list|()
block|{
return|return
name|hiveConf
return|;
block|}
block|}
comment|// Derby commandline parser
specifier|public
specifier|static
class|class
name|DerbyCommandParser
extends|extends
name|AbstractCommandParser
block|{
specifier|private
specifier|static
name|String
name|DERBY_NESTING_TOKEN
init|=
literal|"RUN"
decl_stmt|;
specifier|public
name|DerbyCommandParser
parameter_list|(
name|String
name|dbOpts
parameter_list|,
name|String
name|msUsername
parameter_list|,
name|String
name|msPassword
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|super
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getScriptName
parameter_list|(
name|String
name|dbCommand
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
operator|!
name|isNestedScript
argument_list|(
name|dbCommand
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a script format "
operator|+
name|dbCommand
argument_list|)
throw|;
block|}
name|String
index|[]
name|tokens
init|=
name|dbCommand
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokens
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Couldn't parse line "
operator|+
name|dbCommand
argument_list|)
throw|;
block|}
return|return
name|tokens
index|[
literal|1
index|]
operator|.
name|replace
argument_list|(
literal|";"
argument_list|,
literal|""
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"'"
argument_list|,
literal|""
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNestedScript
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
comment|// Derby script format is RUN '<file>'
return|return
name|dbCommand
operator|.
name|startsWith
argument_list|(
name|DERBY_NESTING_TOKEN
argument_list|)
return|;
block|}
block|}
comment|// MySQL parser
specifier|public
specifier|static
class|class
name|MySqlCommandParser
extends|extends
name|AbstractCommandParser
block|{
specifier|private
specifier|static
specifier|final
name|String
name|MYSQL_NESTING_TOKEN
init|=
literal|"SOURCE"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DELIMITER_TOKEN
init|=
literal|"DELIMITER"
decl_stmt|;
specifier|private
name|String
name|delimiter
init|=
name|DEFAUTL_DELIMITER
decl_stmt|;
specifier|public
name|MySqlCommandParser
parameter_list|(
name|String
name|dbOpts
parameter_list|,
name|String
name|msUsername
parameter_list|,
name|String
name|msPassword
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|super
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isPartialCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|boolean
name|isPartial
init|=
name|super
operator|.
name|isPartialCommand
argument_list|(
name|dbCommand
argument_list|)
decl_stmt|;
comment|// if this is a delimiter directive, reset our delimiter
if|if
condition|(
name|dbCommand
operator|.
name|startsWith
argument_list|(
name|DELIMITER_TOKEN
argument_list|)
condition|)
block|{
name|String
index|[]
name|tokens
init|=
name|dbCommand
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokens
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Couldn't parse line "
operator|+
name|dbCommand
argument_list|)
throw|;
block|}
name|delimiter
operator|=
name|tokens
index|[
literal|1
index|]
expr_stmt|;
block|}
return|return
name|isPartial
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getScriptName
parameter_list|(
name|String
name|dbCommand
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|String
index|[]
name|tokens
init|=
name|dbCommand
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokens
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Couldn't parse line "
operator|+
name|dbCommand
argument_list|)
throw|;
block|}
comment|// remove ending ';'
return|return
name|tokens
index|[
literal|1
index|]
operator|.
name|replace
argument_list|(
literal|";"
argument_list|,
literal|""
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNestedScript
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
return|return
name|dbCommand
operator|.
name|startsWith
argument_list|(
name|MYSQL_NESTING_TOKEN
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDelimiter
parameter_list|()
block|{
return|return
name|delimiter
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNonExecCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
return|return
name|super
operator|.
name|isNonExecCommand
argument_list|(
name|dbCommand
argument_list|)
operator|||
operator|(
name|dbCommand
operator|.
name|startsWith
argument_list|(
literal|"/*"
argument_list|)
operator|&&
name|dbCommand
operator|.
name|endsWith
argument_list|(
literal|"*/"
argument_list|)
operator|)
operator|||
name|dbCommand
operator|.
name|startsWith
argument_list|(
name|DELIMITER_TOKEN
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|cleanseCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
return|return
name|super
operator|.
name|cleanseCommand
argument_list|(
name|dbCommand
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"/\\*.*?\\*/[^;]"
argument_list|,
literal|""
argument_list|)
return|;
block|}
block|}
comment|// Postgres specific parser
specifier|public
specifier|static
class|class
name|PostgresCommandParser
extends|extends
name|AbstractCommandParser
block|{
specifier|private
specifier|static
name|String
name|POSTGRES_NESTING_TOKEN
init|=
literal|"\\i"
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|String
name|POSTGRES_STANDARD_STRINGS_OPT
init|=
literal|"SET standard_conforming_strings"
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|String
name|POSTGRES_SKIP_STANDARD_STRINGS_DBOPT
init|=
literal|"postgres.filter.81"
decl_stmt|;
specifier|public
name|PostgresCommandParser
parameter_list|(
name|String
name|dbOpts
parameter_list|,
name|String
name|msUsername
parameter_list|,
name|String
name|msPassword
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|super
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getScriptName
parameter_list|(
name|String
name|dbCommand
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|String
index|[]
name|tokens
init|=
name|dbCommand
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokens
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Couldn't parse line "
operator|+
name|dbCommand
argument_list|)
throw|;
block|}
comment|// remove ending ';'
return|return
name|tokens
index|[
literal|1
index|]
operator|.
name|replace
argument_list|(
literal|";"
argument_list|,
literal|""
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNestedScript
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
return|return
name|dbCommand
operator|.
name|startsWith
argument_list|(
name|POSTGRES_NESTING_TOKEN
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsQuotedIdentifier
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNonExecCommand
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
comment|// Skip "standard_conforming_strings" command which is read-only in older
comment|// Postgres versions like 8.1
comment|// See: http://www.postgresql.org/docs/8.2/static/release-8-1.html
if|if
condition|(
name|getDbOpts
argument_list|()
operator|.
name|contains
argument_list|(
name|POSTGRES_SKIP_STANDARD_STRINGS_DBOPT
argument_list|)
condition|)
block|{
if|if
condition|(
name|dbCommand
operator|.
name|startsWith
argument_list|(
name|POSTGRES_STANDARD_STRINGS_OPT
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
name|super
operator|.
name|isNonExecCommand
argument_list|(
name|dbCommand
argument_list|)
return|;
block|}
block|}
comment|//Oracle specific parser
specifier|public
specifier|static
class|class
name|OracleCommandParser
extends|extends
name|AbstractCommandParser
block|{
specifier|private
specifier|static
name|String
name|ORACLE_NESTING_TOKEN
init|=
literal|"@"
decl_stmt|;
specifier|public
name|OracleCommandParser
parameter_list|(
name|String
name|dbOpts
parameter_list|,
name|String
name|msUsername
parameter_list|,
name|String
name|msPassword
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|super
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getScriptName
parameter_list|(
name|String
name|dbCommand
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
operator|!
name|isNestedScript
argument_list|(
name|dbCommand
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a nested script format "
operator|+
name|dbCommand
argument_list|)
throw|;
block|}
comment|// remove ending ';' and starting '@'
return|return
name|dbCommand
operator|.
name|replace
argument_list|(
literal|";"
argument_list|,
literal|""
argument_list|)
operator|.
name|replace
argument_list|(
name|ORACLE_NESTING_TOKEN
argument_list|,
literal|""
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNestedScript
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
return|return
name|dbCommand
operator|.
name|startsWith
argument_list|(
name|ORACLE_NESTING_TOKEN
argument_list|)
return|;
block|}
block|}
comment|//MSSQL specific parser
specifier|public
specifier|static
class|class
name|MSSQLCommandParser
extends|extends
name|AbstractCommandParser
block|{
specifier|private
specifier|static
name|String
name|MSSQL_NESTING_TOKEN
init|=
literal|":r"
decl_stmt|;
specifier|public
name|MSSQLCommandParser
parameter_list|(
name|String
name|dbOpts
parameter_list|,
name|String
name|msUsername
parameter_list|,
name|String
name|msPassword
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|super
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getScriptName
parameter_list|(
name|String
name|dbCommand
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|String
index|[]
name|tokens
init|=
name|dbCommand
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokens
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Couldn't parse line "
operator|+
name|dbCommand
argument_list|)
throw|;
block|}
return|return
name|tokens
index|[
literal|1
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNestedScript
parameter_list|(
name|String
name|dbCommand
parameter_list|)
block|{
return|return
name|dbCommand
operator|.
name|startsWith
argument_list|(
name|MSSQL_NESTING_TOKEN
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|NestedScriptParser
name|getDbCommandParser
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
return|return
name|getDbCommandParser
argument_list|(
name|dbName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|NestedScriptParser
name|getDbCommandParser
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|dbOpts
parameter_list|,
name|String
name|msUsername
parameter_list|,
name|String
name|msPassword
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
if|if
condition|(
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|DB_DERBY
argument_list|)
condition|)
block|{
return|return
operator|new
name|DerbyCommandParser
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|DB_MSSQL
argument_list|)
condition|)
block|{
return|return
operator|new
name|MSSQLCommandParser
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|DB_MYSQL
argument_list|)
condition|)
block|{
return|return
operator|new
name|MySqlCommandParser
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|DB_POSTGRACE
argument_list|)
condition|)
block|{
return|return
operator|new
name|PostgresCommandParser
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|DB_ORACLE
argument_list|)
condition|)
block|{
return|return
operator|new
name|OracleCommandParser
argument_list|(
name|dbOpts
argument_list|,
name|msUsername
argument_list|,
name|msPassword
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown dbType "
operator|+
name|dbName
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

