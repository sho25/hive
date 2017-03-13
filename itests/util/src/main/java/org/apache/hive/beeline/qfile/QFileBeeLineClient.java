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
operator|.
name|qfile
package|;
end_package

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
specifier|public
name|boolean
name|execute
parameter_list|(
name|String
index|[]
name|commands
parameter_list|,
name|File
name|resultFile
parameter_list|)
block|{
name|boolean
name|hasErrors
init|=
literal|false
decl_stmt|;
name|beeLine
operator|.
name|runCommands
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!set outputformat csv"
block|,
literal|"!record "
operator|+
name|resultFile
operator|.
name|getAbsolutePath
argument_list|()
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
name|commands
operator|.
name|length
operator|!=
name|beeLine
operator|.
name|runCommands
argument_list|(
name|commands
argument_list|)
condition|)
block|{
name|hasErrors
operator|=
literal|true
expr_stmt|;
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
return|return
operator|!
name|hasErrors
return|;
block|}
specifier|private
name|void
name|beforeExecute
parameter_list|(
name|QFile
name|qFile
parameter_list|)
block|{
assert|assert
operator|(
name|execute
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"USE default;"
block|,
literal|"SHOW TABLES;"
block|,
literal|"DROP DATABASE IF EXISTS `"
operator|+
name|qFile
operator|.
name|getName
argument_list|()
operator|+
literal|"` CASCADE;"
block|,
literal|"CREATE DATABASE `"
operator|+
name|qFile
operator|.
name|getName
argument_list|()
operator|+
literal|"`;"
block|,
literal|"USE `"
operator|+
name|qFile
operator|.
name|getName
argument_list|()
operator|+
literal|"`;"
block|}
argument_list|,
name|qFile
operator|.
name|getInfraLogFile
argument_list|()
argument_list|)
operator|)
assert|;
block|}
specifier|private
name|void
name|afterExecute
parameter_list|(
name|QFile
name|qFile
parameter_list|)
block|{
assert|assert
operator|(
name|execute
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"USE default;"
block|,
literal|"DROP DATABASE IF EXISTS `"
operator|+
name|qFile
operator|.
name|getName
argument_list|()
operator|+
literal|"` CASCADE;"
block|,         }
argument_list|,
name|qFile
operator|.
name|getInfraLogFile
argument_list|()
argument_list|)
operator|)
assert|;
block|}
specifier|public
name|boolean
name|execute
parameter_list|(
name|QFile
name|qFile
parameter_list|)
block|{
name|beforeExecute
argument_list|(
name|qFile
argument_list|)
expr_stmt|;
name|boolean
name|result
init|=
name|execute
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"!run "
operator|+
name|qFile
operator|.
name|getInputFile
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
block|}
argument_list|,
name|qFile
operator|.
name|getRawOutputFile
argument_list|()
argument_list|)
decl_stmt|;
name|afterExecute
argument_list|(
name|qFile
argument_list|)
expr_stmt|;
return|return
name|result
return|;
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

