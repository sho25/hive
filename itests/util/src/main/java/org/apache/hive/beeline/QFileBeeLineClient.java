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
name|lang3
operator|.
name|ArrayUtils
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
name|QTestUtil
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
name|dataset
operator|.
name|QTestDatasetHandler
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
name|ConvertedOutputFile
operator|.
name|Converter
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
name|DatabaseMetaData
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
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  * QFile test client using BeeLine. It can be used to submit a list of command strings, or a QFile.  */
end_comment

begin_class
specifier|public
class|class
name|QFileBeeLineClient
implements|implements
name|AutoCloseable
block|{
specifier|private
name|BeeLine
name|beeLine
decl_stmt|;
specifier|private
name|PrintStream
name|beelineOutputStream
decl_stmt|;
specifier|private
name|File
name|logFile
decl_stmt|;
specifier|private
name|String
index|[]
name|TEST_FIRST_COMMANDS
init|=
operator|new
name|String
index|[]
block|{
literal|"!set outputformat tsv2"
block|,
literal|"!set verbose false"
block|,
literal|"!set silent true"
block|,
literal|"!set showheader false"
block|,
literal|"!set escapeCRLF false"
block|,
literal|"USE default;"
block|,
literal|"SHOW TABLES;"
block|,   }
decl_stmt|;
specifier|private
name|String
index|[]
name|TEST_SET_LOG_COMMANDS
init|=
operator|new
name|String
index|[]
block|{
literal|"set hive.testing.short.logs=true;"
block|,
literal|"set hive.testing.remove.logs=false;"
block|,   }
decl_stmt|;
specifier|private
name|String
index|[]
name|TEST_RESET_COMMANDS
init|=
operator|new
name|String
index|[]
block|{
literal|"set hive.testing.short.logs=false;"
block|,
literal|"!set verbose true"
block|,
literal|"!set silent false"
block|,
literal|"!set showheader true"
block|,
literal|"!set escapeCRLF false"
block|,
literal|"!set outputformat table"
block|,
literal|"USE default;"
block|}
decl_stmt|;
specifier|protected
name|QFileBeeLineClient
parameter_list|(
name|String
name|jdbcUrl
parameter_list|,
name|String
name|jdbcDriver
parameter_list|,
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|File
name|log
parameter_list|)
throws|throws
name|IOException
block|{
name|logFile
operator|=
name|log
expr_stmt|;
name|beeLine
operator|=
operator|new
name|BeeLine
argument_list|()
expr_stmt|;
name|beelineOutputStream
operator|=
operator|new
name|PrintStream
argument_list|(
name|logFile
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|setOutputStream
argument_list|(
name|beelineOutputStream
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|setErrorStream
argument_list|(
name|beelineOutputStream
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!set verbose true"
block|,
literal|"!set shownestederrs true"
block|,
literal|"!set showwarnings true"
block|,
literal|"!set showelapsedtime false"
block|,
literal|"!set trimscripts false"
block|,
literal|"!set maxwidth -1"
block|,
literal|"!connect "
operator|+
name|jdbcUrl
operator|+
literal|" "
operator|+
name|username
operator|+
literal|" "
operator|+
name|password
operator|+
literal|" "
operator|+
name|jdbcDriver
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getDatabases
parameter_list|()
throws|throws
name|SQLException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|databases
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|DatabaseMetaData
name|metaData
init|=
name|beeLine
operator|.
name|getDatabaseMetaData
argument_list|()
decl_stmt|;
comment|// Get the databases
try|try
init|(
name|ResultSet
name|schemasResultSet
init|=
name|metaData
operator|.
name|getSchemas
argument_list|()
init|)
block|{
while|while
condition|(
name|schemasResultSet
operator|.
name|next
argument_list|()
condition|)
block|{
name|databases
operator|.
name|add
argument_list|(
name|schemasResultSet
operator|.
name|getString
argument_list|(
literal|"TABLE_SCHEM"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|databases
return|;
block|}
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getTables
parameter_list|()
throws|throws
name|SQLException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|tables
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|DatabaseMetaData
name|metaData
init|=
name|beeLine
operator|.
name|getDatabaseMetaData
argument_list|()
decl_stmt|;
comment|// Get the tables in the default database
name|String
index|[]
name|types
init|=
operator|new
name|String
index|[]
block|{
literal|"TABLE"
block|}
decl_stmt|;
try|try
init|(
name|ResultSet
name|tablesResultSet
init|=
name|metaData
operator|.
name|getTables
argument_list|(
literal|null
argument_list|,
literal|"default"
argument_list|,
literal|"%"
argument_list|,
name|types
argument_list|)
init|)
block|{
while|while
condition|(
name|tablesResultSet
operator|.
name|next
argument_list|()
condition|)
block|{
name|tables
operator|.
name|add
argument_list|(
name|tablesResultSet
operator|.
name|getString
argument_list|(
literal|"TABLE_NAME"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|tables
return|;
block|}
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getViews
parameter_list|()
throws|throws
name|SQLException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|views
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|DatabaseMetaData
name|metaData
init|=
name|beeLine
operator|.
name|getDatabaseMetaData
argument_list|()
decl_stmt|;
comment|// Get the tables in the default database
name|String
index|[]
name|types
init|=
operator|new
name|String
index|[]
block|{
literal|"VIEW"
block|}
decl_stmt|;
try|try
init|(
name|ResultSet
name|tablesResultSet
init|=
name|metaData
operator|.
name|getTables
argument_list|(
literal|null
argument_list|,
literal|"default"
argument_list|,
literal|"%"
argument_list|,
name|types
argument_list|)
init|)
block|{
while|while
condition|(
name|tablesResultSet
operator|.
name|next
argument_list|()
condition|)
block|{
name|views
operator|.
name|add
argument_list|(
name|tablesResultSet
operator|.
name|getString
argument_list|(
literal|"TABLE_NAME"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|views
return|;
block|}
specifier|public
name|void
name|execute
parameter_list|(
name|String
index|[]
name|commands
parameter_list|,
name|File
name|resultFile
parameter_list|,
name|Converter
name|converter
parameter_list|)
throws|throws
name|Exception
block|{
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!record "
operator|+
name|resultFile
operator|.
name|getAbsolutePath
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|setRecordOutputFile
argument_list|(
operator|new
name|ConvertedOutputFile
argument_list|(
name|beeLine
operator|.
name|getRecordOutputFile
argument_list|()
argument_list|,
name|converter
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|lastSuccessfulCommand
init|=
name|beeLine
operator|.
name|runCommands
argument_list|(
name|commands
argument_list|)
decl_stmt|;
if|if
condition|(
name|commands
operator|.
name|length
operator|!=
name|lastSuccessfulCommand
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Error executing SQL command: "
operator|+
name|commands
index|[
name|lastSuccessfulCommand
index|]
argument_list|)
throw|;
block|}
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!record"
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|beforeExecute
parameter_list|(
name|QFile
name|qFile
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|commands
init|=
name|TEST_FIRST_COMMANDS
decl_stmt|;
name|String
index|[]
name|extraCommands
decl_stmt|;
if|if
condition|(
name|qFile
operator|.
name|isUseSharedDatabase
argument_list|()
condition|)
block|{
comment|// If we are using a shared database, then remove not known databases, tables, views.
name|Set
argument_list|<
name|String
argument_list|>
name|dropCommands
init|=
name|getDatabases
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|database
lambda|->
operator|!
name|database
operator|.
name|equals
argument_list|(
literal|"default"
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|database
lambda|->
literal|"DROP DATABASE `"
operator|+
name|database
operator|+
literal|"` CASCADE;"
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|srcTables
init|=
name|QTestDatasetHandler
operator|.
name|getSrcTables
argument_list|()
decl_stmt|;
name|dropCommands
operator|.
name|addAll
argument_list|(
name|getTables
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|table
lambda|->
operator|!
name|srcTables
operator|.
name|contains
argument_list|(
name|table
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|table
lambda|->
literal|"DROP TABLE `"
operator|+
name|table
operator|+
literal|"` PURGE;"
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|dropCommands
operator|.
name|addAll
argument_list|(
name|getViews
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|view
lambda|->
literal|"DROP VIEW `"
operator|+
name|view
operator|+
literal|"`;"
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|extraCommands
operator|=
name|dropCommands
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// If we are using a test specific database, then we just drop the database, and recreate
name|extraCommands
operator|=
operator|new
name|String
index|[]
block|{
literal|"DROP DATABASE IF EXISTS `"
operator|+
name|qFile
operator|.
name|getDatabaseName
argument_list|()
operator|+
literal|"` CASCADE;"
block|,
literal|"CREATE DATABASE `"
operator|+
name|qFile
operator|.
name|getDatabaseName
argument_list|()
operator|+
literal|"`;"
block|,
literal|"USE `"
operator|+
name|qFile
operator|.
name|getDatabaseName
argument_list|()
operator|+
literal|"`;"
block|}
expr_stmt|;
block|}
name|commands
operator|=
name|ArrayUtils
operator|.
name|addAll
argument_list|(
name|commands
argument_list|,
name|extraCommands
argument_list|)
expr_stmt|;
name|commands
operator|=
name|ArrayUtils
operator|.
name|addAll
argument_list|(
name|commands
argument_list|,
name|TEST_SET_LOG_COMMANDS
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|commands
argument_list|,
name|qFile
operator|.
name|getBeforeExecuteLogFile
argument_list|()
argument_list|,
name|Converter
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|beeLine
operator|.
name|setIsTestMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|afterExecute
parameter_list|(
name|QFile
name|qFile
parameter_list|)
throws|throws
name|Exception
block|{
name|beeLine
operator|.
name|setIsTestMode
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|String
index|[]
name|commands
init|=
name|TEST_RESET_COMMANDS
decl_stmt|;
if|if
condition|(
operator|!
name|qFile
operator|.
name|isUseSharedDatabase
argument_list|()
condition|)
block|{
comment|// If we are using a test specific database, then we just drop the database
name|String
index|[]
name|extraCommands
init|=
operator|new
name|String
index|[]
block|{
literal|"DROP DATABASE IF EXISTS `"
operator|+
name|qFile
operator|.
name|getDatabaseName
argument_list|()
operator|+
literal|"` CASCADE;"
block|}
decl_stmt|;
name|commands
operator|=
name|ArrayUtils
operator|.
name|addAll
argument_list|(
name|commands
argument_list|,
name|extraCommands
argument_list|)
expr_stmt|;
block|}
name|execute
argument_list|(
name|commands
argument_list|,
name|qFile
operator|.
name|getAfterExecuteLogFile
argument_list|()
argument_list|,
name|Converter
operator|.
name|NONE
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|execute
parameter_list|(
name|QFile
name|qFile
parameter_list|)
throws|throws
name|Exception
block|{
name|execute
argument_list|(
name|qFile
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|execute
parameter_list|(
name|QFile
name|qFile
parameter_list|,
name|List
argument_list|<
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|>
name|preCommands
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|preCommands
operator|==
literal|null
condition|)
block|{
name|preCommands
operator|=
operator|new
name|ArrayList
argument_list|<
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|beforeExecute
argument_list|(
name|qFile
argument_list|)
expr_stmt|;
for|for
control|(
name|Callable
argument_list|<
name|Void
argument_list|>
name|c
range|:
name|preCommands
control|)
block|{
name|c
operator|.
name|call
argument_list|()
expr_stmt|;
block|}
name|String
index|[]
name|commands
init|=
name|beeLine
operator|.
name|getCommands
argument_list|(
name|qFile
operator|.
name|getInputFile
argument_list|()
argument_list|)
decl_stmt|;
name|execute
argument_list|(
name|qFile
operator|.
name|filterCommands
argument_list|(
name|commands
argument_list|)
argument_list|,
name|qFile
operator|.
name|getRawOutputFile
argument_list|()
argument_list|,
name|qFile
operator|.
name|getConverter
argument_list|()
argument_list|)
expr_stmt|;
name|afterExecute
argument_list|(
name|qFile
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|beeLine
operator|!=
literal|null
condition|)
block|{
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!quit"
block|}
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|beelineOutputStream
operator|!=
literal|null
condition|)
block|{
name|beelineOutputStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Builder to generated QFileBeeLineClient objects. The after initializing the builder, it can be    * used to create new clients without any parameters.    */
specifier|public
specifier|static
class|class
name|QFileClientBuilder
block|{
specifier|private
name|String
name|username
decl_stmt|;
specifier|private
name|String
name|password
decl_stmt|;
specifier|private
name|String
name|jdbcUrl
decl_stmt|;
specifier|private
name|String
name|jdbcDriver
decl_stmt|;
specifier|public
name|QFileClientBuilder
parameter_list|()
block|{     }
specifier|public
name|QFileClientBuilder
name|setUsername
parameter_list|(
name|String
name|username
parameter_list|)
block|{
name|this
operator|.
name|username
operator|=
name|username
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClientBuilder
name|setPassword
parameter_list|(
name|String
name|password
parameter_list|)
block|{
name|this
operator|.
name|password
operator|=
name|password
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClientBuilder
name|setJdbcUrl
parameter_list|(
name|String
name|jdbcUrl
parameter_list|)
block|{
name|this
operator|.
name|jdbcUrl
operator|=
name|jdbcUrl
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileClientBuilder
name|setJdbcDriver
parameter_list|(
name|String
name|jdbcDriver
parameter_list|)
block|{
name|this
operator|.
name|jdbcDriver
operator|=
name|jdbcDriver
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|QFileBeeLineClient
name|getClient
parameter_list|(
name|File
name|logFile
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|QFileBeeLineClient
argument_list|(
name|jdbcUrl
argument_list|,
name|jdbcDriver
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|logFile
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

