begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * See SQLLine notice in LICENSE  */
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
name|BufferedReader
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
name|InputStreamReader
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|StringTokenizer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|console
operator|.
name|completer
operator|.
name|StringsCompleter
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

begin_class
class|class
name|SQLCompleter
extends|extends
name|StringsCompleter
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
name|SQLCompleter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|SQLCompleter
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|completions
parameter_list|)
block|{
name|super
argument_list|(
name|completions
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|getSQLCompleters
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|,
name|boolean
name|skipmeta
parameter_list|)
throws|throws
name|IOException
throws|,
name|SQLException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|completions
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// add the default SQL completions
name|String
name|keywords
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|SQLCompleter
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/sql-keywords.properties"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|readLine
argument_list|()
decl_stmt|;
comment|// now add the keywords from the current connection
try|try
block|{
name|keywords
operator|+=
literal|","
operator|+
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getDatabaseMetaData
argument_list|()
operator|.
name|getSQLKeywords
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"fail to get SQL key words from database metadata due to the exception: "
operator|+
name|e
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|keywords
operator|+=
literal|","
operator|+
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getDatabaseMetaData
argument_list|()
operator|.
name|getStringFunctions
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"fail to get string function names from database metadata due to the exception: "
operator|+
name|e
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|keywords
operator|+=
literal|","
operator|+
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getDatabaseMetaData
argument_list|()
operator|.
name|getNumericFunctions
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"fail to get numeric function names from database metadata due to the exception: "
operator|+
name|e
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|keywords
operator|+=
literal|","
operator|+
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getDatabaseMetaData
argument_list|()
operator|.
name|getSystemFunctions
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"fail to get system function names from database metadata due to the exception: "
operator|+
name|e
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|keywords
operator|+=
literal|","
operator|+
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getDatabaseMetaData
argument_list|()
operator|.
name|getTimeDateFunctions
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"fail to get time date function names from database metadata due to the exception: "
operator|+
name|e
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// also allow lower-case versions of all the keywords
name|keywords
operator|+=
literal|","
operator|+
name|keywords
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
for|for
control|(
name|StringTokenizer
name|tok
init|=
operator|new
name|StringTokenizer
argument_list|(
name|keywords
argument_list|,
literal|", "
argument_list|)
init|;
name|tok
operator|.
name|hasMoreTokens
argument_list|()
condition|;
name|completions
operator|.
name|add
argument_list|(
name|tok
operator|.
name|nextToken
argument_list|()
argument_list|)
control|)
block|{
empty_stmt|;
block|}
comment|// now add the tables and columns from the current connection
if|if
condition|(
operator|!
operator|(
name|skipmeta
operator|)
condition|)
block|{
name|String
index|[]
name|columns
init|=
name|beeLine
operator|.
name|getColumnNames
argument_list|(
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getDatabaseMetaData
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|columns
operator|!=
literal|null
operator|&&
name|i
operator|<
name|columns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|completions
operator|.
name|add
argument_list|(
name|columns
index|[
name|i
operator|++
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|completions
return|;
block|}
block|}
end_class

end_unit

